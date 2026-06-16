package tn.educanet.backend.persistence;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;

@Entity
public class Facture implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Statut {
        PAYEE, NON_PAYEE, EN_RETARD
    }

    public enum Type {
        FIXE, VARIABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idCompte", nullable = false)
    private Compte compte;

    @ManyToOne
    @JoinColumn(name = "idCategorie")
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "idMembre", nullable = false)
    private Membre membre;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDate issueDate;

    private LocalDate dueDate;

    // ✅ NOUVEAU — date réelle du paiement (null si pas encore payée)
    private LocalDate datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut;

    // Type Fixe / Variable saisi par l'utilisateur dans le modal Ajouter/Modifier.
    // Indépendant de la catégorie (par exemple : Électricité peut être déclarée
    // VARIABLE si l'utilisateur le choisit). Nullable côté JPA pour permettre
    // l'ajout de la colonne sur des bases existantes via ddl-auto=update —
    // les nouveaux enregistrements partent à VARIABLE (cf. constructeur).
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Type type;

    // URL relative vers le fichier téléchargé (ex. "/factures/files/123_facture.pdf").
    // Le fichier réel est stocké sur disque par l'endpoint multipart
    // /factures/upload/{id} ; on ne garde en base que le chemin court (VARCHAR(255)).
    private String fileUrl;

    // Nom de fichier original (pour l'affichage et le téléchargement côté UI).
    private String fileName;

    public Facture() {
        this.statut = Statut.NON_PAYEE;
        this.issueDate = LocalDate.now();
        this.type = Type.VARIABLE;
    }

    /**
     * Après chaque chargement depuis la base, si la facture est NON_PAYEE
     * et que la dueDate est dépassée, on passe automatiquement à EN_RETARD.
     */
    @PostLoad
    public void checkIfLate() {
        if (this.statut == Statut.NON_PAYEE
                && this.dueDate != null
                && this.dueDate.isBefore(LocalDate.now())) {
            this.statut = Statut.EN_RETARD;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Membre getMembre() { return membre; }
    public void setMembre(Membre membre) { this.membre = membre; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    // ✅ NOUVEAU getter/setter datePaiement
    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}