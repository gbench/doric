package gbench.sandbox.record;

import org.junit.jupiter.api.Test;
import static gbench.common.tree.LittleTree.*;
import static gbench.commonApp.data.tushare.TuShareApp.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.fs.XlsFile.DataMatrix.*;
import gbench.common.tree.LittleTree.IRecord;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Stream;


/**
 * 
 * @author gbench
 *
 */
public class IRecordScan {
    @Test
    public void foo() {
        final var m = cph2(series,"1:10","1:10","1:10","1:10").collect(imc).reduceColumns("0,1,2",intavg,IRecord::REC);
        System.out.println(m);
    }
    
    public static Integer prod(List<Integer> nn) {
        return nn.stream().collect(Collector.of(()->new AtomicInteger(1),
            (aa,a)->aa.set(a*aa.get()),
            (aa,bb)->{aa.set(aa.get()*bb.get()); return aa;})).get();
    }
    
    public static String join(List<Integer> nn) {
        return nn.toString();
    }
    
    @Test
    public void foo2() {
        A2REC(series.apply("1:10")).scanl2((List<Integer>ll)->join(ll)).forEach(line->{
            System.out.println(line);
        });
        
        IRecord.scan(Stream.of(1,2,3,4,5,6),null,false,true).forEach(line->{
            System.out.println(line);
        });
    }
    
    @Test
    public void foo3() {
        IRecord.scan(Stream.of(1,2,3,4,5,6),null,false,true).forEach(line->{
            System.out.println(line);
        });
    }
    
    @Test
    public void foo4() {
        final var rec = STRING2REC("苹果,西瓜,草莓,哈密瓜,鸭梨");// 水果列表 
        final var mx = cph2(RPT(rec.size(),L(true,false))).map(e->rec.gets(e.bools()))
        .collect(omc);
        System.out.println(FMT2(LIST(mx.mapByRow(IRecord::REC))));
        System.out.println(Arrays.asList(mx.getFlatCells()));
        System.out.println(Arrays.asList(mx.getFlatCells2()));
    }
}
