package com.passer.demo.zuuldynamicproxy.service;

import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.Map;

/**
 * @author passer
 * @time 2022/11/12 17:23
 */
public interface DynamicRouter {
    Map<String, ZuulProperties.ZuulRoute> getAllAvailableRoute();

    void addRoute(String path, ZuulProperties.ZuulRoute route);

    void deleteRoute(String path);

    void clearAll();

    default void registerRouterLocator(RefreshableRouteLocator locator) {
    }
}
