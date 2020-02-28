package gbench.commonApp.data.tushare;

import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import gbench.common.client.AbstractHttpClient;
import gbench.common.fs.XlsFile.StrMatrix;
import java.io.IOException;

/**
 * 
 * @author xuqinghua
 *
 */
public class TuShareClient extends AbstractHttpClient {
    
    /**
     * 构造函数
     * @param apiUrl  api 服务器地址
     * @param token 接口访问token
     */
    public TuShareClient(String apiUrl , String token) {
        super(apiUrl, token);
    }

    /**
     * 根据json 数据结构，生成一个StrMatrix 矩阵．
     * @param json  json数据
     * @param header_path　表头路径 $.data.fields
     * @param items_path　表数据陆路径 $.data.items 
     * @param size　数据的最大长度
     * @return
     */
    @SuppressWarnings("unchecked")
    public static StrMatrix matrix(String json,String header_path,String items_path,int size) {
        JSONObject obj = JSON.parseObject(json);
        List<String> fields = (List<String>)JSONPath.eval(obj,header_path);// 1d array
        List<List<String>> items = (List<List<String>>)JSONPath.eval(obj,items_path); // 2d array
        StrMatrix m = new StrMatrix(items.subList(0, Math.min(size,items.size())),fields);
        return m;
    }
    
    /**
     * 根据response 数据结构，生成一个StrMatrix 矩阵．
     * @param response　ＨttpResponse的相应结构
     * @param header_path　表头路径 $.data.fields
     * @param items_path　表数据陆路径 $.data.items 
     * @param size 数据的最大长度
     * @return StrMatrix
     * @throws ParseException
     * @throws IOException
     */
    public static StrMatrix matrix( HttpResponse response,String header_path,String items_path,int size)
        throws ParseException, IOException {
        return  matrix(str(response),header_path,items_path,size);
    }
    
    /**
     * 根据response 数据结构，生成一个StrMatrix 矩阵．
     * @param response Tushare 的Http数据请求．
     * @param header_path　表头路径 $.data.fields
     * @param items_path　表数据陆路径 $.data.items 
     * @param size 数据的最大长度
     * @return StrMatrix
     */
    public static StrMatrix strmx( HttpResponse response,String header_path,String items_path,int size) {
        StrMatrix mx = null;
        try {
            mx = matrix(response,header_path,items_path,size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return mx;
    }
}