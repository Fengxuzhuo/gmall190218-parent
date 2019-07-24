package com.atguigu.gmall190218.publisher.service;

import java.util.Map;

public interface PublisherService {

    /**
     * 查询总数
     * @param date
     * @return
     */
    public int getDauTotal(String date);


    /**
     * 查询分时明细
     * @param date
     * @return
     */
    public Map getDauHour(String date);

}
