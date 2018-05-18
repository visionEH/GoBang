package com.example.visioneh.gobang.socket;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.visioneh.gobang.ConfigData;
import com.example.visioneh.gobang.MainActivity;
import com.hss01248.dialog.MyActyManager;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Time:2017.4.5 13:44
 * Created By:ThatNight
 */

public class BleServerSocketThread extends Thread {

    private BluetoothServerSocket mServerSocket;
    private BluetoothSocket mSocket;
    private String mName, mAdress;
    private Activity mBleConnectActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isAccept = false;
    private boolean isConnecting = true;

    public BleServerSocketThread(BluetoothSocket socket, String name, String adress, Activity bleConnectActivity, BluetoothAdapter bluetoothAdapter, boolean isAccept) {
        mSocket = socket;
        mName = name;
        mAdress = adress;
        mBleConnectActivity = bleConnectActivity;
        mBluetoothAdapter = bluetoothAdapter;
        this.isAccept = isAccept;
        isConnecting = true;
    }

    public void run() {
        DataOutputStream dos = null;
        try {
            mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mName, ConfigData.UUID);
            while (isConnecting) {
                isAccept = false;
                mSocket = mServerSocket.accept();
                if(mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBleConnectActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StyledDialog.init(mBleConnectActivity.getApplicationContext());
                        MyActyManager.getInstance().setCurrentActivity(mBleConnectActivity);


                        StyledDialog.buildMdAlert("提醒", "是否接受挑战?", new MyDialogListener() {
                            @Override
                            public void onFirst() {
                                Toast.makeText(mBleConnectActivity, "连接成功!", Toast.LENGTH_SHORT).show();
                                isAccept = true;
                            }

                            @Override
                            public void onSecond() {
                                isAccept = false;
                            }
                        }).show();
                    }
                });
                while (true) {
                    if (isAccept) {
                        String result = "accept";
                        dos = new DataOutputStream(mSocket.getOutputStream());
                        dos.writeUTF(result);
                        SocketManager.addBleSocketHm(mAdress, mSocket);
                        Intent intent = new Intent(mBleConnectActivity, MainActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putInt("choose",4);
                        bundle.putString("address", mAdress);
                        bundle.putBoolean("isStart", false);
                        intent.putExtras(bundle);
                        mBleConnectActivity.startActivity(intent);
                        break;
                    } else {

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cancel() {
        isConnecting = false;
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
