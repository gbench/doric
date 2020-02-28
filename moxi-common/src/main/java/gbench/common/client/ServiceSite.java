package gbench.common.client;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

/**
 * 
 * @author gbench
 *
 */
public class ServiceSite {

    /**
     * 	获得客户端
     * @param cls
     * @return
     */
   public<T> T getClient(Class<T> cls){
	  return getClient(this.hostUrl,cls);
    }

    /**
     * 	构造客户端
     * @param hostUrl
     */
    public ServiceSite(String hostUrl) {
        this.hostUrl = hostUrl;
    }
    
    /**
     * 
     * @param hosturl:站点主机地址，
     * @param cls:提取的接口名
     * @return 接口对象
     */
    public static <T> T getClient(String hosturl,Class<T> cls){
        if(cls==null)return null;
       return  Feign.builder()
               .decoder(new GsonDecoder())
               .encoder(new GsonEncoder()).target(cls,hosturl);
    }

    private String hostUrl = null;// 服务域名
}
