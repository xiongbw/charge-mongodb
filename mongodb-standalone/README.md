# mongodb-standalone

## 模块介绍

主要是使用 `Spring Boot` 与 **单实例** `MongoDB(4.4.16)` 相结合的一些实例。
1. `MongoTemplate` 和 `MongoRepository` 的使用；
2. 与 `MongoDB` 的新特性——事务结合使用；
3. `MongoDB` 与 `MySQL` 结合于同一事务管理。

## 搭建（单实例）复制集

> Q：该模块是与 `MongoDB` 单实例的结合，为什么还要搭建复制集？
>
> A：本项目使用的 `MongoDB` 版本为 `4.4`，该模块中有与 `MongoDB` 事务特性结合使用；
> 而 `MongoDB` 从 `4.0` 版本开始**支持复制集部署模式下的事务** ，所以需要搭建一个**单实例的复制集**（很简单，不搭建会报错😂

### 搭建单实例

安装过程可见 [Windows、macOS 和 Linux 图文安装](https://mp.weixin.qq.com/s/yaPbuUqMF_4oFkaoCqilJQ)

接下来就是将它变成一个只有一个节点的复制集。

### 修改配置文件
新增配置项
```properties
# 设置绑定的复制集名字
replSet = standalone
```

### 设置复制集
1. 进入 `MongoDB` 服务
2. 配置复制集
```javascript
rs.initiate({
    _id: "standalone",
    "members": [
        {
            "_id": 0,
            "host": "localhost:27017"
        }
    ]
})
```

你的 `mongo` 命令行会先从 `>` 变成 `standalone:SECONDARY>`，再变成 `standalone:PRIMARY>`

1. 此时我们的节点已经进入了复制集的状态，我们的节点不再是个单独的节点，是个复制集的节点
2. 很显然，standalone 就是我们复制集的名称
3. SECONDARY 就是从节点的意思，刚开始的状态肯定是先从 SECONDARY 开始，然后再进到主节点
4. 这个时候再按下回车，我们的命令行标识就变成了 `standalone:PRIMARY>`
5. 此时，该节点就在刚刚已经由**从节点**角色变成了**主节点**角色。

## 数据准备
解压 `resource` 目录下的 `dump.7z`，使用[数据库工具](https://www.mongodb.com/try/download/database-tools)的 `mongorestore` 备份工具，导入 10,0000 条订单 `db_charge.order` 数据。
```shell
mongorestore -h 127.0.0.1:27017 dump
```