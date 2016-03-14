package araba.akilli.saurobot.sauroakllaraba;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Main activity.
 *
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class DeviceActivity extends Activity {
	private Button mActivateBtn;
	private Button mScanBtn;

	//private ProgressDialog mProgressDlg;

	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

	private BluetoothAdapter mBluetoothAdapter;

	private ListView mListView;
	private DeviceListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_device);
		StaticFunctions.onCreateSetups(this);
		mActivateBtn 		= (Button) findViewById(R.id.btn_enable);
		mScanBtn 			= (Button) findViewById(R.id.btn_scan);

		mBluetoothAdapter	= BluetoothAdapter.getDefaultAdapter();

		/*mProgressDlg 		= new ProgressDialog(this);

		mProgressDlg.setMessage("Cihazlar Aranıyor...");
		mProgressDlg.setCancelable(false);
		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "İptal", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();

		        mBluetoothAdapter.cancelDiscovery();
		    }
		});
*/
		if (mBluetoothAdapter == null) {
			showUnsupported();
		} else {
			// eski eşleşilmişler ekleniyyor
			/*
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices == null || pairedDevices.size() == 0) {
				showToast("Eşleştirilemedi");
			} else {

				mDeviceList.addAll(pairedDevices);
			}
			*/
			mListView		= (ListView) findViewById(R.id.lv_devices);

			mAdapter		= new DeviceListAdapter(this);

			mAdapter.setData(mDeviceList);
			mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
				@Override
				public void onPairButtonClick(int position) {
					BluetoothDevice device = mDeviceList.get(position);

					if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
						unpairDevice(device);
					} else {
						showToast("Eşleştiriliyor...");
						pairDevice(device);
					}
				}
			});

			mListView.setAdapter(mAdapter);

			/*mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					showToast("selam");
				}
			});*/
			registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

			mScanBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (mBluetoothAdapter.isDiscovering()) {
						mBluetoothAdapter.cancelDiscovery();
					} else {
						mBluetoothAdapter.startDiscovery();
					}
				}
			});

			mActivateBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mBluetoothAdapter.isEnabled()) {
						mBluetoothAdapter.disable();

						showDisabled();
					} else {
						Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

					    startActivityForResult(intent, 1000);
					}
				}
			});

			if (mBluetoothAdapter.isEnabled()) {
				showEnabled();
			} else {
				showDisabled();
			}
		}

		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, filter);
	}

	private void pairDevice(BluetoothDevice device) {
		try {
			Method method = device.getClass().getMethod("createBond", (Class[]) null);
			method.invoke(device, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void unpairDevice(BluetoothDevice device) {
		try {
			Method method = device.getClass().getMethod("removeBond", (Class[]) null);
			method.invoke(device, (Object[]) null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
				final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

				if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
					showToast("Eşleştirildi");
				} else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
					showToast("Eşleştirilemedi");
				}

				mAdapter.notifyDataSetChanged();
			}
		}
	};
	@Override
	public void onPause() {
		super.onPause();
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
		}
	}
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);

		super.onDestroy();
	}

	private void showEnabled() {

		mActivateBtn.setTextColor(Color.BLUE);
		mActivateBtn.setText("Bluetooth Devre Dışı Bırak");
		mActivateBtn.setEnabled(true);

		mScanBtn.setEnabled(true);


	}

	private void showDisabled() {

		mActivateBtn.setTextColor(Color.RED);
		mActivateBtn.setText("Bluetooth Etkinleştir");
		mActivateBtn.setEnabled(true);

		mScanBtn.setEnabled(false);
	}

	private void showUnsupported() {
		mActivateBtn.setText("Unsupported");
		mActivateBtn.setEnabled(false);

	}

	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();

	        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

	        	if (state == BluetoothAdapter.STATE_ON) {
	        		showToast("Bluetooth Aktifleştirildi.");
	        		showEnabled();
	        	 }
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				//mProgressDlg.show();
				mScanBtn.setText("Taramayı Iptal Et");
				showToast("Cihazlar Aranıyor...");
	        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	        	//mProgressDlg.dismiss();
				mScanBtn.setText("Cihazları Tara");
				showToast("Cihaz Arama Tamamlandı.");
	        }
			else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				for (BluetoothDevice deviceItem: mDeviceList)
				{
					if (deviceItem.getAddress().equals(device.getAddress())) {
						// eski cihaz siliniyor yeni cihaz ekleniyorki durum değişikliği gözlenebilsin.
						mDeviceList.remove(deviceItem);
						showToast("Bulunan Cihaz:" + device.getName());
						break;
					}
				}
				mDeviceList.add(device);
				mAdapter.setData(mDeviceList);
				mAdapter.notifyDataSetChanged();

	        }
	    }
	};

}