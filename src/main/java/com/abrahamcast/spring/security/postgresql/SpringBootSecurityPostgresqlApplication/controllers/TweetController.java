package com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.controllers;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.models.*;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.payload.response.*;
import com.abrahamcast.spring.security.postgresql.SpringBootSecurityPostgresqlApplication.repository.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tweets")
public class TweetController {

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path uploadDir = Paths.get("uploads");

    @GetMapping("/all")
    public ResponseEntity<List<TweetResponse>> getAllTweets() {
        List<Tweet> tweets = tweetRepository.findAllWithComments();

        List<TweetResponse> response = new ArrayList<>();

        for (Tweet tweet : tweets) {
            List<CommentResponse> commentDTOs = tweet.getComments().stream().map(comment ->
                new CommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    comment.getUser() != null ? comment.getUser().getUsername() : "Anónimo"
                )
            ).toList();

            Map<String, Long> rawCount = tweet.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction().getName() != null)
                .collect(Collectors.groupingBy(
                    r -> r.getReaction().getName().name(),
                    Collectors.counting()
                ));

            Map<String, Integer> reactionCount = new LinkedHashMap<>();
            for (EReaction er : EReaction.values()) {
                long count = rawCount.getOrDefault(er.name(), 0L);
                reactionCount.put(er.name(), (int) count);
            }

            reactionCount = reactionCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));

            UserMinResponse autor = tweet.getPostedBy() != null
                ? new UserMinResponse(tweet.getPostedBy().getId(), tweet.getPostedBy().getUsername())
                : new UserMinResponse(0L, "Anónimo");

            TweetResponse tweetDTO = new TweetResponse(
                tweet.getId(),
                tweet.getTweet(),
                tweet.getImageUrl(),
                autor,
                commentDTOs,
                reactionCount
            );

            response.add(tweetDTO);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTweet(
            @RequestParam("tweet") String tweetText,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Authentication authentication) {

        try {
            Tweet tweet = new Tweet(tweetText);

            // Obtener usuario autenticado (si lo hay)
            if (authentication != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                userOpt.ifPresent(tweet::setPostedBy);
            }

            // Guardar imagen si viene
            if (imageFile != null && !imageFile.isEmpty()) {
                String url = saveImage(imageFile);
                if (url == null) {
                    return ResponseEntity.badRequest().body("Formato de imagen no permitido. Solo JPG y PNG.");
                }
                tweet.setImageUrl(url);
            }

            Tweet saved = tweetRepository.save(tweet);

            UserMinResponse autor = tweet.getPostedBy() != null
                ? new UserMinResponse(tweet.getPostedBy().getId(), tweet.getPostedBy().getUsername())
                : new UserMinResponse(0L, "Anónimo");

            TweetResponse response = new TweetResponse(
                    saved.getId(),
                    saved.getTweet(),
                    saved.getImageUrl(),
                    autor,
                    null,
                    Map.of()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTweet(@PathVariable Long id, Authentication authentication) {
        Optional<Tweet> tweetOpt = tweetRepository.findById(id);
        if (tweetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Tweet no encontrado"));
        }

        Tweet tweet = tweetOpt.get();
        String authUsername = authentication != null ? authentication.getName() : null;
        String tweetOwner = tweet.getPostedBy() != null ? tweet.getPostedBy().getUsername() : null;

        if (tweetOwner != null && !tweetOwner.equals(authUsername)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No puedes eliminar este tweet"));
        }

        if (tweet.getImageUrl() != null) {
            String filePath = tweet.getImageUrl().replace("/uploads/", "");
            Path imagePath = Paths.get("uploads", filePath);
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.out.println("No se pudo eliminar la imagen: " + e.getMessage());
            }
        }

        tweetRepository.delete(tweet);
        return ResponseEntity.ok(Map.of("message", "Tweet eliminado correctamente 🗑️"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTweet(
            @PathVariable Long id,
            @RequestParam("tweet") String newText,
            @RequestParam(value = "image", required = false) MultipartFile newImage,
            @RequestParam(value = "removeImage", required = false, defaultValue = "false") boolean removeImage,
            Authentication authentication) {

        Optional<Tweet> tweetOpt = tweetRepository.findById(id);
        if (tweetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Tweet no encontrado"));
        }

        Tweet tweet = tweetOpt.get();
        String authUsername = authentication != null ? authentication.getName() : null;
        String tweetOwner = tweet.getPostedBy() != null ? tweet.getPostedBy().getUsername() : null;

        if (tweetOwner != null && !tweetOwner.equals(authUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No puedes editar este tweet"));
        }

        tweet.setTweet(newText);

        if (removeImage) {
            tweet.setImageUrl(null);
        }

        if (newImage != null && !newImage.isEmpty()) {
            try {
                String newUrl = saveImage(newImage);
                if (newUrl == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Formato de imagen inválido. Solo JPG y PNG."));
                }
                tweet.setImageUrl(newUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Error al guardar imagen: " + e.getMessage())
                );
            }
        }

        tweetRepository.save(tweet);
        return ResponseEntity.ok(Map.of("message", "Tweet actualizado correctamente ✨"));
    }

    private String saveImage(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null) return null;

        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!List.of("jpg", "jpeg", "png").contains(extension)) {
            return null;
        }

        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + "_" + originalName;
        Path destination = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename;
    }
}
