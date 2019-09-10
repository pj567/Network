package com.hzjy.network.core;

import java.lang.reflect.Method;

/**
 * pj567
 * 2019/9/9
 */

public class MethodManager {
    private Method method;
    private NetType netType;
    private Class<?> type;

    public MethodManager(Class<?> type, NetType netType, Method method) {
        this.type = type;
        this.netType = netType;
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public NetType getNetType() {
        return this.netType;
    }

    public Class<?> getType() {
        return this.type;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setNetType(NetType netType) {
        this.netType = netType;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
}
