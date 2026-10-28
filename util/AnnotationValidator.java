package util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import model.annotation.MaxLength;
import model.annotation.Positive;

public final class AnnotationValidator {
    private AnnotationValidator() {
    }

    public static String[] validate(Object obj) {
        if (obj == null) {
            return new String[] {"Object cannot be null"};
        }

        List<String> errors = new ArrayList<>();
        Class<?> currentClass = obj.getClass();

        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Positive.class)) {
                    try {
                        Object value = field.get(obj);
                        if (value instanceof Number number && number.doubleValue() <= 0) {
                            Positive positive = field.getAnnotation(Positive.class);
                            errors.add(field.getName() + " " + positive.message());
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(
                                "Unable to access field: " + field.getName(), e);
                    }
                }

                if (field.isAnnotationPresent(MaxLength.class)) {
                    try {
                        Object value = field.get(obj);
                        MaxLength maxLength = field.getAnnotation(MaxLength.class);

                        if (value instanceof CharSequence chars
                                && chars.length() > maxLength.value()) {
                            errors.add(field.getName() + " exceeds max length of " + maxLength.value());
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(
                                "Unable to access field: " + field.getName(), e);
                    }
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return errors.toArray(String[]::new);
    }
}
