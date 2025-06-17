package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.controllers;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.*;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.request.TweetReactionRequest;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reactions")
public class TweetReactionController {

    @Autowired
    private TweetReactionRepository tweetReactionRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    // ✅ Agregar o actualizar reacción SIN JWT
    @PostMapping("/react")
    public ResponseEntity<?> reactToTweet(@RequestBody TweetReactionRequest request) {
        // Obtener user fijo (ID 1) — reemplaza según sea necesario
        Optional<User> userOpt = userRepository.findById(1L);
        Optional<Tweet> tweetOpt = tweetRepository.findById(request.getTweetId());
        Optional<Reaction> reactionOpt = reactionRepository.findById(request.getReactionId());

        if (userOpt.isEmpty() || tweetOpt.isEmpty() || reactionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Datos inválidos");
        }

        User user = userOpt.get();
        Tweet tweet = tweetOpt.get();
        Reaction newReaction = reactionOpt.get();

        Optional<TweetReaction> existingReactionOpt = tweetReactionRepository.findByTweetAndUser(tweet, user);

        if (existingReactionOpt.isPresent()) {
            TweetReaction existing = existingReactionOpt.get();

            if (existing.getReaction().getId().equals(newReaction.getId())) {
                return ResponseEntity.ok(Map.of(
                    "message", "Ya reaccionaste con esta reacción",
                    "reaction", newReaction.getName().name()
                ));
            }

            existing.setReaction(newReaction);
            tweetReactionRepository.save(existing);
            return ResponseEntity.ok(Map.of(
                "message", "Reacción actualizada",
                "reaction", newReaction.getName().name()
            ));
        } else {
            TweetReaction tweetReaction = new TweetReaction();
            tweetReaction.setTweet(tweet);
            tweetReaction.setUser(user);
            tweetReaction.setReaction(newReaction);
            tweetReactionRepository.save(tweetReaction);
            return ResponseEntity.ok(Map.of(
                "message", "Reacción registrada",
                "reaction", newReaction.getName().name()
            ));
        }
    }

    // ❌ Eliminar reacción sin autenticación
    @DeleteMapping("/tweet/{tweetId}")
    public ResponseEntity<?> removeReaction(@PathVariable Long tweetId) {
        Optional<User> userOpt = userRepository.findById(1L);
        Optional<Tweet> tweetOpt = tweetRepository.findById(tweetId);

        if (userOpt.isEmpty() || tweetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Datos inválidos"
            ));
        }

        Optional<TweetReaction> existingReaction = tweetReactionRepository.findByTweetAndUser(tweetOpt.get(), userOpt.get());

        if (existingReaction.isPresent()) {
            tweetReactionRepository.delete(existingReaction.get());
            return ResponseEntity.ok(Map.of(
                "message", "Reacción eliminada exitosamente 💥"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "No se encontró una reacción para eliminar ❌"
            ));
        }
    }

    // 📊 Contar reacciones
    @GetMapping("/count/tweet/{tweetId}")
    public ResponseEntity<?> countReactionsByType(@PathVariable Long tweetId) {
        List<TweetReaction> reactions = tweetReactionRepository.findByTweetId(tweetId);

        Map<String, Long> countByType = reactions.stream()
            .filter(r -> r.getReaction() != null && r.getReaction().getName() != null)
            .collect(Collectors.groupingBy(
                r -> r.getReaction().getName().name(),
                Collectors.counting()
            ));

        return ResponseEntity.ok(countByType);
    }
}
