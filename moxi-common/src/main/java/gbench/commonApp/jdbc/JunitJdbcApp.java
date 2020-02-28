package gbench.commonApp.jdbc;

import gbench.common.tree.LittleTree.Jdbc;
import gbench.common.tree.LittleTree.Jdbc.JdbcConfig;
import gbench.common.tree.LittleTree.Jdbc.SqlPatternPreprocessor;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitJdbcApp implements IJdbcApp{
    
    @JdbcConfig(url="jdbc:mysql://localhost:3306/chaos?serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true")
    interface MySqlDatabase extends ISqlDatabase{
    }
    
    /**
     * jdbc 数据库
     */
    public void jdbc() {
        final var database = IJdbcApp.newNsppDBInstance("rdbms.sql",MySqlDatabase.class);
        final var proxy = database.getProxy();
        final var spp = proxy.findOne(SqlPatternPreprocessor.class);
        final var s1 = spp.handle(null, null, "#getUserByName", null);// 使用#开头的key可以在rdbms.sql总提取对应的SQL语句模板
        System.out.println(MFT("模板:{0}",s1));
        final var s2 = Jdbc.quote_substitute(s1, "#+(\\w+)",REC("name","张三","max",200,"min",160));
        System.out.println(MFT("sql:{0}",s2));
        final var s3 = spp.handle(null,REC("name","张三","max",200,"min",160),"#getUserByName",null);
        System.out.println(MFT("sql:{0}",s3));
    }
    
    /**
     * nosql 数据库
     */
    public void nosql() {
        final var database = IJdbcApp.newNspebDBInstance("nosqldb.sql",MySqlDatabase.class);
        final var proxy = database.getProxy();
        final var spp = proxy.findOne(SqlPatternPreprocessor.class);
        final var sqlp = spp.handle(null, null, "#getDatabase", null);
        System.out.println(sqlp);
    }
    
    /**
     * 演示示例
     * @param args
     */
    public static void main(String args[]) {
        final var app = new JunitJdbcApp();
        app.jdbc();
        app.nosql();
    }
}
