package tn.educanet.backend.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tn.educanet.backend.service.AdminService;

/**
 * Tâche planifiée du roulement mensuel des budgets.
 *
 * — resetBudgetsForNewMonth : le 1er de chaque mois à 00:02, fige les reports
 *   (reste à vivre par membre + revenu non distribué de la famille) du mois
 *   écoulé vers le nouveau mois.
 *
 * Pourquoi un job planifié : le reset était jusqu'ici déclenché UNIQUEMENT côté
 * client (au premier login du mois). Si personne ne se connecte pendant un mois
 * entier, ce mois est sauté et les reports sont perdus. Le job garantit le
 * roulement même sans connexion. L'opération est idempotente (garde par
 * mois/année estampillé sur Membre et Compte), donc le déclenchement client
 * et ce job peuvent coexister sans double comptage.
 *
 * Activé globalement par @EnableScheduling sur l'application Spring Boot.
 */
@Component
public class BudgetScheduler {

    @Autowired
    private AdminService adminService;

    /** 1er jour du mois à 00:02 — cron "sec min h jour mois jourSemaine". */
    @Scheduled(cron = "0 2 0 1 * *")
    public void rolloverBudgets() {
        adminService.resetBudgetsForNewMonth();
        System.out.println("[BudgetScheduler] roulement mensuel des budgets exécuté.");
    }
}
