# mongodb-single
> `MongoDB` 3.0 版本开始引入 `WiredTiger` 存储引擎之后开始支持事务；
>
> `MongoDB` 3.6 之前的版本只能支持单文档的事务；
>
> `MongoDB` 4.0 版本开始支持复制集部署模式下的事务；
>
> `MongoDB` 4.2 版本开始支持分片集群中的事务。

## 模块介绍

主要是使用 `Spring Boot` 与 **单实例** `MongoDB(4.4.16)` 相结合的一些实例。
1. `MongoTemplate` 和 `MongoRepository` 的使用；
2. 与 `MongoDB` 事务结合；
3. `MongoDB` 与 `MySQL` 结合于同一事务管理。

## 搭建（单实例）复制集

> Q：该模块是与 `MongoDB` 单实例的结合，为什么还要搭建复制集？
>
> A：本项目使用的 `MongoDB` 版本为 `4.4`
> 
> 其中有与 `MongoDB` 事务特性结合使用，所以需要搭建一个**单实例**的复制集（很简单，不搭建会报错😂

### 修改配置文件
新增配置项
```
# 设置绑定的复制集名字
replSet = singleRepl
```

### 设置复制集
1. 进入 `MongoDB` 服务
2. 配置复制集
```mongodb-json-query
rs.initiate({
    _id: "singleRepl",
    "members": [
        {
            "_id": 0,
            "host": "localhost:27017"
        }
    ]
})
```

你的 `mongo` 命令行会先从 `>` 变成 `singleRepl:SECONDARY>`，再变成 `singleRepl:PRIMARY>`

1. 此时我们的节点已经进入了复制集的状态，我们的节点不再是个单独的节点，是个复制集的节点 
2. 很显然，singleRepl 就是我们复制集的名称 
3. SECONDARY 就是从节点的意思，刚开始的状态肯定是先从 SECONDARY 开始，然后再进到主节点  
4. 这个时候再按下回车，我们的命令行标识就变成了 `singleRepl:PRIMARY>`
5. 此时，该节点就在刚刚已经由**从节点**角色变成了**主节点**角色。