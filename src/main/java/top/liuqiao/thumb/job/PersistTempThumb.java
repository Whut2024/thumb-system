package top.liuqiao.thumb.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liuqiao.thumb.constant.redis.ThumbRedisConstant;
import top.liuqiao.thumb.enums.ThumbOperationEnum;
import top.liuqiao.thumb.mapper.ThumbCountMapper;
import top.liuqiao.thumb.mapper.ThumbMapper;
import top.liuqiao.thumb.model.entity.Thumb;
import top.liuqiao.thumb.util.ThumbUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 间隔固定时间扫描 Redis 中缓存的暂时点赞操作，持久化这些操作 <br>
 * {@link top.liuqiao.thumb.service.impl.ThumbServiceImpl} <br>
 * {@link top.liuqiao.thumb.constant.redis.ThumbLuaConstant}
 *
 * @author liuqiao
 * @since 2025-04-20
 */
@Component
@AllArgsConstructor
@Slf4j
public class PersistTempThumb {

    private final ThumbMapper thumbMapper;

    private final ThumbCountMapper thumbCountMapper;

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redissonClient;

    private final static ExecutorService exe = Executors.newSingleThreadExecutor();


    @Scheduled(initialDelay = 10_000L, fixedRate = 10_000L)
    void run() {
        long targetTime = System.currentTimeMillis() - 10_000;
        String slice = ThumbUtil.getTimeStampSlice(targetTime);
        persist(slice);
    }


    void persist(String slice) {
        log.info("开始持久化用户点赞相关操作");
        // 加锁
        RLock lock = redissonClient.getLock(ThumbRedisConstant.THUMB_TMP_PERSISTENCE_LOCK_KEY);
        if (!lock.tryLock()) {
            log.info("当前时间其他实例已经在持久化用户点赞相关操作");
            return; // todo 多实例部署时考虑负载均衡
        }
        log.info("当前时间当前实例获取到用户点赞相关操作持久化资格");


        try {
            // 获取对应的时间段内用户点赞操作缓存
            String key = ThumbRedisConstant.getThumbTmpKey(slice);
            Map<Object, Object> uidBidThuOpeMap = redisTemplate.opsForHash().entries(key);
            if (CollectionUtil.isEmpty(uidBidThuOpeMap)) {
                log.info("用户点赞相关操作缓存为 NULL");
                return;
            }

            log.info("用户点赞相关操作缓存存在数据 记录时间戳为:{}", slice);
            // 是否存在需要删除的情况
            boolean haveDelete = false;
            // 存储待删除的点赞记录的 uid-bid
            List<Long> duidlist = null, dbidlist = null;

            // 获取对应用户点赞情况(包含用户取消点赞)
            List<Thumb> thumbList = new ArrayList<>(uidBidThuOpeMap.size());
            // 点赞量改变
            Map<Long, Integer> bidThuChaCouMap = new HashMap<>();

            for (Map.Entry<Object, Object> uidBidThuOpeEnt : uidBidThuOpeMap.entrySet()) {
                String[] uidBidStrs = ((String) uidBidThuOpeEnt.getKey()).split(StrPool.COLON);

                Long uid = Long.parseLong(uidBidStrs[0]), bid = Long.parseLong(uidBidStrs[1]);
                Integer ope = Integer.parseInt((String) uidBidThuOpeEnt.getValue());

                if (ThumbOperationEnum.INCR.getValue() == ope) {
                    Thumb thumb = new Thumb();
                    thumb.setId(IdUtil.getSnowflakeNextId());
                    thumb.setUserId(uid);
                    thumb.setItemId(bid);
                    thumbList.add(thumb);
                } else if (ThumbOperationEnum.DECR.getValue() == ope) {
                    haveDelete = true;
                    // 初始化待删除 uid bid list
                    if (duidlist == null) {
                        dbidlist = new ArrayList<>();
                        duidlist = new ArrayList<>();
                    }

                    duidlist.add(uid);
                    dbidlist.add(bid);
                } else {
                    if (ThumbOperationEnum.NON.getValue() != ope) {
                        log.error("数据异常 {}:{}:{}", uid, bid, ope);
                    }
                    // 当前操作数无效
                    continue;
                }

                bidThuChaCouMap.put(bid, bidThuChaCouMap.getOrDefault(bid, 0) + ope);
            }

            if (CollectionUtil.isNotEmpty(thumbList)) {
                // 执行更新 SQL
                thumbMapper.addBatchThumb(thumbList); // 批量插入 // todo 批量的大小要进行限制
            }

            if (haveDelete) {
                thumbMapper.batchDeleteByUidBids(duidlist, dbidlist); // 批量删除 // todo 批量删除的大小要进行限制
            }

            if (CollectionUtil.isNotEmpty(bidThuChaCouMap)) {
                thumbCountMapper.batchUpdateCount(bidThuChaCouMap); // 批量修改点赞数
            }

            // 异步删除对应的用户点赞操作缓存
            exe.submit(() -> redisTemplate.delete(key));
        } finally {
            lock.unlock();
        }
    }
}
