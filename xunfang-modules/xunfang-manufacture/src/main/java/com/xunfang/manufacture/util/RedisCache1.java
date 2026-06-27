package com.xunfang.manufacture.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类
 *
 * @author xunfang
 */
@Component
public class RedisCache1 {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache1.class);

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    @SuppressWarnings("rawtypes")
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== common ====================

    @SuppressWarnings("unchecked")
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            logger.error("[ERROR]exception when expire cache", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("[ERROR]exception when hasKey cache", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
                logger.debug("[DEL]\tKey=" + key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
                logger.debug("[DEL]\tKey=" + key);
            }
        }
    }

    // ==================== String ====================

    public Object get(String key) {
        if (key == null) {
            return null;
        }
        Object o = redisTemplate.opsForValue().get(key);
        logger.debug("[get]\tKey=" + key + "\tValue=" + o);
        return o;
    }

    @SuppressWarnings("unchecked")
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            logger.debug("[set]\tKey=" + key + "\tValue=" + value);
            return true;
        } catch (Exception e) {
            logger.error("[ERROR]exception when set cache", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
                logger.debug("[set]\tKey=" + key + "\tValue=" + value + "\tTime=" + time);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("[ERROR]exception when set cache time", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @SuppressWarnings("unchecked")
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ==================== Hash ====================

    @SuppressWarnings("unchecked")
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    @SuppressWarnings("unchecked")
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @SuppressWarnings("unchecked")
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            logger.error("[ERROR]exception when hmset cache", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("[ERROR]exception when hmset cache time", e);
            return false;
        }
    }
}
