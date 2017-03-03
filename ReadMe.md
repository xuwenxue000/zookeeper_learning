官方网站

https://zookeeper.apache.org/

目前最新稳定版本3.4.9

下载后修改conf/zoo_sample.cfg为zoo.cfg
然后命令行启动 bin/zkServer.sh start

启动成功

关闭命令:
bin/zkServer.sh stop

zkCli是简易客户端
zkCleanup 清理数据
zkEnv 修改环境变量

连接远程zookeeper
zkCli.sh -server ip:port

create [-s] [-e] path data acl

	-s是持久节点
	-e是临时节点
	默认是持久节点
	path是节点路径
	data是节点数据
	acl是做权限控制的
	,默认不控制
	




