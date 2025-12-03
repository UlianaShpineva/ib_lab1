package ru.itmo.ib.ib_lab1.controller;

import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import ru.itmo.ib.ib_lab1.dto.CreatePostRequest;
import ru.itmo.ib.ib_lab1.model.Post;
import ru.itmo.ib.ib_lab1.model.User;
import ru.itmo.ib.ib_lab1.repo.PostRepository;
import ru.itmo.ib.ib_lab1.repo.UserRepository;
import ru.itmo.ib.ib_lab1.utils.UserPrincipal;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostRequest request,
                                        Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Защита от XSS: данные валидируются и сохраняются как есть,
        // но при отображении на фронтенде должны экранироваться
        Post post = new Post(request.getTitle(), request.getContent(), user);
        postRepository.save(post);

        return ResponseEntity.ok(Map.of("message", "Post created successfully", "id", post.getId()));
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        // Hibernate использует параметризованные запросы - защита от SQL injection
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

        // Защита от XSS: Явное экранирование пользовательских данных с помощью OWASP Encoder
        List<Map<String, Object>> response = posts.stream()
                .map(post -> {
                    Map<String, Object> postMap = new HashMap<>();
                    postMap.put("id", post.getId());
                    // Явное экранирование для защиты от XSS
                    postMap.put("title", Encode.forHtml(post.getTitle()));
                    postMap.put("content", Encode.forHtml(post.getContent()));
                    postMap.put("author", Encode.forHtml(post.getAuthor().getUsername()));
                    postMap.put("createdAt", post.getCreatedAt());
                    return postMap;
                })
                .toList();

        return ResponseEntity.ok(response);
    }
}