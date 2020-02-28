package gbench.sandbox.neo4j;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

import gbench.commonApp.jdbc.Neo4jApp;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.SimpleRecord.*;
import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class PopStarRelations {
    
    @Test
    public void foo() {
        final var app = new Neo4jApp();
        app.set_intialize_handler(g -> {
            g.setVertex_name_renderer(e -> MFT("n{0}", e.value()));// 节点名称绘制。
            g.setEdge_label_renderer(e -> {
                final var name = g.getEdge_name_renderer().apply(e);// 提取边name
                final var rel = g.getAttribute(name).str("rel");// 提取关系属性
                return Stream.of(rel).filter(p -> p != null).collect(Collectors.joining(":"));
            });// 边的label 绘制办法
        });// 图的初始化结构
        
        // 关系的定义：含有数据属性。
        final var rr = REC2 (// 关系集合,关系的属性。
            "陈冠希/张柏芝",  "旧爱",
            "谢霆锋/张柏芝",  "离婚",
            "陈冠希/谢霆锋",  "昔日好友",
            "王菲/谢霆锋",   "旧爱",
            "王菲/李亚鹏",   "离婚",
            "李亚鹏/瞿颖",   "旧爱",
            "瞿颖/张亚东",   "旧爱",
            "张亚东/窦颖",   "离婚",
            "朴树/张亚东",   "制作人",
            "周迅/朴树",    "旧爱",
            "周迅/李亚鹏",   "旧爱",
            "周迅/李大齐",   "旧爱",
            "周迅/窦凯",    "旧爱",
            "周迅/谢霆锋",   "绯闻",
            "窦凯/窦唯",    "堂兄弟",
            "王菲/窦唯",    "离婚",
            "窦唯/窦颖",    "兄妹"
        );// 图的定义
        
        final var g = app.graph(rr.applyForKvs(IRecord::STRING2REC,e->REC("rel",e)));
        //System.out.println(g);
        //Neo4jApp.updateDatabaseAnnotaion("url", "123");
        final var jdbc = Neo4jApp.getJdbc();
        if(System.currentTimeMillis()>0)jdbc.withTransaction(sess->{
            sess.sqlexecute("match (a:Vertex)-[e]->(b:Vertex) delete a,e,b");
            sess.sqlexecute("match (a)-[e:Edge]->(b) delete a,e,b");
            sess.sqlexecute(MFT("create {0}",g));
            var mm = sess.sql2records("match (a:Vertex)-[e]->(b:Vertex) return a,e,b");
            System.out.println(FMT(mm));
            sess.sqlexecute("match p = (a)-[e:Edge]->(b) foreach (n in nodes(p) |set n.marked=TRUE)");
        });
        
    }
}
