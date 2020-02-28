package gbench.sandbox.neo4j;

import org.junit.Test;

import gbench.common.tree.LittleTree.IRecord;
import gbench.commonApp.jdbc.Neo4jApp;
import gbench.commonApp.jdbc.Neo4jApp.Graph;
import static gbench.common.tree.LittleTree.SimpleRecord.*;
import java.time.LocalDateTime;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.*;

/**
 * 
 * @author gbench
 *
 */
public class Neo4jPersons {
    
    @Test
    public void foo() {
                                                                                                             
        final var app = new Neo4jApp(true);
        app.set_intialize_handler(g->{
            g.setVertex_name_renderer(e->MFT("v"+e.value()));
            g.setEdge_label_renderer(edge->{
                final var name = g.getEdge_name_renderer().apply(edge);
                final var rel = g.getAttribute(name).str("rel");
                return rel;
            });// setEdge_label_renderer
        });
        
        final Graph g = app.graph(REC2(
            "张三/李四", R("rel","喜欢","张三.address","上海",
                "张三.father","lisi",
                "张三.birth",LocalDateTime.now(),
                "李四.phone","1812074620",
                "李四.postcode","200052"
                ),
            "李四/王五/赵六", R("rel","喜欢","2 # address","北京","1#age",25,"2#height",1.98),
            "赵六/王八", R("rel","喜欢","2 # address","北京"),
            "陈七/王八", R("rel","喜欢","$address","中国"),
            "闫妮/陈七", R("rel","喜欢","$address","中国"),
            "张三/闫妮", R("rel","喜欢","0#postcode","200053")
        ).applyForKvs(IRecord::STRING2REC,e->(IRecord)e));
        
        System.out.println(g);
        final var jdbc = Neo4jApp.getJdbc();
        jdbc.withTransaction(sess->{
            sess.sqlexecute("match (a:Vertex)-[e]->(b:Vertex) delete a,e,b");
            sess.sqlexecute(MFT("create {0}",g));
            var mm = sess.sql2records("match (a:Vertex)-[e]->(b:Vertex) return a,e,b");
            System.out.println(FMT(mm));
        });
        
    }
    
    @Test
    public void bar() {
        final var app = new Neo4jApp(true); 
        final var g = app.graph(REC2( 
            "张三/李四", R("rel","喜欢","张三.address","上海", 
                "张三.father","lisi", 
                "张三.birth",LocalDateTime.now(), 
                "李四.phone","1812074620" 
                ), 
            "李四/王五/赵六", R("rel","喜欢","2 # address","北京","1#age",25,"2#height",1.98), 
            "赵六/王八", R("rel","喜欢","2 # address","北京"), 
            "陈七/王八", R("rel","喜欢","$address","中国") 
        ).applyForKvs(IRecord::STRING2REC,e->(IRecord)e)); 
        System.out.println(g);
    }
    
}
