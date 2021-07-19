package xyz.jiahao.memory.engine;

import xyz.jiahao.memory.engine.common.Constants;
import xyz.jiahao.memory.engine.common.WrapperConditions;
import xyz.jiahao.memory.engine.common.model.EngineObj;
import xyz.jiahao.memory.engine.utils.CollectionUtils;
import xyz.jiahao.memory.engine.wrapper.QueryWrapper;
import xyz.jiahao.memory.engine.wrapper.model.WrapperEntity;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final T source = tEngineObj.getSource();
        return Objects.equals(modelClass, tEngineObj.getTargetClass()) ? source : null;
    }

    @Override
    public List<T> findList(T pojo) {
        return searchInDataBase(modelClass, pojo);
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
        final Field idField = getFieldByFieldName(modelClass, Constants.ID_FIELDS_NAME);
        Long id = (Long) getFieldValue(idField, targetObj);
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

    @Override
    public List<T> findList(QueryWrapper queryWrapper) {
        final Map<Long, EngineObj<T>> table = dataBase.get(modelClass);
        if (Objects.isNull(table)) {
            return new ArrayList<>();
        }
        return findListByConditions(table, queryWrapper);
    }

    protected List<T> findListByConditions(Map<Long, EngineObj<T>> table, QueryWrapper queryWrapper) {
        Stream<EngineObj<T>> stream = table.values().stream();
        for (WrapperEntity entity : queryWrapper.getContainer()) {
            stream = stream.filter(v -> matchCondition(entity, v));
        }
        List<T> resList = new ArrayList<>();
        stream.forEach(v -> resList.add(v.getSource()));
        return resList;
    }

    private boolean matchCondition(WrapperEntity entity, EngineObj<T> v) {
        final Field matchedField = getFieldByFieldName(modelClass, entity.getFieldName());
        final Object fieldValue = getFieldValue(matchedField, v.getSource());
        final Class<?> fieldType = matchedField.getType();
        if (Objects.isNull(entity.getFieldValue())) {
            return false;
        }
        if (Objects.equals(WrapperConditions.EQUAL, entity.getCondition())) {
            return Objects.equals(fieldValue, entity.getFieldValue());
        }
        try {
            if (Objects.equals(WrapperConditions.LESS_THAN, entity.getCondition())) {
                double checkValue = Double.parseDouble(entity.getFieldValue().toString());
                double sourceValue = Double.parseDouble(fieldValue.toString());
                return checkValue > sourceValue;
            }
            if (Objects.equals(WrapperConditions.MORE_THAN, entity.getCondition())) {
                double checkValue = Double.parseDouble(entity.getFieldValue().toString());
                double sourceValue = Double.parseDouble(fieldValue.toString());
                return sourceValue < checkValue;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("the field named [%s] is not a number type", matchedField.getName()));
        }

        if (Objects.equals(WrapperConditions.LIKE, entity.getCondition())) {
            if (!(fieldType.equals(String.class)) || !(entity.getFieldValue().getClass().equals(String.class))) {
                throw new IllegalArgumentException(String.format("the field named [%s] is not instance of String.class ", matchedField.getName()));
            }
            return fieldValue.toString().contains(entity.getFieldValue().toString());
        }
        throw new IllegalArgumentException(String.format("unknown condition [%s]", entity.getCondition()));
    }

    private Object getFieldValue(Field idField, T targetObj) {
        try {
            idField.setAccessible(true);
            return idField.get(targetObj);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("pojo or pojo's super class can't find the field named [%s]", idField.getName()));
        }
    }

    private Field getFieldByFieldName(Class<T> modelClass, String fieldName) {

        Field declaredField;
        try {
            declaredField = modelClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            //如果没取到 取父类的
            try {
                declaredField = modelClass.getSuperclass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException noSuchFieldException) {
                throw new IllegalArgumentException(String.format("pojo or pojo's super class can't find the field named [%s]", fieldName));
            }

        }

        return declaredField;
    }

    private List<T> searchInDataBase(Class<T> modelClass, Object pojo) {
        final Map<Long, EngineObj<T>> tables = dataBase.get(modelClass);
        if (CollectionUtils.isEmpty(tables)) {
            return Collections.emptyList();
        }
        List<T> targetList = new ArrayList<>();
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
            Field id = getFieldByFieldName(modelClass, Constants.ID_FIELDS_NAME);
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

