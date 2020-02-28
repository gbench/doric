package gbench.sandbox.matrix.algebra;

import org.junit.Test;

import static gbench.common.tree.LittleTree.*;
import java.util.function.Function;
import java.util.stream.Stream;
import gbench.commonApp.data.DataMatrixApp;

public class JunitMatrix extends DataMatrixApp{
    
    @Test
    public void foo() {
        var dd = dbls(100);
        var sqb = DSQB(6);
        var mm = sqb.dblM(dd);
        
        
        var v1 = dblvec(RPTA(50,8.0));
        var v2 = dblvec(RPTA(5,9.0)).transpose();
        System.out.println(mm.toString(frt(2)));
        System.out.println(v1.mmult(v2).toString(frt(4)));
        
        
        Function<Object,IRecord> f = IRecord::P;
        var t = Stream.of(f,f,f,f).reduce(Function::compose).get().apply("1");// 包装顺序
        var t2 = Stream.of(f,f,f,f).reduce(Function::andThen).get().apply("1");// 分布殊勋。
        System.out.println(t);
        System.out.println(t2);
        
        final Function<Integer,Integer> g = (n)->n+1;
        int t3 =RPTS(100,g).reduce(Function::compose).get().apply(0);
        System.out.println(t3);
    }

}
