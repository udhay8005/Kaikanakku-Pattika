package in.udhaya.kaikanakku.ui.converter;

import android.annotation.SuppressLint;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveDataReactiveStreams;

import java.util.List;
import java.util.Locale;

import in.udhaya.kaikanakku.R;
import in.udhaya.kaikanakku.data.db.HistoryEntry;
import in.udhaya.kaikanakku.data.repository.HistoryRepository;
import in.udhaya.kaikanakku.data.repository.SettingsRepository;
import in.udhaya.kaikanakku.util.ConversionUtils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for the ConverterFragment. This class handles the business logic
 * for conversions, interacts with repositories for data persistence and settings,
 * and exposes LiveData for the UI to observe.
 */
public class ConverterViewModel extends AndroidViewModel {

    private final HistoryRepository historyRepository;
    private final SettingsRepository settingsRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<String> result = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final LiveData<List<HistoryEntry>> recentHistory;

    // Store the last successful conversion to allow saving it to history
    private String lastInputText = null;
    private String lastOutputText = null;
    private double lastTotalCm = 0.0;

    public ConverterViewModel(@NonNull Application application) {
        super(application);
        historyRepository = HistoryRepository.getInstance(application);
        settingsRepository = SettingsRepository.getInstance(application);
        // Fetch the recent history once and observe it
        recentHistory = LiveDataReactiveStreams.fromPublisher(historyRepository.getRecentHistory());
    }

    // --- LiveData Getters for the UI ---

    public LiveData<String> getResult() {
        return result;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<HistoryEntry>> getRecentHistory() {
        return recentHistory;
    }

    /**
     * Converts a centimeter value to the Kol/Viral system, respecting user settings
     * for precision and rounding. Automatically saves the result upon success.
     *
     * @param cmValue The value in centimeters to convert.
     */
    public void convertCmToKol(double cmValue) {
        if (cmValue < 0) {
            error.setValue(getApplication().getString(R.string.error_negative_input));
            return;
        }

        // Fetch precision and rounding settings from the repository.
        // Flowable.combineLatest combines the latest emissions from all streams.
        disposables.add(
                Flowable.combineLatest(
                                settingsRepository.isPrecisionEnabled(),
                                settingsRepository.getRoundingMode(),
                                (isPrecision, roundingMode) -> ConversionUtils.cmToKolFormatted(cmValue, isPrecision, roundingMode.equals(SettingsRepository.ROUND_MODE))
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                formattedResult -> {
                                    // On success, update the result LiveData and store details for saving
                                    result.setValue(formattedResult);
                                    lastInputText = String.format(Locale.US, "%.2f cm", cmValue);
                                    lastOutputText = formattedResult;
                                    lastTotalCm = cmValue;
                                    saveLastConversionToHistory(); // Auto-save
                                },
                                throwable -> {
                                    // On error, update the error LiveData
                                    error.setValue(getApplication().getString(R.string.error_conversion_failed));
                                }
                        )
        );
    }

    /**
     * Converts a measurement from the Kol/Viral system to centimeters.
     * Automatically saves the result upon success.
     *
     * @param kol   The 'Kol' part of the measurement.
     * @param viral The 'Viral' part of the measurement.
     * @param cm    The 'cm' part of the measurement.
     */
    public void convertKolToCm(int kol, int viral, double cm) {
        if (kol < 0 || viral < 0 || cm < 0) {
            error.setValue(getApplication().getString(R.string.error_negative_input));
            return;
        }

        // As per requirements: viral must be <= 23 and cm must be < 3
        if (viral > 23) {
            error.setValue(getApplication().getString(R.string.error_invalid_viral_input));
            return;
        }
        if (cm >= 3) {
            error.setValue(getApplication().getString(R.string.error_invalid_kol_cm_input));
            return;
        }

        double totalCm = ConversionUtils.kolToCm(kol, viral, cm);
        @SuppressLint("DefaultLocale") String formattedResult = String.format(Locale.US, "%.2f cm", totalCm);

        result.setValue(formattedResult);

        // Store details for saving
        lastInputText = ConversionUtils.formatKolViralCmInput(kol, viral, cm);
        lastOutputText = formattedResult;
        lastTotalCm = totalCm;
        saveLastConversionToHistory(); // Auto-save
    }

    /**
     * Saves the last successfully calculated conversion to the history database.
     * This is now a private method called internally.
     */
    private void saveLastConversionToHistory() {
        if (lastInputText == null || lastOutputText == null) {
            error.setValue(getApplication().getString(R.string.error_no_result_to_save));
            return;
        }

        HistoryEntry entry = new HistoryEntry(
                lastInputText,
                lastOutputText,
                lastTotalCm,
                System.currentTimeMillis(),
                false
        );

        historyRepository.insert(entry);
        // We no longer need a save status LiveData as saving is automatic.
        clearLastConversion();
    }

    /**
     * Clears any active error messages.
     */
    public void clearError() {
        error.setValue(null);
    }

    /**
     * Clears the last saved conversion details.
     */
    private void clearLastConversion() {
        lastInputText = null;
        lastOutputText = null;
        lastTotalCm = 0.0;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clear all disposables to prevent memory leaks.
        disposables.clear();
    }
}