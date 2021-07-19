package xyz.jiahao.memory.engine;

import xyz.jiahao.memory.engine.wrapper.QueryWrapper;

import java.util.List;

/**
 * @author jiahao.zhang
 */
public interface IEngine<T> {
    boolean create(T pojo);
    T findById(Long id);
    List<T> findList(T pojo);
    List<T> findList(QueryWrapper queryWrapper);
    boolean delete(Long id);
    boolean updateById(T targetObj);
}
