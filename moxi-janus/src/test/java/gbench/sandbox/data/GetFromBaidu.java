package gbench.sandbox.data;

import gbench.commonApp.data.DataApp;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.common.client.AbstractHttpClient;
import static gbench.common.client.AbstractHttpClient.*;
import static gbench.common.tree.LittleTree.*;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import gbench.common.fs.XlsFile.StrMatrix;
import java.io.IOException;

/**
 * 
 * @author gbench
 *
 */
public class GetFromBaidu extends DataApp{
    
    public static class TuShareClient extends AbstractHttpClient{

        public TuShareClient(String apiUrl, String token) {
            super(apiUrl, token);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public static StrMatrix matrix(String json,String header_path,String items_path,int size) {
        JSONObject obj = JSON.parseObject(json);
        List<String> fields = (List<String>)JSONPath.eval(obj,header_path);// 1d array
        List<List<String>> items = (List<List<String>>)JSONPath.eval(obj,items_path); // 2d array
        StrMatrix m = new StrMatrix(items.subList(0, Math.min(size,items.size())),fields);
        return m;
    }
    
   
    public static StrMatrix matrix( HttpResponse response,String header_path,String items_path,int size)
        throws ParseException, IOException {
        return  matrix(str(response),header_path,items_path,size);
    }
    
    public static StrMatrix strmx( HttpResponse response,String header_path,String items_path,int size) {
        StrMatrix mx = null;
        try {
            mx = matrix(response,header_path,items_path,size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return mx;
    }
    
    /**
     * 
     * @param args
     */
    @Test
    public void foo() {
        cfg.set("url", "jdbc:mysql://localhost:3306/chaos?serverTimezone=GMT%2B8");
        reload();
        jdbc.withTransaction(sess->{
            sess.psql2records("select title from t_enterprise limit 10",A()).forEach(rec->{
                final var name = rec.str("title");
                var url = MFT("https://www.baidu.com/s?wd={0}",name);
                System.out.println(url);
                var $ = new TuShareClient(url,null);// repect jQuery
                $.ajax(r(
                    "User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36",
                    "$succes_type","STR2STR",
                    "$success",(STR2STR)str -> {
                        System.out.println(str);
                        return str;
                    }
               ));
                
            });
           
        });
    }
}
