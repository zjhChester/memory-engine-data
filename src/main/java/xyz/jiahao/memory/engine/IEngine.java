package xyz.jiahao.memory.engine;

import java.util.List;

/**
 * @author jiahao.zhang
 */
public interface IEngine<T> {
    boolean create(T pojo);
    T findById(Long id);
    List<T> findList(T pojo);
    boolean delete(Long id);
    boolean updateById(T targetObj);
}
