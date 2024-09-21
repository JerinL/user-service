package com.user.service.controller;


import ch.qos.logback.core.model.Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.user.service.model.SignupDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@RestController
public class AccessController {

    @Value("${firebase.web.client-id}")
    private String firebaseClientId;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupDto request) {
        try {
            // Create user in Firebase
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword());

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);

            // Generate a custom token
            String customToken = FirebaseAuth.getInstance().createCustomToken(userRecord.getUid());
            SignupDto signupResponse = new SignupDto();
            signupResponse.setToken(customToken);
            return ResponseEntity.ok(signupResponse);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(400).body("Error creating user: " + e.getMessage());
        }
    }


    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignupDto request) {
        SignupDto signupDto = request;
        System.out.println(signupDto);
        try {
            // Authenticate user with Firebase
            System.out.println(request.getEmail());
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());

            // Generate a custom token
            String customToken = FirebaseAuth.getInstance().createCustomToken(userRecord.getUid());
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(customToken);
            SignupDto signupResponse = new SignupDto();
            signupResponse.setToken(userRecord.getUid());
            signupResponse.setPassword(userRecord.getTenantId());
            return ResponseEntity.ok(firebaseToken);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("Error signing in: " + e.getMessage());
        }
    }
    @PostMapping("/verifyToken")
    public String verifyToken(@RequestBody String idToken) {
        try {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
//            FirebaseToken decodedToken = firebaseTokenService.verifyToken(idToken);
            String uid = firebaseToken.getUid();
            // Handle authenticated user
            return "Token is valid. User ID: " + uid;
        } catch (FirebaseAuthException e) {
            return "Invalid token: " + e.getMessage();
        }
    }


    @GetMapping("/google/signin")
    public ResponseEntity<String> googleSignIn() {
        String redirectUri = "http://localhost:8080/welcome";  // Your redirect URI
        String state = UUID.randomUUID().toString();  // To prevent CSRF attacks

        // Construct the Google OAuth URL
        String googleAuthUrl = String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid%%20email%%20profile&state=%s",
                firebaseClientId, redirectUri, state
        );

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", googleAuthUrl).build();
    }

    @GetMapping("/google/callback")
    public Object handleGoogleCallback(@RequestParam("code") String code) {
        Object accessToken = null;
        try{
             accessToken = getAccessToken(code);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return accessToken;
//        return "Google Sign-In Successful!";
    }


    private Object getAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";



        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", "");  // Your actual client ID
        params.add("client_secret", "");  // Your actual client secret
        params.add("redirect_uri", "http://localhost:8080/auth/google/callback");  // Ensure this matches what you used during authorization
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        RestTemplate restTemplate = new RestTemplate();
        String accessToken = null;
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
        System.out.println( response.getBody());
            // Extract access token from respons
             accessToken = (String) response.getBody().get("access_token");
        accessToken = (String) response.getBody().get("access_token");
            System.out.println("Access Token: " + accessToken);
        String userInfo = getUserInfo(accessToken);
        return response.getBody();
    }


    public String getUserInfo(String accessToken) {
        // URL for the Google User Info API
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        // Create headers and set the Bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // Adds 'Bearer <access_token>' automatically

        // Create the HTTP entity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Make the GET request and get the user's info
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

            // Return the response body
            return response.getBody();

        } catch (Exception e) {
            System.err.println("Error fetching user info: " + e.getMessage());
            return null;
        }
    }

    @GetMapping("/welcome")
    public String getMessage(){
        return "welcome";
    }


    public static boolean validateAccessToken(String accessToken) {
        String tokenInfoUrl = "https://www.googleapis.com/oauth2/v1/tokeninfo";

        // Build the full URL with the access token as a query parameter
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tokenInfoUrl)
                .queryParam("access_token", accessToken);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Send a GET request to the tokeninfo endpoint
            ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // If the response is successful, token is valid
                return true;
            }
        } catch (Exception e) {
            // Log the exception and return false if token is invalid or verification fails
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {


        boolean b = validateAccessToken("test");
        System.out.println(b);

    }




}
