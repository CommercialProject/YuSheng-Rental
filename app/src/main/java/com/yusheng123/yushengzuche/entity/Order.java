package com.yusheng123.yushengzuche.entity;

/**
 * Created by Monty on 2017/1/4.
 */
public class Order {
    public String id;
    /**
     * 订单创建时间
     */
    public String ctime;
    /**
     * 订单开始时间
     */
    public String startTime;
    /**
     * 订单结束时间
     */
    public String endTime;
    /**
     * 订单状态
     */
    public String status;
    /**
     * 车辆图片路径
     */
    public String imgPath;
    /**
     * 车辆名称
     */
    public String carName;
    /**
     * 车辆属性
     */
    public String carFeatures;
    /**
     * 取车门店
     */
    public String takeStore;
    /**
     * 还车门店
     */
    public String yetStore;
    /**
     * 取车时间
     */
    public String takeTime;
    /**
     * 还车时间
     */
    public String yetTime;
}
