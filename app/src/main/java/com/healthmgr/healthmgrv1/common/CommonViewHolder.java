package com.healthmgr.healthmgrv1.common;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class CommonViewHolder {
	public final View convertView;

	public CommonViewHolder(View convertView) {
		super();
		this.convertView = convertView;
		convertView.setTag(this);
	}
	 
	Map<Integer, View> views = new HashMap<Integer, View>();
	// 利用集合吧找好的View存起来，一旦集合中有，直接返回，不用查找
//	public View getView(int viewId){
//		if(views.get(viewId) ==null){
//			View v = convertView.findViewById(viewId);
//			views.put(viewId, v);
//		}
//		return views.get(viewId);
//	}
	
	// 类型推导 由返回值类型推导
	// 减少强转
	public <T extends View> T getView (int viewId){
		if (views.get(viewId) == null) {
			View v = convertView.findViewById(viewId);
			views.put(viewId, v);
		}
		return (T)views.get(viewId);
	}
	
	// 类型推导 由参数类型推导
	// 减少局部变量的定义
	public <T extends View> T getView (int viewId ,Class<T> viewClass){
		return (T) getView(viewId);
	}

	// 提供最常用的功能
	public TextView getTv(int viewId){
		return getView(viewId, TextView.class);
	}
	public ImageView getIv(int viewId){
		return getView(viewId, ImageView.class);
	}
	
	

	public static  CommonViewHolder getCVH(View convertView,Context context, int itemLayoutRes){
		if(convertView ==null){
			convertView = View.inflate(context, itemLayoutRes, null);
			return new CommonViewHolder(convertView);
		}else{
			return (CommonViewHolder) convertView.getTag();
		}
		
	}

}
