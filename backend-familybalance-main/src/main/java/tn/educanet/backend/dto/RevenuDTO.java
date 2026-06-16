package tn.educanet.backend.dto;

import tn.educanet.backend.persistence.Revenu;

/**
 * Merged RevenuDTO — exposes BOTH frontends' field expectations:
 *   - Angular web app expects: {@code type}, {@code categorieNom}
 *   - React Native mobile app expects: {@code categorieId}, {@code categorieName}
 * All five fields are populated so each frontend sees what it needs.
 */
public class RevenuDTO {

    public int id;
    public double amount;
    public String kind;
    public String perceptionDate;
    public String description;
    public String type;
    public int membreId;
    public String membrePrenom;
    public String membreNom;

    // Category fields kept for both frontends:
    public Integer categorieId;
    public String categorieName;
    public String categorieNom;

    public static RevenuDTO from(Revenu r) {
        RevenuDTO dto = new RevenuDTO();
        dto.id             = r.getId();
        dto.amount         = r.getAmount();
        dto.kind           = r.getKind() != null ? r.getKind().name() : "FIXE";
        dto.perceptionDate = r.getPerceptionDate() != null
                             ? r.getPerceptionDate().toString()
                             : null;
        dto.description    = r.getDescription();
        dto.type           = r.getType();
        dto.membreId       = r.getMembre().getId();
        dto.membrePrenom   = r.getMembre().getUser().getFirstName();
        dto.membreNom      = r.getMembre().getUser().getLastName();

        if (r.getCategorie() != null) {
            dto.categorieId   = r.getCategorie().getId();
            dto.categorieName = r.getCategorie().getName();
            dto.categorieNom  = r.getCategorie().getName();
        } else {
            dto.categorieNom  = "Sans catégorie";
        }

        return dto;
    }
}
