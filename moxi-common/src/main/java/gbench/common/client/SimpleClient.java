package gbench.common.client;

import feign.Param;
import feign.RequestLine;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 	简单的客户端
 */
public class SimpleClient {
    @Setter @Getter @ToString
    class Item{
        String field;
        String label;
        String defaul;
        String inptype;
        String options;
        String remarks;
    }

    /**
     * 
     * @author gbench
     *
     */
    interface MoxiCatalogService {
        /**
         * 	获取请求
         * @param table_scheme
         * @param sysid
         * @return
         */
        @RequestLine("POST /pctattrs?path={path}&sysid={sysid}&bundleId={code}")
        List<Item> getAttributes(@Param("path")String path,@Param("sysid")int sysid,@Param("bundleId")int bundleId);
        
        @RequestLine("POST /catalog/product/saveorupdate?goods={goods}")
        Map<String,Object> saveOrUpdate(@Param("goods")String goods);
    };

    /**
     *
     * @param args
     */
    @SuppressWarnings("unused")
    public static void main(String args[]){
        ServiceSite site = new ServiceSite("http://localhost:8081");// 获取微服务站点
        MoxiCatalogService client = site.getClient(MoxiCatalogService.class);// 获取微服务客户端
       
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        String path = "根节点:0:1/芳烃:429:1/二甲苯:17:1";
        int sysid = 2;
        int bundleId=17;
        
        // 执行微服务方法
        for (Item item : client.getAttributes(path,sysid,bundleId)) {
            System.out.println(item);
        }
        Map<String,Object> map = new HashMap<>();
      
        System.out.println(client.saveOrUpdate("{\"name\":\"zhangsan\",\"sex\":\"男\"}"));
    }

}