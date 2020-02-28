package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import static gbench.common.tree.LittleTree.IRecord.*;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class JunitMatrix extends DataMatrixApp{
    
    @Test
    public void foo() {
        final var sales = DBLMX(
            R("苹果",100.0,"香蕉",20.0,"葡萄",30.0,"哈密瓜",50.0),
            R("苹果",102.0,"香蕉",204.0,"葡萄",130.0,"哈密瓜",540.0),
            R("苹果",102.0,"香蕉",204.0,"葡萄",130.0,"哈密瓜",540.0),
            R("苹果",102.0,"香蕉",204.0,"葡萄",130.0,"哈密瓜",540.0),
            R("苹果",102.0,"香蕉",204.0,"葡萄",130.0,"哈密瓜",540.0)
        );
        
        final var prices = DBLMX2( 
            "价格1", R("苹果",5.8,"香蕉",3.5,"葡萄",8.5,"哈密瓜",12.4),
            "价格2", R("苹果",4.8,"香蕉",5.6,"葡萄",12.8,"哈密瓜",8.4)
        );
        
        System.out.println(sales);
        System.out.println(prices);
        System.out.println(sales.mmult(prices,"价格1,价格2"));
        System.out.println(sales.div2(1.0));
    }
    
    public static long factorial(long n) {
        return n==0?1:n*factorial(n-1);
    }
    
    public static double power(double x,long n) {
        return n==0?1:x*power(x,n-1);
    }
    
    @Test
    public void foo2() {
       final var elu = new ExpressionEvaluator();
       final var x = 1.0;
       final var proto = REC("elu",elu);
       final var r = NATS(39).map(n->REC("Fn",factorial(n),"Xn",power(x,n)))
       .collect(Collectors.summarizingDouble(e->proto.evalExpr("1/#Fn*#Xn",e))).getSum();
       System.out.println(r);
    }
    
 
    @AllArgsConstructor @ToString @Data @NoArgsConstructor
    public static class User{String name;String sex;}
    @Test
    public void foo3() {
       var rec = REC("name","李四","sex","man");
       System.out.println(rec.toTarget(User.class));
       System.out.println(rec.toTarget(String.class));
       Function<Object,IRecord> p = IRecord::P;
       final var t = Stream.of(p,p,p,p,p,p,p,p,p,p,p).reduce(rec,(e,f)->f.apply(e),(a,b)->a);
       System.out.println(t);
    }
}
