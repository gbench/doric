package gbench.sandbox.matrix.algebra;

import org.junit.Test;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.common.fs.XlsFile.DataMatrix;
import gbench.commonApp.data.DataMatrixApp;
import static gbench.commonApp.data.DataMatrixApp.MathOps.*;
import static java.lang.Math.*;
import java.util.function.Function;

/**
 * 
 * @author gbench
 *
 */
public class JunitTaylorSeries extends DataMatrixApp {

    DataMatrix<Double> getM(Number n) {
        return DSQB2(n).get(DBLS(10000));// 随机生成一个矩阵
    }
    
    /**
     * 简单的taylor 级数的模拟：缺陷就是当x过大的时候会导致溢出
     * 泰勒公式:需要注意当X过大 pw 会发生溢出进而结果就不正确了。
     * 
     * @param x 自变量
     * @param derivative (n->dbl) 的高阶导数公式
     * @return 泰勒级数的结果
     */
    public Double taylorSeries2(Number x, Function<Integer,Double> derivative) {
        final var size = 20d;// 级数的长度
        final var drvt = V(size, derivative);// 高阶导数
        final var coef = V(size, n->pow(x.doubleValue(),n)/fact(n));// 幂级数
        final var ones = vones(size);// 求和的全1向量
        return t(drvt.multiply(coef)).mmult(ones).get(0);
    }

    /**
     * 简单的taylor 级数的模拟：缺陷就是当x过大的时候会导致溢出
     * 泰勒公式:需要注意当X过大 pw 会发生溢出进而结果就不正确了。
     * 
     * @param x 自变量
     * @param derivative (n->dbl) 的高阶导数公式
     * @return 泰勒级数的结果
     */
    public Double taylorSeries(Number x, Function<Integer,Double> derivative) {
        final var size = 20d;// 级数的长度
        final var drvt = V(size, derivative);// 高阶导数
        final var pwr = V(size, n->pow(x.doubleValue(),n));// 幂级数
        final var fct = V(size, n->fact(n)*1.0);// 阶乘系数
        final var ones = vones(size);// 求和的全1向量
        //println( pwr.multiply(1d).tp().toString(frt(18))); // 会出现 溢出情况：pwr 溢出后会变成：0
        return t(fct.div2(drvt).multiply(pwr)).mmult(ones).get(0);
    }
    
    final static Double[] sin_aa = new Double[]{0d,1d,0d,-1d};
    final static Double[] cos_aa = new Double[]{1d,0d,-1d,0d};
    final static Function<Integer,Double> sin_drvt = n->sin_aa[n%4];
    final static Function<Integer,Double> cos_drvt = n->cos_aa[n%4];
    final static Function<Integer,Double> exp_drvt = n->1d;
    
    @Test
    public void foo() {
        final double x = PI/3;
        println("taylor:"+taylorSeries(x,sin_drvt));
        println("math:"+sin(x));
        println("");// 空一行
    }
    
    @Test
    public void test2() {
        final var size = 10d;// 演算规模
        final var id = V(size,n->n+1);// 编号项目
        
        // 测试函数1
        var a = V(size,x->taylorSeries(x,sin_drvt));// 测试函数
        var b = V(size,x->sin(x.doubleValue()));// 标准函数
        
        // 测试函数2
        a = V(size,x->taylorSeries(x,exp_drvt));// 测试函数
        b = V(size,x->exp(x));// 标准函数
        
        // 测试函数3
        a = V(size,x->taylorSeries(x,cos_drvt));// 测试函数:alpha
        b = V(size,x->cos(x));// 标准函数:beta
        
        final var d = a.minus(b);// 差:difference
        final var e = d.mapRows2(p->REC(p).applyOnValues(
            (Double v)->MFT("{0}:{1,number,#.##}",abs(v)>=0.01d ? "数据溢出":"正常",abs(v))
        ));// error
        
        final var line = id.corece(Object::toString)// 编号 
            .cbind( a.cbind(b).cbind(d).corece(Object::toString) ) // 泰勒函数,标准值,误差
            .cbind(e) // 备注说明
            .setHeader("编号,泰勒函数,标准值,误差,说明");// 设置表头
        
        // 结果输出
        println(line);
    }
}
