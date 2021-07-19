package xyz.jiahao.memory.engine.common.model;

/**
 * @author jiahao.zhang
 */
public class EngineObj<T> {
    public Class<T> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }

    private Class<T> targetClass;
    private T source;
}
