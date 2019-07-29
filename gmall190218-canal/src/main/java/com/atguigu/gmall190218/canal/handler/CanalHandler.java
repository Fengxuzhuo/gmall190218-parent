package com.atguigu.gmall190218.canal.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.atguigu.gmall190218.canal.MyKafkaSender;
import com.atguigu.gmall190218.common.constant.GmallConstant;

import java.util.List;
import java.util.Random;

public class CanalHandler {

    String tableName;
    CanalEntry.EventType eventType;
    List<CanalEntry.RowData> rowDataList;

    public CanalHandler(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDataList) {
        this.tableName = tableName;
        this.eventType = eventType;
        this.rowDataList = rowDataList;
    }


    public void handle() {
        if (tableName.equals("order_info") && eventType == CanalEntry.EventType.INSERT) { //下单操作
            sendKafka(GmallConstant.KAFKA_TOPIC_ORDER);
        } else if (tableName.equals("order_detail") && eventType == CanalEntry.EventType.INSERT) { //订单明细
            sendKafka(GmallConstant.KAFKA_TOPIC_ORDER_DETAIL);
        } else if (tableName.equals("user_info") && eventType == CanalEntry.EventType.UPDATE) { //用户信息
            sendKafka(GmallConstant.KAFKA_TOPIC_USER);
        }
    }

    private void sendKafka(String topic) {
        for (CanalEntry.RowData rowData : rowDataList) { //遍历行集
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();//修改后的集合
            JSONObject jsonObject = new JSONObject();
            for (CanalEntry.Column column : afterColumnsList) {
                System.out.println(column.getName() + " & " + column.getValue());
                jsonObject.put(column.getName(), column.getValue());
            }

            try {
                Thread.sleep(new Random().nextInt(5) * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //发送Kafka
            MyKafkaSender.send(topic, jsonObject.toJSONString());
        }

    }

}
