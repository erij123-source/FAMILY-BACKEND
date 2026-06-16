package tn.educanet.backend.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "`admin`")
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "idFamille", nullable = false)
    private Famille famille;

    @Column(nullable = false, updatable = false)
    private LocalDateTime nominationDate;

    public Admin() {
        this.nominationDate = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Famille getFamille() { return famille; }
    public void setFamille(Famille famille) { this.famille = famille; }

    public LocalDateTime getNominationDate() { return nominationDate; }
    public void setNominationDate(LocalDateTime nominationDate) { this.nominationDate = nominationDate; }

}