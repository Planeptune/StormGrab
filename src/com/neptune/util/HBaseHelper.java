package com.neptune.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neptune on 16-10-8.
 * hbase工具集
 */
public class HBaseHelper {
    private static final String LOG_PATH = "/home/neptune/logs/hbase.log";

    private Connection mConnection;
    private final static String QUORUM = "hbase.zookeeper.quorum";
    private final static String PORT = "hbase.zookeeper.property.clientPort";
    //private final static String MASTER = "hbase.master";
    //private final static String AUTHENTICATION = "hbase.security.authentication";

    //初始化

    /**
     * @param quorum hbase使用的zookeeper地址
     * @param port   hbase是用的zookeeper端口
     */
    public HBaseHelper(String quorum, int port) {
        Configuration config = HBaseConfiguration.create();
        config.set(QUORUM, quorum);
        config.set(PORT, Integer.toString(port));
        try {
            mConnection = ConnectionFactory.createConnection(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 创建数据库表
    public void createTable(String tableName, String[] columnFamilies)
            throws Exception {
        // 新建一个数据库管理员
        Admin hAdmin = mConnection.getAdmin();
        TableName t = TableName.valueOf(tableName);
        if (hAdmin.tableExists(t)) {
            LogWriter.writeLog(LOG_PATH, "table existed : " + tableName);
            return;
        } else {
            // 新建一个 scores 表的描述
            HTableDescriptor tableDesc = new HTableDescriptor(t);
            // 在描述里添加列族
            for (String columnFamily : columnFamilies) {
                tableDesc.addFamily(new HColumnDescriptor(columnFamily));
            }
            // 根据配置好的描述建表
            hAdmin.createTable(tableDesc);
            LogWriter.writeLog(LOG_PATH, "created table : " + tableName);
        }
    }

    // 删除数据库表
    public void deleteTable(String tableName) throws Exception {
        // 新建一个数据库管理员
        Admin hAdmin = mConnection.getAdmin();
        TableName t = TableName.valueOf(tableName);
        if (hAdmin.tableExists(t)) {
            // 关闭一个表
            hAdmin.disableTable(t);
            // 删除一个表
            hAdmin.deleteTable(t);
            LogWriter.writeLog(LOG_PATH, "delete table : " + tableName);
        }
    }

    // 添加一条数据
    public void addRow(String tableName, String row,
                       String columnFamily, String[] columns, String[] values) throws Exception {
        Table table = mConnection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(row));
        // 参数出分别：列族、列、值
        for (int i = 0; i < columns.length; i++)
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columns[i]), Bytes.toBytes(values[i]));
        table.put(put);
    }

    //添加一条数据，数据内容可以是自定的对象
    public void addRow(String tableName, String row, String columnFamily, String[] columns, Object[] values) throws IOException {
        Table table = mConnection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(row));//设置主码

        int i;
        for (i = 0; i < columns.length; i++) {
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columns[i]), ObjectAndBytes.objectToBytes(values[i]));
        }
        table.put(put);
    }

    // 删除一条数据
    public void delRow(String tableName, String row) throws Exception {
        Table table = mConnection.getTable(TableName.valueOf(tableName));
        Delete del = new Delete(Bytes.toBytes(row));
        table.delete(del);
    }

    // 删除多条数据
    public void delMultiRows(String tableName, String[] rows)
            throws Exception {
        Table table = mConnection.getTable(TableName.valueOf(tableName));
        List<Delete> list = new ArrayList<Delete>();

        for (String row : rows) {
            Delete del = new Delete(Bytes.toBytes(row));
            list.add(del);
        }

        table.delete(list);
    }

    @Deprecated
    //根据主码查询列簇中的指定列
    //写入时使用什么方式序列化，读出时就要使用相应方法还原
    //例如写入String时采用hbase提供的Bytes类序列化，反序列化时也要用Bytes类反序列化
    //写入Object时采用的是自定工具类ObjectAndBytes，反序列化时也要用相应方法
    //返回的List中，每一个KeyValue对象是一个列名与值的键值对
    public List<KeyValue> queryByRowkey(String tableName, String rowkey, String columnFamily, String... columns) {
        try {
            Table table = mConnection.getTable(TableName.valueOf("tableName"));
            Get get = new Get(Bytes.toBytes(rowkey));

            for (String column : columns)
                get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column));

            Result result = table.get(get);
            return result.list();
        } catch (IOException e) {
            return null;
        }
    }

    // get row
    //没有返回值？
    @Deprecated
    public void getRow(String tableName, String row) throws Exception {
        Table table = mConnection.getTable(TableName.valueOf(tableName));
        Get get = new Get(Bytes.toBytes(row));
        Result result = table.get(get);
    }

    // get all records
    //没有返回值？
    @Deprecated
    public void getAllRows(String tableName) throws Exception {
        Table table = mConnection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        ResultScanner results = table.getScanner(scan);
    }

    //获取所有表的名称
    public List<String> getAllTables() {
        List<String> tables = null;
        try {
            Admin admin = mConnection.getAdmin();

            HTableDescriptor[] allTable = admin.listTables();
            if (allTable.length > 0)
                tables = new ArrayList<>();
            for (HTableDescriptor hTableDescriptor : allTable) {
                tables.add(hTableDescriptor.getNameAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public Connection getConnection() {
        return mConnection;
    }

    public void close() {
        try {
            if (mConnection != null)
                mConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
