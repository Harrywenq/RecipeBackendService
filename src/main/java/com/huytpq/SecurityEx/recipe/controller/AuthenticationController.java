package com.huytpq.SecurityEx.recipe.controller;

import com.huytpq.SecurityEx.base.dto.JwtResponse;
import com.huytpq.SecurityEx.base.dto.LoginInput;
import com.huytpq.SecurityEx.recipe.dto.input.RegisterInput;
import com.huytpq.SecurityEx.recipe.dto.output.OutputObject;
import com.huytpq.SecurityEx.recipe.dto.output.RegisterOutput;
import com.huytpq.SecurityEx.recipe.repo.RoleRepo;
import com.huytpq.SecurityEx.recipe.service.impl.AuthService;
import com.huytpq.SecurityEx.recipe.service.impl.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterInput input) {
        try {
            RegisterOutput output = userService.register(input);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginInput request) {
        try {
            String token = userService.verify(request.getUsername(), request.getPassword(), request.getRoleId());

            if ("fail".equals(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }

    @GetMapping("/social/login")
    public ResponseEntity<String> socialAuth(
            @RequestParam("login_type") String loginType,
            HttpServletRequest request
    ) {
        loginType = loginType.trim().toLowerCase();
        String url;
        try {
            url = authService.generateAuthUrl(loginType);
            if (url.isEmpty()) {
                return ResponseEntity.badRequest().body("Unsupported login type: " + loginType);
            }
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate auth URL: " + e.getMessage());
        }
    }

    // "code" => google token => Lấy ra các thông tin khác
    @GetMapping("/social/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("login_type") String loginType,
            HttpServletRequest request
    ) throws Exception {
        // Gọi AuthService để lấy thông tin người dùng từ OAuth
        Map<String, Object> userInfo = authService.authenticateAndFetchProfile(code, loginType);

        if (userInfo == null) {
            return ResponseEntity.badRequest().body(new OutputObject(
                    "Failed to authenticate",
                    HttpStatus.BAD_REQUEST,
                    null
            ));
        }

        // Extract user information from userInfo map
        String accountId = "";
        String name = "";
        String picture = "";
        String email = "";

        if ("google".equals(loginType.trim())) {
            accountId = (String) Objects.requireNonNullElse(userInfo.get("sub"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            picture = (String) Objects.requireNonNullElse(userInfo.get("picture"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
        } else if ("facebook".equals(loginType.trim())) {
            accountId = (String) Objects.requireNonNullElse(userInfo.get("id"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");

            // Lấy URL ảnh từ cấu trúc dữ liệu của Facebook
            Object pictureObj = userInfo.get("picture");
            if (pictureObj instanceof Map) {
                Map<?, ?> pictureData = (Map<?, ?>) pictureObj;
                Object dataObj = pictureData.get("data");
                if (dataObj instanceof Map) {
                    Map<?, ?> dataMap = (Map<?, ?>) dataObj;
                    Object urlObj = dataMap.get("url");
                    if (urlObj instanceof String) {
                        picture = (String) urlObj;
                    }
                }
            }
        }

        // Tạo đối tượng LoginInput từ thông tin lấy được
        LoginInput loginInput = LoginInput.builder()
                .username(email)
                .password("")
                .displayName(name)
                .profileImage(picture)
                .googleAccountId("google".equals(loginType.trim()) ? accountId : "")
                .facebookAccountId("facebook".equals(loginType.trim()) ? accountId : "")
                .build();

        // Gọi service để xử lý login và tạo token
        return userService.verifyOAuth(loginInput, request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logout successful");
    }

}