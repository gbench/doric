package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.fs.XlsFile.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitDMX extends DataMatrixApp {
    @Test
    public void foo() {
        final var rec = REC("香蕉",3.5,"苹果",5.0,"鸭梨",2.5,"橘子",4.8);// 数据结构
        final var prices = RPTS(1,rec).collect(dmc).transpose("价格");// 价格向量
        final var sales = NATS(10).map(e->rec.apply(p->Math.random()*100)).collect(DMC(e->e));// 销售数量矩阵
        System.out.println(sales);
        System.out.println(prices);
        
        final var tt = sales.mmult(prices,"销售价格");
        System.out.println(tt); 
        
        System.out.println(sales.mmult(DBLMX2("价格1",rec,"价格二",rec,"价格三",rec)).plus(0.0).multiply(1000.0)); 
    }
    
    @Test
    public void foo2() {
        final var mx = DMX(Integer.class,"1,2,3;4,5,6;7,8,9").setHeader("A,B,C");
        System.out.println(mx);
        System.out.println(mx.det());
        System.out.println(dblvec("1,2,3").saxpy(1.0,dblvec("1")));
        System.out.println(dblvec("1,2,3").plus(1.4));
        System.out.println(dblvec("1,2,3").multiply(5.0));
        System.out.println(dblvec("1,2,3").multiply(dblvec(3,5)).get(1));
        
    }
    
    @Test
    public void foo3() {
        final var c = DataMatrix.excelname2i("AAA");
        System.out.println("--->"+c);
    }
    
    @Test
    public void foo4() {
        final var mx = DMX(Double.class,"1,2;3,4");
        final var dd = DataMatrix.complement(mx.getCells(), 0, 0);
        System.out.println("complement:-->"+DataMatrix.fmt(dd));
        System.out.println(mx.mmult(mx.reverse()).div(5.0));
        System.out.println(mx.div2(1.0));
        System.out.println(mx.mmult(mx.reverse()));
        System.out.println(mx.reverse().mmult(mx));
    }
    
    @Test
    public void foo5() {
        var mx = DMX(Double.class,STRING2REC("1,3,4,5"));
        mx = dblvec(STRING2REC("1,3,4,5"));
        mx = dblvec("1,2,3,4,5;6,7,8,9,10");
        System.out.println(mx);
        System.out.println(A2R(1,2,3,4,5));
        System.out.println(dblvec(1,2,3,4,5));
    }
}
