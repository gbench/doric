package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.commonApp.data.DataMatrixApp;
import static gbench.common.tree.LittleTree.DataFrame.*;
import static gbench.common.tree.LittleTree.CronTime.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.common.fs.XlsFile.*;
import gbench.common.fs.XlsFile.DColumn;
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
        
        // IRecord的类型转换
        println("数据矩阵");
        println(mm);
        
        var mx= mm.mapByRow(IRecord::REC).collect(omc);
        println("record 集合转数据矩阵");
        println(mx);
        
        var rec_x = mx.reduceRows(DRow::getElems,IRecord::REC);
        System.out.println("数据矩阵转Record(reduceRows) 产生一个 record:");
        System.out.println(rec_x.toString2(e->MFT("#{0}",e),frt(2)));
        
        var rec_y = mx.reduceColumns(DColumn::getElems,IRecord::REC);
        println("数据矩阵转Record(reduceColumns):产生一个 record:");
        println(rec_y.toString2(e->MFT("#{0}",e),frt(2)));
        
    }
}
