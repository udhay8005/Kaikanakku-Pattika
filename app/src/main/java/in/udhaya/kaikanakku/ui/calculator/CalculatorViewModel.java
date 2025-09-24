package in.udhaya.kaikanakku.ui.calculator;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.material.snackbar.Snackbar;
import in.udhaya.kaikanakku.R;
import in.udhaya.kaikanakku.data.db.HistoryEntry;
import in.udhaya.kaikanakku.data.repository.HistoryRepository;
import in.udhaya.kaikanakku.util.CalculatorUtils;
import in.udhaya.kaikanakku.util.ConversionUtils;

/**
 * ViewModel for the CalculatorFragment. It handles all business logic for performing
 * arithmetic operations, validating inputs, and interacting with the HistoryRepository
 * to persist calculation results. It exposes LiveData objects for the UI to observe.
 */
public class CalculatorViewModel extends AndroidViewModel {

    public enum Operation {
        ADD,
        SUBTRACT
    }

    private final HistoryRepository historyRepository;

    private final MutableLiveData<String> result = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> saveStatus = new MutableLiveData<>();

    // Holds the details of the last successful calculation for history saving.
    private String lastInputText = null;
    private String lastOutputText = null;
    private double lastTotalCm = 0.0;

    public CalculatorViewModel(@NonNull Application application) {
        super(application);
        historyRepository = HistoryRepository.getInstance(application);
    }

    // --- LiveData Getters for the UI ---

    public LiveData<String> getResult() {
        return result;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSaveStatus() {
        return saveStatus;
    }


    /**
     * Performs the core calculation (add or subtract) on two measurements.
     * It converts both measurements to a base unit (cm) for accuracy, performs
     * the operation, formats the result, and automatically saves it to history.
     *
     * @param operation    The operation to perform (ADD or SUBTRACT).
     * @param a            The first measurement (Value A).
     * @param b            The second measurement (Value B).
     */
    public void calculate(Operation operation, CalculatorUtils.Measurement a, CalculatorUtils.Measurement b) {
        // Convert both inputs to the base unit (cm) before calculation.
        double cmA = ConversionUtils.kolToCm(a.kol, a.viral, a.cm);
        double cmB = ConversionUtils.kolToCm(b.kol, b.viral, b.cm);

        if (cmA < 0 || cmB < 0) {
            error.setValue(getApplication().getString(R.string.error_negative_input));
            return;
        }

        double resultCm;
        String operationSymbol;

        if (operation == Operation.ADD) {
            resultCm = CalculatorUtils.add(cmA, cmB);
            operationSymbol = " + ";
        } else { // SUBTRACT
            if (cmB > cmA) {
                error.setValue(getApplication().getString(R.string.error_b_greater_than_a));
                return;
            }
            resultCm = CalculatorUtils.subtract(cmA, cmB);
            operationSymbol = " - ";
        }

        // Format the result back into the Kol system for display.
        String formattedResult = ConversionUtils.cmToKolFormatted(resultCm, true, false);
        result.setValue(formattedResult);

        // Prepare the text for history logging.
        String inputA = ConversionUtils.formatKolViralCmInput(a.kol, a.viral, a.cm);
        String inputB = ConversionUtils.formatKolViralCmInput(b.kol, b.viral, b.cm);

        lastInputText = "(" + inputA + ")" + operationSymbol + "(" + inputB + ")";
        lastOutputText = formattedResult;
        lastTotalCm = resultCm;

        // Automatically save the successful calculation.
        saveLastResultToHistory();
    }

    /**
     * Saves the last successfully calculated result to the history database.
     * This is now called automatically after each successful calculation.
     */
    private void saveLastResultToHistory() {
        if (lastInputText == null || lastOutputText == null) {
            // This case should ideally not be reached with auto-save, but it's a safeguard.
            error.setValue(getApplication().getString(R.string.error_no_result_to_save));
            return;
        }

        HistoryEntry entry = new HistoryEntry(
                lastInputText,
                lastOutputText,
                lastTotalCm,
                System.currentTimeMillis(),
                false // New entries are not favorited by default.
        );

        historyRepository.insert(entry);
        saveStatus.setValue(getApplication().getString(R.string.status_saved_to_history));
        clearLastCalculation();
    }

    /**
     * Clears any active error messages to prevent them from being shown again
     * on configuration changes.
     */
    public void clearError() {
        error.setValue(null);
    }

    /**
     * Resets the details of the last calculation to prevent duplicate saves.
     */
    private void clearLastCalculation() {
        lastInputText = null;
        lastOutputText = null;
        lastTotalCm = 0.0;
    }
}