package tn.educanet.backend.model;

import java.util.List;
import java.util.Map;

public class ChatRequest {

    private String message;

    /**
     * Prior conversation turns, oldest first. Each entry: {"role":"user"|"bot","text":"..."}.
     * Optional — when absent the assistant answers without conversational memory.
     */
    private List<Map<String, String>> history;

    /**
     * Identifiers sent by the frontend (from the session) so the backend can build
     * the financial summary reliably, independently of the short-lived JWT. When
     * null, the backend falls back to the authenticated principal.
     */
    private Integer familleId;
    private Integer membreId;
    private String username;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, String>> getHistory() {
        return history;
    }

    public void setHistory(List<Map<String, String>> history) {
        this.history = history;
    }

    public Integer getFamilleId() {
        return familleId;
    }

    public void setFamilleId(Integer familleId) {
        this.familleId = familleId;
    }

    public Integer getMembreId() {
        return membreId;
    }

    public void setMembreId(Integer membreId) {
        this.membreId = membreId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
