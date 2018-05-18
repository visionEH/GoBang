package com.example.visioneh.gobang;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.visioneh.gobang.socket.BleServerSocketThread;
import com.example.visioneh.gobang.socket.BleSocketThread;
import com.hss01248.dialog.MyActyManager;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private final static int REQUEST_BLUE = 123;
    private BluetoothDevicesAdapter mAdapter;
    private BluetoothReceiver mReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private String mName, mAdress;
    private List<BluetoothReceiver.Bluetooth> mBluetooths;
    private List<BluetoothDevice> mDevices;
    private BleServerSocketThread mBleServerSocketThread;
    private BleSocketThread mBleSocketThread;
    private BluetoothSocket mSocket;
    private BluetoothSocket mThisSocket;
    private BluetoothDevice mThisDevice;
    private boolean isAccept = false;


    private Button mBtnSearch;
    private ListView mLvBluetooth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        getSupportActionBar().hide();
        mLvBluetooth=(ListView)findViewById(R.id.lv_bluetooth);
        mBtnSearch=(Button)findViewById(R.id.btn_search);

        StyledDialog.init(getApplicationContext());
        MyActyManager.getInstance().setCurrentActivity(this);

        init();

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findBluetooth();
            }
        });

        mLvBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectBluetooth(position);
            }
        });

        initSocket();
    }


    private void init() {
        mBluetooths = new ArrayList<>();
        mDevices = new ArrayList<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent openIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            openIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500); //设置可见性的时间500s, 0为一直可见
            startActivity(openIntent);
        }
        mName = mBluetoothAdapter.getName();
        mAdress = mBluetoothAdapter.getAddress();

        mAdapter = new BluetoothDevicesAdapter(mBluetooths, this);
        mLvBluetooth.setAdapter(mAdapter);

        //注册广播
        mReceiver = new BluetoothReceiver(mDevices, mBluetooths, new BluetoothReceiver.OnReceiverListener() {
            @Override
            public void setBluetoothList(List<BluetoothReceiver.Bluetooth> bluetooths, List<BluetoothDevice> devices) {
                mBluetooths = bluetooths;
                mDevices = devices;
                mAdapter.setDevices(mBluetooths);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void showText() {
                Toast.makeText(BluetoothActivity.this, "搜索完成!", Toast.LENGTH_SHORT).show();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

    }
    //初始化Scocket
    private void initSocket() {
        mBleServerSocketThread = new BleServerSocketThread(mSocket, mName, mAdress, this, mBluetoothAdapter, isAccept);
        mBleServerSocketThread.start();
    }
    // 开始连接对方
    private void connectBluetooth(int position) {
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetooths.get(position).getAddress());
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Method method = BluetoothDevice.class.getMethod("createBond");
                method.invoke(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                mThisSocket = device.createRfcommSocketToServiceRecord(ConfigData.UUID);

                StyledDialog.buildIosAlert("发起挑战", "确定挑战玩家:" + mBluetooths.get(position).getName() + "吗？", new MyDialogListener() {
                    @Override
                    public void onFirst() {
                        mBleSocketThread = new BleSocketThread(mBluetoothAdapter, mThisSocket, BluetoothActivity.this, mAdress);
                        mBleSocketThread.start();
                    }

                    @Override
                    public void onSecond() {

                    }
                }).show();


            }
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BluetoothActivity.this, "连接失败!", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }
    private void findBluetooth() {
        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetooths.clear();
        mDevices.clear();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                if(!mDevices.contains(device)){
                    mBluetooths.add(new BluetoothReceiver.Bluetooth(device.getName(), device.getAddress()));
                    mDevices.add(device);
                }
            }
        }
        mAdapter.setDevices(mBluetooths);
        mAdapter.notifyDataSetChanged();
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLUE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_BLUE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if(mBleServerSocketThread!=null){
            mBleServerSocketThread.cancel();
        }
        if(mBleSocketThread!=null){
            mBleSocketThread.cancel();
        }
    }
    class BluetoothDevicesAdapter extends BaseAdapter {

        private List<BluetoothReceiver.Bluetooth> mBluetooths;
        private Context mContext;
        private LayoutInflater mLayoutInflater;


        public BluetoothDevicesAdapter(List<BluetoothReceiver.Bluetooth> bluetooths, Context context) {
            mBluetooths = bluetooths;
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mBluetooths.size();
        }

        @Override
        public Object getItem(int position) {
            return mBluetooths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = mLayoutInflater.inflate(R.layout.item_bluetooth, null);
                viewHolder.mName = (TextView) view.findViewById(R.id.tv_item_bluetooth_name);
                viewHolder.mAdress = (TextView) view.findViewById(R.id.tv_item_bluetooth_adress);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.mName.setText(mBluetooths.get(position).getName());
            viewHolder.mAdress.setText(mBluetooths.get(position).getAddress());
            return view;
        }

        public void setDevices(List<BluetoothReceiver.Bluetooth> bluetooths) {
            mBluetooths = bluetooths;
        }

        class ViewHolder {
            TextView mName;
            TextView mAdress;
        }
    }
}
