package in.udhaya.kaikanakku.ui.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import in.udhaya.kaikanakku.R;
import in.udhaya.kaikanakku.data.repository.SettingsRepository;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    @SuppressLint("CheckResult")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SettingsRepository settingsRepository = SettingsRepository.getInstance(requireActivity());

        // --- Language Preference ---
        ListPreference languagePreference = findPreference("language");
        if (languagePreference != null) {
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsRepository.updateLanguage((String) newValue)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    Log.d(TAG, "Language updated successfully.");
                                    requireActivity().recreate();
                                },
                                throwable -> Log.e(TAG, "Failed to update language", throwable)
                        );
                return true;
            });
        }

        // --- Rounding Mode Preference ---
        ListPreference roundingPreference = findPreference("rounding_mode");
        if (roundingPreference != null) {
            roundingPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                settingsRepository.updateRoundingMode((String) newValue)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d(TAG, "Rounding mode updated successfully."),
                                throwable -> Log.e(TAG, "Failed to update rounding mode", throwable)
                        );
                return true;
            });
        }
    }
}