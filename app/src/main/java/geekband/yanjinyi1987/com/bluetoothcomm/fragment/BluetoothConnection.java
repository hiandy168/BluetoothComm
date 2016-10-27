package geekband.yanjinyi1987.com.bluetoothcomm.fragment;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import geekband.yanjinyi1987.com.bluetoothcomm.R;

/**
 * Created by lexkde on 16-10-22.
 */

public class BluetoothConnection extends DialogFragment implements View.OnClickListener {

    private Button mRefreshBtButton,mCancleButton;
    private TextView mDiscoveredBtText;
    private ListView mBtPairedListView;
    private ListView mBtDiscoveredListView;
    private ArrayList<BTDevice> mBtPairedDevices;
    private PairedBtAdapter mPairedBtAdapter;
    private ArrayList<BTDevice> mBtDiscoveredDevices;
    private PairedBtAdapter mDiscoveredBtAdapter;
    private static BluetoothAdapter mBluetoothAdapter;
    public final static String NAME = "JGCX";
    public final static UUID MY_UUID = UUID.randomUUID();

    Handler mBluetoothConnectionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1:
                    //连接不成功，跳不出绑定的界面，即弹出要求输入的密码框啥的
                    Toast.makeText(getActivity(),"已连接到设备",Toast.LENGTH_LONG).show();
                    //关闭ProgressDialog
                    mProgressDialog.dismiss();
                    //获取Socket并传递给MainActivity
                    //关闭自己
                    dismiss();
                    break;
                case 0:
                    Toast.makeText(getActivity(),"连接到设备失败",Toast.LENGTH_LONG).show();
                    //关闭ProgressDialog
                    mProgressDialog.dismiss();
                    //重新开启startRecovery
                    mDiscoveredBtAdapter.clear();
                    discoverBtDevices();
                    mRefreshBtButton.setEnabled(false);
                    break;
                default:
                    break;
            }
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //when discovery finds a device
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Add the name and address to an array adapter to show in a ListView
                mDiscoveredBtAdapter.add(new BTDevice(device,device.getType(),device.getName(),null,false));
            }
        }
    };
    private boolean discovered=false;
    public static int timeout = 120; //搜索超时120秒
    public static String frameText0 = "发现设备";
    public static String frameText1 = "发现设备.";
    public static String frameText2 = "发现设备..";
    public static String frameText3 = "发现设备...";
    public static String frameText4 = "发现设备....";
    public static int flipflop = 0;
    public static int count = 5;

    Handler animatorHandle =  new Handler();
    Handler timeoutHandle = new Handler();
    private BluetoothSocket mBluetoothSocket;
    private ProgressDialog mProgressDialog;

    public static BluetoothConnection newInstance(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
        return new BluetoothConnection();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(false); //保持在最前
        int style = DialogFragment.STYLE_NORMAL;
        int theme = android.R.style.Theme_DeviceDefault_Light_Dialog;
        setStyle(style,theme);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.getActivity().registerReceiver(mReceiver,filter); //这里不需要进行改动，Fragment不能接收信号，但这只是注册一下
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("蓝牙设置");
        View v = inflater.inflate(R.layout.bluetooth_connection_dialog,container,false);
        initViews(v);
        return v;
    }

    private void initViews(View v) {
        //mEnableBtButton = (Button) v.findViewById(R.id.bt_enable_button);
        mRefreshBtButton = (Button) v.findViewById(R.id.refresh_action_button);
        mCancleButton = (Button) v.findViewById(R.id.cancle_action_button);
        mDiscoveredBtText = (TextView) v.findViewById(R.id.bt_discovered_title);

        mRefreshBtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //刷新列表
                if(discovered==true) {
                    mDiscoveredBtAdapter.clear();
                    discoverBtDevices();
                }
            }
        });

        mCancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter!=null) {
                    if(discovered==true) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                }
                //退出界面
                dismiss();
            }
        });
        initListViews(v);
    }



    private void initListViews(View v) {
        mBtPairedListView = (ListView) v.findViewById(R.id.bt_paried_list);
        mBtDiscoveredListView = (ListView) v.findViewById(R.id.bt_discovered_list);
        mBtPairedDevices = new ArrayList<>();
        mPairedBtAdapter = new PairedBtAdapter(this.getActivity(),
                R.layout.bt_device_list,
                mBtPairedDevices);

        mBtDiscoveredDevices = new ArrayList<>();
        mDiscoveredBtAdapter = new PairedBtAdapter(this.getActivity(),
                R.layout.bt_device_list,
                mBtDiscoveredDevices);

        mBtPairedListView.setAdapter(mPairedBtAdapter);
        mBtDiscoveredListView.setAdapter(mDiscoveredBtAdapter);

        mBtPairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //connectBtClient(((BTDevice)mBtDiscoveredListView.getItemAtPosition(position)).mBtDevice);
                BluetoothDevice mmBtDevice = mPairedBtAdapter.getItem(position).mBtDevice;
                ConnectThread btConnectThread = new ConnectThread(mmBtDevice,mBluetoothConnectionHandler);
                btConnectThread.start(); //线程开始运行
                //展示ProgressDialog
                openProgressDialog(mmBtDevice.getName());
            }
        });

        mBtDiscoveredListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //connectBtClient(((BTDevice)mBtDiscoveredListView.getItemAtPosition(position)).mBtDevice);
                BluetoothDevice mmBtDevice = mDiscoveredBtAdapter.getItem(position).mBtDevice;
                ConnectThread btConnectThread = new ConnectThread(mmBtDevice,mBluetoothConnectionHandler);
                btConnectThread.start(); //线程开始运行
                //展示ProgressDialog
                openProgressDialog(mmBtDevice.getName());
            }
        });

        queryPairedBtDevices(mPairedBtAdapter);
        discoverBtDevices(); //异步程序
    }

    //当连接完成之后就关闭这个ProgressDialog与DialogFragment
    void openProgressDialog(String bt_device_name) {
        Log.i("BluetoothConnection","ProgressDialog打开了");
        mProgressDialog = new ProgressDialog(this.getActivity());
        mProgressDialog.setTitle("连接到设备"+bt_device_name+"中....");
        mProgressDialog.setMessage("请稍后！");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false); //保持在最前
        mProgressDialog.show();
    }

    void queryPairedBtDevices(PairedBtAdapter tempPairedBtAdapter) {
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //这里应该使用MainActivity传递进来的数据
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                tempPairedBtAdapter.add(new BTDevice(device,device.getType(),device.getName(),null,false));
            }
        }
    }
    //设置搜寻动画，当搜索超时后，停止搜索，并使能refresh button
    void discoverBtDevices() {
        if(!mBluetoothAdapter.startDiscovery()) {
            Toast.makeText(this.getActivity(),"发现设备失败",Toast.LENGTH_LONG).show();
        }
        else {
            discovered =true;
            mRefreshBtButton.setEnabled(false); //程序自动搜索咯，不要自己按refresh

            final Runnable discoverBtDeviceThread = new Runnable() {
                @Override
                public void run() {
                    switch(flipflop%count) {
                        case 0:
                            mDiscoveredBtText.setText(frameText0);
                            break;
                        case 1:
                            mDiscoveredBtText.setText(frameText1);
                            break;
                        case 2:
                            mDiscoveredBtText.setText(frameText2);
                            break;
                        case 3:
                            mDiscoveredBtText.setText(frameText3);
                            break;
                        case 4:
                            mDiscoveredBtText.setText(frameText4);
                            break;
                        default:
                            break;
                    }
                    flipflop++;
                    animatorHandle.postDelayed(this,500);
                }
            };

            animatorHandle.postDelayed(discoverBtDeviceThread,500);

            timeoutHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animatorHandle.removeCallbacks(discoverBtDeviceThread);
                    mBluetoothAdapter.cancelDiscovery();
                    mRefreshBtButton.setEnabled(true);
                }
            },120*1000);
        }
    }

    //这个函数必须是阻塞的，因为连不上对应的设备，这个APP并没有鸟用。
    //点击之后，出现一个进度条并且其余设备不能被继续点击。如果连接设备成功，那么应该自动的退出这个dialog并展示Toast
    //将获得的mBluetoothSocket传递到MainActivity来进行后续I/O操作，因为控制逻辑都在那里。
    private boolean connectBtClient(BluetoothDevice mRemoteBtDevice) { //我们需要手动链接到对应的bluetooth device上所以应该使用connecting as a client

        try {
            mBluetoothSocket = mRemoteBtDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBluetoothAdapter.cancelDiscovery(); //这个时候必然是已经找到了对应的设备才会调用这个函数的

        try {
            mBluetoothSocket.connect();
        } catch (IOException e) {
            //连接不上这个设备，那么关闭socket并退出函数
            try {
                mBluetoothSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }

        //设备已经链接上了，那么可以开始传递数据了，但这是在主界面中实现的功能，所以这里这个函数必须向MainActivity传递一个什么东东。
        return true;
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private Handler mBluetoothConnectionHandler;
        public ConnectThread(BluetoothDevice device,Handler mBluetoothConnectionHandler) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            this.mBluetoothConnectionHandler = mBluetoothConnectionHandler;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(BluetoothConnection.MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                mBluetoothConnectionHandler.sendEmptyMessage(0); //连接失败
                return;
            }

            //将mmSocket传递给MainActivity
            //manageConnectedSocket(mmSocket);
            mBluetoothConnectionHandler.sendEmptyMessage(1); //连接成功

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}



class BTDevice {
    int deviceType;
    String deviceName;
    UUID uuid;
    boolean devicePaired;
    BluetoothDevice mBtDevice;

    BTDevice(BluetoothDevice btDevice,int deviceType,String deviceName,UUID uuid) {
        mBtDevice = btDevice;
        this.deviceType=deviceType;
        this.deviceName=deviceName;
        this.uuid=uuid;
        devicePaired=true;
    }

    BTDevice(BluetoothDevice btDevice,int deviceType,String deviceName,UUID uuid,boolean devicePaired) {
        mBtDevice = btDevice;
        this.deviceType=deviceType;
        this.deviceName=deviceName;
        this.uuid=uuid;
        this.devicePaired=false;
    }
}

class PairedBtAdapter extends ArrayAdapter<BTDevice> {
    int resourceId;
    Context context;
    List<BTDevice> btDevices;
    public PairedBtAdapter(Context context, int resource, List<BTDevice> objects) {
        super(context, resource, objects);
        resourceId = resource;
        this.context = context;
        btDevices = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BTDevice btDevice = getItem(position);
        View view;
        ParentViewHolder parentViewHolder;
        if(convertView==null) {
            view = LayoutInflater.from(context).inflate(resourceId,null);
            parentViewHolder = new ParentViewHolder();
            parentViewHolder.typeImage = (ImageView) view.findViewById(R.id.bt_type_image);
            parentViewHolder.name = (TextView) view.findViewById(R.id.bt_name);
            parentViewHolder.infoImage = (ImageButton) view.findViewById(R.id.bt_paried_info_button);
            view.setTag(parentViewHolder);
            if(btDevice.devicePaired==true) {
                parentViewHolder.infoImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }
        else {
            view = convertView;
            parentViewHolder = (ParentViewHolder) view.getTag();
        }
        if(btDevice.devicePaired==true) {
            parentViewHolder.infoImage.setImageResource(R.drawable.bt_info);
            parentViewHolder.infoImage.setVisibility(View.VISIBLE);
        }
        else {
            parentViewHolder.infoImage.setVisibility(View.INVISIBLE);
        }
        parentViewHolder.typeImage.setImageResource(R.drawable.bt_type_image);
        parentViewHolder.name.setText(btDevice.deviceName);
        return view;
    }

    class ParentViewHolder {
        ImageView typeImage;
        TextView name;
        ImageView infoImage;
    }
}
