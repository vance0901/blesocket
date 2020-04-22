package com.healthmgr.healthmgrv1;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.healthmgr.healthmgrv1.bluetooth.BLEService;
import com.healthmgr.healthmgrv1.common.CommonViewHolder;
import com.healthmgr.healthmgrv1.utils.CONST;
import com.healthmgr.healthmgrv1.utils.SPutil;

import java.util.ArrayList;
import java.util.List;


public class BluetoothActivity extends Activity{
	private static ImageButton  ibBluetooth;
	private static LinearLayout   llBTSelect;
	private static ListView  lvBtDeviceList;
	private static Button btnResearch;
	private static ProgressBar pbSearch;
	private List<BluetoothDevice> deviceList;
	private List<String> devices;
	private BluetoothAdapter bluetoothAdapter;;



	private Context mContext;
	private List<BluetoothDevice> mDevices=new ArrayList<>();
	private BluetoothAdapter BleAdapter;
    private BaseAdapter mAdapter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
        mContext = this;
		initUI();
		initSearch();
	}

	private void initUI(){
		/*
		pbSearch  = (ProgressBar)    findViewById(R.id.pbSearch);
		btnResearch = (Button)findViewById(R.id.btnResearch);
		btnResearch.setOnClickListener(this);*/

		lvBtDeviceList = (ListView)findViewById(R.id.lvBtDeviceList);
        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
            return 	mDevices.size();
            }

            @Override
            public Object getItem(int position) {
                return mDevices.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CommonViewHolder holder = CommonViewHolder.getCVH(convertView, mContext, android.R.layout.simple_list_item_1);
                holder.getTv(android.R.id.text1).setText(mDevices.get(position).getName());
                return holder.convertView;
            }
        };
        lvBtDeviceList.setAdapter(mAdapter);
		lvBtDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				new   AlertDialog.Builder(mContext).setMessage("是否连接此设备?").setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String address = mDevices.get(position).getAddress();
						SPutil.putValue(CONST.DEVICE_ADDRESS,address);
						SPutil.putValue(CONST.DEVICE_IS_BOUND,true);
						startService(new Intent(mContext, BLEService.class));
						dialog.dismiss();


                        //  here
                        // 获取启动该Activity之前的Activity对应的Intent
                        Intent intent=BluetoothActivity.this.getIntent();
                        //设置当前Activity的结果码
                        BluetoothActivity.this.setResult(RESULT_OK, intent);
                        //关闭当前的Activity
                        BluetoothActivity.this.finish();
					}
				}).show();


            }

		});
	}

	private void initSearch() {
		BleAdapter = BluetoothAdapter.getDefaultAdapter();
		if (BleAdapter==null) {
			Toast.makeText(this,"没有蓝牙驱动",Toast.LENGTH_SHORT);
		}
		if (!BleAdapter.isEnabled()) {
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),200);
		}else if (BleAdapter.isEnabled()){
			//这里改了将startLeScan改为connectGatt
			connectGatt();
			//startLeScan();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==200&&resultCode==RESULT_OK) {
			//这里改了将startLeScan改为connectGatt
			connectGatt();
			//startLeScan();
		}
	}
    private BluetoothAdapter.LeScanCallback mCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			boolean isRepeated = checkDeviceIsRepeated(device);
			if (!isRepeated) {
				mDevices.add(device);
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	//这里改了将startLeScan改为connectGatt
	private void connectGatt() {

		boolean isStarted = BleAdapter.startLeScan(mCallback);
		if (isStarted){
			Toast.makeText(this,"蓝牙搜索开启",Toast.LENGTH_SHORT);

		}
	}

	private boolean checkDeviceIsRepeated(BluetoothDevice device) {
//		if (mDevices.contains(device)) {
//			return true;
//		}
		for (BluetoothDevice mDevice : mDevices) {
			if (mDevice.getAddress().equals(device.getAddress())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}

}
