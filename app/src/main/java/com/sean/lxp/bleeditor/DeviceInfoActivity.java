package com.sean.lxp.bleeditor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class DeviceInfoActivity extends AppCompatActivity {
    public static final String TAG = DeviceInfoActivity.class.getSimpleName();
    public static final String UUID_SERVICE = "0000AE00-0000-1000-8000-00805F9B34FB";
    public static final String UUID_WRITE = "0000AE01-0000-1000-8000-00805F9B34FB";
    public static final String UUID_NOTIFY = "0000AE02-0000-1000-8000-00805F9B34FB";
    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothDevice mDevice;
    private EditText et_name, et_pwd;
    private Button btn_ok;
    private TextView tv_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info);
        initView();
        initDevieInfo();
    }

    private void initView() {
        et_name = findViewById(R.id.new_blue_name);
        et_pwd = findViewById(R.id.new_blue_pwd);
        btn_ok = findViewById(R.id.btn_ok);
        tv_name = findViewById(R.id.old_name);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String pwd = et_pwd.getText().toString();
                if (!name.equals("")) {
                    if(isASCII(name)){
                        String cmd_name = getCommandName(name);
                        writeToDevice(cmd_name);
                    }else{
                        Toast.makeText(DeviceInfoActivity.this,"修改的名称只能包含ASCII字符",Toast.LENGTH_SHORT).show();
                    }

                }
                if(!pwd.equals("")){
                    String cmd_pwd = getCommandPassword(pwd);
                    if(cmd_pwd==null){
                        Toast.makeText(DeviceInfoActivity.this,"密码长度固定为4",Toast.LENGTH_SHORT).show();
                    }else {
                        writeToDevice(cmd_pwd);
                    }
                }

            }
        });

    }

    /**
     * 初始化设备信息
     */
    private void initDevieInfo() {
        mDevice = getIntent().getParcelableExtra(ConstData.BLUE_DEVICE);
        tv_name.setText(mDevice.getName());
        mDevice.connectGatt(DeviceInfoActivity.this, false, new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Toast.makeText(DeviceInfoActivity.this, "设备连接成功", Toast.LENGTH_SHORT).show();
                    //连接成功开始发现服务
                    Log.v(TAG, "连接成功开始发现服务");
                    //初始化gatt
                    mGatt = gatt;
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Toast.makeText(DeviceInfoActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
                }

            }

            //发现服务的回调
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "onServicesDiscovered: 成功");
                    BluetoothGattService service = gatt.getService(UUID.fromString(UUID_SERVICE));
                    if(service!=null){
                        mWriteCharacteristic = service.getCharacteristic(UUID.fromString(UUID_WRITE));
                        mNotifyCharacteristic = service.getCharacteristic(UUID.fromString(UUID_NOTIFY));
                    }
                    else{
                        Toast.makeText(DeviceInfoActivity.this, "没有发现服务！", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            //写操作的回调
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.v(TAG,"onCharacteristicWrite");
                Toast.makeText(DeviceInfoActivity.this, "写入成功！", Toast.LENGTH_SHORT).show();
            }

            //数据返回的回调
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.i(TAG, "onCharacteristicChanged: 回调");
                byte[] bytes = characteristic.getValue();   //获取设备发来的数据
                Log.i(TAG, "value=" + byte2hex(bytes));   //byte2hex()是把字节数组转换成16进制的字符串，方便看
                Toast.makeText(DeviceInfoActivity.this, "value=" + byte2hex(bytes), Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * 向蓝牙设备写
     * @param content
     */
    private void writeToDevice(String content) {
        if (mWriteCharacteristic != null) {
            mWriteCharacteristic.setValue(content);
            mGatt.writeCharacteristic(mWriteCharacteristic);
        }else{
            Toast.makeText(DeviceInfoActivity.this, "无法写入，服务不存在!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * @param name
     * @return
     */
    private String getCommandName(String name) {
        if (name.length() > 20)
            return null;
        StringBuffer buffer = new StringBuffer();
        int len = name.length();
        buffer.append("AXX+");
        buffer.append(len + "+");
        buffer.append(name + "+OK");
        return buffer.toString();
    }

    private String getCommandPassword(String psw) {
        if (psw.length() != 4) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("AXX+04+");
        buffer.append(psw + "+OK");
        return buffer.toString();
    }

    /**
     * 字节转16进制字符串
     * @param bytes
     * @return
     */
    private String byte2hex(byte[] bytes){
        StringBuffer sb = new StringBuffer(bytes.length);
        String sTemp;
        for (int i = 0; i < bytes.length; i++) {
            sTemp = Integer.toHexString(0xFF & bytes[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 判断字符串是否为ASCII
     */
    private boolean isASCII(String str){
        return str.getBytes().length==str.length();
    }

    private void setNotification(BluetoothGatt mGatt, BluetoothGattCharacteristic mCharacteristic, boolean mEnable) {
        if (mCharacteristic == null) {
            return;
        }
//        //设置为Notifiy,并写入描述符
//        mGatt.setCharacteristicNotification(mCharacteristic, mEnable);
//        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        mGatt.writeDescriptor(descriptor);
    }

}
