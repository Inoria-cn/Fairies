package org.squirrelnest.fairies.storage.datasource.interfaces;

/**
 * 系统数据存储的通用接口，简易版实现可以使用本地文件，高级版实现可以使用数据库等等
 * Created by Inoria on 2019/3/6.
 */
public interface DataSource {

    /**
     * 数据源标准存储方法
     * @param partName 数据源分区名称（文件名/表名字等）
     * @param key 存储的key
     * @param value 存储值
     * @param <T> 泛型，任意类型
     * @throws Exception 存储或者序列化异常
     */
    <T> void save(String partName, String key, T value) throws Exception;

    /**
     * 数据源标准获取方法
     * @param partName 数据源分区名称（文件名/表名字等）
     * @param key 存储的key
     * @param valueClass 值的类型
     * @param <T> 泛型，任意类型
     * @throws Exception 取值或者反序列化异常
     */
    <T> T load(String partName, String key, Class<T> valueClass) throws Exception;
}
