package gbench.sandbox.matrix.algebra;

import org.junit.Test;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.commonApp.data.DataMatrixApp;
import static gbench.commonApp.data.DataMatrixApp.MathOps.*;

public class JunitSqrmAndZerom extends DataMatrixApp {
    
    @Test
    public void foo() {
        final var x = sqrm(10d).mapByRow(IRecord::REC)
        .map(e -> e.scanl2(pp -> L2REC(pp).reduce(kv2dbl, 0d, add()) 
             / e.computeIfAbsent("$total",k->e.reduce(0d,add())) // 百分比
            ,IRecord::L2REC))
        .collect(dmc);
        println(x.tp().toString(frt(3)));
    }
    
    @Test
    public void bar() {
        var v = V(50,e->Math.random());
        println(v.toString(frt(2)));
        var v2 = zerom(5,4d);
        println(v2.toString(frt(2)));
    }
}
