package geekband.yanjinyi1987.com.bluetoothcomm;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean noBluetooth;
    private boolean bluetoothDisable;
    private static final int REQUEST_ENABLE_BT=1;

    BroadcastReceiver mBroadcstReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                break;
            default:
                break;
        }
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
        //蓝牙设备已经被使能了，then do the job，paired or discovery and then connecting，看sample我们
        //需要做一个listview来实现这一点。

    }
    /*
    关于白天黑夜 ，春夏秋冬什么的，好像是可以通过手机上的时间或者网络获取的
    需要选择的只是植物的种类
    在传递较多数据时，应该对这些数据做校验以确保传输正确。
     */
    void initViews()
    {

    }
}
