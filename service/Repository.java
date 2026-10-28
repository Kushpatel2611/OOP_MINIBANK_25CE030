package service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import model.annotation.Id;

public class Repository<T> {
    private final Map<String, T> store = new HashMap<>();

    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        String id = resolveId(entity);
        store.put(id, entity);
        return entity;
    }

    public T findById(String id) {
        return store.get(id);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    @SafeVarargs
    public final void saveAll(T... items) {
        if (items == null) {
            return;
        }

        for (T item : items) {
            save(item);
        }
    }

    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }

        double total = 0.0;
        for (N value : values) {
            total += value.doubleValue();
        }
        return total / values.length;
    }

    private static String resolveId(Object entity) {
        Class<?> currentClass = entity.getClass();

        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(entity);
                        if (value == null) {
                            throw new IllegalArgumentException("Id field cannot be null");
                        }
                        return value.toString();
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Unable to read @Id field", e);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        throw new IllegalArgumentException("No @Id field found for entity: " + entity.getClass().getName());
    }
}
