package dev.sood.wifip2pdemo;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WifiP2pDeviceArrayAdapter extends ArrayAdapter<WifiP2pDevice> {

    private ArrayList<WifiP2pDevice> availableDevices;
    private int resource;
    private Context context;

    public WifiP2pDeviceArrayAdapter(@NonNull  Context context, int resource, List<WifiP2pDevice> objects) {
        super(context, resource, 0, objects);
        this.availableDevices = (ArrayList<WifiP2pDevice>) objects;
        this.resource = resource;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createSpinnerView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createSpinnerView(position, convertView, parent);
    }

    private View createSpinnerView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        View spinnerItem = convertView;
        if(spinnerItem == null) {
            spinnerItem = LayoutInflater.from(context).inflate(R.layout.spinner_item_device, parent, false);
        }

        WifiP2pDevice wifiP2pDevice = availableDevices.get(position);

        TextView deviceNameTextView = spinnerItem.findViewById(R.id.text_view_device_name);
        deviceNameTextView.setText(wifiP2pDevice.deviceName);

        return spinnerItem;
    }

    /*@Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getDropDownView(position, convertView, parent);

        if(convertView == null) {
            //convertView = parent.lay
        }
    }*/
}
