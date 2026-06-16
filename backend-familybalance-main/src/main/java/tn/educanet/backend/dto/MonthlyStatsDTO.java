package tn.educanet.backend.dto;

/**
 * One bucket on a monthly line/area chart.
 *
 *  - {@code month}  : short French label, e.g. "Jan", "Fév", "Mar".
 *  - {@code total}  : sum of amounts for that month.
 *
 * Returned as a sequence ordered chronologically by the dashboard endpoints
 * (oldest → newest), so the frontend can render it directly without
 * re-sorting.
 */
public class MonthlyStatsDTO {

    public String month;
    public double total;

    public MonthlyStatsDTO() { }

    public MonthlyStatsDTO(String month, double total) {
        this.month = month;
        this.total = total;
    }
}
