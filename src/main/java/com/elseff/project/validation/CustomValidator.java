package com.elseff.project.validation;

public class CustomValidator {
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Long l = Long.valueOf(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
