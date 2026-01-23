<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contrôle Veilleuse</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            max-width: 500px;
            width: 100%;
            padding: 30px;
        }

        h1 {
            color: #333;
            margin-bottom: 10px;
            font-size: 28px;
        }

        .connection-info {
            background: #f0f7ff;
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
            border-left: 4px solid #667eea;
        }

        .connection-info h3 {
            color: #667eea;
            font-size: 14px;
            margin-bottom: 5px;
        }

        .connection-info p {
            color: #555;
            font-size: 13px;
            line-height: 1.5;
        }

        .status {
            padding: 12px 20px;
            border-radius: 10px;
            margin: 20px 0;
            font-weight: 600;
            text-align: center;
            transition: all 0.3s;
        }

        .status.disconnected {
            background: #ffebee;
            color: #c62828;
        }

        .status.connected {
            background: #e8f5e9;
            color: #2e7d32;
        }

        .section {
            margin: 25px 0;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 12px;
        }

        .section h2 {
            font-size: 18px;
            color: #555;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            width: 100%;
        }

        .btn-primary {
            background: #667eea;
            color: white;
        }

        .btn-primary:hover {
            background: #5568d3;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }

        .btn-primary:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }

        .btn-danger {
            background: #ff5252;
            color: white;
        }

        .btn-danger:hover {
            background: #ff1744;
        }

        .switch-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin: 15px 0;
        }

        .switch {
            position: relative;
            width: 60px;
            height: 30px;
        }

        .switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }

        .slider {
            position: absolute;
            cursor: pointer;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-color: #ccc;
            transition: 0.4s;
            border-radius: 30px;
        }

        .slider:before {
            position: absolute;
            content: "";
            height: 22px;
            width: 22px;
            left: 4px;
            bottom: 4px;
            background-color: white;
            transition: 0.4s;
            border-radius: 50%;
        }

        input:checked + .slider {
            background-color: #667eea;
        }

        input:checked + .slider:before {
            transform: translateX(30px);
        }

        .brightness-control {
            margin: 20px 0;
        }

        .brightness-value {
            text-align: center;
            font-size: 24px;
            font-weight: bold;
            color: #667eea;
            margin: 10px 0;
        }

        input[type="range"] {
            width: 100%;
            height: 8px;
            border-radius: 5px;
            background: #ddd;
            outline: none;
            -webkit-appearance: none;
        }

        input[type="range"]::-webkit-slider-thumb {
            -webkit-appearance: none;
            appearance: none;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
        }

        input[type="range"]::-moz-range-thumb {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
            border: none;
        }

        .color-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            margin-top: 15px;
        }

        .color-btn {
            padding: 15px;
            border: 3px solid transparent;
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.3s;
            font-weight: 600;
            text-align: center;
        }

        .color-btn:hover {
            transform: scale(1.05);
        }

        .color-btn.selected {
            border-color: #333;
            box-shadow: 0 0 0 2px white, 0 0 0 4px #333;
        }

        .schedule-list {
            max-height: 200px;
            overflow-y: auto;
            margin-top: 15px;
        }

        .schedule-item {
            background: white;
            padding: 12px;
            margin: 8px 0;
            border-radius: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .schedule-item button {
            background: #ff5252;
            color: white;
            border: none;
            padding: 6px 12px;
            border-radius: 5px;
            cursor: pointer;
        }

        .time-picker {
            display: flex;
            gap: 10px;
            align-items: center;
            margin: 10px 0;
        }

        .time-picker input {
            padding: 8px;
            border: 2px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
        }

        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            justify-content: center;
            align-items: center;
            z-index: 1000;
        }

        .modal.show {
            display: flex;
        }

        .modal-content {
            background: white;
            padding: 30px;
            border-radius: 15px;
            max-width: 400px;
            width: 90%;
        }

        .modal-buttons {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }

        .btn-secondary {
            background: #6c757d;
            color: white;
        }

        .toast {
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: #333;
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.3);
            opacity: 0;
            transition: opacity 0.3s;
            z-index: 2000;
        }

        .toast.show {
            opacity: 1;
        }

        .console {
            background: #1e1e1e;
            color: #00ff00;
            padding: 15px;
            border-radius: 8px;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            max-height: 150px;
            overflow-y: auto;
            margin-top: 10px;
        }

        .console-line {
            margin: 2px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🌙 Contrôle Veilleuse</h1>
        
        <div class="connection-info">
            <h3>📡 Connexion USB/Série</h3>
            <p>Branchez votre Arduino en USB et cliquez sur "Connecter". Compatible avec Chrome et Edge.</p>
        </div>

        <div id="status" class="status disconnected">
            État: Déconnecté
        </div>

        <button id="btnConnect" class="btn btn-primary">
            🔌 Connecter en USB
        </button>

        <button id="btnDisconnect" class="btn btn-danger" style="display: none; margin-top: 10px;">
            ⏹️ Déconnecter
        </button>

        <div class="section">
            <h2>⚡ Alimentation</h2>
            <div class="switch-container">
                <span>Veilleuse</span>
                <label class="switch">
                    <input type="checkbox" id="switchPower">
                    <span class="slider"></span>
                </label>
            </div>
            <div class="switch-container">
                <span>Mode automatique</span>
                <label class="switch">
                    <input type="checkbox" id="switchAuto">
                    <span class="slider"></span>
                </label>
            </div>
            <div class="switch-container">
                <span>Feu tricolore</span>
                <label class="switch">
                    <input type="checkbox" id="switchPower">
                    <span class="slider"></span>
                </label>
            </div>
        </div>

        <div class="section">
            <h2>🎨 Couleurs</h2>
            <div class="color-grid">
                <button class="color-btn" data-color="red" style="background: #FF6B6B; color: white;">Rouge</button>
                <button class="color-btn" data-color="green" style="background: #95E1D3;">Vert</button>
                <button class="color-btn" data-color="yellow" style="background: #fcf300;">Bleu</button>
            </div>
        </div>

        <div class="section">
            <h2>⏰ Programmations</h2>
            <button id="btnAddSchedule" class="btn btn-primary">
                ➕ Ajouter une programmation
            </button>
            <div id="scheduleList" class="schedule-list">
                <p style="text-align: center; color: #999; padding: 20px;">Aucune programmation</p>
            </div>
        </div>

        <div class="section">
            <h2>📟 Console</h2>
            <div id="console" class="console"></div>
        </div>
    </div>

    <div id="scheduleModal" class="modal">
        <div class="modal-content">
            <h2>Nouvelle programmation</h2>
            <div class="time-picker">
                <label>Début:</label>
                <input type="time" id="timeStart" value="20:00">
            </div>
            <div class="time-picker">
                <label>Fin:</label>
                <input type="time" id="timeEnd" value="07:00">
            </div>
            <div class="modal-buttons">
                <button class="btn btn-primary" id="btnSaveSchedule">Ajouter</button>
                <button class="btn btn-secondary" id="btnCancelSchedule">Annuler</button>
            </div>
        </div>
    </div>

    <div id="toast" class="toast"></div>

    <script>
        let port = null;
        let reader = null;
        let writer = null;
        let schedules = [];
        let currentColor = 'warm';

        const elements = {
            btnConnect: document.getElementById('btnConnect'),
            btnDisconnect: document.getElementById('btnDisconnect'),
            status: document.getElementById('status'),
            switchPower: document.getElementById('switchPower'),
            switchAuto: document.getElementById('switchAuto'),
            brightness: document.getElementById('brightness'),
            brightnessValue: document.getElementById('brightnessValue'),
            colorButtons: document.querySelectorAll('.color-btn'),
            btnAddSchedule: document.getElementById('btnAddSchedule'),
            scheduleList: document.getElementById('scheduleList'),
            scheduleModal: document.getElementById('scheduleModal'),
            btnSaveSchedule: document.getElementById('btnSaveSchedule'),
            btnCancelSchedule: document.getElementById('btnCancelSchedule'),
            timeStart: document.getElementById('timeStart'),
            timeEnd: document.getElementById('timeEnd'),
            toast: document.getElementById('toast'),
            console: document.getElementById('console')
        };

        // Connexion Série USB
        elements.btnConnect.addEventListener('click', async () => {
            try {
                if (!('serial' in navigator)) {
                    showToast('Web Serial API non supportée. Utilisez Chrome ou Edge.');
                    logConsole('❌ Web Serial API non disponible');
                    return;
                }

                logConsole('🔍 Recherche de ports série...');
                
                port = await navigator.serial.requestPort();
                await port.open({ baudRate: 9600 });

                logConsole('✅ Port série ouvert à 9600 bauds');

                const textDecoder = new TextDecoderStream();
                const readableStreamClosed = port.readable.pipeTo(textDecoder.writable);
                reader = textDecoder.readable.getReader();

                const textEncoder = new TextEncoderStream();
                const writableStreamClosed = textEncoder.readable.pipeTo(port.writable);
                writer = textEncoder.writable.getWriter();

                updateStatus(true);
                showToast('Connecté en USB');
                logConsole('🎉 Connexion établie');

                elements.btnConnect.style.display = 'none';
                elements.btnDisconnect.style.display = 'block';

                // Lecture des données
                readSerialData();

            } catch (error) {
                console.error('Erreur connexion:', error);
                showToast('Erreur de connexion: ' + error.message);
                logConsole('❌ Erreur: ' + error.message);
            }
        });

        // Déconnexion
        elements.btnDisconnect.addEventListener('click', async () => {
            await disconnect();
        });

        async function disconnect() {
            if (reader) {
                await reader.cancel();
                reader = null;
            }
            if (writer) {
                await writer.close();
                writer = null;
            }
            if (port) {
                await port.close();
                port = null;
            }

            updateStatus(false);
            showToast('Déconnecté');
            logConsole('⏹️ Déconnexion');

            elements.btnConnect.style.display = 'block';
            elements.btnDisconnect.style.display = 'none';
        }

        async function readSerialData() {
            try {
                while (true) {
                    const { value, done } = await reader.read();
                    if (done) break;
                    if (value) {
                        logConsole('◀️ Reçu: ' + value.trim());
                    }
                }
            } catch (error) {
                console.error('Erreur lecture:', error);
                logConsole('❌ Erreur lecture: ' + error.message);
            }
        }

        // Switch alimentation
        elements.switchPower.addEventListener('change', (e) => {
            const isOn = e.target.checked;
            sendCommand('POWER:' + (isOn ? 'ON' : 'OFF'));
            showToast('Veilleuse ' + (isOn ? 'allumée' : 'éteinte'));
        });

        // Switch mode auto
        elements.switchAuto.addEventListener('change', (e) => {
            const isAuto = e.target.checked;
            sendCommand('AUTO:' + (isAuto ? 'ON' : 'OFF'));
            showToast('Mode automatique ' + (isAuto ? 'activé' : 'désactivé'));
        });

        // Luminosité
        elements.brightness.addEventListener('input', (e) => {
            elements.brightnessValue.textContent = e.target.value + '%';
        });

        elements.brightness.addEventListener('change', (e) => {
            sendCommand('BRIGHTNESS:' + e.target.value);
            showToast('Intensité: ' + e.target.value + '%');
        });

        // Couleurs
      elements.colorButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            elements.colorButtons.forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');

            // Mapping simplifié pour Arduino
            switch(btn.dataset.color) {
                case 'red':
                    sendCommand('red');
                    break;
                case 'green':
                    sendCommand('green');  // ou autre LED
                    break;
                case 'yellow':
                    sendCommand('yellow'); // juste pour test
                    break;
            }
        });
    });



        // Programmations
        elements.btnAddSchedule.addEventListener('click', () => {
            elements.scheduleModal.classList.add('show');
        });

        elements.btnCancelSchedule.addEventListener('click', () => {
            elements.scheduleModal.classList.remove('show');
        });

        elements.btnSaveSchedule.addEventListener('click', () => {
            const start = elements.timeStart.value;
            const end = elements.timeEnd.value;
            
            schedules.push({ start, end });
            updateScheduleList();
            
            sendCommand(`SCHEDULE:${start}-${end}`);
            showToast('Programmation ajoutée');
            elements.scheduleModal.classList.remove('show');
        });

        function updateScheduleList() {
            if (schedules.length === 0) {
                elements.scheduleList.innerHTML = '<p style="text-align: center; color: #999; padding: 20px;">Aucune programmation</p>';
            } else {
                elements.scheduleList.innerHTML = schedules.map((s, i) => `
                    <div class="schedule-item">
                        <span>${i + 1}. ${s.start} - ${s.end}</span>
                        <button onclick="removeSchedule(${i})">✕</button>
                    </div>
                `).join('');
            }
        }

        function removeSchedule(index) {
            schedules.splice(index, 1);
            updateScheduleList();
            showToast('Programmation supprimée');
        }

        function updateStatus(connected) {
            if (connected) {
                elements.status.className = 'status connected';
                elements.status.textContent = 'État: Connecté USB';
            } else {
                elements.status.className = 'status disconnected';
                elements.status.textContent = 'État: Déconnecté';
            }
        }

        async function sendCommand(command) {
            if (!writer) {
                showToast('Pas de connexion série');
                logConsole('⚠️ Tentative d\'envoi sans connexion');
                return;
            }

            try {
                await writer.write(command + '\n');
                logConsole('▶️ Envoyé: ' + command);
            } catch (error) {
                console.error('Erreur envoi:', error);
                showToast('Erreur envoi commande');
                logConsole('❌ Erreur envoi: ' + error.message);
            }
        }

        function logConsole(message) {
            const line = document.createElement('div');
            line.className = 'console-line';
            line.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
            elements.console.appendChild(line);
            elements.console.scrollTop = elements.console.scrollHeight;
        }

        function showToast(message) {
            elements.toast.textContent = message;
            elements.toast.classList.add('show');
            setTimeout(() => {
                elements.toast.classList.remove('show');
            }, 3000);
        }
    </script>
</body>
</html>