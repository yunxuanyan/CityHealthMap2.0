package com.cityhealthmap;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;

public class EnvDataItem {

    public String userId;
    public String startTime;
    public String endTime;
    public String locations;
    public double longitude;//不入库,只做计算使用
    public double latitude;
    public String temperature;
    public String humidity;
    public String pm1;
    public String pm25;
    public String pm10;
    public String lineMark; //标记各点是否为同一条曲线 01911,019userid,1季节,1线号一位或两位

    public HashMap<String, AttributeValue> getDynamoDBItem(){
        HashMap<String,AttributeValue> itemValues = new HashMap<String,AttributeValue>();
        itemValues.put("userId", AttributeValue.builder().n(userId).build());
        itemValues.put("startTime", AttributeValue.builder().s(startTime).build());
        itemValues.put("endTime", AttributeValue.builder().s(endTime).build());
        itemValues.put("locations", AttributeValue.builder().s(locations).build());
        itemValues.put("temperature", AttributeValue.builder().n(temperature).build());
        itemValues.put("humidity", AttributeValue.builder().n(humidity).build());
        itemValues.put("pm1", AttributeValue.builder().n(pm1).build());
        itemValues.put("pm25", AttributeValue.builder().n(pm25).build());
        itemValues.put("pm10", AttributeValue.builder().n(pm10).build());
        itemValues.put("lineMark", AttributeValue.builder().n(lineMark).build());
        return itemValues;
    }

    public String toString(){
        return "userid:" + this.userId +"|"
                +"starttime:" + this.startTime +"|"
                +"endtime:" + this.endTime +"|"
                + "locations:" + this.locations+"|"
                + "temperature:" + this.temperature+"|"
                + "humidity:" + this.humidity+"|"
                + "pm1:" + this.pm1+"|"
                + "pm25:" + this.pm25+"|"
                + "pm10:" + this.pm10+"|"
                + "lineMark:" + lineMark+"|";
    }

}
