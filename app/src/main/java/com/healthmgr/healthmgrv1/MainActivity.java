package com.healthmgr.healthmgrv1;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.healthmgr.healthmgrv1.bluetooth.BluetoothUtils;
import com.healthmgr.healthmgrv1.common.MyApplication;
import com.healthmgr.healthmgrv1.data.DataChangeListener;
import com.healthmgr.healthmgrv1.data.DataParse;
import com.healthmgr.healthmgrv1.utils.AudioUtils;
import com.healthmgr.healthmgrv1.utils.PreferencesService;
import com.healthmgr.healthmgrv1.utils.Utils;
import com.healthmgr.healthmgrv1.wave.Wave;
import com.healthmgr.healthmgrv1.wave.WaveParse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;



public class MainActivity extends Activity {
    //UI



    public static LinearLayout vBT;
    public static Button vCheck;
    public static LinearLayout vCFG;
    public static TextView vheartRate;
    public static TextView vheartRateState;
    public static TextView vcontactPerson;
    public static Wave mECGWaveDraw;
    public static SurfaceView vECGWave;
    public static  int panduan = 0;

    private Handler handler = new Handler();

    //bluetooth service
    public static BluetoothUtils mBluetoothUtils;
    public static Context mContext;
    private BluetoothAdapter mBluetoothAdapter;

    private TextView addre;
    private LocationManager locationManager;
    private String provider;
    private GeocodeSearch search;

    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;

    private PreferencesService service;
    private String phoneNumber;
    private  int smsSendCount = 3;

    Thread byteDealThread;

    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("heartRateState")) {
                final String heartRateState = intent.getStringExtra("heartRateState");
                vheartRateState.setText(heartRateState);

                if (!heartRateState.equals("心率正常")  ) {
                    if(panduan>0)
                        panduan --;
                    else {
                        panduan = 20;

                        StringBuffer smsTextStr = new StringBuffer();
                        smsTextStr = smsTextStr.append(addre.getText()).append(heartRateState);
                        final String smsStr = smsTextStr.toString();
                        if (!phoneNumber.equals(("")) && phoneNumber != null && smsSendCount > 0) {
                            smsSendCount--;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.sendSms(mContext, phoneNumber, smsStr);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utils.showCallPanel(mContext, phoneNumber);
                                        }
                                    }, 3 * 1000);
                                }
                            }, 5 * 1000);
                        }

                    }
                }

            }else{
                final String heartRateVal = String.valueOf(intent.getIntExtra("heartRateVal", 70));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (vheartRate != null) {
                                vheartRate.setText(heartRateVal);
                            }
                        } catch (Exception ex) {
                            Log.e("", "", ex);
                        }
                    }
                });
            }
        }
    };




    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mContext = this;
        addre = (TextView) findViewById(R.id.vaddress);
        vheartRate = (TextView) findViewById(R.id.vheartRate);
        vheartRateState = (TextView) findViewById(R.id.vheartRateState);


        initEcgWave();
        setContactPerson();
        initLocation();
        //	Utils.openGPSSettings(MainActivity.this);


        IntentFilter filter = new IntentFilter();
        filter.addAction("heartRateState");
        filter.addAction("heartRateVal");
        LocalBroadcastManager.getInstance(MyApplication.mContext).registerReceiver(mReceiver, filter);



        mBluetoothUtils = new BluetoothUtils();
        mBluetoothUtils.initOpenNativeBluetooth();
        initOpenNativeBluetooth();

        ((ImageButton) findViewById(R.id.vBT)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivityForResult(intent,11);
                //startActivity(intent);
            }
        });

        //此处还有一个开始检查按钮的处理
		/*	vCheck.setOnClickListener(this);*/

        ((ImageButton) findViewById(R.id.vCFG)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });

        try {
            AudioUtils.me().startPlay();//  处理好播音的准备工作
            AudioUtils.me().listenRealVoice();// 准备从协议解析的输出处 收数据   notiffyEcgWaveData(val);
            AudioUtils.me().startMain();    //  注释掉后，走单字节写入音轨的流程；不注释，走批量写入音轨的流程
//            AudioUtils.me().createSimulationDatas();// 该句从录音好的文件播放出来 上面一句是从蓝牙实时数据流播放出来
        } catch (Exception ex) {
            Log.e("", "", ex);
        }
    }

    private void initLocation(){
        String sha1Info = Utils.sHA1(getApplicationContext());// 本机密钥 所以需要每台计算机申请一个高德的密码
        Log.i("", "package sha1 info is " + sha1Info);
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(2000);
//        mLocationClient.setApiKey("3fd8d247a63da308934a8520f246d3fd");
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否强制刷新WIFI，默认为true，强制刷新。
        mLocationOption.setWifiActiveScan(false);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //设置定位回调监听
        mLocationClient.setLocationListener(new AMapLocationListener() {// 注册了一个回调函数
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {// 定位成功或者改变位置以后 回调这个函数
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //可在其中解析amapLocation获取相应内容。
                        final StringBuilder sb = new StringBuilder();
                        sb.append("\n 当前位置:")
                                .append(amapLocation.getAddress())
                                .append("\n Latitude:")
                                .append(amapLocation.getLatitude())
                                .append("\n LongiTude:")
                                .append(amapLocation.getLongitude());
                               /* . append("\n poiName:")
                                .append(amapLocation.getPoiName())
                                .append("\n Latitude:")
                                .append(amapLocation.getLatitude())
                                .append("\n LongiTude:")
                                .append(amapLocation.getLongitude());*/
                        Log.e("initLocation", "onLocationChanged: "+sb );
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (addre != null) {
                                        addre.setText(sb);// 这个文本框把拼起来的 定位信息显示出来
                                    }
                                } catch (Throwable th) {
                                    Log.e("", "", th);
                                }
                            }
                        });
                    }else {
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }
                }
            }
        });
        //启动定位
        mLocationClient.startLocation();//
    }


    private void initEcgWave(){// 初始化心电波形
        vECGWave = (SurfaceView)findViewById(R.id.vECGWave);
        WaveParse mECGWaveParas = new WaveParse();
        mECGWaveParas.bufferCounter = 5;
        mECGWaveParas.xStep = 2;
        mECGWaveDraw = new Wave(vECGWave,mECGWaveParas);
/**
 * 心电波形的显示
 */
        DataParse.getInstance().addDataChangeListener(DataParse.ECG_WAVE_DATA_LISTENER, new DataChangeListener() {

            private int idx = 0;

            private static final int INTERVAL   =   50;

            @Override
            public String getType() {
                return DataParse.ECG_WAVE_DATA_LISTENER;
            }

            @Override
            public void addData(final Integer val) {
                try {
                    idx++;
                    if (idx >= INTERVAL) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (mECGWaveDraw != null) {
                                        Log.i("ECG_WAVE", ">>>> val   " + val);
                                        mECGWaveDraw.add(val.intValue());  // 画图纵向的比例因子
                                    }
                                } catch (Throwable th) {
                                    Log.e("" , "", th);
                                }
                            }
                        });
                        idx = 0;
                    }
                } catch (Exception ex) {
                    Log.e("", "", ex);
                }
            }
        });

    }




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

    private void setContactPerson() {
        service = new PreferencesService(MainActivity.this);
        //打开时读取保存的参数
        Map<String, String> params = service.getPreferences();
        TextView contactPerson = (TextView) findViewById(R.id.vcontactPerson);
        TextView contactPhone = (TextView) findViewById(R.id.vcontactPhone);
        contactPerson.setText(params.get("cfgConPerson"));
        phoneNumber = params.get("cfgPhone");
        contactPhone.setText(phoneNumber);
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        //MainActivity.mBluetoothUtils.UnRegisterRecevier();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationOption = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==11&&resultCode==RESULT_OK) {
            Log.d("MainActivity", "返回住页面" );
        }
    }


}
