package gbench.commonApp.jdbc;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Collection;

import gbench.common.tree.LittleTree.IRecord;
import gbench.common.tree.LittleTree.Jdbc;
import gbench.common.tree.LittleTree.KVPair;
import gbench.common.tree.LittleTree.Tuple2;
import gbench.common.tree.LittleTree.Jdbc.JdbcConfig;

import static gbench.common.tree.LittleTree.ANNO;
import static gbench.common.tree.LittleTree.LIST;
import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.MFT;
import static gbench.common.tree.LittleTree.SET_FIELD_OF_ANNOTATION;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图结构的创建App： <br>
 * 顶点采用 KVPair<String,Object>来表示。 <br>
 * 演示示例如下：Map<IRecord,Record> edge2attributes 的结构创建  <br>
 * @Test <br>
 * public void foo0() { <br>
 *     final var app = new Neo4jApp(true); <br>
 *     final var g = app.graph(REC2( <br>
 *         "张三/李四", R("rel","喜欢","张三.address","上海", <br>
 *             "张三.father","lisi", <br>
 *             "张三.birth",LocalDateTime.now(), <br>
 *             "李四.phone","1812074620" <br>
 *             ), <br>
 *         "李四/王五/赵六", R("rel","喜欢","2 # address","北京","1#age",25,"2#height",1.98), <br>
 *         "赵六/王八", R("rel","喜欢","2 # address","北京"), <br>
 *         "陈七/王八", R("rel","喜欢","$address","中国") <br>
 *     ).apply2kvs(IRecord::STRING2REC,e->(IRecord)e)); <br>
 *     System.out.println(g); <br>
 *     
 *     // 数据库主要构件的创建。
 *     // final var database = IJdbcApp.newNspebDBInstance("neo4j.sql", Database.class);// 根据SQL语句模板文件neo4j.sql 生成代理数据库
 *     // final var proxy = database.getProxy();// 提取代理对象
 *     // final var jdbc = proxy.findOne(Jdbc.class); // 提取jdbc
 *     // final var spp = proxy.findOne(SqlPatternPreprocessor.class);// 提取SQLPattern 处理器
 *     // final var line = spp.handle(null, null, "#createLine", null);// 提取数sql语句定义

 *     
 *     final var jdbc = Neo4jApp.getJdbc(); <br>
 *     jdbc.withTransaction(sess->{ <br>
 *         sess.sqlexecute("match (a:Vertex)-[e]->(b:Vertex) delete a,e,b"); <br>
 *         sess.sqlexecute(MFT("create {0}",g)); <br>
 *         var mm = sess.sql2records("match (a:Vertex)-[e]->(b:Vertex) return a,e,b");<br>
 *         System.out.println(FMT(mm)); <br>
 *     }); <br>
 * } <br>
 * 
 *  Stream<IRecord> 的结构创建 <br>
 *  @Test <br>
 *  public void foo1() { <br>
 *      var lines = cph(series,"1:5","1:5","1:2","1:6").stream(); <br>
 *      lines =Stream.of( <br>
 *          "1,2,3,4,5,6,7,8,9", <br>
 *          "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作", <br>
 *          "起床/开电视/听新闻/接电话/喝牛奶/出门") <br>
 *      .map(IRecord::STRING2REC); <br>
 *      
 *      final var neo4jApp = new Neo4jApp(); <br>
 *      final var g = neo4jApp.graph(lines,false); <br>
 *      g.setVertex_name_renderer(e->"n"+e.value()); <br>
 *      // 设置图的属性 <br>
 *      g.addVertexAttribute("n起床",REC("time","早上","人物","张小宝")); <br>
 *      g.addEdgeAttribute(REC(0,"起床",1,"开电视"),REC("time","早上","人物","张小宝")); <br>
 *      g.addEdgeAttribute("n起床-n开电视",REC("time0","早上","人物0","张小宝")); <br>
 *      
 *      System.out.println(g); <br>
 *  } <br>
 *
 * @author gbench
 *
 */
@NoArgsConstructor @AllArgsConstructor
public class Neo4jApp {
    
    /**
     * 是否在创建节点名的时候忽略节点的所在阶层。即 a/b/a,这样的序列，是表示一个环，而不是一个直线。<br>
     * 即是    a_ b_ a_  (注意此处的_后缀标注仅用于展示，而不是实际意义，_表示忽略掉顶点的层级) 还是<br> 
     * a0 b1 a2 (此处额0,1,2分别哦表示顶点所在的层级)<br>
     * 
     * @param discardLevel true忽略节点所在的层级序号,false 保留节点的层级序号。
     */
    public Neo4jApp(boolean discardLevel){
        if(discardLevel) this.set_intialize_handler(g->{
            g.setVertex_name_renderer(e->MFT("v{0}",e.value()));
        });
    }
    
    /**
     * @author Bean_bag
     * @description 进行Hash运算
     * 
     * @param input 参数字符串
     * @return 生成的hash值
     */
    public static String md5(String input){
        try {
            //参数校验
            if (null == input) {
                return null;
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            BigInteger bi = new BigInteger(1, digest);
            String hashText = bi.toString(16);
            while(hashText.length() < 32){
                hashText = "0" + hashText;
            }
            return hashText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    

    /**
     * 图结构:
     * 所谓图 ,理论上 就是一个二元组(V,E)即 Vertex 顶点集 与 Edge边集 的元组 .<br><br>
     * Graph 的实现结构，采用了 (V,E,A)的结果对二元组结果进行了扩展，加入了除了顶点与边，增加属性库集合,Attributes  <br>
     * 其中顶点集与边集 中的顶点与边 都拥有一个唯一的名字。用于标识自己， 属性库集合 A用于存放 顶点与边的属性信息。 <br>
     * 保存结构是一个键值对集合IRecord.(Key/String:Value/Object) <br>
     * 对于一个顶点或是边：Graph 是通过其name 在 属性仓库中进行检索/索引访问的。<br>
     * 每个顶点或是边都拥有一个可以自定义的 vertex/edge_name_render 用于生成器唯一名称/属性集合的名称的索引。 <br>
     * 默认的的 edge_name_render是采用  :md5(开始顶点名-结束顶点名) 的形式来给予编码的。。当然这个可以自行设置的。<br>
     * 默认的的 vertex_name_render是采用  :直接返回原始的顶点名称。当然这个也是可以自行设置的。<br>
     * 名称非常重要，设置vertex/edge_name_render的需要谨慎,一定要保持名称唯一性,边名不能与顶点相同,这也是为何默认实现的时候,边名左md5加密,而顶点名不做 md5加密的原因。<br>
     * 
     * @author gbench
     *
     */
    @Data
    public static class Graph {
        
        /**
         * 数据格式化
         */
        public String toString() {
            final var stream = Stream.of(// 流化处理
                distinct(vertexes.stream(),vertex_name_renderer).map(this.vertex_renderer()),
                distinct(edges.stream(),edge_name_renderer).map(this.edge_renderer())
            ).flatMap(e -> e);
            return stream.collect(Collectors.joining(",\n"));
        }
        
        /**
         * 根据 obj的运行时类型对obj对象进行引用
         * @param obj 待引用的对象
         * @return 除了数值(Number的子类)，其他一律添加 “"”给括起来。
         */
        private static String quote(Object obj) {
            if(obj instanceof Number) return obj+"";
            else return MFT("\"{0}\"",obj+"");
        }
        
        public Pattern VERTEX_ATTRIBUTE_NAME_PATTERN= Pattern.compile("(\\s*[^\\$#\\.\\s]*)\\s*([\\$#\\.]+)\\s*([^\\s]+)\\s*");;// 顶点的属性名的模式
        private Predicate<String> is_vertex_attribute_name = name->VERTEX_ATTRIBUTE_NAME_PATTERN.matcher(name).matches();
        private Function<KVPair<String, Object>,String> vertex_name_renderer = (v)->
            MFT("v{0}{1}",v.key(),v.value());
        private Function<KVPair<String, Object>,String> vertex_label_renderer = (v)->
            Arrays.asList(MFT("{0}","Vertex")).stream().collect(Collectors.joining(":"));
        private Function<KVPair<String, Object>,String> vertex_attrs_renderer = (v)->
            Stream.of(
                MFT("level:{0}",quote(v.key())),// 节点阶层
                MFT("name:{0}",quote(vertex_name_renderer.apply(v))),// 节点名称
                MFT("{0}",this.getAttribute(vertex_name_renderer.apply(v)).kvs().stream()
                    .map(p->MFT("{0}:{1}",p.key(),quote(p.value()))).collect(Collectors.joining(","))
                )// 节点属性
            ).filter(e->e.length()>0).collect(Collectors.joining(","));
        
        // 边绘制信息
        private Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_name_renderer = (tup)->
            MFT("e{0}",md5(MFT("{0}-{1}",vertex_name_renderer.apply(tup._1()),vertex_name_renderer.apply(tup._2()) )));
        private Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_label_renderer = (tup)->
            Arrays.asList(MFT("{0}","Edge")).stream().collect(Collectors.joining(":"));
        private Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_attrs_renderer = (tup)->
            Stream.of(
                MFT("from:{0}",quote(vertex_name_renderer.apply(tup._1()))),
                MFT("to:{0}",quote(vertex_name_renderer.apply(tup._2()))),
                MFT("{0}",this.getAttribute(edge_name_renderer.apply(tup)).kvs().stream()
                    .filter(e->!this.is_vertex_attribute_name.test(e.key()))// 顾虑掉顶点属性名的苏醒。
                    .map(p->MFT("{0}:{1}",p.key(),quote(p.value()))).collect(Collectors.joining(","))
                )// 节点属性
            ).filter(e->e.length()>0).collect(Collectors.joining(","));
         
        /**
         * VALUE EQs
         * @param obj 待检测的对象
         * @return obj 是否与一个 对象的 kvp.value 相等价的判定
         */
        public static Predicate<KVPair<String,Object>> VEQS(Object obj){
            return (kvp)->kvp.value().equals(obj);
        }
        
        /**
         * 获取一个节点的Next节点
         * @param predicate 节点的等价判定
         * @return 节点的后继节点
         */
        public List<KVPair<String,Object>> getPrevs(Predicate<KVPair<String,Object>>predicate){
            return this.getEdges().stream().filter(e->predicate.test(e._2()))
                .map(e->e._1())
                .collect(Collectors.toList());
        }
        
        /**
         * 获取一个节点的Next节点
         * @param predicate 节点的等价判定
         * @return 节点的后继节点
         */
        public List<KVPair<String,Object>> getNexts(Predicate<KVPair<String,Object>>predicate){
            return this.getEdges().stream().filter(e->predicate.test(e._1()))
                .map(e->e._2())
                .collect(Collectors.toList());
        }
        
        /**
         * 根据节点值进行后续节点获取
         * 获取一个节点的Next节点
         * @param predicate 节点的等价判定
         * @return 节点的后继节点
         */
        public List<KVPair<String,Object>> getNexts2(Object v){
            return this.getNexts(VEQS(v));
        }
        
        /**
         * 根据节点值进行前驱节点获取
         * 获取一个节点的Next节点
         * @param predicate 节点的等价判定
         * @return 节点的后继节点
         */
        public List<KVPair<String,Object>> getPrevs2(Object v){
            return this.getNexts(VEQS(v));
        }
        
        /**
         * 或如入口 顶点集合
         * @return 节点的后继节点
         */
        public List<KVPair<String,Object>> getStarts(){
            return LIST(this.getVertexes().stream().filter(e->this.getPrevs(p->e.equals(p)).size()==0));
        }
        
        /**
         * 获取前驱节点集合
         * @return 节点的后继节点
         */
        public List<KVPair<String,Object>> getEnds(){
            return LIST(this.getVertexes().stream().filter(e->this.getNexts(p->e.equals(p)).size()==0));
        }
        
        /**
         * 顶点描绘
         * @return 顶点的渲染器
         */
        public Function<KVPair<String, Object>,String> vertex_renderer(
            Function<KVPair<String, Object>,String> vertex_name_renderer,
            Function<KVPair<String, Object>,String> vertex_label_renderer,
            Function<KVPair<String, Object>,String> vertex_attrs_renderer ) {
            
            return v->MFT("({0} :{1} '{'{2}})",
                vertex_name_renderer.apply(v),
                vertex_label_renderer.apply(v),
                vertex_attrs_renderer.apply(v)
            );
        }
        
        /**
         * 获取顶点名称
         * @param kvp
         * @return
         */
        public String getVetexName(KVPair<String,Object> vertex) {
            return this.vertex_name_renderer.apply(vertex);
        }
        
        /**
         * 获取顶点标签
         * @param vertex
         * @return
         */
        public String getVetexLabel(KVPair<String,Object> vertex) {
            return this.vertex_label_renderer.apply(vertex);
        }
        
        /**
         * 获取顶点名称
         * @param kvp
         * @return
         */
        public String getEdgeName(Tuple2<KVPair<String,Object>,KVPair<String,Object>> edge) {
            return this.edge_name_renderer.apply(edge);
        }
        
        /**
         * 获取顶点名称
         * @param kvp
         * @return
         */
        public String getEdgeLabel(Tuple2<KVPair<String,Object>,KVPair<String,Object>> edge) {
            return this.edge_label_renderer.apply(edge);
        }
        
        /**
         * 顶点描绘
         * @return 顶点的渲染器
         */
        public Function<KVPair<String, Object>,String> vertex_renderer(){
            return this.vertex_renderer(vertex_name_renderer, vertex_label_renderer, vertex_attrs_renderer);
        }
        
        /**
         * 边的渲染器
         * 
         * @param vertex_name_renderer
         * @param edge_name_renderer
         * @param edge_label_renderer
         * @param edge_attrs_renderer
         * @return 便渲染的CQL
         */
        public Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_renderer(
            Function<KVPair<String, Object>,String>vertex_name_renderer,
            Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_name_renderer,
            Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_label_renderer,
            Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_attrs_renderer){
        
            return tup -> MFT("({0})-[{1} :{2} '{'{3}}]->({4})", 
                vertex_name_renderer.apply(tup._1()),
                edge_name_renderer.apply(tup), 
                edge_label_renderer.apply(tup), 
                edge_attrs_renderer.apply(tup),
                vertex_name_renderer.apply(tup._2()));
        }
        
        /**
         * 顶点集合
         * @return 去重后的顶点集合
         */
        public List<KVPair<String, Object>> getVertexes(){
            return LIST(distinct(this.vertexes.stream(),vertex_name_renderer));
        }
        
        /**
         * 边集合
         * @return 去重后的边集合
         */
        public List<Tuple2<KVPair<String, Object>, KVPair<String, Object>>> getEdges(){
            return LIST(distinct(this.edges.stream(),edge_name_renderer));
        }

        /**
         * 边渲染
         * @return 边渲染的CQL
         */
        public Function<Tuple2<KVPair<String, Object>, KVPair<String, Object>>, String> edge_renderer() {
            return this.edge_renderer(vertex_name_renderer, edge_name_renderer, edge_label_renderer, edge_attrs_renderer);
        }
        
        /**
         * 为节点或边增加一个属性
         * @param key 顶点名 或者 节点名 用于唯一标注 图元素的对象
         * @param rec 属性名 ,属性名称,[(key0,vlaue0),(key1,vlaue1),...]的序列
         * @return 图对象本省，用以实现链式编程
         */
        public Graph addAttribute(String key,IRecord rec) {
            return this.setAttribute(key, rec);
        }
        
        /**
         * 为节点或边增加一个属性
         * @param key 顶点名 或者 节点名 用于唯一标注 图元素的对象
         * @param rec 属性名 ,属性名称,[(key0,vlaue0),(key1,vlaue1),...]的序列
         * @return 图对象本省，用以实现链式编程
         */
        public Graph setAttribute(String key,IRecord rec) {
            rec.foreach((name,value)->this.addAttribute(key, name, value));
            return this;
        }
        
        /**
         * 为节点或边增加一个属性
         * @param vertex 顶点名  用于唯一标注 图元素的对象
         * @param rec 属性名 ,属性名称,[(key0,vlaue0),(key1,vlaue1),...]的序列
         * @return 图对象本省，用以实现链式编程
         */
        public Graph addVertexAttribute(String vertex,IRecord rec) {
            return this.addAttribute(vertex, rec);
        }
        
        /**
        * 为节点增加一个属性
        * @param vertexes 顶点结合
        * @param rec 属性名 ,属性名称,[(key0,vlaue0),(key1,vlaue1),...]的序列
        * @return 图对象本省，用以实现链式编程
        */
       public Graph addVertexAttribute(IRecord vertexes,IRecord rec) {
           vertexes.kvstream().forEach(vertex->this.addVertexAttribute(vertex, rec));
           return this;
       }
        
        /**
         * 为节点增加一个属性
         * @param vertex 顶点名
         * @param rec 属性名 ,属性名称,[(key0,vlaue0),(key1,vlaue1),...]的序列
         * @return 图对象本省，用以实现链式编程
         */
        public Graph addVertexAttribute(KVPair<String,Object>v,IRecord rec) {
            rec.foreach((name,value)->this.addAttribute(this.vertex_name_renderer.apply(v), name, value));
            return this;
        }
        
        /**
         * 为边增加属性
         * @param edge 边的定义
         * @param rec 属性的集合
         * @return 图对象本省，用以实现链式编程
         */
        public Graph addEdgeAttribute(IRecord edge,IRecord rec) {
            edge.tuple2Stream().forEach(tup->this.addEdgeAttribute(tup, rec));
            return this;
        }
        
        /**
         * 为边增加属性
         * @param edge 边的定义
         * @param rec 属性的集合
         * @return 图对象本省，用以实现链式编程
         */
        public Graph addEdgeAttribute(String edgeName,IRecord rec) {
            return this.addAttribute(MFT("e{0}",md5(edgeName)), rec);
        }
        
        /**
         * 为节点增加一个属性
         * @param tup 边的开始与结束节点。
         * @param rec 属性名 ,属性名称,[(key0,vlaue0),(key1,vlaue1),...]的序列
         * @return 图对象本省，用以实现链式编程
         */
        public Graph addEdgeAttribute(Tuple2<KVPair<String,Object>,KVPair<String,Object>>tup,IRecord rec) {
            rec.foreach((name,value)->this.addAttribute(this.edge_name_renderer.apply(tup), name, value));
            return this;
        }
        
        /**
         * 为顶点或边增加一个属性
         * @param key 顶点名 或者 节点名 用于唯一标注 图元素的对象
         * @param name 属性名
         * @param value 舒总的值
         * @return 图对象本省，用以实现链式编程
         */
        public Graph addAttribute(String key,String name,Object value) {
            attributes.compute(key, (k,v)->(v==null?REC():v).add(name,value));
            return this;
        }
        
        /**
         * 获取节点属性：若节点属性不存在会返回一个空的IRecord，不会返回空值。
         * @param name 属性名
         * @return 节点属性
         */
        public IRecord getAttribute(String name) {
            return attributes.getOrDefault(name,REC());
        }

        // 图的基本属性
        private List<KVPair<String, Object>> vertexes = new LinkedList<>();// 图的顶点
        private List<Tuple2<KVPair<String, Object>, KVPair<String, Object>>> edges = new LinkedList<>();// 图的边
        private Map<String,IRecord> attributes = new HashMap<>();// 节点或是边的的属性
    }
    
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个 <br>
     * KVPair[基层名/序号,顶点名的]结构。 <br>
     * 
     * 生成一个图结构<br>
     * 所谓数据行lines 是一个类似于 如下的列表数据 <br>
     * lines =Stream.of(<br>
     *          "1,2,3,4,5,6,7,8,9", <br>
     *          "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作",<br>
     *          "起床/开电视/听新闻/接电话/喝牛奶/出门")<br>
     *      .map(IRecord::STRING2REC);<br>
     * @param lines 数据行 IRecord的流
     * @return Graph 图结构
     */
    public Graph graph(final List<IRecord> lines) {
        return graph(lines.stream(),false,null,null);
    }
    
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个 <br>
     * KVPair[基层名/序号,顶点名的]结构。 <br>
     * 
     * 生成一个图结构<br>
     * 所谓数据行lines 是一个类似于 如下的列表数据 <br>
     * lines =Stream.of(<br>
     *          "1,2,3,4,5,6,7,8,9", <br>
     *          "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作",<br>
     *          "起床/开电视/听新闻/接电话/喝牛奶/出门")<br>
     *      .map(IRecord::STRING2REC);<br>
     * @param lines 数据行 IRecord的流
     * @param reverse 是否对lines 中的数据左reverse
     * @return Graph 图结构
     */
    public Graph graph(final List<IRecord> lines,final boolean reverse) {
        return graph(lines.stream(),reverse,null,null);
    }
    
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个 <br>
     * KVPair[基层名/序号,顶点名的]结构。 <br>
     * 
     * 生成一个图结构<br>
     * 所谓数据行lines 是一个类似于 如下的列表数据 <br>
     * lines =Stream.of(<br>
     *          "1,2,3,4,5,6,7,8,9", <br>
     *          "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作",<br>
     *          "起床/开电视/听新闻/接电话/喝牛奶/出门")<br>
     *      .map(IRecord::STRING2REC);<br>
     * @param lines 数据行 IRecord的流
     * @return Graph 图结构
     */
    public Graph graph(final Stream<IRecord> lines) {
        return graph(lines,false,null,null);
    }
    
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个 <br>
     * KVPair[基层名/序号,顶点名的]结构。 <br>
     * 
     * 生成一个图结构<br>
     * 所谓数据行lines 是一个类似于 如下的列表数据 <br>
     * lines =Stream.of(<br>
     *          "1,2,3,4,5,6,7,8,9", <br>
     *          "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作",<br>
     *          "起床/开电视/听新闻/接电话/喝牛奶/出门")<br>
     *      .map(IRecord::STRING2REC);<br>
     * @param lines 数据行 IRecord的流
     * @param reverse 是否对lines 中的数据左reverse
     * @return Graph 图结构
     */
    public Graph graph(final Stream<IRecord> lines,final boolean reverse) {
        return graph(lines,reverse,null,null);
    }
    
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个
     * KVPair[基层名/序号,顶点名的]结构。
     * 
     * 示例：<br>
     * final var rel3 = app.graph(REC2((Object[])"张三/李四,喜欢1,李四/王五,喜欢2,王五/张三,喜欢3".split(",")) <br>
     * .apply2kvs(IRecord::STRING2REC, e->REC("rel",e)));<br>
     * 生成一个图结构 <br>
     * 
     * 对边属性字段,可以在定义 边属性的同时，指定节点属性，节点属性的语法是  (节点名)(分隔符)(属性名) 分隔符包括：".","$","#",其中#表示边节点的序号,0表示开始节点,<br>
     * 其余表示二号节点。<br>
     * REC2("张三/李四",R("rel","喜欢","张三.address","上海"), // 张三的address为上海   <br>
     *      李四/王五/赵六",R("rel","喜欢","2 # address","北京"),// 赵六的address为北京 <br>
     *      "陈七/王八",R("rel","喜欢","$address","中国") // 陈七 和王八 address 都设置为中国。 <br>
     *  ).apply2keys(IRecord::STRING2REC)// 节点定义。
     * 
     * 不对边进行倒转。reverse 默认为false <br>
     * @param edge2attribues 数据行 IRecord的流:边定义->边属性,比如：STRING2REC("zhansan/lisi")->REC(“relation",”同学“)
     * @return Graph 图结构
     */
    public Graph graph(final Map<IRecord,IRecord> edge2attribues) {
        return graph(edge2attribues,false,null,null);
    }
    
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个 <br>
     * KVPair[基层名/序号,顶点名的]结构。 <br>
     * 
     * 生成一个图结构 <br>
     * 示例：<br>
     * final var rel3 = app.graph(REC2((Object[])"张三/李四,喜欢1,李四/王五,喜欢2,王五/张三,喜欢3".split(",")) <br>
     * .apply2kvs(IRecord::STRING2REC, e->REC("rel",e)));<br>
     * 生成一个图结构 <br>
     * 
     * 对边属性字段,可以在定义 边属性的同时，指定节点属性，节点属性的语法是  (节点名)(分隔符)(属性名) 分隔符包括：".","$","#",其中#表示边节点的序号,0表示开始节点,<br>
     * 其余表示二号节点。<br>
     * REC2("张三/李四",R("rel","喜欢","张三.address","上海"), // 张三的address为上海   <br>
     *      李四/王五/赵六",R("rel","喜欢","2 # address","北京"),// 赵六的address为北京 <br>
     *      "陈七/王八",R("rel","喜欢","$address","中国") // 陈七 和王八 address 都设置为中国。 <br>
     *  ).apply2keys(IRecord::STRING2REC)// 节点定义。
     *      
     * @param edge2attribues 数据行 IRecord的流:边定义->边属性,比如：STRING2REC("zhansan/lisi")->REC(“relation",”同学“)
     * @param reverse 是否对lines 中的数据左reverse
     * @return Graph 图结构
     */
    public Graph graph(final Map<IRecord,IRecord> edge2attribues,final boolean reverse) {
        return graph(edge2attribues,reverse,null,null);
    }
    
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个 <br>
     * KVPair[基层名/序号,顶点名的]结构。 <br>
     * 
     * 生成一个图结构
     * 
     * @param edge2attribues 数据行 IRecord的流:边定义->边属性,比如：STRING2REC("zhansan/lisi")->REC(“relation",”同学“)
     * @param reverse 是否对lines 中的数据左reverse
     * @param onVertex 接收到顶点结构的回调数据。
     * @param onEdge 接收到边数据的回调函数
     * @return Graph 图结构
     */
    public Graph graph(final Map<IRecord,IRecord> edge2attribues,final boolean reverse,
        BiConsumer<KVPair<String, Object>,Neo4jApp> onVertex,
        BiConsumer<Tuple2<KVPair<String, Object>,KVPair<String, Object>>,Neo4jApp> onEdge
        ) {
        final var g = this.graph(edge2attribues.keySet().stream());
        edge2attribues.forEach((line,attributes)->{
            // 边属性的处理
            line.tuple2Stream().map(g.getEdge_name_renderer()).forEach(edgeName->{
                g.addAttribute(edgeName,attributes);// 设置边属性。
            });// tuple2Stream 边流
            
            // 顶点的属性处理。
            if(attributes!=null && attributes.size()>0)line.tuple2Stream().forEach(edge->{
                attributes// 属性值的处理
                //.filter(kvp->g.is_vertex_attribute_name.test(kvp.key()))// 提取属性名
                .foreach((name,value)->{// 属性名与属性值
                   final var matcher = g.VERTEX_ATTRIBUTE_NAME_PATTERN.matcher(name);
                   if(matcher.matches()) {
                       final var vertex_name = matcher.group(1);// 顶点名
                       final var delim = matcher.group(2); // 分隔符
                       final var attr_name = matcher.group(3);// 属性名
                       final var v0 = edge._1();// 开始点
                       final var v1 = edge._2();// 终止点
                       KVPair<String,Object> v = null;// 当亲啊的节点位置
                       if(delim.matches("#+")) {// 使用序号代替节点名
                           try {
                               final var no = Integer.parseInt(vertex_name);
                               final var v0_no = Integer.parseInt(v0.key());
                               final var v1_no = Integer.parseInt(v1.key());
                               if(no==v0_no) v= v0; // 开始节点
                               if(no==v1_no) v= v1;// 结束节点
                           }catch(Exception e) {};
                       }else if(delim.matches("\\.+")){// vertex_name
                           if(vertex_name.equals(edge._1().value()))v=v0; // 开始节点
                           if(vertex_name.equals(edge._2().value()))v=v1; // 结束节点
                       } else {// 共通属性
                           g.addVertexAttribute(v0,REC(attr_name,value));// 仅当名称有效。
                           g.addVertexAttribute(v1,REC(attr_name,value));// 仅当名称有效。
                       }
                       if(v!=null)g.addVertexAttribute(v,REC(attr_name,value));// 仅当名称有效。
                   }//if
                });// foreach
                
            });
        });
        return g;
    }
   
    /**
     * 边路径lines的定义结构IRecord的[KVPair<String,Object>]是[{阶层名,顶点信息}] 的机构，顶点信息一般为顶点的名称。即 路径Record 是一个 <br>
     * KVPair[基层名/序号,顶点名的]结构。 <br>
     * 
     * 生成一个图结构<br>
     * 所谓数据行lines 是一个类似于 如下的列表数据 <br>
     * lines =Stream.of(<br>
     *          "1,2,3,4,5,6,7,8,9", <br>
     *          "起床/穿衣服/洗脸/刷牙/吃早饭/出门/乘地铁/到公司/开电脑/收发邮件/开始一天的工作",<br>
     *          "起床/开电视/听新闻/接电话/喝牛奶/出门")<br>
     *      .map(IRecord::STRING2REC);<br>
     * @param lines 数据行 IRecord的流
     * @param reverse 是否对lines 中的数据左reverse
     * @param onVertex 接收到顶点结构的回调数据。
     * @param onEdge 接收到边数据的回调函数
     * @return Graph 图结构
     */
    public Graph graph(final Stream<IRecord> lines,final boolean reverse,
        BiConsumer<KVPair<String, Object>,Neo4jApp> onVertex,
        BiConsumer<Tuple2<KVPair<String, Object>,KVPair<String, Object>>,Neo4jApp> onEdge
        ) {
        final var g = new Graph();// 图结构数据
        this.on_initialize_handler.accept(g);// 初始化准备
        lines.forEach(e -> {
            final var rec  = reverse ? e.reverse() : e;
            rec.tuple2Stream().forEach(edge -> {
                final List<KVPair<String, Object>> vertexes = edge.tt();
                vertexes.forEach(vertex->{
                    g.vertexes.add(vertex);
                    if(this.on_vertex_handler!=null)this.on_vertex_handler.accept(vertex,g);// 搜集顶点信息
                });// forEach 
                g.edges.add(edge);// 搜集边信息
                if(this.on_edge_handler!=null)this.on_edge_handler.accept(edge,g);
            });// tuple2Stream
        });// forEach
        this.on_finalize_handler.accept(g);
        return g;
    }
    
    /**
     * 顶点处理器
     * @param on_vertex_handler 顶点处理器
     */
    public void set_vertex_handler(BiConsumer<KVPair<String, Object>,Graph> on_vertex_handler) {
        this.on_vertex_handler = on_vertex_handler;
    }
    
    /**
     * 边处理器
     * @param on_edge_hanlder 边处理器
     */
    public void set_edge_handler(BiConsumer<Tuple2<KVPair<String, Object>,KVPair<String, Object>>,Graph> 
        on_edge_hanlder) {
        this.on_edge_handler = on_edge_hanlder;
    }
    
    /**
     * 初始化处理器
     * @param on_finalize_handler 初始化处理器
     */
    public void set_intialize_handler(Consumer<Graph> on_initialize_handler) {
        this.on_initialize_handler = on_initialize_handler;
    }
    
    /**
     * 尾处理处理器 
     * @param on_edge_hanlder 尾处理处理器
     */
    public void set_finalize_handler(Consumer<Graph> on_initialize_handler) {
        this.on_initialize_handler = on_initialize_handler;
    }
    
    /**
     * 去除重复数据
     * @param <T> 元素类型
     * @param <U> ID类型
     * @param cc 数据源
     * @param t2id 唯一值 id
     * @return 唯一值的数据源流
     */
    public static <T,U> Stream<T> distinct(Collection<T>cc,Function<T,U>t2id){
       return distinct(cc.stream(),t2id);
     };
     
     /**
      * 去除重复数据
      * @param <T> 元素类型
      * @param <U> ID类型
      * @param cc 数据源
      * @param t2id 唯一值 id
      * @return 唯一值的数据源流
      */
     public static <T,U> Stream<T> distinct(Stream<T>stream,Function<T,U>t2id){
         @SuppressWarnings("unchecked")
         final var collector = Collector.of(()->new AtomicReference<T>(null),
             (atom,a)->atom.set((T)a),
             (aa,bb)->aa.get()==null?bb:aa);
         final var mm = stream.collect(Collectors.groupingBy(t2id,collector));
         return mm.values().stream().map(e->e.get());
    };
    
    /**
     * 测试数据库系统
     * @author gbench
     *
     */
    @JdbcConfig(url="jdbc:neo4j:bolt://localhost/mydb?nossl",
          driver="org.neo4j.jdbc.Driver",user="neo4j",password="123456")
    public interface Database extends ISqlDatabase{}
    
    /**
     * 修改注解中的字段内容
     * 
     * @param <T> 注解的类的类型
     * @param targetClass 目标位置
     * @param annotationClass 注解类型
     * @param field 注解中的字段名称
     * @param value 注解中的字段值
     */
    public static <T extends Annotation> void updateAnnotaion(Class<?>targetClass,
        Class<T> annotationClass,String field,String value) {
        SET_FIELD_OF_ANNOTATION(ANNO(targetClass, annotationClass), field,value);
    }
    
    /**
     * 修改注解中的字段内容
     * 
     * @param <T> 注解的类的类型
     * targetClass 目标位置 默认为Database.class
     * annotationClass 注解类型 默认为JdbcConfig.class
     * @param field 注解中的字段名称
     * @param value 注解中的字段值
     */
    public static <T extends Annotation> void updateDatabaseAnnotaion(String field,String value) {
        SET_FIELD_OF_ANNOTATION(ANNO(Database.class, JdbcConfig.class), field,value);
    }
    
    /**
     * 创建数据库对象，并提取其中的Jdbc对象。这是一个简化操作。
     * 获取jdbc 对象
     * @return jdbc 对象
     */
    public static Jdbc getJdbc() {
        final var db= Jdbc.newInstance(Database.class);
        final var jdbc=db.getProxy().findOne(Jdbc.class);
        return jdbc;
    }
    
    private BiConsumer<KVPair<String, Object>,Graph> on_vertex_handler = // 顶点的处理
        (kvp,graph)->{};
    private BiConsumer<Tuple2<KVPair<String, Object>,KVPair<String, Object>>,Graph> on_edge_handler = // 边的处理
        (kvp,graph)->{};
    private Consumer<Graph> on_initialize_handler = // 初始处理
        (graph)->{};
    private Consumer<Graph> on_finalize_handler = // 收尾处理
        (graph)->{};
}
