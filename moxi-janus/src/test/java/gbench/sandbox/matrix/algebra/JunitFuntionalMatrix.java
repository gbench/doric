package gbench.sandbox.matrix.algebra;

import org.junit.Test;
import gbench.common.fs.XlsFile;
import gbench.common.tree.LittleTree.IRecord;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.commonApp.data.DataMatrixApp;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 
 * @author gbench
 *
 */
public class JunitFuntionalMatrix extends DataMatrixApp {
    
    @Test
    public void foo() {

        final var alpha = V(10, n -> ((Function < Object,Object >) e -> n + ":" + e)); //函数向量 
        final var beta = alpha.transpose(); // //函数向量 
        final var ff = alpha.mmult2(beta, Function::compose, e->e, Function::compose);// 矩阵式的函数组合。 
        final var xx = ff.mapByRow(IRecord::REC).map(e -> { 
           final var r = e.aovs((Function<Object, Object>f)->f.apply(REC())); 
           System.out.println(r);
           return REC(r);
        }).collect(tmc(IRecord.class));
        System.out.println(xx); 
    }
    
    @Test
    public void bar() {
        
        final var alpha = V(10, n -> ((Function<IRecord,IRecord>) e ->REC(n,e))); //函数向量 
        final var beta = alpha.transpose(); // //函数向量 
        final var ff = alpha.mmult2(beta, Function::compose, e->e, Function::compose);//矩阵式的函数组合。 
        final var mm = ff.evaluate(e->e.apply(REC("o","-")));
        System.out.println(mm); 
        
        //显示数据单元
        var cell = mm.getDataCell("A1");
        var leaf = cell.offset(1, 1).getValue().leaf();
        println(leaf.aoks(IRecord::STRING2REC));
        
    }
    
    @Test
    public void barz() {
        final var rec = REC( 
            "name","zhangsan", 
            "sex","man", 
            "contact",REC("mobile","13120751773","phone","0411833802234","email","gbench@sina.com"), 
            "address",REC("provice","liaoning","city","dalian","district","pulandian") 
        ); 
        System.out.println(rec.leafs());
        System.out.println(rec.leaf(3)); // "/contact/phone:0411833802234" 
    }
    
    @Test
    public void foo1() {
        Stream.of("0,0;1,2;1a;A1;A3;$1$B;$B$1;1b;B5;AGE100".split(";"))
        .map(e->new XlsFile.Tuple2<>(e,Arrays.asList(XlsFile.DataCell.addr2offset(e))))
        .forEach(System.out::println);
    }
}
