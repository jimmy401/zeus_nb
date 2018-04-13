#zeus_nb
zeus是阿里多年前开源的。介绍见下面。摘自网络。
阿里转向云计算平台后，项目停止了更新。不过很多中小型公司还在使用zeus作为大数据作业调度平台。本人也使用过zeus，觉得相比oozie，azkaban等任务流调度系统，
zeus操作起来更加方便。对被阿里抛弃的zeus，感觉很可惜。决心维护起该项目，为中小公司提供一个拿起就用，稳定成熟的调度系统。

##老版zeus存在以下几个问题：

    1.jdk支持的版本较低，支持jdk1.6。
    2.技术框架版本较低，有些模块已经过时。例如spring版本是2.5，hibernate版本3.2.7，netty版本3.2.4，前端技术采用的是GWT，这个已经严重过时。
    3.任务管理中没有完善的权限模块，缺少审核功能。
    4.没有完整的ETL功能支持，需要二次开发。
    5.没有配置页面，例如添加worker,没有可视化界面。还有其类似他不方便的地方，不一一列举。
    6.部分功能不合理。

我打算一步步的改善这些问题。

1. 什么是宙斯（Zeus）
宙斯是阿里巴巴开源的一款分布式Hadoop作业调度平台，支持多机器的水平扩展。Zeus是一款完全分布式的调度系统，，支持多机器的水平扩展，一台机器为一个节点，由master节点分发任务至不同的worker,实现任务的分布式调度。目前支持的任务类型主要由hive脚本和shell脚本。Zeus不仅仅可以执行独立任务调度，还支持任务之间依赖调度。这就使得zeus完全不同于传统的任务调度系统中，任务只能单个任务之间调度，这也是zeus的设计中一大亮点。
2. Zeus的架构设计
软件架构设计 
![Alt text](https://github.com/jimmy401/zeus_nb/raw/master/Screenshots/ruanjianjiagou.png)
3.集群架构设计
![Alt text](https://github.com/jimmy401/zeus_nb/raw/master/Screenshots/jishujiagou.png)


##版本日期 20180413
更新内容：
    1.jdk版本支持1.8
    2.用mybatis代替hibernate,mybatis用的是xml方式，还不是最新的注解方式。

下面是阅读源码，时做的笔记。先放在这里，后期分类整理。

##zeus安装步骤
        1.zeus_user 表添加用户 admin is_effective=1 user_type=1<br>
        2.yum install dos2unix<br>
        3.修改web工程下filter里的prod.properties里的数据库连接，hadoop,hive等配置。<br>
         hadoop.home=/usr/lib/hadoop 可以不改变，在对应linux服务器上建软连接的方式指向真正的hadoop lib路径。<br>
         hive.home=/usr/lib/hive配置参考这个方法。<br>
         zeus的每个节点都需要有这个配置。<br>
        4.修改resources下的env.sh中的java_home位置。<br>
        5.GWTEnvironment.java重修改文件id。重新部署. <br>
        6.zeus_group表里的最小的id所在的作为调度中心的根目录。<br>
        7.zeus_host_group必须初始化，zeus_host_relation也许要初始化。<br>
        8./data/applogs/zeus 下的日志，zeus.log是master和worker的一般性日志，socket.log是master和worker的通信日志。<br>
        9.从前端添加任务开始，调用链是RpcFilter-->FilterJobServiceImpl.createJob-->JobServiceImpl.createJob--   <br>  >PermissionGroupManager.createJob--<br>
        ScheduleGroupManager.createJob-->MysqlGroupManager.createJob--<br>
        10.zeus的工作路径是/tmp/zeus/yyyy-MM-dd，每天一个。<br>
        11.zeus选择worker节点的策略是在hostgrouopid中robin算法轮询选出一个host,当这个host有心跳，内存使用率小于zeus.maxMemRate，cpu负载小于      zeus.cpuLoadPerCore，<br>
        且这个主机正在运行的任务数量小于等于一定数量，才会选中作为worker运行任务。如果不满足条件的话，隔zeus.scanRate毫秒，再重做一个选择。<br>
        12.任务执行的hostGroupId默认是拿zeus.defaultWorkerGroup.id<br>
        13.env.sh需要配置环境信息<br>
        14.超级管理员是biadmin,部署zeus的节点上，必须有biadmin这个用户。部署zeus程序时候，也必须用biadmin这个帐号。<br>
        biadmin帐号必须有/tmp/zeus,/data/applogs/,/data/zeus/job_dir/下的增删改权限。<br>
        biadmin 必须可以sudo -u biadmin sh 执行命令。<br>
        15.zeus的选择worker节点的时候，如果第一个节点的任务数，负载没有超过指定阈值的话，都会选择第一个节点。可以改造，成公平策略。<br>
        16.zeus没有补跑action的原因是 Master里有清理scheduler的模块，小于当前时间往前追15分钟的actionId会被清理掉。<br>
        17.手动触发后，在运行日志中可以看到两个实例，一条是手动触发的，如果执行成功后，状态一直保持成running，而原来的实例会变成success。<br>
        18.每一个小时，会进行漏跑检测。<br>
        19.启动后，每60秒钟扫描一次zeus_lock表，尝试更新记录，谁占有记录，谁就是master.<br>

##志同道合的朋友可以联系我
邮箱 dufu0401@126.com 
我是拼命的诗人
