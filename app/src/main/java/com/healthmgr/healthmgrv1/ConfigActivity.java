package com.healthmgr.healthmgrv1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.healthmgr.healthmgrv1.utils.PreferencesService;

import java.util.Map;
/**
 * Created by xzj on 2016/11/17 0017.
 */
public class ConfigActivity extends Activity{
    private EditText cfgConPerson;
    private EditText cfgPhone;
    private PreferencesService service;
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        cfgConPerson = (EditText) findViewById(R.id.vCfgConPerson);
        cfgPhone = (EditText) findViewById(R.id.vCfgPhone);
        service = new PreferencesService(ConfigActivity.this);
        //打开时读取保存的参数
        Map<String,String> params = service.getPreferences();
        cfgConPerson.setText(params.get("cfgConPerson"));
        cfgPhone.setText(params.get("cfgPhone"));
    }
    public void vcfgsave(View v){
        String conPerson = cfgConPerson.getText().toString();
        String phone = cfgPhone.getText().toString();
        service.save(conPerson,phone);
        Toast.makeText(ConfigActivity.this,"success",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ConfigActivity.this, MainActivity.class);
        startActivity(intent);
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
        super.onDestroy();
    }

}