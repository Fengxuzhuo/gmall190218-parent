package com.atguigu.gmall190218.publisher.service.impl;

import com.atguigu.gmall190218.publisher.mapper.DauMapper;
import com.atguigu.gmall190218.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    DauMapper dauMapper;

    @Override
    public int getDauTotal(String date) {
        return dauMapper.getDauTotal(date);
    }

    @Override
    public Map getDauHour(String date) {

        List<Map> dauHourList = dauMapper.getDauHour(date);
        //把List<Map> 转换成 Map
        Map dauHourMap = new HashMap();

        for (Map map : dauHourList) {
            String loghour = (String) map.get("LOGHOUR");
            Long ct = (Long) map.get("CT");
            dauHourMap.put(loghour, ct);
        }

        return dauHourMap;
    }
}
