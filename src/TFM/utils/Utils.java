/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.utils;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class Utils {
    public static double dmsToDd(double degrees, double minutes, double seconds) {
        return degrees + (minutes / 60.0) + (seconds / 3600.0);
    }

    public enum ComparisonOperator {
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL_TO,
        LESS_THAN_OR_EQUAL_TO,
        EQUAL_TO
    }

    public static boolean compare(double value1, double value2, ComparisonOperator operator) {
        switch (operator) {
            case GREATER_THAN:
                return value1 > value2;
            case LESS_THAN:
                return value1 < value2;
            case GREATER_THAN_OR_EQUAL_TO:
                return value1 >= value2;
            case LESS_THAN_OR_EQUAL_TO:
                return value1 <= value2;
            case EQUAL_TO:
                return value1 == value2;
            default:
                throw new IllegalArgumentException("Invalid operator");
        }
    }
}
