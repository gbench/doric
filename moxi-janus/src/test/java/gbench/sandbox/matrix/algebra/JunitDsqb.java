package gbench.sandbox.matrix.algebra;

import org.junit.Test;
import static gbench.common.tree.LittleTree.*;
import gbench.commonApp.data.DataMatrixApp;

public class JunitDsqb extends DataMatrixApp{
    
    @Test public void foo() {
       var n = 10d;
       var A = DSQB2(n).get(DBLS(10000));// 随机生成一个矩阵
       var B = A.mmult(diag(n).cbind(1d));// 求行和
       var C = (hones(n).mmult(B)); // 求列和
       var D = B.rbind(C);// 记录列的求和结果
       System.out.println(D.toString(frt(2)));
    }
}
