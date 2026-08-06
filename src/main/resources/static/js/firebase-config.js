import { initializeApp } from "https://www.gstatic.com/firebasejs/12.15.0/firebase-app.js";

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

const db = getFirestore(app);

export { app, db };
