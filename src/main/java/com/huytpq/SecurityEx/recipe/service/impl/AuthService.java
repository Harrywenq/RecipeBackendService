package com.huytpq.SecurityEx.recipe.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthService {

    @Value("${google.oauth2.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    @Value("${google.oauth2.client-secret}")
    private String googleClientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String googleRedirectUri;

    @Value("${facebook.oauth2.user-info-uri}")
    private String facebookUserInfoUri;

    @Value("${facebook.oauth2.client-id}")
    private String facebookClientId;

    @Value("${facebook.oauth2.client-secret}")
    private String facebookClientSecret;

    @Value("${facebook.oauth2.redirect-uri}")
    private String facebookRedirectUri;

//    public String generateAuthUrl(String loginType) {
//        if (googleClientId == null || googleRedirectUri == null) {
//            throw new IllegalStateException("Missing client ID or redirect URI configuration");
//        }
//
//        loginType = loginType.trim().toLowerCase();
//        String url = "";
//
//        if ("google".equals(loginType)) {
//            GoogleAuthorizationCodeRequestUrl urlBuilder = new GoogleAuthorizationCodeRequestUrl(
//                    googleClientId,
//                    googleRedirectUri,
//                    Arrays.asList("email", "profile", "openid")
//            );
//            url = urlBuilder.build();
//        }
//
//        return url;
//    }

    public String generateAuthUrl(String loginType) {
        loginType = loginType.trim().toLowerCase();
        String url = "";

        if ("google".equals(loginType)) {
            if (googleClientId == null || googleRedirectUri == null) {
                throw new IllegalStateException("Missing Google client ID or redirect URI configuration");
            }
            GoogleAuthorizationCodeRequestUrl urlBuilder = new GoogleAuthorizationCodeRequestUrl(
                    googleClientId,
                    googleRedirectUri,
                    Arrays.asList("email", "profile", "openid")
            );
            url = urlBuilder.build();
        } else if ("facebook".equals(loginType)) {
            if (facebookClientId == null || facebookRedirectUri == null) {
                throw new IllegalStateException("Missing Facebook client ID or redirect URI configuration");
            }
            // Xây dựng URL thủ công cho Facebook OAuth
            url = "https://www.facebook.com/v19.0/dialog/oauth" +
                    "?client_id=" + facebookClientId +
                    "&redirect_uri=" + facebookRedirectUri +
                    "&scope=email,public_profile" +
                    "&response_type=code";
        }

        if (url.isEmpty()) {
            throw new IllegalArgumentException("Unsupported login type: " + loginType);
        }

        return url;
    }

    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        String accessToken;

        switch (loginType.toLowerCase()) {
            case "google":
                accessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        new GsonFactory(),
                        googleClientId,
                        googleClientSecret,
                        code,
                        googleRedirectUri
                ).execute().getAccessToken();

                restTemplate.getInterceptors().add((req, body, executionContext) -> {
                    req.getHeaders().set("Authorization", "Bearer " + accessToken);
                    return executionContext.execute(req, body);
                });

                return new ObjectMapper().readValue(
                        restTemplate.getForEntity(googleUserInfoUri, String.class).getBody(),
                        new TypeReference<Map<String, Object>>() {}
                );

            case "facebook":
                String tokenUrl = "https://graph.facebook.com/v19.0/oauth/access_token" +
                        "?client_id=" + facebookClientId +
                        "&redirect_uri=" + facebookRedirectUri +
                        "&client_secret=" + facebookClientSecret +
                        "&code=" + code;

//                // Use RestTemplate to fetch the Facebook access token
//                ResponseEntity<String> response = restTemplate.getForEntity(urlGetAccessToken, String.class);
//                ObjectMapper mapper = new ObjectMapper();
//                JsonNode node = mapper.readTree(response.getBody());
//                String accessToken = node.get("access_token").asText();
//
//                // Set the URL for the Facebook API to fetch user info
//                String userInfoUri = facebookUserInfoUri + "&access_token=" + accessToken;
//
//                return mapper.readValue(
//                        restTemplate.getForEntity(userInfoUri, String.class).getBody(),
//                        new TypeReference<Map<String, Object>>() {}
//                );

            Map<String, Object> tokenResponse = restTemplate.getForObject(tokenUrl, Map.class);
                accessToken = (String) tokenResponse.get("access_token");

                // Lấy thông tin user từ Facebook Graph API
                String userInfoUrl = facebookUserInfoUri + "?fields=id,name,email,picture&type=large&access_token=" + accessToken;
                Map<String, Object> userInfoResponse = restTemplate.getForObject(userInfoUrl, Map.class);

                // Chuẩn hóa dữ liệu giống Google (sub -> id, picture -> url)
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", userInfoResponse.get("id"));
                userInfo.put("name", userInfoResponse.get("name"));
                userInfo.put("email", userInfoResponse.get("email"));

                // Lấy URL ảnh từ cấu trúc nested của picture
                Map<String, Object> pictureData = (Map<String, Object>) ((Map<String, Object>) userInfoResponse.get("picture")).get("data");
                userInfo.put("picture", pictureData.get("url"));

                return userInfo;

            default:
                System.out.println("Unsupported login type: " + loginType);
                return null;
        }
    }
}