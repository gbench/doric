package gbench.common.tree.junit;

import static gbench.common.tree.LittleTree.IRecord.REC;

import java.sql.Connection;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.stream.Stream.*;
import gbench.common.tree.LittleTree.Jdbc;
import static gbench.common.tree.LittleTree.Jdbc.IJdbcSession.*;
import gbench.common.tree.LittleTree.SQL;
import gbench.common.tree.LittleTree.Jdbc.FunctionWithThrowable;

import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
@SuppressWarnings("unused")
public class JunitJdbcPreparedStatement {
	
	public static Jdbc jdbc = new Jdbc(REC("url","jdbc:mysql://gpc:3306/s1214?serverTimezone=GMT%2B8&characterEncoding=utf8",
			"driver","com.mysql.cj.jdbc.Driver","user","root","password","123456"));
	
	/**
	 * withConnection 接口的使用示例：数据库连接的基本使用。
	 */
	public static void foo_withconnection() {
		// 的回调函数:数据连接的使用。
		Function<String,FunctionWithThrowable<Connection,Object>> ops = (sql)->conn->{
			var lines = new LinkedList<List<Object>>(); // 结果集
			try(var stmt = conn.prepareStatement(sql); var rs = stmt.executeQuery();){
				var m = rs.getMetaData();var ncol = m.getColumnCount();// 列数据
				while(rs.next()) {// 结果遍历
					lines.add(iterate(0,i->i<ncol,i->i+1).map(i->{ 
						Object t = null; // 目标值
						try{t= rs.getObject(i+1);}catch(Exception e) {}
						return t;
					}).collect(Collectors.toList()));
				}//while
			};
			// 返回结果处理集合
			return lines.stream()// 结果整理
				.map(line->line.stream().map(Object::toString).collect(Collectors.joining("\t")))
				.collect(Collectors.joining("\n"));
		};// ops SQL 语句的操作函数。
		
		var d = jdbc.withConnection(ops.apply("select * from t_user"));
		System.out.println(d);
	}
	
	/**
	 * withTransaction 的使用示例
	 */
	public static void foo_witransaction() {
		
		jdbc.withTransaction(sess->{
			String tname = "t_user";
			sess.sqlupdate(MFT("drop table if exists {0}",tname));
			sess.sqlupdate(MFT("create table {0} ( id int primary key auto_increment,name varchar(25) )",tname));
			for(int i=0;i<10;i++){
				//String sql = new SQL("t_user",REC("name","zhang"+i)).insert();
				//System.out.println(sql);
				//var id = sess.sql2execute2int(sql);
				var res = sess.psql2update("insert into t_user(name) values (?)",Arrays.asList("zhangsan"+i));
				System.out.println("generate key:"+id(res));
				//sess.psql2update("update t_user set name=? where id=?",Arrays.asList("zhangsan"+i+100,id(res)));
				//System.out.println("generate key:"+id(res));
				System.out.println("过程结果1:"+FMT(sess.psql2records(MFT("select * from {0} where id=?",tname),
					Arrays.asList(id(res)))));
				//System.out.println("过程结果2:"+FMT ( sess.sql2records(MFT("select * from {0} where id={1}",tname,id(res)))));
				//System.out.println("过程结果2"+FMT(sess.sql2records(MFT("select * from {0} where id=?",tname))));// 会立即终止
				if(id(res)%2==0) {// 删除偶数行
					sess.psql2update(MFT("delete from {0} where id=?",tname),Arrays.asList(id(res)));
				}//if
			}//for
			
		});// withTransaction
		System.out.println("最终结果"+FMT(jdbc.sql2records(MFT("select * from {0}","t_user"))));
	}
	
	/**
	 * jdbc 接口的演示示例。
	 * @param args
	 */
	public static void main(String args[]) {
		System.out.println("foo_witransaction 演示结果：");
		foo_witransaction();
		System.out.println("foo_withconnection 演示结果：");
		foo_withconnection();
	}

}
