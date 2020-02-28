package gbench.appdemo.spider.downloader;

import java.util.stream.Stream;

import gbench.common.client.HttpDownloader;
import gbench.common.fs.XlsFile.*;
import gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class EbookDownloader {
    
    /**
     * 文档资料的下载
     * @param sheetName
     * @param excel
     */
    public void download(String sheetName,SimpleExcel excel) {
        final var urls = excel.autoDetect(sheetName)
        .rowStream(IRecord::REC).map(e -> {// 提取url
            String url = e.str("url");// page url
            return url;
        });// 页面url
        // 创建一个网络资料下载器。
        final var downloader = new HttpDownloader(MFT("D:\\sliced\\ebook\\crawler\\{0}",sheetName));
        downloader.addUrls(urls);
        downloader.start("GET","jpg");
    }
    
    /**
     * 文档资料的下载
     * @param sheetName
     * @param excel
     */
    public void download(String sheetName,int start,int end,String url) {
        final var urls = Stream.iterate(start,i->i<=end,i->i+1).map(i->MFT("{0}/{1,number,#}",url,i));
        // 创建一个网络资料下载器。
        final var downloader = new HttpDownloader(MFT("D:/sliced/ebook/crawler/{0}",sheetName));
        downloader.addUrls(urls);
        downloader.start("GET","jpg");
    }
    
    /**
     * 解剖学资料下载器
     * @param args
     */
    public static void main(String args[]) {
        //final var excel = new SimpleExcel("C:\\Users\\gbench\\Desktop\\解剖学图谱\\解剖学图谱.xlsx");// 创建网络数据源资料
        EbookDownloader ebl = new EbookDownloader();
        //ebl.download("临床药物大典",excel);
        ebl.download("新编常用药物手册",1, 1069, "http://www.jztyx.com/imgFileStream/2a3f2af160a54501");
    }

}
