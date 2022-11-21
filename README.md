# A project for learning MongoDB

## 网站
> `MongoDB` 4.4版本之后，`MongoDB` 数据库工具现在与 `MongoDB` 服务，分开发布，需要单独安装
* [官方网站](https://www.mongodb.com)
* [服务下载地址](https://www.mongodb.com/try/download/community)
* [数据库工具下载地址](https://www.mongodb.com/try/download/database-tools)

## 数据准备
解压 `dump.7z`，使用数据库工具的 `mongorestore` 备份工具，导入 10,0000 条订单 `db_charge.order` 数据。
```shell
mongorestore -h 127.0.0.1:27017 dump
```
