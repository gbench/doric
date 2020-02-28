package gbench.sandbox.data;

import static gbench.common.tree.LittleTree.IRecord.L;
import static gbench.common.tree.LittleTree.IRecord.REC;
import org.junit.Test;
import gbench.common.fs.FileSystem;
import gbench.common.fs.XlsFile.DataMatrix;
import gbench.common.fs.XlsFile.SimpleExcel;
import gbench.common.tree.LittleTree;
import gbench.common.tree.LittleTree.*;
import gbench.commonApp.data.DataApp;
import gbench.data.json.JSN;


public class PivotTableOfRecord extends DataApp{
    
    @Test
    public void bar() {
        var roster = REC( 
            "姓名",L("张三","李四","王五","赵六"),
            "城市",L("北京","上海","上海","北京"),
            "性别",L("男","男","女","女")
        );
        System.out.println(roster.toString2());
        var p = roster.pivotTable("性别,城市");
        System.out.println(JSN.pretty(p));
    }

    @Test
    public void foo() {
        cfg.set("url", "jdbc:mysql://localhost:3306/salespro?serverTimezone=GMT%2B8"); reload();
        jdbc.withTransaction(sess->{
            final var sql = "select clientele_name,deliver_place from `order`";
            final var rr = sess.sql2records(sql);
            final var excel = new SimpleExcel("d:/order.xlsx",false);
            @SuppressWarnings("unused")
            final var res = IRecord.pivotTable(rr, "clientele_name,deliver_place",LittleTree::LIST);
            final var vv = jdbc.pmatrix2(DataMatrix::new, "select *from `order` limit 40000");
            FileSystem.utf8write("D:/a.csv",()->vv.toString());
            //System.out.println(vv);
            //excel.write("order!a1", vv);
            excel.save();
            excel.close();
        });
    }
}
