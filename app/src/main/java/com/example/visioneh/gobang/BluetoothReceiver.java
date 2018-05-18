package com.example.visioneh.gobang;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class BluetoothReceiver extends BroadcastReceiver {


    private List<BluetoothDevice> mDevices;
    private List<Bluetooth> mBluetoothList;
    public OnReceiverListener mOnReceiverListener;


    public BluetoothReceiver(List<BluetoothDevice> devices, List<Bluetooth> bluetooths, OnReceiverListener onReceiverListener) {
        mOnReceiverListener = onReceiverListener;
        mDevices = devices;
        mBluetoothList = bluetooths;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            ///获取扫描到的Device信息
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (!mDevices.contains(device)) {
                Bluetooth bluetooth = new Bluetooth(device.getName(), device.getAddress());
                mBluetoothList.add(bluetooth);
                mDevices.add(device);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            mOnReceiverListener.showText();
        }

        if (mOnReceiverListener != null) {
            mOnReceiverListener.setBluetoothList(mBluetoothList, mDevices);
        }
    }

    //设置给BluetoothActivity的回调接口
    public interface OnReceiverListener {
        void setBluetoothList(List<Bluetooth> bluetooths, List<BluetoothDevice> devices);
        void showText();
    }

    public static class Bluetooth{
        private String name;
        private String address;

        public Bluetooth(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

}
