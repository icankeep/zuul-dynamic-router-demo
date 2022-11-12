package com.passer.demo.zuuldynamicproxy.controller;

import com.passer.demo.zuuldynamicproxy.domain.PathRouteDto;
import com.passer.demo.zuuldynamicproxy.service.DynamicRouter;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author passer
 * @time 2022/11/12 18:12
 */
@RestController
@RequestMapping("/api/route")
public class RouteController {

    @Resource
    private DynamicRouter dynamicRouter;

    @PostMapping
    public void addRoute(@RequestBody PathRouteDto pathRouteDto) {
        dynamicRouter.addRoute(pathRouteDto.getPath(), pathRouteDto.getRoute());
    }

    @GetMapping
    public Map<String, ZuulProperties.ZuulRoute> getAllRoutes() {
        return dynamicRouter.getAllAvailableRoute();
    }

    @DeleteMapping
    public void deleteRoute(@RequestBody PathRouteDto pathRouteDto) {
        dynamicRouter.deleteRoute(pathRouteDto.getPath());
    }

    @DeleteMapping("/clearAll")
    public void deleteAllRoutes() {
        dynamicRouter.clearAll();
    }
}
