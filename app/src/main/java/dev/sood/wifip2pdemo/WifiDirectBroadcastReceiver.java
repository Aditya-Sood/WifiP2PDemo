package dev.sood.wifip2pdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private MainActivity wifiActivity;
    private ArrayList<WifiP2pDevice> peerDevices = new ArrayList<WifiP2pDevice>();
    private WifiP2pDeviceArrayAdapter adapter;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        super();
        this.wifiP2pManager = manager;
        this.channel = channel;
        this.wifiActivity = activity;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFi P2P enabled on device", Toast.LENGTH_SHORT).show();
                Button wifiButton = wifiActivity.findViewById(R.id.wifi_button);
                wifiButton.setEnabled(true);
            } else {
                Toast.makeText(context, "WiFi P2P disabled on device", Toast.LENGTH_SHORT).show();
                Button wifiButton = wifiActivity.findViewById(R.id.wifi_button);
                wifiButton.setEnabled(false);
                return;
            }
        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(context, "Peers changed", Toast.LENGTH_SHORT).show();

            if(wifiP2pManager != null) {
                wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        String devicesString = "Device names:";
                        TextView textView = wifiActivity.findViewById(R.id.device_list_text_view);
                        peerDevices.clear();
                        for(WifiP2pDevice device : peers.getDeviceList()) {
                            devicesString += "\n" + device.deviceName;
                            peerDevices.add(device);
                        }

                        textView.setText(devicesString);

                        if(!peerDevices.isEmpty()) {
                            adapter  = new WifiP2pDeviceArrayAdapter(context, R.layout.spinner_item_device, peerDevices);

                            Spinner spinner = wifiActivity.findViewById(R.id.spinner_devices_list);
                            spinner.setAdapter(adapter);
                        } else {
                            textView.setText("NO PEERS");
                        }

                    }
                });
            }

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            WifiP2pDevice self = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Toast.makeText(wifiActivity.getApplicationContext(), "Self: "+self.deviceName, Toast.LENGTH_LONG).show();

        }

    }

    public void clearPeerList() {
        if(peerDevices != null)
            peerDevices.clear();

        if(adapter != null)
            adapter.clear();
    }
/*
    public WifiP2pDeviceArrayAdapter getAdapter() {
        return adapter;
    }

    public ArrayList<WifiP2pDevice> getArrayList() {
        return peerDevices;
    }*/
}
