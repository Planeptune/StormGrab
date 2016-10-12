**将lib.tar.gz解压后，覆盖storm根目录下的lib目录**

#*提交抓取视频帧的topology的方法：*
进入$STORM_HOME/bin目录，执行:*storm jar /home/neptune/Grab/StormGrab.jar com.neptune.GrabTopology /home/neptune/Grab/grab_config.json topology-name*
其中的路径换成两个文件的实际路径，topology-name为拓扑名称，可随意起名

抓取视频帧使用配置文件**grab_config.json**：
- grabParallel	int，截图的Bolt并行度；
- spoutParallel	int，Spout的并行度；
- reduceParallel	int，还原GrabCommend对象的Bolt并行度；
- processLimit	int，抓取视频帧的子进程数量上限；
- cmd	String，启动子进程的命令；
- nameFormat	String，截图命名格式；
- frameRate	double，抓取帧率；
- zkServers	String[]，zookeeper集群地址；
- zkPort	int，zookeeper端口；
- zks	String，kafka需要的zookeeper地址，格式为host1:port,host2:port..."；
- id	String，kafka消费者分组名称；
- topic	String，消息来源的topic名称；
- zkRoot	String，kafka存放消息的标识，根据zks填写，如果zks格式为host1:port,host2:port/>path，则填写"/path"，否则填写""；
- sendTopic	String，将截图发送到指定的topic；
- brokerList	String，kafka集群地址，用于发送；
- redisHost	String，redis地址；
- redisPort	int，redis端口；
- redisPassword	String，redis密码；
- workerNum	int，worker进程数量，影响负载与性能；
- logPath	String，存放日志文件的目录，不设置表示不输出日志文件；

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
