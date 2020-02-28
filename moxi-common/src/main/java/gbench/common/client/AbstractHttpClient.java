package gbench.common.client;

import static gbench.common.tree.LittleTree.IRecord.REC;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.reactor.*;
import org.apache.http.util.EntityUtils;
import gbench.common.tree.LittleTree.IRecord;
import lombok.AllArgsConstructor;

/**
 * 	这是一个通用的HttpClient的实现:
 *  由于是抽象类所以实例化只能采用匿名类的方式 new AbstractHttpClient(
 * @author xuqinghua
 *
 */
@AllArgsConstructor
public abstract class AbstractHttpClient implements IHttpClient {
	
	/**
	 * abbre for REC
	 * @param oo
	 * @return
	 */
	public static IRecord r(Object ...oo) {
		return REC(oo);
	}
	
	/**
	 * abbre for REC
	 * @param oo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static<T> List<T> l(T ... oo) {
		return Arrays.asList(oo);
	}
	
	/**
	 * abbre for REC
	 * @param oo
	 * @return
	 */
	public static List<String> l(String str) {
		return l(str.split(","));
	}
	
	/**
	 * Callback For HtttpResponse, 类型别名
	 * @author xuqinghua
	 */
	public static interface CBFHR extends Consumer<HttpResponse>{};
	
	/**
	 * Callback For String, 类型别名
	 * @author xuqinghua
	 */
	public static interface CBFSTR extends Consumer<String>{};
	
	/**
	 * Callback For HtttpResponse, 类型别名
	 * @author xuqinghua
	 */
	public static interface STR2STR extends Function<String,String>{};
	
	/**
	 * Callback For HtttpResponse, 类型别名
	 * @author xuqinghua
	 */
	public static interface STR2T<T> extends Function<String,T>{};
	
	/**
	 * Callback For HtttpResponse, 类型别名
	 * @author xuqinghua
	 */
	public static interface T2U<T,U> extends Function<T,U>{};
	
	/**
	 * HttpResponse to  Unknown Type( 自定义类型), 类型别名
	 * @author xuqinghua
	 */
	public static interface HR2U<U> extends Function<HttpResponse,U>{};
	
	/**
	 * 
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String str(HttpResponse response) throws ParseException, IOException {
		   String content = EntityUtils.toString(response.getEntity(),"utf8");
           String  s= StringEscapeUtils.unescapeJava(content);//转码
           return s;
	}
	
	/**
	 * 把response转换成字符串
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String str2(HttpResponse response) {
		   String content = null;
		try {
			content = EntityUtils.toString(response.getEntity(),"utf8");
			 String  s= StringEscapeUtils.unescapeJava(content);//转码
			 return s;
		} catch (Exception e) {
			e.printStackTrace();
		}  
        return null;
	}
	
	/**
     * 设置请求参数
     * @param
     * @return
     */
    private static List<NameValuePair> rec2nvps(IRecord rec) {
    	Map<String,String>paramMap = rec.filter(kv->!kv._1().startsWith("$")).toStrMap();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Set<Map.Entry<String, String>> set = paramMap.entrySet();
        for (Map.Entry<String, String> entry : set) {
        	params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return params;
    }
    
	/**
	 * 记录转实体:注意这里把rec中的所有参数转换成HTTP请求参数．除掉了客户端自定义字段$开头字段名．
	 * @param rec　过滤掉所有以$开头的字段信息，他们是客户端的的自定义关键字
	 * @return
	 */
	public HttpEntity rec2entity(IRecord rec) {
		String json = rec.filter(e->!e._1().startsWith("$")).json();// 去除调$属性
		return json2entity(json);
	}
	
	/**
	 * 把json对象转换成请求实体，没有作子国过滤．
	 * @param json
	 * @return
	 */
	public HttpEntity json2entity(String json) {
		if(debug)System.out.println(json);
		byte bb[] = json.getBytes();
		InputStream inputStream = new ByteArrayInputStream(bb, 0, bb.length);
		HttpEntity entity =  new InputStreamEntity(inputStream,bb.length); // 请求体
		return entity;
	}
	
	/**
	 * 同步方法调用
	 * 需要在params中给出
	 * 　url接口地址
	 * 　$success的回调地址．
	 * 
	 * 模仿jquery 实现的异步请求处理函数,不过也可以通过param来指定为同步处理,
	 * 需要注意params所有以＄开头均为客户端的自定义关键字，需要不会被作为请求参数，提交给服务器．
	 * 通过指定
	 * @param params 请求的参数处理
	 * 		- $method: get,post,默认为 get
	 * 		- $encoding: gbk,utf8, 默认为 utf8
	 *  	- $unescape: 是否使用 StringEscapeUtils.unescapeJava
	 *		- $success:通过指定success的回调函数来实现 对请求返回的结果进行处理,
	 *			- 如果是 异步方式:success的返回接口是 Consumer<HttpResponse> 可以简写为 CBFHR
	 *			-如果是 同步方式:success的返回接口是 Function<String,HttpResponse> 可以简写为 STR2T
	 */
	@SuppressWarnings("unchecked")
	public Object sync(IRecord param) {
		param.set("$async", false);
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object get(IRecord param) {
		param.set("$method", "method");
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * 需要在params中给出
	 * 　url接口地址
	 * 　$success的回调地址．
	 * 
	 * 模仿jquery 实现的异步请求处理函数,不过也可以通过param来指定为同步处理,
	 * 需要注意params所有以＄开头均为客户端的自定义关键字，需要不会被作为请求参数，提交给服务器．
	 * 通过指定
	 * @param params 请求的参数处理
	 * 		- $method: get,post,默认为 get
	 * 		- $encoding: gbk,utf8, 默认为 utf8
	 *  	- $unescape: 是否使用 StringEscapeUtils.unescapeJava
	 *		- $success:通过指定success的回调函数来实现 对请求返回的结果进行处理,
	 *			- 如果是 异步方式:success的返回接口是 Consumer<HttpResponse> 可以简写为 CBFHR
	 *			-如果是 同步方式:success的返回接口是 Function<String,HttpResponse> 可以简写为 STR2T
	 */
	@SuppressWarnings("unchecked")
	public Object syncGet(IRecord param) {
		param.set("$method", "get");
		param.set("$async", false);
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object post(IRecord param) {
		param.set("$method", "post");
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object syncPost(IRecord param) {
		param.set("$method", "Post");
		param.set("$async", false);
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object delete(IRecord param) {
		param.set("$method", "delete");
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object syncDelete(IRecord param) {
		param.set("$method", "delete");
		param.set("$async", false);
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object patch(IRecord param) {
		param.set("$method", "patch");
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object syncPatch(IRecord param) {
		param.set("$method", "path");
		param.set("$async", false);
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object put(IRecord param) {
		param.set("$method", "put");
		return ajax(param);
	}
	
	/**
	 * 同步方法调用
	 * @param apiUrl
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object syncPut(IRecord param) {
		param.set("$method", "path");
		param.set("$async", false);
		return ajax(param);
	}
	
	/**
	 * 模仿jquery 实现的异步请求处理函数,不过也可以通过param来指定为同步处理,
	 * 需要注意params所有以＄开头均为客户端的自定义关键字，需要不会被作为请求参数，提交给服务器．
	 * 通过指定
	 * 
	 * @param params 请求的参数处理
	 * 	    - $timeout:客户端的连接超时
	 * 		- $method: get,post,默认为 post
	 * 		- $encoding: gbk,utf8, 默认为 utf8
	 *  	- $unescape: 是否使用 StringEscapeUtils.unescapeJava
 　*      - $succes_type:回调函数的类型，CBFHR,CBFSTR,STR2STR,HR2U, 默认为CBFHR
	 *		- $success:通过指定success的回调函数来实现 对请求返回的结果进行处理,
	 *			- 如果是 异步方式:success的返回接口是 Consumer<HttpResponse> 可以简写为 CBFHR
	 *			- 如果是 同步方式:success的返回接口是 Function<String,HttpResponse> 可以简写为 STR2T
	 *         - 以上是默认类型：不过也可以通过指定： $succes_type 来给予手动设置回调函数类型．
	
	 *		注意：$succes_type与$success 必须保持一致：否则程序就会卡死，这就悲剧了，我花费了一个昼夜才搞明白．
	 *		虽然这个函数是我写的．一般我们采用如下方式进行接口调用．
	 *
	 *		 "$succes_type","CBFSTR",
	 *		"$success", (CBFSTR)line -> {
	 *					System.out.println(line);
	 *			}));
	 *
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	public Object ajax(IRecord params) {
	  if(params.get("$async")!=null && params.bool("$async")==false) {// 同步调用
			return syncCall(params);// 同步请求
		}else {
			asyncCall(params);// 异步请求
			return true;
		}
	}
	
	/**
	 * 准备HTTP请求：默认是Post请求
	 * @param params　请求参数包括　客户端命令参数$xxx
	 * @return
	 */
	 public HttpUriRequest prepareRequest(IRecord params) {
		 HttpUriRequest request = null;
		 if("get".equals(params.str("$method"))){// 提取请求方法
				request = prepareGetRequest(params);
			}else {// more
				HttpPost post = new HttpPost(apiUrl);
				post.setEntity(rec2entity(params));
				request = post;
			}// if
		 return request;
	 }
	 
	/**
	 * 准备ＧetQ请求
	 * @param params
	 * @return
	 */
	public HttpGet prepareGetRequest(IRecord params) {
		var nvps = rec2nvps(params);// name to values
		String param = URLEncodedUtils.format(nvps, "UTF-8").trim();
		HttpGet get = new HttpGet();
		String _url = apiUrl;
		if( apiUrl.contains("?")) {
			int e = apiUrl.indexOf("?");
			_url = apiUrl.substring(0,e);
			String pp = apiUrl.substring(e+1).trim();
			if( pp.length()>0 && param.length()>0 ) param =param+"&"+pp; // url 中含有&
			else param = pp;
			_url =_url+"?"+param;
		}
		//System.out.println("url:--->"+_url);
		get.setURI(URI.create(_url));
		return get;
	}
	
	/**
	 * 同步调用
	 * @param params
	 * @return
	 */
	public Object syncCall(IRecord params) {
		final CloseableHttpClient httClient = HttpClients.createDefault();
		try {
			HttpUriRequest request = this.prepareRequest(params);
			//Executing the Get request
		    HttpResponse httpresponse = httClient.execute(request);
		    String encoding = params.str("$encoding");
		    if( encoding==null)  encoding="utf8";
		    Scanner sc = new Scanner(httpresponse.getEntity().getContent(),encoding);
		    Iterable<String> iterable = () ->sc;
		    Stream<String> targetStream = StreamSupport.stream(iterable.spliterator(), false);
		    String jsn= targetStream
		    		 .map(e->params.bool("$unescape")? StringEscapeUtils.unescapeJava(e):e)
		    		 .collect(Collectors.joining("\n"));
		   //String $succes_type="CBFHR";// 默认callback内容
		   Object obj = null;// 价格对象
		   try {obj = params.eval("$success",jsn);}catch(Exception ex) {ex.printStackTrace();}
		   sc.close();
		   httClient.close();
		   return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 异步调用,只有进行post
	 * @param params
	 */
	public void asyncCall(IRecord params) {
		RequestConfig requestConfig = null;
		var  timeout = 500;//过期默认值
		if(params.i4("$timeout")!=null)timeout=params.i4("$timeout");
		
		/**
		 * ConnectTimeout : 连接超时,连接建立时间,三次握手完成时间。 
		 * SocketTimeout : 请求超时,数据传输过程中数据包之间间隔的最大时间。 
		 * ConnectionRequestTimeout : 使用连接池来管理连接,从连接池获取连接的超时时间。
		 */
		requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout*2)
                .setSocketTimeout(timeout/2)
                .setConnectionRequestTimeout(timeout/3)
                .build();
		
		//配置io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
        		//.setIoThreadCount(Runtime.getRuntime().availableProcessors())
        		.setIoThreadCount(1)
                .setSoKeepAlive(false)
                .build();
        
		//设置连接池大小
        ConnectingIOReactor ioReactor=null;
        try {  ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);} 
        catch (IOReactorException e) { e.printStackTrace(); }
        
        PoolingNHttpClientConnectionManager connManager = 
        		new PoolingNHttpClientConnectionManager(ioReactor);
        connManager.setMaxTotal(1);
        connManager.setDefaultMaxPerRoute(1);
		
		// 构造客户端
		final CloseableHttpAsyncClient httpClient = //HttpAsyncClients.createDefault();
				HttpAsyncClients.custom()	
					.setDefaultRequestConfig(requestConfig)
					.setConnectionManager(connManager)
					.build();
		
		HttpUriRequest request = this.prepareRequest(params);
		httpClient.start();
		httpClient.execute(request,  new FutureCallback<HttpResponse>() {
				@Override public void cancelled() {
					System.out.println("cancelled");
					try {httpClient.close(); } catch (IOException e) { e.printStackTrace(); }
				}
				
				@Override public void failed(Exception arg0) {
					arg0.printStackTrace();
					try {httpClient.close(); } catch (IOException e) { e.printStackTrace(); }
				}
				
				/**
				 * 成功函数的回调
				 */
				@Override public void completed(HttpResponse response) {
					String $succes_type="CBFHR";// 默认callback内容
					if(null!=params.str("$succes_type"))$succes_type=params.str("$succes_type");
					//System.out.println("completed:"+$succes_type);
					switch($succes_type){
						case "STR2STR":params.eval("$success", str2(response));break;
						case "CBFSTR":params.callback("$success", str2(response));break;
						case "HR2U":params.eval("$success", response);break;
						default:params.callback("$success", response);// 默认 CBFHR 
					}//switch
					try {	httpClient.close();} catch (IOException e) { e.printStackTrace(); }
				}// completed
				
			});// 客户端请求
	}

	protected String apiUrl = null;	// http访问的url
	protected String token = null;	// http访问的token
	public static boolean debug = false;// 是否开始debug模式，显示过程的中间信息．
	
}
