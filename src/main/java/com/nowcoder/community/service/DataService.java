package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    // 将指定的IP计入UV
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            //获得这个日期区间内每一天的key,用于后面的合并
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        //合并这些数据，这些数据可能会存在重复数据，例如某个用户这几天都访问了，那他的ip就会被多次记录
        //但是我们最后只算他一次，所以需要去除重复数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        //redisKey是用来存储合并后的数据，keyList.toArray()是哪些key对应的数据要来合并，多个key可以以数组形式
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计的结果，size方法会去除合并后数据的重复数据，获得独立数据数量
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户计入DAU
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        //例如用户id是101，那么第101位，置为true
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            //获得这个日期区间内每一天的key,用于后面的或运算
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            //将key从string转为byte[]数组
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }


        //只要这个日期区间内，用户访问过一次，就算活跃用户
        //进行OR运算，例如id为101用户第一天访问了，第101位为1，第二天没访问，第101位为0，做或运算，第101位为1
        //如果他第一天访问了，第二天也访问了，第101位还是1，我们只记录一次
        //最后统计所有位，哪些位上是1，就证明对应这些位的id用户，为活跃用户，统计所有1的数量
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                //redisKey是用来存储or或运算后的数据
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                //bitOp代表位操作运算，RedisStringCommands.BitOperation.OR代表或运算
                //redisKey.getBytes()，操作运算指定传入byte[]类型的key,用来存储or或运算后的数据
                //keyList.toArray(new byte[0][0]),将集合转为二维byte数组，指定哪些key对应数据来做or运算
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                //统计位上为1的数量，同样参数是byte[]数组形式的key
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }

}
