package tn.educanet.backend.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tn.educanet.backend.service.NotificationService;

/**
 * Tâches planifiées dédiées aux notifications.
 *
 * — purgeOldNotifications : le 1er de chaque mois à 00:01, supprime tout
 *   l'historique des notifications antérieures au mois courant (règle métier
 *   "la page des notifications doit être vidée chaque mois").
 *
 * Activé globalement par @EnableScheduling sur l'application Spring Boot.
 */
@Component
public class NotificationScheduler {

    @Autowired
    private NotificationService notificationService;

    /** 1er jour du mois à 00:01 — cron "sec min h jour mois jourSemaine". */
    @Scheduled(cron = "0 1 0 1 * *")
    public void purgeOldNotifications() {
        int removed = notificationService.purgePreviousMonths();
        System.out.println("[NotificationScheduler] purge mensuelle : "
            + removed + " notification(s) supprimée(s).");
    }
}
