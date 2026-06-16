package tn.educanet.backend.dto;

/**
 * One slice of a pie/donut chart — used by the by-category and by-member
 * endpoints. Always plain numbers + a label, no entity payload, so the
 * frontend never sees Hibernate proxies.
 */
public class PieChartDTO {

    public String label;
    public double value;

    public PieChartDTO() { }

    public PieChartDTO(String label, double value) {
        this.label = label;
        this.value = value;
    }
}
