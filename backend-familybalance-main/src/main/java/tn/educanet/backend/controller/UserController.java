package tn.educanet.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.UserService;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable int id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lookup used by the mobile frontend right after login to resolve the
     * currently authenticated user from the JWT subject.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> findByUsername(@PathVariable String username) {
        return userService.getByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/logout/{username}")
    public ResponseEntity<Void> logout(@PathVariable String username) {
        userService.logout(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/edit-profile/{id}")
    public ResponseEntity<User> editProfile(
            @PathVariable int id,
            @RequestBody User user) {
        return ResponseEntity.ok(userService.editProfile(id, user));
    }

    @PutMapping("/set-active/{id}")
    public ResponseEntity<Boolean> setActive(
            @PathVariable int id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(userService.setActive(id, active));
    }
    
   
    @PostMapping("/upload-photo/{id}")
    public ResponseEntity<User> uploadPhoto(
            @PathVariable int id,
            @RequestParam("file") MultipartFile file) throws Exception {

        String fileName = id + "_" + file.getOriginalFilename();
        Path uploadDir = Paths.get("uploads/");
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(fileName),
                   StandardCopyOption.REPLACE_EXISTING);

        String photoUrl = "http://localhost:8080/uploads/" + fileName;
        return ResponseEntity.ok(userService.updatePhoto(id, photoUrl));
    }

}