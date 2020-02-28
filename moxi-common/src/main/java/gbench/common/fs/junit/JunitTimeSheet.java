package gbench.common.fs.junit;

//import gbench.common.fs.XlsFile.SimpleExcel;
//import gbench.common.fs.XlsFile.StrMatrix;
import lombok.Data;
import static gbench.common.fs.XlsFile.DataMatrix.*;
//import java.util.Arrays;
import java.util.Map;

public class JunitTimeSheet {
	
	public static void main(String args[]) {
		/*SimpleExcel excel = new SimpleExcel("D:\\sliced\\sandbox\\svn\\dev\\trunk\\proj\\group\\meetings\\例会\\周一\\timesheet.xlsx");
		StrMatrix mx = excel.autoDetect("TS0408");
		
		mx = mx.filter( AND(
				TEST_WITH( "项目", e->e != null && !e.equals("null") ),
				//TEST_WITH( "研究反馈", e->e != null && !e.equals("null") ),
				TEST_WITH( "负责人", e->e != null && e.equals("XQH") ),
				TEST_WITH( "测试发布", e->e != null && !e.equals("null") )
			))
			.map(STR_ASSOC("项目,研究反馈,测试发布,决定线上发布,负责人,优先级"))
			.sorted("优先级",(a,b)->-1*a.compareTo(b))
			.sorted("测试发布",a->DATE(a).getTime(),(a,b)->(int)(a-b))
			.map(STR_ASSOC("项目"))
			.cast(StrMatrix::new);
		
		System.out.println(mx);
		long begTime = System.nanoTime();
		System.out.println(Arrays.asList(mx.col(0)));
		long endTime = System.nanoTime();
		System.out.println(endTime-begTime);
		
		begTime = System.nanoTime();
		System.out.println(mx.lcols().get(0));
		endTime = System.nanoTime();
		System.out.println(endTime-begTime);*/
		
		System.out.println(dblmm("1,2;3,4").mmult(dblmm("1,0,1;0,1,1")));
		@Data class Person{ String name;String sex;String address;double weight;
			Person(Map<String,?>rec){OBJINIT(this,rec);}};// 用户对象
		
		of("name sex address weight;张三 男 上海 88;王五 男 北京 89;赵六 男 武汉 45")
		.sorted("weight",Double::parseDouble,Double::compareTo)
		.filter(Person::new,e->e.weight<80)
		.mapByRow(Person::new)
		.forEach(System.out::println);
	}

}
