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

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    private Class<T> targetClass;
    private Object source;
}
