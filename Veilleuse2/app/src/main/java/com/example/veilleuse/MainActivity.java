package com.example.veilleuse;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;
    private static final int REQUEST_ENABLE_BT             = 101;
    private static final int REQUEST_SELECT_DEVICE         = 102;

    private static final char CMD_RED    = '1';
    private static final char CMD_GREEN  = '2';
    private static final char CMD_BLUE   = '3';
    private static final char CMD_YELLOW = '4';
    private static final char CMD_WHITE  = '5';
    private static final char CMD_OFF    = '0';
    private static final char CMD_SIREN  = '!';
    private static final char CMD_MEL_P  = 'p';
    private static final char CMD_MEL_S  = 's';
    private static final char CMD_MEL_F  = 'f';
    private static final char CMD_TEST   = 't';

    private static final float HUM_MAX  = 50.0f;
    private static final float TEMP_MAX = 25.0f;
    private static final float TEMP_MIN = 10.0f;

    // UI
    private Switch   switchPower;
    private Switch   switchAutoMode;
    private TextView tvStatus;
    private TextView tvScheduleList;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvLight;
    private TextView tvSound;
    private TextView tvTempAlert;
    private Button   btnColorWarm, btnColorCool;
    private Button   btnColorRed, btnColorGreen, btnColorBlue;
    private Button   btnAddSchedule;

    private boolean isAutoModeEnabled = false;
    private final List<Schedule> schedules = new ArrayList<>();
    private String bluetoothDeviceAddress = null;
    private final StringBuilder dataBuffer = new StringBuilder();

    private BluetoothConnection bluetoothConnection;
    private android.bluetooth.BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        initBluetooth();
        requestBluetoothPermissions();
        setupListeners();
    }

    private void initBluetooth() {
        android.bluetooth.BluetoothManager manager =
                getSystemService(android.bluetooth.BluetoothManager.class);
        bluetoothAdapter = manager.getAdapter();

        bluetoothConnection = new BluetoothConnection(new BluetoothConnection.ConnectionListener() {
            @Override public void onConnected() {
                runOnUiThread(() -> {
                    tvStatus.setText("Etat: Connecte");
                    tvStatus.setTextColor(0xFF4CAF50);
                    tvStatus.setBackgroundColor(0xFF001A00);
                    Toast.makeText(MainActivity.this, "Connecte au Bluetooth", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onDisconnected() {
                runOnUiThread(() -> {
                    tvStatus.setText("Etat: Deconnecte");
                    tvStatus.setTextColor(0xFFFF5252);
                    tvStatus.setBackgroundColor(0xFF1A0000);
                    tvTemperature.setText("--C");
                    tvHumidity.setText("--%");
                    tvLight.setText("--");
                    tvSound.setText("--");
                    tvTempAlert.setText("");
                    Toast.makeText(MainActivity.this, "Deconnecte", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onDataReceived(String data) {
                runOnUiThread(() -> {
                    // Accumule les données reçues
                    dataBuffer.append(data);
                    String buffer = dataBuffer.toString();

                    // Traite chaque ligne complète (avec ou sans \n)
                    // On force aussi le traitement si on voit "L:" ou "Humidi"
                    if (buffer.contains("\n")) {
                        String[] lines = buffer.split("\n");
                        // Toutes les lignes sauf la dernière (peut être incomplète)
                        for (int i = 0; i < lines.length - 1; i++) {
                            processLine(lines[i].trim());
                        }
                        // Garde le dernier fragment incomplet
                        dataBuffer.setLength(0);
                        dataBuffer.append(lines[lines.length - 1]);
                    } else if (buffer.length() > 30) {
                        // Sécurité : si pas de \n mais buffer long, on force
                        processLine(buffer.trim());
                        dataBuffer.setLength(0);
                    }
                });
            }
            @Override public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erreur: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  Traitement d'une ligne Arduino
    //  Formats :
    //    "L:123 S:456"                            -> capteurs loop
    //    "Humidite: 45.00%  Temperature: 22.50C"  -> DHT11
    //    "Rouge", "Vert", "Mode Auto"...           -> réponses
    // ═══════════════════════════════════════════════════════════
    private void processLine(String line) {
        if (line.isEmpty()) return;

        // ── L:xxx S:xxx ───────────────────────────────────────
        if (line.startsWith("L:")) {
            try {
                // Format : "L:123 S:456"
                String[] parts = line.split(" ");
                for (String part : parts) {
                    if (part.startsWith("L:")) {
                        int val = Integer.parseInt(part.substring(2).trim());
                        tvLight.setText(String.valueOf(val));
                    } else if (part.startsWith("S:")) {
                        int val = Integer.parseInt(part.substring(2).trim());
                        tvSound.setText(String.valueOf(val));
                    }
                }
            } catch (Exception ignored) {}
            return;
        }

        // ── DHT11 humidité ────────────────────────────────────
        if (line.contains("Humidi")) {
            try {
                int pIdx = line.indexOf('%');
                if (pIdx > 0) {
                    int start = pIdx - 1;
                    while (start > 0 &&
                            (Character.isDigit(line.charAt(start - 1)) || line.charAt(start - 1) == '.'))
                        start--;
                    float hum = Float.parseFloat(line.substring(start, pIdx).trim());
                    tvHumidity.setText(String.format("%.1f%%", hum));
                    if (hum > HUM_MAX) {
                        tvTempAlert.setText("Humidite trop elevee !");
                        tvTempAlert.setTextColor(0xFFE91E63);
                    }
                }
            } catch (Exception ignored) {}
        }

        // ── DHT11 température ─────────────────────────────────
        if (line.contains("Temp")) {
            try {
                // Cherche le dernier nombre avant 'C'
                int cIdx = line.lastIndexOf('C');
                if (cIdx > 0) {
                    int start = cIdx - 1;
                    while (start > 0 &&
                            (Character.isDigit(line.charAt(start - 1)) || line.charAt(start - 1) == '.'))
                        start--;
                    float temp = Float.parseFloat(line.substring(start, cIdx).trim());
                    tvTemperature.setText(String.format("%.1f°C", temp));

                    if (temp > TEMP_MAX) {
                        tvTempAlert.setText("Temperature trop elevee !");
                        tvTempAlert.setTextColor(0xFFFF5722);
                    } else if (temp < TEMP_MIN) {
                        tvTempAlert.setText("Temperature trop basse !");
                        tvTempAlert.setTextColor(0xFF2196F3);
                    } else {
                        String alert = tvTempAlert.getText().toString();
                        if (!alert.contains("Humidite")) {
                            tvTempAlert.setText("Conditions normales");
                            tvTempAlert.setTextColor(0xFF4CAF50);
                        }
                    }
                }
            } catch (Exception ignored) {}
            return;
        }

        // ── Réponses commandes ────────────────────────────────
        if (!line.startsWith("L:") && !line.startsWith("S:") &&
                !line.startsWith("USB") && !line.startsWith("BT") &&
                !line.startsWith("Systeme") && !line.isEmpty()) {
            tvStatus.setText("Arduino: " + line);
        }
    }

    private void initializeUI() {
        switchPower    = findViewById(R.id.switchPower);
        switchAutoMode = findViewById(R.id.switchAutoMode);
        tvStatus       = findViewById(R.id.tvStatus);
        tvScheduleList = findViewById(R.id.tvScheduleList);
        tvTemperature  = findViewById(R.id.tvTemperature);
        tvHumidity     = findViewById(R.id.tvHumidity);
        tvLight        = findViewById(R.id.tvLight);
        tvSound        = findViewById(R.id.tvSound);
        tvTempAlert    = findViewById(R.id.tvTempAlert);
        btnColorWarm   = findViewById(R.id.btnColorWarm);
        btnColorCool   = findViewById(R.id.btnColorCool);
        btnColorRed    = findViewById(R.id.btnColorRed);
        btnColorGreen  = findViewById(R.id.btnColorGreen);
        btnColorBlue   = findViewById(R.id.btnColorBlue);
        btnAddSchedule = findViewById(R.id.btnAddSchedule);

        Button btnConnect = findViewById(R.id.btnConnectBluetooth);
        if (btnConnect != null)
            btnConnect.setOnClickListener(v -> connectBluetoothDevice());
    }

    private void setupListeners() {
        if (switchPower != null) {
            switchPower.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    send(CMD_WHITE);
                    tvStatus.setText("Allumee");
                    tvStatus.setTextColor(0xFF4CAF50);
                    tvStatus.setBackgroundColor(0xFF001A00);
                } else {
                    send(CMD_OFF);
                    tvStatus.setText("Eteinte");
                    tvStatus.setTextColor(0xFFFF5252);
                    tvStatus.setBackgroundColor(0xFF1A0000);
                }
            });
        }
        if (switchAutoMode != null) {
            switchAutoMode.setOnCheckedChangeListener((btn, isChecked) -> {
                isAutoModeEnabled = isChecked;
                if (isChecked) { send(CMD_OFF);   toast("Mode auto active"); }
                else           { send(CMD_WHITE); toast("Mode manuel active"); }
            });
        }

        if (btnColorWarm  != null) btnColorWarm.setOnClickListener(v ->  { send(CMD_YELLOW); highlightColor(btnColorWarm);  });
        if (btnColorCool  != null) btnColorCool.setOnClickListener(v ->  { send(CMD_WHITE);  highlightColor(btnColorCool);  });
        if (btnColorRed   != null) btnColorRed.setOnClickListener(v ->   { send(CMD_RED);    highlightColor(btnColorRed);   });
        if (btnColorGreen != null) btnColorGreen.setOnClickListener(v -> { send(CMD_GREEN);  highlightColor(btnColorGreen); });
        if (btnColorBlue  != null) btnColorBlue.setOnClickListener(v ->  { send(CMD_BLUE);   highlightColor(btnColorBlue);  });

        Button btnSiren = findViewById(R.id.btnSiren);
        if (btnSiren != null) btnSiren.setOnClickListener(v -> { send(CMD_SIREN); toast("Sirene !"); });
        Button btnMelP = findViewById(R.id.btnMelodyP);
        if (btnMelP != null) btnMelP.setOnClickListener(v -> { send(CMD_MEL_P); toast("Joyeux Anniversaire"); });
        Button btnMelS = findViewById(R.id.btnMelodyS);
        if (btnMelS != null) btnMelS.setOnClickListener(v -> { send(CMD_MEL_S); toast("Star Wars"); });
        Button btnMelF = findViewById(R.id.btnMelodyF);
        if (btnMelF != null) btnMelF.setOnClickListener(v -> { send(CMD_MEL_F); toast("Frere Jacques"); });
        Button btnTest = findViewById(R.id.btnTest);
        if (btnTest != null) btnTest.setOnClickListener(v -> { send(CMD_TEST); toast("Sequence test"); });

        if (btnAddSchedule != null)
            btnAddSchedule.setOnClickListener(v -> showScheduleDialog());
    }

    private void send(char cmd) { bluetoothConnection.sendCommand(String.valueOf(cmd)); }

    private void highlightColor(Button active) {
        Button[] all = {btnColorWarm, btnColorCool, btnColorRed, btnColorGreen, btnColorBlue};
        for (Button b : all) if (b != null) b.setAlpha(b == active ? 1.0f : 0.45f);
    }

    private void showScheduleDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Nouvelle programmation");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_schedule, null);
        TimePicker tpStart = dialogView.findViewById(R.id.timePickerStart);
        TimePicker tpEnd   = dialogView.findViewById(R.id.timePickerEnd);
        tpStart.setIs24HourView(true);
        tpEnd.setIs24HourView(true);
        builder.setView(dialogView);
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            Schedule s = new Schedule(
                    tpStart.getHour(), tpStart.getMinute(),
                    tpEnd.getHour(),   tpEnd.getMinute());
            schedules.add(s);
            updateScheduleList();
            toast("Programmation ajoutee : " + s);
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void updateScheduleList() {
        if (tvScheduleList == null) return;
        if (schedules.isEmpty()) { tvScheduleList.setText("Aucune programmation"); return; }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < schedules.size(); i++)
            sb.append(i + 1).append(". ").append(schedules.get(i)).append("\n");
        tvScheduleList.setText(sb.toString().trim());
    }

    public void connectBluetoothDevice() {
        startActivityForResult(new Intent(this, BluetoothDeviceActivity.class), REQUEST_SELECT_DEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_DEVICE && resultCode == RESULT_OK) {
            bluetoothDeviceAddress = data.getStringExtra("DEVICE_ADDRESS");
            toast("Connexion a " + bluetoothDeviceAddress);
            bluetoothConnection.connect(bluetoothDeviceAddress);
        }
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothConnection != null && bluetoothConnection.isConnected())
            bluetoothConnection.disconnect();
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    private static class Schedule {
        final int startHour, startMinute, endHour, endMinute;
        Schedule(int sh, int sm, int eh, int em) {
            startHour = sh; startMinute = sm; endHour = eh; endMinute = em;
        }
        @Override public String toString() {
            return String.format("%02d:%02d -> %02d:%02d", startHour, startMinute, endHour, endMinute);
        }
    }
}