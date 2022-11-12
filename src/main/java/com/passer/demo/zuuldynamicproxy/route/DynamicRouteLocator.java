package com.passer.demo.zuuldynamicproxy.route;

import com.passer.demo.zuuldynamicproxy.service.DynamicRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author passer
 * @time 2022/11/12 17:05
 */
public class DynamicRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator {

    private static final Logger log = LoggerFactory.getLogger(DynamicRouteLocator.class);

    private final ZuulProperties properties;
    private final DynamicRouter dynamicRouter;

    public DynamicRouteLocator(String servletPath, ZuulProperties properties, DynamicRouter dynamicRouter) {
        super(servletPath, properties);
        this.properties = properties;
        this.dynamicRouter = dynamicRouter;

        init();
    }

    public void init() {
        for (ZuulProperties.ZuulRoute route : this.properties.getRoutes().values()) {
            log.info("add default route, path: {}, route: {}", route.getPath(), route);
            this.dynamicRouter.addRoute(route.getPath(), route);
        }

        register();
    }

    public void register() {
        this.dynamicRouter.registerRouterLocator(this);
    }

    @Override
    public void refresh() {
        log.info("refresh routers...");
        doRefresh();
    }

    @Override
    protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
        return this.dynamicRouter.getAllAvailableRoute();
    }
}
