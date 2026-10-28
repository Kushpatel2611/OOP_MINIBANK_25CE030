package util;

import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PAN_PATTERN =
            Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    private static final Pattern IFSC_PATTERN =
            Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

    private Validator() {
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPan(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan).matches();
    }

    public static boolean isValidIfsc(String ifsc) {
        return ifsc != null && IFSC_PATTERN.matcher(ifsc).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }

    public static boolean isValidAccountType(String type) {
        if (type == null) {
            return false;
        }

        String normalized = type.trim().toUpperCase();
        return normalized.equals("SAVINGS")
                || normalized.equals("CURRENT")
                || normalized.equals("FIXED")
                || normalized.equals("FIXED_DEPOSIT");
    }

    public static boolean isPositiveAmount(long amount) {
        return amount > 0;
    }
}
