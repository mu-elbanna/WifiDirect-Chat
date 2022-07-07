package com.example.wifidirectchat;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * This class is responsible for storing the mobulor name
 */
public class LocalDevice {
    private static final LocalDevice instance = new LocalDevice();
    private WifiP2pDevice device;

    private LocalDevice() {
        device = new WifiP2pDevice();
    }

    public static LocalDevice getInstance() {
        return instance;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    void setDevice(WifiP2pDevice device) {
        this.device = device;
    }
}
