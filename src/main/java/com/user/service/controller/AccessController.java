package com.user.service.controller;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.user.service.model.SignupDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessController {

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



}
