package tn.educanet.backend.service;

public interface FinancialContextService {

    /**
     * Builds a French financial summary (solde, revenus, charges, factures, budget).
     * Uses the provided familleId/membreId (sent by the frontend session) when
     * available; otherwise resolves the current user from the JWT/SecurityContext.
     * Returns an explicit "no data" block (never an empty string) when nothing can
     * be resolved, so the model is told NOT to invent figures.
     */
    String build(Integer familleId, Integer membreId, String username);

}
