// js/firebase.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
  getDatabase,
  ref,
  set,
  update,
  push,
  query,
  orderByKey,
  get,
  onValue,
  onDisconnect,
  serverTimestamp
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";

// ─── Configuration Firebase ───────────────────────────────────────────────────
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

const app = initializeApp(firebaseConfig);
export const db = getDatabase(app);

// ─── Références ───────────────────────────────────────────────────────────────
export const stateRef     = ref(db, "veilleuse/state");
export const telemetryRef = ref(db, "veilleuse/telemetry");
export const alertsRef    = ref(db, "veilleuse/alerts");
export const historyRef   = ref(db, "veilleuse/history");

const MAX_ENTRIES = 100;

// ─── Utilitaire : nettoie les anciennes entrées si > MAX_ENTRIES ──────────────
async function trimCollection(collectionRef) {
  const snap = await get(query(collectionRef, orderByKey()));
  if (!snap.exists()) return;

  const keys = Object.keys(snap.val());
  if (keys.length <= MAX_ENTRIES) return;

  const toDelete = keys.slice(0, keys.length - MAX_ENTRIES);
  const updates = Object.fromEntries(toDelete.map(k => [k, null]));
  await update(collectionRef, updates);
}

// ─── STATE ────────────────────────────────────────────────────────────────────
export async function writeState(state) {
  try {
    await set(stateRef, state);
  } catch (err) {
    console.error("Erreur writeState :", err);
  }
}

export async function patchState(partial) {
  try {
    await update(stateRef, partial);
  } catch (err) {
    console.error("Erreur patchState :", err);
  }
}

export function onStateChange(cb) {
  return onValue(stateRef, (snap) => cb(snap.val()));
}

// ─── TELEMETRY ────────────────────────────────────────────────────────────────
export async function setLastSeen() {
  await update(telemetryRef, {
    lastSeen: Date.now(),
    status: "online"
  });
}

export async function setBridgeOffline() {
  await update(telemetryRef, {
    connected: false,
    lastSeen: Date.now(),
    status: "offline"
  });
  await addAlert("bridge", "Déconnexion du bridge", null);
}

export async function setBridgeOnline() {
  await onDisconnect(telemetryRef).update({
    connected: false,
    status: "offline",
    lastSeen: Date.now()
  });
  await update(telemetryRef, {
    connected: true,
    lastSeen: Date.now(),
    status: "online"
  });
  await addAlert("bridge", "Connexion du bridge", null);
}

// ─── ALERTES ─────────────────────────────────────────────────────────────────
export async function addAlert(type, message, value = null) {
  const entry = { type, message, timestamp: Date.now() };
  if (value !== null) entry.value = value;

  await push(alertsRef, entry);
  await trimCollection(alertsRef);
}

export function onAlertsChange(cb) {
  return onValue(alertsRef, (snap) => {
    if (!snap.exists()) return cb([]);
    const entries = Object.entries(snap.val())
      .map(([id, data]) => ({ id, ...data }))
      .sort((a, b) => b.timestamp - a.timestamp);
    cb(entries);
  });
}

// ─── HISTORIQUE DES COMMANDES ─────────────────────────────────────────────────
export async function addHistory(command, source, label) {
  const entry = { command, source, label, timestamp: Date.now() };

  await push(historyRef, entry);
  await trimCollection(historyRef);
  await addAlert("manual", `Commande reçue : ${label} (${source})`, null);
}

export function onHistoryChange(cb) {
  return onValue(historyRef, (snap) => {
    if (!snap.exists()) return cb([]);
    const entries = Object.entries(snap.val())
      .map(([id, data]) => ({ id, ...data }))
      .sort((a, b) => b.timestamp - a.timestamp);
    cb(entries);
  });
} 