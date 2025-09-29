package in.udhaya.kaikanakku.ui.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
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

        // --- Auto-delete Preference ---
        SeekBarPreference autoDeletePreference = findPreference("auto_delete_days");
        if (autoDeletePreference != null) {
            autoDeletePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                int days = (Integer) newValue;
                if (days > 0) {
                    preference.setSummary(getString(R.string.settings_auto_delete_summary, days));
                } else {
                    preference.setSummary(getString(R.string.settings_auto_delete_disabled));
                }
                return true;
            });
        }

        // --- Reset Settings Preference ---
        Preference resetPreference = findPreference("reset_settings");
        if (resetPreference != null) {
            resetPreference.setOnPreferenceClickListener(preference -> {
                showResetConfirmationDialog(settingsRepository);
                return true;
            });
        }
    }

    @SuppressLint("CheckResult")
    private void showResetConfirmationDialog(SettingsRepository settingsRepository) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_reset_dialog_title)
                .setMessage(R.string.settings_reset_dialog_message)
                .setPositiveButton(R.string.settings_reset_dialog_positive, (dialog, which) -> {
                    settingsRepository.resetAllPreferences()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Log.d(TAG, "All settings have been reset.");
                                        // Recreate the activity to apply the default settings
                                        requireActivity().recreate();
                                    },
                                    throwable -> Log.e(TAG, "Failed to reset settings", throwable)
                            );
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}