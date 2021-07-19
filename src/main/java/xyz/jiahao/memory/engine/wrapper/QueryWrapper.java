package xyz.jiahao.memory.engine.wrapper;

import xyz.jiahao.memory.engine.common.WrapperConditions;
import xyz.jiahao.memory.engine.wrapper.model.WrapperEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiahao.zhang
 */
public class QueryWrapper implements IQueryWrapper {
    private final List<WrapperEntity> container = new ArrayList<>();

    public List<WrapperEntity> getContainer() {
        return container;
    }

    @Override
    public IQueryWrapper equals(String fieldName,Object fieldValue) {
        container.add(new WrapperEntity(WrapperConditions.EQUAL,fieldName,fieldValue));
        return this;
    }

    @Override
    public IQueryWrapper moreThan(String fieldName,Object fieldValue) {
        container.add(new WrapperEntity(WrapperConditions.MORE_THAN,fieldName,fieldValue));
        return this;
    }

    @Override
    public IQueryWrapper lessThan(String fieldName,Object fieldValue) {
        container.add(new WrapperEntity(WrapperConditions.LESS_THAN,fieldName,fieldValue));
        return this;
    }

    @Override
    public IQueryWrapper like(String fieldName,Object fieldValue) {
        container.add(new WrapperEntity(WrapperConditions.LIKE,fieldName,fieldValue));
        return this;
    }

}
