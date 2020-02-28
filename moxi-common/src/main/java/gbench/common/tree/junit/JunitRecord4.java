package gbench.common.tree.junit;

import static gbench.common.tree.LittleTree.MFT;
import static gbench.common.tree.LittleTree.IRecord.A;
import static gbench.common.tree.LittleTree.IRecord.A2REC;
import static gbench.common.tree.LittleTree.IRecord.L2REC;
import static gbench.common.tree.LittleTree.IRecord.STRING2REC;
import static gbench.common.tree.LittleTree.Tuple2.TUP2;

import java.util.Arrays;
import java.util.stream.Collectors;

import gbench.common.tree.LittleTree.KVPair;

public class JunitRecord4 {
    public static void foo() {
        var t = STRING2REC("1,2,3,4,5");
        var m = t.sliding(2).stream().map(kvs->{
            if(kvs.size()<2)return null;
            final var v0 = kvs.get(0);
            final var v1 = kvs.get(1);
            return MFT("({0})-[e{0}_{1}:Edge]->({1})",v0.value(),v1.value()) ;
        }).filter(e->e!=null);
       
        System.out.println(t);
        System.out.println(t.kk());
        System.out.println(t.vv());
        System.out.println("("+t.vvjoin(")-[e:Edge]->(")+")");
        System.out.println(m.collect(Collectors.joining("/"))
            .replaceAll("(\\([^\\)]+\\))/\\([^\\)]+\\)", "$1"));
        
        final var tup = TUP2(1,2);
        tup.rzip0(Arrays.asList(1,2,3,4,5,5)).forEach(e->{
            System.out.println(e);
        });
    }
    
    public static void foo2() {
        final var line =  A2REC(A(1,2,3,4,5,6,7));
        line.set(0,STRING2REC("1,9"));
        System.out.println(line.getByPath("0/1"));
    }
    
    public static void foo3() {
        final var line = STRING2REC("1/2/3/4/5/6/7/8/9");
        System.out.println(line.vvjoin("-->"));
        System.out.println(line.sliding(2));
        System.out.println(line.sliding(3));
        System.out.println(line.sliding(3,3));
        var rec = L2REC(line.sliding(3,3));
        System.out.println(rec.lla("1",(KVPair<String,Object> s)->Integer.parseInt(s._2()+"")));
    }
    
    public static void main(String args[]) {
       foo3();
    }
}
