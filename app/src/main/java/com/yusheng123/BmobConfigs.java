package com.yusheng123;

/**
 * Created by Monty on 2017/4/19.
 * 
 * Application ID，SDK初始化必须用到此密钥
 * REST API Key，REST API请求中HTTP头部信息必须附带密钥之一
 * Secret Key，是SDK安全密钥，不可泄漏，在云端逻辑测试云端代码时需要用到
 * Master Key，超级权限Key。应用开发或调试的时候可以使用该密钥进行各种权限的操作，此密钥不可泄漏
 *
 */

public class BmobConfigs {
    public static String Application_Id = "b937cbba1374b9ca35b75873d7fc3e58";
    public static String REST_API_Key = "793b51f735cccb693a28e60410138ead";
    public static String Secret_Key = "3c6511db55f61f2d";
    public static String Master_Key = "552a13ddbcde7ba94d8e469a7f4ac730";
}
