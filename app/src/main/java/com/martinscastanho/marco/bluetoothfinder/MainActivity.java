package com.martinscastanho.marco.bluetoothfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    TextView statusTextView;
    Button searchButton;

    BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // decide what to do whenever we receive one of the filtered actions
            String action = intent.getAction();
            Log.i("ACTION", action);

            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                // we have finished our search
                statusTextView.setText("");
                searchButton.setEnabled(true);
            }
            else if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

                Log.i("DEVICE FOUND", "Name: "+ name + ", address: "+ address + ", RSSI:" + rssi);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        statusTextView = findViewById(R.id.statusTextView);
        searchButton = findViewById(R.id.searchButton);

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADMIN}, 1);
        }
        else {
            setBluetoothAdapter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0){
                boolean isPermissionGranted = true;
                for (int grantResult : grantResults) {
                    isPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
                }
                if(isPermissionGranted){
                    setBluetoothAdapter();
                }
            }

        }
    }

    public void setBluetoothAdapter(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter intentFilter = new IntentFilter();     // filter the huge amount of info received from Bluetooth
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void searchClicked(View view){
        statusTextView.setText("Searching...");
        searchButton.setEnabled(false);
        bluetoothAdapter.startDiscovery();
    }
}
