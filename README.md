# A project for learning MongoDB

## 网站
> `MongoDB` 4.4版本之后，`MongoDB` 数据库工具现在与 `MongoDB` 服务，分开发布，需要单独安装
* [官方网站](https://www.mongodb.com)
* [服务下载地址](https://www.mongodb.com/try/download/community)
* [数据库工具下载地址](https://www.mongodb.com/try/download/database-tools)

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

## 数据准备
解压 `dump.7z`，使用数据库工具的 `mongorestore` 备份工具，导入 10,0000 条订单 `db_charge.order` 数据。
```shell
mongorestore -h 127.0.0.1:27017 dump
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
