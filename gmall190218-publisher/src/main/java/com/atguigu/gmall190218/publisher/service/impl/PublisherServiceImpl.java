package com.atguigu.gmall190218.publisher.service.impl;

import com.atguigu.gmall190218.publisher.mapper.DauMapper;
import com.atguigu.gmall190218.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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



        return null;
    }
}
