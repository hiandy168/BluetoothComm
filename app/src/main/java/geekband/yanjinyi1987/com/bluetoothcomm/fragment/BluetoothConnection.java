package geekband.yanjinyi1987.com.bluetoothcomm.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import geekband.yanjinyi1987.com.bluetoothcomm.R;

/**
 * Created by lexkde on 16-10-22.
 */

public class BluetoothConnection extends DialogFragment implements View.OnClickListener {

    private Button mEnableBtButton;
    private ListView mPariedBtList;
    private ListView mDiscvoeredList;

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(false); //保持在最前
        int style = DialogFragment.STYLE_NORMAL;
        int theme = android.R.style.Theme_DeviceDefault_Light_DarkActionBar;
        setStyle(style,theme);
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
        mEnableBtButton = (Button) v.findViewById(R.id.bt_enable_button);
        mPariedBtList = (ListView) v.findViewById(R.id.bt_paried_list);
        mDiscvoeredList = (ListView) v.findViewById(R.id.bt_discovered_list);
    }

    private void initListViews() {

    }
}
class BTDevice {
    String deviceType;
    String deviceName;
    boolean devicePaired;
}
class PariedBtAdapter extends ArrayAdapter<BTDevice> {
    int resourceId;
    Context context;
    List<BTDevice> btDevices;
    public PariedBtAdapter(Context context, int resource, int textViewResourceId, List<BTDevice> objects) {
        super(context, resource, textViewResourceId, objects);
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
            parentViewHolder.infoImage = (ImageView) view.findViewById(R.id.bt_type_image);
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
            parentViewHolder.infoImage.setImageResource(R.drawable.btInfo);
            parentViewHolder.infoImage.setVisibility(View.VISIBLE);
        }
        else {
            parentViewHolder.infoImage.setVisibility(View.INVISIBLE);
        }
        parentViewHolder.typeImage.setImageResource(R.drawable.btTypeImage);
        parentViewHolder.name.setText(btDevice.deviceName);
        return super.getView(position, convertView, parent);
    }

    class ParentViewHolder {
        ImageView typeImage;
        TextView name;
        ImageView infoImage;
    }
}
