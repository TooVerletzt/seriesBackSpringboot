package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.controllers;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.*;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.request.CommentCreateRequest;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.request.CommentUpdateRequest;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.response.CommentResponse;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository.CommentRepository;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository.TweetRepository;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Crear comentario con usuario autenticado (via JWT)
    @PostMapping("/create/{tweetId}")
    public ResponseEntity<?> createComment(@PathVariable Long tweetId,
                                           @Valid @RequestBody CommentCreateRequest request,
                                           Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Tweet> tweetOpt = tweetRepository.findById(tweetId);

        if (tweetOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tweet o usuario no encontrado"));
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setTweet(tweetOpt.get());
        comment.setUser(userOpt.get());

        Comment saved = commentRepository.save(comment);
        return ResponseEntity.ok(Map.of("message", "Comentario creado correctamente 💬", "id", saved.getId()));
    }

    // ✅ Obtener comentarios por tweet
    @GetMapping("/tweet/{tweetId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByTweet(@PathVariable Long tweetId) {
        List<Comment> comments = commentRepository.findByTweetId(tweetId);

        List<CommentResponse> response = comments.stream()
            .map(c -> new CommentResponse(
                c.getId(),
                c.getContent(),
                c.getCreatedAt(),
                (c.getUser() != null) ? c.getUser().getUsername() : "Anónimo"
            )).toList();

        return ResponseEntity.ok(response);
    }

    // ✅ Editar comentario libremente
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @Valid @RequestBody CommentUpdateRequest request) {

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = commentOpt.get();
        comment.setContent(request.getContent());
        commentRepository.save(comment);

        return ResponseEntity.ok(Map.of("message", "Comentario actualizado correctamente ✏️"));
    }

    // ✅ Eliminar comentario libremente
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        commentRepository.delete(commentOpt.get());
        return ResponseEntity.ok(Map.of("message", "Comentario eliminado exitosamente 🗑️"));
    }
}
