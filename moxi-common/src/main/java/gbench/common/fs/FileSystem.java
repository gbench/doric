package gbench.common.fs;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Supplier;
import gbench.common.fs.FileSystem;
import static gbench.common.tree.LittleTree.MFT;
import java.io.*;


/**
 * 
 * @author gbench
 *
 */
public class FileSystem {
	/**
	 * 	生成当前类的默认数据存放路径：用于范文系统资源。
	 *   	1)当clazz为null时候,优先判断 file 是否构成一个本地文件路径,如果构成 则直接返回
	 *   		否则尝试在类文件根路径进行(FileSyste.class加载器的根路径)文件找寻：
	 *   		类似于 C:\Users\gbench\eclipse-workspace\Plutus\misc-service-provider\target\
	 *   		如果存在则相应文件则返回对应路径，否则返回file
	 *   	2) 当class不为空则在class同级的目录下寻找对应的文件
	 *   路径中 "\"会被替换成"/",并且多个"/"会被视做一个：即"/a//b/c"会被视作"/a/b/c"
	 *   
	 *   path for(file:eg2001_900000,class:class gbench.web.apps.finance.kline.KLineController):
     *   nullgbench/web/apps/finance/kline/eg2001_900000
     *   
     *   path for(file:nullgbench/web/apps/finance/kline/eg2001_900000,class:null):
     *   nullgbench/web/apps/finance/kline/eg2001_900000
	 *   
	 * @param file 文件路径
	 * @param clazz 相对于位置的基点：与file文件同级的目录的class文件,null 则返回file本身
	 * @return file的文件的路径：可以通过new File(path(file,class)) 可以直接读取文件。
	 * 当采用jar问运行的时候是相对路径：
	 * 	比如：path for(file:nullgbench/web/apps/finance/kline/eg2001_60000,class:null):
	 *        nullgbench/web/apps/finance/kline/eg2001_60000
	 *  new File(new File(path(file,class)))。getAbsolutePath():
	 *  却可以返回相对于运行路径D:\sliced\ws\gitws\moxi-agent\moxi\target的绝对路径
	 *	D:\sliced\ws\gitws\moxi-agent\moxi\target\nullgbench\web\apps\finance\kline\eg2001_60000
	 *
	 * 当文件展开运行的时候是绝对路劲：
	 */
	public static String path(String file,Class<?>clazz) {
		String path = null;// 结果返回值
		if(new File(file).exists()) {// file本身业已构成完整的文件路径
			path = file;// 绝对存在由对应的文静则直接返回该路径：即这是一个有效的文件路径。
		} else {// 构造一个相对于clazz所在位置的文件路径。
			var classLoader = clazz!=null?clazz.getClassLoader() //  根据classloader 以资源的方式获取路径
				:FileSystem.class.getClassLoader();// 默认为FileSystem.class所在的根路径位置。
			String home = null;// 基准根路径
			try {// 注意需要采用.toURI()把：包路径
				home = classLoader.getResource("").toURI().getPath()+// classLoader  下的根路径
					(clazz==null?"":clazz.getPackage().getName().replace(".", "/")); // 非空的clazz加入相对于类根路径
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			path = (home+"/"+file).replace("\\", "/").replaceAll("/+", "/");// 路径拼接。
		}//if
		//System.out.println("path for(file:"+file+",class:"+clazz+"):\n\t"+path+"\n");
		return path;
	}
	
	/**
	 *	 创建或者读取文件夹
	 * @param dir 文件路径
	 * @param cls 类路径
	 * @return
	 */
	public static File dir(String dir,Class<?>cls) {
		String home = FileSystem.path(dir, cls);
		File fhome = new File(home);
		if(!fhome.exists())fhome.mkdirs();
		return fhome;
	}
	
	/**
	 * 	读取文件所有行
	 * 
	 * @param file 文件对象
	 * @param encoding 文件编码
	 * @return
	 */
	public static Stream<String> lines(String file,String encoding){
		return lines(new File(file),encoding);
	}
	
	/**
	 * 	读取文件所有行
	 * 
	 * @param file 文件对象
	 * @param encoding 文件编码
	 * @return
	 */
	public static Stream<String> lines(File file,String encoding){
		Stream<String> stream = null;
		try {
			stream = lines(new FileInputStream(file),encoding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stream;
	}
	
	/**
	 * 	读取文件所有行
	 * 
	 * @param file 文件对象
	 * @param encoding 文件编码
	 * @return
	 */
	public static Stream<String> lines(InputStream is,String encoding){
		Stream<String> stream = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,encoding));
			stream = br.lines().collect(Collectors.toList()).stream();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stream;
	}
	
	 /**
	  * 读取一个url成为文本字符串
	  * @param apiUrl 数据的url地址
	  * @return 处理后的文本信息
	  */
	 public static String url2text(String pageUrl,Function<BufferedReader,String> handler) {
		 String buffer = null;
		 try {
			 URL url = new URL(pageUrl);
			 HttpURLConnection connection = (HttpURLConnection) url.openConnection();// 打开URL连接
			 connection.setDoOutput(false); connection.setDoInput(true);// 只读不写
			 connection.setRequestMethod("GET");// 使用GET方式
			 connection.setUseCaches(true);//不用缓存
			 connection.setInstanceFollowRedirects(true);//自动执行 HTTP 重定向
			 connection.setConnectTimeout(5000);//超时间5秒
			 connection.connect();// 开始连接
			 if (connection.getResponseCode() != 200) return buffer;
			 //200表示：成功返回，于是逐行读取数据
			 BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf8"));
			 buffer = handler.apply(br);
			 connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		 return buffer;
	 }
	
	 /**
	  * 把一个pathname:既可以是一个绝对路径也可以是是一个相对路径，还可以是一个文件名。所以就叫他pathname
	  * @param pathname 资源的位置 
	  * @param relativeClass 参考位置
	  * @return InputStream
	  */
	@SuppressWarnings("resource")
	public static InputStream pathname2stream(String pathname,Class<?>relativeClass) {
		final var path = path(pathname,relativeClass);// 提取文件位置。
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(path);
		}catch(FileNotFoundException e) {
			inputStream = relativeClass.getResourceAsStream(pathname);
		}
		return inputStream;
	 }
	
	/**
	 * 	读取文件所有行
	 * 
	 * @param file 文件对象
	 * @param encoding 文件编码
	 * @return
	 */
	public static Stream<String> utf8lines(File file){
		return FileSystem.lines(file,"utf8");
	}
	
	/**
	 * 	读取文件所有行
	 * 
	 * 首先把尝试用path(pathOrfilename)作为绝对路径进行文件读取，如果读取失败，就用relativeClass作为
	 * 资源流relativeClass.getResourceAsStream(pathOrfilename) 来读取文件。
	 * 
	 * @param pathOrfilename 文件路径
	 * @param mypos 相对于relativeClass的位置路径
	 * @return 数据行流
	 */
	public static Stream<String> utf8lines(String pathOrfilename,Class<?> relativeClass){
		File f = new  File(path(pathOrfilename,relativeClass));
		if(f.exists())return FileSystem.lines(f,"utf8");;
		InputStream is = relativeClass.getResourceAsStream(pathOrfilename);
		return FileSystem.lines(is,"utf8");
	}
	
	/**
	 * 	读取文件所有行
	 * 
	 * @param pathOrfilename 文件路径
	 * @param mypos 先对于relativeClass的w位置
	 * @return 数据行流
	 */
	public static Stream<String> utf8lines(InputStream is,Class<?> relativeClass){
		return FileSystem.lines(is,"utf8");
	}
	
	/**
	 * 	分段读取
	 * @param file 文件路径
	 * @param pattern 分段标记
	 * @param n 提取key的标记位置
	 * @return 分段key->lines Map
	 */
	public static Map<String,List<String>> split(String file,String pattern,int n){
		Map<String,List<String>> map = new HashMap<>();
		List<String> keys = new ArrayList<String>();
		final Pattern pat = Pattern.compile(pattern);
		FileSystem.utf8lines(new File(file)).forEach(e->{
			Matcher mat = pat.matcher(e);
			if(mat.find()) {
				String key = mat.group(1);
				map.computeIfAbsent(key, k-> new LinkedList<>());
				keys.add(key);
			}else if(!e.matches("^\\s*$")) {
				String key = keys.size()<1?"-":keys.get(keys.size()-1);
				if(map.get(key)!=null)map.get(key).add(e);
			}//if
		});
		return map;
	}
	
	/**
	 * 	文件遍历
	 * @param file
	 * @param cs
	 */
	public static void tranverse(File file,Consumer<File>cs) {
		if(file==null||!file.exists())return;
		if(file.isFile())cs.accept(file);
		else if(file.isDirectory()) Arrays.stream(file.listFiles()).forEach(cs);
	}
	
	/**
	 * 
	 * @param file 文件绝对路径
	 * @param contentSuppler  文件书写内容
	 */
	public static boolean utf8write(String file,Supplier<String> contentSuppler) {
		return write(new File(file),"utf8",contentSuppler);
	}
	
	/**
	 * 
	 * @param fullname 文件绝对路径
	 * @param contentSuppler  文件书写内容
	 */
	public static boolean write(String fullname,String encoding,Supplier<String> contentSuppler) {
		return write(new File(fullname),encoding,contentSuppler);
	}
	
	/**
	 * 
	 * @param file 文件对象
	 * @param contentSuppler  文件书写内容
	 */
	public static boolean write(File file,String encoding,Supplier<String> contentSuppler) {
		if(contentSuppler==null || file == null )return false;
		BufferedWriter bw = null;
		try {
			File pfile = file.getParentFile();
			if(!file.exists())pfile.mkdirs();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),encoding));
			bw.write(contentSuppler.get());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {bw.close();} catch (IOException e) {e.printStackTrace(); return false;}
		}
		
		return true;
	}
	
	/**
	 * 	管道处理
	 * @author gbench
	 *
	 * @param <T> 输入类型
	 * @param <U> 输出类型
	 */
	public static class Pipe<T,U>{
		
		/**
		 * 	创建一条管道
		 * @param tu_handler 管道的处理方式
		 * @param input 起始点的管道数据输入
		 */
		public Pipe(Function<T,U> tu_handler,Supplier<T> supplier){
			this.tu_handler = tu_handler;
			this.supplier = supplier;
		}
		
		/**
		 * 	创建一条管道
		 * @param tu_handler 管道的处理方式
		 * @param input 起始点的管道数据输入
		 */
		public Pipe(Function<T,U> tu_handler,T input){
			this.tu_handler = tu_handler;
			this.supplier = ()->input;
		}
       
		/**
		*	 管道的处理连接
		* @param tu_handler 管道连接处理方式
		*/
		private Pipe(Function<T,U> tu_handler){
			this.tu_handler = tu_handler;
		}
		       
		/**
		* 	管道连接，连接处两个管道节点，在原来的管道出口接上一段新的管道节点，使用tx_handler
		* 	作为管道节点的处理函数
		* @param tx_handler 管道连接处理方式
		* @return
		*/
		public <X> Pipe<U,X> pipe(Function<U,X> tx_handler){
			Pipe<U,X> p = new Pipe<>(tx_handler);
			p.pre = this;
			return p;
		}

		/**
		* 	使用指定的入口值input尽心管道计算
		* @param input 指定的管道入口之
		* @return 整条 管道链的计算结果
		*/
		@SuppressWarnings("unchecked")
		public U evaluate(Object input){
			Stack<Pipe<?,?>> stack = new Stack<>();// 管道计算，擦除掉管道的计算类型信息,以便进行管道的连接的尾巴连接。
			stack.push(this);
			Pipe<?,?> p = this.pre;// 获取前一个处理节点
			while(p!=null){stack.push(p); p=p.pre;}
				Pipe<?,?> piper = stack.pop();// init pipe
				Object v = input==null?piper.input():input;// 确认输入信息: v是Value的缩写
				if(v==null)return null;// 没有有效输入项
				do{//从这里开始依次执行
				v=((Pipe<Object,Object>)piper).tu_handler.apply(v);// 管道计算覆写先前的value.
			    piper = stack.empty()?null:stack.pop();
			}while(piper!=null);// do
			
			return (U)v;// 结果返回，恢复到指定类型
		}
		       
		/**
		* 	使用初始管道入口值进行数计算
		* @return 整条 管道链的计算结果
		*/
		public U evaluate(){
			return evaluate(null);
		}
		
		public T input() {
			return supplier.get();
		}
   
		private Function<T,U> tu_handler = null;// 管道的处理方式
		private Pipe<?,T> pre = null;// 前一段的管道节点
		private Supplier<T> supplier;//管道的接口收入
	};
	
	/**
	 * 	定义一个变量集合
	 * @param objs
	 * @return
	 */
	public static Properties defvars(Object ... objs){
		Properties props = new Properties();
		for(int i=0;i<objs.length-1;i++)props.put(objs[i], objs[i+1]);
		return props;
	}
	
	/**
	 * 把以camel命名转换成snake命名
	 * AbcDefg 变成　abc_defg
	 * @param line
	 * @return
	 */
	public static String camel2snake(String line) {
		var p = P("([A-Z])");
		Properties props = new Properties();
		for(char c = 'a';c<'z';c++) props.put(Character.toUpperCase(c)+"", MFT("_{0}",c));
		return replacen(p,line,props,1).replaceAll("^_+", "");
	}
	
	/**
	 * 
	 * @param pattern 页面模式
	 * @param line 行数据
	 * @return
	 */
	public static Matcher matcher(String pattern,String line) {
		if(pattern==null || line==null )return null;
		return Pattern.compile(pattern).matcher(line);
	}
	
	/**
	 * case sensive
	 * @param pattern
	 * @return
	 */
	public static Pattern P(String pattern) {
		return Pattern.compile(pattern);
	}
	
	/**
	 * case insensive
	 * @param pattern
	 * @return
	 */
	public static Pattern P2(String pattern) {
		return Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
	}
	
	/**
	 * map -->Properties
	 * @param map
	 * @return
	 */
	public static Properties map2props(Map<String,String> map) {
		Properties props = new Properties();
		map.forEach(props::put);
		return props;
	}
	
	/**
	 * 	内容替换,把模式中的数据给与替换
	 * @param pattern 页面模式
	 * @param line 行数据
	 * @return
	 */
	public static String replace(String pattern,String line,Map<String,String> map) {
		return replacen(P(pattern),line,map2props(map),null);
	}
	
	/**
	 * 	内容替换,把模式中的数据给与替换
	 * @param pattern 页面模式
	 * @param line 行数据
	 * @return
	 */
	public static String replace(String pattern,String line,Properties props) {
		return replacen(P(pattern),line,props,null);
	}
	
	/**
	 * 	内容替换,把模式中的数据给与替换
	 * @param pattern 页面模式
	 * @param line 行数据
	 * @return
	 */
	public static String replace(Pattern pattern,String line,Map<String,String> map) {
		return replacen(pattern,line,map,null);
	}
	
	/**
	 * 	内容替换,把模式中的数据给与替换
	 * @param pattern 页面模式
	 * @param line 行数据
	 * @return
	 */
	public static String replace(Pattern pattern,String line,Properties props) {
		return replacen(pattern,line,props,null);
	}
	
	/**
	 * 
	 * 	内容替换,把模式中的数据给与替换
	 * @param pattern 页面模式:
	 * @param line 行数据:pattern模式所对应的字符串
	 * @param map 行数据:pattern的中的第个k个分组岁key的具体的数值．
	 * @param n 变量替换的分组,pattern分组的所在map中俄key．
	 * @return
	 */
	public static String replacen(Pattern pattern,String line,Map<String,String> map,Integer  n) {
		return replacen(pattern,line,map2props(map),n);
	}
	
	/**
	 * 
	 * 	内容替换,把模式中的数据给与替换
	 * @param pattern 页面模式:
	 * @param line 行数据:pattern模式所对应的字符串
	 * @param map 行数据:pattern的中的第个k个分组岁key的具体的数值．
	 * @param n 变量替换的分组,pattern分组的所在map中俄key．
	 * @return
	 */
	public static String replacen(Pattern pattern,String line,Properties props,Integer  n) {
		String buffer = line;
		Matcher matcher = pattern.matcher(new String(line));
		StringBuilder builder = new StringBuilder();// 返回值
		while(buffer.length()>0) {
			if(!matcher.find()) {builder.append(buffer);break;}
			String key = (n==null)?matcher.group():matcher.group(n);
			builder.append(buffer.substring(0,matcher.start()));
			builder.append(props.getOrDefault(key, matcher.group()));	
			buffer = buffer.substring(matcher.end());// 截取
			matcher = pattern.matcher(buffer);
		}
		return builder.toString();
	}
	
	/**
	 * 	变量注入
	 * @param line 行数据
	 * @param vars 变量集合
	 * @return
	 */
	public static String inject(String line,Properties vars) {
		String buffer = line;
		String pattern = "\\$*\\{\\s*([^\\}]*)\\s*\\}";
		Matcher matcher = matcher(pattern,new String(line));
		StringBuilder builder = new StringBuilder();// 返回值
		while(buffer.length()>0) {
			if(!matcher.find()) {builder.append(buffer);break;}
			String key = matcher.group(1);
			builder.append(buffer.substring(0,matcher.start()));
			builder.append(vars.getOrDefault(key, matcher.group()));	
			buffer = buffer.substring(matcher.end());// 截取
			matcher = matcher(pattern,buffer);
		}
		return builder.toString();
	}
	
	/**
	 * 	执行一个shell命令，并返回字符串值
	 *
	 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
	 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
	 * @return 执行结果组成的字符串
	 * @throws IOException
	 */
	public static synchronized void shell(String cmds,Consumer<Map<String,Object>> callback) {
		shell(cmds.split("[\\s+]"),null,System.out::println,System.out::println,callback);
	}
	
	/**
	 * 	执行一个shell命令，并返回字符串值
	 *
	 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
	 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
	 * @return 执行结果组成的字符串
	 * @throws IOException
	 */
	public static synchronized void shell(String cmds[],Consumer<Map<String,Object>> callback) {
		shell(cmds,null,System.out::println,System.out::println,callback);
	}
	
	/**
	 * 	执行一个shell命令，并返回字符串值
	 *
	 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
	 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
	 * @return 执行结果组成的字符串
	 * @throws IOException
	 */
	public static synchronized void shell(String cmds,String workingdirectory,Consumer<Map<String,Object>> callback) {
		shell(cmds.split("[\\s+]"),workingdirectory,System.out::println,System.out::println,callback);
	}
	
	/**
	 * 	执行一个shell命令，并返回字符串值
	 *
	 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
	 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
	 * @return 执行结果组成的字符串
	 * @throws IOException
	 */
	public static synchronized void shell(String cmds,final Consumer<String> stdin,final Consumer<String> stderr,Consumer<Map<String,Object>> callback) {
		shell(cmds.split("[\\s+]"),null,stdin,stderr,callback);
	}
	
	/**
	 * 	执行一个shell命令，并返回字符串值
	 *
	 * @param cmds 用空格却分数组元素： 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
	 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
	 * @return 执行结果组成的字符串
	 * @throws IOException
	 */
	public static synchronized void shell(String cmds, String wd, final Consumer<String> stdin, final Consumer<String> stderr,Consumer<Map<String,Object>> callback) {
		shell(cmds.split("[\\s+]"),wd,stdin,stderr,callback);
	}

	/**
	 * 	执行一个shell命令，并返回字符串值
	 *
	 * @param cmd           命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
	 * @param wd workingdirectory 命令执行路径（例如："system/bin/"）
	 * @return 执行结果组成的字符串
	 * @throws IOException
	 */
	public static synchronized void shell(String[] cmd, final String workingdirectory, 
		final Consumer<String> stdin,Consumer<String> stderr,Consumer<Map<String,Object>> callback) {
		Runnable runnable = ()->{
			try {
				long begTime = System.currentTimeMillis();
				final ProcessBuilder builder = new ProcessBuilder(cmd);
				String wd = workingdirectory;// 工作目录
				if (wd == null) wd = System.getProperty("user.dir"); // 设置一个路径（绝对路径了就不一定需要）
				String user_name = System.getProperty("user.name");
				String user_home = System.getProperty("user.home");
				System.out.println("系统用户名："+user_name);
				System.out.println("用户主目录："+user_home);
				System.out.println("用户工作路径:"+wd);
				builder.directory(new File(wd)); // 设置工作目录（同上）
				builder.redirectErrorStream(true); // 合并标准错误和标准输出
				Process process = builder.start(); // 启动一个新进程
				BiConsumer<InputStream,Consumer<String>> handle = (inputstream,consumer)->{
					BufferedReader br = null;
					try {
						System.out.println(Arrays.asList(cmd).stream().collect(Collectors.joining("\n-->")));
						br = new BufferedReader(new InputStreamReader(inputstream,"utf8"));
						String line = null; while ((line = br.readLine()) != null) consumer.accept(line);
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						if(br!=null)try {br.close();}catch(Exception ex) {ex.printStackTrace();};
					}//try
				};
				// 结果处理
				handle.accept(process.getInputStream(),stdin); handle.accept(process.getErrorStream(),stderr);
	            boolean b = process.waitFor(50, TimeUnit.SECONDS);// 等待50秒钟
	            long endTime = System.currentTimeMillis();
	            Map<String,Object> result = new HashMap<String,Object>();
	            result.put("success", b);
	            result.put("last for", (endTime-begTime)+"ms");
	            callback.accept(result);// 传回执行结果
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				// do nothing
			}// try
		};
		
		new Thread(runnable).start();
	}
	
	/**
	 * 把字符集转成文件
	 * @param fullname 文件绝对路径
	 * @param contentSuppler  文件书写内容
	 */
	public static boolean write(String fullname,Supplier<byte[]> bytesSuppler) {
		return write(new File(fullname),bytesSuppler);
	}
	
	/**
	 * 把字符集转成文件
	 * @param fullname 文件绝对路径
	 * @param contentSuppler  文件书写内容
	 */
	public static boolean write(String fullname,String bytesSuppler) {
		return write(new File(fullname),()->bytesSuppler.getBytes());
	}
	
	/**
	 * 把字符集转成文件
	 * @param fullname 文件绝对路径
	 * @param contentSuppler  文件书写内容
	 */
	public static boolean write(final File file,Supplier<byte[]> bytesSuppler) {
		File pfile = file.getParentFile();
		if(!file.exists())pfile.mkdirs();
		FileOutputStream fos = null; 
		try {
			fos = new FileOutputStream(file);
			byte[] bb = bytesSuppler.get();
			fos.write(bb);
			fos.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			try {
				fos.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param bb
	 * @return
	 */
	public static ByteArrayOutputStream bb2bos(byte[] bb) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(bb);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos;
	}
	
	/**
	 * 转换成输入流
	 * @param bb
	 * @return
	 */
	public static ByteArrayInputStream bb2bis(byte[] bb) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bb);
		return bis;
	}
	
	/**
	 * 
	 * @param imgStr
	 * @param path
	 */
	
	public static byte[] base64str2bytes(String string) {
		byte[] bb = null;
		if(string==null)return null;
		final Base64.Decoder decoder = Base64.getDecoder();
		try {
			bb = decoder.decode(string);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return bb;
	}
	
	/**
	 * 
	 * @param file 文件对象
	 * @return
	 */
	public static String file2base64str(String fullname) {
		return file2base64str(new File(fullname));
	}
	
	/**
	 * 
	 * @param file 文件对象
	 * @return
	 */
	public static String file2base64str(File file) {
		InputStream is = null;
		String line = null;
		try {
			is = new FileInputStream(file);
			line = stream2base64str(is);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				is.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return line;
	}
	
	/**
	 * 
	 * @param path
	 */
	public static String stream2base64str(InputStream is) {
		
		final Base64.Encoder decoder = Base64.getEncoder();
		String line = null;
		try {
			byte[] bb = new byte[(is.available())];
			is.read(bb);
			line = decoder.encodeToString(bb);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return line;
	}
	
	/**
	 * 把window 路径名转换成unix路径名
	 * @param path 路径名
	 * @return unix 格式的path
	 */
	public static String unixpath(String path) {
		if(path==null)return null;
		return path.replaceAll("[\\\\]+", "/").trim();
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
	 * 提取一个全路径文件的扩展名
	 * @param fullname 文件全路径名，例如c:/a/b/c.jpg
	 * @return 文件的简单名，不包含路径,例如 c.jpg
	 */
	public static String extensionpicker(String fullname) {
		fullname = unixpath(fullname);
		if(fullname.indexOf(".")<=0)return "";
		Matcher matcher = Pattern.compile("([^\\\\.]+$)").matcher(fullname);
		return matcher.find()?matcher.group(1):null;
	}
	
	/**
	 * 把一个base62字符串转换成文件独享
	 * @param bas64string
	 * @param fullname 文件的保存保存路径
	 */
	public static File base64str2file(String bas64string,String fullname) {
		boolean b = write(fullname, ()->base64str2bytes(bas64string));
		return b?new File(fullname):null;
	}
	
	/**
	 * 	用于读写jar 文件的属性文件
	 * @author gbench
	 *
	 */
	/**
	 * @author gbench
	 *
	 */
	public static class AnotherProperties {
		
		private AnotherProperties() {};
		
		/**
		 * 	配置文件
		 */
		public AnotherProperties(String file){
			String path = path(file,null);
			try {
				InputStream is = (path!=null && new File(path).exists()) 
					? new FileInputStream(path)
					: this.getClass().getClassLoader().getResourceAsStream(file);
				props.load(is);
				//System.out.println(props);
			}
			catch (Exception e) {e.printStackTrace();}
		}
		
		/**
		 *	 获取属性
		 * @param key 属性键名
		 * @return 属性名
		 */
		public String getProperty(String key) {
			if(key==null)return null;
			String value = props.getProperty(key);
			if(value==null)return null;
			Pattern pattern = Pattern.compile("\\$\\{\\s*([^\\}]+)\\s*\\}");// 获取键值模式
			Matcher matcher = pattern.matcher(value);
			int max_times = 1000;// 最大尝试次数
			while (matcher.find()) {// 尝试进行键值数据替换
				String _key = matcher.group(1);
				value = matcher.replaceAll(getProperty(_key));
				matcher = pattern.matcher(value);
				if( max_times-- <=0){System.out.println("too Many times");break;}
			}
			return value;
		}
		
		/**
		 *  属性结合
		 * @return 属性集
		 */
		public Properties toProps() {
			return toProps((Function<String,String>)null);
		}
		
		/**
		 *  属性结合
		 * @return 属性集
		 */
		public Map<String,String> toMap() {
			Map<String,String> mm = new HashMap<String,String>();
			this.toProps().forEach((k,v)->mm.put(k+"", v+""));
			return mm;
		}
		
		/**
		 * 	翻译一个新的属性集
		 * @param key_mutator 键名变换器的  映射列表
		 * @return
		 */
		public Properties toProps(Map<String,String>key2newkey) {
			return toProps(e->key2newkey.get(e));
		}
		
		/**
		 * 	翻译一个新的属性集
		 * @param key_mutator 键名变换器的  映射列表
		 * @return
		 */
		public static AnotherProperties fromMap(Map<String,String>vv) {
			AnotherProperties ap = new AnotherProperties();
			if(vv!=null) vv.forEach((k,v)->{if(k!=null && v!=null)ap.props.setProperty(k+"",v+"");});
			return ap;
		}
		
		/**
		 *  	属性结合
		 * @return 属性集
		 */
		public Properties toProps(Function<String,String>rename) {
			Properties properties = new Properties();
			props.forEach((k,v)->{
				String key = k+"";
				if(rename!=null) {
					String t = rename.apply(k+"");
					if(t==null)return;
					key = t;
				}
				properties.put(key,getProperty(k+""));
			});
			return properties;
		}
		
		/**
		 * 返回元素key-value 数量．
		 * @return
		 */
		public int size() {
			return props.size();
		}
		
		public final Properties props = new Properties();
	}
	
}
