package org.squirrelnest.fairies.decorator;

import java.util.*;

/**
 * Created by Inoria on 2019/3/16.
 */
public class Decorator<T> extends HashMap<String, Object> {
    private T data;

    public Decorator(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <E> List<Decorator<E>> decorateList(List<E> origin) {
        List<Decorator<E>> result = new ArrayList<>(origin.size());
        for(E element : origin) {
            result.add(new Decorator<E>(element));
        }
        return result;
    }

    public static <E> List<E> unDecorate(List<Decorator<E>> source) {
        List<E> result = new ArrayList<E>(source.size());
        for (Decorator<E> decorator : source) {
            result.add(decorator.getData());
        }
        return result;
    }

    public static <E> Set<Decorator<E>> decoratorSet(Set<E> origin) {
        Set<Decorator<E>> result = new HashSet<>(origin.size());
        for(E element : origin) {
            result.add(new Decorator<E>(element));
        }
        return result;
    }

    public static <E> void putAllDecorators(Collection<Decorator<E>> collection, String key, Object value) {
        for(Decorator<E> decorator : collection) {
            decorator.put(key, value);
        }
    }

    /**
     * 对decorator进行比较和hash的时候，只比较其中包装的值，注解kv信息不参与比较
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Decorator)) return false;

        Decorator<?> decorator = (Decorator<?>) o;

        return getData().equals(decorator.getData());
    }

    @Override
    public int hashCode() {
        return getData().hashCode();
    }
}
