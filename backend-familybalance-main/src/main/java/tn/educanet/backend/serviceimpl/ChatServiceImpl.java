package tn.educanet.backend.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tn.educanet.backend.service.ChatService;
import tn.educanet.backend.service.FinancialContextService;

/**
 * Relays chat messages to Google's Gemini Flash model via its REST API.
 * The API key is read from configuration (env var GEMINI_API_KEY or the
 * gemini.api.key property) so it never lives in the frontend. The user's real
 * financial data is injected into the system instruction, and prior turns are
 * forwarded so the conversation has memory.
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com";

    /** Max conversation turns kept from the history to bound the prompt size. */
    private static final int MAX_HISTORY = 12;

    private static final String SYSTEM_PROMPT =
            "Tu es l'assistant financier de l'application FamilyBalance, destinee aux familles. "
            + "REGLE ABSOLUE : utilise UNIQUEMENT les chiffres presents dans le bloc 'DONNEES FINANCIERES' ci-dessous. "
            + "Il est STRICTEMENT INTERDIT d'inventer, de supposer ou d'estimer des montants (loyer, salaire, charges, etc.) "
            + "qui ne figurent pas dans ce bloc. "
            + "Tous les montants sont dans la devise indiquee dans le bloc (par defaut le dinar tunisien, note DT ou TND) : "
            + "n'utilise JAMAIS l'euro (EUR) ni le dollar. "
            + "Si le bloc indique qu'aucune donnee n'est disponible, ou si un chiffre demande n'y figure pas, "
            + "dis-le honnetement et invite l'utilisateur a saisir ou verifier ses donnees dans l'application — n'invente rien. "
            + "Definition : le 'reste a vivre' = revenus du mois - charges du mois. "
            + "Reponds en francais, de maniere claire, concise et bienveillante, et reste dans le domaine financier et familial.";

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    /** Modèle de secours utilisé si le principal est surchargé (503) ou KO. */
    @Value("${gemini.model.fallback:gemini-2.5-flash-lite}")
    private String fallbackModel;

    @Autowired
    private FinancialContextService financialContextService;

    private final RestClient restClient = RestClient.create();

    @Override
    public String ask(String message, List<Map<String, String>> history, Integer familleId, Integer membreId, String username) {
        if (apiKey == null || apiKey.isBlank()) {
            return "L'assistant n'est pas encore configure (cle API Gemini manquante).";
        }
        if (message == null || message.isBlank()) {
            return "Posez-moi une question sur votre budget familial 🙂";
        }

        // System instruction = role prompt + the user's real financial data.
        List<Map<String, Object>> systemParts = new ArrayList<>();
        systemParts.add(Map.of("text", SYSTEM_PROMPT));
        String context = financialContextService.build(familleId, membreId, username);
        if (context != null && !context.isBlank()) {
            systemParts.add(Map.of("text", context));
        }

        // Multi-turn conversation: prior turns (bounded) + the new user message.
        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - MAX_HISTORY);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> turn = history.get(i);
                if (turn == null) {
                    continue;
                }
                String text = turn.get("text");
                if (text == null || text.isBlank()) {
                    continue;
                }
                String role = "user".equalsIgnoreCase(turn.get("role")) ? "user" : "model";
                // Gemini requires the conversation to start with a user turn.
                if (contents.isEmpty() && "model".equals(role)) {
                    continue;
                }
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", text))));
            }
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", message))));

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", systemParts),
                "contents", contents);

        // Gemini renvoie souvent un 503 "high demand" passager. On ré-essaie sur
        // les erreurs transitoires (503/429/5xx/timeout) avec un petit backoff,
        // puis on bascule sur le modèle de secours avant d'abandonner.
        java.util.LinkedHashSet<String> models = new java.util.LinkedHashSet<>();
        if (model != null && !model.isBlank()) models.add(model.trim());
        if (fallbackModel != null && !fallbackModel.isBlank()) models.add(fallbackModel.trim());
        if (models.isEmpty()) models.add("gemini-2.5-flash");

        String lastError = null;
        for (String mdl : models) {
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = restClient.post()
                            .uri(GEMINI_BASE + "/v1beta/models/" + mdl + ":generateContent")
                            .header("x-goog-api-key", apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body)
                            .retrieve()
                            .body(Map.class);
                    return extractText(response);
                } catch (org.springframework.web.client.HttpStatusCodeException e) {
                    int sc = e.getStatusCode().value();
                    lastError = mdl + " HTTP " + sc;
                    System.err.println("[ChatService] Gemini " + mdl + " HTTP " + sc + " : " + e.getMessage());
                    // 4xx (sauf 429) = requête/clé invalide : inutile de réessayer ce modèle.
                    boolean transientErr = sc == 429 || sc == 500 || sc == 502 || sc == 503 || sc == 504;
                    if (!transientErr) break;
                    sleepQuiet(600L * attempt);
                } catch (org.springframework.web.client.ResourceAccessException e) {
                    lastError = mdl + " timeout/io : " + e.getMessage();
                    System.err.println("[ChatService] Gemini " + mdl + " IO : " + e.getMessage());
                    sleepQuiet(600L * attempt);
                } catch (Exception e) {
                    lastError = mdl + " : " + e.getMessage();
                    System.err.println("[ChatService] Gemini " + mdl + " erreur : " + e.getMessage());
                    break;
                }
            }
        }
        System.err.println("[ChatService] Gemini indisponible après retries — " + lastError);
        return "L'assistant IA est momentanément très sollicité. Réessayez dans quelques secondes.";
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        if (response == null) {
            return "Reponse vide du modele.";
        }
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "Je n'ai pas de reponse a fournir pour cette question.";
            }
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            StringBuilder sb = new StringBuilder();
            if (parts != null) {
                for (Map<String, Object> part : parts) {
                    Object t = part.get("text");
                    if (t != null) {
                        sb.append(t.toString());
                    }
                }
            }
            String text = sb.toString().trim();
            return text.isEmpty() ? "Je n'ai pas de reponse a fournir pour cette question." : text;
        } catch (Exception e) {
            return "Je n'ai pas pu interpreter la reponse du modele.";
        }
    }

}
