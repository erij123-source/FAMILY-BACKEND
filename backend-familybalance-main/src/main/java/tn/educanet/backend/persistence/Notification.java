package tn.educanet.backend.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Business bucket — drives the filter pills on the frontend. */
    public enum Kind { REMINDER, ALERT, SUGGESTION }

    /** Visual hierarchy — drives the colour of the card. */
    public enum Severity { CRITICAL, WARNING, INFO, SUGGESTION }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** A notification belongs to a famille so every family member can see
     *  family-level events. Some notifications target a specific member,
     *  in which case `membre` is also set. */
    @ManyToOne
    @JoinColumn(name = "idFamille", nullable = false)
    private Famille famille;

    @ManyToOne
    @JoinColumn(name = "idMembre")
    private Membre membre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Kind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Severity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    /** Material Icons name shown next to the title (e.g. "warning"). */
    @Column(length = 40)
    private String icon;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean readFlag;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.readFlag = false;
        this.kind = Kind.ALERT;
        this.severity = Severity.INFO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Famille getFamille() { return famille; }
    public void setFamille(Famille famille) { this.famille = famille; }

    public Membre getMembre() { return membre; }
    public void setMembre(Membre membre) { this.membre = membre; }

    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReadFlag() { return readFlag; }
    public void setReadFlag(boolean readFlag) { this.readFlag = readFlag; }
}
