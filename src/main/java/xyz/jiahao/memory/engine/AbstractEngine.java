package xyz.jiahao.memory.engine;

import xyz.jiahao.memory.engine.common.Constants;
import xyz.jiahao.memory.engine.common.model.EngineObj;
import xyz.jiahao.memory.engine.converter.ObjectConverter;
import xyz.jiahao.memory.engine.utils.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 数据引擎，基于内存,的ORM框架
 *
 * @author jiahao.zhang
 */
public abstract class AbstractEngine<T> implements IEngine<T> {
    private final AtomicLong databaseDefaultIndex = new AtomicLong(0);
    private final Map<Class<T>, Map<Long, EngineObj<T>>> dataBase = new ConcurrentHashMap<>();

    private final Class<T> modelClass;

    @SuppressWarnings("unchecked")
    public AbstractEngine() {
        ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
        modelClass = (Class<T>) pt.getActualTypeArguments()[0];
    }
    @Override
    public boolean create(T pojo) {
        EngineObj<T> engineObj = new EngineObj<>();
        engineObj.setSource(pojo);
        engineObj.setTargetClass(modelClass);
        final long index = databaseDefaultIndex.incrementAndGet();
        setPrimaryKey(pojo, index);
        Map<Long, EngineObj<T>> sourcesTables = dataBase.get(modelClass);
        if (Objects.isNull(sourcesTables)) {
            sourcesTables = new ConcurrentHashMap<>(1);
        }
        sourcesTables.put(index, engineObj);
        dataBase.put(modelClass, sourcesTables);
        return true;
    }

    @Override
    public T findById(Long id) {
        final Map<Long, EngineObj<T>> tables = dataBase.get(modelClass);
        if (Objects.isNull(tables)) {
            return null;
        }

        final EngineObj<T> tEngineObj = tables.get(id);
        if (Objects.isNull(tEngineObj)) {
            return null;
        }

        final Object source = tEngineObj.getSource();
        return Objects.equals(modelClass, tEngineObj.getTargetClass()) ? ObjectConverter.convert(source, modelClass) : null;
    }

    @Override
    public List<T> findList(T pojo) {
        return ObjectConverter.convert(searchInDataBase(modelClass, pojo), modelClass);
    }

    @Override
    public boolean delete(Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        final Map<Long, EngineObj<T>> table = dataBase.get(modelClass);
        if (Objects.isNull(table)) {
            return false;
        }
        table.remove(id);
        return true;
    }

    @Override
    public boolean updateById(T targetObj) {
        final Field idField = getIdFeild(targetObj);
        Long id = getIdValue(idField,targetObj);
        if (Objects.isNull(id)) {
            return false;
        }
        final Map<Long, EngineObj<T>> table = dataBase.get(modelClass);
        if (Objects.isNull(table)) {
            return false;
        }
        final EngineObj<T> tEngineObj = table.get(id);
        tEngineObj.setSource(targetObj);
        table.put(id, tEngineObj);
        return true;
    }

    protected  Long getIdValue(Field idField,T targetObj){
        try {
            return (Long) idField.get(targetObj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Field getIdFeild(T targetObj) {

        Field declaredField;
        try {
            declaredField = modelClass.getDeclaredField(Constants.ID_FIELDS_NAME);
        } catch (NoSuchFieldException e) {
            //如果没取到 取父类的
            try {
                declaredField = modelClass.getSuperclass().getDeclaredField(Constants.ID_FIELDS_NAME);
            } catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalArgumentException("pojo实体及其父类没有id字段");
            }

        }

        return declaredField;
    }

    private List<Object> searchInDataBase(Class<T> modelClass, Object pojo) {
        final Map<Long, EngineObj<T>> tables = dataBase.get(modelClass);
        if (CollectionUtils.isEmpty(tables)) {
            return Collections.emptyList();
        }
        List<Object> targetList = new ArrayList<>();
        tables.forEach((k, v) -> {
            if (isMatched(modelClass, v.getSource(), pojo)) {
                targetList.add(v.getSource());
            }
        });
        return targetList;
    }


    private boolean isMatched(Class<T> modelClass, Object source, Object target) {
        if (Objects.isNull(target)) {
            return true;
        }
        Field[] fields = mergeSuperClassFields(modelClass);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object sourcesFieldsValue = field.get(source);
                Object queryFieldsValue = field.get(target);
                if (!Objects.equals(sourcesFieldsValue, field.get(target)) && Objects.nonNull(queryFieldsValue)) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void setPrimaryKey(T pojo, Long key) {
        try {
            Field id = getIdFeild(pojo);
            id.setAccessible(true);
            id.set(pojo, key);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Field[] mergeSuperClassFields(Class<T> modelClass) {
        List<Field> needMerge = Arrays.stream(modelClass.getDeclaredFields()).collect(Collectors.toList());
        needMerge.addAll(Arrays.stream(modelClass.getSuperclass().getDeclaredFields()).collect(Collectors.toList()));
        return needMerge.toArray(new Field[0]);
    }
}

