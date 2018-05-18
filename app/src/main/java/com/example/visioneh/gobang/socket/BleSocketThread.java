package com.example.visioneh.gobang.socket;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.example.visioneh.gobang.MainActivity;
import com.hss01248.dialog.MyActyManager;
import com.hss01248.dialog.StyledDialog;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Time:2017.4.5 13:46
 * Created By:ThatNight
 */

public class BleSocketThread extends Thread {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mThisSocket;
    private Activity mBleConnectActivity;
    private String mAdress;
    private boolean isConnecting;

    public BleSocketThread(BluetoothAdapter bluetoothAdapter, BluetoothSocket thisSocket, Activity bleConnectActivity, String adress) {
        mBluetoothAdapter = bluetoothAdapter;
        mThisSocket = thisSocket;
        mBleConnectActivity = bleConnectActivity;
        mAdress = adress;
        isConnecting = true;
    }

    public void run() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        StyledDialog.init(mBleConnectActivity.getApplicationContext());
        MyActyManager.getInstance().setCurrentActivity(mBleConnectActivity);
        final Dialog[] dialog = {null};
        DataInputStream dis = null;
        try {
            mThisSocket.connect();
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            mBleConnectActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog[0] =   StyledDialog.buildProgress("正在等待对方回应...",false).show();
                }
            });
            while (isConnecting) {
                StyledDialog.dismissLoading();
                dis = new DataInputStream(mThisSocket.getInputStream());
                String result = dis.readUTF();
                if ("accept".equals(result)) {
                    dialog[0].dismiss();
                    SocketManager.addBleSocketHm(mAdress, mThisSocket);
                    Intent intent = new Intent(mBleConnectActivity, MainActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putInt("choose",4);
                    bundle.putString("address", mAdress);
                    bundle.putBoolean("isStart", true);
                    intent.putExtras(bundle);
                    mBleConnectActivity.startActivity(intent);
                    break;
                } else {
                    mBleConnectActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mBleConnectActivity, "对方不接受挑战!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cancel() {
        isConnecting = false;
        try {
            if (mThisSocket != null) {
                mThisSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
