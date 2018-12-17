zeus_nb
==
为什么叫zeus_nb new born的意思，新生。
zeus是阿里多年前开源的。介绍见下面。摘自网络。
阿里转向云计算平台后，项目停止了更新。不过很多中小型公司还在使用zeus作为大数据作业调度平台。本人也使用过zeus，觉得相比oozie，azkaban等任务流调度系统，
zeus操作起来更加方便。对被阿里抛弃的zeus，感觉很可惜。决心维护起该项目，为中小公司提供一个拿起就用，稳定成熟的调度系统。

老版zeus存在以下几个问题：
---

    1.jdk支持的版本较低，支持jdk1.6。
    2.技术框架版本较低，有些模块已经过时。例如spring版本是2.5，hibernate版本3.2.7，netty版本3.2.4，前端技术采用的是GWT，这个已经严重过时。
    3.任务管理中没有完善的权限模块，缺少审核功能。
    4.没有完整的ETL功能支持，需要二次开发。
    5.没有配置页面，例如添加worker,没有可视化界面。还有其类似他不方便的地方，不一一列举。
    6.部分功能不合理。

我打算一步步的改善这些问题。
-----
1. 什么是宙斯（Zeus）
宙斯是阿里巴巴开源的一款分布式Hadoop作业调度平台，支持多机器的水平扩展。Zeus是一款完全分布式的调度系统，，支持多机器的水平扩展，一台机器为一个节点，由master节点分发任务至不同的worker,实现任务的分布式调度。目前支持的任务类型主要由hive脚本和shell脚本。Zeus不仅仅可以执行独立任务调度，还支持任务之间依赖调度。这就使得zeus完全不同于传统的任务调度系统中，任务只能单个任务之间调度，这也是zeus的设计中一大亮点。
2. Zeus的架构设计
软件架构设计 
![Alt text](https://github.com/jimmy401/zeus_nb/raw/master/Screenshots/ruanjianjiagou.png)
3.集群架构设计
![Alt text](https://github.com/jimmy401/zeus_nb/raw/master/Screenshots/jishujiagou.png)


安装部署
--
1.zeus是可以单机部署，集群部署的。这里介绍三台机器master-A,worker-B,worker-C 的集群部署方式。这三台机器必须有大数据集群hive，hadoop的客户端。<br/>
2.在每台linux机器上，安装dos2unix;命令 yum install dos2unix;这是因为在执行shell任务的时候，需要处理文本格式。<br/>
3.配置权限<br/>
  * 在每台linux机器上创建用户biadmin,并给这个用户sudo -u biadmin的权限。<br/>
  * linux机器上visudo。添加 biadmin。这里为什么是biadmin呢？<br/>
  * 因为zeus代码中硬编码了这个用户名，biadmin作为zeus-web的管理员帐号。同时需要用这个账号登录linux服务器，启动zeus。<br/>
  * biadmin帐号必须有/tmp/zeus,/data/applogs/,/data/zeus/job_dir/下的增删改权限。zeus的工作路径是/tmp/zeus/yyyy-MM-dd，每天一个。<br/>
  * /data/applogs/是zeus的日志目录。/data/zeus/job_dir/是zeus的工作目录，每天一个。<br/>
  * /tmp/zeus/yyyy-MM-dd是zeus的临时工作目录，每天一个。<br/>
  
4.配置数据库：<br/>
 在项目文件夹db目录下。里面已经配置好了用户。biadmin/123456<br/>
 如果java_home环境变量需要设置：
 export JAVA_HOME=/usr/java/jdk1.7.0_67-cloudera  
 export PATH=$JAVA_HOME/bin:$PATH
  
5.编译源码：<br/>
  * 修改项目下resources目录下的env.sh中的java_home位置，指向A,B,C三台主机上的对应java_home路径。<br/>
  * 修改web工程下filter里的prod.properties里的数据库连接，hadoop,hive等配置。<br/>
  * hadoop.home=/usr/lib/hadoop 可以不改变，在对应linux服务器上建软连接的方式指向真正的hadoop lib路径。<br><br/>
   * ln -s /opt/cloudera/parcels/CDH-5.10.0-1.cdh5.10.0.p0.41/lib/hadoop /usr/lib/hadoop
  * hive.home=/usr/lib/hive配置参考这个方法。<br>
  * zeus的每个节点都需要有这个配置。
  
6.把web项目下的war上传到A,B,C三台机器上。放在tomcat的部署目录下，tomcat内存配置要大，看任务个数吧。因为zeus的很吃内存的。<br/>
  首先启动A,接着B,C，最先启动会作为master。<br/>
  
7.打开浏览器，输入http://A:8080/zeus-web/login.do 输入biadmin/123456，就可以体验zeus了。<br/>

8.master-A启动后，会在zeus_lock表里插入一条记录，标识自己是master.
worker-B启动后，会判断zeus_lock表里是否有记录，如果有记录，就像master注册自己。不然插入记录，标识自己是master。
worker-C跟worker-B一样。
在master分配任务的时候，会在zeus_host_group，zeus_host_relation中遍历host,选择注册表里活跃的host作为worker,执行任务。

版本
----

##版本日期 20180413
更新内容：
    1.jdk版本支持1.8<br/>
    2.用mybatis代替hibernate,mybatis用的是xml方式，还不是最新的注解方式。<br/>
    3.没有改动核心逻辑，仅仅替换了数据访问层。顺便熟悉了zeus的源代码。<br/>
    4.经过反复调试，修改，确认，该版本在功能上与老版本一致，没有导致出现新的bug。<br/>

下面是阅读源码，时做的笔记。先放在这里，大部分已经整理在上面的步骤中了。<br/>

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
        20.zeus的任务依赖是跟随最小的父节点的周期的，例如，C是依赖任务，依赖A,B,A是每半小时调度一次，B是每小时调度一次，
        那么C会跟随A的节奏，每半小时调度一次。
        21.${zdt.format("yyyyMMdd")} 昨日时间：${zdt.addDay(-1).format("yyyyMMdd hh:mm:ss")},
        ${yesterday}会被替换成昨天的日期，格式是yyyyMMdd，是任务时间的昨天。
        22.http://ip:port/zeus-web/dump.do查看任务状态
        23.任务失败后，会发送给重要联系人，然后是job本身的owner，最后是关注者。
        24.定制Home页面<br/>
           1.在开发中心创建一个文档，纪录下该文档的id<br/>
           2.进入代码  com.taobao.zeus.web.platform.client.util.GWTEnvironment 将id填入相应TODO中(多环境下需要考虑环境判断)<br/>
           3.重新部署代码发布<br/>
           4.动态修改文档中心文件，即可实时修改此处内容,此内容支持html格式<br/>

migu修改地方
1.biadmin作为zeus管理员，zeus服务启动用户；
2.所有用户都是通过biadmin sudo -u hadoop的方式提交shell,hive的。
3.部署的机器上，hadoop都配置了kerberos信息。
4.biadmin也需要配置kerberos，这样才能访问hive元数据库。
5.zeus节点上有/mnt/sdb1/zeus/temp,/mnt/sdb1/zeus/job_dir,/mnt/sdb1/zeus/logs
hadoop.home=/usr/lib/hadoop
hadoop.conf.dir=/etc/hadoop/conf
hive.home=/usr/lib/hive
hive.conf.dir=/etc/hive/conf
kerberos.auth=true
kerberos.user=hadoop
6.在HiveJob类中，填写hive udf的定义语句。请在此处填写udf文件对应的文档id,当前文档Id是121
7.执行时长超过12个小时的任务，置为失败。

需要注意防止重复手动提交任务。插入重复数据的问题。

##志同道合的朋友可以联系我
--
邮箱 dufu0401@126.com 
我是拼命的诗人
