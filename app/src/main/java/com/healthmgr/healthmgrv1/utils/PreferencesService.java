package com.healthmgr.healthmgrv1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xzj on 2016/11/17 0017.
 */

public class PreferencesService {
    private Context context;
    public PreferencesService(Context context){
        super();
        this.context = context;
    }

    /**
     * @param vCfgConPerson
     * @param vCfgPhone
     */
    public void save(String vCfgConPerson,String vCfgPhone){
        SharedPreferences sharedPreferences = context.getSharedPreferences("configPreference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.putString("cfgConPerson", vCfgConPerson);
        editor.putString("cfgPhone", vCfgPhone);  //目前是保存在内存中，还没有保存到文件中
        editor.commit();    //数据提交到xml文件中

    }

    /**
     * @return params
     */
    public Map<String,String> getPreferences(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("configPreference", Context.MODE_PRIVATE);
        Map<String,String> params = new HashMap<String, String>();
        params.put("cfgConPerson", sharedPreferences.getString("cfgConPerson", ""));
        params.put("cfgPhone",  sharedPreferences.getString("cfgPhone", ""));
        return params;
    }
}
