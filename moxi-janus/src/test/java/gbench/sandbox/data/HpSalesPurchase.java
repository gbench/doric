package gbench.sandbox.data;

import static gbench.common.tree.LittleTree.IRecord.dbl_stats;

import java.util.concurrent.atomic.AtomicReference;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.common.tree.LittleTree.IRecord;
import gbench.commonApp.data.DataApp;

import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class HpSalesPurchase extends DataApp{
    
    static IRecord po() {
        var  ar = new AtomicReference<IRecord>();
        jdbc.withTransaction(sess->{
            final var rr = sess.psql2records("select goods_name,clientele_name,total from purchase_order where year(create_at)=2019",A());
            final var res = IRecord.pivotTable(rr, "goods_name,clientele_name", stream -> {
                final var stats = stream.filter(e -> e.dbl("total") != null).collect(dbl_stats("total"));
                return REC("total",stats.getSum());
            });
            
            final var porec=REC();ar.set(porec);
            res.dfs_eval_forone(porec::add);
            var r = res.dfs_eval_forone(pv2rec_eval("pct,vendor","value"));
            r = LIST(r.stream().sorted((a,b)->b.i4("value")-a.i4("value")));
            //System.out.println(FMT(r));
        });
        return ar.get();
    }
    
    static IRecord so() {
        var  ar = new AtomicReference<IRecord>();
        jdbc.withTransaction(sess->{
            final var rr = sess.psql2records("select goods_name,clientele_name,total from  `order` where year(create_at)=2019",A());
            final var res = IRecord.pivotTable(rr, "goods_name,clientele_name", stream -> {
                final var stats = stream.filter(e -> e.dbl("total") != null).collect(dbl_stats("total"));
                return REC("total",stats.getSum());
            });
            
            final var sorec=REC();ar.set(sorec);
            res.dfs_eval_forone(sorec::add);
            
            var r = res.dfs_eval_forone(pv2rec_eval("pct,vendor","value"));
            r = LIST(r.stream().sorted((a,b)->b.i4("value")-a.i4("value")));
            //System.out.println(FMT(r));
        });
        return ar.get();
    }
    
    
    public static void main(String args[]) {
        cfg.set("url", "jdbc:mysql://localhost:3306/salespro?serverTimezone=GMT%2B8"); reload();
        var porec = po();
        var sorec = so();
        var x = porec.join2rr(sorec,"path,po,so");
        System.out.println(FMT(x));
    }
}
