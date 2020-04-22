package com.healthmgr.healthmgrv1.common;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.healthmgr.healthmgrv1.bluetooth.BLEService;


/**
 * Created by Administrator on 2016/11/22 0022.
 */

public class MyApplication extends Application {
    private Handler mHandler;
    public static Context mContext;
    private Runnable mTask=new Runnable() {
        @Override
        public void run() {
            startService(new Intent(mContext,BLEService.class));
            mHandler.postDelayed(this,5000);
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler=  new Handler();
        mContext=getApplicationContext();
        mHandler.postDelayed(mTask,5000);

    }
}
