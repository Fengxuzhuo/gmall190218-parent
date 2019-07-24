package com.atguigu.gmall190218.publisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall190218.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        int newMidTotal = publisherService.getDauTotal(date);
        Map newMidMap = new HashMap();
        newMidMap.put("id", "dau");
        newMidMap.put("name", "新增用户");
        newMidMap.put("value", newMidTotal);
        totalList.add(newMidMap);

        return JSON.toJSONString(totalList);
    }
}
