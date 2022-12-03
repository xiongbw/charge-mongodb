# mongodb-single
> `MongoDB` 3.0 版本开始引入 `WiredTiger` 存储引擎之后开始支持事务
>
> `MongoDB` 3.6 之前的版本只能支持单文档的事务
>
> `MongoDB` 4.0 版本开始支持复制集部署模式下的事务
>
> `MongoDB` 4.2 版本开始支持分片集群中的事务。

## 模块介绍

主要是使用 `Spring Boot` 与 **单实例** `MongoDB(4.4.16)` 相结合的一些实例。
1. `MongoTemplate` 和 `MongoRepository` 的使用；
2. 与 `MongoDB` 事务结合。

## 安装
1. [官方网站](https://www.mongodb.com/try/download/community)
2. 解压压缩包
```shell
tar -xvf mongodb-macos-x86_64-4.4.16.tgz
```

## 启动
> - port 端口（默认：27017）
> - dbpath 设置数据志存放目录
> - logpath 设置日志存放目录
> - fork 以守护进程的方式在后台运行

### 方式一
直接通过命令启动
```shell
./bin/mongod --logpath=/Users/bowy/workspace/mongodb/log/mongo.log --dbpath=/Users/bowy/workspace/mongodb/data --fork
```

### 方式二（推荐）
通过配置文件方式启动
1. 创建配置文件 `vim mongod.conf`
```
# 设置绑定 IP
bind_ip = 127.0.0.1

# --dbpath 设置数据志存放目录
dbpath = /Users/bowy/workspace/mongodb/data

# --logpath 设置日志存放目录
logpath = /Users/bowy/workspace/mongodb/log/mongo.log

# --fork 后台运行
fork = true
```
2. 启动
```shell
./bin/mongod --config mongod.conf
```

## 进入 `MongoDB`
执行命令 `mongo`，默认进入本地 27017 端口的 `MongoDB` 实例。
```shell
./bin/mongo
```

## 停止服务
使用数据库命令关闭
1. 进入 `MongoDB` 服务
```shell
mongo
```
2. 进入 `admin` 库
```shell
use admin
```
3. 停止服务
```shell
db.shutdownServer();
```

## 概念介绍
### 复制集
复制集由一组 `MongoDB` 实例组成，包含一个 Primary 节点和多个 Secondary 节点

`MongoDB Driver`（客户端）的所有数据都写入 Primary，Secondary 从 Primary 同步写入的数据，以保持复制集内所有成员存储相同的数据集，提供数据的高可用。
![img.png](https://www.runoob.com/wp-content/uploads/2013/12/replication.png)

### 分片集群
数据节点基本上以复制集为单位，每一个分片必须是一个复制集，因为不允许在单个分片里面有单点故障，要保证复制集里面的每一个节点有一个互为高可用的角色存在；分片和分片之间的数据是不重复的。

## 搭建（单实例）复制集

> Q：该模块是与 `MongoDB` 单实例的结合，为什么还要搭建集群？
>
> A：本项目使用的 `MongoDB` 版本为 `4.4`
> 
> 其中有与 `MongoDB` 事务特性结合使用，所以需要搭建一个单实例的复制集（很简单，不搭建会报错😂

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