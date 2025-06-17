package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.controllers;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.response.CommentResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.Comment;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.Tweet;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.request.CommentCreateRequest;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.request.CommentUpdateRequest;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository.CommentRepository;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository.TweetRepository;

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

    // ✅ Crear comentario sin autenticación
    @PostMapping("/create/{tweetId}")
    public ResponseEntity<?> createComment(@PathVariable Long tweetId,
                                           @Valid @RequestBody CommentCreateRequest request) {

        Optional<Tweet> tweetOpt = tweetRepository.findById(tweetId);

        if (tweetOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tweet no encontrado"));
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setTweet(tweetOpt.get());
        comment.setUser(null); // sin usuario

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
