package gbench.sandbox.neo4j;

import org.junit.Test;
import static gbench.common.tree.LittleTree.cph;
import static gbench.common.tree.LittleTree.series;

import java.util.stream.Stream;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.common.tree.LittleTree.IRecord;
import gbench.commonApp.jdbc.Neo4jApp;

/**
 * 
 * @author gbench
 *
 */
public class CphGraph {
    
    @Test
    public void foo() {
        var lines = cph(series,"1:5","1:5","1:2","1:6").stream();
        lines = Stream.of(
            "1,2,3,4,5,6,7,8,9",
            "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作",
            "起床/开电视/听新闻/接电话/喝牛奶/出门")
        .map(IRecord::STRING2REC);
        
        final var neo4jApp = new Neo4jApp();
        final var g = neo4jApp.graph(lines,false);
        g.setVertex_name_renderer(e->"n"+e.value());
        // 设置图的属性
        g.addVertexAttribute("n起床",REC("time","早上","人物","张小宝"));
        g.addEdgeAttribute(REC(0,"起床",1,"开电视"),REC("time","早上","人物","张小宝"));
        g.addEdgeAttribute("n起床-n开视",REC("time0","早上","人物0","张小宝"));
        
        System.out.println(g);
    }
}
