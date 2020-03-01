package gbench.sandbox.tushare;

import org.junit.jupiter.api.Test;
import static gbench.common.client.AbstractHttpClient.*;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.*;
import gbench.common.tree.LittleTree.IRecord;
import gbench.commonApp.data.tushare.TuShareApp;
import static gbench.common.fs.XlsFile.DataMatrix.*;

/**
 * 
 * @author gbench
 *
 */
public class ApiOfDaily extends TuShareApp{
    
    /**
     * 返回
     * @param args app的参数
     */
    @Test
    public void foo() {
        tushare.asyncInvoke("daily", 
            r("ts_code","000001.SZ", "start_date","20180701", "end_date","20190221"),
            smx->{
                final var tt = smx.eval2kvs("open,high,low,close",p -> {
                    final var mx =  L2REC(p.val())// 提取lcol转化为IRecord
                        .sliding2(5,1,IRecord::KVS2REC)// 提取窗口,5个一分组。
                        .filter(e->e.size()==5)// 提取完整
                        .collect(dblmxclc(e->REC(
                            "x",e.dbl(0),
                            "x_square",e.dbl(0)*e.dbl(0),
                            "y",e.dbl(4),
                            "y_square",e.dbl(4)*e.dbl(4),
                            "xy",e.dbl(0)*e.dbl(4)
                        )));// 分解矩阵
                    final var rec = mx.reduceColumns(dblsum,IRecord::REC).add("n",mx.height());// 增加数量
                    
                    return rec.add("cov",rec.evalExpr("#xy/#n-#x*#y/(#n*#n)"));// 计算方差
                }).stream().map(e->REC("label",e.key()).add(e.val()))
                .collect(objmxclc());
                
                System.out.println(tt.toString(frt(2)));
            });// smx
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
