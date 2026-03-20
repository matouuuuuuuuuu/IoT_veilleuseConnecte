package com.example.veilleuse;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnection {
    private static final String TAG = "BluetoothConnection";
    private static final UUID SERIAL_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket  bluetoothSocket;
    private InputStream      inputStream;
    private OutputStream     outputStream;
    private final ConnectionListener connectionListener;
    private ReadThread readThread;
    private volatile boolean isConnected = false;

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onDataReceived(String data);
        void onError(String error);
    }

    public BluetoothConnection(ConnectionListener listener) {
        this.bluetoothAdapter   = BluetoothAdapter.getDefaultAdapter();
        this.connectionListener = listener;
    }

    @SuppressLint("MissingPermission")
    public void connect(String deviceAddress) {
        new Thread(() -> {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                Log.d(TAG, "Connexion a " + device.getName());

                if (bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();

                bluetoothSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
                bluetoothSocket.connect();

                inputStream  = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
                isConnected  = true;

                notifyMain(() -> connectionListener.onConnected());

                readThread = new ReadThread();
                readThread.start();

            } catch (IOException e) {
                Log.e(TAG, "Erreur de connexion", e);
                isConnected = false;
                notifyMain(() -> connectionListener.onError("Connexion echouee : " + e.getMessage()));
            }
        }).start();
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (readThread != null) readThread.interrupt();
            if (outputStream != null) outputStream.close();
            if (inputStream  != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
            Log.d(TAG, "Deconnecte");
            notifyMain(() -> connectionListener.onDisconnected());
        } catch (IOException e) {
            Log.e(TAG, "Erreur de deconnexion", e);
        }
    }

    /**
     * Envoie UN seul caractère vers l'Arduino.
     * Pas de '\n' — l'Arduino lit caractère par caractère dans handleCommand().
     */
    public void sendCommand(String command) {
        if (!isConnected || outputStream == null) {
            Log.e(TAG, "Non connecte");
            notifyMain(() -> connectionListener.onError("Non connecte au Bluetooth"));
            return;
        }
        new Thread(() -> {
            try {
                outputStream.write(command.substring(0, 1).getBytes());
                outputStream.flush();
                Log.d(TAG, "Envoye : " + command.charAt(0));
            } catch (IOException e) {
                Log.e(TAG, "Erreur d'envoi", e);
                notifyMain(() -> connectionListener.onError("Erreur d'envoi : " + e.getMessage()));
            }
        }).start();
    }

    public boolean isConnected() { return isConnected; }

    // ── Thread de lecture en continu ─────────────────────────
    private class ReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (isConnected && !Thread.currentThread().isInterrupted()) {
                try {
                    if (inputStream != null && inputStream.available() > 0) {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            String data = new String(buffer, 0, bytes);
                            Log.d(TAG, "Recu : " + data);
                            notifyMain(() -> connectionListener.onDataReceived(data));
                        }
                    } else {
                        // Petit sleep pour ne pas saturer le CPU
                        Thread.sleep(50);
                    }
                } catch (IOException e) {
                    if (isConnected) {
                        Log.e(TAG, "Erreur de lecture", e);
                        disconnect();
                    }
                    break;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private void notifyMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}