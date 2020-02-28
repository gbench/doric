package gbench.sandbox.matrix;

import org.junit.Test;

import gbench.common.fs.XlsFile.DataMatrix;
import gbench.common.tree.LittleTree.DataFrame;
import gbench.commonApp.data.DataMatrixApp;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.DataFrame.*;
import java.time.LocalDate;

public class JunitRecord extends DataMatrixApp {
    @Test
    public void foo() {
        DataFrame dfm = DFM("name","zhangsan","sex",false,"birthday",LocalDate.now(),
            "phone",L(13120751773l,"041183380336"));
        println(dfm);
        println("record  与 DataFrame 是等价的,区别就是dfm 是按照列的形式进行构建。");
        var rec = REC("name","zhangsan","sex",false,"birthday",LocalDate.now(),
            "phone",L(13120751773l,"041183380336"));
        println(rec);
        println(rec.toString2());
        println(REC("name","zhangsan").shape());
        println(REC().shape());
        
        println("对象数组");
        var oo = dfm.toArray2();
        println(DataMatrix.fmt(oo));
        println("字符串数组");
        var ss = dfm.toArray2(o->o+"");
        println(DataMatrix.fmt(ss));
        //println("日期型数组");// 执行失败
        //var tt = dfm.toArray2(o->(LocalDate)o);
        //println(DataMatrix.fmt(tt));
        var xx = REC().toArray2(e->e);// 返回[]数组
        System.out.println(" 空REC()的构造:"+DataMatrix.fmt(xx)+"");
    }
}
