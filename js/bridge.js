import { onStateChange, patchTelemetry } from "./firebase.js";

let port = null;
let writer = null;
let encoder = new TextEncoder();


// 🔌 Connexion au port série (bouton à appeler)
window.connectArduino = async () => {
  try {
    port = await navigator.serial.requestPort();
    await port.open({ baudRate: 9600 });

    writer = port.writable.getWriter();

    console.log("Arduino connecté ✅");

    await patchTelemetry({
      connected: true,
      lastSeen: Date.now()
    });

  } catch (err) {
    console.error("Erreur connexion :", err);
  }
};


// 📤 Envoi vers Arduino
function sendToArduinoLine(line) {
  if (!writer) {
    console.warn("Pas connecté à l'Arduino");
    return;
  }

  writer.write(encoder.encode(line));
}


// 🔄 Firebase → Arduino
onStateChange(async (state) => {
  if (!state) return;

  console.log("Etat Firebase reçu :", state);

  const payload = {
    on: !!state.on,
    brightness: Number(state.brightness ?? 0),
    mode: String(state.mode ?? "warm"),
    r: Number(state.color?.r ?? 0),
    g: Number(state.color?.g ?? 0),
    b: Number(state.color?.b ?? 0),
  };

  try {
    sendToArduinoLine(JSON.stringify(payload) + "\n");

    await patchTelemetry({
      connected: true,
      lastSeen: Date.now()
    });

  } catch (e) {
    console.error("Erreur envoi Arduino :", e);

    await patchTelemetry({
      connected: false
    });
  }
});
