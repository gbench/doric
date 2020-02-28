package gbench.commonApp.data;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import gbench.common.fs.XlsFile.DataMatrix;
import gbench.common.tree.LittleTree.IRecord;

import static gbench.common.tree.LittleTree.IRecord.*;
import static gbench.common.tree.LittleTree.*;
import static gbench.common.tree.LittleTree.IRecord.REC;

/**
 * DataMatrix 的应用实例
 * 
 * @author gbench
 *
 */
public class DataMatrixApp {
    /**
     * 
     * @param <T>    矩阵元素的类型
     * @param stream 源数据流程
     * @param tclass 矩阵元素的类型的class
     * @return DataMatrix<T>
     */
    @SuppressWarnings("unchecked")
    public static <T> DataMatrix<T> matrix(final Stream<IRecord> stream, final Class<T> tclass) {
        final Function<IRecord, LinkedHashMap<String, T>> final_row_mapper = e -> {
            final Class<T> final_tclass = tclass == null ? (Class<T>) Object.class : tclass;
            var mm = e.toLhm(final_tclass);
            if (tclass == Object.class) {
                var new_mm = new LinkedHashMap<String, Object>();
                new_mm.putAll(mm);
                mm = (LinkedHashMap<String, T>) new_mm;
            } // if
            return mm;
        };
        return DataMatrix.of(stream, final_row_mapper, tclass);
    }

    /**
     * 
     * @param stream 源数据流程
     * @param tclass 矩阵元素的类型的class
     * @return DataMatrix<T>
     */
    public static DataMatrix<Object> matrix(final Stream<IRecord> stream) {
        return DataMatrix.of(stream, (IRecord e) -> e.toLhm());
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param recs 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<T> DMX(Class<T> tclass,Stream<IRecord> recs){
        return recs.collect(tmc(tclass));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param recs 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<T> DMX(Class<T>tclass,IRecord...recs){
        return Stream.of(recs).collect(tmc(tclass));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param line 数据的行表示：比如 1,2;3,4 表示<br>
     * 1 2<br>
     * 3 4
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<T> DMX(Class<T> tclass,String line){
        final var recs = Stream.of(line.split("[\n;]+")).map(IRecord::STRING2REC);
        return recs.collect(tmc(tclass));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<Double> DBLMX(IRecord...rows){
        return Stream.of(rows).collect(tmc(Double.class));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<Double> DBLMX(Stream<IRecord> stream){
        return stream.collect(tmc(Double.class));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<Object> OBJMX(IRecord...rows){
        return Stream.of(rows).collect(tmc(Object.class));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<Object> OBJMX(Stream<IRecord> stream){
        return stream.collect(tmc(Object.class));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<String> STRMX(IRecord...rows){
        return Stream.of(rows).collect(tmc(String.class));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<String> STRMX(Stream<IRecord> stream){
        return stream.collect(tmc(String.class));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<Integer> INTMX(IRecord...rows){
        return Stream.of(rows).collect(tmc(Integer.class));
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param rows 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static DataMatrix<Integer> INTMX(Stream<IRecord> stream){
        return stream.collect(tmc(Integer.class));
    }

    /**
     * 把recs转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param recs 列数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<T> DMX2(Class<T> tclass,Stream<IRecord> recs){
        return recs.collect(tmc(tclass)).transpose();
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param recs 行数据列表
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    @SuppressWarnings("unchecked")
    public static <T> DataMatrix<T> DMX2(Class<T> tclass,Object ...ccc){
        IRecord mm = REC(ccc);
        return DMX(tclass,(Stream<IRecord>)(Object)mm.values().stream()).transpose(mm.keys());
    }

    /**
     * 把recs 转换成 数据矩阵：构建一个列矩阵
     * @param <T> 矩阵的元素的类型
     * @param ccc name0,colum1,name1,colum1, ..., 名称与列向量对应的数据矩阵。
     * @return DataMatrix<Double>
     */
    public static DataMatrix<Object> OBJMX2(Object ...ccc){
        return DMX2(Object.class,ccc);
    }

    /**
     * 把recs 转换成 数据矩阵：构建一个列矩阵
     * @param <T> 矩阵的元素的类型
     * @param ccc name0,colum1,name1,colum1, ..., 名称与列向量对应的数据矩阵。
     * @return DataMatrix<Double>
     */
    public static DataMatrix<Double> DBLMX2(Object ...ccc){
        return DMX2(Double.class,ccc);
    }

    /**
     * 把recs 转换成 数据矩阵：构建一个列矩阵
     * @param <T> 矩阵的元素的类型
     * @param ccc name0,colum1,name1,colum1, ..., 名称与列向量对应的数据矩阵。
     * @return DataMatrix<Double>
     */
    public static DataMatrix<Integer> INTMX2(Object ...ccc){
        return DMX2(Integer.class,ccc);
    }

    /**
     * 把recs 转换成 数据矩阵：构建一个列矩阵
     * @param <T> 矩阵的元素的类型
     * @param ccc name0,colum1,name1,colum1, ..., 名称与列向量对应的数据矩阵。
     * @return DataMatrix<Double>
     */
    public static DataMatrix<Long> LNGMX2(Object ...ccc){
        return DMX2(Long.class,ccc);
    }

    /**
     * 把recs 转换成 数据矩阵：构建一个列矩阵
     * @param <T> 矩阵的元素的类型
     * @param ccc name0,colum1,name1,colum1, ..., 名称与列向量对应的数据矩阵。
     * @return DataMatrix<Double>
     */
    public static DataMatrix<String> STRMX2(Object ...ccc){
        return DMX2(String.class,ccc);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param <T>        矩阵元素的类型
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @param tclass     矩阵元素的类型的class
     * @return Collector<IRecord,IRecord,DataMatrix<T>>
     */
    public static <T> Collector<IRecord, List<IRecord>, DataMatrix<T>> mxclc(Function<IRecord, IRecord> row_mapper,
            Class<T> tclass) {

        Supplier<List<IRecord>> supplier = () -> new LinkedList<IRecord>();
        BiConsumer<List<IRecord>, IRecord> accumulator = (aa, a) -> aa.add(row_mapper.apply(a));
        BinaryOperator<List<IRecord>> combiner = (aa, bb) -> {
            aa.addAll(bb);
            return aa;
        };
        Function<List<IRecord>, DataMatrix<T>> finisher = ll -> matrix(ll.stream(), tclass);

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<T>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Double>> dblmxclc(
            Function<IRecord, IRecord> row_mapper) {

        Supplier<List<IRecord>> supplier = () -> new LinkedList<IRecord>();
        BiConsumer<List<IRecord>, IRecord> accumulator = (aa, a) -> aa.add(row_mapper.apply(a));
        BinaryOperator<List<IRecord>> combiner = (aa, bb) -> {
            aa.addAll(bb);
            return aa;
        };
        Function<List<IRecord>, DataMatrix<Double>> finisher = ll -> matrix(ll.stream(), Double.class);

        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<T>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Integer>> intmxclc(
            Function<IRecord, IRecord> row_mapper) {

        Supplier<List<IRecord>> supplier = () -> new LinkedList<IRecord>();
        BiConsumer<List<IRecord>, IRecord> accumulator = (aa, a) -> aa.add(row_mapper.apply(a));
        BinaryOperator<List<IRecord>> combiner = (aa, bb) -> {
            aa.addAll(bb);
            return aa;
        };
        Function<List<IRecord>, DataMatrix<Integer>> finisher = ll -> matrix(ll.stream(), Integer.class);

        return Collector.of(supplier, accumulator, combiner, finisher);
    }
    
    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<T>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Long>> lngmxclc(Function<IRecord, IRecord> row_mapper) {

        Supplier<List<IRecord>> supplier = () -> new LinkedList<IRecord>();
        BiConsumer<List<IRecord>, IRecord> accumulator = (aa, a) -> aa.add(row_mapper.apply(a));
        BinaryOperator<List<IRecord>> combiner = (aa, bb) -> {
            aa.addAll(bb);
            return aa;
        };
        Function<List<IRecord>, DataMatrix<Long>> finisher = ll -> matrix(ll.stream(), Long.class);

        return Collector.of(supplier, accumulator, combiner, finisher);
    }
    
    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<Object>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Object>> objmxclc(
            final Function<IRecord, IRecord> row_mapper) {
    
        final Function<IRecord, IRecord> final_row_mapper = row_mapper == null ? e -> e : row_mapper;
        final Supplier<List<IRecord>> supplier = () -> new LinkedList<IRecord>();
        final BiConsumer<List<IRecord>, IRecord> accumulator = (aa, a) -> aa.add(final_row_mapper.apply(a));
        final BinaryOperator<List<IRecord>> combiner = (aa, bb) -> {
            aa.addAll(bb);
            return aa;
        };
        final Function<List<IRecord>, DataMatrix<Object>> finisher = ll -> matrix(ll.stream(), Object.class);
    
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<String>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<String>> strmxclc(
            Function<IRecord, IRecord> row_mapper) {
    
        Supplier<List<IRecord>> supplier = () -> new LinkedList<IRecord>();
        BiConsumer<List<IRecord>, IRecord> accumulator = (aa, a) -> aa.add(row_mapper.apply(a));
        BinaryOperator<List<IRecord>> combiner = (aa, bb) -> {
            aa.addAll(bb);
            return aa;
        };
        Function<List<IRecord>, DataMatrix<String>> finisher = ll -> matrix(ll.stream(), String.class);
    
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @return Collector<IRecord,IRecord,DataMatrix<Object>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Object>> objmxclc() {
    
        return objmxclc(null);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<Object>>
     */
    public static <T> Collector<IRecord, List<IRecord>, DataMatrix<T>> tmxclc(
            final Function<IRecord, IRecord> row_mapper,Class<T>tclass) {
    
        final Function<IRecord, IRecord> final_row_mapper = row_mapper == null ? e -> e : row_mapper;
        final Supplier<List<IRecord>> supplier = () -> new LinkedList<IRecord>();
        final BiConsumer<List<IRecord>, IRecord> accumulator = (aa, a) -> aa.add(final_row_mapper.apply(a));
        final BinaryOperator<List<IRecord>> combiner = (aa, bb) -> {
            aa.addAll(bb);
            return aa;
        };
        final Function<List<IRecord>, DataMatrix<T>> finisher = ll -> matrix(ll.stream(), tclass);
    
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * 一个IRecord的Collector:dblmxclc 的别名
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<T>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Double>> DMC(Function<IRecord, IRecord> row_mapper) {
        return dblmxclc(row_mapper);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<T>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Integer>> IMC(
            Function<IRecord, IRecord> row_mapper) {
        return intmxclc(row_mapper);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<T>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Long>> LMC(Function<IRecord, IRecord> row_mapper) {
        
        return lngmxclc(row_mapper);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<String>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<String>> SMC(
            Function<IRecord, IRecord> row_mapper) {
        return strmxclc(row_mapper);
    }

    /**
     * 一个IRecord的Collector
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<Object>>
     */
    public static <T> Collector<IRecord, List<IRecord>, DataMatrix<T>> TMC(
            final Function<IRecord, IRecord> row_mapper,Class<T>tclass){
        return tmxclc(row_mapper,tclass);
    }

    /**
     * 一个IRecord的Collector:objmxclc 的别名
     * 
     * @param row_mapper 行变换器，把记录的值装换成统一的数据类型 以方便作为数组进行存储:对源数据流
     *                   变换成中间的T为值类型IRecord的流对象。
     * @return Collector<IRecord,IRecord,DataMatrix<Object>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Object>> OMC(
            final Function<IRecord, IRecord> row_mapper) {
        return objmxclc(row_mapper);
    }

    /**
     * 一个IRecord的Collector:OMC
     * 
     * @return Collector<IRecord,IRecord,DataMatrix<Object>>
     */
    public static Collector<IRecord, List<IRecord>, DataMatrix<Object>> OMC() {

        return objmxclc(null);
    }
    
    /**
     * 生成全 T 类型的向量
     * @param <T> 元素类型
     * @param n 向量的长度
     * @param t 向量元素
     * @return DataMatri <T>
     */
    public static <T extends Number> DataMatrix<T> vones(T n){
        @SuppressWarnings("unchecked")
        final T one = P(1).toTarget(n!=null?(Class<T>)n.getClass():(Class<T>)(Object)Object.class);
        return VT(RPTA(n.intValue(),one));
    }
    
    /**
     * 生成全 T 类型的向量 :全 one 的向量
     * @param <T> 元素类型
     * @param n 向量的长度
     * @param one 向量元素：one元 ，什么代表1
     * @return DataMatri <T>
     */
    public static <T> DataMatrix<T> vones(int n,T one){
        return VT(RPTA(n,one));
    }
    
    /**
     * 生成全 T 类型的向量 :全 one 的行向量
     * @param <T> 元素类型
     * @param n 向量的长度
     * @param one 向量元素：one元 ，什么代表1
     * @return DataMatri <T>
     */
    public static <T> DataMatrix<T> hones(int n,T one){
        return VT(RPTA(n,one)).tp();
    }
    
    /**
     * 生成全 T 类型的向量:行向量
     * @param <T> 元素类型
     * @param n 向量的长度: one元默认为1
     * @return DataMatri <T>
     */
    public static <T extends Number> DataMatrix<T> hones(T n){
        return vones(n).tp();
    }
    
    /**
     * 生成一个T类型的向量:VT 向量的别名
     * @param <T> 元素类型
     * @param oo 元素数据
     * @return DataMatrix<T>
     */
    @SuppressWarnings("unchecked")
    public static <T> DataMatrix<T> V(T ...oo){
        return VT(oo);
    }

    /**
     * 生成一个学姐级数向量
     * @param <T> 元素的类型
     * @param size 向量长度
     * @param fx : 向量元素生成函数,x->u,x 从0开始知道 n-1
     * @return DataMatrix<T> 一维列向量
     */
    @SuppressWarnings("unchecked")
    public static <T> DataMatrix<T> V(Number size,Function<Integer,T> fx){
        final var uu = NATS(size.intValue()).map(e->e.intValue()).map(fx).collect(Collectors.toList());
        final var uclass = uu.stream().filter(e->e!=null)
            .map(e->(Class<T>)e.getClass()).findAny().orElse((Class<T>)Object.class);
        return VT(uu.toArray(n->(T[])Array.newInstance(uclass,n)));
    }

    /**
     * 生成一个T类型的向量
     * @param <T> 元素类型
     * @param oo 元素数据
     * @return DataMatrix<T>
     */
    @SuppressWarnings("unchecked")
    public static <T> DataMatrix<T> VT(T ...oo){
        final Class<T> clazz = Arrays.stream(oo).filter(e->e!=null)
            .map(e->(Class<T>)e.getClass()).findAny().get();
        final var rec = IRecord.A2REC(oo);
        return DMX(clazz,Stream.of(rec)).transpose();
    }

    /**
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param line 数据的行表示：比如 1,2;3,4 表示<br>
     * 1 2<br>
     * 3 4
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<T> VEC(Class<T> tclass,String line){
        final var recs = Stream.of(line.split("[\n;]+")).map(IRecord::STRING2REC);
        return recs.collect(tmc(tclass)).transpose();
    }

    /**
     * double 向量
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param line 数据的行表示：比如 1,2;3,4 表示<br>
     * 1 2<br>
     * 3 4
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<Double> dblvec(Number ...oo){
        final var rec = IRecord.A2REC(Stream.of(oo).map(Number::doubleValue).toArray());
        return DMX(Double.class,Stream.of(rec)).transpose();
    }

    /**
     * double 向量:把一个Record 变成一个向量
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param line 数据的行表示：比如 1,2;3,4 表示<br>
     * 1 2<br>
     * 3 4
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<Double> dblvec(IRecord rec){
        return DMX(Double.class,Stream.of(rec)).transpose();
    }

    /**
     * double 向量
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param line 数据的行表示：比如 1,2;3,4 表示<br>
     * 1 2<br>
     * 3 4
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<Double> dblvec(String line){
       return VEC(Double.class,line);
    }

    /**
     * int 向量
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param line 数据的行表示：比如 1,2;3,4 表示<br>
     * 1 2<br>
     * 3 4
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<Integer> intvec(String line){
        return VEC(Integer.class,line);
    }

    /**
     *  long 向量
     * 把recs 转换成 数据矩阵
     * @param <T> 矩阵的元素的类型
     * @param line 数据的行表示：比如 1,2;3,4 表示<br>
     * 1 2<br>
     * 3 4
     * @param tclass 矩阵的元素的类型 类型
     * @return DataMatrix<T>
     */
    public static <T> DataMatrix<Long> lngvec(String line){
        return VEC(Long.class,line);
    }

    /**
     * 对角矩阵
     * @param <T> 元素类型
     * @param oo 对角数据
     * @return 以oo为元素的对角矩阵
     */
    @SuppressWarnings("unchecked")
    public static <T> DataMatrix<T> diag(T[] oo,T zero){
        final Class<T> clazz = Arrays.stream(oo).filter(e->e!=null)
            .map(e->(Class<T>)e.getClass()).findAny().get();
        final var size = oo.length;
        Function<Integer,IRecord> line = i->{
            T[] tt = RPTA(size,zero);
            tt[i]=oo[i];
            return A2REC(tt);
        };
        
        return Stream.iterate(0,i->i<size,i->i+1).map(line).collect(tmc(clazz));
    }
    
    /**
     * 对角矩阵
     * @param <T> 元素类型
     * @param oo 对角数据
     * @return 以oo为元素的对角矩阵
     */
    public static <T> DataMatrix<T> diag(int n,T t,T zero){
        return diag(RPTA(n,t),zero);
    }
    
    /**
     * 对角矩阵
     * @param <T> 元素类型
     * @param oo 对角数据
     * @return 以oo为元素的对角矩阵
     */
    public static <T> DataMatrix<T> diag(int n,T t){
        @SuppressWarnings("unchecked")
        final T zero = P(0).toTarget(t!=null?(Class<T>)t.getClass():(Class<T>)Object.class);
        return diag(RPTA(n,t),zero);
    }
    
    /**
     * 对角矩阵:矩阵的元素的类型根据根据 n 的类型给与确认
     * @param <T> 元素类型
     * @param n 对角矩阵的维度大小
     * @return 以oo为元素的对角矩阵
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> DataMatrix<T> diag(T n){
        final T one = P(1).toTarget(n!=null?(Class<T>)n.getClass():(Class<T>)(Object)Object.class);
        final T zero = P(0).toTarget(n!=null?(Class<T>)n.getClass():(Class<T>)(Object)Object.class);
        return diag(RPTA(n.intValue(),one),zero);
    }

    public static Collector<IRecord, List<IRecord>, DataMatrix<Object>> omc=OMC();
    public static Collector<IRecord, List<IRecord>, DataMatrix<Double>> dmc=DMC(record_identity(Double.class));
    public static Collector<IRecord, List<IRecord>, DataMatrix<Integer>> imc=IMC(record_identity(Integer.class));
    public static Collector<IRecord, List<IRecord>, DataMatrix<Long>> lmc=LMC(record_identity(String.class));
    public static Collector<IRecord, List<IRecord>, DataMatrix<String>> smc=SMC(record_identity(String.class));
    
    /**
     * 提取T结构的矩阵
     * @param <T> 目标元素类型
     * @param tclass
     * @return 任意类型的搜集器
     */
    public static <T> Collector<IRecord, List<IRecord>, DataMatrix<T>> tmc(Class<T>tclass){
        return TMC(record_identity(tclass),tclass);
    }

    /**
     * 带有类型的Record转换
     * @param <T> 目标元素类型
     * @param tclass 目标元素类型的class
     * @return 目标元素类型
     */
    public static <T> Function <IRecord,IRecord> record_identity(Class<T> tclass) {
        return (IRecord e)->{
            var t = e.apply(p->IRecord.rec2obj(REC(p),tclass));
            final var b = t.values().get(0)!=null;
            return b?t:e;
        };
    }
    
    /**
     * 常用的数学函数操作
     * @author gbench
     *
     */
    public static class MathOps {
        
        /**
         * 阶乘函数
         * @param n 整数:
         * @return 阶乘结果
         */
        public static Long fact(Number n) {
            return n.longValue()==0l?1:n.longValue()*fact(n.longValue()-1l);
        }
        
        /**
         * 加法
         * @param <T>
         * @return
         */
        @SuppressWarnings("unchecked")
        public static <T extends Number>  BinaryOperator<T> add(){
            return (T a,T b) ->(T)(Number)(a.doubleValue()+b.doubleValue());
        };
        
        /**
         * 减法
         * @param <T>
         * @return
         */
        @SuppressWarnings("unchecked")
        public static <T extends Number>  BinaryOperator<T> sub(){
            return (T a,T b) ->(T)(Number)(a.doubleValue()-b.doubleValue());
        }
    
        /**
         * 乘法
         * @param <T>
         * @return
         */
        @SuppressWarnings("unchecked")
        public static <T extends Number>  BinaryOperator<T> mul(){
            return (T a,T b) ->(T)(Number)(a.doubleValue()*b.doubleValue());
        };
        
        /**
         * 除法
         * @param <T>
         * @return
         */
        @SuppressWarnings("unchecked")
        public static <T extends Number>  BinaryOperator<T> div(){
            return (T a,T b) ->(T)(Number)(a.doubleValue()/b.doubleValue());
        };
        
        /**
         * 生成一个随机方阵:  Square Random Matrix sqrm
         * @param <T> 方阵元素类型。
         * @param size 方阵尺寸
         * @return 随机方阵
         */
        @SuppressWarnings("unchecked")
        public static <T extends Number> DataMatrix<T> sqrm(T size) {
            var dd = DBLS(size.intValue()*size.intValue());
            final Class<T> cls = (Class<T>)size.getClass();
            final var mm = SQB2(cls,size).get(Arrays.stream(dd)
                .toArray(n->(T[])Array.newInstance(cls,n)));// 随机生成一个矩阵
            return (DataMatrix<T>)mm;
        }
        
        /**
         * m * n 的全零矩阵, zero matrix
         * @param <T> 矩阵中的元素的类型
         * @param size 方阵尺寸
         * @return 全0矩阵
         */
        @SuppressWarnings("unchecked")
        public static <T extends Number> DataMatrix<T> zerom(T m,T n) {
            final Class<T> cls = (Class<T>)m.getClass();
            final var mm = DMB2(cls,m,n).get(RPTS(m.intValue()*n.intValue(),m)
                .toArray(length->(T[])Array.newInstance(cls,length)));// 随机生成一个矩阵
            return (DataMatrix<T>)(mm.minus(mm));
        }
    }

    /**
     * 生成一个矩阵构造器
     * @author gbench
     *
     * @param <T> 矩阵元素类型
     */
    public static class DataMatrixBuilder<T>{
        
        /**
         * 矩阵构造器
         * @param typeClass 元素类型
         * @param m 行数高度
         * @param n 列数宽度
         */
        public DataMatrixBuilder(Class<T>typeClass,final int m,final int n,Boolean byRow) {
            this.height = m;
            this.width = n;
            this.typeClass = typeClass;
            this.byRow = byRow;
        }
        
        /**
         * 矩阵构造器
         * @param typeClass 元素类型
         * @param m 行数高度
         * @param n 列数宽度
         */
        public DataMatrixBuilder(Class<T>typeClass,final int m,final int n) {
            this.height = m;
            this.width = n;
            this.typeClass = typeClass;
            this.byRow = true;
        }
        
        /**
         * 矩阵构造器
         * @param typeClass 元素类型
         * @param m 行数高度
         * @param n 列数宽度
         */
        public DataMatrixBuilder(final int m,final int n) {
            this.height = m;
            this.width = n;
            this.typeClass = null;
            this.byRow =true;
        }
        
        /**
         * 创建矩阵
         * @param dd 矩阵数据元素:digits,doubles
         * @return DataMatrix<T>
         */
        public DataMatrix<Double> dblM(final double ...dd){
            return DataMatrixBuilder.get(Double.class, height, width, byRow,
                Arrays.stream(dd).mapToObj(e->e).toArray(Double[]::new));
        }
        
        /**
         * 创建矩阵
         * @param dd 矩阵数据元素:digits
         * @return DataMatrix<T>
         */
        public DataMatrix<Integer> intM(final int ...dd){
            return DataMatrixBuilder.get(Integer.class, height, width, byRow,
                    Arrays.stream(dd).mapToObj(e->e).toArray(Integer[]::new));
        }
        
        /**
         * 创建矩阵
         * @param dd 矩阵数据元素:digits
         * @return DataMatrix<T>
         */
        public DataMatrix<Long> lngM(final long ...dd){
            return DataMatrixBuilder.get(Long.class, height, width, byRow,
                    Arrays.stream(dd).mapToObj(e->e).toArray(Long[]::new));
        }
        
        /**
         * 创建矩阵
         * @param ooo 矩阵数据元素
         * @return DataMatrix<T>
         */
        public static <U>DataMatrix<U> get(Class<U> typeClass,int height,int width ,boolean byRow,
            @SuppressWarnings("unchecked") final U ...oo){
            final int size =oo.length;
            @SuppressWarnings("unchecked")
            U[][] dd = DataMatrix.newArray(typeClass==null
                ? Stream.of(oo).filter(e->e!=null).findAny()
                        .map(e->(Class<U>)e.getClass()).orElse((Class<U>)Object.class)
                :typeClass,height,width);
            for(int i=0;i<height;i++) { // 列
                for(int j=0;j<width;j++) { // 列
                    int k = byRow // 参照是否按照行排序
                        ? (i*width+j)%size
                        : (j*width+i)%size;
                    dd[i][j]= oo[k]; // 元素设置
                }//j
            }//i
            
            return new DataMatrix<>(dd);
        }
        
        /**
         * 创建矩阵
         * @param ooo 矩阵数据元素
         * @return DataMatrix<T>
         */
        public DataMatrix<T> get(@SuppressWarnings("unchecked") final T ...oo){
           return DataMatrixBuilder.get(typeClass, height, width, byRow, oo);
        }

        public Class<T> getTypeClass() {
            return typeClass;
        }

        public Boolean getByRow() {
            return byRow;
        }


        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }
        
        final private int height;
        final private int width;
        final private Class<T> typeClass;
        final private Boolean byRow;
    }
    
    /**
     * 生成一个按照行顺序的矩阵
     * @param <T>  元素类型
     * @param typeClass 元素类型
     * @param m 行数高度
     * @param n 列数宽度
     * @return DataMatrixBuilder<T>
     */
    public static <T> DataMatrixBuilder<T> DMB(Class<T>typeClass,final Number m,final Number n){
       return new DataMatrixBuilder<T>(typeClass,m.intValue(),n.intValue());
    }
    
    /**
     * 生成一个按照列顺序的矩阵
     * @param <T> 元素类型
     * @param typeClass 元素类型
     * @param m 行数高度
     * @param n 列数宽度
     * @return DataMatrixBuilder<T>
     */
    public static <T> DataMatrixBuilder<T> DMB2(Class<T>typeClass,final Number m,final Number n){
       return new DataMatrixBuilder<T>(typeClass,m.intValue(),n.intValue(),false);
    }

    /**
     * 生成一个按照行顺序的方阵:SQuareBuilder
     * @param <T>  元素类型
     * @param typeClass 元素类型
     * @param n 方阵维数
     * @return DataMatrixBuilder<T>
     */
    public static <T> DataMatrixBuilder<T> SQB(Class<T>typeClass,final Number n){
       return new DataMatrixBuilder<T>(typeClass,n.intValue(),n.intValue());
    }
    
    /**
     * 生成一个按照列顺序的矩阵:SQuareBuilder
     * @param <T> 元素类型
     * @param typeClass 元素类型
     * @param n n 方阵维数
     * @return DataMatrixBuilder<T>
     */
    public static <T> DataMatrixBuilder<T> SQB2(Class<T>typeClass,final Number n){
       return new DataMatrixBuilder<T>(typeClass,n.intValue(),n.intValue(),false);
    }

    /**
     * 生成一个按照行顺序的矩阵:SQuareBuilder
     * @param <T> 元素类型
     * @param typeClass 元素类型
     * @param n n 方阵维数
     * @return DataMatrixBuilder<T>
     */
    public static DataMatrixBuilder<Double> DSQB(final Number n){
       return new DataMatrixBuilder<>(Double.class,n.intValue(),n.intValue(),true);
    }
    
    /**
     * 生成一个按照列顺序的矩阵:SQuareBuilder
     * @param <T> 元素类型
     * @param typeClass 元素类型
     * @param n n 方阵维数
     * @return DataMatrixBuilder<T>
     */
    public static DataMatrixBuilder<Double> DSQB2(final Number n){
       return new DataMatrixBuilder<>(Double.class,n.intValue(),n.intValue(),false);
    }

    /**
     * 把一个Double 数组转换成 double数组
     * @param aa 一维数组
     * @return double[]
     */
    public static double[] D2ds(Double[]aa) {
        return Stream.of(aa).mapToDouble(e->e).toArray();
    }
    
    /**
     * 把一个Double 二维数组转换成 double二数组
     * @param aa 一维数组
     * @return double[]
     */
    public static double[][] DD2dds(Double[][]aa) {
        return Stream.of(aa).toArray(double[][]::new);
    }
    
    /**
     * 把一个 double 数组转换成一个Double数组
     * @param aa 一维数组
     * @return  Double[]
     */
    public static Double[] d2Ds(double[]aa) {
        return Arrays.stream(aa).mapToObj(e->e).toArray(Double[]::new);
    }
    
    /**
     * 把一个 double 二维数组转换成一个Double数组
     * @param aa 一维数组
     * @return  Double[]
     */
    public static Double[][] dd2DDs(double[][]aa) {
        return Arrays.stream(aa).map(e->e).toArray(Double[][]::new);
    }
    
    /**
     * 矩阵转置
     * @param <T> 矩阵元素类型
     * @param mx 待转置的矩阵
     * @return 转置后的矩阵
     */
    public static <T> DataMatrix<T> transpose(DataMatrix<T> mx){
        return mx.transpose();
    }
    
    /**
     * 矩阵转置
     * @param <T> 矩阵元素类型
     * @param mx 待转置的矩阵
     * @return 转置后的矩阵
     */
    public static <T> DataMatrix<T> t(DataMatrix<T> mx){
        return transpose(mx);
    }

    /**
     * 输出函数的会回调函数
     * @param lines 输出的数据内容
     */
    public Consumer<Object[]> println_callback = lines -> {
        if(lines==null||lines.length<1) {
            System.out.println();
        }
        System.out.println(lines[0]);
    };

    /**
     * 行输出:System.out.println 的简写:只显示一行数据
     * @param <T>
     * @param lines 行对象
     * @return lines
     */
    public Object println(Object ...lines) {
        println_callback.accept(lines);
        return lines;
    }
}
