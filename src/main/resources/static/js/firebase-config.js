import { initializeApp } from "https://www.gstatic.com/firebasejs/12.15.0/firebase-app.js";

import { getFirestore } from "https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js";

const firebaseConfig = {
    apiKey: "",
    authDomain: "app24-db038.firebaseapp.com",
    projectId: "app24-db038",
    storageBucket: "app24-db038.firebasestorage.app",
    messagingSenderId: "",
    appId: "",
    measurementId: ""
};

const app = initializeApp(firebaseConfig);

const db = getFirestore(app);

export { app, db };
