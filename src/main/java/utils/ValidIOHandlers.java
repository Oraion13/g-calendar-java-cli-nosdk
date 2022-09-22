package utils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Scanner;

public class ValidIOHandlers {
    /**
     * Get input choice
     * 
     * @param message message to display
     * @return an integer choice
     * @throws IOException
     */
    public static int getChoice(String message) throws IOException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                System.out.print(message);
                int choice = sc.nextInt();

                // if correct input is given, return the input
                if (choice >= 0)
                    return choice;
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid input [Number]!");
            }
        }

    }

    /**
     * Get valid y / n choice
     * 
     * @param message message to display
     * @return a boolean true / false
     */
    public static boolean getYorN(String message) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(message);
            String choice = sc.nextLine().toLowerCase();

            // y - true
            if (choice.equals("y")) {
                return true;
            }

            // n - false
            if (choice.equals("n")) {
                return false;
            }

            System.out.println("Enter a valid input [Y/n]!");
        }
    }

    /**
     * Get valid date and return
     * 
     * @param message message for user
     * @return a valid dateTime
     */
    public static String getDate(String message, int confirm) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(message);
            String dateStr = sc.nextLine();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.getDefault())
                    .withResolverStyle(ResolverStyle.STRICT);
            DateValidatorUsingDateTimeFormatter validator = new DateValidatorUsingDateTimeFormatter(dateFormatter);

            // is a valid date
            if (confirm == 1) {
                if (validator.isValid(dateStr)) {
                    return dateStr;
                }
            } else {
                if (validator.isValid(dateStr) || dateStr.equals("0")) {
                    return dateStr;
                }
            }

            System.out.println("Enter a valid date [YYYY-MM-DD]!");
        }
    }

    /**
     * Get a valid message
     * 
     * @param message message for user
     * @return
     */
    public static String getString(String message) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(message);
            String str = sc.nextLine();

            if (str != null) {
                return str;
            }

            System.out.println("Enter a valid message!");
        }
    }

    public static String getMinHour(String message, boolean isMinute) throws IOException {
        while (true) {
            int num = getChoice(message);

            // is minute
            if (isMinute && (num >= 0 && num <= 59)) {
                return "" + num;
            }

            // is hour
            if (num >= 0 && num <= 23) {
                return "" + num;
            }

            System.out.println("Enter a valid " + (isMinute ? "minute!" : "hour!"));
        }
    }

}
