# mongodb-replica-set

`MongoDB` 原生就提供了一种复制（副本）集的概念，不需要任何第三方的支持就可以实现复制集的架构。

复制集的实现主要依赖于以下2个功能：

1. 数据写入时迅速将数据复制到其它节点上；
2. 在接受写入的节点发生故障时自动选举出一个新的替代节点。

## 模块介绍

主要是使用 `Spring Boot` 与 **复制集** `MongoDB(4.4.16)` 相结合的一些实例。

## 作用

> 主要意义：实现服务高可用。

在实现高可用的同时，在其他几个角度还提供了价值：

- **数据分发**：将数据从一个区域复制到另一个区域，减少另一个区域的读延迟；
  一家公司的数据比如在海外，那这数据可以同时复制到国内的服务器上，在国内如果需要访问的话，就不需要远程访问国外的服务器了，可以直接访问国内的服务器。有点类似于 `CDN`。
- **读写分离**：不同类型的压力分别在不同的节点上执行；
  一个复制集中，不同类型的读写可以分布到不同节点上去执行，起到对应用程序分压的目的。
- **异地容灾**：在数据中心故障时能快速切换到异地。
  通过数据在实时地同步到另外一个数据中心，在出现故障时迅速地切换到另外一个数据中心。

## 架构

`MongoDB` 复制集至少需要两个节点。其中一个是主节点，负责处理客户端请求，其余的都是从节点，负责复制主节点上的数据。

![replica-set-read-write-operations-primary](https://www.mongodb.com/docs/v4.4/images/replica-set-read-write-operations-primary.bakedsvg.svg)

## 原理

### 数据复制

当一个写（插入、更新或删除）操作到达主节点时，这个数据操作会作为一个日志项被记录下来，记录在 `oplog` 这个特别的集合里面 ，称为[操作日志](https://www.mongodb.com/docs/v4.4/core/replica-set-oplog/)，它保留所有修改存储在数据库中的数据的操作的滚动记录。

然后有一个线程监听这个 `oplog` 的变动，有变动时就会把日志读取到从节点，在从节点上进行“回放”，与主节点保持一致。

#### oplog

> `MongoDB` 中复制集节点间的数据同步就是靠 `oplog` 实现的。

`oplog` 本质其实就是一张表（`local` 库中的 `oplog.rs`），用来记录每次写操作对应的逻辑日志。它是一个固定大小的集合（ `capped collection`），当它达到最大大小时后面的新数据会自动覆盖前面最旧的数据。

`MongoDB` 复制集里写入一个文档时，需要做以下数据变更：

1. 将文档数据写入对应的集合
2. 更新集合的索引信息
3. 写入一条 `oplog` 日志记录

`MongoDB` 在写入数据时，会将上面3个操作放到一个的事务里，确保**原子性**。

`oplog` 中记录了有关写操作的操作时间、操作类型、以及操作的具体内容，几乎保留的每行实际数据的变更（在4.0及以后版本中，一个事务中涉及的多个文档，会写在一条 `oplog` 中）。

**`MongoDB` 主从之间实现数据复制的主要过程：**

1. `Primary` 节点把数据库更改记录到 `oplog` 中；
2. `Secondary` 节点把 `Primary` 节点上的 `oplog` 拉取到自己的回放队列中；
3. `Secondary` 节点读取到队列中的 `oplog`，批量回放（applyOps）到数据库中；
4. `Secondary` 节点再将队列中的 `oplog` 写入到自己的 `oplog.rs` 集合中。

这样源源不断的复制，实现了数据在数据库节点之间的一致。

[官方文档](https://www.mongodb.com/docs/v4.4/core/replica-set-oplog/)中有这么一句话：

> To facilitate replication, all replica set members send heartbeats (pings) to all other members. Any secondary member can import oplog entries from any other member.

翻译过来就是：为了便于复制，所有复制集节点都会向所有其它成员发送心跳（ping）。任何从节点成员都可以从任何其它成员导入 `oplog` 条目。

所以说 `Secondary` 节点也不一定非从 `Primary` 节点拉取 `oplog`，根据 `ping` 时间和其它节点复制状态的变化，`Secondary` 节点可能从其它的 `Secondary` 节点拉取。

从 4.4 版本开始，复制集协议最主要的更新就是 [Streaming Replication](https://www.mongodb.com/docs/v4.4/core/replica-set-sync/#streaming-replication)：从拉改成主动推。

### 选举

> `MongoDB` 充分实现高可用的机制：主节点倒下时，其中一个从节点会变成新的主节点。

在复制集里每两个节点之间，会有心跳的连接，默认2s发送（监听）一次，如果超过5次心跳没收到那就会认为该节点挂了。

- 如果主节点“失联”了，那么在从节点间就会进行选举，让一个从节点变成主节点。
- 如果从节点“失联”了，集群正常工作，不会产生选举动作。

选举成功的必要条件是大多数投票节点存活，是基于 `RAFT` 一致性算法实现。

复制集中最多可以有50个节点，但具有投票权的节点最多7个。

![replica-set-primary-with-two-secondaries](https://www.mongodb.com/docs/v4.4/images/replica-set-primary-with-two-secondaries.bakedsvg.svg)

#### 影响因素

1. 整个集群必须有大多数节点存活：共3个节点，那必须至少2个节点存活；共5个节点，至少3个节点存活。
2. 被选举为主节点必须满足：
    - 能够与多数节点建立连接（否则过不了多久又“失联”了）
    - 具有较新的 `oplog` 操作日志
    - 具有较高的优先级（如果有配置）

## 注意事项

1. 硬件：
    - 因为正常的复制集节点都有可能成为主节点，它们的地位其实是一样的，因此硬件配置上必须保持一致；
    - 为了保证节点不会同时宕机，各节点的硬件必须具有独立性。
2. 软件：
    - 复制集各节点软件版本必须保持一致，避免出现不可预知的问题。
3. 增加节点不会增加系统的写性能。

## 搭建复制集

搭建演示，这里直接在同一台机器上启3个 `MongoDB` 服务分别用不同的端口来达到复制集的效果。

### 配置环境变量

- Windows 系统需配置好 `MongoDB` 可执行文件的环境变量
- Linux 和 macOS 系统需为 `MongoDB` 配置好 `PATH` 变量

### 创建数据目录

`MongoDB` 在启动的时候会使用一个目录来存放数据文件，我们打算建立一个3节点的复制集，所以创建3个目录。

- Windows：`md C:\mongodb\replica_set\mongo_data0`、`md C:\mongodb\replica_set\mongo_data1`、`md C:\mongodb\replica_set\mongo_data2`
- Linux 和 macOS：`mkdir -p /Users/bowy/workspace/mongodb/replica_set/data{0,1,2}`

### 创建日志目录

- Windows：`md C:\mongodb\replica_set\log0`、`md C:\mongodb\replica_set\log1`、`md C:\mongodb\replica_set\log2`
- Linux 和 macOS：`mkdir -p /Users/bowy/workspace/mongodb/replica_set/log{0,1,2}`

### 准备配置文件

#### 配置说明

官方文档：https://www.mongodb.com/docs/v4.4/reference/configuration-options

| 配置项                  | 含义                                               | 备注                                                         |
| ----------------------- | -------------------------------------------------- | ------------------------------------------------------------ |
| systemLog.destination   | 将所有日志输出发送到的目标。 指定 syslog 或 file。 | 默认 syslog：MongoDB 会将所有日志输出发送到标准输出。<br />指定 file：必须指定 systemLog.path。 |
| systemLog.path          | 日志文件路径                                       | mongod 或 mongos 应将所有诊断日志记录信息发送到的日志文件的路径，而不是标准输出或主机的系统日志。<br />MongoDB 在指定路径创建日志文件。 |
| systemLog.logAppend     | 日志文件是否追加                                   | 默认 false：mongod 将备份现有日志并创建一个新文件。<br />指定 true ：mongos 或 mongod 在 mongos 或 mongod 实例重新启动时将新条目附加到现有日志文件的末尾。 |
| storage.dbPath          | 数据存储的目录                                     | Windows 默认：`\data\db`<br />Linux 和 macOS 默认：`/data/db` |
| net.bindIp              | 监听范围：可连接访问的 IP                          | 默认 localhost：只有本机可连接<br />127.0.0.1：只有本机可连接<br />0.0.0.0：所有均可连接<br />192.168.11.11,192.168.11.12：只有192.168.11.11,192.168.11.12 可连接 |
| net.port                | 实例端口                                           |                                                              |
| replication.replSetName | 该实例所属的副本集的名称                           | 没有该配置的话，说明它只是作为一个单节点的存在。             |
| processManagement.fork  | 后台运行                                           | 在 Windows 上不支持该配置项。<br />默认为 false；true 则启动后作为守护进程在后台运行。 |

Linux 和 macOS

```yaml
# /Users/bowy/workspace/mongodb/replica_set/mongod0/mongod.conf
systemLog:
  # 日志输出发送到的目标
  destination: file
  # 日志文件路径
  path: /Users/bowy/workspace/mongodb/replica_set/log0/mongod.log
  # 日志文件追加
  logAppend: true
storage:
  # 数据目录
  dbPath: /Users/bowy/workspace/mongodb/replica_set/data0
net:
  # 设置可连接访问的 IP
  bindIp: 127.0.0.1
  # 端口
  port: 37017
replication:
  # 复制集名称
  replSetName: rs0
processManagement:
  # 后台运行
  fork: true
```

Windows

```yaml
# C:\mongodb\replica_set\mongo_data0\mongod.conf
systemLog:
  destination: file
  # 日志文件路径
  path: C:\mongodb\replica_set\log0\mongod.log
  logAppend: true
storage:
  # 数据目录
  dbPath: C:\mongodb\replica_set\data0
net:
  bindIp: 127.0.0.1
  # 端口
  port: 37017
replication:
  replSetName: rs0
```

上面只是其中一个节点的配置文件，现在还要再准备另外两个节点配置文件，以 Linux 和 macOS 为例，在第一个配置文件的基础上改其中几个配置就行。

|       | systemLog.path                                               | storage.dbPath                                      | net.port  |
| ----- | ------------------------------------------------------------ | --------------------------------------------------- | --------- |
| 节点1 | /Users/bowy/workspace/mongodb/replica_set/**log0**/mongod.log | /Users/bowy/workspace/mongodb/replica_set/**data0** | 3701**7** |
| 节点2 | /Users/bowy/workspace/mongodb/replica_set/**log1**/mongod.log | /Users/bowy/workspace/mongodb/replica_set/**data1** | 3701**8** |
| 节点3 | /Users/bowy/workspace/mongodb/replica_set/**log2**/mongod.log | /Users/bowy/workspace/mongodb/replica_set/**data2** | 3701**9** |

### 启动服务

使用 `mongod --config` 或 `mongod -f` 指定配置文件的方式启动服务。

（因为 Windows 不支持 fork，所以命令需要在3个不同的窗口下执行，执行后窗口不能关闭，否则进程直接结束）

```shell
mongod -f /Users/bowy/workspace/mongodb/replica_set/mongod0/mongod.conf

mongod -f /Users/bowy/workspace/mongodb/replica_set/mongod1/mongod.conf

mongod -f /Users/bowy/workspace/mongodb/replica_set/mongod2/mongod.conf
```

## 配置复制集

上一步只是启动了3个独立的 `MongoDB` 而已，但它们之间还是互相不知道的，现在要做的就是让它们“绑”在一起。

进入其中一个节点（以节点1为例）

```shell
mongo --port 37017
```

### 方法一

直接进入其中一个节点，对其进行复制集初始化并添加复制集其它节点。

注意：实际添加的 HOSTNAME 要是另外两台的主机名，主机间必须要能通过这主机名正常通信，可以直接通过命令 `hostname` 查看主机名。

```
mongo --port 37017
> rs.initiate()
> rs.add("HOSTNAME:37018")
> rs.add("HOSTNAME:37019")
```

### 方法二（推荐）

```javascript
rs.initiate({
    _id: "rs0",
    members: [{
        _id: 0,
        host: "localhost:37017"
    },{
        _id: 1,
        host: "localhost:37018"
    },{
        _id: 2,
        host: "localhost:37019"
    }]
})
```

你的 `mongo` 命令行会先从 `>` 变成 `singleRepl:SECONDARY>`，再变成 `singleRepl:PRIMARY>`

1. 此时我们的节点已经进入了复制集的状态，我们的节点不再是个单独的节点，是个复制集的节点
2. 很显然，singleRepl 就是我们复制集的名称
3. SECONDARY 就是从节点的意思，刚开始的状态肯定是先从 SECONDARY 开始，然后再进到主节点
4. 这个时候再按下回车，我们的命令行标识就变成了 `singleRepl:PRIMARY>`
5. 此时，该节点就在刚刚已经由**从节点**角色变成了**主节点**角色。

我们可以通过命令 `rs.status()` 查看复制集状态：可以看到该复制集节点下的名称、健康状态和主从关系等信息。

### 节点常见选项

对复制集运行参数做一些常规调整，可通过 `rs.conf()` 命令可以查看复制集中各节点的配置。

| 参数       | 含义           | 说明                                                         |
| ---------- | -------------- | ------------------------------------------------------------ |
| votes      | 是否具有投票权 | 有则参与投票（默认会具有投票权）。                           |
| priority   | 优先级         | 优先级越高的节点越优先成为主节点，优先级为0的节点无法成为主节点。 |
| hidden     | 隐藏           | 复制数据，但对应用不可见。隐藏节点可以具有投票权，但优先级必须为0。<br />可能就是单纯想让一个节点做数据的额外备份，但又不想让它参与数据的操作为应用程序服务。 |
| slaveDelay | 延迟           | 复制 n 秒前的数据，保持与主节点的时间差，配置了 slaveDelay 的节点优先级必须为0。 |

### 调整配置

直接在命令行输入以下语句，就和写 JS 一样

```javascript
var conf = rs.conf()
// 将2号节点的延迟调整为 10 秒
conf.members[2].slaveDelay = 10
// 将2号节点的优先级调为 0
conf.members[2].priority = 0
// 应用配置
rs.reconfig(conf)
```
