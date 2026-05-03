package chire.python.lib;

import chire.python.lib.base.PyObject;

import java.util.*;
import java.util.stream.Collectors;

public class PyList extends PyObject implements Iterator<Object> {
    private int cursor = 0;
    private final List<Object> data;

    // 构造方法
    public PyList() {
        this.data = new ArrayList<>();
    }

    public PyList(Object iterable) {
        this.data = new ArrayList<>();
        if (iterable instanceof Collection) {
            data.addAll((Collection<?>) iterable);
        } else if (iterable instanceof Object[]) {
            Collections.addAll(data, (Object[]) iterable);
        }
    }

    // 添加元素到末尾
    public void append(Object item) {
        data.add(item);
    }

    // 获取元素
    public Object __getitem__(Integer index) {
        if (index < 0) index += data.size();
        return data.get(index);
    }

    // 设置元素
    public void __setitem__(Integer index, Object value) {
        if (index < 0) index += data.size();
        data.set(index, value);
    }

    // 获取长度
    public Integer __len__() {
        return data.size();
    }

    // 字符串表示
    public String __str__() {
        return data.stream()
                .map(obj -> {
                    if (obj instanceof String) return "'" + obj + "'";
                    return Objects.toString(obj);
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    // 插入元素
    public void insert(Integer index, Object item) {
        if (index < 0) index += data.size();
        data.add(index, item);
    }

    // 移除元素
    public void remove(Object item) {
        data.remove(item);
    }

    // 弹出元素
    public Object pop(Integer index) {
        if (index < 0) index += data.size();
        return data.remove(index);
    }

    // 包含检查
    public boolean __contains__(Object item) {
        return data.contains(item);
    }

    @Override
    public String toString() {
        return __str__();
    }

    public Iterator<Object> iterator(){
        return data.iterator();
    }

    @Override
    public boolean hasNext() {
        return cursor < data.size();
    }

    @Override
    public Object next() {
        Object var = data.get(cursor);
        cursor++;
        return var;
    }
}
