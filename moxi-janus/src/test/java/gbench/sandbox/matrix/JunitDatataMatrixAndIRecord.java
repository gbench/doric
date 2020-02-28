package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import static gbench.common.tree.LittleTree.DataFrame.*;
import static gbench.common.tree.LittleTree.CronTime.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.common.fs.XlsFile.*;
import gbench.common.fs.XlsFile.Column;
import java.time.LocalDate;

import static gbench.common.tree.LittleTree.*;

public class JunitDatataMatrixAndIRecord extends DataMatrixApp{
    @Test
    public void foo() {
        var df = DFM("name","zhangsan","sex","man","birth",LocalDate.now());
        var ll = RPTS(100,df);
        println(df);
        var mm = ll.collect(smc);
        println(mm);
        println(df.column(0));
        println(df.row(0));
        println(df.column("birth",LocalDate.class));
        println(df.column("birth",(LocalDate d)->dtf("yyyy").format(d)));
        println("------------");
        var dd = df.columns(LocalDate.class);// 会出现错误的类型构造，但是在编译期间不予给与体现。给错误
        dd.forEach(col->{
            System.out.println(col);
        });
        
        var pp= df.columns();
        pp.forEach(col->{
            System.out.println(col);
        });
    }
    
    /**
     * DataMatrix与IRecord之间的转换
     */
    @Test
    public void bar() {
        // Cells 列表
        final var cc = L(
            L(1,2,3,4,5,7,8,9,0),
            L(1,2,3,4)
        );// 数据单元集合
        var mm = new DataMatrix<>(cc,L("A"));
        println("数据矩阵");
        println(mm);
        
        var t= mm.mapByRow(IRecord::REC).collect(omc);
        println("record 集合转数据矩阵");
        println(t);
        
        var rec = t.reduceColumns(Column::getElems,IRecord::REC);
        println("数据矩阵转Record");
        println(rec.toString2());
        
    }
}
