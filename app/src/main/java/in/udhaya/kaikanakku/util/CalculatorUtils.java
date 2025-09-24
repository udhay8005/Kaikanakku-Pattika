package in.udhaya.kaikanakku.util;

/**
 * A final utility class for performing arithmetic operations on measurements.
 * All calculations are performed on the base unit (centimeters) to ensure accuracy and
 * simplify the logic, as complex carry-over rules are handled by the ConversionUtils class.
 * The ViewModel is responsible for converting all Kol/Viral inputs to total centimeters before
 * calling the methods in this class.
 */
public final class CalculatorUtils {

    // Private constructor to prevent instantiation of this utility class.
    private CalculatorUtils() {}

    /**
     * A simple, immutable data class to hold the components of a measurement.
     * This is used to pass user input from the Fragment to the ViewModel in a structured way.
     */
    public static class Measurement {
        public final int kol;
        public final int viral;
        public final double cm;

        public Measurement(int kol, int viral, double cm) {
            this.kol = kol;
            this.viral = viral;
            this.cm = cm;
        }
    }

    /**
     * Adds two measurement values together.
     *
     * @param cmA The first value in total centimeters.
     * @param cmB The second value in total centimeters.
     * @return The sum in total centimeters.
     */
    public static double add(double cmA, double cmB) {
        // The ViewModel is responsible for validating against negative inputs before this call.
        return cmA + cmB;
    }

    /**
     * Subtracts the second measurement value from the first.
     *
     * @param cmA The value to subtract from (minuend), in total centimeters.
     * @param cmB The value to subtract (subtrahend), in total centimeters.
     * @return The difference in total centimeters.
     */
    public static double subtract(double cmA, double cmB) {
        // The ViewModel is responsible for ensuring cmA >= cmB before calling this method
        // to prevent negative length results, as specified in the project requirements.
        return cmA - cmB;
    }
}
