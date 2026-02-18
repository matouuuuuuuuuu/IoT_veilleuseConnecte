<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🌙 Contrôle Veilleuse IoT</title>
    <link rel="stylesheet" href="./style/style.css">
</head>
<body>
    <section>
        <div class="container">
            <h1>🌙 Contrôle Veilleuse IoT</h1>
            
            <!-- Connexion USB -->
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

            <!-- Capteurs en temps réel -->
            <div class="section">
                <h2>📊 Capteurs en Temps Réel</h2>
                <div class="sensors-grid">
                    <div class="sensor-card">
                        <div class="sensor-icon">🌡️</div>
                        <div class="sensor-label">Température</div>
                        <div id="tempValue" class="sensor-value">--°C</div>
                        <div id="tempStatus" class="sensor-status">En attente...</div>
                    </div>
                    
                    <div class="sensor-card">
                        <div class="sensor-icon">💧</div>
                        <div class="sensor-label">Humidité</div>
                        <div id="humValue" class="sensor-value">--%</div>
                        <div id="humStatus" class="sensor-status">En attente...</div>
                    </div>
                    
                    <div class="sensor-card">
                        <div class="sensor-icon">💡</div>
                        <div class="sensor-label">Luminosité</div>
                        <div id="lightValue" class="sensor-value">--</div>
                        <div id="lightStatus" class="sensor-status">En attente...</div>
                    </div>
                    
                    <div class="sensor-card">
                        <div class="sensor-icon">🔊</div>
                        <div class="sensor-label">Son</div>
                        <div id="soundValue" class="sensor-value">--</div>
                        <div id="soundStatus" class="sensor-status">En attente...</div>
                    </div>
                </div>
            </div>

            <!-- Modes -->
            <div class="section">
                <h2>⚡ Modes de Fonctionnement</h2>
                <div class="switch-container">
                    <span>Veilleuse (Mode Manuel)</span>
                    <label class="switch">
                        <input type="checkbox" id="switchPower">
                        <span class="slider"></span>
                    </label>
                </div>
                <div class="switch-container">
                    <span>Mode Automatique (Capteurs)</span>
                    <label class="switch">
                        <input type="checkbox" id="switchAuto" checked>
                        <span class="slider"></span>
                    </label>
                </div>
            </div>

            <!-- Couleurs -->
            <div class="section">
                <h2>🎨 Couleurs LED</h2>
                <div class="color-grid">
                    <button class="color-btn" data-color="r">
                        <span class="color-preview red"></span>
                        Rouge
                    </button>
                    <button class="color-btn" data-color="g">
                        <span class="color-preview green"></span>
                        Vert
                    </button>
                    <button class="color-btn" data-color="b">
                        <span class="color-preview blue"></span>
                        Bleu
                    </button>
                    <button class="color-btn" data-color="y">
                        <span class="color-preview yellow"></span>
                        Jaune
                    </button>
                    <button class="color-btn" data-color="w">
                        <span class="color-preview white"></span>
                        Blanc
                    </button>
                </div>
            </div>

            <!-- Section Mélodies et Sons -->
            <div class="section">
                <h2>🎵 Mélodies & Sons</h2>
                <div class="melody-grid">
                    <button class="melody-btn" data-melody="p">
                        <span class="melody-icon">🎂</span>
                        <div class="melody-info">
                            <div class="melody-name">Joyeux Anniversaire</div>
                            <div class="melody-desc">Avec harmonies (3 buzzers)</div>
                        </div>
                    </button>
                    
                    <button class="melody-btn" data-melody="s">
                        <span class="melody-icon">⭐</span>
                        <div class="melody-info">
                            <div class="melody-name">Star Wars</div>
                            <div class="melody-desc">Thème épique avec basse</div>
                        </div>
                    </button>
                    
                    <button class="melody-btn" data-melody="f">
                        <span class="melody-icon">🔔</span>
                        <div class="melody-info">
                            <div class="melody-name">Frère Jacques</div>
                            <div class="melody-desc">Canon à 3 voix</div>
                        </div>
                    </button>
                    
                    <button class="melody-btn alarm" data-melody="!">
                        <span class="melody-icon">🚨</span>
                        <div class="melody-info">
                            <div class="melody-name">Sirène d'Alarme</div>
                            <div class="melody-desc">Effet stéréo (20 cycles)</div>
                        </div>
                    </button>
                    
                    <button class="melody-btn test" data-melody="t">
                        <span class="melody-icon">🧪</span>
                        <div class="melody-info">
                            <div class="melody-name">Mode Test</div>
                            <div class="melody-desc">Test buzzers & LEDs</div>
                        </div>
                    </button>
                    
                    <button class="melody-btn stop" data-melody="x">
                        <span class="melody-icon">⏹️</span>
                        <div class="melody-info">
                            <div class="melody-name">Arrêter la Musique</div>
                            <div class="melody-desc">Stopper tous les buzzers</div>
                        </div>
                    </button>
                </div>
            </div>


            <!-- Programmations -->
            <div class="section">
                <h2>⏰ Programmations Horaires</h2>
                <button id="btnAddSchedule" class="btn btn-primary">
                    ➕ Ajouter une programmation
                </button>
                <div id="scheduleList" class="schedule-list">
                    <p style="text-align: center; color: #999; padding: 20px;">Aucune programmation</p>
                </div>
            </div>

            <!-- Console -->
            <div class="section">
                <h2>📟 Console de Débogage</h2>
                <button id="btnClearConsole" class="btn btn-secondary" style="margin-bottom: 10px;">
                    🗑️ Effacer la console
                </button>
                <div id="console" class="console"></div>
            </div>
        </div>

        <!-- Modal Programmation -->
        <div id="scheduleModal" class="modal">
            <div class="modal-content">
                <h2>⏰ Nouvelle Programmation</h2>
                <div class="time-picker">
                    <label>🌙 Heure de début:</label>
                    <input type="time" id="timeStart" value="20:00">
                </div>
                <div class="time-picker">
                    <label>🌅 Heure de fin:</label>
                    <input type="time" id="timeEnd" value="07:00">
                </div>
                <div class="modal-buttons">
                    <button class="btn btn-primary" id="btnSaveSchedule">✓ Ajouter</button>
                    <button class="btn btn-secondary" id="btnCancelSchedule">✗ Annuler</button>
                </div>
            </div>
        </div>

        <!-- Toast Notifications -->
        <div id="toast" class="toast"></div>
    </section>

    <script>
        document.querySelectorAll('.melody-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.melody-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
            });
        });
    </script>
    <script src="./js/functions.js"></script>
    </body>
</html>