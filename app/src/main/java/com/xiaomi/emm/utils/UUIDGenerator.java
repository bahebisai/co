package com.xiaomi.emm.utils;

/**
 * Created by Administrator on 2017/5/31.
 */

import java.util.UUID;

/**
 * UUID 生成器
 */
public class UUIDGenerator {
    private UUIDGenerator() {
    }
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }
}
