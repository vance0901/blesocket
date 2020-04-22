package com.healthmgr.healthmgrv1.utils;






public class CONST
{
	//    绑定设备的mac地址
	public static final String DEVICE_ADDRESS = "device_address";
	//    后台服务对设备的连接状态
	public static final String STATE_CONNECTED = "state_connected";
	public static final String STATE_DISCONNECTED = "state_disconnected";
	//    后台连接绑定设备过程的状态
	public static final int SEARCH_DEVICE = 1;
	public static final int CONNECT_DEVICE = 2;
	public static final int CONNECT_DEVICE_OK = 3;
	//    后台服务接收存储文件的时间间隔 单位为ms
	public static final long DATA_SAVE_INTERVAL = 1000 * 60;
	//    蓝牙设备数据存储的文件夹名称
	public static final String DATA_SAVE_DIR = "蓝牙心电数据";
	//    是否绑定了设备
	public static final String DEVICE_IS_BOUND = "device_is_bound";
	
}