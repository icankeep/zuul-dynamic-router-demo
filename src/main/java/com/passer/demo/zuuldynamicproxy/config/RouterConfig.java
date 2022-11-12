package com.passer.demo.zuuldynamicproxy.config;

import com.passer.demo.zuuldynamicproxy.route.DynamicRouteLocator;
import com.passer.demo.zuuldynamicproxy.service.DynamicRouter;
import com.passer.demo.zuuldynamicproxy.service.impl.ConcurrentHashMapDynamicRouter;
import com.passer.demo.zuuldynamicproxy.service.impl.EtcdDynamicRouter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author passer
 * @time 2022/11/12 17:40
 */
@Configuration
public class RouterConfig {

    @Resource
    private ZuulProperties zuulProperties;

    @Resource
    private ServerProperties serverProperties;

    @Bean
    public DynamicRouter getDynamicRouter(@Value("${custom.router.type}") String routerType) {
        if ("etcd".equals(routerType)) {
            return new EtcdDynamicRouter();
        } else {
            return new ConcurrentHashMapDynamicRouter();
        }
    }

    @Bean
    public DynamicRouteLocator getDynamicRouteLocator(DynamicRouter dynamicRouter) {
        final String contextPath = serverProperties.getServlet().getContextPath();
        return new DynamicRouteLocator(contextPath, zuulProperties, dynamicRouter);
    }
}
