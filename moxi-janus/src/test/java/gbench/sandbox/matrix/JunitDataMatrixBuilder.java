package gbench.sandbox.matrix;

import org.junit.jupiter.api.Test;
import gbench.commonApp.data.DataMatrixApp;
import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitDataMatrixBuilder extends DataMatrixApp {
    
    @Test
    public void foo() {
        var n = 10;
        var builder = DMB(Double.class,n,n);
        var mx = builder.get(DBLS(10000));
        System.out.println(mx.reverse().mmult(mx).toString(frt(2)));
    }
    
    @Test
    public void bar() {
        int n =5;
        var builder = DMB(Integer.class,n,n);
        var mm = builder.get(DBLSTREAM(100).map(e->((Number)(e*100)).intValue()).toArray(Integer[]::new));
        System.out.println(mm);
        System.out.println(
            mm.plus(mm).minus(1)
            .toString(frt(2))
        );
        
        var m1 = DMB(Double.class,n,n).get(DBLS(100));
        System.out.println(m1.reverse().mmult(m1));
    }

}
