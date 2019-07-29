package com.atguigu.gmall190218.publisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall190218.publisher.service.PublisherService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class PublisherController {

    @Autowired
    PublisherService publisherService;

    @GetMapping("realtime-total")
    public String getRealtimeTotal(@RequestParam("date") String date) {

        List<Map> totalList = new ArrayList<>();

        //日活总数
        int dauTotal = publisherService.getDauTotal(date);
        Map dauMap = new HashMap();
        dauMap.put("id", "dau");
        dauMap.put("name", "新增日活");
        dauMap.put("value", dauTotal);
        totalList.add(dauMap);


        //新增用户
        //int newMidTotal = publisherService.getDauTotal(date);
        Map newMidMap = new HashMap();
        newMidMap.put("id", "new_mid");
        newMidMap.put("name", "新增设备");
        newMidMap.put("value", 233);
        totalList.add(newMidMap);

        return JSON.toJSONString(totalList);
    }


    @GetMapping("realtime-hour")
    public String getRealtimeHour(@RequestParam("id") String id, @RequestParam("date") String todayDate) {

        if(id.equals("dau")) {
            //日活
            Map dauHourTDMap = publisherService.getDauHour(todayDate);

            String yesterdayDate = getYdate(todayDate);
            Map dauHourYDMap = publisherService.getDauHour(yesterdayDate);

            Map<String, Map> hourMap = new HashMap();
            hourMap.put("today", dauHourTDMap);
            hourMap.put("yesterday", dauHourYDMap);

            return JSON.toJSONString(hourMap);
        }

        return null;
    }


    //日期时间转换
    private String getYdate(String todayDate) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String ydateString = "";
        try {
            Date tdate = simpleDateFormat.parse(todayDate);
            Date ydate = DateUtils.addDays(tdate, -1);
            ydateString = simpleDateFormat.format(ydate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ydateString;
    }



}
