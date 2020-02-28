package gbench.sandbox.funcitonal;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import java.util.function.Function;
import java.util.function.Supplier;
import static gbench.common.tree.LittleTree.DBLS;
import static gbench.common.tree.LittleTree.MFT;
import static gbench.common.tree.LittleTree.frt;
import static java.util.stream.Stream.*;
import java.util.Arrays;

/**
 * 
 * @author gbench
 *
 */
public class JunitFunction extends DataMatrixApp{
    @Test
    public void foo() {
        var total = iterate(1,i->i+1).parallel()
            .map(i->(Function<Integer,Integer>) j->i+j ).limit(1000)
            .reduce(Function::andThen).get()
            .apply(0);
        System.out.println(total);
    }
    
    @Test
    public void foo2() {
        var total = iterate(1,i->i+1).parallel()
            .map(i->(Function<Integer,Integer>) j->i+j ).limit(1000)
            .reduce(Function::compose).get()
            .apply(0);
        System.out.println(total);
    }
    
    @Test
    public void foo3() {
        var b = DSQB(100);
        var dd = DBLS(10000);// 生成100个数
        var t = Arrays.stream(dd).reduce((i,j)->i+j).get();
        System.out.println(t);// 线性数组求和
        var mm = b.get(dd);// 生成一个矩阵
        
        // 求矩阵第i 行的函数
        final Function<Integer,Double> sum_i = i->{
           var total_i = Arrays.stream(mm.row(i)).reduce((x,y)->x+y).get();// 求出第i行的和。
           System.out.print(MFT("行{0}的和{1},",i,total_i));
           return t;
        };
        var t2 = iterate(0,i->i+1)
            .limit(mm.height())
            .map(i->(Supplier<Double>)()->sum_i.apply(i))
            .parallel() // 并行计算
            .mapToDouble(f->f.get())
            .reduce((x,y)->x+y).getAsDouble();
        System.out.println();
        System.out.println(t2);
        
        // 矩阵的打印
        System.out.println(mm.toString(frt(2)));
    }
}
