// js/bridge.js
import {
  onStateChange,
  addAlert,
  addHistory,
  setBridgeOnline,
  setBridgeOffline,
  setLastSeen
} from "./firebase.js";

// ─── Exposition des fonctions Firebase à functions.js (non-module) ────────────
window.fb = {
  addAlert,
  addHistory,
  setBridgeOnline,
  setBridgeOffline,
  setLastSeen
};

// ─── Firebase → Arduino ───────────────────────────────────────────────────────
onStateChange(async (state) => {
  if (!state) return;
  console.log("État Firebase reçu :", state);

  if (typeof sendCommand !== "function") return;

  if (!state.on) {
    sendCommand('0', 'Firebase → Extinction');
    return;
  }

  if (state.mode === "auto") {
    sendCommand('a', 'Firebase → Mode Auto');
    return;
  }

  if (state.color) {
    const { r, g, b } = state.color;
    if      (r > 200 && g < 50  && b < 50)  sendCommand('r', 'Firebase → Rouge');
    else if (r < 50  && g > 200 && b < 50)  sendCommand('g', 'Firebase → Vert');
    else if (r < 50  && g < 50  && b > 200) sendCommand('b', 'Firebase → Bleu');
    else if (r > 200 && g > 200 && b < 50)  sendCommand('y', 'Firebase → Jaune');
    else if (r > 200 && g > 200 && b > 200) sendCommand('w', 'Firebase → Blanc');
  }

  await setLastSeen();
});