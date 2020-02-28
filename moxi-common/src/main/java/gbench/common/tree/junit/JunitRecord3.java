package gbench.common.tree.junit;

import static gbench.common.tree.LittleTree.MFT;
import static gbench.common.tree.LittleTree.cph;
import static gbench.common.tree.LittleTree.series;
import static gbench.common.tree.LittleTree.CronTime.now;
import static gbench.common.tree.LittleTree.IRecord.FMT;
import static gbench.common.tree.LittleTree.IRecord.cbll;
import static gbench.common.tree.LittleTree.IRecord.kv2int;
import static gbench.common.tree.LittleTree.IRecord.supll;
import static java.util.Arrays.asList;
import java.util.stream.Collectors;

/**
 * cph 函数的演示示例
 * @author gbench
 *
 */
public class JunitRecord3 {
	public static void main(String args[]) {
		// 重命名混编
		cph(series,"1:10","1:10","1:10")
		.stream().map(e->e.tagkvs_i(i->MFT("a{0}",i)))// 重新标记列名
		.forEach(e->{
			System.out.println( MFT("{0} --> {1}",
				e.collect(supll(Object.class), 
					(aa,a)->{aa.add( MFT( "{0}[{1}]", a.key(),a.value() )   );},
					cbll(Object.class)
				).stream().map(f->f+"").collect(Collectors.joining("*")),
			e.reduce(kv2int,1,(a,b)->a*b)));
		});
		
		// 数字混编
		cph(series,"-5:5:2","1:5:2").forEach(e->{
			System.out.println(e);
		});
		
		// 类型混编
		var t = cph(
			asList(1,2,3,4),// 数字类型
			asList("abcedefg".split("")),// 字符串
			asList("甲乙丙丁".split("")),// 中文
			asList(true,false),// boolean
			asList(now(),now().plusDays(1))// 时间类型
		);// cph
		System.out.println(FMT(t));
	}
}
