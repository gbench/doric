package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import static java.util.Arrays.*;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.A;
import static gbench.common.tree.LittleTree.IRecord.L;
import static gbench.commonApp.data.DataMatrixApp.MathOps.*;

public class JunitDataMatrixOps extends DataMatrixApp{
    
    @Test
    public void foo() {
        println("列操作");
        var m0 = zerom(5,5);
        var m1 = m0.insertColumn(2,asList(1,2,3));
        println(m1);
        var m2 = m0.insertColumn(10,asList(1,2,3));
        println(m2);
        var m3 = m2.removeRow(2);
        println(m3);
    }
    
    @Test
    public void bar() {
        println("行操作");
        var m0 = zerom(3,5);
        var m1 = m0.insertRow(2,asList(1,2,3));
        println(m1);
        var m2 = m0.insertRow(10,asList(1,2,3));
        println(m2);
        var m3 = m2.removeColumn(2);
        println(m3);
    }
    
    @Test
    public void qux() {
        var mm = DSQB(10d).get(DBLS(100));
        var bb = diag(10d).cbind(1.0);
        var cc = mm.mmult(bb).setColumnNames(-1, "合计");
        println(cc.toString(frt(4)));
        println(diag(10d).dot(2d).div2(1d).div(2d).insertRow(5, L(8d)));
        println(diag(A(1, 2, 3, 4, 5), 0).insertColumn(3, L(3), "三"));
        println(diag(A(1, 2, 3, 4, 5)).cbind(A(1), "last")
            .cbind(1).cbind(new Integer[][] { { null, 2 }, { 8, 9 } },"好漂亮"));

        var c = diag(A(1d,5d,6d,9d,15), 0d);
        c.lcol(-2, L(3,2,1));
        c.lrow(2, L(3,2,1));
        println(c);
    }
    
    @Test
    public void foobar() {
        var c = diag(10d);
        
        c.set(
            1,0,10,// 2行 1列 的元素设置为 10
            5,7,"15" // 6行 8列 的元素设置为 15
        );
        println("散点设置");
        println(c);
        
        c.setRow(2, 4, 1,2,3,4,5,6,8,9,10); // 设置行数据
        c.setColumn(2, 4, 1,2,3,4,5,6,8,9,10); // 设置列数据
        println("射线设置");
        println(c);
        
        c.lcol(8,L(8.1d)); // 设置整行数据
        c.lrow(8,L(8.2d)); // 设置整行数据
        println("整行设置");
        println(c);
        
    }

}
