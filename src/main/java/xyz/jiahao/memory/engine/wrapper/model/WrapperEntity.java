package xyz.jiahao.memory.engine.wrapper.model;

/**
 * @author jiahao.zhang
 */
public class WrapperEntity {
    private String condition;
    private String fieldName;
    private Object fieldValue;

    public WrapperEntity(String condition, String fieldName, Object fieldValue) {
        this.condition = condition;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public WrapperEntity() {
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
