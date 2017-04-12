package com.yusheng123.fix;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;

/**
 * Created by wang on 2017/4/12.
 */
public class FixDexManager {

    private static final String TAG = FixDexManager.class.getName();
    private Context mContext;

    private File mDexDir;

    //非单例，若context为activity会出现内存泄漏问题
    public FixDexManager(Context context) {
        this.mContext = context;
        this.mDexDir = context.getDir("odex", context.MODE_PRIVATE);
    }

    /**
     * 修复dex包
     *
     * @param fixDexPath
     */
    public void fixDex(String fixDexPath) throws Exception {
        //1、先获取已经运行的 dexElement
        ClassLoader applicationClassLoader = mContext.getClassLoader();
        Object applicationDexElements = getDexElementsByClcassLoader(applicationClassLoader);
        //2、获取下载好的fixDex的dexElement
        //2.1 将fixDex移动到系统能够访问的路径
        File srcFile = new File(fixDexPath);
        if (!srcFile.exists()) {
            throw new FileNotFoundException(fixDexPath);
        }
        File destFile = new File(mDexDir, srcFile.getName());
        //为什么不覆盖？下载的dex应该带有版本号，所以不可能重复
        if (destFile.exists()) {
            Log.d(TAG, "patch [" + fixDexPath + "] has be loaded.");
            return;
        }
        copyFile(srcFile, destFile);
        //2.2 ClassLoader 读取fixDex路径
        List<File> fixDexFiles = new ArrayList<>();
        fixDexFiles.add(destFile);

        File optimizedDirectory = new File(mDexDir, "odex");
        if (!optimizedDirectory.exists()) {
            optimizedDirectory.mkdirs();
        }
        for (File fixDexFile : fixDexFiles) {
            ClassLoader fixDexClassLoader = new BaseDexClassLoader(
                    fixDexFile.getAbsolutePath(),//dexPath dex路径 必须要要在应用目录下的odex文件中
                    optimizedDirectory,     //optimizedDirector 解压路径
                    null,        //librarySearchPath .so位置
                    applicationClassLoader //parent 父classLoader
            );
            Object fixDexElements = getDexElementsByClcassLoader(fixDexClassLoader);
            //3、把补丁的dexElements 插到 已经运行的dexElements最前面,由于这两个都是数组，通过数组合并
            applicationDexElements = combinArray(fixDexElements, applicationDexElements);
        }

        //把合并的数组注入到原来的applicationClassLoader中
        injectDexElemets(applicationClassLoader, applicationDexElements);
    }

    /**
     * 把dexElements注入到classloader中
     *
     * @param classLoader
     * @param dexElements
     */
    private void injectDexElemets(ClassLoader classLoader, Object dexElements) throws Exception {
        //1、先从BaseDexClassLoader中获取pathList
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);

        //2、获取pathList里的dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        //反射注入合并的dexElements
        dexElementsField.set(pathList, dexElements);

    }

    /**
     * 合并两个数组
     *
     * @param arrayLhs
     * @param arrayRhs
     * @return
     */
    private static Object combinArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

    /**
     * copy file
     *
     * @param src  source file
     * @param dest target file
     * @throws IOException
     */
    public static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * 从classLoader中获取 dexElements
     *
     * @param classLoader
     * @return
     */
    private Object getDexElementsByClcassLoader(ClassLoader classLoader) throws Exception {
        //1、先从BaseDexClassLoader中获取pathList
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);

        //2、获取pathList里的dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        Object dexElements = dexElementsField.get(pathList);

        return dexElements;

    }
}
