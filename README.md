1. 什么是宙斯（Zeus）
宙斯是阿里巴巴开源的一款分布式Hadoop作业调度平台，支持多机器的水平扩展。Zeus是一款完全分布式的调度系统，，支持多机器的水平扩展，一台机器为一个节点，由master节点分发任务至不同的worker,实现任务的分布式调度。目前支持的任务类型主要由hive脚本和shell脚本。Zeus不仅仅可以执行独立任务调度，还支持任务之间依赖调度。这就使得zeus完全不同于传统的任务调度系统中，任务只能单个任务之间调度，这也是zeus的设计中一大亮点。

添加测试dev.properties & 生产prod.properties 编译 mvn clean package -Pprod
2. Zeus的架构设计
软件架构设计 
![Alt text](https://github.com/jimmy401/zeus_nb/tree/master/Screenshots/ruanjianjiagou.png)
设定worker并发运行数，添加running queue dump监控
