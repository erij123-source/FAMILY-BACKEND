package tn.educanet.backend.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.UserDao;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.UserService;

@Service
public class __UserServiceImpl__ implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findById(int id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public Optional<User> getByPhone(String phone) {       
        return userDao.findByPhone(phone);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public void deleteById(int id) {
        userDao.deleteById(id);
    }

    @Override
    public User register(User user) {
        // Téléphone unique sur toute l'application (le nom, lui, reste libre :
        // deux membres peuvent porter le même nom tant que le téléphone diffère).
        if (user.getPhone() != null && !user.getPhone().isBlank()
                && userDao.findByPhone(user.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce numéro de téléphone est déjà utilisé.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        return userDao.save(user);
    }

    @Override
    public void logout(String username) {
    }

    @Override
    public User editProfile(int id, User updatedUser) {
        return userDao.findById(id).map(user -> {
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setPhone(updatedUser.getPhone());
            user.setUsername(updatedUser.getUsername());
            return userDao.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean setActive(int id, boolean active) {
        return userDao.findById(id).map(user -> {
            user.setActive(active);
            userDao.save(user);
            return active;
        }).orElse(false);
    }

    @Override
    public User save(User user) {
        return userDao.save(user);
    }
    
    @Override
    public User updatePhoto(int id, String photoUrl) {
        return userDao.findById(id).map(user -> {
            user.setPhoto(photoUrl);
            return userDao.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}