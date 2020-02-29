package gbench.common.tree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.function.Predicate;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;

import gbench.common.tree.LittleTree.Term.TermType;

import static gbench.common.tree.LittleTree.IRecord.FIELDS;
import static gbench.common.tree.LittleTree.IRecord.OBJ2SQLTYPE;
import static gbench.common.tree.LittleTree.IRecord.REC;
import static gbench.common.tree.LittleTree.Jdbc.substitute;
import static gbench.common.tree.LittleTree.Json.json2recs;
import static gbench.common.tree.LittleTree.SimpleRecord.REC2;
import static gbench.common.tree.LittleTree.Tuple2.TUP2;

/**
 *
 * @author gbench
 *
 */
public class LittleTree {

    /**
     * 简单的文本读取类：小型文件，可以把文件内容全部读入内存的文件
     * @author gbench
     *
     */
    public static class SimpleFile{

        /**
         * 构造函数
         * @param path 文件绝对路径
         */
        public SimpleFile(String path){
            try {
                is = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        /**
         * 文件的输入流
         * @param is 输入流
         */
        public SimpleFile(InputStream is) {
            this.is = is;
        }


        /**
         * 读取数据行 读取行数据
         * @return
         */
        public List<String> readlines(){
            return readlines(null);
        }

        /**
         * 读取数据行
         *
         * @param filter 行数据过滤器
         * @return
         */
        public List<String> readlines(final Predicate<String> filter){
            BufferedReader br = null;
            List<String> ll = null;
            final Predicate<String> flt = filter==null?(s->true):filter;
            try {
                br = new BufferedReader( new InputStreamReader(is,"utf8") );
                ll = br.lines().filter(flt).collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
            }// try

            return ll;
        }

        private InputStream is ;
    }

    /**
     * 术语项目
     * @author gbench
     *
     */
    public static class Term{
        /**
         * 字段项目
         * @param data 自动换项目的字符串序列
         */
        public Term(String data) {
            final List<Predicate<String>> handlers = Arrays.asList(foreach_handler,where_handler,symbol_handler);
            final Iterator<Predicate<String>> itr = handlers.iterator();
            while(itr.hasNext()&&itr.next().test(data));
        }//Term

        /**
         * 符号处理器
         * @param data
         * @return
         */
        public Predicate<String> symbol_handler=(String data) ->{
            final var SYMBOL_PATTERN="\\s*([_$a-z][a-z0-9]*)\\s*";// 符号模式
            final var mat = Pattern.compile(SYMBOL_PATTERN,Pattern.CASE_INSENSITIVE).matcher(data);
            if(mat.find()) {
                this.data = data;
                this.type= TermType.SYMBOL;
                this.pattern = SYMBOL_PATTERN;
                return false;
            }else
                return true;
        };

        /**
         * foreach 的处理
         * @param data
         * @return
         */
        public Predicate<String> foreach_handler=(String data)->{
            final var FOREACH_PATTERN = "foreach\\s+([a-z_]+)\\s+in\\s+([a-z%-_]+)\\s+(.+)";// foreach 模式
            final var p = Pattern.compile(FOREACH_PATTERN,Pattern.CASE_INSENSITIVE);//%表示字段替换不需要甲引号
            final var m = p.matcher(data.trim());

            if(m.find()) {
                final var loopvar= m.group(1);// 迭代变量
                final var container = m.group(2);// 迭代的数据集合范围
                final var loopbody = m.group(3);// 循环体

                this.foreachTerm = new ForeachTerm(loopvar,container,loopbody);
                //System.out.println(loopvar+"==>"+container+"==>"+loopbody);
                final var pat = Pattern.compile(loopvar+"\\s*\\.\\s*"+"([_0-9a-z\\$]+)",Pattern.CASE_INSENSITIVE);// 注意不区分大消息
                final var matcher = pat.matcher(loopbody);
                while(matcher.find()) {
                    final var placeholder = matcher.group();// 占位符 是由[ 循环变量/loopvar+"."+字段属性名/keyname]二字段来结构的
                    final var keyname = matcher.group(1);// 对应到Record对象中的属性值,也就是占位符在"."之后的部分。
                    this.foreachTerm.add(placeholder, keyname);
                }//while matcher
                this.type = TermType.FOREACH;// 修改类为foreach
                this.data = data;
                this.pattern = loopvar;
                return false;
            }else{// if(m.find)
                return true;
            }//if
        };

        /**
         * 符号处理器
         * @param data
         * @return
         */
        public Predicate<String> where_handler=(String data) ->{
            final String WHERE_PATTERN = "where\\s+(.+)";// foreach 模式
            Matcher mat = Pattern.compile(WHERE_PATTERN,Pattern.CASE_INSENSITIVE).matcher(data);
            if(mat.find()) {
                this.data = data;
                this.type= TermType.WHERE;
                this.pattern = WHERE_PATTERN;
                //System.out.println(mat.group(1));
                final var m = Pattern.compile("([^#]+)\\s*#\\s*([a-z]+)([^a-z_]+)\\s*").matcher(mat.group(1));

                this.whereTerm = new WhereTerm();
                this.whereTerm.setData(data);
                while(m.find()) {
                    this.whereTerm.add(m.group(1),m.group(2),m.group(3));
                };

                return false; // 不再进行处理
            }else
                return true;
        };

        /**
         * 符号项目
         * @return
         */
        public String getSymbol() {
            return this.getType()==TermType.SYMBOL?data:null;
        }

        /**
         * 符号项目
         * @return
         */
        public TermType getType() {
            return type;
        }

        /**
         *
         * @param map
         * @return
         */
        public String toForeachString(Map<String,Object> map) {
            return this.foreachTerm!=null?this.foreachTerm.toString(map):null;
        }

        /**
         *
         * @param map
         * @return
         */
        public String toWhereString(Map<String,Object> map) {
            return this.whereTerm!=null?this.whereTerm.toString(map):null;
        }

        /**
         * 字符串
         */
        public String toString() {
            return this.data;
        }

        public enum TermType{SYMBOL,FOREACH,WHERE};// 语句项

        /**
         * 条件表达式
         * @author gbench
         *
         */
        public class WhereTerm{
            private List<Tuple3<String,String,String>> tuples = new LinkedList<>();
            private String data = null;

            /**
             *
             * @param key
             * @param value
             */
            void add(String key,String value,String postfix) {
                tuples.add(new Tuple3<String,String,String>(key,value,postfix));
            }

            /**
             * SQL语句格式化输出
             * @param map 参数列表 key->value
             * @return 字符换
             */
            public String toString(Map<String,Object> map) {
                StringBuffer buffer = new StringBuffer();
                Stream<Tuple3<String,String,String>> stream = this.tuples.stream();
                //System.out.println(this.data);
                Function<Object,List<Tuple2<String,Object>>> variables = (oo)->{ // 提取环境变量
                    List<Tuple2<String,Object>> vars = new LinkedList<>();
                    if(oo!=null) {
                        if(oo instanceof IRecord) oo = ((IRecord)oo).toMap();
                        if(oo instanceof Collection) {
                            @SuppressWarnings("unchecked")
                            Collection<Object> coll = (Collection<Object>)oo;
                            coll.forEach(o->{
                                if(o instanceof String) vars.add(new Tuple2<String,Object>(o+"",o));
                            });
                        }else if (oo.getClass().isArray()) {
                            Object[] aa = (Object[])oo;
                            for(Object a:aa)vars.add(new Tuple2<String,Object>(a+"",a));
                        }else if(oo instanceof Map){
                            Map<?,?> aa = (Map<?,?>)oo;
                            vars.addAll(aa.entrySet().stream().map(e->
                                new Tuple2<String,Object>(e.getKey()+"",e.getValue()))
                                    .collect(Collectors.toList()));
                        }else {
                            vars.add(new Tuple2<String,Object>(oo+"",oo));
                        }//if
                    }//  if oo
                    return vars;
                };

                stream.forEach(tuple->{
                    String line = "";
                    Object oo = map.get(tuple._2());// 获取字段并展开字段项目
                    String func_name ="";
                    String prefix = "";
                    Matcher mat = Pattern.compile("([a-z_]+)\\s*$").matcher(tuple._1());
                    if(!mat.find())return;
                    func_name = mat.group(1);// 获得关键词
                    prefix = tuple._1().substring(0,tuple._1().lastIndexOf(func_name));
                    if("and_null".equalsIgnoreCase(func_name)) {
                        line = JOIN(MAP(variables.apply(oo),o->o._1()+" is null")," AND ");
                    }else if("and_notnull".equalsIgnoreCase(func_name)) {
                        line = JOIN(MAP(variables.apply(oo),o->o._1()+" is not null")," AND ");
                    }else if("or_notnull".equalsIgnoreCase(func_name)) {
                        line = JOIN(MAP(variables.apply(oo),o->o._1()+" is not null")," OR ");
                    }else if("and_in".equalsIgnoreCase(func_name)) {
                        line = JOIN(MAP(variables.apply(oo),o->o._1()+" not in "+tuple._2)," AND ");
                    }else if("and_not_in".equalsIgnoreCase(func_name)) {
                        line = JOIN(MAP(variables.apply(oo),o->o._1()+" not in "+tuple._2)," AND ");
                    }else if("and_equals".equalsIgnoreCase(func_name)) {
                        line = JOIN(MAP(variables.apply(oo),o->o._1().replaceAll("##","")+" = "+
                            (((o._1()+"")).startsWith("##")? o._2() :
                                "'"+o._2()+"'"))," AND ");
                    }//if

                    buffer.append(prefix+line+tuple._3);
                });

                return buffer.length()<=0?"":"where "+buffer.toString();
            }

            public String getData() {
                return data;
            }

            public void setData(String data) {
                this.data = data;
            }
        }

        /**
         * Foreach Term的结构处理
         * @author gbench
         */
        class ForeachTerm {

            /**
             * 构建foreach 结构
             * @param loopvar 循环变量
             * @param container 循环体容器
             * @param loopbody 循环体
             */
            ForeachTerm(String loopvar,String container,String loopbody){
                this.loopvar = loopvar;
                this.container = container;
                this.loopbody = loopbody;
            }

            /**
             * 添加一个占位符并指定与其对应的键值
             * @param placeholder 占位符号
             * @param keyname 字段的键名
             */
            public void add(String placeholder,String keyname) {
                kvs.add(new KVPair<String,String>(placeholder,keyname));
            }

            /**
             * 获得循环变量
             * @return
             */
            public String getLoopvar() {
                return loopvar;
            }

            public void setLoopvar(String loopvar) {
                this.loopvar = loopvar;
            }

            /**
             * 获得容器对象
             * @return
             */
            public String getContainer() {
                return container;
            }

            public void setContainer(String container) {
                this.container = container;
            }

            /**
             * 获得键值名称
             * @return
             */
            public List<KVPair<String, String>> getKvs() {
                return kvs;
            }

            public void setKvs(List<KVPair<String, String>> kvs) {
                this.kvs = kvs;
            }

            /**
             *
             * @param map 映射对象:变量名与变量值的映射集合
             * @return
             */
            @SuppressWarnings("unchecked")
            public String toString(Map<String,Object> map) {

                List<IRecord> ll = null;
                final String foreachExpr = "foreach "+loopvar+" in "+" "+container+" "+loopbody;// foreach 的源代码表达式
                try{
                    Object obj = map.get(this.getContainer());
                    if(obj==null)return foreachExpr;
                    Class<?>objCls = obj.getClass();
                    if(obj instanceof Collection || objCls.isArray()) {
                        Collection<Object> oo = objCls.isArray()?(Arrays.asList(obj)):(Collection<Object>)obj;
                        if(oo.size()>0) {// 集合类非空
                            long cnt = oo.stream().filter(o->o instanceof IRecord).count();
                            if(cnt!=oo.size()) {// 尝试给予人工修复
                                //System.out.println("List中含有非IRecord对象"+oo);
                                map.put(this.getContainer(),
                                    oo.stream().map(o->{// 在kvs增加占位符placeholder->键名keyname之间的对应关系
                                        //特别注意对于非IRecord数据placeholder与keyname一样都等于循环变量
                                        kvs.add(new KVPair<String,String>(loopvar,loopvar));
                                        // 转为IRecord对象，仅有一个乙循环变量名作为键名的IRecord元素
                                        return new SimpleRecord().add(loopvar,o);})
                                            .collect(Collectors.toList()) );
                            }//  尝试给予人工修复
                        }else {
                            System.out.println( container+" 为 空list");
                            return foreachExpr;
                        }//if
                    }//if
                    ll = (List<IRecord>)map.get(this.getContainer());
                }catch(Exception e) {
                    e.printStackTrace();
                }
                if(ll == null) return foreachExpr;

                return ll.stream().map(rec->{
                    StringBuffer buffer = new StringBuffer();
                    StringBuffer segmentBuffer = new StringBuffer();// 代码片段：用于从循环体loopbody中读入字符
                    Map<String,Object> f2v = rec.toMap();// 字段与值的映射,field-->value, 值集合
                    for(int i=0;i<loopbody.length();i++) {
                        segmentBuffer.append(loopbody.charAt(i));// 读入字符,一个一个的读入占位符

                        for( KVPair<String,String> kv:kvs ) {// kvs 字段值 fld.name --> name, 字段表达式对应到 rec中的属性名。
                            String placeholder = kv.key();// 占位符,fld.name
                            Boolean withNum = false;//默认以字符串形式显示，用双引号括起来，如果键名以#开头则使用数字格式不用引号括起来

                            if(f2v.containsKey(kv.value()) &&
                                segmentBuffer.indexOf(placeholder)>=0 ){// 一旦出现占位符，立即给予替换。替换占位符号,
                                Object value = f2v.get(kv.value());// 获取对象值
                                value = value==null?"":value;
                                int s = segmentBuffer.lastIndexOf(placeholder);// 最近读出的placeholder的开头位置。
                                if(s>0 && segmentBuffer.charAt(s-1)=='%') {s--;withNum=true;}//%表示值为数字类型。
                                if(placeholder.endsWith("$"))withNum=true;
                                if(value instanceof Number) withNum=true;// 如果值是数字类型就一定会不加引号的。
                                //根据 withNum的状态决定是否 为值添加引号
                                segmentBuffer.replace(s, segmentBuffer.length(), withNum?value+"":"'"+value+"'");
                                buffer.append(segmentBuffer);
                                segmentBuffer = new StringBuffer();
                                continue;
                            }//if
                        }//for
                    }//for i=0;

                    buffer.append(segmentBuffer);// 补充剩余的数据
                    //System.out.println(buffer);
                    return buffer;
                }).collect(Collectors.joining(", "))+" ";
            }

            /**
             * 一个 foreach 结构是这样的形式：
             * foreach loopvar in container loopbody
             * 其中 在 loopody 会出现 placeholder,placeholder 一般是 loopvar+"."+keyname
             * 但这并不是一定的。比如对于非IRecord的集合数据，loopvar可以干脆就是keyname
             * keyname对应到具体的字段名称。这样我们就可以根据字段名称给与获取具体的值了。
             * placeholder 用于在替换循环体中的数值信息。
             *
             * 写几个例子:
             * foreach u in user (user.name,user.password):
             * ----------------------------------------------
             * placeholder          keyname
             * user.name            path
             * user.password        password 
             * kvs:user.name->path,user.password->password
             *
             * foreach u in user u:
             * --------------------------------------------
             * placeholder          keyname
             * u                    u
             * kvs:u->u
             */
            private String loopvar;// 循环变量名
            private String container;// 循环容器的，也就一个List的对象集合(一般为IRecord对象)
            private String loopbody;//循环体
            private List<KVPair<String,String>> kvs = new LinkedList<>();
        }//ForeachTerm

        /**
         * 返回模式
         * @return
         */
        public String getPattern() {
            return pattern;
        }

        private String data;// 字段项目的字符串序列
        private TermType type; // 字段项目类型,简单符号 还是foreach结构
        private ForeachTerm foreachTerm = null;// forEach 字段项目
        private WhereTerm whereTerm = null;
        private String pattern = null;// 模式

        public final static String TERM_PATTERN = "\\$?\\s*\\{([^\\{\\}]+)\\}";
    }

    /**
     * 命名SQL语句对象，每个SQL都有一个名字
     * 含有模板参数(unix风格和mybatis风格) #variable ${variable}
     * 占位符即模板参数式样：#xxx 字符串类型, ##xxx 数值类型,或是 ${xxx}
     * 一个SQL对象包含有：
     * 名称:name，一个sql模板数据,
     * 模板:sqltpl， sql语句模板或者框架，含有必要的参数，参数可以有sqlctx来提供 或者个与计算生成。参数采用 #xxx 字符串类型, ##xxx 数值类型,或是 ${xxx} 字符串结尾会添加换行符号类型  来进行占位,其中
     *  语句上下文：sqlctx 一系列的环境参数，参数蚕蛹键值对的形式 在sqlctx 以IRecord为单位进行存放。 sqlctx 是一个IRecord 集合用来表示多组数据，比如：插入语句 的多条数据记录。
     * 
     * @author gbench
     */
    public static class SQL{

        /**
         * 这是一个查询语句
         * @param name 语句名称
         * @param sqltpl 查询语句，参数采用 #xxx 字符串类型, ##xxx 数值类型,或是 ${xxx} 字符串结尾会添加换行符号类型  来进行占位,其中
         */
        public SQL(final String name,final String sqltpl) {
            this.name = name;
            this.sqltpl = sqltpl;
        }

        /**
         * 命名SQL语句对象，每个SQL都有一个名字
         * @param name SQL语句的名，可以是人资字符串，由于标识SQL，便于检索
         * @param rec SQL 语句的上下文数据：比如插入的操作的 插入记录
         */
        public SQL(final String name,IRecord rec) {
            this.name = name;
            this.sqlctx = Arrays.asList(rec);// 保证sqlctx为一个记录集合
        }
        
        /**
         * 命名SQL语句对象，每个SQL都有一个名字
         * @param name SQL语句的名，可以是人资字符串，由于标识SQL，便于检索
         */
        public SQL(final String name) {
            this.name = name;
            this.sqlctx = Arrays.asList((IRecord)null);// 行数据
        }

        /**
         * 命名SQL语句对象，每个SQL都有一个名字
         * @param name SQL语句的名，可以是人资字符串，由于标识SQL，便于检索
         * @param sqlctx 行数据，SQL 语句的上下文数据：比如插入的操作的，插入记录
         */
        public SQL(final String name,final List<IRecord> sqlctx) {
            this.name = name;
            this.sqlctx = sqlctx;
        }

        /**
         * 语句的名字
         * @return
         */
        public String name() {return this.name;};

        /**
         * 查询语句
         * @return
         */
        public String string() {return this.sqltpl;};

        /**
         *  数据查询:这里关于 字符转义的描述很有问题，有时间的是需要要补充
         *    带有变量值的语句内容
         *
         *  变量的替换规则如下:
         * 1、#便开头的变量 使用 单引号进行包被
         * 2、##开头的变量不是使用单引号包被
         * 3、${foreach id in ids id } 为单值形式foreach 结构。ids为单值列表
         * 4、${foreach fld in kvset %fld.name=fld.value} 为复合值形式的列表,kvset为集合列表，
         *           列表中每个元素为 字段记录,每个字段属性有name,和value
         *        如果以%开头，会根据替换值的类型智能判断是否添加单引号：为数字不加单引号，否则加上单引号
         * 由于 rec 中的值可能会被加上单引号，所以转义字符需要进行手动给予转换完成。以此保证语法合理性。
         * 5.如果占位符以$结尾则则视该值为数字，即不加引号：此种标识为为强制转换为数字，而不管该字段是否是真的为为数字类型，即使是字符串也不加引号。会覆盖掉%的规则
         *
         * 例子：
         * kvset 中的字段标识符的 正则表达式的格式为：[_0-9a-z\\$]+,即java 中标识符的规则。其中以$结尾的字段一般用于标识这是一个数字类型的字段。
         * 1、List<IRecord> kvset = Arrays.asList(
         *       REC2("name","id","value",437),
         *       REC2("name","name","value","张三"),
         *       REC2("name","sex","value","男"),
         *       REC2("name","address","value","上海市徐汇区") );
         *    IRecord rec = REC2("kvset",kvset,"code",98)
         *  update t_student set ${ foreach fld in kvset %fld.name=fld.value} where code=#code
         *     生成：update t_student set id=437, name='张三', sex='男', address='上海市徐汇区'  where code='98'
         * 注意：
         *     若是kvset中的  是这样的结构：注意kvset中的value$,有一个'$'后缀,于是fld.value$ 就会被视作数字：而不管它是否真的是数字了，
         *     即$是强制为数字额形式来显示（去除单引号）
         *     final var kvset = Arrays.asList( // 数据列表
         *           REC2("name", "id", "value$", 437),
         *           REC2("name", "name", "value$", "张三"),
         *           REC2("name", "sex", "value$", "男"),
         *           REC2("name", "address", "value$", "上海市徐汇区")
         *       );// kvset
         *       update t_student set ${ foreach fld in kvset %fld.name=fld.value$ } where code=##code
         *        生成update t_student set id=437, name=张三, sex=男, address=上海市徐汇区  where code=98
         *       请注意：##code是用于非foreach结构的数字类型的显示方法，而fld.value$是专用于foreach 结构的强制显示为数字的方法。
         *
         * 2、IRecord rec = REC2("users",Arrays.asList(1,2,3,4))
         *   select * from t_student where id in (${ foreach id in users id } )
         *   生成: select * from t_student where id in (1, 2, 3, 4  )
         *
         * 3、IRecord rec = REC2("users",Arrays.asList("1,2,3,4".split(",")))
         *   select * from t_student where id in ( ${ foreach id in users id } )
         *   生成: select * from t_student where id in ('1', '2', '3', '4'  )
         *
         * @param rec 被替换数据的 键值对
         * @return 替换后的sql语句。
         */
        public String string(final IRecord rec) {
            if(rec==null) return this.string();
            return this.string(rec.toMap());
        }

        /**
         * 带有变量值的语句内容。 用map中的值数据替换掉  sql模板语句的内容。
         * @param map 变量的名与值的集合
         * @return SQL语句
         */
        public String string(final Map<String,Object> map) {
            String s= this.string();
            final Function<Object,String> escape = (obj)->(obj+"").replace("(", "\\(").replace(")","\\)").replace("$","\\$");
            for(Term term:this.terms()) { 
                //try {// 确保出现错误依旧可以运行
                    if(term.getType() == TermType.SYMBOL) {// 记录符号
                        Object obj = map.get(term.getSymbol());
                        String v = (obj+"").replace("$", "\\$");// 转义￥符号
                        if(!v.matches("[.\\d]+"))v=v.replace(".", "\\.");// 如果不是字符进行。转义
                        //  System.out.println(v); // 测试数据模式
                        if(v==null)continue;
                        s=s.replaceAll("\\$?\\s*\\{\\s*"+term+"\\s*\\}","'"+v+"'");
                        s=s.replaceAll("##"+term,v+"");// 使用双#号表示为数字不需要用括号括起来
                        s=s.replaceAll("#"+term,"'"+v+"'");// 使用单警号表示是字符串需要采用括号括起来
                    }else if(term.getType() == TermType.FOREACH) {// foreach 解析
                        Object t = term.toForeachString(map);// 获得term 的替换后的数据
                        if (t==null)continue;
                        String foreach_term="\\$?\\s*\\{\\s*"+escape.apply(term.data)+"\\s*\\}";// foreach的字符描述
                        s=s.replaceAll(foreach_term,escape.apply(t));
                    }else if(term.getType() == TermType.WHERE) {
                        final Object t = term.toWhereString(map);// 获得term 的替换后的数据
                        if ( t==null ) continue;
                        final String where_term="\\$?\\s*\\{\\s*"+escape.apply(term.data)+"\\s*\\}";// foreach的字符描述
                        s=s.replaceAll(where_term,escape.apply(t));
                    }
                //}catch(Exception e) {e.printStackTrace();}
            }//for

            return s;
        };

        /**
         * 插入一条数据记录到name所在的表中
         * @param rec 模板命名参数的键值对儿集合：用recB-recA的中的变量来实例化SQL模板，生成一个insert 插入语句
         * @return
         */
        public String insert(final IRecord rec) {
            return insert(rec,kv->kv.value()!=null);
        }

        /**
         * 插入数据
         * @param rec 插入的数据的字段列表，用recB-recA的中的变量来实例化SQL模板，生成一个insert 插入语句
         * @param pfilter 插入的字段过滤器
         * @return
         */
        public String insert(final IRecord rec,final Predicate<KVPair<String,Object>>pfilter) {
            final var buffer = new StringBuffer();
            final var flds = new LinkedList<String>();
            final var vals = new LinkedList<String>();
            Predicate<KVPair<String,Object>> pfiltertmp = pfilter;
            if(pfiltertmp==null)pfiltertmp=(kv)->true;
            final Predicate<KVPair<String,Object>> pft = pfiltertmp;
            rec.stream().filter(kv->pft.test(kv)) // 删除空值字符串
                .forEach(e->{
                    flds.add(e.key());
                    vals.add("'"+(e.value()+"").replace("'", "\\'")+"'");
                });// forEach
            buffer.append("insert into "+this.name+" (");
            buffer.append(flds.stream().collect(Collectors.joining(",")));
            buffer.append(") values (");
            buffer.append(vals.stream().collect(Collectors.joining(",")));
            buffer.append(")");
            return buffer.toString();
        }

        /**
         * 插入数据
         * @param recs 插入的数据的字段列表，用recB-recA的中的变量来实例化SQL模板，生成一个insert 插入语句
         * @param pfilter 插入的字段过滤器
         * @return
         */
        public String insert(final List<IRecord> recs, final Predicate<KVPair<String,Object>>pfilter) {
            return recs.stream().map(rec->this.insert(rec,pfilter))
                .collect(Collectors.joining(";\n"));
        }

        /**
         * 生成insert 插入语句
         * @return insert 插入语句
         */
        public String insert() {
            final var line = this.insert(this.sqlctx,kv->kv.value()!=null);
            return line.endsWith(";")?line:(line+";");
        }

        public String insert(final Predicate<KVPair<String,Object>>pfilter) {
            return this.insert(this.sqlctx,pfilter);
        }

        /**
         * 字段信息扩展
         * @param recB 覆盖到本字段信息的数据记录
         * @return
         */
        public IRecord extend (IRecord recB) {
            if(this.sqlctx.size()<=0)return recB.duplicate();
            IRecord recA = this.sqlctx.get(0);
            return SimpleRecord.extend(recA,recB);
        }

        /**
         * 注意：不能把id 设置成null
         *
         * 使用 recB 去更新 recA
         * @param recA 老数据
         * @param recB 新数据，用recB-recA的中的变量来实例化SQL模板，生成一个update语句
         * @param which 更新范围,IRecord 解构个字段采用and 进行过滤,String 类型直接拼接到 update 语句结尾
         * @return 更新的sql语句
         */
        public String update(IRecord recA,IRecord recB,Object which) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("update "+this.name());

            if(recA==null)recA = new SimpleRecord();
            if(recB==null)recB = new SimpleRecord();

            List<String> keys = recA.kvs().stream()
                .map(e->e.key()).collect(Collectors.toList());
            keys.addAll(recB.kvs().stream()
                .map(e->e.key()).collect(Collectors.toList()));
            keys = keys.stream().distinct().collect(Collectors.toList());
            final var kvs = new
                LinkedList<KVPair<String,Object>>();// 键值对
            //System.out.println("全量的更新键值表："+keys);
            for(String key:keys) {
                Object oA = recA.get(key);
                Object oB = recB.get(key);
                if(oA!=null && oB!=null && oA.equals(oB)) continue;
                kvs.add(new KVPair<String,Object>(key,oB));
            }

            if(kvs.size()>0) {
                buffer.append(" set ");
                //内存添加
                buffer.append(kvs.stream()
                    .filter(kv->("id".equals(kv.key())
                        && kv.value()==null)?false:true) // 过滤掉id==null 的键值
                    .map(e->e.key()+"='"+e.value()+"'")
                    .collect(Collectors.joining(",")));
            }//if kvs.size()>0

            // 指定更新范围
            if(which!=null && which instanceof IRecord) {
                String filter = ((IRecord)which).kvs().stream()
                    .map(e->e.key()+"='"+e.value()+"'")
                    .collect(Collectors.joining(" and "));
                buffer.append(" where "+filter);
            }else if(which!=null && which instanceof String) {
                buffer.append(" where "+which);
            }else if(which==null && recA.get("id")!=null) {// 默认更新id
                buffer.append(" where id='"+recA.get("id")+"'");
            }//if which!=null

            return buffer.toString();
        }

        /**
         * 更新语句
         *
         * @param newData 新数据，用 newData的中的变量来实例化SQL模板，生成一个update语句
         * @param whichone 更新范围, 可以是 IRecord 或 String 
         * IRecord 解构每个字段采用and 进行过滤,
         * String 类型直接拼接到 update 语句结尾
         * @return 更新后的sql 语句
         */
        public String update(IRecord newData,Object whichone) {
            return this.update(this.sqlctx.get(0),newData,whichone);
        }

        /**
         * 更新语句:update all
         *
         * @param newData 新数据 用 newData的中的变量来实例化SQL模板，生成一个update语句
         * @return 更新数据的sql
         */
        public String update(IRecord newData) {
            return this.update(this.sqlctx.get(0),newData,null);
        }

        /**
         * 根据字段的数据类型进行表定义，这个函数不建议在生产环境中使用。
         * 仅用于原型开发时快速测试使用
         * @return
         */
        public List<String> createTable() {
            List<String> sqls = new LinkedList<String>();
            StringBuffer buffer = new StringBuffer();
            buffer.append("-- -----------------------------------\n");
            buffer.append("-- #"+name+"\n");
            buffer.append("-- author:gbench/"+new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss").format(new Date())+"\n");
            buffer.append("-- -----------------------------------\n");
            buffer.append("drop table if exists "+ name+";\n");
            sqls.add(buffer.toString());

            buffer.append("create table "+name+"(\n\t");
            String flds = this.sqlctx.get(0).stream()
                .map(e->e.key()+" "+((e.value()==null)?"varchar(511)":
                        javaType2SqlType(e.value().getClass()
                    )))// map
                .collect(Collectors.joining(",\n\t"));
            buffer.append(flds);
            buffer.append("\n);");
            buffer.append(" comment '"+name+"';");
            sqls.add(buffer.toString());

            return sqls;
        }

        /**
         * @param table 表名
         * @param rec 字段定义
         * @param brief 表摘要信息
         * @return 创建表解构
         */
        public static List<String> createTable(final String table,final IRecord rec,final String brief) {
            final var sqls = new LinkedList<String>();
            var buffer = new StringBuffer();
            buffer.append("-- -----------------------------------\n");
            buffer.append("-- #"+brief+"\n");
            buffer.append("-- author:gbench/"+new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(new Date())+"\n");
            buffer.append("-- -----------------------------------\n");
            buffer.append("drop table if exists "+ table+";\n");
            sqls.add(buffer.toString());

            buffer = new StringBuffer();
            buffer.append("create table "+table+"(\n\t");
            String flds = rec.kvs().stream()
                .map(e->e.key()+" "+e.value()+"")
                .collect(Collectors.joining(",\n\t"));
            buffer.append(flds);
            buffer.append("\n)");
            buffer.append(" comment '"+brief+"';");
            sqls.add(buffer.toString());
            return sqls;
        }

        /**
         * @param table 表名
         * @param rec 字段定义
         * @return 创建表解构
         */
        public static List<String> createTable(String table,IRecord rec) {
            return createTable(table,rec,table);
        }

        /**
         * 获得模板变量
         * @return 去重了之后的模板变量
         */
        public List<Term> terms() {
            return getAllTerms(sqltpl).stream()
                .distinct()
                .collect(Collectors.toList());
        }

        /**
         * javaType转sqlType
         * @param cls JAVA类名
         *
         * @return SQL名
         */
        public static String javaType2SqlType(Class<?> cls){
            if(cls.equals(Double.class)){
                return "DOUBLE";
            }else if(cls.equals(Boolean.class)){
                return "BOOL";
            }else if(cls.equals(Short.class)){
                return "INT";
            }else if(cls.equals(Character.class)){
                return "VARCHAR(512)";
            }else if(cls.equals(Integer.class)){
                return "INT";
            }else if(cls.equals(Date.class)){
                return "TIMESTAMP";
            }else if(cls.equals(Timestamp.class)){
                return "TIMESTAMP";
            }else{
                return "VARCHAR(512)";
            }
        }

        /**
         * 格式化输出
         */
        public String toString() {
            if(this.string()!=null) {
                return this.string()+(terms().size()<=0?"":"\nterms:"+terms());
            }else {
                return this.insert();
            }
        };

        /**
         * 从模板文字中提取文件参数变量
         * @param line 模板
         * @return 返回变量的次序就是变量在模板中出现测序，并且没有去重
         */
        public static List<Term> getAllTerms(String line) {
            List<Term> ll = new LinkedList<>();
            for(String name:VAR_NAME_PATTERNS) {// 变量名模式
                Pattern p = Pattern.compile(name);
                Matcher mat = p.matcher(line);
                while(mat.find())ll.add(new Term(mat.group(1)));
            }//for

            return ll;
        }

        final static String[] VAR_NAME_PATTERNS = {// 参数变量的模式
            Term.TERM_PATTERN, // unix 变量格式
            "#([a-zA-z0-9]+)" // mybatis 变量格式
        };

        /**
         * 生成SQL语句: 依赖于字段反射，从obj(pojo)重提取属性数据，给与闯将对应ORM 关系映射里的 数据库表
         *   生成的表名默认为  obj的类名 :obj.getClass()。getSimpleName()
         * @param obj：待从中提取属性字段数据的javabean
         * @return
         */
        public static String DDL_CREATE_TABLE(Object obj) {
            return DDL_CREATE_TABLE(obj,null);
        }

        /**
         * 生成SQL语句 依赖于字段反射，从obj(pojo)重提取属性数据，给与闯将对应ORM 关系映射里的 数据库表
         * @param obj：待从中提取属性字段数据的javabean
         * @param name：数据表明
         * @return
         */
        public static String DDL_CREATE_TABLE(Object obj,final String name) {
            final Class<?> cls = obj instanceof Class<?> ? (Class<?>)obj:obj.getClass();
            final var tab = name ==null ?cls.getSimpleName():name;
            final var ll = new LinkedList<Tuple2<String,String>>();
            FIELDS(cls).forEach((k,v)->{
                String fld = k;
                String type = OBJ2SQLTYPE(v.getType());
                ll.add(new Tuple2<String,String>(fld,type));
            });
            final var s = "create table if not exists "+tab+" ("+ll.stream().map(e->e._1()+" "+e._2())
                .collect(Collectors.joining(","))+")";
            return s;
        }

        /**
         * 生成SQL语句:
         *   依赖于字段反射，从obj(pojo)重提取属性数据，给与闯将对应ORM 关系映射里的 数据库表
         *   表名默认为  obj的类名 :obj.getClass()。getSimpleName()
         * @param obj:待从中提取属性字段数据的javabean
         * @return
         */
        public static String DDL_DROP_TABLE(final Object obj) {
            return DDL_DROP_TABLE(obj,null);
        }

        /**
         * 生成SQL语句
         *   依赖于字段反射，从obj(pojo)重提取属性数据，给与闯将对应ORM 关系映射里的 数据库表
         *   表名默认为  obj的类名 :obj.getClass()。getSimpleName()
         * @param obj：待从中提取属性字段数据的javabean
         * @param name：表名
         * @return
         */
        public static String DDL_DROP_TABLE(final Object obj,final String name) {
            final Class<?> cls = obj instanceof Class<?> ? (Class<?>)obj:obj.getClass();
            final var tab = name ==null ?cls.getSimpleName():name;
            return "drop table if exists "+tab ;
        }

        /**
         * 清除表中数据
         *   依赖于字段反射，从obj(pojo)重提取属性数据，给与闯将对应ORM 关系映射里的 数据库表
         *   表名默认为  obj的类名 :obj.getClass()。getSimpleName()
         * @param obj：待从中提取属性字段数据的javabean
         * @return
         */
        public static String DDL_TRUNCATE_TABLE(final Object obj) {
            final Class<?> cls = obj instanceof Class<?> ? (Class<?>)obj:obj.getClass();
            return "truncate table "+cls.getSimpleName();
        }

        private String name = null;// sql 名称
        private String sqltpl = null;// sql 语句模板， 占位符即模板参数式样：#xxx 字符串类型, ##xxx 数值类型,或是 ${xxx}
        private List<IRecord> sqlctx = null;// sql 上下文{参数集}
    }

    /**
     * 脚本文件:存放着查询语句的模板
     * @author gbench
     *
     */
    public static class ScriptFile extends SimpleFile{

        /**
         *
         * @param path
         */
        public ScriptFile(String path) {
            super(path);
            this.initialize();
        }

        /**
         *
         * @param is
         */
        public ScriptFile(InputStream is) {
            super(is);
            this.initialize();
        }

        /**
         * 数据初始化
         */
        public void initialize() {
            final var buffer = new StringBuffer();
            final var titlePattern = Pattern.compile(
                "\\s*--\\s*#+\\s*(([^\\s])(.*[^\\s])?)\\s*"); // sql标题开始行
            String title = null;
            for(var line:this.readlines(s->!s.matches("[\\s*]"))){
                var mat = titlePattern.matcher(line);
                if(mat.matches()) {
                    if(title!=null&&buffer.length()>0) {//记录标题
                        stmts.put(title,new SQL(title,buffer.toString()));
                    }//if
                    title = mat.group(1);// 标题
                    buffer.delete(0, buffer.length());//buffer 清空
                }else {
                    if(!line.matches("\\s*--\\s*.*")) {// 去掉注释行
                        if(buffer.length()>0)buffer.append("\n");
                        line=line.replaceAll("\\s*((--)|(//)).*$","");// 去除行尾部的注释  -- 和 --都是注释符号
                        buffer.append(line);
                    }//if
                }//if
            };//for
            stmts.put(title,new SQL(title,buffer.toString()));

            //stmts.forEach((k,v)->{System.out.println("name"+k+"\nstmts\n"+v);});
        }

        /**
         *
         * @param name
         * @return
         */
        public SQL get(final String name) {
            return stmts.get(name);
        }

        public Map<String,SQL> getStmts(){
            return stmts;
        }

        private Map<String,SQL> stmts = new LinkedHashMap<>();// 语句集合

    }
    
    /**
     * Jdbc拦截器:SQL 的处理器
     * @author xuqinghua
     *
     */
    public static abstract class JdbcPatternPreprocessor{
        //构造函数：记得调用初始化函数
        public JdbcPatternPreprocessor(){this.initialize();}
        // 初始化函数
        public abstract void initialize();
        // Pattern/Preprocessor  ARGS
        public class PPARG{
            public  PPARG(Method method, IRecord params, String sqlpattern, Jdbc jdbc) 
            {this.method = method;this.params = params;this.sqlpattern=sqlpattern;this.jdbc=jdbc;}
            public String getName(){return method.getName();}
            protected  Method method; 
            protected IRecord params;  
            protected String sqlpattern; 
            protected Jdbc jdbc;
        }
        public  interface P2S extends Function<PPARG,String>{};//函数:PARG -> String
        /**
         * 这是对 SqlPatternPreProcessor的实现
         * @param method
         * @param params
         * @param sqlpattern
         * @param jdbc
         * @return
         */
        public String handle(Method method, IRecord params, String sqlpattern, Jdbc jdbc){
            var parg = new PPARG(method, params, sqlpattern, jdbc);//把接口参数package成统一参数对象．
            String key  = parg.getName();
            if(!callbacks.has(key))return sqlpattern;
            return callbacks.evaluate(key, parg, String.class);
        }
        /**
         * 在接口函数中实例化：按照如下实现，就可以 
         * Jdbc.newInstance(xxxDatabase.class,new JdbcPatternPreprocessor(){
         * }::handle);
         * 
         * 
         * callbacks=REC("methodName",(PPARG)pp->{return pparg.pattern});
         */
        protected IRecord callbacks;
    }

    /**
     * JDBC访问数据库
     * @author gbench
     *
     */
    public static class Jdbc {

        /**
         * 创建jdbc连接对象
         * @param supplierConn 链接贩卖商，也就是不适用传统的DriverManager,而是第三方来提供。
         */
        public Jdbc(Supplier<Connection> supplierConn){
            this.supplierConn=supplierConn;// 初始化连接贩卖商
        }

        /**
         * 创建jdbc连接对象
         * @param ds 数据源
         */
        public Jdbc(DataSource ds){
            this.supplierConn=()->{
                Connection conn =null;
                try {conn = ds.getConnection();}catch(Exception e) {e.printStackTrace();}
                return conn;
            };// 初始化连接贩卖商
        }

        /**
         * 创建jdbc连接对象
         */
        public Jdbc(String driver,String url,String user,String password){
            this.init(driver, url, user, password);
        }
        
        /**
         * 创建jdbc连接对象
         * 
         * @param rec: 需要包含key:driver,url, user, password
         */
        public Jdbc(IRecord rec) {
            var props = rec.toProps();
            this.init(props.getProperty("driver"), props.getProperty("url"), 
                props.getProperty("user"), props.getProperty("password"));
        }
        
        /**
         * 创建jdbc连接对象
         * 
         * @param props: 需要包含key:driver,url, user, password
         */
        public Jdbc(Properties props) {
            this.init(props.getProperty("driver"), props.getProperty("url"), 
                props.getProperty("user"), props.getProperty("password"));
        }
        
        /**
         * Jdbc上下文:提供一个JDBC的基本操作参数，注意serverTimezone一定需要加入：否则结果返回的时间会有问题：MYSQL采用格林乔治时间．
         * driver 驱动程序：默认 "com.mysql.cj.jdbc.Driver";
         * url:数据据连接字符串，默认  "jdbc:mysql://localhost:3306/hello?serverTimezone=GMT%2B8";
         * user：用户名，默认 "root"
         * password：密码，默认 "123456"
         * @author gbench
         *
         */
        @Target(value = {ElementType.TYPE, ElementType.METHOD})
        @Retention(value = RetentionPolicy.RUNTIME)
        public static @interface JdbcConfig {
            String driver()  default "com.mysql.cj.jdbc.Driver";
            String url() default  "jdbc:mysql://localhost:3306/hello?serverTimezone=GMT%2B8";
            String user() default "root";
            String password() default "123456";
        }
        
        /**
         * 执行一个jdbc语句，value 中提供的是一个 sql 语句模板：比如 ："select * from t_user where id={0}"
         *   语句模板采用 MessageFormat 给与格式化处理，占位符用 {index},形式给与提供，index从0开始，代表装饰函数的第一个参数，第二个参数，以此类推
         *   修饰的模仿默认返回未 List<IRecord>
         *   也可是一个用户自定义的 POJO 此时会采用 IRecord.OBJINIT 来吧IRecord转换成 POJO.
         * 对于是接口中的default 方法，如果该方法的最后一个参数为空数值，则会把SQL语句的返回数值传入到该该参数中，并调用该默认方法．
         * @author gbench
         *
         */
        @Target(value = {ElementType.METHOD})
        @Retention(value = RetentionPolicy.RUNTIME)
        public static @interface JdbcQuery {
            String value() default "";
        }
        
        /**
         * 使用preparedStatement 模板
         * 执行一个jdbc语句，value 中提供的是一个 sql 语句模板：比如 ："select * from t_user where id=?"
         * ? 占位符将由参数顺序给予依次替换．
         *   也可是一个用户自定义的 POJO 此时会采用 IRecord.OBJINIT 来吧IRecord转换成 POJO.
         * 对于是接口中的default 方法，如果该方法的最后一个参数为空数值，则会把SQL语句的返回数值传入到该该参数中，并调用该默认方法．
         * @author gbench
         *
         */
        @Target(value = {ElementType.METHOD})
        @Retention(value = RetentionPolicy.RUNTIME)
        public static @interface JdbcPreparedQuery{
            String value() default "";
        }
        
        /**
         * 这是一个事务级别的SQL语句执行级别．
         * 执行一个jdbc语句，value 中提供的是一个 sql 语句模板：比如 ： "update t_user set name=''{1}'' where id={0}"
         *   语句模板采用 MessageFormat 给与格式化处理，占位符用 {index},形式给与提供，index从0开始，代表装饰函数的第一个参数，第二个参数，以此类推
         * @author gbench
         *
         */
        @Target(value = {ElementType.METHOD})
        @Retention(value = RetentionPolicy.RUNTIME)
        public static @interface JdbcExecute {
            String[] value() default {};// sql语句集合．
        }
        
        /**
         * 这是一个事务级别的SQL语句执行级别．
         * 执行一个jdbc语句，value 中提供的是一个 sql 语句模板：比如 ： "update t_user set name=''{1}'' where id={0}"
         *   语句模板采用 MessageFormat 给与格式化处理，占位符用 {index},形式给与提供，index从0开始，代表装饰函数的第一个参数，第二个参数，以此类推
         * @author gbench
         *
         */
        @Target(value = {ElementType.METHOD})
        @Retention(value = RetentionPolicy.RUNTIME)
        public static @interface JdbcPreparedExecute {
            String[] value() default {};// sql语句集合．
        }
        
        /**
         * 带有连接的数据查询
         * @param conn 数据库连接
         * @param query 查询语句
         * @return 查询结果
         * @throws SQLException
         */
        public static List<IRecord> queryWithConnection(Connection conn,String query) throws SQLException {
            var pstmt = conn.prepareStatement(query);// 生成SQL语句
            return preparedQuery(()->pstmt,null);
        }
        
        /**
         * 带有连接的数据查询
         * @param conn 数据库连接
         * @param query 查询语句
         * @param prepare PreparedStatement 的查询前的数据处理。比如设置 PreparedStatement 的各个参数。
         * @return 查询结果
         * @throws SQLException
         */
        public static List<IRecord> queryWithConnection(Connection conn,String query,
            Consumer<PreparedStatement> prepare) throws SQLException {
            var pstmt = conn.prepareStatement(query);// 生成SQL语句
            return preparedQuery(()->pstmt,prepare);
        }
        
        /**
         * preparedStatement 结果处理。
         * @param pstmt parepared statement 生成器
         * @return 查询的结果集合
         * @throws SQLException
         */
        public static List<IRecord> preparedQuery(Supplier<PreparedStatement> pstmt)
            throws SQLException {
            return preparedQuery(pstmt,(Consumer<PreparedStatement>)null);
        }
        
        /**
         * preparedStatement 结果处理。
         * @param pstmt parepared statement 生成器
         * @param prepare  parepared statement的参数准备
         * @return 查询的结果集合
         * @throws SQLException
         */
        public static List<IRecord> preparedQuery(Supplier<PreparedStatement> pstmt,
            Consumer<PreparedStatement> prepare) 
            throws SQLException {
            
            final List<IRecord> ll = new LinkedList<>();
            try (var stmt = pstmt.get()) {
                if(prepare!=null)prepare.accept(stmt);
                try (var rs = stmt.executeQuery()) {
                    while (rs.next())ll.add(readline(rs));
                }// try rs
            }// try stmt
            
            return ll;
        }
        
        /**
         * 执行prepared statment
         * @param conn 数据库连接
         * @return 查询的结果集合
         * @throws SQLException
         */
        public static void executeWithConnection(final Connection conn,String sql) 
            throws SQLException{
            executeWithConnection(conn,sql,(Consumer<PreparedStatement>)null);
        }
        
        /**
         * 执行prepared statment
         * @param conn 数据库连接
         * @param sql prepare sql语句的处理
         * @return 查询的结果集合
         * @throws SQLException
         */
        public static void executeWithConnection(final Connection conn,String sql,
            Consumer<PreparedStatement> prepare) 
            throws SQLException {
            final var pstmt = conn.prepareStatement(sql);
            preparedExecute(()->pstmt,prepare);
        }
        
        /**
         * 执行prepared statment
         * @param pstmt parepared statement 生成器
         * @return 无
         * @throws SQLException
         */
        public static void preparedExecute(Supplier<PreparedStatement> pstmt) 
            throws SQLException {
            preparedExecute(pstmt,(Consumer<PreparedStatement>)null,
                (BiFunction<Boolean,PreparedStatement,?>)null);
        }
        
        /**
         * 执行prepared statment
         * @param pstmt parepared statement 生成器
         * @param prepare  parepared statement的参数准备
         * @return 无
         * @throws SQLException
         */
        public static void preparedExecute(Supplier<PreparedStatement> pstmt,
            Consumer<PreparedStatement> prepare) 
            throws SQLException {
            preparedExecute(pstmt,prepare,(BiFunction<Boolean,PreparedStatement,?>)null);
        }
        
        /**
         * 执行prepared statment
         * @param pstmt parepared statement 生成器
         * @param callback 返回结果的处理
         * @return callback 处理的结果
         * @throws SQLException
         */
        public static <T> T preparedExecute(Supplier<PreparedStatement> pstmt,
                BiFunction<Boolean,PreparedStatement,T> callback) throws SQLException {
            return preparedExecute(pstmt,(Consumer<PreparedStatement>)null,callback);
        }
        
        /**
         * 执行prepared statment
         * @param pstmt parepared statement 生成器
         * @param prepare  parepared statement的参数准备
         * @param callback 返回结果的处理
         * @return callback 处理的结果
         * @throws SQLException
         */
        public static <T> T preparedExecute(Supplier<PreparedStatement> pstmt,
            Consumer<PreparedStatement> prepare,BiFunction<Boolean,PreparedStatement,T> callback) 
            throws SQLException {
            
            T t = null;// 返回结果
            try (var stmt = pstmt.get()) {
                if(prepare!=null)prepare.accept(stmt);
                if(callback!=null)t = callback.apply(stmt.execute(),stmt);
            }// try stmt
            
            return t;// 返回结果
        }
        
        /**
         * 从ResultSet 中读取一条数据。部队结果结做任何改变。
         * Record 的key 采用rs.getMetaData().getColumnLabel(索引）来获取。
         * @param rs 结果集合
         * @return 结果集数据的record的表示,出现异常则放回null
         */
        public static IRecord readline(final ResultSet rs) {
            IRecord rec = null;// 默认的非法结果
            try {
                final var rsm = rs.getMetaData();// 结果集元数据
                final var n = rsm.getColumnCount();// 列数据项目
                rec = REC();
                for(int i=0;i<n;i++) {// 读取当前行的各个字段信息。
                    final var name = rsm.getColumnLabel(i+1);
                    final var value = rs.getObject(i+1);
                    rec.add(name, value);
                }// for
            }catch(Exception e) {
                e.printStackTrace();
            }// try
            
            return rec;
        }
        
        /**
         * 从ResultSet 中读取一条数据。部队结果结做任何改变。
         * Record 的key 采用rs.getMetaData().getColumnLabel(索引）来获取。
         * @param rs 结果集合
         * @param close 读完是否关闭rs,true 表示读完后关闭rs
         * @return 结果集数据的records 集合
         * @throws SQLException
         */
        public static List<IRecord> readlines(final ResultSet rs,boolean close) throws SQLException{
            final var recs = new LinkedList<IRecord>();
            while(rs.next())recs.add(readline(rs));
            if(close)rs.close();
            return recs;
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * @param itf 访问接口
         * @return 数据库访问的代理对象
         */
        public static <T> T newInstance(final Class<T> itf) {
            
            return newInstance(itf,(SqlPatternPreprocessor)null);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * @param itf 访问接口
         * @param sqlpattern_preprocessor　 sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @return 书苦苦访问的代理对象
         */
        public static<T>  T newInstance(final Class<T> itf,
            final SqlPatternPreprocessor sqlpattern_preprocessor) {
            
            return newInstance(itf,(Map<String,String>)null,sqlpattern_preprocessor);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * @param itf 访问接口
         * @param sqlpattern_preprocessor　 sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @param jdbc_postprocessor 事后处理
         * @return 书苦苦访问的代理对象
         */
        public static<T>  T newInstance(final Class<T> itf,
            final SqlPatternPreprocessor sqlpattern_preprocessor,
            final JdbcPostProcessor<?> jdbc_postprocessor) {
            
            return newInstance(itf,(Map<String,String>)null,sqlpattern_preprocessor,jdbc_postprocessor);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * 请确保jdbcConfig　含有有效的driver,url,user,password
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @return 数据库访问的代理对象
         */
        public static <T> T newInstance(final Class<T> itf,final IRecord jdbcConfig) {
            
            return newInstance(itf,jdbcConfig,(SqlPatternPreprocessor)null);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * 请确保jdbcConfig　含有有效的driver,url,user,password
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @param sqlpattern_preprocessor　 sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @return 书苦苦访问的代理对象
         */
        public static <T> T newInstance(final Class<T> itf,final IRecord jdbcConfig,
            final SqlPatternPreprocessor sqlpattern_preprocessor) {
            
            return newInstance(itf,jdbcConfig.toStrMap(),sqlpattern_preprocessor);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * 请确保jdbcConfig　含有有效的driver,url,user,password
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @return 数据库访问的代理接口
         */
        public static <T> T newInstance(final Class<T> itf,final Map<String,String> jdbcConfig) {
            
            return newInstance(itf,jdbcConfig,(SqlPatternPreprocessor)null);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。<br>
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 <br>
         * 请确保jdbcConfig　含有有效的driver,url,user,password <br>
         * 
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @param sqlpattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @return 数据库访问的代理接口
         */
        public static <T> T newInstance(final Class<T> itf,final Map<String,String> jdbcConfig,
            final SqlPatternPreprocessor sqlpattern_preprocessor) {
            
            return newInstance(itf,jdbcConfig,sqlpattern_preprocessor,
                (SqlInterceptor<List<IRecord>>)null);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。<br>
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 <br>
         * 请确保jdbcConfig　含有有效的driver,url,user,password <br>
         * 
         * @param itf 访问接口
         * @param interceptor SqlInterceptor<List<IRecord>>:方法执行的接获函数,返回一个 List<IRecord>,如果非空，表示完成接获
         * 代理对象的执行结果就是该interceptor所接获的结果，反之就是 就会继续执行。后续的操作。
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @return 数据库访问的代理接口
         */
        public static <T> T newInstance(final Class<T> itf, final SqlInterceptor<List<IRecord>> interceptor) {
            
            return newInstance(itf,(Map<String,String>)null,(SqlPatternPreprocessor)null,
                (SqlInterceptor<List<IRecord>>)interceptor);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。<br>
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 <br>
         * 请确保jdbcConfig　含有有效的driver,url,user,password <br>
         * 
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @param interceptor SqlInterceptor<List<IRecord>>:方法执行的接获函数,返回一个 List<IRecord>,如果非空，表示完成接获
         * 代理对象的执行结果就是该interceptor所接获的结果，反之就是 就会继续执行。后续的操作。
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @return 数据库访问的代理接口
         */
        public static <T> T newInstance(final Class<T> itf,final IRecord jdbcConfig,
            final SqlInterceptor<List<IRecord>> interceptor) {
            
            Map<String,String> cfg = jdbcConfig == null?null:jdbcConfig.toStrMap();
            return newInstance(itf,cfg,(SqlPatternPreprocessor)null,
                (SqlInterceptor<List<IRecord>>)interceptor);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。<br>
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。<br> 
         * 请确保jdbcConfig　含有有效的driver,url,user,password <br>
         * 
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @param interceptor SqlInterceptor<List<IRecord>>:方法执行的接获函数,返回一个 List<IRecord>,如果非空，表示完成接获
         * 代理对象的执行结果就是该interceptor所接获的结果，反之就是 就会继续执行。后续的操作。
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @return 数据库访问的代理接口
         */
        public static <T> T newInstance(final Class<T> itf,final Map<String,String> jdbcConfig,
            final SqlInterceptor<List<IRecord>> interceptor) {
            
            return newInstance(itf,jdbcConfig,(SqlPatternPreprocessor)null,
                (SqlInterceptor<List<IRecord>>)interceptor);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * 请确保jdbcConfig　含有有效的driver,url,user,password
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @param sqlpattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * @param jdbc_postprocessor 结果的后置处理处理器：对一般标准的处理结果进行后续处理。
         * @return 数据库访问的代理接口
         */
        public static <T,U> T newInstance(final Class<T> itf,final Map<String,String> jdbcConfig,
            final SqlPatternPreprocessor sqlpattern_preprocessor,
            final JdbcPostProcessor<U> jdbc_postprocessor) {
            
            return newInstance(itf,jdbcConfig,sqlpattern_preprocessor,
                (SqlInterceptor<List<IRecord>>)null,jdbc_postprocessor);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。 <br>
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 <br>
         * 请确保jdbcConfig　含有有效的driver,url,user,password <br>
         * @param itf 访问接口 <br>
         * @param jdbcConfig jdbc的配置：driver,url,user,password <br>
         * @param sqlpattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．<br>
         * 是一个(m,a,p,j)->p 的形式 <br>
         * method(m)　方法对象,<br>
         * params(a args) 参数列表:name->value,<br>
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,<br>
         * jdbc(j) 当前连接的数据库对象．
         * 
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;<br>
         *    method(m)　方法对象,<br>
         *    params(a args) 参数列表:name->value,<br>
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,<br>
         *    jdbc(j) 当前连接的数据库对象．
         * @param <T> 接口类的类型
         * @param <U> 接口类的类型
         * @return 数据库访问的代理接口
         */
        public static <T,U> T newInstance(final Class<T> itf,
            final Map<String,String> jdbcConfig, final SqlPatternPreprocessor sqlpattern_preprocessor,
            final SqlInterceptor<List<IRecord>> sqlinterceptor) {
            
            return newInstance(itf,jdbcConfig,sqlpattern_preprocessor,
                sqlinterceptor,(JdbcPostProcessor<U>)null);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * 请确保jdbcConfig　含有有效的driver,url,user,password
         * @param itf 访问接口
         * @param jdbcConfig jdbc的配置：driver,url,user,password
         * @param sqlpattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * 是一个(m,a,p,j)->p 的形式
         * method(m)　方法对象,
         * params(a args) 参数列表:name->value,
         * sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * jdbc(j) 当前连接的数据库对象．
         * 
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;
         *    method(m)　方法对象,
         *    params(a args) 参数列表:name->value,
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         *    jdbc(j) 当前连接的数据库对象．
         * @param jdbcPostProcessor 结果的后置处理处理器：对一般标准的处理结果进行后续处理。
         * @return 数据库访问的代理接口
         */
        public static <T,U> T newInstance(final Class<T> itf,final Map<String,String> jdbcConfig,
                final SqlPatternPreprocessor sqlpattern_preprocessor,
                final SqlInterceptor<List<IRecord>> sqlinterceptor,
                final JdbcPostProcessor<U> jdbcPostProcessor) {
            
            final var jc = itf.getAnnotation(JdbcConfig.class);// 获取jdbc的配置
            final var cfg = null==jdbcConfig?new HashMap<String,String>():jdbcConfig;// 提供默认数据库配置
            T objT = null;// 接口实例对象
            try {
                final var jdbc = ( jdbcConfig==null || jdbcConfig.size()<1 ) && ( jc == null ) 
                    ? null // 没有默认配置
                    : new Jdbc(cfg.computeIfAbsent("driver",k->jc.driver()),
                        cfg.computeIfAbsent("url",k->jc.url()),
                        cfg.computeIfAbsent("user",k->jc.user()),
                        cfg.computeIfAbsent("password",k->jc.password()));
                
                if(jdbc==null)System.err.println(MFT("尚未配置Jdbc实例,无法处理 数据库连接操作,"
                    + "因为jdbc的配置:\njdbc:{0},\n@JdbcConfig:{1}！",jdbcConfig,jc));
                
                objT =  newInstance(itf,jdbc,sqlpattern_preprocessor,sqlinterceptor,jdbcPostProcessor);
            }catch(Exception e) {
                e.printStackTrace();
            }
            
            return objT;// 返回接口实例
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * @param itf 访问接口
         * @param jdbc 数据库访问接口
         * @return 数据库访问的代理接口
         */
        public static synchronized <T> T newInstance(final Class<T> itf,final Jdbc jdbc) {
            
            return newInstance(itf,jdbc,null,null);
        }
        
        /**
         * SQL 语句模板的预处理器 <br>
         * 函数接口 String handle(Method method,IRecord params,String sqlpattern,Jdbc jdbc) 对 <br>
         * 请求的 sqlpattern 进行预处理。比如读写分离，分库分表的处理等。 <br>
         * -- method　方法对象, <br>
         * -- params 参数列表:name->value, <br>
         * -- sqlpattern sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力, <br>
         * -- jdbc 当前连接的数据库对象． <br>
         * @author xuqinghua
         *
         */
        @FunctionalInterface
        public interface SqlPatternPreprocessor{
            /**
             * sql 模板的参数的预处理函数。 <br>
             * 比如可以有这样一种预处理的实现SQL变换的预处理实现，当然也可以采用 另外的自定义实现 等。  <br>
             *   可以有某个SqlPatternPreprocessor 的实现自动对读sqlpattern中的命名参数进行替换： <br>
             *   method:public User getUserByName(String name); <br>
             *   sqlpattern: select * from user where name=#name <br>
             *   params:REC("name","张三") <br>
             *   返回值:select * from user where name="张三" <br>
             *   
             *   需要注意对于sql语句模板不能以#开头。
             *   
             * @param method 方法对象
             * @param params 形参与实参之间的对应关系列表:{name/形式参数的名称->value/实际参数的数值}
             * @param sqlpattern sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力．
             * @param jdbc 当前连接的数据库对象．
             * @return 处理后的sql模板
             */
            public String handle(final Method method,final IRecord params,final String sqlpattern,final Jdbc jdbc);
        }
        
        /**
         * 命名SQL文件的解析，类似于如下的格式。需要注意的#后面必须带有空格，才可以作为 sql语句的名称<br>
         * 需要注意：‘# count1’  才是sql语句的名称，而‘#name:  用户名称’ 则是参数的说明。区别在于<br> 
         * count1与# 之间有一个 空格。而name 与#之间没有 <br>
         * 
         * -- -------------------------- <br>
         * -- # count1 <br>
         * -- name:  用户名称 <br>
         * -- --------------------------<br>
         * select count(*) from t_user <br>
         * where name = #name <br>
         *
         * -- --------------------------<br>
         * -- # count2
         * -- --------------------------<br>
         * select count(*) from t_user;<br>
         *
         * -- --------------------------<br>
         * -- # insertUser <br>
         * -- -------------------------- <br>
         * insert into ##tableName (name,password,sex,address,birth,phonenumber,email) <br>
         * values (#name,#password,#sex,#address,#birth,#phonenumber,#email) <br>
         * 
         * @param lines 行文本序列
         * @return 命名sql 集合。
         */
        public static Map<String,String> parse2namedsqls(final Stream<String>lines){
            
            return parse2namedsqls(lines,true);
        }
        
        /**
         * 命名SQL文件的解析，类似于如下的格式。需要注意的#后面必须带有空格，才可以作为 sql语句的名称 <br>
         * 需要注意：‘# count1’  才是sql语句的名称，而‘#name:  用户名称’ 则是参数的说明。区别在于 
         * count1与# 之间有一个 空格。而name 与#之间没有 <br>
         * 
         * -- -------------------------- <br>
         * -- # count1 <br>
         * -- #name: 用户名称 <br>
         * -- -------------------------- <br>
         * select count(*) from t_user <br>
         * where name = #name <br>
         *
         * -- -------------------------- <br>
         * -- # count2 <br>
         * -- -------------------------- br>
         * select count(*) from t_user; <br>
         *
         * -- -------------------------- <br>
         * -- # insertUser <br>
         * -- -------------------------- <br>
         * insert into ##tableName (name,password,sex,address,birth,phonenumber,email) <br>
         * values (#name,#password,#sex,#address,#birth,#phonenumber,#email) <br>
         * 
         * @param lines 行文本序列
         * @param remove_inline_comments 是否移除行内注释。 true:移除,否则不移除
         * @return 命名sql 集合。
         */
        public static Map<String,String> parse2namedsqls(final Stream<String>lines,
            final boolean remove_inline_comments){
            
            final var namedsqls = new HashMap<String,String>();
            final var p = Pattern.compile("^([\\s-])*#\\s+([^\\s#]+)\\s*$");// key 标记
            final var p_comment = Pattern.compile("^([\\s]*)--.*$");// key 标记
            final var keys = new LinkedList<String>();
            final var values = new LinkedList<String>();
            
            // 添加key values
            final Supplier<Map<String,String>> add2namedsqls = ()->{
                if(keys.size()>0 && values.size()>0) {
                    final var k = keys.getLast();
                    namedsqls.put(k, values.stream().collect(Collectors.joining("\n")));
                    keys.clear();
                    values.clear();
                }// keys 和 values 缓存框都给清空
                
                return namedsqls;// 命名SQL
            };
            
            // 逐行遍历
            lines.forEach(line->{
                if(line.matches("^\\s*$"))return;// 空白行过滤
                final var key_matcher = p.matcher(line);
                if(key_matcher.matches()) {
                    final var key = key_matcher.group(2);
                    add2namedsqls.get();
                    keys.add(key);
                    
                    return;
                }else {
                    if(p_comment.matcher(line).matches())return;
                    final var _line = remove_inline_comments // 是否移除行内注释
                        ? line.replaceAll("--\\s+.*$","") // 去除行内的尾部注释 ,由 --空格 引导的内容被省略掉。
                        : line;// 不移除行内注释
                    
                    values.add(_line);// 数据追加
                }//if (key_matcher.matches())
            });// lines.forEach
            
            // 添加剩余的SQL语句。
            add2namedsqls.get();
            
            // 返回结果集合
            return namedsqls;
        }
        
        /**
         * 提取参数名列表:
         * @param line 类似于 这样的的sql 模板insert into ##tableName (name,password,sex,address,birth,phonenumber,email)
         * values (#name,#password,#sex,#address,#birth,#phonenumber,#email),把
         * [#name,#password,#sex,#address,#birth,#phonenumber,#email],提取出来,注意提取后 井号被去除了。
         * 
         * @return 从模板提取出来的参数集合。
         */
        public static LinkedHashSet<String> retrieve_params(final String line) {
            
            final var placeholder = Pattern.compile("#+([a-z_][a-z0-9_]+)",
                Pattern.CASE_INSENSITIVE).matcher(line);
            final var params = new LinkedHashSet<String>();
            //提取所有位置参数
            while(placeholder.find()) params.add(placeholder.group(1));
            
            return params;
        }
        
        /**
         * namedsql_processor 对namedsqls中的‘{’进行转义：防止MessageFormater把他误认为参数。
         * @param namedsqls sql语句。 name->sql
         * @return SqlPatternPreprocessor
         */
        public static SqlPatternPreprocessor namedsql_processor_escape_brace(
            final Map<String,String> namedsqls) {
            
            return namedsql_processor(namedsqls,e->e.replace("{","'{'"));
        }
        
        /**
         * namedsql_processor 对namedsqls中的‘{’进行转义：防止MessageFormater把他误认为参数。
         * @param namedsqls sql语句。 name->sql
         * @param preprocessor sql语句 的预处理器：例如对 neo4j的转义。
         * @return SqlPatternPreprocessor
         */
        public static SqlPatternPreprocessor namedsql_processor(final Map<String,String> namedsqls,
            final Function<String,String> preprocessor) {
            
            if(namedsqls!=null)namedsqls.forEach((k,v)->{// 依次处理每个SQL
                namedsqls.compute(k,(key,value)->preprocessor.apply(value));
            });// 左括号‘{’ 转义
            
            return namedsql_processor(namedsqls);
        }
        
        /**
         * 根据代理对象结构： 提取sharpPattern 中的数据操作语句 <br>
         * @param proxy 代理对象结构：一般是通过Jdbc.newInstance 调用一个未标记的接口函数，故意造成调用失败，致使Jdbc返回返回代理对象结构。
         * proxy内部包含，连接器，sqlpattern 解析器，一级后置收尾处理器，当前的数据库连接操作对象jdbc等 代理的内部结构，可以通过IRecord。都是
         * 作为单例的模式包装在IRecord 之中。因此可以通过的IRecordfindOne(Class<?>)的方式来提取，进而被使用。
         * @param sharpPattern sharppattern 的名称。如妹没有#开头，会自动添加一个#符号。作为标记。
         * @param params 占位符变量的 值定义容器{key->value}的集合。key 就是占位符的名称会根据 sharp占位符 "#(\\w+)" 的规则，即是否以井号
         * 开头的标识符号（identifier），来 对sharpPattern所标记SQL语句（也可以不是SQL遇见的一种DSL)提取，进而做值替换。随有点绕口，但是很简单。
         * @return pattern 所对应的数据操作语句。占位符变量也已经用params 做了替换。非数字类型会被自动的添加上 双引号“"”
         */
        public static String parseSharpPattern(final IRecord proxy,final String sharpPattern,
            final IRecord params) {
            
            if(proxy==null)return null;
            final var processor = proxy.findOne(SqlPatternPreprocessor.class);
            if(processor==null) return null;
            final var tpl = processor.handle(null,null,// 把processor 视作一个Map<String,String>的namedSql
                MFT(sharpPattern.startsWith("#")?"{0}":"#{0}",sharpPattern), null);// namesql 的模板
            if(tpl==null)return null;
            final var sql = substitute(tpl,"#+(\\w+)", params,// 对模板中的参数的位置 采用 params 中的数据来给予替换。
                (pat,t)-> pat.startsWith("##")
                ? t+"" // 对于以两个#开头的变量视作强制字符类型不加引号。
                : MFT(t instanceof Number?"{0,number,0}":"\"{0}\"",t));// 根据参数类型决定是否添加引号。
            
            return sql;
        }
        
        /**
         * 其实就是提取在方法Method上的标记比如annotation:JdbcQuery,JdbcExecute中的value 值。
         * 根据方法签名提取 对含有 井号#的pattern 进行侦测，如果侦测出来就用sharppattern_todetect所表标的内容，作为键值在
         * 在sharppattern_defs进行检索，并对method上的标记
         * 
         * @param method 方法方法对象，当sharppattern为null时，用于作为生成默认的sharppattern 名。
         * @param sharppattern 待检测的位于JdbcQuery中的以井号#开头的字符串 ,null 或是空白 表示 是# "#"+方法名称。
         * 例如对于方法  getName,sharppattern 为null,sharppattern则被视作#getName
         * @param sharppattern_defs 命名sharp pattern 的定义
         * @return 识别出来的 sharp pattern
         */
        public static String parseJdbcMethodSharpPattern(final Method method,final String sharppattern,
            final Map<String,String> sharppattern_defs) {
            final var sqlpattern= (sharppattern==null||sharppattern.matches("\\s*"))
                ?    "#"+method.getName()
                :    sharppattern;// 默认的sqlpattern为方法名
            final var sharp_matcher = Pattern.compile("#+([a-z_][a-z0-9_]+)",Pattern.CASE_INSENSITIVE)
                .matcher(sqlpattern);// namedsql 是一个用＃号作为前缀的名称
            if(!sharp_matcher.matches())return sqlpattern;// 非namedsql
            
            final var namedSqlpattern = sharppattern_defs.get(sharp_matcher.group(1));// 提取sqlpattern
            if(namedSqlpattern==null) {// namedsqls 中无法对应
                System.out.println(MFT("in {0} 无法对应到:{1}",method==null?"\"方法缺失\"":method.getName(),sqlpattern));
                return sqlpattern;// 
            }// if
            
            return namedSqlpattern;
        }
        
        /**
         * 生成一个命名sql(namedsql)的预处理器。 <br>
         * 对与双#号的参数不予进行替换 类型转换。 <br>
         * 
         * 一个典型使用 namedsql进行创建数据库接机口的案例是如下情形：<br>
         * 定义接口 <br>
         * interface UserDatabase { <br>
         * @JdbcExecute("#createTable") <br>
         * public void createTable(String tableName); <br>
         * }<br>
         * //提取命名SQL的集合。 <br>
         * var namedsqls = parse2namedsqls(utf8lines(new File(path("user.sql",UserModel.class)))); <br>
         * //生成接口的代理实例，并注入SqlPatternPreprocessor: 即namedsql_processor(namedsqls)的函数调用。 <br>
         * var database = Jdbc.newInstance(UserDatabase.class,jdbcConfig,namedsql_processor(namedsqls));<br>
         * //使用代理实例进行数据库操作。<br>
         * database.createTable()//  <br>
         * 
         * #createTable 是 user.sql 中的一条创建表的SQL语句。 <br>
         * 
         * 把如下的 命名sql 通常是来自于一个SQL文件。 <br>
         * -- -------------------------- <br>
         * -- # insertUser <br>
         * -- -------------------------- <br>
         * insert into ##tableName (name,password,sex,address,birth,phonenumber,email) <br>
         * values (#name,#password,#sex,#address,#birth,#phonenumber,#email) <br>
         *
         * 根据接口 <br>
         * @JdbcExecute({"#insertUser"}) <br>
         * public void  insertUser(String tableName,String name,String password,String sex,String address, <br>
         *  Date birth,String phonenumber,String email); <br>
         *
         * 给替换成 如下的式样 <br>
         * insert into {0} (name,password,sex,address,birth,phonenumber,email) <br>
         * values (''{1}'',''{2}'',''{3}'',''{4}'',''{5,date,yyyy-MM-dd HH:mm:ss}'',''{6}'',''{7}'') <br>
         * 
         * @param namedsqls 命名sql集合：{#key1->sql1,#key1->sql2,...}, 
         * @return 变换后的sqlapttern 可以被 MessageFormat处理的SQL语句。
         */
        public static SqlPatternPreprocessor namedsql_processor(final Map<String,String> namedsqls) {
            
            return (Method method,IRecord params,String sqlpattern,Jdbc jdbc)->{
                // 自动侦测SQL Pattern, 或者mehtod sharp pattern
                final var namedSqlpattern = parseJdbcMethodSharpPattern (method,sqlpattern,namedsqls);
                final var dblsharp_pattern = Pattern.compile("#{2}([a-z_][a-zA-z0-9_]+)",
                    Pattern.CASE_INSENSITIVE);// 双井号的侦测工具
                var line = namedSqlpattern;// 对namedSqlpattern 进行数据处理。
                if(method==null) return params!=null // 根据参数数据的不同进行数据变换
                    ? Jdbc.quote_substitute(line, "#+(\\w+)", params) // 采用参数sharpPattern进行数据替换
                    : line; // 不予进行方法参数回填。
                final var ai = new AtomicInteger(0);//计数变量
                final var aa = method.getParameters();// 方法的参数集合。
                for(String key:params.keys()) {// 遍历方法参数。
                    final var i = ai.getAndIncrement();// 获取当前的参数位置
                    final var type = aa[i].getType();// 获取参数类型
                    var t = "''{"+i+"}''";// 占位符的式样
                    if(Number.class.isAssignableFrom(type)) t="{"+i+",number,#}";// 数值类型
                    else if(Date.class.equals(type)) t= "''{"+i+",date,yyyy-MM-dd HH:mm:ss}''";// 日期类型。
                    else if(dblsharp_pattern.matcher(line).find()) t= "{"+i+"}";// 双引用不加引号。
                    // 更新 sql模板
                    line = line.replaceAll(MFT("#+{0}",key),t);
                }// for
                
                // System.out.println(line);
                return line;// 返回处理后的sql 语句模板。
            };//(Method method,IRecord params,String sqlpattern,Jdbc jdbc)->
        };
        
        /**
         * SQL 的拦截器
         * 函数接口 String handle(Method method,IRecord params,String sqlpattern,Jdbc jdbc) 对
         * 请求的 sqlpattern 进行预处理。比如读写分离，分库分表的处理等。
         * -- method　方法对象,
         * -- params 参数列表:name->value,
         * -- sqlpattern sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         * -- jdbc 当前连接的数据库对象．
         * @author xuqinghua
         *
         * @param <T> 返回结果的类型
         */
        @FunctionalInterface
        public interface SqlInterceptor<T>{
            /**
             * 拦截器：如果结果返回null则给予放行,否则方法给与放行
             * @param method　方法对象
             * @param params 参数列表:name->value
             * @param sqlpattern sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力．
             * @param jdbc 当前连接的数据库对象．
             * @return　处理后的sql模板 
             */
            T intercept(final Method method,final IRecord params,final String sqlpattern,final Jdbc jdbc);
        }
        
        /**
        * jdbc 的事后处理。一般做一些收尾的拦截与
         * @author xuqinghua
         * @param <T> 返回结果的类型
         */
        @FunctionalInterface
        public interface JdbcPostProcessor<T>{
            /**
             * 后续处理的方法
             * @param result jdbc 的处理结果
             * @return
             * <T> 先前的Jdbc的处理结果。这个类型是根据 接口返回类型而侦测出来的，一般不需要给予更改。
             */
            Object process(final Method method,final IRecord params,final Jdbc jdbc,final T result);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。 
         * @param itf 访问接口
         * @param jdbc 数据库访问接口
         * @param sqlpattern_preprocessor　 sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         *  是一个(m,a,p,j)->p 的形式
         *    method(m)　方法对象,
         *    params(a args) 参数列表:name->value,
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         *    jdbc(j) 当前连接的数据库对象．
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;
         *     method(m)　方法对象,
         *    params(a args) 参数列表:name->value,
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         *    jdbc(j) 当前连接的数据库对象．
         * @return 数据库访问的代理接口
         */
        public static synchronized <T> T newInstance(final Class<T> itf,final Jdbc jdbc,
            final SqlPatternPreprocessor sqlpattern_preprocessor,
            final SqlInterceptor<List<IRecord>> sqlinterceptor) {
            
            return newInstance(itf,jdbc,sqlpattern_preprocessor,
                sqlinterceptor,(JdbcPostProcessor<?> )null);
        }
        
        /**
         * 根据JDBC的上下文构建一个JDBC的执行环境。<br>
         * 创建一个接口itf的实现bean 根据注解信息来给与实现。<br> 
         * @param itf 访问接口 <br>
         * @param jdbc 数据库访问接口 <br>
         * @param sqlpattern_preprocessor　 sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．<br>
         *  是一个(m,a,p,j)->p 的形式 <br>
         *    method(m)　方法对象, <br>
         *    params(a args) 参数列表:name->value, <br>
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,<br>
         *    jdbc(j) 当前连接的数据库对象．<br>
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。<br>
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;<br>
         *     method(m)　方法对象,<br>
         *    params(a args) 参数列表:name->value,<br>
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,<br>
         *    jdbc(j) 当前连接的数据库对象．<br>
         * @param jdbc_postprocessor 结果的后置处理处理器：对一般标准的处理结果进行后续处理。<br>
         * @return 数据库访问的代理接口
         */
        @SuppressWarnings("unchecked")
        public static synchronized <T,U> T newInstance(final Class<T> itf,final Jdbc jdbc,
            final SqlPatternPreprocessor sqlpattern_preprocessor, // sql 语句模板的处理
            final SqlInterceptor<List<IRecord>> sqlinterceptor,// jdbc的sql 执行拦截
            final JdbcPostProcessor<U> jdbc_postprocessor) { // jdbc_postprocessor 的jdbc处理结果最终递交。
            
            // 定义代码拦截器
            final SqlInterceptor<List<IRecord>> interceptor = // 方法的前置拦截器
                sqlinterceptor == null?(m,a,s,j)->null:sqlinterceptor;// 定义拦截器
            final JdbcPostProcessor<U> postprocessor = // jdbc 处理结果的最终 称帝的处理包装结果。
                jdbc_postprocessor==null?(m,a,j,r)->r:jdbc_postprocessor;// 定义后置处理器:默认不做处理e->e
            // 创建数据库操作的代理对象。
            final var t = (T)Proxy.newProxyInstance(itf.getClassLoader(),new Class<?>[] {itf},(proxy,method,args)->{
                /*对这里的程序设计做一点说明：考虑到各种语句Annotation的处理逻辑相似，这里我没有把他们分装成函数而是 统一的卸载一个函数里了
                 * 好处就是变量的共享非常方便：坏处就是 出现了多出口（postprocessor 出现的地方）。造成程序的理解混乱，但是由于注解不多，这种缺点还是
                 * 可以忍受的。所以就采用了这种混编程。*/
                
                
                ///////////////////////////////////////////////////////////////////////
                // 数据注入
                // 注入jdbc对象等环境对象．就像SpringMVC的请求参数的注入
                ///////////////////////////////////////////////////////////////////////
                final BiConsumer<Object[],Map<Class<?>,Object>> inject_default_values=(oo,mm)->{
                    if(oo!=null&&oo.length>0) {
                        final var tt = method.getParameterTypes();// 提取方法的参数列表
                        mm.forEach((cls,v)->{for(int i=0;i<tt.length;i++) 
                            if((tt[i] ==cls) && oo[i]==null)oo[i]=v;}// 根据参数类型进行注入。该位置的参数为null的时候给予注入。
                        );// foreach
                    }// oo!=null&&oo.length>0
                };// inject
                final var default_values = new HashMap<Class<?>,Object>();// Jdbc代理提供的默认参数列表。
                default_values.put(Jdbc.class, jdbc);// Jdbc的默认值
                
                
                ///////////////////////////////////////////////////////////////////////
                // 方法注释解析：JdbcQuery
                ///////////////////////////////////////////////////////////////////////
                //sql查询的执行
                final JdbcQuery[] jcqs = method.getAnnotationsByType(JdbcQuery.class);
                final SqlPatternPreprocessor pattern_preprocessor=sqlpattern_preprocessor==null
                    ? (m,a,p,j)->p:sqlpattern_preprocessor;// sql pattern 预处理。
                if(jcqs!=null && jcqs.length>0) {// 查询优先
                    final var o = handleJdbcQuery(jdbc,jcqs, method, args,pattern_preprocessor,interceptor);// 查询优先
                    // 默认方法的处理
                    if(isDefaultMethod(method)&& args.length>0 && args[args.length-1]==null) {
                        final Object oo[] = args.clone(); oo[oo.length-1] = o; //把查询结果作为参数列表的最后一位给予传递
                        inject_default_values.accept(oo,default_values);// 注入默认值
                        return postprocessor.process( method,params(method,args),jdbc,(U) // 结果呈递
                            invokeDefaultMethod(proxy,method,oo));
                    }// if isDefaultMethod
                    return postprocessor.process( method,params(method,args),jdbc,(U) // 结果呈递
                        o );
                }// if jcqs!=null
                
                
                ///////////////////////////////////////////////////////////////////////
                // 方法注释解析：JdbcPreparedQuery
                ///////////////////////////////////////////////////////////////////////
                //sql查询的执行:第二种jcq 这也是为何有个2的原因。s结尾表示这里复数的意思。
                final JdbcPreparedQuery[] jcq2s = method.getAnnotationsByType(JdbcPreparedQuery.class);
                // 这一段代码是一种示例：用于演示每个annotaion 都可以自定义 SqlPatternPreprocessor,其实与采用pattern_preprocessor效果一样
                final SqlPatternPreprocessor pattern_preprocessor2=sqlpattern_preprocessor==null
                    ? (m,a,p,j)->p:sqlpattern_preprocessor;// 演示使用一个专用的 pattern_preprocessor2 
                if(jcq2s!=null && jcq2s.length>0) {// 查询优先
                    final var o = handleJdbcPreparedQuery(jdbc,jcq2s, method, args,pattern_preprocessor2,
                        interceptor);// 查询优先
                    // 默认方法的处理
                    if(isDefaultMethod(method)&& args.length>0 && args[args.length-1]==null) {
                        final Object oo[] = args.clone(); oo[oo.length-1] = o; //把查询结果作为参数列表的最后一位给予传递
                        inject_default_values.accept(oo,default_values);// 注入默认值
                        return postprocessor.process( method,params(method,args),jdbc,(U) // 结果呈递
                            invokeDefaultMethod(proxy,method,oo));
                    }// if isDefaultMethod
                    return postprocessor.process( method,params(method,args),jdbc,(U) // 结果呈递
                        o );
                }// if jcqs!=null
                
                
                ///////////////////////////////////////////////////////////////////////
                // 方法注释解析：JdbcExecute
                ///////////////////////////////////////////////////////////////////////
                // SQL语句的执行:JdbcExecute
                final JdbcExecute[] jces = method.getAnnotationsByType(JdbcExecute.class);
                synchronized (jdbc){//保持执行操作同步运行．
                    if(jces!=null && jces.length>0)return
                        postprocessor.process( method,params(method,args),jdbc,(U) // 结果呈递
                            handleJdbcExecute(jdbc,jces, method, args,pattern_preprocessor,interceptor));
                }// synchronized (jdbc)
                
                
                ///////////////////////////////////////////////////////////////////////
                // 方法注释解析：PreparedExecute
                ///////////////////////////////////////////////////////////////////////                
                // SQL语句的执行:PreparedExecute
                final JdbcPreparedExecute[] jce2s = method.getAnnotationsByType(JdbcPreparedExecute.class);
                synchronized (jdbc){//保持执行操作同步运行．
                    if(jce2s!=null && jce2s.length>0)
                        return postprocessor.process( method,params(method,args),jdbc,(U) // 结果呈递
                            handlePreparedExecute(jdbc,jce2s,method,args,pattern_preprocessor,interceptor));
                }// synchronized (jdbc)

                
                ///////////////////////////////////////////////////////////////////////
                // 方法注释解析：默认方法  -没有注释但是默认方法
                ///////////////////////////////////////////////////////////////////////    
                // 如果以上的注解都没有处理成功，尝试执行函数的默认方法：默认函数的处理
                if(isDefaultMethod(method))
                    return postprocessor.process( method,params(method,args),jdbc,(U) // 结果呈递
                        Jdbc.handleDefaultMethod(proxy, method, args, jdbc));
                
                
                ///////////////////////////////////////////////////////////////////////
                // 方法注释解析：Jdbc所不能理解的方法
                ///////////////////////////////////////////////////////////////////////
                final var params = params(method,args);
                final var message = MFT(
                    "方法：{0},参数:{1}，超出了Jdbc所能理解的范围，代理失败。但是这个方法会把代理的对象的结构信息也给返回出去，\n"
                    + "可以作为访问:sqlinterceptor，sqlpattern_preprocessor，jdbc_postprocessorjdbc等对象的一个入口。",
                    method.getName(),params);
                
                U u = null;// 默认的返回值
                // 仅当方法的返回值类型为IRecord 系列的时候才提供全面代理结构的信息作为返回值。
                if(method.getReturnType().isAssignableFrom(IRecord.class)) {
                    try {
                        u = (U)(Object)REC(// 返回结果结构
                            "success",false,// 标记执行失败
                            "message",message,// 提示消息内容
                            "params",params,// 调用的参数
                            "method",method,// 执行失败的额方法
                            "sqlinterceptor",sqlinterceptor,
                            "sqlpattern_preprocessor",sqlpattern_preprocessor,
                            "jdbc_postprocessor",jdbc_postprocessor,
                            "jdbc",jdbc //jdbc 的连接对象
                        );// 返回当前的执行环境。;
                    }catch(Exception e) {
                        e.printStackTrace();
                    }// try
                } else {// if(method.getReturnType().isAssignableFrom(IRecord.class)) 
                    System.err.println("返回类型非:IRecord，不予提供 代理详情结构信息：\n"+message);
                }//  if(method.getReturnType().isAssignableFrom(IRecord.class))
                
                // 以上均处理不了，折返回null 作为处理失败的结果。
                // 方法的计算结果，与 下面的 return t 是不一样的，return t 是代理对象，而这里是方法的运算结果。
                return postprocessor.process( method,params,jdbc,(U) u);
                
            });// newProxyInstance
            
            return t;// 返回T类型的代理对象。
        }
        
        /**
         * 处理默认的构造方法<br>
         * 注意invoke 与  handleDefaultMethod的区别是 handleDefaultMethod会 左接口的参数方法注入。<br>
         * 而 invokeDefaultMethod 则不会。 所 handleDefaultMethod 需要传递Jdbc 参数<br>
         * 
         * @param proxy　代理对象
         * @param method　方法对象
         * @param args　方法参数对象数组
         * @param jdbc　jdbc对象
         * @return 默认方法的执行结果
         */
        public static Object handleDefaultMethod(final Object proxy,final Method method,
            final Object[]args,final Jdbc jdbc) {
            
            if(jdbc==null)return null;
            
            // 数据注入,注入jdbc对象等环境对象．
            final BiConsumer<Object[],Map<Class<?>,Object>> inject=(oo,mm)->{
                if(oo!=null&&oo.length>0) {
                    var tt = method.getParameterTypes();
                    mm.forEach((cls,v)->{for(int i=0;i<tt.length;i++) 
                        if((tt[i] ==cls) && oo[i]==null)oo[i]=v;}
                    );// foreach
            }};// inject
            
            final var mm = new HashMap<Class<?>,Object>();mm.put(Jdbc.class, jdbc);
            inject.accept(args,mm);
            final boolean isTransaction = Arrays.stream(method.getParameterTypes())
                .filter(e->e==IJdbcSession.class).findAny().isPresent();// IJdbcSession对象作为是否为事务操作的标记。
            // 封装trycatch,trycach 的编码真的很占地方：哈哈啥
            final Supplier<Object> sp = () -> {
                Object obj = null;// 返回值
                try {obj = invokeDefaultMethod(proxy,method,args);}// 调用默认处理
                catch(Throwable e) {e.printStackTrace();}
                return obj;// 返回默认值的处理结果
            };// sp 结果生成器
            
            // 返回结果
            return isTransaction // 是否存在事务处理
            ? jdbc.withTransaction( sess -> { // 执行事务
                    mm.put( IJdbcSession.class, sess );// 准备sess会话对象
                    inject.accept(args,mm);// 把sess事务对象注入到参数上下文中
                    //System.out.println(Arrays.asList(args));
                    invokeDefaultMethod(proxy,method,args); 
              }) // withTransaction
            : sp.get(); // 非事务操作
        }
        
        /**
         * 调用接口默认方法：<br>
         * 注意invoke 与  handleDefaultMethod的区别是 handleDefaultMethod会 左接口的参数方法注入。<br>
         * 而 invokeDefaultMethod 则不会。 <br>
         * 
         * @param proxy　代理对象
         * @param method　方法名
         * @param args　参数名
         * @return　调用默认方法．
         * @throws Throwable
         */
        private static Object invokeDefaultMethod(final Object proxy, 
            final Method method, final Object[] args)
            throws Throwable {
            
            final var declaringClass = method.getDeclaringClass();
            final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, 
                MethodHandles.lookup());// 确定似有方法寻找工具
            return lookup.unreflectSpecial(method,declaringClass)
                .bindTo(proxy)//　防止对proxy的第归调用．
                .invokeWithArguments(args);
        }
        
        /**
         * 判断是否是默认方法<br>
         * 
         * @param method　方法对象
         * @return true default method 否则 非默认函数
         */
          private static boolean isDefaultMethod(final Method method) {
              
              return ((method.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC |
                    Modifier.STATIC)) == Modifier.PUBLIC) && method.getDeclaringClass().isInterface();
         }
          
          /**
           * 装配方法参数:形参数->实参数<br>
           * 
           * @param method 方法对象
           * @param args　实际参数对象集合
           * @return 参数的key-vlaue 集合
           */
          private static IRecord params(final Method method, final Object[] args) {
              
                final var pp = method.getParameters();
                final var map = new LinkedHashMap<String,Object>();// 命名参数:需要注意这里采用LinkedHashMap以保持原来参数的顺序。
                for(int i=0;i<pp.length;i++) map.put(pp[i].getName(),args[i]);
                
                return new LinkedRecord(map);
          }
          
        /**
         * 执行的处理：
         * 
         * 对于一个换行位含有;sql会自动尽心拆分即：<br>
         * 对于一个 这样的sqlpattern：<br>
         * drop table if exists t_aaa;<br>
         * create table t_aaa(id int)<br>
         * 会视作两条sql 语句<br>
         * 
         * @param jdbc jdbc对象
         * @param jces jdbc的执行语句集合
         * @param method　调用方法
         * @param args 调用参数
         * @param pattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．<br>
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。<br>
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;<br>
         *    method(m)　方法对象,<br>
         *    params(a args) 参数列表:name->value,<br>
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,<br>
         *    jdbc(j) 当前连接的数据库对象．
         * @return 执行结果
         */
        private static Object handleJdbcExecute(final Jdbc jdbc,final JdbcExecute[] jces,
            final Method method,final Object[]args, final SqlPatternPreprocessor pattern_preprocessor,
            final SqlInterceptor<List<IRecord>> sqlinterceptor) {
            //非法的执行SQL语句则直接返回
            if(jces==null || jces.length<1) return null;
            
            final var _patterns = jces[0].value();// sql语句模板数组
            final String[] patterns = (_patterns==null || _patterns.length<1) 
                ? new String[]{null}:_patterns; //  没有传入sqlpattern 代表一个采用默认 pattern 需要用pattern_preprocessor 来解析。
            final var pargs =  params(method,args);//构造参数对象
            return jdbc.withTransaction(sess->{// 开启事务管理
                for(var pattern:patterns) { // sql语句模板 :当pattern 为null的时候，pattern_preprocessor 会为其左方法签名的解释。不过需要namedsql_processor配置。
                    final var dd = sqlinterceptor.intercept(method,pargs ,pattern, jdbc);
                    if(dd!=null)continue;// 非空表示拦截
                    // 提取SQL语句模板,会自动为 null的pattern 提供方法签名的解释
                    final var sqlPattern = pattern_preprocessor.handle(method,pargs, pattern,jdbc);
                    final var sqlLines = MessageFormat.format(sqlPattern,args);// 提取SQL语句,并添加参数
                    if(sqlLines==null) {// 方法的SQL语句模板解释失败
                        throw new Exception(MFT(
                            "无法为方法{0}解析出正确的SQL语句，请确保为Jdbc对象安装了正确的pattern_preprocessor!",
                            method.getName())) ;
                    }// lines
                    final String[] sqls = sqlLines.split(";\\s*\n+");// 尝试对sqls 进行多语句解析。位于行末的分号给予分解
                    for(final var sql:sqls){// 依次执行SQL语句
                        if(sql.matches("\\s*"))continue;
                        if(debug)System.out.println("jdbc:handleJdbcQuery:"+sql);
                        sess.sql2execute2int(sql);
                    }// for sql:sqls
                }//for var pattern:patterns
            });// withTransaction
        }
        
        /**
         * 
         * 对于一个换行位含有;sql会自动尽心拆分即：<br>
         * 对于一个 这样的sqlpattern：<br>
         * drop table if exists t_aaa; <br>
         * create table t_aaa(id int) <br>
         * 会视作两条sql 语句 <br>
         * 
         * sqlpattern 和sqltpl 的区别就是<br>
         * sqlpattern 是{0},{1},等 MessageFormat 格式的模板，用于匹配 参数 <br>
         * sqltpl 是sqlpattern被解析后的结果，其中的参数是 ?的占位符，用于对PreparedStatement的处理。 <br>
         * 执行的处理<br>
         * 
         * @param jdbc jdbc对象
         * @param jces jdbc的执行
         * @param method　调用方法
         * @param args 调用参数
         * @param pattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;
         *    method(m)　方法对象,
         *    params(a args) 参数列表:name->value,
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,
         *    jdbc(j) 当前连接的数据库对象．
         * @return 执行结果
         */
        private static Object handlePreparedExecute(final Jdbc jdbc,final JdbcPreparedExecute[] jces,
            final Method method, final Object[]args, final SqlPatternPreprocessor pattern_preprocessor, 
            final SqlInterceptor<List<IRecord>> sqlinterceptor) {
            
            if(jces==null || jces.length<1)return null;
            
            final var _patterns = jces[0].value();// sql语句模板数组
            final String[] patterns = (_patterns==null || _patterns.length<1) 
                ? new String[]{null}:_patterns; //  没有传入sqlpattern 代表一个采用默认 pattern 需要用pattern_preprocessor 来解析。
            final var pargs =  params(method,args);//构造参数对象
            return jdbc.withConnection(conn->{// 开启事务管理
                for(var pattern:patterns) {// sql语句模板 :当pattern 为null的时候，pattern_preprocessor 会为其左方法签名的解释。不过需要namedsql_processor配置。
                    final var dd = sqlinterceptor.intercept(method,pargs,pattern, jdbc);
                    if(dd!=null)continue;// 非空表示拦截
                    //// 提取SQL语句模板,会自动为 null的pattern 提供方法签名的解释
                    final var patternLines = pattern_preprocessor.handle(method,pargs,pattern,jdbc);
                    if(patternLines==null) {// 方法的SQL语句模板解释失败
                        throw new Exception(MFT(
                            "无法为方法{0}解析出正确的SQL语句，请确保为Jdbc对象安装了正确的pattern_preprocessor!",
                            method.getName())) ;
                    }// lines
                    final String[] tpl_patterns = patternLines.split(";\\s*\n+");// 尝试对sqls 进行多语句解析，位于行末的分号给予分解
                    for(final var tpl_pattern:tpl_patterns){// 依次执行SQL语句
                        if(tpl_pattern.matches("\\s*"))continue;
                        final var sqltpl = MessageFormat.format(tpl_pattern,args);// 提取SQL语句
                        if(debug)System.out.println("jdbc:handleJdbcQuery:"+sqltpl);
                        final var pstmt=pudt_stmt(conn, sqltpl);
                        jdbc.pstmt_execute_throws(pstmt,sqltpl.contains("?")? args:null, true);
                    }// for sql:sqls
                }//for var pattern:patterns
                
                return true;
            });// withTransaction
        }
        
        /**
         * 查询的处理：通用查询，由具体俄查询分析工
         * @param jdbc jdbc对象
         * @param queryer 具体的查询器：handleJdbcQuery，handleJdbcPreparedQuery
         * @param method 调用方法
         * @param args 调用参数
         * @return SQL 查询结果
         */
        private static Object handleGenericQuery(final Jdbc jdbc, final Supplier<List<IRecord>> queryer,
            final Method method,final Object[]args){
            
            final Class<?> retCls = method.getReturnType();// 返回类型
            //System.out.println(retCls.getName());
            final List<IRecord> recs = queryer.get();// 数据查询
            
            // 集合类型数据处理：判断结果的依旧就返回值是Collection 则为集合，否则就是单值
            if(Collection.class.isAssignableFrom(retCls)) {// 集合类型数据处理：是否单值
                return recs;
            }else {//单个值：则尝试进行左类型转换。
                final IRecord rec = recs!=null && recs.size()>0? recs.get(0):null;// 不存在则返回null
                return IRecord.rec2obj(rec,retCls);// 把单个record 转传承目标类型。
          }//if
       }
        
        /**
         * 查询的处理
         * @param jdbc jdbc对象
         * @param jcqs jdbc的查询
         * @param method 调用方法
         * @param args 调用参数
         * @param pattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。<br>
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;<br>
         *    method(m)　方法对象,<br>
         *    params(a args) 参数列表:name->value,<br>
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,<br>
         *    jdbc(j) 当前连接的数据库对象．<br>
         * @return SQL 查询结果
         */
        private static Object handleJdbcQuery(final Jdbc jdbc, final JdbcQuery[] jcqs, 
            final Method method, final Object[]args, final SqlPatternPreprocessor pattern_preprocessor,
            final SqlInterceptor<List<IRecord>> sqlinterceptor){
            
            //非法参数直接返回
            if(jcqs==null || jcqs.length<1) return null;
            
            //通用jdbc 查询
            return Jdbc.handleGenericQuery(jdbc, ()->{
                final var params = params(method,args);
                final var oo = sqlinterceptor.intercept(method, params, jcqs[0].value(), jdbc);
                if(oo!=null)return (List<IRecord>) oo;// 方法截取
                
                final var pattern=pattern_preprocessor.handle(method, 
                    params(method,args),jcqs[0].value(),jdbc);// 模式处理
                final var sql = MessageFormat.format(pattern,args);// SQL语句组装
                if(debug)System.out.println("jdbc:handleJdbcQuery:"+sql);// 调试信息
                final var recs = jdbc.sql2records(sql);// 执行SQL查询
                
                // 结果返回
                return recs;
            },method, args);// handleGenericQuery
       }
        
        /**
         * 查询的处理<br>
         * 
         * @param jdbc jdbc对象
         * @param jcq2s jdbc的查询
         * @param method 调用方法
         * @param args 调用参数
         * @param pattern_preprocessor sql语句pattern预处理：比如对于特征占位符个与替换，特别是datasharding的分数据分表．<br>
         * @param sqlinterceptor SQL方法执行拦截器：需要注意 sqlinterceptor 是在 sqlpattern_preprocessor 处理之后才给与调用的。<br>
         *    也就是说先调用sqlpattern_preprocessor，然后在调用sqlinterceptor;<br>
         *    method(m)　方法对象,<br>
         *    params(a args) 参数列表:name->value,<br>
         *    sqlpattern(p pattern) sql模板 ，sql 模板通称会携带模板参数，以完成分数据库，分表的能力,<br>
         *    jdbc(j) 当前连接的数据库对象．<br>
         * @return SQL 查询结果
         */
        private static Object handleJdbcPreparedQuery(final Jdbc jdbc,final JdbcPreparedQuery[] jcq2s,
            final Method method,final Object[]args, final SqlPatternPreprocessor pattern_preprocessor,
            final SqlInterceptor<List<IRecord>> sqlinterceptor){
            
                //条件检测
                if(jcq2s==null || jcq2s.length<1) return null;
                
                //通用查询
                return Jdbc.handleGenericQuery(jdbc, ()->{
                    final var params = params(method,args);
                    final var oo = sqlinterceptor.intercept(method, params, jcq2s[0].value(), jdbc);
                    if(oo!=null)return oo;// 方法截取
                
                    final var preparedSqlpattern =pattern_preprocessor.handle(method,params,
                        jcq2s[0].value(),jdbc);
                    final var preparedSql = MessageFormat.format(preparedSqlpattern,args);// SQL语句组装
                    if(debug)System.out.println("jdbc:handleJdbcQuery2:preparedsql"+preparedSql);
                    final var recs = jdbc.precords(preparedSql, args);// prepared SQL 执行查询
                    
                    // 检索结果返回
                    return recs;
                },method, args);// handleGenericQuery
        }
        
        /**
         * 值替换的时候 自动为值添加商英文双引号“"”，但是对于以double sharp 开头的pat 不予添加引号。<br>
         * 用params中的值替换line中的各种pattern的内容。<br>
         * 
         * 示例：quote_substitute("添加引号:#name,不添加引号 :##name", "#+(\\w+)",REC("name","肥大细胞"))返回 <br>
         * 添加引号:"肥大细胞",不添加引号 :肥大细胞 <br>
         * 
         * @param line 待解析的行数据
         * @param pattern 变量名的的pattern
         * @param params 变量的定义：{name->value}
         * @return 解析后的结果
         */
        public static String quote_substitute(final String line,final String pattern,
            final IRecord params) {
            
            return substitute(line, Pattern.compile(pattern), params,
                 (pat,e)->!pat.startsWith("##")?MFT("\"{0}\"",e+""):e+""
            );
        }
        
        /**
         * 值替换的时候 自动为值添加商英文双引号“"”，但是对于以double sharp 开头的pat 不予添加引号。
         * 用params中的值替换line中的各种pattern的内容。 但是对于以double sharp 开头的pat 不予添加引号。
         * 
         * 示例：quote_substitute("添加引号:#name,不添加引号 :##name", "#+(\\w+)",REC("name","肥大细胞"))返回
         * 添加引号:"肥大细胞",不添加引号 :肥大细胞
         * 
         * @param line 待解析的行数据
         * @param pattern 变量名的的pattern
         * @param params 变量的定义：{name->value}
         * @return 解析后的结果
         */
        public static String quote_substitute(final String line,final Pattern pattern,
            final IRecord params) {
            
            return substitute(line, pattern, params,
                (pat,e)->!pat.startsWith("##")?MFT("\"{0}\"",e+""):e+""
            );
        }
        
        /**
         * 简单替换 <br>
         * 用params中的值替换line中的各种pattern的内容。不会添加任何 辅助标记，比如引号。<br>
         * 
         * @param line 待解析的行数据
         * @param pattern 变量名的的pattern
         * @param params 变量的定义：{name->value}
         * @return 解析后的结果
         */
        public static String substitute(final String line,final String pattern,
            final IRecord params) {
            
            return substitute(line, Pattern.compile(pattern), params,  (pat,e)->e+"");
        }
        
        /**
         * 简单替换
         * 用params中的值替换line中的各种pattern的内容。不会添加任何 辅助标记，比如引号。
         * @param line 待解析的行数据
         * @param pattern 变量名的的pattern
         * @param params 变量的定义：{name->value}
         * @return 解析后的结果
         */
        public static String substitute(final String line,final Pattern pattern,
            final IRecord params) {
            
            return substitute(line, pattern, params, (pat,e)->e+"");
        }
        
        /**
         * 模式替换：带有回调函数，可以进行替换值的自定义。
         * 
         * 示例：
         * var t = Jdbc.substitute("hello #your_name i am #my_name","#(\\w+)",<br>
         * REC("your_name","张三","my_name","李四"),(e)->e+"!");<br>
         * 返回:hello 张三! i am 李四!<br>
         * 
         * 用params中的值替换line中的各种pattern的内容。<br>
         * 
         * @param line 待解析的行数据
         * @param pattern 变量名的的pattern
         * @param params 变量的定义：{name->value}
         * @param callback 二元函数：使用params中的值替换pattern结构的变量的时候，对值的操作函数。把对象转换成字符串的方法。<br>
         *   (pat,value)->String, pat 是匹配的模式的字符串,value 是在params 与 pat 中的key 为 pat的
         *   group(1)的数据值。
         * @return 解析后的结果
         */
        public static <T> String substitute(final String line,final String pattern,
            final IRecord params,BiFunction<String,T,String>callback) {
            return Jdbc.substitute(line, Pattern.compile(pattern), params, callback);
        }
        
        /**
         * 模式替换：带有回调函数，可以进行替换值的自定义。<br>
         * 
         * 用params中的值替换line中的各种pattern的内容。<br>
         * 
         * 例如：
         * 一段 line: hello #your_name i am #my_name 的一段文字。<br>
         * 使用 pattern: "#(\\w+)" 在 REC("your_name","张三","my_name","李四")进行替换, <br>
         * callback :e->e+"!"<br>
         * 就会返回：<br>
         * hello 张三！i am 李四！<br>
         * 
         * @param line 待解析的行数据
         * @param pattern 变量名的的pattern,必须在pattern中使用分组标识出：pattern 标识的变量名称！ 否则就会之际返回
         * @param params 变量的定义：{name->value}, 对于满足line中的pattern,但是尚未再params找到对应值的数据，将其给予再line中删除，即替换为空白。
         * @param callback 二元函数：使用params中的值替换pattern结构的变量的时候，对值的操作函数。把对象转换成字符串的方法。<br>
         *   (pat,value)->String, pat 是匹配的模式的字符串,value 是在params 与 pat 中的key 为 pat的 <br>
         *   group(1)的数据值。
         * @return 解析后的结果，对于满足line中的pattern,但是尚未再params找到对应值的数据，将其给予再line中删除，即替换为空白。
         */
        @SuppressWarnings("unchecked")
        public static  <T> String substitute(final String line,final Pattern pattern,
            final IRecord params,BiFunction<String,T,String>callback) {
            var matcher = pattern.matcher(line);
            var _line = line;
            var times = 10000;// 最大的替换次数
            while(matcher.find()){
                if(times<=0) {
                    System.err.println("替换次数超过1000次，给与终止替换");
                }
                if(matcher.groupCount()<1) {
                    System.err.println("必须在pattern中使用分组标识出：pattern 标识的变量名称！");
                    return line;// 
                }// if
                final var key = matcher.group(1);// 提取pattern所标识的变量
                final var value = params.get(key);// 提取该pattern所标识的变量的值。
                final var s = callback.apply(matcher.group(),(T)value);// 获取替换值的字符串标识。
                _line = matcher.replaceFirst(s==null?"":s);// 更新此次匹配的结果
                matcher = pattern.matcher(_line);
            }// while
            
            return _line;// 结果返回
        }
        
        /**
         * 从clazz 对象中寻找一个名字叫做 name的方法
         * @param clazz 类对象
         * @param name 方法名称的字符串表示
         * @return 方法对象
         */
        public static  Method methodOf(Class<?> clazz,String name) {
            return Arrays.stream(clazz.getDeclaredMethods())
                .filter(e->e.getName().equals(name)).findFirst().get();
        }
        
        /**
         * 快速构造一个Map&ltObject,Object&gt的对象:快送构造Map的方法
         * @param oo key1,value1,key2,value2的序列
         * @return Map&ltObject,Object&gt的对象
         */
        public static Map<Object,Object> M(Object ...oo){
            final var map = new LinkedHashMap<Object,Object>();
            if(oo!=null&&oo.length>0) {
                for(int i=0;i+1<oo.length;i+=2) {
                    map.put(oo[i],oo[i+1]);
                }//for
            }//if
            return map;
        }

        /**
         * 创建jdbc连接对象
         */
        public void init(String driver,String url,String user,String password){
            this.supplierConn=()->{// 自己提供数据库连接
                Connection conn = null;// 数据库连接
                try {
                    Class<?> cls= (null!=driver)? Class.forName(driver):null;
                    if(cls==null) {System.err.println("数据库驱动:\""+driver+"\"加载失败!");return null;}
                    conn = DriverManager.getConnection(url, user, password);
                } catch (Exception e) {
                    System.err.println(MessageFormat.format(
                            "jdbc connection error for,driver:{0},url:{1},user:{2},password:{3}",
                            driver,url,user,password));
                    e.printStackTrace();
                }//try
                return conn;
            };// 连接构造其
        }
        
        /**
         * 目前只支持mysql，为了防止意外发生，不提供表删除操作
         * @param tableName　表名
         * @return
         */
        public boolean tblExists(String tableName) {
            return this.sql2records("show tables like '"+tableName+"'").size()>0;
        }
        
        /**
         * 目前只支持mysql
         * @param database 数据库名
         * @return
         */
        public boolean dbExists(String database) {
            return this.sql2records("show databases like '"+database+"'").size()>0;
        }
        
        /**
         * 目前只支持mysql
         * @param tableName　表名
         * @param defs 表定义
         * @return
         */
        public boolean createTable(String tableName,String defs) {
            String sql = MessageFormat.format("create table {0} ( {1} ) ",defs);
            return this.sqlexecute(sql);
        }
        
        /**
         * 目前只支持mysql
         * @param tableName 表名
         * @param defs 表定义
         * @return
         */
        public boolean createTable(String tableName,String ... defs) {
            String sql = MessageFormat.format("create table {0} ( {1} ) ",
                tableName,Arrays.asList(defs).stream().collect(Collectors.joining(",")));
            return this.sqlexecute(sql);
        }
        
        /**
         * 目前只支持mysql
         * @param tableName 表名
         * @param defs 表定义
         * @return
         */
        public boolean createTableIfNotExists(String tableName,String ... defs) {
            if(this.tblExists(tableName))return false;
            String sql = MessageFormat.format("create table {0} ( {1} ) ",
                tableName,Arrays.asList(defs).stream().collect(Collectors.joining(",")));
            if(debug)System.out.println("jdbc:createTableIfNotExists:"+sql);
            return this.sqlexecute(sql);
        }

        /**
         * 获得数据库连接:每次都是重新创建一个数据库连接．
         * @return 数据库连接
         */
        public Connection getConnection() {
            Connection conn = supplierConn.get();
            return conn;
        }

        /**
         * 執行sql
         * @param sql
         */
        public boolean sqlexecute(final String sql) {
            return psqlexecute(sql,(Map<Integer,Object>)null);
        }
        
        /**
         * 執行sql
         * @param sql
         */
        public boolean psqlexecute(final String sql,final Map<Integer,Object>params) {
            return psqlexecute(sql,params,this.getConnection(),true);
        }
        
        /**
         * 执行SQL语句不考虑对原有的数据的做的变化：
         * @param sql 
         * @param oo 参数数组
         * @return 执行状态标记。
         */
        public boolean psqlexecute(final String sql,final Object[]oo) {
            final Map<Integer,Object> params = new HashMap<>();
            if(oo!=null)for(int i=0;i<oo.length;i++)params.put(i+1,oo[i]);
            return psqlexecute(sql,params,this.getConnection(),true);
        }

        /**
         * 對查詢結果進行操作
         * @author gbench
         *
         * @param <T>
         */
        public interface QueryHandler<T>{
            /**
             * 對查詢結果進行處理（結果已經被生成道了rs之中）
             * @param conn 數據庫練級 通常不需要操作
             * @param stmt 查詢語句  通常不需要操作
             * @param rs 返回結果
             * @param rsm 結果信息結構
             * @param n 結果列數
             * @return 處理結果
             */
            public T handle(final Connection conn,final Statement stmt,
                final ResultSet rs,final ResultSetMetaData rsm,final int n) throws SQLException;
        }

        /**
         * 一次事务性的会话：共享一个数据库连接，并且出现操作失败（sql操作)，将给予先前的动作回滚．
         * IJdbcSession 将作为一个Monad来出现。它内部保存由会话过程的状态数据。D 类型的data
         * 用来作为链式编程的 特有编写方法。
         * 
         * IJdbcSession 专门定义了一套jdbc的操作函数。这些函数的操作不会关闭数据库连接。所以
         * 当需要进行一些列共享数据库连接的操作的时候，可以使用 带有IJdbcSession 的函数来进行操作
         * 比如Jdbc.withTransaction的系列函数。
         *
         * @author xuqinghua
         *
         * @param <T> transCode，即SessionId的类型．又叫事务编号
         * @param <D> Data，Session 中的数据类型。又叫Monad session 的状态数据。
         */
        public static interface IJdbcSession<T,D>{
            
            /**
             * 获取数据库连接
             * @return
             */
            public Connection getConnection();
            
            /**
             * 获得交易代码
             * @return 返回一个UUID,交易代码，用于标识一次会话过程．
             */
            public T transCode();
            
            /**
             * 会话中的数据内容
             * @return session中的数据类型。
             */
            public D getData();
            
            /**
             * 会话中的数据内容
             * @return session中的数据类型。
             */
            public D setData(final D data);
            
            /**
             * 返回会话属性集合
             * @return 绘画属性
             */
            public Map<Object,Object> getAttributes();
            
            /**
             * 获得SessionId, 默认采用transCode来实现．
             * @return  返回一个UUID,交易代码，用于标识一次会话过程．
             */
            public default T getSessionId() {
                return transCode();
            }
            
            /**
             * 设置属性
             * @param key 属性的键值
             * @param value 属性的值
             * @return 自身的 IJdbcSession<T,D> 的实例，用以实现链式编程
             */
            public default IJdbcSession<T,D> setAttribute(Object key,Object value) {
                this.getAttributes().put(key, value);
                return this;
            }
            
            /**
             * 设置属性
             * @attributes 待设置的属性集合
             * @return 自身的 IJdbcSession<T,D> 的实例，用以实现链式编程
             */
            public default IJdbcSession<T,D> setAttributes(Map<Object,Object>attributes) {
                this.getAttributes().putAll(attributes);
                return this;
            }
            
            /**
             * 清空属性集合
             * @return 自身的 IJdbcSession<T,D> 的实例，用以实现链式编程
             */
            public default IJdbcSession<T,D> clearAttributes() {
                this.getAttributes().clear();
                return this;
            }
            
            /**
             * 设置属性
             * @param key 属性的键值
             * @return 属性的值，如果不存在返回空
             */
            public default Object getAttribute(Object key) {
               return this.getAttributes().get(key);
            }
            
            /**
             * 绑定函数：使用d2d1 apply 到先的 Monad 进而把IJdbcSession 推进到一个新的状态D1:
             * IJdbcSession<T,D1>。bind 的实现需要首先检测当前自身的数据状态，对于无效的数据状态
             * getDate()==null 则拒绝执行绑定传递函数:d2d1
             * 
             * @param <D1> 目标容器的参数类型
             * @param d2d1> 结合自生状态计算的绑定传递函数：d => d1, 表示从d转换成d1状态，2是to的简写
             * @return 保存由d1类型的数据容器：新状态数据的容器
             * @throws SQLException
             * @throws Exception
             */
            public default <D1> IJdbcSession<T,D1> bind(final FunctionWithException<D,D1> d2d1)
                throws SQLException, Exception{
                final var d = this.getData();// 提取monad中的数据
                return d==null?null:this.fmap(this.getData(), d2d1);// 这句是关键如果  当前数据为null 则不再做继续传递的操作。
            }
            
            /**
             * Monad fmap 用于纪念monad 名称
             * @param <D1> 目标容器中的元素的类型。
             * @param <S> 输入参数的类型:初始入参的类型:Start 开始点入口数据的准备：
             * @return 一个以数据元素类型为 为
             * @throws SQLException
             */
            public default <D1,S> IJdbcSession<T, D1> monad(final S s, 
                final FunctionWithException<S, D1> dataInitor) throws SQLException, Exception{
                return fmap(s,dataInitor);
            }
            
            /**
            * Monad fmap 用于
            * @param s 初始数据 start
            * @param <D1> 目标容器中的元素的类型。
            * @param <S> 输入参数的类型:初始入参的类型:Start 开始点入口数据的准备
            * @return 一条结果的数据集合
            * @throws SQLException
            */
            public default <D1,S> IJdbcSession<T, D1> fmap(final S s, 
                final FunctionWithException<S, D1> s2d1) throws SQLException, Exception {
                
                // s2d1 是对  new IJdbcSession<T, D1>() 中的数据 data 字段的初始化。
                final var d1 = s2d1.apply(s);// 计算映射结果. 然后把这个d1 设置到一个新的 IJdbcSession 容器之中.
            
                // 创建一个新的 IJdbcSession
                return new IJdbcSession<T, D1>() {// 这就是一个容器的类型的生成过程。注意D1是元素类型。IJdbcSession 是容器类型。
                    /**
                     * 查询结果集合
                     */
                    public List<IRecord> psql2records(final String sql, final Map<Integer, Object> params) 
                        throws SQLException {
                        final var pstmt = pstmt(this.getConnection(),SQL_MODE.QUERY,sql,params);
                        final var recs = readlines(pstmt.executeQuery(),true); pstmt.close();// 读取数据并关闭语句
                        return recs;
                    }
                    
                    /**
                     * 更新数据:execute 与update 同属于与 update
                     */
                    public List<IRecord> psql2update(final String sql, final Map<Integer, Object> params)
                        throws SQLException {
                        final var pstmt = pstmt(this.getConnection(),SQL_MODE.UPDATE,sql,params);
                        // 获取生成主键信息，字段名称为：GENERATED_KEY
                        final var recs = readlines(pstmt.getGeneratedKeys(),true); pstmt.close();// 读取数据并关闭语句
                        return recs;
                    }
                    
                    // 创建session 会话
                    @Override public Connection getConnection() {return IJdbcSession.this.getConnection();}
                    @Override public T transCode() {return uuid;}// 交易编码的实现
                    @Override public D1 getData() {return data;};// 会话中的数据
                    @Override public D1 setData(D1 d) {return this.data = d;};// 会话中的数据
                    @Override public Map<Object,Object> getAttributes(){return this.attributes;};// 返回会话属性
                    
                    private T uuid = IJdbcSession.this.getSessionId();// 重用当前的 SessionId 以保证Session 共享
                    private D1 data = d1; // 计算映射结果
                    private Map<Object,Object> attributes = new HashMap<Object,Object>();// 会话属性
                };// IJdbcSession
                
            }// fmap 
            
            /**
             * 执行sql语句查询出结果集合
             * @param sql sql 语句
             * @return IRecord类型的结果集合
             * @throws SQLException
             */
            public default List<IRecord> sql2records(final String sql) throws SQLException,Exception{
                return this.psql2records(sql,(Map<Integer,Object>)null);
            }
            
            /**
             * 执行sql语句查询出结果集合.<br>
             * 比如：传入与sharp标号语句：让spp 从其语句库中检索。<br>
             * final var recs = sess.sql2records("#getCytokines",REC("name",name),spp); <br>
             * 或者是直接传入带有sharp变量编号的语句让spp解析。<br>
             * final var recs = sess.sql2records( <br>
             * "MATCH (n)-[:Secrete]->(b) where b.name=#name RETURN n.name as host",<br>
             * REC("name",name),spp); <br>
             * @param sqlpattern sql模板那：可以是是一个sharp语句标记：比如#getUser,用于从模板文件中提取脚本，也可以是函数有sharp变量的语句模板。
             * @param params sharp变量的形参与实际参数的对应关系
             * @param spp sqlpattern 的解释器
             * @return IRecord类型的结果集合
             * @throws SQLException
             */
            public default List<IRecord> sql2records(String sqlpattern,IRecord params,SqlPatternPreprocessor spp) throws SQLException,Exception{
                return this.sql2records(spp.handle(null, params, sqlpattern, null));
            }
            
            /**
             * 使用自身带有的spp(sqlpattern 的解释器,前提需要设置Class<SqlPatternPreprocessor>为键值的属性)执行sql语句查询出结果集合.<br>
             * 比如：传入与sharp标号语句：让spp 从其语句库中检索。<br>
             * final var recs = sess.sql2records("#getCytokines",REC("name",name)); <br>
             * 或者是直接传入带有sharp变量编号的语句让spp解析。<br>
             * final var recs = sess.sql2records( <br>
             * "MATCH (n)-[:Secrete]->(b) where b.name=#name RETURN n.name as host",<br>
             * REC("name",name)); <br>
             * @param sqlpattern sql模板那：可以是是一个sharp语句标记：比如#getUser,用于从模板文件中提取脚本，也可以是函数有sharp变量的语句模板。
             * @param params sharp变量的形参与实际参数的对应关系
             * @return IRecord类型的结果集合
             * @throws SQLException
             */
            public default List<IRecord> sql2records(String sqlpattern,IRecord params) throws SQLException,Exception{
                final var spp = (SqlPatternPreprocessor)this.getAttribute(SqlPatternPreprocessor.class);
                if(spp==null) {
                    System.err.println("Session没有内置的SqlPatternPreprocessor(spp),方法无法进行调用，请设置正确的spp:以Class<SqlPatternPreprocessor>为键值");
                    return null;
                }//if
                return this.sql2records(spp.handle(null, params, sqlpattern, null));
            }
            
            /**
             * 注意这里的 rec 是对SQL 中的参数补充，而不是对preparedStatement 中的参数。
             * @param sql
             * @return
             * @throws SQLException
             */
            public default List<IRecord> sql2records(final SQL sql,final IRecord rec) throws SQLException,Exception{
                return this.psql2records(sql.string(rec),(Map<Integer,Object>)null);
            }
            
            /**
             * 从结果集中提取一行数据。
             * @param sql sql 语句
             * @return 一条结果的数据集合，单行数据或者没有数据。
             * @throws SQLException
             */
            public default Optional<IRecord> sql2maybe(final String sql) throws SQLException,Exception{
                return this.sql2records(sql).stream().findFirst();
            }
            
            /**
             * 从结果集中提取一行数据。
             * @param sql sql 语句
             * @param targetClass 目标结果类型
             * @return 一条结果的数据集合，单行数据或者没有数据。
             * @throws SQLException
             */
            public default <U> U sql2get(final String sql,Class<U>targetClass) throws SQLException,Exception{
                return this.sql2maybe(sql, targetClass).get();
            }
            
            /**
             * 从结果集中提取一行数据。
             * @param sql sql 语句
             * @return 一条结果的数据集合，单行数据或者没有数据。
             * @throws SQLException
             */
            public default <U> Optional<U> sql2maybe(final String sql,final Class<U> targetClass) throws SQLException,Exception{
                return this.sql2records(sql).stream().findFirst().map(e->IRecord.rec2obj(e,targetClass));
            }
            
            /**
             * 从结果集中提取一行数据。
             * @param sql
             * @return 单行数据或者没有数据。
             * @throws SQLException
             */
            public default Optional<IRecord> sql2maybe(final SQL sql,final IRecord rec) throws SQLException,Exception{
                return this.sql2maybe(sql.string(rec));
            }
            
            /**
             * sql语句执行  这是对sqlupdate的别名
             * @param sql sql 语句
             * @return 查询结果集合
             * @throws SQLException
             */
            public default List<IRecord> sql2update(final String sql) throws SQLException,Exception{
                return this.sqlupdate(sql);
            }
            
            /**
            * sql语句执行等同于　sql2update
            * @param sql
            * @return
            * @throws SQLException
            */
            public default List<IRecord> sql2execute(final String sql) throws SQLException,Exception{
                return sqlupdate(sql);
            }
            
            /**
            * 执行sql语句更新
            * @param sql sql 语句
            * @return 位置参数
            * @throws SQLException
            */
            public default List<IRecord> sqlupdate(final SQL sql,final IRecord rec) throws SQLException,Exception{
                return this.sqlupdate(sql.string(rec));
            }
            
            /**
             * sql语句执行
             * @param sql
             * @return 带有：生成的主键 GENERATED_KEY的字段记录
             * @throws SQLException
             */
            public default Optional<IRecord> sql2execute2maybe(final String sql) throws SQLException,Exception {
                return this.sql2update(sql).stream().findFirst();
            }
            
            /**
             * sql语句执行
             * @param sql sql 语句
             * @param <U> 目录结果的类型
             * @return 带有：生成的主键 GENERATED_KEY的字段记录
             * @throws SQLException
             */
            public default <U> Optional<U> sql2execute2maybe(final String sql,final Class<U> targetClass) throws SQLException,Exception {
                return this.sql2update(sql).stream().findFirst().map(e->IRecord.rec2obj(e,targetClass));
            }
            
            /**
             * sql语句执行
             * @param sql，sql 语句模板，参数采用 #xxx 字符串类型, ##xxx 数值类型,或是 ${xxx} 类型  来进行占位,其中 
             * @param rec：sql 语句模板：命名参数集合，key->value的键值对
             * @return 带有：生成的主键 GENERATED_KEY的字段记录
             * @throws SQLException
             */
            public default Optional<IRecord> sql2execute2maybe(final SQL sql,final IRecord rec) throws SQLException,Exception {
                return this.sql2update(sql.string(rec)).stream().findFirst();
            }
            
            /**
             * sql语句执行
             * @param sql insert 或 update 语句
             * @return 生成的主键 GENERATED_KEY,如果没有生成 GENERATED_KEY比如update语句，返回空值
             * @throws SQLException
             */
            public default Integer sql2execute2int(final String sql) throws SQLException,Exception {
                final Number num = this.sql2execute2num(sql);
                return num==null?null:num.intValue();
            }
            
            /**
             * sql语句执行
             * @param sql insert 或 update 语句
             * @return 生成的主键 GENERATED_KEY，如果没有生成 GENERATED_KEY比如update语句，返回空值
             * 对于非mysql 数据库比如不会生成GENERATED_KEY,但杀局插入成功返回-1
             * @throws SQLException
             */
            public default Number sql2execute2num(final String sql) throws SQLException,Exception {
                final Optional<IRecord> maybe =this.sql2execute2maybe(sql);
                final Number num = maybe.isPresent()?maybe.map(e->{
                    var gk = e.num("GENERATED_KEY");// 尝试提取
                    return gk ==null?-1:gk;
                }).get():null;
                return num;
            }
            
            /**
             * sql语句执行
             * @param sql：sql 语句模板，参数采用 #xxx 字符串类型, ##xxx 数值类型,或是 ${xxx} 类型  来进行占位,其中
             * @param rec：sql 语句模板：命名参数集合，key->value的键值对
             * @return 生成的主键 GENERATED_KEY
             * @throws SQLException
             */
            public default int sql2execute2int(final SQL sql,final IRecord rec) throws SQLException,Exception {
                return sql2execute2num(sql,rec).intValue();
            }
            
            /**
             * sql语句执行
             * @param sql : sql 语句模板，参数采用 #xxx 字符串类型, ##xxx 数值类型,或是 ${xxx} 类型  来进行占位,其中
             * @param rec：sql 语句模板：命名参数集合，key->value的键值对
             * @return 生成的主键 GENERATED_KEY
             * @throws SQLException
             */
            public default Number sql2execute2num(final SQL sql,final IRecord rec) throws SQLException,Exception {
                return sql2execute2num(sql.string(rec));
            }
            
            /**
             * 批量執行sql语句集合
             * @param sqls sql 语句集合
             */
            public default List<List<IRecord>> sql2batch(final List<String>sqls) throws SQLException,Exception {
                if(sqls==null)return null;
                final List<List<IRecord>> rr = new LinkedList<>();// 语句链表
                for(String sql:sqls) {
                    if(sql==null||sql.length()<=0||sql.matches("^[\\s]*$"))continue;
                    rr.add(sql2update(sql));
                }//for
                return rr;
            }
            
            /**
             * 批量执行目前只能够执行 插入更新语句，而不能自动判断出是 QUERY 还是 UPDATE
             * @param sqls 需要批量执行的语句集合
             * @return 批量执行的结果
             * @throws SQLException
             */
            public default List<List<IRecord>> sql2batch(final String ... sqls) throws SQLException,Exception {
                return sql2batch(Arrays.asList(sqls));
            }
            
            /**
             * 查询结果集合
             *
             * @param sql preppared sql 语句
             * @param params params sql 语句中占位符参数的值集合，位置从1开始
             * @return 查询结果集合
             * @throws SQLException
             */
            public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params) throws SQLException;
            
            /**
             * 查询结果集合
             *
             * @param sql preppared sql 语句
             * @param params sql 语句中占位符参数的值集合，位置从1开始
             * @return 查询结果集合
             * @throws SQLException
             */
            public default List<IRecord> psql2records(final String sql,final IRecord params) throws SQLException{
                final var mm = new HashMap<Integer,Object>();
                params.foreach((k,v)->{
                    try {
                        final var i = Integer.parseInt(k);
                        mm.put(i, v);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });// foreach
                return psql2records(sql,mm);
            }
            
            /**
             * 查询结果集合
             * 
             * @param <U> 参数类型
             * @param sql preppared sql 语句
             * @param params sql 语句中占位符参数的值列表，位置从1开始
             * @return 查询结果集合
             * @throws SQLException
             */
            public default <U> List<IRecord> psql2records(final String sql,final List<U> params) throws SQLException{
                return psql2records(sql,params==null?new Object[] {}:params.toArray());
            }
            
            /**
             * 查询结果集合
             * 
             * @param sql preppared sql 语句
             * @param oo preppared sql 语句中占位符参数的值数组
             * @return 查询结果集合
             * @throws SQLException
             */
            public default List<IRecord> psql2records(final String sql,final Object [] oo) throws SQLException{
                final Map<Integer,Object> params = new LinkedHashMap<>();
                if(oo!=null)for(int i=0;i<oo.length;i++)params.put(i+1, oo[i]);// pareparedstatement 占位符 从1开始
                return psql2records(sql,params);
            }
            
            /**
             * 查询结果集合
             * 
             * @param sql preppared sql 语句
             * @param params preppared sql 语句中占位符参数的值集合，位置从1开始
             * @return 查询结果Optional
             * @throws SQLException
             */
            public default Optional<IRecord> psql2maybe(final String sql,final IRecord params) throws SQLException{
               return psql2records(sql,params).stream().findFirst();
            }
            
            /**
             * 查询结果集合
             * 
             * @param <U> 返回值的类型
             * @param sql preppared sql 语句
             * @param params preppared sql 语句中占位符参数的值集合，位置从1开始
             * @param targetClass 结果的目标雷西给 
             * @return U类型的对象
             * @throws SQLException
             */
            public default <U> U psql2get(final String sql,final IRecord params,Class<U> targetClass) throws SQLException{
               return psql2maybe(sql,params).map(e->IRecord.rec2obj(e,targetClass)).get();
            }
            
            /**
             * 查询结果集合
             * 
             * @param sql preppared sql 语句
             * @param oo preppared sql 语句中占位参数的值数组
             * @return 查询结果集：Optional
             * @throws SQLException
             */
            public default Optional<IRecord> psql2maybe(final String sql,final Object [] oo) throws SQLException{
                return psql2records(sql,oo).stream().findFirst();
            }
            
            /**
             * 查询结果集合
             * 
             * @param <U> 返回值的类型
             * @param sql preppared sql 语句
             * @param oo preppared sql 语句中占位参数的值数组
             * @param targetClass 结果的目标雷西给 
             * @return U类型的对象
             * @throws SQLException
             */
            public default <U> U psql2get(final String sql,final Object [] oo,Class<U> targetClass) 
                throws SQLException{
                
                return psql2maybe(sql,oo).map(e->IRecord.rec2obj(e,targetClass)).get();
            }
            
            /**
             * 更新数据:execute 与update 同属于与 update
             * @param sql prepared sql 语句
             * @param params 占位符的对应值的Map,位置从1开始
             * @return 更新结果集合函数有 generatedKeys 键值
             * @throws SQLException
             */
            public List<IRecord> psql2update(final String sql,final Map<Integer,Object> params) throws SQLException;
            
            /**
             * 更新数据:execute 与update 同属于与 update
             * @param sql sql 语句
             * @param params 占位符的对应值列表
             * @return 更新结果集合函数有 generatedKeys 键值
             * @throws SQLException
             */
            public default <U> List<IRecord> psql2update(final String sql,final List<U> params) throws SQLException{
                return this.psql2update(sql,params==null?new Object[] {}:params.toArray());
            }
            
            /**
             * 更新数据:execute 与update 同属于与 update
             * @param sql prepared sql 语句
             * @param oo 语句中占位参数的值数组
             * @return 更新结果集合函数有 generatedKeys 键值
             * @throws SQLException
             */
            public default List<IRecord> psql2update(final String sql,final Object[] oo) throws SQLException{
                final Map<Integer,Object> mm = new LinkedHashMap<>();
                if(oo!=null)for(int i=0;i<oo.length;i++)mm.put(i+1,oo[i]);// pareparedstatement 占位符 从1开始
                return this.psql2update(sql,mm);
            }
            
            /**
             * 更新数据:execute 与update 同属于与 update
             * @param sql sql 语句
             * @param rec sql语句中占位参数的值集合，位置参数从1开始
             * @return 更新结果集合函数有 generatedKeys 键值
             * @throws SQLException
             */
            public default List<IRecord> psql2update(final String sql,final IRecord rec) throws SQLException{
                var mm = rec.toMap2(Integer::parseInt);
                return this.psql2update(sql,mm);
            }
            
            /**
             * 更新数据:execute 与update 同属于与 update
             * 
             * @param sql SQL语句 这里是采用 preparedStatement 来执行。
             * @return sql 语句的执行记过
             * @throws SQLException
             */
            public default List<IRecord> sqlupdate(final String sql) throws SQLException{
                return psql2update(sql,(Map<Integer,Object>)null);
            }
            
            /**
             * 执行SQL语句
             * 
             * @param sql SQL 语句
             * @param oo sql语句中占位参数的值数组
             * @return 结果是否为一个resultset
             */
            public default boolean psqlexecute(final String sql,final Object[]oo) throws SQLException{
                final Map<Integer,Object> params = new LinkedHashMap<>();
                if(oo!=null)for(int i=0;i<oo.length;i++)params.put(i+1, oo[i]);// pareparedstatement 占位符 从1开始
                final var pstmt = pstmt(this.getConnection(),SQL_MODE.UPDATE,sql,params);
                final var b = pstmt.execute();
                pstmt.close();//关闭语句
                return b;
            }
            
            /**
             * 执行sql语句不产生结果集合.<br>
             * @param sql SQL 语句
             * @return 结果是否为一个resultset
             */
            public default boolean sqlexecute(final String sql) throws SQLException{
                return psqlexecute(sql,null);
            }

            /**
             * 执行sql语句不产生结果集合.<br>
             * 比如：传入与sharp标号语句：让spp 从其语句库中检索。<br>
             * final var recs = sess.sql2records("#getCytokines",REC("name",name),spp); <br>
             * 或者是直接传入带有sharp变量编号的语句让spp解析。<br>
             * final var recs = sess.sql2records( <br>
             * "MATCH (n)-[:Secrete]->(b) where b.name=#name RETURN n.name as host",<br>
             * REC("name",name),spp); <br>
             * @param sqlpattern sql模板那：可以是是一个sharp语句标记：比如#getUser,用于从模板文件中提取脚本，也可以是函数有sharp变量的语句模板。
             * @param params sharp变量的形参与实际参数的对应关系
             * @param spp sqlpattern 的解释器：把sqlpattern转换成sql语句。
             * @return 执行的sql语句是否返回了一个结果集合
             * @throws SQLException
             */
            public default boolean sqlexecute(String sqlpattern,IRecord params,SqlPatternPreprocessor spp) throws SQLException{
                return this.sqlexecute(spp.handle(null, params, sqlpattern, null));
            }
            
            /**
             * 使用自身带有的spp(sqlpattern 的解释器,前提需要设置Class<SqlPatternPreprocessor>为键值的属性)执行sql语句不产生结果集合.<br>
             * 比如：传入与sharp标号语句：让spp 从其语句库中检索。<br>
             * final var recs = sess.sql2records("#getCytokines",REC("name",name)); <br>
             * 或者是直接传入带有sharp变量编号的语句让spp解析。<br>
             * final var recs = sess.sql2records( <br>
             * "MATCH (n)-[:Secrete]->(b) where b.name=#name RETURN n.name as host",<br>
             * REC("name",name)); <br>
             * @param sqlpattern sql模板那：可以是是一个sharp语句标记：比如#getUser,用于从模板文件中提取脚本，也可以是函数有sharp变量的语句模板。
             * @param params sharp变量的形参与实际参数的对应关系
             * @return IRecord类型的结果集合
             * @throws SQLException
             */
            public default boolean sqlexecute(String sqlpattern,IRecord params) throws SQLException{
                final var spp = (SqlPatternPreprocessor) this.getAttribute(SqlPatternPreprocessor.class);
                if(spp==null) {
                    System.err.println("Session没有内置的SqlPatternPreprocessor(spp),方法无法进行调用，请设置正确的spp:以Class<SqlPatternPreprocessor>为键值");
                    return false;
                }//if
                return this.sqlexecute(spp.handle(null, params, sqlpattern, null));
            }

            /**
             * 提取 GENERATED_KEY 字段的数据
             * @param res sql 的查询结果集合
             * @param mapper 把GENERATED_KEY 字段转换成目标类型。
             * @return GENERATED_KEY 生成的数据列表
             */
            public static <U> List<U> gkeys(final List<IRecord> res,Function<Object,U>mapper){
                return res.stream().map(e->e.str("GENERATED_KEY")).map(mapper).collect(Collectors.toList());
            }
            
            /**
             * 生成一个 GENERATED_KEY 字段的数据
             * @param res sql 的查询结果集合
             * @return GENERATED_KEY 生成的数据列表
             */
            public static List<Long> ids (final List<IRecord> res){
                return gkeys(res,e->Long.parseLong(e+""));
            }
            
            /**
             * GENERATED_KEY 字段生成的数据
             * @param res SQL 语句查询出来的额结果集合
             * @return id值，从res中提取一个数值
             */
            public static Long id(final List<IRecord> res){
                return ids(res).stream().findAny().get();
            }
            
        }// IJdbcSession

        /**
         * IJdbcSession 专门定义了一套jdbc的操作函数。这些函数的操作不会关闭数据库连接。
         * 这里提供一个半成品的的IJdbcSession的实现，以方便具体IJdbcSession的具体实现，没必要每次都从零开始
         * 可以从AbstractJdbcSession开始，
         * @author gbench
         *
         */
        public abstract class AbstractJdbcSession<T,D> implements IJdbcSession<T,D> {

            /**
             * 查询结果集合
             */
            public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params) throws SQLException{
                //return Jdbc.this.psql2records_throws(sql,params, this.getConnection(), false, SQL_MODE.QUERY, null,Optional.empty());
                
                // 需要注意：precords_throws 使用的的对象数据作为模板参数。
                Object[] _params = params==null ? new Object[]{} : params.values().toArray();// preparedSQL的模板参数列表。
                return Jdbc.this.precords_throws(this.getConnection(),sql,_params);
            }//

            /**
             * 更新数据:execute 与update 同属于与 update
             */
            public List<IRecord> psql2update(final String sql,final Map<Integer,Object>params)  throws SQLException{
                return Jdbc.this.psql2records_throws(sql,params, this.getConnection(),
                    false, SQL_MODE.UPDATE, null,Optional.empty());
            }//
        }

        /**
         * 一个DataManipulation代表一个数据操作计划：需要有媒介载体：即Session来给予表达，即DataManipulation
         * 中的符号概念，具体对应在什么物理物理位置．这些信息通常就是一个数据库连接来给予间接表达．
         * 
         * DataManipulation 是专门为了数据库的事务性操作来而创建的接口。
         * 可以发出执行异常的 SQL语句,这是专门为了实现SQL事务而构造的结构。
         * DataManipulation 其实就是对Consumer函数的一个模拟，只是由于Consumer的accept不能够抛出异常
         * DataManipulation 加入了一个可以抛出异常的invoke,
         * @author gbench
         *
         * @param <T> 会话对象 每个数据操作都会存在一个特有的会话对象，以保留数据操作的上下文信息。
         * 则个会话对象需要由事务的创建者来给与提供。
         */
        public static interface DataManipulation<T>{
            /**
             * 这个函数主要是通过异常 throw Exception 来表示执行失败
             * @param session 数据操作的媒介载体：即交易会话
             * @throws SQLException
             * @throws IndexOutOfBoundsException
             * @throws Exception
             * @throws Throwable
             */
            public void invoke(final T session) throws SQLException,IndexOutOfBoundsException,Exception,Throwable;
        }

        //请求模式
        public enum SQL_MODE{QUERY,QUERY_SCROLL,UPDATE}

        /**
         * 指定session 执行DataManipulation．:Session 是Monad对象。因此可以进行函数式的状态编程<br>
         * 
         * 发起创建一个IJdbcSession对象，并通过IJdbcSession急性数据库操作<br>
         * 事务处理,每一个事务，系统会动态的创建出一个 session 对象（IJdbcSession），这个Session 对象拥有一个UUID类型的对象标识。<br>
         * 在一次事务性的会话IJdbcSession中：共享一个数据库连接，并且出现操作失败（sql操作)，将给予先前的动作回滚．<br>
         * 事务只能对DML语句进行操作，对数据定义类语句DDL无法操作，例如建表、建立索引、建立分区等。<br>
         * 一般采用如下方式调用此函数：<br>
         * jdbc.withTransaction(sess->{session.sql2records("show databases");});<br>
         * 
         * @param dm ：DataManipulation　代表，数据操作的具体过程 dm 的数据如果需要会馆请使用dm所提供的session 来操作数据,通常采用lamba表达式来给予
         * 创建操作过程：sess->{写入你的操作代码}. 需要注意对于withTransaction创建的会话IJdbcSession 是以monad 容器。其初始数据为Object类型
         * 值为null.
         * @return ret返回值boolean值 ,exception： 异常类型,throwable:异常类型,用于动态代理的默认函数，参见Jdbc.newInstance
         */
        public synchronized IRecord withTransaction (final DataManipulation<IJdbcSession<UUID,Object>> dm) {
            return this.withTransaction(dm, (IJdbcSession<UUID,Object>)null,(Map<Object,Object>)null);
        }
        
        /**
         * 指定session 执行DataManipulation．:Session 是Monad对象。因此可以进行函数式的状态编程<br>
         * 
         * 发起创建一个IJdbcSession对象，并通过IJdbcSession急性数据库操作<br>
         * 事务处理,每一个事务，系统会动态的创建出一个 session 对象（IJdbcSession），这个Session 对象拥有一个UUID类型的对象标识。<br>
         * 在一次事务性的会话IJdbcSession中：共享一个数据库连接，并且出现操作失败（sql操作)，将给予先前的动作回滚．<br>
         * 事务只能对DML语句进行操作，对数据定义类语句DDL无法操作，例如建表、建立索引、建立分区等。<br>
         * 一般采用如下方式调用此函数(使用Jdbc.M设置 session属性)：<br>
         * jdbc.withTransaction(sess->{session.sql2records("show databases");},<br>
         * Jdbc.M(SqlPatternPreprocessor.class,spp));<br>
         * 
         * @param dm ：DataManipulation　代表，数据操作的具体过程 dm 的数据如果需要会馆请使用dm所提供的session 来操作数据,通常采用lamba表达式来给予
         * 创建操作过程：sess->{写入你的操作代码}. 需要注意对于withTransaction创建的会话IJdbcSession 是以monad 容器。其初始数据为Object类型
         * 值为null.
         * @param sessAttributes 附加到sess上的属性信息。
         * @return ret返回值boolean值 ,exception： 异常类型,throwable:异常类型,用于动态代理的默认函数，参见Jdbc.newInstance
         */
        public synchronized IRecord withTransaction (final DataManipulation<IJdbcSession<UUID,Object>> dm,
            Map<Object,Object>sessAttributes) {
            return this.withTransaction(dm,(IJdbcSession<UUID,Object>)null,sessAttributes);
        }

        /**
         * 指定session 执行DataManipulation．:Session 是Monad对象。因此可以进行函数式的状态编程<br>
         * 发起创建一个IJdbcSession对象，并通过IJdbcSession急性数据库操作<br>
         * 事务处理,每一个事务，系统会动态的创建出一个 session 对象，这个Session 对象拥有一个UUID类型的对象标识。<br>
         * 在一次事务性的会话IJdbcSession中：共享一个数据库连接，并且出现操作失败（sql操作)，将给予先前的动作回滚．<br>
         * 事务只能对DML语句进行操作，对数据定义类语句DDL无法操作，例如建表、建立索引、建立分区等。<br>
         * @param dm 数据操作的具体过程 dm 的数据如果需要会馆请使用dm所提供的session 来操作数据
         * @param sess 数据操作所在的会话会话对象,其实就是对一个Connection的包装.需要注意对于withTransaction创建的会话IJdbcSession 是以monad 容器。其初始数据为Object类型
         * 值为null.
         * @param sessAttributes 附加到sess上的属性信息。
         * @return ret返回值boolean值 ,exception： 异常类型,throwable:异常类型,用于动态代理的默认函数，参见Jdbc.newInstance
         */
        public synchronized IRecord withTransaction (final DataManipulation<IJdbcSession<UUID,Object>>  dm,
            final IJdbcSession<UUID,Object> sess,Map<Object,Object>sessAttributes) {
            
            Boolean success= true;// 执行状态，不是成功就是失败，判断标志就是 completed是否爆出异常
            // IJdbcSession 专门定义了一套jdbc的操作函数。这些函数的操作不会关闭数据库连接。
            final Connection conn = this.supplierConn.get();//  自己创建一个数据库连接，这个数据库连接将在整个transaction 进行共享。
            Exception exception = null;// 异常结构
            Throwable throwable = null;// 可抛出性异常的结构
            try {
                if(conn.getAutoCommit()) conn.setAutoCommit(false);// 取消自动提交。
                try {// DM数据操作
                    //给出一个自定义实现
                    final IJdbcSession<UUID,Object> session = sess != null // 判断参数提供的session 是否有效。
                    ? sess // 非空则采用 参数提供的Session 
                    : new AbstractJdbcSession<UUID,Object> () { // 空值则提供一个默认的实现 会话的实现。
                        @Override public Connection getConnection() {return conn;}// 创建session 会话
                        @Override public UUID transCode() {return uuid;}// 交易编码的实现
                        @Override public Object getData() {return data;};// 会话中的数据
                        @Override public Object setData(Object _data) {return this.data = _data;};// 会话中的数据
                        @Override public Map<Object,Object> getAttributes(){return this.attributes;};// 返回会话属性
                        private final UUID uuid = UUID.randomUUID();
                        private Object data = null;// 初始创建Monad中data 初始化为null
                        private final Map<Object,Object> attributes = new HashMap<Object,Object>();// 会话属性
                    };// IJdbcSession
                    
                    if( sessAttributes!=null && sessAttributes.size()>0 ){ // 设置session 的会话属性
                        session.setAttributes(sessAttributes);// 设置属性
                    }//if 设置session的属性
                    
                    if(dm!=null) dm.invoke(session); // 方法属性调用
                }catch(Throwable e) { // 出现异常则进行回滚
                    success = false; conn.rollback(); throw e;// 把异常抛向外层
                }finally { // try 异常出现则回滚，标记执行失败
                    if( success ) conn.commit();// 未出现异常则提交结果
                    if( conn!=null && !conn.isClosed() ) conn.close();// 关闭连接
                }// try 异常处理
            }catch (Exception e) {
                exception = e;// 记录异常场景
                e.printStackTrace();
            }catch (Throwable e) {
                throwable = e;// 记录异常场景
                e.printStackTrace();
            }finally {
                try{if(conn!=null && !conn.isClosed())conn.close();}catch(Exception e) {e.printStackTrace();};
            }

            return SimpleRecord.REC2("ret",success,"exception",exception,"throwable",throwable);
        };

        /**
         * 執行sql
         * @param handler 數據庫連接处理子
         */
        public <T> T sqlexecute(final Function<Connection,T> handler) {
            
            return handler.apply(this.getConnection());
        }

        /**
         * 執行sql
         * @param sql
         * @param conn 數據庫連接
         * @param close 是否關閉連接
         *
         * @return true if the first result is a ResultSet object; false if it is an update count or there are no results
         */
        public boolean psqlexecute(final String sql, final Map<Integer,Object> params,
            final Connection conn,final boolean close) {
            
           PreparedStatement pstmt = null;
            boolean ret=false;
            if(conn==null) {
                System.out.println("数据库连接为null,不予执行语句！");
                return false;
            }
            try {
                pstmt = conn.prepareStatement(sql);
                if(params!=null)for(var key:params.keySet())pstmt.setObject(key,params.get(key));
                ret = pstmt.execute();
                //System.out.println(ret+"------");
            }catch(Exception e) {
                System.out.println("error sql:\n\""+sql+"\"");
                e.printStackTrace();
            }finally {
                try {
                    if(pstmt!=null)pstmt.close();
                    if(close)conn.close();
                }catch(Exception ee) {
                    ee.printStackTrace();
                }
            }
            
            return ret;
        }

        /**
         * 批量執行sql语句集合
         * @param sqls sql 语句集合
         * @param sqls 數據庫連接
         */
        public List<Boolean> sqlbatch(final List<String>sqls) {
            
            if(sqls==null)return null;
            final Connection conn = this.getConnection();
            List<Boolean> bb = sqls.stream()
                .filter(e->e!=null&&e.length()>0&&!e.matches("^[\\s]*$"))// 滤除空语句
                .map(sql->psqlexecute(sql,null,conn,false))
                .collect(Collectors.toList());
            try {conn.close();}catch(Exception e) {e.printStackTrace();}
            
            return bb;
        }


        /**
         * 批量執行sql语句集合
         * @param sqls sql 语句集合
         */
        public List<Boolean> sqlbatch(final String[] sqls) {
            
            if(sqls==null)return null;
            return this.sqlbatch(Arrays.asList(sqls));
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param qh 結果的處理
         * @return 执行结果集合的recordList
         */
        public<T> T psql2apply(final String sql,final Map<Integer,Object>params,
            final SQL_MODE mode, final QueryHandler<T> qh) {
            
            return psql2apply(sql,params,this.getConnection(),true,mode,qh);
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param conn 數據庫連接
         * @param mode 请求的模式 :查询还是更新
         * @param qh 結果的處理
         * @return 执行结果集合的recordList
         */
        public<T> T psql2apply(final String sql, final Map<Integer,Object>params,
            final Connection conn,final SQL_MODE mode,final QueryHandler<T> qh) {
            
            return psql2apply(sql,params,conn,true,mode,qh);
        }

        /**
         * 这个函数是专门为了DataMatrix::new 来进行设计的．
         * var mx = jdbc.psql2apply(sql,params,DataMatrix::new);
         * 生成结构表对象
         * @param sql sql 语句
         * @param producer 对象构造行数  (oo,hh)->T , oo数据对象矩阵,hh 表头
         * @return
         */
        public<T> T sql2apply(final String sql,final BiFunction<Object[][],List<String>,T> producer) {
            
            return this.psql2apply(sql, null, producer);
        }
        
        /**
         * 这个函数是专门为了DataMatrix::new 来进行设计的．
         * var mx = jdbc.psql2apply(sql,params,DataMatrix::new);
         * 生成结构表对象
         * @param sql sql 语句
         * @param producer 对象构造行数  (oo,hh)->T , oo数据对象矩阵,hh 表头
         * @return
         */
        public<T> T psql2apply(final String sql,final Map<Integer,Object> params,
            final BiFunction<Object[][],List<String>,T> producer) {
            
            T t = null;
            try {
                t = psql2apply_throws(sql,params,producer);
            }catch(Exception e) {
                e.printStackTrace();
            }
            return t;
        }

        /**
         * 返回的结果集：就是一个key值为列名：ColumnLabel
         * 对于 SQL_UPDATE 模式结果会返回会包括，GENERATED_KEY，生成的主键，COLS_CNT，列数，UPDATED_CNT，更新数据行数。
         *
         * @param sql 查询语句
         * @param conn 數據庫連接
         * @param close 是否關閉連接
         * @param qh 結果的處理
         *
         * @return 执行结果集合的recordList
         */
        public<T> T psql2apply(final String sql, final Map<Integer,Object>params,
            final Connection conn,final boolean close, final SQL_MODE mode, final QueryHandler<T> qh) {
            
            T ret = null;
            try {
                ret = this.psql2apply_throws(sql,params, conn, close, mode, qh);
            } catch (SQLException e) {
                e.printStackTrace();
            }//try
            return ret;
        }
        
        /**
         * 生成结构表对象
         * @param <T> 返回结果的类型
         * @param sql sql 语句
         * @param producer 对象构造行数  (oo,hh)->T , oo数据对象矩阵,hh 表头
         * @return T类型数据对象 由 producer 所生成。
         */
        public<T> T psql2apply_throws(final String sql,final Map<Integer,Object>params,
            final BiFunction<Object[][],List<String>,T> producer) throws SQLException {
            
            return psql2apply_throws(sql,params,(conn, stmt, rs, rsm, n)->{
                rs.last(); Object[][] oo= new Object[rs.getRow()][n]; rs.beforeFirst();
                while(rs.next())for(int j=1;j<=n;j++)oo[rs.getRow()-1][j-1]=rs.getObject(j);
                Function<Integer,String> foo = i->{String s = null;try{s=rsm.getColumnLabel(i);}catch(Exception e) {};return s;};
                List<String> hh = Stream.iterate(1,i->i+1).map(foo).limit(n).collect(Collectors.toList());
                return producer.apply(oo,hh);
            });
        }

        /**
         * sql2apply_throws 是对SQL 语句执行的一种抽象
         * sql2apply_throws 认为对于任何的一次SQL请求包括：
         * sql 语句,sql语句的处理模式(mode),数据库连接(conn)，连接的使用后处理情况(close)，以及对返回结果的后续加工处理(QueryHandler)。
         *
         * 带有异常抛出的 sql2apply
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * 对于 SQL_UPDATE 模式结果会返回会包括，GENERATED_KEY，生成的主键，COLS_CNT，列数，UPDATED_CNT，更新数据行数。
         *
         * @param sql 查询语句
         * @param qh 結果的處理  (conn, stmt, rs, rsm, cols_cnt)->T
         * @param params sql 中的占位符参数
         * @return 执行结果集合的recordList
         */
        public<T> T psql2apply_throws(final String sql, final Map<Integer,Object>params,
            final QueryHandler<T> qh) throws SQLException{
            
            return psql2apply_throws(sql,params,this.getConnection(),true,SQL_MODE.QUERY_SCROLL,qh);
        }
        
        /**
         * 生成sql语句．
         * @param conn 数据库连接
         * @param sql 语句
         * @param params sql 中的占位符参数
         * @return　PreparedStatement
         * @throws SQLException 
         */
        public static PreparedStatement pudt_stmt(final Connection conn, final String sql,
            final Map<Integer,Object> params) throws SQLException{
            
            return pstmt(conn, SQL_MODE.UPDATE, sql, params);
        }
        
        /**
         * 生成sql语句．
         * @param conn 数据库连接
         * @param sql 语句
         * @return　PreparedStatement
         * @throws SQLException 
         */
        public static PreparedStatement pudt_stmt(final Connection conn, final String sql) throws SQLException{
            
            return pstmt(conn, SQL_MODE.UPDATE, sql, null);
        }
        
        /**
         * 生成sql语句．
         * @param conn 数据库连接
         * @param sql 语句：含有占位符
         * @param params　sql 参数: {Key:Integer-Value:Object},Key  从1开始。
         * @return　PreparedStatement
         * @throws SQLException 
         */
        public static PreparedStatement pstmt(final Connection conn,final SQL_MODE mode,final String sql,
            final Map<Integer,Object> params) throws SQLException {
            
            PreparedStatement ps = null;
            if(mode==SQL_MODE.UPDATE){
                ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);//生成数据主键
            }else if(mode==SQL_MODE.QUERY_SCROLL){
                ps = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
            }else{
                ps = conn.prepareStatement(sql);
            }
            
            final var pm = ps.getParameterMetaData();// 参数元数据
            try {
                final var pcnt = pm.getParameterCount(); // paramcnt;
                if( pcnt>0 && params!=null && params.size()>0 ) {
                    for( final var paramIndex : params.keySet() ) {
                        if(pcnt<paramIndex) continue;// 对于超出sql的参数范围的项目
                        final var value= params.get(paramIndex);
                        ps.setObject(paramIndex, value);// 设置参数
                    }//for
                }//if
            }catch(UnsupportedOperationException e) {
                if( params!=null && params.size()>0 ) {
                    for( final var paramIndex : params.keySet() ) {
                        final var value= params.get(paramIndex);
                        ps.setObject(paramIndex, value);// 设置参数
                    }//for
                }//if
            }//try 
            
            return ps;
        }// pstmt
        
        /**
         * psql:prepared SQＬ的别名．
         * psql2apply_throws 是对SQL 语句执行的一种抽象
         * psql2apply_throws 认为对于任何的一次SQL请求包括：
         * sql 语句,sql语句的处理模式(mode),数据库连接(conn)，连接的使用后处理情况(close)，以及对返回结果的后续加工处理(QueryHandler)。
         *
         * 带有异常抛出的 sql2apply
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * 对于 SQL_UPDATE 模式结果会返回会包括，GENERATED_KEY，生成的主键，COLS_CNT，列数，UPDATED_CNT，更新数据行数。
         *
         * @param sql 查询语句
         * @param params 语句参数
         * @param conn 數據庫連接
         * @param close 是否關閉連接
         * @param qh 結果的處理  (conn, stmt, rs, rsm, cols_cnt)->T
         *
         * @return 执行结果集合的recordList
         */
        public<T> T psql2apply_throws(final String sql,final Map<Integer,Object>params,
            final Connection conn, final boolean close, final SQL_MODE mode, final QueryHandler<T> qh) 
            throws SQLException{
            
            PreparedStatement stmt = null;// 查询语句
            ResultSet rs = null;// 结果集对象
            T ret = null;
            final long begTime = System.currentTimeMillis();
            if(conn == null) {System.out.println("數據連接為空(null),不予進行數據查詢");  return null; }

            try {
                int cols_cnt = 0;// 结果集列数量
                int updated_cnt=0; // 更新的行数量
                stmt = pstmt(conn,mode,sql,params); // 创建语句
                if(mode==SQL_MODE.UPDATE) {// SQL update 模式
                    updated_cnt = stmt.executeUpdate();
                    rs = stmt.getGeneratedKeys();// 获取生成主键信息，字段名称为：GENERATED_KEY
                }else if(mode==SQL_MODE.QUERY_SCROLL) {
                    rs = stmt.executeQuery(); // 执行语句
                }else{//默认为SQL query 模式
                    rs = stmt.executeQuery(); // 执行语句
                }//if
                
                final ResultSetMetaData rsm = rs.getMetaData();
                cols_cnt = rsm.getColumnCount();
                ret = qh.handle(conn, stmt, rs, rsm, cols_cnt);
                if( mode==SQL_MODE.UPDATE && ret instanceof List && ((List<?>)ret).size()==1
                        && ((List<?>)ret).get(0) instanceof IRecord ) {
                    ((IRecord)((List<?>)ret).get(0)).add("COLS_CNT",cols_cnt).add("UPDATED_CNT",updated_cnt );
                }//if 返回结果
            }catch(Exception e) {
                System.out.println("\n-------------gbench提醒：出错sql-------------");
                System.out.println(sql);
                System.out.println("--------------------------------------------\n");
                e.printStackTrace();
                throw e;// 把捕获的异常继续跑出去
            }finally {
                try {
                    if(rs!=null)rs.close();
                    if(stmt!=null)stmt.close();
                    if(close && conn!=null)conn.close();
                }catch(Exception e) {
                    e.printStackTrace();
                    throw e; // 把捕获的异常继续跑出去
                }//try 关闭结果集合
            }//try
            
            final long endTime = System.currentTimeMillis();
            if(debug)System.out.println("last for:"+(endTime-begTime)+"ms");
            
            return ret;
        }
        
        /**
        * p开头的函数：表示prepared sql相关测操作。
        * @return 执行结果集合的recordList
        */
       public boolean pstmt_execute_throws(final PreparedStatement pstmt,
           final Map<Integer,Object>params,final boolean close) throws SQLException{
           
           if(params!=null) for(var i:params.keySet()) pstmt.setObject(i, params.get(i));
           final boolean b =pstmt.execute();
           if(close)pstmt.close();
           
           return b;
       }
       
       /**
        * p开头的函数：表示prepared sql相关测操作。
       * preapared 批处理
       * @return 执行结果集合的recordList
       */
      public boolean pstmt_execute_throws(final PreparedStatement pstmt,
              final Object[] params,final boolean close) throws SQLException{
          
          if(params!=null) for(int i=0;i<params.length;i++) {
             pstmt.setObject(i+1, params[i]);
          }
          final var b= pstmt.execute();
          if(close)pstmt.close();
          
          return b;
      }
       
       /**
        * p开头的函数：表示prepared sql相关测操作。
        * @param conn 数据据库连接
        * @param sql SQL语句
        * @param params parepared 的位置参数{pos->value}
        * @return 执行结果标记
        * @throws SQLException
        */
      public boolean pconn_execute_throws(final Connection conn, final String sql,final Map<Integer,Object>params) 
             throws SQLException{
          
         return pstmt_execute_throws(conn.prepareStatement(sql),params,true);
      }
      
     /**
      * p开头的函数：表示prepared sql相关测操作。
      * @param conn
      * @param sql
      * @param pp
      * @return
      * @throws SQLException
      */
      public boolean pconn_execute_throws(final Connection conn, final String sql, final Object ...pp) 
              throws SQLException{
          
          if(pp==null)return false;
          final Map<Integer,Object> params = new HashMap<Integer,Object>();
          for(int i=1;i<=pp.length;i++)params.put(i, pp[i]);
          
         return pstmt_execute_throws(conn.prepareStatement(sql),params,true);
      }
      
     /**
      * p开头的函数：表示prepared sql相关测操作. 执行psql请求
      * @param <T> 目标结果类型
      * @param conn 外界提供的数据库连接。
      * @param transformer 把resultset 转变成目标结果类型。 
      * @param sql sql语句模板
      * @param params 模板参数数组
      * @return 目标类型的T对象
      * @throws Throwable 异常原因。使用Throwable的好处是可以很方便的进行二次异常封装，这样就可以建立器与JDBC SQL异常的最完整偶联机制。
      */
      public <T> T pconn_query_throws(final Connection conn,final FunctionWithThrowable<ResultSet,T> transformer,
          final String sql, final Object[] params) throws Throwable {
          
          PreparedStatement pstmt = null; ResultSet rs=null; T t=null;// 目标结果
          try {
              
             pstmt = conn.prepareStatement(sql);// 制作Prepare 语句。
             var pcnt = -1;// 默认参数非法
             try {// 检查是否可以使用getParameterCount
                 pcnt = pstmt.getParameterMetaData().getParameterCount();// 语句可以接收参数的个数。
             }catch(UnsupportedOperationException uoe) {// 方法不支持异常
                 //对于方法不支持异常不予处理。
                 // uoe.printStackTrace();
             }// try 方法检测
             
             // 这段代码为了减少代码长度排成了一行。意思很简单就是 把SQL模板sql填充实际参数params
             if( pcnt>0 && params!=null ) {
                 
                 for(int i=0;i<params.length;i++) { // 模板参数遍历。
                     if(i>pcnt) {// 参数设置超过了 PreparedStatement 所能容纳的参数个数。
                         System.err.println (
                            MFT("参数#{2}设置超过了 PreparedStatement 所能容纳的参数个数{3}。参数设置提前终止！\n"+
                                "set-warnnings:pconn_query_throws-sql:{0},params:{1}",
                            sql,Arrays.asList(params),i,pcnt) );
                         break;
                     }// i>pcnt
                     
                     if(params[i]!=null) try{ // 只有当参数param[i]不为空才给与进行模板参数填充。
                         pstmt.setObject(i+1,params[i]);// 模板参数填充，注意模板参数需要从 1开始。
                     } catch (Exception x) {
                         System.err.println(MFT("set-error:pconn_query_throws-sql:{0},params:{1}",
                         sql,Arrays.asList(params)));
                         throw x;// 一旦出现设置失败，立即异常抛出。因为参数设置师表就表名，该SQL语句执行不了，尽早告知用户程序，给予处理解决，别浪费时间做无用功。
                    }// if params[i]!=null try
                }//for 模板参数遍历。
                
            }else if(pcnt<0){// 非法数值表明getParameterCount无法获得参数
                
                for(int i=0;i<params.length;i++) { // 模板参数遍历。
                    pstmt.setObject(i+1,params[i]);// 模板参数填充，注意模板参数需要从 1开始。
                }//for 模板参数遍历。
                
            }//if pcnt>0 && params!=null
            
            rs=pstmt.executeQuery();// 执行结果查询
            t =  transformer.apply(rs);// 进行目标类型变换。
          }catch(SQLException e){
              System.out.println("pconn_query_throws,error sql:"+sql);
              throw e;// 抛出异常 原因。
          }finally{
              if(pstmt!=null)pstmt.close();
              if(rs!=null)rs.close();
          }// try
          
          // 放回目标结果。
          return t;
      }
      
      /**
       * 提取列标签
       * @param rs　结果集合
       * @return 标签数组
     * @throws SQLException 
       */
      String []labels2(final ResultSet rs) throws SQLException{
          
          String [] aa = null;
          if(rs==null) return null;
          final var rsm = rs.getMetaData();
          final var n = rsm.getColumnCount();
          aa = new String[n];
          for(int i=1;i<=n;i++)aa[i-1]=rsm.getColumnLabel(i);
          
          return aa;
      }
      
      /**
       * 提取列标签:key 从1开始
       * @param rs　结果集合
       * @return 标签的值信息(位置从1开始）{位置：Integer->标签名：String}
       */
      Map<Integer,String>labels(final ResultSet rs){
          
          final var mm  = new HashMap<Integer,String>();
          try {
              if(rs==null)return mm;
              var rsm = rs.getMetaData();
              var n = rsm.getColumnCount();
              for(int i=1;i<=n;i++)mm.put(i, rsm.getColumnLabel(i));
          }catch(Exception e) {
              e.printStackTrace();
          }
          
          return mm;
      }
      
      /**
       * 需要进一步优化设计的函数。 LinkedList<T> ll  干嘛用的？
       * 提供一个数据库连接，来执行callbacks函数
       * @param connection 数据库连接，如果为null,表示自动创建数据库连接。
       * @param callbacks 数据库连接 的操作函数，通过数据库连接生产出目标结果对象 T。
       * @return 执行结果集合的recordList
       * @throws Throwable 
       */
     public<T> T withConnection_throws(final FunctionWithThrowable<Connection,T>callbacks,
            final Connection connection) throws SQLException{
         
         // 返回结果集合:这其实用作一个单数值容器。为什么用List，而不是直接用T类型，我忘记了当时的原因，保留这个问题，想到了再补充。
          final LinkedList<T> ll = new LinkedList<T>();
          var need_close = connection==null;// 外界没有提供数据库连接,则使用自创建的数据库连接。此时使用完毕后，需要给与关闭。否则连接由外部给与关闭。
          var _conn = connection==null?this.getConnection():connection;
          
          try {
              ll.add(callbacks.apply(_conn));
          }catch(Throwable e) {
              throw new SQLException(e);
          }finally{
              if(need_close)_conn.close();
          }
          
          return ll.size()>0?ll.getFirst():null; // 从容其中剔除运算结果。
      }
      
     /**
       * 提供一个数据库连接，来执行callbacks函数
       *@return 执行结果集合的recordList
       */
     public<T> T withConnection(final FunctionWithThrowable<Connection,T>callbacks) {
         
         T res = null;
         try {
            res = withConnection_throws(callbacks,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }// 自动创建数据库连接
         
        return res;
     }
     
     /**
      * prepared Statement 的查询操纵
      * @param sql prepared Statement 
      * @param pp pp 位置参数集合
      * @return 查询结果结合
      */
     public List<IRecord> precords(final String sql, final Object ...pp){
       List<IRecord> ll = null;
       try {ll = this.precords_throws(null,sql,pp);}
       catch(Exception e) {e.printStackTrace();}
       return ll;
     }

     /**
      * prepared Statement 的查询操纵
      * @param sql  prepared Statement 
      * @param pp 位置参数集合 
      * @return 查询结果结合
      */
     public List<IRecord> precords(final Connection connection,final String sql, final Object ...pp){
         
       List<IRecord> ll = null;
       try {ll = this.precords_throws(null,sql,pp); }
       catch(Exception e) {e.printStackTrace();}
       return ll;
     }

   /**
    * p开头的函数：表示prepared sql相关测操作。
    * @param connection 提供的数据库连接，如果为null 则自动创建
    * @param sql sql语句模板
    * @param params sql语句参数  数组 ,可以为空表示没有参数，单此时需要sql中没有?问号占位符，即不做参数设置
    * @return 结果集合
    * @throws SQLException
    */
     public List<IRecord> precords_throws(final Connection connection,
            final String sql,final Object[] params) throws SQLException{
         
          return  this.withConnection_throws(conn->this.pconn_query_throws(conn, rs->{
              var recs = new LinkedList<IRecord>();
              var lbls = labels(rs); int n =lbls.size();// 结果集的列标签(列名）
              while(rs.next()) {
                  var rec = new LinkedRecord();
                  for(int i=1;i<=n;i++)rec.set(lbls.get(i),rs.getObject(i));
                  recs.add(rec);
             }
              return recs;
          }, sql, params),connection);// withConnection
     }
       
   /**
    * p开头的函数：表示prepared sql相关测操作。
    * 专门为了DataMatrix::new 来进行设计的接口
    *
    * @param mxbuilder 数据矩阵的构建器
    * @param sql SQL语句
    * @param pp sql 语句的位置参数：占位符对应的实际数值
    * @param <T> 返回结果的的类型即数据矩阵的类型 mxbuilder 所生成的数据对象
    * @return
    */
    public <T>T pmatrix(final BiFunction<Object[][],String[],T> mxbuilder,
        final String sql,final Object ...pp){
        
       T t= null;
       try {
           t= this.pmatrix_throws(mxbuilder,sql,pp);
       }catch(Exception e) {
           e.printStackTrace();
       }
       return t;
    }
    
    /**
     * p开头的函数：表示prepared sql相关测操作。
     * 专门为了DataMatrix::new 来进行设计的接口
     * @param <T> 返回结果的的类型即数据矩阵的类型 mxbuilder 所生成的数据对象的类型
     * @param mxbuilder 数据矩阵的构建器
     * @param sql 语句模板
     * @param pp sql语句的位置参数：占位符对应的实际数值
     * @return 返回结果的的类型即数据矩阵的类型 mxbuilder 所生成的数据对象
     * @throws SQLException
     */
     public <T>T pmatrix2(final BiFunction<String[][],String[],T> mxbuilder,
         final String sql,final Object ...pp){
         
        T t= null;
        
        final BiFunction<Object[][],String[],T> _mxbuilder = (ooo,hh)->mxbuilder.apply(ooo2sss(ooo),hh);
        try {
            t= this.pmatrix_throws(_mxbuilder,sql,pp);
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        return t;
     }
    
    /**
     * 把一个对象数组转换成一个字符串数组。
     * @param ooo 对象二维数据
     * @return 字符串二维数组
     */
    public static String[][] ooo2sss(final Object[][] ooo){
       final int height = ooo.length;
       final int width = ooo[0].length;
       String[][] sss = new String[height][];
       for(int i=0;i<height;i++) {
           sss[i] = new String[width];
           for(int j=0;j<width;j++)sss[i][j]=ooo[i][j]+"";
       }
       
       return sss;
    }

    /**
    * p开头的函数：表示prepared sql相关测操作。
    * 专门为了DataMatrix::new 来进行设计的接口
    * @param <T> 返回结果的的类型即数据矩阵的类型 mxbuilder 所生成的数据对象的类型
    * @param mxbuilder　数据矩阵的构建器
    * @param sql　语句模板
    * @param pp sql 参数 sql 语句的位置参数：占位符对应的实际数值
    * @return
    * @throws SQLException
    */
    public <T> T pmatrix_throws(final BiFunction<Object[][],String[],T> mxbuilder,
        final String sql, final Object ...pp) throws SQLException{
        
       return  this.withConnection(conn->this.pconn_query_throws(conn, rs->{
           var recs = new ArrayList<Object[]>();
           var lbls = labels2(rs); int n =lbls.length;
           
           while(rs.next()) {  
               var oo =new Object[n];
               for(int i=1;i<=n;i++)oo[i-1]=rs.getObject(i);
               recs.add(oo);
          }
         return mxbuilder.apply(recs.toArray(Object[][]::new),lbls);
       }, sql, pp));// withConnection
    }

      /**
         * 大数据检索,
         * 单次获取数据的数据量 30000
         * @param largesql 大数据sql
         * @return record 数据集合
         */
        public List<IRecord> bigdataQuery(final String largesql){
            return blockquery(largesql,30000);
        }

        /**
         * 分块数据检索
         * @param sql 大数据的sql语句
         * @param fecthsize 单次获取数据的数据量大小
         * @return
         */
        @SuppressWarnings("unchecked")
        public List<IRecord> blockquery(final String sql,final int fecthsize){
            Supplier<String> alias = ()->"table"+UUID.randomUUID().toString().replace("-", "");// 表格别名
            String big_query = "select count(*) cnt from ("+sql+") "+alias.get();
            Optional<IRecord> opt = this.sql2maybe(big_query);
            if(!opt.isPresent())return new LinkedList<>();
            int cnt = opt.get().i4("cnt");// 首先读取记录条目数
            final int BATCH_SIZE = Math.abs(fecthsize)>0?Math.abs(fecthsize):30000;//默认块大小为30000
            List<String> sub_sqls = new ArrayList<>(10);
            if(cnt>BATCH_SIZE) {// 只有超过BATCH_SIZE才给予分割
                for(int i=0;i<cnt;i+=BATCH_SIZE) {
                    int start = i;int end = (i+BATCH_SIZE)-1;//结束标记
                    if(end>cnt)end = cnt;
                    sub_sqls.add("select * from ("+sql+") "+alias.get()+
                        " limit "+start+","+BATCH_SIZE);// 定义子任务处理范围批量大小
                }//for
            }else{//没有超过batchsize不予分割
                sub_sqls.add(sql);
            }//if

            if(System.currentTimeMillis()<0) {// 代码块开启标志
                final List<IRecord> ll = new LinkedList<>();// 结果集列表
                final int n = sub_sqls.size();// 任务数
                final Object aa[] = new Object[n];// 结果集列表
                Semaphore semaphore = new Semaphore(1-n);
                AtomicInteger counter = new AtomicInteger();//计数器
                sub_sqls.forEach(e->{ new Thread(()-> {
                    int i = counter.getAndIncrement();//线程编号,领取一个任务号,然后执行
                    String stmt = sub_sqls.get(i);// 获得待执行的sql语句
                    Thread.currentThread().setName("#bigquery-"+i);
                    long begTime = System.currentTimeMillis();
                    System.out.println("sql 语句验证:["+i+"]"+e.equals(stmt)+"\ne:"+e+"\nstmt:"+stmt+"\n");
                    List<IRecord> recs = this.sql2records(stmt);
                    if(System.currentTimeMillis()<0) synchronized(ll){ll.addAll(recs);}
                    else aa[i]=recs;

                    long endTime = System.currentTimeMillis();
                    System.out.println(""+Thread.currentThread()
                        .getName()+" last for "+(endTime-begTime)+"ms");
                    semaphore.release();
                }).start(); });
                try {semaphore.acquire();}catch(InterruptedException e) {e.printStackTrace();}
                return ll.size()>0?ll:Stream.of(aa).map(e->(List<IRecord>)e)
                    .flatMap(e->e.stream()).collect(Collectors.toList());
            }else{
                return sub_sqls.parallelStream()// 并行各个子任务
                    .flatMap(e->this.sql2records(e).stream())// 分组获取数据集
                    .collect(Collectors.toList());// 采用jdk的流式实现
            }//if
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         *
         * @return 执行结果集合的recordList
         */
        public List<IColumn<?>> sql2ll(final String sql) {
            return Arrays.asList(sql2cols(sql,this.getConnection(),true));
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         *
         * @return 执行结果集合的recordList
         */
        public IColumn<?>[] sql2cols(final String sql) {
            return sql2cols(sql,this.getConnection(),true);
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param con 數據庫連接
         * @param close 是否關閉連接
         *
         * @return 执行结果集合的recordList
         */
        public IColumn<?>[] sql2cols(final String sql,final Connection con,final boolean close) {

            return psql2apply(sql,null,con,close,SQL_MODE.QUERY,(conn,stmt,rs,rsm,n)->{
                final Column<?>[] finalcc = new Column<?>[n];// 初始化空間
                try {
                    // 获得列名
                    Function<Integer,KVPair<String,Integer>> colname = (i)->{
                        String name = null;
                        try {name = rsm.getColumnLabel(i);}
                        catch(Exception e) {}
                        return new KVPair<String,Integer>(name,i);
                    };

                    //构造表头
                    Stream.iterate(1,i->i+1).limit(n).map(colname)
                        .forEach((kv)->{
                            Column<?> c = new Column<>(kv.key());
                            finalcc[kv.value()-1] = c;
                        });

                    //遍歷數據
                    while (rs.next()) {
                        for(int i=0;i<n;i++) {
                            Object obj = rs.getObject(i+1);
                            if(finalcc[i].getType()==null && obj!=null)
                                finalcc[i].setType(obj.getClass());
                            finalcc[i].addObject(obj);
                        }//for
                    }//while

                }catch(Exception e) {
                    e.printStackTrace();
                }
                return finalcc;
            });
        }

        /**
         * 查询结果集合
         * 连接使用完后自动关闭
         * @param sql 查询语句
         * @return 单挑数据的结果集
         */
        public Optional<IRecord> sql2maybe(final String sql){
            List<IRecord> recs = this.sql2records(sql);
            if(recs.size()>1)System.out.println(sql+"\n返回多条("+recs.size()+")数据，仅截取第一条返回！");
            return recs.stream().findFirst();
        }
        
        /**
         * 查询结果集合
         * 连接使用完后自动关闭
         * @param sql 查询语句
         * @return 单挑数据的结果集
         */
        public <T> Optional<T> sql2maybe(final String sql,Class<T>tclazz){
            return sql2maybe(sql).map(e->IRecord.rec2obj(e,tclazz));
        }
        
        /**
         * 查询结果集合
         * 连接使用完后自动关闭
         * @param sql 查询语句
         * @param tclazz 目标结果类型
         * @return 单挑数据的结果集
         */
        public <T> T sql2get(final String sql,Class<T>tclazz){
            return sql2maybe(sql).map(e->IRecord.rec2obj(e,tclazz)).get();
        }

        /**
         * 查询结果集合
         * 连接使用完后自动关闭
         * @param sql 查询语句
         * @param mode sql 模式查询还是更新
         * @return 单条数据的结果集
         */
        public Optional<IRecord> sql2maybe(final String sql,final SQL_MODE mode){
            List<IRecord> recs = this.sql2records(sql,mode);
            if(recs.size()>1)System.out.println(sql+"\n返回多条("+recs.size()+")数据，仅截取第一条返回！");
            return recs.stream().findFirst();
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * 连接使用完后自动关闭
         * @param sql 查询语句
         * @return 执行结果集合的recordList
         */
        public List<IRecord> sql2records(final String sql) {
            return this.psql2records(sql,null,this.getConnection(), true,SQL_MODE.QUERY,null,Optional.empty());
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param mode 结果获取模式，QUERY，UPDATE
         * @return 执行结果集合的recordList
         */
        public List<IRecord> sql2records(final String sql,final SQL_MODE mode) {
            return this.psql2records(sql,null,this.getConnection(), true,mode,null,Optional.empty());
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel,连接不予关闭
         * @param sql 查询语句
         * @param mode 结果获取模式，QUERY，UPDATE
         * @return 执行结果集合的recordList
         */
        public List<IRecord> sql2records(final String sql,final Connection conn,final SQL_MODE mode) {
            if(conn==null)return new LinkedList<>();
            return this.psql2records(sql,null,conn, false,mode,null,Optional.empty());
        }

        /**
         * 返回经过变换之后的数据记录
         * @param sql 查询语句
         * @param mapper 结果变换函数 IRecord->T
         *
         * @return 执行结果集合的List<T>
         */
        public<T> List<T> sqlmutate(final String sql,final Function<IRecord,T>mapper) {
            return this.psql2records(sql,null,this.getConnection(), true,SQL_MODE.QUERY,null,Optional.empty())
                .stream().map(mapper).collect(Collectors.toList());
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param jsn2keys json keys
         *
         * @return 执行结果集合的recordList
         */
        public List<IRecord> sql2records(final String sql,final Map<String,String[]> jsn2keys) {
            return this.psql2records(sql,null,this.getConnection(), true,
                SQL_MODE.QUERY,null,Optional.of(jsn2keys));
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param jsn2keysrec json keys 的键值集合
         *
         * @return 执行结果集合的recordList
         */
        public List<IRecord> sql2records(final String sql,final IRecord jsn2keysrec) {
            return this.sql2records(sql,null,jsn2keysrec);
        }
        
        /**
        * 返回的结果集：就是一个中的key值为列名：ColumnLabel
        * @param sql 查询语句
        * @param jsn2keysrec json keys 的键值集合
        *
        * @return 执行结果集合的recordList
        */
       public List<IRecord> sql2records(final String sql,final Map<Integer,Object>params,
               final IRecord jsn2keysrec) {
           
           final Map<String,String[]> jsn2keys = new HashMap<>();
           jsn2keysrec.stream().forEach(kvp->{
               Object obj = kvp._2();
               String[] oo = null;
               if (obj instanceof String ) {
                   oo = (obj+"").split("[,]+");
               }else if(obj.getClass().isArray()){
                   try {oo = (String[])obj;}
                   catch(Exception e) {return;} // 转换异常忽略该项目
               }else {
                   return;// 忽略该项目
               }
               jsn2keys.put(kvp.key(), oo);
           });
           return this.psql2records(sql,params,this.getConnection(), true,
                  SQL_MODE.QUERY,null,Optional.of(jsn2keys));
       }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param close 是否關閉連接
         *
         * @return 执行结果集合的recordList
         */
        public List<IRecord> sql2records(final String sql,final boolean close) {
            return this.psql2records(sql,null,this.getConnection(), close,
                SQL_MODE.QUERY,null,Optional.empty());
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param rec prepared statement 的位置参数 integer->value ,integer 从1开始
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(String sql,IRecord rec) {
            final Map<Integer,Object> params = new HashMap<>();
            rec.foreach((k,v)->{// 位置解析
                Integer key = Integer.parseInt(k);
                params.put(key, v);
            });// 数据转换
            return this.psql2records(sql,params,this.getConnection(), true,
                SQL_MODE.QUERY,null,Optional.empty());
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param oo prepared statement 的位置参数 序列
         * @return 执行结果集合的recordList
         */
        public Optional<IRecord> psql2maybe(String sql,Object[] oo) {
           return psql2records(sql,oo).stream().findFirst();
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param params prepared statement 的位置参数 integer->value ,integer 从1开始
         * 参数序号从1开始。1->xxx,2->yyy..
         * @return 执行结果集合的recordList
         */
        public Optional<IRecord> psql2maybe(String sql,IRecord params) {
           return psql2records(sql,params).stream().findFirst();
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param params prepared statement 的位置参数 integer->value ,integer 从1开始
         * @return 执行结果集合的recordList
         */
        public Optional<IRecord> psql2maybe(String sql,Map<String,Object> params) {
           return psql2records(sql,REC(params)).stream().findFirst();
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param params prepared statement 的位置参数 integer->value ,integer 从1开始
         * @param targetClass 期待的结果类型的class
         * @param <T> 期待的结果类型的
         * @return 执行结果集合的recordList
         */
        public <T> Optional<T> psql2maybe(String sql,Map<String,Object> params,Class<T> targetClass) {
           return psql2records(sql,REC(params)).stream().findFirst().map(e->IRecord.rec2obj(e,targetClass));
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param oo prepared statement 的位置参数数组
         * @param targetClass 期待的结果类型的class
         * @param <T> 期待的结果类型的
         * @return 执行结果集合的recordList
         */
        public <T> Optional<T> psql2maybe(String sql,Object[] oo,Class<T> targetClass) {
           return psql2records(sql,oo).stream().findFirst().map(e->IRecord.rec2obj(e,targetClass));
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param oo prepared statement 的位置参数 序列
         * @return 执行结果集合的recordList
         */
        public <T> T psql2get(String sql,Object[] oo,Class<T> targetClass) {
           return psql2records(sql,oo).stream().findFirst().map(e->IRecord.rec2obj(e,targetClass)).get();
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param params prepared statement的位置参数 序列: 参数序号从1开始。1->xxx,2->yyy..
         * @return 执行结果集合的recordList
         */
        public <T> T psql2get(String sql,IRecord params,Class<T> targetClass) {
           return psql2records(sql,params).stream().findFirst().map(e->IRecord.rec2obj(e,targetClass)).get();
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param oo  参数列表
         * @return 执行结果集合的recordList
         */
        public <T> List<IRecord> psql2records(String sql,List<T> oo) {
            return psql2records(sql,oo.toArray());
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param oo  参数数组
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Object [] oo) {
            final Map<Integer,Object> params = new HashMap<>();
            if(oo!=null)for(int i=0;i<oo.length;i++)params.put(i+1,oo[i]);
            return this.psql2records(sql,params,this.getConnection(), true,
                SQL_MODE.QUERY,null,Optional.empty());
        }
        
        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param params  sql中的占位符多对应的实际数据，即占位符参数
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params) {
            return this.psql2records(sql,params,this.getConnection(), true,
                SQL_MODE.QUERY,null,Optional.empty());
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param recSupplier 行记录的生成器 即提供一种自定义的结果集的行实现
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params,
            final Supplier<IRecord> recSupplier) {
            return this.psql2records(sql,params,this.getConnection(), true,
                SQL_MODE.QUERY,recSupplier,Optional.empty());
        }

        /**
         * 返回的结果集：就是一个中的key值为列名：ColumnLabel
         * @param sql 查询语句
         * @param close 是否關閉連接
         * @param recSupplier 行记录的生成器 即提供一种自定义的结果集的行实现
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params,
            final boolean close,final Supplier<IRecord> recSupplier) {
            return this.psql2records(sql,params,this.getConnection(), close,
                SQL_MODE.QUERY, recSupplier,Optional.empty());
        }

        /**
         * 返回的结果集(不会返回空值,null,失败也返回长度为0的list）
         * @param sql 查询语句
         * @param conn 數據庫連接
         * @param close 是否關閉連接
         *
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params,
            final Connection conn,final boolean close){
            return this.psql2records(sql,params,conn,close,SQL_MODE.QUERY,null,Optional.empty());
        }

        /**
         * 返回的结果集(不会返回空值,null,失败也返回长度为0的list）
         * @param sql 查询语句
         * @param con 數據庫連接
         * @param close 是否關閉連接
         * @param mode 结果获取模式 QUERY or UDATE
         *
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params,Connection con,boolean close,SQL_MODE mode){
            return this.psql2records(sql,params,con,close,mode,null,Optional.empty());
        }

        /**
         * 返回的结果集(不会返回空值,null,失败也返回长度为0的list）
         * @param sql 查询语句
         * @param close 是否關閉連接
         * @param recSupplier 行记录的数据格式
         *
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params,Connection con,boolean close,
             final Supplier<IRecord> recSupplier){
            return this.psql2records(sql,params,con,close,SQL_MODE.QUERY,recSupplier,Optional.empty());
        }

        /**
         * 数据烹饪大师
         * @author gbench
         *
         */
        static interface RecordCooker{
            void initialize(int n,ResultSet rs,ResultSetMetaData rsm);//准备一下,为cook做好必要的数据结构
            IRecord cook(IRecord rec) throws SQLException;// 开始工作
        }
        
        /**
         * 带有抛出异常的函数
         * @author xuqinghua
         *
         * @param <T>　参数类型
         * @param <U>　返回类型
         */
        public interface FunctionWithSQLException<T,U>{
            public U apply(T t) throws SQLException;
        }

        /**
         * 带有抛出异常的函数
         * @author xuqinghua
         *
         * @param <T>　参数类型
         * @param <U>　返回类型
         */
        public interface FunctionWithThrowable<T,U>{
            public U apply(T t) throws Throwable;
        }
        
        /**
         * 带有抛出异常的函数
         * @author xuqinghua
         *
         * @param <T>　参数类型
         * @param <U>　返回类型
         */
        public interface FunctionWithException<T,U>{
            public U apply(T t) throws Exception;
        }


        /**
         * 返回的结果集(不会返回空值,null,失败也返回长度为0的list）
         * @param sql 查询语句
         * @param params sql语句中的位置参数
         * @param con 數據庫連接
         * @param close 是否關閉連接
         * @param mode sql语句的执行模式查询还是更新
         * @param recSupplier 行记录的数据格式
         * @param jsncol2keys 把json列按照指定的序列结构给与展开，即一jsn列表换成多列(keys)
         * jsncol2keys是一个 {json列名->展开序列keys}的结构的Map
         *
         * @return 执行结果集合的recordList
         */
        public List<IRecord> psql2records(final String sql,final Map<Integer,Object>params,
            final Connection con,boolean close,SQL_MODE mode,
            final Supplier<IRecord> recSupplier,Optional<Map<String,String[]>> jsncol2keys){
            List<IRecord> ll = new LinkedList<>();
            try {
                ll = this.psql2records_throws(sql, params,con, close, mode, recSupplier, jsncol2keys);
            }catch(Exception e) {
                e.printStackTrace();
            }
            return ll;
        }
        
        /**
         * 返回的结果集(不会返回空值,null,失败也返回长度为0的list）　该方法依赖于：psql2apply_throws
         * @param sql 查询语句
         * @param params 语句参数
         * @param con 數據庫連接
         * @param close 是否關閉連接
         * @param mode sql语句的执行模式查询还是更新
         * @param recSupplier 行记录的数据格式
         * @param _jsncol2keys 把json列按照指定的序列结构给与展开，即一jsn列表换成多列(keys)
         * _jsncol2keys是一个 {json列名->展开序列keys}的结构的Map　:　比如｛product->[id,name,description]｝
         *
         * @return 执行结果集合的recordList,返回结果不为null,可以是一个没有数据的List
         */
        public List<IRecord> psql2records_throws(final String sql,final Map<Integer,Object>params,
            final Connection con,final boolean close,final SQL_MODE mode,
            final Supplier<IRecord> recSupplier,Optional<Map<String,String[]>> _jsncol2keys) 
            throws SQLException {
            
            //System.out.println(sql);
            final Optional<Map<String,String[]>> jsncol2keys = (_jsncol2keys==null)
                ? Optional.empty() : _jsncol2keys;//保证jsncol2keys结构非空
            final Optional<Map<String,String[]>> finaljsncol2keys =
                jsncol2keys==null?Optional.empty():jsncol2keys;
            final IRecord proto = new LinkedRecord();;// 采用原型法进行记录创建
            final IRecord finalProto=proto;// 转换成 final 类型，java的lambda的要求，这个有点恶心，哈哈！
            final Supplier<IRecord> recsup = (null==recSupplier)?
                ()->finalProto.duplicate():recSupplier;// 构造默认的记录结构生成器

            //烹饪 数据的方法，老子道德经曰:治大国，若烹小鲜, Json字段的记录生成器
            final RecordCooker jsnCooker = new RecordCooker(){
                int n; ResultSet rs; ResultSetMetaData rsm;
                ArrayList<String> labels = null;// 列的显示名(label),name 是实际名
                ArrayList<String[]> jks = null;//json 的列名:展开字段序列，索引从０开始，０,1,2 依次对应　第一，第二，第三等．
                
                // 把rec(含有复合字段：jsn的多key字段）cook(转换)成 扁平的结构
                FunctionWithSQLException<IRecord,IRecord> lambda_cook = rec->{// 莫认为需要展含有展开列名即jks 不是全部为null
                   for (int i = 0; i < n; i++) {// 逐列添加数据
                       final var label = labels.get(i);// 列名
                       final String[] seqkeys = jks.get(i);// 对应于第i列（从０开始）的json keys,seqkeys表述key的序列，受到scala影响的命名．
                       Object value = rs.getObject(i + 1);// 列值
                       if (seqkeys != null) {// 需要对jsn字段给与展开,剪开的键名序列不为空
                           if (value == null) value = "{}";// 空对象，保证value有效
                           @SuppressWarnings("unchecked")
                           Map<String, Object> jsnmap = Json.json2obj(value, Map.class);// json 展开成关联数组 Map
                           if (jsnmap == null) {// 默认值的字段填充
                               jsnmap = new HashMap<String, Object>();
                               for (var key : seqkeys)rec.add(key, null);//为了保证key名称存在，不过对于Map结构的rec，这是无效的．
                           } // if jsnmap==null
                           // 提取jsnmap中的seqkeys中的键名数据，并把他们置如结果记录里面去．
                           new LinkedRecord(seqkeys, jsnmap).forEach((k, v) -> rec.add(k, v));
                       } else {// 不需要对jsn列进行展开
                           rec.add(label, value);
                       } // if 逐列添加数据
                   }/*for*/
                   return rec;
                };// lambda_cook
                
                //n:结果集合的列数量;rs:查询的结果集,rsm:结果集合的字段描述
                public void initialize(int n, ResultSet rs, ResultSetMetaData rsm) {
                    this.n = n;this.rs = rs;this.rsm = rsm;
                    labels = new ArrayList<>(n);// 便签集合
                    jks = new ArrayList<>(n);// json key 集合
                    for (int i = 0; i < n; i++) {
                        String label = null;
                        try {label = this.rsm.getColumnLabel(i + 1);}catch(Exception e) {}
                        labels.add(label);
                        String[] cc = finaljsncol2keys.isPresent() ? finaljsncol2keys.get().get(label) : null;//展开json中的复合列
                        jks.add((cc != null && cc.length > 0) ? cc : null); // 展开键值序列：cc就是展开之后扁平列名集合
                    } // for i
                    // 根据是否含有json字段判断是否开启
                    if(finaljsncol2keys.isEmpty())lambda_cook= rec->
                        {for(int i=0;i<n;i++)rec.add(labels.get(i),rs.getObject(i+1));return rec;};
                }// initialize
                
               // 把rec(含有复合字段：jsn的多key字段）cook(转换成)扁平的结构
                public IRecord cook(IRecord rec) throws SQLException {// 数据烹饪方法：把一个空白记录，赋予内容
                    return lambda_cook.apply(rec);
                };//cooklambda_cook
            };//jsnCooker

            //  简单记录生成器:这个记录生成器已经随着JsonCooker lamba_cook变得意义不大了．但我还是用他，因为简单．
            final RecordCooker simpleCooker = new RecordCooker() {
                int n; ResultSet rs; ResultSetMetaData rsm;
                public void initialize(int n, ResultSet rs, ResultSetMetaData rsm) {// cooker 的基本准备
                    this.n = n;this.rs = rs;this.rsm = rsm;}
                public IRecord cook(IRecord rec) throws SQLException {// 数据烹饪方法：把一个空白记录，赋予内容
                    for (int i = 0; i < n; i++) {rec.add(rsm.getColumnLabel(i + 1), rs.getObject(i + 1));}
                    return rec;
                }//cook
            };//simpleCooker

            final RecordCooker cooker = jsncol2keys.isPresent()?jsnCooker:simpleCooker;//选择一个合适的厨师
            //final RecordCooker cooker = jsnCooker;
            
            //代码在这里正是开始，以上都是准备工作．准备活动要有条不紊，可以慢（此处的慢是缜密与完备）但不能乱．行动之时要快如闪电．
            final List<IRecord> ll = psql2apply_throws(sql,params,con,close,mode,(conn,stmt,rs,rsm,n)->{//结果集合的生成．
                final var recs = new LinkedList<IRecord>();
                cooker.initialize(n,rs, rsm);//在烹饪之前准备一下，cooker  准备．．．，Ｇo! Go!! Go!!!
                while(rs.next())recs.add(cooker.cook(recsup.get()));//數據遍历,把rec煮熟一下再放入结果集recs中
                return recs;
            });// sqlquery 查詢結果集

            return ll==null?new LinkedList<>():ll;//返回结果不为null,
        }

        private Supplier<Connection> supplierConn=null;// 数据连接贩卖商
        public static boolean debug = false;// 调试标记
    }

    /**
     * 二元组(t,u)
     * @author gbench
     *
     * @param <T> 1号位置元素类型
     * @param <U> 2号位置元素类型
     */
    public static class Tuple2<T,U> implements Serializable{
        private static final long serialVersionUID = -9103479443503056191L;
        
        /**
         * 
         * @param <M> 1号位置的元素类型
         * @param <N> 2号位置的元素类型
         * @param m 1号位置的元素的值
         * @param n 2号位置的元素的值
         * @return (m,n) 的二元组
         */
        public static <M,N> Tuple2<M,N> TUP2(M m,N n){ return new Tuple2<M,N>(m,n);}
        
        /**
         * 默认构造函数
         */
        public Tuple2(){}
        
        /**
         * 构造函数
         * @param t 1号位置的元素
         * @param u 2号位置的严肃
         */
        public Tuple2(T t,U u){this._1 = t;this._2 = u;}
        
        /**
         * 1号位置的元素值
         * @return 1号位置的元素值
         */
        public T _1() {return _1;};
        
        /**
         * 设置1号位置的元素的值
         * @param t 待设置的值
         * @return 设置后的值
         */
        public T _1(T t) {return _1=t;};
        
        
        /**
         * 2号位置的元素值
         * @return 1号位置的元素值
         */
        public U _2() {return _2;};
        
        /**
         * 设置2号位置的元素的值
         * @param  u 待设置的值
         * @return 设置后的值
         */
        public U _2(U u) {return _2=u;};
        
        private T _1; // 1号位置元素
        private U _2; // 2号位置元素
        
        /**
         * 交换1号与二号位置 (t,u)->(u,t)
         * @return (u,t)
         */
        public Tuple2<U, T> swap() {
            return TUP2(this._2, this._1);
        };

        /**
         * hashCode
         */
        public int hashCode() {
            return _1 == null ? Integer.valueOf(0).hashCode() : this._1().hashCode();
        }

        /**
         * equals
         */
        public boolean equals(Object obj) {
            if(obj instanceof Tuple2) {// 同类比较
                final var tup2 = (Tuple2<?,?>)obj;
                if(_1==null&&tup2._1==null) {
                    if(_2==null&&tup2._2==null)return true;
                    else return false;
                }
                if(_1==null) return false;
                if(_2==null) return false;
                return _1.equals(tup2._1) && _2.equals(tup2._2);
            }
            return _1 == null ? false : _1().equals(obj);
        }

        /**
         * 格式化输出
         */
        public String toString() {
            return _1() + " --> " + _2();
        }

        /**
         * 使用自身作为1位元素进行 zip
         * 
         * @param <V> 新2号位置的元素的类型
         * @param vv  新2号位置元素
         * @return {((T,U),V)} 结构的数组
         */
        public <V> List<Tuple2<Tuple2<T, U>, V>> lzip(Collection<V> vv) {
            return zip(Arrays.asList(this), vv, true);
        }
        
        /**
         * 2号位zip
         * 使用自身作为1位元素进行 zip
         * 
         * @param <V> 新2号位置的元素的类型
         * @param vv  新2号位置元素
         * @return {(T,(U,V))} 结构的数组
         */
        public <V> List<Tuple2<T,Tuple2<U,V>>> lzip0(Collection<V> vv) {
            final var aa = zip(Arrays.asList(this._2),vv,true);
            return zip(Arrays.asList(this._1),aa,true);
        }

        /**
         * 批量设置1号位置
         * @param <V> 1号位置元素
         * @param vv 1号位置元素集合
         * @return {(v,u)}
         */
        public <V> List<Tuple2<V, U>> multiSet1(Collection<V> vv) {
            return zip(vv,Arrays.asList(this._2), true);
        }
        
        /**
         * 批量设置2号位置
         * @param <V> 二号位置元素
         * @param vv 二号位置元素集合
         * @return {(t,v)}
         */
        public <V> List<Tuple2<T, V>> multiSet2(Collection<V> vv) {
            return zip(Arrays.asList(this._1),vv, true);
        }

        /**
         * 使用自身作为2位元素进行 zip
         * 
         * @param <V> 新1位的元素的类型
         * @param vv  新1位元素集合
         * @return {(V,(T,U))} 结构的数组
         */
        public <V> List<Tuple2<V, Tuple2<T, U>>> rzip(Collection<V> vv) {
            return zip(vv, Arrays.asList(this), true);
        }
        
        /**
         * 2号位zip
         * 使用自身作为1位元素进行 zip
         * 
         * @param <V> 新2号位置的元素的类型
         * @param vv  新2号位置元素
         * @return {(U,(T,V)} 结构的数组
         */
        public <V> List<Tuple2<U,Tuple2<T,V>>> rzip0(Collection<V> vv) {
            final var m = this.swap().lzip0(vv);
            return m;
        }
        
        /**
         * 把1号二号元素封装成Object的列表元素
         * @return Object的列表元素[t,u],1号原物对应list的第0个元素，2号原物对应list的第1个元素，
         */
        public List<Object> oo() {
            return Arrays.asList(this._1,this._2);
        }
        
        /**
         * 强制转换成T类型的数组元素
         * 把1号二号元素封装成Object的列表元素
         * @return Object的列表元素[t,u],1号原物对应list的第0个元素，2号原物对应list的第1个元素，
         */
        @SuppressWarnings({ "hiding", "unchecked" })
        public <T> List<T> tt() {
            return Arrays.asList((T)this._1,(T)this._2);
        }

        /**
         * 把一个字符串str,视作一个由separator分隔出来的列表。把第一项提取在tuple第一项,其余放在tuple第二项 比如 abc/efg/hij
         * 分解成 abc --> efg/hij
         * 
         * @param str       检测的字符串
         * @param separator 分割符号
         * @return
         */
        public static Tuple2<String, String> TUPLE2(String str, String separator) {
            int p = str.indexOf(separator);
            Tuple2<String, String> tup = new Tuple2<>();
            if (p < 0) {
                tup._1(str);
            } else {
                tup._1(str.substring(0, p));
                if (p + 1 < str.length())
                    tup._2(str.substring(p + 1));
            } // if
            return tup;
        }

        /**
         * 拼接成一个键值对
         * 
         * @param <K> 1号位置元素类型
         * @param <V> 2好位置元素类型
         * @param kk  1号位置元素集合
         * @param vv  2号位置元素结合
         * @param recycle   是否进行循环补位
         * @return [[k1,v1],[k2,v2],...] 的二元组集合。
         */
        public static <K, V> List<Tuple2<K, V>> zip(Collection<K> kk, Collection<V> vv, boolean recycle) {
            final var tups = new LinkedList<Tuple2<K, V>>();
            final var kitr = kk.iterator();
            final var vitr = vv.iterator();
            K pre_k = null;
            V pre_v = null;
            while (kitr.hasNext() || vitr.hasNext()) {
                var k = kitr.hasNext() ? kitr.next() : null;
                var v = vitr.hasNext() ? vitr.next() : null;
                if (recycle && k == null)
                    k = pre_k;
                if (recycle && v == null)
                    v = pre_v;
                tups.add(TUP2(k, v));
                pre_k = k;
                pre_v = v;
            } // while

            return tups;
        }
        
        /**
         * 拼接成一个键值对
         * 
         * @param <K> 1号位置元素类型
         * @param <V> 2好位置元素类型
         * @param kk  1号位置元素集合数组
         * @param vv  2号位置元素结合数组
         * @param recycle   是否进行循环补位
         * @return [[k1,v1],[k2,v2],...] 的二元组集合。
         */
        public static <K, V> List<Tuple2<K, V>> zip(K[] kk, V[] vv, boolean recycle){
            return zip(Arrays.asList(kk),Arrays.asList(vv),recycle);
        }
        
        /**
         * 拼接成一个键值对
         * recycle 默认循环补位
         * 
         * @param <K> 1号位置元素类型
         * @param <V> 2好位置元素类型
         * @param kk  1号位置元素集合数组
         * @param vv  2号位置元素结合数组
         * @return [[k1,v1],[k2,v2],...] 的二元组集合。
         */
        public static <K, V> List<Tuple2<K, V>> zip(K[] kk, V[] vv){
            return zip(Arrays.asList(kk),Arrays.asList(vv),true);
        }
    }


    /**
     * 二元组
     * @author gbench
     *
     * @param <T>
     * @param <U>
     */
    public static class Tuple3<T,U,V>{
        public Tuple3(){}
        public Tuple3(T t,U u,V v){this._1 = t;this._2 = u;this._3=v;}
        public T _1() {return _1;};
        public T _1(T t) {return _1=t;};
        public U _2() {return _2;};
        public U _2(U u) {return _2=u;};
        public V _3() {return _3;};
        public V _3(V v) {return _3=v;};

        public Optional<V> __3() {return Optional.ofNullable(_3);};

        private T _1;
        private U _2;
        private V _3;
        public int hashCode(){ return _1==null? Integer.valueOf(0).hashCode(): this._1().hashCode();}
        public boolean equals(Object obj){return _1==null?false: _1().equals(obj);}
        public String toString() {return _1()+" --> "+_2()+" --> "+_3();}
        
        public static <T1,T2,T3> Tuple3<T1,T2,T3> TUP3(T1 _1, T2 _2, T3 _3){ return new Tuple3<T1,T2,T3>(_1,_2,_3);};
    }

    /**
     * 键值对儿
     * @author gbench
     *
     * @param <K> 键名类型
     * @param <V> 值类型
     */
    public static class KVPair<K,V> extends Tuple2<K,V>{
        
        /**
         * 键值对儿
         * @param key 键名
         * @param value 键值
         */
        public KVPair(K key,V value) {super(key,value);}
        
        /**
         * 获取键名
         * @return 键名
         */
        public K key() {return this._1();}
        
        /**
         * 设置键名
         * @param k 键名的值
         * @return 设置后的键名的值
         */
        public K key(K k) {return this._1(k);}
        
        /**
         * 获取当前的值
         * @return
         */
        public V value() {return this._2();}
        
        /**
         * 设置当前的值为v
         * @param v 值
         * @return 设置后的值
         */
        public V value(V v) {return this._2(v);}
        
        /**
         * 格式化输出
         */
        public String toString() {
            return MFT("({0}:{1})",_1(),_2()) ;
        }
        
        /**
         * 创建一个键值对儿
         * @param <K1> 键类型
         * @param <V1> 值类型
         * @param k1 键名
         * @param v1 键值
         * @return 键值对儿(k1,v1)
         */
        public static <K1,V1> KVPair<K1,V1> KVP(K1 k1,V1 v1){
            return new KVPair<>(k1,v1);
        }
        
        private static final long serialVersionUID = 1110300882502209203L;
    }

    /**
     *
     * @author gbench
     *
     * @param <V>
     */
    public static interface IColumn<V>{
        /**
         * 獲得第個元素
         * @param i 元素編號，從0開始
         * @return 元素内容
         */
        V get( int i);

        /**
         * 添加一個元素
         * @param v
         */
        public void add(V v);

        /**
         * 添加一個元素
         * @param v
         */
        public void addObject(Object v);

        /**
         * 獲得初始類型
         * @return
         */
        public Class<V> getType();
    }
    
    /**
     * 表达式计算器
     * @author gbench
     *
     */
    public static class ExpressionEvaluator{
        
        /**
         * 表达式计算
         * @param line 表达式的行
         * @return 表达式计算的结果
         */
        public Object eval(String line){
            try {
                return engine.eval(line);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        /**
         * 计算表达式的数值
         * @param expression 机械
         * @return 双精度
         */
        public Double eval2dbl(String expression) {
            if(expression==null)return null;
            final var result = eval(expression);
        
            return result==null ?null :Double.valueOf(result.toString());
        }
        
        private ScriptEngineManager manager = new ScriptEngineManager();
        private ScriptEngine engine = manager.getEngineByName("javascript");
    }

    /**
     * 代表一个列数据 其实就是 键值对儿,差别就是这个键值是一个列表数据。
     * @author gbench
     *
     * @param <V> 列中值的类型
     */
    public static class Column<V> extends KVPair<String,List<V>>
            implements IColumn<V>{
        private static final long serialVersionUID = 5783906325810918364L;
        final static int INIT_LIST_SIZE = 100;

        public Column(String name) {
            super(name,new ArrayList<V>(INIT_LIST_SIZE));
            this.clazz = null;
        }

        public Column(String name, Class<V> clazz) {
            super(name,new ArrayList<V>(INIT_LIST_SIZE));
            this.clazz = clazz;
        }

        public V get( int i) {
            return this.value().get(i);
        }

        public void add(V v) {
            this.value().add(v);
        }

        public Class<V> getType(){
            return this.clazz;
        }

        @SuppressWarnings("unchecked")
        public void setType(Class<?> cls) {
            clazz = (Class<V>) cls;
        }

        /**
         * 添加一個元素
         * @param v
         */
        @SuppressWarnings("unchecked")
        public void addObject(Object v) {
            this.add((V)v);
        }

        /**
         * 格式化數據
         */
        public String toString() {
            String type = this.getType()==null?"unknown":this.getType()
                    .getSimpleName();// 類型名
            return this.key()+"("+type+")"+" -> "+
                    this.value();
        }

        @SuppressWarnings("unchecked")
        public V[] toArray() {
            List<V> vv = this.value();
            if(vv==null)return  null;
            return (V[])vv.toArray();
        }

        private Class<V> clazz = null; // 初始類型
    }

    /**
     * IRecord的序列化方法
     * @author gbench
     *
     */
    public static class IRecordSerializer extends StdSerializer<IRecord>{

        private static final long serialVersionUID = -6713069486531158400L;

        /**
         * 序列化
         */
        protected IRecordSerializer() {
            super(IRecord.class);
        }

        @Override
        public void serializeWithType(IRecord value, JsonGenerator generator ,SerializerProvider provider,
                                      TypeSerializer typeSer) throws IOException {
            this.serialize(value, generator, provider);
        }

        @Override
        public void serialize(IRecord value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            for(KVPair<String,Object> kvp:value.kvs()) generator.writeObjectField(kvp.key(),kvp.value());
            generator.writeEndObject();
        }

    }

    /**
     *
     * @author gbench
     *
     */
    public static class IRecordDeserializer extends StdDeserializer<IRecord>{

        private static final long serialVersionUID = 637227298143614828L;

        /**
         *
         */
        protected IRecordDeserializer() {
            super(IRecord.class);
        }

        /**
         * 单层节点转换的ObjectNode变为IRecord
         * @param node
         * @return
         */
        public static IRecord objnode2rec(ObjectNode node) {
            Map<String,Object> mm = new LinkedHashMap<>();
            node.fieldNames().forEachRemaining(name->mm.put(name, node.get(name)));
            //System.out.println(mm);
            return new LinkedRecord(mm);
        }
        
        /**
         * 单层节点转换的ObjectNode变为IRecord
        * @param node 节点
        * @return
        */
       public static Function<ObjectNode,IRecord> node2rec=(node) -> {
           return objnode2rec(node);
       };

        @Override
        public IRecord deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
            
            JsonNode node = jp.getCodec().readTree(jp);
            Map<String,Object> mm = new LinkedHashMap<>();
            node.fieldNames().forEachRemaining(name->{
                JsonNode jsn = node.get(name);
                if(jsn.isObject()) {
                    mm.put(name, objnode2rec((ObjectNode)node.get(name)));
                }else {
                    Object value = jsn;
                    if(jsn.isTextual()) {value = jsn.asText();} else if (jsn.isDouble()){value = jsn.asDouble();}
                    else if (jsn.isInt()){value = jsn.asInt();} else if (jsn.isBoolean()){value = jsn.asBoolean();}
                    else if (jsn.isFloat()){value = jsn.asDouble();} else if (jsn.isLong()){value = jsn.asLong();}//if
                    mm.put(name, value);
                }//if
            });
            return new LinkedRecord(mm);
        }
    }

    /**
     * IRecord 记录结构：<br>
     * IRecord 是键值对儿(Key Value Pairs KVPs)的集合 (kvs)。其基本单元就是一个键值对儿KVP,有时候简写乘 pair或p。<br>
     * KVP 是一个(String,Object) 的二元组，需要对于一个kvp的value他是可以为任何类型的。因此也是可以为IRecord的类型， <br>
     * 所以IRecord是一个可以进行递归定义的数据结构。由于IRecord的设计思想来源于LISP的列表。因此他具有丰富的表达能力。可以表达 <br>
     * 列表,树,图 等基础结构。因此可以很容易拼装成其他的数据结构。是一种即具有原子性 又具有集合性的 数据结构。非常适合表达动态定义的概念
     * 同时对静态概念也又很好的变换方法 比如:<br>
     * muate,toTarget(有损变换),OBJINIT,OBJ2REC,,OBJ2KVS,P(点数据)等方法，可以很方便的在动态概念与静态概念的之间的转换。<br>
     * 静态概念：结构固定的数据结构，即一旦概念确定，结构的数据成员就拥有了确定类型和数量的概念。 与静态概念相对应 成员的类型与数量不确定的数据的结构就是动态概念。<br>
     * 
     * 由于record 就是模仿 LISP语言中的列表（增强了key-value的树形特征）。所以 IRecord 理论上是可以标识任何数据结构的。<br>
     * 用一句话来形容 IRecord:IRecord 就是结构化的数据的意思。IRecord 需要具有递归的含义，也就是IRecord的元素也可以是IRecord<br>
     * IRecord 不仅表现出结构化，还要表现出类似于LISP 列表计算。也就是可以依附于符号(key)的绑定类型：而表现出值的计算的能力。：比如<br>
     * public default <T,U> U get(String key,T t,Function<T,U> t2u)，<br>
     * 就把一个key,绑定到一个Function<T,U> t2u 然后对vlaue 进行计算。<br>
     * 另外key->value 中的value 不仅可以是数值,还可以是函数。这样key:value 就是不是一个静态意义，而是一个动态的概念了。比如：<br>
     * interface FOO{};
     * REC("foo",());
     * 记录对象:结构化的数据单元。<br>
     * 键值集合 <br>
     * 
     * @author gbench
     *
     */
    @JsonSerialize(using = IRecordSerializer.class)
    @JsonDeserialize(using = IRecordDeserializer.class)
    public static interface IRecord extends Serializable{
        
        
        /**
         * 添加一个新的键值对儿(key,value).根据具体的IRecord的不同 对于已经存在的key：若允许同名字段，则添加新的键值对儿，若不允许同名key
         * 则修改key的值位vlaue
         * @param key 字段名: key 会调用 toString作为进行键名转换
         * @param value 字段值
         * @return 当前的IRecord 以保证可以链式编程。
         */
        public IRecord add(Object key,Object value);
        
        /**
         * 设置字段key的值位vlaue,如果key不存在则添加，否则修改字段key的值位value
         * @param key 字段名
         * @param value 字段值
         * @return 当前的IRecord 以保证可以链式编程。
         */
        public IRecord set(String key,Object value);
        
        /**
         * 提取字段key 所对应的值
         * @param key 字段名
         * @return 字段key 所对应的值
         */
        public Object get(String key);
        
        /**
         * 字段key 所对应的值列表，如果存在多个同名的key 则把这些key的值合并成一个列表。
         * @param key 字段名
         * @return 字段key 所对应的值列表，如果存在多个同名的key 则把这些key的值合并成一个列表。
         */
        public List<Object> gets(String key);
        
        /**
         * 键值序{(k0,v0),(k1,v1),...}
         * @return 键值序{(k0,v0),(k1,v1),...}
         */
        public Stream<KVPair<String,Object>> stream();
        
        /**
         * 键值序{(k0,v0),(k1,v1),...}
         * @return 键值序{(k0,v0),(k1,v1),...}
         */
        public List<KVPair<String,Object>> kvs();
        
        /**
         * 键值序{(k0,v0),(k1,v1),...}
         * @return 键值序{(k0,v0),(k1,v1),...}
         */
        public Map<String,Object> toMap();
        
        /**
         * 复制当前IRecord，即做一个当前实例的拷贝:这是一个浅拷贝。仅当对字段值进行拷贝，不对字段值的属性进行进一步拷贝。
         */
        public IRecord duplicate();//复制克隆
        
        /////////////////////////////////////////////////////////////////////
        // 以下是IRecord 的默认方法区域
        /////////////////////////////////////////////////////////////////////
        
        /**
         * 键值对儿的个数
         * @return 键值对儿的个数
         */
        public default int size() {
            return this.keys().size();
        }
        
        /**
         * 是否含有key字段
         * @param key　字段名称
         * @return boolean 包含true:不包含false
         */
        public default boolean has(final String key) {
            return this.get(key)!=null;
        }
        
        /**
         * 判断是否为一个空记录：IRecord.如果没有一个key就是一个空对象。
         * @return 空记录标记。
         */
        public default boolean isEmpty() {
            return this.keys().size()<=0;
        }
        
        /////////////////////////////////////////////////////////////////////
        // 以下是IRecord 的默认方法区域
        /////////////////////////////////////////////////////////////////////
        
        /**
         * 使用bb 筛选键值对儿: 比如 提取水果的的所有子集 <br>
         * final var rec = STRING2REC("苹果,西瓜,草莓,哈密瓜,鸭梨");// 水果列表 <br>
         * cph2(RPT(rec.size(),L(true,false))).map(e->rec.gets(e.bools())).map(e->MFT("{0}",e.values())) <br>
         *      .forEach(System.out::println); <br>
         * @param bb 下标选择器
         * @return bb 所筛选出来的对应字段
         */
        public default IRecord gets(Boolean bb[]) {
            final IRecord rec = REC();
            if(bb!=null) {
                final var n = bb.length;
                final var ai = new AtomicInteger(0);
                this.kvs().forEach(p->{
                    final var i = ai.getAndIncrement()%n;
                    if(bb[i])rec.add(p._1(),p._2());
                });// forEach
            }//if
            return rec;
        }

        /**
         * 是否含有key字段
         * @param key　字段名称
         * @param t　参照对象
         * @return key字段所对应的值
         */
        @SuppressWarnings("unchecked")
        public default <T> T get(final String key,final T t) {
            return map(key,e->{
                T t1 = null;
                try {t1 = (T)e;}catch(Exception ex) {}
                return t1;
            });
        }
        
        /**
         * 是否含有key字段
         * @param idx　字段名称
         * @param t　参照对象
         * @return idx 所对应的值
         */
        @SuppressWarnings("unchecked")
        public default <T> T get(final int idx,final T t) {
            return map(idx,e->{
                T t1 = null;
                try {t1 = (T)e;}catch(Exception ex) {}
                return t1;
            });
        }
        
        /**
         * 所谓叶子节点是值：元素类型不是IRecord 或是Map之类的节点。 注意：contact,address 不是叶子节点
         * 比如；<br>
         *  final var rec = REC( <br> 
         *      "name","zhangsan", <br>
         *      "sex","man", <br>
         *      "contact",REC("mobile","13120751773","phone","0411833802234","email","gbench@sina.com"), <br>
         *      "address",REC("provice","liaoning","city","dalian","district","pulandian"));<br>
         *  System.out.println(rec.leafs());<br>
         * 提取所有叶子节点:注意当叶子节点是多元素类型：如List,Set 之类的Collection的时候，只返回Collection中的的第一个元素
         * @return 叶子节点集合：
         */
        public default List<IRecord> leafs() {
            // 单节点遍历
            return this.dfs_eval_forone(IRecord::REC);
        }
        
        /**
         * 提取第一个叶子节点 <br>
         * 所谓叶子节点是值：元素类型不是IRecord 或是Map之类的节点。 注意：contact,address 不是叶子节点
         * 比如：<br>
         * final var rec = REC( <br> 
         *      "name","zhangsan", <br>
         *      "sex","man", <br>
         *      "contact",REC("mobile","13120751773","phone","0411833802234","email","gbench@sina.com"), <br>
         *      "address",REC("provice","liaoning","city","dalian","district","pulandian")); <br>
         *  System.out.println(rec.leaf()); // "/name:zhangsan" <br>
         *  System.out.println(rec.leaf(3)); // "/contact/phone:0411833802234" <br>
         *  <br>
         * @param n 叶子节点的序号 从0开始,默认为0，如果不输入的化，只返回一个叶子，节点输入多个序号只提取第一个元素
         * @return 第一个叶子姐弟啊
         */
        public default IRecord leaf(final Number...n) {
            
           final var final_n = (n==null||n.length<1)?0:n[0].intValue();
           // 单节点遍历
           return this.dfs_eval_forone(IRecord::REC).get(final_n);
        }
        
        /**
         * 按照索引 idx 设置字段的值
         * @param idx 键值索引序号
         * @param value 字段值
         * @return 当前的IRecord 以保证可以链式编程。
         */
        public default IRecord set(int idx,Object value) {
            final var key = this.idx2key(idx);
            return this.set(key, value);
        }
        
        /**
         * 把key 所代表的值(value) 变换成 U类型的数据 
         * @param key　字段名
         * <T>　index 所代表的值的类型
         * <U> 返回结果的类型
         * @return U类型数据
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U get(final String key,final Function<T,U> t2u) {
            return map(key,e->{
                T t1 = null;
                try {t1 = (T)e;}catch(Exception ex) {}
                return t2u.apply(t1);
            });
        }
        
        /**
         * 把index 所代表的值(value) 变换成 U类型的数据
         * @param idx　字段名称
         * @param <T>　index 所代表的值的类型
         * @param <U> 返回结果的类型
         * @return U类型数据
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U get(final int idx,final Function<T,U> t2u) {
            return map(idx,e->{
                T t1 = null;
                try {t1 = (T)e;}catch(Exception ex) {}
                return t2u.apply(t1);
            });
        }
        
        /**
         * 根据路径获取Record 数据值。
         * 这是对于一个 递归结果的 键名访问方式,即 IRecord的字段元素仍然是 IRecord的形式  <br> 
         * 类似于如下的形式
         * [k0:[ <br>
         *   k1:[ <br>
         *  k2:value]]] <br>
         * getByPath("k0/k1/k2",identity) 返回 value 数值 <br>
         *
         * @param path 键名序列,分隔符sep 默认为："[/]+"  键名序列 的分割符号，这样就可以从path中构造出层级关系。
         * @param t2u 对 record 结果进行转换的函数
         * @return U类型数据值。
         */
        public default <T,U> U getByPath(final String path,final Function<T,U> t2u) {
            return getByPath(Arrays.asList(path.split("[/]+")),t2u);
        }
        
        /**
         * 根据路径获取Record 数据值。
         * 这是对于一个 递归结果的 键名访问方式,即 IRecord的字段元素仍然是 IRecord的形式  <br> 
         * 类似于如下的形式
         * [k0:[ <br>
         *   k1:[ <br>
         *  k2:value]]] <br>
         * getByPath("k0/k1/k2","/",identity) 返回 value 数值 <br>
         * 
         * @param path 键名序列
         * @param sep 键名序列 的分割符号，这样就可以从path中构造出层级关系。
         * @param t2u 对 record 结果进行转换的函数
         * @return U类型数据值。
         */
        public default <T,U> U getByPath(final String path,String sep,final Function<T,U> t2u) {
            return getByPath(Arrays.asList(path.split(sep)),t2u);
        }
        
        /**
         * 根据路径获取Record 数据值。
         * 这是对于一个 递归结果的 键名访问方式,即 IRecord的字段元素仍然是 IRecord的形式  <br> 
         * 类似于如下的形式 <br>
         * [k0:[ <br>
         *   k1:[ <br>
         *  k2:value]]] <br>
         * getByPath("k0/k1/k2") 返回 value 数值 <br>
         * 
         * @param path 键名序列, 分隔符：sep 默认为："[/]+"  键名序列 的分割符号，这样就可以从path中构造出层级关系。
         * @return U类型数据值。
         */
        public default Object getByPath(final String path) {
            return getByPath(Arrays.asList(path.split("[/]+")),identity);
        }
        
        /**
         * 根据路径获取Record 数据值。
         * 这是对于一个 递归结果的 键名访问方式,即 IRecord的字段元素仍然是 IRecord的形式  <br> 
         * 类似于如下的形式 <br>
         * [k0:[ <br>
         *   k1:[ <br>
         *  k2:value]]] <br>
         * getByPath("k0/k1/k2","/") 返回 value 数值 <br>
         * 
         * @param path 键名序列
         * @param sep 键名序列 的分割符号，这样就可以从path中构造出层级关系。
         * @return U类型数据值。
         */
        public default Object getByPath(final String path,String sep) {
            return getByPath(Arrays.asList(path.split(sep)),identity);
        }
        
        /**
         * 这是对于一个 递归结果的 键名访问方式,即 IRecord的字段元素仍然是 IRecord的形式  <br> 
         * 类似于如下的形式 <br>
         * [k0:[ <br>
         *   k1:[ <br>
         *  k2:value]]] <br>
         * getByPath([k0,k1,k2]) 返回 value 数值
         * 
         * 根据路径获取Record 数据值。依据keys:k0/k1/k2/... 按层次访问元素数据。<br>
         * 
         * @param keys 键名序列
         */
        public default Object getByPath(List<String> keys) {
            return this.getByPath(keys,identity);
        }
        
        /**
         * 这是对于一个 递归结果的 键名访问方式,即 IRecord的字段元素仍然是 IRecord的形式  <br> 
         * 类似于如下的形式 <br>
         * [k0:[ <br>
         *   k1:[ <br>
         *  k2:value]]] <br>
         * getByPath([k0,k1,k2],identity) 返回 value 数值 <br>
         * 
         * 根据路径获取Record 数据值。依据keys:k0/k1/k2/... 按层次访问元素数据。<br>
         * 
         * @param <T> 节点的数据类型
         * @param <U> 转换结果的数据类型
         * @param keys 键名序列：键名额层级结构
         * @param t2u 对 record 结果进行转换的函数
         * @return U类型数据值。
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U getByPath(final List<String> keys,final Function<T,U> t2u) {
            final var kk = keys.stream().filter(e->!e.matches("[\\s/\\\\]*")).collect(Collectors.toList());//
            final var size = kk.size();
            if(size<1)return t2u.apply((T)this);//
            final var obj = this.get(kk.get(0));
            if(kk.size()==1)return t2u.apply((T)obj);
            
            IRecord node = null;// 中间节点数据
            try {
                if(obj instanceof IRecord) {// IRecord 直接转换
                    node = (IRecord)obj;
                }else if (obj instanceof Map) {// 对Map类型 需要通过IRecord 给予简介转换。
                    final var mm = (Map<String,Object>)obj;
                    node = REC(mm);
                }else {
                    return null;
                }
            }catch(Exception e) {// 类型转换出现了异常
                e.printStackTrace();
                return null;
            }
            
            // 步进一级继续按路径检索数据。
            return node.getByPath(kk.subList(1,size),t2u);
        }
        
        /**
         * 这里key 是一个集合对象List，用t2u的对集合List中的元素进行处理：<br>
         * 把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶 <br>
         * 如果value是一个List类型,Collection 这直接使用 序列转化，对于单元素的类型，这使用Arrays.asList 对其进行包装<br>
         * 给与扩展乘一个只有一个元素的List<br>
         * 不存在的key,不予处理返回null<br>
         * @param <T> 字段key所以对应的列表数据的元素的类型
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param key 列表类型的字段名
         * @param t2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        @SuppressWarnings("unchecked")
        public default <T,U> List<U> llapply(final String key,final Function<T,U> t2u){
            List<U> uu = null;
            try {
                List<T> tt = null;
                final Object o = get(key);
                if(o==null)return null;// 不存在的value值不予处理返回null
                if(o instanceof List) {// List 类型
                    tt = (List<T>)o;
                }else if(o instanceof ArrayNode) {// Jackson的节点元素类型
                    tt = new LinkedList<>();
                    final ArrayNode anode = (ArrayNode)o;
                    var itr = anode.iterator();
                    var splitr = Spliterators.spliteratorUnknownSize(itr,
                        Spliterator.ORDERED);
                    try {StreamSupport.stream(splitr,false)
                        .map(e->(T)e).forEach(tt::add);
                    }catch(Exception e){e.printStackTrace();}// 忽略异常
                }else if(o instanceof Collection){// Collection 类型
                    tt = new LinkedList<>();
                    for(var t : ((Collection<?> )o))tt.add((T)t);
                }else if(o.getClass().isArray()) {// 数组类型
                    tt = (List<T>)Arrays.asList((Object[])o);
                }else if(o instanceof IRecord) {// IRecord 类型
                    tt = (List<T>)((IRecord)o).values();
                }else if(o instanceof Map) {// IRecord 类型
                    tt = (List<T>)((Map<?,?>)o).values().stream().collect(Collectors.toList());
                }else {// 其他 给予包装成一个List类型
                    tt = (List<T>)Arrays.asList(o);
                }// 键值得类型判断与列表化构造
                
                if(tt!=null) {// 尝试进行类型转换
                    uu = tt.stream().map(t2u).collect(Collectors.toList());
                }//if
            }catch(Exception e) {
                e.printStackTrace();
            }
            return uu;
        }
        
        /**
         * 这里key 是一个一个集合对象List，用t2u的对集合List中的元素进行处理：<br>
         * 把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶 <br>
         *
         * @param <T> 字段key所以对应的列表数据的元素的类型
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param key 列表类型的字段名
         * @param t2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        public default <T,U> List<U> lla(final String key,final Function<T,U> t2u){
            return llapply(key,t2u);
        }
        
        /**
         * 这里key 是一个一个集合对象List，用t2u的对集合List中的元素进行处理：<br>
         *   把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶 <br>
         *
         * @param <T> 字段key所以对应的列表数据的元素的类型
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param idx 列表类型的字段的索引号从0开始
         * @param t2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        public default <T,U> List<U> lla(final int idx,final Function<T,U> t2u){
            return llapply(idx,t2u);
        }
        
        /**
         * 这里key 是一个一个集合对象List，用t2u的对集合List中的元素进行处理：<br>
         * 把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶 <br>
         *
         * @param <T> 字段key所以对应的列表数据的元素的类型
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param idx 列表类型的字段的索引号从0开始
         * @param t2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        public default <T,U> List<U> llapply(final int idx,final Function<T,U> t2u){
            return llapply(idx2key(idx),t2u);
        }
        
        /**
         * 这里key 是一个一个集合对象List，用t2u的对集合List中的元素进行处理： <br>
         *   把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶 <br>
         *   T 为 objectNode 的特例 <br>
         * 
         * @param <T> 字段key所以对应的列表数据的元素的类型
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param key 列表类型的字段名称
         * @param n2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        public default <T,U> List<U> nnapply(final String key,final Function<ObjectNode,U> n2u){
            return this.llapply(key, (ObjectNode o)->o).stream()
                .map(n2u).collect(Collectors.toList());
        }
        
        /**
         * 这里key 是一个一个集合对象List，用t2u的对集合List中的元素进行处理：<br>
         * 把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶<br>
         * T 为 objectNode 的特例<br> 
         * 
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param idx 列表类型的字段的索引号从0开始
         * @param n2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        public default <T,U> List<U> nnapply(final int idx,final Function<ObjectNode,U> n2u){
            return this.llapply(idx2key(idx), (ObjectNode o)->o).stream()
                .map(n2u).collect(Collectors.toList());
        }
        
        /**
         * 这里key 是一个一个集合对象List，用t2u的对集合List中的元素进行处理：<br>
         * 把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶<br>
         * T 为 objectNode 的特例<br>
         *  
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param key 列表类型的字段名称
         * @param n2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        public default <T,U> List<U> nna(final String key,final Function<ObjectNode,U> n2u){
            return nnapply(key,n2u);
        }
        
        /**
         * 这里key 是一个一个集合对象List，用t2u的对集合List中的元素进行处理：<br>
         * 把一个列表改变成另一个列表，也就是在不改变聚合方式的情况下改变元素内容，这就是所谓的换药不换瓶<br>
         * T 为 objectNode 的特例<br>
         *  
         * @param <U> 字段key所以对应的列表数据的元素的类型T进行变换的结果
         * @param idx 列表类型的字段的索引号从0开始
         * @param n2u 对key字段的元素数据进行变换的结构
         * @return 以U数元素类型的列表结构
         */
        public default <T,U> List<U> nna(final int idx,final Function<ObjectNode,U> n2u){
            return nnapply(idx2key(idx),n2u);
        }
        
        /**
         * 是否含有指定索引的字段名．
         * @param idx　索引
         * @return 是否含有idx标号的索引
         */
        public default boolean has(final int idx) {
            return this.get(idx2key(idx))==null;
        }
        
        /**
         * 字段的索引下标转字段名称
         * @param idx 索引序号从0开始
         * @return 索引对应的键名
         */
        public default String idx2key(final Integer idx) {
            if(idx>=this.keys().size())return null;
            final String key = this.keys().get(idx);
            return key;
        }
        
        /**
         * 按照 索引进行字段　取值
         * @param idx 从0开始
         * @return idx 所标识的字段的值
         */
        public default Object get(final Integer idx) {
            final String key = this.idx2key(idx);
            return key==null?null:get(key);
        }
        
        /**
         * 获取并变换设置。相当于 get&set:
         * U u = get(key,t2u)
         * set(key,u);
         * 
         * @param key
         * @param t2u
         * @return 值变换后的数据。 U类型
         */
        public default <T,U> U compute(final String key,final Function<T,U> t2u){
            final U u = this.get(key, t2u);
            this.set(key, u);
            return u;
        }
        
        /**
         * 当且仅当key值不存在的才给予:compute,否则直接返回键值，属于名称来源于 Java Map
         * compute 就是获取并变换设置。相当于 get&set:
         * U u = get(key,t2u)
         * set(key,u);
         * 
         * @param key
         * @param t2u
         * @return 值变换后的数据。 U类型
         */
        public default <T,U> U computeIfAbsent(final String key,final Function<T,U> t2u){
            @SuppressWarnings("unchecked")
            final var o = (U)this.get(key);
            if(o!=null)return o;
            final U u = this.get(key, t2u);
            this.set(key, u);
            return u;
        }
        
        /**
         * 获取并变换设置。相当于 相当于 get&set:
         * U u = get(key,t2u)
         * set(key,u);
         * @param idx 键名索引
         * @param t2u 值变换函数
         * @return 值变换后的数据。
         */
        public default <T,U> U compute(final Integer idx,final Function<T,U> t2u){
            final U u = this.get(idx, t2u);
            final var key = this.idx2key(idx);
            this.set(key, u);
            return u;
        }
        
        /**
         * 当且仅当key值不存在的才给予:compute,否则直接返回键值，属于名称来源于 Java Map
         * compute 就是获取并变换设置。相当于 get&set:
         * U u = get(key,t2u)
         * set(key,u);
         * 
         * @param idx 键名索引
         * @param t2u
         * @return 值变换后的数据。 U类型
         */
        public default <T,U> U computeIfAbsent(final Integer idx,final Function<T,U> t2u) {
            final var key = this.idx2key(idx);
            return this.computeIfAbsent(key, t2u);
        }
        
        /**
         * 把自身 与 rec的kvs对象合并成一个对象
         * 
         * @param rec IRecord类型的 键值对儿集合：记录结构
         * @param append 是否追加到自身
         * @return 新合并后的对象 包含有 this,与 rec 的所有属性。
         */
        public default IRecord union(final IRecord rec,boolean append) {
            return union(this,rec,append);
        }
        
        /**
         * 把rec的所有在kvs值添加自身的kvs 之中，采用的时rec的啊add 方法。
         * @param rec 等待添加的record
         * @return 滋生对象
         */
        public default IRecord add(final IRecord rec) {
            return union(this,rec,true);
        }
        
        /**
         * 表头:所有字段名集合
         * @return 所有字段名集合列表
         */
        public default List<String> keys(){
            return this.stream().map(e->e._1()).collect(Collectors.toList());
        }
        
        /**
         * 表头: 所有字段名集合 keys 的别名
         * @return 所有字段名集合列表
         */
        public default List<String> kk(){
            return this.keys();
        }
        
        /**
         * 值集合
         * @param <T> 值类型
         * @param mapper 值映射对象方法
         * @return 所有值集合列表
         */
        public default <T> List<T> values(Function<Object,T>mapper){
            return this.stream().map(e->mapper.apply(e._2())).collect(Collectors.toList());
        }
        
        /**
         * 值集合
         * @return 值集合列表
         */
        public default List<Object> values(){
            return this.values(identity);
        }
        /**
         * 值集合
         * @param <T> 值类型
         * @param mapper 值映射目标对象方法
         * @return 所有值集合列表
         */
        public default <T> List<T> vv(Function<Object,T>mapper){
            return this.values(mapper);
        }
        
        /**
         * 值集合
         * @return 所有值集合列表
         */
        public default List<Object> vv(){
            return this.values();
        }
        
        /**
         * 转换成一个一维数组
         */
        public default String[] toStringArray(){
            return this.stream().map(g->g._2()==null?"":g._2().toString()).toArray(String[]::new);
        }
        
        /**
         * 转换成一个一维数组
         */
        public default Object[] toObjArray(){
            return this.stream().map(g->g._2()==null?"":g._2()).toArray(Object[]::new);
        }
        
        /**
         * 
         * @param <T> 源元素的类型
         * @param <U> 目标元素的类型
         * @param mapper 变换函数
         * @return
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U[] toArray(Function<T,U>mapper){
            Object[] oo = this.stream().map(e->(T)e._2()).map(mapper).toArray();
            Class<U> uclass = null;
            for(Object o:oo) {
                if(o!=null) {
                    uclass = (Class<U>)o.getClass();
                    break;
                }
            }
            U[] uu = (U[])Array.newInstance(uclass, oo.length);
            for(int i=0;i<oo.length;i++) {
                try{uu[i]=(U)oo[i];}catch(Exception e) {};
            }
            return uu;
        }
        
        /**
         * 
         * @param <T> 源元素的类型
         * @param <U> 目标元素的类型
         * @param mapper 变换函数
         * @return
         */
        @SuppressWarnings("unchecked")
        public default <U> U[] toArray(Class<U> uclass){
            Object[] oo = this.toObjArray();
            Class<U> final_uclass = uclass==null?(Class<U>)Object.class:uclass;
            U[] uu = (U[])Array.newInstance(final_uclass, oo.length);
            for(int i=0;i<oo.length;i++) {
                try{uu[i]=(U)oo[i];}catch(Exception e) {};
            }
            return uu;
        }
        
        /**
         * 转换成布尔值数组
         */
        public default Boolean[] bools(){
           return this.toArray(Boolean.class);
        }
        
        /**
         * 把IRecord 视为一个 key-value的序列
         * 提取指定键值徐璐 拼装成一个键值列表
         * @return 返回的 IRecord 结构为  key:kkk,value:vvv
         */
        public default List<IRecord> kvlist(){
            return kvlist((String[])null);
        }
        
        /**
         * 把IRecord 视为一个 key-value的序列
         * 提取指定键值徐璐 拼装成一个键值列表
         * @param keys 用逗号分割的键名序列
         * @return 返回的 IRecord 结构为  key:kkk,value:vvv
         */
        public default List<IRecord> kvlist(final String keys){
            return LIST(kvstream(keys));
        }
        
        /**
         * 把IRecord 视为一个 key-value的序列
         * 提取指定键值徐璐 拼装成一个键值列表
         * @param keys 用逗号分割的键名序列
         * @return 返回的IRecord 结构为  key:kkk,value:vvv
         */
        public default List<IRecord> kvlist(final String []keys){
            return LIST(kvstream(keys));
        }
        
        /**
         * 把IRecord 视为一个 key-value的序列
         * 提取指定键值徐璐 拼装成一个键值列表
         * @return 返回的 IRecord 结构为  key:kkk,value:vvv，即一个 IRecord 格式的kv键值对儿记录流
         */
        public default Stream<IRecord> kvstream(){
            return kvstream((String)null);
        }
        
        /**
         * 把IRecord 视为一个 key-value的序列
         * 提取指定键值徐璐 拼装成一个键值列表
         * @param keys 用逗号分割的键名序列:用于过滤返回的key,keys 为null不进行任何过滤，返回全部
         * @return 返回的 IRecord 结构为  key:kkk,value:vvv
         */
        public default Stream<IRecord> kvstream(final String keys){
            final String kk[] = keys==null?null:keys.split("[\\s,]+");
            return this.filter(kk).stream().map(g->REC2("key",g._1(),"value",g._2()));
        }
        
        /**
         * 把IRecord 视为一个 key-value的序列
         * 提取指定键值徐璐 拼装成一个键值列表
         * @param keys 用逗号分割的键名序列:用于过滤返回的key,keys 为null不进行任何过滤，返回全部
         * @return 返回的 IRecord 结构为  key:kkk,value:vvv
         */
        public default Stream<IRecord> kvstream(final String keys[]){
            return this.filter(keys).stream().map(g->REC2("key",g._1(),"value",g._2()));
        }

        /**
         * 生成一个 记录对象，不会对原来的对象进行更改操作
         * @param key 需要移除的键值名
         * @return 移除了指定key的字段序列
         */
        public default IRecord remove(final String key) {
            return this.filter(kvp->!kvp.key().equals(key));
        }
        
        /**
         *   使用map 函数变换指定列的数据,以key 所在数值在作为参数调用mapper所指定的函数.
         * @param key 指定列名
         * @param <T>目标结果的类型
         * @return T类型的目标结果
         */
        public default <T> T map(final String key,final Function<Object,T> mapper){
            return mapper.apply(this.get(key));}
        
        /**
         *   使用map 函数变换指定列的数据,以key 所在数值在作为参数调用mapper所指定的函数.
         * @param key 指定列名
         * @return 以key 所在数值在作为参数调用mapper所指定的函数.
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U map2(final String key,final Function<T,U> t2u){
            T t = null;
            try{t = (T)this.get(key);}catch(Exception e) {};
            if(t==null)return null;
            return t2u.apply(t);
        }
        
        /**
         * 使用map 函数变换指定列的数据,以key 所在数值在作为参数调用mapper所指定的函数.
         *   
         * @param idx 指定列名的索引序号从0开始
         * @return 以key 所在数值在作为参数调用mapper所指定的函数.
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U map2(final int idx, final Function<T,U> t2u){
            T t = null;
            try{t = (T)this.get(idx);}catch(Exception e) {};
            if(t==null)return null;
            return t2u.apply(t);
        }
        
        
        /**
         * 强制类型转换，把key 作为T 类型数据
         * @param id 字段序号，从0开始
         * @return T 类型数据
         */
        @SuppressWarnings("unchecked")
        public default <T> T as(final int id,final Class<T>tcls){
            return (T)(this.get(id));}
        
        /**
         * 强制类型转换，把key 作为iT 类型数据
         * @param key 字段名
         * @return T类型数据
         */
        @SuppressWarnings("unchecked")
        public default <T> T as(final String key,final Class<T>tcls){
            return (T)(this.get(key));}
        
        /**
         *   检索满足条件所有键值对儿
         * @param predicate 字段名
         * @return 满足 predicate 条件的键值对儿
         */
        public default List<KVPair<String,Object>> find (final Predicate<KVPair<String,Object>>predicate){
           if(predicate==null)return null;
           return this.kvs().stream().filter(predicate).collect(Collectors.toList());
        }
        
        
        /**
         *   检索满足条件的第一个键值对儿
         * @param predicate 字段名
         * @return 满足 predicate 条件的键值对儿
         */
        public default Optional<KVPair<String,Object>> findOne (final Predicate<KVPair<String,Object>>predicate){
            if(predicate==null)return null;
            return this.kvs().stream().filter(predicate).findFirst();
        }
        
        /**
         * 检索值类型为clazz 类型的对象。检索成功返回该值，不成功 null
         * @param <T> 检索的目标对象的类型
         * @param clazz 目标值的类型
         * @return 检索成功返回该值，不成功 null
         */
        @SuppressWarnings("unchecked")
        public default <T> T findOne (Class<T> clazz){
          final var opt = this.kvs().stream().filter(e->e.value()!=null &&
                clazz.isAssignableFrom(e.value().getClass())).findFirst();
           if(opt.isPresent()) {
               return (T)opt.get().value();
           }else {
               return null;
           }
         }
        
        /**
         * 强制类型转换，把key 作为T 类型数据,转换成函数,一般用于lambda表达式提取
         * @param key 字段名
         * @return Function类型的对象
         */
        @SuppressWarnings("rawtypes")
        public default Function func(final String key){
            return as(key,Function.class);
        }
        
        /**
         * 普通函数求职<br>
         * 强制类型转换，把key 作为T 类型数据,转换成函数,一般用于lambda表达式提取<br>
         * 
         * @param <T> 函数的参数类型
         * @param <U> 函数返回值类型
         * @param key 字段名, key 业与一个(Function<T,U> t2u 绑定。
         * @return 调用与key绑定的的t2u.apply(arg)
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U eval(final String key,final T arg){
            Function<T,U> foo = ((Function<T,U>)as(key,Function.class));
            if(foo==null)return null;
            U u = foo.apply(arg);
            return u;
        }
        
        /**
         * 强制类型转换，把key 作为T 类型数据,转换成函数,一般用于lambda表达式提取
         * 
         * @param <FUNC> 函数类型
         * @param <U> 目标结果类型
         * @param key key 字段名, key 业与一个(Function<T,U> t2u 绑定。
         * @param functorClass 函数类
         * @param args 函数的实际参数
         * @return 调用与key绑定的的t2u.apply(arg)
         */
        @SuppressWarnings("unchecked")
        public default <FUNC,U> U eval2(final String key,final Class<FUNC> functorClass,final Object ...args){
            FUNC func = as(key,functorClass);
            if(ANNO(functorClass,FunctionalInterface.class)==null) {
                System.out.println(functorClass+" 需要时一个函数接口，必须强制用@FunctionalInterface标记！");
                return null;
            }// if 
            final var method = functorClass.getMethods()[0];
            U obj = null;// 返回值
            try {
                obj = (U)method.invoke(func, args);
            } catch (Exception e) {
                e.printStackTrace();
            }// try
            return obj;
        }
        
        /**
         * 强制类型转换，把key 作为iT 类型数据,转换成函数,一般用于lambda表达式提取
         * @param key 字段名
         * @param args 回调函数的参数
         */
        @SuppressWarnings("unchecked")
        public default <T> void callback(final String key,final T args){
            Consumer<T> foo = as(key,Consumer.class);
            if(foo==null)return;
            foo.accept(args);
        }
        
        /**
         * 强制类型转换，把key 作为T 类型数据,转换成函数,一般用于lambda表达式提取
         * @param key 字段名：这是一个lamba数值类型的字段．
         * @param arg 参数名称
         * @param <U> 字段名 返回的结果类型
         * @return U类型数据
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U evaluate(final String key,final T arg,final Class<U> ucls){
            Function<T,U> foo = ((Function<T,U>)as(key,Function.class));
            if(foo==null)return null;
            U u = foo.apply(arg);
            return u;
        }
        
        /**
         * 强制类型转换，把key 作为T 类型数据,转换成函数,一般用于lambda表达式提取
         * @param key 字段名
         * @return U类型数据
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U evaluate(final String key,final T arg,final U obj){
            if(obj==null)return eval(key,arg);
            return this.evaluate(key, arg,(Class<U>) obj.getClass());
        }
        
        /**
         * 表达式求职：这里是模仿 R语言的 eviroment的设计的方法。
         * 把key作为变量名执行一段javascript程序,变量名采用#开头的变量名。<br>
         * "#{1}([a-z_][a-zA-z0-9_]*)" <br>
         * 示例：<br>
         * System.out.println(REC("a",3,"b",4).evalExpr("Math.pow(#a,2)+Math.pow(#b,2)"));<br>
         * evaluate 会寻找内建的ExpressionEvaluator来进行表达式计算。
         * @param expr javascript 的脚本文件
         * @param varpattern 变量的模式结构,默认为 IRecord.SHARP_VARIABLE 
         * @param ExpressionEvaluator 表达式计算引擎
         * @return javascript 程序的执行结果 :浮点数的数值
         */
        public default Double evalExpr(final String expr,final String varpattern,final ExpressionEvaluator elu){
            final ExpressionEvaluator _elu = elu ==null ?this.findOne(ExpressionEvaluator.class) :elu; // 计算环境中的计算器
            final var evaluator = _elu != null  ?_elu :new ExpressionEvaluator() ; // 计算环境没有计算器 则自动创建一个。
            final var var_pattern = Pattern.compile(varpattern==null
                ? IRecord.SHARP_VARIABLE_PATTERN // 默认的变量模式
                : varpattern);// 变量的模式
            // 表达式计算
            final var line = substitute(expr,var_pattern,this);// 变量补充。
            final var value = line==null ?null :evaluator.eval2dbl(line);// 计算表达式的值
            
            return value;
        }
        
        /**
         * 把key作为变量名执行一段javascript程序,变量名采用#开头的变量名。<br>
         * "#{1}([a-z_][a-zA-z0-9_]*)" <br>
         * 示例：<br>
         * System.out.println(REC("a",3,"b",4).evalExpr("Math.pow(#a,2)+Math.pow(#b,2)"));<br>
         * evaluate 会寻找内建的ExpressionEvaluator来进行表达式计算。
         * @param expr javascript 的脚本文件
         * @param varpattern 变量的模式结构,默认为 IRecord.SHARP_VARIABLE  
         * @return javascript 程序的执行结果 :浮点数的数值
         */
         public default Double evalExpr(String expr,String varpattern){
            return evalExpr(expr,varpattern,null);
         }
        
         /**
          * 把key作为变量名执行一段javascript程序,变量名采用#开头的变量名。<br>
          * "#{1}([a-z_][a-zA-z0-9_]*)" <br>
          * 示例：<br>
          * System.out.println(REC("a",3,"b",4).evalExpr("Math.pow(#a,2)+Math.pow(#b,2)"));<br>
          * evaluate 会寻找内建的ExpressionEvaluator来进行表达式计算。
          * @param expr javascript 的脚本文件  
          * @return javascript 程序的执行结果 :浮点数的数值
          */
         public default Double evalExpr(String expr) {
             return evalExpr(expr,(String)null);
         }
         
         /**
          * 把key作为变量名执行一段javascript程序,变量名采用#开头的变量名。<br>
          * "#{1}([a-z_][a-zA-z0-9_]*)" <br>
          * 示例：<br>
          * System.out.println(REC("a",3,"b",4).evalExpr("Math.pow(#a,2)+Math.pow(#b,2)"));<br>
          * evaluate 会寻找内建的ExpressionEvaluator来进行表达式计算。
          * @param expr javascript 的脚本文件  
          * @return javascript 程序的执行结果 :浮点数的数值
          */
         public default Double evalExpr(String expr,IRecord context) {
             final var rec = this.duplicate().add(context);
             return rec.evalExpr(expr,(String)null);
         }
        
        /**
         * 强制类型转换，把key 作为iT 类型数据,转换成函数,一般用于lambda表达式提取
         * @param key 字段名
         * @return IRecord 结果的数据
         */
       public default <T,U> IRecord bind(final String key,final Function<T,U>transform, final String newKey){
            U obj =  this.eval(key, transform);
            return REC(newKey==null?key:newKey,obj);
        }
       
       /**
        * 强制类型转换，把key 作为iT 类型数据,转换成函数,一般用于lambda表达式提取
        * @param transform 变换函数
        * @return bind 结果的数据
        */
        @SuppressWarnings("unchecked")
        public default <T, U> IRecord bind(final Function<T, U> transform) {
            final IRecord rec = REC();
            Function<T, U> foo = t -> {
                U u = null;
                try {
                    u = transform.apply(t);
                } catch (Exception ex) {
                    // ex.printStackTrace();
                } // try
                return u;
            };

            this.kvs().stream().map(e -> new Tuple2<>(e._1(), foo.apply((T) get(e._1())))).forEach(kv -> {
                if (kv._2 != null)
                    rec.add(kv._1, kv._2);
            });
            return rec;
        }
        
        /**
         * 使用map 函数变换指定列的数据
         * @param mapper 变换函数
         * @param <T> mapper 的转换类型
         * @return map 函数变换指定列的数据
         */
        public default <T> T map(final int idx,final Function<Object,T> mapper){
            String key = this.idx2key(idx);return key==null?null:map(key,mapper);
          }
        
        /**
         * 把key列转换成字符串
         * @param key
         * @return 字符串数据
         */
        public default String str(final String key){return map(key,o->o==null?null:o.toString());};
        
        /**
         * 把key列转换成浮点数
         * @param key 键名
         * @return Double
         */
        public default String str(final String key,String default_value){
            final var ret = this.str(key);
            return ret == null?default_value:ret;
        };
        
        /**
         * 把key列转换成字符串
         * @param idx 从0开始
         * @return 字符串
         */
        public default String str(final int idx){String key = this.idx2key(idx);return key==null?null:str(key);};

        /**
         * 把key列转换成浮点数
         * @param idx 索引编号 从0开始
         * @return Double
         */
        public default String str(final int idx,String default_value){
            final var ret = this.str(idx);
            return ret == null?default_value:ret;
        };
        
        /**
         * 把key列转换成字符串
         * @param key
         * @return 短整形
         */
        public default short i2(final String key){return this.num(key).shortValue();};
        
        /**
         * 把key列转换成字符串
         * @param index 从0开始
         * @return 短整形
         */
        public default short i2(final int index){return this.num(index).shortValue();};

        /**
         * 把key列转换成浮点数
         * @param key 键名
         * @return Double
         */
        public default Double dbl(final String key){return map(key,o->{Double dbl=null;
            if(o instanceof Number) return((Number)o).doubleValue();
            try {dbl=Double.parseDouble(o+"");}catch(Exception e){}return dbl;});};
            
        /**
         * 把key列转换成浮点数
         * @param key 键名
         * @return Double
         */
        public default Double dbl(final String key,Double default_value){
            final var ret = this.dbl(key);
            return ret == null?default_value:ret;
        };
        
        /**
         * Record 搜集器:这个方法是为了放置编译器抱怨ambiguous错误
         * 
         * @param <R> 规约结果的类型
         * @param supplier 容器:()->r0
         * @param accumulator 累加:(r0,kv)->r1
         * @param combiner 和兵器:(r1,r2)->r3
         * @return 规约的结果 R
         */
        public default <R> R collect0(final Supplier<R> supplier,
            final BiConsumer<R,KVPair<String,Object>> accumulator,
            final BinaryOperator<R> combiner) {
            final var collector = Collector.of(supplier,accumulator, combiner);
            return  this.stream().collect(collector);
        };
        
        /**
         * Record 搜集器:这个方法是为了放置编译器抱怨ambiguous错误<br>
         * 默认 combiner 为:(a,b)->a
         * 
         * @param <R> 规约结果的类型
         * @param supplier 容器:()->r0
         * @param accumulator 累加:可以带有一个返回值，这个返回值会被忽略掉:(r0,kv)->r1
         * @return 规约的结果 R
         */
        public default <R> R collect(final Supplier<R> supplier,
            final BiConsumer<R,KVPair<String,Object>> accumulator) {
            return this.collect0(supplier, accumulator, (a,b)->a);
        };
        
        /**
         * Record 搜集器
         * 
         * @param <R> 规约结果的类型
         * @param supplier 容器:()->r0
         * @param accumulator 累加:(r0,kv)->r1
         * @param combiner 合并器:(r1,r2)->r3
         * @return 规约的结果 R
         */
        public default <R> R collect(final Supplier<R> supplier,
            final BiConsumer<R,KVPair<String,Object>> accumulator,
            final BinaryOperator<R> combiner) {
            return this.collect0(supplier, accumulator, combiner);
        };
        
        /**
         * Record 搜集器
         * 
         * @param <R> 结果的类型
         * @param initial 初始值:r0
         * @param accumulator 累加器:(r0,kv)->r1
         * @param combiner 合并器:(r1,r2)->r3
         * @return 规约的结果 R
         */
        public default <R> R collect(final R initial,
            final BiConsumer<AtomicReference<R>,KVPair<String,Object>> accumulator,
            final BinaryOperator<R> combiner) {
            final var collector = Collector.of(()->new AtomicReference<R>(initial),accumulator,
                (aa,bb)->new AtomicReference<R>(combiner.apply(aa.get(),bb.get())));
            return  this.stream().collect(collector).get();
        };
        
       /**
        * 把KV值规约到一个数值R
        * 
        * @param <R> 规约结果的类型
        * @param kv2r KV值转换成R类型数据。kv->r
        * @param initial 初始值:r0
        * @param reducer 规约算法:(r0,r1)->r2 
        * @return 规约的结果 R
        */
        public default <R> R reduce(final Function<KVPair<String,?>,R> kv2r,final R initial,
            final BinaryOperator<R> reducer) {
            final Collector<KVPair<String,Object>,AtomicReference<R>,AtomicReference<R>> collector = 
                Collector.of(()->new AtomicReference<R>(initial),
                (aa,b)->{aa.set(reducer.apply(aa.get(),kv2r.apply(b)));},
                (aa,bb)->new AtomicReference<R>(reducer.apply(aa.get(),bb.get())));
            return  this.stream().collect(collector).get();
        };
            
        /**
         * 左折叠
         * @param <R> 规约结果的类型
         * @param initial 初始值:r0
         * @param op 折叠函数 (r0,kv)->r1
         * @return R 的累计结果
         */
        public default <R> R foldLeft(final R initial, final BiFunction<R, KVPair<String, ?>, R> op) {
            var ar = new AtomicReference<R>(initial);
            this.stream().forEach(kv -> ar.set(op.apply(ar.get(), kv)));
            return ar.get();
        };
        
        /**
         * 把键值对儿中的值数据 用 delim 进行串联：
         * [0:1    1:2 2:3 3:4] 的返回结果是: 1/2/3/4
         * @param delim 分隔符
         * @return delim 分割的values序列
         */
        public default String vvjoin(String delim) {
           return this.foldLeft((String)null,(r,kv)->MFT("{0}{1}{2}",r==null?"":r,r==null?"":delim,kv._2()));
        };
        
        /**
         * 把键值对儿中的值数据 用 delim 进行串联：
         * [0:1    1:2 2:3 3:4] 的返回结果是: 1/2/3/4
         * delim 分隔符 默认为 ：“/”
         * @return delim 分割的values序列
         */
        public default String vvjoin() {
           final var delim = "/";
           return this.foldLeft((String)null,(r,kv)->MFT("{0}{1}{2}",r==null?"":r,r==null?"":delim,kv._2()));
        };
         
        /**
         * 右折叠
         * 
         * @param <R> 规约结果的类型
         * @param initial 初始值:r0
         * @param op 折叠函数 ：(kv,r0)->r1
         * @return 右折叠的累计结果
         */
        public default <R> R foldRight(R initial, final BiFunction<KVPair<String, ?>,R, R> op) {
            final var ar = new AtomicReference<R>(initial);
            final var kvs = this.kvs();
            final var reverse_litr = kvs.listIterator(kvs.size());// 移动到列表末尾
            while (reverse_litr.hasPrevious()) ar.set(op.apply(reverse_litr.previous(),ar.get()));
            return ar.get();
        };
        
        /**
         * 把KV值规约到一个整形
         *
         * @param initial 初始值:r0
         * @param reducer 规约算法:(r0,r1)->r2 
         * @return 规约的结果
         */
         public default Integer reduce(final Integer initial,
             BinaryOperator<Integer> reducer) {
             return this.reduce(kv2int, initial, reducer);
         }
         
         /**
          * 把KV值规约到一个数值:Double 类型
          *
          * @param initial 初始值:r0
          * @param reducer 规约算法:(r0,r1)->r2
          * @return 规约的结果
          */
          public default Double reduce(final Double initial,
              final BinaryOperator<Double> reducer) {
              return this.reduce(kv2dbl, initial, reducer);
          }
          
          /**
           * 把KV值规约到一个数值R:Long 类型
           *
           * @param initial 初始值:r0
           * @param reducer 规约算法:(r0,r1)->r2 
           * @return 规约的结果
           */
           public default Long reduce(final Long initial,
               final BinaryOperator<Long> reducer) {
               return this.reduce(kv2lng, initial, reducer);
           }
           
           /**
            * 数据窗口滑动：step 每次移动的步长为1<br>
            * 对一个:1 2 3 4,按照 size:为2,step为1的参数进行滑动的结果。<br>
            * 
            * | size | 每个窗口大小为 size,每次移动的步长为step<br>
            * [1    2]<br>
            * step0:[2    3]<br>
            *   - step1:[3    4]<br>
            *   -   -   step2:[4]<br>
            * 返回:[  [1,2],  [2,3],  [3,4],  [4] ]<br>
            * 
            * @param size 滑动的窗口大小
            * @return 滑动窗口的列表。
            */
           public default List<List<KVPair<String,Object>>> sliding (final int size) {
               return sliding(this.kvs(),size,1);
           }
           
           /**
            * slidingStream
            * 数据窗口滑动：step 每次移动的步长为1<br>
            * 对一个:1 2 3 4,按照 size:为2,step为1的参数进行滑动的结果。<br>
            * 
            * | size | 每个窗口大小为 size,每次移动的步长为step<br>
            * [1    2]<br>
            * step0:[2    3]<br>
            *   - step1:[3    4]<br>
            *   -   -   step2:[4]<br>
            * 返回:[  [1,2],  [2,3],  [3,4],  [4] ]<br>
            * 
            * @param size 滑动的窗口大小
            * @return 滑动窗口的Stream<List<KVPair<String,Object>>>
            */
           public default Stream<List<KVPair<String,Object>>> sliding2 (final int size) {
               return sliding(this.kvs(),size,1).stream();
           }
           
           /**
            * tup2Stream
            * 分解成一个窗口长度为2的收尾向量的线段:线段空间。
            * 数据窗口滑动：step 每次移动的步长为1<br>
            * 对一个:1 2 3 4,按照 size:为2,step为1的参数进行滑动的结果。<br>
            * 
            * | size | 每个窗口大小为 size,每次移动的步长为step<br>
            * [1    2]<br>
            * step0:[2    3]<br>
            *   - step1:[3    4]<br>
            *   -   -   step2:[4]<br>
            * 返回:[  [1,2],  [2,3],  [3,4],  [4] ]<br>
            * 
            * @return 滑动窗口的Stream<List<KVPair<String,Object>>>
            */
           public default Stream<Tuple2<KVPair<String,Object>,KVPair<String,Object>>> tuple2Stream () {
               return sliding(this.kvs(),2,1).stream().filter(e->e.size()==2)
                   .map(e->TUP2(e.get(0),e.get(1)));
           }
           
           /**
            * 数据窗口滑动<br>
            * 对一个:1 2 3 4,按照 size:为2,step为1的参数进行滑动的结果。<br>
            * 
            * | size | 每个窗口大小为 size,每次移动的步长为step<br>
            * [1    2]<br>
            * step0:[2    3]<br>
            *   - step1:[3    4]<br>
            *   -   -   step2:[4]<br>
            * 返回:[  [1,2],  [2,3],  [3,4],  [4] ]<br>
            * 
            * @param size 滑动的窗口大小
            * @param step 每次移动的步长
            * @return 滑动窗口的列表。
            */
           public default List<List<KVPair<String,Object>>> sliding (final int size, final int step) {
               return sliding(this.kvs(),size,step);
           }
           
           /**
            * 数据窗口滑动<br>
            * 对一个:1 2 3 4,5按照 size:为2,step为1的参数进行滑动的结果。<br>
            * 
            * | size | 每个窗口大小为 size,每次移动的步长为step<br>
            * [1    2]<br>
            * step0:[2    3]<br>
            *   - step1:[3    4]<br>
            *   -   -   step2:[4]<br>
            * 返回:[  U1,  U2,  U3,  U4 ]<br>
            * mapper.apply([1,2])==U1
            * mapper.apply([2,3])==U2
            * 
            * @param size 滑动的窗口大小
            * @param step 每次移动的步长
            * @param mapper 窗口变换函数
            * @return 滑动窗口的列表。
            */
           public default <U> Stream<U> sliding2 (final int size,final int step,
                final Function<List<KVPair<String,Object>>,U> mapper) {
               return sliding(this.kvs(),size,step).stream().map(mapper);
           }
           
           /**
         * 把key列转换成浮点数
         * @param idx
         * @return
         */
        public default Double dbl(final int idx) {String key = this.idx2key(idx);return key==null?null:dbl(key);};
        
        /**
         * 用法示例： System.out.println(REC("0","1.1234").i4(0)); --> 1
         * System.out.println(REC("0",".1234").i4(0)); -->0
         * System.out.println(REC("0","1.1234").dbl(0)); -->1.1234
         * System.out.println(REC("0","00.1234").dbl(0)); -->0.1234
         * System.out.println(REC("0","0.0.1234").dbl(0)); -->null
         * 
         * 把key列的值转换成数字，除了数字以外其中： 字符串："true","fasle" 会被分别被转换成 1和0； bool型： true,fasle
         * 会被分别被转换成 1和0
         * 
         * @param key
         * @return
         */
        public default Integer i4(final String key) {
            return map(key, o -> {

                Integer i4 = null;
                final Object v = this.get(key);
                if (v == null)
                    return null;

                if (o instanceof Number)
                    return ((Number) o).intValue();
                if (v instanceof String // Boolean 类型的检测
                        || v instanceof Boolean || v.getClass() == boolean.class) {

                    String v1 = v.toString().toLowerCase().trim();
                    if (v1.equals("true"))
                        return 1;
                    if (v1.equals("false"))
                        return 0;
                } // if Boolean 类型的检测

                if (v instanceof String && // 浮点字符串格式的判断
                (v.toString().matches("\\d+(\\.\\d+)?") || v.toString().matches("\\.\\d+"))) {

                    Double d = Double.parseDouble(v.toString());
                    return ((Number) d).intValue();
                } // 浮点字符串格式的判断

                try {
                    i4 = Integer.parseInt(o + "");
                } catch (Exception e) {// 默认的数据处理。比如科学计数法。
                    try {
                        i4 = ((Number) Double.parseDouble(o + "")).intValue();
                    } catch (Exception e1) {
                        // e1.printStackTrace();// hide the excetption 为了简洁
                    }// try Number
                }// Integer

                return i4;
            });// map
        }
            
        /**
         * 把key列的值转换成数字，除了数字以外其中：
         *    字符串："true","fasle" 会被分别被转换成 1和0；
         *    bool型： true,fasle 会被分别被转换成 1和0
         * @param key 键名
         * @return Integer
         */
        public default Integer i4(final String key,Integer default_value) {
            final var ret = this.i4(key);
            return ret==null?default_value:ret;
        }
            
        /**
         * 把key列转换成浮点数
         * @param idx 键名索引 从0开始
         * @return Integer
         */
        public default Integer i4(final int idx) {
            String key = this.idx2key(idx);
            return key==null?null:i4(key);
        };

        /**
         *  把key列的值转换成数字，除了数字以外其中：
         *  字符串："true","fasle" 会被分别被转换成 1和0；
         *  bool型： true,fasle 会被分别被转换成 1和0
         * @param idx 键名 的索引号,从0开始
         * @return Integer
         */
        public default Integer i4(final int idx,Integer default_value) {
            final var ret = this.i4(idx);
            return ret==null?default_value:ret;
        }
        
        /**
         * 把key列转换成数字
         * @param key 键名
         * @return 长整形
         */
        public default Long lng(final String key){
            return map(key,o->{ Long lng=null; // 返回结果
                if(o instanceof Number) return((Number)o).longValue();
                try {lng=Long.parseLong(o+"");}catch(Exception e){
                    try{lng = ((Number)Double.parseDouble(o+"")).longValue();}
                    catch(Exception e1) {} // try
                }// try
                return lng;
            });
        };
            
        /**
         * 把key列转换成浮点数
         * @param idx
         * @return
         */
        public default Long lng(final int idx) {
            String key = this.idx2key(idx);
            return key==null?null:lng(key);
        };

        /**
         * 把key字段转换成数字
         * @param key
         * @return
         */
        public default Number num(final String key){
            return map(key,o->{ Double dbl=null;
                if(o instanceof Number) return((Number)o);// 数字值提取
                try {dbl=Double.parseDouble(o+"");}catch(Exception e){} // try
                return dbl;
            });// map
        };
            
        /**
         * 把key字段转换成数字
         * @param key 键名
         * @return Number
         */
        public default Number num(final String key,Number default_value){
            final var ret = this.num(key);
            return ret==null?default_value:ret;
        };
        
        /**
         * 把key列转换成浮点数
         * @param idx
         * @return
         */
        public default Number num(final int idx) {
            String key = this.idx2key(idx);
            return key==null?null:num(key);
        };

        /**
         * 把key字段转换成数字
         * @param idx 键名索引，从0开始
         * @return Number
         */
        public default Number num(final int idx,Number default_value){
            final var ret = this.num(idx);
            return ret==null?default_value:ret;
        };
        
        /**
         * 把key字段转换成 异常对象
         * @param key
         * @return
         */
        public default Exception except(final String key){return map(key,o->{
            if(o instanceof Exception) return((Exception)o);// 数字值提取
            else return null;});}
        
        /**
         * 把key字段转换成 异常对象
         * @param idx
         * @return
         */
        public default Exception except(final int idx) {
            String key = this.idx2key(idx);
            return key==null?null:except(key);
        };
        
        /**
         * 把key列转换成逻是时间值
         * @param key
         * @return
         */
        public default Timestamp timestamp(final String key){
            return map(key,o->o==null?null:(Timestamp)o);
        };
        
        /**
         * 把key列转换成逻是时间值
         * @param idx
         * @return
         */
        public default Timestamp timestamp(final int idx) {
            String key = this.idx2key(idx);
            return key==null?null:timestamp(key);
        };

        /**
         * 把key列转换成逻是时间值
         * @param key 键名
         * @return LocalDateTime
         */
        public default LocalDateTime ldt(final String key){
            Object obj = this.get(key);
            if(obj==null)return null;
            if(obj instanceof LocalDateTime) return (LocalDateTime) obj;
            return CronTime.date2localDateTime(this.date(key));
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param key 键名
         * @param default_value 默认值,当 值为null时候返回
         * @return LocalDateTime
         */
        public default LocalDateTime ldt(final String key,LocalDateTime default_value){
            final var ret = this.ldt(key);
            return ret == null?default_value:ret;
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @return LocalDateTime
         */
        public default LocalDateTime ldt(final int idx){
            return CronTime.dt2ldt(this.date(idx2key(idx)));
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @param default_value 默认值,当 值为null时候返回
         * @return LocalDateTime
         */
        public default LocalDateTime ldt(final int idx,LocalDateTime default_value){
            final var ret = this.ldt(idx);
            return ret == null?default_value:ret;
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param key 键名
         * @return LocalDate
         */
        public default LocalDate ld(final String key){
            final Object obj = this.get(key);
            if(obj==null)return null;
            if(obj instanceof LocalDate) return (LocalDate) obj;
            return CronTime.dt2ld(this.date(key));
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param key 键名
         * @param default_value 默认值,当 值为null时候返回
         * @return LocalDate
         */
        public default LocalDate ld(final String key,LocalDate default_value){
            final var ret = ld(key);
            return ret==null?default_value:ret;
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @return LocalDate
         */
        public default LocalDate ld(final int idx){
            final Object obj = this.get(idx);
            if(obj==null)return null;
            if(obj instanceof LocalDate) return (LocalDate) obj;
            return CronTime.dt2ld(this.date(idx));
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @param default_value 默认值,当 值为null时候返回
         * @return LocalDate
         */
        public default LocalDate ld(final int idx,LocalDate default_value){
            final var ret = ld(idx);
            return ret==null?default_value:ret;
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param key 键名
         * @return LocalTime
         */
        public default LocalTime lt(final String key){
            final Object obj = this.get(key);
            if(obj==null)return null;
            if(obj instanceof LocalTime) return (LocalTime) obj;
            return CronTime.dt2lt(this.date(key));
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param key 键名
         * @param default_value 默认值,当 值为null时候返回
         * @return LocalTime
         */
        public default LocalTime lt(final String key,LocalTime default_value){
            final var ret = lt(key);
            return ret==null?default_value:ret;
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 索引需要从0开始
         * @return LocalTime
         */
        public default LocalTime lt(final int idx) {
            final Object obj = this.get(idx);
            if(obj==null)return null;
            if(obj instanceof LocalTime) return (LocalTime) obj;
            return CronTime.dt2lt(this.date(idx));
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 索引需要从0开始
         * @param default_value 默认值,当 值为null时候返回
         * @return LocalTime
         */
        public default LocalTime lt(final int idx,LocalTime default_value) {
            final var ret = lt(idx);
            return ret==null?default_value:ret;
        }
        
        /**
         * 把key列转换成时间值,时间值转换成long
         * @param key 列
         * @return long 时间值
         */
        public default Long lngdate(final String key) {
            final Date date = date(key);
            if(date == null)return null;
            return date.getTime();
        }
        
        /**
         * 时间值
         * 把key列转换成时间值
         * @param key 键名
         * @return Date
         */
        public default Date date(final String key){
             return map(key,o->{
                Date date = null;
                if(o instanceof Date)return (Date)o;
                if(o instanceof LocalDateTime)return CronTime.ldt2dt((LocalDateTime)o);
                if(o instanceof LocalDate)return CronTime.ld2dt((LocalDate)o);
                if(o instanceof Timestamp) date = new Date(((Timestamp)o).getTime());
                else if (o instanceof Long || o instanceof LongNode){
                    Long lng =  o instanceof LongNode?((LongNode)o).asLong():(Long)o;
                    date = new Date(lng);}
                else if (o instanceof String || o instanceof TextNode ){
                    String value =o instanceof TextNode ?((TextNode)o).asText(): o.toString();
                                // 需要注意顺序很重要"yyyy-MM-dd"，yyMMdd" 很重要,不能把yyMMdd放在首位
                    if(value.endsWith("Z")) {//解析带有时区的字符串．这个很丑陋，但是我也没有更好的办法,专门为mongo日期准备的解析
                        String s = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";value = value.replace("Z", " UTC");
                        try{date = new SimpleDateFormat(s).parse(value);return date; }catch(Exception xx) {}
                    }
                    String ss[] = new String[] {"yyMMdd","yyyyMMdd","yy-MM-dd","yyyy-MM-dd",
                            "yyyy-MM-dd HH","yyyy/MM/dd HH","yyyy-MM-dd HH:mm","yyyy/MM/dd HH:mm",
                            "yyyyMMdd HH:mm:ss","yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss",
                       };
                   LinkedList<Date> dates = new LinkedList<>();
                    for(String s:ss) {
                        try {date = new SimpleDateFormat(s).parse(value);} catch (Exception e) {}
                        if(date!=null) {
                            dates.add(date);
                        }else {
                            // do nothing
                        }// if if(date!=null) 
                    }//for s:ss
                    if(dates.size()>0)date = dates.getLast();//挑选出一个时间格式
                }//if
                return date;
            });
        };
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 索引序号从０开始
         * @return 日期类型
         */
        public default Date date(final int idx) {
            String key = this.idx2key(idx);
            return key==null?null:date(key);
        };

        /**
         * 把key列转换成逻辑值
         * @param key
         * @return
         */
        public default Boolean bool(final String key){
            return map(key,o->Boolean.parseBoolean(o+""));
        };
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @return Boolean 类型
         */
        public default Boolean bool(final int idx) {
            String key = this.idx2key(idx);
            return key==null?null:bool(key);
        };

        /**
         * 把列转换成指定的 类型，这里只是强制转换，没有完成值结构的变换。
         * @param <T> 目标类型
         * @param key 键/字段名称
         * @param cls 指定类型
         * @return T类型的返回值
         */
        @SuppressWarnings("unchecked")
        public default <T> T val(final String key,final Class<T>cls){
            return (T)this.get(key);
        };//获取接口值
        
        /**
         * 把key列转换成逻是时间值
         * @param <T> 目标类型
         * @param idx 从0开始
         * @return T类型
         */
        public default <T> T val(final int idx,final Class<T>cls) {
            final String key = this.idx2key(idx);
            return key==null?null:val(key,cls);
        };


        /**
         * 转换成一个 转义映射 '\' 被转译成  \\ "'" 被转译成   \'
         * @return 转移后的Map
         */
        public default Map<String,Object> toEscapedMap(){
            final var m = this.toMap();
            m.forEach((k,v)->{// 依次对每个MAP元素进行转义
                m.put(k,v==null?null:v instanceof String
                ? v.toString() .replace("\"", "\\\\\"").replace("'","\\'")
                : v);// m.put
            });// forEach
            
            return m;
        };

        /**
         * 转换成一个json对象
         * @return
         */
        public default String json(){
            return Json.obj2json(this.toEscapedMap());
        }
        
        /**
         * 强制类型转换方法 不保证安全
         * 把当前record(key-value)的值拼装成一个对象列表{obj0,obj1,obj2,...}
         * 空值 null 被视作 ""
         * @return {obj0,obj1,obj2,...}
         */
        public default List<Object> oo(){
            return Arrays.asList(this.toObjArray());
        }
        
        /**
         * 强制类型转换方法 不保证安全
         * 把当前record的key的值转装成一个对象列表{obj0,obj1,obj2,...}
         * @param key 字段名
         * @return {obj0,obj1,obj2,...}
         */
        public default List<Object> oo(final String key){
            return llapply(key,identity);
        }
        
        /**
         * 强制类型转换方法 不保证安全
         * 把当前record的key的值转装成一个对象列表{obj0,obj1,obj2,...}
         * @param key 字段名
         * @param default_value 默认值：当key不存在的时候
         * @return {obj0,obj1,obj2,...}
         */
        public default List<Object> oo(final String key,final List<Object> default_value){
            final var oo = llapply(key,identity);
            return oo==null?default_value:oo;
        }
        
        /**
         * 强制类型转换方法 不保证安全
         * 把当前record(key-value)的值拼装成一个字符出啊列表{str0,str1,str2,...}
         * 空值 null 被视作 ""
         * @return {str0,str1,str2,...}
         */
        public default List<String> ss(){
            return Arrays.stream(this.toObjArray()).map(e->e+"").collect(Collectors.toList());
        }

        /**
         * 强制类型转换方法 不保证安全
         * 把当前record的key的值转装成一个字符串啊列表{str0,str1,str2,...}
         * @param key 字段名
         * @return {str0,str1,str2,...}
         */
        public default List<String> ss(final String key){
            return llapply(key,identity(String.class));
        }
        
        /**
         * 强制类型转换方法 不保证安全
         * 把当前record的key的值转装成一个字符串啊列表{str0,str1,str2,...}
         * @param key 字段名
         * @param default_value 默认值：当key不存在的时候
         * @return {str0,str1,str2,...}
         */
        public default List<String> ss(final String key,final List<String> default_value){
            final var ss = llapply(key,identity(String.class));
            return ss == null?default_value:ss;
        }
        
         /**
         * 强制类型转换方法 不保证安全
         * 把当前record的key的值转装成一个字符串啊列表{rec0,rec1,rec2,...}
         * @param key 字段名
         * @return {rec0,rec1,rec2,...}
         */
        public default List<IRecord> rr(final String key){
            return llapply(key,identity(IRecord.class));
        }
        
         /**
         * 强制类型转换方法 不保证安全
         * 把当前record的key的值转装成一个字符串啊列表{rec0,rec1,rec2,...}
         * @param key 字段名
         * @param default_value 默认值：当key不存在的时候
         * @return {rec0,rec1,rec2,...}
         */
        public default List<IRecord> rr(final String key,final List<IRecord> default_value){
            final var rr =  llapply(key,identity(IRecord.class));
            return rr==null?default_value:rr;
        }
        
        /**
         * 当且仅当  key 所代表的数据是一个Map<String,Object> 的实例返回一个 key 的value对象。否则
         * 生成一个 Map的复制品(clone)
         * @param key 需要进行分解的字段名：一般未json结构的列
         * @return Map<String,Object>
         */
        @SuppressWarnings("unchecked")
        public default Map<String,Object> asMap(final String key) {
            final Object obj = this.get(key);
            if(obj==null)return null;
            if(obj instanceof Map) {
                final var mm = (Map<Object,Object>)obj;
                if(mm.keySet().iterator().next() instanceof String)
                    return (Map<String,Object>)obj;// Map 的key是不含null值的
                
                var mss = new LinkedHashMap<String,Object>();// 复制品
                mm.forEach((k,v)->mss.put(k.toString(),v));// 进行数据复制。
                return mss;
            }//if
            return map(key,e->{return Json.json2obj(e, Map.class);});
        }
        
        /**
         * 把REC　转传成　tclass类型的对象
         * @param <T> 目标对象类型
         * @param tclass　目标类型
         * @return　tclass类型的对象
         */
        public default <T> T cast(final Class<T> tclass) {
            return cast(tclass,null);
        }
        
        /**
         * 把REC　转传成　tclass类型的对象
         * @param <T>
         * @param tclass　目标类型
         * @param init　对象的初始值
         * @return　tclass类型的对象
         */
        public default <T> T cast(Class<T> tclass,T init) {
            T t =null;
            if(tclass==null)return null;
            try {
                t = tclass.getDeclaredConstructor((Class<?>[])null).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(init==null)init = t;
            return OBJINIT(init,this.toMap());
        }
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @return Map<String,Object>
         */
        public default  Map<String,Object> asMap(final int idx) {
            final String key = this.idx2key(idx);
            return key==null?null:asMap(key);
        };
        
        /**
         * 修改列名：根据具体实现类的不同 同名的key 可能会被覆盖。如果需要用重名的key请tagkvs序列
         * @param keymapper 改名
         * @return IRecord
         */
        public default  IRecord keymap(final Function<String,String>keymapper) {
            return REC(toMap2(keymapper,identity));
        };
        
        /**
         * 依据索引值对kvs 进行标记
         * @param key2tag 改名函数：把键名改成指定的标号
         * @return SimpleRecord 的Record 可以存在多个同名的键名。即tag
         */
        public default  IRecord tagkvs(final Function<String,String>key2tag) {
            final var rec = new SimpleRecord();
            
            this.kvs().forEach(kv->{
                final var key = key2tag.apply(kv.key());
                final var value = kv.value();
                rec.add(key,value);
            });
            
            return rec;
        };
        
        /**
         * 依据索引值对kvs 进行标记
         * @param idx2tag 改名函数
         * @return SimpleRecord
         */
        public default  IRecord tagkvs_i(final Function<Integer,String>idx2tag) {
            final var rec = new SimpleRecord();
            final var ai = new AtomicInteger(0);
            
            this.kvs().forEach(kv->{
                final var key = idx2tag.apply(ai.getAndIncrement());
                rec.add(key,kv.value());
            });
            
            return rec;
        };
        
        /**
         * 修改列名:keymap 的别名，调整映射位置 project
         * @param keymapper 改名
         * @return 修改了键名之后的IRecord
         */
        public default  IRecord proj(final Function<String,String>keyname_mapper) {
            return keymap(keyname_mapper);
        };
        
        /**
         *  对值集合进行Map,不对key 进行变换
         * 把函数mapper 应用到  values 对象:Object->U 对象。
         * 转换成一个 String -> U 的Map
         * @param <U> valuemapper 对值的变换结果
         * @param valuemapper 值变换函数
         * @return {(String,U)} 结构的Map
         */
        public default <U> LinkedHashMap<String,U> toMap(final Function<Object,U> valuemapper) {
            final LinkedHashMap<String,U> mm = new LinkedHashMap<>();
            this.toMap().forEach((k,v)->mm.put(k, valuemapper.apply(v)));
            return mm;
        }

        /**
         * 对键名集合进行Map,不对value 进行变换
         * @param <T> key 的类型
         * @param keymapper 键名变换函数:把原来的字符类型的key,转换成T类型的键名。
         * @return 键名变换后键值对儿({T ,Object)}
         */
        public default  <T> LinkedHashMap<T,Object> toMap2(final Function<String,T>keymapper) {
            return toMap2(keymapper,identity);
        };

        /**
         * 分别对keys 和 values 进行变换。
         * 
         * @param <T> key 的类型
         * @param <U> value 的类型
         * @param keymapper key 键名变换函数:把原来的字符类型的key,转换成T类型的键名。
         * @param valuemapper value 的变换函数:把原来的Object类型的Value,转换成U类型的值。
         * @return {(t, u)} 的Map
         */
        public default  <T,U> LinkedHashMap<T,U> toMap2(final Function<String,T>keymapper,Function<Object,U>valuemapper) {
            final LinkedHashMap<T,U> mm = new LinkedHashMap<>();
            this.foreach((k,v)->{
                mm.put(keymapper.apply(k),valuemapper.apply(v));
            });
            return mm;
        }
        
        /**
         * 字符串格式化
         * @param cell_formatter 键值得格式化算法
         * @return IRecord 的字符串形式
         */
        public default String toString(final Function<Object,String> cell_formatter) {
            final var builder = new StringBuilder();
            final Function<Object,String> final_cell_formatter = cell_formatter!=null
            ?   cell_formatter
            :   v->{
                    if(v==null) return"(null)";
                    var line = "{0}";// 数据格式化
                    if(v instanceof Date) {
                        line = "{0,Date,yyyy-MM-dd HH:mm:ss}"; // 时间格式化
                    }else if(v instanceof Number) {
                        line = "{0,Number,#}"; // 数字格式化
                    }//if
                    return MessageFormat.format(line, v);
                };// cell_formatter
            
            this.kvs().forEach(p->builder.append(p._1()+":"+final_cell_formatter.apply(p._2())+"\t"));
            
            return builder.toString().trim();
        }
        
        /**
         * 返回一个 LinkedHashMap<String,T>
         * 
         * @param <T> LinkedHashMap 中的值得类型
         * @param tclass 值类型 :null 表示Object.class
         * @return  LinkedHashMap<String,T>
         */
        @SuppressWarnings("unchecked")
        public default <T> LinkedHashMap<String,T> toLhm(final Class<T>tclass){
            if(this instanceof LinkedHashMap ) return (LinkedHashMap<String,T>) this;
            final LinkedHashMap<String,T> mm = new LinkedHashMap<>();
            this.foreach(mm::put);
            return mm;
        }
        
        /**
         * 返回一个 LinkedHashMap<String,T>
         * 
         * @return LinkedHashMap<String,T>
         */
        public default LinkedHashMap<String,Object> toLhm(){
            return toLhm(null);
        }
        
        /**
         * 分别对keys 和 values 进行变换。 toMap2 函数的别名。
         * 
         * @param <T> key 的类型
         * @param <U> value 的类型
         * @param keymapper key 键名变换函数:把原来的字符类型的key,转换成T类型的键名。
         * @param valuemapper value 的变换函数:把原来的Object类型的Value,转换成U类型的值。
         * @return {(t, u)} 的Map
         */
        public default  <T,U> Map<T,U> applyForKvs(final Function<String,T>keymapper,Function<Object,U>valuemapper) {
           return this.toMap2(keymapper, valuemapper);
        }

        /**
         * 对键名集合进行Map,不对value 进行变换
         * @param <T> key 的类型
         * @param keymapper 键名变换函数:把原来的字符类型的key,转换成T类型的键名。
         * @return 键名变换后键值对儿({T ,Object)}
         */
        public default  <T> Map<T,Object> applyOnkeys(final Function<String,T>keymapper) {
            return toMap2(keymapper,identity);
        }

        /**
         * 对键名集合进行Map,不对value 进行变换:applyOnkeys 的简写
         * @param <T> key 的类型
         * @param keymapper 键名变换函数:把原来的字符类型的key,转换成T类型的键名。
         * @return 键名变换后键值对儿({T ,Object)}
         */
        public default  <T> Map<T,Object> aoks(final Function<String,T>keymapper) {
            return this.applyOnkeys(keymapper);
        }
        
        /**
         * 对值集合进行Map,不对key 进行变换
         * 把函数mapper 应用到  values 对象:Object->U 对象。
         * 转换成一个 String -> U 的Map
         * @param <T> Value 的值类型
         * @param <U> valuemapper 对值的变换结果
         * @param valuemapper 值变换函数
         * @return {(String,U)} 结构的Map
         */
        @SuppressWarnings("unchecked")
        public default <T,U> LinkedHashMap<String,U> applyOnValues(final Function<T,U> valuemapper) {
           return toMap(t->valuemapper.apply((T)t));
        }
        
        /**
         * applyOnValues:的简写
         * 对值集合进行Map,不对key 进行变换
         * 把函数mapper 应用到  values 对象:Object->U 对象。
         * 转换成一个 String -> U 的Map
         * @param <T> Value 的值类型
         * @param <U> valuemapper 对值的变换结果
         * @param valuemapper 值变换函数
         * @return {(String,U)} 结构的Map
         */
        public default <T,U> LinkedHashMap<String,U> aovs(final Function<T,U> valuemapper) {
           return applyOnValues(valuemapper);
        }
        
        /**
         * 把函数mapper 应用到 键值列表进行变换，返回一个新的IRecourd对象。
         * @param <T> mapper 的数据源类型
         * @param <U> mapper 的结果类型
         * @param mapper 对键值对儿进行数值变换的结果。
         * @return 与当前的键名相同的record 但是键值使用mapper进行变换了后的结果。
         */
        @SuppressWarnings("unchecked")
        public default <T,U> IRecord apply(final Function<KVPair<String,T>,U> mapper) {
            final List<KVPair<String,U>> kvs = LIST(this.kvs().stream()
                .map(p->KVPair.KVP(p._1(),mapper.apply((KVPair<String,T>)p))));
            return KVS2REC(kvs);
        }

        /**
         * 转换成一个 String -> String 的Map
         * @return  String -> String 的Map
         */
        public default  Map<String,String> toStrMap(){
            return this.toMap(e->e+"");
        }
        
        /**
         * 转换成一个 Properties 
         * @return  Properties
         */
        public default Properties toProps(){
            final Properties props = new Properties();
            this.toStrMap().forEach(props::put);
            return props;
        }

        /**
         *
         * @param key 需要进行分解的字段名：一般未json结构的列
         * @return
         */
        public default IRecord map2rec(final String key){
            final Object obj = this.get(key);
            if(obj ==null)return null;
            if(obj instanceof IRecord) return (IRecord)obj;
            return new LinkedRecord(this.asMap(key));}
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @return IRecord
         */
        public default  IRecord map2rec(final int idx) {
            final String key = this.idx2key(idx);
            return key==null?null:map2rec(key);
        };

        /**
         *
         * @param key 需要进行分解的字段名：一般为键值是一个json结构的key。
         * @return key被展开后的 kv集合(IRecord),IRecord 的本值就是一组 键值集合，特殊的及时其值还可使是一组 键值集合
         */
        public default IRecord rec(final String key){return map2rec(key);}
        
        /**
         * 把key列转换成逻是时间值
         * @param idx 从0开始
         * @return IRecord
         */
        public default  IRecord rec(final int idx) {
            final String key = this.idx2key(idx);
            return key==null?null:rec(key);
        };

        /**
         * json 格式的字段转换
         * @param key 需要进行分解的字段名：一般未json结构的列
         * @return
         */
        @SuppressWarnings("unchecked")
        public default List<IRecord> map2recs(final String key){
            final Object obj = this.get(key);
            if(obj==null)return null;
            if(obj instanceof List ) {
                final List<?> oo = (List<?>)obj;
                final var o = oo.stream().filter(e->e!=null).findAny();// 获取一个非空数据。用于类型检测。
                if(o.isPresent() && o.get() instanceof IRecord) {// 本身就是 List<IRecord> 类型，直接返回。
                    return (List<IRecord>)oo;
                }
            }
            return json2recs(str(key));
        }
        
        /**
         * 把key列转换成逻是recs的列表
         * @param idx  索引列表 从0开始
         * @return recs List
         */
        public default List<IRecord> map2recs(final int idx) {
            final String key = this.idx2key(idx);
            return key==null?null:map2recs(key);
        };

        /**
         * 把index列转换成逻是recs的列表 map2recs(别名);
         * @param idx 列索引 从0开始
         * @return recs List
         */
        public default List<IRecord> recs(final int idx) {
            return map2recs(idx);
        }
        
        /**
         * 把key列转换成逻是recs的列表:map2recs(别名);
         * @param key 列名
         * @return recs List
         */
        public default List<IRecord> recs(final String key) {
            return map2recs(key);
        }
        
        /**
         * 保留键名的真前缀扫描
         * 对键值儿进行扫描:所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真后缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
         * [[1], [1, 2], [1, 2, 3], [1, 2, 3, 4], [1, 2, 3, 4, 5]]
         * fx 就是作用 真后缀的集合的元素如[1],[4,5]的函数(不包括空集合),把每个元素后缀转换成目标类型T的对象
         * @param <T> fx 目标结果类型
         * @param <X> fx 函数的源数据类型
         * @param fx 真后缀的变换函数: [(k,x)]->t
         * @param finisher 结果呈递函数:对fx作用后的真后缀集tt，进行再加工 递呈 最终结果 U: tt->u
         * @return List<T> fx真前缀处理结果的集合
         */
        @SuppressWarnings("unchecked")
        public default <X,T> List<T> scanl(Function<List<KVPair<String,X>>,T> fx) {
            return IRecord.scan(this.kvs().stream().map(p->KVPair.KVP(p._1(),(X)p._2())), fx);
        }
        
        /**
         * 对键值儿进行扫描:所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真后缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
         * [[1], [1, 2], [1, 2, 3], [1, 2, 3, 4], [1, 2, 3, 4, 5]]
         * fx 就是作用 真后缀的集合的元素如[1],[4,5]的函数(不包括空集合),把每个元素后缀转换成目标类型T的对象
         * @param <T> fx 目标结果类型
         * @param <X> fx 函数的源数据类型
         * @param fx 真前缀的变换函数:xx->t
         * @return List<T> fx真前缀处理结果的集合
         */
        public default <X,T> List<T> scanl2(Function<List<X>,T> fx) {
            return scanl2(fx,e->e);
        }

        /**
         * 对键值儿进行扫描:所谓扫描是指 对于 [1,2,3,4] 这样的序列生成
         * [[1],[1,2],[1,2,3],[1,2,3,4]] 这样的真前缀的集合。
         * 
         * @param <T> 目标结果类型
         * @param <X> fx 函数的源数据类型
         * @param <U> 目标结果：finisher 的返回类型
         * @param fx 真前缀的变换函数:xx->t
         * @param finisher 结果呈递函数:对fx作用后的真后缀集tt，进行再加工 递呈 最终结果 U: tt->u
         * @return U finisher 呈递 的目标结果
         */
        @SuppressWarnings("unchecked")
        public default <X,T,U> U scanl2(Function<List<X>,T> fx,Function<List<T>,U> finisher) {
            var tt = IRecord.scan(this.kvs().stream().map(e->(X)e._2()), fx);
            return finisher.apply(tt);
        }

        /**
         * 保留键名的真后缀扫描
         * 对键值儿进行扫描:所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真后缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
         * [[5], [4, 5], [3, 4, 5], [2, 3, 4, 5], [1, 2, 3, 4, 5]]
         * fx 就是作用 真后缀的集合的元素如[1],[4,5]的函数(不包括空集合),把每个元素后缀转换成目标类型T的对象
         * @param <T> fx 目标结果类型
         * @param <X> fx 函数的源数据类型
         * @param fx 真后缀的变换函数:  [(k,x)]->t
         * @return List<T> fx真后缀处理结果的集合
         */
        @SuppressWarnings("unchecked")
        public default <X,T> List<T> scanr(Function<List<KVPair<String,X>>,T> fx) {
            return IRecord.scan(this.kvs().stream().map(p->KVPair.KVP(p._1(),(X)p._2())), fx,true);
        }
        
        /**
         * 对键值儿进行扫描:所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真后缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
         * [[5], [4, 5], [3, 4, 5], [2, 3, 4, 5], [1, 2, 3, 4, 5]]
         * fx 就是作用 真后缀的集合的元素如[1],[4,5]的函数(不包括空集合),把每个元素后缀转换成目标类型T的对象
         * @param <T> fx 目标结果类型
         * @param <X> fx 函数的源数据类型
         * @param fx 真后缀的变换函数: xx->t
         * @return List<T> fx真后缀处理结果的集合
         */
        public default <X,T> List<T> scanr2(Function<List<X>,T> fx) {
           return this.scanr2(fx,e->e);
        }

        /**
         * 对键值儿进行扫描:所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真后缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
         * [[5], [4, 5], [3, 4, 5], [2, 3, 4, 5], [1, 2, 3, 4, 5]]
         * fx 就是作用 真后缀的集合的元素如[1],[4,5]的函数(不包括空集合),把每个元素后缀转换成目标类型T的对象
         * @param <T> fx 目标结果类型
         * @param <X> fx 函数的源数据类型
         * @param <U> 目标结果：finisher 的返回类型
         * @param fx 真后缀的变换函数: xx->t
         * @param finisher 结果呈递函数:对fx作用后的真后缀集tt，进行再加工 递呈 最终结果 U: tt->u
         * @return U finisher呈递的目标结果
         */
        public default <X,T,U> U scanr2(Function<List<X>,T> fx,Function<List<T>,U> finisher) {
            final var kvs = LIST(this.kvs());
            Collections.reverse(kvs);
            @SuppressWarnings("unchecked")
            final var tt = IRecord.scan(kvs.stream().map(e->(X)e._2()), fx,true);
            
            return finisher.apply(tt);
        }
        
        /**
         * 这是按照kk 所指定的键值进行数据过滤。
         * @param flds 提取的字段集合用逗号分割,flds为null 表示不进行过滤。
         * @return 一个SimpleRecord 以保证空值字段也可以保持顺序。
         */
        public default IRecord filter(final String flds) {
            String kk[] = flds==null?null:flds.split("[,、，]+");
            return this.filter(kk);
        }

        /**
         * 这是按照kk 所指定的键值进行数据过滤。
         * @param kk 提取的字段的键值名称数据,kk 为null 标识不进行过滤。
         * @return 一个LinkedRecord 以保证空值字段也可以保持顺序。
         */
        public default IRecord filter(final String kk[]){
            final var rec = REC();
            final String[] ss = kk == null
               ? this.stream().map(e->e._1()).toArray(String[]::new)
               : kk;
            for(String s:ss) {
                final var v = this.get(s);
                if(v!=null)rec.add(s, this.get(s));
            }
            
            return rec;
        }

        /**
         * 这是按照kk 所指定的键值进行数据过滤。
         * @param pfilter 字段过滤检测器,null 不进行过滤。
         * @return 一个LinkedRecord 以保证空值字段也可以保持顺序。
         */
        public default IRecord filter(final Predicate<KVPair<String,Object>>pfilter){
            final var rec = REC();
            Predicate<KVPair<String,Object>> _pfilter = pfilter==null?e->true:pfilter;
            this.stream().filter(_pfilter).forEach(s->rec.add(s.key(),s.value()));
            return rec;
        }
        
        /**
         * 看我72变化：Record的变化比72般变化还要多，哈哈
         * IRecord的变身：就像孙悟空一样变成 林林种种的 其他物件。
         * 孙悟空.mutate(白骨精）：就是孙悟空变成一个白骨精。
         * @param <T> mutator 所要变成的目标对象的类型
         * @param mutator 变换函数:变身逻辑
         * @return mutator 变换后的目标对象。
         */
        public default <T> T mutate(final Function<IRecord,T> mutator) {
            return mutator.apply(this);
        }

        /**
         * 字段变身:不带有类型变换功能，对于属性字段只是强制类型转换，转换不成功则设置为null
         * @param <T>
         * @param targetClass 目标类对象的类
         * @return 目标对象T类型
         */
        @SuppressWarnings("unchecked")
        public default <T> T mutate(Class<T> targetClass) { 
            T bean= null; // 返回值
            if(targetClass==null) return bean;// 目标targetClass对象
            try{
                if(this.getClass().isAssignableFrom(targetClass))return (T)this;// 如果 是cls的子类，则直接强制转换并返回。
                
                final T target = targetClass.getDeclaredConstructor().newInstance(); // 创建载体对象（承载Record的各个属性的值)
                Stream.of(targetClass.getDeclaredFields())
                // 对字段进行分组选取每个分组的第一个字段作为属性名(对于SimpleRecord 是可以存在同名的field的),这也是分组的原因。
                .collect(Collectors.groupingBy(e->e.getName(),//键为字段名称：
                    Collector.of(HashSet<Field>::new,HashSet::add,(l,r)->{l.addAll(r);return l;},
                    f->f.iterator().next())) // 敛集字段信息
                ).forEach( (name,fld)->{ // 字段遍历
                    fld.setAccessible(true);/*使得字段变得可编辑*/
                    Object value = this.get(name);
                    if(value!=null)try {fld.set(target, this.get(name));}
                    catch(Exception ex){ex.printStackTrace();} /*根据键值名设置字段值,从record拷贝到Class*/
                });/*foreach*/ 
                bean = target;// 成功完成对象转换
            }catch(Exception e) {
                e.printStackTrace();
            };
            
            return bean;
            
        }//mutate
        
        /**
         * 字段变身  带有简答的类型转换比如时间转换。字符串类型转换成时间对象
         * 既然mutate2比mutate 更为智能为何保留mutate呢，原因在制动targetClass的具体类型的时候，mutate比较快。
         * @param <T> 目标类类型。
         * @param targetClass 目标类对象的类
         * @return
         */
        public default <T> T mutate2(final Class<T> targetClass) {
            return OBJINIT(targetClass,this);
        }
        
        /**
         * 字段浅遍历
         * @param cons
         */
        @SuppressWarnings("unchecked")
        public default <V> void foreach(final BiConsumer<String,V>cons) {
            this.stream().forEach(kv->cons.accept(kv.key(), (V)kv.value()));
        }
        
        /**
         * 遍历IRecord 深度遍历: Deep First Search
         * @param cons 值函数 key,key 所对应的值的集合的流化表示
         */
        public default void dfs(final BiConsumer<String,Stream<Object>> cons) {
            dfs(this,cons,null,null);
        }
        
        /**
         * 遍历IRecord 深度遍历: Deep First Search
         * @param cons 值函数 key,key 所对应的值的集合
         */
        @SuppressWarnings("unchecked")
        public default<T> void dfs_forall(final BiConsumer<String,List<T>> cons) {
            dfs(this,(path,stream)->cons.accept(path,
                (List<T>)(stream.collect(Collectors.toList()))),null,null);
        }
        
        /**
         * 遍历IRecord 深度遍历: Deep First Search
         * @param cons 值函数 key,key 所对应的值的集合的第一个元素。
         */
        @SuppressWarnings("unchecked")
        public default <T> void dfs_forone(final BiConsumer<String,T> cons) {
            dfs(this,(path,stream)->{
                var opt = stream.findFirst();
                cons.accept(path,(T)(opt.isPresent()?opt.get():null));
            },null,null);
        }
        
        /**
         * 单值式深度优先遍历计算
         * 遍历IRecord 深度遍历: Deep First Search
         * @param evaluaotr 指标计算器
         * @param <U> 结果的参数类型
         * @param <T> 元素的参数类型
         * @param evaluaotr 值函数 key,key 所对应的值的集合的第一个元素。T key 所对应的元素类型
         */
        @SuppressWarnings("unchecked")
        public default <T,U> List<U> dfs_eval_forone(final BiFunction<String,T,U> evaluaotr) {
            final var uu = new LinkedList<U>();
            dfs(this,(path,stream)->{
                var opt = stream.findFirst();
                var u = evaluaotr.apply(path,(T)(opt.isPresent()?opt.get():null));
                uu.add(u);
            },null,null);
            return uu;
        }
        
        /**
         * 多值式深度优先遍历计算
         * 遍历IRecord 深度遍历: Deep First Search
         * @param evaluaotr 指标计算器
         * @param <U> 结果的参数类型
         * @param <T> 元素的参数类型
         * @param evaluaotr 值函数 key,key 所对应的值的集合的第一个元素。T key 所对应的集合的元素类型
         */
        @SuppressWarnings("unchecked")
        public default <T,U> List<U> dfs_eval_forall(final BiFunction<String,Stream<T>,U> evaluaotr) {
            final var uu = new LinkedList<U>();
            dfs(this,(path,stream)->{
                final var u = evaluaotr.apply(path,(Stream<T>)stream);
                uu.add(u);
            },null,null);
            return uu;
        }
        
        /**
         * 结果生成：一个Key-Value的列表。
         * 遍历IRecord 深度遍历: Deep First Search
         * @param <U > 值类型:Record的值的类型，但是这个类型被隐藏了
         * @param mapper 值函数 key,key 所对应的值的集合的第一个元素。
         */
        public default <U> IRecord dfs2rec(final BiFunction<String,Stream<Object>,U> mapper) {
            final var rec = REC();
            dfs(this,(path,stream)->rec.add(path,mapper.apply(path,stream)),null,null);
            return rec;
        }
        
        /**
         * 结果生成：一个Key-Value的列表。
         * 遍历IRecord 深度遍历: Deep First Search
         * @param <U> 值类型
         * @param mapper 值函数 key,提取 key 所对应的值的集合的第一个元素 进行变换后的结果
         * @eturn mapper 值函数 key,提取 key 所对应的值的集合的第一个元素 进行变换后的结果
         */
        public default <U> Map<String,U> dfs2kvs(final BiFunction<String,Stream<Object>,U> mapper) {
            final var kvs = new LinkedHashMap<String, U>();
            dfs(this,(path,stream)->kvs.put(path,mapper.apply(path,stream)),null,null);
            return kvs;
        }
        
        /**
         * 遍历IRecord 深度遍历: Deep First Search
         * @param cons 值函数 key,key 所对应的值的集合, 对于集合只取第一个值
         */
        public default void dfs2(final BiConsumer<String,Object> cons) {
            dfs(this,(k,v)->cons.accept(k,v.findFirst().get()),null,null);
        }
        
        /**
         * 类似于数据的库的表连接,通过 key 把 自己与另外一个 rec 进行连接 <br>
         * a = REC("1","A","2","B","4","D"); <br>
         * b = a.join(REC("1","a","2","b","3","c")) <br>
         * 则b 就是： 1:A --> a    2:B --> b   4:D --> null    3:null --> c
         * 
         * @param rec 另外的一个连接对象
         * @return {key->TUP2<Object,Object>}的 Map
         */
        public default IRecord join(final IRecord rec) {
            return REC(join(this,rec));
        }
        
        /**
         * 连接成列表 <br>
         * 类似于数据的库的表连接,通过 key 把 自己与另外一个 rec 进行连接 <br>
         * a = REC("1","A","2","B","4","D"); <br>
         * b = a.join(REC("1","a","2","b","3","c")) <br> 
         * 则b 就是： 1:A --> a    2:B --> b   4:D --> null    3:null --> c <br>
         * 
         * var x = porec.join2ll(sorec,"path,po,so".split(",")); <br>
         * 类似于这样的集合：<br>
         * path po  so <br>
         * /煤焦油/计提损耗   37180.8 (null) <br>
         * /煤焦油/徐州市龙山制焦有限公司    1.60138826E7    (null) <br>
         * 
         * @param rec 另外的一个连接对象 
         * @param keys 新生记录的键值名称 
         * @return {key->TUP2<Object,Object>}的 Map
         */
        public default List<IRecord> join2rr(IRecord rec,String [] keys) {
            final var rr = new LinkedList<IRecord>();
            join(this,rec).forEach((k,tup)->{
               rr.add(REC(keys[0],k,keys[1],tup._1,keys[2],tup._2));
            });
            return rr;
        }
        
        /**
         * 连接成列表 <br>
         * 类似于数据的库的表连接,通过 key 把 自己与另外一个 rec 进行连接 <br>
         * a = REC("1","A","2","B","4","D"); <br>
         * b = a.join(REC("1","a","2","b","3","c")) <br>
         * 则b 就是： 1:A --> a    2:B --> b   4:D --> null    3:null --> c <br>
         * 
         * var x = porec.join2ll(sorec,"path,po,so".split(",")); <br>
         * 类似于这样的集合： <br>
         * path po  so <br>
         * /煤焦油/计提损耗   37180.8 (null) <br>
         * /煤焦油/徐州市龙山制焦有限公司    1.60138826E7    (null) <br>
         * 
         * @param rec 另外的一个连接对象 
         * @param keys 新生记录的键值名称 ,用逗号分割
         * @return {key->TUP2<Object,Object>}的 Map
         */
        public default List<IRecord> join2rr(final IRecord rec,final String keys) {
            return join2rr(rec,(keys==null?"key,v1,v2":keys).split("[,]+"));
        }
        
        /**
         * 连接成列表 <br>
         * 类似于数据的库的表连接,通过 key 把 自己与另外一个 rec 进行连接 <br>
         * a = REC("1","A","2","B","4","D"); <br>
         * b = a.join(REC("1","a","2","b","3","c")) <br>
         * 则b 就是： 1:A --> a    2:B --> b   4:D --> null    3:null --> c <br>
         * 
         * var x = porec.join2ll(sorec,"path,po,so".split(",")); <br>
         * 类似于这样的集合：<br>
         * path po  so
         * /煤焦油/计提损耗   37180.8 (null) <br>
         * /煤焦油/徐州市龙山制焦有限公司    1.60138826E7    (null) <br>
         * 
         * @param rec 另外的一个连接对象
         * keys 新生记录的键值名称:默认为[key,v1,v2] 
         * @return {key->TUP2<Object,Object>}的 Map
         */
        public default List<IRecord> join2rr(final IRecord rec) {
            return join2rr(rec,(String)null);
        }
        
        /**
         * 对于IRecord 进行排序
         * 
         * @param comparator 排序比较器
         * @return 新的IRecord 排序后
         */
        public default IRecord sorted(final Comparator<? super KVPair<String, Object>> comparator){
            final var rec = REC();
            this.stream().sorted(comparator).forEach(kv -> rec.add(kv._1(), kv._2()));
            return rec;
        }
        
        /**
         * 对于IRecord 的字段进行倒排
         * @return 新的IRecord 倒排后
         */
        public default IRecord reverse(){
            return this.foldRight(REC(),(kv,rec)->rec.add(kv._1(),kv._2()));
        }
        
        /**
         * 吧Record 转换成 目标类型对象
         * @param <T> 目标类型
         * @param targetClass 目标类型类：null 则返回IRecord本身
         * @return T 结构的对象
         */
        @SuppressWarnings("unchecked")
        public default <T> T toTarget(Class<T> targetClass) {
            if(targetClass==null)return ((T)this);
            return IRecord.rec2obj(this, targetClass);
        }
        
        /////////////////////////////////////////////////////////////////////
        // 以下是IRecord DataFrame 类型的方法区域:所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)
        /////////////////////////////////////////////////////////////////////
        
        /**
         * 生成一个二维数组矩阵：数组元素采用 Object 类型.
         * @param t2u 值变换函数
         * @return Object[][] U类型的二维数组
         */
        public default Object[][] toArray2(){
            return toArray2(e->e,Object.class);
        }
        
        /**
         * 生成一个二维数组矩阵,生成数组的类型,采用t2u对于第一列元素进行 Apply,提取第一个费控元素的类型作为U类型，对于 <br>
         * 全为null的情况采用Object.class作为默认值。<br>
         * 第一列的值不存在则返回null<br>
         * @param <T> t2u的源类型，即Record 中List的中的元素类型 一般为Object,除非明确知道IRecord中的具体的数据结构
         * @param <U> t2u的目标结果的类型
         * @param t2u 值变换函数 t->u
         * @return U[][] U类型的二维数组
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U[][] toArray2(Function<T,U> t2u){
           final var ll  = this.lla(0, t2u);//尝试应用到第一列t2u，提取Class<U>的类型信息。
           if(ll==null)return null;// 列值不村子直接返回
           final var cellClass = ll.stream().filter(e->e!=null)
                .map(e->(Class<U>)e.getClass()).findFirst().orElse((Class<U>)Object.class);
          return this.toArray2(t2u, cellClass);
        }
        
        /**
         * 生成一个二维数组矩阵,生成数组的类型,采用t2u对于第一列元素进行 Apply,提取第一个费控元素的类型作为U类型，对于 <br>
         * 全为null的情况采用Object.class作为默认值。<br>
         * @param <T> t2u的源类型，即Record 中List的中的元素类型 一般为Object,除非明确知道IRecord中的具体的数据结构
         * @param <U> t2u的目标结果的类型
         * @param t2u 值变换函数 t->u
         * @param cellClass 结果容器的数据类型
         * @return U[][] U类型的二维数组
         */
        @SuppressWarnings("unchecked")
        public default <T,U> U[][] toArray2(final Function<T,U> t2u,final Class<U>cellClass){
           final var shape = this.shape();
           final var ooo = this.rows().stream().map(row->row.toArray(t2u)).toArray(n->{
                U[][] uu = null;
                uu = (U[][])Array.newInstance(cellClass==null
                    ?(Class<U>)Object.class:cellClass,shape._1(),shape._2());
                return uu;
           });//ooo
           return ooo;
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 术语来源 pandas<br>
         * 返回矩阵形状<br>
         * @return (height,width)的二维矩阵
         */
        public default Tuple2<Integer,Integer> shape(){
            final var width = this.keys().size();
            final var height = this.kvstream()
                .map(rec->{
                    var vv = rec.lla("value",e->e);
                    if(vv==null)return 0;
                    else return vv.size();
                }).collect(Collectors.summarizingInt(e->e)).getMax();
            return new Tuple2<>(height<0?0:height,width);
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 行化操作：数据分析类 需要与DataMatrix 相结合生成 data.frame类型的 转换函数 <br>
         * 
         * row:Map 行记录 <br>
         * S:结果类型为Stream <br>
         * 主要用途就是 完成 IRecord 向 DataMatrix的转换，但是为了保证DataMatrix 与IRecord 的独立。而设置这个函数。比如 <br>
         * var dm = new DataMatrix<> (rec.rr2rowS(),Integer.class); 就构造了一个 DataMatrix 对象。
         * 
         * @return 生成一个hashmap 的集合
         */
         @SuppressWarnings("unchecked")
         public default Stream<Map<String, ?>> rowS() {
             return (Stream<Map<String, ?>>) (Object)Stream.of(this.toMap());
         }

         /**
          * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
          * 行化操作：数据分析类 需要与DataMatrix 相结合生成 data.frame类型的 转换函数 <br>
          * 
          * row:Map 行记录 <br>
          * S:结果类型为Stream <br>
          * 主要用途就是 完成 IRecord 向 DataMatrix的转换，但是为了保证DataMatrix 与IRecord 的独立。而设置这个函数。比如 <br>
          * var dm = new DataMatrix<> (rec.rowS(),Integer.class); 就构造了一个 DataMatrix 对象。<br>
          * 
          * @return 生成一个hashmap 的集合
          */
         @SuppressWarnings("unchecked")
         public default <T> Stream<Map<String,T>> rowS(final Class<T>clazz) {
             return (Stream<Map<String,T>>) (Object)Stream.of(this.toMap());
         }

         /**
          * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
          * 行化操作：数据分析类 需要与DataMatrix 相结合生成 data.frame类型的 转换函数<br>
          * 
          * row:Map 行记录 <br>
          * L:结果类型为List <br>
          * 主要用途就是 完成 IRecord 向 DataMatrix的转换，但是为了保证DataMatrix 与IRecord 的独立。而设置这个函数。比如 <b>
          * var dm = new DataMatrix<> (rec.rowS(),Integer.class); 就构造了一个 DataMatrix 对象。<br>
          * 
          * @return 生成一个hashmap 的集合
          */
         @SuppressWarnings("unchecked")
         public default <T> List<Map<String,T>> rowL(final Class<T>clazz) {
             return (List<Map<String,T>>) (Object)Arrays.asList(this.toMap());
         }

         /**
          * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
          * 行化操作：数据分析类 需要与DataMatrix 相结合生成 data.frame类型的 转换函数<br>
          * 
          * row:Map 行记录<br>
          * L:结果类型为List<br>
          * 主要用途就是 完成 IRecord 向 DataMatrix的转换，但是为了保证DataMatrix 与IRecord 的独立。而设置这个函数。比如 <br>
          * var dm = new DataMatrix<> (rec.rowL(),Integer.class); 就构造了一个 DataMatrix 对象。<br>
          * 
          * @return 生成一个hashmap 的集合<br>
          */
         public default <T> List<Map<String,Object>> rowL() {
             return Arrays.asList(this.toMap());
         }

        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 返回行列表:<br>
         *  final var dfm = REC( <br>
         *    "A",L("a","b","c"), // 第一列 <br>
         *    "B",L(1,2,3), // 第二列 <br>
         *    "C",A(2,4,6,10),// 第三列 <br>
         *    "D",REC(0,3,1,6,2,9),// 第四列 <br>
         *    "E",REC(0,31,1,61,2,91).toMap()// 第五列 <br>
         *  );// dfm <br>
         *  
         *  返回:<br>
         *  A:a	B:1	C:2	D:3	E:31 <br>
         *  A:b	B:2	C:4	D:6	E:61 <br>
         *  A:c	B:3	C:6	D:9	E:91 <br>
         *  A:a	B:1	C:10	D:3	E:31 <br>
         *  
         * @param mapper 元素类型格式化函数,类型为， (key:String,value:Object)->new_value
         * @param hh列名序列,若为空则采用EXCEL格式的列名称(0:A,1:B,...),如果列表不够也采用excelname给与填充区别只不过添加了一个前缀"_"
         */
        public default <T> List<IRecord> rows(final BiFunction<String,Object,T> mapper,final List<String>hh) {
            final var shape = this.shape();// 提取图形结构
            final var rows = new ArrayList<IRecord>(shape._1());
            final List<String> final_hh = hh == null
                ? LIST(Stream.iterate(0, i->i+1).limit(shape._2()).map(LittleTree::excelname))
                : hh;
            if(hh!=null)Stream.iterate(hh.size(),i->i<shape._2(),i->i+1).forEach(i->{
                final_hh.add(excelname(i));
            });
            final var keys = this.keys().toArray(String[]::new);
            for(int j=0;j<shape._2();j++) {
                final var col = this.lla(keys[j],e->e);// 提取name列
                if(col==null)continue;
                final var size = col.size();// 列大小
                for(int i=0;i<shape._1();i++) { // 列名索引
                    if(rows.size()<=i)rows.add(REC());
                    final var row = rows.get(i);// 提取i 行的数据记录。
                    final var key = final_hh.get(j);
                    row.add(key,mapper.apply(keys[j],col.get(i%size)));
                }//for i
             }// keys
           return rows;
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 返回行列表:<br>
         *  final var dfm = REC( <br>
         *    "A",L("a","b","c"), // 第一列 <br>
         *    "B",L(1,2,3), // 第二列 <br>
         *    "C",A(2,4,6,10),// 第三列 <br>
         *    "D",REC(0,3,1,6,2,9),// 第四列 <br>
         *    "E",REC(0,31,1,61,2,91).toMap()// 第五列 <br>
         *  );// dfm <br>
         *  
         *  返回:<br>
         *  A:a B:1 C:2 D:3 E:31 <br>
         *  A:b B:2 C:4 D:6 E:61 <br>
         *  A:c B:3 C:6 D:9 E:91 <br>
         *  A:a B:1 C:10    D:3 E:31 <br>
         *  
         */
        public default <T> List<IRecord> rows() {
            return this.rows((key,value)->value,this.keys());
        }
        
        /**
         * 第rowid 所在的行记录
         * @param rowid 行号索引：从0开始
         * @return 行记录
         */
        public default IRecord row(int rowid) {
            final var rows = this.rows((name,e)->e,this.keys());
            if(rows==null||rows.size()<1||rows.size()<=rowid)return null;
            return rows.get(rowid);
        }
        
        /**
         * 返回idx 位置的列元素集合
         * 
         * @param idx 列名索引，从0开始
         * @param t2u 列值转换函数:t->u
         */
        public default List<Object> column(Integer idx){
            return this.lla(idx, e->e);
        }

        /**
         * 返回idx 列索引位置的列数据:列表
         *  
         * @param <T> 列的元数据类型
         * @param <U> 返回值变换后的列的类型
         * @param idx 列名索引，从0开始
         * @param t2u 列值转换函数:t->u
         */
        public default <T,U> List<U> column(Integer idx,Function<T,U>t2u){
            return this.lla(idx, t2u);
        }

        /**
         * 返回colName的列元素集合：强制转换为targetClass 的类型 <br>
         * 
         * 类型采用强制转换，因此可能会出现不同列之间的类型不一致的风险，使用时候需要注意。这一部分需要在编程中给注意与防范。<br>
         * 类库设置不予考虑。<br>
         * 
         * @param <T> 列的元数据类型
         * @param colName 列名
         * @param targetClass 列的值类型类
         */
        @SuppressWarnings("unchecked")
        public default <T> List<T> column(String colName,Class<T> targetClass){
            return this.lla(colName, e->(T)e);
        }

        /**
         * 提取columnName 所在的列数据列表
         *  
         * @param <T> 列的元数据类型
         * @param <U> 返回值变换后的列的类型
         * @param columnName 列名：这是对lla的别名
         * @param t2u 列值转换函数 :t->u
         */
        public default <T,U> List<U> column(String columnName,Function<T,U>t2u){
            return this.lla(columnName, t2u);
        }

        /**
         * 
         * 返回idx 位置的列元素集合：：强制转换为targetClass 的类型
         * 
         * 类型采用强制转换，因此可能会出现不同列之间的类型不一致的风险，使用时候需要注意。这一部分需要在编程中给注意与防范。<br>
         * 类库设置不予考虑。<br>
         * 
         * @param <T> 列的元数据类型
         * @param idx 列名索引，从0开始
         * @param targetClass 列的值类型类
         */
        @SuppressWarnings("unchecked")
        public default <T> List<T> column(Integer idx,Class<T> targetClass){
            return this.lla(idx, e->(T)e);
        }

        /**
         * 提取所有的列数据列表
         * 
         * @param <T> 列的元数据类型
         * @param <U> 返回值变换后的列的类型
         * @param t2u 列值转换函数:t->u
         * @return 列集合每个列嗾使一个U类型的列表
         */
        public default <T,U> List<List<U>> columns(Function<T,U>t2u){
            return this.keys().stream().map(name->this.lla(name, t2u)).collect(Collectors.toList());
        }
        
        /**
         * 返回所有的列的数据的列表 <br>
         * 
         * 类型采用强制转换，因此可能会出现不同列之间的类型不一致的风险，使用时候需要注意。这一部分需要在编程中给注意与防范。<br>
         * 类库设置不予考虑。<br>
         * 
         * @param <T> 列的元数据类型
         * @return 列集合每个列嗾使一个U类型的列表
         */
        @SuppressWarnings("unchecked")
        public default <T> List<List<T>> columns(Class<T> targetClass){
            return this.keys().stream().map(name->this.lla(name,e->(T)e)).collect(Collectors.toList());
        }
        
        /**
         * 提取列值集合：统统返回List &lt;Object&gt;
         * 
         * @param t2u 列值转换函数:t->u
         * @return 列集合每个列嗾使一个U类型的列表
         */
        public default List<List<Object>> columns(){
            return this.keys().stream().map(name->this.lla(name,e->e)).collect(Collectors.toList());
        }
        
        /**
         * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个 记录集合的列表 rr：<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * <br>
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * <br>
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * 待分类的数据集合(rr)。即源数据,采用默认的this.rows()<br>
         * 指标计算器evaluator 列指标：分类结果的计算器，采用LittleTree::LIST<br>
         * <br>
         * @param keys 分类的key列表，分类依据字段列表。或者说 分类层级的序列
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public default IRecord pivotTable(final Object...keys) {
            return this.rows().stream().collect(pvtclc(keys));
        }
        
        /**
         * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个 记录集合的列表 rr：<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * <br>
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * <br>
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * 待分类的数据集合(rr)。即源数据,采用默认的this.rows()<br>
         * 指标计算器evaluator 列指标：分类结果的计算器，采用LittleTree::LIST<br>
         * <br>
         * @param keys 分类 的层级key列表，分类依据字段列表。或者说 分类层级的序列，采用',','\'和'/' 进行分隔,
         * null 或者空白字符，默认为record的keys()作为分类层级。
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public default IRecord pivotTable(final String keys) {
            final var kk = keys!=null && !keys.matches("\\s*") // 输入参数的有效性检查
                ? Arrays.stream(keys.split("[,\\\\/]+")).map(String::strip).toArray() // 转换成Object[]
                : null; // 分类 的层级key列表
            return this.rows().stream().collect(pvtclc(kk));
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 返回行列表:<br>
         *  final var dfm = REC( <br>
         *    "A",L("a","b","c"), // 第一列 <br>
         *    "B",L(1,2,3), // 第二列 <br>
         *    "C",A(2,4,6,10),// 第三列 <br>
         *    "D",REC(0,3,1,6,2,9),// 第四列 <br>
         *    "E",REC(0,31,1,61,2,91).toMap()// 第五列 <br>
         *  );// dfm
         *  
         *  返回:<br>
         *  A	B	C	D	E <br>
         *  a	1	2	3	31 <br>
         *  b	2	4	6	61 <br>
         *  c	3	6	9	91 <br>
         *  a	1	10	3	31 <br>
         *  
         * 按照列进行展示
         * 对DataFrame进行初始化
         * @param key_formatter 键名内容初始化
         * @param cell_formatter 键值元素内容初始化
         * @Return 格式化字符串
         */
        public default String toString2(
            final Function<Object,String> key_formatter,
            final Function<Object,String> cell_formatter) {
            
           final var builder = new StringBuilder();
           final var final_cell_formatter = cell_formatter!=null?cell_formatter:frt(2);
           final var final_key_formatter = key_formatter!=null?key_formatter:frt(2);
           builder.append(this.keys().stream().map(final_key_formatter).collect(Collectors.joining("\t"))+"\n");
           this.rows().forEach(rec->{
               builder.append(rec.values().stream()
                    .map(final_cell_formatter)
                    .collect(Collectors.joining("\t")));
               builder.append("\n");
           });// forEach
           return builder.toString();
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 返回行列表:<br>
         *  final var dfm = REC( <br>
         *    "A",L("a","b","c"), // 第一列 <br>
         *    "B",L(1,2,3), // 第二列 <br>
         *    "C",A(2,4,6,10),// 第三列 <br>
         *    "D",REC(0,3,1,6,2,9),// 第四列 <br>
         *    "E",REC(0,31,1,61,2,91).toMap()// 第五列 <br>
         *  );// dfm
         *  
         *  返回:<br>
         *  A   B   C   D   E <br>
         *  a   1   2   3   31 <br>
         *  b   2   4   6   61 <br>
         *  c   3   6   9   91 <br>
         *  a   1   10  3   31 <br>
         *  
         * 按照列进行展示
         * 对DataFrame进行初始化
         * @param cell_formatter 元素内容初始化
         * @Return 格式化字符串
         */
        public default String toString2(final Function<Object,String> cell_formatter) {
            return this.toString2(null,cell_formatter);
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 返回行列表:<br>
         *  final var dfm = REC( <br>
         *    "A",L("a","b","c"), // 第一列 <br>
         *    "B",L(1,2,3), // 第二列 <br>
         *    "C",A(2,4,6,10),// 第三列 <br>
         *    "D",REC(0,3,1,6,2,9),// 第四列 <br>
         *    "E",REC(0,31,1,61,2,91).toMap()// 第五列 <br>
         *  );// dfm
         *  
         *  返回:<br>
         *  A   B   C   D   E <br>
         *  a   1   2   3   31 <br>
         *  b   2   4   6   61 <br>
         *  c   3   6   9   91 <br>
         *  a   1   10  3   31 <br>
         *  
         * 按照列进行展示
         * 对DataFrame进行初始化
         * @Return 格式化字符串
         */
        public default String toString2() {
            return toString2(null);
        }
        
        /**
         * Unpivot a DataFrame from wide to long format, optionally leaving identifiers set <br>
         * 
         * @param mapper
         * @param hh
         * @param id_vars Column(s) to use as identifier variables.
         * @param value_vars Column(s) to unpivot. If not specified, uses all columns that are not set as id_vars.
         * @param var_namescalarName to use for the ‘variable’ column. If null use  ‘variable’.
         * @param value_name Name to use for the ‘value’ column.
         * @return Unpivoted DataFrame.
         */
        public default IRecord melt(
            final List<String> id_vars ,final List<String> value_vars ,
            final String var_name,final String value_name ){
            return _melt((s,e)->e,null,id_vars,value_vars,var_name, value_name);
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 术语来源于：R reshape, 接口 原型来源于 pandas
         * Unpivot a DataFrame from wide to long format, optionally leaving identifiers set <br>
         * 
         * @param mapper
         * @param hh
         * @param id_vars Column(s) to use as identifier variables.
         * @param value_vars Column(s) to unpivot. If not specified, uses all columns that are not set as id_vars.
         * @param var_namescalarName to use for the ‘variable’ column. If null use  ‘variable’.
         * @param value_name Name to use for the ‘value’ column.
         * @return Unpivoted DataFrame.
         */
        public default IRecord melt(
            final List<String> id_vars ,final List<String> value_vars){
            return _melt((s,e)->e,null,id_vars,value_vars,"variable","value");
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 术语来源于：R reshape, 接口 原型来源于 pandas
         * Unpivot a DataFrame from wide to long format, optionally leaving identifiers set <br>
         * 
         * @param mapper
         * @param hh
         * @param id_vars Column(s) to use as identifier variables.
         * @param value_vars Column(s) to unpivot. If not specified, uses all columns that are not set as id_vars.
         * @param var_namescalarName to use for the ‘variable’ column. If null use  ‘variable’.
         * @param value_name Name to use for the ‘value’ column.
         * @return Unpivoted DataFrame.
         */
        public default <T> List<IRecord> melt2recs(
            final BiFunction<String, Object, T> mapper,
            final List<String> hh,
            final List<String> id_vars ,final List<String> value_vars ,
            final String var_name,final String value_name ) {
            
            final var final_id_vars = id_vars;// identifier variables names
            final var final_value_vars = value_vars;// variables names
            final var final_var_name = var_name;// 
            final var final_value_name = value_name;
           
            
            final var idvars = final_id_vars.stream().toArray(String[]::new);
            final var items = this.rows(mapper, hh).stream().flatMap(rec->{// DataFrame  的行记录名称
                final var proto = rec.filter(idvars);// 制作原型数据
                return final_value_vars.stream().map(value_var->{
                    final var item = proto.duplicate();
                    item.add(final_var_name,value_var);
                    item.add(final_value_name,rec.get(value_var));
                    return item;
                });
            }).collect(Collectors.toList());
            
            return items;
        }
        
        /**
         * DataFrame 类型的数据方法,所谓DataFrame 是指键值对儿中的值为List的IRecord(kvs)<br>
         * 术语来源于：R reshape, 接口 原型来源于 pandas
         * 
         * Unpivot a DataFrame from wide to long format, optionally leaving identifiers set <br>
         * @param mapper
         * @param hh
         * @param id_vars Column(s) to use as identifier variables.
         * @param value_vars Column(s) to unpivot. If not specified, uses all columns that are not set as id_vars.
         * @param var_namescalarName to use for the ‘variable’ column. If null use  ‘variable’.
         * @param value_name Name to use for the ‘value’ column.
         * @return Unpivoted DataFrame.
         */
        public default <T> IRecord _melt(
            final BiFunction<String, Object, T> mapper,
            final List<String> hh,
            final List<String> id_vars ,final List<String> value_vars ,
            final String var_name,final String value_name ) {
            final var dfm = REC();// 生成一个DataFrame
            this.melt2recs(mapper, hh, id_vars, value_vars, var_name, value_name)
            .forEach(item->{
                item.kvs().forEach(p->{
                    dfm.computeIfAbsent(p.key(), _k->new LinkedList<Object>())
                    .add(p.value());
                });
            });// forEach
            
            return dfm;
        }
        
        /////////////////////////////////////////////////////////////////////
        // 以下是IRecord 的静态方法区域
        /////////////////////////////////////////////////////////////////////
        
         /**
         * 全连接连个rec1,rec2:返回一个 类型为{(key,(v1,v2)}的LinkedHashMap mm <br>
         * mm 中的key 与 rec1,rec2 中的key 相一致,可以这样理解mm是对rec1,rec2中的键值对儿按照key 进行分组，<br>
         * 每个分组是一个二元组,1号位置是rec1 中的元素 <br>
         * 2号位置是rec1中的元素，过rec1,rec2中没有对应与key的值,则对应key的值设置为null <br>
         * 
         * 全连接连个rec1,rec2 ,比如:<br>
         * var r1 = REC("1","A","2","B","4","D");<br>
         * var r2 = REC("1","a","2","b","3","c");<br>
         * var r3 = join(r1,r2);<br>
         * 返回：<br>
         * key tuple2
         * 1   A --> a <br>
         * 2   B --> b <br>
         * 4   D --> null <br>
         * 3   null --> c <br>
         * 
         * @param rec1 左边的记录(键值对儿集合)
         * @param rec2 右边的记录(键值对儿集合)
         * @return 连接后的连个字 {(key,(v1,v2)} 的集合，这是一个LinkedHashMap,是一个有序序列
         */
        public static Map<String,Tuple2<Object,Object>> join(final IRecord rec1,final IRecord rec2){
            // mm 中的key 与 rec1,rec2 中的key 相一致,可以这样理解mm是对rec1,rec2中的键值对儿按照key 进行分组，每个分组是一个二元组,1号位置是rec1 中的元素
            // 2号位置是rec1中的元素，过rec1,rec2中没有对应与key的值,则对应key的值设置为null
            final var mm = new LinkedHashMap<String,Tuple2<Object,Object>>();// 返回值:{(key,(v1,v2)}
            
            if(rec1!=null)rec1.foreach((k,v)->{// 遍历第一个record:k 是键名,v是record中与k对应的值
                //tuple_value 是一个用于存放字段连接结果的 二元组,1号位置rec1中的数据,2号位置rec2中对应的数据。遍历rec1号时候2号位置不予设置
                mm.compute(k, (key,tuple_value)->{// key 与  k 是同一个值,
                    if(tuple_value==null)tuple_value=TUP2(v,null); // 初次创建,2号位置不予设置
                    else tuple_value._1(v);// 该键名位置窜在
                    return tuple_value; // 返回当前的值
                }); // 遍历MAP
            });// rec1.foreach
            
            if(rec2!=null)rec2.foreach((k,v)->{// 遍历第一个record:k 是键名,v是record中与k对应的值
                //tuple_value 是一个用于存放字段连接结果的 二元组,1号位置rec1中的数据,2号位置rec2中对应的数据。遍历rec2号时候1号位置不予设置
                mm.compute(k, (key,tuple_value)->{// key 与  k 是同一个值,
                    if(tuple_value==null)tuple_value = TUP2(null,v); // 初次创建,1号位置不予设置
                    else tuple_value._2(v); // 已经存在则修改原有的值
                    return tuple_value; // 返回当前的值
                });// 遍历MAP
            });// rec2.foreach
            
            return mm;
        }
        
        /**
        * 数据窗口滑动<br>
        * 对一个:1 2 3 4,按照 size:为2,step为1的参数进行滑动的结果。<br>
        * 
        * | size | 每个窗口大小为 size,每次移动的步长为step<br>
        * [1    2]<br>
        * step0:[2    3]<br>
        *   - step1:[3    4]<br>
        *   -   -   step2:[4]<br>
        * 返回:[  [1,2],  [2,3],  [3,4],  [4] ]<br>
        * 
        * @param <T> 元素类型
        * @param ll 待操作的列表
        * @param size 滑动的窗口大小
        * @param step 每次移动的步长
        * @return 滑动窗口的列表。
        */
       public static <T> List<List<T>> sliding(List<T> ll,int size,int step) {
           int n = ll.size();
           final var aa = new ArrayList<T>(ll);// 转换成数组类型
           final var res = new LinkedList<List<T>>();// 返回结果
           for(int i=0;i<n;i+=step) {
              final var from = i;// 开始位置
              final var to = i+size;// 结束位置
              final var sl = aa.subList(from,to>=n?n:to);
              res.add(sl);
           }//for
           return res;
       }
       
       /**
         * 把一个信息路径：path分解成各个组分结构的IRecord:kvp 定义为(路径位点key,路径信息元)：例如 :<br> 
         * (path:中国/辽宁/大连,  keys:["country","province","city"]),给变换成:
         * REC("country","中国","province","辽宁","city","大连") <br>
         * <br>
         * @param path 信息路劲,即具有层级的信息结构,是一个路径信息元（信息单元）的序列,比如：中国/辽宁/大连
         * @param delim path的分隔符
         * @param keys 路径层级的名称：每个阶层应该如何称谓，比如对于path:中国/辽宁/大连，
         *         第一层是国家，第二层是省份，第三层是城市，层级称谓keys就是[国家,省份,城市]
         * @return IRecord
         */
        public static IRecord path2kvs(String path,String delim,String keys[]) {
            final var rec = REC();
            final var pp = path.split(delim);
            final var n = Math.min(keys.length,pp.length);
            Stream.iterate(0,i->i<n,i->i+1).forEach(i->rec.add(keys[i],pp[i+1]));
            return rec;
        }

        /**
         * 把一个信息路径：path分解成各个组分结构的IRecord:kvp 定义为(路径位点key,路径信息元)：例如 :<br> 
         * (path:中国/辽宁/大连,  keys:["country","province","city"]),给变换成:
         * REC("country","中国","province","辽宁","city","大连") <br>
         * <br>
         * @param path 信息路劲,即具有层级的信息结构,是一个路径信息元（信息单元）的序列,比如：中国/辽宁/大连
         * 信息单元的分隔符： delim 默认为 "[,/\\]+" <br> 
         * @param keys 路径层级的名称：每个阶层应该如何称谓，比如对于path:中国/辽宁/大连，
         * 第一层是国家，第二层是省份，第三层是城市，层级称谓keys就是[国家,省份,城市]
         * @return IRecord
         */
        public static IRecord path2kvs(String path,String keys[]) {
            final var rec = REC();
            final var delim = "[,/\\\\]+";
            final var pp = path.split(delim);
            final var n = Math.min(keys.length,pp.length);
            Stream.iterate(0,i->i<n,i->i+1).forEach(i->rec.add(keys[i],pp[i+1]));
            return rec;
        }

        /**
         * 这是专门为pv2rec_eval 函数设计的值变换函数：所以采用采用value2kvs 而不是 obj2rec这样的名称。<br>
         * 把一个对象转换一个IRecord:kvs 是IRecord的别名与IRecord可以互换 <br>
         * 
         * 用指定的key 去构建一个Record,确保返回的结果中含有一个 名为key 键。<br>
         * 
         * @param value 值对象
         * @param key 值名称
         * @return IRecord IRecord 即键值对 kvs 
         */
        public static IRecord value2kvs(Object value,String key) {
            if(value instanceof IRecord)return (IRecord)value;
            if(value instanceof Map)return REC( (IRecord)value);
            return REC(key,value);
        }

        /**
         * 这是专门为pv2rec_eval 函数设计的值变换函数：所以采用采用value2kvs 而不是 obj2rec这样的名称。<br>
         * 把一个对象转换陈一个IRecord:kvs 是IRecord的别名与IRecord可以互换<br>
         * 
         * @param value 值对象
         * @return IRecord 即键值对 kvs 
         */
        public static IRecord value2kvs(Object value) {
                return value2kvs(value,"value");
        }

        /**
        * 层级信息的维护合并 path,value 计算 即 (path,value) 的变换为IRecord（键值对集合)<br>
        * 把 path 通过 pathkeys 进行变换<br>
        * 把value 通过value_key 进行变换<br>
        * 比如对于  (path:iphone6/苹果公司,value:6800) 进行pv2rec_eval("产品名/生产企业","价格") 计算 即:<br>
        * pv2rec_eval("产品名/生产企业","价格").apply("iphone6/苹果公司",6800);<br>
        * 就会返回：(产品名:iphone6,生产企业:苹果公司,价格:6800) 的一个IRecord 即kvs<br>
        * 
        * 使用示例：<br>
        * var r = res.dfs_eval_forone(pv2rec_eval("pct,vendor".split(","),"value"));<br>
        * path,vlaue 转record 的 计算器 <br>
        * 
        * @param pathkeys 路径 keys, 使用"[,/\\\\]+"进行分割 
        * @param value_key 值 的key名
        * @return BiFunction<String,Object,IRecord>
        */
       public static BiFunction<String,Object,IRecord> pv2rec_eval(
           final String pathkeys,String value_key) {
           return pv2rec_eval(pathkeys.split("[,/\\\\]+"),value_key);
       }
       
       /**
        * 层级信息的维护合并 path,value 计算 即 (path,value) 的变换为IRecord（键值对集合)<br>
        * 把 path 通过 pathkeys 进行变换<br>
        * 把value 通过value_key 进行变换<br>
        * 比如对于  (path:iphone6/苹果公司,value:6800) 进行pv2rec_eval("产品名/生产企业","价格") 计算 即:<br>
        * pv2rec_eval("产品名/生产企业","价格").apply("iphone6/苹果公司",6800);<br>
        * 就会返回：(产品名:iphone6,生产企业:苹果公司,价格:6800) 的一个IRecord 即kvs<br>
        * 
        * 使用示例：<br>
        * var r = res.dfs_eval_forone(pv2rec_eval("pct,vendor".split(","),"value"));<br>
        * path,vlaue 转record 的 计算器 <br>
        * 
        * @param pathkeys 路径 keys 对层级信息(path)进行分解：维度分解的键名序列
        * @param value_key 值 key 值键名
        * @return BiFunction<String,Object,IRecord> 一个 path,vlaue 转record 的 计算器
        */
        public static BiFunction<String,Object,IRecord> pv2rec_eval(
            final String[] pathkeys,String value_key) {
            return (path,value)->{
                var rec = REC();
                rec.add(path2kvs(path,pathkeys)); // 路径信息转换成 IRecord 键值对儿
                rec.add(value2kvs(value,value_key)); // value 信息转换成键值对儿
                return rec;
            };
        }
        
        /**
         * 遍历IRecord 深度遍历  <br>
         * 
         * @param rec 遍历的对象
         * @param cons biCONSumer 回调函数, 即KV值的处理函数( key,values), 需要注意cons是一个二元函数。
         * key 是指节点的路径信息; values 是指与key所对应的值的*集合*：这个集合是Stream 类型的；
         * 之所以采用Stream来封装值数据，是为统一单值与集合(Collection)类型的值数据。stream 读一下只返回一个值。单值与多值是一样的。
         * @param prefix 键值的前缀,null表示"/"，即根节点
         * @param delim 层级分隔符号,null表示"/"
         */
        @SuppressWarnings("unchecked")
        public static void dfs(final IRecord rec,final BiConsumer<String,Stream<Object>> cons,
            final String prefix,final String delim) {
            final var default_prefix = "/";// 默认的根前缀（第0基层的前缀）
            final var default_delim = "/";// 默认的阶层分隔符号
            final var final_prefix = (prefix==null?default_prefix:prefix);// 生成可用的不可变对象
            final var final_delim = (delim==null?default_delim:delim);// 生成可用的不可变对象
            final Function<String,String> new_prefix = (k)->{// 更具节点名称(key)生成下一阶层的名称前缀。
                var d = ((default_prefix.equals(final_prefix))?"":final_delim);
                return final_prefix+d+k; // 新的阶层前缀。
            };
                
            rec.foreach((k,v)->{
                Stream<Object> stream = null;// 流是对单值对象与Collection的统一描述。这是dfs所规定的默认数据）数值的访问方法。
                if(v instanceof Collection) {// 集合类型给与展开
                    stream = ((Collection<Object>)v).stream();
                }else if(v instanceof Stream) {// 流类型不给予展开因为展开后就不再可以用了。
                    // stream = ((Stream<Object>)v);
                }else if(v instanceof IRecord) {
                    dfs((IRecord)v,cons,new_prefix.apply(k),null);// 修改前缀，递归进入更深一层
                }else if(v instanceof Map) {
                    var r = REC();// 把map  转换成 IRecord
                    ((Map<Object,Object>)v).forEach((key,value)->r.add(key.toString(),value));
                    if(r.size()>0)dfs(r,cons,new_prefix.apply(k),delim) ;// 修改前缀，递归进入更深一层
                    else stream = new LinkedList<>().stream(); // 空值列表
                }else {// 默认对象绑定。
                    stream = Stream.of(v);// 生成一个单值的流
                }// if
                
                // 回调函数处理
                if(stream!=null)cons.accept(new_prefix.apply(k),stream);
            });// record 对象遍历
        }
        
        /**
         * SUPplier<LinkedList>的一个简写：这是一个容器生运算符 <br>
         * 生成一个 列表：使用示例 <br>
         * e.collect(supll(Object.class), 
         *  (List<Object>aa, KVPair<String,Object>a)->{aa.add( MFT( "{0}[{1}]", a.key(),a.value() ) );}, <br>
         *  cbll(Object.class) <br>
         * ).stream().map(f->f+"").collect(Collectors.joining("*")), <br>
         *   
         * @param <R> 容器中的元素的类型 
         * @param clazz 容器中的元素的类型的class
         * @return 新建列表
         */
        public static <R> Supplier<List<R>> supll(Class<R> clazz){
            return ()->new LinkedList<R>();
        }
        
        /**
         * ComBiner<LinkedList> 的一个简写：这是一个操作运算符
         * e.collect(supll(Object.class), 
         *  (List<Object>aa, KVPair<String,Object>a)->{aa.add( MFT( "{0}[{1}]", a.key(),a.value() )   );},
         *   cbll(Object.class)
         *  ).stream().map(f->f+"").collect(Collectors.joining("*")),
         *  
         * @param <R> 容器中的元素的类型
         * @param clazz 容器中的元素的类型的class
         * @return 新列表
         */
        public static <R> BinaryOperator<List<R>> cbll(Class<R> clazz){
            return (List<R> aa,List<R>bb)->{aa.addAll(bb);return aa;};
        }
           
        /**
         * 把一个键值对的数据转换成一个R的类型
         * 
         * @param <R> 容器中的元素的类型
         * @param obj2r 把键值对儿kv 转换成 目标对象R的函数
         * @return R类型的数据对象
         */
        public static <R> Function<KVPair<String,Object>,R> kv2r(final Function<Object,R>obj2r){
            return (kv)->obj2r.apply(kv.value());
        }
        
        /**
         * 合并两个 Record：即这个一个按照键名对连个 键值对儿集合进行 的 并集操作。
         * 
         * @param rec1 IRecord 记录1
         * @param rec2 IRecord 记录2
         * @param rec2_appendto_rec1 是否把 rec2 追加到rec1, 是 追加,否 合并  生成了一个新的对象。
         * @return 合并了rec1 和 rec2 两个记录内容键值儿 后的Record
         */
        public static IRecord union(final IRecord rec1,final IRecord rec2,final boolean rec2_appendto_rec1) {
            var rec = rec2_appendto_rec1?rec1:REC();
            if(rec1!=null && rec!=rec1)rec1.foreach(rec::add);
            if(rec2!=null)rec2.foreach(rec::add);
            return rec;
        }

        /**
         * 格式化记录列表
         * @param recs 记录列表
         * @return
         */
        public static String format(final List<IRecord> recs) {
            return format(recs,"\t");
        }
        
        /**
         * 格式化记录列表
         * @param recs 记录列表
         * @param sep 分隔符
         * @return
         */
        public static String format(final List<IRecord> recs,final String sep) {
            final var buffer = new StringBuffer();
            @SuppressWarnings("unused")
            final var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(recs==null)return "";
            int n = recs.size();
            List<String> hh = new ArrayList<>(n);// 表头字段顺序
            for(int i=0;i<n;i++){
                if(i==0) {
                    IRecord r = recs.get(0);
                    buffer.append(recs.get(i).stream().map(kv->kv.key()+"")
                        .collect(Collectors.joining(sep))+"\n");
                    hh = MAP(r.kvs(),e->e.key());//只提取第一个记录的结构字段
                }//if
                
                final var line = new StringBuffer();
                // 之所以使用 hh,而不是kvs collect joining 是为了保持异质的record 集合。即里那个中不同结构的record混合list
                for(var h:hh) {
                    var obj = recs.get(i).get(h);
                    if(obj==null) obj = "(null)";// 空数值变换
                    line.append(obj+sep);
                }
                buffer.append(line.toString().trim()+"\n");
            }//for
            return buffer.toString();
        }
        
        /**
         * 格式化数据对象
         * @return
         */
        public static String FMT(final List<IRecord> recs) {
            return format(recs);
        }
        
        /**
         * 格式化数据对象,带有行号的格式化
         * @return 带有行号的格式化
         */
        public static String FMT2(final List<IRecord> recs) {
           final var line =  format(recs);
           final var seed = new AtomicLong(0l);// 行号生成器的种子
           final Supplier<String> rownum = ()->{// 行号生成器
               final var n = seed.getAndIncrement();
               return n==0 ? "#" : n+"";// 行号生成器
           };
           return JOIN( Arrays.stream(line.split("\n")).map(e->MFT("{0}\t{1}",rownum.get(),e)),// 行格式化
                   "\n");// JOIN 行连接
        }

        /**
         * 格式化数据对象
         * @return
         */
        public static String FMT2(final List<IRecord> recs,final String sep) {
            return format(recs,sep);
        }

        /**
         * 获取指定Class的字段列表
         * @param clsu 类对象
         * @return clsu的字段列表
         */
        public static <U> Map<String,Field> FIELDS(final Class<U> clsu) {
            Map<String,Field> map = new LinkedHashMap<>();
            Arrays.stream(clsu.getDeclaredFields()).forEach(e->{
                map.put(e.getName(), e);
            });
            return map;
        }

        /**
         * 把一个java object的值类型 转换成 获取对应SQL类型
         * @return obj对应的SQL数据类型。
         */
        public static String OBJ2SQLTYPE(final Object obj) {
            return OBJ2SQLTYPE(obj,null);
        }
        
        /**
         * 把对象转换成key->value对儿
         * @param obj 数据对象
         * @return key->value对儿集合的IRecord
         */
        public static IRecord OBJ2REC(final Object obj){
            return OBJ2REC(obj,(Predicate<Field>)null);
        }
        
        /**
         * 把对象转换成key->value对儿
         * @param obj 数据对象
         * @param pfilter 字段过滤,只返回返回结果为true的结果字段.
         * @return key->value对儿
         */
        public static IRecord OBJ2REC(final Object obj,final Predicate<Field> pfilter){
            return REC(OBJ2KVS(obj,pfilter==null?(t)->true:pfilter));
        }
        
        /**
         * 把对象转换成key->value对儿
         * @param obj 数据对象
         * @param keys 字段过滤数组,只返keys中字段.
         * @return key->value对儿
         */
        public static IRecord OBJ2REC(final Object obj,String keys[]){
            final var kk = Arrays.asList(keys);
            return REC(OBJ2KVS(obj,e->kk.contains(e.getName())));
        }
        
        /**
         * 把对象转换成key->value对儿
         * @param obj 数据对象
         * @param keys 字段过滤,只返keys中字段. keys 中采用"[,\\s，/\\\\]+"的分隔符进行分隔。 
         * @return key->value对儿
         */
        public static IRecord OBJ2REC(final Object obj,String keys){
            final var kk = Arrays.asList(keys.split("[,\\s，/\\\\]+"));
            return REC(OBJ2KVS(obj,e->kk.contains(e.getName())));
        }
        
        /**
         * 把对象转换成key->value对儿
         * 分解一个对象obj成为键值对儿集合即IRecord 记录。
         * 
         * @param obj 待分解的数据对象
         * @param pfilter 字段过滤,只返回返回结果为true的结果字段. 当pfilter 为null 不对字段进行过滤。
         * @return key->value对儿
         */
        public static Map<String,Object> OBJ2KVS(final Object obj,final Predicate<Field> pfilter){
            final var mm = new LinkedHashMap<String,Object>();
            final Predicate<Field> _pfilter = pfilter==null?e->true:pfilter;
            
            if(obj!=null)Arrays.stream(obj.getClass().getDeclaredFields())
            .filter(_pfilter)
            .forEach(fld->{
                fld.setAccessible(true);
                Object v = null;
                try{v = fld.get(obj);}catch(Exception e) {};
                mm.put(fld.getName(),v);
            });
            
            return mm;
        }
        
        /**
         * 用inits初始化对象obj
         * 数据初始化,使用 inits 来初始化对象obj,用inits中的key的值设置obj中的对应的字段 
         * @param objClass 待初始对象的类型class
         * @param inits 初始源数据
         */
        public static<T> T OBJINIT(final Class<T> objClass,final IRecord inits) {
            if(objClass==null||inits==null)return null;
            return OBJINIT(objClass,inits.toMap());
        }
        
        /**
         * 用inits初始化obj
         * 数据初始化,使用 inits 来初始化对象obj,用inits中的key的值设置obj中的对应的字段 
         * @param objClass 待初始对象的类型class
         * @param inits 初始源数据
         */
        public static<T> T OBJINIT(final Class<T> objClass,final Map<String,Object> inits) {
            if(inits==null)return null;
            T obj = null;
            try {
                obj = objClass.getConstructor((Class[])null).newInstance((Object[])null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(obj==null)return null;
            return OBJINIT(obj,inits);
        }
        
        /**
         * 用inits初始化对象obj
         * 数据初始化,使用 inits 来初始化对象obj,用inits中的key的值设置obj中的对应的字段 
         * @param obj 待初始对象
         * @param inits 初始源数据
         */
        public static<T> T OBJINIT(final T obj,final IRecord inits) {
            if(inits==null||obj==null)return null;
            return OBJINIT(obj,inits.toMap());
        }
        
        /**
         * 数据初始化,使用 inits 来初始化对象obj,用inits中的key的值设置obj中的对应的字段:带有简单的类型转换功能 
         * @param obj 待初始对象
         * @param inits 初始源数据
         * @param <T> 待初始化的对象的类型
         */
        public static<T> T OBJINIT(final T obj,final Map<String,?>inits) {
            
            Arrays.stream(obj.getClass().getDeclaredFields()) // 依次处目标对象结构的各个字段属性
            .filter(e->inits.keySet().contains(e.getName())).forEach(fld->{// 字段数值的设置
                try {
                    fld.setAccessible(true);
                    final Object value=inits.get(fld.getName());
                    if(value==null)return;
                    
                    /*基本类型与包裹类之间的转换*/{
                        // 这里关于类型的转换没有一个好的实现办法，目前来看只能一个一个的写出来。
                        final Class<?> src = value.getClass();// 源类型
                        final Class<?> target = fld.getType();// 目标类型
                        //System.out.println(fld.getName()+"("+value+")["+target+"<-"+src+"]");
                        if( (target ==int.class||target == Integer.class) && 
                            (src == Long.class || src == long.class) ) { // 长整类型换 整形
                            fld.set(obj,((Long)value).intValue());return;
                        }
                        if( (target ==long.class||target == Long.class) && 
                            (src == int.class || src == Integer.class) ) { // 长整类型换 整形
                            fld.set(obj,((Number)value).longValue());return;
                        }
                        
                        // 从timestamp 转换为其他类型
                        if(src==Timestamp.class && target==Date.class){// Timestamp -> Date
                            final Date d = ((Timestamp)value);
                            fld.set(obj,d);return;
                        }else if(src==Timestamp.class && target==LocalDate.class){// Timestamp -> LocalDate
                            final Date d = ((Timestamp)value);
                            fld.set(obj,CronTime.dt2ld(d));return;
                        }else if(src==Timestamp.class && target==LocalDateTime.class){// Timestamp -> LocalDateTime
                            final Date d = ((Timestamp)value);
                            fld.set(obj,CronTime.dt2ldt(d));return;
                        }else if(src==Timestamp.class && target==LocalTime.class){// Timestamp -> LocalTime
                            final Date d = ((Timestamp)value);
                            fld.set(obj,CronTime.dt2lt(d));return;
                        }
                        
                        if(src==Date.class && target==Timestamp.class){// Date->Timestamp
                            final Timestamp d =new Timestamp(((Date) value).getTime());
                            fld.set(obj,d);return;
                        }else if(src==LocalDateTime.class && target==Timestamp.class){// LocalDateTime->Timestamp
                            final Timestamp d =new Timestamp(CronTime.ldt2dt((LocalDateTime)value).getTime());
                            fld.set(obj,d);return;
                        }else if(src==LocalDate.class && target==Timestamp.class){// LocalDateTime->Timestamp
                            final Timestamp d =new Timestamp(CronTime.ld2dt((LocalDate)value).getTime());
                            fld.set(obj,d);return;
                        }else if(src==LocalTime.class && target==Timestamp.class){// LocalDateTime->Timestamp
                            final Timestamp d =new Timestamp(CronTime.lt2dt((LocalTime)value).getTime());
                            fld.set(obj,d);return;
                        }
                    }// /*基本类型与包裹类之间的转换*/
                    
                    if(fld.getType() == value.getClass()) {
                        fld.set(obj,value);
                    }else if (value instanceof String){// 对字符串类型尝试做类型转换
                        if(fld.getType()==Character.class || fld.getType()==char.class) {// 数字
                            fld.set(obj, (value.toString().charAt(0)));
                        } else if(fld.getType()==Integer.class || fld.getType()==int.class) {// 数字
                            fld.set(obj, ((Double)Double.parseDouble(value.toString())).intValue());
                        } else if(fld.getType()==Double.class || fld.getType()==double.class) {// 数字
                            fld.set(obj, Double.parseDouble(value.toString()));
                        } else if(fld.getType()==Float.class || fld.getType()==float.class ) {// 数字
                            fld.set(obj, Float.parseFloat(value.toString()));
                        } else if(fld.getType()==Short.class || fld.getType()==short.class) {// 数字
                            fld.set(obj, Short.parseShort(value.toString()));
                        }  else if(fld.getType()==Boolean.class || fld.getType()==boolean.class) {// 数字
                            fld.set(obj, Boolean.parseBoolean(value.toString()));
                        } else if(fld.getType()==Long.class || fld.getType()==long.class) {// 数字
                            fld.set(obj, ((Number)Double.parseDouble(value.toString())).longValue());
                            //System.out.println(obj+"===>"+value);
                        } else if(fld.getType() == Date.class||
                            fld.getType() == LocalDate.class ||
                            fld.getType() == LocalDateTime.class||
                            fld.getType() == LocalTime.class) {// 时间类型的处理。
                            
                            Date date = null;// 日期对象
                            if(value instanceof Number) {
                                final long time = ((Number)value).longValue();
                                date = new Date(time);
                            } else {
                                final String ss[] = "yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm,yyyy-MM-dd HH,yyyy-MM-dd,yyyy-MM,yyyy-MM,yyyy"
                                    .split("[,]+");
                                for(String s:ss) {
                                    try {date = new SimpleDateFormat(s).parse(value.toString());}catch(Exception ex) {};
                                    if(date!=null)break;
                                }//for
                            }// if( value instanceof Number
                            // 设置时间字段
                            
                            if(fld.getType() == LocalDate.class) {// LocalDate
                                fld.set(obj,CronTime.dt2ld(date));
                            }else if(fld.getType() == LocalDateTime.class) {// LocalDateTime
                                fld.set(obj,CronTime.dt2ldt(date));
                            }else if(fld.getType() == LocalTime.class) {// LocalTime
                                fld.set(obj,CronTime.dt2lt(date));
                            }else {
                                fld.set(obj,date);
                            }// if(fld.getType() == LocalDate.class) 
                            
                        }// else if(fld.getType() == Date.class)
                        
                    }// if(fld.getType() == value.getClass())
                }catch(Exception ex) {ex.printStackTrace();}
            });// forEach(fld
            
            return obj;
        }// OBJINIT

        /**
         * 把一个java object的值类型 转换成 获取对应SQL类型
         * @param size 类型的尺寸大小
         * @return
         */
        public static String OBJ2SQLTYPE(Object obj,Integer size) {
            if(obj==null)return null;
            Class<?> cls = null;
            cls = (obj instanceof Class<?>)?(Class<?>)obj:obj.getClass();
            final var mm= new HashMap<Object,String>();
            mm.put(byte.class, "char(1)");mm.put(Byte.class, "char(1)");
            mm.put(char.class, "int");mm.put(Character.class, "int");
            mm.put(int.class, "int");mm.put(Integer.class, "int");
            mm.put(long.class, "long");mm.put(Long.class, "long");
            mm.put(short.class, "int");mm.put(Short.class, "int");
            mm.put(double.class, "double");mm.put(Double.class, "double");
            mm.put(float.class, "double");mm.put(float.class, "double");
            mm.put(boolean.class, "tinyint(1)");mm.put(boolean.class, "tinyint(1)");
            if(size!=null && cls == String.class )
                mm.put(String.class, "varchar("+size+")");
            else
                mm.put(String.class, "TEXT");
            mm.put(StringBuffer.class, "TEXT");
            mm.put(Date.class, "timestamp");
            
            return mm.get(cls);
        }
        
        /**
         * 具体了的REC函数的实现：为了保证REC的美观性与不限定参数的性质。
         * 标准版的记录生成器, map 生成的是LinkedRecord
         * @param values:key1,value1,key2,value2,....
         * @return IRecord对象
         */
        public static IRecord _REC(Object[] values) {
            final LinkedRecord rec = new LinkedRecord();
            if(values==null)return rec;
            if(values.length==1) {// 单个元素
                final Object o = values[0];
                return (o!=null && o instanceof Map)// (key,value) 序列
                ? LinkedRecord.of((Map<?,?>)o)
                : rec;
            }
            
            for(int i=0;i<values.length;i+=2) {
                String key = values[i]==null?"null":values[i].toString();// 键值名
                Object value = (i+1)<values.length?values[i+1]:null;//如果最后一位是key则它的value为null
                rec.data.put(key, value);
            }//for
            
            return rec;
        }
        
        /**
         * 标准版的记录生成器, map 生成的是LinkedRecord
         * @param map:key1,value1,key2,value2,....
         * @return IRecord对象
         */
        public static IRecord REC(Map<?,?>map) {
            final LinkedRecord rec = new LinkedRecord();
            map.forEach((k,v)->rec.data.put(k.toString(), v));
            return rec;
        }
        
        /**
         * 标准版的记录生成器, map 生成的是LinkedRecord
         * @param values:key1,value1,key2,value2,....
         * @return IRecord对象
         */
        public static IRecord REC(final Object ... values) {
            return _REC(values);
        }
        
        /**
         * 把一个键值对儿
         * @param kvp 键值对儿序列
         * @return IRecord
         */
        @SafeVarargs
        public static IRecord REC(KVPair<String,Object>...kvps) {
            final var rec = new LinkedRecord();
            Stream.of(kvps).forEach(p->{
                rec.add(p.key(),p.value());
            });
            return rec;
        }
        
        /**
         * 把一个数组元素尊换成一个IRecord对象
         * 键值对序列为[key_i,tt[i]]
         * @param tt 数组元素对象
         * @param t2key 元素转换成key,把一个tt[i]->key_i,进而成为一个键值对儿[key_i,tt[i]]
         * @return IRecord对象
         */
        public static <T> IRecord A2REC(final T[] tt, final Function<T,String>t2key) {
            return REC(Stream.of(tt).flatMap(e->Stream.of(t2key.apply(e),e)).toArray());
        }
        
        /**
         * 把一个数组元素尊换成一个IRecord对象
         * 键值对序列为[i,tt[i]],i从0开始
         * @param <T> 数组元素类型
         * @param tt 数组元素对象
         * @return IRecord对象
         */
        public static <T> IRecord A2REC(final T[] tt) {
            final var aint = new AtomicInteger(0);
            final Function<Object,String> t2key=o->aint.getAndIncrement()+"";
            return REC(Stream.of(tt).flatMap(e->Stream.of(t2key.apply(e),e)).toArray());
        }
        
        /**
         * A2REC 的别名
         * 把一个数组元素尊换成一个IRecord对象
         * 键值对序列为[i,tt[i]],i从0开始
         * @param <T> 数组元素类型
         * @param tt 数组元素的序列
         * @return IRecord对象
         */
        public static <T> IRecord A2R(@SuppressWarnings("unchecked") final T ... tt) {
            return A2REC(tt);
        }
        
        /**
         * 用一个IRecord 来表示一个点数据Point，键名为"0"
         * 把一个对象转换成Object: Pair:{0,Object}
         * @param obj 数据对象
         * @return 键名为"0" 的IRecord
         */
        public static IRecord P(Object obj) {
            return REC("0",obj);// 生成默认键名为0
        }
        
        /**
         * 解析一个字符串数据，把它拆分成字符串数组
         * 然后把该数组元素尊换成一个IRecord对象
         * 键值对序列为[i,tt[i]],i从0开始
         * 
         * 例如：STRING2REC("1,2,3,4","[,/\\\\]+") 生成 {0:1,1:2,2:3,3:4}
         * 
         * @param line 带解析的数据行
         * @param delim 分隔符,分隔符好的正则表达式。比如 "[,/\\\\]+" 表示  使用  “,” “/” “\” 作为分隔符号。
         * @return 解析行的line而得到的IRecord
         */
        public static IRecord STRING2REC( final String line, final String delim) {
           return A2REC(line.split(delim));
        }
        
        /**
         * 解析一个字符串数据，把它拆分成字符串数组<br>
         * 然后把该数组元素尊换成一个IRecord对象<br>
         * 键值对序列为[i,tt[i]],i从0开始<br>
         * 
         * 例如：STRING2REC("1,2,3,4","[,/\\\\]+") 生成 {0:1,1:2,2:3,3:4}<br>
         * 
         * 默认的delim "[,/\\\\]+"<br>
         * @param line 带解析的数据行
         * @return 解析行的line而得到的IRecord
         */
        public static IRecord STRING2REC(final String line) {
           return A2REC(line.split("[,/\\\\]+"));
        }
        

        /**
         * 把一个数组元素尊换成一个IRecord对象<br>
         * 键值对序列为[key_i,tt[i]] <br>
         * 
         * @param <T> 数组元素类型
         * @param ss 数组元素对象
         * @param s2key 元素转换成key,把一个tt[i]->key_i,进而成为一个键值对儿[key_i,tt[i]]
         * @return IRecord对象
         */
        public static <T> IRecord STREAM2REC(final Stream<T> ss, final Function<T,String>s2key) {
            return REC(ss.flatMap(e->Stream.of(s2key.apply(e),e)).toArray());
        }
        
        /**
         * 把一个元素流转换成一个IRecord对象
         * 键值对序列为[i,tt[i]],i从0开始
         * 
         * @param <T> 数组元素类型
         * @param ss 数组元素对象
         * @return IRecord对象
         */
        public static <T> IRecord STREAM2REC(final Stream<T> ss) {
            final var aint = new AtomicInteger(0);
            final Function<Object,String> t2key=o->aint.getAndIncrement()+"";
            return REC(ss.flatMap(e->Stream.of(t2key.apply(e),e)).toArray());
        }
        
        /**
         * 把一个列表转换成一个IRecord对象
         * 键值对序列为[key_i,tt[i]]
         * 
         * @param <T> 数组元素类型
         * @param ll 列表元素对象
         * @param t2key 元素转换成key,把一个tt[i]->key_i,进而成为一个键值对儿[key_i,tt[i]]
         * @return IRecord对象
         */
        public static <T> IRecord L2REC(final List<T> ll, final Function<T,String>t2key) {
            return REC(ll.stream().flatMap(e->Stream.of(t2key.apply(e),e)).toArray());
        }
        
        /**
         * 把一个列表转换成一个IRecord对象,
         * 键值对序列为[i,tt[i]],i从0开始
         * @param <T> 数组元素类型
         * @param ll 数组元素对象:
         * @return IRecord对象
         */
        public static <T> IRecord L2REC(final List<T> ll) {
            var aint = new AtomicInteger(0);
            final Function<Object,String> t2key=o->aint.getAndIncrement()+"";
            return REC(ll.stream().flatMap(e->Stream.of(t2key.apply(e),e)).toArray());
        }
        
        /**
         *  把一个KVS列表转换成一个IRecord对象,
         * @param kvs 键值列表
         * @return IRecord对象
         */
        public static <T> IRecord KVS2REC(final List<KVPair<String,T>> kvs) {
           return KVS2REC(kvs.stream());
        }
        
        /**
         *  把一个KVS列表转换成一个IRecord对象,
         * @param kvs 键值列表
         * @return IRecord对象
         */
        public static <T> IRecord KVS2REC(final Stream<KVPair<String,T>> kvsStream) {
           final IRecord rec = REC();
           if(kvsStream!=null)kvsStream.forEach(kvp->{
               rec.add(kvp.key(),kvp.value());
           });
           return rec;
        }
        
        /**
         * 标准版的记录生成器, map 生成的是LinkedRecord
         * @param values:key1,value1,key2,value2,....
         * @return IRecord对象
         */
        public static IRecord R(final Object ... values) {
            return REC(values);
        }
        
        /**
         * 制作一个列表
         * @param <T>
         * @param tt 列表元素
         * @return List<T>
         */
        @SafeVarargs
        public static <T> List<T> L(final T...tt) {
            return Arrays.asList(tt);
        }
        
        /**
         * 制作一个数组
         * @param tt 列表元素
         * @return Object[]
         */
        public static Object[] A(final Object...tt) {
            return Arrays.asList(tt).toArray();
        }
        
        /**
         * 制作一个Map<String,Object>
         * @param tt 列表元素
         * @return Map<String,Object>
         */
        public static Map<String,Object> M(final Object...tt) {
            return _REC(tt).toMap();
        }
        
        /**
         * 等价函数
         */
        @SuppressWarnings("unchecked")
        public static <T> Function<Object,T> identity(final Class<T> tcls){
            return (Object o)->(T)o;
        }
        
        /**
         * 行化操作：数据分析类 需要与DataMatrix 相结合生成 data.frame类型的 转换函数
         * 
         * rr:Collection<IRecord> recs 记录集合
         * row:Map 行记录
         * S:结果类型为Stream
         * 主要用途就是 完成 IRecord 向 DataMatrix的转换，但是为了保证DataMatrix 与IRecord 的独立。而设置这个函数。比如
         * var dm = new DataMatrix<> (rr2rowS(recs),Integer.class); 就构造了一个 DataMatrix 对象。
         * 
         * @param recs record 集合
         * @return 生成一个hashmap 的集合
         */
        
        public static <T> Stream<LinkedHashMap<String,T>> rr2rowS(final Collection<IRecord> recs) {
           return rr2rowS(recs.stream());
        }

        /**
         * 行化操作：数据分析类 需要与DataMatrix 相结合生成 data.frame类型的 转换函数
         * 
         * rr:Collection<IRecord> recs 记录集合
         * row:Map 行记录
         * S:结果类型为Stream
         * 主要用途就是 完成 IRecord 向 DataMatrix的转换，但是为了保证DataMatrix 与IRecord 的独立。而设置这个函数。比如
         * var dm = new DataMatrix<> (rr2rowS(recs),Integer.class); 就构造了一个 DataMatrix 对象。
         * 
         * @param stream records 的集合
         * @return 生成一个hashmap 的集合
         */
        @SuppressWarnings("unchecked")
        public static <T> Stream<LinkedHashMap<String,T>> rr2rowS(final Stream<IRecord> stream) {
            return (Stream<LinkedHashMap<String,T>>)(Object) stream.map(e -> e.toLhm());
        }

        /**
         * 行化操作：数据分析类 需要与DataMatrix 相结合生成 data.frame类型的 转换函数
         * r:record 单条记录
         * row:Map 行记录
         * s:结果类型为Stream 
         * 主要用途就是 完成 IRecord 向 DataMatrix的转换，但是为了保证DataMatrix 与IRecord 的独立。而设置这个函数。比如
         * var dm = new DataMatrix<> (rr2rowS(recs),Integer.class); 就构造了一个 DataMatrix 对象。
         * 
         * @param rec 的单元素：但也把他视为只有一个元素的集合。
         * @return 生成一个hashmap 的集合
         */
        @SuppressWarnings("unchecked")
        public static Stream<Map<String, ?>> r2rowS(final IRecord rec) {
            return (Stream<Map<String, ?>>) (Object) Stream.of(rec.toEscapedMap());
        }
        
        /**
         * 一般用法就是对于一个 Records List,通过某种某种classifier:根分组成一个map 这是一个 List 转Map的方法。
         * 需要注意 classifier 需要保证不会把由多个元素给到相同组。所以classifier 一般就是Records 记录的候选主键：比如,id,name 之类
         * 可以唯一标识 record的字段属性。 
         * 
         * 比如:
         * final var rr = jdbc.sql2records(sql);
         * final var map = rr.stream().collect(Collectors.groupingBy(e->e.str("name"),// name 唯一标识e 
         *    atomic_collector(e->e.get("cnt"))));// rr->map
         * 
         * 但值容器：即只保存一个值。
         * 把一个T类型的元素给归约成一个U类型的单个元素。
         * @param t2u 把T类型转换成U类型的变换杉树
         * @param <T> Stream 元素类型
         * @param <U> t2u 生成的Value值
         * @return U类型的目标值。
         */
        public static <T,U> Collector<T, AtomicReference<U>, U> atomic_collector(final Function<T,U>t2u){
            return Collector.of(AtomicReference::new,
                (atomic,t)->atomic.set(t2u.apply(t)),// 规约容器之
                (atomic_a,atomic_b)->atomic_b,// 合并容器之
                atomic->atomic.get());// 提取容器值
        }
        
        /**
         * 用法示例：
         * 
         * final var rr = jdbc.sql2records(sql); <br>
         * final var map = rr.stream().collect(groupby(e->e.str("name"),e->e.get("cnt"))); <br>
         * 
         * 制作一个分组器 把一个 Stream<U> 给份组成也成一个Map<K,U>
         * @param classifier T 类型的的分类器，classifier 会申城一个键类型。
         * @param t2u 把T类型给转换成U类型
         * 
         * @param <T> Stream 元素类型
         * @param <K> classifier 生的K的类型
         * @param <U> t2u 生成的Value值。
         * 
         * @return Map<K,U>
         */
        public static <T,K,U> Collector<T,?, Map<K,U>> groupby(final Function<T,K> classifier,final Function<T,U>t2u){
            return Collectors.groupingBy(classifier,atomic_collector(t2u));
        }
        
        /**
         * 对Stream<T> 中的元素进行 classifier 分组。
         * @param classifier 分组函数
         * 
         * @param <T> Stream 元素类型
         * @param <K> classifier 生的K的类型
         * 
         * @return Map<K,List<K>
         */
        public static <T,K> Collector<T, ?, Map<K, List<T>>> groupby(final Function<T,K> classifier){
            return Collectors.groupingBy(classifier);
        }
        
        /**
         * 使用示例
         * var s = ll.stream().map(e->e.toString()).collect(join("\n"));
         * 把String <String> 通过 delimiter 连接骑起来
         * @param delimiter 分隔符
         * @return 用delimiter 分割的Stream<String>
         */
        public static Collector<CharSequence, ?, String> join(final CharSequence delimiter){
            return Collectors.joining(delimiter);
        }
        
        /**
         * 对 rr 按照key的值进行分类：即key 就是分类依据。
         * @param rr 一个records 列表
         * @param key 列名：分类依据
         * @return 对rr进行分类的映射:Map<String,List<IRecord>
         */
        public static Map<String,List<IRecord>> classify(final List<IRecord> rr,final String key) {
            var mm = rr.parallelStream().filter(e->e!=null).collect(
                groupby(e->{ var k = e.str(key); return k==null?"unknown ":k; }));
            return mm;
        }
        
        /**
           * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个列表 rr：<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * <br>
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * <br>
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * @param <U> 分类结果的计算器 结果类型
         * @param rr 待分类的数据集合。即源数据,null 或者长度为0返回一个空IRecord
         * @param keys 分类的key列表：分类依据字段列表。或者说 分类层级的序列
         * @param level 当前的处理节点的层级，从0开始。
         * @param parent 分类的结果的保存位置
         * @param evaluator 列指标：分类结果的计算器
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public static<U> IRecord pivotTable(final List<IRecord> rr,final String keys[], final int level,
            final IRecord parent,final Function<List<IRecord>,U>evaluator) {
            
            if(rr==null||rr.size()<1)return REC();// 
            final var final_keys  = (keys==null||keys.length<1)//  分类层级的序列无效,默认为全部分类
                ? rr.get(0).keys().toArray(String[]::new) // 提取第一个元素的键值作为层级分类依据。
                : keys;// 分类层级依据
            final var key = final_keys[level];// 进行分类的 分析依据字段名。
            if(level<final_keys.length) {
                // 创建新节点
                classify(rr,key).forEach((k,sub_rr)->{// 把rr 进行分类：分类型若个子分类
                    if( level!=keys.length-1 ) {// 只要不是最后一个分类项就需要继续分类。
                        final var node = REC(); parent.add(k,node);// 生成一个中间分类节点。注意不记录分组数据。
                        pivotTable(sub_rr, final_keys,level+1,node,evaluator);// 对分类节点继续分类。
                    } else {// 到达了分类层级的末端 ,即当前是最后一个分类的 key
                        parent.add(k,evaluator.apply(sub_rr));// 最后一层记录分组数据
                    }//if level
                });// classify 对 rr 按照key 的值进行分类：即key 就是分类依据。
            }// 阶层有效。
            
            return parent;// 返回计算计算
        }
        
        /**
         * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个列表 rr：<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * <br>
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * <br>
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * @param <U> 分类结果的计算器 结果类型
         * @param rr 待分类的数据集合。即源数据
         * @param keys 分类的key列表，分类依据字段列表。或者说 分类层级的序列
         * @param evaluator 列指标：分类结果的计算器
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public static <U> IRecord pivotTable(final List<IRecord> rr, final String keys[],
            final Function<List<IRecord>,U>evaluator) {
            
            return pivotTable(rr,keys,0,REC(),evaluator);
        }
        
        /**
         * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个 记录集合的列表 rr：<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * <br>
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * <br>
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * @param <U> 分类结果的计算器 结果类型
         * @param rr 待分类的数据集合。即源数据
         * @param keys 分类的key列表，分类依据字段列表。或者说 分类层级的序列
         * @param evaluator 列指标：分类结果的计算器
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public static <U> IRecord pivotTable2(final List<IRecord> rr, final String keys[],
            final Function<Stream<IRecord>,U>evaluator) {
            return pivotTable(rr,keys,0,REC(),ee->evaluator.apply(ee.stream()));
        }
            
        /**
         * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个 记录集合的列表 rr：<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * <br>
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * <br>
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * @param <U> 分类结果的计算器 结果类型
         * @param rr 待分类的数据集合。即源数据
         * @param keys 分类的key列表，分类依据字段列表。或者说 分类层级的序列
         * @param delim 分隔符
         * @param evaluator 列指标：分类结果的计算器
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public static <U> IRecord pivotTable(final List<IRecord> rr, final String keys,String delim,
            final Function<Stream<IRecord>,U>evaluator) {
            return pivotTable(rr,keys.split(delim),0,REC(),ee->evaluator.apply(ee.stream()));
        }
            
        /**
         * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个列表 rr：<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * <br>
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * <br>
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * @param <U> 分类结果的计算器 结果类型
         * @param rr 待分类的数据集合。即源数据
         * @param keys 分类的key列表，分类依据字段列表。或者说 分类层级的序列
         * @param evaluator 列指标：分类结果的计算器
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public static <U> IRecord pivotTable(final List<IRecord> rr, final String keys,
            final Function<Stream<IRecord>,U>evaluator) {
            return pivotTable(rr,keys.split(","),0,REC(),ee->evaluator.apply(ee.stream()));
        }
        
        /**
         * 生成一个 数据透视表:参照Excel的实现。<br>
         * 简单说说就是把 一个列表 rr:<br>
         * a    b   c   d <br>
         * ..   ..  ..  ..<br>
         * ..   ..  ..  ..<br>
         * 分类成成 a/b/c [(a,b,c,d)],即如下图所示的 分组的层次结构，并调用函数 evaluator 计算终端数据集合：<br>
         *  [[a0 b0 c0 di],[a0 b0 c0 dj],...]的指标结果 U <br>
         *  
         * 可以所所谓透视就是对一个列表 rr进行分组再分组的过程，亦即 递归分组。<br>
         * a0 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d0],[a0 b0 c0 d1],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d2],[a0 b0 c0 d3],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a0 b0 c0 d4],[a0 b0 c0 d5],...] <br>
         * - - c1 <br>
         * - - - [[a0 b0 c0 d6],[a0 b0 c0 d7'],...] <br>
         * a1 <br>
         * - b0 <br>
         * - - c0 <br>
         * - - - [[a1 b0 c0 d7],[a1 b0 c0 d9],...] <br>
         * - - c1 <br>
         * - - - [[a1 b0 c1 d10],[a1 b0 c1 d11],...] <br>
         * - b1 <br>
         * - - c0 <br>
         * - - - [[a1 b1 c0 d12],[a1 b1 c0 d12],...] <br>
         * - - c1 <br>
         * - - - [[a1 b1 c1 d14],[a1 b1 c1 d15],...] <br>
         * 
         * 这样的层级结构。然后再对每个分组计算：调用函数 evaluator 结算集合的指标结果 U<br>
         * 
         * 例如:rr 需要包含：sale_name,goods_name 的key,以及 number 的值字段。<br>
         * var result = IRecord.pivotTable( rr, <br>
         *  "sale_name,goods_name".split(","), <br>
         *  ee->ee.stream().collect(Collectors.summarizingDouble(e->e.dbl("number"))).getSum() <br>
         * ); <br>
         * 
         * @param <U> 分类结果的计算器 结果类型
         * @param stream 待分类的数据集合流的形式。即源数据
         * @param keys 分类的key的数组，分类依据字段列表。或者说 分类层级的序列
         * @param evaluator 列指标：分类结果的计算器
         * @return 一个包含由层级关系 IRecord. 中间节点是IRecord类型，叶子节点是 U 类型。
         */
        public static <U> IRecord pivotTable(final Stream<IRecord> stream, final String keys[],
            final Function<List<IRecord>,U>evaluator) {
            return pivotTable(stream.collect(Collectors.toList()),keys,0,REC(),evaluator);
        }
        
        /**
         * Pivot Table 的搜集器：使用示例
         * cph(RPTS(3,L("A","B"))).stream().collect(pvtclc("0,1"));
         * @param keys 键名序列,用逗号分隔
         * @return pivotTalbe 的 搜集器
         */
        public static Collector<IRecord,List<IRecord>,IRecord> pvtclc(String keys){
            return Collector.of(LinkedList::new,List::add,(a,b)->{a.addAll(b);return a;}
            ,aa->IRecord.pivotTable(aa,keys,LittleTree::LIST));
        }
        
        /**
         * Pivot Table 的搜集器：使用示例
         * cph(RPTS(3,L("A","B"))).stream().collect(pvtclc(0,1));
         * @param keys 键名序列,分类层级序列 ， ,键名非String类型的对象，会调用toString给与完成转换。
         * @return pivotTalbe 的 搜集器
         */
        public static Collector<IRecord,List<IRecord>,IRecord> pvtclc(Object...keys){
            
            final var kk = keys== null
                ? new String[] {} // 空的分类层级序列
                : Arrays.stream(keys).map(Object::toString).toArray(String[]::new);// 透视表的层级分类依据
            return Collector.of(LinkedList::new,List::add,(a,b)->{a.addAll(b);return a;}
            ,aa->IRecord.pivotTable(aa,kk,LittleTree::LIST));
        }
        
        /**
         * 转换成一个二维数组：把IRecord视为一个values的LinkedList,将其转换成一维数组，然后再把 rr 集成起来拼装成二维数组。
         * @param rr IRecord的集合
         * @return 二维数组
         */
        public static String[][] toStringArray(final List<IRecord> rr){
            return rr.stream().map(IRecord::toStringArray).toArray(String[][]::new);
        }
        
        /**
         * 转换成一个二维数组
         */
        public static Object[][] toObjArray(final List<IRecord> rr){
            return rr.stream().map(IRecord::toObjArray).toArray(Object[][]::new);
        }
        
        /**
         * 把一个函数t2u 应用到一个 List<T>类型的容器类型
         * @param <T> 列表的元素的类型
         * @param <U> 函数t2u的返回结果
         * @param t2u 应用到列表上的的函数
         * @return 把一个函数应用到 一个LIST
         */
        public static <T,U> Function<List<T>,List<U>> applicative(final Function<T,U> t2u){
            return  ll->{
                return ll.stream().map(t2u).collect(Collectors.toList());
            };
        }
        
        /**
         * line 待分解的字符序列 分解成 所有的字串名称。
         * 一般用于数据钻取的的分层处理。累加性运算的Key-Value 表达具有普遍意义。
         * 
         * 把 /a/b/c 解析成
         *   /a,/a/b,/a/b/c 的需求
         * @param line 待分解的字符序列 delim 分隔符默认为 "/" 
         * @return 字符串的所有前缀的集合。
         */
        public static List<String> split2prefixes(final String line){
            return split2prefixes(line,"/");
        }
        
        /**
         * line 待分解的字符序列 分解成 所有的字串名称。
         * 一般用于数据钻取的的分层处理。累加性运算的Key-Value 表达具有普遍意义。
         * 
         * 把 /a/b/c 解析成
         *   /a,/a/b,/a/b/c 的需求
         * @param line 待分解的字符序列
         * @param delim 分隔符
         * @return 字符串的所有前缀的集合。
         */
        public static List<String> split2prefixes(final String line,final String delim){
            final var ss = line.split(delim);
            return split2prefixes(ss,delim);
        }
        
        /**
         * 遮住scanl具有相似意义，差别是split2prefixes 返回的是List<字符串> 二者在原理上一致
         * line 待分解的字符序列 分解成 所有的字串名称。
         * 一般用于数据钻取的的分层处理。累加性运算的Key-Value 表达具有普遍意义。
         * 
         * 把 [a,b,c] 解析成
         *   /a,/a/b,/a/b/c 的序列，这个对于累加数据的钻取，累加性运算的Key-Value 表达具有普遍意义。
         * @param ss 字符串序列，拼接成前缀字符串
         * @param delim 分隔符,作根节点的分隔符
         * @return 字符串的所有前缀的集合。
         */
        public static List<String> split2prefixes(final String []ss,final String delim){
            final var ll = new LinkedList<String>();
            ll.add(delim);// 加入根节点。
            final var buffer = new StringBuffer();
            Arrays.stream(ss).filter(e->!e.matches("\\s*"))// 去除掉空白的key
            .forEach(s->{
                buffer.append(delim+s);
                ll.add(buffer.toString());
            });// 前缀累加
            return ll;
        }
        
        /**
        * 对键值儿进行扫描:
        * 所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真前缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
        * [[1], [1, 2], [1, 2, 3], [1, 2, 3, 4], [1, 2, 3, 4, 5]]
        * 
        * @param <T> 元素类型
        * @param <U> 变换结果类型
        * @param stream 元素的流数据
        * @param fx 真前缀的变换函数:tt->u
        * @param reverse 是否对数据进行倒转
        * @param include_empty 是否包含空列表。true 表示包含空集合
        * @return List<U>
        */
        @SuppressWarnings("unchecked")
        public static <T,U> List<U> scan(final Stream<T> stream, final Function<List<T>,U> fx,
            boolean reverse,boolean include_empty) {
            final var uu = new LinkedList<U>();
            if(stream==null)return uu;
            final Function<List<T>,U> final_fx = fx==null? (Function<List<T>,U>)(e->(U)e):fx;
            final var prev = new LinkedList<T>();
            if(include_empty)uu.add(final_fx.apply(LIST(prev)));//包含空列表
            stream.forEach(t->{
                prev.add(t);
                final var pp = LIST(prev);
                if(reverse)Collections.reverse(pp);
                final var u = final_fx.apply(pp);
                uu.add(u);
            });
            
            return uu;
        }
        
       /**
        * 对键值儿进行扫描:
        * 所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真前缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
        * [[1], [1, 2], [1, 2, 3], [1, 2, 3, 4], [1, 2, 3, 4, 5]]
        * @param <T> 元素类型
        * @param <U> fx:变换结果类型
        * @param stream 元素的流数据
        * @param fx 真前缀的变换函数:tt->u
        * @param reverse 是否对数据进行倒转
        * @return List<U> U的集合
        */
        public static <T,U> List<U> scan(final Stream<T> stream, final Function<List<T>,U> fx,boolean reverse) {
            return scan(stream,fx,reverse,false);
        }
        
        /** 
         * 对键值儿进行扫描:
         * 所谓扫描是指 对于 [1,2,3,4] 这样的序列生成 如下的真前缀的集合(不包括空集合)：术语scan 来源于 haskell 语言
         * [[1], [1, 2], [1, 2, 3], [1, 2, 3, 4], [1, 2, 3, 4, 5]]
         * @param <T> 元素类型
         * @param <U> 变换结果类型
         * @param stream 元素的流数据
         * @param fx 真前缀的变换函数:tt->u
         * @return List<U> U 的集合
         */
         public static <T,U> List<U> scan(final Stream<T> stream, final Function<List<T>,U> fx){
             return scan(stream,fx,false);
         }
         
         /**
          * 把一个record 转换目标类型：由于record 就是模仿 LISP语言中的列表（增强了key-value的树形特征）。<br>
          * 所以 IRecord 理论上是可以标识任何数据结构的。<br>
          * 所以rec 转换成任意一个对象结构也是可行的。<br>
          * 
          * @param <T> 目标类型数据的class
          * @param rec IRecord 结构的数据
          * @param targetClass 目标类型
          * @return targetClass 的一个实例数据。
          */
         @SuppressWarnings("unchecked")
         public static <T> T rec2obj(IRecord rec,Class<T> targetClass){
             // IRecord 作为特殊存在，给予直接返回。
             if (targetClass.isAssignableFrom(IRecord.class))return (T)rec;// IRecord 直接返回
             
             //////////////////////////////////
             //java 基本类型的处理
             //////////////////////////////////
             
             // 布尔类型的处理
             if(targetClass== Boolean.class||targetClass==boolean.class){// 布尔类型的转换
                 //这是规定。比如 判断表是否存在 "show tables like ''{0}''",不存在的就是返回null,因此作为false;
                 if(rec==null)return (T)(Object)false;// 值null 是为false.
                 final var v = rec.get(0);// 提取字段的第一项
                 if(v==null) {// null 被视为 false
                     return (T)(Object)false;
                 } else {// 非空值
                     if( v instanceof String) {// 字符串类型  "false" 视为false,其他视为true
                         final var sv = (String)v;
                         return (T)(Object)((sv.trim().equalsIgnoreCase("false"))?false:true); 
                     }else if (Number.class.isAssignableFrom(v.getClass())){// 数值类型非0视为true;
                         final var nv =(Number)v;
                         return  (T)(Object)((nv.intValue()==0)?false:true);// 非零均视为true
                     }else {// 其他类型均视为true
                         return (T)(Object)true;
                     }// if
                 }// if(v==null) 
             }//  targetClass== Boolean.class
             
             // 把Boolean类型前置再rec==null 就是为了处理 null作为false的理解的情况。
             if(rec==null)return null;// 无效则直接返回。
             
             // java 的基本类型的处理
             if(targetClass== Byte.class || targetClass==byte.class) {// 字节类型
                 return (T)(Object)(rec.num(0).byteValue());
             } else if (targetClass == String.class) {// 字符串类型
                 return (T)(Object)(rec.str(0));
             } else if(targetClass== Integer.class||targetClass==int.class) {// 整形
                 return (T)(Object)(rec.i4(0));
             } else if(targetClass== Long.class||targetClass==long.class) {// 长整形
                 return (T)(Object)(rec.lng(0));
             }else if(targetClass== Double.class||targetClass==double.class) {// 双精度
                 return (T)(Object)(rec.dbl(0));
             }else if(targetClass== Float.class||targetClass==float.class) {// 单精度
                 return (T)(Object)(rec.num(0).floatValue());
             }else if(targetClass== Short.class||targetClass==short.class) {// 短整型
                 return (T)(Object)(rec.num(0).shortValue());
             }else if(targetClass== Character.class||targetClass==char.class) {// 字符型:这里由精度损失。仅用于ascii码
                 return (T)(Object)((char)rec.num(0).byteValue());
             }else if(targetClass== Number.class||targetClass==Number.class) {// 数字
                 return (T)(Object)(rec.num(0));
             }else if(targetClass== Date.class) {// 日期类型
                 return (T)(Object)(rec.date(0));
             }else if(targetClass== LocalDate.class) {// 本地日期
                 return (T)(Object)(rec.ld(0));
             }else if(targetClass== LocalTime.class) {// 本地时间
                 return (T)(Object)(rec.lt(0));
             }else if(targetClass== LocalDateTime.class) {//本事日期使劲。
                 return (T)(Object)(rec.ldt(0));
             }
             
             //////////////////////////////////
             //javaBean 对象的 的处理
             //////////////////////////////////
             Object javaBean = null;// 目标类型
             try {
                 final var ctor = targetClass.getDeclaredConstructor((Class<?>[])null);
                 ctor.setAccessible(true);// 增加够着函数的方文性
                 javaBean = ctor.newInstance((Object[])null);// 提取默认构造函数
                 IRecord.OBJINIT(javaBean,rec);// 对javabean 进行实例化
             }catch(Exception e ) {
                 if(debug)e.printStackTrace();
             }// try
             
             // 返回结果值
             return (T)javaBean;// 以bean的实例作为对象返回值。
         }
         
        /////////////////////////////////////////////////////////////////////
        // 以下是IRecord 的默认静态函数区域
        /////////////////////////////////////////////////////////////////////
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final  Function<Object,Number> obj2num=obj->{
            if(obj instanceof Number) return (Number)obj;
            Number num =(Number) Double.parseDouble(obj.toString());
            return num;
        };
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,?>,Number> kv2num = (kv)->obj2num.apply(kv.value());
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,?>,Integer> kv2int = (kv)->obj2num.apply(kv.value()).intValue();
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,?>,Long> kv2lng = (kv)->obj2num.apply(kv.value()).longValue();
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,?>,Double> kv2dbl = (kv)->obj2num.apply(kv.value()).doubleValue();
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,?>,Float> kv2float = (kv)->obj2num.apply(kv.value()).floatValue();
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,?>,Short> kv2short = (kv)->obj2num.apply(kv.value()).shortValue();
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,?>,Byte> kv2byte = (kv)->obj2num.apply(kv.value()).byteValue();
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,Character>,Boolean> kv2bool = (kv)->Boolean.valueOf(kv.value()+"");
        
        /**
         * 把一个键值对的数据转换成一个R的类型
         */
        public static final Function<KVPair<String,Character>,Character> kv3char = (kv)->Character.valueOf((kv.value()+"").charAt(0));
        
        /**
         * 等价函数
         */
        public static final Function<Object,Object> identity = (Object o)->o;
        
        /**
         * 统计搜集工具
         * @param name 字段名称
         * @return
         */
        public static Collector<? super IRecord, ?, DoubleSummaryStatistics> dbl_stats(String name){
            return dbl_stats(e->e.dbl(name));
        }
        
        /**
         * 统计搜集工具
         * @param t2dbl 类型变换函数 把 一个T类型的对象转换成浮点数
         * @return 浮点数的统计器
         */
        public static <T> Collector<? super T, ?, DoubleSummaryStatistics> dbl_stats(ToDoubleFunction<T> t2dbl){
            return Collectors.summarizingDouble(t2dbl);
        }
       
        
        /**
         * 统计搜集工具
         * @param name 字段名称
         * @return 整型统计对象
         */
        public static Collector<? super IRecord, ?, IntSummaryStatistics> int_stats(String name){
            return Collectors.summarizingInt(e->e.i4(name));
        }
        
        /**
         * 统计搜集工具
         * @param <T> 源数据对象的类型
         * @param t2int  对象转换成整型的函数
         * @return 整型统计对象
         */
        public static <T> Collector<? super T, ?, IntSummaryStatistics> int_stats(ToIntFunction<T> t2int){
            return Collectors.summarizingInt(t2int);
        }
        
        /**
         * 统计搜集工具
         * @param name 字段名称
         * @return 长整型统计对象
         */
        public static Collector<? super IRecord, ?, LongSummaryStatistics> lng_stats(String name){
            return lng_stats(e->e.i4(name));
        }
        
        /**
         * 统计搜集工具
         * @param t2lng 字段名称
         * @param <T> 列表数据元素的类型
         * @return 长整型统计对象
         */
        public static <T> Collector<? super T, ?, LongSummaryStatistics> lng_stats(ToLongFunction<T> t2lng){
            return Collectors.summarizingLong(t2lng);
        }
        
        public static final String SHARP_VARIABLE_PATTERN="#{1}([a-zA-Z_][a-zA-Z0-9_]*)";// sharp 变量的此法模式
        
    }

    /**
     * TreeRecord 区分对key的名称区分大小写
     * @author gbench
     *
     */
    public static abstract class AbstractMapRecord implements IRecord{

        /**
         * 返回自身便于实现链式编程
         */
        @Override
        public IRecord add(Object key, Object value) {
            data.put(key.toString(), value);
            return this;
        }

        @Override
        public IRecord set(String key, Object value) {
            data.put(key, value);
            return this;
        }

        @Override
        public Object get(String key) {
            if(key==null)return null;
            Object o = data.get(key);
            if(o==null) {// 忽略大小写
                Optional<String> k = data.keySet().stream()
                        .filter(key::equalsIgnoreCase).findFirst();
                return k.isPresent()?data.get(k.get()):null;
            }//if
            return o;
        }

        @Override
        public List<Object> gets(String key) {
            return Arrays.asList(this.get(key));
        }

        @Override
        public Stream<KVPair<String, Object>> stream() {
            return data.entrySet().stream()
                    .map(e->new KVPair<String,Object>(e.getKey(),e.getValue()));
        }

        @Override
        public List<KVPair<String, Object>> kvs() {
            return this.stream().collect(Collectors.toList());
        }

        /**
         * 字符串格式化
         */
        public String toString() {
            final var builder = new StringBuilder();
            final Function<Object,String> cell_formatter = v->{
                if(v==null) return"(null)";
                var line = "{0}";// 数据格式化
                if(v instanceof Date) {
                    line = "{0,Date,yyyy-MM-dd HH:mm:ss}"; // 时间格式化
                }else if(v instanceof Number) {
                    line = "{0,Number,#}"; // 数字格式化
                }//if
                return MessageFormat.format(line, v);
            };// cell_formatter
            
            if(this.data!=null) {
                this.data.forEach((k,v)->builder.append(k+":"+cell_formatter.apply(v)+"\t"));
            }
            
            return builder.toString().trim();
        }
        
        /**
         *
         */
        public Map<String,Object> toMap(){
            return this.data;
        }

        private static final long serialVersionUID = -6173203337428164904L;
        protected Map<String,Object> data= null;// 数据信息,使用TreeMap 就是为了保持key的顺序
    } // AbstractMapRecord 

    /**
     * TreeRecord 区分对key的名称区分大小写
     * @author gbench
     *
     */
    public static class LinkedRecord extends AbstractMapRecord
        implements IRecord{

        /**
         * 序列构造函数
         * @param oo
         */
        public LinkedRecord(Object ...oo){
            if(data==null)data = new LinkedHashMap<>();
            for(int i=0;i<oo.length;i+=2)data.put(oo[i]+"",i<oo.length-1?oo[i+1]:null);
        };//键值序列构造函数

        public LinkedRecord(){
            data = new LinkedHashMap<>();
        }

        /**
         * 使用map来初始化记录对象：Record 存储一个对initData对象的拷贝。
         * @param initData 记录对象初始化:
         */
        public LinkedRecord(Map<String,?>initData){
            Map<String,Object> oo = new LinkedHashMap<String,Object>();
            if(initData!=null)oo.putAll(initData);
            data = initData!=null?oo:new LinkedHashMap<>();
        }
        
        /**
         * 使用map来初始化记录对象,注意这里是直接使用把 initData来给予包装的。：这是一种快速构造方法。
             * 但会造成实际结构不是LinkedMap,这个适合于属性名称不重要的情况。比如以集合的形式来统一传递参数。
         * @param initData 记录对象初始化
         */
        @SuppressWarnings("unchecked")
        public static LinkedRecord of(Map<?,?>initData){
           var rec =  new LinkedRecord();
           rec.data = (Map<String, Object>) initData;
           return rec;
        }

        /**
         * 建立以keys为字段的记录对象，并且每个key的值采用initData中对应键值的值进行初始化
         * 当map中出现不再keys中出现的记录数据，给与舍弃，即LinkedRecord是严格
         * 按照keys的结构进行构造
         * @param keys 字段序列
         * @param initData 默认值集合。超出keys中的数据将给与舍弃
         */
        public LinkedRecord(List<String>keys,Map<String,?>initData) {
            this.intialize(keys, initData);
        }

        /**
         * 建立以keys为字段的记录对象，并且每个key的值采用initData中对应键值的值进行初始化
         * 当map中出现不再keys中出现的记录数据，给与舍弃，即LinkedRecord是严格
         * 按照keys的结构进行构造
         * @param keys 字段序列
         * @param initData 默认值集合。超出keys中的数据将给与舍弃
         */
        public LinkedRecord(String keys[],Map<String,?>initData) {
            this.intialize(Arrays.asList(keys), initData);
        }

        /**
         * 建立以keys为字段的记录对象，并且每个key的值采用initData中对应键值的值进行初始化
         * 当map中出现不再keys中出现的记录数据，给与舍弃，即LinkedRecord是严格
         * 按照keys的结构进行构造
         * @param keys 字段序列
         * @param initData 默认值集合。超出keys中的数据将给与舍弃
         */
        public LinkedRecord(String keys,Map<String,?>initData) {
            if(keys==null)
                this.data = new LinkedHashMap<>();
            else
                this.intialize(Arrays.asList(keys.split("[,]+")), initData);
        }

        /**
         * 建立以keys为字段的记录对象，并且每个key额值采用map中对应键值的值进行初始化
         * 当map中出现不再keys中出现的记录数据，给与舍弃，即LinkedRecord是严格
         * 按照keys的结构进行自主
         * @param keys 字段序列
         * @param map 默认值集合。超出keys中的数据将给与舍弃
         */
        public void intialize(List<String>keys,Map<String,?>map){
            @SuppressWarnings("unchecked")
            Map<String,Object> initData = (Map<String,Object>)(map!=null?map:new LinkedHashMap<>());// 初始数据
            data =  new LinkedHashMap<>();
            if(keys == null || keys.size()<=0) {
                // do nothing
            } else {// 仅当keys 有效是才给予字段初始化
                keys.stream().filter(e->e!=null).forEach(key->data
                    .computeIfAbsent(key,k->initData.getOrDefault(k,"-")));
            }//if
        }
        
        /**
         * 赋值一个 Record 对象
         */
        @Override
        public IRecord duplicate() {
            return new LinkedRecord(this.data);
        }

        /**
         *   数据便历
         * @param bicons
         */
        public void forEach(BiConsumer<? super String, ? super Object>bicons) {
            this.data.forEach(bicons);
        }

        /**
         * 使用recB 来覆盖recA
         * merged recA 和 recB 的各个各个属性的集合。
         * @param recA
         * @param recB 将要覆盖中的属性的数据,会利用recB中空值覆盖a中的空值
         * @return merged recA 和 recB 的各个各个属性的集合。这是一个新生成的数据对象，该操作不会对recA 和 recB 有任何涌向
         */
        public static IRecord extend (final IRecord recA,final IRecord recB) {
            IRecord rec = new LinkedRecord();//
            if(recA!=null) {recA.kvs().forEach(kv->{rec.add(kv.key(), kv.value());});}
            if(recB!=null) {recB.kvs().forEach(kv->{rec.set(kv.key(), kv.value());});}
            return rec;
        }
        
        private static final long serialVersionUID = 1060363673536668645L;
    } // LinkedRecord
    
    /**
     * DataFrame 数据框，(key,[v0,v1,...]) 结构的键值对集合 kvs 即值元素为集合类型的KVPs.<br>
     * 术语来源于R，<r>
     * 
     * @author gbench
     *
     */
    public static class DataFrame extends LinkedRecord {
        
        /**
         * @param cell_formatter 的初始化
         */
        public String toString() {
            return this.toString2(frt(2));
        }

        /**
         * DataFrame 构造一个数据框对象
         * @param objects 键值序列 key0,value0,key1,value1
         * @return DataFrame  对象
         */
        public static DataFrame DFM(final Object ... objects ) {
            final var n = objects.length;
            final var rec = new DataFrame();
            for(int i=0;i<n-1;i+=2)rec.add(objects[i].toString(),objects[i+1]);
            return rec;
        }
        
        private static final long serialVersionUID = 1L;
    }// class DataFrame

    /**
     * TreeRecord 区分对key的名称区分大小写
     * @author gbench
     *
     */
    public static class TreeRecord extends AbstractMapRecord
            implements IRecord{

        /**
         * 字段构造函数，只能put存放在keys中的字段
         * @param keys 键使用,分割
         */
        public TreeRecord(String keys) {
            this.intialize(Arrays.asList(keys.split("[,]+")));
        }

        /**
         * 字段构造函数，只能put存放在keys中的字段
         * @param keys 键使用,分割
         */
        public TreeRecord(List<String> keys) {
            this.intialize(keys);
        }

        public TreeRecord(Comparator<String> comparator) {
            data = new TreeMap<String,Object>(comparator);
            this.comparator = comparator;
        }

        public TreeRecord(String[] keys) {
            this.intialize(Arrays.asList(keys));
        }

        /**
         * 按照字键名出现次序生成比较器
         * @param keys 键名序列
         */
        public void intialize(List<String> keys) {
            // 比较器
            Comparator<String> comparator = (a,b)->{
                int ret = 0;// 比较结果
                int i = 0;// 键名编号
                Map<String,Integer> map = new HashMap<>();
                for(String s:keys)map.put(s, i++);
                try{ret = map.get(a)-map.get(b);}catch(Exception e) {
                    System.out.println("a:"+a+"\nb:"+b+
                        ",没有出现在 键名列表中："+
                        map+"\n 无法进行数据判断");
                    e.printStackTrace();
                }// try
                return ret;
            };

            data = new TreeMap<String,Object>(comparator);
            this.comparator = comparator;
        }

        /**
         * 复制克隆
         * @return
         */
        public IRecord duplicate() {
            final TreeRecord rec = this.imitate();
            this.kvs().forEach(kv->{
                rec.set(kv.key(), kv.value());
            });
            return rec;
        }

        public Comparator<String> getComparator(){
            return this.comparator;
        }

        /**
         * 仿制就是为了重用原来的比较器
         * @return
         */
        public TreeRecord imitate() {
            return new TreeRecord(comparator);
        }

        private static final long serialVersionUID = 5297462049239467986L;
        private Comparator<String> comparator = null; // 比较器
    } // TreeRecord

    /**
     * 简单的记录类型
     * @author gbench
     *
     */
    public static class SimpleRecord implements IRecord {

        /**
         * 复制克隆
         * @return
         */
        public IRecord duplicate() {
            SimpleRecord rec = new SimpleRecord();
            this.kvs().forEach(kv->rec.set(kv.key(),kv.value()));
            return rec;
        }

        /**
         * 添加一个键值，如果存在则添加一个一个重复的键
         */
        @Override
        public IRecord add(Object key,Object value) {
            kvs.add(new KVPair<String,Object>(key.toString(),value));
            return this;
        }

        /**
         * 设置键值key的值,如果键值已经存在则修改，否则添加
         */
        public IRecord set(String key,Object value) {
            final var kp = this.kvs().stream()
                .filter(kv->kv.key().equals(key)).findFirst();
            if(kp.isPresent())
                kp.get().value(value);
            else
                kvs.add(new KVPair<String,Object>(key,value));

            return this;
        }

        /**
         * 获取指定键值中的数据的第一条
         * @param recs 记录集合：查找范围
         * @param key 键名
         * @param value 键值
         * @return 记录信息
         */
        public static Optional<IRecord> fetchOne(List<IRecord> recs,String key,Object value) {
            return recs.stream().filter(e->e.get(key).equals(value)).findFirst();
        }

        /**
         * 获取指定键值中的数据的第一条
         * @param recs 记录集合：查找范围
         * @param key 键名
         * @param value 键值
         * @return 记录信息
         */
        public static List<IRecord> fetchAll(List<IRecord> recs,
            String key,Object value) {
            
            return recs.stream()
                .filter(e->e.get(key).equals(value))
                .collect(Collectors.toList());
        }

        /**
         * 使用recB 来覆盖recA
         * merged recA 和 recB 的各个各个属性的集合。
         * @param recA
         * @param recB 将要覆盖中的属性的数据,会利用recB中空值覆盖a中的空值
         * @return merged recA 和 recB 的各个各个属性的集合。这是一个新生成的数据对象，该操作不会对recA 和 recB 有任何涌向
         */
        public static IRecord extend (final IRecord recA,final IRecord recB) {
            IRecord rec = new SimpleRecord();//
            if(recA!=null) {recA.kvs().forEach(kv->{rec.add(kv.key(), kv.value());});}
            if(recB!=null) {recB.kvs().forEach(kv->{rec.set(kv.key(), kv.value());});}
            return rec;
        }

        /**
         * 忽略key的大小写，相同key仅寻找第一项
         * @param key 键值表
         * @return key 所对应的值元素
         */
        public Object get(String key) {
            Optional<KVPair<String,Object>> opt = this.kvs.stream()
                .filter(e->e.key().equalsIgnoreCase(key))
                .findFirst();
            if(opt.isPresent()) {
                return opt.get().value();
            }else {
                return null;
            }
        }

        /**
         * 忽略key的大小写，相同key仅寻找第一项
         * @param key 键值表
         * @return
         */
        public List<Object> gets(String key) {
            List<Object> ll = this.kvs.stream()
                .filter(e->e.key().equalsIgnoreCase(key))
                .map(e->e.value())
                .collect(Collectors.toList());
            return ll;
        }

        /**
         * 字符串格式化
         */
        public String toString() {
            return this.kvs().stream().map(e->e.key()+":"+e.value())
                .collect(Collectors.joining(","));
        }

        /**
         * 键值对儿的流
         *@return Stream<KVPair<String,Object>> 的流
         */
        public Stream<KVPair<String,Object>> stream(){
            return kvs.stream();
        }

        /**
         * 转换成{(String,Object)} 类型的Map
         * @return {(String,Object)} 类型的Map
         */
        public Map<String,Object> toMap(){
            Map<String,Object> map = new LinkedHashMap<>();
            this.kvs().forEach(kv->map.put(kv.key(), kv.value()));
            return map;
        }
        
        /**
         * 创造一个Record对象,参数分别是：键名1,键值1,键名2,键值2,。。。的序列。
         * @param values 键值序列
         * @return SimpleRecord 的序列结构可以含有重复的键名
         */
        public static SimpleRecord REC2(List<?>values) {
           return _REC2(values.toArray());
        }
        
        /**
         * 创造一个Record对象,参数分别是：键名1,键值1,键名2,键值2,。。。的序列。
         * @param values 键值序列
         * @return SimpleRecord 的序列结构可以含有重复的键名
         */
        public static SimpleRecord REC2(Stream<?>values) {
           return _REC2(values.toArray());
        }

        /**
         * 创造一个Record对象,参数分别是：键名1,键值1,键名2,键值2,。。。的序列。
         * @param values 键值序列
         * @return SimpleRecord 的序列结构可以含有重复的键名
         */
        public static SimpleRecord REC2(Object ... values) {
           return _REC2(values);
        }
        
        /**
         * 创造一个Record对象,参数分别是：键名1,键值1,键名2,键值2,。。。的序列。
         * @param values 键值序列
         * @return SimpleRecord 的序列结构可以含有重复的键名
         */
        public static SimpleRecord _REC2(Object[] values) {
            SimpleRecord rec = new SimpleRecord();
            if(values==null)return rec;
            for(int i=0;i<values.length;i+=2) {
                String key = values[i]+"";// 键值名
                Object value = (i+1)<values.length?values[i+1]:null;//如果最后一位是key则它的value为null
                rec.add(key, value);
            }//for
            return rec;
        }

        /**
         * 解释字符串生成SimpleRecord
         * @param json json格式的对象描述
         * @return IRecord 对象
         */
        @SuppressWarnings("unchecked")
        public static SimpleRecord REC2(String json){
            ObjectMapper mapper = new ObjectMapper();
            SimpleRecord rec = new SimpleRecord();
            if(json==null)return rec;
            json = json.trim();
            if(!json.startsWith("{"))json="{"+json;// 补充开头的"{"
            if(!json.endsWith("}"))json=json+"}";// 补充结尾的"{"
            try {
                mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                mapper.readValue(json, Map.class)
                        .forEach((k,v)->rec.add(k+"",v+""));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return rec;
        }

        public  int hasCode() {
            return this.toString().hashCode();
        }
        
        public boolean equals(Object obj) {
            return this.toString().equals(obj.toString());
        }
        
        /**
         * 键值对的列表
         */
        public List<KVPair<String,Object>> kvs() {
            return kvs;
        }
        
        private static final long serialVersionUID = 1L;
        private List<KVPair<String,Object>> kvs = new LinkedList<>();
    } // SimpleRecord

    /**
     * 树结构的节点。
     * Node 节点为 record的值类型 做了特殊设计(根据record的键值对儿来获取节点名称)，不过也可以用于非IRecord的值类型
     * @author gbench
     *
     * @param <T> 节点中的元素的的类型
     */
    public static class Node<T>{
        /**
         * 拷贝钩爪函数
         * @param node
         */
        public Node(final Node<T> node) {
            this.value = node.value;
            this.properties = node.properties;
            this.children = node.children;
            this.lambda_name = node.lambda_name;
            this.parent = node.parent;
        }

        /**
         * 节点的值
         * @param value 节点的值
         */
        public Node(T value){
            this.value = value;
        }

        /**
         *
         * @param value 节点的值
         * @param lambda_name 节点的名称获取函数
         */
        public Node(T value,Function<T,String> lambda_name){
            this.value = value;
            this.lambda_name = lambda_name;
        }

        /**
         * 获取节点的值信息
         * @return
         */
        public T getValue() {
            return value;
        }

        /**
         * 简单的额取值写法
         * getValue 的别名
         * @return
         */
        public T val() {
            return getValue();
        }

        /**
         * 返回节点自身，以便实现链式编程
         * @param value
         * @return 返回节点自身，以便实现链式编程
         */
        public Node<T> setValue(T value) {
            this.value = value;
            return this;
        }

        /**
         * 简单的额设置值的写法
         * setValue 的别名
         * @return 返回节点自身，以便实现链式编程
         */
        public Node<T> val(T value) {
            return this.setValue(value);
        }

        /**
         * 简单的额设置值的写法
         * @param fld_picker 值字段的提取器
         * @return 返回节点自身，以便实现链式编程
         */
        public <U> U val(Function<T,U> fld_picker) {
            return fld_picker.apply(this.getValue());
        }

        /**
         * 获得节点名称，节点名是通过信息值进行访问的。
         * @return
         */
        public String getName() {
            if(lambda_name!=null) {
                return lambda_name.apply(this.value);
            }else {
                if(value instanceof IRecord) {
                    IRecord rec = ((IRecord) value);
                    Object name = rec.get("path");// 首先尝试path,使用path是历史遗留问题
                    if(name==null)name=rec.get("name");// 其次尝试使用name
                    if(name!=null)
                        return name+"";
                    else
                        return value+"";
                }else {
                    return value+"";
                }//if value
            }// lambda_name
        }

        /**
         * 获得节点名称，节点名是通过信息值进行访问的。
         *   nameit 根据节点生成对应的名字
         * @return 获取产品名称
         */
        public String getName(Function<Node<T>,String> nameit) {
            return nameit.apply(this);
        }

        /**
         * 获取节点的父节点，根节的父节点为null
         * @return 返回父节点
         */
        public Node<T> getParent(){
            return this.parent;
        }

        /**
         * 获得节点路径
         * @return
         */
        public String getPath() {
            return this.getPath(this);
        }

        /**
         * 获得父节点路径
         *   如果父节点为null返回空
         * @return
         */
        public String getParentPath() {
            if(this.getParent()==null)return null;
            return this.getParent().getPath();
        }

        /**
         * 获取节点路径
         * @param node 节点
         * @return  节点路径
         */
        public String getPath(Node<T> node) {
            if(node==null)return "";
            String ppath = this.getPath(node.getParent());
            if(!ppath.matches("[\\s]*"))ppath+="/";
            return ppath+node.getName();
        }

        /**
         * 这是专门为IRecord 设计的一个提取键值的简单的办法
         * String key record 的键值名
         */
        public Object recget(String key) {
            if(this.value !=null && this.value instanceof IRecord) {
                IRecord rec = (IRecord)this.value;
                return rec.get(key);
            }
            return null;
        }

        /**
         * 这是专门为IRecord 设计的一个提取键值的简单的办法
         * String key record 的键值名
         */
        public String recstr(String key) {
            if(this.value !=null && this.value instanceof IRecord) {
                IRecord rec = (IRecord)this.value;
                return rec.str(key);
            }
            return null;
        }

        /**
         * 这是专门为IRecord 设计的一个提取键值的简单的办法
         * String key record 的键值名
         */
        public Number recnum(String key) {
            if(this.value !=null && this.value instanceof IRecord) {
                IRecord rec = (IRecord)this.value;
                return rec.num(key);
            }
            return null;
        }

        /**
         * 这是专门为IRecord 设计的一个提取键值的简单的办法
         * String key record 的键值名
         */
        public Integer reci4(String key) {
            if(this.value !=null && this.value instanceof IRecord) {
                IRecord rec = (IRecord)this.value;
                return rec.i4(key);
            }
            return null;
        }

        /**
         * 这是专门为IRecord 设计的一个提取键值的简单的办法
         * String key record 的键值名
         */
        public Double recdbl(String key) {
            if(this.value !=null && this.value instanceof IRecord) {
                IRecord rec = (IRecord)this.value;
                return rec.dbl(key);
            }
            return null;
        }

        /**
         * 这是专门为IRecord 设计的一个提取键值的简单的办法
         * String key record 的键值名
         */
        public Timestamp rectimestamp(String key) {
            if(this.value !=null && this.value instanceof IRecord) {
                IRecord rec = (IRecord)this.value;
                return rec.timestamp(key);
            }
            return null;
        }

        /**
         * 这是专门为IRecord 设计的一个提取键值的简单的办法
         * String key record 的键值名
         */
        public Date recdate(String key) {
            if(this.value !=null && this.value instanceof IRecord) {
                IRecord rec = (IRecord)this.value;
                return rec.date(key);
            }
            return null;
        }

        /**
         * 获取所有的子节点信息记录
         * @return
         */
        public List<Node<T>> getChildren() {
            return children;
        }

        /**
         * 添加子节点,自动设置子节点的父节点
         * @param c 子节点,null 节点不予添加
         */
        public void addChild(Node<T> c) {
            if(c!=null) {
                if(this.hasChild(c)) {// 存在性检测
                    //System.out.println("节点"+c+",已存在");
                    return;
                }//if 存在性检测
                this.children.add(c);
                c.setParent(this);
            }//
        }

        /**
         * 是否包含由孩子节点
         * @param c 孩子节点
         * @return 是否含有指定的节点数据
         */
        public boolean hasChild(Node<T> c) {
            return this.getChildren().contains(c);
        }

        /**
         * 是否包含由孩子节点
         * @param cc 孩子节点列表
         * @return
         */
        public boolean hasChildren(List<Node<T>> cc) {
            return cc.stream().collect(Collectors.summingInt(e->this.hasChild(e)?1:0))
                    .intValue() == cc.size();
        }

        /**
         * 添加子节点
         * @param cc 子节点集合
         */
        void addChildren(List<Node<T>>cc){
            this.children.addAll(cc);
            cc.forEach(c->c.setParent(this));//设置父节点
        }

        /**
         * 注意这里是通过addChildren 完成父子关系的构建。设置父节点
         * 并不保证父节点的子节点中包含有 本节点(this)。所以可以 setParent(null)
         * 这样就在逻辑上构建了一个独立的树（根节点没有父节点，每颗树只有一个父节点）
         *
         * 设置父节点
         * @param parent 父节点对象，可以为null,这样就是表明他是一颗树的根
         */
        public void setParent(Node<T> parent){
            this.parent=parent;
        }

        /**
         * 判断当前节点是否为根节点
         *
         * @return
         */
        public boolean isRoot() {
            return this.getParent() ==null;
        }

        /**
         * 是否是叶子节点
         * @return 是否为叶子节点
         */
        public boolean isLeaf() {
            return this.children.size()<=0;
        }

        /**
         * 节点的阶层位置 根节点处于0层
         * @return 节点的层级,根节点为0层,根节点的自子节点为1,以此类推, 即 子节点的层级为父节点的层级+1
         */
        public Integer getLevel() {
            return this.getLevel(this);
        }

        /**
         * 获取指定节点的层级
         * 节点的层级,根节点为0层,根节点的自子节点为1,以此类推, 即 子节点的层级为父节点的层级+1
         * @param node 节点数据
         * @return 节点的层级,根节点为0层,根节点的自子节点为1,以此类推, 即 子节点的层级为父节点的层级+1
         */
        public Integer getLevel(Node<T> node) {
            if(node==null)return 0;
            return this.getLevel(node.getParent())+1;
        }

        /**
         * 获取属性值
         * @param name 树形名称
         * @return 属性值
         */
        public Object prop(String name) {
            return properties.get(name);
        }

        /**
         * 设置属性值
         * @param name 属性名称
         * @param value 属性值
         * @return
         */
        public Object prop(String name,Object value) {
            return properties.put(name,value);
        }

        /**
         * 获取属性集合
         * @return
         */
        public Map<String,Object> props() {
            return properties;
        }

        /**
         * 字符串属性
         * @param name 属性名
         * @return
         */
        public String strProp(String name) {
            Object obj = this.prop(name);
            return obj==null?null:obj+"";
        }

        /**
         * 数字属性属性
         * @param name 属性名
         * @return
         */
        public Number numProp(String name) {
            Object obj = this.prop(name);
            if(obj==null)return null;
            if(obj instanceof Number) return (Number)obj;
            try {obj = Double.parseDouble(obj+"");}catch(Exception e) {e.printStackTrace();};
            return (Number)obj;
        }

        /**
         * 数字属性属性
         * @param name 属性名
         * @return
         */
        public int intProp(String name) {
            return numProp(name).intValue();
        }

        /**
         * 数字属性属性
         * @param name 属性名
         * @return
         */
        public double dblProp(String name) {
            return numProp(name).doubleValue();
        }


        public String toString() {
            return value+"";
        }

        /**
         * 使用产品的名字进行节点散列
         * @return
         */
        public int hashCode() {
            return this.getName().hashCode();
        }

        /**
         * 依据名称来进行判断对象属性
         */
        public boolean equals(Object obj) {
            if(obj instanceof Node ) {
                @SuppressWarnings("unchecked")
                Node<T> node = (Node<T>)obj;
                if(node.getName().equals(this.getName()))return true;
            }
            return false;
        }

        /**
         * 根据路径获取子节点,路径采用"/"进行分割
         * 对于路径 A/B/C, 以节点 A为例 nodeA.path("B/C") 返回节点C
         * @param path
         * @return
         */
        public Node<T> get(Node<T> node,String path){
            if(node==null)return null;
            int p = path.indexOf("/");//产品路径 backslash 位置 pos
            final String name= p<0?path:path.substring(0,p);// 获取一级目录项目
            final String rest = p<path.length()?path.substring(p+1):null;
            if(name.equals(node.getName()))
                return node;
            else {
                Optional<Node<T>> c = node.children.stream().filter(e->e.getName().equals(name)).findFirst();
                if(c.isPresent() && rest!=null) return get(c.get(),rest);//从子节点重继续寻找
            }

            return null;
        }

        /**
         * 路径节点
         */
        public Node<T> get(String path){
            return get(this,path);
        }

        /**
         * 节点遍历
         * @param cs 节点长处理函数
         * @param node 遍历的节点起始节点
         */
        public static <U> void forEach(Consumer<Node<U>> cs,Node<U> node) {
            if(node==null || cs == null)return;
            cs.accept(node);
            for(Node<U> u:node.getChildren())forEach(cs,u);
        }

        /**
         * 以该节点为起始节点，进行树形结构遍历
         * @param cs 节点长处理函数
         */
        public void forEach(Consumer<Node<T>> cs) {
            forEach(cs,this);
        }

        /**
         * 返回所有的 叶子节点
         * @return 叶子节点
         */
        public List<Node<T>> getAllLeaves(){
            return this.flatMap().stream().filter(e->e.isLeaf()).collect(Collectors.toList());
        }

        /**
         * 树形结构的节点。扁平化成序列列表
         * 以该节点为起始节点，进行树形结构遍历
         *
         * 示例： MAP(root.flatMap(),f->f.val(g->g.str("name")));// 生成一个树形结构的额各个节点名称序列。
         * 这里是一个Node<IRecord> 结构，并且IRecord中包含了name字段
         */
        public synchronized List<Node<T>> flatMap() {
            return this.flatMap(e->e);
        }

        /**
         * 以该节点为起始节点，进行树形结构遍历
         * @param mapper 节点的处理函数 把 <T> 类型转换成 <U> 类型。
         */
        public synchronized <U>  List<U> flatMap(Function<Node<T>,U> mapper) {
            if(mapper==null)return null;
            List<U> list = new LinkedList<>();
            this.forEach(e->{
                list.add(mapper.apply(e));});
            return list;
        }

        /**
         * 树形结构的节点。扁平化成序列列表
         * 以该节点为起始节点，进行树形结构遍历
         *
         * 示例： MAP(root.flatMap(),f->f.val(g->g.str("name")));// 生成一个树形结构的额各个节点名称序列。
         * 这里是一个Node<IRecord> 结构，并且IRecord中包含了name字段
         */
        public synchronized Stream<Node<T>> flatStream() {
            return this.flatStream(e->e);
        }

        /**
         * 以该节点为起始节点，进行树形结构遍历
         * @param mapper 节点的处理函数 把 <T> 类型转换成 <U> 类型。
         */
        public synchronized <U>  Stream<U> flatStream(Function<Node<T>,U> mapper) {
            return this.flatMap(mapper).stream();
        }

        private T value;// 节点的信息值
        private Node<T> parent=null;// 父节点，初始化为null,表示他是一个根节点，没有父节点
        private Function<T,String> lambda_name = null;// 获得节点的名称
        private List<Node<T>> children=new LinkedList<>();// 子节点：集合

        private Map<String,Object> properties = new LinkedHashMap<>();// 属性记录的存储空间
    }

    /**
     *   Json 的帮助类
     * @author gbench
     *
     */
    public static class Json {
        
        public static ObjectMapper objM = new ObjectMapper();// 默认实现的ObjectMapper
        
        /**
         * 把一个对象转换成json对象
         * @param obj 对象
         * @return json 结构的对象
         */
        public static String obj2json(Object obj) {
            if(obj==null)return "{}";
            String jsn = ""; // jsn 结构的对象
            try {
                Object o = obj;// 对象结构
                //if(o instanceof IRecord) o = ((IRecord)o).toEscapedMap();// 映射对象
                jsn = objM.writeValueAsString(o);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }//try

            return jsn;
        }

        /**
         * 把一个对象转换成json对象
         * 当cls为Map.class 时候结果不会返回null,空值使用一个长度为0的HashMap代替
         * @param jsn 对象 json的字符串表示结构，之所以使用Object是便于从Map这样的容器中取值进行直接传送。
         * @return json 结构的对象
         */
        public static<T> IRecord json2rec(final Object jsn) {
            @SuppressWarnings("unchecked")
            Map<String,Object> map = json2obj(jsn,Map.class,true);
            return new LinkedRecord(map);
        }

        /**
         * 把一个对象转换成json对象
         * 当cls为Map.class 时候结果不会返回null,空值使用一个长度为0的HashMap代替
         * @param jsn 对象 json的字符串表示结构，之所以使用Object是便于从Map这样的容器中取值进行直接传送。
         * @return json 结构的对象
         */
        public static<T> T json2obj(final Object jsn,Class<T> cls) {
            return json2obj(jsn,cls,true);
        }

        /**
         * 把一个对象转换成json对象
         * 当cls为Map.class 时候结果不会返回null,空值使用一个长度为0的HashMap代替
         * @param jsn 对象 json的字符串表示结构，之所以使用Object是便于从Map这样的容器中取值进行直接传送。
         * @param cls 对象类型
         * @param b 是否打印异常信息
         * @return json 结构的对象
         */
        @SuppressWarnings("unchecked")
        public static<T> T json2obj(final Object jsn,Class<T> cls,Boolean b) {

            ObjectMapper objM = new ObjectMapper();
            T obj = null;
            try {
                obj = objM.readValue(jsn+"",cls);
            } catch (Exception e) {
                if(b)e.printStackTrace();
                if(cls==Map.class) {// 保证当cls为Map的时候不会返回空
                    obj=(T)new LinkedHashMap<Object,Object>();
                }
            }//try

            return obj;
        }

        /**
         * 把一个对象转换成json对象
         * 当cls为Map.class 时候结果不会返回null,空值使用一个长度为0的HashMap代替
         * @param jsn 对象 json的字符串表示结构，之所以使用Object是便于从Map这样的容器中取值进行直接传送。
         * @param typeRef 类型信息
         * @return json 结构的对象
         */
        public static<T> T json2obj(final Object jsn,TypeReference<T> typeRef) {

            final var objM = new ObjectMapper();
            T obj = null;
            try {
                obj = objM.readValue(jsn+"",typeRef);
            } catch (Exception e) {
                e.printStackTrace();
            }//try

            return obj;
        }

        /**
         * 生成   record 列表
         * json2recs
         * @param jsn 一个jsn数组[{key:value,key2:value2,...},{key:value,key2:value2,...},...]
         * @return
         */
        public static List<IRecord> json2recs(final Object jsn) {
            return Json.json2obj(jsn,new TypeReference<List<IRecord>>(){});// 注意这里 不能用 json2list，必须指定List<IRecord>类型
        }

        /**
         * 这个方法不怎么有用。
         * json2list：这里的转化知识是一个形式转化，并没有实际调用序列化函数。
         * @param jsn 一个jsn数组[{key:value,key2:value2,...},{key:value,key2:value2,...},...]
         * @param itemClass 列表项的数据类型
         * @param <T> 返回结果LIST中的元素的类型
         * @return T 类型的List
         */
        public static<T> List<T> json2list(final Object jsn,final Class<T>itemClass) {
            return Json.json2obj(jsn,new TypeReference<List<T>>(){});
        }

        /**
         * 读取json的键值属性
         *
         * @param jsn json 数据
         * @param key 键名
         * @param cls 键的值类型
         * @param <T> 返回结果的类型
         * @return T类型的对象
         */
        @SuppressWarnings("unchecked")
        public static<T> T jsnget(String jsn,String key,Class<T> cls) {
            return (T)json2obj(jsn,Map.class).get(key);
        }

        /**
         * 序列转船成一个map
         * 创造一个Record对象,参数分别是：键名1,键值1,键名2,键值2,。。。的序列。
         * @return SimpleRecord
         */
        public static Map<Object,Object> seq2map(Object ... values) {
            Map<Object,Object> rec = new LinkedHashMap<>();
            for(int i=0;i<values.length;i+=2) {
                String key = values[i]+"";// 键值名
                Object value = (i+1)<values.length?values[i+1]:null;//如果最后一位是key则它的value为null
                rec.put(key, value);
            }//for

            return rec;
        }

        /**
         * 创造一个Record对象,参数分别是：键名1,键值1,键名2,键值2,。。。的序列。
         * @return SimpleRecord
         */
        public static String build(Object ... values) {
            Map<Object,Object> rec = new LinkedHashMap<>();
            for(int i=0;i<values.length;i+=2) {
                String key = values[i]+"";// 键值名
                Object value = (i+1)<values.length?values[i+1]:null;//如果最后一位是key则它的value为null
                rec.put(key, value);
            }//for
            return obj2json(rec);
        }

        /**
         * 判断一个字符串是否时json,字段需要待由双引号
         * @param json
         */
        public static boolean isJson(Object json) {
            ObjectMapper objM = new ObjectMapper();
            Object obj = null;
            try {
                obj = objM.readValue(json+"", Map.class);
            }catch(Exception e) {
                try {
                    obj = objM.readValue(json+"", List.class);
                }catch(Exception ee) {
                    // do nothing
                }//try
            }//try

            return obj!=null;
        }

    }
    

    /**
     * 基本时间操作函数
     */
    public static class CronTime{
        /**
         * 
         * @param localDate
         * @return
         */
        public static Date ld2dt(LocalDate localDate) {
            if(localDate==null)return null;
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
            return Date.from(zdt.toInstant());
        }
        
        /**
         * localDateTime2Date 的别名
         * @param localDateTime,null 返回null
         * @return
         */
        public static Date ldt2dt(LocalDateTime localDateTime) {
            if(localDateTime==null)return null;
            return localDateTime2date(localDateTime);
        }
        /**
         * 
         * @param localDateTime
         * @return
         */
        public static Date localDateTime2date(LocalDateTime localDateTime) {
            if(localDateTime==null)return null;
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = localDateTime.atZone(zoneId);
            return Date.from(zdt.toInstant());
        }
        
        /**
         * date2LocalDateTime的别名
         * @param date 时间
         * @return 本地日期时间
         */
        public static LocalDateTime dt2ldt(Date date) {
            if(date==null)return null;
            return date2localDateTime(date);
        }
        
        /**
         * date　-> localDate
         * @param date
         * @return
         */
        public static LocalDateTime date2localDateTime(Date date) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            return instant.atZone(zoneId).toLocalDateTime();
        }
        
        /**
         * 日期Date 转 LocalDate
         * @param date
         * @return
         */
        public static LocalDate dt2ld(Date date) {
            if(date==null)return null;
            return date2localDate(date);
        }

        /**
         * date　-> localDate
         * @param date
         * @return
         */
        public static LocalDate date2localDate(Date date) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            return instant.atZone(zoneId).toLocalDateTime().toLocalDate();
        }
    
        
        /**
         * 默认为当日日期
         * @param lt 本地实间
         * @return 日期
         */
        public static Date ld2dt(LocalTime lt) {
            return localTime2date(lt);
        }
        
        /**
         * 默认为当日日期
         * @param lt 本地时间
         * @param ld 本地日期,null 表示当日
         * @return 日期
         */
        public static Date ld2dt(LocalTime lt,LocalDate ld) {
            return localTime2date(lt,ld);
        }
        
        /**
         * 默认为当日日期
         * @param lt 本地时间
         * @param dt 日期,null 表示当日
         * @return 日期
         */
        public static Date ld2dt(LocalTime lt,Date dt) {
            return localTime2date(lt,dt2ld(dt));
        }
        
        /**
         * 日期转LocalTime
         * @param lt 本地时间
         * @return LocalTime
         */
        public static Date  lt2dt(LocalTime lt) {
           return localTime2date(lt);
        }
        
        /**
         * 默认为当日日期
         * @param lt 本地时间
         * @return 日期
         */
        public static Date localTime2date(LocalTime lt) {
            return localTime2date(lt,null);
        }
        
        /**
         * 默认为当日日期
         * @param localTime
         * @param ld 补填出的日期,null 表示当日
         * @return 日期
         */
        public static Date localTime2date(LocalTime localTime,LocalDate ld) {
            LocalDate localDate = ld==null?LocalDate.now():ld;
            LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
            ZoneId zone = ZoneId.systemDefault();
            Instant instant = localDateTime.atZone(zone).toInstant();
            java.util.Date date = Date.from(instant);
            return date;
        }
        
        /**
         * 日期转LocalTime
         * @param date 日期值
         * @return LocalTime
         */
        public static LocalTime  dt2lt(Date date) {
            Instant instant = date.toInstant();
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            return localDateTime.toLocalTime();
        }
        
        /**
         * 
         * @param date
         * @return
         */
        public static LocalTime date2localTime(Date date) {
            Instant instant = date.toInstant();
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            return localDateTime.toLocalTime();
        }
        
        /**
         * localDateTime now
         * @return localDateTime now
         */
        public static LocalDateTime now() {
            return LocalDateTime.now();
        }
        
        /**
         * localDateTime now
         * @return localDateTime now
         */
        public static LocalDateTime ldtnow() {
            return LocalDateTime.now();
        }
        
        /**
         * localDate now
         * @return localDate now
         */
        public static LocalDate ldnow() {
            return LocalDate.now();
        }
        
        /**
         * Date now
         * @return Date now
         */
        public static Date dtnow() {
            return new Date();
        }
        
        /**
         * LocalTime Now
         * @return LocalTime Now
         */
        public static LocalTime ltnow() {
            return LocalTime.now();
        }
        
        /**
         * date2LocalDateTime的别名
         *
         * @param year
         * @param mon
         * @param day
         * @param hour
         * @param min
         * @param sec
         * @param ns
         * @return
         */
        public static LocalDateTime ldt(int year,int mon,int day,int hour,int min,int sec,int ns) {
            return LocalDateTime.of(year, mon,day, hour, min, sec, ns);
        }
        
        /**
         * date2LocalDateTime的别名
         * @param year
         * @param mon
         * @param day
         * @param hour
         * @param min
         * @param sec
         * @return
         */
        public static LocalDateTime ldt(int year,int mon,int day,int hour,int min,int sec) {
            return LittleTree.CronTime.ldt(year, mon, day, hour, min, sec,0);
        }
        
        /**
         * date2LocalDateTime的别名
         *
         * @param year
         * @param mon
         * @param day
         * @param hour
         * @param min
         * @return
         */
        public static LocalDateTime ldt(int year,int mon,int day,int hour,int min) {
            return LittleTree.CronTime.ldt(year, mon, day, hour, min, 0,0);
        }
        
        /**
         * date2LocalDateTime的别名
         *
         * @param year
         * @param mon
         * @param day
         * @param hour
         * @return
         */
        public static LocalDateTime ldt(int year,int mon,int day,int hour) {
            return LittleTree.CronTime.ldt(year, mon, day, hour, 0, 0,0);
        }
        
        /**
         * date2LocalDateTime的别名
         *
         * @param year
         * @param mon
         * @param day
         * @return
         */
        public static LocalDateTime ldt(int year,int mon,int day) {
            return LittleTree.CronTime.ldt(year, mon, day, 0, 0, 0,0);
        }
        
        /**
         * date2LocalDateTime的别名
         *
         * @param year
         * @param mon
         * @return
         */
        public static LocalDateTime ldt(int year,int mon) {
            return LittleTree.CronTime.ldt(year, mon, 0, 0, 0, 0,0);
        }
        
        /**
         * date2LocalDateTime的别名
         *
         * @param year
         * @return
         */
        public static LocalDateTime ldt(int year) {
            return LittleTree.CronTime.ldt(year, 0, 0, 0, 0, 0,0);
        }
        
        /**
         * 
         * @param year
         * @param month
         * @param dayOfMonth
         * @return
         */
        public static LocalDate ld(int year,int month,int dayOfMonth) {
            return LocalDate.of(year, month, dayOfMonth);
        }
        
        /**
         *  本地日期
         * @param datestr
         * @return
         */
        public static LocalDate ld(String datestr) {
            Date date = date(datestr);
            return dt2ld(date);
        }
        
        /**
         * 
         * @param hour
         * @param minute
         * @param second
         * @param nanoOfSecond
         * @return
         */
        public static LocalTime lt(int hour,int minute,int second,int nanoOfSecond) {
            return LocalTime.of(hour, minute, second, nanoOfSecond);
        }
        
        /**
         * 
         * @param hour
         * @param minute
         * @param second
         * @return
         */
        public static LocalTime lt(int hour,int minute,int second) {
            return LocalTime.of(hour, minute, second);
        }
        
        /**
         * 
         * @param hour
         * @param minute
         * @return
         */
        public static LocalTime lt(int hour,int minute) {
            return LocalTime.of(hour, minute);
        }
        
        /**
         * 
         * @param hour
         * @return
         */
        public static LocalTime lt(int hour) {
            return LocalTime.of(hour, 0);
        }
        
        /**
         * 整型
         * @param s 字符串
         * @return 整长整型
         */
        public static Integer i4(String s) {
            Integer i = null;
            try {
                i = Integer.parseInt(s);
            }catch(Exception e) {
                e.printStackTrace();
            }
            
            return i;
        }
        
        /**
         * 长整型
         * @param s 字符串
         * @return 长整型
         */
        public static Long lng(String s) {
            Long i = null;
            try {
                i = Long.parseLong(s);
            }catch(Exception e) {
                e.printStackTrace();
            }
            
            return i;
        }
        
        /**
         * 
         * @param s
         * @return
         */
        public static Double dbl(String s) {
            Double i = null;
            try {
                i = Double.parseDouble(s);
            }catch(Exception e) {
                e.printStackTrace();
            }
            
            return i;
        }

        /**
         * 本地时间
         * @param datestr
         * @return
         */
        public static LocalTime lt(String datestr) {
            if(datestr==null) return null;
            var p1=Pattern.compile("(\\d+):(\\d+)").matcher(datestr.strip());
            var p2=Pattern.compile("(\\d+):(\\d+):(\\d+)").matcher(datestr.strip());
            var p3=Pattern.compile("(\\d+):(\\d+):(\\d+)\\.(\\d+)").matcher(datestr.strip());
            LocalTime lt = null;
            try {
                if(p3.matches()) {
                    lt = lt(i4(p3.group(1)),i4(p3.group(2)),i4(p3.group(3)),i4(p3.group(4)));
                }else if (p2.matches())  {
                    lt = lt(i4(p2.group(1)),i4(p2.group(2)),i4(p2.group(3)));
                }else if (p1.matches())  {
                    lt = lt(i4(p1.group(1)),i4(p1.group(2)));
                }else {
                    // do nothing
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            
            return lt;
        }
        
        /**
         * 提取毫秒
         * @param date
         * @return
         */
        public static int MILLISECOND(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.MILLISECOND);
        }

        /**
         * 提取秒数
         * @param date
         * @return
         */
        public static int SECOND(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.SECOND);
        }
        
        /**
         * 提取分钟
         * @param date
         * @return
         */
        public static int MINUTE(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.MINUTE);
        }
        
        /*
         * 提取小时
         *@reutrn  0-23
         */
        public static int HOUR(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.HOUR_OF_DAY);
        }
        
        /**
         * 提取日期
         * @param date
         * @return
         */
        public static int DATEOFYEAR(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.DAY_OF_MONTH);
        }
        
        /**
         * 提取月份
         * @param date
         * @return
         */
        public static int MONTH(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.MONTH);
        }
        
        /**
         * 日期的即使
         * @param datestr
         * @return
         */
        public static Date date(String datestr) {
            Date d = null;
            if(datestr == null)return null;
            var s = datestr.strip();
            try {
                if(s.matches("\\d+-\\d+-\\d+")) {
                    d = sdf2.parse(s);
                }else {
                    d = sdf.parse(s);
                }
            }catch(Exception e) {
                //
            }
            return d;
        }
        
        /**
         * 日期的即使
         * @param datestr
         * @return
         */
        public static LocalDateTime ldt(String datestr) {
            return dt2ldt(date(datestr));
        }
        
        /**
         * 日期格式化式样：yyyy-MM-dd HH:mm:ss
         * @param pattern
         * @return
         */
        public static DateTimeFormatter dtf(String pattern) {
            return DateTimeFormatter.ofPattern(pattern);
        }
        
        public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        public static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    } // class CronTime 


    /**
     * 这是对一下的数据结构进行实现。
     * LittleTree.buildTree(categories,0,REC2("id","id","pid","pid"));
     * 从List<Tree> 结构生成节点树
     * 根节点id 默认为 "0";
     * @param recs 节点集合，节点需要包含由id,pid两个字段
     * @return
     */
    public static Node<IRecord> buildTree(List<IRecord> recs){
        return buildTree(recs,"0");
    }

    /**
     * 构建一棵树
     * @param sup_root 根节点函数
     * @param get_children 子节点函数
     * @return 树形结构的根节点
     */
    public static <T> Node<T> buildTree( Supplier<Node<T>> sup_root,
         Function<Node<T>,List<Node<T>>> get_children){
        Node<T> root = sup_root.get();

        Stack<Node<T>> stack = new Stack<Node<T>>();
        stack.push(root);// 根节点入栈，随后开始进行子节点遍历

        while(!stack.empty()) {
            Node<T> node = stack.pop();
            List<Node<T>> cc = get_children.apply(node);
            if(cc.size()>0) {
                stack.addAll(cc);
                node.addChildren(cc);
                //System.out.println(node+"：子节点为："+cc);
            }else {
                //System.out.println(node+"：没有子节点");
            }
        }//while

        return root;
    }

    /**
     * 这个应该是 最常用的生成结构
     *
     * 从List<Tree> 结构生成节点树
     *
     * @param recs 节点集合，节点需要包含由id,pid两个字段
     * @param rootId 根节点id， 根节点是通过字符串即对象的toString然后trim 的形式进行比较的。
     * @return 根节点 Node<IRecord>
     */
    public static Node<IRecord> buildTree(List<IRecord> recs ,Object rootId){
        return buildTree(recs ,rootId,SimpleRecord.REC2("id","id","pid","pid"));
    }
    
    /**
     * 这个应该是 最常用的生成结构：自动补充根节点
     *
     * 从List<Tree> 结构生成节点树
     * 当rootId 在List<IRecord> 中不存在的时候,在地宫创建一个根节点
     * REC("id",rootId,"name","根节点","pid",null) 添加到recs之中
     * @param recs 节点集合，节点需要包含由id,pid两个字段
     * @param rootId 根节点id， 根节点是通过字符串即对象的toString然后trim 的形式进行比较的。
     * @return 根节点 Node<IRecord>
     */
    public static Node<IRecord> buildTree2(final List<IRecord> recs ,final Object rootId){
        final var b = recs.stream().filter(e->e.get("id").equals(rootId)).findAny().isPresent();
        if(!b)recs.add(REC("id",rootId,"name","根节点","pid",null));
        return buildTree(recs ,rootId,SimpleRecord.REC2("id","id","pid","pid"));
    }
    
    /**
     * 从List<Tree> 结构生成节点树
     * 使用示例:
     * LittleTree.buildTree(categories,0,REC2("id","id","pid","p_id"));
     *
     * @param recs 节点集合，节点需要包含由id,pid两个字段
     * @param rootId 根节点id， 根节点是通过字符串即对象的toString然后trim 的形式进行比较的。
     * @param mappings 节点id,父节点id 与IRecord集合recs重属性字段的对应关系
     * @return
     */
    public static Node<IRecord> buildTree(List<IRecord> recs ,Object rootId,IRecord mappings){
        return buildTree(recs ,rootId,mappings.toMap());
    }

    /**
     * 从List<Tree> 结构生成节点树,生成一个U结构的树
     * 使用示例:
     * LittleTree.buildTree(categories,0,REC2("id","id","pid","p_id"));
     *
     * @param recs 节点集合，节点需要包含由id,pid两个字段
     * @param rootId 根节点id， 根节点是通过字符串即对象的toString然后trim 的形式进行比较的。
     * @param mappings 节点id,父节点id 与IRecord集合recs重属性字段的对应关系
     * @param node_builder 节点构建器
     * @return
     */
    public static <T,U  extends Node<T>> U buildTree(List<IRecord> recs,Object rootId,IRecord mappings,
        Function<Node<IRecord>,U> node_builder){
        
        if(node_builder==null||recs==null||mappings==null||rootId==null)return null;
        Node<IRecord> root = buildTree(recs,rootId,mappings);
        Map<String,U> nodeCache = new HashMap<>();
        U uroot = node_builder.apply(root);
        nodeCache.put(root.getPath(),uroot );
        root.forEach(e->{
            if(e.getParent()==null)return;
            U pcategory = nodeCache.get(e.getParentPath());//获得父节点
            U catnode = node_builder.apply(e);
            pcategory.addChild(catnode);
            nodeCache.put(e.getPath(), catnode);
        });
        return uroot;
    }

    /**
     * 从List<Tree> 结构生成节点树
     *
     * @param rootId 根节点id， 根节点是通过字符串即对象的toString然后trim 的形式进行比较的。
     * @param mappings 节点id,父节点id 与IRecord集合recs重属性字段的对应关系
     * @return
     */
    public static Node<IRecord> buildTree(List<IRecord> recs ,Object rootId,Map<String,?> mappings){
        //获得根节点
        String id =  (mappings.get("id")+"").trim();
        String pid = (mappings.get("pid")+"").trim();
        Optional<IRecord> optRoot = SimpleRecord.fetchOne(recs,id, rootId);

        if(!optRoot.isPresent())optRoot = SimpleRecord.fetchOne(recs.stream()
            .map(e->e.duplicate().set("id", e.get("id")+""))
            .collect(Collectors.toList()),id, rootId+"");// 转换成字符串类型进行比较

        if(!optRoot.isPresent()) {
            System.out.println("无法确认根节点(不存在根节点：id/"+rootId+")");
            return null;
        }//if

        final IRecord final_root = optRoot.get();
        //获取根节点
        Supplier<Node<IRecord>> sp_root = ()->new Node<IRecord>(final_root);
        //获得子节点
        Function<Node<IRecord>,List<Node<IRecord>>> get_children =
            (node)->recs.stream()
                .filter(e->node.recget(id).toString().equals(e.get(pid)))// 父子节点关系
                .map(e->new Node<IRecord>(e))
                .collect(Collectors.toList());
        //创建树结构
        Node<IRecord> tree = LittleTree.buildTree(sp_root,get_children);
        return tree;
    }

    /**
     * 创建属性结构
     * @param paths, 每条路径采用一维数组进行表示， 路径列表 [a,b,c1],[a,b,c2],....
     * @param node_creator  节点创建函数 （节点名称->节点数据)
     * @return 树形节点
     */
    public static <T> Node<T> buildTree(List<String[]> paths,Function<String,Node<T>> node_creator){
        Map<String,Node<T>> map = new LinkedHashMap<>();
        Function<List<String>,String> buildpath = list->list.stream().collect(Collectors.joining("/"));
        paths.forEach(ss->{
            List<String> list = new LinkedList<>();
            for(String s:ss) {
                String parent = buildpath.apply(list);
                list.add(s);
                String cur= buildpath.apply(list);
                if(map.get(cur)==null)map.put(cur, node_creator.apply(s));

                Node<T> pnode = map.get(parent);
                Node<T> node = map.get(cur);
                if(pnode!=null && !pnode.hasChild(node)) pnode.addChild(node);
            }//for
        });
        Node<T> root = map.values().iterator().next();
        return root;
    }


    /**
     * 遍历树形结构
     * @param root 根节点
     * @param cs 回调函数
     * @param level 阶层数
     */
    protected static <T> void traverse(Node<T> root,
        BiConsumer<Node<T>,Integer> cs,int level) {

        if(root!=null) {
            cs.accept(root,level);
            root.children.forEach(e->traverse(e,cs,level+1));
        }//if
    }

    /**
     * 遍历树形结构
     * @param root 根节点
     * @param cs 回调函数
     * @param level 阶层数
     */
    protected static <T> void traverse_throws(Node<T> root,
        BiConsumerThrows<Node<T>,Integer> cs,int level) throws Exception{
        if(root!=null) {
            if(cs.accept(root,level))for(Node<T>e:root.children)traverse_throws(e,cs,level+1);
        }//if
    }

    /**
     * 遍历树形结构
     * @param root 根节点
     * @param cs 回调函数 返回 false 终止遍历.
     * @throws Throwable
     */
    public static <T,U extends Node<T> > void traverse_throws2(U root,
        BiConsumerThrows<U,Integer> cs) throws Exception{
        traverse_throws2(root,cs,0);
    }


    /**
     * 遍历树形结构
     * @param root 根节点
     * @param cs 回调函数
     * @param level 阶层数
     */
    @SuppressWarnings("unchecked")
    protected static <T,U extends Node<T>> void traverse_throws2(U root,
        BiConsumerThrows<? super U,Integer> cs,int level) throws Exception{
        if(root!=null) {
            BiConsumerThrows<U,Integer> cs1 = (BiConsumerThrows<U,Integer>)cs;
            if(cs.accept(root,level))for(Node<T>e:root.getChildren())traverse_throws2((U)e,cs1,level+1);
        }//if
    }

    /**
     * 遍历树形结构
     * @param root 根节点
     * @param cs 回调函数
     */
    public static <T> void traverse(Node<T> root,BiConsumer<Node<T>,Integer> cs) {
        traverse(root,cs,0);
    }

    /**
     *
     * @author gbench
     * accept 返回 false终止遍历
     * @param <T> 输入参数1类型 用在 traverse_throws T类型就是节点类型
     * @param <U> 输入参数2的类型 用在 traverse_throws U类型就是当前节点所在阶层的数值的累心那个
     */
    public static interface BiConsumerThrows<T,U>{
        public boolean accept(T t,U u)throws Exception;
    }

    /**
     * 遍历树形结构
     * @param root 根节点
     * @param cs 回调函数 返回 false 终止遍历.
     * @throws Throwable
     */
    public static <T> void traverse_throws(Node<T> root,BiConsumerThrows<Node<T>,Integer> cs) throws Exception{
        traverse_throws(root,cs,0);
    }

    /**
     * 缩进尺度
     * @param n 缩进记录
     * @return 缩进字符串
     */
    public static <T> String ident(int n) {
        return rep("\t",n).stream().collect(Collectors.joining(""));
    }
    
    /**
     * 位数精度:fraction的别名
     * @param n 位数的长度
     * @return 小数位置的精度
     */
    public static Function<Object,String> frt(int n){
        return fraction(n);
    }

    /**
     * 位数精度
     * @param n 位数的长度
     * @return 小数位置的精度
     */
    public static Function<Object,String> fraction(int n){
        return v->{
            if(v==null) return"(null)";
            var line = "{0}";// 数据格式化
            if(v instanceof Date) {
                line = "{0,Date,yyyy-MM-dd HH:mm:ss}"; // 时间格式化
            }else if(v instanceof Number) {
                line = "{0,number,"+("#."+"#".repeat(n))+"}"; // 数字格式化
            }//if
            return MessageFormat.format(line, v);
        };// cell_formatter
    }

    /**
     * 重n个对象放在列表中那个
     * @param obj 对象
     * @param n
     * @return
     */
    public static <T> List<T> rep(T obj,int n) {
        return Stream
            .iterate(0, i->i+1).limit(n)
            .map(e->obj)
            .collect(Collectors.toList());
    }

    /**
     * 缩进尺度
     * @param ws 空白:whitespace
     * @param n 缩进记录
     * @return 缩进字符串
     */
    public static <T> String ident(String ws,int n) {
        return rep(ws,n).stream().collect(Collectors.joining(""));
    }

    /**
     * 生成Json的树形解构
     * @param node
     * @return
     */
    public static String json(Node<IRecord> node) {
        StringBuffer buffer = new StringBuffer();
        tranverse4json(node,0,buffer);
        return buffer.toString();
    }

    /**
     * 生成一个json的树形结构
     * @param node 节点
     */
    public static String tranverse4json(Node<IRecord> node) {
        StringBuffer buffer = new StringBuffer();// 构建一个局部变量用户递归遍历
        tranverse4json(node,0,buffer);
        return buffer.toString();
    }

    /**
     * 为了json而缓存
     * @param node 节点
     * @param level 层级
     * @param buffer 遍历缓存
     */
    private static StringBuffer tranverse4json(Node<IRecord> node,int level,StringBuffer buffer) {

        Function<IRecord,String> rec2jsn = (rec)->{
            return rec.stream()
                    .map(e->"\""+e.key()+"\":\""+e.value()+"\"")
                    .collect(Collectors.joining(","));
        };

        // 打印jsn
        buffer.append(ident(level)+"{"+rec2jsn.apply(node.getValue()));

        // 非叶子节点
        if(!node.isLeaf()) {
            buffer.append(",children:[\n");
            node.getChildren().forEach(e->tranverse4json(e,level+1,buffer));
        }

        boolean b = false;//是否是子节点中的最后一个
        if(node.getParent()!=null) {
            List<Node<IRecord>> cc = node.getParent().getChildren();
            if(cc.size()>0) {
                if(node.equals(cc.get(cc.size()-1)))b=true;
            }
        }

        // 收尾节点
        buffer.append(ident(level)+(node.isLeaf()?"":"]")+"}"+
                (node.isRoot()?"":(b?"":","))+"\n");

        return buffer;
    }
    
    /**
     * 字符串解析:
     * "1:10",解析成 1,2,3,4,5,6,7,8,9
     * 1 inclusive, 10 exclusive
     * @param line
     * @return 数组序列。
     */
    public static Integer[] NN(String line) {
        return NATS(line).stream().toArray(Integer[]::new);
    }
    
    /**
     * 字符串序列解析(函数):
     * "1:10",解析成 1,2,3,4,5,6,7,8,9
     * 1 inclusive, 10 exclusive
     * @param line
     * @return 数组序列。
     */
    public static Function<String,Integer[]> series=line->NN(line);
    
    /**
     * 字符串解析:
     * "1:10",解析成 1,2,3,4,5,6,7,8,9
     * 1 inclusive, 10 exclusive
     * @param line
     * @return 数组序列。
     */
    public static List<Integer> NATS(String line) {
        final var ll = new LinkedList<Integer>();
        final var matcher = Pattern.compile("\\s*([0-9-]+)\\s*:\\s*([0-9-]+)\\s*(:\\s*([0-9-]+))?\\s*").matcher(line);
        
        if(matcher.matches()) {
            try {
                final var n = matcher.groupCount();
                final var start = Integer.parseInt(matcher.group(1));
                final var end = Integer.parseInt(matcher.group(2));
                final var sign = end >=start ? 1:-1;
                var step = sign*1;// 步长
                final var s_step = matcher.group(4);
                if(n>2 && s_step!=null) step = sign*Math.abs(Integer.parseInt(s_step));
                for(var i=start;(sign>0?i<end:i>end);i+=step)ll.add(i);
            }catch(Exception e) {
                e.printStackTrace();
            }// try
        }// if
        
        return ll;
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * LIST的医用用途就是去克隆列表。比如：
     * -------------------------------
     * var a = asList(0,1,2,3,4,5,6,7);
     * var b = LIST(a.subList(1, 4));
     * b.addAll(asList(9,10,11));
     * 
     * a:[0, 1, 2, 3, 4, 5, 6, 7]
     * b:[1, 2, 3, 9, 10, 11]
     * --------------------------------
     * 对比：
     * var a = new LinkedList<Integer>();// 使用链表方便进行拆解。
     * a.addAll(asList(0,1,2,3,4,5,6,7));
     * var b = a.subList(1, 4);// 返回的是一个视图
     * b.addAll(asList(9,10,11));
     * 
     * a:[0, 1, 2, 3, 9, 10, 11, 4, 5, 6, 7]
     * b:[1, 2, 3, 9, 10, 11]
     * 
     * @param coll 流对象
     * @return coll元素组成的列表，注意这是一个coll对象浅层克隆的版本。
     * <T> 元素类型
     */
    public static <T> List<T> LIST(Collection<T> coll){
        if(coll==null)return null;
        return LIST(coll.stream());
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param tt 流对象
     * @return
     */
    public static <T> List<T> LIST(T[] tt){
        if(tt==null)return null;
        return LIST(Arrays.stream(tt));
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param stream 流对象
     * @return
     */
    public static <T> List<T> LIST(Stream<T> stream){
        if(stream==null)return null;
        return stream.collect(Collectors.toList());
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param stream 流对象
     * @param mapper 转换器
     * @return
     */
    public static <T,U> List<U> MAP(Stream<T> stream,Function<T,U> mapper){
        if(stream==null)return null;
        return stream.map(mapper).collect(Collectors.toList());
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 把一个T类型的List转换成一个U类型的 List
     * @param coll 流对象
     * @param mapper 转换器
     * @return
     */
    public static <T,U> List<U> MAP(Collection<T> coll,Function<T,U> mapper){
        if(coll==null)return null;
        return MAP(coll.stream(),mapper);
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 把一个T类型的List转换成一个R类型对象
     * @param coll 流对象
     * @param collector 转换器
     * @return
     */
    public static <T,A,R> R COLLECT(Collection<T> coll,Collector<? super T, A, R> collector){
        if(coll==null)return null;
        return coll.stream().collect(collector);
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
         * 把一个T类型的List转换成一个R类型对象
     * @param stream 流对象
     * @return 分类征集
     */
    public static <T,A,R> R COLLECT(Stream<T> stream,Collector<? super T, A, R> collector){
        if(stream==null)return null;
        return stream.collect(collector);
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 把一个T类型的List转换成一个 用逗号进行
     * @param coll 流对象
     * @return 分类征集
     */
    public static  String COMMASTR(Collection<?> coll){
        if(coll==null)return null;
        return SEQSTR(coll.stream(),",");
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 把一个T类型的List转换成一个 用逗号进行
     * @param stream 流对象
     * @return 分类征集
     */
    public static  String COMMASTR(Stream<?> stream){
        if(stream==null)return null;
        return SEQSTR(stream,",");
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 分隔符字符串序列
     * @param coll 流对象
     * @param separator 转换器
     * @return 分类征集
     */
    public static  String SEQSTR(Collection<?> coll,String separator){
        if(coll==null)return null;
        return SEQSTR(coll.stream(),separator);
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 分隔符字符串序列
     * @param stream 流对象
     * @param separator 转换器
     * @return 分类征集
     */
    public static  String SEQSTR(Stream<?> stream,String separator){
        if(stream==null)return null;
        return stream.map(Object::toString).collect(Collectors.joining(separator));
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param stream 流对象
     * @param pfilter 流对象过滤器
     * @return
     */
    public static <T> List<T> FILTER(Stream<T> stream,Predicate<T> pfilter){
        if(stream==null)return null;
        if(pfilter==null) return LIST(stream);
        return stream.filter(pfilter).collect(Collectors.toList());
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param stream 流对象
     * @param pfilter 流对象过滤器
     * @return
     */
    public static <T,U> List<U> FILTER(Stream<T> stream,Predicate<T> pfilter,Function<T,U>mapper){
        if(stream==null)return null;
        if(pfilter==null) return MAP(stream,mapper);
        return stream.filter(pfilter).map(mapper).collect(Collectors.toList());
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param coll 集合类
     * @param pfilter 流对象过滤器
     * @return
     */
    public static <T> List<T> FILTER(Collection<T> coll,Predicate<T> pfilter){
        if(coll==null)return null;
        if(pfilter==null) return LIST(coll.stream());
        return coll.stream().filter(pfilter).collect(Collectors.toList());
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param coll 集合类
     * @param pfilter 流对象过滤器
     * @return
     */
    public static <T,U> List<U> FILTER(Collection<T> coll,Predicate<T> pfilter,Function<T,U>mapper){
        if(coll==null)return null;
        if(pfilter==null) return MAP(coll.stream(),mapper);
        return coll.stream().filter(pfilter).map(mapper).collect(Collectors.toList());
    }


    /**
     * 对集合数据进行分组
     * @param coll 集合数据
     * @param classifier 分类器
     * @return
     */
    public static <T,K> Map<K,List<T>> GROUPBY(Collection<T> coll,Function<T,K> classifier){
        if(coll==null)return new HashMap<>();
        return GROUPBY(coll.stream(),classifier);
    }

    /**
     * 对集合数据进行分组
     * @param stream 集合数据
     * @param classifier  分类器
     * @return
     */
    public static <T,K> Map<K,List<T>> GROUPBY(Stream<T> stream,Function<T,K> classifier){
        if(stream==null)return new HashMap<>();
        return stream.collect(Collectors.groupingBy(classifier));
    }

    /**
     * 对集合数据进行分组
     * @param coll 集合数据
     * @param classifier  分类器
     * @return
     */
    public static <T,K> Map<K,T> GROUP2MAP(Collection<T> coll,Function<T,K> classifier){
        return GROUP2MAP(coll.stream(),classifier);
    }

    /**
     * 对集合数据进行分组
     * @param stream 集合数据
     * @param classifier  分类器
     * @return
     */
    public static <T,K> Map<K,T> GROUP2MAP(Stream<T> stream,Function<T,K> classifier){
        if(stream==null)return new HashMap<>();
        return (Map<K,T>)stream.collect(Collectors.groupingBy(classifier,Collector.of(()->new LinkedList<T>(),
                LinkedList::add,(aa,bb)->{aa.addAll(bb);return aa;},LinkedList::getFirst)));
    }

    /**
     * 对集合数据进行分组
     * @param coll 集合数据
     * @param classifier  分类器
     * @return
     */
    public static <T,K> Map<K,Collection<T>> GROUP(Collection<T> coll,Function<T,K> classifier){
        return GROUP(coll.stream(),classifier,e->e);
    }

    /**
     * 对集合数据进行分组
     * @param stream 集合数据
     * @param classifier  分类器
     * @return
     */
    public static <T,K> Map<K,Collection<T>> GROUP(Stream<T> stream,Function<T,K> classifier){
        return GROUP(stream,classifier,e->e);
    }

    /**
     * 对集合数据进行分组
     * @param coll 集合数据
     * @param classifier  分类器
     * @param finalizer  归并函数
     * @return
     */
    public static <T,K,U> Map<K,U> GROUP(Collection<T> coll,Function<T,K> classifier,Function<Collection<T>,U> finalizer){
        return GROUP(coll.stream(),classifier,finalizer);
    }

    /**
     * 对集合数据进行分组
     * @param stream 集合数据
     * @param classifier  分类器
     * @return
     */
    public static <T,K,U> Map<K,U> GROUP(Stream<T> stream,Function<T,K> classifier,Function<Collection<T>,U> finalizer){
        if(stream==null)return new HashMap<>();
        return (Map<K,U>)stream.collect(Collectors.groupingBy(classifier,Collector.of(()->new LinkedList<T>(),
                Collection::add,(aa,bb)->{aa.addAll(bb);return aa;},finalizer)));
    }

    /**
     * 检查集合中是否窜在一个元素满足指定条件
     * @param predicator 检查㢝元素是否满足指定的条件判断函数。
     * @return
     */
    public static <T,K> Boolean EXISTS(Stream<T> stream,Predicate<T> predicator){
        if(stream==null)return false;
        return stream.filter(predicator).findFirst().isPresent();
    }

    /**
     * 检查集合中是否窜在一个元素满足指定条件
     * @param predicator 检查㢝元素是否满足指定的条件判断函数。
     * @return
     */
    public static <T,K> Boolean EXISTS(Collection<T> coll,Predicate<T> predicator){
        if(coll==null)return false;
        return coll.stream().filter(predicator).findFirst().isPresent();
    }

    /**
     * 检查集合中是否窜在一个元素满足指定条件
     * @param e 集合中的元素
     * @return
     */
    public static <T> Boolean EXISTS2(Collection<T> coll,T e){
        if(coll==null)return false;
        return coll.stream().filter(g->g.equals(e)).findFirst().isPresent();
    }

    /**
     * 对集合数据进行分组
     * @param coll 集合数据
     * @param mapper  分类器
     * @param delimiter  分隔符
     * @return
     */
    public static <T> String JOIN(Collection<T> coll,Function<T,String> mapper,String delimiter) {
        return JOIN(coll.stream(),mapper,delimiter);
    }

    /**
     * 对集合数据进行分组
     * @param tt 集合数据
     * @param delimiter  分类器
     * @return
     */
    public static <T> String JOIN(T[] tt,String delimiter) {
        return JOIN(Arrays.stream(tt),e->e+"",delimiter);
    }

    /**
     * 对集合数据进行分组
     * @param tt 集合数据
     * @param delimiter  分类器
     * @return
     */
    public static <T> String JOIN(T[] tt,Function<T,String> mapper,String delimiter) {
        return JOIN(Arrays.stream(tt),mapper,delimiter);
    }

    /**
     * 对集合数据进行分组
     * @param coll 集合数据
     * @param delimiter  分割符号
     * @return
     */
    public static <T> String JOIN(Collection<T> coll,String delimiter) {
        return JOIN(coll.stream(),e->e+"",delimiter);
    }

    /**
     * 对集合数据进行分组
     * @param stream 集合数据
     * @param delimiter  分隔符
     * @return
     */
    public static <T> String JOIN(Stream<T> stream,String delimiter) {
        return JOIN(stream,e->e+"",delimiter);
    }

    /**
     * 对集合数据进行分组
     * @param stream 集合数据
     * @param mapper 转换器
     * @param delimiter 分隔符
     * @return
     */
    public static <T> String JOIN(Stream<T> stream,Function<T,String> mapper,String delimiter){
        if(stream==null)return "";
        return stream.map(mapper).collect(Collectors.joining(delimiter));
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 求两个集合之间的差. 把 coll2在coll1中存在的元素给与删除
     * coll1-coll2,
     * @param coll1 集合类1
     * @param coll2 集合类2
     * @return
     */
    public static <T> List<T> DIFF(Collection<T> coll1,Collection<T> coll2){
        return FILTER(coll1,e->!coll2.contains(e));
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * 求两个集合之间的交集
     * @param coll1 集合类2
     * @param coll2 集合类2
     * @return
     */
    public static <T> List<T> INTERSECT(Collection<T> coll1,Collection<T> coll2){
        return FILTER(coll1,e->coll2.contains(e));
    }

    /**
     * 之所以写这个方法就是为了把代码写的精减一些
     * @param coll1 集合类1
     * @param coll2 集合类2
     * @return
     */
    public static <T> List<T> UNION(Collection<T> coll1,Collection<T> coll2){
        List<T> list = LIST(coll1);
        list.addAll(coll2.stream().filter(e->!coll1.contains(e)).collect(Collectors.toList()));
        return list;
    }

    /**
     * 判断一个集合对象是否为空
     *
     * @param coll 集合对象，coll 为null 返回为null
     * @return coll 为null 返回为null
     */
    public static <T> Boolean EMPTY(Collection<T> coll) {
        if(coll==null)return null;
        return coll.isEmpty();
    }

    /**
     * 左部填充
     * @param coll 集合 not Set
     * @param n 长度
     * @return 左部填充了 指定字符的列表
     */
    public static <T> List<T> LFILL(Collection<T> coll,int n) {
        return LFILL(coll,n,null);
    }

    /**
     * 左部填充,填充到n位字长
     * @param coll 集合 not Set
     * @param n 长度
     * @param t 填充数据
     * @return 左部填充了 指定字符的列表
     */
    public static <T> List<T> LFILL(Collection<T> coll,int n,T t) {
        if(coll==null)return null;
        int size = coll.size();
        List<T> list = new LinkedList<T>();
        for(int i=0;i<n-size;i++)list.add(t);
        list.addAll(coll);
        return list;
    }

    /**
     * 右部填充
     * @param coll 集合 not Set
     * @param n 长度
     * @return 右部填充了 指定字符的列表
     */
    public static <T> List<T> RFILL(Collection<T> coll,int n){
        return RFILL(coll,n,null);
    }

    /**
     * 右部填充
     * @param coll 集合 not Set
     * @param n 长度
     * @param t 填充数据
     * @return 右部填充了 指定字符的列表
     */
    public static <T> List<T> RFILL(Collection<T> coll,int n,T t) {
        if(coll==null)return null;
        int size = coll.size();
        List<T> list = new LinkedList<T>();
        list.addAll(coll);
        for(int i=0;i<n-size;i++)list.add(t);

        return list;
    }

    /**
     * 拉链函数
     * @param names 合并在之后的键值名
     * @param objs
     * @return
     */
    public static List<IRecord> ZIP(String names,Object[]...objs){
        Collection<?>[] oo = (Collection<?>[]) Array.newInstance(Collection.class, objs.length);
        for(int i=0;i<objs.length;i++)oo[i]=Arrays.asList(objs[i]);
        return ZIP(names,oo);
    }

    /**
     * 拉链函数
     * @param names
     * @param objs
     * @return
     */
    public static List<IRecord> ZIP(String names,Collection<?>...objs){
        if(names==null)return ZIP((String[])null,objs);
        return ZIP(names.split("[,-]+"),objs);
    }

    /**
     * 拉链函数
     * @param names
     * @param objs 数组对象,把多个数组对象按照 names 给出的字段名顺序拼装成一个字段列表
     * @return 一个合并成功的Record列表
     */
    public static List<IRecord> ZIP(String[] names,Collection<?>...objs){
        Function<Collection<?>,Function<Integer,?>> getter = (coll)->{
            if(coll==null||coll.size()<=0)return x->null;// 空值返回一个空值函数
            Object[] oo = coll.toArray();
            int n = oo.length;
            return x->n>=0?oo[x%n]:oo[0];
        };

        List<Function<Integer,?>> gg= Stream.of(objs).map(getter).collect(Collectors.toList());
        Function<Integer,?> hh = getter.apply(Arrays.asList(names));
        if(names ==null) names = Stream.iterate(0,e->e+1).limit(objs.length).map(e->e+"").toArray(String[]::new);
        List<IRecord> ll = new LinkedList<>();
        int n  = Arrays.stream(objs).collect(Collectors.summarizingInt(Collection::size)).getMax();
        for(int i=0;i<n;i++) {
            IRecord rec = new SimpleRecord();
            for(int j=0;j<objs.length;j++)rec.add(hh.apply(j)+"",gg.get(j).apply(i));
            ll.add(rec);
        }//for

        return ll;
    }

    /**
     *
     * @param obj
     * @return obj为null 返回null
     */
    public static String ESCAPE2STR(Object obj) {
        if(obj==null)return null;
        return (obj+"").replaceAll("'", "").replaceAll("\"", "");
    }

    /**
     * 把一个record 改装成一个 只有 name->value两个字段值的 record列表
     * @param rec 字段值
     * @return
     */
    public static List<IRecord> REC2KVS(IRecord rec){
        return MAP(rec.kvs(),kvp->SimpleRecord.REC2("key",kvp.key(),"value",kvp.value()));
    }

    /**
     * 把一个record 改装成一个 只有 name->value两个字段值的 record列表
     * @param map 字段值
     * @return
     */
    public static List<IRecord> MAP2KVS(Map<String,?> map){
        return MAP(map.entrySet(),entry->SimpleRecord.REC2("key",entry.getKey(),"value",entry.getValue()));
    }

    /**
     * 过滤掉MAP指定的值
     * @param map
     * @param filter
     * @return
     */
    public static <K,V> Map<K,V> FILTER(Map<K,V> map,BiPredicate<K, V> filter){
        return FILTER(map,filter,e->e);
    }

    /**
     * 过滤掉MAP指定的值
     * @param map 映射对象
     * @param filter 过滤函数
     * @return
     */
    public static <K,V,U> Map<K,U> FILTER(Map<K,V> map,BiPredicate<K, V> filter,Function<V,U> mapper){
        Map<K,U> newMap = new HashMap<>();
        map.forEach((k,v)->{if(filter.test(k, v))newMap.put(k, mapper.apply(v));});
        return newMap;
    }
    
    /**
     * 对于复杂对象stream.distinct 无效，这个我一直灭有找到原因，修改了 equals 和hashcode 也是无效。
     * 为对象创建一个 根据键值进行去重的方法。
     * 使用示例： tt 是一个 含有“名称”项目的记录流
     * LIST(tt.stream().filter(distinctByKey(e->e.str("名称"))));
     * @param keyExtractor 提取对象键值
     * @return
     */
    public static <T> Predicate<T> DISTINCT_BY_KEY(Function<? super T, Object> keyExtractor){
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    /**
     * 计时函数
     * @param supplier 被计时函数
     * @return
     */
    public static double timeit(Supplier<?> supplier) {
        long begTime = System.nanoTime();
        supplier.get();
        long endTime = System.nanoTime();
        double duration = (endTime-begTime)/1000000d;
        System.out.println("last for:"+duration+"ms");
        return duration;
    }
    
    /**
     * 计时函数
     * @param command 待执行的命令
     * @return command 执行的运行时间
     */
    public static double timeit(Runnable command) {
        long begTime = System.nanoTime();
        command.run();
        long endTime = System.nanoTime();
        double duration = (endTime-begTime)/1000000d;
        System.out.println("last for:"+duration+"ms");
        return duration;
    }
    
    /**
     * 测试函数的执行时间： 例如：
     * timeit(p->{ 
     *    final List<List<?>> directions = LIST(iterate(0,i->i<10,i->i+1).map(e->asList(1,0,-1)));// 价格的变动方向 
     *    final var ff = cph(directions);// 生成涨跌列表 
     *    ff.forEach(e->e.compute("total",o->e.reduce(kv2int,0, (a,b)->a+b)));
     * System.out.println(FMT2(ff)); //结果输出 
     * },args,true);
     * 
     * @param <T> cons的参数的类型
     * @param cons 被统计函数
     * @param args 被统计函数 参数
     * @param isnano 是否采用纳秒进行时间统计 true 纳秒 false 毫秒时间单位
     * @return 历时时长 纳秒
     * @return 执行结果
     */
    public static <T> long timeit(Consumer<T> cons,T args,boolean isnano){
        final var ai = new AtomicInteger(0);
        final var th = new Thread(()->{
            while(ai.get()>=0) {
                System.out.println(MFT(". {0} S passed",ai.getAndIncrement()));
                try{ Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
            }// while
        });
        th.start();
        final var start = isnano?System.nanoTime():System.currentTimeMillis();
        cons.accept(args);
        final var end = isnano?System.nanoTime():System.currentTimeMillis();
        final var last = end-start;
        ai.set(-1);// 停止计时器
        System.out.println(MFT("\nlast for:{0} {1}",last,isnano?"ns":"ms"));
        return last;
    };
    
    /**
     * 毫秒的计时统计
     * @param cons 被统计函数
     * @param args 被统计函数 参数
     * @return 历时时长 毫秒
     */
    public static <T> long ms_timeit(Consumer<T> cons,T args){
        return timeit(cons,args,false);
    }
    
    /**
     * 把 一个数字 n转换成一个字母表中的数值(术语）
     * 在alphabetics中:ABCDEFGHIJKLMNOPQRSTUVWXYZ
     * 比如:0->A,1-B,25-Z,26-AA 等等
     * @param n 数字
     * @param alphas 字母表
     * @return 生成exel式样的名称
     */
    public static String nomenclature ( final Integer n, final String[] alphas){
        final int model = alphas.length;// 字母表尺寸
        final List<Integer> dd = new LinkedList<Integer>();
        Integer num = n;
        do {
            dd.add(num%model);
            num /= model;// 进入下回一个轮回
        } while( num -- >0 ); // num-- 使得每次都是从A开始，即Z的下一个是AA而不是BA
        // 就是这个简答但算法我想了一夜,我就是不知道如何让10位每次都从0开始。
        Collections.reverse(dd);
        return dd.stream()
            .map(e->alphas[e])
            .collect(Collectors.joining(""));
    }
    
    /**
     * 列名称： 从0开始
     * 0->A,1->B;2->C;....,25->Z,26->AA
     * @param n 数字
     * @return 类似于EXCEL的列名称
     */
    public static String excelname(final int n) {
        //字母表
        String alphabetics[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""); 
        return nomenclature(n,alphabetics);
    }
    
    /**
     * 纳秒的计时统计
     * @param cons 被统计函数
     * @param args 被统计函数 参数
     * @return 历时时长 纳秒
     */
    public static <T> long ns_timeit(Consumer<T> cons,T args){
        return timeit(cons,args,true);
    }
    
    /**
     * 用法示例:// 修改Database上的注解JdbcConfig的url属性
     * SET_FIELD_OF_ANNOTATION(ANNO(Database.class, JdbcConfig.class), "url","jdbc:neo4j:bolt://localhost/mydb?nossl");
     * 
     * 获得指定对象obj的对应class的注解ana，当对obj为class则直接获取其中的注解ana
     * 注意：ANNO无法获取代理对象上的注解．因为在代理对象的类可能是cglib给予组装出来的类
     * @param <T> 注解的类的类型
     * @param target 目标对象实例，指定对象obj的对应class的注解annotation，当对obj为class则直接获取其中的注解annotation
     * @param annotationClass 目标注解类型
     * @return　ana注解的内容
     */
    public static <T extends Annotation> T ANNO(Object target,Class<T> annotationClass) {
        Class<?> cls = target instanceof Class<?> ?( Class<?>)target:target.getClass();
        return cls.getAnnotation(annotationClass);
    }
    
    /**
     * 用法示例:// 修改Database上的注解JdbcConfig的url属性
     * SET_FIELD_OF_ANNOTATION(ANNO(Database.class, JdbcConfig.class), "url","jdbc:neo4j:bolt://localhost/mydb?nossl");
     * 修改注解中的字段内容
     * @param <T> 注解类型
     * @param annotation 注解对象实例
     * @param annofld 注解中的字段名称
     * @param annoValue 修正的内容．
     */
    public static <T extends Annotation>  void SET_FIELD_OF_ANNOTATION(T annotation ,
        String annofld,Object annoValue) {
        //注解修改工具,只有value非空才给予修改
        BiConsumer<String,Object> modifer = (fldName,value)->{// 修改计划函数
            try {
                if(annotation==null||value==null)return;//只有value非空才给予修改
                InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
                //var flds = invocationHandler.getClass().getDeclaredFields();
                //for(var fld:flds) System.out.println(fld.getName()+"/"+fld.getType());// 产看注解类的成员结构
                
                // 获取注解中数据
                final String annotationFieldsName = "memberValues"; // 注解中配置的字段存在于memberValues
                Field fld = invocationHandler.getClass().getDeclaredField(annotationFieldsName);
                fld.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, Object> memberValues = (Map<String, Object>) fld.get(invocationHandler);
                if(value!=null)memberValues.put(fldName, value);
            } catch (Exception e) {
                e.printStackTrace();
            }// try
        };// modifer
        
        // 执行修改计划
        modifer.accept(annofld,annoValue);
    }
    
    /**
     * 自然数子集合．0,1,2,3,....
     * 生成一个特定长度的字数字流,从０开始
     * @param size　流的长度,n 为null 或者0,负数返回无限流.
     * @return 序列长度
     */
    public static Stream<Long> NATS(Integer size){
        if(size==null||size<=0)NATS();
         return Stream.iterate(0l, (Long i)->i+1l).limit(size);
     }
 
    /**
     * 自然数子集合．0,1,2,3,....
     * 生成一个无限长度的数字流.0,1,2,3,....
     * @return 数字序列
     */
    public static  Stream<Long> NATS(){
         return Stream.iterate(0l, (Long i)->i+1l);
    }
    
    /**
     * double 随机数　: DouBLs
     * @param n 随机数的superior
     * @param nfractions 小数的位数
     * @return 浮点数的数字
     */
    public static Double[] DBLS(final int n) {
        return NATS(n).map(e->new Random().nextDouble()).toArray(Double[]::new);
    }
    
    /**
     * double 随机数　: DouBLs
     * @param n 随机数的superior
     * @param nfractions 小数的位数
     * @return 浮点数的数字
     */
    public static double[] dbls(final int n) {
        return NATS(n).map(e->new Random().nextDouble()).mapToDouble(e->e).toArray();
    }
    
    /**
     * double 随机数　:DouBLs Stream
     * @param n 随机数的superior
     * @param nfractions 小数的位数
     * @return 浮点数的数字的流
     */
    public static Stream<Double> DBLSTREAM(final int n) {
        return NATS(n).map(e->new Random().nextDouble());
    }
    
    /**
     * double 随机数　
     * @param n 随机数的superior
     * @param nfractions 小数的位数
     * @return
     */
    public  static Double RNDDBL(final int n,final int nfractions) {
        
        final Number  d = Math.random()*n;
        var f= "#.";
        for(var i=0;i<nfractions;i++)f+="0";
        var df = new DecimalFormat(f);
        String s = df.format(d);
        //System.out.println(s);
        return Double.parseDouble(s);
    }
    
    /**
     * int 随机数　
     * @param n 随机数的superior
     * @return
     */
    public static int RNDINT(int n) {
        return new Random().nextInt(n);
    }
    
    /**
     * Ｍessage FormaT
     * 数据格式化
     * @param pattern:number,date
     * @param arguments:参数列表
     * @return　数据格式化
     */
    public static String MFT(String pattern,Object ...arguments) {
        return MessageFormat.format(pattern, arguments);
    }
    
    /**
     * 
     * @param n 重复的次数
     * @param t 重复的元素
     * @return List<T>
     */
    public static <T> List<T> REPEAT(int n,T t){
        return LIST(NATS(n).map(i->t));
    }
    
    /**
     * REPEAT 的别名
     * @param n 重复的次数
     * @param t 重复的元素
     * @return List<T>
     */
    public static <T> List<T> RPT(int n,T t){
        return REPEAT(n,t);
    }
    
    /**
     * REPEAT 的别名  的流的返回值
     * @param n 重复的次数
     * @param t 重复的元素
     * @return List<T>
     */
    public static <T> Stream<T> RPTS(int n,T t){
        return REPEAT(n,t).stream();
    }
    
    /**
     * REPEAT 的别名  的数组类型的返回值
     * @param n 重复的次数
     * @param t 重复的元素
     * @return List<T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] RPTA(final int n,final T t){
        final Class<T> tclass = t==null?(Class<T>)Object.class:(Class<T>)t.getClass();
        return REPEAT(n,t).toArray(a->(T[])Array.newInstance(tclass, a));
    }
    
    /**
     * 包装一个计数器:计数变量从init开始,每次增加 step
     * 这就是模拟for(long i = start;i<end;i+=step) 的语句。
     * 用于对一个stream进行遍历和截取。
     * 
     * @param start 开始数值
     * @param step 步长
     * @param tester 测试函数:(i,t)-> true|false,
     * i 索引从start开始，t 当前的数值。
     * @return
     */
    public static <T> Predicate<T> i_for(long start,long step,BiPredicate<Long,T> tester){
        AtomicLong atom = new AtomicLong(start);
        return t->tester.test(atom.getAndAdd(step),t);
    }
    
    /**
     * 包装一个计数器:计数变量从init开始,每次增加 step
     * 
     * Stream.iterate(0, i->i+1).takeWhile(i_for(6)).forEach(System.out::println);
     * Stream.iterate(0,t->t<6, i->i+1).forEach(System.out::println);
     * 
     * @param n 序列的长度
     * @return Predicate 的谓词判断
     */
    public static <T> Predicate<T> i_for(final long n){
        return i_for(0l,1l,(i,t)->i<n);
    }
    
    /**
     *  Key->Value:<br>
     *  0->t1,1->t1,n->tn-1,... <br>
     *  NATS(10).map(kvp()).forEach(System.out::println);<br>
     * @return 键值列表
     */
    public static <T> Function<T,KVPair<Long,T>> kvp(){
        var atom = new AtomicLong(0l);// 状态缓存：用于生成序号
        return t->new KVPair<>(atom.getAndIncrement(),t);
    }
    
    /**
     * key_generator 根据key 生成 相应的主键。
     * NATS(10).map(kvp(t->t%2)).forEach(System.out::println);
     * 
     * @param <K> 键值名类型
     * @param <T> 键值类型
     * @param key_generator 键变换函数
     * @return 键值列表
     */
    public static <T,K> Function<T,KVPair<K,T>> kvp(Function<T,K>key_generator){
        return t->new KVPair<>(key_generator.apply(t),t);
    }
    
    /**
     * 列表延展：即构造 全排列的组合。比如：comprehensive([1,2],[a,b]) 会产生：如下结构的数据 <br>
     * 0    1 表头 <br>
     * -------- <br>
     * 1    a 数据名项目 <br>
     * 1    b 数据名项目 <br>
     * 2    a 数据名项目 <br>
     * 2    b 数据名项目 <br>
     * 
     * @param ccc 待延展的列表集合。位置向量集合 [cc1,cc2,...] ccc 表示集合的集合即 列表的元素依然是列表
     * @param position 当前的位置
     * @param rr 返回结果集合。需要递归累加, r 表示result,rr 象征着 r 的集合。
     */
    public static void _comprehensive(final List<List<?>> ccc,int position,List<IRecord> rr){
        if(ccc==null || ccc.size()<0 || position>=ccc.size())return;// 保证 参数合法有效。
        
        if(position>=0) {
            final var cc = ccc.get(position);// 提取当前位置的列表数组
            if(rr.size()==0){
                cc.forEach(c->rr.add(REC(position,c)));
            }else{
                
                // 这是一个写起来有效率，但运行起来没有效率的方法
                //final var aa = LIST(cc.stream().flatMap(c->rr.stream().map(r->REC(position,c).union(r))));// 列表向前展开一层。
                //使用aa内容展开一层后代的结果，替换上一次计算的结果：即模拟:rr = aa的结果，把啊啊赋值与rr
                //rr.clear(); rr.addAll(aa);
                
                // 这是一种运行起来很有效率高：但写起来没有效率的方法,考虑到运行，就采用这种写起来没有效率的算法了。haha 
                final var r_litr  = rr.listIterator();// listIterator 带有插入功能,listIterator add 会自动移动itr
                while ( r_litr.hasNext() ) { // r_litr 的迭代器
                    final var r_current = r_litr.next();// 待给予展开的元素节点
                    final var pos = String.valueOf(position);// 当前位置
                    final var c_itr = cc.iterator();// 当前位置向量
                    var isfirst = true;// 标志变量:
                    while (c_itr.hasNext()) {
                        final var c = c_itr.next();// 待追加到r_current的数据元素。
                        if ( isfirst ) {// 第一个元素
                            r_current.add(pos, c); isfirst=false;
                        } else {// 其他元素
                            r_litr.add(r_current.duplicate().set(pos,c));// 追加最新的 数据记录。
                        }//if
                    }//while c_itr
                }//while l_itr
                
            }//if if(rr.size()==0)
            
            // 继续向前进行延展
            _comprehensive(ccc,position-1,rr);
        }// 仅当尚未结束。 position>=0
    }
    
    /**
     * 数组的comprehensive
     * @param ccc 待延展的列表集合。ccc 表示集合的集合即 列表的元素依然是列表
     * @return 返回结果集合。需要递归累加
     */
    public static List<IRecord> comprehensive(final List<List<?>> ccc) {
        if(ccc==null || ccc.size()<0)return new LinkedList<>();
        List<IRecord> rr = new LinkedList<>(); // 构造返回结果集合
        _comprehensive(ccc,ccc.size()-1,rr);
        return rr;
    }
    
    /**
     * comprehensive 的别名
     * 数组的comprehensive
     * @param ccc 待延展的列表集合。ccc 表示集合的集合即 列表的元素依然是列表
     * @return 返回结果集合。需要递归累加
     */
    public static List<IRecord> cph(final List<List<?>> ccc) {
        return comprehensive(ccc);
    }
    
    /**
     * comprehensive 的别名:返回Stream结果
     * 数组的comprehensive
     * @param ccc 待延展的列表集合。ccc 表示集合的集合即 列表的元素依然是列表
     * @return 返回结果集合。需要递归累加
     */
    public static Stream<IRecord> cph2(final List<List<?>> ccc) {
        return comprehensive(ccc).stream();
    }
    
    /**
     * comprehensive 的别名
     * 数组的comprehensive
     * @param ccc 待延展的列表集合。ccc 表示集合的集合即 列表的元素依然是列表
     * @return 返回结果集合。需要递归累加
     */
    public static List<IRecord> cph(final Stream<List<?>> ccc) {
        return comprehensive(LIST(ccc));
    }
    
    /**
     * 数组的comprehensive
     * @param ccc 待延展的列表集合。
     * @return 返回结果集合。需要递归累加
     */
    public static List<IRecord> comprehensive(final List<?> ... ccc) {
        if(ccc==null || ccc.length<0)return new LinkedList<>();
        List<IRecord> rr = new LinkedList<>(); // 构造返回结果集合
        _comprehensive(Arrays.asList(ccc),ccc.length-1,rr);
        return rr;
    }
    
    /**
     * comprehensive 的别名
     * @param ccc 待延展的列表集合。
     * @return 返回结果集合。需要递归累加
     */
    public static List<IRecord> cph(final List<?> ... ccc) {
        return comprehensive((List<?>[])ccc);
    }
    
    /**
     * comprehensive(LittleTree::NN,"1:10:2") 示例程序
     * 数组的comprehensive
     * 用法示例
     * comprehensive(s->s.split(","),"1,2,3,4","a,b,c,d").forEach(System.out::println);
     * 
     * @param <S> 源数据类型
     * @param <T> 目标的数据元素类型 
     * @param parser S对象解析函数
     * @param ss 数据源集合。
     * @return List<IRecord> 结构的元素 结合
     */
    public static <S,T> List<IRecord> comprehensive(final Function<S,T[]> parser,
        @SuppressWarnings("unchecked") S...ss) {
        List<List<?>> ccc= LIST(Arrays.asList(ss).stream().map(parser).map(Arrays::asList));
        return comprehensive(ccc);
    }
    
    /**
     * comprehensive 的别名
     * comprehensive(LittleTree::NN,"1:10:2") 示例程序
     * 数组的comprehensive
     * 用法示例
     * comprehensive(s->s.split(","),"1,2,3,4","a,b,c,d").forEach(System.out::println);
     * 
     * @param <S> 源数据类型
     * @param <T> 目标的数据元素类型 
     * @param parser S对象解析函数
     * @param ss 数据源集合。
     * @return List<IRecord> 结构的元素 结合
     */
    public static <S,T> List<IRecord> cph(final Function<S,T[]> parser,
        @SuppressWarnings("unchecked") S...ss) {
        return comprehensive(parser,(S[])ss);
    }
    
    /**
     * comprehensive 的别名
     * comprehensive(LittleTree::NN,"1:10:2") 示例程序
     * 数组的comprehensive
     * 用法示例
     * comprehensive(s->s.split(","),"1,2,3,4","a,b,c,d").forEach(System.out::println);
     * 
     * @param <S> 源数据类型
     * @param <T> 目标的数据元素类型
     * @param parser S对象解析函数
     * @param ss 数据源集合。
     * @return Stream<IRecord> 结构的IRecord流 ，每个IRecord元素是倒序的。
     */
    public static <S,T> Stream<IRecord> cph2(final Function<S,T[]> parser,
        @SuppressWarnings("unchecked") S...ss) {
        return comprehensive(parser,(S[])ss).stream().map(e->e.reverse());
    }
    
    /**
     * 数组的comprehensive
     * @param ccc 待延展的列表集合。
     * @return 返回全排列的结果  IRecord集合的流。
     */
    public static List<IRecord> comprehensive(final Object[] ... ccc) {
        if(ccc==null || ccc.length<0)return new LinkedList<>();
        List<IRecord> rr = new LinkedList<>();
        List<List<?>> lll = new LinkedList<List<?>>();
        for(var cc:ccc)lll.add(Arrays.asList(cc));
        _comprehensive(lll,ccc.length-1,rr);
        return rr;
    }
    
    /**
     * comprehensive 的别名，对ccc 中的集合元素进行全排列。
     * @param ccc 待延展的列表集合。
     * @return 返回全排列的结果  IRecord集合的流。
     */
    public static List<IRecord> cph(final Object[] ... ccc) {
        return comprehensive((Object[][])ccc);
    }
    
    /**
     * comprehensive 的别名,对ccc 中的集合元素进行全排列。
     * 对排列结果，采用reverse以维持原有谁能够。
     * @param ccc 待延展的列表集合。
     * @return 返回全排列的结果  IRecord集合的流。
     */
    public static Stream<IRecord> cph2(final Object[] ... ccc) {
        return comprehensive((Object[][])ccc).stream().map(e->e.reverse());
    }
    
    public static boolean debug = false;// 调试信息开启标记
    
}
