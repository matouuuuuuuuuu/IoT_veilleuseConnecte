// Variables globales
let port;
let reader;
let writer;
let isConnected = false;
let schedules = [];


// Seuils des capteurs (selon le code Arduino)
const THRESHOLD_LIGHT = 480;
const THRESHOLD_SOUND = 550;
const TEMP_MAX = 25.0;
const TEMP_MIN = 10.0;
const HUM_MAX = 50.0;

// Éléments DOM (seront initialisés dans DOMContentLoaded)
let btnConnect;
let btnDisconnect;
let btnClearConsole;
let status;
let consoleDiv;
let switchPower;
let switchAuto;
let tempValue;
let tempStatus;
let humValue;
let humStatus;
let lightValue;
let lightStatus;
let soundValue;
let soundStatus;
let scheduleModal;
let btnAddSchedule;
let btnSaveSchedule;
let btnCancelSchedule;
let scheduleList;
let toast;

/// ============= CONNEXION SÉRIE =============
async function connectSerial() {
    try {
        console.log('=== DÉBUT DE LA CONNEXION ===');
        addConsoleLog('🔍 Vérification de Web Serial API...', 'info');
        
        if (!('serial' in navigator)) {
            const errorMsg = 'Web Serial API n\'est pas disponible dans ce navigateur';
            console.error('ERREUR CRITIQUE:', errorMsg);
            addConsoleLog('❌ ERREUR: ' + errorMsg, 'error');
            addConsoleLog('💡 Solutions possibles:', 'warning');
            addConsoleLog('  1. Utilisez Google Chrome (version 89+)', 'warning');
            addConsoleLog('  2. Utilisez Microsoft Edge (version 89+)', 'warning');
            addConsoleLog('  3. Vérifiez que vous êtes sur HTTPS ou localhost', 'warning');
            showToast('Navigateur non compatible. Consultez la console.', 'error');
            return;
        }
        console.log('✓ Web Serial API disponible');
        addConsoleLog('✓ Web Serial API disponible', 'info');
        
        console.log('Ouverture de la boîte de dialogue de sélection du port...');
        addConsoleLog('📋 Sélectionnez votre Arduino dans la liste...', 'info');
        
        port = await navigator.serial.requestPort();
        console.log('✓ Port sélectionné:', port);
        addConsoleLog('✓ Port sélectionné avec succès', 'info');
        
        console.log('Tentative d\'ouverture du port à 9600 bauds...');
        addConsoleLog('🔓 Ouverture du port série...', 'info');
        
        await port.open({ baudRate: 9600 });
        console.log('✓ Port ouvert à 9600 bauds');
        addConsoleLog('✓ Port ouvert (9600 bauds)', 'info');

        console.log('Création des streams de lecture/écriture...');
        addConsoleLog('📡 Configuration des streams...', 'info');
        
        if (!port.readable) throw new Error('Le port n\'a pas de stream de lecture (readable)');
        if (!port.writable) throw new Error('Le port n\'a pas de stream d\'écriture (writable)');
        console.log('✓ Streams disponibles (readable + writable)');

        const textDecoder = new TextDecoderStream();
        const readableStreamClosed = port.readable.pipeTo(textDecoder.writable);
        reader = textDecoder.readable.getReader();
        console.log('✓ Reader créé');

        const textEncoder = new TextEncoderStream();
        const writableStreamClosed = textEncoder.readable.pipeTo(port.writable);
        writer = textEncoder.writable.getWriter();
        console.log('✓ Writer créé');

        isConnected = true;
        updateConnectionUI(true);
        
        console.log('=== CONNEXION RÉUSSIE ===');
        addConsoleLog('✅ CONNEXION ÉTABLIE AVEC SUCCÈS !', 'info');
        addConsoleLog('📊 En attente des données Arduino...', 'info');
        showToast('Connecté avec succès !', 'success');

        // ✅ AJOUT : Signaler la connexion à Firebase
        if (window.fb) {
            await window.fb.setBridgeOnline();
        }

        readSerialData();

    } catch (error) {
        console.error('=== ERREUR DE CONNEXION ===');
        console.error('Type d\'erreur:', error.name);
        console.error('Message:', error.message);
        console.error('Stack:', error.stack);
        
        addConsoleLog('❌ ERREUR DE CONNEXION', 'error');
        addConsoleLog('Type: ' + error.name, 'error');
        addConsoleLog('Message: ' + error.message, 'error');
        
        if (error.name === 'NotFoundError') {
            addConsoleLog('🔍 DIAGNOSTIC: Aucun port sélectionné', 'warning');
            addConsoleLog('💡 Solutions:', 'warning');
            addConsoleLog('  1. Branchez votre Arduino en USB', 'warning');
            addConsoleLog('  2. Attendez quelques secondes', 'warning');
            addConsoleLog('  3. Réessayez de vous connecter', 'warning');
            addConsoleLog('  4. Si rien n\'apparaît: redémarrez l\'Arduino', 'warning');
            showToast('Aucun port trouvé. Branchez l\'Arduino.', 'error');
            
        } else if (error.name === 'InvalidStateError') {
            addConsoleLog('🔍 DIAGNOSTIC: Port déjà ouvert ailleurs', 'warning');
            addConsoleLog('💡 Solutions:', 'warning');
            addConsoleLog('  1. Fermez le Moniteur Série de l\'IDE Arduino', 'warning');
            addConsoleLog('  2. Fermez tous les autres programmes utilisant le port', 'warning');
            addConsoleLog('  3. Déconnectez et reconnectez l\'Arduino', 'warning');
            addConsoleLog('  4. Rechargez cette page', 'warning');
            showToast('Port déjà utilisé. Fermez le Moniteur Série.', 'error');
            
        } else if (error.name === 'NetworkError') {
            addConsoleLog('🔍 DIAGNOSTIC: Erreur de communication', 'warning');
            addConsoleLog('💡 Solutions:', 'warning');
            addConsoleLog('  1. Vérifiez le câble USB', 'warning');
            addConsoleLog('  2. Changez de port USB', 'warning');
            addConsoleLog('  3. Redémarrez l\'Arduino', 'warning');
            addConsoleLog('  4. Vérifiez que le code Arduino est bien uploadé', 'warning');
            showToast('Erreur de communication USB.', 'error');
            
        } else if (error.name === 'NotAllowedError' || error.name === 'SecurityError') {
            addConsoleLog('🔍 DIAGNOSTIC: Permission refusée', 'warning');
            addConsoleLog('💡 Solutions:', 'warning');
            addConsoleLog('  1. Autorisez l\'accès au port série', 'warning');
            addConsoleLog('  2. Vérifiez les paramètres du navigateur', 'warning');
            addConsoleLog('  3. Si sur HTTPS: vérifiez le certificat', 'warning');
            showToast('Permission refusée. Autorisez l\'accès.', 'error');
            
        } else if (error.message.includes('readable') || error.message.includes('writable')) {
            addConsoleLog('🔍 DIAGNOSTIC: Problème de stream', 'warning');
            addConsoleLog('💡 Solutions:', 'warning');
            addConsoleLog('  1. Déconnectez l\'Arduino', 'warning');
            addConsoleLog('  2. Attendez 5 secondes', 'warning');
            addConsoleLog('  3. Reconnectez l\'Arduino', 'warning');
            addConsoleLog('  4. Rechargez la page', 'warning');
            showToast('Erreur de communication. Reconnectez l\'Arduino.', 'error');
            
        } else {
            addConsoleLog('🔍 DIAGNOSTIC: Erreur inconnue', 'warning');
            addConsoleLog('💡 Actions recommandées:', 'warning');
            addConsoleLog('  1. Notez le message d\'erreur ci-dessus', 'warning');
            addConsoleLog('  2. Déconnectez et reconnectez l\'Arduino', 'warning');
            addConsoleLog('  3. Redémarrez le navigateur', 'warning');
            addConsoleLog('  4. Vérifiez les drivers USB', 'warning');
            showToast('Erreur inconnue. Consultez la console.', 'error');
        }
        
        addConsoleLog('📞 Si le problème persiste:', 'info');
        addConsoleLog('  - Vérifiez que l\'Arduino fonctionne (LED d\'alimentation)', 'info');
        addConsoleLog('  - Testez avec l\'IDE Arduino classique', 'info');
        addConsoleLog('  - Essayez un autre câble USB', 'info');
        addConsoleLog('  - Redémarrez votre ordinateur', 'info');
        
        console.log('=== FIN DU DIAGNOSTIC ===');
    }
}

async function disconnectSerial() {
    try {
        console.log('=== DÉBUT DE LA DÉCONNEXION ===');
        addConsoleLog('🔌 Déconnexion en cours...', 'info');
        
        if (reader) {
            console.log('Fermeture du reader...');
            await reader.cancel();
            reader = null;
            console.log('✓ Reader fermé');
            addConsoleLog('✓ Reader fermé', 'info');
        }
        
        if (writer) {
            console.log('Fermeture du writer...');
            await writer.close();
            writer = null;
            console.log('✓ Writer fermé');
            addConsoleLog('✓ Writer fermé', 'info');
        }
        
        if (port) {
            console.log('Fermeture du port...');
            await port.close();
            port = null;
            console.log('✓ Port fermé');
            addConsoleLog('✓ Port série fermé', 'info');
        }
        
        isConnected = false;
        updateConnectionUI(false);
        
        console.log('=== DÉCONNEXION RÉUSSIE ===');
        addConsoleLog('✅ Déconnexion réussie', 'info');
        showToast('Déconnecté', 'info');

    } catch (error) {
        console.error('=== ERREUR DE DÉCONNEXION ===');
        console.error('Type:', error.name);
        console.error('Message:', error.message);
        
        addConsoleLog('⚠️ Erreur lors de la déconnexion', 'warning');
        addConsoleLog('Message: ' + error.message, 'warning');
        addConsoleLog('💡 Rechargez la page si nécessaire', 'warning');
        
        showToast('Erreur de déconnexion', 'error');
        
        // Forcer la réinitialisation
        isConnected = false;
        updateConnectionUI(false);
        reader = null;
        writer = null;
        port = null;
    }
}

function updateConnectionUI(connected) {
    if (connected) {
        status.textContent = 'État: Connecté ✓';
        status.className = 'status connected';
        btnConnect.style.display = 'none';
        btnDisconnect.style.display = 'inline-block';
    } else {
        status.textContent = 'État: Déconnecté';
        status.className = 'status disconnected';
        btnConnect.style.display = 'inline-block';
        btnDisconnect.style.display = 'none';
    }
}

// ============= LECTURE DES DONNÉES SÉRIE =============
async function readSerialData() {
    let buffer = '';
    
    try {
        while (true) {
            const { value, done } = await reader.read();
            if (done) break;
            
            buffer += value;
            const lines = buffer.split('\n');
            buffer = lines.pop(); // Garder la dernière ligne incomplète
            
            lines.forEach(line => {
                if (line.trim()) {
                    processSerialLine(line.trim());
                }
            });
        }
    } catch (error) {
        console.error('Erreur de lecture:', error);
        if (isConnected) {
            showToast('Connexion perdue', 'error');
            isConnected = false;
            updateConnectionUI(false);
        }
    }
}

async function processSerialLine(line) {
    addConsoleLog(line);
    if (line.includes('L:') && line.includes('S:')) {
        const lightMatch = line.match(/L:(\d+)/);
        const soundMatch = line.match(/S:(\d+)/);
        if (lightMatch) {
            const light = parseInt(lightMatch[1]);
            lightValue.textContent = light;
            
            if (light < THRESHOLD_LIGHT) {
                lightStatus.textContent = '🌑 Obscurité détectée';
                lightStatus.className = 'sensor-status warning';
            } else {
                lightStatus.textContent = '☀️ Lumineux';
                lightStatus.className = 'sensor-status normal';
            }
        } 
        if (soundMatch) {
            const sound = parseInt(soundMatch[1]);
            soundValue.textContent = sound;
            
            if (sound > THRESHOLD_SOUND) {
                soundStatus.textContent = '🔊 Bruit détecté';
                soundStatus.className = 'sensor-status warning';
                // Alerte Firebase bruit
                if (window.fb) {
                    await window.fb.addAlert("sound", "Bruit détecté", sound);
                }
            } else {
                soundStatus.textContent = '🔇 Silencieux';
                soundStatus.className = 'sensor-status normal';
            }
        }
    }
    // Température et Humidité
    if (line.includes('Humidité:') && line.includes('Température:')) {
        const humMatch  = line.match(/Humidité:\s*([\d.]+)/);
        const tempMatch = line.match(/Température:\s*([\d.]+)/);
        
        if (humMatch) {
            const hum = parseFloat(humMatch[1]);
            humValue.textContent = hum.toFixed(1) + '%';
            
            if (hum > HUM_MAX) {
                humStatus.textContent = '⚠️ Humidité élevée !';
                humStatus.className = 'sensor-status alert';
            } else {
                humStatus.textContent = '✓ Normal';
                humStatus.className = 'sensor-status normal';
            }
        }
        if (tempMatch) {
            const temp = parseFloat(tempMatch[1]);
            tempValue.textContent = temp.toFixed(1) + '°C';
            
            if (temp > TEMP_MAX) {
                tempStatus.textContent = '🔥 Trop chaud !';
                tempStatus.className = 'sensor-status alert';
            } else if (temp < TEMP_MIN) {
                tempStatus.textContent = '❄️ Trop froid !';
                tempStatus.className = 'sensor-status alert';
            } else {
                tempStatus.textContent = '✓ Normal';
                tempStatus.className = 'sensor-status normal';
            }
        }
    }
}

// ============= ENVOI DE COMMANDES =============
async function sendCommand(cmd, label = null) {
    if (!isConnected || !writer) {
        showToast('Veuillez vous connecter d\'abord', 'warning');
        return;
    }

    try {
        await writer.write(cmd);
        addConsoleLog(`→ Commande envoyée: ${cmd}`, 'info');

        // Logger dans Firebase
        if (window.fb && label) {
            await window.fb.addHistory(cmd, "web", label);
        }

    } catch (error) {
        addConsoleLog('✗ Erreur d\'envoi: ' + error.message, 'error');
        showToast('Erreur d\'envoi de commande', 'error');
    }
}

// ============= CONSOLE =============
function addConsoleLog(message, type = '') {
    const line = document.createElement('div');
    line.className = `console-line ${type}`;
    
    const timestamp = new Date().toLocaleTimeString();
    line.textContent = `[${timestamp}] ${message}`;
    
    consoleDiv.appendChild(line);
    consoleDiv.scrollTop = consoleDiv.scrollHeight;
}

// ============= NOTIFICATIONS TOAST =============
function showToast(message, type = 'info') {
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ============= GESTION DES PROGRAMMATIONS =============
function updateScheduleList() {
    if (schedules.length === 0) {
        scheduleList.innerHTML = '<p style="text-align: center; color: #999; padding: 20px;">Aucune programmation</p>';
        return;
    }
    
    scheduleList.innerHTML = '';
    schedules.forEach(schedule => {
        const div = document.createElement('div');
        div.className = 'schedule-item';
        div.innerHTML = `
            <span class="schedule-time">🌙 ${schedule.start} → 🌅 ${schedule.end}</span>
            <button class="schedule-delete" onclick="deleteSchedule(${schedule.id})">🗑️ Supprimer</button>
        `;
        scheduleList.appendChild(div);
    });
}

function deleteSchedule(id) {
    schedules = schedules.filter(s => s.id !== id);
    updateScheduleList();
    localStorage.setItem('schedules', JSON.stringify(schedules));
    showToast('Programmation supprimée', 'info');
}

function loadSchedules() {
    const saved = localStorage.getItem('schedules');
    if (saved) {
        schedules = JSON.parse(saved);
        updateScheduleList();
    }
}

// ============= VÉRIFICATION AUTO DES PROGRAMMATIONS =============
function checkSchedules() {
    const now = new Date();
    const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    
    schedules.forEach(schedule => {
        // Vérifier si on est dans la plage horaire
        if (schedule.start <= currentTime && currentTime <= schedule.end) {
            // Activer la veilleuse si pas déjà en mode manuel
            if (switchAuto.checked && !switchPower.checked) {
                switchPower.checked = true;
                sendCommand('w'); // Allumer en blanc
            }
        }
    });
}

// ============= INITIALISATION =============
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM chargé, initialisation...');
    
    // Initialiser tous les éléments DOM
    btnConnect = document.getElementById('btnConnect');
    btnDisconnect = document.getElementById('btnDisconnect');
    btnClearConsole = document.getElementById('btnClearConsole');
    status = document.getElementById('status');
    consoleDiv = document.getElementById('console');
    switchPower = document.getElementById('switchPower');
    switchAuto = document.getElementById('switchAuto');
    
    tempValue = document.getElementById('tempValue');
    tempStatus = document.getElementById('tempStatus');
    humValue = document.getElementById('humValue');
    humStatus = document.getElementById('humStatus');
    lightValue = document.getElementById('lightValue');
    lightStatus = document.getElementById('lightStatus');
    soundValue = document.getElementById('soundValue');
    soundStatus = document.getElementById('soundStatus');
    
    scheduleModal = document.getElementById('scheduleModal');
    btnAddSchedule = document.getElementById('btnAddSchedule');
    btnSaveSchedule = document.getElementById('btnSaveSchedule');
    btnCancelSchedule = document.getElementById('btnCancelSchedule');
    scheduleList = document.getElementById('scheduleList');
    
    toast = document.getElementById('toast');
    
    console.log('Éléments DOM initialisés');
    
    // Event Listeners - Connexion
    btnConnect.addEventListener('click', () => {
        console.log('Bouton de connexion cliqué');
        connectSerial();
    });
    
    btnDisconnect.addEventListener('click', () => {
        console.log('Bouton de déconnexion cliqué');
        disconnectSerial();
    });
    
    // Event Listeners - Console
    btnClearConsole.addEventListener('click', () => {
        consoleDiv.innerHTML = '';
        addConsoleLog('Console effacée', 'info');
    });
    
    // Event Listeners - Modes (mutuellement exclusifs)
    switchPower.addEventListener('change', (e) => {
        if (e.target.checked) {
            // Désactiver le mode automatique
            switchAuto.checked = false;
            
            // Mettre à jour les styles visuels
            switchPower.parentElement.parentElement.classList.add('active');
            switchAuto.parentElement.parentElement.classList.remove('active');
            
            sendCommand('w');
            showToast('Veilleuse allumée (Mode Manuel)', 'success');
            addConsoleLog('Mode manuel activé', 'info');
        } else {
            // Retirer le style actif
            switchPower.parentElement.parentElement.classList.remove('active');
            
            // Si on désactive le mode manuel, activer automatiquement le mode auto
            sendCommand('0');
            showToast('Veilleuse éteinte', 'info');
        }
    });
    
    switchAuto.addEventListener('change', (e) => {
        if (e.target.checked) {
            // Désactiver le mode manuel
            switchPower.checked = false;
            
            // Mettre à jour les styles visuels
            switchAuto.parentElement.parentElement.classList.add('active');
            switchPower.parentElement.parentElement.classList.remove('active');
            
            sendCommand('a');
            showToast('Mode automatique activé', 'success');
            addConsoleLog('Mode automatique: capteurs actifs', 'info');
        } else {
            // Retirer le style actif
            switchAuto.parentElement.parentElement.classList.remove('active');
            
            // Si on désactive le mode auto sans activer le manuel, éteindre
            if (!switchPower.checked) {
                sendCommand('0');
                showToast('Mode automatique désactivé', 'info');
            }
        }
    });
    
    // Event Listeners - Couleurs
    const colorButtons = document.querySelectorAll('.color-btn');
    colorButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const color = btn.dataset.color;
            sendCommand(color);
            
            // Activer le mode manuel et désactiver le mode auto
            switchAuto.checked = false;
            switchPower.checked = true;
            
            const colorNames = {
                'r': 'Rouge',
                'g': 'Vert',
                'b': 'Bleu',
                'y': 'Jaune',
                'w': 'Blanc'
            };
            
            showToast(`Couleur: ${colorNames[color]}`, 'success');
            addConsoleLog(`Couleur sélectionnée: ${colorNames[color]} (Mode Manuel)`, 'info');
        });
    });
    
    // Event Listeners - Mélodies
    const melodyButtons = document.querySelectorAll('.melody-btn');
    melodyButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const melody = btn.dataset.melody;
            sendCommand(melody);
            
            const melodyNames = {
                'p': 'Joyeux Anniversaire',
                's': 'Star Wars',
                'f': 'Frère Jacques',
                '!': 'Sirène d\'Alarme',
                't': 'Mode Test'
            };
            
            showToast(`Lecture: ${melodyNames[melody]}`, 'success');
            addConsoleLog(`♪ Mélodie: ${melodyNames[melody]}`, 'info');
        });
    });
    
    // Event Listeners - Programmations
    btnAddSchedule.addEventListener('click', () => {
        scheduleModal.classList.add('show');
    });
    
    btnCancelSchedule.addEventListener('click', () => {
        scheduleModal.classList.remove('show');
    });
    
    btnSaveSchedule.addEventListener('click', () => {
        const timeStart = document.getElementById('timeStart').value;
        const timeEnd = document.getElementById('timeEnd').value;
        
        if (!timeStart || !timeEnd) {
            showToast('Veuillez remplir tous les champs', 'warning');
            return;
        }
        
        const schedule = {
            id: Date.now(),
            start: timeStart,
            end: timeEnd
        };
        
        schedules.push(schedule);
        updateScheduleList();
        scheduleModal.classList.remove('show');
        showToast('Programmation ajoutée', 'success');
        
        localStorage.setItem('schedules', JSON.stringify(schedules));
    });
    
    // Fermer le modal en cliquant en dehors
    scheduleModal.addEventListener('click', (e) => {
        if (e.target === scheduleModal) {
            scheduleModal.classList.remove('show');
        }
    });
    
    // Charger les programmations
    loadSchedules();
    
    // Initialiser l'état visuel des switches (Mode Auto activé par défaut)
    if (switchAuto.checked) {
        switchAuto.parentElement.parentElement.classList.add('active');
    }
    if (switchPower.checked) {
        switchPower.parentElement.parentElement.classList.add('active');
    }
    
    // Vérifier les programmations toutes les minutes
    setInterval(checkSchedules, 60000);
    
    // Messages de démarrage
    addConsoleLog('🌙 Interface veilleuse IoT prête', 'info');
    addConsoleLog('Connectez votre Arduino via USB pour commencer', 'info');
    
    // Vérifier le support de Web Serial API
    if (!('serial' in navigator)) {
        addConsoleLog('⚠️ Web Serial API non supportée', 'error');
        showToast('Votre navigateur ne supporte pas Web Serial API. Utilisez Chrome ou Edge.', 'error');
        btnConnect.disabled = true;
        btnConnect.style.opacity = '0.5';
        btnConnect.style.cursor = 'not-allowed';
    } else {
        addConsoleLog('✓ Web Serial API disponible', 'info');
    }
    
    console.log('Initialisation terminée');
});