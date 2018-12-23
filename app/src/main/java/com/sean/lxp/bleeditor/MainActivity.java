package com.sean.lxp.bleeditor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sean.lxp.bleeditor.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    /**
     * 设备列表 和 适配器
     */
    private RecyclerView mDeviceListView;
    private Button btn_scan;
    private DeviceAdapter mDeviceAdapter;

    private List<BluetoothDevice> mDeviceList=new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //判断手机蓝牙支持，是则打开蓝牙
        if (checkBleSupport()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                openBleAdapter();
            }
        } else {
            Toast.makeText(MainActivity.this, "手机不支持蓝牙Ble！", Toast.LENGTH_SHORT).show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.V(TAG,"onResume()");
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,intentFilter);
        startScanBleDevice();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.V(TAG,"onPause()");
        cancleScanBleDevice();
        unregisterReceiver(mReceiver);
    }

    /**
     * 开始扫描蓝牙设备
     */
    private void startScanBleDevice(){
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * 停止扫描蓝牙设备
     */

    private void cancleScanBleDevice(){
        mBluetoothAdapter.cancelDiscovery();
    }

    //一般在程序开始的时候先要判断手机是否支持BLE
    private boolean checkBleSupport() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 打开蓝牙
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openBleAdapter() {
        //通过系统服务获取蓝牙管理者
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //获取蓝牙适配器
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            System.out.println("蓝牙没有开启");
            //请求开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    /**
     * 初始化组件
     */
    private void initView() {
        mDeviceListView = findViewById(R.id.device_list);
        btn_scan=findViewById(R.id.btn_refresh);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter!=null){
                    if(mBluetoothAdapter.isDiscovering()){
                        cancleScanBleDevice();
                    }else {
                        startScanBleDevice();
                    }
                }
            }
        });
        //初始化数据适配器
        mDeviceAdapter = new DeviceAdapter(MainActivity.this, mDeviceList);
        mDeviceAdapter.setOnClickListener(mClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mDeviceListView.setLayoutManager(linearLayoutManager);
        //设置数据适配器
        mDeviceListView.setAdapter(mDeviceAdapter);
    }

    // 广播接收发现蓝牙设备
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtil.V(TAG, "开始扫描...");
                btn_scan.setText(getString(R.string.scanning));
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    // 添加到ListView的Adapter。
                   if(!mDeviceList.contains(device)){
                       mDeviceList.add(device);
                       mDeviceAdapter.notifyDataSetChanged();
                   }
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtil.V(TAG, "扫描结束.");
                btn_scan.setText(getString(R.string.start_scan));
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LogUtil.V(TAG, "读写权限获取成功！");
        }
    }

    DeviceAdapter.OnClickListener mClickListener=new DeviceAdapter.OnClickListener() {
        @Override
        public void OnClick(BluetoothDevice device) {
            Intent intent=new Intent(MainActivity.this,DeviceInfoActivity.class);
            Bundle bundle=new Bundle();
            bundle.putParcelable(ConstData.BLUE_DEVICE,device);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };
}
