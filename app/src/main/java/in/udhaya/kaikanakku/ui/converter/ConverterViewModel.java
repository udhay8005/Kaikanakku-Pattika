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

public class ConverterViewModel extends AndroidViewModel {

    private final HistoryRepository historyRepository;
    private final SettingsRepository settingsRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<String> result = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final LiveData<List<HistoryEntry>> recentHistory;

    private String lastInputText = null;
    private String lastOutputText = null;
    private double lastTotalCm = 0.0;

    public ConverterViewModel(@NonNull Application application) {
        super(application);
        historyRepository = HistoryRepository.getInstance(application);
        settingsRepository = SettingsRepository.getInstance(application);
        recentHistory = LiveDataReactiveStreams.fromPublisher(historyRepository.getRecentHistory());
    }

    public LiveData<String> getResult() {
        return result;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<HistoryEntry>> getRecentHistory() {
        return recentHistory;
    }

    public void convertCmToKol(double cmValue) {
        if (cmValue <= 0) {
            error.setValue(getApplication().getString(R.string.error_negative_input));
            return;
        }

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
                                    result.setValue(formattedResult);
                                    lastInputText = String.format(Locale.US, "%.2f cm", cmValue);
                                    lastOutputText = formattedResult;
                                    lastTotalCm = cmValue;
                                    saveLastConversionToHistory();
                                },
                                throwable -> {
                                    error.setValue(getApplication().getString(R.string.error_conversion_failed));
                                }
                        )
        );
    }

    public void convertKolToCm(int kol, int viral, double cm) {
        if (kol <= 0 && viral <= 0 && cm <= 0) {
            error.setValue(getApplication().getString(R.string.error_negative_input));
            return;
        }

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

        lastInputText = ConversionUtils.formatKolViralCmInput(kol, viral, cm);
        lastOutputText = formattedResult;
        lastTotalCm = totalCm;
        saveLastConversionToHistory();
    }

    private void saveLastConversionToHistory() {
        if (lastInputText == null || lastOutputText == null || lastInputText.trim().isEmpty() || lastOutputText.trim().isEmpty()) {
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
        clearLastConversion();
    }

    public void clearError() {
        error.setValue(null);
    }

    private void clearLastConversion() {
        lastInputText = null;
        lastOutputText = null;
        lastTotalCm = 0.0;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}