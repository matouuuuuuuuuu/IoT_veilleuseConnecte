package com.example.veilleuse;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothDeviceActivity extends AppCompatActivity {
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;

    private ListView listViewDevices;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceAdapter;
    private List<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);

        listViewDevices = findViewById(R.id.listViewDevices);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<>();

        // Demander les permissions
        requestBluetoothPermissions();

        // Charger les appareils appairés
        loadPairedDevices();

        // Configurer le listener pour la sélection d'appareil
        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = pairedDevices.get(position);
            returnDeviceAddress(device.getAddress());
        });
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void loadPairedDevices() {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        List<String> deviceNames = new ArrayList<>();

        for (BluetoothDevice device : devices) {
            deviceNames.add(device.getName() + " (" + device.getAddress() + ")");
            pairedDevices.add(device);
        }

        if (deviceNames.isEmpty()) {
            Toast.makeText(this, "Aucun appareil appairé", Toast.LENGTH_SHORT).show();
        }

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNames);
        listViewDevices.setAdapter(deviceAdapter);
    }

    private void returnDeviceAddress(String address) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("DEVICE_ADDRESS", address);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}