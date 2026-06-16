package tn.educanet.backend.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tn.educanet.backend.service.FactureService;

/**
 * Tâche planifiée : chaque jour à minuit, met à jour les factures
 * NON_PAYEE dont la dueDate est dépassée → EN_RETARD en base.
 *
 * Pour activer, ajouter @EnableScheduling sur votre classe principale
 * ou sur une classe @Configuration.
 */
@Component
public class FactureScheduler {

    @Autowired
    private FactureService factureService;

    /** Tous les jours à 00:05 */
    @Scheduled(cron = "0 5 0 * * *")
    public void updateOverdueFactures() {
        int count = factureService.updateOverdueFactures();
        System.out.println("[Scheduler] " + count + " facture(s) passée(s) EN_RETARD.");
    }

}