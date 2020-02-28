package gbench.common.tree.junit;

import gbench.common.tree.LittleTree.IRecord;
import gbench.common.tree.LittleTree.Jdbc;
import static gbench.common.tree.LittleTree.Jdbc.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author xuqinghua
 *
 */
public class JunitJdbc {
	
	@JdbcConfig(url="jdbc:mysql://localhost:3306/futures?serverTimezone=GMT%2B8&characterEncoding=utf8")
	public  interface JdbcDatabase{
		@JdbcQuery("show tables")
		public default List<String> get(List<IRecord>  ll) {
			System.out.println("hello\t"+ll);
			return ll.stream().map(e->e.str("Tables_in_futures")).collect(Collectors.toList());
		}
		
		@JdbcPreparedQuery("select * from t_price_line_eg2001 limit ?")
		public  List<String> list(int size); 
		
		@JdbcPreparedExecute({
			"create table if not exists t_user(id int primary key auto_increment,name varchar(25)) default charset utf8",
			"insert into t_user(name)values(?)",
		})
		public  void insert(String name); 
		
		public default void foo(Jdbc jdbc) {
			jdbc.withTransaction(sess->{
				for(int i=0;i<100;i++) {
					int t = sess.sql2execute2int("insert into t_user(name) values('xx"+i+"')");
					System.out.println(t);
				}
			});
		};
	}
	
	public static void main(String[] args) {
		var jdbc = Jdbc.newInstance(JdbcDatabase.class);
		System.out.println("1:"+jdbc.get(new LinkedList<>()));
		System.out.println(":回调默认方法----------------------,在回调函数中进行结果处理");
		System.out.println("2:"+jdbc.get(null));
		System.out.println("2:"+jdbc.list(3));
		jdbc.insert("王五");
		jdbc.foo(null);
	}

}
