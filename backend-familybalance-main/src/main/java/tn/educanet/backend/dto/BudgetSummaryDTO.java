package tn.educanet.backend.dto;

/**
 * Live snapshot of a member's monthly budget situation.
 *
 *  - baseBudget       : monthly allowance set by the admin on the Membre row
 *  - carryover        : unspent portion rolled over from last month (>= 0)
 *  - effectiveBudget  : baseBudget + carryover
 *  - depensesMois     : sum of charges + factures PAYEE for the current month
 *  - restAvivre       : effectiveBudget - depensesMois (floored at 0 for display)
 *  - exhausted        : true when budget set AND fully consumed.
 *                       Used by the spend-guard to block new charges/factures/revenus.
 */
public class BudgetSummaryDTO {

    public double baseBudget;
    public double carryover;
    public double effectiveBudget;
    public double depensesMois;
    public double restAvivre;
    public boolean exhausted;

    public BudgetSummaryDTO() { }

    public BudgetSummaryDTO(double baseBudget, double carryover, double effectiveBudget,
                            double depensesMois, double restAvivre, boolean exhausted) {
        this.baseBudget = baseBudget;
        this.carryover = carryover;
        this.effectiveBudget = effectiveBudget;
        this.depensesMois = depensesMois;
        this.restAvivre = restAvivre;
        this.exhausted = exhausted;
    }
}
