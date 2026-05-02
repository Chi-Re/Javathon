package chire.python.lib;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 一个不可变的、类似 Python tuple 的元组类型。
 * 可包含任意类型和数量的元素，支持比较、连接、重复、切片、包含检查等操作。
 * 使用方式：Tuple t = Tuple.of(1, "a", true);
 */
public final class PyTuple implements Iterable<Object>, Comparable<PyTuple>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final PyTuple EMPTY = new PyTuple(new Object[0]);

    private final Object[] elements;
    private int hash;

    // 私有构造器，确保防御性拷贝
    private PyTuple(Object[] elements) {
        this.elements = elements.clone();
    }

    public static PyTuple of(Object... elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY;
        }
        return new PyTuple(elements);
    }

    public static PyTuple empty() {
        return EMPTY;
    }

    public static PyTuple fromArray(Object[] array) {
        return new PyTuple(array.clone());
    }

    public int size() {
        return elements.length;
    }

    public Object get(int index) {
        if (index >= elements.length) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        if (index < 0) {
            return elements[index + elements.length];
        }
        return elements[index];
    }

    public boolean contains(Object o) {
        for (Object e : elements) {
            if (Objects.equals(e, o)) return true;
        }
        return false;
    }

    public int indexOf(Object o) {
        for (int i = 0; i < elements.length; i++) {
            if (Objects.equals(elements[i], o)) return i;
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        for (int i = elements.length - 1; i >= 0; i--) {
            if (Objects.equals(elements[i], o)) return i;
        }
        return -1;
    }

    public int count(Object o) {
        int cnt = 0;
        for (Object e : elements) {
            if (Objects.equals(e, o)) cnt++;
        }
        return cnt;
    }

    public PyTuple concat(PyTuple other) {
        if (other == null) {
            throw new IllegalArgumentException("other tuple cannot be null");
        }
        Object[] newElements = new Object[elements.length + other.elements.length];
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        System.arraycopy(other.elements, 0, newElements, elements.length, other.elements.length);
        return new PyTuple(newElements);
    }

    public PyTuple repeat(int times) {
        if (times < 0) {
            throw new IllegalArgumentException("Times cannot be negative");
        }
        if (times == 0 || elements.length == 0) {
            return EMPTY;
        }
        int newLen = elements.length * times;
        Object[] newElements = new Object[newLen];
        for (int i = 0; i < times; i++) {
            System.arraycopy(elements, 0, newElements, i * elements.length, elements.length);
        }
        return new PyTuple(newElements);
    }

    public PyTuple subTuple(int start) {
        return subTuple(start, elements.length);
    }

    public PyTuple subTuple(int start, int end) {
        if (start < 0 || start > elements.length) {
            throw new IndexOutOfBoundsException("start: " + start);
        }
        if (end < start || end > elements.length) {
            throw new IndexOutOfBoundsException("end: " + end);
        }
        int newLen = end - start;
        if (newLen == 0) return EMPTY;
        Object[] newElements = new Object[newLen];
        System.arraycopy(elements, start, newElements, 0, newLen);
        return new PyTuple(newElements);
    }

    public Object[] toArray() {
        return elements.clone();
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {
            private int cursor = 0;
            @Override
            public boolean hasNext() {
                return cursor < elements.length;
            }
            @Override
            public Object next() {
                if (!hasNext()) throw new NoSuchElementException();
                return elements[cursor++];
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PyTuple other)) return false;
        return Arrays.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && elements.length > 0) {
            h = Arrays.hashCode(elements);
            hash = h;
        }
        return h;
    }

    @Override
    public String toString() {
        if (elements.length == 0) return "()";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(elements[i]);
        }
        if (elements.length == 1) sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public int compareTo(PyTuple other) {
        if (other == null) throw new NullPointerException("other tuple is null");
        int minLen = Math.min(elements.length, other.elements.length);
        for (int i = 0; i < minLen; i++) {
            Object e1 = elements[i];
            Object e2 = other.elements[i];
            if (e1 == e2) continue;
            // null 与 non-null 比较 -> 抛出异常（Python 中 None 与非 None 比较会 TypeError）
            if (e1 == null || e2 == null) {
                throw new ClassCastException("Cannot compare null with non-null element");
            }
            if (!(e1 instanceof Comparable)) {
                throw new ClassCastException("Element type does not implement Comparable: " + e1.getClass());
            }
            int cmp = ((Comparable) e1).compareTo(e2);
            if (cmp != 0) return cmp;
        }

        return Integer.compare(elements.length, other.elements.length);
    }
}
