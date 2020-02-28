package gbench.sandbox.data;

import gbench.common.tree.LittleTree.IRecord;
import gbench.commonApp.data.DataApp;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import org.junit.Test;

/**
 * 
 * @author gbench
 *
 */
public class PivotTableOfOrders extends DataApp {
    
    @Test
    public void foo() {
        cfg.set("url", "jdbc:mysql://localhost:3306/salespro?serverTimezone=GMT%2B8");
        reload();
        jdbc.withTransaction(sess -> {
            final var sql = "select clientele_name,goods_name," + "date_format(create_at,'%Y') year,"
                    + "date_format(create_at,'%m') month," + "date_format(create_at,'%d') day," + "total from `order`";
            final var rr = sess.sql2records(sql);
            final var res = IRecord.pivotTable(rr, "goods_name,year,month,day", stream -> {
                return stream.filter(e -> e.dbl("total") != null).collect(dbl_stats("total")).getSum();
            });
            final var pvt = res.dfs_eval_forone((path, value) -> REC("path", path, "value", value));
            final var dd = LIST(pvt.stream()
                .sorted((a, b) -> -1 * (a.str("path").compareTo(b.str("path"))))
                .sorted((a, b) -> -1 * (a.i4("value") - b.i4("value")))
                .map(e->{
                    var rec = e.duplicate();// 复制实例
                    var cc = e.get("path",(String path)->path.split("/+"));//ά�ȱ任
                    rec.add("name",cc[1]);// 名称
                    rec.add("year",cc[2]);// 年份
                    rec.add("month",cc[3]);//月份
                    rec.add("day",cc[4]);//日期
                    return rec;
                })// ���ݱ任
            );
            System.out.println(FMT(dd));
        });
    }
}
