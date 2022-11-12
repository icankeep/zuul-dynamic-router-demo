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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author passer
 * @time 2022/11/12 22:18
 */
public class EtcdDynamicRouter extends AbstractDynamicRouter {

    private static final Logger log = LoggerFactory.getLogger(EtcdDynamicRouter.class);

    private static final String DEFAULT_PREFIX= "/default";

    private KvClient kvClient;

    private String prefix;

    public EtcdDynamicRouter() {
        this("localhost", 2379, null);
    }

    public EtcdDynamicRouter(String host, Integer port, String prefix) {
        super();

        final KvStoreClient client = EtcdClient.forEndpoint(host, port).withPlainText().build();
        this.kvClient = client.getKvClient();
        if (prefix == null) {
            this.prefix = DEFAULT_PREFIX;
        } else {
            this.prefix = prefix;
        }

        kvClient.watch(ByteString.copyFromUtf8(this.prefix)).asPrefix().start(new StreamObserver<WatchUpdate>() {
            @Override
            public void onNext(WatchUpdate value) {
                final List<Event> events = value.getEvents();
                if (CollectionUtils.isEmpty(events)) {
                    return;
                }

                for (Event event : events) {
                    // todo add kv cache
                    if (Event.EventType.DELETE == event.getType()) {
                        log.info("[watch event] delete kv, key: {}", event.getKv().getKey().toStringUtf8());
                        onChange();
                    }
                    if (Event.EventType.PUT == event.getType()) {
                        log.info("[watch event] put kv, key: {}", event.getKv().getKey().toStringUtf8());
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

    @Override
    public Map<String, ZuulProperties.ZuulRoute> getAllAvailableRoute() {
        final Map<String, ZuulProperties.ZuulRoute> routes = new HashMap<>();
        final RangeResponse resp = this.kvClient.get(ByteString.copyFromUtf8(this.prefix)).asPrefix().sync();
        final List<KeyValue> kvsList = resp.getKvsList();
        for (KeyValue keyValue : kvsList) {
            final String path = keyValue.getKey().toStringUtf8().substring(this.prefix.length());
            final ZuulProperties.ZuulRoute route =
                    GsonUtils.fromJson(keyValue.getValue().toStringUtf8(), ZuulProperties.ZuulRoute.class);
            routes.put(path, route);
        }
        return routes;
    }

    @Override
    public void addRoute(String path, ZuulProperties.ZuulRoute route) {
        final ByteString keyByteString = ByteString.copyFromUtf8(this.prefix + path);
        final ByteString valueByteString = ByteString.copyFromUtf8(GsonUtils.toJson(route));
        this.kvClient.put(keyByteString, valueByteString).sync();
    }

    @Override
    public void deleteRoute(String path) {
        final ByteString keyByteString = ByteString.copyFromUtf8(this.prefix + path);
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
}
