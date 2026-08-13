package com.project.oditji.common.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

/**
 * ODITJI 채팅용 Firebase Admin SDK 설정입니다.
 *
 * 서비스 계정 JSON 파일 경로는 소스에 저장하지 않고
 * GOOGLE_APPLICATION_CREDENTIALS 환경변수로 전달합니다.
 */
@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_APP_NAME = "oditji-chat";

    /**
     * firebase.chat.enabled=true인 환경에서만 Firebase Admin SDK를 초기화합니다.
     */
    @Bean
    @ConditionalOnProperty(
            name = "firebase.chat.enabled",
            havingValue = "true")
    FirebaseApp firebaseChatApp(
            @Value("${firebase.chat.project-id}") String projectId)
            throws IOException {

        try {
            return FirebaseApp.getInstance(FIREBASE_APP_NAME);
        } catch (IllegalStateException exception) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId(projectId)
                    .build();

            return FirebaseApp.initializeApp(
                    options,
                    FIREBASE_APP_NAME);
        }
    }

    /**
     * Custom Token 발급에 사용하는 Firebase Authentication 객체입니다.
     */
    @Bean
    @ConditionalOnProperty(
            name = "firebase.chat.enabled",
            havingValue = "true")
    FirebaseAuth firebaseChatAuth(FirebaseApp firebaseChatApp) {
        return FirebaseAuth.getInstance(firebaseChatApp);
    }

    /**
     * 채팅방 및 참가자 권한 문서를 서버 권한으로 동기화합니다.
     */
    @Bean
    @ConditionalOnProperty(
            name = "firebase.chat.enabled",
            havingValue = "true")
    Firestore firebaseChatFirestore(FirebaseApp firebaseChatApp) {
        return FirestoreClient.getFirestore(firebaseChatApp);
    }
}
