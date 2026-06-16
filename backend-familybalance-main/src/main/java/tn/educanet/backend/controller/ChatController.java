package tn.educanet.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.educanet.backend.model.ChatRequest;
import tn.educanet.backend.model.ChatResponse;
import tn.educanet.backend.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = chatService.ask(
                request.getMessage(),
                request.getHistory(),
                request.getFamilleId(),
                request.getMembreId(),
                request.getUsername());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

}
