package tn.educanet.backend.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Notification;

@Repository
public interface NotificationDao extends JpaRepository<Notification, Integer> {
    List<Notification> findByFamilleIdOrderByCreatedAtDesc(int familleId);
    List<Notification> findByMembreIdOrderByCreatedAtDesc(int membreId);
    long countByFamilleIdAndReadFlagFalse(int familleId);
    long countByMembreIdAndReadFlagFalse(int membreId);

    /** Notifications du mois courant pour un membre — alimente la liste UI
     *  côté web/mobile (l'historique est vidé chaque mois côté affichage). */
    @Query("SELECT n FROM Notification n WHERE n.membre.id = :membreId "
         + "AND n.createdAt >= :from ORDER BY n.createdAt DESC")
    List<Notification> findByMembreSince(@Param("membreId") int membreId,
                                          @Param("from") LocalDateTime from);

    /** Purge la totalité des notifications strictement antérieures à
     *  `before` — appelé par le scheduler mensuel le 1er du mois à 00:01. */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :before")
    int deleteAllBefore(@Param("before") LocalDateTime before);
}
