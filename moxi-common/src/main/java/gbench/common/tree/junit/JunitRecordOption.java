package gbench.common.tree.junit;

import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import static java.util.Arrays.*;
import static java.util.stream.Stream.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  	期权计算示例
 * @author gbench
 *
 */
public class JunitRecordOption {
	
	/**
	 *	 时间统计
	 * @param args
	 */
	public static void main(String args[]) {
		 ms_timeit(p->{ 
		  	final List<List<?>> directions = LIST(iterate(0,i->i<13,i->i+1).map(e->asList(1,0,-1)));// 价格的变动方向 
		  	var ff = cph(directions);// 生成涨跌列表
		  	ff.forEach(e->e.compute("total",o->e.reduce(kv2int,0, (a,b)->a+b)));
		   	//System.out.println(FMT2(ff)); //结果输出
		  	var ai = new AtomicInteger(0);
		  	ff.forEach(f->{
		  		System.out.println(MFT("{0}\t{1}",(ai.getAndIncrement()+1),f));
		  	});
		},args);
		
	}
}
