package gbench.common.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

import static gbench.common.fs.XlsFile.DColumn.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



/**
 * 使用时需要导入 POI 依赖

        <!-- https://mvnrepository.com/artifact/org.apache.poi/poi -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi</artifactId>
            <version>4.1.0</version>
        </dependency>
        
        <!-- https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml-schemas -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>4.1.0</version>
        </dependency>
        
        <!-- https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml-schemas -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml-schemas</artifactId>
            <version>4.1.0</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.apache.poi/poi-scratchpad -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-scratchpad</artifactId>
            <version>4.1.0</version>
        </dependency>
        
        <!-- https://mvnrepository.com/artifact/org.apache.poi/poi-excelant -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-excelant</artifactId>
            <version>4.1.0</version>
        </dependency>
 * 
 */


/**
 * EXCEL的 简单处理函数
 * 
 * @author gbench
 *
 */
public class XlsFile {
    
    /**
     * 按键值对儿
     * @author admin
     *
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static class KVPair <K,V> implements IKVPair<K,V>{
        
        /**
         * 键值对儿
         * @param k 键名
         * @param v 键值
         */
        public KVPair(K k,V v) {
            this.k = k;
            this.v = v;
        }
        
        public K key(){
            return k;
        }
        
        public V val(){
            return v;
        }
        
        public void val(V v) {
            this.v = v;
        }
        
        public void key(K k) {
            this.k = k;
        }
        
        @Override
        public K getKey() {
            return k;
        }
    
        @Override
        public V getValue() {
            return v;
        }
    
        @Override
        public void setKey(K key) {
            this.k = key;
        }
    
        @Override
        public void setValue(V value) {
            this.v = value;
        }
        
        /**
         * 
         * @param kvp
         * @return
         */
        @Override
        public boolean equals(Object obj) {
            //System.out.println(kvp);
            if(obj==null)return false;
            if(!(obj instanceof KVPair))return false;
            KVPair<?,?> kvp = (KVPair<?,?>)obj;
            if(this.k==null && kvp.k==null) {
                if(this.v==null && kvp.v==null) return true;
            }
            if(this.k == null)return false;
            if(this.v == null)return false;
            if(this.k.equals(kvp.k) && 
                this.v.equals(kvp.v))return true;
            return false;
        }
        
        /**
         * 
         */
        public int hashCode() {
            return (this.k+""+this.v).hashCode();
        }
        
        /**
         * 
         */
        public String toString() {
            return "[ "+k+"-->"+v+" ]";
        }
        
        /**
         * 
         * @param <K1> 键类型
         * @param <V1> 值类型
         * @param k 键
         * @param v 值
         */
        public static <K1,V1> KVPair<K1,V1> KVP(K1 k,V1 v){
            return new KVPair<>(k,v);
        }
        
        private K k;
        private V v;
        
    }

    /**
     * 二元组
     * @author gbench
     *
     * @param <X> 第一组分类型
     * @param <Y> 第二组分类型
     */
    public static class Tuple2<X,Y>{// 点对象
        
        /**
         * 构造函数
         * @param _1
         * @param _2
         */
        public Tuple2(X _1, Y _2) {
            this._1 = _1;
            this._2 = _2;
        };

        /**
         * 返回第一组分
         * @return 第一组分
         */
        public X _1() {
            return _1;
        };

        /**
         * 返回第二组分
         * @return 第二组分
         */
        public Y _2() {
            return _2;
        };

        /**
         * 设置第一组分
         * @param x 第一组分
         * @return 第一组分
         */
        public Tuple2<X, Y> _1(X x) {
            this._1 = x;
            return this;
        }

        /**
         * 设置第二组分
         * @param y 第儿组分
         * @return 第二组分
         */
        public Tuple2<X, Y> _2(Y y) {
            this._2 = y;
            return this;
        }

        /**
         * 字符串格式化
         */
        public String toString() {
            return MessageFormat.format("({0} -> {1})", _1, _2);
        }
        
        /**
         * 快速生成 Tuple2的实例
         * @param <X1> 第一组分类型
         * @param <Y1> 第二组分类型
         * @param x1 第一组分值
         * @param y1 第二组分值
         * @return Tuple2的实例
         */
        public static <X1,Y1> Tuple2<X1,Y1> TUPLE2(X1 x1,Y1 y1){
            return new Tuple2<>(x1,y1);
        }
        
        /**
         * 合并两条列表为一个列表
         * @param <T> 第一个列表的元素类型
         * @param <U> 第二个列表的元素类型
         * @param tt 第一个列表
         * @param uu 第二个李彪
         * @return 合并后的二元元组列表
         */
        public static <T,U> List<Tuple2<T,U>> zip(List<T> tt,List<U>uu) {
            List<Tuple2<T,U>> tups = new LinkedList<>();
            int n = Math.max(tt.size(), uu.size());
            var titr = tt.iterator();
            var uitr = uu.iterator();
            for(int i=0;i<n;i++) {
                final var tup = new Tuple2<T,U>(null,null);
                if(!titr.hasNext())titr = tt.iterator();
                if(!uitr.hasNext())uitr = uu.iterator();
                tup._1(titr.next());
                tup._2(uitr.next());
            }//for
            return tups;
        }

        protected X _1; // 第一组分
        protected Y _2; // 第二组分
    }
    /**
     * 
     * @author XUQINGHUA
     *
     * @param <K>
     * @param <V>
     */
    public static interface IKVPair <K,V> {
        /**
         * 获得键值
         * @return
         */
        public K getKey();
        /**
         * 获得值信息
         * @return
         */
        public V getValue();
        
        /**
         * 设置键信息
         * @param key
         */
        public void setKey(K key);
        
        /**
         * 设置值信息
         * @param value
         */
        public void setValue(V value);
    }
    
    /**
     * 单键多值对儿
     * 一个  {name:elems} 的键值对儿,这里对 KVPair 做一个扩展。
     * @author gbench
     *
     * @param <T> 列元素的类型
     * @param <SELF> 是自身元素用户标识继承元素自己。 对于   对于需要返回自身类型的函数比如 setElems,setName之类的设置函数，采用SELF用来标识自己。
     * 为了对SELF 标识的对象赋予方法：这里让SELF extends AbstractK2VV<T,SELF>，由此 出现了一个类型继承自己的现象.同事也反映了，SELF 
     * 就是 AbstractK2VV<T,SELF> 本身 的意思。
     */
    public static abstract class AbstractK2VV<T, SELF extends AbstractK2VV<T,SELF> > 
        extends KVPair<String,List<T>>{
        
        /**
         * 默认的字段名称:
         * @param size 尺寸大小
         * @return 生成一个默认尺寸大小的字段列表
         */
        public List<String> defaultFieldName(final Number size){
            return Stream.iterate(0,i->i<size.intValue(),i->i+1)
            .map(Object::toString).collect(Collectors.toList());
        }
        
        /**
         * 构造函数
         * @param name 键名
         * @param elems 值集合
         */
        @SuppressWarnings("unchecked")
        public AbstractK2VV(final String name, final List<T> elems) {
            super(name, (List<T>)Arrays.asList(elems.toArray()));
            if(TypeU.isNumberic(name)) {
                this.id = TypeU.coerce(name,Integer.class);
            }
            this.fieldNames = this.defaultFieldName(this.size());
        }
        
        /**
         * 构造函数
         * @param name 键名
         * @param elems 值集合
         */
        @SuppressWarnings("unchecked")
        public AbstractK2VV(final Number name, final List<T> elems) {
            super(name+"", (List<T>)Arrays.asList(elems.toArray()));
            this.id = name.intValue();
            this.fieldNames = this.defaultFieldName(this.size());
        }
        
        /**
         * 构造函数
         * @param name 键名
         * @param elems 值集合
         * @param fieldNames 字段名集合
         */
        @SuppressWarnings("unchecked")
        public AbstractK2VV(final Number name, final List<T> elems, final List<String> fieldNames) {
            super(name+"", (List<T>)Arrays.asList(elems.toArray()));
            this.id = name.intValue();
            this.fieldNames = fieldNames == null || fieldNames.size()<1
                ? this.defaultFieldName(this.size())
                : fieldNames;
        }
        
        /**
         * 构造函数
         * @param name 键名
         * @param elems 值集合
         * @param fieldNames 字段名集合
         */
        @SuppressWarnings("unchecked")
        public AbstractK2VV(final String name, final List<T> elems, final List<String> fieldNames) {
            super(name+"", (List<T>)Arrays.asList(elems.toArray()));
            this.fieldNames = fieldNames == null || fieldNames.size()<1
                ? this.defaultFieldName(this.size())
                : fieldNames;
            if(TypeU.isNumberic(name)) {
                this.id = TypeU.coerce(name,Integer.class);
            }
        }
        
        /**
         * 单键多值对儿的名称
         * 获取K2VV的囊
         * @return 列名
         */
        public String getName() {
            return this.key();
        }
        
        /**
         * 列名
         * @return 设置列名并返回自身
         */
        @SuppressWarnings("unchecked")
        public SELF setName(final String key) {
           this.key(key);
           return (SELF) this;
        }
        
        /**
         * 列长度
         * @return
         */
        public long size() {
            if(this.val()==null)return 0l;
            return this.val().size();
        }
        
        /**
         * 列元素集合
         */
        @SuppressWarnings("unchecked")
        public List<T> getElems(){
            if(this.val()==null)return (List<T>)Arrays.asList();
            return this.val();
        }
        
        /**
         * 
         * @param i 元素需要从0开始
         * @param element 元素内容
         * @return this  本省
         */
        @SuppressWarnings("unchecked")
        public SELF setElem(final int i,final T element){
            if(i<this.size()) {
                this.val().set(i, element);
            }
            return (SELF)this;
        }
        
        /**
         * 获取列数据元素
         */
        public T getElem(final int i){
            if(i<this.size()) {
                return this.getElems().get(i);
            }
            return null;
        }
        
       /**
         * 提取列元素的类型
         * @return
         */
        @SuppressWarnings("unchecked")
        public Class<T> getElemClass(){
            if(this.getElems()==null || this.getElems().size()<1)return (Class<T>)Object.class;
            return this.stream().filter(e->e!=null).map(e->(Class<T>)e.getClass()).findAny().orElse((Class<T>)(Object)Object.class);
        }

        /**
        * 
        * @param <U> 元素类型
        * @param mapper 元素变换函数
        * @return 变换后的数据元素
        */
        public <U> List<U> mapElems(final Function<T,U> mapper){
           return this.getElems().stream().map(mapper).collect(Collectors.toList());
        }
        
        /**
         * 借尸还魂，用老的的K2VV变成新的K2UU.
         * @param name 名称
         * @param uu 值元素
         * @return 新创建的对象元素。
         */
        @SuppressWarnings("unchecked")
        public SELF newInstance(final String name,final List<?>uu) {
            SELF instance = null;
            try {// 需要注意uu 通常并不是List<T> 类型而是 List<U>的类型，这里用到了Java的类型擦除机制。使的可以动态更改集合元素中的类型。
                var ctor = this.getClass().getDeclaredConstructor(new Class<?>[] {String.class,List.class});// 日趋对象构造函数。
                instance = (SELF)ctor.newInstance(this.getName(),uu);// 生成新的对象实例。
            } catch (Exception e) {
                e.printStackTrace();
            }// try
            return instance;
        }
        
        /**
         * 元祖变换 : 私有的类型映射。
         * @param <U> 元素类型
         * @param mapper 元素变换函数
         * @return 变换后的数据元素
         */
        public <U> SELF _map(final Function<T,U> mapper){
            return this.newInstance(this.getName(),this.mapElems(mapper));// java 的类型擦除泛型的功能。
        }
        
        /**
         * 设置列元素集合
         * @param elems 列元素集合
         * @return
         */
        @SuppressWarnings("unchecked")
        public SELF setElems(final List<T> elems){
            this.val(elems);
            return (SELF)this;
        }
        
        /**
         * 列元素集合
         */
        public Iterator<T> iterator(){
           return this.getElems().iterator();
        }
        
        /**
         * 列元素集合
         */
        public Stream<T> stream(){
           return this.getElems().stream();
        }
        
        /**
         * 生成键值列表
         * @return 键值列表
         */
        public List<IKVPair<String,T>> kvs(){
            final var ai = new AtomicInteger();
            final var names = this.fieldNames == null || this.fieldNames.size()<1 
                ? Stream.iterate(0,i->i+1).map(Object::toString).limit(this.size()).toArray(String[]::new)
                : this.fieldNames.toArray(String[]::new);
            final int n = ((Number)this.size()).intValue();
            return this.stream().map(e->{
                int i = ai.get();
                return new KVPair<String,T>(names[i%n],e);
            }).collect(Collectors.toList());
        }
        
        /**
         * 数据归集
         * @param <A> 累加器类型
         * @param <R> 规约规约类型
         * @param collector 搜集器
         * @return R 规约结构
         */
        public <A,R> R collect(final Collector<T,A,R>collector) {
            return this.stream().collect(collector);
        }
        
        /**
         * DoubleSummaryStatistics 统计结果包含 最大,最小,累加和, 平均数,计数个数
         * @param <N> 累加器类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型N的数值。
         * @return DoubleSummaryStatistics 统计结果包含 最大,最小,累加和, 平均数,计数个数
         */
        public <N extends Number>DoubleSummaryStatistics stats(final Function<T,N> mapper) {
            @SuppressWarnings("unchecked")
            final Function<T,N> final_mapper = mapper==null ?o->(N)o:mapper;
            return this.stream().map(final_mapper).collect(Collectors.summarizingDouble(e->e.doubleValue()));
        }
        
        /**
         * 元素和
         * @param <N> mapper变换的结果类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值
         * @return Doule 元素的和
         */
        public Double sum () {
           return this.stats(o->(Double)TypeU.coerce(o,Double.class)).getSum();
        }
        
        /**
         * 元素和
         * @param <N> mapper变换的结果类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值
         * @return Doule 元素的和
         */
        public <N extends Number> Double sum (final Function<T,N> mapper) {
           return this.stats(mapper).getSum();
        }
        
        /**
         * 元素平均值
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return Double 平均值
         */
        public Double mean () {
           return this.stats(o->(Double)TypeU.coerce(o,Double.class)).getAverage();
        }
        
        /**
         * 元素平均值
         * @param <N> mapper变换的结果类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return R 规约结构
         */
        public <N extends Number> Double mean (final Function<T,N> mapper) {
           return this.stats(mapper).getAverage();
        }

        /**
         * 元素最大值
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return R 规约结构
         */
        public Double max () {
           return this.stats(o->(Double)TypeU.coerce(o,Double.class)).getMax();
        }
        
        /**
         * 元素最大值
         * @param <N> mapper变换的结果类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return 最大值
         */
        public <N extends Number> Double max (final Function<T,N>mapper) {
           return this.stats(mapper).getMax();
        }
        
        /**
         * 元素最小值
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return 最小值
         */
        public <N extends Number> Double min () {
           return this.stats(o->(Double)TypeU.coerce(o,Double.class)).getMin();
        }
        
        /**
         * 元素最小值
         * @param <N> mapper变换的结果类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return R 规约结构
         */
        public <N extends Number> Double min (final Function<T,N> mapper) {
           return this.stats(mapper).getMin();
        }

        /**
         * 元素个数
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return R 规约结构
         */
        public Long count () {
           return this.stats(o->(Double)TypeU.coerce(o,Double.class)).getCount();
        }
        
        /**
         * 总体方差:eXY-eXeY
         * @return 总体方差
         */
        public Double var_p () {
           final var sums = this.stream()
                .map(o->(Double)TypeU.coerce(o,Double.class))// 转换成浮点数
                .collect(Collector.of(
                    ()->new Tuple2<Double,Double>(0d,0d),
                    (tup,d)->tup
                        ._1(tup._1()+d*d) // X^2
                        ._2(tup._2()+d), // X
                    (a,b)->a));// 第一项X^2的和 ,第二项 X的和
           final var n = ((Number)this.size()).doubleValue();
           return sums._1()/n - Math.pow(sums._2()/n,2);// // E^2X -(EX)^2 实现总体方差的计算
        }

        /**
         * 样本方差  /(n-1)
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return R 规约结构
         */
        public Double var_s () {
            final var mean = this.mean();
            final var ss = this.sum(e->{
                var m = TypeU.coerce(e,Double.class).doubleValue()-mean;
                return m*m;
            });
            final var n = ((Number)(this.size()-1)).doubleValue();
            return ss/(n>0?n:1);
        }
        
        /**
         * 标准差:population 总体 1/n
         * @param <A> 累加器类型
         * @param <R> 规约规约类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return R 规约结构
         */
        public Double std_p () {
          return Math.sqrt(this.var_p());
        }
        
        /**
         * 标准差:样本population 总体 1/n-1
         * @param <A> 累加器类型
         * @param <R> 规约规约类型
         * @param mapper 数字变换函数 把一个T类型的元素转变成一个数字类型的数值。
         * @return R 规约结构
         */
        public Double std_s () {
          return Math.sqrt(this.var_s());
        }
        
        /**
         * 获取字段名称列表
         * @return 字段名列表
         */
        public List<String> getFieldNames() {
            return fieldNames;
        }
        
        /**
         * 求一列与另外一列数据之间的协方差
         * @param col2
         * @return 求一列与另外一列数据之间的协方差
         */
        public Double cov(final AbstractK2VV<T, SELF> col2) {
            final var xx = this.mapElems(e->TypeU.coerce(e, Double.class));
            final var yy = this.mapElems(e->TypeU.coerce(e, Double.class));
            final var exy = Tuple2.zip(xx,yy).stream()
                .collect(Collectors.summarizingDouble(e->e._1()*e._2())).getAverage();
            final var ex = this.mean();
            final var ey = this.mean();
            
            return exy-ex*ey;
        }
        
        /**
         * 求一列与另外一列数据之间的协方差
         * @param col2
         * @return 求一列与另外一列数据之间的协方差
         */
        public Double cor(final SELF col2) {
            return this.cov(col2)/(this.std_p()*col2.std_p());
        }
        
        /**
         * 提取指定field的数据
         * @param field
         * @return field 所对应值
         */
        public T getByField(final String field) {
            if(field==null||this.fieldNames==null||fieldNames.size()<0) return null;
            final var ai = new AtomicInteger(0);// 计数器
            return this.fieldNames.stream()
                .map(e->e.equals(field) ?ai.getAndIncrement() :-1)
                .filter(e->e>=0).findFirst().map(this::getElem).get();
        }
        
        /**
         * id 号
         * 返回id号：非法id号为-1,具体对应为行号与列号
         * @return 行号 从0开始
         */
        public Integer id() {
            if(id!=null)return id;
            var rowid =  -1;
            try {
                rowid = Integer.parseInt(this.getName());
            }catch(Exception e) {
                // do nothing
            }
            return rowid;
        }
        
        /**
         * 信息格式化
         */
        public String toString() {
            return Stream.of(this.fieldNames,this.getElems())
            .map(line->line.stream().map(e->e+"").collect(Collectors.joining("\t")))
            .collect(Collectors.joining("\n"));
        }

        private Integer id;// id 号
        private final List<String> fieldNames;
        
        
    }// AbstractK2VV
    
    /**
     * 一个  {name:elems} 的键值对儿:列对象
     * @author gbench
     *
     * @param <T> 列元素的类型
     */
    public static class DColumn<T> extends AbstractK2VV<T,DColumn<T>>{
        /**
         * 构造函数
         * name 列名 默认随机生成
         * @param elems 列值集合
         */
        @SuppressWarnings("unchecked")
        public DColumn(final List<T> elems) {
            super(UUID.randomUUID().toString(), (List<T>)Arrays.asList(elems.toArray()));
        }
        
        /**
         * 构造函数 只有一个数据元素的列
         * name 列名 默认随机生成
         * @param elems 列值集合
         */
        public DColumn(@SuppressWarnings("unchecked") T ...tt) {
            super(UUID.randomUUID().toString(), (List<T>)Arrays.asList(tt));
        }
        
        /**
         * 构造函数
         * @param name 列名
         * @param elems 列值集合
         */
        @SuppressWarnings("unchecked")
        public DColumn(final String name, final List<T> elems) {
            super(name, (List<T>)Arrays.asList(elems.toArray()));
        }
        
        /**
         * 通过mapper 变换成另一种列类型
         * @param <U> 新列的元素类型
         * @param mapper
         * @return U为元素的列
         */
        @SuppressWarnings("unchecked")
        public <U> DColumn<U> map(final Function<T,U>mapper){
            return (DColumn<U>)this._map(mapper);
        }
        
        /**
         * 列对象
         * @param name 列明
         * @param elems 元素结婚
         * @return Column<U>
         */
        public static <U> DColumn<U> COLUMN(final String name, final List<U>elems){
           return new DColumn<>(name,elems);
        }
        
        /**
         * 列对象
         * @param <U> 列元素的类型
         * @param p KVPair的结构数据
         * @return Column<T>
         */
        public static <U> DColumn<U> COLUMN(final KVPair<String,List<U>> p){
           return new DColumn<>(p.key(),p.val());
        }

        /**
         * 列对象
         * @param <U> 列元素类型
         * @param name 列明
         * @param elems 元素结婚
         * @return Column<U>
         */
        public static <U> DColumn<U> COL(final String name, final List<U>  elems){
           return COLUMN(name,elems);
        }
        
        /**
         * 列对象
         * @param <T> 列元素的类型
         * @param p KVPair的结构数据
         * @return Column<T>
         */
        public static <U> DColumn<U> COL(final KVPair<String,List<U>> p){
           return COLUMN(p);
        }
        
        /**
         * 构造函数 只有一个数据元素的列
         * name 列名 默认随机生成
         * @param elems 列值集合
         */
        @SuppressWarnings("unchecked")
        public static <T> DColumn<T> COL(T ...tt) {
            return new DColumn<>(UUID.randomUUID().toString(), (List<T>)Arrays.asList(tt));
        }
        
    }// Column
    
    /**
     * 一个  {name:elems} 的键值对儿:行对象
     * @author gbench
     *
     * @param <T> 列元素的类型
     */
    public static class  DRow<T> extends AbstractK2VV<T,DRow<T>>{
        
        /**
         * 构造函数
         * @param name 行名
         * @param elems 行值集合
         */
        @SuppressWarnings("unchecked")
        public DRow(final String name, final List<T> elems) {
            super(name.toString(), (List<T>)Arrays.asList(elems.toArray()));
        }
        
        /**
         * 构造函数
         * @param name 行名,随机名称
         * @param elems 行值集合
         */
        @SuppressWarnings("unchecked")
        public DRow(final List<T> elems) {
            super(UUID.randomUUID().toString(), (List<T>)Arrays.asList(elems.toArray()));
        }
        
        /**
         * 构造函数 构造匿名行
         * name 列名 默认随机生成
         * @param elems 列值集合
         */
        public DRow(@SuppressWarnings("unchecked") T ...tt) {
            super(UUID.randomUUID().toString(), (List<T>)Arrays.asList(tt));
        }
        
        /**
         * 构造函数
         * @param name 行名
         * @param elems 行值集合
         */
        @SuppressWarnings("unchecked")
        public DRow(final String name, final List<T> elems, final List<String> fields) {
            super(name.toString(), (List<T>)Arrays.asList(elems.toArray()),fields);
        }
        
        /**
         * 构造函数
         * @param name 行名
         * @param elems 行值集合
         */
        @SuppressWarnings("unchecked")
        public DRow(final Number name, final List<T> elems, final List<String> fields) {
            super(name.toString(), (List<T>)Arrays.asList(elems.toArray()),fields);
        }
        
        /**
         * 构造函数
         * @param name 行名
         * @param elems 行值集合
         */
        @SuppressWarnings("unchecked")
        public DRow(final Number name, final List<T> elems) {
            super(name.toString(), (List<T>)Arrays.asList(elems.toArray()));
        }
        
        @SuppressWarnings("unchecked")
        public <U> DRow<U> map(final Function<T,U>mapper){
            return (DRow<U>)this._map(mapper);
        }
        
        /**
         * 列对象
         * @param name 行名
         * @param elems 元素结婚
         * @return DRow<U>
         */
        public static <U> DRow<U> DROW(final String name, final List<U>elems){
           return new DRow<>(name,elems);
        }
        
        
        /**
         * 列对象
         * @param name 行名
         * @param elems 元素结婚
         * @return DRow<U>
         */
        public static <U> DRow<U> DROW(final Number name, final List<U>elems){
           return new DRow<>(name,elems);
        }
        
        /**
         * 列对象
         * @param name 行名
         * @param elems 元素结婚
         * @return DRow<U>
         */
        public static <U> DRow<U> DROW(final String name, final List<U>elems, final List<String> fields){
           return new DRow<>(name,elems,fields);
        }
        
        /**
         * 列对象
         * @param name 行名
         * @param elems 元素结婚
         * @return DRow<T>
         */
        public static <U> DRow<U> DROW(final Number name, final List<U>elems, final LinkedList<String> fields){
           return new DRow<>(name,elems,fields);
        }
        
        /**
         * 列对象
         * @param <T> 行元素的类型
         * @param p KVPair的结构数据
         * @return DRow<T>
         */
        public static <U> DRow<U> DROW(final KVPair<String,List<U>> p){
           return new DRow<>(p.key(),p.val());
        }

        /**
         * 列对象
         * @param name 行名
         * @param elems 元素结婚
         * @return DRow<T>
         */
        public static <U> DRow<U> ROW(final String name, final List<U>elems){
           return DROW(name,elems);
        }
        
        /**
         * 列对象
         * @param name 行名
         * @param elems 元素结婚
         * @return DRow<U>
         */
        public static <T> DRow<T> ROW(final String name, final List<T>elems,List<String> fields){
           return DROW(name,elems,fields);
        }
        
        /**
         * 列对象
         * @param name 行名
         * @param elems 元素结婚
         * @return DRow<T>
         */
        public static <U> DRow<U> ROW(final Number name, final List<U>elems){
           return DROW(name,elems);
        }
        
        /**
         * 列对象
         * @param <U> 列元素的类型
         * @param p KVPair的结构数据
         * @return DRow<T>
         */
        public static <U> DRow<U> ROW(final KVPair<String,List<U>> p){
           return ROW(p);
        }
        
        /**
         * 列对象
         * @param <T> 列元素的类型
         * @param p KVPair的结构数据
         * @return DRow<T>
         */
        @SuppressWarnings("unchecked")
        public static <U> DRow<U> ROW(final String name, final Map<String,Object>mm){
           return new DRow<>(name,mm.values().stream().map(e->(U)e).collect(Collectors.toList()));
        }
        
        /**
         * 构造函数
         * @param name 行名,随机名称
         * @param elems 行值集合
         */
        @SuppressWarnings("unchecked")
        public static <U> DRow<U> ROW(final U ...uu) {
            return new DRow<>(UUID.randomUUID().toString(), (List<U>)Arrays.asList(uu));
        }
        
    }// Column
    
    /**
     * 类型转换的工具类
     * @author gbench
     *
     */
    public static class TypeU {

        /**
         * 按键值对儿
         * @author admin
         *
         * @param <K> 键类型
         * @param <V> 值类型
         */
        public class KVPair <K,V> implements IKVPair<K,V>{
            
            /**
             * 键值对儿
             * @param k
             * @param v
             */
            public KVPair(K k,V v) {
                this.k = k;
                this.v = v;
            }
            
            public K key(){
                return k;
            }
            
            public V val(){
                return v;
            }
            
            public void val(V v) {
                this.v = v;
            }
            
            public void key(K k) {
                this.k = k;
            }
            
            @Override
            public K getKey() {
                return k;
            }
        
            @Override
            public V getValue() {
                return v;
            }
        
            @Override
            public void setKey(K key) {
                this.k = key;
            }
        
            @Override
            public void setValue(V value) {
                this.v = value;
            }
            
            /**
             * 
             * @param kvp
             * @return
             */
            @Override
            public boolean equals(Object obj) {
                //System.out.println(kvp);
                if(obj==null)return false;
                if(!(obj instanceof KVPair))return false;
                KVPair<?,?> kvp = (KVPair<?,?>)obj;
                if(this.k==null && kvp.k==null) {
                    if(this.v==null && kvp.v==null) return true;
                }
                if(this.k == null)return false;
                if(this.v == null)return false;
                if(this.k.equals(kvp.k) && 
                    this.v.equals(kvp.v))return true;
                return false;
            }
            
            /**
             * 
             */
            public int hashCode() {
                return (this.k+""+this.v).hashCode();
            }
            
            /**
             * 
             */
            public String toString() {
                return "[ "+k+"-->"+v+" ]";
            }
            
            private K k;
            private V v;
            
        }
    
        
        /**
         * 数据是否可以转化才能横数字类型
         * @param obj
         * @return
         */
        public static Number parseNumber(Object obj) {
            if(obj==null)return null;
            if(obj instanceof Number) return (Number)obj;
            String s = obj+"";
            Matcher matcher = numPattern.matcher(s);
            Double d = null;
            if(matcher.matches()) {
                String num = matcher.group(1);
                try {
                    d = Double.parseDouble(num);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return d;
        }
        
        /**
         * 
         * @param o
         * @return
         */
        public static boolean isNumberic(Object obj) {
            if(obj==null)return false;
            if(obj instanceof Number) return true;
            String s = obj+"";
            Matcher matcher = numPattern.matcher(s);
            
            return matcher.matches()?true:false;
        }
        
        //数字的识别模式
        private static Pattern numPattern = Pattern.compile(
            "\\s*((\\d(\\d+)?(\\.\\d+)?)|(\\.\\d+)|(\\d+\\.))\\s*");
        
        /**
         * 
         * @param date
         * @return
         */
        public static Long date2long(Date date){
            if(date==null)return null;
            return date.getTime();
        }
        
        /**
         * 
         * @param value
         * @param type
         * @return
         */
        @SuppressWarnings("unchecked")
        public static <T> T coerce(Object value,T type) {
            if(type==null)return null;
            Class<T> cls = (Class<T>)type.getClass();
            T t = coerce(value,cls);
            return t;
        }
        
        /**
         * 对象类型强制转换换
         * 
         * @param type 类型名称
         * @param value 类型值
         * 
         * @return 强制转换
         */
        @SuppressWarnings("unchecked")
        public static <T> T coerce(Object value,Class<T> type){
            
            if(value==null)return null;
            if(type==null)return (T)null;
            Object obj = null;
            if(type == Long.class || type == long.class){
                if(value instanceof Long)return (T)value;
                if(value instanceof Date){
                    value = date2long((Date)value);
                    return (T)value;
                }
                String str = String.valueOf(value);
                str = str.replaceAll("\\..*$","");
                obj = Long.parseLong(str);
            }else if(type == Double.class || type == double.class){
                if(value instanceof Double)return (T)value;
                if(value instanceof Date){
                    value = date2long((Date)value).doubleValue();
                    return (T)value;
                }
                try{obj = Double.parseDouble(String.valueOf(value));}catch(Exception x){}
            }else if(type == Float.class || type == float.class){
                if(value instanceof Date){
                    value = date2long((Date)value).floatValue();
                    return (T)value;
                }
                if(value instanceof Float)return (T)value;
                obj = Float.parseFloat(String.valueOf(value));
            }else if(type == Short.class || type == short.class){
                if(value instanceof Short)return (T)value;
                if(value instanceof Date){
                    value = date2long((Date)value).shortValue();
                    return (T)value;
                }
                String str = String.valueOf(value);
                str = str.replaceAll("\\..*$","");
                obj = Short.parseShort(String.valueOf(str));
            }else if(type == Integer.class || type == int.class){
                if(value instanceof Integer)return (T)value;
                if(value instanceof Date){
                    value = date2long((Date)value).intValue();
                    return (T)value;
                }
                String str = String.valueOf(value);
                str = str.replaceAll("\\..*$","");
                obj = Integer.parseInt(str);
            }else if(type == String.class){
                if(value instanceof String)return (T)value;
                obj = String.valueOf(value);
            }else if(type == char.class || type == Character.class){
                if(value instanceof Character)return (T)value;
                if(String.valueOf(value).length()>0){
                    obj = Character.valueOf(String.valueOf(value).charAt(0));
                }else{
                    obj =null;
                }
            }else if(type == Date.class){
                if(value instanceof Date)return (T)value;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    obj = sdf.parse(String.valueOf(value));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(type == Boolean.class || boolean.class == type ){
                if(value instanceof Boolean)return (T)value;
                obj = Boolean.parseBoolean(String.valueOf(value));
            }else{
                obj = value;
            }
            
            return (T)obj;
        }
        
    }// class TypeU

    /**
     * 数据单元对象：在芸芸众生当中的一份子，的那个份子。 一个数据单元其实就是一个以address为Key的键值对儿，Value是  cells[address] 所对应的数据
     * 
     * 矩阵数据的访问对象:数据单元
     * DataCell 的特点就是他可以公国offset操作来完成对矩阵元素的遍历。
     * 
     * @author xuqinghua
     *  
     * @param <T> 数据元素的类型。
     */
    public static class DataCell<T> extends Tuple2<String,T>{
            
        /**
         * 构建一个单元格数据
         * @param address 数据位置
         * @param cells 矩阵
         */
        public DataCell(final String address,final T[][] cells) {
            super(address,get(address,cells));
            this.address = address;
            this.cells = cells;
        }
        
        /**
         * 构建一个单元格数据
         * @param i 行号 从1开始
         * @param j 列号 从1开始
         * @param cells 矩阵
         */
        public DataCell(final Number i, final Number j, final T[][] cells) {
             this(MessageFormat.format("{i},{j}", i.intValue(),j.intValue()),cells);
        }
        
        /**
         * 获取元素内容
         * @return 获取元素内容
         */
        public T getValue() {
            Integer[] addr = this.offsetAddress();
            return cells[addr[0]][addr[1]];
        }
        
        /**
         * getValue 的别名
         * @return 获取元素内容
         */
        public T val() {
            return getValue();
        }
        
        /**
         * getValue 的别名
         * @return
         */
        public T get() {
            return getValue();
        }
        
        /**
         * apply fx 到数据单元
         * @param <U> 目标函数类型
         * @param fx 对元素急性 apply的函数
         * @return U类型的结果
         */
        public <U> U get(final Function<T,U> fx) {
            return fx.apply(this.getValue());
        }
        
        /**
         * 修改数据内容
         * @param value
         */
        public void setValue(final T value) {
            final var offset = this.offsetAddress();
            if(offset==null)return;
            cells[offset[0]][offset[1]]=value;
            this._2 = value;// 更新数据单元的值
        }
        
        /**
         * excel 格式的数据地质：1a->cell[0][0]
         * @return 返回数据单元的地址
         */
        public String getAddress() {
            return address;
        }
        
        /**
         * 获取行号从1开始
         * @return 行号
         */
        public Integer getRow() {
            Integer[] address = addr2offset(this.address);
            if(address==null)return null;
            return address[0]+1;
        }
        
        /**
         * 获取列号从1开始
         * @return 列号
         */
        public Integer getColumn() {
            Integer[] address = addr2offset(this.address);
            if(address==null)return null;
            return address[1]+1;
        }
        
        /**
         * 偏移地址
         * 提取地址偏移位置(相对于矩阵的1第一个元素1行1列(0,0))
         * @return
         */
        public Integer[] offsetAddress() {
            return addr2offset(this.address);
        }
        
        /**
         *  offset 术语来源于EXCEL VBA 提供了一种从一个位置周游其他矩阵元素的方法。
         *  相对于当前元素的 (i,j) 偏移位置的元素。关系：如下图所示 :O(origin，原点参照的意思)<br> 
         *  O:cell[0,0] A:cell[0,1] <br>
         *  B:cell[1,0] C:cell[1,1] <br>
         *  O与O自身的偏移是(0,0), 即O.offset(0,0) == O
         *  O与A的偏移量是(0,1), 即 O.offset(0,1) == A; A与O的偏移量是 (0,-1) 即 A.offset(0,-1) == O
         *  O与B的偏移量是(1,0), 即 O.offset(1,0) == B;  B与O的偏移量是 (-1,0) 即 B.offset(0,-1) == O
         *  O与C的偏移量是(1,1), 即 O.offset(1,1) == C;  C与O的偏移量  是 (-1,-1)即 C.offset(-1,-1) == O
         *  其他位置依次类推。
         *  
         * @param i 行偏移
         * @param j 列偏移
         * @return 相对于当前元素的 (i,j) 偏移位置的元素。
         */
        public DataCell<T> offset(final int i, final int j) {
            Integer[] addr = new Integer[] {
                    offsetAddress()[0]+i, offsetAddress()[1]+j
            };
            return new DataCell<T>(formatOffsetAddress(addr[0],addr[1]),cells);
        }
        
        /**
         * 对地址名进行解析,把一个地址名称解析成偏移地址
         * 地址名称行列从1开始，偏移地址的行列从0开始。
         * 使用示例：
         *  Stream.of("0,0;1,2;1a;A1;A3;$1$B;$B$1;1b;B5;AGE100".split(";"))
         *      .map(e->new XlsFile.Tuple2<>(e,Arrays.asList(XlsFile.DataCell.parseAddress(e))))
         *      .forEach(System.out::println);
         * 返回结果
         * (0,0 -> [-1, -1])
         * (1,2 -> [0, 1])
         * (1a -> [0, 0])
         * (A1 -> [0, 0])
         * (A3 -> [2, 0])
         * ($1$B -> [0, 1])
         * ($B$1 -> [0, 1])
         * (1b -> [0, 1])
         * (B5 -> [4, 1])
         * (AGE100 -> [99, 862])
         * 
         * @param address 地址名称 地址名称行列从1开始
         * @return [行索引,列索引] 
         */
        public static Integer[] addr2offset(final String address) {
            var strAddress  = address.replace("$","");// 去除绝对地址符号获取地址名称的标准字符串结构。
            final var  excelAddressMatcher = Pattern //   先是列名后是数字
                .compile("\\s*([a-z]+)[^a-z,\\s\\w/\\\\]*([\\d]+)\\s*"
                    ,Pattern.CASE_INSENSITIVE) // excel 的地址名形式
                .matcher(strAddress);// 
            if(excelAddressMatcher.matches()) {
                strAddress =  excelAddressMatcher.replaceAll("$2$1");
            }
            
            final var p = Pattern.compile("(\\d+)[,/]?([^,/]+)");
            final Matcher mat = p.matcher(strAddress);
            if(mat.matches()) {
                final var row = mat.group(1);// 行名地址
                var col = mat.group(2).trim().toUpperCase();// 列名地址
                var j = 0;// 列索引
                var n = col.length();// 列名长度
                if(col.matches("[A-Z]+")) {
                    for(int i=0;i<n;i++) {// 位数
                        var d = col.charAt(i)-'A'+1;// 当前的位置索引
                        j=j*26+d;
                    }//for
                    col=j+"";
                }//if
                
                if(!TypeU.isNumberic(row)) return null;
                if(!TypeU.isNumberic(col))return null;
                final var i = TypeU.parseNumber(row).intValue() -1;
                j = TypeU.parseNumber(col).intValue() -1;
                
                return new Integer[] {i,j};
            }// mat.matches()
            
            return null;
        }

        /**
         * 格式化地址表达
         * @param offsetAddress (行,列) 偏移地址:从0开始
         * @return 行列索引
         */
        public static String formatOffsetAddress(final Integer[] offsetAddress) {
            final var buffer = new StringBuffer();
            final var stack = new Stack<Integer>();
            int n = offsetAddress[1];// 列变成变成索引
            if(n==0)stack.push(n);
            while(n!=0) { stack.push(n%26); n = n/26;}
            final String names = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            while(!stack.isEmpty()) {
                int i = stack.pop();
                if(stack.size()>0)i--;
                buffer.append(names.charAt(i));
            }// while
            buffer.append(offsetAddress[0]+1);// 行偏移转换成绝对行号
            return buffer.toString();
        }

        /**
         * 对于地址名称进行解析
         * @param i 航偏移位置 从0喀什
         * @param j 列偏移位置 从0开始
         * @return 解析后的地址名称
         */
        public static String formatOffsetAddress(final Integer i,final Integer j) {
            return formatOffsetAddress(new Integer[] {i,j});
        }

        @Override
        public String toString() {
            return "Cell [value=" + this.getValue() + ", address=" + address +":"+
                    this.getRow()+"/"+this.getColumn() + "]";
        }
        
        /**
         * 从 cells 中返回address位置的元素
         * @param <U> cells中的数据元素的类型
         * @param address address 地址名称：行列从1开始
         * @param cells 二维的数据矩阵
         * @return U类型的数据单元
         */
        public static <U> U get(final String address,final U[][] cells){
            var offset = addr2offset(address);
            return cells[offset[0]][offset[1]];
        }

        final private String address; // cell 的当前位置
        final private T[][]  cells;// cell 隶属于的矩阵空间
    }// DataCell

    /**
     * 数据矩阵
     * @author gbench@sina.com
     *
     * @param <T> 矩阵的元素的类型
     */
    public static class DataMatrix<T> {
            
            /**
             * 构建一个数据矩阵
             * @param m 行数
             * @param n 列数
             * @param cellClass 元素类型
             * @param hh 表头定义，null 则首行为表头
             */
            public DataMatrix(final int m,final int n,final Class<T> cellClass,final List<String> hh){
                this.initialize(m, n, cellClass,hh);
            }
            
            /**
             * 构造DataMatrix函数
             * @param cc 数据
             * @param hh 表头定义，null 则首行为表头
             */
            @SuppressWarnings("unchecked")
            public DataMatrix(final Collection<List<T>> cc, final List<String> hh){
                if(cc==null)return;// do nothing;
                final var n = cc.stream().filter(e->e!=null)
                    .collect(Collectors.summarizingInt(e->e.size())).getMax();// 获得最大行数
                if(cc.size()<=0)return;
                final var firstLine = cc.iterator().next();
                final var cellClass = cc.stream().flatMap(e->e.stream())
                    .filter(e->e!=null).map(e->(Class<T>)e.getClass())
                    .findAny().orElse((Class<T>)Object.class);
                // 数据初始化
                this.initialize(cc.size(), n,cellClass,hh);
                final var itr = cc.iterator();// 提取数据矩阵
                for(int i=0;i<this.height();i++) this.lrow(i,itr.next());
                final List<String> final_hh = hh == null // null 表示需要使用excel额名称给是给与进行列名定义
                    ? firstLine.stream().map(e->e+"").collect(Collectors.toList()) // 默认采用 excelname给予进行列名定义。
                    : hh.stream().collect(Collectors.toList());// 转换成列表格式，方便对其进行数据追加
                if(hh==null) this.cells=removeFirstLine(cells);
                
                final var hitr = final_hh.listIterator();
                for(int i=0;i<n;i++) {// 诸列检查
                    if(!hitr.hasNext()) {// 使用excelname 来进行补充列表的补填。
                        hitr.add("_"+excelname(i));// 使用默认的excel名称加入一个下划线前缀
                    }else {
                        hitr.next();// 步进到下一个位置
                    }//if !hitr.hasNext()
                }//for i
                this.header2id(final_hh);//表头设置
            }
            
            /**
             * 把一个按照行顺序进行组织的集合类Colleciton,构建乘一个数据矩阵
             * @param rows 数据集合接,每个行元素都可以通过row_mapper 变换成一个 hashmap{(字段名，字段值)}的索引序列
             * @param row_mapper 元素转换列:t->{(name,value)} 的hashmap
             */
            public DataMatrix(final Collection<?> rows,final Function<Object,LinkedHashMap<String,T>> row_mapper) {
                this.internalBuild(rows.stream().map(row_mapper).collect(Collectors.toList()),null);
            }
            
            /**
             * 把一个按照行顺序进行组织的集合类Colleciton,构建乘一个数据矩阵
             * @param rows 可以转换成 [LinkedHashMap<String,T>] 的数据集合，row_mapper 就是把Object（假定为T类型）给造成DataMatrix独享
             * @param row_mapper 行转换函数:t->{(name,value)} 把Object（假定为U类型）转成乘一个LinkedHashMap,
             * LinkedHashMap 就是元素类型为(key:String,value:T)键值对儿集合
             * @param cellClass 矩阵的元素类型
             */
            public <U> DataMatrix(final Collection<?> rows,final Function<U,LinkedHashMap<String,T>> row_mapper,final Class<T> cellClass) {
                @SuppressWarnings("unchecked")
                final var final_row_mapper = (Function<Object,LinkedHashMap<String,T>>)row_mapper;
                this.internalBuild(rows.stream().map(final_row_mapper).collect(Collectors.toList()),cellClass);
            }
            
            /**
             * 把一个按照行顺序进行组织的流数据stream,构建乘一个数据矩阵
             * 
             * @param <T> 目标的键值对儿集合(LinkedHashMap)中的Value的类型 ,符号T用作 target之义
             * @param rowStream 行数据的流
             * @param row_mapper 行转换函数:t->{(name,value)} 把Object（假定为U类型）转成乘一个LinkedHashMap,
             * LinkedHashMap 就是元素类型为(key:String,value:T)键值对儿集合
             * @return DataMatrix<U> 数据矩阵
             */
            @SuppressWarnings("unchecked")
            public <U> DataMatrix(final Stream<?> rowStream,final Function<U,LinkedHashMap<String,T>> row_mapper) {
                this.internalBuild(rowStream.map(e->row_mapper.apply((U)e)).collect(Collectors.toList()),null);
            }
            
            /**
             * 把一个按照行顺序进行组织的流数据stream,构建乘一个数据矩阵
             * 
             * @param <T> 目标的键值对儿集合(LinkedHashMap)中的Value的类型 ,符号T用作 target之义
             * @param <U> 流对象的元素类型
             * @param rowStream 行数据的流
             * @param row_mapper 行转换函数:t->{(name,value)} 把Object（假定为U类型）转成乘一个LinkedHashMap,
             * LinkedHashMap 就是元素类型为(key:String,value:T)键值对儿集合
             * @param cellClass 矩阵的元素类型类
             * @return DataMatrix<U> 数据矩阵
             */
            @SuppressWarnings("unchecked")
            public <U> DataMatrix(final Stream<?> rowStream, final Function<U,LinkedHashMap<String,T>> row_mapper,final Class<T> cellClass) {
                this(rowStream.map(e->row_mapper.apply((U)e)).collect(Collectors.toList()),cellClass);
            }
            
            /**
             * 把一个按照行顺序进行组织的流数据stream,构建乘一个数据矩阵 <br> 
             * 使用示例 <br>
             * var oo = new DataMatrix<Integer> (recs.stream().map(e->e.toMap()),Integer.class);<br>
             * 
             * @param <T> 矩阵元素的类型
             * @param rowStream 行数据的流
             * @param cellClass 矩阵的元素类型类
             * @param 目标元素类型 的强制转化函数
             */
            public DataMatrix(final Stream<?> rowStream, final Class<T> clazz) {
                this(rowStream,lhm_cast(clazz),clazz);
            }
            
            /**
             * 数据阵列
             * @param rowStream 一个以LinkedHashMap<String,T>为元素的列表
             * @param clazz LinkedHashMap中的节点元素的类型T的class
             * @param 目标元素类型 的强制转化函数
             */
            public DataMatrix(final Collection<?> rowStream, final Class<T> clazz) {
                this(rowStream,lhm_cast(clazz),clazz);
            }
            
            /**
             * 数据矩阵
             * @param cells 数据
             * @param hh 表头定义，null 则首行为表头
             */
            public DataMatrix(final T[][] cells, final List<String> hh){
                this.initialize(cells, hh);
            }
            
            /**
             * 数据矩阵
             * @param cells 数据
             * @param hh 表头定义，null 则首行为表头
             */
            public DataMatrix(final T[][] cells, final String[] hh){
                this.initialize(cells, hh==null?null:Arrays.asList(hh));
            }
            
            /**
             * 数据矩阵,使用exelname 规则进行表头命名
             * @param cells 数据
             */
            public DataMatrix(final T[][] cells){
                if(cells==null)return;
                for(T[] c:cells)if(c==null)return;
                this.initialize(cells,
                    Stream.iterate(0,i->i+1)
                        .limit(cells[0].length)
                        .map(DataMatrix::excelname)
                        .collect(Collectors.toList()));
            }
            
            /**
             * 构造函数
             * @param columns 列表
             * @param tclass 矩阵的元素类型，当tcls为null就可以 自动检测数据元素类型
             */
            public DataMatrix(final List<DColumn<T>> columns,final Class<T> tclass){
               this.internalBuild2(columns, tclass);
            }
            
            /**
             * 按照行进行构建 DataMatrix,默认给予进行循环补充
             * @param rows 行数据集合
             * @param cellClass 矩阵元素类型,当tcls为null就可以 自动检测数据元素类型
             * @return DataMatrix<T> 对象自身
             */
            public DataMatrix<T> internalBuild(final Collection<LinkedHashMap<String,T>> rows,final Class<T> cellClass){
                return internalBuild(rows,cellClass,true);
            }
            
            /**
             * 按照行进行构建 DataMatrix
             * @param rows 行数据集合
             * @param cellClass 矩阵元素类型,当tcls为null就可以 自动检测数据元素类型
             * @param recycle 当rows中的元素长短不一的时候,是否采用索引循环遍历的方式给与补填。true 补填,false 不补填(默认为null)
             * @return DataMatrix<T> 对象自身
             */
            @SuppressWarnings("unchecked")
            public DataMatrix<T> internalBuild(final Collection<LinkedHashMap<String,T>> rows,final Class<T> cellClass,final Boolean recycle){
                if(rows==null || rows.size()<=0)return this;
                final List<String> hh = new LinkedList<>();
                final int height = rows.size();// 提取行数(高)
                final int width = rows.stream().collect(Collectors.summarizingInt(e->e.size())).getMax();// 提取列数(宽)
                final var itr = rows.iterator();
                T[][] cells = cellClass==null ? null :(T[][])Array.newInstance(cellClass, height,width);// 结果的容器矩阵,tcls为null递延创建
                
                for(int i=0;i<height;i++) {// i 行元素展开
                    final LinkedHashMap<String,T> rowrec = itr.next();// 行记录
                    hh.addAll(rowrec.keySet().stream().filter(e->!hh.contains(e)).collect(Collectors.toList()));// 补填表头
                    final Iterator<Entry<String, T>> entry_itr= rowrec.entrySet().iterator();
                    for(int j=0;j<rowrec.size();j++) {  // j 列元素展开
                        final var cij = entry_itr.next().getValue();
                        if(cells==null && cij!=null) cells = (T[][]) Array.newInstance(cij.getClass(), height,width);// 数据词组
                        cells[i][j]= cij;
                    }// for j 列元素展开
                    
                    if(recycle) {// 是否进行循环补充
                        var blanks_left = width-rowrec.size();// 还剩余多少没有填充
                        if(blanks_left>0) { // 尚有空白没有填充，此时需要采用下标索引的循环遍历的方法给与将空白补填
                            var entryitr = rowrec.entrySet().iterator(); // 提取行遍历器
                            var j = rowrec.size();// 承接前次遍历结果
                            while( blanks_left-- >0 ){// blanks_left>0表示尚未填满,要补填完整
                                if(!entryitr.hasNext())entryitr = rowrec.entrySet().iterator();// 取得可用的迭代器
                                cells[i][j++]= entryitr.next().getValue(); // 提取位置的数据值
                            }//while blanks_left-->0
                        }//if blanks_left>0
                    }// recycle
                    
                }// for  i 行元素展开
                
                this.initialize(cells, hh); // 矩阵初始化
                
                return this;
            }
            
            /**
             * 按列进行构建 DataMatrix
             * @param columns 列数据集合
             * @param cellClass 列元素类型  数据容器的元素类型
             * @return DataMatrix<T> 对象自身
             */
            public DataMatrix<T> internalBuild2(final Collection<DColumn<T>> columns,final Class<T> cellClass) {
                return this.internalBuild2(columns, cellClass, true);
            }

            /**
             * 按列进行构建 DataMatrix
             * @param columns 列数据集合
             * @param cellClass 列元素类型  数据容器的元素类型
             * @param recycle 当rows中的元素长短不一的时候,是否采用索引循环遍历的方式给与补填。true 补填,false 不补填(默认为null)
             * @return DataMatrix<T> 对象自身
             */
            @SuppressWarnings("unchecked")
            public DataMatrix<T> internalBuild2(final Collection<DColumn<T>> columns,final Class<T> cellClass,final Boolean recycle){
                final var width = columns.size();
                final var height = columns.stream().map(e->e.size())
                    .collect(Collectors.summarizingInt(e->((Number)e).intValue())).getMax();
                final Iterator<DColumn<T>> j_itr = columns.iterator();// 列的迭代器
                T[][] cells = cellClass==null ? null :(T[][])Array.newInstance(cellClass,height,width);// cellClass为null 递延创建容器cells
                
                for(int j=0;j<width;j++) {// j 列展开
                    final var column = j_itr.next().getElems();
                    final Iterator<T> ij_itr = column.iterator();// 提取j列的数据向量
                    for(int i=0;i<height;i++) {// i// 行展开
                        final var cij = ij_itr.hasNext() ? ij_itr.next() : null; // 提取 (i,j)的元素
                        if(cells==null && cij!=null ) cells = (T[][])Array.newInstance(cij.getClass(),height,width);
                        cells[i][j]= cij;
                    }//for i 行展开
                    
                    if(recycle) {// 是否进行循环补充
                        var blanks_left = height-column.size(); // 还剩余多少没有填充
                        if(blanks_left>0) {// 尚有空白没有填充，此时需要采用下标索引的循环遍历的方法给与将空白补填
                            var entryitr = column.iterator();// 创建列遍历迭代器
                            var i = column.size();// 行号
                            while( blanks_left-- >0 ){// 尚未填满就需要补充
                                if(!entryitr.hasNext())entryitr = column.iterator();// 取得可用的迭代器
                                cells[i++][j]= entryitr.next(); // 提取位置的数据值
                            }//while blanks_left-->0
                        }//if blanks_left>0
                    }// recyle
                    
                }//for j 列展开
                
                final var hh = columns.stream().map(e->e.getName()).collect(Collectors.toList());// 提取列名序列
                
                this.initialize(cells,hh);// 矩阵初始化
                
                return this;
            }

            /**
             * 数据矩阵:使用cells的数据初始化一个数据矩阵
             * @param cells 数据:
             * @param hh 表头定义，null 则首行为表头,对于hh短与width的时候, 使用编号的excel名称,名称用“_”作为开头。
             * @return DataMatrix<T> 对象自身
             */
            public DataMatrix<T> initialize(final T[][] cells,final List<String> hh){
                this.cells = cells;
                final List<String> final_hh = new LinkedList<>();
                if(hh==null) {// 采用数据的第一行作为表头
                    final_hh.addAll(Arrays.asList(cells[0]).stream()
                        .map(e->e+"").collect(Collectors.toList()));
                    this.cells=removeFirstLine(cells);
                }else {
                    final_hh.addAll(hh);
                }
                
                final int n = this.width();// 矩阵的列宽,每行的数据长度
                final var itr = final_hh.listIterator();
                for(int i=0;i<n;i++) {// 诸列检查
                    if(!itr.hasNext()) {// 使用excelname 来进行补充列表的补填。
                        itr.add("_"+excelname(i));// 使用默认的excel名称加入一个下划线前缀
                    }else {
                        itr.next();// 步进到下一个位置
                    }//if !itr.hasNext()
                }//for i
                
                // 设置键名列表
                this.header2id(final_hh);// 设置表头，表头与键名列表同义
                
                return this;
            }
            
            /**
             * 初始化一个空矩阵（元素内容均为null)
             * @param m 行数
             * @param n 列数
             * @param cellClass 元素类型：null 默认为Object.class
             * @param hh 列名序列,null 使用excelname 进行默认构造
             * @return DataMatrix<T> 对象自身
             */
            @SuppressWarnings("unchecked")
            public DataMatrix<T> initialize(final int m,final int n,Class<T> cellClass,final List<String> hh) {
                final Class<T> finalCellClass= cellClass!= null?cellClass:(Class<T>) Object.class;// 元素类型默认为Object类型
                this.cells = (T[][]) Array.newInstance(finalCellClass,m,n);
                final List<String> final_hh = (hh==null) 
                    ?  Stream.iterate(0, i->i+1).map(DataMatrix::excelname).limit(n).collect(Collectors.toList())
                    : hh;
                
                this.header2id(final_hh);// 设置表头
                
                return this;
            }

            /**
             * 矩阵乘法
             * @param <U> 中间结果计算类型
             * @param mm 右乘矩阵
             * @param hh 生成矩阵的列名序列, 用逗号分隔
             * @return DataMatrix<T> 的结果矩阵
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> mmult(final DataMatrix<T> mm,final List<String> hh) {
                
                if (!(cells[0][0] instanceof Number) || !(mm.cells[0][0] instanceof Number))
                    return null;
                final T[][] tt = (T[][]) DataMatrix.mmult((U[][]) this.cells, (U[][]) mm.cells);   
                
                return new DataMatrix<T>(tt, hh == null ? mm.header() : hh);
            }
            
            /**
             * 矩阵乘法
             * @param mm 右乘矩阵
             * @param hh 生成矩阵的列名序列, 用逗号分隔
             * @return DataMatrix<T>
             */
            public DataMatrix<T> mmult(final DataMatrix<T> mm,final String hh) {
                
                return mmult(mm,hh==null?null:Arrays.asList(hh.split("[,\\\\/]+")));
            }

            /**
             * 矩阵乘法
             * @param mm 右乘矩阵
             * @param hh 生成矩阵的列名序列, 用逗号分隔
             * @return DataMatrix<T>
             */
            public DataMatrix<T> mmult(final DataMatrix<T> mm,final String[] hh) {
                
                return mmult(mm,hh==null?null:Arrays.asList(hh));
            }

            /**
             * 矩阵乘法
             * @param m1 右乘矩阵
             * @return DataMatrix<T>
             */
            public DataMatrix<T> mmult(final DataMatrix<T> m1) {
               return mmult(m1,(List<String>)null);
            }

            /**
             * 使用示例:需要结合DataMatrix的V与tmc 和 LittleTree的IRecord,从这个函数可以看出 
             * DataMatrix 与 IRecord 既是可以亲密无间的，又可以是独立存在的。 <br>
             * <br>
             *  final var alpha = V(10, n -> ((Function &lt; Object,Object &gt;) e -> n + ":" + e)); //函数向量 <br>
             *  final var beta = alpha.transpose(); // //函数向量 <br>
             *  final var ff = alpha.mmult2(beta, Function::compose, e->e, Function::compose);// 矩阵式的函数组合。 <br>
             *  final var xx = ff.mapByRow(IRecord::REC).map(e -> { <br>
             *  &nbsp;&nbsp; @SuppressWarnings("unchecked")<br>
             *  &nbsp;&nbsp; final var r = e.applyOnValues(f->((Function&lt;Object, Object&gt;)f).apply(REC())); <br>
             *  &nbsp;&nbsp; System.out.println(r);<br>
             *  &nbsp;&nbsp; return REC(r);<br>
             *  }).collect(tmc(IRecord.class));<br>
             *  System.out.println(xx); <br>
             * <br>
             *  
             * 通用的:矩阵乘法
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 结果矩阵的元素类型
             * @param mm 右矩阵
             * @param product_operator 乘法算子
             * @param identity 零元元素
             * @param op 累加元素运算
             * @param hh 生成矩阵的列名序列, 用逗号分隔
             * @return 以V为元素的矩阵：行数与与this 相同，列数与 mm的列数相同
             */
            public <U,V> DataMatrix<V> mmult2(final DataMatrix<U> mm, final BiFunction<T,U,V> product_operator, 
                final V identity, final BinaryOperator<V> op,final List<String> hh) {
                
                final V[][] vv= DataMatrix.mmult2(this.cells, mm.cells, product_operator, identity, op);
                return new DataMatrix<V>(vv,hh==null?mm.header():hh);
            }

            /**
             * 使用示例:需要结合DataMatrix的V与tmc 和 LittleTree的IRecord,从这个函数可以看出 
             * DataMatrix 与 IRecord 既是可以亲密无间的，又可以是独立存在的。 <br>
             * <br>
             *  final var alpha = V(10, n -> ((Function &lt; Object,Object &gt;) e -> n + ":" + e)); //函数向量 <br>
             *  final var beta = alpha.transpose(); // //函数向量 <br>
             *  final var ff = alpha.mmult2(beta, Function::compose, e->e, Function::compose);// 矩阵式的函数组合。 <br>
             *  final var xx = ff.mapByRow(IRecord::REC).map(e -> { <br>
             *  &nbsp;&nbsp; @SuppressWarnings("unchecked")<br>
             *  &nbsp;&nbsp; final var r = e.applyOnValues(f->((Function&lt;Object, Object&gt;)f).apply(REC())); <br>
             *  &nbsp;&nbsp; System.out.println(r);<br>
             *  &nbsp;&nbsp; return REC(r);<br>
             *  }).collect(tmc(IRecord.class));<br>
             *  System.out.println(xx); <br>
             * <br>
             * 通用的:矩阵乘法
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 结果矩阵的元素类型
             * @param mm 右矩阵
             * @param product_operator 乘法算子
             * @param identity 零元元素
             * @param op 累加元素运算
             * @return 以V为元素的矩阵：行数与与this 相同，列数与 mm的列数相同
             */
            public <U,V> DataMatrix<V> mmult2(final DataMatrix<U> mm, final BiFunction<T,U,V> product_operator, 
                final V identity,BinaryOperator<V> op) {
                
                return this.mmult2(mm, product_operator, identity, op, null);
            }
            
            /**
             * 使用示例:需要结合DataMatrix的V与tmc 和 LittleTree的IRecord,从这个函数可以看出 
             * DataMatrix 与 IRecord 既是可以亲密无间的，又可以是独立存在的。 <br>
             * <br>
             *  final var alpha = V(10, n -> ((Function &lt; Object,Object &gt;) e -> n + ":" + e)); //函数向量 <br>
             *  final var beta = alpha.transpose(); // //函数向量 <br>
             *  final var ff = alpha.mmult2(beta, Function::compose, e->e, Function::compose);// 矩阵式的函数组合。 <br>
             *  final var xx = ff.mapByRow(IRecord::REC).map(e -> { <br>
             *  &nbsp;&nbsp; @SuppressWarnings("unchecked")<br>
             *  &nbsp;&nbsp; final var r = e.applyOnValues(f->((Function&lt;Object, Object&gt;)f).apply(REC())); <br>
             *  &nbsp;&nbsp; System.out.println(r);<br>
             *  &nbsp;&nbsp; return REC(r);<br>
             *  }).collect(tmc(IRecord.class));<br>
             *  System.out.println(xx); <br>
             * <br>
             * 
             * 通用的:矩阵乘法
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 结果矩阵的元素类型
             * @param mm 右矩阵
             * @param product_operator 乘法算子
             * @param identity 零元元素
             * @param op 累加元素运算
             * @param hh 生成矩阵的列名序列, 用逗号分隔
             * @return 以V为元素的矩阵：行数与与this 相同，列数与 mm的列数相同
             */
            public <U,V> DataMatrix<V> mmult2(final U[][] mm, final BiFunction<T,U,V> product_operator,
                final V identity,BinaryOperator<V> op, final List<String> hh) {
                
                final V[][] vv= DataMatrix.mmult2(this.cells, mm, product_operator, identity, op);
                
                return new DataMatrix<V>(vv,hh==null?this.header():hh);
            }
            
            /**
             * 使用示例:需要结合DataMatrix的V与tmc 和 LittleTree的IRecord,从这个函数可以看出 
             * DataMatrix 与 IRecord 既是可以亲密无间的，又可以是独立存在的。 <br>
             * <br>
             *  final var alpha = V(10, n -> ((Function &lt; Object,Object &gt;) e -> n + ":" + e)); //函数向量 <br>
             *  final var beta = alpha.transpose(); // //函数向量 <br>
             *  final var ff = alpha.mmult2(beta, Function::compose, e->e, Function::compose);// 矩阵式的函数组合。 <br>
             *  final var xx = ff.mapByRow(IRecord::REC).map(e -> { <br>
             *  &nbsp;&nbsp; @SuppressWarnings("unchecked")<br>
             *  &nbsp;&nbsp; final var r = e.applyOnValues(f->((Function&lt;Object, Object&gt;)f).apply(REC())); <br>
             *  &nbsp;&nbsp; System.out.println(r);<br>
             *  &nbsp;&nbsp; return REC(r);<br>
             *  }).collect(tmc(IRecord.class));<br>
             *  System.out.println(xx); <br>
             * <br>
             * 
             * 通用的:矩阵乘法
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 结果矩阵的元素类型
             * @param mm 右矩阵
             * @param product_operator 乘法算子
             * @param identity 零元元素
             * @param op 累加元素运算
             * @return 以V为元素的矩阵：行数与与this 相同，列数与 mm的列数相同
             */
            public <U,V> DataMatrix<V> mmult2(final U[][] mm, final BiFunction<T,U,V> product_operator,
                final V identity,BinaryOperator<V> op) {
                
                return this.mmult2(mm, product_operator, identity, op);
            }
            
            /**
             * 矩阵乘法
             * 
             * 返回矩阵的表头默认使用ruu的表头
             * 
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 中间结果矩阵的元素类型
             * @param <O> 最终结果矩阵的元素类型
             * @param ltt 左边的T类型矩阵
             * @param ruu 右边的U类型矩阵
             * @param product_operator T类型与U类型的向V类型进行映射的二元函数 (t,u)->v, 
             * @param reducer V类型集合向O类型元素进行映射的多元函数 (...vvv)->o
             * 
             * @return O 类型的矩阵
             */
            public <U,V,O> DataMatrix<O> mmult2(final DataMatrix<U>ruu, final BiFunction<T,U,V> product_operator,
                final Function<Stream<V>,O>reducer){
                
                if(ruu==null) {
                    System.err.println("不能对空矩阵做乘法运算");
                    return null;
                }
                
                final var _ltt = this.getCells();
                final var _ruu = ruu.getCells();
                final var _mm = mmult2(_ltt,_ruu,product_operator,reducer);
                
                return new DataMatrix<>(_mm);
            }
            
            /**
             * 矩阵乘法
             * 
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 中间结果矩阵的元素类型
             * @param <O> 最终结果矩阵的元素类型
             * @param ltt 左边的T类型矩阵
             * @param ruu 右边的U类型矩阵
             * @param product_operator T类型与U类型的向V类型进行映射的二元函数 (t,u)->v, 
             * @param reducer V类型集合向O类型元素进行映射的多元函数 (...vvv)->o
             * @param hh 返回矩阵的表头 hh 结果矩阵的列头名序列,或者叫做表头,当null时候默认使用ruu的表头
             * @return O 类型的矩阵
             */
            public <U,V,O> DataMatrix<O> mmult2(final DataMatrix<U> ruu, final BiFunction<T,U,V> product_operator,
                final Function<Stream<V>,O>reducer,final List<String> hh){
                
                if(ruu==null) {
                    System.err.println("不能对空矩阵做乘法运算");
                    return null;
                }
                
                final var _ltt = this.getCells();
                final var _ruu = ruu.getCells();
                final var _mm = mmult2(_ltt,_ruu,product_operator,reducer);
                
                return new DataMatrix<>(_mm).setHeader(hh==null ?ruu.header() :hh);
            }
            
            /**
             * 矩阵乘法
             * 
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 中间结果矩阵的元素类型
             * @param <O> 最终结果矩阵的元素类型
             * @param ltt 左边的T类型矩阵
             * @param ruu 右边的U类型矩阵
             * @param product_operator T类型与U类型的向V类型进行映射的二元函数 (t,u)->v, 
             * @param reducer V类型集合向O类型元素进行映射的多元函数 (...vvv)->o
             * @param hh 返回矩阵的表头 hh 结果矩阵的列头名序列,或者叫做表头,当null时候默认使用ruu的表头
             * @return O 类型的矩阵
             */
            public <U,V,O> DataMatrix<O> mmult2(final DataMatrix<T>ltt, final DataMatrix<U> ruu,
                final BiFunction<T,U,V> product_operator, final Function<Stream<V>,O>reducer, final List<String> hh){
                
                if(ltt==null||ruu==null) {
                    System.err.println("不能对空矩阵做乘法运算");
                    return null;
                }
                
                final var _ltt = ltt.getCells();
                final var _ruu = ruu.getCells();
                final var _mm = mmult2(_ltt,_ruu,product_operator,reducer);
                
                return new DataMatrix<>(_mm).setHeader(hh==null ?ruu.header() :hh);
            }
            
            /**
             * 矩阵乘法:列表类型的参数聚合reducer
             * 
             * 返回矩阵的表头默认使用ruu的表头
             * 
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 中间结果矩阵的元素类型
             * @param <O> 最终结果矩阵的元素类型
             * @param ltt 左边的T类型矩阵
             * @param ruu 右边的U类型矩阵
             * @param product_operator T类型与U类型的向V类型进行映射的二元函数 (t,u)->v, 
             * @param reducer V类型集合向O类型元素进行映射的多元函数 (...vvv)->o
             * 
             * @return O 类型的矩阵
             */
            public <U,V,O> DataMatrix<O> lmmult2(final DataMatrix<U>ruu, final BiFunction<T,U,V> product_operator,
                final Function<List<V>,O>reducer){
                
                if(ruu==null) {
                    System.err.println("不能对空矩阵做乘法运算");
                    return null;
                }
                
                final var _ltt = this.getCells();
                final var _ruu = ruu.getCells();
                final var _mm = mmult2(_ltt,_ruu,product_operator,
                    stream->reducer.apply(stream.collect(Collectors.toList())));
                
                return new DataMatrix<>(_mm);
            }

            /**
             * 矩阵乘法:列表类型的参数聚合reducer:列表类型的参数聚合reducer
             * 
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 中间结果矩阵的元素类型
             * @param <O> 最终结果矩阵的元素类型
             * @param ltt 左边的T类型矩阵
             * @param ruu 右边的U类型矩阵
             * @param product_operator T类型与U类型的向V类型进行映射的二元函数 (t,u)->v, 
             * @param reducer V类型集合向O类型元素进行映射的多元函数 (...vvv)->o
             * @param hh 返回矩阵的表头 hh 结果矩阵的列头名序列,或者叫做表头,当null时候默认使用ruu的表头
             * @return O 类型的矩阵
             */
            public <U,V,O> DataMatrix<O> lmmult2(final DataMatrix<U> ruu, final BiFunction<T,U,V> product_operator,
                final Function<List<V>,O>reducer,final List<String> hh){
                
                if(ruu==null) {
                    System.err.println("不能对空矩阵做乘法运算");
                    return null;
                }
                
                final var _ltt = this.getCells();
                final var _ruu = ruu.getCells();
                final var _mm = mmult2(_ltt,_ruu,product_operator,
                    stream->reducer.apply(stream.collect(Collectors.toList())));
                
                return new DataMatrix<>(_mm).setHeader(hh==null ?ruu.header() :hh);
            }

            /**
             * 矩阵乘法
             * 
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 中间结果矩阵的元素类型
             * @param <O> 最终结果矩阵的元素类型
             * @param ltt 左边的T类型矩阵
             * @param ruu 右边的U类型矩阵
             * @param product_operator T类型与U类型的向V类型进行映射的二元函数 (t,u)->v, 
             * @param reducer V类型集合向O类型元素进行映射的多元函数 (...vvv)->o
             * @param hh 返回矩阵的表头 hh 结果矩阵的列头名序列,或者叫做表头,当null时候默认使用ruu的表头
             * @return O 类型的矩阵
             */
            public <U,V,O> DataMatrix<O> lmmult2(final DataMatrix<T>ltt, final DataMatrix<U> ruu,
                final BiFunction<T,U,V> product_operator, final Function<List<V>,O>reducer, final List<String> hh){
                
                if(ltt==null||ruu==null) {
                    System.err.println("不能对空矩阵做乘法运算");
                    return null;
                }
                
                final var _ltt = ltt.getCells();
                final var _ruu = ruu.getCells();
                final var _mm = mmult2(_ltt,_ruu,product_operator,
                    stream->reducer.apply(stream.collect(Collectors.toList())));
                
                return new DataMatrix<>(_mm).setHeader(hh==null ?ruu.header() :hh);
            }
            
            /**
             * 协方差矩阵
             * @return 协方差矩阵
             */
            @SuppressWarnings("unchecked")
            public DataMatrix<T> cov(){
                final var shape = this.shape();
                final var eXY = this.tp().mmult(this).div((T)shape._1());
                final var eX = this.mapColumns(col->COL((T)col.mean()));
                final var eXeY = eX.tp().mmult(eX);
                
                return eXY.minus(eXeY);
            }
            
            /**
             * 协方差矩阵
             * @return 协方差矩阵
             */
            @SuppressWarnings("unchecked")
            public DataMatrix<T> cor(){
                final var sigma = this.mapColumns(col->COL((T)col.std_p()));// 标准差向量
                return this.cov().div(sigma.tp().mmult(sigma));
            }
            
            /**
             * 矩阵乘法
             * 
             * @param <U> 数据类型：编译辅助类。
             * @param m1 右乘矩阵
             * @param hh 生成矩阵的列名
             * @return DataMatrix<T>
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> T det(){
               return (T)DataMatrix.det((U[][])this.cells);
            }
            
            /**
             * 提取 (i,j)位置的元素
             * 
             * @param i 行号 从0开始
             * @param j 列号 从0开始
             * @return 矩阵中的数据元素
             */
            public T get(final int i,final int j) {
                if(i>=this.cells.length)return null;
                if(j>=this.cells[i].length)return null;
                
                return cells[i][j];
            }
            
            /**
             * 设置  (i,j)位置的元素
             * @param i 行编号从0开始
             * @param j 列编号从0开始
             * @return 当前矩阵示例，用于链式编程
             */
            public DataMatrix<T> set(final int i,final int j, final T value) {
                if (i < this.cells.length)
                    return null;
                if (j < this.cells[i].length)
                    return null;
                this.cells[i][j] = value;
                
                return this;
            }
            
            /**
             * 矩阵的宽度 ，列数
             * @return 矩阵的宽度 ，列数
             */
            public int width() {
                if(cells==null)return 0;
                if(cells.length>0)return cells[0].length;
                return 0;
            }
            
    
            /**
             * 矩阵高度：行数
             * @return 行数
             */
            public int height() {
                if(cells==null)return 0;
                return cells.length;
            }
            
            /**
             * 用row 的内设置指定行数据
             * 采用循环填充的方式仅从行设置
             * @param i 航编号从0开始
             * @param list 
             */
            @SuppressWarnings("unchecked")
            public void lrow(final int i, final List<T> row) {
                //Optional<Class<T>> optCls1 = row.stream().map(e->e!=null).findFirst().map(e->(Class<T>)e.getClass());
                //System.out.println(optCls1.get());
                final Optional<Class<T>> optCls = Optional.ofNullable(getGenericClass(cells));
                if(!optCls.isPresent())return;
                final T[] tt= row.stream().toArray(n->(T[])Array.newInstance(optCls.get(), n));
                row(i,tt);
            }
            
            /**
             * 用row 的内设置指定行数据
             * 采用循环填充的方式仅从行设置
             * @param i
             * @param list 
             */
            public void row(final int i,final T[] row) {
                if(row ==null||row.length<1|| this.cells==null) {
                    System.out.println("数据格式非法，无法设置行数据。");
                    return;
                }
                
                if(this.cells.length>i) {
                    final int size = row.length;
                    for(int j=0;j<this.width();j++) {
                        try {
                            this.cells[i][j]=row[j%size];
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }// for
                }
            }
            
            /**
             * 采用循环填充的方式仅从行设置
             * @param i
             * @param list
             */
            public void lcol(final int j,final List<T> list) {
                if(list ==null||list.size()<1 || this.cells==null) {
                    System.out.println("数据格式非法，无法设置行数据。");
                    return;
                }//if
                
                if(this.cells.length>j) {
                    final int size = list.size();
                    for(int i=0;i<this.height();i++) {
                        this.cells[i][j]=list.get(i%size);
                    }//for
                }//if
            }
            
            /**
             * 返回矩阵的高度与宽度即行数与列数
             * @return (height:行数,width:列数)
             */
            public Tuple2<Integer,Integer> shape(){
                return DataMatrix.shape(this.cells);
            }
            
            /**
             * 获取行信息
             * list 宝石获取列表形式
             * @param i 从0开始
             * @return
             */
            public List<T> lrow(final int i) {
                T [] t = this.row(i);
                if(t ==null) return null;
                return Arrays.asList(t);
            }
            
            /**
             * 这个一般用于：当 DataMatrix为一个列向量（n*1）的时候,提取数据元素。<br>
             * 返回i行的一个元素 ,若是列向量则为该列向量的第i个元素<br> 
             * @param i 行号：从0开始
             * @return T 每一行的第一个元素。
             */
            public T get(final int i) {
                T [] t = this.row(i);
                if(t ==null) return null;
                return t.length<1?null:t[0];
            }
            
            /**
             * 获取行信息
             * list 宝石获取列表形式
             * @param i 从0开始
             * @return
             */
            public LinkedHashMap<String,T> record(final int i) {
                final T [] tt = this.row(i);
                if(tt ==null) return null;
                LinkedHashMap<String,T> rec = new LinkedHashMap<String,T>();
                final String[] hh = hh();
                for(int j=0;j<tt.length;j++) {
                    rec.put(hh[j%hh.length],tt[j]);
                }
                return rec;
            }
            
            /**
             * 行列表
             * @return 行列表
             */
            public List<List<T>>lrows(){
                return Arrays.stream(this.cells).map(Arrays::asList).collect(Collectors.toList());
            }
            
            /**
             * 按照做规约计算<br>
             * 视矩阵为行列表[rows]：对每行数据使用row_evaluator做进行变换 得到一个 中间结构为：{(rowname:String,u:U)} 的hash列表lhm,<br>
             * 然后在调用finisher对中间结构<br>
             * lhm 进行变成目标结构O。<br>
             * 
             * @param <U> 中间结构hashMap的元素类型 Usual 一般型，Unknown 变量型
             * @param <O> 目标结果的类型 Objective
             * @param filter 行选择器 :row->bool
             * @param rowname 行名生成器：row->String
             * @param row_evaluator 行变换器:row->u
             * @param finisher 结果整合器:{(string,u)}->o
             * @return 目标结果的类型 O
             */
            public <U,O> O reduceRows(final Predicate<DRow<T>> filter,final Function<Integer,String> rowname,
                final Function<DRow<T>,U> row_evaluator,final Function<LinkedHashMap<String,U>,O> finisher){
                
                final Predicate<DRow<T>> final_filter = filter==null?e->true:filter;
                final Function<Integer,String> final_rowname = rowname==null?e->e+"":rowname;
                final LinkedHashMap<String,U> mm = new LinkedHashMap<>();
                final var ai = new AtomicInteger(0);
                this.lrows().forEach(row->{
                    final int i = ai.getAndIncrement();
                    final var drow = DRow.ROW(final_rowname.apply(i),row,this.header());// 生行对象
                    if(final_filter.test(drow)) mm.put(drow.getName(),row_evaluator.apply(drow));
                });
                return finisher.apply(mm);
            }
            
            /**
             * 按照做规约计算<br>
             * 视矩阵为行列表[lhm]：对每行数据使用row_evaluato r做进行变换 得到一个 中间结构为：{(rowname:String,u:U)} 的hash列表lhm,<br>
             * 然后在调用finisher对中间结构<br>
             * lhm 进行变成目标结构O。<br>
             * 
             * @param <U> 中间结构hashMap的元素类型 Usual 一般型，Unknown 变量型
             * @param <O> 目标结果的类型 Objective
             * @param filter 行选择器 :lhm->bool
             * @param rowname 行名生成器：lhm->String
             * @param row_evaluator 行变换器:row->u
             * @param finisher 结果整合器:{(string,u)}->o
             * @return 目标结果的类型 O
             */
            public <U,O> O reduceRows2(final Predicate<LinkedHashMap<String,T>> filter,
                final Function<LinkedHashMap<String,T>,U> row_evaluator,final Function<LinkedHashMap<String,U>,O> finisher){
                final Predicate<LinkedHashMap<String,T>> final_filter = filter==null?e->true:filter;
                final LinkedHashMap<String,U> mm = new LinkedHashMap<>();
                AtomicInteger ai = new AtomicInteger(0);
                this.forEach(lhm->{
                    if(final_filter.test(lhm))mm.put(ai.get()+"", row_evaluator.apply(lhm));
                });
                return finisher.apply(mm);
            }
            
            /**
             * 按照做规约计算<br>
             * 视矩阵为行列表[rows]：对每行数据使用row_evaluator做进行变换 得到一个 中间结构为：{(rowname:String,u:U)} 的hash列表lhm,<br>
             * 然后在调用finisher对中间结构<br>
             * lhm 进行变成目标结构O。<br>
             * 
             * @param <U> 中间结构hashMap的元素类型 Usual 一般型，Unknown 变量型
             * @param <O> 目标结果的类型 Objective
             * @param row_evaluator 行变换器:row->u
             * @return DataMatrix<U> 数据阵列
             */
            public <U> DataMatrix<U> mapRows(final Function<DRow<T>,DRow<U>> row_evaluator){
                
                return this.reduceRows(row_evaluator, rows->{
                    final var uu = rows.values().stream().map(e->e.getElems()).collect(Collectors.toList());
                    return new DataMatrix<U>(uu,(List<String>)null);
                });
            }

            /**
             * 行映射 行是一个 hashMap:注意区分 mapByRows 变换成一个数据流Stream<T>
             * @param mapper 行映射: {<String,T>} --> {<String,U>} 的函数
             * @return 一个新的行数据 {<String,U>}
             */
            public <U> DataMatrix<U> mapRows2(final Function<LinkedHashMap<String,T>,LinkedHashMap<String,U>> mapper){
                final List<String> hh = new ArrayList<String>();
                final Collection<List<U>> cc = new LinkedList<>();
                @SuppressWarnings("unchecked")
                final Function<LinkedHashMap<String,T>,LinkedHashMap<String,U>> final_mapper = mapper==null
                    ?   e->(LinkedHashMap<String,U>)e // 强制类型转换
                    :   mapper;
                this.forEach(rec->{
                    final LinkedHashMap<String,U> mm = final_mapper.apply(rec);
                    cc.add(mm.entrySet().stream().map(e->e.getValue()).collect(Collectors.toList()));
                    hh.addAll(mm.keySet().stream()
                        .filter(key->!hh.contains(key))
                        .collect(Collectors.toList()));
                });
                
                return new DataMatrix<U>(cc,hh);
            }

            /**
             * 按照做规约计算<br>
             * 视矩阵为行列表[lhm]：对每行数据使用row_evaluato r做进行变换 得到一个 中间结构为：{(rowname:String,u:U)} 的hash列表lhm,<br>
             * 然后在调用finisher对中间结构 <br>
             * lhm 进行变成目标结构O。<br>
             * 行名变换器：rowname 默认为行号,从0开始<br>
             * <br>
             * @param <U> 中间结构hashMap的元素类型 Usual 一般型，Unknown 变量型
             * @param row_evaluator 行变换器:row->u
             * @param u2o 结果整合器:{(string,u)}->o
             * @return O 目标结果类型
             */
           public <U,O> O reduceRows(final Function<DRow<T>,U> row_evaluator,
                final Function<LinkedHashMap<String,U>,O> finisher){
               
               return this.reduceRows(null,null,row_evaluator, finisher);
           }
           
           /**
            * 对列进行规约
            * @param <U> 中间元素的 对象类型
            * @param col_evaluator 中间结果的计算方法
            * @return LinkedHashMap<String,U>
            */
           public <U> LinkedHashMap<String,U> reduceColumns(final Function<DColumn<T>,U> col_evaluator){
               
              return reduceColumns((Predicate<DColumn<T>>)null,col_evaluator,e->e);
           }
           
            /**
             * 使用示例：<br>
             * final var mx = DataMatrix.of(dd,(IRecord e)->(e).lhm(Double.class));<br>
             * final var rec = mx.reduceColumns(dblsum,IRecord::REC);<br>
             * 
             * @param <U> 中间元素的 对象类型
             * @param <O> 最终结果的对象类型
             * @param filter 列的过滤方法:(name,col)->boolean
             * @param col_evaluator 中间结果的计算方法
             * @param finisher 最终结果的变换函数
             * @return O类型的最终结果
             */
            public <U,O> O reduceColumns(final Predicate<DColumn<T>> filter,
                final Function<DColumn<T>,U> col_evaluator,
                final Function<LinkedHashMap<String,U>,O> finisher){
                
                final LinkedHashMap<String,U> mm = new LinkedHashMap<>();
                final var ai = new AtomicInteger(0);
                final var names = this.hh();
                final Predicate<DColumn<T>> final_filter = filter==null?s->true:filter;
                this.lcols().forEach(col->{
                    final int idx = ai.getAndIncrement(); // 编号
                    final String name = names[idx];// 键名
                    final var kv = DColumn.COLUMN(name,col);
                    if(final_filter.test(kv)) mm.put(name,col_evaluator.apply(kv));
                });
                return finisher.apply(mm);
            }
            
            /**
             * 使用示例：
             * final var mx = DataMatrix.of(dd,(IRecord e)->(e).lhm(Double.class));
             * final var rec = mx.reduceColumns(dblsum,IRecord::REC);
             * 
             * @param <U> 中间元素的 对象类型
             * @param <O> 最终结果的对象类型
             * @param filter 列的过滤方法:(name,col)->boolean
             * @param col_evaluator 列值计算方法：
             * @param finisher 最终结果的变换函数
             * @return O类型的最终结果
             */
            public <U,O> O reduceColumns (final Function<DColumn<T>,U> col_evaluator,
                final Function<LinkedHashMap<String,U>,O> finisher){
                
                return this.reduceColumns ((String)null,col_evaluator, finisher);
            }
            
            /**
             * 按照列计算
             * 使用示例：
             * final var mx = DataMatrix.of(dd,(IRecord e)->(e).lhm(Double.class));
             * final var rec = mx.reduceColumns(dblsum,IRecord::REC);
             * 
             * @param <U> 中间元素的 对象类型
             * @param <O> 最终结果的对象类型
             * @param colnames 用逗号进行分割的列名列表。
             * @param col_evaluator 中间结果的计算方法
             * @param finisher 最终结果的变换函数
             * @return O类型的最终结果
             */
            public <U,O> O reduceColumns(final String colnames,final Function<DColumn<T>,U> col_evaluator,
                final Function<LinkedHashMap<String,U>,O> finisher) {
                
                final String names[] =  (colnames!=null)
                ?   colnames.split("[\\s,\\\\]+")
                :   this.hh();
                return this.reduceColumns(names,col_evaluator, finisher);
            }
            
            /**
             * 按照列计算
             * 使用示例：
             * final var mx = DataMatrix.of(dd,(IRecord e)->(e).lhm(Double.class));
             * final var rec = mx.reduceColumns(dblsum,IRecord::REC);
             * 
             * @param <U> 中间元素的 对象类型
             * @param <O> 最终结果的对象类型
             * @param colnames 用逗号进行分割的列名列表。
             * @param col_evaluator 中间结果的计算方法
             * @param finisher 最终结果的变换函数
             * @return O类型的最终结果
             */
            public <U,O> O reduceColumns(final String[] colnames,final Function<DColumn<T>,U> col_evaluator,
                final Function<LinkedHashMap<String,U>,O> finisher) {
                final Predicate<DColumn<T>> filter = colnames==null
                    ? p->true
                    : p->Arrays.asList(colnames).contains(p.key());
                return this.reduceColumns(filter, col_evaluator, finisher);
            }
            
            /**
            * 列的多指标变换：即一列变为多列的变换,比如 对于一个矩阵第一列是X,可以变换成 (X,X^2,DX,EX,...) 等一些列的统计指标列。也可以立理解为行展开运算。
            * @param <U>
            * @param filter
            * @param col_evaluator
            * @return DataMatrix<U>
            */
            public <U> DataMatrix<U> flatMapColumns(final Predicate<DColumn<T>> filter,
                final Function<DColumn<T>,List<DColumn<U>>> col_evaluator){
                
                final var ai = new AtomicInteger(0);
                final var names = this.hh();
                final Predicate<DColumn<T>> final_filter = filter==null?s->true:filter;
                List<DColumn<U>> columns= new LinkedList<>();
                this.lcols().forEach(col->{
                    final int idx = ai.getAndIncrement(); // 编号
                    final String name = names[idx];// 键名
                    final var column = new DColumn<>(name,col);
                    if(final_filter.test(column)) columns.addAll(col_evaluator.apply(column));
                });
                
                return new DataMatrix<U>(columns,(Class<U>)null);
            }
            
            /**
             * 列的多指标变换：即一列变为多列的变换,比如 对于一个矩阵第一列是X,可以变换成 (X,X^2,DX,EX,...) 等一些列的统计指标列。也可以立理解为行展开运算。
             * @param <U>
             * @param filter
             * @param col_evaluator
             * @return DataMatrix<U>
             */
             public <U> DataMatrix<U> flatMapColumns(final String colnames[],
                 final Function<DColumn<T>,List<DColumn<U>>> col_evaluator){
                 
                 final Predicate<DColumn<T>> final_filter = colnames==null
                        ?   s->true
                        :   s->Arrays.asList(colnames).contains(s.getName());
                 return this.flatMapColumns(final_filter, col_evaluator);
             }
             
             /**
             * 列的多指标变换：即一列变为多列的变换,比如 对于一个矩阵第一列是X,可以变换成 (X,X^2,DX,EX,...) 等一些列的统计指标列。也可以立理解为行展开运算。
             * @param <U> 变换的值类型
             * @param filter 过滤器
             * @param col_evaluator
             * @return DataMatrix<U>
             */
             public <U> DataMatrix<U> flatMapColumns(final String colnames,
                 final Function<DColumn<T>,List<DColumn<U>>> col_evaluator){
                 
                 final Predicate<DColumn<T>> final_filter = colnames==null
                        ?   s->true
                        :   s->Arrays.asList(colnames.split("[,\\\\/]+")).contains(s.getName());
                 return this.flatMapColumns(final_filter, col_evaluator);
             }
             
             /**
              * 列的多指标变换：即一列变为多列的变换,比如 对于一个矩阵第一列是X,可以变换成 (X,X^2,DX,EX,...) 等一些列的统计指标列。也可以立理解为行展开运算。
              * @param <U> 变换的值类型
              * @param filter 过滤器
              * @param col_evaluator
              * @return DataMatrix<U>
              */
             public <U> DataMatrix<U> mapColumns(final String colnames,
                 final Function<DColumn<T>,DColumn<U>> col_evaluator){
                 
                 final Predicate<DColumn<T>> final_filter = colnames==null
                        ?   s->true
                        :   s->Arrays.asList(colnames.split("[,\\\\/]+")).contains(s.getName());
                  return this.flatMapColumns(final_filter, col->Arrays.asList(col_evaluator.apply(col)));
             }
             
             /**
              * 列的多指标变换：即一列变为多列的变换,比如 对于一个矩阵第一列是X,可以变换成 (X,X^2,DX,EX,...) 等一些列的统计指标列。也可以立理解为行展开运算。
              * @param <U> 变换的值类型
              * @param filter 过滤器
              * @param col_evaluator
              * @return DataMatrix<U>
              */
             public <U> DataMatrix<U> mapColumns(final Function<DColumn<T>,DColumn<U>> col_evaluator){
                 
                return this.mapColumns(null,col_evaluator);
             }
            
            /**
             * 计算成一个键值对儿 集合
             * @param <U> 值类型
             * @param colnames 键名列表用逗号分隔
             * @param evaluator 列计算函数
             * @return {[String,U]}的结果序列
             */
            public <U> List<KVPair<String,U>> eval2kvs(final String colnames,
                final Function<DColumn<T>,U> evaluator) {
                
                final List<KVPair<String,U>> ll = new LinkedList<>();
                this.reduceColumns(colnames, evaluator, e->e).forEach((k,u)->{
                    ll.add(new KVPair<String,U>(k,u));
                });
                return ll;
            }
            
            /**
             * 矩阵计算: 使用示例：
             * 需要引用:DataMatrixApp的V向量函数<br>
             * <br>
             * final var alpha = V(10, n -> ((Function&lt;IRecord,IRecord&gt;) e ->REC(n,e))); //函数向量 <br> 
             * final var beta = alpha.transpose(); // //函数向量 <br> 
             * final var ff = alpha.mmult2(beta, Function::compose, e->e, Function::compose);//矩阵式的函数组合。 <br>
             * final var xx = ff.evaluate(e->e.apply(REC("o","-")));<br>
             * System.out.println(xx); 
             * <br>
             * @param <U> 计算结果的元素类型
             * @param evaluator 计算函数:t->u
             * @return U类型数据矩阵
             */
            @SuppressWarnings("unchecked")
            public <U> DataMatrix<U> evaluate(Function<T,U> evaluator){
                U[][] uu = null;// 结果矩阵
                for (int i = 0; i < this.height(); i++) {
                    for (int j = 0; j < this.width(); j++) {
                        final U u = evaluator.apply(this.cells[i][j]);
                        if (uu == null && u != null) {// 为结果分配存储空间
                            uu = (U[][]) Array.newInstance(u.getClass(), this.height(), this.width());
                        }// uu==null
                        uu[i][j]=u;// 结果数值的返回记录
                    }// for j
                }// for i
                return new DataMatrix<U>(uu,this.header());
            }

            /**
             * 返回行数据
             * @return
             */
            public T[][] rows(){
                return cells;
            }
            
            /**
             * 获取行信息
             * @param i 从0开始
             * @return
             */
            public T[] row(final int i) {
                if(this.cells.length>i)return cells[i];
                return null;
            }
            
            /**
             * 提取出列对象
             * @param name
             * @return 提取列对象
             */
            public DColumn<T> getColumn(final String name){
                return DColumn.COLUMN(name, this.lcol(name));
            }
            
            /**
             * 提取出列对象
             * @param name
             * @return 提取列对象
             */
            public List<DColumn<T>> getColumns(){
                return this.header().stream().map(name->DColumn.COLUMN(name,this.lcol(name))).collect(Collectors.toList());
            }
            
            /**
             * 提取出列对象
             * @param j 列序号，从0开始
             * @return 提取列对象
             */
            public DColumn<T> getColumn(final int j){
                if(this.header()==null || this.header().size()<j)return null;
                return DColumn.COLUMN(this.header().get(j), this.lcol(j));
            }
            
            /**
             * 
             * @return
             */
            public List<List<T>>lcols(){
                T[][] tt = transpose(this.cells);
                return Arrays.stream(tt).map(e->Arrays.asList(e)).collect(Collectors.toList());
            }
            
            /**
             * 获取列信息
             * @param j 从0开始 的列索引
             * @return 列表结构的列数据
             */
            public List<T> lcol(final int j) {
                if(this.cells!=null&& this.cells.length>0 && this.cells[0].length>j)
                    return Arrays.asList(cols()[j]);
                
                return null;
            }
            
            /**
             * 获取列信息
             * @param name 列名称
             * @return 列表结构的列名称
             */
            public List<T> lcol(final String name) {
                final var final_name = (!this.km.containsKey(name)) 
                    ? name.toUpperCase() // 如果name 不存在尝试变成大写形式在进行测试
                    : name; // 列名
                return this.lcol(this.idx(final_name));
            }
            
            /**
             * 把name 列转换成 U类型的对象
             * 
             * @param <U> 目标对象类型
             * @param j 从0开始 的列索引
             * @param mapper 列变换函数
             * @return j列转换成的U对象
             */
            public <U> U lcol(final int j,final Function<List<T>,U>mapper) {
                return mapper.apply(this.lcol(j));
            }
            
            /**
             * 把name 列转换成 U类型的对象
             * 
             * @param <U> 目标对象类型
             * @param name 列名称
             * @param mapper 列变换函数
             * @return name列转换成的U对象
             */
            public <U> U lcol(final String name,final Function<List<T>,U>mapper) {
                final var final_name = (!this.km.containsKey(name)) 
                    ? name.toUpperCase() // 如果name 不存在尝试变成大写形式在进行测试
                    : name; // 列名
                return mapper.apply(this.lcol(this.idx(final_name)));
            }
            
            /**
             * 获取行信息
             * @param j 从0开始
             * @return T类型数组
             */
            public T[] col(final int j) {
                return cols()[j];
            }
            
            /**
             * 获取行信息
             * @param name 列名称
             * @return T类型数组
             */
            public T[] col(final String name) {
                final var final_name = (!this.km.containsKey(name)) 
                    ? name.toUpperCase() // 如果name 不存在尝试变成大写形式在进行测试
                    : name; // 列名
                return this.col(this.idx(final_name));
            }
            
            /**
             * 把name 列转换成 U类型的对象
             * 
             * @param <U> 目标对象类型
             * @param name 列名称
             * @param mapper 列变换函数
             * @return name列转换成的U对象
             */
            public <U> U col(String name,Function<T[],U>mapper) {
                final var final_name = (!this.km.containsKey(name)) 
                    ? name.toUpperCase() // 如果name 不存在尝试变成大写形式在进行测试
                    : name; // 列名
                return mapper.apply(this.col(this.idx(final_name)));
            }
            
            /**
             * 把name 列转换成 U类型的对象
             * 
             * @param <U> 目标对象类型
             * @param j 从0开始 的列索引
             * @param mapper 列变换函数
             * @return j列转换成的U对象
             */
            public <U> U col(final int j, final Function<List<T>,U>mapper) {
                return mapper.apply(this.lcol(j));
            }
            
            /**
             * 返回列表数组
             * @return T类型二维数组
             */
            public T[][] cols(){
                if(this.cells==null)return null;
                T[][] mm = transpose(this.cells);
                return mm;
            }

            /**
             * 行遍历
             * @param cs:(row)->{...} 遍历函数
             */ 
            public void rfor(final Consumer<List<T>> cs) {
                for(int i=0;i<this.height();i++) {
                    cs.accept(this.lrow(i));
                }
            }
            
            /**
             * 行遍历
             * @param cs:cs:(string,t)->{...}
             */
            public void rfor2(final Consumer<List<KVPair<String,T>>> cs) {
                final List<String> hh = this.header();// 表头
                // 从第一行开始，取消掉了镖头
                for(int i=1;i<this.height();i++) {
                    List<T> ll = this.lrow(i);// 行数据
                    cs.accept( Stream.iterate(0, j->j+1)
                        .limit(ll.size()).map(j->new KVPair<String,T>(
                            hh.get(j),ll.get(j)))
                        .collect(Collectors.toList()) );
                }//for
            }
            
            /**
             * 列遍历
             * @param cs:(ll)->{...}
             */
            public void cfor(final Consumer<List<T>> cs) {
                for(int i=0;i<this.width();i++) {
                    cs.accept(this.lcol(i));
                }//
            }
            
            /**
             * 列遍历
             * @param cs:(column)->{...}
             */
            public void cfor2(final Consumer<DColumn<T>> cs) {
                for(int i=0;i<this.width();i++) {
                    cs.accept(this.getColumn(i));
                }//
            }
            
            /**
             * 返回数据的列数(水平长度较列数，垂直长度叫行数）
             * @return this.width;
             */
            public int size() {
                return this.width();
            }

            /**
             * 矩阵格式化
             * @param ident 行内间隔
             * @param ln 行间间隔
             * @param cell_formatter 元素格式化
             * @return 格式化输出的字符串
             */
            public String toString(final String ident,final String ln,
                    final Function<Object,String> cell_formatter) {
                final Function<Object,String> final_cell_formatter = cell_formatter==null
                    ?   e->{
                            var line = "{0}";//数据格式字符串
                            if(e instanceof Number)
                                line="{0,number,#}";
                            else if(e instanceof Date) {
                                line="{0,Date,yyy-MM-dd HH:mm:ss}";
                            }
                            return MessageFormat.format(line, e);
                        }// 默认的格式化
                    :   cell_formatter;
                if( cells == null || cells.length <1 || 
                    cells[0] == null || cells[0].length<1)return "";
                final StringBuffer buffer = new StringBuffer();
                final String headline = this.header().stream().collect(Collectors.joining(ident));
                if(!headline.matches("\\s*"))
                buffer.append(headline+ln);
                for(int i=0;i<cells.length;i++) {
                    final int n = (cells[i]!=null && cells[i].length>0) ? cells[i].length:0; 
                    for (int j=0;j<n;j++) {
                        buffer.append(final_cell_formatter.apply(cells[i][j])+ident);
                    }
                    buffer.append(ln);
                }
                
                return buffer.toString();
            }

            /**
             * 格式化输出
             */
            public String toString() {
                return toString("\t","\n",null);
            }
            
            /**
             * 格式化输出
             */
            public String toString(Function<Object,String>cell_formatter) {
                return toString("\t","\n",cell_formatter);
            }
            
            /**
             * 
             * @param keysMap
             */
            public Map<String,Integer> header2id(final Map<String,Integer> keysMap){
                this.km = keysMap;
                return km;
            }
            
            /**
             * 
             * @param km
             */
            public Map<String,Integer> header2id(final String ss,String delim){
                // 制定健值映射
                return this.header2id(Arrays.asList(ss.split(",")));
            }
            
            /**
             * 设置或获取
             * @param items 名称集合
             * @return Map<String,Integer>
             */
            public Map<String,Integer> header2id(final List<?> items){
                final Map<String, Integer> keysMap = new HashMap<String, Integer>();
                if (items == null) {
                    final var hh = this.header();
                    for (int i = 0; i < hh.size(); i++) {
                        keysMap.put(hh.get(i), i);
                    }//for
                    return keysMap;
                } else {
                    int i = 0;
                    for (Object key : items) {
                        keysMap.put(key + "", i++);
                    }//for
                    this.header2id(keysMap);
                    return keysMap;
                }// if
            }
            
            /**
             * 
             * @param key
             * @return
             */
            public Integer idx(final String key) {
                return km.get(key);
            }
            
            /**
             * 获得子集合
             * @param str excel 格式的cell的名称 1a:3h或者1/1:9/8
             * @return
             */
            public DataMatrix<T> range(final String str) {
                if(str == null || !str.contains(":")) {
                    return null;
                }
                
                Function<String,Integer[]> parse = cname->{
                    cname=cname.trim();
                    final Pattern p = Pattern.compile("(\\d+)[,/\\s]*([^,/\\s]+)");
                    final Matcher mat = p.matcher(cname);
                    if(mat.matches()) {
                        String row = mat.group(1);
                        String header = mat.group(2);
                        
                        if(TypeU.isNumberic(row)) {
                            if(!TypeU.isNumberic(header)) {
                                if(!km.containsKey(header)) {
                                    header = header.toUpperCase();
                                    if(!km.containsKey(header)) {// 这个可能是采用地质命名
                                        Integer[] aa = DataCell.addr2offset(cname);
                                        if(aa==null)return null;
                                        header = aa[1]+"";
                                    }else {// excel 表名解析失败
                                        header = this.km.get(header)+"";// 使用列名解释
                                    }
                                }else {// 使用列名解释
                                    header = this.km.get(header)+"";
                                }
                                
                            }//if
                            int i = TypeU.parseNumber(row).intValue();
                            if(i>0)i--;// 转换成offset地质，从1开始。
                            return new Integer[] {
                                i,
                                TypeU.parseNumber(header).intValue()
                            };
                        }//if
                    }
                    return null;
                };
                
                final String ss[] = str.split("[:]+");
                final Integer[] from = parse.apply(ss[0]);
                final Integer[] to = parse.apply(ss[1]);
                
                return this.range(from[0],from[1],to[0],to[1]);
            }
            
            /**
             * 获得子集合
             *    从上到下，从左到右
             * @param i0 从0开始 行坐标 z：左上角奥
             * @param j0 从0开始 列坐标：左上角
             * @param i1 包含
             * @param j1 包含
             * @return
             */
            public DataMatrix<T> range(final int i0,final int j0,final int i1,final int j1){
                final List<String> hh = this.header().subList(j0,j1+1);
                return new DataMatrix<>(sub_2darray(this.cells,i0,j0,i1,j1),hh);
            }
            
            /**
             * 船舰子矩阵
             * @param cells 对象二维数组
             * @param i0 从0开始 行坐标
             * @param j0 从0开始 列坐标
             * @param i1 包含
             * @param j1 包含
             * @return 子矩阵
             */
            public T[][] sub_2darray(final T[][] cells, final int i0,final int j0,final int i1,final int j1) {
                final int h = i1-i0+1;
                final int w = j1-j0+1;
                @SuppressWarnings("unchecked")
                final T[][] cc = (T[][])Array.newInstance(getGenericClass(cells),h,w);
                for(int i=i0;i<=i1;i++) {
                    for(int j=j0;j<=j1;j++) {
                        cc[i-i0][j-j0]=cells[i][j];
                    }//for
                }//for
                return cc;
            }
            
            /**
             * 船舰子矩阵
             * @param cells 对象二维数组
             * @param i0 从0开始
             * @param j0 从0开始
             * @param i1 包含
             * @param j1 包含
             * @return 子矩阵
             */
            public T[][] sub_2darray(final int i0,final int j0,final int i1,final int j1) {
                int h = i1-i0+1;
                int w = j1-j0+1;
                @SuppressWarnings("unchecked")
                T[][] cc = (T[][])Array.newInstance(getGenericClass(cells),h,w);
                for(int i=i0;i<=i1;i++) {
                    for(int j=j0;j<=j1;j++) {
                        cc[i-i0][j-j0]=cells[i][j];
                    }//for
                }//for
                return cc;
            }
            
            /**
             * 船舰子矩阵
             * @param cells 对象二维数组
             * @param i0 从0开始
             * @param j0 从0开始
             * @param i1 包含
             * @param j1 包含
             * @return 子矩阵
             */
            public T[][] sub_2darray(final T[][] cells,final String rangeName) {
                RangeDef rangedef = name2range(rangeName);
                if(rangedef==null)return null;
                int i0 = rangedef.x0(); int j0 = rangedef.y0();
                int i1 = rangedef.x1(); int j1 = rangedef.y1();
                return this.sub_2darray(cells,i0, j0, i1, j1);
            }
            
            /**
             *  创建子矩阵
             * rangeName:excel 中的区域命名
             * @return 子矩阵
             */
            public T[][] sub_2darray(final String rangeName) {
                RangeDef rangedef = name2range(rangeName);
                if(rangedef==null)return null;
                int i0 = rangedef.x0(); int j0 = rangedef.y0();
                int i1 = rangedef.x1(); int j1 = rangedef.y1();
                return this.sub_2darray(i0, j0, i1, j1);
            }
            
            /**
             * 获取子矩阵用EXCEL使用坐标
             * @param i0 从0开始
             * @param j0 从0开始
             * @param i1 包含
             * @param j1 包含
             * @return 子矩阵
             */
            public DataMatrix<T> submx(final int i0,final int j0,final int i1,final int j1) {
                final int n = j1-j0+1;
                if(n<=0)return null;
                final List<String> hh = this.header().stream().limit(n).collect(Collectors.toList());// 表头数据
                return new DataMatrix<>(this.sub_2darray(i0, j0, i1, j1),hh);
            }
            
            /**
             * 获取子矩阵用EXCEL命名吧
             * @param rangeName,比如 A1:B5  -> 0,0:4,2
             * 注意：rangeName是相对于this矩阵本身的便宜，不要于excel 的sheet 互相混淆。
             * @return 子矩阵
             */
            public DataMatrix<T> submx(final String rangeName) {
                RangeDef rangedef = name2range(rangeName);
                if(rangedef==null)return null;
                final int i0 = rangedef.x0(); final int j0 = rangedef.y0();
                final int i1 = rangedef.x1(); final int j1 = rangedef.y1();
                final int n = j1-j0+1;// 区间长度
                if(n<=0)return null;
                final List<String> hh = this.header().stream().limit(n).collect(Collectors.toList());// 表头数据
                System.out.println(rangedef);
                return new DataMatrix<>(this.sub_2darray(i0, j0, i1, j1),hh);
            }
            
            /**
             * 根据地址提取单元格中的数据：一个DataCell 就是一个对矩阵数据的数据单元的引用。
             * @param addr excel 格式的cell的名称/地址。比如A2代表2行1列。相当于索引的(1,0),或者是 用 “/” 或是 “，” 
             * 分割出来的两个数字。比如 2,3就对应EXCEL的C2命名
             * @return 指定单元格里面的数据:返回的结果的DataCell 类型的数据。
             */
            public DataCell<T> getDataCell(final String addr) {
                return new DataCell<T>(addr,this.cells);
            }
            
            /**
             * 提取 addr 所标识区域中的数据
             * @param addr excel 格式的cell的名称/地址。比如A2代表2行1列。相当于索引的(1,0),或者是 用 “/” 或是 “，” 
             * 分割出来的两个数字。比如 2,3就对应EXCEL的C2命名
             * @return addr 所标识区域中的数据：
             */
            public T get(final String addr) {
                return this.getDataCell(addr).get();
            }
            
            /**
             *  获取单元格内容
             *  @param i 行名
             *  @param key 列名
             *  @return T (i,key) 所标识的数据元素
             */
            public T get(final int i,final String key) {
                if(key==null)return null;
                final Integer j = idx(key.toUpperCase());
                if(j==null)return null;
                return cells[i][j];
            }
            
            /**
             * 获得第i行的第key 列
             * @param i 行号，从0开始
             * @param key 列名
             * @return Integer (i,key) 所标识的数据元素
             */
            public Integer integer(final int i,final String key) {
                final T t = this.get(i, key);
                final String s = t+"";
                if(t==null)return null;
                final String ints = s.replaceFirst("\\.\\s*[\\d\\s]+$", "");// 去除小数后的值
                return Integer.parseInt(ints);
            }
            
            /**
             * 获得第i行的第key 列
             * @param i 行号，从0开始
             * @param key 列名
             * @return long (i,key) 所标识的数据元素
             */
            public long lng(final int i,final String key) {
                final T t = this.get(i, key);
                final String s = t+"";
                final String ints = s.replaceFirst("\\.\\s*[\\d\\s]+$", "");// 去除小数后的值
                return Long.parseLong(ints);
            }
            
            /**
             * 提取(i,key)之间的元素
             * 
             * @param i 航索引从0喀什
             * @param key 列名
             * @return double (i,key) 所标识的数据元素
             */
            public double dbl(final int i,final String key) {
                T t = this.get(i, key);
                final String s = t+"";
                return Double.parseDouble(s);
            }
            
            
            /**
             * 表头，列名序列
             * 
             * @return 返回 数据 的表头，列名序列
             */
            public String[] hh(){
                return this.km.entrySet().stream()
                    .sorted((a,b)->a.getValue()-b.getValue())
                    .map(e->e.getKey()).toArray(String[]::new);
            }

            /**
             * 表头，列名序列
             * @return 返回 数据 的表头，列名序列
             */
            public List<String> header(){
                return this.km.entrySet().stream()
                    .sorted((a,b)->a.getValue()-b.getValue())
                    .map(e->e.getKey()).collect(Collectors.toList());
            }
            
            /**
             * 设置 表头：列名序列
             * @param hh 表头,列名序列
             * @return 当前对象的本身 以实现链式编程
             */
            public DataMatrix<T> setHeader(final String ...hh){
               return this.setHeader(Arrays.asList(hh));
            }
            
            /**
             * 设置 表头：列名序列
             * @param hh 表头名称序列，用逗号分隔
             * @return 当前对象的本身 以实现链式编程
             */
            public DataMatrix<T> setHeader(final String hh){
               return this.setHeader(Arrays.asList(hh.split("[,\\\\/\n]+")));
            }
            
            /**
             * 设置 表头列表
             * @return 当前对象的本身 以实现链式编程
             */
            public DataMatrix<T> setHeader(final List<String>hh){
                final int n = this.width();
                final var final_hh = new LinkedList<String>();
                if(hh!=null)final_hh.addAll(hh);
                final var hitr = final_hh.listIterator();
                for(int i=0;i<n;i++) {// 诸列检查
                    if(!hitr.hasNext()) {// 使用excelname 来进行补充列表的补填。
                        hitr.add("_"+excelname(i));// 使用默认的excel名称加入一个下划线前缀
                    }else {
                        hitr.next();// 步进到下一个位置
                    }//if !hitr.hasNext()
                }//for i
                this.header2id(final_hh);//表头设置
                
                return this;
            }
            
            /**
             * 列名转列id id 从0开始
             * @return Map<String, Integer>
             */
            public Map<String, Integer> header2id() {
                return km;
            }

            /**
             * id2header
             * @return Map<Integer, String>
             */
            public Map<Integer, String> id2header() {
                final var i2h = new HashMap<Integer,String>();
                km.forEach((header,id)->{
                    i2h.put(id,header);
                });
                return i2h;
            }

            /**
             * 获取Cell的类型信息
             * @return Cell的类型信息,即T的Class对象
             */
            public Class<T> getCellClass(){
                return DataMatrix.getGenericClass(cells);
            }

            /**
             * 数据矩阵的cells二维数组
             * @return 数据矩阵的cells二维数组
             */
            public T[][] getCells() {
                return cells;
            }
            
            /**
             * 需要注意与cell系列方法的区别：cell返回的MatrixCell对象，而getCell返回的是数据元素
             * 取货  (i,j)位置的元素
             * @param i 行号索引从0开始
             * @param j 列号索引从0开始
             * @return  (i,j)位置的元素 T类型的元素。
             */
            public T getCell(int i,int j) {
                return cells[i][j];
            }
            
            /**
             * 行顺序的cells一维序列
             * @return 行顺序的cells一维序列
             */
            @SuppressWarnings("unchecked")
            public T[] getFlatCells() {
               final Stream<T> tts =  Arrays.stream(this.cells).flatMap(Arrays::stream);
               return tts.toArray(n->(T[])Array.newInstance(this.getCellClass(),n));
            }
            
            /**
             * 列顺序的cells一维序列
             * @return 列顺序的cells一维序列
             */
            @SuppressWarnings("unchecked")
            public T[] getFlatCells2() {
               final Stream<T> tts =  Arrays.stream(DataMatrix.transpose(this.cells)).flatMap(Arrays::stream);
               return tts.toArray(n->(T[])Array.newInstance(this.getCellClass(),n));
            }
            
            /**
             * 提取根据列索引提取列名
             * @param j 列索引 从0开始
             * @return j 列索引所对应的列名 
             */
            public String getHeaderByColumnIndex(int j) {
                return this.header().get(j);
            }

            /**
             * 强制类型转换
             * @param cls 目标类类型
             * @return U类型的DataMatrix<U>
             */
            @SuppressWarnings("unchecked")
            public <U> DataMatrix<U> corece(final Class<U> cls){
                T[][] cc = this.cells;
                try {
                    final U[][] dd = (U[][])cc;// 强制类型转换
                    return new DataMatrix<U>(dd,this.header());
                }catch(Exception e) {
                    return null;
                }
            }
            
            /**
             * 类型装换
             * @param corecer 类型变换函数
             * @return 类型变换
             */
            @SuppressWarnings("unchecked")
            public <U> DataMatrix<U> corece(final Function<T,U>corecer){
                T[][] cc = this.cells;
                try { 
                    final int m = this.height();
                    final int n = this.width();
                    final List<U> ulist = Arrays.stream(cc).flatMap(Arrays::stream).map(corecer)
                        .collect(Collectors.toList());// 找到一个非空的数据类型
                    final Optional<Class<U>> opt = ulist.stream().filter(e->e!=null)
                        .findFirst().map(e->(Class<U>)e.getClass()); // 提取费控的类型
                    final Class<U> cls = opt.orElseGet(()->(Class<U>)(Object)Object.class);
                    final U[][] uu = (U[][])Array.newInstance(cls, m,n);
                    final Iterator<U> itr = ulist.iterator();
                    for(int i=0;i<m;i++) for(int j=0;j<n;j++) uu[i][j]=itr.hasNext()?itr.next():null;
                    
                    return new DataMatrix<U>(uu,this.header());
                }catch(Exception e) {
                    return null;
                }
            }
            
            /**
             * 增加数据预处理函数，只改变数据内容并改变数据形状shape:比如 无效非法值，缺失值，数字格式化等功能。
             * @param mapper 行数据映射 LinkedHashMap<String,T>的结构, key->value 
             */
            public DataMatrix<T> preProcess(final Consumer<T[]> handler) {
                for(int i=0;i<this.height();i++)handler.accept(cells[i]);
                return this;
            }
            
            /**
             * 按照行进行映射
             * @param <U> 目标行记录{(String,T)} 所变换成的结果类型
             * @param mapper 行数据映射 LinkedHashMap<String,T>的结构, key->value 
             * @return U 类型的流
             */
            public <U> Stream<U> rowStream(final Function<LinkedHashMap<String,T>,U> mapper) {
                final String hh[] = this.header().stream().toArray(String[]::new);
                final int hn = hh.length;// 表头长度
                @SuppressWarnings("unchecked")
                Function<LinkedHashMap<String,T>,U> final_mapper = mapper ==null
                    ?   e->(U)e
                    :   mapper;  
                return this.lrows().stream().map(row->{
                    int n = row.size();
                    LinkedHashMap<String,T> mm = new LinkedHashMap<>();
                    for(int i=0;i<n;i++) mm.put(hh[i%hn],row.get(i)); 
                    return final_mapper.apply(mm);
                });// lrows
            }
            
            /**
             * 按照行进行映射:注意区分 mapRows 变换成另外一个 DataMatrix<U>
             * @param <U> 目标行记录{(String,T)} 所变换成的结果类型
             * @param mapper 行数据映射 LinkedHashMap<String,T>的结构, key->value 
             * @return U 类型的流
             */
            public <U> Stream<U> mapByRow(final Function<LinkedHashMap<String,T>,U> mapper) {
                return rowStream(mapper);
            }

            /**
             * 字段排序
             * @param key 键值
             * @param kcomparator 行比较器
             * @return 排序字敦
             */
            public DataMatrix<T> sorted(final String key, final Comparator<T> kcomparator){
                return sorted((mm1,mm2)->kcomparator.compare(mm1.get(key), mm2.get(key)));
            }
            
            /**
             * 字段排序
             * @param <U> ucast 变换结果的类型
             * @param key 键值
             * @param ucast 变换函数
             * @param kcomparator 行比较器
             * @return 排序字敦
             */
            public <U> DataMatrix<T> sorted(final String key,final Function<T,U> ucast,final Comparator<U> kcomparator){
                return sorted((mm1,mm2)->kcomparator.compare(ucast.apply(mm1.get(key)), ucast.apply(mm2.get(key))));
            }
            
            /**
             * 字段排序
             * @param comparator 行比较器
             * @return 重新排序的字段矩阵
             */
            public DataMatrix<T> sorted(final Comparator<LinkedHashMap<String,T>> comparator){
                
                final Function<T[],LinkedHashMap<String,T>> ctor = cc->{
                    final LinkedHashMap<String,T> mm = new LinkedHashMap<>();
                    final List<String> hh = header();
                    for(int i=0;i<header().size();i++)mm.put(hh.get(i),cc[i]);
                    return mm;
                };// ctor
                
                final List<LinkedHashMap<String,T>> tt = Arrays.asList(this.rows()).stream()
                    .map(ctor).sorted(comparator).collect(Collectors.toList());
                
                return this.internalBuild(tt,null);
            }
            
            /**
             * 按照行过滤并映射
             * @param <U> ubuilder 变换结果的类型
             * @param ubuilder 行数据转换器 把一个LinkedHashMap<String,T> 转换成一个U类型
             * @param utester 测试:对 u对象进行测试
             * @return 返回使用utester进行过滤的结果集
             */
            public <U> DataMatrix<T> filter(final Function<LinkedHashMap<String,T>,U> ubuilder,
                final Predicate<U> utester){
                
                return filter(e->utester.test(ubuilder.apply(e)));
            }
            
            /**
             * 删除指定 行的数据:对于非法行索引号(小于零或者大于heigh-1,返回不做任何处理的拷贝)
             * @param i 行号，从0开始
             * @return 删除掉了i行数的数据矩阵
             */
            public DataMatrix<T> removeRow(final int i){
                final var ai = new AtomicInteger(0);
                return this.filter(e->ai.getAndIncrement() != i);
            }
            
            /**
             * 删除指定 列的数据：对于非法列索引号(小于零或者大于width-1,返回不做任何处理的拷贝)
             * @param i 列号，从0开始
             * @return 删除掉了i行数的数据矩阵
             */
            public DataMatrix<T> removeColumn(final int i){
                
                final var ai = new AtomicInteger(0);
                final var cc = this.getColumns().stream()
                    .filter(e->ai.getAndIncrement() != i)
                    .collect(Collectors.toList());
                return new DataMatrix<>(cc,(Class<T>)null);
            }
            
            /**
             *  把mm处插入到列 i的位置 
             * @param i 列号，从0开始
             * @return 删除掉了i行数的数据矩阵
             */
            public DataMatrix<T> insertColumns(final int i,final DataMatrix<T> mm){
                
                final var ai = new AtomicInteger(0);
                final var cc = this.getColumns();
                final var litr = cc.listIterator();
                while(litr.hasNext())  {
                    if(ai.getAndIncrement()==i) break; 
                    else litr.next();
                }
                mm.getColumns().forEach(litr::add);// 插入列数据
                return new DataMatrix<>(cc,(Class<T>)null);
            }
            
            /**
             *  把mm处插入到行 i的位置
             * @param i 行号，从0开始
             * @return 删除掉了i行数的数据矩阵
             */
            public DataMatrix<T> insertRows(final int i,final DataMatrix<T> mm){
                
                final var ai = new AtomicInteger(0);
                final var rr = this.lrows();
                final var litr = rr.listIterator();
                while(litr.hasNext()) {
                    if(ai.getAndIncrement()==i) break;
                    else litr.next();
                }
                final var hh = this.header();
                mm.lrows().forEach(litr::add);// 插入列数据
                return new DataMatrix<T>(rr,(List<T> e)-> {
                        final var lhm = new LinkedHashMap<String,T>();
                        final var itr = e.iterator();
                        Stream.iterate(0,j->itr.hasNext(),j->j+1).forEach(j->lhm.put(hh.get(j++),itr.next()));
                        return lhm;
                    },(Class<T>)null).setHeader(this.header());
            }
            
            /**
             *  把mm处插入到行 i的位置
             * @param i 行号，从0开始
             * @param row 行元素列表
             * @return 删除掉了i行数的数据矩阵
             */
            public DataMatrix<T> insertRow(final int i,final List<T> row){
                final var hh = this.header();
                final var mm = new DataMatrix<T>(Arrays.asList(row),
                        (List<T> e)-> {
                            final var lhm = new LinkedHashMap<String,T>();
                            final var itr = e.iterator();
                            Stream.iterate(0,j->itr.hasNext(),j->j+1).forEach(j->lhm.put(hh.get(j++),itr.next()));
                            return lhm;
                        },this.getCellClass());
                return this.insertRows(i, mm);
            }
            
            /**
             *  把mm处插入到行 i的位置
             * @param i 列号，从0开始
             * @param column 列元素列表
             * @return 删除掉了i行数的数据矩阵
             */
            public DataMatrix<T> insertColumn(final int j,final List<T> column){
                final var col = new DColumn<T>("_"+DataMatrix.excelname(this.width()+1),column);
                final var mm = new DataMatrix<T>(Arrays.asList(col),this.getCellClass());
                return this.insertColumns(j, mm);
            }
            
            /**
             * 按照行映射
             * @param tester 行数据tester
             * @return 过滤后的数据矩阵
             */
            public DataMatrix<T> filter(final Predicate<LinkedHashMap<String,T>> tester){
                final List<LinkedHashMap<String,T>> ll = new LinkedList<>();
                this.forEach(rec->{
                    if(tester.test(rec)) ll.add(rec);
                });
                final Collection<List<T>> cc = ll.stream()
                    .map(e->e.values().stream().collect(Collectors.toList()))
                    .collect(Collectors.toList());
                return new DataMatrix<T>(cc,this.header());
            }
            
            /**
             * 矩阵对象
             * @param <U> creator 的结果类型
             * @param creator 矩阵生成器
             * @return 矩阵对象
             */
            public <U> U cast(final Function<DataMatrix<T>,U> creator) {
                return creator.apply(this);
            }
            
            /**
             * 迭代遍历 元素按照表头字段的长度
             * @param handler 字段处理
             */
            public void forEach(final Consumer<LinkedHashMap<String,T>> handler) {
                this.forEach(e->e,handler);
            }
            
            /**
             * 迭代遍历 元素按照表头字段的长度
             * @param <U> mapper  的返回结构类型
             * @param mapper 行数据转换器
             * @param handler 字段处理
             */
            public <U> void forEach(final Function<LinkedHashMap<String,T>,U> mapper,final Consumer<U> handler) {
                final String hh[] = this.header().stream().toArray(String[]::new);
                final int hn = hh.length;
                this.rfor(row->{
                    final int n = row.size();
                    final LinkedHashMap<String,T> mm = new LinkedHashMap<>();
                    for(int i=0;i<n;i++) {
                        String key = hh[i%hn];
                        T value = row.get(i%hn);
                        mm.put(key,value); 
                    }
                    final U u = mapper.apply(mm);
                    if(u!=null)handler.accept(u);
                });
            }
            
            /**
             * 行追加
             * @param aa 行数据：多行
             * @return 新生成的矩阵
             */
            public DataMatrix<T> rbind(final T[][] aa) {
                final T[][] uu = rbind(cells,aa);
                return new DataMatrix<T>(uu,this.header());
            }

            /**
             * 行追加
             * @param mm 追加的行数据
             * @return 新生成的矩阵
             */
            public DataMatrix<T> rbind(final DataMatrix<T>mm) {
                final T[][] uu = rbind(cells,mm.cells);
                final List<String> header = new LinkedList<>();
                header.addAll(this.header());
                header.addAll(mm.header());
                return new DataMatrix<T>(uu,this.header());
            }

            /**
             * 列追加
             * @param aa 追加的列数据：多列。
             * @return 新生成的矩阵
             */
            public DataMatrix<T> cbind(final T[][] aa) {
                final T[][] uu = cbind(cells,aa);
                return new DataMatrix<T>(uu,this.header());
            }
            
            /**
             * 列追加：自动数据延展进行行填充，模仿r语言的数据添加
             * @param t 追加的行数据
             * @return 新生成的矩阵
             */
            @SuppressWarnings("unchecked")
            public DataMatrix<T> cbind(final T t) {
                final Class<T> tclass = t!=null?(Class<T>)t.getClass():(Class<T>)(Object)Object.class;
                final T cc[] = (T[])Array.newInstance(tclass,1);
                cc[0] = t;// 数据初始化为t 
                return this.cbind(cc);
            }

            /**
             * 行朱家
             * @param aa 追加的行数据
             * @return 新生成的矩阵
             */
            public DataMatrix<T> cbind(final T[] aa) {
                final T[][] uu = cbind(cells,vv2mm(aa));
                return new DataMatrix<T>(uu,this.header());
            }
            
            /**
             * 行追加 :自动数据延展进行行填充，模仿r语言的数据添加
             * @param t 追加的行数据
             * @return 新生成的矩阵
             */
            @SuppressWarnings("unchecked")
            public DataMatrix<T> rbind(final T t) {
                final Class<T> tclass = t!=null?(Class<T>)t.getClass():(Class<T>)(Object)Object.class;
                final T cc[] = (T[])Array.newInstance(tclass,1);
                cc[0] = t; // 数据初始化为t 
                return this.rbind(cc);
            }


            /**
             * 行追加
             * @param aa 追加的行数据
             * @return 新生成的矩阵
             */
            public DataMatrix<T> rbind(final T[] aa) {
                final T[][] uu = rbind(cells,transpose(vv2mm(aa)));
                return new DataMatrix<T>(uu,this.header());
            }

            /**
             * 列数据合并
             * @param mm 追加的列数据
             * @return 新生成的矩阵
             */
            public DataMatrix<T> cbind(final DataMatrix<T>mm) {
                final T[][] uu = cbind(cells,mm.cells);
                final List<String> header = new LinkedList<>();
                header.addAll(this.header());
                header.addAll(mm.header());
                return new DataMatrix<T>(uu,header);
            }

            /**
             * 矩阵转置,放弃了原来的表头，转置会丧失矩阵表头信息，表明命名法采用使用EXCEL的列名规则进行标识
             * @return 矩阵转置
             */
            public DataMatrix<T> transpose() {
                return new DataMatrix<>(transpose(cells));
            }
            
            /**
             * transpose 的别名
             * 矩阵转置,放弃了原来的表头，转置会丧失矩阵表头信息，表明命名法采用使用EXCEL的列名规则进行标识
             * @return 矩阵转置
             */
            public DataMatrix<T> tp() {
                return new DataMatrix<>(transpose(cells));
            }
            
            /**
             * 矩阵转置,放弃了原来的表头，转置会丧失矩阵表头信息，表明命名法采用使用EXCEL的列名规则进行标识
             * @param hh转放后的列名
             * @return 矩阵转置
             */
            public DataMatrix<T> transpose(final List<String>hh) {
                return new DataMatrix<>(transpose(cells),hh);
            }
            
            /**
             * 矩阵转置,放弃了原来的表头，转置会丧失矩阵表头信息，表明命名法采用使用EXCEL的列名规则进行标识
             * @param hh转放后的列名
             * @return 矩阵转置
             */
            public DataMatrix<T> transpose(final String[] hh) {
                return new DataMatrix<>(transpose(cells),hh);
            }
            
            /**
             * 矩阵转置,放弃了原来的表头，转置会丧失矩阵表头信息，表明命名法采用使用EXCEL的列名规则进行标识
             * @param hh转放后的列名,分隔符默认为"[,\\\\/]+" 
             * @return 矩阵转置
             */
            public DataMatrix<T> transpose(final String hh) {
                return new DataMatrix<>(transpose(cells),hh.split("[,\\\\/]+"));
            }
            
            /**
             * saxpy:this xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> plus(final T y){
                final Class<T> tclazz = y==null?getGenericClass(this.cells):(Class<T>)y.getClass();
                final T yy[][] = newArray(tclazz,1,1);
                yy[0][0]=y;
                return this.saxpy((T)(Object)1,yy);
            }

            /**
             * saxpy:this xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> plus(final T[][]yy){
                return this.saxpy((T)(Object)1,yy);
            }

            /**
             * saxpy:this xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> plus(final DataMatrix<T> y){
                return this.saxpy((T)(Object)1,y.cells);
            }

            /**
             * 矩阵减法,减去: this - subtractor 
             * @param <U> 中间过渡计算的数据类型
             * @param subtractor 减数
             * @return 差矩阵
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> minus(final T subtractor){
                final T _subtractor = (T)multiply((U)(Object)(-1.0),(U)(Object)subtractor);
                return this.plus(_subtractor);
            }

            /**
             * 矩阵减法,减去: this - subtractor
             * @param <U> 中间过渡计算的数据类型
             * @param subtractor 减数
             * @return 差矩阵
             */
            public <U extends Number> DataMatrix<T> minus(T[][] subtractor){
                return this.minus(new DataMatrix<>(subtractor));
            }

            /**
             * 矩阵减法 ,减去:this - subtractor
             * @param <U> 中间过渡计算的数据类型
             * @param subtractor 减数
             * @return 差矩阵
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> minus(DataMatrix<T> subtractor){
                return this.plus(subtractor.multiply((T)(Number)(-1.0)));
            }

            /**
             * 矩阵减法: 被减,subtrahend - this  
             * @param <U> 中间过渡计算的数据类型
             * @param subtractor 减数
             * @return 差矩阵
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> minus2(final T subtrahend){
               return this.multiply((T)(Object)(-1.0)).plus(subtrahend);
            }

            /**
             * 矩阵减法:被减,subtrahend - this  
             * @param <U> 中间过渡计算的数据类型
             * @param subtractor 减数
             * @return 差矩阵
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> minus2(T[][] subtrahend){
                return (new DataMatrix<>(subtrahend)).plus(this.multiply((T)(Object)(-1.0)));
            }

            /**
             * 矩阵减法:被减, subtrahend - this 
             * @param <U> 中间过渡计算的数据类型
             * @param subtractor 减数
             * @return 差矩阵
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> minus2(DataMatrix<T> subtrahend){
               return this.multiply((T)(Object)(-1.0)).plus(subtrahend);
            }

            /**
             * 矩阵点乘积  dot product
             * saxpy:this xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            public <U extends Number> DataMatrix<T> multiply(final T a){
                return this.saxpy(a,(T[][])null);
            }

            /**
             * 矩阵点乘积  dot product
             * saxpy:this is xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> multiply(final T[][] yy){
                U[][] xx = (U[][])this.cells;
                return new DataMatrix<>((T[][])DataMatrix.multiply(xx, (U[][])yy));
            }

            /**
             * 矩阵点乘积  dot product
             * saxpy:this is xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> multiply(final DataMatrix<T>y){
                U[][] xx = (U[][])this.cells;
                return new DataMatrix<>((T[][])DataMatrix.multiply(xx, y==null?null:(U[][])y.cells));
            }

            /**
             * 用矩阵 divisor 除 当前矩阵:this/divisor
             * @param <U> 中间数据类型
             * @param divisor 除数
             * @return 按元素位置进行的求商结果
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> div(final T divisor){
                U a = div((U)(Object)1,(U)divisor);//
                return this.saxpy((T)a,(T[][])null);
            }

            /**
             * 用矩阵 divisor 除 当前矩阵:this/divisor
             * @param <U> 中间数据类型
             * @param divisor 除数
             * @return 按元素位置进行的求商结果
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> div(final T[][] divisor){
                final U uu[][] = DataMatrix.div((U)(Object)1.0, (U[][])divisor);
                return this.multiply((T[][])(Object)uu);
            }
            
            /**
             * 用矩阵 divisor 除 当前矩阵:this/divisor
             * 矩阵除以 divisor
             * @param <U> 中间数据类型
             * @param divisor 除数矩阵
             * @return 按元素位置进行的求商结果
             */
            public <U extends Number> DataMatrix<T> div(DataMatrix<T> divisor){
                return this.div(divisor.cells);
            }

            /**
             * 数字除以矩阵,dividend 除以矩阵
             * @param <U> 中间数据类型
             * @param dividend 被除数
             * @return 矩阵倍除的结果
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> div2(final T dividend){
                
                final U uu[][] = DataMatrix.div((U)(Object)dividend, (U[][])this.cells);
                return new DataMatrix<>((T[][])uu);
            }

            /**
             * 用矩阵 dividend 除以 当前矩阵 :dividend/this
             * @param <U> 中间数据类型
             * @param dividend 被除数矩阵
             * @return 按元素位置进行的求商结果
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> div2(final T[][] dividend){
                final U uu[][] = DataMatrix.div((U)(Object)1.0, (U[][])this.cells);
                final U vv[][] = DataMatrix.multiply((U[][])dividend, uu);
                return new DataMatrix<T>((T[][])vv,this.header());
            }
            
            /**
             * 用矩阵 dividend 除以 当前矩阵 :dividend/this
             * @param <U> 中间数据类型
             * @param dividend 被除数矩阵
             * @return 按元素位置进行的求商结果
             */
            public <U extends Number> DataMatrix<T> div2(DataMatrix<T> dividend){
                return this.div2(dividend.cells);
            }

            /**
             * saxpy:this xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            public <U extends Number> DataMatrix<T> saxpy(final T a,final DataMatrix<T> y){
                return this.saxpy(a, (y==null?null:y.cells));
            }

            /**
             * saxpy:this xx,
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> saxpy(final T a,final T[][] yy){
                U[][] xx = (U[][])this.cells;
                U[][] uu = DataMatrix.saxpy((U)a, xx, (U[][])yy);
                return new DataMatrix<>((T[][])uu);
            }

            /**
             * saxpy:this.yy
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            public <U extends Number> DataMatrix<T> saxpy2(final T a,final DataMatrix<T> x){
                return this.saxpy2(a, (x==null?null:x.cells));
            }
            
            /**
             * saxpy:this.yy
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> adjugate(){
                final U[][] uu = DataMatrix.adjugate((U[][])this.cells);
                return new DataMatrix<>((T[][])uu);
            }
            
            /**
             * 求矩阵的逆矩阵
             * @param <U> 中间数据类型
             * @return 矩阵的逆矩阵
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> reverse(){
                final U[][] uu = DataMatrix.reverse((U[][])this.cells);
                return new DataMatrix<>((T[][])uu);
            }

            /**
             * saxpy:this.yy
             * @param <U> 中间数据类型
             * @param a 系数
             * @param y 偏移向量
             * @return saxpy
             */
            @SuppressWarnings("unchecked")
            public <U extends Number> DataMatrix<T> saxpy2(final T a,final T[][] xx){
                final U[][] yy = (U[][])this.cells;
                final U[][] uu = DataMatrix.saxpy((U)a, (U[][])xx,yy);
                return new DataMatrix<>((T[][])uu);
            }
            
            /**
             * 生成一个 DataMatrix
             * 
             * @param <T> 流对象的元素的类型
             * @param <U> 目标的集合的乐行
             * @param stream 流对象
             * @param mapper 映射
             * @return DataMatrix<U>
             */
            public static <T,U> DataMatrix<U> of(final Stream<T> stream,
                final Function<T,LinkedHashMap<String,U>> mapper){
                
                return new DataMatrix<U>(stream,mapper);
            }

            /**
             * 生成一个 DataMatrix
             * 
             * @param <T> 流对象的元素的类型
             * @param <U> 目标的集合的乐行
             * @param stream 流对象
             * @param mapper 映射
             * @return DataMatrix<U>
             */
            public static <T,U,V> DataMatrix<U> of(final Stream<T> stream,
                final Function<T,LinkedHashMap<String,U>> row_mapper,final Class<U> uclass){
                
                return new DataMatrix<U>(stream,row_mapper,uclass);
            }

            /**
             * 
             * @param line 数据行
             * @return DataMatrix<String>
             */
            public static DataMatrix<String> of(final String line){
                return of(line,(List<String>)null);
            }

            /**
             * 
             * @param line 数据行
             * @param header 表头
             * @return DataMatrix<String>
             */
            public static DataMatrix<String> of(final String line,final List<String> header){
                
                final Stream<List<String>> ll = Arrays.stream(line.split("[\n;]+"))
                .map(e->Arrays.asList(e.split("[,\\s]+")));
                
                return new DataMatrix<String>(ll.collect(Collectors.toList()),header);
            }

            /**
             * 
             * @param line
             * @return
             */
            public static DataMatrix<String> of(final String line,final String[] hh){
                return of(line,Arrays.asList(hh));
            }

            /**
             * 
             * @param line
             * @return
             */
            public static DataMatrix<String> of(final String line,final String hh){
                return of(line,Arrays.asList(hh.split("[,]+")));
            }

            /**
             * 数据矩阵,使用exelname 规则进行表头命名
             * @param cells 数据
             */
            public static <U> DataMatrix<U> of(final U[][] cells,final List<String>hh) {
                return new DataMatrix<U>(cells,hh);
            }

            /**
             * 数据矩阵,使用exelname 规则进行表头命名
             * @param cells 数据
             */
            public static <U> DataMatrix<U> of(final U[][] cells) {
                return of(cells,null);
            }

            /**
             * 生曾一个
             * @param rows 行数据集合：元素列表每个含数据为以HashMap<String,T>
             * @param clazz 元素类型
             * @return T类型的矩阵
             */
            public static <U> DataMatrix<U> of(final Collection<?> rows,final Class<U> clazz) {
                return new DataMatrix<U>(rows,clazz);
            }

            /**
             * 列集合构造DataMatrix
             * @param columns 劣迹和
             * @param uclass 元素类型
             */
            public static <U> DataMatrix<U> of(final List<DColumn<U>> columns,final Class<U> clazz){
                return new DataMatrix<U>(columns,clazz);
            }

            /**
             * 列集合构造DataMatrix
             * @param columns 劣迹和
             * @param uclass 元素类型
             */
            public static <U> DataMatrix<U> of2(final Stream<DColumn<U>> columns,final Class<U> clazz){
                return new DataMatrix<U>(columns.collect(Collectors.toList()),clazz);
            }

            /**
             * 生曾一个
             * @param rows 行数据集合流：元素列表每个含数据为以HashMap<String,T> 
             * @param clazz 元素类型
             * @return T类型的矩阵
             */
            public static <U> DataMatrix<U> of(final Stream<?> rows,final Class<U> clazz) {
                return new DataMatrix<U>(rows,clazz);
            }

            /**
             * 删除数组的第一行
             * @param cells
             * @return
             */
            @SuppressWarnings("unchecked")
            public static <U> U[][] removeFirstLine(final U[][] cells) {
                final int m = cells.length;
                final int n = cells[0].length;
                if(m<1)return null;
                
                final U[][] cc = (U[][])Array.newInstance(getGenericClass(cells),m-1,n);
                /*for(int i=1;i<cells.length;i++) {
                    for(int j=0;j<cells[0].length;j++) {
                        cc[i-1][j]=cells[i][j];
                    }
                }*/
                
                System.arraycopy(cells,1, cc,0, (m-1));
                
                return cc;
            }

            /**
             * 获得cells数组中的元素的数据类型。
             * 如果cells中的所有元素都是null,返回Object.class;
             * @param <U> ells数组中的元素的数据类型。
             * @param cells 数据矩阵
             * @return cells 元素类型
             */
            @SuppressWarnings("unchecked")
            public static <U> Class<U> getGenericClass(final U cells[][]){
                Class<U> uclass = (Class<U>) Object.class;
                if(cells==null)return uclass;
                List<Class<?>> ll= Arrays.stream(cells).flatMap(Arrays::stream).filter(e->e!=null)
                    .map(e->e.getClass()).distinct().collect(Collectors.toList());
                if(ll.size()==1) {
                    uclass =  (Class<U>) ll.get(0);
                }else {
                    uclass = (Class<U>) Object.class;
                }
                
                return uclass;
            }
            
            /**
             * 格式化一个二维矩阵
             * @param mm 二维矩阵
             * @param delim 行内分隔符号
             * @param ln 行间分隔符号
             * @return 二维数据组的格式化输出
             */
            public static <U> String fmt(final U[][] mm,String delim,String ln) {
                if(mm==null||mm.length<1) {
                    System.out.println("数组为空或维度不足！");
                    return "[]";
                }//if
                
                final int m = mm.length;
                final int n = mm[0].length;
                final StringBuffer buffer= new StringBuffer();
                if(mm!=null)for(int i=0;i<m;i++) {
                    for(int j=0;j<n;j++) {
                        buffer.append(mm[i][j]+delim);
                    }
                    buffer.append(ln);
                }
                return buffer.toString();
            }
            
            /**
             * 格式化一个二维矩阵：行内分隔delim 采用'\t',行间分隔ln符号采用'\n'
             * @param mm 二维矩阵
             * @return 二维数据组的格式化输出
             */
            public static <U> String fmt(final U[][] mm) {
                return fmt(mm,"\t","\n");
            }

            /**
             * 类型转换 强制类型转换:静态类型转换
             * @param <U> 目标类型的类
             * @param clazz 目标类型
             * @return Function<Object,LinkedHashMap<String,U>>
             */
            @SuppressWarnings("unchecked")
            public static <U> Function<Object,LinkedHashMap<String,U>> lhm_cast (final Class<U> clazz){
                return obj->(LinkedHashMap<String,U>)obj;
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
             * 把A转换成0,b转换成1,aa 转换成26
             * 如果line 本身就是一个数字则直接返回
             * "A1:B2" -> x0=0, y0=0, x1=1, y1=1
             * 
             * @param line 字符串:
             * 字符类别,A:转换成0,B 转换成1,AAA 转换成 702
             * 数字类别 ,1:转换哼0,2 转换成 1;
             * @return 数据解析
             */
            public static Integer excelname2i(final String line) {
                if(line == null)return null;
                final var final_line = line.toUpperCase().trim();// 转换成大写形式。
                Matcher matcher = Pattern.compile("\\d+")
                    .matcher(final_line);
                if(matcher.matches()) {// 数字格式
                    return Integer.parseInt(final_line)-1;
                }
                
                matcher = Pattern.compile("[A-Z]+")
                    .matcher(final_line);
                if(!matcher.matches())return null;
                final int N_SIZE = 26;// 进制
                final int len = final_line.length();
                int num = 0;// 数量内容
                for(int i=0;i<len;i++) {
                    final int n = (final_line.charAt(i)-'A')+(i==len-1?0:1);// A在line尾端为0，否则A对应为1
                    num=num*N_SIZE+n;
                }// for
                
                return num;
            }

            /**
             * A3:--->2,0
             * @param name excel 格式的单元格地址描述
             * @return
             */
            public static Tuple2<Integer,Integer> address2tuple(final String address) {
                final Pattern p = Pattern.compile("\\s*([A-Z]+)\\s*([0-9]+)\\s*");
                final Matcher matcher = p.matcher(address.toUpperCase());
                if(matcher.find()) {
                    final String g1 = matcher.group(1);
                    final String g2 = matcher.group(2);
                    //System.out.println(g1+"/"+excelname2i(g1)+"----->"+g2+"/"+excelname2i(g2));
                    final Integer x0 = excelname2i(g1);// 列名
                    final Integer y0 = excelname2i(g2);// 行名
                    return new Tuple2<>(y0,x0);
                }//if
                return null;
            }

            /**
             * 把一个从Range名称得出一个Range对象（平原四元组）
             * @param range_name 比如：A1:I1000
             * @return
             */
            public static RangeDef name2range(final String rangeName) {
                final String range_name = rangeName.trim();// 区域名称改变
                final String ss[] = range_name.split(":+");// 多个冒号算一个冒号
                if(ss.length<=1)return null;
                
                final List<Tuple2<Integer,Integer>> tups = new ArrayList<>(2);
                for(int i=0;i<ss.length;i++) {
                    final String s = ss[i].toUpperCase();
                    final Tuple2<Integer,Integer> tup = address2tuple(s);
                    if(tup!=null)tups.add(tup);
                    
                }//if
                
                return new RangeDef(tups.get(0)._1,tups.get(0)._2,tups.get(1)._1,tups.get(1)._2); 
            }

            /**
             * 数字格式化 :科学记数法转换成普通记数
             * 比如：1.8121574632E10 就变换成18121574632E10
             * @param number
             * @return 数值了ing，如果转换失败则返回null
             */
            public static Number sci2num(final String number) {
                if(number==null)return null;
                final Pattern p = Pattern.compile("\\s*\\d+\\.\\d+e\\d+\\s*",Pattern.CASE_INSENSITIVE);
                final Matcher mat = p.matcher(number);
                if(mat.matches()) {
                    final DecimalFormat df = new DecimalFormat("0");
                    Number num=null;
                    try {
                        num = df.parse(number);
                        return num;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }//try
                }
                return null;// 无法转换成数字
            }

            /**
             * 转换成double 序列 把数字序列拆分成一位向量
             * 逗号，或者空格分割
             * @param line
             * @return 一维数组
             */
            public static Double[] series(final String line) {
                return Arrays.asList(line.split("[,\\s]+")).stream()
                    .map(Double::parseDouble).toArray(Double[]::new);
            }

            /**
             * 创建  m * n 的二维数组
             * @param <U> 元素类型
             * @param ucls 元素类型的class
             * @param m 行数
             * @param n 列数
             * @return U[][]
             */
            @SuppressWarnings("unchecked")
            public static <U> U[][] newArray(final Class<U> ucls,final int m,final int n){
                return (U [][] )Array.newInstance(ucls, m,n);
            }

            /**
             * 行绑定 把bb 以行的形式最佳到aa的后面：即形成的新矩阵
             * aa<br>
             * bb<br>
             * @param aa 原来数据矩阵
             * @param bb 追加数据矩阵
             * @return 如下结构的结果：<br>aa<br>bb
             */
            public static <U> U[][] rbind(final U[][] aa,final U[][] bb) {
                
                final int m1 = aa.length;
                final int n1 = aa[0].length;
                final int m2 = bb.length;
                final int n2 = bb[0].length;
                
                final U[][] uu = newArray(getGenericClass(aa),m1+m2,Math.max(n1,n2));
                final int m = uu.length;
                final int n = uu[0].length;
                
                for(int i=0;i<m;i++) {
                    final U[][] cc  = i<m1 ? aa : bb; // 选择提取元素的矩阵,保证先提取aa而后在提取bb
                    final var offset = i<m1 ? 0 : m1; //行的索引偏移,只有当移动到 bb 矩阵的发生移动。
                    for(int j=0;j<n;j++) uu[i][j]=cc[(i-offset)%cc.length][j%cc[0].length];
                }
                
                return uu;
            }

            /**
             * 把矩阵bb 以列的形式 与aa 进行连接：即形成
             * aa bb 的记过
             * @param aa 原来的数据矩阵
             * @param bb 连接数据的矩阵
             * @return U[][] aa bb 的结果
             */
            public static <U> U[][] cbind(final U[][] aa,final U[][] bb) {
                final int m1 = aa.length;// aa的行数
                final int n1 = aa[0].length;// aa的列数
                final int m2 = bb.length;// bb 的行数
                final int n2 = bb[0].length;// bb 的列数
                
                final U[][] uu = newArray(getGenericClass(aa),Math.max(m1,m2),n1+n2);// 结果矩阵
                final int m = uu.length;// 结果矩阵的行数
                final int n = uu[0].length;// 结果矩阵的列数
                
                for(int j=0;j<n;j++){// 按照列进行数据追加
                    final U[][] cc = j<n1 ? aa :bb;// 选择提取元素的矩阵,保证先提取aa而后在提取bb
                    final var offset = j<n1 ? 0 :n1; // 列的索引偏移,只有当移动到 bb 矩阵的发生移动。
                    for(int i=0;i<m;i++)uu[i][j] = cc[i%cc.length][(j-offset)%cc[0].length];// 采用R的索引循环的办法进行提取
                }//for j
                
                return uu;// 列连接之后的结果
            }

            /**
             * 转换成double 序列 把数字序列拆分成一个n*1维度的矩阵
             * @param line 字段文本 待采用series 进行数据分析数据
             * @return Double[][] 
             */
            public static Double[][] v2m(final String line) {
                return dbl_vv2mm(series(line));
            }

            /**
             * 一个长度为n维数组转n*1矩阵
             * @param dd 一维的数组数据
             * @return nx1的矩阵
             */
            @SuppressWarnings("unchecked")
            public static <U> U[][] vv2mm(final U uu[]){
                if(uu ==null)return null;
                int n = uu.length;
                Class<U> ucls = null;
                for(U u:uu) if(u!=null) {ucls = (Class<U>)u.getClass();break;}
                final U oo[][] = (U[][])Array.newInstance(ucls,n,1);
                for(int i=0;i<n;i++)oo[i][0]=uu[i];
                return oo;
            }

            /**
             * 生成一个数字序列
             * @param start 开始元素
             * @param unary 递增函数
             * @param n 数据长度
             * @return Double[] 数字数组
             */
            public static Double[] numbers(final Double start,
                final UnaryOperator<Double> unary,final int n) {
                
                return Stream.iterate(start, unary).limit(n).toArray(Double[]::new);
            }
            
            /**
             * 生成数据向量
             * @param line 带解析的字符串数据,[\n;] 分隔行,[,\s] 分格列
             * @retur DataMatrix<Integer>
             */
            public static DataMatrix<Integer> intmm(final String line){
                final Integer n = Arrays.stream(line.split("[\n;]+")) // 行分割
                    .map(e->e.split("[,\\s]+").length) // 列采用 逗号和空格进行分割
                    .collect(Collectors.summarizingInt(e->e)).getMax();// excel 列名称
                return of(line,Stream.iterate(0, i->i+1).map(DataMatrix::excelname).limit(n).
                    collect(Collectors.toList()))
                    .corece(Integer::parseInt);// 强制转换成整数矩阵
            }

            /**
             * 解析一段文本为矩阵：
             * 
             * @param line 文本矩阵的没描述，行分隔符为回车货真“;” 个视为一个。 , 列分隔符为 “,” 或者 空白（空格 多
             * @return 文本矩阵
             */
            public static DataMatrix<String> strmm(final String line){
                
                final Integer n = Arrays.stream(line.split("[\n;]+"))// 行分割
                    .map(e->e.split("[,\\s]+").length) // 列采用 逗号和空格进行分割
                    .collect(Collectors.summarizingInt(e->e)).getMax();// excel 列名称
                return of(line,Stream.iterate(0, i->i+1)
                    .map(DataMatrix::excelname).limit(n)
                    .collect(Collectors.toList())); // 强制转换成整数矩阵
            }

            /**
             * 浮点数的矩阵
             * @param line 带解析的字符串数据,[\n;] 分隔行,[,\s] 分格列
             * @return 浮点数矩阵
             */
            public static DataMatrix<Double> dblmm(final String line){
                final Integer n = Arrays.stream(line.split("[\n;]+")) // 行分割
                    .map(e->e.split("[,\\s]+").length) // // 列采用 逗号和空格进行分割
                    .collect(Collectors.summarizingInt(e->e)).getMax();// excel 列名称
                return of(line,Stream.iterate(0, i->i+1)
                    .map(DataMatrix::excelname).limit(n)
                    .collect(Collectors.toList()))
                    .corece(Double::parseDouble); // 强制转换双精度浮点数矩阵
            }

            /**
             * 转换成double 序列 把数字序列拆分成一个n*1维度的矩阵
             * @param line 带解析的字符串数据,[\n;] 分隔行,[,\s] 分格列
             * @return DataMatrix<Double>
             */
            public static DataMatrix<Double> dbl_v2mx(final String line) {
                return new DataMatrix<Double>(dbl_vv2mm(series(line)));
            }

            /**
             * 把一维度数组 转换成二维数组：nx1的矩阵
             * @param dd 向量数组转矩阵对象
             * @return nx1的矩阵 
             */
            public static DataMatrix<Double> dbl_vec2mx(final Double dd[]){
                return new DataMatrix<Double>(dbl_vv2mm(dd));
            }

            /**
             * 一个长度为n维数组转n*1矩阵
             * @param dd 一维的数组数据
             * @return nx1的矩阵
             */
            public static Double [][] dbl_vv2mm(final Double dd[]){
                if(dd ==null)return null;
                int n = dd.length;
                final Double oo[][] = new Double[n][1];
                for(int i=0;i<n;i++)oo[i][0]=dd[i];
                return oo;
            }

            /**
             * 生成全1数组
             * @param n 方阵宽度
             * @return 全1数组
             */
            public static Double[] ones(final int n) {
                return numbers(1d,e->1d,n);
            }

            /**
             * 生成一个指定元素的对角矩阵
             * @param aa 元素 对角元素
             * @return  Double[][] 对角阵
             */
            public static Double[][] diag(final int n) {
                return diag(ones(n));
            }

            /**
             * 生成一个指定元素的对角矩阵
             * @param aa 元素
             * @return
             */
            public static Double[][] diag(final Double aa[]){
                final int n = aa.length;
                final Double[][] oo= new Double[n][n];
                for(int i=0;i<n;i++) for(int j=0;j<n;j++) oo[i][j]=(i==j)?aa[i]:0d;
                return oo;
            }

            /**
             * 生成一个指定元素的对角矩阵
             * @param aa 元素
             * @return 对角矩阵
             */
            public static DataMatrix<Double> diagM(final int n) {
                return new DataMatrix<Double>(diag(ones(n)));
            }

            /**
             * 矩阵专职
             * @param cells 数据矩阵
             * @return U[][]
             */
            @SuppressWarnings("unchecked")
            public static <U> U[][] transpose(U[][] cells){
                if(cells==null || cells[0] == null)return null;
                final int m = cells.length;
                final int n = cells[0].length;
                Class<U> u = getGenericClass(cells);
                if( u == null) u = (Class<U>) Object.class;
                U[][] cc = (U[][]) Array.newInstance(u,n,m);
                for(int i=0;i<cells.length;i++)
                    for(int j=0;j<cells[0].length;j++)
                cc[j][i] = cells[i][j];
                
                return cc;
            }
            
            /**
             * 
             * @param <U>
             * @param x
             * @param y
             * @return
             */
            @SuppressWarnings("unchecked")
            public static <U extends Number> Class<U> getTargetClass(U x,U y){
                final var ll = Stream.of(x,y).filter(e->e!=null).map(e->e.getClass()).distinct().collect(Collectors.toList());
                if(ll.size()==1)return ( Class<U>)(Object)(ll.get(0).getClass());
                if(ll.size()==2) {
                    Class<?> c1 = ll.get(0);
                    Class<?> c2 = ll.get(1);
                    if(c1==Integer.class)return ( Class<U>)(Object) c2;
                    else if(c1==Long.class)return ( Class<U>)(Object) c2;
                    else if(c1==Float.class)return ( Class<U>)(Object) c2;
                    else return (Class<U>)(Object) c1;
                }
                return null;
            }

            /**
             * 数字类型转换
             * @param <U> 目标类型
             * @param number 数字
             * @param targetClass 目标类型
             */
            @SuppressWarnings("unchecked")
            public static <U extends Number> U cast(Number number,Class<U>targetClass){
                if(targetClass==Integer.class)
                    return (U)(Object)number.intValue();
                else if(targetClass==Double.class) {
                    return (U)(Object)number.doubleValue();
                }else if(targetClass==Float.class) {
                    return (U)(Object)number.floatValue();
                }else if(targetClass==Long.class) {
                    return (U)(Object)number.longValue();
                }else {
                    return (U)(Object)number.doubleValue();
                }
            }

            /**
             * 和的结果
             * @param <U> 元素类型
             * @param x 左元素
             * @param y 右元素
             * @return 和的结果
             */
            public static <U extends Number> U plus(final U x,final U y){
                if(x==null)return y;
                if(y==null)return x;
                final Number num = (x.doubleValue()+y.doubleValue());
                
                return (U)cast(num,getTargetClass(x,y));
            }

            /**
             * 除法:dividend/divisor
             * @param <U> 数据元素类型
             * @param dividend 左边
             * @param divisor 右边
             * @return U 乘积
             */
            public static <U extends Number> U div(final U dividend,final U divisor){
                if(dividend==null)return null;
                if(divisor==null)return null;
                final Number num = (dividend.doubleValue()/divisor.doubleValue());
                
                return (U)cast(num,getTargetClass(dividend,divisor));
            }

            /**
             * 乘积:矩阵点乘积  dot product
             * @param <U> 数据元素类型
             * @param x 左边
             * @param y 右边
             * @return U 乘积
             */
            public static <U extends Number> U multiply(final U x,final U y){
                if(x==null)return null;
                if(y==null)return null;
                final Number num = x.doubleValue()*y.doubleValue();
                
                return (U)cast(num,getTargetClass(x,y));
            }

            /**
             * 矩阵点乘积  dot product
             * 把 xx 与 yy 的对应的元素进行相乘
             * @param xx 矩阵
             * @param yy 矩阵
             * @return a*x+y
             */
            public static <U extends Number> U[][] multiply(final U[][] xx,final U[][] yy) {
                final var shapex = shape(xx);
                final var shapey = shape(yy);
                final var shapemax = shapemax(xx,yy);
                Class<U> uclazz= getGenericClass(xx);
                if(uclazz==null)uclazz=getGenericClass(yy);
                
                final U[][] uu = newArray(uclazz,shapemax._1(),shapemax._2());
                final BiFunction<Integer,Integer,U> getx =(i,j)->get(xx,i,j,shapex);
                final BiFunction<Integer,Integer,U> gety =(i,j)->get(yy,i,j,shapey);
                
                for(int i=0;i<shapemax._1();i++) {
                    for(int j=0;j<shapemax._2();j++) {
                        final var x = getx.apply(i,j);
                        final var y = gety.apply(i,j);
                        final var u = multiply(x,y);;
                        uu[i][j] = cast(u,uclazz);
                    }//for
                }//for
                
                return uu;
            }
            
            /**
             * 除法数除以矩阵
             * @param <U> 数据元素类型
             * @param dividend 被除数
             * @param xx 被除数矩阵
             * @return dividend/xx 的结果
             */
            public static <U extends Number> U[][] div(final U dividend,final U[][] xx){
                final var shapex = shape(xx);
                final Class<U> uclazz= getGenericClass(xx);
                final U[][] uu = newArray(uclazz,shapex._1(),shapex._2());
                final BiFunction<Integer,Integer,U> getx =(i,j)->get(xx,i,j,shapex);// 索引循环取值
                
                for(int i=0;i<shapex._1();i++) {
                    for(int j=0;j<shapex._2();j++) {
                        final var x = getx.apply(i,j);// 采用R语言的循环取值的办法进行索引取值
                        final var u = div(dividend,x);
                        uu[i][j] = cast(u,uclazz);
                    }//for j 列循环
                }//for i 行循环
                
                return uu;// 返回结矩阵
            }

            /**
             * 矩阵乘法
             * @param aa 左乘矩阵
             * @param bb 右乘矩阵
             * @return Double[][] 结果矩阵
             */
            @SuppressWarnings("unchecked")
            public static <U extends Number> U[][] mmult(final U aa[][],final U bb[][]) {
                
                return DataMatrix.mmult2(aa, bb,DataMatrix::multiply, (U)(Object)0,DataMatrix::plus);
            }
            
            /**
             * 通用的:矩阵乘法
             * @param <T> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 结果矩阵的元素类型
             * @param aa 左矩阵
             * @param bb 右矩阵
             * @param product_operator 乘法算子
             * @param identity 零元元素
             * @param op 累加元素运算
             * @return 以V为元素的矩阵
             */
            public static <T,U,V> V[][] mmult2(final T aa[][],final U bb[][], 
                final BiFunction<T,U,V> product_operator, final V identity,BinaryOperator<V> op) {
                
                return DataMatrix.mmult2(aa, bb, product_operator,vv->vv.reduce(identity,op));
            }

            /**
             * 矩阵乘法
             * @param <T1> 左矩阵的元素类型
             * @param <U> 右矩阵的元素类型
             * @param <V> 中间结果矩阵的元素类型
             * @param <O> 最终结果矩阵的元素类型
             * @param ltt 左边的T类型矩阵
             * @param ruu 右边的U类型矩阵
             * @param product_operator T类型与U类型的向V类型进行映射的二元函数 (t,u)->v, 
             * @param reducer V类型集合向O类型元素进行映射的多元函数 (...vvv)->o
             * @return O 类型的矩阵
             */
            @SuppressWarnings("unchecked")
            public static <T1,U,V,O> O[][] mmult2(final T1[][] ltt, final U[][] ruu,
                final BiFunction<T1,U,V> product_operator, final Function<Stream<V>,O> reducer){
                // 对矩阵维度进行检查
                if(ltt==null||ruu==null||ltt.length<1 || ltt[0].length<1
                    ||ruu.length<1||ruu[0].length<1) {
                    System.err.println("矩阵维度数不足,至少需要二维");
                    return null;// 
                }
                
                final int m = ltt.length;// 行数量
                final int p = ltt[0].length; // 中间联系维度。
                final int n = ruu[0].length; // 列数量
                
                if (ruu.length != p) {
                    System.err.println(MessageFormat.format("左右矩阵的行列不匹配[{0},{1}] x [{1},{2}]，不能做乘法",
                        m,p,ruu.length,n));
                    return null;
                }
                
                Class<O> oclass = null;// 最终结果矩阵中的元素类型
                O mm[][] = null;// 最终结果矩阵的缓存
                final O backups[][] = (O[][])DataMatrix.newArray(Object.class, m, n);// 结果矩阵 的备用方案
                int failue_times = 0;// 失败次数计数器
                for (int i = 0; i < m; i++) {// 行遍历
                    for (int j = 0; j < n; j++) { // 列遍历
                        
                        final List<V> vv = new LinkedList<V>();// ij 位置 的中间结果累积缓存
                        for (int k = 0; k < p; k++) { // 中间生成与遍历遍历程序
                           final var v = product_operator.apply(ltt[i][k], ruu[k][j]); // (t1,u)->v 二元乘积。
                           vv.add(v);// 中间结果累积
                        } // for k
                        
                        final var sum = reducer==null ?(O)vv :reducer.apply(vv.stream()); // 中间结果集规约
                        if(sum==null) { 
                            continue; // 空值结果不予处理，因为 mm生成的时候就默认为null.
                        }else if(oclass==null) {
                            oclass = (Class<O>) sum.getClass();
                            mm = (O[][])DataMatrix.newArray(oclass, m, n);// 结果矩阵 的备用方案
                        }//if
                        
                        try {
                            if( failue_times>0 ) throw new Exception(MessageFormat
                                .format("失败次数大于1异常,累积失败次数:{0}",failue_times)) ;
                            mm[i][j] = sum;// 揭露累加和
                        }catch(Exception e){// ArrayStoreException
                            backups[i][j] = sum;// 启用备用保存。
                            failue_times++;
                        }// 尝试进行结果赋值
                        
                    } // for j 列遍历
                } // for i 行遍历
                
                return failue_times==0 ?mm :backups;// 返回数据乘积:若是 失败则返回备用数据。
            }

            /**
             * sum of aX plus Y: saxpy
             * @param a 系数
             * @param xx X矩阵
             * @param yy Y矩阵
             * @return a*X+Y
             */
            @SuppressWarnings("unchecked")
            public static <U extends Number> U[][] saxpy(final U a,final U[][] xx,final U[][] yy) {
                
                final var shapex = shape(xx);// 
                final var shapey = shape(yy);
                final var shapemax = shapemax(xx,yy);
                Class<U> uclazz= getGenericClass(xx);
                if(uclazz==null||uclazz==(Class<U>)(Object)Object.class)uclazz=getGenericClass(yy);
                
                final U[][] uu = newArray(uclazz,shapemax._1(),shapemax._2());
                final BiFunction<Integer,Integer,U> getx =(i,j)->get(xx,i,j,shapex);
                final BiFunction<Integer,Integer,U> gety =(i,j)->get(yy,i,j,shapey);
                
                for(int i=0;i<shapemax._1();i++) {
                    for(int j=0;j<shapemax._2();j++) {
                        final var x = getx.apply(i,j);
                        final var y = gety.apply(i,j);
                        final var u = plus(multiply(a,x),y);
                        uu[i][j] = cast(u,uclazz);
                    }//for
                }//for
                
                return uu;
            }

            /**
             * 求一个矩阵的行列式
             * @param <U> 数据元素类型
             * @param xx 矩阵
             * @return xx 的行列式
             */
            public static <U extends Number> U det(final U[][] xx) {
                
                final var shapex = shape(xx);
                if(shapex._1==1 && shapex._2==1)return xx[0][0];// 一维度矩阵不求行列式
                final BiFunction<Integer,Integer,U> getx =(i,j)->get(xx,i,j,shapex);
                U sum = null; // 累加的结果
                for(int j=0;j<shapex._2;j++) {
                    final U e = multiply(det(complement(xx,0,j)), getx.apply(0,j));// 行列式按照行展开的 基本元素e:elem
                    @SuppressWarnings("unchecked")
                    final U sign = (U)(Object)(Math.pow(-1.0,0+j));// 余子式的符号
                    sum = plus(sum,multiply(e,sign));// 数据累加
                }//for
                
                return sum;// 返回行列式的结果
            }
            
            /**
             * 一个矩阵的伴随矩阵
             * @param <U> 数据元素类型
             * @param xx 矩阵
             * @return xx 的行列式
             */
            @SuppressWarnings("unchecked")
            public static <U extends Number> U[][] adjugate(U[][] xx) {
                final var shapex = shape(xx);
                final Class<U> uclass = DataMatrix.getGenericClass(xx);
                final U[][] uu = newArray(uclass,shapex._1,shapex._2);// 生成结果的形状
                
                for(int i=0;i<shapex._1;i++) {// 行
                    for(int j=0;j<shapex._2;j++) {// 列
                        final var aij = DataMatrix.det(DataMatrix.complement(xx, j, i));// 注意这里发生了转至。i,j 顺序。
                        final var u = multiply(((U)(Object)Math.pow(-1,i+j)),aij);// 为aij添加符号并保存;
                        uu[i][j] = cast(u,uclass);
                    }// for j 列
                }// for i 行
                
                return uu;
            }
            
            /**
             * 求一个矩阵的逆矩阵
             * @param <U> 数据元素类型
             * @param xx 矩阵
             * @return xx 逆矩阵
             */
            @SuppressWarnings("unchecked")
            public static <U extends Number> U[][] reverse(U[][] xx) {
                final var adjX = DataMatrix.adjugate(xx);// xx的伴随矩阵
                final var detX = DataMatrix.det(xx);// 矩阵的行列式
                final var revX = DataMatrix.saxpy( div((U)(Object)1,detX) , adjX, null);// 使用saxpy 实现数乘(除)矩阵
                
                return revX;
            }

            /**
             * 补充矩阵
             * 
             * @param <U>
             * @param xx 源数据矩阵
             * @param p 行号 从0开始
             * @param q 列号 从0开始
             * @return U[][] 
             */
            public static <U extends Number> U[][] complement(final U[][] xx, final int p,final int q) {
                final var shapex = shape(xx);
                if(shapex._1()!=shapex._2())return null;
                if(shapex._1()==1 && shapex._2()==1)return null;
                final BiFunction<Integer,Integer,U> getx =(i,j)->get(xx,i,j,shapex);
                final U[][] uu = newArray(getGenericClass(xx),shapex._1()-1,shapex._2()-1);
                for(int i=0;i<shapex._1();i++) {
                    if(i==p)continue;
                    for(int j=0;j<shapex._2();j++) {
                        if(j==q)continue;
                        uu[i>=p?i-1:i][j>=q?j-1:j]=getx.apply(i,j);
                    }//for
                }//for
                
                return uu;
            }

            /**
             * 从xx中提取 (i,j) 位置的数据 
             * @param <U> 数据元素的类型
             * @param xx 数据矩阵
             * @param i 行编号 从0爱是
             * @param j 列编号从0开始
             * @param shapex 提取的数据范围
             * @return U 数据元素
             */
            public static <U extends Number> U get(final U[][] xx,final int i,final int j,
                final Tuple2<Integer,Integer> shapex){
                if(shapex._1==0||shapex._2==0)return null;
                return xx[i%shapex._1][j%shapex._2];
            }
            
            /**
             * 返回 shapex,shapey 的最大值
             * @param <U> X的元素类型
             * @param <V> Y的元素类型
             * @param xx 数据矩阵
             * @param yy 数据矩阵
             * @return uple2<Integer,Integer>
             */
            public static <U,V> Tuple2<Integer,Integer> shapemax(U[][] xx,V[][] yy){
                final var shapey = shape(xx);
                final var shapex = shape(yy);
                final var m = Math.max(shapex._1(),shapey._1());
                final var n = Math.max(shapex._2(),shapey._2());
                return new Tuple2<>(m,n);
            }
            
            /**
             * 返回矩阵的高度与宽度即行数与列数
             * 
             * @param aa 待检测的矩阵：过矩阵为null返回一个(0,0)的二元组。
             * @return (height:行数,width:列数)
             */
            public static <U> Tuple2<Integer,Integer> shape(U[][] aa){
                if(aa==null||aa.length<1)return new Tuple2<>(0,0);
                final int height = aa.length;
                int width = 0;
                for(int i=0;i<height;i++) {
                    if(aa[i]!=null) {
                        width = aa[i].length;
                        break;
                    }//if
                }//for
                return new Tuple2<>(height,width);
            }
            
            /**
             * 
             * @param <T> 列元素类型
             * @param <U> 结果类型
             * @param eval 运算器
             * @return 列的计算器
             */
            public static <T, U> Function<KVPair<String, List<T>>, U> summarize(final Function<List<T>, U> eval) {
                return kvp -> eval.apply(kvp.val());// 累加器
            }

            /**
             *
             * 字符串转换成矩阵
             * @param date yyyy-mm-dd HH:mm:ss
             * @return 日期对象, // 默认值当前系统时间
             */
            public static Date DATE(final String date){
                Date d = new Date();
                try {
                    d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                    
                return d;
            }

            /**
             * 与关系计算
             * @param pp 判断谓词
             * @return 与谓词
             */
            @SafeVarargs
            public static <U> Predicate<LinkedHashMap<String,U>> AND(final Predicate<LinkedHashMap<String,U>> ...pp){
                return e->{
                    for(Predicate<LinkedHashMap<String,U>> p:pp)if(!p.test(e))return false;
                    return true;
                };
            }

            /**
             * 或否计算
             * @param pp 判断谓词
             * @return 或谓词
             */
            @SafeVarargs
            public static <U> Predicate<LinkedHashMap<String,U>> OR(final Predicate<LinkedHashMap<String,U>> ...pp){
                return e->{
                    for(Predicate<LinkedHashMap<String,U>> p:pp)if(p.test(e))return true;
                    return false;
                };
            }

            /**
             * 关联数组：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static <U> Predicate<LinkedHashMap<String,U>> TEST_WITH(final String key,final Predicate<U>u_tester){
                return uu->u_tester.test(uu.get(key));
            }

            /**
             * 关联数组：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static <U> LinkedHashMap<String,U> ASSOC(final Class<U> cls,final String hh,final Map<String,U>uu){
                return ASSOC(cls,hh.split("[,]+"),uu);
            }

            /**
             * 关联数组：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static  LinkedHashMap<String,String> STR_ASSOC(final String hh,final Map<String,String>uu){
                return ASSOC(String.class,hh.split("[,]+"),uu);
            }

            /**
             * 关联数组提取函数：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射map 这是以函数的参数的形式出现的。
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static  Function<LinkedHashMap<String,String>,LinkedHashMap<String,String>> STR_ASSOC(final String hh){
                return uu->ASSOC(String.class,hh.split("[,]+"),uu);
            }

            /**
             * 关联数组提取函数：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射map 这是以函数的参数的形式出现的。
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static  Function<LinkedHashMap<String,Long>,LinkedHashMap<String,Long>> LNG_ASSOC(final String hh){
                return uu->ASSOC(Long.class,hh.split("[,]+"),uu);
            }

            /**
             * 关联数组：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static  LinkedHashMap<String,Integer> INT_ASSOC(final String hh,final Map<String,Integer>uu){
                return ASSOC(Integer.class,hh.split("[,]+"),uu);
            }

            /**
             * 关联数组：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static  LinkedHashMap<String,Long> LNG_ASSOC(final String hh,final Map<String,Long>uu){
                return ASSOC(Long.class,hh.split("[,]+"),uu);
            }

            /**
             * 关联数组：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static  LinkedHashMap<String,Double> DBL_ASSOC(final String hh,final Map<String,Double>uu){
                return ASSOC(Double.class,hh.split("[,]+"),uu);
            }

            /**
             * 关联数组：key-value 序列：这里是在Map:uu中提取hh中所列示的数据内容。可以说hh是一个键名过滤器
             * 注意这里对uu 中的元素给予重新命名。
             * @param cls 值类型
             * @param hh 键名过滤器
             * @param uu 待提取数据的映射
             * @return 从uu 中提取了hh键序列的元素。
             */
            public static <U> LinkedHashMap<String,U> ASSOC(final Class<U> cls,final String hh[],final Map<String,U>uu){
                LinkedHashMap<String,U> mm = new LinkedHashMap<>();
                for(int i=0;i<hh.length;i++) {
                    String key = hh[i%hh.length];
                    mm.put(key, uu.get(key));
                }
                return mm;
            }

            /**
             * 关联数组：key-value 序列, 也可以说为aa中的元素进行分别命名。这里是根据hh给予对应命名
             * 关联数组:key->value,也就是Map<String,UnknownType>
             * 
             * @param cls UnknownType U 的类型
             * @param hh 键名序列:(hh表示headers)也就是为uu中的元素进行命名
             * @param uu UnknownType U 的元素序列
             * @return 整型数字可记录
             */
            public static <U> LinkedHashMap<String,U> ASSOC(final Class<U> cls,final String hh[],U[] uu){
                LinkedHashMap<String,U> mm = new LinkedHashMap<>();
                for(int i=0;i<hh.length;i++) {
                    mm.put(hh[i%hh.length], uu[i%uu.length]);
                }
                return mm;
            }

            /**
             * 关联数组：key-value 序列, 也可以说为aa中的元素进行分别命名。这里是根据需要位置命名。
             * key 采用位置需要，从0开始。
             * 比如数组：{1，2，3，4} {0->1,1->2,2->3,3->4}
             * @param aa 数据对象数组
             * @return 从位置0开始为aa 中的元素提供位置命名。
             */
            public static <U> LinkedHashMap<String,U> ASSOC(final Class<U> cls,final Object ... aa){
                LinkedHashMap<String,U> mm = new LinkedHashMap<>();
                for(int i=0;i<aa.length;i+=2) {
                    String key = aa[i].toString();
                    @SuppressWarnings("unchecked")
                    U value = (U)aa[i+1];
                    mm.put(key, value);
                }
                return mm;
            }

            /**
             * 数据初始化,使用 inits 来初始化对象obj,用inits中的key的值设置obj中的对应的字段 
             * @param obj 待初始对象
             * @param inits 初始源数据
             */
            public static void OBJINIT(final Object obj,final Map<String,?>inits) {
                Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(e->inits.keySet().contains(e.getName())).forEach(fld->{
                    try {
                        fld.setAccessible(true);
                        Object value=inits.get(fld.getName());
                        if(value==null)return;
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
                                System.out.println(obj+"===>"+value);
                            } else if(fld.getType() == Date.class) {
                                if(value instanceof Number) {
                                    long time = ((Number)value).longValue();
                                    Date date = new Date(time);
                                    fld.set(obj, date);
                                } else {
                                    String ss[] = "yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm,yyyy-MM-dd HH,yyyy-MM-dd,yyyy-MM,yyyy-MM,yyyy"
                                        .split("[,]+");
                                    Date date = null;
                                    for(String s:ss) {
                                        try {date = new SimpleDateFormat(s).parse(value.toString());}catch(Exception ex) {};
                                        if(date!=null)break;
                                    }//for
                                    fld.set(obj, date);
                                }// date
                            }//if
                        }
                    }catch(Exception ex) {ex.printStackTrace();}
                });
            }

            public static Function<DColumn<Double>, DoubleSummaryStatistics> dblstats = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e));
    
            public static Function<DColumn<Integer>, Integer> intsum = p -> (int) (p.stream()
                    .collect(Collectors.summarizingInt(e -> e)).getSum());
    
            public static Function<DColumn<Integer>, Long> lngsum = p -> (p.stream()
                    .collect(Collectors.summarizingLong(e -> e)).getSum());
    
            public static Function<DColumn<Double>, Double> dblsum = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e)).getSum();
    
            public static Function<DColumn<Double>, Double> dblavg = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e)).getAverage();
    
            public static Function<DColumn<Integer>, Double> intavg = p -> p.stream()
                    .collect(Collectors.summarizingInt(e -> e)).getAverage();
    
            public static Function<DColumn<Long>, Double> lngavg = p -> p.stream()
                    .collect(Collectors.summarizingLong(e -> e)).getAverage();
    
            public static Function<DColumn<Double>, Long> dblcnt = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e)).getCount();
    
            public static Function<DColumn<Integer>, Integer> intcnt = p -> (int) p.stream()
                    .collect(Collectors.summarizingInt(e -> e)).getCount();
    
            public static Function<DColumn<Double>, Long> lngcnt = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e)).getCount();
    
            public static Function<DColumn<Double>, Double> dblmax = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e)).getMax();
    
            public static Function<DColumn<Double>, Double> intmax = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e)).getMax();
    
            public static Function<DColumn<Long>, Long> lngmax = p -> p.stream().collect(Collectors.summarizingLong(e -> e))
                    .getMax();
    
            public static Function<DColumn<Double>, Double> dblmin = p -> p.stream()
                    .collect(Collectors.summarizingDouble(e -> e)).getMin();
    
            public static Function<DColumn<Integer>, Integer> intmin = p -> p.stream()
                    .collect(Collectors.summarizingInt(e -> e)).getMin();
    
            public static Function<DColumn<Long>, Long> lngmin = p -> p.stream().collect(Collectors.summarizingLong(e -> e))
                    .getMin();

            private T[][] cells; // 单元格数据
            private Map<String, Integer> km = new HashMap<>();// key-->id map
            
    }

    /**
     * 字符串矩阵
     * @author admin
     *
     */
    public static class StrMatrix extends DataMatrix<String> {
        
        /**
         * 
         * @param m
         * @param n
         * @param hh 表头定义，null 则首行为表头
         */
        public StrMatrix(DataMatrix<String> mx) {
            super(mx.cells,mx.header());
        }
    
        /**
         * 
         * @param m
         * @param n
         * @param hh 表头定义，null 则首行为表头
         */
        public StrMatrix(int m, int n,List<String>  hh) {
            super(m, n,String.class,hh);
        }
    
        /**
         * 字符串矩阵
         * @param ll 数据定义
         * @param hh 表头定义，null 则首行为表头
         */
        public StrMatrix(List<List<String>> ll,List<String> hh) {
            super(ll,hh);
        }
        
        /**
         * 数据矩阵
         * @param cells数据矩阵
         * @param hh 表头定义,null 则首行为表头
         */
        public StrMatrix(String[][] cells,List<String> hh) {
            super(cells,hh);
        }
        
        /**
         * 行遍历
         * @param cs
         */
        @Override
        public void rfor2(Consumer<List<KVPair<String,String>>> cs) {
            final List<String> hh = this.header();// 表头
            Function<String,String> trim=(str)->{// remove tailing 0
                String s = (""+str).trim();
                Matcher m = Pattern.compile("(.+)\\.[\\s0]+$").matcher(s);
                if(m.matches()) s = m.group(1);
                return s;
            };
            // 从第一行开始，取消掉了表头
            for(int i=1;i<this.height();i++) {
                List<String> ll = this.lrow(i);// 行数据
                cs.accept( Stream.iterate(0, j->j+1)
                    .limit(ll.size()).map(j->new KVPair<String,String>(
                        hh.get(j),trim.apply(ll.get(j))))
                    .collect(Collectors.toList()) );
            }//for
        }
        
        /**
         *  删除指定 行的数据
         * @param i 行号，从0开始
         * @return
         */
        public StrMatrix removeRow(int i){
            AtomicInteger ai = new AtomicInteger(i);
            return new StrMatrix(this.filter(e->ai.getAndIncrement() != i));
        }
        
        /**
         * 更具字符串矩阵创建 StrMatrix
         * @param mx 字段穿矩阵
         * @return
         */
        public static StrMatrix of(DataMatrix<String>mx) {
            return new StrMatrix(mx);
        }
    
    }

    /**
     * 表示一个平面的额区的矩形区域 <br>
     * 
     * x0,y0    ----------------    x0,y1 <br>
     *   |                              | <br>
     *   |                              | <br>
     *   |                              | <br>
     *   |                              | <br>
     *   |                              | <br>
     *   |                              | <br>
     * x1,y0    ----------------    x1,y1 <br>
     * @author admin,编号从01开始
     *
     */
    public static class RangeDef {
        
        /**
         * 构造函数
         */
        public RangeDef() {
            super();
        }
        
        /**
         * 构造函数
         * @param x0
         * @param y0
         * @param x1
         * @param y1
         */
        public RangeDef(Integer x0, Integer y0, Integer x1, Integer y1) {
            super();
            this._x0 = x0;
            this._y0 = y0;
            this._x1 = x1;
            this._y1 = y1;
        }
        
        public RangeDef(int x0, int y0, int x1, int y1) {
            super();
            this._x0 = x0;
            this._y0 = y0;
            this._x1 = x1;
            this._y1 = y1;
        }
        public int x0() {
            return _x0;
        }
        public void x0(int x0) {
            this._x0 = x0;
        }
        public int y0() {
            return _y0;
        }
        public void y0(int y0) {
            this._y0 = y0;
        }
        public int x1() {
            return _x1;
        }
        public void x1(int x1) {
            this._x1 = x1;
        }
        public int y1() {
            return _y1;
        }
        public void y1(int y1) {
            this._y1 = y1;
        }
        
        @Override
        public String toString() {
            return "Range [x0=" + _x0 + ", y0=" + _y0 + ", x1=" + _x1 + ", y1=" + _y1 + "]";
        }
        
        private Integer _x0;// 左上角第一个单元格水平坐标
        private Integer _y0;// 右上角第一个单元格垂直坐标
        private Integer _x1;// 右下角第一个单元格垂直坐标
        private Integer _y1;// 右下角第一个单元格垂直坐标
    }
    
    /**
     * 简单的EXCEL操作类，这个从PTRACE中ExcelXlsx 分支过来的一个 只读的简化版
     * 我把IDFrame 简化成了StrMatrix
     * 
     * Sheet2!B2:D10 的含义就是 选择sheet2的区域 B2：D10，在EXCEL中，数据的范围叫做RANGE
     * 是按照 左上角右下角的坐标进行定位的，坐标采用列名+行名进行定义，列名用英文字母标识（从A开始，26进制），行名阿拉伯数组标识，
     * 
     * @author admin
     *
     */
    public static class SimpleExcel {
        
        /**
         * 简单的EXCEL文件
         */
        public SimpleExcel(){
            
        }
        
        /**
         * 简单的EXCEL, try to load a excel file
         * @param filename
         */
        public SimpleExcel(String filename){
            this.load(filename);
        }
        
        /**
         * 
         * @param filename excel 文件
         * @param readonly 是否只读
         */
        public SimpleExcel(String filename,boolean readonly){
            this.load(filename,readonly);
        }
        
        /**
         * 简单的EXCEL, try to load a excel file
         * @param filename
         */
        public SimpleExcel(File file){
            this.load(file,true);
        }
        
        /**
         * 加载一个EXCEL的文件
         * 
         * @param path
         * @throws IOException 
         * @throws FileNotFoundException 
         */
        public void load(String path){
            load(path,true);
        }
        
        /**
         * 不支持读写并存
         * @param file 文件对象
         * @param readonly 是否只读
         */
        public void load(File file,boolean readonly) {
            try {
                if(this.workbook!=null) this.workbook.close();
                String fullpath = file.getAbsolutePath();
                String ext = extensionpicker(fullpath).toLowerCase();
                try { if(!ext.equals("xlsx") && !ext.equals("xls")) throw new Exception("数据格式错误,文件需要xls 或者 xlsx"); } 
                catch(Exception e) { e.printStackTrace(); return; }// try
                if(file.exists() && readonly) {// 文件只读
                    workbook = ext.equals("xlsx") ? new XSSFWorkbook(fullpath) : new HSSFWorkbook(new FileInputStream(file));
                }else{
                    workbook = ext.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
                }
                evaluator = workbook.getCreationHelper() .createFormulaEvaluator();
                this.xlsfile = file;
            } catch (IOException e) {
                e.printStackTrace();
            }// try
        }
        
        /**
         * 带扩展名的 资源加载:只读加载
         * @param inputStream 文件对象
         * @param ext 扩展名
         */
        public void loadWithExtension(InputStream inputStream,String ext) {
            this.loadWithExtension(inputStream, ext, true);
        }
        
        /**
         * 带扩展名的 资源加载
         * @param inputStream 文件对象
         * @param ext 扩展名
         * @param readonly 是否只读
         */
        public void loadWithExtension(InputStream inputStream,String ext,boolean readonly) {
            try {
                if(this.workbook!=null) this.workbook.close();
                
                if(readonly) {// 文件只读
                    workbook = ext.equals("xlsx") ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
                }else{
                    workbook = ext.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
                }
                evaluator = workbook.getCreationHelper() .createFormulaEvaluator();
                this.xlsfile = File.createTempFile("xlsfile-"+System.nanoTime(), ext);
            } catch (IOException e) {
                e.printStackTrace();
            }// try
        }
        
        /**
         * 
         * @param sht
         * @param i
         * @param j
         * @return
         */
        public Cell getOrCreateCell(Sheet sht,int i,int j) {
            Row row = sht.getRow(i);
            if(row==null)row=sht.createRow(i);
            Cell c= row.getCell(j);
            if(c==null) c= row.createCell(j);
            return c;
        }
        
        /**
         *  写入一段数据
         * @param firstCell 第一个单元格
         * @param mx
         */
        public void write(Cell firstCell,int height,int width,List<List<String>> ll) {
            Sheet sht = firstCell.getSheet();
            int start_i = firstCell.getAddress().getRow();// 开始行号
            int start_j = firstCell.getAddress().getColumn();// 开始列号
            Iterator<List<String>> litr = ll.iterator();
            height = Math.min(ll.size(), height);
            for(int i=start_i;i<start_i+height;i++) {
                Iterator<String> itr = litr.next().iterator();
                int j=start_j;
                while(itr.hasNext() && j-start_j<width) getOrCreateCell(sht,i,j++).setCellValue(itr.next());
            }//for
        }
        
        /**
         *  写入一段数据
         * @param firstCell 第一个单元格
         * @param mx
         */
        public void write(Cell firstCell,List<List<String>> ll) {
            this.write(firstCell, Integer.MAX_VALUE,Integer.MAX_VALUE,ll);
        }
        
        /**
         * 
         * @param firstCell 第一个单元格
         * @param mx
         */
        public void write(Cell firstCell,StrMatrix mx) {
            if(firstCell==null||mx==null) {
                 System.out.println("传入参数存在空值");return;
            }
            //System.out.println(mx);
            List<List<String>> ll = new LinkedList<>();
            ll.add(mx.header());
            ll.addAll(mx.lrows());
            int height = mx.height();
            int width = mx.width();
            this.write(firstCell, height, width, ll);
            
        }
        
        /**
         * 写入excel
         * @param shtname sheet 名称
         * @param address 地址名称
         * @param mx 数据矩阵
         */
        public void write(String shtname,String address,String [][]mm) {
            write(shtname,address,new StrMatrix(mm,null));
        }
        
        /**
         * 写入excel
         * @param shtname sheet 名称
         * @param address 地址名称
         * @param mx 数据矩阵
         */
        public void write(String shtname,String address,StrMatrix mx) {
            String ss[] = address.split("[:]+");
            Tuple2<Integer,Integer> tup2 = DataMatrix.address2tuple(ss[0]);
            this.write(getOrCreateCell(this.getOrCreateSheet(shtname), tup2._1,tup2._2), mx);
        }
        
        /**
         * 根据sheet 名称获取或创建sheet
         * @param shtname
         * @return
         */
        Sheet getOrCreateSheet(String shtname) {
            Integer shtid = this.sheetname2shtid(shtname);
            Sheet sht = shtid<0?this.workbook.createSheet(shtname):sheet(shtid);
            return sht;
        }
        
        /**
         * 写入excel
         * @param fulladdress 位置全地址 比如：sheet2!C3
         * @param mm 数据内容
         * @param header 表头
         */
        public void write(String fulladdress,String mm[][],List<String> header) {
            this.write(fulladdress, new StrMatrix(mm,header));
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
         * 写入excel
         * @param fulladdress sheet2!C3
         * @param mm 数据内容
         */
        public void write(String fulladdress,DataMatrix<?> dmx) {
            List<String> header = dmx.header();
            String[][] mm = ooo2sss(dmx.getCells());
            this.write(fulladdress, mm,header);
        }
        
        /**
         * 写入excel
         * @param fulladdress sheet2!C3
         * @param mm 数据内容
         */
        public void write(String fulladdress,String mm[][]) {
            
            int height = mm.length;
            int width = mm[0].length;
            List<List<String>> ll = Arrays.stream(mm).map(e->Arrays.asList(e)).collect(Collectors.toList());
            
            String shtname = null;
            String address = null;
            
            String ss[] = fulladdress.split("[!]+");
            if(ss.length<2) {
                address = fulladdress;
                Sheet sht = this.workbook.createSheet();
                shtname = sht.getSheetName();
            }else{
                shtname = ss[0];
                address = ss[1];
            }
            
            Tuple2<Integer,Integer> tup2 = DataMatrix.address2tuple(address);
            write(this.getOrCreateCell(this.getOrCreateSheet(shtname), tup2._1, tup2._2),height,width,ll);
        }
        
        /**
         * 写入excel
         * @param shtname sheet 名称
         * @param address 地址名称
         * @param mx 数据矩阵
         */
        public void write(String fulladdress,StrMatrix mx) {
            String ss[] = fulladdress.split("[!]+");
            String shtname = null;
            String address = null;
            if(ss.length<2) {
                address = fulladdress;
                Sheet sht = this.workbook.createSheet();
                shtname = sht.getSheetName();
            }else{
                shtname = ss[0];
                address = ss[1];
            }
            write(shtname,address,mx);
        }
        
        /**
         * 数据保存
         * @param file
         */
        public void saveAs(String  filename) {
            this.saveAs(new File(filename));
        }
        
        /**
         * 保存成文件
         * @param file
         */
        public void saveAs(File file) {
            try {
                if(!file.exists()) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.flush();
                    this.workbook.write(fos);
                }else {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.flush();
                    this.workbook.write(fos);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        
        /**
         * 
         */
        public void save() {
            this.saveAs(this.xlsfile);
        }

        /**
         * 加载一个EXCEL的文件
         * 
         * @param path
         * @throws IOException 
         * @throws FileNotFoundException 
         */
        public void load(String path,boolean readonly){
            File file= new File(path);
            this.load(file,readonly);
        }
        
        /**
         * 自动定位数据位置
         * 读取指定sht 的最大可用状态
         * @param sht
         * @param maxSize 检索范文边界
         * @return 数据矩阵
         */
        public StrMatrix autoDetect(Sheet sht,Integer firstRowIndex,Integer maxSize) {
            Tuple2<Integer,Integer> t1= this.lt(sht,firstRowIndex,maxSize);
            Tuple2<Integer,Integer> t2= this.rb(sht,firstRowIndex,maxSize);
            String rangeName = tt2rangedef(t1,t2);// 转换成rangename
            //System.out.println(rangeName);
            return this.range(sht, parse(rangeName));
        }
        
        /**
         * 读取指定sht 的最大可用状态
         * 默认数据首行为标题：
         * @param shtid sheetid 编号从0开始
         * @param maxSize 检索范文边界 
         * @return 可用的  sheetid 页面的数据区域
         */
        public StrMatrix autoDetect(int shtid,Integer firstRowIndex,Integer maxSize) {
            if(shtid>=this.sheets().size())return null;
            return autoDetect(this.sheet(shtid),firstRowIndex,maxSize);
        }
        
        /**
         * 名称转id
         * @param name sheet 名称
         * 默认数据首行为标题：
         * @return 把  sheet名称转换成sheetdi
         */
        public Integer sheetname2shtid(String name) {
            return this.sheets().stream().map(e->e.getSheetName()).collect(Collectors.toList()).indexOf(name);
        }
        

        /**
         * sheetid 转 sheet名称
         * 默认数据首行为标题：
         * @param name sheet 名称
         * @return 把  sheet名称转换成sheetdi
         */
        public String shtid2sheetname(Integer shtid) {
            try {
                return this.sheets().get(shtid).getSheetName();
            }catch(Exception e) {
                return null;
            }
        }
        
        /**
         * 读取指定sht 的最大可用状态
         * 默认数据首行为标题：
         * @param shtid sheetid 编号从0开始
         * @param maxSize 检索范文边界 
         * @return 可用的  sheetid 页面的数据区域
         */
        public StrMatrix autoDetect(String sheetname,Integer firstRowIndex,Integer maxSize) {
            int shtid = this.sheetname2shtid(sheetname);
            if(shtid<0) {
                System.out.println(sheetname+" sheet,不存在！");
                return null;
            }
            return autoDetect(this.sheet(shtid),firstRowIndex,maxSize);
        }
        
        /**
         * 读取指定sht 的最大可用状态
         * 默认数据首行为标题：
         * @param shtid sheetid 编号从0开始
         * @param maxSize 检索范文边界 
         * @return 可用的  sheetid 页面的数据区域
         */
        public StrMatrix autoDetect(String sheetname,Integer maxSize) {
            return this.autoDetect(sheetname,0, maxSize);
        }
        
        /**
         * 读取指定sht 的最大可用状态
         * 默认数据首行为标题：
         * @param shtid sheetid 编号从0开始
         * @param shtid firstRowIndex 编号从0开始
         * @param maxSize 检索范文边界 
         * @return 可用的  sheetid 页面的数据区域
         */
        public StrMatrix autoDetect(String sheetname) {
            return this.autoDetect(sheetname,0, MAX_SIZE);
        }
        
        /**
         * 读取指定sht 的最大可用状态
         * 默认数据首行为标题：
         * @param shtid sheetid 编号从0开始
         * @param shtid firstRowIndex 编号从0开始
         * @param maxSize 检索范文边界 
         * @return 可用的  sheetid 页面的数据区域
         */
        public StrMatrix autoDetect(int shtid,Integer maxSize) {
            return this.autoDetect(shtid,0, maxSize);
        }
        
        /**
         * 读取指定sht 的最大可用状态
         * 默认数据首行为标题：
         * @param shtid sheetid 编号从0开始
         * @param shtid firstRowIndex 编号从0开始
         * @param maxSize 检索范边界  默认的EXCEL。MAX_SIZE 默认为10万
         * @return 可用的  sheetid 页面的数据区域
         */
        public StrMatrix autoDetect(int shtid) {
            return this.autoDetect(shtid,0, MAX_SIZE);
        }
        
        /**
         * 从excel中读取数据矩阵
         * @param <U> 目标矩阵的元素类型
         * @param shtid sheet的id
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public <U> DataMatrix<U> autoDetect(int shtid,Function<String,U> mapper) {
            return this.autoDetect(shtid,0, MAX_SIZE).corece(mapper);
        }
        
        /**
         * 从excel中读取数据矩阵
         * @param <U> 目标矩阵的元素类型
         * @param shtname sheet的名称
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public <U> DataMatrix<U> autoDetect(String shtname,Function<String,U> mapper) {
            return this.autoDetect(shtname,0, MAX_SIZE).corece(mapper);
        }
        
        /**
         * 从excel中读取数据矩阵
         * @param shtname sheet的名称
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public DataMatrix<Double> dblAutoDetect(String shtname) {
            return this.autoDetect(shtname,0, MAX_SIZE).corece(Double::parseDouble);
        }
        
        /**
         * 从excel中读取数据矩阵
         * @param shtname sheet的名称
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public DataMatrix<Double> dblAutoDetect(int shtid) {
            return this.autoDetect(shtid,0, MAX_SIZE).corece(Double::parseDouble);
        }
        
        /**
         * 从excel中读取数据矩阵
         * @param shtname sheet的名称
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public DataMatrix<Integer> intAutoDetect(String shtname) {
            return this.autoDetect(shtname,0, MAX_SIZE).corece(Integer::parseInt);
        }
        
        /**
         * 从excel中读取数据矩阵
         * @param shtname sheet的名称
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public DataMatrix<Integer> intAutoDetect(int shtid) {
            return this.autoDetect(shtid,0, MAX_SIZE).corece(Integer::parseInt);
        }

        /**
         * 从excel中读取数据矩阵
         * @param shtname sheet的名称
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public DataMatrix<Long> lngAutoDetect(String shtname) {
            return this.autoDetect(shtname,0, MAX_SIZE).corece(Long::parseLong);
        }
        
        /**
         * 从excel中读取数据矩阵
         * @param shtname sheet的名称
         * @param mapper 值变换函数
         * @return DataMatrix<U>
         */
        public DataMatrix<Long> lngAutoDetect(int shtid) {
            return this.autoDetect(shtid,0, MAX_SIZE).corece(Long::parseLong);
        }
        
        /**
         * 根据sheet 的名称确定sheet的编号
         * 
         * @param name
         *        sheet 的名称
         * @return name 对应的编号
         */
        public Integer shtid(String name) {
            int shtid = -1;
            
            List<Sheet> shts = sheets();// 获取表单列表
            for (int i = 0; i < sheets().size(); i++) {
                if(shts.get(i)==null)continue;
                String shtname = shts.get(i).getSheetName();
                if (shtname!=null && shtname.equals(name)) {
                    shtid = i;
                    break;
                }//if
            }//forEach
            
            return shtid;
        }
        
        /**
         * 判断cell 是否为空值
         * @param cell 单元格
         */
        public boolean isblank (Cell cell) {
            if(cell==null)return true;
            return (cell.getCellType() == CellType.BLANK) || format(cell).matches("\\s*");
        }
        
        /**
         * 左上  单元的位置，可能为空
         * @param sht
         * @param firstRowIndex 首行索引；从0开始
         * @return
         */
        public Tuple2<Integer,Integer> lt(Sheet sht,Integer firstRowIndex,Integer maxSize) {
            if(sht==null)return null;
            int c1=Integer.MAX_VALUE;int c2=Integer.MAX_VALUE;
            for(int i=firstRowIndex;i<maxSize;i++) {
                Row row = sht.getRow(i);
                if(row==null)continue;
                if(row.getPhysicalNumberOfCells()<1)continue;
                if(c1==Integer.MAX_VALUE)c1=i;
                if(row.getFirstCellNum()<c2)c2 = row.getFirstCellNum();
            }
            if(c1==Integer.MAX_VALUE||c2==Integer.MAX_VALUE) return null;
            //System.out.println("lt:"+c1+","+c2);
            return new Tuple2<Integer,Integer>(c1,c2);
        }
        
        /**
         * 右下
         * @param sht excell sheet
         * @param firstRowIndex 首行索引；从0开始
         * @param maxSize 最大遍历行数
         * @return
         */
        public Tuple2<Integer,Integer> rb(Sheet sht,Integer firstRowIndex,Integer maxSize) {
            int c1=Integer.MIN_VALUE;int c2=Integer.MIN_VALUE;
            if(sht==null)return null;
            for(int i=firstRowIndex;i<maxSize;i++) {
                Row row = sht.getRow(i);
                if(row==null)continue;
                if(row.getPhysicalNumberOfCells()<1)continue;
                c1=i;
                if(row.getLastCellNum()>c2)c2=row.getLastCellNum()-1;
            }
            
            if(c1==Integer.MIN_VALUE || c1==Integer.MIN_VALUE) return null;
            return new Tuple2<Integer,Integer>(c1,c2);
        }
        
        /**
         * 书写单元格
         * 
         * @param i 从0开始
         * @param j 从0开始
         */
        public String strval(int i, int j) {
            return strval(activesht,i,j);
        }
        
        /**
         * 书写单元格
         * 
         * @param i 从0开始
         * @param j 从0开始
         * @param sht 当前的表单
         */
        public String strval(Sheet sht,int i, int j) {
            if(i<0 || j<0) return null;
            
            if(sht==null) {
                System.out.println("未指定表单,sht==null");
                return null;
            }
            
            Row row = sht.getRow(i);
            if (row == null) return "";
            Cell cell = row.getCell(j);
            if (cell == null)
                return "";
            
            return format(cell);
        }
        
        /**
         * 格式化输出结构
         * 
         * @param cell excel的输出结构
         * @return 字符串输出
         */
        public Object evaluate(Cell cell) {
            Object value = null;
            if(cell == null)return null;
            
            if (cell.getCellType() == CellType.STRING) { // 字符串类型
                value = cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) { // 公式处理
                CellValue vv = null; // 公式的值
                try {
                    vv = evaluator.evaluate(cell);// 计算单元格的数值
                }catch(Exception e) {
                    //e.printStackTrace();
                    value = String.valueOf(cell.getRichStringCellValue());
                    return value;
                }
                try {
                    if (DateUtil.isCellDateFormatted(cell)) {// 日期值处理
                        Date date = cell.getDateCellValue();
                        value = sdf.format(date);
                    } else if (vv.getCellType() == CellType.NUMERIC ){
                        value = vv.getNumberValue();
                    } else if (vv.getCellType() == CellType.BOOLEAN ){
                        value = vv.getBooleanValue();
                    } else if (vv.getCellType() == CellType.ERROR ){
                        value = vv.getErrorValue();
                    } else {
                        value = vv.getStringValue();
                    }// 值类型的处理
                } catch (IllegalStateException e) {
                    value = cell.getRichStringCellValue();
                }
            } else if (cell.getCellType() == CellType.NUMERIC) {// 数值得处理
                if (DateUtil.isCellDateFormatted(cell))
                    value = sdf.format(cell.getDateCellValue());
                else
                    value = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.BOOLEAN) {// 布尔值得处理
                value = cell.getBooleanCellValue() ;
            }
            //System.out.println(value);
            return value;
        }
        
        /**
         * 格式化输出结构
         * 
         * @param cell excel的输出结构
         * @return 字符串输出
         */
        public String format(Cell cell) {
            return this.evaluate(cell)+"";
        }
        
        /**
         * 
         * @param i0,i1(inclusive) 行范围 ,从 0开始,包括i1
         * @param i1,j1(inclusive) 列范围,从 0开始,包括 j1
         * @return excel 范围
         */
        public StrMatrix get(int _i0, int _j0, int _i1, int _j1) {
            return get(activesht,_i0,_j0,_i1,_j1);
        }
        
        /**
         * 默认表头
         * @param i0,i1(inclusive) 行范围 ,从 0开始,包括i1
         * @param j0,j1(inclusive) 列范围,从 0开始,包括 j1
         * @param sht 表单数据
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return excel 范围
         */
        public StrMatrix get(Sheet sht,int _i0, int _j0, int _i1, int _j1) {
            DataMatrix<String> cc = this.evaluate(sht, _i0, _j0, _i1, _j1,null,e->e+"");
            String cells[][] = cc.cells;
            List<String> headers = cc.header();
            return new StrMatrix(cells,headers);
        }
        
        /**
         * 即选择那些列数据 这就点类似于 select mapper(hh) from name 这样的数据操作。
         * 
         * 对于有 name 进行标识的excel中的区域给予计算求职。
         * 由于name标识的区域是一个 数据u框，所以可以通过 在hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵 
         *  c1    c2    c3 :列名         ----> ci    cj    ck  [i,j,k是 一个c1,c2,c3...的子集]
         *  a11 a12 a13 ：行数据          ----> b1i    b1j    b1k
         *  a21 a22 a23  ：行数据     ----> b2i    b2j    b2k
         *  ... ... ...             ----> ... ... ...    
         * 
         * @param sht sheet 名
         * @param rangeName 区域名称
         * @param hh 表头名称
         * @param mapper 元素处理函数
         * @return 新数据矩阵
         */
        public <T> DataMatrix<T> evaluate(Sheet sht,String rangeName,List<String> hh,Function<Object,T> mapper) {
            return this.evaluate(sht,parse(rangeName), hh, mapper);
            
        }
        
        /**
         * 即选择那些列数据 这就点类似于 select mapper(hh) from name 这样的数据操作。
         *   对于有 name 进行标识的excel中的区域给予计算求职。
         * 由于name标识的区域是一个 数据u框，所以可以通过 在hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵 
         *  c1    c2    c3 :列名         ----> ci    cj    ck  [i,j,k是 一个c1,c2,c3...的子集]
         *  a11 a12 a13 ：行数据          ----> b1i    b1j    b1k
         *  a21 a22 a23  ：行数据     ----> b2i    b2j    b2k
         *  ... ... ...             ----> ... ... ...    
         * 
         * @param sht 表单名
         * @param rangeName 区域名称
         * @param mapper 元素处理函数
         * @return 新数据矩阵
         */
        public <T> DataMatrix<T> evaluate(Sheet sht,String rangeName,Function<Object,T> mapper) {
            return this.evaluate(sht,parse(rangeName),null, mapper);
        }
        
        /**
         * 对于有 name 进行标识的excel中的区域给予计算求职。
         * 由于name标识的区域是一个 数据u框，所以可以通过 在hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵 
         *  c1    c2    c3 :列名         ----> ci    cj    ck  [i,j,k是 一个c1,c2,c3...的子集]
         *  a11 a12 a13 ：行数据          ----> b1i    b1j    b1k
         *  a21 a22 a23  ：行数据     ----> b2i    b2j    b2k
         *  ... ... ...             ----> ... ... ...    
         *  
         * @param name 区域全名称比如sheet2!A1:B100
         * @param hh 列名序列,即选择那些列数据 这就点类似于 select mapper(hh) from name 这样的数据操作。
         * @param mapper 元素变换
         * @return 重新计算后的新的数据矩阵
         */
        public <T> DataMatrix<T> evaluate(String name,List<String> hh,Function<Object,T> mapper) {
            String names[] = name.split("[!]+");// 多个！视为一个
            Sheet sht = this.sheet(0);
            String rangeName = name;
            // 默认为第一个sheet的区域名
            if(names.length>=2) {
                sht = this.sheet(this.shtid(names[0]));// 获取sheetid
                rangeName=names[1];//选区第二项目作为区域名称
            }//选区第二项目作为区域名称
            return this.evaluate(sht,parse(rangeName),hh, mapper);
        }
        
        /**
         * 对于有 name 进行标识的excel中的区域给予计算求职。
         * 由于name标识的区域是一个 数据u框，所以可以通过 在hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵 
         *  c1    c2    c3 :列名         ----> ci    cj    ck  [i,j,k是 一个c1,c2,c3...的子集]
         *  a11 a12 a13 ：行数据          ----> b1i    b1j    b1k
         *  a21 a22 a23  ：行数据     ----> b2i    b2j    b2k
         *  ... ... ...             ----> ... ... ...    
         *  
         * @param name 区域全名称比如sheet2!A1:B100
         * @param hh 区域全名称比如sheet2!A1:B100
         * @param mapper 元素变换
         * @return 重新计算后的新的数据矩阵
         */
        public <T> DataMatrix<T> evaluate(String name,Function<Object,T> mapper) {
            return this.evaluate(name, null,mapper);
        }
        
        /**
         * 
         * 对于有 name 进行标识的excel中的区域给予计算求职。
         * 由于name标识的区域是一个 数据u框，所以可以通过 在hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵 
         *  c1    c2    c3 :列名         ----> ci    cj    ck  [i,j,k是 一个c1,c2,c3...的子集]
         *  a11 a12 a13 ：行数据          ----> b1i    b1j    b1k
         *  a21 a22 a23  ：行数据     ----> b2i    b2j    b2k
         *  ... ... ...             ----> ... ... ...    
         * 
         * @param i0,i1(inclusive) 行范围 ,从 0开始,包括i1
         * @param j0,j1(inclusive) 列范围,从 0开始,包括 j1
         * @param sht 表单数据
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return 新数据矩阵
         */
        public <T> DataMatrix<T> evaluate(Sheet sht,RangeDef rangedef,List<String> hh,Function<Object,T> mapper) {
            return this.evaluate(sht, rangedef.x0(),rangedef.y0(),rangedef.x1(),rangedef.y1(), hh, mapper);
            
        }
        
        /**
         *  对于有 name 进行标识的excel中的区域给予计算求职。
         * 由于name标识的区域是一个 数据u框，所以可以通过 在hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵 
         *  c1    c2    c3 :列名         ----> ci    cj    ck  [i,j,k是 一个c1,c2,c3...的子集]
         *  a11 a12 a13 ：行数据          ----> b1i    b1j    b1k
         *  a21 a22 a23  ：行数据     ----> b2i    b2j    b2k
         *  ... ... ...             ----> ... ... ...    
         * 
         * @param i0,i1(inclusive) 行范围 ,从 0开始,包括i1
         * @param j0,j1(inclusive) 列范围,从 0开始,包括 j1
         * @param sht 表单数据
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return excel 范围
         */
        @SuppressWarnings("unchecked")
        public <T> DataMatrix<T> evaluate(Sheet sht,int _i0, int _j0, int _i1, int _j1,List<String> hh,Function<Object,T> mapper) {
            if(sht==null) { System.out.println("指定的sheet为空,无法获得表单数据");return null;}
            int i0 = Math.min(_i0, _i1);
            int i1 = Math.max(_i0, _i1);
            int j0 = Math.min(_j0, _j1);
            int j1 = Math.max(_j0, _j1);
            
            int offset = hh == null ? 1:0;// 数据从哪一行开始
            Object[][] mm = new Object[(i1 - i0 + 1-offset)][(j1 - j0 + 1)];
            if(mm.length<=0||mm[0]==null || mm[0].length<=0) {
                System.out.println("数据矩阵的行数为空，没有数据！");
                return null;
            }
            List<String> aa = new ArrayList<>(mm[0].length);
            Set<Class<?>> classes = new HashSet<>();
            for (int i = i0; i <= i1-offset; i++) { // 数据尾行需要去掉offset部分因为这些跳过了
                for (int j = j0; j <= j1; j++) {//当hh==null的时候数据要偏移一行
                    if(i==i0)aa.add(this.strval(sht,i, j));// 记录首行
                    Cell c = null;// 单元格
                    try {
                        c= sht.getRow(i+offset).getCell(j);// 获取产品单元格,注意含有行偏移值：这里是 i+offset
                        T value = mapper.apply(this.evaluate(c));// 跳过offset行;
                        mm[i - i0][j - j0] = value;// 计算矩阵数值
                        if(value!=null)classes.add(value.getClass());
                    }catch(Exception e) {
                        e.printStackTrace();
                        System.out.println("error on ("+i+","+j+"),"+
                            (sht.getRow(i)==null?"行对象:'"+sht.getRow(i)+"为空":
                            "行对象或者cell单元格异常,请指定有效的EXCEL数据范围（或是EXCEL自行判断的数据范围有错误）！"));
                    }// try
                }// for j
            }//for i
            
            if(hh==null) {// 使用第一行作为表头
                hh = aa;// 默认的第一行作表头
            }else {// 使用指定表头
                String[] nn=hh.toArray(new String[0]);
                int size = hh.size();
                hh = Stream.iterate(1,i->i+1)
                    .limit(mm[0].length).map(e->size>=e?nn[e-1]:("col"+e))
                    .collect(Collectors.toList());
            }
            Class<?>cls = null;// 获取矩阵的数据分类
            if(classes.size()>0) {cls = classes.iterator().next();
                if(classes.size()>1)System.out.println("warnnings:矩阵中出现不同类别:"+classes+",取用类别:"+classes.iterator().next());
            }
            int m = mm.length; int n = mm[0].length;// 表列宽度
            T[][] tt =(T[][]) Array.newInstance((Class<T>)cls, mm.length,mm[0].length);
            for(int i=0;i<m;i++)for(int j=0;j<n;j++)try{tt[i][j]=(T)mm[i][j];}catch(Exception e) {e.printStackTrace();}
            return new DataMatrix<T>(tt,hh);
        }
        
        /**
         * 默认表头
         * 读取指定区域的数据内容
         * @param rangedef 数据区域
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return 数据区域的数据内容
         */
        public StrMatrix range(RangeDef rangedef){
            return range(activesht,rangedef,null);
        }
        
        /**
         * 读取指定区域的数据内容
         * @param rangedef 数据区域
         * @return 数据区域的数据内容
         */
        public StrMatrix range(RangeDef rangedef,List<String> hh){
            return range(activesht,rangedef,hh);
        }
        
        /**
         * 默认表头
         * 读取指定区域的数据内容
         * @param rangedef 数据区域
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return 数据区域的数据内容
         */
        public StrMatrix range(Sheet sht,RangeDef rangedef) {
            return range(sht,rangedef,null);
        }
        
        /**
         * 读取指定区域的数据内容
         * @param rangedef 数据区域
         * @return 数据区域的数据内容
         */
        public StrMatrix range(Sheet sht,RangeDef rangedef,List<String> hh) {
            if(rangedef==null) {
                System.out.println("无法获得rangedef数据,rangedef 为空数据");
                return null;
            }
            
            DataMatrix<String> cc = this.evaluate(sht,rangedef.x0(),rangedef.y0(),rangedef.x1(),rangedef.y1(),hh,e->e+"");
            if(cc==null)return null;
            String cells[][] = cc.cells;
            List<String> headers = cc.header();
            return new StrMatrix(cells,headers);
        }
        
        /**
         * 默认表头
         * @param name 获得数据区域,比如 Sheet2!A1:B10
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return excel 范围数据
         */
        public StrMatrix range(String name) {
            return range(name,(List<String>)null);
        }
        
        
        /* * 
         * @param name 获得数据区域
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return excel 范围数据
         */
        public StrMatrix range(String name,String[] hh) {
            if(hh==null)return range(name,(List<String>)null);
            return range(name,Arrays.asList(hh));
        }
        
        /**
         * 
         * @param name 获得数据区域：range 全名用比如sheet1!A1:B10
         * @param hh null,表示数据中包含表头,第一行就是表头
         * @return excel 范围数据
         */
        public StrMatrix range(String name,List<String> hh) {
            if(name.contains("!")) {// 名称中包含有表单名
                String ss[] = name.split("!");
                if(ss.length>1) {
                    String sheetname = ss[0].trim();
                    String rangeName = ss[1].trim();
                    // 按表单进行区域rangedef内容的获取
                    return range(sheetname,rangeName);
                }else {// 名称不包含表单名
                    System.out.println("非法表单名称:"+name);
                    return null;
                }//if
            }//if name.contains("!")
            
            // 设置默认的额sheet数据
            if(activesht==null) activesht = selectSheet(0);
            
            return range(parse(name),hh);
        }
        
        /**
         * 读取sheet :
         * @param shtname sheet 名字
         * @param rangeName 区域名称
         * @return IDFrame
         */
        public StrMatrix range(String shtname,String rangeName) {
            Integer shtid = this.shtid(shtname);
            if(shtid==null || shtid<0) {
                System.out.println("不存在表单:\""+shtname+"\"");
                return null;
            }
            return range(shtid,rangeName);
        }
        
        /**
         * 读取sheet 
         * @param shtid sheet 编号从0开始
         * @param rangeName 表单的名称 比如：A1:B10
         * @return 获得指定表单的值
         */
        public StrMatrix range(int shtid,String rangeName) {
            Sheet sht = sheet(shtid);// 获得表单名称
            if(sht==null) {
                System.out.println("不存在编号为"+
                    shtid+"sheet,不予读取任何数据");
                return null;
            }//if
            
            return range(sht,parse(rangeName));
        }
        
        /**
         * 获取表单数据
         * @param i 表单id 从0开始
         * @return
         */
        public Sheet sheet(int i) {
            Sheet sht = null;
            try {
                sht = this.workbook.getSheetAt(i);
            }catch(Exception e){e.printStackTrace();}
            return sht;
        }
        
        /**
         * 选择制定位置的sheet
         * 
         * @param i sheet 的编号
         * @return 选择表单
         */
        public Sheet selectSheet(int i) {
            if(i<0 || i>=this.workbook.getNumberOfSheets()) {
                System.out.println("sheet["+i+"] 编号非法!");
                return null;
            }
            this.workbook.setActiveSheet(i);
            this.activesht = this.workbook.getSheetAt(
                this.workbook.getActiveSheetIndex());
            
            return this.activesht;
        }
        
        /**
         * 所有的Sheet 列表
         */
        public List<Sheet> sheets() {
            // 便利workbook获取sheet 列表
            return LIST(workbook.sheetIterator()).stream()
                .map(e -> (Sheet) e)
                .collect(Collectors.toList());
        }

        /**
         * 文件关闭
         */
        public void close() {
            try { workbook.close();} 
            catch (IOException e) {e.printStackTrace();}// try
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
         * 
         * @param tuple 顶坐标 x(水平),y （垂直）
         * @return 字符串
         */
        public static String tuple2name (Tuple2<Integer,Integer> tuple) {
            return StrMatrix.excelname(tuple._2)+""+tuple._1;
        }

        /**
         * 包坐标索引转换成 EXCEL 区域名称，比如 (0,0),(1,1) 转换成 A1:B2
         * @param tuple1:左上角单元的坐标索引,从0，开始：（0，0）表示第一个单元格
         * @param tuple2：右下角的坐标索引，从0开始，（0，0）表示第一个单元格
         * @return
         */
        public static String tt2rangedef (Tuple2<Integer,Integer> tuple1,Tuple2<Integer,Integer> tuple2) {
            if(tuple1==null || tuple2==null)return null;
            tuple1._1+=1;tuple2._1+=1;// 调整行列坐标为从1开始:因为excel的行名是从1开始
            String s1 = tuple2name(tuple1);
            String s2 = tuple2name(tuple2);
            return s1+":"+s2;
        }

        /**
         * 
         * @param itr
         * @return
         */
        public static <T> List<T> LIST(Iterator<T> itr){
            List<T> ll = new LinkedList<T>();
            while(itr.hasNext())ll.add(itr.next());
            return ll;
        }

        /**
         * 把excel 名称转换成位置坐标
         * 
         * @param rangeName A1:B13这样的字符串
         * @return 名称转rangedef
         */
        public static RangeDef parse(final String rangeName) {
            String pattern = "(([A-Z]+)\\s*(\\d+))(\\s*:\\s*(([A-Z]+)\\s*(\\d+)))?";
            if(rangeName==null)return null;
            String name = rangeName.toUpperCase(); // 转换成大写形式
            Matcher matcher = Pattern.compile(pattern)
                .matcher(name);
            
            RangeDef rangedef = null;// 数值区域
            if(matcher.find()) {
                String y0 = matcher.group(2).replaceAll("\\s*", "");
                String x0 = matcher.group(3).replaceAll("\\s*", "");
                String y1 = matcher.group(6).replaceAll("\\s*", "");
                String x1 = matcher.group(7).replaceAll("\\s*", "");
                Integer ix0 = DataMatrix.excelname2i(x0);
                Integer iy0 = DataMatrix.excelname2i(y0);
                Integer ix1 = DataMatrix.excelname2i(x1);
                Integer iy1 = DataMatrix.excelname2i(y1);
                rangedef = new RangeDef(ix0,iy0,ix1,iy1);
            }// if
            
            return rangedef; // 数据区域内容
        }

        public static Integer MAX_SIZE = 1000000;//  最大处理行数
        private Workbook workbook = new XSSFWorkbook();
        private Sheet activesht = null;// 当前的对象
        private SimpleDateFormat sdf = 
            new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        private static FormulaEvaluator evaluator;
        private File xlsfile = null;
    }
}
