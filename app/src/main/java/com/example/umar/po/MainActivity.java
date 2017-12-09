package com.example.umar.po;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;



public class MainActivity extends AppCompatActivity {

    private TextView batteryState, sharedPreferenceName;
    private Switch wifiState, swtAirPlane;
    private SharedPreferences sp;
    private WifiManager wifiManager;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        sp = getSharedPreferences("WIRELESS_SETTING", MODE_PRIVATE);
        batteryState = findViewById(R.id.txt_battery_status);
        wifiState = findViewById(R.id.switchWifi);
        swtAirPlane = findViewById(R.id.switchAirplane);
        sharedPreferenceName = findViewById(R.id.txt_SharePreference);
        // wifi swtting
        sharedPreferenceName.setText(sp.getString("NAME", ""));
        wifiState.setChecked(sp.getBoolean("WIFI_STATUS", false));
        wifiState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(MainActivity.this, "Wifi On", Toast.LENGTH_SHORT).show();
                } else {
                    wifiManager.setWifiEnabled(false);
                    Toast.makeText(MainActivity.this, "Wifi Off", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Airplane setting
        swtAirPlane.setChecked(sp.getBoolean("AIR_PLANE_STATE", false));
        swtAirPlane.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sp.edit();
                if (isChecked) {
                    editor.putBoolean("AIR_PLANE_STATE", true);
                } else {
                    editor.putBoolean("AIR_PLANE_STATE", false);
                }
                editor.apply();
            }
        });

        // sharedPreference values sync
        sharedPreferenceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor = sp.edit();
                editor.putString("NAME", sharedPreferenceName.getText().toString());
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // communication between broadcast receiver and this class
    public void myReceiver(OnReceived received) {
        Intent intent = received.intent;
        if (intent.getAction().equals("android.intent.action.AIRPLANE_MODE")) {
            swtAirPlane.setChecked(intent.getBooleanExtra("state", false));
        }
        if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
            wifiState.setChecked(wifiManager.isWifiEnabled());
        }
    }

    // battery sigficant change broadcast receiver
    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
                batteryState.setText("Battery Status:  OKAY");
                batteryState.setBackgroundColor(getResources().getColor(R.color.green));
            }
            if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
                batteryState.setText("Battery Status:  LOW");
                Log.d("MTAG", "onReceive: low");
                batteryState.setBackgroundColor(getResources().getColor(R.color.red));
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(batteryReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
        super.onPause();
    }
}