package gbench.sandbox.matrix.algebra;

import org.junit.Test;

import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.KVPair.*;
import static gbench.common.fs.XlsFile.DRow.*;
import static gbench.common.fs.XlsFile.DColumn.*;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gbench.common.fs.XlsFile.DataMatrix;
import gbench.common.fs.XlsFile.TypeU;
import gbench.common.tree.LittleTree;
import gbench.common.fs.XlsFile.DRow;
import gbench.commonApp.data.DataMatrixApp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@SuppressWarnings("all")
public class JunitPloynomial extends DataMatrixApp {
    @Test
    public void foo() {
        cph2(RPTA(3,"ABCDEFG".split(""))).map(e-> LIST(e.values().stream().sorted()))
        .collect(Collectors.groupingBy(e->e))
        .forEach((key,vv)->{
            var item = key.stream().map(Object::toString).collect(Collectors.joining(""));
            var cnt = vv.size();
            System.out.println(MFT("{0}:{1}",cnt,item));
        });
    }
    
    @Test
    public void bar() {
        // 二项展开式
        var binomial = cph2(RPTA(15,A("a","b"))).collect(smc)
            .reduceRows(row->KVS2REC( row.collect(Collectors.groupingBy(e->e)).entrySet().stream()
                .map(e->KVP(e.getKey(),e.getValue().size()))) // KVS2REC
            , mm->REC (mm.entrySet().stream()
                    .map(e->e.getValue())// 提取出IRecord
                    .map(e->e.kvs().stream()
                        .map(p->p+"").collect(Collectors.joining("")))
                        .map(e->e.replace(")(","*").replace(":","^"))
                        .collect(Collectors.groupingBy(e->e))) // REC
                .applyOnValues((List<Object> v)->v.size())
            );
        println(binomial);
    }
    
    
    /* ---------------------------------------------------
     * 定义函数类型对象:
     *---------------------------------------------------*/
    interface Fx extends Function<Number,Number>{};// 一元函数
    interface Fxy extends BiFunction<Number,Number,Number>{};// 二元函数
    
    @Test
    public void baz() {
        final Fx sin = x->Math.sin(x.doubleValue());
        final Fx cos = x->Math.cos(x.doubleValue());
        final Fx exp = x->Math.exp(x.doubleValue());
        final Fx pow = x->Math.exp(x.doubleValue());
        final Fxy add = (x,y)->x.doubleValue()+y.doubleValue();
        final Fxy sub = (x,y)->x.doubleValue()-y.doubleValue();
        final Fxy mul = (x,y)->x.doubleValue()*y.doubleValue();
        final Fxy div = (x,y)->x.doubleValue()/y.doubleValue();
        
       final Fxy f2 = (x,y)->add.apply(x,y);
       println(f2.apply(1, 2));
       
       final Fxy f3 = (x,y)->( (Fx) (p->p.doubleValue() + (
                (((Fx) (q->q.doubleValue())).apply(y) ).doubleValue()
            ))).apply(x);
       println(f3.apply(1, 2));
       
    }
    
    @Test
    public void qux() {
        final Fx sin = x->Math.sin(x.doubleValue());
        final Fx cos = x->Math.cos(x.doubleValue());
        final Fx exp = x->Math.exp(x.doubleValue());
        final Fx pow = x->Math.exp(x.doubleValue());
        final Fxy add = (x,y)->x.doubleValue()+y.doubleValue();
        final Fxy sub = (x,y)->x.doubleValue()-y.doubleValue();
        final Fxy mul = (x,y)->x.doubleValue()*y.doubleValue();
        final Fxy div = (x,y)->x.doubleValue()/y.doubleValue();
        
        final var ff = RPTS(10, (Fxy) (x,y)->add.apply(x,y) );
        final var h = ff.reduce( (f,g)->(x,y)->add.apply(f.apply(x, y),g.apply(x, y)) ).get();
        println(h.apply(1, 2));
        
        final var alpha = V(4,n->(Fxy) (x,y)->mul.apply(n,mul.apply(x,y)));
        final var beta = alpha.tp();
        final BiFunction<Fxy,Fxy,Fxy> product_operator = (f,g)->(x,y)->add.apply(f.apply(x,y),g.apply(x,y));
        final BinaryOperator<Fxy> op = (f,g)->(x,y)->add.apply(f.apply(x,y),g.apply(x,y));
        final Fxy identity = (x,y)->0d;
        
        //自定义乘法运算
        final var m1 = alpha.mmult2(beta, (f,g)->(x,y)->add.apply(f.apply(x,y),g.apply(x,y)), (Fxy) (x,y)->0d, 
            (f,g)->(x,y)->add.apply(f.apply(x,y),g.apply(x,y)));
        final var v1 = m1.evaluate(f->f.apply(1, 2));
        println(v1);
        
        // 使用reducer 进行规约。
        final Function<Stream<Fxy>,Object> reducer = vv->vv.reduce(0, (aa,fxy)->aa+fxy.apply(1, 2).intValue(), (a,b)->a+b);
        final var v2 = alpha.mmult2(beta, product_operator,
            vv->vv.reduce(0, (aa,fxy)->aa+fxy.apply(1, 2).intValue(), (a,b)->a+b));
        System.out.println(v2);
        
        // 模拟矩阵乘法
        final var gamma = V(4,n->n);
        final var m2 = gamma.mmult2(gamma.tp(),(x,y)->x*y, 0, (a,b)->a+b);
        println(m2);
        
    }
    
    @Test
    public void foobar() {
        final var mm = DSQB2(10).dblM(dbls(100));
        println("数据矩阵：");
        println(mm.toString(frt(2)));
        
        System.out.println("列方差");
        final var rec = mm.reduceColumns(e->e.var_p(),IRecord::REC);
        println(rec.toString2());
        
        println("cov:");
        println(mm.cov().toString(frt(2)));
        println("cor:");
        println(mm.cor().toString(frt(2)));
        println("行均值:");
        println(mm.mapRows(e->ROW(e.mean())).toString(frt(2)));
        println("列均值:");
        println(mm.mapColumns(e->COL(e.mean())).toString(frt(2)));
        
    }
    
}
