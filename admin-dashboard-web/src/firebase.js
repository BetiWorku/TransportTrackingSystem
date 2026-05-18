import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyAriK7OD7nejhp4_Db3tgpsnh0TuQhttHg",
  authDomain: "transportation-tracker-app.firebaseapp.com",
  projectId: "transportation-tracker-app",
  storageBucket: "transportation-tracker-app.firebasestorage.app",
  messagingSenderId: "802991968990",
  appId: "1:802991968990:web:44f07e1f48b74ed1b1b2d135a139c204" // Standard format
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const auth = getAuth(app);
