package gbench.sandbox.tushare;

import static gbench.common.client.AbstractHttpClient.r;
import org.junit.Test;
import gbench.common.client.AbstractHttpClient.CBFHR;
import gbench.common.client.AbstractHttpClient.STR2STR;
import gbench.common.fs.XlsFile.StrMatrix;
import gbench.commonApp.data.tushare.TuShareApp;
import gbench.commonApp.data.tushare.TuShareClient;
import static gbench.commonApp.data.tushare.TuShareClient.*;

public class JunitTuShareFunction extends TuShareApp{
    @Test
    public void stock_basic() {
        final var apiName = "stock_basic";// api name
        final var $ = new TuShareClient(TUSHARE_URL, TUSHARE_TOKEN);// repect jQuery
        
        // api demo
        Object obj = $.ajax(r("api_name", apiName, "token", TUSHARE_TOKEN, 
            "params",   r("list_status", "L"), 
            "fields", "", //asList("symbol,name".split("")),
                "$success", (CBFHR)response -> {
                    StrMatrix mx = strmx(response, "$.data.fields", "$.data.items", 10);
                    System.out.println(mx);
                }));
        
        // api demo
        obj = $.sync(r("api_name", apiName,"$unescape",true, "token", TUSHARE_TOKEN, "params",
            r("list_status", "L"), "fields", "", //asList("symbol,name".split("")),
                "$success", (STR2STR)str -> {
                    return str;
                }));
        
        System.out.println("---"+obj);
    }
}
