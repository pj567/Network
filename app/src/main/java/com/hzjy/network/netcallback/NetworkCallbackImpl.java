package com.hzjy.network.netcallback;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.hzjy.network.core.MethodManager;
import com.hzjy.network.core.NetType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * pj567
 * 2019/9/9
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
    private Map<Object, List<MethodManager>> networkMap;
    private NetType netType;

    public NetworkCallbackImpl() {
        //初始化
        netType = NetType.NONE;
        networkMap = new HashMap<>();
    }

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        Log.e("onLost", "断开连接");
        if (netType != NetType.NONE) {
            netType = NetType.NONE;
            post(NetType.NONE);
        }
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                if (netType != NetType.WIFI) {
                    netType = NetType.WIFI;
                    Log.e("onCapabilitiesChanged", "WIFI");
                    post(NetType.WIFI);
                }
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                if (netType != NetType.MOBILE) {
                    netType = NetType.MOBILE;
                    post(NetType.MOBILE);
                }
            } else {
                if (netType != NetType.AUTO) {
                    netType = NetType.AUTO;
                    post(NetType.AUTO);
                }
            }
        }
    }

    private void post(NetType netType) {
        Set<Object> set = networkMap.keySet();
        for (Object observer : set) {
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

    private void invoke(MethodManager methodManager, Object observer, NetType netType) {
        try {
            methodManager.getMethod().invoke(observer, netType);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册
     *
     * @param observer 观察者(Activity/Fragment)
     */
    public void registerObserver(Object observer) {
        List<MethodManager> methodManagers = networkMap.get(observer);
        if (methodManagers == null) {
            methodManagers = findAnnotationMethods(observer);
            networkMap.put(observer, methodManagers);
        }
    }

    private List<MethodManager> findAnnotationMethods(Object observer) {
        List<MethodManager> methodList = new ArrayList<>();
        Class<?> clazz = observer.getClass();
        while (clazz != null) {
            String className = clazz.getName();
            if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("android.")) {
                break;
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                //获取方法注解
                com.hzjy.network.core.Network network = method.getAnnotation(com.hzjy.network.core.Network.class);
                if (network == null) {
                    continue;
                }
                //方法参数校验
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new RuntimeException(method.getName() + "有且只有一个参数与");
                }
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
    }
}

