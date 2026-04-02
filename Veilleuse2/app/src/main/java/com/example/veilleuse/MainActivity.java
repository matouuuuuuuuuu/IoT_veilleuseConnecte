package com.example.veilleuse;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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


    // UI
    private Switch   switchPower;
    private Switch   switchAutoMode;
    private TextView tvStatus;
    private TextView tvLight;
    private TextView tvSound;
    private TextView tvTempAlert;
    private Button   btnColorWarm, btnColorCool;
    private Button   btnColorRed, btnColorGreen, btnColorBlue;

    private String bluetoothDeviceAddress = null;

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
                    tvLight.setText("--");
                    tvSound.setText("--");
                    tvTempAlert.setText("");
                    Toast.makeText(MainActivity.this, "Deconnecte", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onDataReceived(String data) {
                runOnUiThread(() -> processLine(data.trim()));
            }
            @Override public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erreur: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void processLine(String line) {
        if (line.isEmpty()) return;

        if (line.startsWith("L:")) {
            tvLight.setText(line.substring(2));

        } else if (line.startsWith("S:")) {
            tvSound.setText(line.substring(2));


        } else if (!line.startsWith("USB") && !line.startsWith("BT") && !line.startsWith("Systeme")) {
            // Réponses commandes (Rouge, Vert, Mode Auto…)
            tvStatus.setText("Arduino: " + line);
        }
    }

    private void initializeUI() {
        switchPower    = findViewById(R.id.switchPower);
        switchAutoMode = findViewById(R.id.switchAutoMode);
        tvStatus       = findViewById(R.id.tvStatus);
        tvLight        = findViewById(R.id.tvLight);
        tvSound        = findViewById(R.id.tvSound);
        tvTempAlert    = findViewById(R.id.tvTempAlert);
        btnColorWarm   = findViewById(R.id.btnColorWarm);
        btnColorCool   = findViewById(R.id.btnColorCool);
        btnColorRed    = findViewById(R.id.btnColorRed);
        btnColorGreen  = findViewById(R.id.btnColorGreen);
        btnColorBlue   = findViewById(R.id.btnColorBlue);

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
        Button btnStop = findViewById(R.id.btnStop);
        if (btnStop != null) btnStop.setOnClickListener(v -> {
            // Envoi en rafale pour passer entre les delay() de l'Arduino
            new Thread(() -> {
                for (int i = 0; i < 20; i++) {
                    bluetoothConnection.sendCommand("x");
                    try { Thread.sleep(50); } catch (Exception ignored) {}
                }
            }).start();
            toast("Stop");
        });
    }

    private void send(char cmd) { bluetoothConnection.sendCommand(String.valueOf(cmd)); }

    private void highlightColor(Button active) {
        Button[] all = {btnColorWarm, btnColorCool, btnColorRed, btnColorGreen, btnColorBlue};
        for (Button b : all) if (b != null) b.setAlpha(b == active ? 1.0f : 0.45f);
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
}
