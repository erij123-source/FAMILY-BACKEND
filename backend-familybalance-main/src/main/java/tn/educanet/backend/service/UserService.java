package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.User;

public interface UserService {

    User save(User user);

    Optional<User> findById(int id);
    Optional<User> getByUsername(String username);
    Optional<User> getByPhone(String phone);  // ← ajout
    List<User> findAll();
    void deleteById(int id);

    User register(User user);
    void logout(String username);
    User editProfile(int id, User user);
    boolean setActive(int id, boolean active);
    
    User updatePhoto(int id, String photoUrl);
}