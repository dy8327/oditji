import { initializeApp } from "https://www.gstatic.com/firebasejs/12.15.0/firebase-app.js";

import {
    getAuth,
    inMemoryPersistence,
    setPersistence,
    signInWithCustomToken,
    signOut
} from "https://www.gstatic.com/firebasejs/12.15.0/firebase-auth.js";

import { getFirestore } from "https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js";

const firebaseConfig = {
    apiKey: "AIzaSyDLgD8JhDiaOvMs5zzSLzbfrBQ8b6AisBw",
    authDomain: "app24-db038.firebaseapp.com",
    projectId: "app24-db038",
    storageBucket: "app24-db038.firebasestorage.app",
    messagingSenderId: "659277862313",
    appId: "1:659277862313:web:d8ee6240d33ce2345e20b8",
    measurementId: "G-XVZGVK8S8B"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

let persistencePromise = null;
let authenticationPromise = null;

/**
 * 현재 화면에서 Spring 애플리케이션 context-path를 찾습니다.
 */
export function getApplicationContextPath() {

    const dataElement = document.querySelector("[data-context-path]");

    if (dataElement && dataElement.dataset.contextPath !== undefined) {
        return dataElement.dataset.contextPath || "";
    }

    const hiddenElement = document.getElementById("contextPath");

    if (hiddenElement && hiddenElement.value !== undefined) {
        return hiddenElement.value || "";
    }

    return "";
}

/**
 * Firebase 인증 정보를 브라우저 메모리에만 유지합니다.
 *
 * ODITJI 세션이 끝난 뒤 Firebase 로그인만 브라우저에 남는 것을 방지하기 위해
 * localStorage나 IndexedDB에는 인증 정보를 저장하지 않습니다.
 */
function prepareInMemoryPersistence() {

    if (!persistencePromise) {
        persistencePromise = setPersistence(
            auth,
            inMemoryPersistence
        );
    }

    return persistencePromise;
}

/**
 * Spring 세션이 발급한 Firebase Custom Token으로 로그인합니다.
 * 같은 페이지에서 여러 채팅 모듈이 동시에 호출해도 한 번만 인증 요청합니다.
 */
export async function ensureFirebaseChatAuth(
    contextPath = getApplicationContextPath()
) {

    if (auth.currentUser) {
        return auth.currentUser;
    }

    if (authenticationPromise) {
        return authenticationPromise;
    }

    authenticationPromise = (async function() {

        await prepareInMemoryPersistence();

        const response = await fetch(
            contextPath + "/chat/api/firebase/token",
            {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                },
                cache: "no-store",
                credentials: "same-origin"
            }
        );

        if (!response.ok) {
            await signOut(auth);
            throw new Error(
                "Firebase 채팅 인증 요청 실패: " + response.status
            );
        }

        const result = await response.json();

        if (!result.success || !result.enabled || !result.token) {
            await signOut(auth);
            throw new Error(
                result.message || "Firebase 채팅 인증을 사용할 수 없습니다."
            );
        }

        const credential = await signInWithCustomToken(
            auth,
            result.token
        );

        return credential.user;
    })();

    try {
        return await authenticationPromise;
    } finally {
        authenticationPromise = null;
    }
}

/**
 * 현재 페이지에서 사용 중인 Firebase 채팅 인증을 종료합니다.
 */
export async function clearFirebaseChatAuth() {
    authenticationPromise = null;
    await signOut(auth);
}

export { app, auth, db };
