package com.yusheng123.fix;

import android.content.Context;
import android.util.Log;

import com.yusheng123.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;

/**
 * Created by wang on 2017/4/12.
 */
public class FixDexManager {

    private static final String TAG = FixDexManager.class.getName();
    private Context mContext;

    private File mDexDir;//内部存储dex的路径

    //非单例，若context为activity会出现内存泄漏问题
    public FixDexManager(Context context) {
        this.mContext = context;
        this.mDexDir = context.getDir("odex", context.MODE_PRIVATE);
    }

    /**
     * 将下载的dexfile转移到应用目录中
     *
     * @param fixDexPath 下载的dex文件（最好在外部存储中）
     * @throws Exception
     */
    public void addFixDex(String fixDexPath) throws Exception {
        //1、获取下载好的fixDex的dexElement
        //1.1 将fixDex移动到系统能够访问的路径
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
        FileUtil.copyFile(srcFile, destFile);
        FileUtil.deleteFile(srcFile);
    }

    /**
     * 把dexElements注入到classloader中
     *
     * @param classLoader 需要替换掉dexElements的classLoader（运行中的classloader）
     * @param dexElements 新的dexElements（附带没有bug的dex文件数组）
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
     * @param arrayLhs 此数组元素在新数组的前面
     * @param arrayRhs 此数组元素在新数组的后面
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
     * 从classLoader中获取 dexElements
     *
     * @param classLoader 获取dexElements的classLoader
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

    /**
     * 加载应用内部存在的dex文件，并修复bug
     */
    public void loadFixDex() throws Exception {
        //筛选出后缀为.dex的文件
        File[] dexFiles = mDexDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".dex");
            }
        });
        if (dexFiles.length != 0) {
            //fixbug
            fix(dexFiles);
        } else {
            Log.i(TAG, "no dexfiles!");
            throw new Exception("no dexfiles");
        }
    }

    /**
     * 修复bug
     *
     * @param dexFiles 没有bug的文件数组
     * @throws Exception
     */
    private void fix(File[] dexFiles) throws Exception {
        //1、先获取已经运行的 dexElement
        ClassLoader applicationClassLoader = mContext.getClassLoader();
        Object applicationDexElements = getDexElementsByClcassLoader(applicationClassLoader);

        File optimizedDirectory = new File(mDexDir, "odex");
        if (!optimizedDirectory.exists()) {
            optimizedDirectory.mkdirs();
        }
        for (File dexFile : dexFiles) {
            ClassLoader fixDexClassLoader = new BaseDexClassLoader(
                    dexFile.getAbsolutePath(),//dexPath dex路径 必须要要在应用目录下的odex文件中
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


}
