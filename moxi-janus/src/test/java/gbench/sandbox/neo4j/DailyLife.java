package gbench.sandbox.neo4j;

import static gbench.common.tree.LittleTree.cph;
import static gbench.common.tree.LittleTree.series;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import static gbench.common.tree.LittleTree.IRecord.*;
import gbench.common.tree.LittleTree.IRecord;
import gbench.commonApp.jdbc.Neo4jApp;

/**
 * 
 * @author gbench
 *
 */
public class DailyLife {
    public static void main(String args[]) {
        
        final var neo4jApp = new Neo4jApp();
        // 图表初始化调用
        neo4jApp.set_intialize_handler(g->{// 设置顶点
            g.setVertex_name_renderer(e->"n"+e.value());
        });
        // 节点处理器
        neo4jApp.set_vertex_handler((vertex,g)->{
            final var vname = g.getVetexName(vertex);
            g.setAttribute(vname, REC("createTime",LocalDateTime.now()));
        });
        // 便处理器
        neo4jApp.set_edge_handler((edge,g)->{
           final var ename = g.getEdgeName(edge);
           g.setAttribute(ename, REC("createTime",LocalDateTime.now()));
        });
        
        var lines = cph(series,"1:5","1:5","1:2","1:6").stream();
        lines = 
            Stream.of(
                "1,2,3,4,5,6,7,8,9",
                "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作",
                "起床/开电视/听新闻/接电话/喝牛奶/出门")
            .map(IRecord::STRING2REC);
        
        final var g = neo4jApp.graph(lines,false);
        
        // 设置图的属性
        g.addVertexAttribute("n起床",R("time","早上","人物","张小宝"));
        g.addVertexAttribute("n穿衣服",R("服装品牌","皮尔卡丹"));
        g.addEdgeAttribute(REC(0,"起床",1,"开电视"),R("time","早上","人物","张小宝"));
        g.addEdgeAttribute("n起床-n开电视",R("time0","早上","人物0","张小宝"));
        
        // build graph
        System.out.println("\ncreate "+g);
        // data manipulate
        System.out.println("\nmatch (a)-[e:Edge]->(b) delete a,e,b;");
        System.out.println("\nmatch (a)-[e:Edge]->(b) return a,e,b;");
        
    }

}
