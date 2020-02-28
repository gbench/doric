package gbench.commonApp.jdbc;

import java.util.List;
import gbench.common.tree.LittleTree.IRecord;
import gbench.common.tree.LittleTree.Jdbc;
import gbench.common.tree.LittleTree.Jdbc.JdbcExecute;
import gbench.common.tree.LittleTree.Jdbc.JdbcQuery;
import gbench.common.tree.LittleTree.Jdbc.SqlPatternPreprocessor;

/**
 * SQL数据库
 * @author gbench
 *
 */
public interface ISqlDatabase {
    /**
     * 创建一个数据库
     * @param dbName 数据库名称
     */
    @JdbcQuery("create database''{0}'' default character set utf8mb4")
    public void createDatabase (String dbName);
    
    /**
     * 创建一个数据库
     * @param dbName 数据库名称
     */
    @JdbcQuery("select database() database")
    public String getDbName();
    
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
    
    /**
     * 调用SqlPatternPreprocessor 处理后的对。sqlpattern
     * SqlPatternPreprocessor & sqlpattern的说明
     *   SqlPatternPreprocessor 会自动对sqlpattern中的命名参数进行替换： <br>
     *   sqlpattern: select * from user where name=#name <br>
     *   params:REC("name","张三") <br>
     *   返回值:select * from user where name="张三" <br>
     * 
     * @param sqlpattern 一个#开头的SQL语句模板语句变量。或是含有#变量的sql语句模板。
     * @param params sharp变量的占位符参数
     * @return SqlPatternPreprocessor 处理后的SQL语句。
     */
    public default String getSql(final String sqlpattern, final IRecord params) {
        final var proxy = this.getProxy();
        final var spp = proxy.findOne(SqlPatternPreprocessor.class);
        final var jdbc = proxy.findOne(Jdbc.class);
        final var sql = spp.handle(null, params, sqlpattern, jdbc);;
        return sql;
    }
    
    /**
     * 仅仅返回 SqlPattern 并不给予对参数进行替换。
     * @param sqlpattern 一个#开头的变量名。
     * @return SqlPatternPreprocessor 处理后的SQL语句。
     */
    public default String getSqlPattern(final String sqlpattern) {
       return this.getSql(sqlpattern, null);
    }
    
    /**
     * 返回一个获取代理对象
     * Jdbc 的代理对象：可以通过 IRecord.findOne(Jdbc.class) 获取jdbc对象。
     * @return Proxy
     */
    public IRecord getProxy();
}
