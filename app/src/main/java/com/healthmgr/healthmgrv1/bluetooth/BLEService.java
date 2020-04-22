package com.healthmgr.healthmgrv1.bluetooth;

/**
 * Created by Administrator on 2016/11/22 0022.
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.healthmgr.healthmgrv1.MainActivity;
import com.healthmgr.healthmgrv1.data.DataParse;
import com.healthmgr.healthmgrv1.utils.CONST;
import com.healthmgr.healthmgrv1.utils.SPutil;
import com.healthmgr.healthmgrv1.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

//import static com.igsun.www.handsetmonitor.common.MyApplication.mContext;
public class BLEService extends Service {
    private static boolean isOverTime = true;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;
//  6e400001-b5a3-f393-e0a9-e50e24dcca9e

    private static final UUID UUID_HRMService = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID NOTIFY_HRMCharacteristic = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private static final UUID NOTIFY_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

//    private static final UUID UUID_HRMService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
//    private static final UUID NOTIFY_HRMCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
//
//    private static final UUID NOTIFY_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private long ScheduleTime;
    public static boolean isConnected = false;
    private List<byte[]> ecgData = new LinkedList<>();
    private List<byte[]> ecgCache = new LinkedList<>();

    public static List<Integer> ecgDa = new LinkedList<>();
    public static int heartRate = 75;
    public static String heartRateState = "正常";



    private static BluetoothGattCharacteristic HRM_WRITE;
    private static BluetoothGattCharacteristic HRM_NOTIFY;

    private Context mContext;
    private static BluetoothGatt mGATT;
    private Handler mHandler;
    private OnDataListener onDataListener;

    //初始化
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new Handler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        init();
    }

    private void init() {
        // 初始化配置,获取蓝牙模块和绑定蓝牙设备地址
        initConfig();
        initConnection();
    }

    private void initConfig() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    private void initConnection() {
        String deviceAddress = SPutil.getString(CONST.DEVICE_ADDRESS);
        // TODO: 2016/11/8 断线重连 有问题!!!!
        if (deviceAddress != null) {
            //      检测绑定的设备地址是否可用
            if (BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                if (mDevice == null) {
                    connectGatt(deviceAddress);
                } else {
                    if (mDevice.getAddress().equals(deviceAddress)) {
                        Log.d("initConnection", "是设置的设备地址");
                        if (mGATT != null) {
                            int connectionState = ((BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE)).getConnectionState
                                    (mDevice, BluetoothProfile.GATT);
                            Log.d("initConnection", "connectionState:" + connectionState);
                            if (connectionState != BluetoothProfile.STATE_CONNECTED && connectionState !=
                                    BluetoothProfile.STATE_CONNECTING) {
                                mGATT.connect();
                            } else {
                                Log.d("initConnection", "已经连接上,不进行操作");
                            }
                        } else {
                            connectGatt(deviceAddress);
                        }
                    } else {
                        Log.d("initConnection", "不是设置的设备地址");
                        if (mGATT != null) {
                            mGATT.close();
                            Log.d("initConnection", "清除链接");
                        }
                        connectGatt(deviceAddress);
                    }
                }
            } else {
//            UIUtil.toast("绑定蓝牙设备的MAC地址不可用", false);
                Log.d("BLEService", "绑定蓝牙设备的MAC地址不可用");
                stopSelf();
            }
        } else {
            //      检测是否绑定了设备
            Log.d("BLEService", "您还未绑定设备,请绑定设备后在进行此操作");
            stopSelf();
        }
    }

    private void connectGatt(String deviceAddress) {
        mDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        mDevice.connectGatt(mContext, false, mCallBack);
        Log.d("BLEService", "链接Gatt");
    }

    public void setOnDataListener(OnDataListener Listener) {
        this.onDataListener = Listener;
    }


    public interface OnDataListener {
        void onHrmReceived(int hrm);

        void onEcgReceived(byte[] ecg);

        void onPrReceived(int pr);
    }

    public boolean enableNotification(BluetoothGatt Gatt, boolean enable, BluetoothGattCharacteristic characteristic) {
        if (Gatt == null) {
            Log.d("BLEService", "Gatt==null");
            return false;
        }
        if (!Gatt.setCharacteristicNotification(characteristic, enable)) {
            Log.d("BLEService", "setCharacteristicNotification:false");
            return false;
        }
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(NOTIFY_DESC);
        if (clientConfig == null) {
            Log.d("BLEService", "clientConfig==null");
            return false;
        }
        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Log.d("BLEService", "Gatt.writeDescriptor(clientConfig):" + Gatt.writeDescriptor(clientConfig));
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            Log.d("BLEService", "Gatt.writeDescriptor(clientConfig):" + Gatt.writeDescriptor(clientConfig));
        }
        return Gatt.writeDescriptor(clientConfig);
    }

    //    与绑定activity进行链接通道
    @Override
    public IBinder onBind(Intent intent) {
        return new DeviceBinder();
    }

    public class DeviceBinder extends Binder {
        public Service getService() {
            return BLEService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();

        return super.onStartCommand(intent, flags, startId);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGATT != null) {
            mGATT.disconnect();
        }
    }

    private BluetoothGattCallback mCallBack = new BluetoothGattCallback() {

        //连接状态的监听
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // TODO: 2016/6/22  A GATT operation completed successfully
                Intent intent = new Intent();
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTED:
                        mGATT = gatt;
                        Log.d("BLEService", "设备连接了!");
                        gatt.discoverServices();
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        mGATT = null;

                        HRM_NOTIFY = null;
                        Log.d("BLEService", "设备正常断开了!");
                        gatt.close();
                        break;
                }
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            } else if (status == BluetoothGatt.GATT_FAILURE) {
                // TODO: 2016/6/22  A GATT operation failed, errors other than the above
//                UIUtil.toast("status == BluetoothGatt.GATT_FAILURE", false);
                Log.d("BLEService", "连接失败了!");

            }
        }

        //解析服务的回调

        /**
         * 当点连接蓝牙设备时会回调这个函数  此函数只是连接蓝牙设备
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status){
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic characteristic = gatt.getService(UUID_HRMService)//这里的UUID_HRMService,NOTIFY_HRMCharacteristic 要与单片机的三个（设备、读模式、写模式）UUID一致
                        .getCharacteristic(NOTIFY_HRMCharacteristic);
//                BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
//                        .getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                enableNotification(gatt,true,characteristic);
                Log.d("BLEService", "连接成功");
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLEService", "onCharacteristicRead:" + characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status){
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLEService", "onCharacteristicWrite:" + characteristic.getUuid());
            }
        }

        //当连接成功将回调该方法
        /**
         * 此函数   在蓝牙设备有数据发送过来时   这个函数会被触发  该函数是获取蓝牙设备传回的数据
         */
        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] bytes = characteristic.getValue();  //   获取蓝牙设备传回的数据
            StringBuilder builder = new StringBuilder();

            Log.d("BLEService", "data===========" + Arrays.toString(bytes));
            if (characteristic.getUuid().equals(NOTIFY_HRMCharacteristic)) {
                DataParse.getInstance().add(bytes, bytes.length);  // 把获取的数据放入  DataParse 缓存中
//                builder.append("HRM");
//                for (int i = 0; i < bytes.length; i++) {
//                    builder.append(bytes[i]).append(" ");
//                }
//                Log.d("BLEService", "onCharacteristicChanged:" + builder);
//                hrmDataParser(bytes);
  //              dataParserV2(bytes);
            } else {
                Log.d("BLEService", "onCharacteristicChanged and ignore because uuid incorect.");
            }

        }

        @Override
        public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor,int status){
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLEService", descriptor.getUuid().toString() + ":success");
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                Log.d("BLEService", descriptor.getUuid().toString() + ":failure");
            }
        }
    };

    private void dataParserV1(byte[] bytes){
        if(bytes[0]==0x21 || bytes[0]==0x22|| bytes[0]==0x23|| bytes[0]==0x24|| bytes[0]==0x25){
            //心率失常类型数据
            if(bytes[0]==0x21){
                //高大T波
                heartRateState = "高大T波";
            }else if(bytes[0]==0x22){
                //双R峰
                heartRateState = "双R峰";
            }else if(bytes[0]==0x23){
                //小R大S
                heartRateState = "小R大S";
            }else if(bytes[0]==0x24){
                //室早
                heartRateState = "室早";
            }else if(bytes[0]==0x24){
                //B型预激
                heartRateState = "B型预激";
            }
        }else if(bytes[0]>=0x10 && bytes[0]<=0x1F){
            if(bytes[1]>=0x00 && bytes[1]<=0x0F){
              //心率值数据
                heartRate = (bytes[0]-16)*16+bytes[1];
            }
        }else if(bytes[0]>=0xC0 && bytes[0]<=0xFF){
            if(bytes[1]>=0x80 && bytes[1]<=0xBF){
              //心电波形数据
                int c = (bytes[0]-192)*64+bytes[1]-128;
                ecgDa.add(c);
            }
        }
    }

    private void dataParserV2(byte[] bytes){
        if((bytes[0]&0x40)==0x40){
            if((bytes[1]&0x00)==0x00){
                //心电波形有效数据
                int c = (bytes[0]-64)*64+bytes[1];
               // ecgDa.add(c);
                MainActivity.mECGWaveDraw.add(c);
            }
        }else if ((bytes[0]&0xC0)==0xC0){
            if((bytes[1]&0x80)==0x80){
                //心率有效数据
                heartRate = (bytes[0]-192)*64+bytes[1]-128;
            }
        }
    }

    //心电数据解析
    private void hrmDataParser(byte[] bytes) {
        switch (bytes[0]) {
            case 0:
                //          type为Ecg原始数据的时候
                byte[] ecg = new byte[18];
                for (int i = 1; i < 19; i++) {
                    ecg[i - 1] = bytes[i];
                }
                ecgData.add(ecg);
                ecgDataParse();
                if (onDataListener != null) {
                    onDataListener.onEcgReceived(ecg);
                }
                break;
            case 1:
                //          type为心率数据的时候
                int hrm = bytes[2] * 255 + bytes[3];
                if (onDataListener != null) {
                    Log.d("BLEService", "hrm:" + hrm);
                    onDataListener.onHrmReceived(hrm);
                }
                break;
            case 0x5b:
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < 20; i++) {
                    stringBuilder.append(bytes[i]);
                }
                Log.d("BLEService", "心电设备SN:" + stringBuilder);
                break;
            case (byte) 0xbb:
                //                    WRITE_OK  //WRITE 接口的回复
                boolean isSuccess = false;
                if (bytes[2] == 1) {
                    //            write success
                    isSuccess = true;
                    Log.d("BLEService", "Write成功");
                } else if (bytes[2] == 0) {
                    //             write failure
                    isSuccess = false;
                    Log.d("BLEService", "Write失败");
                }
                switch (bytes[1]) {
                    case 0x3A:
                        Log.d("BLEService", "心电RESET" + String.valueOf(isSuccess));
                        break;
                    case 0x3B:
                        Log.d("BLEService", "心电SN" + String.valueOf(isSuccess));
                        break;
                    case 0x3C:
                        Log.d("BLEService", "心电UART" + String.valueOf(isSuccess));
                        break;
                }

                break;
        }
    }





    //    设置Hrm设备Sn或获取
  /*  public static void writeOrReadHrmDeviceSN(@Nullable byte[] SN) {
        byte[] bytes = new byte[20];
        bytes[0] = 0x3B;
        int length = 0;
        if (SN != null) {
            length = SN.length;
            System.arraycopy(SN, 0, bytes, 1, length);
        }
        bytes[19] = (byte) (length + 1);
        sendBytesToHrmDevice(bytes);
    }



    private static void sendBytesToHrmDevice(byte[] bytes) {
        if (HRM_WRITE != null && mGATT != null) {
            HRM_WRITE.setValue(bytes);
            mGATT.writeCharacteristic(HRM_WRITE);
        }
    }*/

    //    ECG数据解析及文件存储
    private void ecgDataParse() {
        if (ScheduleTime == 0) {
            //            初始化接收数据时间
            ScheduleTime = System.currentTimeMillis();
            Log.d("BLEService", "初始化接收数据时间" + ScheduleTime);
        } else {
            if (System.currentTimeMillis() - ScheduleTime >= CONST.DATA_SAVE_INTERVAL) {
                ScheduleTime = System.currentTimeMillis();
                //              转换引用,将一分钟内的数据存到缓存中,然后新建一个新的列表来存储数据
                ecgCache.clear();
                ecgCache.addAll(ecgData);
                ecgData = new LinkedList<>();
                //           TODO: 2016/6/30 进行存储数据库的操作
             /*   Util.runOnBackGroud(new Runnable() {
                    @Override
                    public void run() {
                        File dir = new File(Util.getFilePathInSdCard(CONST.DATA_SAVE_DIR));
                        if (!dir.exists()) {
                            dir.mkdirs();
                            Log.d("BLEService", "文件夹" + dir.getAbsolutePath() + "创建成功");
                        }
                        String fileName = UIUtil.getCustomerDate(ScheduleTime, "yyyy-MM-dd-HH-mm");
                        File file = new File(dir, fileName);
                        FileOutputStream ops = null;
                        try {
                            if (file.exists()) {
                                file.delete();
                            }
                            file.createNewFile();
                            // TODO: 2016/7/27  用outputstream写入文件
                            Log.d("BLEService", "文件" + file.getAbsolutePath() + "创建成功");
                            ops = new FileOutputStream(file, true);
                            for (byte[] bytes : ecgCache) {
                                ops.write(bytes);
                                ops.flush();
                            }
                            Log.d("BLEService", "存储文件success");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("BLEService", "创建或储存文件failure");
                        } finally {
                            try {
                                if (ops != null) {
                                    ops.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ecgCache = null;
                        }
                    }
                });*/

            }
        }
    }

}