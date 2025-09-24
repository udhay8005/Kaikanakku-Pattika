package in.udhaya.kaikanakku.util;

import java.util.Locale;

/**
 * A final utility class containing static methods for all measurement conversions and formatting logic.
 * This class is the mathematical core of the application. All logic here is pure and has no
 * dependency on the Android Framework, making it highly testable. It operates on the principle
 * of converting all inputs to a base unit (centimeters) before performing calculations to
 * ensure accuracy and simplify complex carry-over logic.
 */
public final class ConversionUtils {

    // --- Authoritative Conversion Constants ---
    public static final double CM_PER_VIRAL = 3.0;
    public static final int VIRAL_PER_KOL = 24;
    public static final double CM_PER_KOL = CM_PER_VIRAL * VIRAL_PER_KOL; // 72.0

    // Private constructor to prevent instantiation of this utility class.
    private ConversionUtils() {}

    /**
     * Converts a measurement from the Kol system to its total equivalent in centimeters.
     * This method also performs normalization to handle carry-overs correctly (e.g., 25 viral
     * becomes 1 kol, 1 viral). This is the primary method that should be used for Kol -> CM conversions.
     *
     * @param kol   The number of kols.
     * @param viral The number of virals.
     * @param cm    The number of centimeters.
     * @return The total measurement in centimeters as a double.
     */
    public static double kolToCm(int kol, int viral, double cm) {
        // First, handle carry-over from cm to viral.
        int viralFromCm = (int) (cm / CM_PER_VIRAL);
        double remainingCm = cm % CM_PER_VIRAL;
        viral += viralFromCm;

        // Next, handle carry-over from viral to kol.
        int kolFromViral = viral / VIRAL_PER_KOL;
        int remainingViral = viral % VIRAL_PER_KOL;
        kol += kolFromViral;

        // Finally, calculate the total centimeters from the normalized values.
        return (kol * CM_PER_KOL) + (remainingViral * CM_PER_VIRAL) + remainingCm;
    }

    /**
     * Formats a centimeter value into the traditional Kol system string representation.
     * Intelligently omits zero-value parts for readability and handles rounding/truncation
     * based on user settings.
     *
     * @param totalCm         The total centimeters to convert.
     * @param isPrecisionMode If true, allows decimal cm remainders. If false, cm is a whole number.
     * @param isRound         How to handle cm decimals when precision is off (true to round, false to truncate).
     * @return A formatted string, e.g., "6 kol 22 viral 2 cm", "1 kol", or "15.5 cm".
     */
    public static String cmToKolFormatted(double totalCm, boolean isPrecisionMode, boolean isRound) {
        if (totalCm < 0) return "0 cm";

        int kols = (int) (totalCm / CM_PER_KOL);
        double remainingCmAfterKols = totalCm % CM_PER_KOL;

        int virals = (int) (remainingCmAfterKols / CM_PER_VIRAL);
        double finalCm = remainingCmAfterKols % CM_PER_VIRAL;

        // Handle rounding and precision
        if (!isPrecisionMode) {
            finalCm = isRound ? Math.round(finalCm) : (int) finalCm;
            // Edge Case: Rounding up to the next viral (e.g., 2.8cm -> 3cm)
            if (finalCm >= CM_PER_VIRAL) {
                virals += 1;
                finalCm = 0;
            }
            // Edge Case: Rounding resulted in a carry-over to the next kol
            if (virals >= VIRAL_PER_KOL) {
                kols += 1;
                virals = 0;
            }
        }

        StringBuilder result = new StringBuilder();
        if (kols > 0) {
            result.append(kols).append(" kol ");
        }
        if (virals > 0) {
            result.append(virals).append(" viral ");
        }

        // Append cm part only if it has a value or if the total result is zero.
        // A small tolerance (epsilon) is used for robust double comparison.
        if (finalCm > 0.001 || result.length() == 0) {
            if (isPrecisionMode) {
                // For precision mode, show one decimal place only if it's not a whole number.
                result.append(String.format(Locale.US, finalCm % 1 == 0 ? "%.0f cm" : "%.1f cm", finalCm));
            } else {
                result.append(String.format(Locale.US, "%.0f cm", finalCm));
            }
        }

        return result.toString().trim();
    }

    /**
     * Creates a formatted input string from Kol system values for saving in the history log.
     * This is used to represent the user's original input clearly.
     *
     * @param kol   The number of kols.
     * @param viral The number of virals.
     * @param cm    The number of centimeters.
     * @return A formatted string, e.g., "1 kol 10 viral 2.5 cm".
     */
    public static String formatKolViralCmInput(int kol, int viral, double cm) {
        StringBuilder result = new StringBuilder();
        if (kol > 0) {
            result.append(kol).append(" kol ");
        }
        if (viral > 0) {
            result.append(viral).append(" viral ");
        }
        // Always include cm if it's the only value or if it's non-zero.
        if (cm > 0.001 || result.length() == 0) {
            result.append(String.format(Locale.US, cm % 1 == 0 ? "%.0f cm" : "%.1f cm", cm));
        }
        return result.toString().trim();
    }
}
