
# Zuul动态反向代理Demo

## 启动
```bash
mvn spring-boot:run
```

## 动态路由的底层存储
动态路由的存储中间件有很多，可以选择单机的ConcurrentHashMap，可以选择分布式存储中间件，如etcd、consul、zk和redis等

都可以直接通过继承 `AbstractDynamicRouter` 实现对应方法来实现动态路由

### ConcurrentHashMap
实现可见 `ConcurrentHashMapDynamicRouter`

### etcd
1. 使用etcd的实现，需要先安装etcd
```bash
# 下面的mac的安装方式，其他的也可以参考文档 https://github.com/etcd-io/etcd/releases
brew install etcd
```

2. 启动etcd

```bash
etcd --advertise-client-urls 'http://0.0.0.0:2379' \
     --listen-client-urls 'http://0.0.0.0:2379'
```

```bash
etcdctl put foo bar
etcdctl get foo 
```

3. 启动动态代理

```bash
mvn spring-boot:run -Drouter.type=etcd
```

## route api
可以通过接口操作路由，达到动态代理路由的功能

- 增加route
```bash
curl -X POST http://localhost/api/route \
     -H "Content-Type: application/json" \
     -d @route.json
```

- 删除route
```bash
curl -X DELETE http://localhost/api/route \
     -H "Content-Type: application/json" \
     -d @route.json
```


- 查看所有route信息
```bash
curl -X GET http://localhost/api/route 
curl -X GET http://localhost:81/api/route 
```

- 删除所有route信息
```bash
curl -X DELETE http://localhost/api/route/clearAll
```
