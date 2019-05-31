package dev.sood.wifip2pdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private MainActivity wifiActivity;
    private WifiP2pManager.PeerListListener peerListListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        super();
        this.wifiP2pManager = manager;
        this.channel = channel;
        this.wifiActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFi P2P enabled on device", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "WiFi P2P disabled on device", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(context, "Discovered peers", Toast.LENGTH_SHORT).show();

            if(wifiP2pManager != null) {
                wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        String devicesString = "Device names:";
                        TextView textView = wifiActivity.findViewById(R.id.device_list_text_view);
                        for(WifiP2pDevice device : peers.getDeviceList()) {
                            devicesString += "\n" + device.deviceName;
                        }

                        textView.setText(devicesString);
                    }
                });
            }

        } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

        } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            WifiP2pDevice self = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Toast.makeText(wifiActivity.getApplicationContext(), "Self: "+self.deviceName, Toast.LENGTH_LONG).show();

        }

    }
}
