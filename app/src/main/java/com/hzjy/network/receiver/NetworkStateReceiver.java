package com.hzjy.network.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hzjy.network.Constants;
import com.hzjy.network.NetworkListener;
import com.hzjy.network.core.MethodManager;
import com.hzjy.network.core.NetType;
import com.hzjy.network.core.Network;
import com.hzjy.network.utils.NetworkUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 网络状态改变接收-使用反射注解形式
 */

public class NetworkStateReceiver extends BroadcastReceiver {
    private NetType netType;
    private Map<Object, List<MethodManager>> networkMap;

    public NetworkStateReceiver() {
        //初始化网络连接状态
        netType = NetType.NONE;
        networkMap = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(Constants.ANDROID_NET_CHANGE_ACTION)) {
            if (netType != NetworkUtils.getNetType()) {
                netType = NetworkUtils.getNetType();
                post(netType);
                Log.e("onReceive",netType+"");
            }
        }
    }

    /**
     * 分发
     *
     * @param netType NetType
     */
    private void post(NetType netType) {
        //Activity集合
        Set<Object> set = networkMap.keySet();
        for (Object observer : set) {
            //所有注解的方法
            List<MethodManager> methodList = networkMap.get(observer);
            if (methodList != null) {
                for (MethodManager methodManager : methodList) {
                    //两者参数比较
                    if (methodManager.getType().isAssignableFrom(netType.getClass())) {
                        switch (methodManager.getNetType()) {
                            case AUTO:
                                invoke(methodManager, observer, netType);
                                break;
                            case WIFI:
                                if (netType == NetType.WIFI || netType == NetType.NONE) {
                                    invoke(methodManager, observer, netType);
                                }
                                break;
                            case MOBILE:
                                if (netType == NetType.MOBILE || netType == NetType.NONE) {
                                    invoke(methodManager, observer, netType);
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 在Activity中执行方法，参数为NetType
     *
     * @param methodManager
     * @param observer
     * @param netType
     */
    private void invoke(MethodManager methodManager, Object observer, NetType netType) {
        try {
            methodManager.getMethod().invoke(observer, netType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册
     * 获取Activity/Fragment中的注解方法并添加到networkList中
     *
     * @param observer 观察者(Activity/Fragment)
     */
    public void registerObserver(Object observer) {
        List<MethodManager> methodList = networkMap.get(observer);
        if (methodList == null) {
            methodList = findAnnotationMethod(observer);
            networkMap.put(observer, methodList);
        }
    }

    /**
     * 获取Activity/Fragment中的注解方法
     *
     * @param observer 观察者(Activity/Fragment)
     * @return
     */
    private List<MethodManager> findAnnotationMethod(Object observer) {
        List<MethodManager> methodList = new ArrayList<>();
        Class<?> clazz = observer.getClass();
        while (clazz != null) {
            String className = clazz.getName();
            if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("android.")) {
                break;
            }
            //获取Activity中的所有公有方法，包括其父类及其实现的接口的方法，因方法数过多，此处使用getDeclaredMethods()
            //Method[] methods = clazz.getMethods();
            //获取Activity中的所有方法，包括公共、保护、默认(包)访问和私有方法,当然也包括它所实现接口的方法，但不包括继承的方法
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                //获取方法的注解
                Network network = method.getAnnotation(Network.class);
                if (network == null) {
                    continue;
                }
                //方法参数校验
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new RuntimeException(method.getName() + "方法有且只有一个参数");
                }
                //参数类型校验
                String name = parameterTypes[0].getName();
                if (name.equals(NetType.class.getName())) {
                    MethodManager methodManager = new MethodManager(parameterTypes[0], network.netType(), method);
                    methodList.add(methodManager);
                }

            }
            clazz = clazz.getSuperclass();
        }
        return methodList;
    }

    /**
     * 解除注册
     *
     * @param observer 观察者(Activity/Fragment)
     */
    public void unRegisterObserver(Object observer) {
        if (!networkMap.isEmpty()) {
            networkMap.remove(observer);
        }
    }

    /**
     * 应用退出时调用
     */
    public void unRegisterAll() {
        if (!networkMap.isEmpty()) {
            networkMap.clear();
        }
        NetworkListener.getInstance().getContext().unregisterReceiver(this);
    }
}
