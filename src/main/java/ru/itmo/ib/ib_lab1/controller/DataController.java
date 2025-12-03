package ru.itmo.ib.ib_lab1.controller;

import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.ib.ib_lab1.repo.UserRepository;
import ru.itmo.ib.ib_lab1.utils.UserPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DataController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/data")
    public ResponseEntity<?> getData(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Защита от XSS: Явное экранирование пользовательских данных с помощью OWASP Encoder
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    // Явное экранирование для защиты от XSS
                    userMap.put("username", Encode.forHtml(user.getUsername()));
                    return userMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        // Явное экранирование для защиты от XSS
        response.put("message", "Hello " + Encode.forHtml(userPrincipal.getUsername()) + "!");
        response.put("users", users);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}