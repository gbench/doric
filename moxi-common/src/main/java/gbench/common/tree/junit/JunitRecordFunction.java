package gbench.common.tree.junit;

import static gbench.common.tree.LittleTree.*;
import lombok.Data;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.CronTime.*;
import static java.util.stream.Stream.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import gbench.common.fs.XlsFile.DataMatrix;

import static gbench.common.fs.FileSystem.*;
import static gbench.common.fs.XlsFile.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitRecordFunction {
	
	@Data
	public static class User{
		int id;
		String name;
		String mobile;
		String sex;
		String address;
		LocalDate birth;
		
		public Long getAge() {
			return birth.until(LocalDate.now(),ChronoUnit.YEARS);
		}
	}
	
	@FunctionalInterface
	public static interface FINDUSER{
		Optional<User> user(int id,List<IRecord> database);
	}
	
	@FunctionalInterface
	public static interface AVG{
		Double averageAge(List<IRecord> database);
	}
	
	public static void foo() {
		// 构造一个用户数据库
		var rand = new Random();// 随机对象
		var database = LIST(iterate(0,i->i<10,i->i+1).map(i->REC( // 自动生成用户数据
			"id",i+1,
			"name",MFT("zhang{0}",i+1),
			"sex",i%2==0?"男":"女",
			"address",MFT("孵化祯禄101弄#{0}",MFT("{0,number,0000}",rand.nextInt(10000))),
			"birth",ld(1980+rand.nextInt(20),rand.nextInt(12)+1,rand.nextInt(29)+1),
			"mobile",MFT("{0,number,000}-{1,number,000}-{2,number,000000}",
				rand.nextInt(10)%2==0?131:189,rand.nextInt(999),rand.nextInt(1000000))
		)));// 数据库初始化
		// 显示数据库
		System.out.println(FMT(database));
		
		// 函数式REC
		System.out.println(
			"id\tname\tsex\taddress\tbirth\tage\tmobile\n"+
			database.stream().map(e->e.mutate(User.class))
				.map(e->MFT("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}",
					e.getId(),e.getName(),e.getSex(),
					e.getAddress(),e.getBirth(),e.getAge(),e.getMobile()))
			.collect(Collectors.joining("\n"))
		);// System.out.println 
		
		final var rec = REC(
			// 默认为Function接口
			"user",(Function<Integer,Optional<User>>)(id)->database.stream().filter(e->e.i4("id")==id)
				.map(e->e.mutate(User.class)).findAny().map(e->e),
				
			//自定义接口
			"finduser",(FINDUSER)(id,db)->db.stream().filter(e->e.i4("id")==id)
				.map(e->e.mutate(User.class)).findAny().map(e->e),
				
			//平均年龄
			"avg",(AVG)(db)->db.stream().map(e->e.mutate(User.class)).
				collect(Collectors.summarizingLong(e->e.getAge())).getAverage()
		);// REC
		
		System.out.println("----------------------------------------");
		// 普通函数求值
		Optional<User> u= rec.eval("user",2);
		System.out.println(MFT("eval:{0}",u));
		
		// 用户查找
		Optional<User>  target= rec.eval2("finduser",FINDUSER.class,4,database);
		System.out.println(MFT("eval:{0}",target));
		
		// 平均年龄
		Double avg = rec.eval2("avg",AVG.class,database);
		System.out.println(MFT("平均年龄:{0}",avg));
	}
	
	/**
	 * 	函数式REC的演示用例
	 * @param args
	 */
	public static void main(String args[]) {
		var excel =  new SimpleExcel(path("user_database.xlsx",JunitRecordFunction.class));
		var mm = excel.autoDetect(0);
		var dd = LIST(mm.mapByRow(e->REC(e)));
		System.out.println(FMT(dd));
		var x = DataMatrix.ASSOC(Integer.class, "1,2,3,4".split(","),new Integer[]{1,2,3,4,5});
		System.out.println(x);
	}

}
