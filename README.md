**将lib.tar.gz解压后，覆盖storm根目录下的lib目录**
**本工程在jdk1.8.0_102环境下完成**
**将topology提交到storm集群中运行前，需要在storm的配置文件storm.yaml中加入java.libray.path这一项，内容为工程运行时所需要的库所在的目录，包括.so依赖的动态库所在的目录，多个目录之间使用“\:”分隔，一般内容为系统默认的库目录加系统环境变量LD_LIBRARY_PATH,可以写一个普通java程序加载动态库测试成功后，使用System.getProperty("java.library.path")查看应设置的内容**
#*提交抓取视频帧的topology的方法：*
进入$STORM_HOME/bin目录，执行:*storm jar /home/neptune/Grab/StormGrab.jar com.neptune.GrabTopology /home/neptune/Grab/grab_config.json topology-name*
其中的路径换成两个文件的实际路径，topology-name为拓扑名称，可随意起名

抓取视频帧使用配置文件**grab_config.json**：
- grabParallel	int，截图的Bolt并行度；
- spoutParallel	int，Spout的并行度；
- reduceParallel	int，还原GrabCommend对象的Bolt并行度；
- uploadParallel	int，上传截图的并行度；
- kafkaParallel	int，发送消息的并行度；
- libPath	String，动态库绝对路径；
- zkServers	String[]，zookeeper集群地址,格式为host1,host2...；
- zkPort	int，zookeeper端口；
- zks	String，kafka需要的zookeeper地址，格式为host1:port,host2:port...；
- id	String，kafka消费者分组名称；
- topic	String，接受消息的topic名称；
- zkRoot	String，kafka在zookeeper中存放消息的路径，普通情况下为""，不建议修改kafka在zookeeper中的默认路径；
- sendTopic	String，将截图发送到指定的topic；
- brokerList	String，kafka集群地址；
- hdfsDir	String，保存截图的hdfs目录；
- workerNum	int，worker进程数量；
- logPath	String，日志存放目录；

#*提交人脸提取的topology的方法：*
进入$STORM_HOME/bin目录，执行:*storm jar /home/neptune/Grab/StormGrab.jar com.neptune.FacerigTopology /home/neptune/Grab/facerig_config.json topology-name*
其中的路径换成两个文件的实际路径，topology-name为拓扑名称，可随意起名

分离人脸采用配置文件**facerig_config.json**：
- logPath    String，日志文件存放目录，不设置表示不输出日志文件；
- spoutParallel  int，spout的并行度；
- pretreatParallel   int，预处理图片的bolt的并行度；
- facerigParallel    int，人脸提取的bolt的并行度；
- hdfsParallel  int，hdfs写入的并行度；
- kafkaParallel	int，发送消息的bolt的并行度；
- targetTopic	String，kafka消息发送的目的topic；
- bootstrap	String，kafka集群地址；
- id	String，kafka消费者分组名称；
- zkRoot	String，kafka存放消息的标识；
- zks	String，kafka需要的zookeeper集群地址；
- zkServers	String[]，zookeeper集群地址；
- zkPort	int，zookeeper端口；
- topic	String，消息来源的topic；
- hdfsDir	String,存放人脸图片的hdfs目录
- height	int，图片高度；
- width	int，图片宽度；
- libPath	String，人脸提取的动态库的绝对路径
- modelPath	String，so库所需要的model文件夹中的seeta\_fd\_frontal\_v1.0.bin所在的绝对路径
- workerNum	int，工作进程数量；

#*提交人脸识别的topology的方法：*
进入$STORM_HOME/bin目录，执行:*storm jar /home/neptune/Grab/StormGrab.jar com.neptune.AnalyzeTopology /home/neptune/Grab/analyze_config.json topology-name*
其中的路径换成两个文件的实际路径，topology-name为拓扑名称，可随意起名

人脸识别采用配置文件**analyze_config.json**：
- spoutParallel	int,spout并行度;
- downloadParallel	int，下载图片的bolt并行度;
- analyzeParallel	int，人脸识别的并行度；
- queryParallel	int，查询bolt的并行度；
- recordParallel	int，记录bolt的并行度；
- workerNum	int，工作进程数量；
- logPath	String,存放日志文件的目录；
- bufferLimit	int，图片处理缓存的数量；
- timeLimit	int，图片处理缓存等待时间；
- zks	String，zookeeper地址与端口，不能使用ip；
- id	String，kafka消费者分组名称；
- zkRoot	String，kafka存放消息的标识，根据zks的末尾设置；
- zkServers	String[]，zookeeper集群地址；
- zkPort	int，zookeeper端口；
- topic	String，kafka作为消息来源的topic；
- redisHost	String，redis地址；
- redisPassword	String，redis密码；
- redisChannels	String[]，redis发布的channel名称；
- tableName	String，记录数据的hbase表名；
- analyzeLibPath	String，人脸特征提取的动态库的绝对路径
- recognizeLibPath	String，黑名单查找的动态库的绝对路径

#*对maven使用者的一点帮助*
本工程大致用到了以下这些依赖：
- gson 3.0
- kafka 0.10.0
- storm 0.9.6
- jedis
- hadoop 2.6.4
- hbase 1.0.3

*将topology提交到storm运行时，务必将以上依赖库全部放进storm根目录下的lib/内*
