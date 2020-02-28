package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import static gbench.common.tree.LittleTree.IRecord.*;

public class JunitMatriReverse extends DataMatrixApp{
    @Test
    public void foo() {
        final var mx = DBLMX(
            A2R(1,2,3,4),
            A2R(8,9,3,5),
            A2R(7,6,5,3),
            A2R(1,3,1,2)
        );
        
        System.out.println(mx.reverse().mmult(mx));
        
        final var builder = DMB(Double.class,5,50);
        final var m = builder.get(1.0,3.0,4.0,5.0,6.0,6.0);
        System.out.println(m);
        
        final var builder2 = DMB2(Double.class,50,5);
        final var m2 = builder2.get(1.0,3.0,4.0,5.0,6.0,6.0);
        System.out.println(m2);
    }
}
