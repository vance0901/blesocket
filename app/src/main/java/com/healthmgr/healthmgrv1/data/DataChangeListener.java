package com.healthmgr.healthmgrv1.data;

/**
 * Created by Administrator on 2016/11/24 0024.
 */

public interface DataChangeListener {

    String getType();

    void addData(Integer val);

}
