package com.artillexstudios.axplayerwarps.guis.framework;

/**
 * Evaluates conditions like "%rating% >= 3" or "%access% == Public". Numbers are compared as numbers, anything else as text.
 */
public final class SimpleExpression {
    private static final String[] OPERATORS = {"==", "!=", ">=", "<=", ">", "<"};

    private SimpleExpression() {
    }

    public static boolean eval(String input) {
        for (String operator : OPERATORS) {
            int index = input.indexOf(operator);
            if (index == -1) continue;
            String left = input.substring(0, index).trim();
            String right = input.substring(index + operator.length()).trim();
            return compare(left, right, operator);
        }
        throw new IllegalArgumentException("Invalid condition: " + input);
    }

    private static boolean compare(String left, String right, String operator) {
        int result;
        try {
            result = Double.compare(Double.parseDouble(left), Double.parseDouble(right));
        } catch (NumberFormatException ex) {
            result = left.compareTo(right);
        }

        return switch (operator) {
            case "==" -> result == 0;
            case "!=" -> result != 0;
            case ">=" -> result >= 0;
            case "<=" -> result <= 0;
            case ">" -> result > 0;
            default -> result < 0;
        };
    }
}
