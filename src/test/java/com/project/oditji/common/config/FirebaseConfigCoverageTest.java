package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

/** Firebase Admin SDK 설정 빈의 기존 앱 재사용/신규 초기화/하위 빈 생성을 검증합니다. */
class FirebaseConfigCoverageTest {

    private static final String APP_NAME = "oditji-chat";

    @Test
    void firebaseChatAppShouldReuseExistingNamedApp() throws IOException {
        FirebaseConfig config = new FirebaseConfig();
        FirebaseApp expected = mock(FirebaseApp.class);

        try (MockedStatic<FirebaseApp> firebaseApps = mockStatic(FirebaseApp.class)) {
            firebaseApps.when(() -> FirebaseApp.getInstance(APP_NAME)).thenReturn(expected);

            FirebaseApp actual = config.firebaseChatApp("oditji-project");

            assertSame(expected, actual);
            firebaseApps.verify(() -> FirebaseApp.getInstance(APP_NAME));
        }
    }

    @Test
    void firebaseChatAppShouldInitializeWhenNamedAppDoesNotExist() throws IOException {
        FirebaseConfig config = new FirebaseConfig();
        FirebaseApp expected = mock(FirebaseApp.class);
        GoogleCredentials credentials = mock(GoogleCredentials.class);

        try (MockedStatic<FirebaseApp> firebaseApps = mockStatic(FirebaseApp.class);
                MockedStatic<GoogleCredentials> googleCredentials = mockStatic(GoogleCredentials.class)) {

            firebaseApps.when(() -> FirebaseApp.getInstance(APP_NAME))
                    .thenThrow(new IllegalStateException("not initialized"));
            googleCredentials.when(GoogleCredentials::getApplicationDefault)
                    .thenReturn(credentials);
            firebaseApps.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq(APP_NAME)))
                    .thenReturn(expected);

            FirebaseApp actual = config.firebaseChatApp("oditji-project");

            assertSame(expected, actual);
            firebaseApps.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq(APP_NAME)));
        }
    }

    @Test
    void firebaseChatAuthShouldReturnAuthForConfiguredApp() {
        FirebaseConfig config = new FirebaseConfig();
        FirebaseApp app = mock(FirebaseApp.class);
        FirebaseAuth expected = mock(FirebaseAuth.class);

        try (MockedStatic<FirebaseAuth> firebaseAuth = mockStatic(FirebaseAuth.class)) {
            firebaseAuth.when(() -> FirebaseAuth.getInstance(app)).thenReturn(expected);

            assertSame(expected, config.firebaseChatAuth(app));
        }
    }

    @Test
    void firebaseChatFirestoreShouldReturnFirestoreForConfiguredApp() {
        FirebaseConfig config = new FirebaseConfig();
        FirebaseApp app = mock(FirebaseApp.class);
        Firestore expected = mock(Firestore.class);

        try (MockedStatic<FirestoreClient> firestoreClient = mockStatic(FirestoreClient.class)) {
            firestoreClient.when(() -> FirestoreClient.getFirestore(app)).thenReturn(expected);

            assertSame(expected, config.firebaseChatFirestore(app));
        }
    }
}
