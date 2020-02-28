package gbench.common.tree.junit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import lombok.Data;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.CronTime.*;
import static gbench.common.tree.LittleTree.*;

/**
 * Record 的类型变换
 * @author gbench
 *
 */
public class JunitRecordMutate {
	
	@Data
	public static class User{
		String name;
		String address;
		Date birth;
		LocalDateTime localDateTime;
		LocalDate localDate;
		LocalTime localTime;
		int height;
		int weight;
	}
	
	/**
	 *  	信息访问
	 * @param args
	 */
	public static void main(String args[]) {
		var r = REC("name","zhangsan","address","上海",
			"birth",now().format(dtf("yyyy-MM-dd")),
			"localDateTime",now().format(dtf("yyyy-MM-dd HH:mm:ss")),
			"localDate",now().format(dtf("yyyy-MM-dd")),
			"localTime",now().format(dtf("yyyy-MM-dd HH:mm:ss")),
			"height",198,
			"weight","98"
		);// 基本的Record 数据类型
		//对于不同的额类型Muate会报错
		System.out.println(MFT("简单版的：异常自我消化：rec->obj::mutate:{0}",r.mutate(User.class)));
		//会自动完成类型转换
		var u = r.mutate2(User.class);
		System.out.println(MFT("智能版的：类型自动转换：rec->obj::mutate2:{0}",u));
		System.out.println(MFT("obj->rec:{0}",OBJ2REC(u)));
		
		System.out.println("--------------------转换成json对象--------------------");
		System.out.println(r.json());
		System.out.println(Json.obj2json(u));
	}

}
