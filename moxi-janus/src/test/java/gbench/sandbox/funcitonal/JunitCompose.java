package gbench.sandbox.funcitonal;

import org.junit.jupiter.api.Test;
import gbench.commonApp.data.DataMatrixApp;
import java.util.*;

public class JunitCompose extends DataMatrixApp {
    
    @Test
    public void foo() {
        var v = V(5,n->(Fx) (x)->n+"+"+x);
        var b = V(5,n->(Fx) (x)->((char)('A'+n))+"+"+x);
        var c = V(b.mmult2(v.tp(),DataMatrixApp::compose, DataMatrixApp::ff2fx));// 向量化
        var d = V(c.mmult2(v.tp(),DataMatrixApp::compose, DataMatrixApp::ff2fx));
        var x = d.mmult2(d.tp(),DataMatrixApp::compose, DataMatrixApp::ff2fx).evaluate(f->f.apply("0"));
        
        System.out.println(x);
        //System.out.println(L(x.getFlatCells()));
    }
    
    /**
     * 唯一编号
     * @return
     */
    public static String id() {
        return UUID.randomUUID().toString();
    }
    
    @SuppressWarnings("unused")
    @Test
    public void bar() {
        Fxy add = (x,y)->((Number)x).doubleValue()+((Number)y).doubleValue();
        Fxy sub = (x,y)->((Number)x).doubleValue()-((Number)y).doubleValue();
        Fxy mul = (x,y)->((Number)x).doubleValue()*((Number)y).doubleValue();
        Fxy div = (x,y)->((Number)x).doubleValue()/((Number)y).doubleValue();
        
    }

}
