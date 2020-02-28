package gbench.common.tree.junit;

import static gbench.common.tree.LittleTree.SimpleRecord.*;
import static gbench.common.tree.LittleTree.*;
import java.util.*;
import static java.util.Arrays.*;

public class JunitSQL {
	
	/**
	 *  强制为数字类型
	 */
	public static void rec0() {
		final var kvset = Arrays.asList( // 数据列表
			REC2("name", "id", "value$", 437),
			REC2("name", "name", "value$", "张三"),
			REC2("name", "sex", "value$", "男"),
			REC2("name", "address", "value$", "上海市徐汇区")
			);
		final var rec = REC2("kvset", kvset, "code", 98);
		final var s = "update t_student set ${ foreach fld in kvset %fld.name=fld.value$ } where code=##code";
		final var sql = new SQL("hello2", s);
		System.out.println(sql.string(rec));
	}

	/**
	 * foreach的基本展示。
	 */
	public static void rec1() {
		final var kvset = Arrays.asList( // 数据列表
			REC2("name", "id", "value", 437),
			REC2("name", "name", "value", "张三"),
			REC2("name", "sex", "value", "男"),
			REC2("name", "address", "value", "上海市徐汇区"),
			REC2("name", "deposit", "value", "8765")
			);
		final var rec = REC2("kvset", kvset, "code", 98);
		final var s = "update t_student set ${ foreach fld in kvset %fld.name=fld.value} where code=#code";
		final var  sql = new SQL("hello", s);
		System.out.println(sql.string(rec));
		
		final var s2 = "update t_student set ${ foreach fld in kvset %fld.name=fld.value } where code=##code";
		final var sql2 = new SQL("hello2", s2);
		System.out.println(sql2.string(rec));
	}

	public static void rec2() {
		final var rec = REC2("users", Arrays.asList(1, 2, 3, 4));
		final var s = "select * from t_student where id in ( ${ foreach id in users id } )";
		final var  sql = new SQL("hello", s);
		System.out.println(sql.string(rec));
	}

	public static void rec3() {
		final var rec = REC2("users", Arrays.asList("1,2,3,4".split(",")));
		final var s = "select * from t_student where id in ( ${ foreach id in users id } )";
		final var sql = new SQL("hello", s);
		System.out.println(sql.string(rec));
	}
	
	public static void rec4() {
		System.out.println("create:");
		final var user = new SQL("t_user",asList(
			REC2("id",1,"name","张三","birth",new Date()),
			REC2("id",2,"name","李四","birth",new Date()),
			REC2("id",3,"name","王五","birth",new Date())
			));
		System.out.println(user.createTable());
		
		System.out.println("\ninsert:");
		System.out.println(user.insert());
		System.out.println("\nupdate:");
		final var user2 = new SQL("t_user");
		System.out.println(user2.update(REC2("id",1,"name","张三","birth",new Date())));
	}

	public static void main(String args[]) {
		System.out.println("\n---rec0:");
		rec0();
		System.out.println("\n---rec1:");
		rec1();
		System.out.println("\n---rec2:");
		rec2();
		System.out.println("\n---rec3:");
		rec3();
		System.out.println("\n---rec4:");
		rec4();
	}

}
