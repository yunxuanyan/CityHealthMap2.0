package com.cityhealthmap;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static private String TableName = "AllEnv";
    static Region region = Region.AP_SOUTHEAST_2;
    static DynamoDbClient ddb = DynamoDbClient.builder().region(region).build();


    private static void putItem(DynamoDbClient ddb, EnvDataItem item){

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TableName)
                .item(item.getDynamoDBItem())
                .build();
        try {
            ddb.putItem(request);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", TableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }

    public static void csv2DynamoDB(String filePath, String season){
        List<EnvDataItem> envItems = new ArrayList<EnvDataItem>();
        try {
            Files.lines(Paths.get(filePath), Charset.defaultCharset())
                .forEach(line->{
                    if(!line.contains("UserId")){
                        String[] vals = line.replaceAll(", ",",").split(",");
                        EnvDataItem item = new EnvDataItem();
                        item.userId = vals[0];
                        item.startTime = standardlizeTimeString(vals[1]);
                        item.endTime = standardlizeTimeString(vals[1]);
                        item.locations = "{\"longitude\":\""+vals[2]+"\",\"latitude\":\""+vals[3]+"\"}";
                        item.longitude = Double.parseDouble(vals[2]);
                        item.latitude = Double.parseDouble(vals[3]);
                        item.temperature = vals[4];
                        item.humidity = vals[5];
                        item.pm1 = vals[6];
                        item.pm25 = vals[7];
                        item.pm10 = vals[8];
                        envItems.add(item);
                    }
                });
        } catch (IOException e) {
            System.out.println(e);
        }

        List<EnvDataItem> mergedItems = mergeItems(envItems,season);
        for(int i = 0; i < mergedItems.size();i++){
            System.out.println(mergedItems.get(i));
            putItem(ddb,mergedItems.get(i));
        }
        System.out.println("Merge before: "+envItems.size());
        System.out.println("Merge After: "+mergedItems.size());

    }

    private static String standardlizeTimeString(String time){
        if(time.contains("/")) {
            return time.replaceAll("/", "-") + ":00";
        } else {
            return time;
        }
    }

    private static List<EnvDataItem> mergeItems(List<EnvDataItem> envItems,String season){
        List<EnvDataItem> mergedItems = new ArrayList<>();
        EnvDataItem item = null;
        //用于计算是否在同一条线上
        int m = 0;
        String mark="";
        for (int i = 0; i < envItems.size(); i++){
            if(item == null){
                item = envItems.get(i);
            } else {
                EnvDataItem currentItem = envItems.get(i);
                mark = currentItem.userId+season;
                if(item.userId.equals(currentItem.userId) && item.humidity.equals(currentItem.humidity)
                && item.temperature.equals(currentItem.temperature)){//认为是同一组值,合并
                    item.locations += "," + currentItem.locations;
                    item.endTime = currentItem.startTime;
                    item.longitude = currentItem.longitude;
                    item.latitude = currentItem.latitude;
                } else {
                    item.locations = "["+item.locations+"]";
                    item.lineMark = mark + m;
                    mergedItems.add(item);
                    if(!isOnTheSameLine(item,currentItem)){
                        m++;
                    }
                    item = currentItem;
                }
            }
        }
        item.locations = "["+item.locations+"]";
        item.endTime = envItems.get(envItems.size()-1).startTime;
        item.lineMark = mark + m;
        mergedItems.add(item);
        return mergedItems;
    }

    private static boolean isOnTheSameLine(EnvDataItem a, EnvDataItem b){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long amil=0, bmil=0;
        try{
            amil = sdf.parse(a.endTime).getTime();
            bmil = sdf.parse(b.startTime).getTime();
        }catch (ParseException e){
            System.out.println(e);
        }
        boolean isTimeShort = Math.abs(bmil-amil) < 3600000;
        boolean isDistantShort = 0.0004 > (Math.pow(a.longitude-b.longitude,2)+Math.pow(a.latitude-b.latitude,2));
        return isTimeShort&&isDistantShort;
    }

    public static void main(String[] args) {

        csv2DynamoDB("/Users/xiaoxi/Documents/job/jenny/cityhealthoutlook/完整版四期所有实验数据/processed/1-all.csv","1");
//        csv2DynamoDB("/Users/xiaoxi/git/city_health_map_2/testSample.csv","1");

        System.out.println("End!");
    }
}
