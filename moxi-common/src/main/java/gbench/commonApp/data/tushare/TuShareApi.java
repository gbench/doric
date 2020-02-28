package gbench.commonApp.data.tushare;

import java.util.List;
import java.util.function.Consumer;

import gbench.common.fs.XlsFile.StrMatrix;
import gbench.common.tree.LittleTree.IRecord;

/**
 * TuShare 数据源
 * @author xuqinghua
 *
 */
public class TuShareApi extends TuShareClient{

    /**
     * 构造函数
     * @param url 请求的url
     * @param token 请求的token
     */
    public TuShareApi(String url,String token) {
        super(url, token);
    }
    
    /**
     * 异步接口调用
     * @param apiName api的名称
     * @param params 请求的参数
     * @param fields 返回的字段列表,null 返回所有列数据
     * @param callback 回调函数
     */
    public void asyncInvoke(String apiName,IRecord params,List<String> fields,Consumer<StrMatrix> callback) {
        this.ajax(r("api_name", apiName,"$unescape",true, "token", token,"$async",true, "$timeout",4000,
            "params",params, // 对于tushare 一定要出现在params中的参数中，这个问题我花费了一晚上才给予明白．
            "fields",fields==null?l():fields,//结果返回列
             "$succes_type","HR2U",
             "$success", (HR2U<StrMatrix>)response -> {
                 StrMatrix mx = null;
                 try {
                     mx = strmx(response, "$.data.fields", "$.data.items", 10000);
                     if(callback!=null)callback.accept(mx);
                 }catch(Exception e) {
                     e.printStackTrace();
                 }//try
                return mx;
            }));
    }
    
    /**
     * 异步接口调用
     * @param apiName api的名称
     * @param params 请求的参数
     * @param callback 回调函数
     */
    public void asyncInvoke(String apiName,IRecord params,Consumer<StrMatrix> callback) {
       this.asyncInvoke(apiName,params,null,callback);
    }
    
    /**
     * 异步接口调用
     * @param apiName
     * @param params
     * @param fields
     * @return
     */
    public StrMatrix syncInvoke(String apiName,IRecord params,List<String> fields) {
        return (StrMatrix) this.sync(r("api_name", apiName,"$unescape",true, "token", token,"$async",false, "$timeout",4000,
            "params",params, // 对于tushare 一定要出现在params中的参数中，这个问题我花费了一晚上才给予明白．
            "fields",fields==null?l():fields,//结果返回列
             "$success", (T2U<String,StrMatrix>)line -> {
                 StrMatrix mx = matrix(line, "$.data.fields", "$.data.items", 10000);
                 return mx;
            }));
    }

}