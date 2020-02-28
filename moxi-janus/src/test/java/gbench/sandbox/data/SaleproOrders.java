package gbench.sandbox.data;

import gbench.common.tree.LittleTree.*;
import gbench.commonApp.data.DataApp;
import static java.util.stream.Collectors.*;
import gbench.data.json.JSN;

/**
 * 
 * @author gbench
 *
 */
public class SaleproOrders extends DataApp{
    
    public static void main(String args[]) {
        cfg.set("url", "jdbc:mysql://localhost:3306/salespro?serverTimezone=GMT%2B8"); reload();
        jdbc.withTransaction(sess->{
            final var rr = sess.sql2records("select * from `order`");
            final IRecord result= IRecord.pivotTable(rr, 
                  "goods_name,clientele_name", 
                  stream->stream.collect(summarizingDouble(e->e.dbl("number"))).getSum());
            
            result.dfs_forone((path,e)->{
                //System.out.println(MFT("{0}\t{1,number,0}",path,e));
            });
            
            System.out.println(JSN.pretty(result));
        });
    }

}
