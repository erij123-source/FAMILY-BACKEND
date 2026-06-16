package tn.educanet.backend.service;

import java.util.List;

import tn.educanet.backend.persistence.Notification;

public interface NotificationService {

    /** Listing par famille — kept for back-compat (admin global views).
     *  L'UI utilise désormais le listing par membre. */
    List<Notification> findByFamille(int familleId);

    /** Listing scoped au membre connecté (mois courant uniquement).
     *  C'est l'endpoint principal du listing UI. */
    List<Notification> findByMembreCurrentMonth(int membreId);

    Notification add(Notification notification);
    Notification setRead(int id, boolean read);
    void delete(int id);
    int markAllReadForMembre(int membreId);
    int clearReadForMembre(int membreId);
    long countUnreadForMembre(int membreId);

    /**
     * Scan l'état courant pour TOUS les membres de la famille et crée des
     * notifications scopées par membre :
     *   - Factures : J-3, J-1, en retard (basé sur la due date du membre).
     *   - Budget   : 1/4 restant, 1/8 restant, totalement épuisé.
     * Règle admin spécifique : l'admin reçoit aussi une notification quand
     * le budget d'un autre membre est totalement épuisé (mais PAS pour les
     * seuils 1/4 ou 1/8).
     * Idempotent — ne recrée pas une notification déjà présente (même titre,
     * même mois). Retourne le nombre créé.
     */
    int generateForFamille(int familleId);

    /** Purge les notifications créées avant le 1er du mois courant.
     *  Utilisé par le scheduler mensuel pour "vider l'historique chaque mois". */
    int purgePreviousMonths();
}
