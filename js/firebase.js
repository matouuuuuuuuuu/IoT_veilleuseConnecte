// js/firebase.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
  getDatabase,
  ref,
  set,
  update,
  onValue,
  serverTimestamp
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";

// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyBL6CLqNUnU50M4nGFp53jQ0DXLRcLizIY",
  authDomain: "veilleuseconnectee.firebaseapp.com",
  databaseURL: "https://veilleuseconnectee-default-rtdb.europe-west1.firebasedatabase.app",
  projectId: "veilleuseconnectee",
  storageBucket: "veilleuseconnectee.firebasestorage.app",
  messagingSenderId: "595487460865",
  appId: "1:595487460865:web:e07a35b3520436253bfa00",
  measurementId: "G-MZ84ZZW3B2"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
export const db = getDatabase(app);

// Helpers
export const stateRef = ref(db, "veilleuse/state");
export const telemetryRef = ref(db, "veilleuse/telemetry");

// Ecrire l'état complet (commande)
export async function writeState(state) {
  // state = { on, brightness, mode, color:{r,g,b} }
  await set(stateRef, state);
}

// Mettre à jour seulement certains champs
export async function patchState(partial) {
  await update(stateRef, partial);
}

// Ex: signaler que le bridge/arduino est vivant
export async function setLastSeen() {
  await update(telemetryRef, { lastSeen: Date.now() });
}

// Ecouter les commandes
export function onStateChange(cb) {
  return onValue(stateRef, (snap) => cb(snap.val()));
}
