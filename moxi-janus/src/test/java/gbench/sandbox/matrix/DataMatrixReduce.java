package gbench.sandbox.matrix;

import gbench.commonApp.data.tushare.TuShareApp;
import org.junit.Test;
import gbench.common.fs.XlsFile.*;
import gbench.common.tree.LittleTree;
import gbench.common.tree.LittleTree.IRecord;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.fs.XlsFile.DColumn.*;
import static gbench.common.fs.XlsFile.DataMatrix.*;
import static java.util.Arrays.*;

public class DataMatrixReduce extends TuShareApp {

    @Test
    public void foo() {
        final var excel = new SimpleExcel("C:\\Users\\gbench\\Desktop\\myfile.xlsx");
        final var mx = excel.dblAutoDetect("Sheet1");
        final var tt = mx.flatMapColumns("A1,A2", p -> asList(COL(p), COL(p.getName().repeat(2), p.mapElems(e -> e * e))));
        final var ree = REC(0,new LittleTree.ExpressionEvaluator());
        final var t = mx.mapByRow(IRecord::REC).collect(DMC(e -> REC(
            "A1A2", ree.evalExpr("#A1*#A2",e)
        ).add(e)));
        
        System.out.println(t.toString(LittleTree.frt(2)));
        
        final var rec = tt.reduceColumns(dblsum, IRecord::REC);
        rec.add("n", tt.height());
        final var v = ree.evalExpr("Math.sqrt(#A1A1/#n- #A1*#A1/(#n*#n))",rec);
        System.out.println(v);
    }

}
