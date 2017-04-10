package com.yusheng123.permission;

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * Created by wang on 2017/4/10.
 */
public class PermissionHelper {


    private static PermissionHelper instance;
    private int mRequestCode;
    private String[] mPermissions;

    private PermissionHelper() {

    }

    public static void requestPermission(Activity activity, int requestCode, String[] permissions) {
        PermissionHelper.with().requestCode(requestCode).requestPermission(permissions).request(activity);
    }

    public static void requestPermission(Fragment fragment, int requestCode, String[] permissions) {
        PermissionHelper.with().requestCode(requestCode).requestPermission(permissions).request(fragment);
    }

    private PermissionHelper requestPermission(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    private PermissionHelper requestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    /**
     * 发起请求权限
     */
    private void request(Object object) {
        if (!PermissionUtil.isOverMarshmallow()) {
            //非6.0以上直接执行
            PermissionUtil.executeSucceedMethod(object, mRequestCode);
            return;
        }
        //如果是6.0以上
        //1、先判断权限是否授予
        List<String> deniedPermissions = PermissionUtil.getDeniedPermissions(object, mPermissions);
        if (deniedPermissions.size() == 0) {
            //全部都是授予过的
            PermissionUtil.executeSucceedMethod(object, mRequestCode);
        } else {
            //没有授予，则申请没有授予的权限
            ActivityCompat.requestPermissions(PermissionUtil.getActivity(object), deniedPermissions.toArray(new String[deniedPermissions.size()]), mRequestCode);
        }
    }

    /**
     * 链式调用
     *
     * @param
     * @return
     */
    private static PermissionHelper with() {
        return getInstance();
    }

    private static PermissionHelper getInstance() {
        if (instance == null) {
            instance = new PermissionHelper();
        }
        return instance;
    }

    /**
     * 处理申请权限的回调
     *
     * @param object
     * @param requestCode
     * @param permissions
     */
    public static void requestPermissionResult(Object object, int requestCode, String[] permissions) {
        //再次获取没有授予的权限
        List<String> deniedPermissions = PermissionUtil.getDeniedPermissions(object, permissions);

        if (deniedPermissions.size() == 0) {
            //权限用户都同意了
            PermissionUtil.executeSucceedMethod(object, requestCode);
        } else {
            //用户不同意
            PermissionUtil.executeFailMethod(object, requestCode);
        }
    }
}
