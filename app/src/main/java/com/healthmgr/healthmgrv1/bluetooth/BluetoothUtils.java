package com.healthmgr.healthmgrv1.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BluetoothUtils
{
	private static String Tag   = "BluetoothUtils";

	private Context   mContext;
	private BluetoothAdapter     mBluetoothAdapter;
	private Handler   mHandler;
	private BluetoothChatService mBluetoothChatService;

	public BluetoothUtils(){
	}
	public BluetoothUtils(Context context, Handler handler) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mHandler = handler;
	}

	/**
	 * Init
	 */
	public void initOpenNativeBluetooth()
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter!=null){
			if(!mBluetoothAdapter.isEnabled())
			{
				mBluetoothAdapter.enable();
			}
		}
	}

}


















