package in.udhaya.kaikanakku;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import in.udhaya.kaikanakku.data.repository.SettingsRepository;
import in.udhaya.kaikanakku.util.LocaleHelper;
import in.udhaya.kaikanakku.workers.AutoDeleteWorker;

public class KaiKanakkuApp extends Application implements Configuration.Provider {

    /**
     * FIX: Apply the saved language to the entire application's context.
     * This is the key fix for the language change and stability issues.
     * It's called before onCreate(), ensuring the correct locale is set up from the start.
     */
    @Override
    protected void attachBaseContext(Context base) {
        SettingsRepository settingsRepository = SettingsRepository.getInstance(base);
        String language = settingsRepository.getLanguage().blockingFirst("en"); // Default to English
        super.attachBaseContext(LocaleHelper.setLocale(base, language));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleAutoDeleteWorker();
    }

    private void scheduleAutoDeleteWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresCharging(false)
                .build();

        PeriodicWorkRequest autoDeleteWorkRequest =
                new PeriodicWorkRequest.Builder(AutoDeleteWorker.class, 1, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "autoDeleteWork",
                ExistingPeriodicWorkPolicy.KEEP,
                autoDeleteWorkRequest
        );
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}