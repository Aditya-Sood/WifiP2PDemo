package dev.sood.wifip2pdemo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //TODO: Refactor (multiple arraylists??)

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel channel;
    private WifiDirectBroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private Spinner peerSpinner;
    private ArrayList<WifiP2pDevice> peerDevices = new ArrayList<WifiP2pDevice>();
    private WifiP2pDeviceArrayAdapter adapter;
    private boolean userInteracting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiP2pDevice foobar = new WifiP2pDevice();
        foobar.deviceName = "Searching for peers...";
        peerDevices.add(foobar);
        this.adapter  = new WifiP2pDeviceArrayAdapter(getApplicationContext(), R.layout.spinner_item_device, peerDevices);
        this.peerSpinner = findViewById(R.id.spinner_devices_list);
        peerSpinner.setAdapter(adapter);
        peerSpinner.setOnItemSelectedListener(this);

        p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = p2pManager.initialize(this, getMainLooper(), null);
        broadcastReceiver = new WifiDirectBroadcastReceiver(p2pManager, channel, this);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled() != true) {
            wifiManager.setWifiEnabled(true);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Button wifiButton = findViewById(R.id.wifi_button);
        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkLocationServicesEnabled();
                }

                //TODO: Replace with adapter.notifyDataSetChanged()
                //broadcastReceiver.clearPeerList();

                p2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener(){

                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Peer discovery call finished", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        String error = "";
                        switch (reason) {
                            case WifiP2pManager.ERROR:           error = "Internal error"; break;
                            case WifiP2pManager.BUSY:            error = "Framework busy, unable to service request"; break;
                            case WifiP2pManager.P2P_UNSUPPORTED: error = "P2P unsupported on this device"; break;

                            default: error = "Unknown error"; break;
                        }
                        Toast.makeText(getApplicationContext(), "Operation failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        wifiButton.setEnabled(false);
    }

    public void setDevicesSpinner(ArrayList<WifiP2pDevice> deviceList) {
        this.peerDevices = deviceList;

        //TODO: Add flag for enabling connect button (if peers exist)
        //this.adapter  = new WifiP2pDeviceArrayAdapter(getApplicationContext(), R.layout.spinner_item_device, peerDevices);
        adapter.clear();
        if(deviceList != null) {
            for(WifiP2pDevice device : peerDevices) {
                adapter.add(device);
            }
            // Don't change user selection
            //peerSpinner.setSelection(0, false);
        }

        adapter.notifyDataSetChanged();

        //this.peerSpinner = findViewById(R.id.spinner_devices_list);

        //peerSpinner.setAdapter(adapter);

        /*peerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Only called the first time the item is selected
                *//*String deviceName = ((WifiP2pDevice) parent.getItemAtPosition(position)).deviceName;
                Toast.makeText(getApplicationContext(), deviceName, Toast.LENGTH_SHORT).show();*//*
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                *//*String deviceName = ((WifiP2pDevice) parent.getItemAtPosition(position)).deviceName;
                Toast.makeText(getApplicationContext(), deviceName, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_SHORT).show();*//*
            }
        });*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Granted", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            default:
                Toast.makeText(getApplicationContext(), "Loc req not granted", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {ex.printStackTrace();}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {ex.printStackTrace();}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("Enable location to allow detection of peers")
                    .setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: {
                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    return;
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage("Cannot discover peers without location services")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //((MainActivity) getApplicationContext()).finish();
                                }
                            })
                            .show();
                }

            }
        }
    }
    public void setUserInteracting(boolean value) {
        userInteracting = value;
    }

    /*@Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userInteracting = true;
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String deviceName = ((WifiP2pDevice) parent.getItemAtPosition(position)).deviceName;
        Toast.makeText(getApplicationContext(), deviceName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_SHORT).show();
    }
}
