package tn.educanet.backend.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String code;           // code à 6 chiffres

    @Column(nullable = false)
    private LocalDateTime expiryDate;  // expire dans 15 minutes

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    private boolean used = false;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}