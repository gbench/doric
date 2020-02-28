package gbench.common.client;

import static gbench.common.tree.LittleTree.MFT;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import gbench.common.fs.FileSystem;

/**
 * 文件下载器
 * final var downloader = new HttpDownloader("D:/sliced/ebook/crawler/anatomy1",
 *  "http://www.jztyx.com/imgFileStream/b8899a45e3206c8c/27");
 *  downloader.start();
 * @author gbench
 *
 */
public class HttpDownloader {
    /**
     * 文件下载器
     * @param home 文件保存路径
     * @param urls 网络资源url
     */
    public HttpDownloader(String home,String ...urls) {
        this.home  = home;
        if(urls!=null)this.addUrls(Arrays.asList(urls));
    }
    
    /**
     * 添加一个url
     * @param url 网络资源的url
     */
    public void addUrl(String url) {
       if(!this.urls.contains(url)) this.urls.add(url);
    }
    
    /**
     * 添加一个url集合
     * @param urls 网络资源的urls集合
     */
    public void addUrls(List<String> urls) {
        urls.forEach(url->{
            if(!this.urls.contains(url)) this.urls.add(url);
        });
    }
    
    /**
     * 添加一个url集合
     * @param urls 网络资源的urls集合
     */
    public void addUrls(Stream<String> urls) {
        urls.forEach(url->{
            if(!this.urls.contains(url)) this.urls.add(url);
        });
    }
    
    /**
     * 启动下载
     * @param extension 下载文件的扩展名
     */
    public void start() {
        this.start(null,null);
    }
    
    /**
     * 启动下载
     * @param method 请求方式
     * @param extension 下载文件的扩展名
     */
    public void start(String method,final String extension) {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        FileOutputStream out = null;
        try {
            /**
             * 下载所有网络资源
             */
            for(String url:this.urls){
                final var dirHome = new File(home);
                if(!dirHome.exists())dirHome.mkdirs();
                File file0 = new File(home);
                // 确保目标文件路径存在
                if (!file0.isDirectory() && !file0.exists())file0.mkdirs();
                final var name = FileSystem.namepicker(url);
                out = new FileOutputStream(MFT("{0}/{1}.{2}",home,name,extension==null?"":extension));
                final var httpUrl = new URL(url); // 建立链接
                conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setRequestMethod(method==null?"GET":method);//// 以Post方式提交表单，默认get方式
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);// post方式不能使用缓存
                conn.connect(); // 连接指定的资源
                inputStream = conn.getInputStream(); // 获取网络输入流
                bis = new BufferedInputStream(inputStream);
                final var bb= new byte[1024];
                int len = 0;
                while ((len = bis.read(bb)) != -1) out.write(bb, 0, len);
                if(debug)System.out.println(MFT("{0} 下载完成...",url));
            }//for
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)  out.close();
                if (bis != null) bis.close();
                if (inputStream != null)  inputStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

    }
    
    public static boolean debug=true;
    private List<String> urls = new LinkedList<String>();// 网络资源的url 集合
    private String home;// 下载文件的保存路径

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        final var home = "D:/sliced/ebook/crawler/anatomy1";
        final var downloader = new HttpDownloader(home);
        final var url = "http://www.jztyx.com/imgFileStream/b8899a45e3206c8c/27";
        downloader.addUrl(url);
        downloader.start("GET",".jpg");
    }
}