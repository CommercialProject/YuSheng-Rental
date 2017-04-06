package com.yusheng123.yushengzuche.order;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yusheng123.yushengzuche.entity.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monty on 2017/1/6.
 */

public abstract class AbsModel implements IModel {
    private int page = 1;
    private int totalPage = 1;

    protected int getTotalPage() {
        return totalPage;
    }
    protected int getCurentPage(){
        return page;
    }

    protected List<Order> parseOrderList(String json) {
        List<Order> orders = new ArrayList<>();
        Gson gson = new GsonBuilder().create();

        Log.e("monty", "json : " + json);
        JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
        JsonArray content = jsonObj.get("content").getAsJsonArray();
        totalPage = jsonObj.get("totalPage").getAsInt();

        for (int i = 0; i < content.size(); i++) {
            Order order = new Order();
            JsonObject object = content.get(i).getAsJsonObject();
            order.id = object.get("id").getAsString();
            order.ctime = object.get("ctime").getAsString();
            order.startTime = object.get("startTime").getAsString();
            order.endTime = object.get("endTime").getAsString();
            order.status = object.get("dictItem").getAsJsonObject().get("name").getAsString();
            JsonElement imgPath = object.get("car").getAsJsonObject().get("img");

            if (imgPath.isJsonNull()) {
                order.imgPath = "";
            } else {
                order.imgPath = imgPath.getAsString();
            }

            order.carName = object.get("car").getAsJsonObject().get("brand").getAsJsonObject().get("brand").getAsJsonObject().get("name") + " " + object.get("car").getAsJsonObject().get("brand").getAsJsonObject().get("name") + " " + object.get("car").getAsJsonObject().get("name");
            order.carFeatures = object.get("car").getAsJsonObject().get("output").getAsString() + " / " + object.get("car").getAsJsonObject().get("automatic").getAsJsonObject().get("name").getAsString() + " / " + object.get("car").getAsJsonObject().get("struct").getAsJsonObject().get("name").getAsString();
            order.takeStore = object.get("takeStore").getAsJsonObject().get("name").getAsString();
            order.yetStore = object.get("yetStore").getAsJsonObject().get("name").getAsString();
            order.takeTime = order.startTime;
            order.yetTime = order.endTime;
            orders.add(order);
        }
        return orders;
    }
}
