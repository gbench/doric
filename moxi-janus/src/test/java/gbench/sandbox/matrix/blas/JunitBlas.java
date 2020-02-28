package gbench.sandbox.matrix.blas;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.fs.XlsFile.*;
import org.jblas.DoubleMatrix;

public class JunitBlas extends DataMatrixApp {
    
    public static DataMatrix<Double> M(DoubleMatrix mx){
        var builder = DMB(Double.class,mx.rows,mx.columns);
        return builder.get(mx.elementsAsList().toArray(Double[]::new));
    }
    
    @Test()
    public void foo() {
        int n = 1000;
        var builder = DMB(Double.class,n,n);
        var dbls = DBLS(n*n);
        var dd = D2ds(dbls);
        println(dd.length);
        var dm = new DoubleMatrix(n,n,dd);
        dm = dm.mmul(dm);
        println(M(dm).toString(frt(2)));
        var m1 = builder.get(dbls);
        println(m1.mmult(m1).toString(frt(2)));
    }
    
    @Test()
    public void foo2() {
        int n = 1000;
        var dbls = DBLS(n);
        var builder = DSQB(10);
        println(builder.get(dbls).toString(frt(2)));
    }
}
