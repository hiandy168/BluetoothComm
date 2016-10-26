package geekband.yanjinyi1987.com.bluetoothcomm.fragment;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RunnableFuture;

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
    private BluetoothAdapter mBluetoothAdapter;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //when discovery finds a device
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Add the name and address to an array adapter to show in a ListView
                mDiscoveredBtAdapter.add(new BTDevice(device.getType(),device.getName(),false));
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

    public static BluetoothConnection newInstance() {
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
        this.getActivity().registerReceiver(mReceiver,filter); //这里需要进行改动，Fragment不能接收信号么？
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

            }
        });

        mBtDiscoveredListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        queryPairedBtDevices(mPairedBtAdapter);
        discoverBtDevices(); //异步程序
    }

    void queryPairedBtDevices(PairedBtAdapter tempPairedBtAdapter) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                tempPairedBtAdapter.add(new BTDevice(device.getType(),device.getName(),false));
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

}

class BTDevice {
    int deviceType;
    String deviceName;
    boolean devicePaired;

    BTDevice(int deviceType,String deviceName) {
        this.deviceType=deviceType;
        this.deviceName=deviceName;
        devicePaired=true;
    }

    BTDevice(int deviceType,String deviceName,boolean devicePaired) {
        this.deviceType=deviceType;
        this.deviceName=deviceName;
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
