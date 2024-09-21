package com.user.service.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FireBaseConfig {

    FireBaseConfig() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("D://user-service//src//main//resources//future-synapse-429515-a1-firebase-adminsdk-3rp7x-a3fdc9f6ab.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://future-synapse-429515-a1-default-rtdb.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
    }
}

