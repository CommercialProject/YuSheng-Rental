package com.yusheng123.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wang on 2017/4/10.
 */
public class PermissionUtil {


    private PermissionUtil() {
    }


    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void executeSucceedMethod(Object object, int requestCode) {
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            PermissionSucceed permissionSucceed = method.getAnnotation(PermissionSucceed.class);
            if (permissionSucceed != null) {
                int methodRequestCode = permissionSucceed.requestCode();
                if (methodRequestCode == requestCode) {
                    //这个就是我们要找的成功方法
                    executeMethod(object, method);
                }
            }
        }
    }

    /**
     * 反射执行该方法
     *
     * @param object
     * @param method
     */
    private static void executeMethod(Object object, Method method) {
        method.setAccessible(true);
        try {
            method.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取没有授予的权限
     *
     * @param object
     * @param permissions
     * @return
     */
    public static List<String> getDeniedPermissions(Object object, String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String requestPermissions : permissions) {
            //把没有授予的权限加入集合
            if (ContextCompat.checkSelfPermission(getActivity(object), requestPermissions) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(requestPermissions);
            }
        }

        return deniedPermissions;
    }

    /**
     * 获取activity
     *
     * @param object
     * @return
     */
    public static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return (Activity) object;
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        }
        return null;
    }

    /**
     * 执行失败的方法
     * @param object
     * @param requestCode
     */
    public static void executeFailMethod(Object object, int requestCode) {
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            PermissionFail permissionFail = method.getAnnotation(PermissionFail.class);
            if (permissionFail != null) {
                int methodRequestCode = permissionFail.requestCode();
                if (methodRequestCode == requestCode) {
                    //这个就是我们要找的失败方法
                    executeMethod(object, method);
                }
            }
        }
    }
}
