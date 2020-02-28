package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import static java.util.Arrays.*;
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

}
