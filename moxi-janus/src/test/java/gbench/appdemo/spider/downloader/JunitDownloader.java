package gbench.appdemo.spider.downloader;

import org.junit.Test;

import gbench.common.client.HttpDownloader;

public class JunitDownloader {
    
    @Test
    public void foo() {
        final var dl = new HttpDownloader("D:\\sliced\\ebook\\crawler\\anatomy1",
            "http://www.jztyx.com/imgFileStream/83478c21e5520858/1");
        dl.start(null,"jpg");
    }

}
