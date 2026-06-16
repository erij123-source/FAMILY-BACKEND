package tn.educanet.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/otp")
public class OtpController {

    
    private static final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static class OtpEntry {
        String code;
        LocalDateTime expiry;
        OtpEntry(String code) {
            this.code   = code;
            this.expiry = LocalDateTime.now().plusMinutes(10);
        }
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Numéro de téléphone requis."));
        }

        String code = String.format("%04d", (int)(Math.random() * 10000));

        otpStore.put(phone, new OtpEntry(code));

       
        System.out.println(">>> OTP pour " + phone + " : " + code);

        return ResponseEntity.ok(Map.of("message", "Code OTP envoyé au " + phone));
    }

   
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code  = body.get("code");

        if (phone == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Téléphone et code requis."));
        }

        OtpEntry entry = otpStore.get(phone);

        if (entry == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Aucun code envoyé pour ce numéro."));
        }

        if (entry.isExpired()) {
            otpStore.remove(phone);
            return ResponseEntity.status(400).body(Map.of("message", "Code expiré. Veuillez renvoyer un nouveau code."));
        }

        if (!entry.code.equals(code)) {
            return ResponseEntity.status(400).body(Map.of("message", "Code incorrect."));
        }

        otpStore.remove(phone);

        return ResponseEntity.ok(Map.of("message", "Numéro vérifié avec succès."));
    }
}