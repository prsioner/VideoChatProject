package com.prsioner.videochatproject;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

/**
 * @ClassName: PermissionsUtils
 * @Author: qinglin
 * @CreateDate: 2021/2/15 11:20
 * @Description: 权限
 */
public class PermissionsUtils {



    //申请文件访问权限
    public static void verifyStoragePermissions(Activity activity) {
        //先定义
        final int REQUEST_EXTERNAL_STORAGE = 1;

        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
