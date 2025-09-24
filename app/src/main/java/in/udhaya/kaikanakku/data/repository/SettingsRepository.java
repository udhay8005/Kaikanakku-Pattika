package in.udhaya.kaikanakku.data.repository;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Repository for handling all data operations related to user settings.
 * This class abstracts the Preferences DataStore from the rest of the application,
 * providing a clean, type-safe, and reactive API for ViewModels to read and write settings.
 * DataStore is the modern replacement for SharedPreferences, offering better safety and
 * asynchronous operations to prevent blocking the UI thread.
 * This repository is implemented as a singleton.
 */
public class SettingsRepository {

    private static final String PREFERENCES_NAME = "kaikanakku_settings";
    private static volatile SettingsRepository INSTANCE;

    private final RxDataStore<Preferences> dataStore;

    // Define preference keys with strong types. This prevents runtime errors
    // from using incorrect keys or value types when accessing preferences.
    public static final Preferences.Key<Boolean> KEY_IS_CM_TO_KOL_DEFAULT = PreferencesKeys.booleanKey("default_mode_cm_to_kol");
    public static final Preferences.Key<Boolean> KEY_IS_PRECISION_ENABLED = PreferencesKeys.booleanKey("precision_mode_enabled");
    public static final Preferences.Key<String> KEY_ROUNDING_MODE = PreferencesKeys.stringKey("rounding_mode");
    public static final Preferences.Key<Integer> KEY_AUTO_DELETE_DAYS = PreferencesKeys.intKey("auto_delete_days");
    public static final Preferences.Key<String> KEY_LANGUAGE = PreferencesKeys.stringKey("app_language");

    // Constants for rounding modes to ensure consistency.
    public static final String ROUND_MODE = "ROUND";
    public static final String TRUNCATE_MODE = "TRUNCATE";


    private SettingsRepository(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, PREFERENCES_NAME).build();
    }

    public static SettingsRepository getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (SettingsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SettingsRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // --- Methods to get reactive streams of preferences ---
    // These Flowable streams automatically emit new values whenever a preference changes.

    public Flowable<Boolean> isCmToKolDefault() {
        return dataStore.data().map(prefs -> {
            Boolean value = prefs.get(KEY_IS_CM_TO_KOL_DEFAULT);
            // FIX: Safely handle null by providing a default value. Default is true.
            return value == null ? true : value;
        });
    }

    public Flowable<Boolean> isPrecisionEnabled() {
        return dataStore.data().map(prefs -> {
            Boolean value = prefs.get(KEY_IS_PRECISION_ENABLED);
            // FIX: Safely handle null by providing a default value. Default is false.
            return value == null ? false : value;
        });
    }

    public Flowable<String> getRoundingMode() {
        return dataStore.data().map(prefs -> {
            String value = prefs.get(KEY_ROUNDING_MODE);
            // FIX: Safely handle null by providing a default value. Default is ROUND_MODE.
            return value == null ? ROUND_MODE : value;
        });
    }

    public Flowable<Integer> getAutoDeleteDays() {
        return dataStore.data().map(prefs -> {
            Integer value = prefs.get(KEY_AUTO_DELETE_DAYS);
            // FIX: Safely handle null by providing a default value. Default is 0 (disabled).
            return value == null ? 0 : value;
        });
    }

    public Flowable<String> getLanguage() {
        return dataStore.data().map(prefs -> {
            String value = prefs.get(KEY_LANGUAGE);
            // FIX: Safely handle null by providing a default value. Default is English.
            return value == null ? "en" : value;
        });
    }

    // --- Methods to update preferences ---
    // These methods return a Completable, allowing the caller to know when the
    // asynchronous write operation has finished.

    public Completable updateIsCmToKolDefault(boolean isDefault) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(KEY_IS_CM_TO_KOL_DEFAULT, isDefault);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public Completable updatePrecisionEnabled(boolean isEnabled) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(KEY_IS_PRECISION_ENABLED, isEnabled);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public Completable updateRoundingMode(String mode) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(KEY_ROUNDING_MODE, mode);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public Completable updateAutoDeleteDays(int days) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(KEY_AUTO_DELETE_DAYS, days);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public Completable updateLanguage(String languageCode) {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(KEY_LANGUAGE, languageCode);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public Completable resetAllPreferences() {
        return dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.clear();
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }
}