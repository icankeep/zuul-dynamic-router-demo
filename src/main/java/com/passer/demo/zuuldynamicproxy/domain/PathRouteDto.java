package com.passer.demo.zuuldynamicproxy.domain;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

/**
 * @author passer
 * @time 2022/11/12 19:25
 */
public class PathRouteDto {
    private String path;

    private ZuulProperties.ZuulRoute route;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ZuulProperties.ZuulRoute getRoute() {
        return route;
    }

    public void setRoute(ZuulProperties.ZuulRoute route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return "PathRouteDto{" +
                "path='" + path + '\'' +
                ", route=" + route +
                '}';
    }
}
