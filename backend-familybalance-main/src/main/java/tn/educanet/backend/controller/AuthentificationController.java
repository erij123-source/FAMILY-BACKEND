package tn.educanet.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.educanet.backend.config.JwtUtil;
import tn.educanet.backend.model.JwtResponse;
import tn.educanet.backend.model.LoginRequest;
import tn.educanet.backend.model.RegisterFamilyRequest;
import tn.educanet.backend.model.TokenRefreshRequest;
import tn.educanet.backend.persistence.Admin;
import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.persistence.RefreshToken;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.dao.AdminDao;
import tn.educanet.backend.dao.CompteDao;
import tn.educanet.backend.dao.FamilleDao;
import tn.educanet.backend.dao.MembreDao;
import tn.educanet.backend.service.RefreshTokenService;
import tn.educanet.backend.service.UserService;
import tn.educanet.backend.dao.PasswordResetTokenDao;
import tn.educanet.backend.model.ForgotPasswordRequest;
import tn.educanet.backend.model.ResetPasswordRequest;
import tn.educanet.backend.persistence.PasswordResetToken;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@RestController
@RequestMapping("/authentification")
public class AuthentificationController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordResetTokenDao passwordResetTokenDao;
    @Autowired
    private FamilleDao familleDao;
    @Autowired
    private CompteDao compteDao;
    @Autowired
    private MembreDao membreDao;
    @Autowired
    private AdminDao adminDao;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.getByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        String jwt = jwtUtil.generateToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        Optional<RefreshToken> tokenOpt = refreshTokenService.getByToken(request.getRefreshToken());
        if (!tokenOpt.isPresent()) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenService.verifyExpiration(tokenOpt.get());
        String jwt = jwtUtil.generateToken(refreshToken.getUser().getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken()));
    }

    @Transactional
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        Optional<User> userOpt = userService.getByPhone(request.getPhoneNumber());

        if (!userOpt.isPresent()) {
            return ResponseEntity.ok("Si ce compte existe, un code a été généré.");
        }

        User user = userOpt.get();

        passwordResetTokenDao.deleteByUser_Id(user.getId());

        String code = String.format("%04d", (int)(Math.random() * 10000));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setCode(code);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);
        passwordResetTokenDao.save(resetToken);

        System.out.println(">>> CODE SMS pour " + request.getPhoneNumber() + " : " + code);

        return ResponseEntity.ok("Code envoyé au " + request.getPhoneNumber());
    }

    /**
     * Atomic family bootstrap: creates the admin user, the famille, the compte,
     * the ADMINISTRATEUR membre and the admin row in a single transaction.
     * Used by the React Native mobile signup flow.
     */
    @Transactional
    @PostMapping("/register-family")
    public ResponseEntity<?> registerFamily(@RequestBody RegisterFamilyRequest request) {
        String step = "validate";
        try {
            if (request == null || request.getAdmin() == null
                    || request.getFamilyName() == null || request.getFamilyName().isBlank()
                    || request.getAdmin().getUsername() == null || request.getAdmin().getUsername().isBlank()
                    || request.getAdmin().getPassword() == null || request.getAdmin().getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Payload invalide."));
            }

            if (userService.getByUsername(request.getAdmin().getUsername()).isPresent()) {
                return ResponseEntity.status(409)
                        .body(Map.of("message", "Cet identifiant est déjà utilisé."));
            }

            step = "createUser";
            User admin = new User();
            admin.setUsername(request.getAdmin().getUsername());
            admin.setPassword(request.getAdmin().getPassword());
            admin.setFirstName(request.getAdmin().getFirstName());
            admin.setLastName(request.getAdmin().getLastName());
            admin.setPhone(request.getAdmin().getPhone());
            User savedUser = userService.register(admin);

            step = "createFamille";
            Famille famille = new Famille();
            famille.setName(request.getFamilyName());
            famille.setCreator(savedUser);
            Famille savedFamille = familleDao.save(famille);

            step = "createCompte";
            Compte compte = new Compte();
            compte.setFamille(savedFamille);
            compteDao.save(compte);

            step = "createMembre";
            Membre membre = new Membre();
            membre.setUser(savedUser);
            membre.setFamille(savedFamille);
            membre.setRole(Membre.Role.ADMINISTRATEUR);
            Membre savedMembre = membreDao.save(membre);

            step = "createAdmin";
            Admin adminRow = new Admin();
            adminRow.setUser(savedUser);
            adminRow.setFamille(savedFamille);
            adminDao.save(adminRow);

            step = "issueTokens";
            String jwt = jwtUtil.generateToken(savedUser.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("refreshToken", refreshToken.getToken());
            response.put("userId", savedUser.getId());
            response.put("username", savedUser.getUsername());
            response.put("familleId", savedFamille.getId());
            response.put("familleName", savedFamille.getName());
            response.put("membreId", savedMembre.getId());
            response.put("role", savedMembre.getRole().name());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            Throwable root = ex;
            while (root.getCause() != null && root.getCause() != root) root = root.getCause();
            String msg = root.getClass().getSimpleName() + ": " + root.getMessage();
            System.err.println("[register-family] failed at step=" + step + " → " + msg);
            ex.printStackTrace();
            Map<String, Object> body = new HashMap<>();
            body.put("step", step);
            body.put("error", root.getClass().getSimpleName());
            body.put("message", msg);
            return ResponseEntity.status(500).body(body);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<PasswordResetToken> tokenOpt =
            passwordResetTokenDao.findByCodeAndUsedFalse(request.getCode());

        if (!tokenOpt.isPresent()) {
            return ResponseEntity.status(400).body("Code invalide ou déjà utilisé.");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Code expiré. Veuillez recommencer.");
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(request.getNewPassword()));
        userService.save(user);

        resetToken.setUsed(true);
        passwordResetTokenDao.save(resetToken);

        return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
    }
}
