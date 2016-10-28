package geekband.yanjinyi1987.com.bluetoothcomm;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import geekband.yanjinyi1987.com.bluetoothcomm.fragment.BluetoothConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private BluetoothAdapter mBluetoothAdapter;
    private boolean noBluetooth;
    private boolean bluetoothDisable;
    private static final int REQUEST_ENABLE_BT=1;
    private static ArrayList<String> mConnectedBTDevices = new ArrayList<>();

    //处理蓝牙接收器的状态变化
    /**
     * https://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html#STATE_TURNING_ON
     * int STATE_OFF : Indicates the local Bluetooth adapter is off.
     * int STATE_ON  : Indicates the local Bluetooth adapter is on, and ready for use.
     * int STATE_TURNING_OFF : Indicates the local Bluetooth adapter is turning off.
     *                         Local clients should immediately attempt graceful disconnection of any remote links.
     *int STATE_TURNING_ON: Indicates the local Bluetooth adapter is turning on.
     *                      However local clients should wait for STATE_ON before attempting to use the adapter.
     */
    BroadcastReceiver mBroadcstReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
                int state_previous = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE,-1);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mConnectedBTDevices.clear(); //清除连接列表
                        //关闭读写通道？
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if(noBluetooth==false) {
                            callBtConnectionDialog();
                        }
                        else {
                            try {
                                throw(new Exception("程序不可能运行到这里"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };
    private Button mBTConnectionButton;
    private EditText mAtCommandText;
    private Button mSendAtCommandButton;
    private EditText mReceivedSPPDataText;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode==RESULT_OK) {
                    //蓝牙设备已经被使能了，then do the job，paired or discovery and then connecting，看sample我们
                    //需要做一个listview来实现这一点。
                    /*
                    mBTConnectionButton.setEnabled(false);
                    //先打开系统自带的蓝牙设置界面来配对和连接蓝牙，有时间再自己写一个DialogFragment的例子
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    //但是打开的这个Activity好像只有显示配对和查找配对设备的功能，没有连接的功能哦。
                    startActivity(settingsIntent);
                    */
                }
                else if(resultCode == RESULT_CANCELED) {
                    //蓝牙设备没有被使能
                }
                else {
                    //不可能到这里来
                    Toast.makeText(this,"Error！",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    void callBtConnectionDialog() {
        BluetoothConnection btDialog = BluetoothConnection.newInstance(mBluetoothAdapter,mConnectedBTDevices);
        btDialog.show(getFragmentManager(), "蓝牙设置");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        //添加IntentFilter来监听Bluetooth的状态变化
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //注册接受器
        registerReceiver(mBroadcstReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcstReceiver);
    }

    /*
        Bluetooth init
         */
    void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            //设备不支持蓝牙
            Toast.makeText(this,"您的设备不支持蓝牙",Toast.LENGTH_LONG).show();
            noBluetooth = true;
            return;
        }
        //蓝牙设备是存在的
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); //对应onActivityResult
        }
        else {
            callBtConnectionDialog();
        }
    }
    /*
    关于白天黑夜 ，春夏秋冬什么的，好像是可以通过手机上的时间或者网络获取的
    需要选择的只是植物的种类
    在传递较多数据时，应该对这些数据做校验以确保传输正确。
     */
    void initViews()
    {
        mBTConnectionButton = (Button) findViewById(R.id.connect_bt_device);
        mAtCommandText = (EditText) findViewById(R.id.AT_command_text);
        mReceivedSPPDataText = (EditText) findViewById(R.id.received_SPP_data_text);
        mSendAtCommandButton = (Button) findViewById(R.id.send_AT_command);

        mBTConnectionButton.setOnClickListener(this);
        mSendAtCommandButton.setOnClickListener(this);
    }
    //接受传回的结果肯定是异步的哦！
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.connect_bt_device:
                initBluetooth();
                break;
            case R.id.send_AT_command:
                String at_command = mAtCommandText.getText().toString();
                if(at_command!=null && at_command.length()>0) {
                    //发送命令
                }
                break;
            default:
                break;
        }

    }
}
