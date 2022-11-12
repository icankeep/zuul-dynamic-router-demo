package com.passer.demo.zuuldynamicproxy.service;

import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Resource;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author passer
 * @time 2022/11/12 19:32
 */
public abstract class AbstractDynamicRouter implements DynamicRouter, CallbackListener {
    private final Queue<RefreshableRouteLocator> locators = new LinkedBlockingQueue<>();

    @Resource
    private ApplicationEventPublisher publisher;

    @Override
    public void onChange() {
        for (RefreshableRouteLocator locator : locators) {
            RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(locator);
            publisher.publishEvent(routesRefreshedEvent);
        }
    }

    @Override
    public void registerRouterLocator(RefreshableRouteLocator locator) {
        locators.add(locator);
    }
}
