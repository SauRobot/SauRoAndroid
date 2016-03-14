package Adapter;

import java.util.List;
import java.util.Set;

import araba.akilli.saurobot.sauroakllaraba.R;
import araba.akilli.saurobot.sauroakllaraba.R.id;
import araba.akilli.saurobot.sauroakllaraba.R.layout;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceAdapter extends BaseAdapter {
    LayoutInflater mInflater;
    private List<String> deviceList;
    private Context context;

    public DeviceAdapter (Context con, List<String> pairedDevices) {

        context = con;
        deviceList = pairedDevices;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public static class ViewHolder{

        public TextView txtDeviceName;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder=new ViewHolder();
        String device =  deviceList.get(position);
        View vi=convertView;
        if(convertView==null){
            try{
                vi = mInflater.inflate(R.layout.device_list, null);
                holder.txtDeviceName=(TextView)vi.findViewById(R.id.txtDeviceName);
                vi.setTag(holder);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else
            holder=(ViewHolder)convertView.getTag();
        holder.txtDeviceName.setText(device);

        return vi;
    }
}
