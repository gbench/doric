package gbench.commonApp.data;

import static gbench.common.tree.LittleTree.IRecord.REC;
import java.util.List;
import gbench.common.tree.LittleTree.IRecord;
import gbench.common.tree.LittleTree.Jdbc;
import gbench.common.tree.LittleTree.Jdbc.JdbcExecute;
import gbench.common.tree.LittleTree.Jdbc.JdbcQuery;

/**
 *  数据应用：
 *  用户可以通过继承DataApp 来使用他的数据接口 SimpleDatabase，
 *  不过用户需要安装配重相应的驱动程序
 *  
 * @author gbench
 *
 */
public abstract class DataApp {
    
    /**
     * 
     * @author gbench
     *
     */
    interface SimpleDatabase{
        
        /**
         * 获取代理结构对象 一个类似于下面的的结构
         * 可以作为访问:sqlinterceptor，sqlpattern_preprocessor，jdbc_postprocessorjdbc等对象的一个入口。    
         * params: ""
         * method:public abstract gbench.common.tree.LittleTree$IRecord 
         *  gbench.demo.data.DataApp$SimpleDatabase.getProxy() 
         * sqlinterceptor:(null) sqlpattern_preprocessor:(null)  
         * jdbc_postprocessor:(null)   
         * jdbc:gbench.common.tree.LittleTree$Jdbc@26f67b76    
         * 
         * @return sqlpattern_preprocessor,interceptor,posprocessor 等内部结构。
         */
        public IRecord getProxy();
        
        /**
         * 创建一个数据库
         * @param dbName 数据库名称
         */
        @JdbcQuery("create database''{0}'' default character set utf8mb4")
        public void createDatabase (String dbName);
        
        /**
         * 可以返回一些简单的类型：非空则认为true
         * @param tableName
         * @return 表格是否存在。
         */
        @JdbcQuery("show tables like ''{0}''")
        public boolean exists(String tableName);
        
        /**
         * 删除一张表
         * @param tableName
         */
        @JdbcExecute("drop table if exists {0}")
        public void dropTable(String tableName);
        
        /**
         * 查询用户表记录
         * @param tableName
         * @return
         */
        @JdbcQuery("select * from {0}")
        public List<IRecord>  getAll(String tableName);
        
        /**
         * 查询用户表记录
         * @param tableName
         * @return
         */
        @JdbcQuery("select * from {0} limit {1}")
        public List<IRecord>  getAll(String tableName,int maxSize);
    }
    
    protected static IRecord  cfg = REC("url",
        "jdbc:mysql://localhost:3306/salespro?useSSL=false&serverTimezone=GMT%2B8&AllowPublicKeyRetrieval=True",
         "driver","com.mysql.cj.jdbc.Driver","user","root","password","123456");
    
    public static Class<String> cstr=String.class;
    public static Class<Integer> cint=Integer.class;
    public static Class<Double> cdbl=Double.class;
    public static Class<Short> cshort=Short.class;
    public static Class<Boolean> cbool=Boolean.class;
    public static Class<Character> cchar=Character.class;
    public static Class<Byte> cbyte=Byte.class;
    public static Class<Long> clng=Long.class;
    public static Class<Float> cflt=Float.class;
    public static Class<IRecord> crec=IRecord.class;
    public static Class<Object> cobj=Object.class;
    
    protected static SimpleDatabase database = Jdbc.newInstance(SimpleDatabase.class,cfg);
    protected static IRecord proxy = database.getProxy();
    protected static Jdbc jdbc = proxy.findOne(Jdbc.class);
    
    /**
     * 关键数据结构结构的重新加载
     */
    public static void reload() {
        database = Jdbc.newInstance(SimpleDatabase.class,cfg);
        proxy = database.getProxy();
        //System.out.println(proxy);
        jdbc = proxy.findOne(Jdbc.class);
    }// reload
}
