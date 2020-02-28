package gbench.common.tree.junit;

import static gbench.common.fs.XlsFile.*;
import static gbench.common.fs.XlsFile.DataMatrix.*;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import static java.util.Arrays.*;
import java.util.concurrent.atomic.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitDataMatrix {
	
	/**
	 * 数据矩阵
	 */
	public static void datamatrix() {
		var digits = asList(1,0,-1);
		var recs = cph(digits,digits,digits);// 全排列
		var dm = new DataMatrix<> (rr2rowS(recs),Integer.class);
		dm.mapByRow(IRecord::REC).forEach(e->{
			System.out.println("foldLeft-->"+e+"\t"+e.foldLeft(0,(a,kv)->a+kv2int.apply(kv)));
			System.out.println("foldRight-->"+e+"\t"+e.foldRight(0,(kv,a)->a+kv2int.apply(kv)));
		});
		
		System.out.println("----------------------");
		var ss = dm.transpose().mapByRow(IRecord::REC).map(e->{e.compute("total",(o)->e.reduce(0L,(a,b)->a+b));return e;});
		System.out.println(FMT(LIST(ss)));
	}
	
	/**
	 * 
	 */
	public static void foo() {
		var sales =REC("apple",200.0,"grape",300.0,"banana",400.0);// 销售数量
		var prices =REC("apple",4.8,"grape",8.3,"banana",3.5);// 产品价格
		var ss = sales.mutate(r->of(r.rowS(),Double.class));// record 变身为DataMatrix
		var pp = new DataMatrix<>(prices.rowL(),Double.class);// 一般方式。 List方式行化
		var ff = ss.rbind(pp);
		System.out.println(ss);
		System.out.println(ff);
		var total = new AtomicReference<Double>(0.0);
		ff.transpose().mapByRow(IRecord::REC).forEach(e->{
			total.set(e.reduce(1.0,(a,b)->a*b)+total.get());
			System.out.println(MFT("{0}-->{1}",e,total));
		});
		System.out.println(ss.mmult(pp.transpose()));
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		datamatrix();
		System.out.println("----------------");
		foo();
	}

}
