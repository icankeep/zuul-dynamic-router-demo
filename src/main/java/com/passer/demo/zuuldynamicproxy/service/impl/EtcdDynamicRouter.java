package com.passer.demo.zuuldynamicproxy.service.impl;

import com.google.protobuf.ByteString;
import com.ibm.etcd.api.Event;
import com.ibm.etcd.api.KeyValue;
import com.ibm.etcd.api.RangeResponse;
import com.ibm.etcd.client.EtcdClient;
import com.ibm.etcd.client.KvStoreClient;
import com.ibm.etcd.client.kv.KvClient;
import com.ibm.etcd.client.kv.WatchUpdate;
import com.passer.demo.zuuldynamicproxy.service.AbstractDynamicRouter;
import com.passer.demo.zuuldynamicproxy.utils.GsonUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author passer
 * @time 2022/11/12 22:18
 */
public class EtcdDynamicRouter extends AbstractDynamicRouter {

    private static final Logger log = LoggerFactory.getLogger(EtcdDynamicRouter.class);

    private static final String DEFAULT_PREFIX = "/default";

    private static final Map<String, ZuulProperties.ZuulRoute> CACHE = new ConcurrentHashMap<>();

    private final KvStoreClient kvStoreClient;

    private final KvClient kvClient;

    private final String prefix;

    private KvClient.Watch watcher;

    public EtcdDynamicRouter() {
        this("localhost", 2379, null);
    }

    public EtcdDynamicRouter(String host, Integer port, String prefix) {
        super();

        this.kvStoreClient = EtcdClient.forEndpoint(host, port).withPlainText().build();
        this.kvClient = this.kvStoreClient.getKvClient();
        if (prefix == null) {
            this.prefix = DEFAULT_PREFIX;
        } else {
            this.prefix = prefix;
        }
        CACHE.putAll(getAllAvailableRouteFromEtcd());

        addChangeCallback();

        addShutdownHook();
    }

    private void addChangeCallback() {
        this.watcher = kvClient.watch(ByteString.copyFromUtf8(this.prefix)).asPrefix().start(new StreamObserver<WatchUpdate>() {
            @Override
            public void onNext(WatchUpdate value) {
                final List<Event> events = value.getEvents();
                if (CollectionUtils.isEmpty(events)) {
                    return;
                }

                for (Event event : events) {
                    final ByteString key = event.getKv().getKey();
                    if (Event.EventType.DELETE == event.getType()) {
                        final String path = getPathFromByteStringKey(key);
                        log.info("[watch event] delete kv, key: {}", key);
                        CACHE.remove(path);
                        onChange();
                    }
                    if (Event.EventType.PUT == event.getType()) {
                        final String path = getPathFromByteStringKey(key);
                        final ZuulProperties.ZuulRoute route = GsonUtils.fromJson(event.getKv().getValue().toStringUtf8(), ZuulProperties.ZuulRoute.class);
                        log.info("[watch event] put kv, key: {}, value: {}", key, route);
                        CACHE.put(path, route);
                        onChange();
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("[watch event] etcd watch error", t);
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (kvStoreClient != null) {
                try {
                    kvStoreClient.close();
                    log.info("close kvStoreClient successfully");
                } catch (IOException e) {
                    log.error("close kvStoreClient error", e);
                }
            }

            if (watcher != null) {
                watcher.close();
                log.info("close watcher successfully");
            }
        }));
    }

    @Override
    public Map<String, ZuulProperties.ZuulRoute> getAllAvailableRoute() {
        return CACHE;
    }

    @Override
    public void addRoute(String path, ZuulProperties.ZuulRoute route) {
        final ByteString keyByteString = getByteStringKeyFromPath(path);
        final ByteString valueByteString = ByteString.copyFromUtf8(GsonUtils.toJson(route));
        this.kvClient.put(keyByteString, valueByteString).sync();
    }

    @Override
    public void deleteRoute(String path) {
        final ByteString keyByteString = getByteStringKeyFromPath(path);
        this.kvClient.delete(keyByteString).sync();
    }

    @Override
    public void clearAll() {
        final ByteString keyByteString = ByteString.copyFromUtf8(this.prefix);
        this.kvClient.delete(keyByteString).asPrefix().sync();
    }

    @Override
    public void onChange() {
        super.onChange();
    }

    public Map<String, ZuulProperties.ZuulRoute> getAllAvailableRouteFromEtcd() {
        final Map<String, ZuulProperties.ZuulRoute> routes = new HashMap<>();
        final RangeResponse resp = this.kvClient.get(ByteString.copyFromUtf8(this.prefix)).asPrefix().sync();
        final List<KeyValue> kvsList = resp.getKvsList();
        for (KeyValue keyValue : kvsList) {
            final String path = getPathFromByteStringKey(keyValue.getKey());
            final ZuulProperties.ZuulRoute route =
                    GsonUtils.fromJson(keyValue.getValue().toStringUtf8(), ZuulProperties.ZuulRoute.class);
            routes.put(path, route);
        }
        return routes;
    }

    private String getPathFromByteStringKey(ByteString key) {
        return key.toStringUtf8().substring(this.prefix.length());
    }

    private ByteString getByteStringKeyFromPath(String path) {
        return ByteString.copyFromUtf8(this.prefix + path);
    }
}
