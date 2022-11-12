package com.passer.demo.zuuldynamicproxy.service.impl;

import com.passer.demo.zuuldynamicproxy.service.AbstractDynamicRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author passer
 * @time 2022/11/12 17:26
 */
public class ConcurrentHashMapDynamicRouter extends AbstractDynamicRouter {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentHashMapDynamicRouter.class);

    private static final Map<String, ZuulProperties.ZuulRoute> routers = new ConcurrentHashMap<>();

    @Override
    public Map<String, ZuulProperties.ZuulRoute> getAllAvailableRoute() {
        return routers;
    }

    @Override
    public void addRoute(String path, ZuulProperties.ZuulRoute route) {
        log.info("addRoute, path: {}, route: {}", path, route);
        ZuulProperties.ZuulRoute oldRoute = routers.put(path, route);
        if (null != oldRoute) {
            log.info("addRoute, replace old route: {}", oldRoute);
        }
        super.onChange();
    }

    @Override
    public void deleteRoute(String path) {
        ZuulProperties.ZuulRoute route = routers.remove(path);
        if (null != route) {
            log.info("deleteRoute, path: {}, route: {}", path, route);
        }
        super.onChange();
    }

    @Override
    public void clearAll() {
        routers.clear();
        super.onChange();
    }
}
