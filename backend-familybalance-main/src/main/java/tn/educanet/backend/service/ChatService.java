package tn.educanet.backend.service;

import java.util.List;
import java.util.Map;

public interface ChatService {

    /**
     * Sends the user's message (plus optional prior turns and identifiers) to the
     * Gemini model and returns the assistant's reply. Never throws: on any failure
     * a user-friendly French message is returned instead.
     *
     * @param message   the new user message
     * @param history   prior turns, oldest first, each as {"role":"user"|"bot","text":"..."} (may be null)
     * @param familleId family id from the frontend session (may be null -> fall back to JWT)
     * @param membreId  membre id from the frontend session (may be null)
     */
    String ask(String message, List<Map<String, String>> history, Integer familleId, Integer membreId, String username);

}
