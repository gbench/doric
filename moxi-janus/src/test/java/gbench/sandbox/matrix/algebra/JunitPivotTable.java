package gbench.sandbox.matrix.algebra;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import gbench.data.json.JSN;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;

public class JunitPivotTable extends DataMatrixApp{
    
    @Test
    public void boo() {
        var x  = cph(RPTS(3,L("A","B"))).stream().collect(pvtclc(0,1));
        var p = JSN.pretty(x);
        System.out.println(p);
    }
}
