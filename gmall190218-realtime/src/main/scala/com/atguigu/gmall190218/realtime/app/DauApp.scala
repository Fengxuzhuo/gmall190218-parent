package com.atguigu.gmall190218.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.atguigu.gmall190218.common.constant.GmallConstant
import com.atguigu.gmall190218.realtime.bean.StartupLog
import com.atguigu.gmall190218.realtime.util.{MyKafkaUtil, RedisUtil}
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import org.apache.phoenix.spark._

object DauApp {

    def main(args: Array[String]): Unit = {

        val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("dau_app")
        val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(5))

        //1.消费kafka
        val inputDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstant.KAFKA_TOPIC_STARTUP, ssc)

        /*
        inputDStream.foreachRDD(rdd => {
            println(rdd.map(_.value()).collect().mkString("\n"))
        })*/


        // 2 结构转换成case class 补充两个时间字段
        val startupLogDStream: DStream[StartupLog] = inputDStream.map { record =>
            val jsonString: String = record.value()
            val startupLog: StartupLog = JSON.parseObject(jsonString, classOf[StartupLog])
            val formatter = new SimpleDateFormat("yyyy-MM-dd HH")
            val datetimeStr: String = formatter.format(new Date(startupLog.ts))
            val dateTimeArr: Array[String] = datetimeStr.split(" ")
            startupLog.logDate = dateTimeArr(0)
            startupLog.logHour = dateTimeArr(1)
            startupLog
        }

        startupLogDStream.cache()



        //2  去重  根据今天访问过的用户清单进行过滤
        val filteredDStream: DStream[StartupLog] = startupLogDStream.transform { rdd =>
            println("过滤前："+rdd.count())
            val jedis: Jedis = RedisUtil.getJedisClient
            val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
            val dateString: String = dateFormat.format(new Date())
            val key: String = "dau:" + dateString
            val midSet: util.Set[String] = jedis.smembers(key)
            jedis.close()
            val midBC: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
            val filteredRDD: RDD[StartupLog] = rdd.filter { startupLog =>
                !midBC.value.contains(startupLog.mid)
            }
            println("过滤后："+filteredRDD.count())
            filteredRDD

        }
        //本批次内进行去重
        val distinctDStream: DStream[StartupLog] = filteredDStream.map(startuplog => (
                startuplog.mid, startuplog)).groupByKey().flatMap {
                    case (mid, startupLogItr) =>
                        startupLogItr.take(1)
        }



        // 问题 ：没有周期性查询redis 而只执行了一次
        //    val jedis: Jedis = RedisUtil.getJedisClient
        //    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
        //    val dateString: String = dateFormat.format(new Date())
        //    val key: String = "dau:"+  dateString
        //    val midSet: util.Set[String] = jedis.smembers(key)
        //    val midBC: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
        //    startupLogDstream.filter{startupLog=>
        //      !midBC.value.contains(startupLog.mid)
        //    }


        //  问题： 连接 操作jedis次数过多
        //    startupLogDstream.filter{startupLog=>
        //      val jedis: Jedis = RedisUtil.getJedisClient
        //      val key: String = "dau:"+startupLog.logDate
        //      !jedis.sismember(key,startupLog.mid)
        //
        //    }


        // 3 把所有今天访问过的用户保存起来

        distinctDStream.foreachRDD{rdd=>
            rdd.foreachPartition{ startupItr=>   //利用foreachPartition 减少创建连接的次数
                val jedis: Jedis = RedisUtil.getJedisClient
                for (startupLog <- startupItr ) {
                    val key: String = "dau:"+startupLog.logDate
                    jedis.sadd(key,startupLog.mid)
                    println(startupLog)
                }
                jedis.close()
            }
        }


        // 4 保存到hbase
        distinctDStream.foreachRDD{rdd=>
            rdd.saveToPhoenix(
                "gmall190218_dau",
                Seq("MID", "UID", "APPID", "AREA", "OS", "CH", "TYPE", "VS", "LOGDATE", "LOGHOUR", "TS"),
                new Configuration,
                Some("hadoop102,hadoop103,hadoop104:2181"))
        }




        ssc.start()
        ssc.awaitTermination()
    }

}
