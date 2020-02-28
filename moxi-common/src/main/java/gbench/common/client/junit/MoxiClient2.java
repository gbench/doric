package gbench.common.client.junit;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.Data;

/**
 * Moxi客户算使用示例
 * @author gbench
 *
 */
public class MoxiClient2{
	
	/**
	 * OSS 记录模型
	 * @author gbench
	 *
	 */
	public interface OssModel{
		
		/**
		 * OSS 数据保存接口
		 * @author gbench
		 *
		 */
		@Data
		public static class ResultMessage{int errorcode;String message;String url;}
		public static final String OSS_HOME = "test/gbench/moxi/thumbnail/2019-05-19/tmp";// url oss 上的文件保存位置
		
		/**
		 * 把文件转换字节数组
		 * @param fullname 文件的全路径
		 * @return 使用base64转换后的字符串
		 */
		public static String file2base64str(String fullname){FileInputStream fis=null; byte[] bb=null;
			try{fis=new FileInputStream(fullname);bb =new byte[fis.available()];fis.read(bb);}
			catch(Exception e) {e.printStackTrace();}finally{try{fis.close();}catch(Exception e){e.printStackTrace();}} 
			return Base64.getEncoder().encodeToString(bb);
		};
		
		/**
		 * 把window 路径名转换成unix路径名
		 * @param path 路径名
		 * @return unix 格式的path
		 */
		public static String unixpath(String path) {
			return path.replace("\\", "/");
		}
		
		/**
		 * 提取一个全路径文件的文件名
		 * @param fullname 文件全路径名，例如c:/a/b/c.jpg
		 * @return 文件的简单名，不包含路径,例如 c.jpg
		 */
		public static String namepicker(String fullname) {
			fullname = unixpath(fullname);
			if(fullname.indexOf("/")<0)return fullname;
			Matcher matcher = Pattern.compile("([^//]+$)").matcher(fullname);
			return matcher.find()?matcher.group(1):null;
		}
		
		/**
		 * 使用默认位置保存文件
		 * @param base64str 文件base64字符串
		 * @param myname 文件名称
		 * @return oss上的文件url
		 */
		@RequestLine("POST /oss/save2cloud?myname={myname}&base64str={base64str}")
		public ResultMessage __save2cloud__(@Param("base64str")String base64str,@Param("myname")String myname);
		
		/** 包文件保存在指定位置
		 * @param base64str 文件base64字符串
		 * @param myname 文件名称
		 * @param home 文件保存位置
		 * @return oss上的文件url
		 */
		@RequestLine("POST /oss/save2cloud?myname={myname}&base64str={base64str}&home={home}")
		public ResultMessage __save2cloud2__(@Param("base64str")String base64str,@Param("myname")String myname,@Param("home")String home);
	
		/**
		 * 把一个文件上换到 oss的的默认oss_home位置
		 * @param fullname 本地文件全路径
		 * @return oss上的文件url
		 */
		public default ResultMessage save2cloud(String fullname) {
			return __save2cloud__(file2base64str(fullname),namepicker(fullname));
		}
		
		/**
		 * 把一个文件上换到 oss的oss_home位置
		 * @param fullname 本地文件全路径
		 * @param home 保存在oss上的根目文件位置
		 * @return  oss上的文件url
		 */
		public default ResultMessage save2cloud2(String fullname,String oss_home) {
			return __save2cloud2__(file2base64str(fullname),namepicker(fullname),oss_home);
		}
	}
	
	/**
	 *	接口调用断点
	 * @author gbench
	 *
	 */
	public static class Endpoint{
		
		/**
	     * 	构造客户端
	     * @param hosturl
	     */
	    public Endpoint(String hosturl) {
	        this.hosturl = hosturl;
	    }
	    
		 /**
	     * 	获得客户端
	     * @param cls
	     * @return
	     */
	   public<T> T getClient(Class<T> cls){
		  return getClient(this.hosturl,cls);
	    }
	   
	    /**
	     * 
	     * @param hosturl:站点主机地址，比如：http://localhost:8081
	     * @param cls:提取的接口名，比如：OssModel
	     * @return 接口对象
	     */
	    public static <T> T getClient(String hosturl,Class<T> cls){
	        if(cls==null)return null;
	       return  Feign.builder().decoder(new GsonDecoder()).encoder(new GsonEncoder())
	               .target(cls,hosturl);
	    }

	    private String hosturl = null;// 服务域名
	}

	/**
	 * 使用示例
	 * @param args
	 */
	public static void main(String args[]) {
		OssModel ossModel = Endpoint.getClient("http://localhost:8081",OssModel.class);// 获取微服务客户端
		String[] pics=new String[] {// 图片集合
			"C:/Users/gbench/Pictures/Saved Pictures/avatar.jpg",
			"C:/Users/gbench/Pictures/Saved Pictures/avatar2.jpg"
		};
		
		long begTime = System.currentTimeMillis();
		// 数据遍历处理
		for(String pic:pics){ // 便利图片把图片加入到数OSS
			OssModel.ResultMessage result= ossModel.save2cloud2(pic,OssModel.OSS_HOME);
			System.out.println(result);
		};
		System.out.println("last for"+(System.currentTimeMillis()-begTime)+"ms");
	}
}
