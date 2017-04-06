package com.yusheng123.yushengzuche.util;

/**
 * Created by Monty on 2017/1/1.
 */

public class Url {
    private static String host = "http://www.yusheng123.com";
    /**
     * 获取订单列表，分页接口
     * http://www.yusheng123.com/admin/orders/list?pageNumber=1
     */
    private static String URL_ORDER_LIST = "/admin/orders/list";

    /**
     * 获取图片Url
     *
     * @param imgPath
     * @return
     */
    public static String getImageUrl(String imgPath) {
        return host + imgPath;
    }

    public static String getOrderPath() {
        return host + URL_ORDER_LIST;
    }
}
