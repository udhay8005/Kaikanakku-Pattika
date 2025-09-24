package in.udhaya.kaikanakku.util;

import java.util.Locale;

public final class ConversionUtils {

    public static final double CM_PER_VIRAL = 3.0;
    public static final int VIRAL_PER_KOL = 24;
    public static final double CM_PER_KOL = CM_PER_VIRAL * VIRAL_PER_KOL; // 72.0

    private ConversionUtils() {}

    public static double kolToCm(int kol, int viral, double cm) {
        int viralFromCm = (int) (cm / CM_PER_VIRAL);
        double remainingCm = cm % CM_PER_VIRAL;
        viral += viralFromCm;

        int kolFromViral = viral / VIRAL_PER_KOL;
        int remainingViral = viral % VIRAL_PER_KOL;
        kol += kolFromViral;

        return (kol * CM_PER_KOL) + (remainingViral * CM_PER_VIRAL) + remainingCm;
    }

    public static String cmToKolFormatted(double totalCm, boolean isPrecisionMode, boolean isRound) {
        if (totalCm < 0) return "0 cm";

        int kols = (int) (totalCm / CM_PER_KOL);
        double remainingCmAfterKols = totalCm % CM_PER_KOL;

        int virals = (int) (remainingCmAfterKols / CM_PER_VIRAL);
        double finalCm = remainingCmAfterKols % CM_PER_VIRAL;

        if (!isPrecisionMode) {
            finalCm = isRound ? Math.round(finalCm) : (int) finalCm;
            if (finalCm >= CM_PER_VIRAL) {
                virals += 1;
                finalCm = 0;
            }
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

        if (finalCm > 0.001 || result.length() == 0) {
            if (isPrecisionMode) {
                result.append(String.format(Locale.US, finalCm % 1 == 0 ? "%.0f cm" : "%.1f cm", finalCm));
            } else {
                result.append(String.format(Locale.US, "%.0f cm", finalCm));
            }
        }

        return result.toString().trim();
    }

    public static String formatKolViralCmInput(int kol, int viral, double cm) {
        StringBuilder result = new StringBuilder();
        if (kol > 0) {
            result.append(kol).append(" kol ");
        }
        if (viral > 0) {
            result.append(viral).append(" viral ");
        }
        if (cm > 0.001 || result.length() == 0) {
            result.append(String.format(Locale.US, cm % 1 == 0 ? "%.0f cm" : "%.1f cm", cm));
        }
        return result.toString().trim();
    }

    public static String multiplyKol(int kol, int viral, double multiplier) {
        double totalViral = (kol * VIRAL_PER_KOL) + viral;
        totalViral *= multiplier;

        int newKol = (int) (totalViral / VIRAL_PER_KOL);
        int newViral = (int) Math.round(totalViral % VIRAL_PER_KOL);

        return String.format(Locale.US, "%d kol %d viral", newKol, newViral);
    }
}