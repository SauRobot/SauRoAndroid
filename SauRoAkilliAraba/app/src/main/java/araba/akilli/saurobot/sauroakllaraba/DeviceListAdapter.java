package araba.akilli.saurobot.sauroakllaraba;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * Device list adapter.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class DeviceListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;	
	private List<BluetoothDevice> mData;
	private OnPairButtonClickListener mListener;
   private Context context;
	BluetoothSocket mmSocket;
	public DeviceListAdapter(Context c) {

		context=c;
		mInflater = LayoutInflater.from(context);
    }
	
	public void setData(List<BluetoothDevice> data) {
		mData = data;
	}
	
	public void setListener(OnPairButtonClickListener listener) {
		mListener = listener;
	}
	
	public int getCount() {
		return (mData == null) ? 0 : mData.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {			
			convertView			=  mInflater.inflate(R.layout.list_item_device, null);
			
			holder 				= new ViewHolder();
			
			holder.nameTv		= (TextView) convertView.findViewById(R.id.tv_name);
			holder.addressTv 	= (TextView) convertView.findViewById(R.id.tv_address);
			holder.pairBtn		= (Button) convertView.findViewById(R.id.btn_pair);
			holder.btnConnect=(Button) convertView.findViewById(R.id.btnConnect);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final BluetoothDevice device	= mData.get(position);
		
		holder.nameTv.setText(device.getName());
		holder.addressTv.setText(device.getAddress());
		holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unut" : "Eşleş");
		holder.pairBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onPairButtonClick(position);
				}
			}
		});
		holder.btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(device.getBondState() != BluetoothDevice.BOND_BONDED)
				{
					Toast.makeText(context, "Eşleştiriniz.", Toast.LENGTH_SHORT).show();
				}
				else{
					if ( device != null) {
						if (connectBT(device) == null) {
							Toast.makeText(context, "Cihaza bağlanılamadı.", Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(context, "Cihaza bağlandı.", Toast.LENGTH_LONG).show();
							Intent intent=new Intent(context,MainActivity.class);
							intent.putExtra("device", device);

							context.startActivity(intent);
						}
					}
					else{
						Toast.makeText(context, "Cihaz Seçilemedi!", Toast.LENGTH_LONG).show();
					}
			}
			}
		});
        return convertView;
	}

	static class ViewHolder {
		TextView nameTv;
		TextView addressTv;
		TextView pairBtn;
		Button btnConnect;
	}
	
	public interface OnPairButtonClickListener {
		public abstract void onPairButtonClick(int position);
	}
	private  UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	public BluetoothSocket connectBT(BluetoothDevice mmDevice) {
		try {
			int curentVersion=android.os.Build.VERSION.SDK_INT;
			if (curentVersion>14){
				ParcelUuid uuids[] =mmDevice.getUuids();
				if(uuids!=null)
					MY_UUID = uuids[0].getUuid();
			}

			mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
			mmSocket.connect();
			MainActivity.mmSocket=mmSocket;
			return mmSocket;
		} catch (IOException e) {
			Log.d("BLUETOOTH_CLIENT", e.getMessage());
		}
		return null;
	}

}
