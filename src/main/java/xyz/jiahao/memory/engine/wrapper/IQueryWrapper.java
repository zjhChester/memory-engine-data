package xyz.jiahao.memory.engine.wrapper;

import xyz.jiahao.memory.engine.wrapper.model.WrapperEntity;

/**
 * @author jiahao.zhang
 */
public interface IQueryWrapper {
    IQueryWrapper equals(String fieldName,Object fieldValue);
    IQueryWrapper moreThan(String fieldName,Object fieldValue);
    IQueryWrapper lessThan(String fieldName,Object fieldValue);
    IQueryWrapper like(String fieldName,Object fieldValue);
}
