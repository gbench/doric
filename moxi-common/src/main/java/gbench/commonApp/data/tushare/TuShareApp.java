package gbench.commonApp.data.tushare;

import static gbench.common.client.AbstractHttpClient.r;
import static gbench.common.fs.XlsFile.DataMatrix.dblsum;
import static gbench.common.tree.LittleTree.IRecord.L2REC;
import static gbench.common.tree.LittleTree.IRecord.REC;
import gbench.common.tree.LittleTree.IRecord;
import gbench.commonApp.data.DataMatrixApp;

/**
 * Tushare的应用实例
 * 
 * 
 * @author gbench
 *
 */
public class TuShareApp extends DataMatrixApp{

    /**
     * 
     * @param url   请求的url
     * @param token 请求的url
     * @return TuShareDatabase
     */
    public static TuShareApi getApi(String url, String token) {
        return new TuShareApi(TUSHARE_URL, TUSHARE_TOKEN);
    }

    /**
     * 
     * @return TuShareDatabase
     */
    public static TuShareApi getDatabase() {
        return getApi(TUSHARE_URL, TUSHARE_TOKEN);
    }

    protected static String TUSHARE_URL = "https://api.tushare.pro";
    protected static String TUSHARE_TOKEN = "6ce350a15c9171f97126dc3300e252ee1e1a765461997362099a1a06";
    protected static TuShareApi tushare = getDatabase(); // app的API
    
    /**
     * 返回
     * @param args app的参数
     */
    public static void main(String args[]) {
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
                
                System.out.println(tt);
            });// smx
    }
    
}
