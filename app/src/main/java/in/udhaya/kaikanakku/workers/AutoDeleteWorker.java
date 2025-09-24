package in.udhaya.kaikanakku.workers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import in.udhaya.kaikanakku.data.repository.HistoryRepository;
import in.udhaya.kaikanakku.data.repository.SettingsRepository;
import io.reactivex.rxjava3.core.Flowable;

/**
 * A background worker, managed by Android's WorkManager, that periodically deletes old entries
 * from the history database. This task is designed to be battery-efficient and will run
 * approximately once a day, even if the app is not open.
 */
public class AutoDeleteWorker extends Worker {

    private static final String TAG = "AutoDeleteWorker";

    private final HistoryRepository historyRepository;
    private final SettingsRepository settingsRepository;

    public AutoDeleteWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // It's safe to instantiate repositories here, as the worker's constructor and
        // doWork() method are both called on a background thread provided by WorkManager.
        Application application = (Application) context.getApplicationContext();
        historyRepository = HistoryRepository.getInstance(application);
        settingsRepository = SettingsRepository.getInstance(application);
    }

    /**
     * The main entry point for the worker's execution. This method is called by WorkManager
     * on a background thread when the defined constraints are met.
     *
     * @return The result of the work, indicating success, failure, or a need to retry.
     */
    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Auto-delete background worker started.");

        try {
            // The SettingsRepository provides a reactive Flowable stream for the preference.
            // Since the doWork() method is synchronous, we must block the current background
            // thread to get the latest value from the DataStore. This is a safe and intended
            // use of blocking calls within a WorkManager worker.
            Flowable<Integer> daysFlowable = settingsRepository.getAutoDeleteDays();
            Integer daysToKeep = daysFlowable.blockingFirst(); // Get the current setting

            // The user's setting determines if the deletion should proceed.
            // A value of 0 indicates that auto-delete is disabled.
            if (daysToKeep == null || daysToKeep <= 0) {
                Log.i(TAG, "Auto-delete is disabled by user setting (days set to 0). Worker finishing successfully.");
                return Result.success();
            }

            Log.i(TAG, "Proceeding with auto-delete. Will remove entries older than " + daysToKeep + " days.");

            // Calculate the cutoff timestamp. Any history entry with a timestamp older than
            // this will be deleted.
            long cutoffMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysToKeep);

            // Execute the delete operation via the repository. This is a blocking database call
            // which is safe to run here on the worker's background thread.
            historyRepository.deleteOlderThan(cutoffMillis);

            Log.i(TAG, "Successfully deleted old history entries. Worker finishing.");
            return Result.success();

        } catch (Exception e) {
            // If any unexpected error occurs (e.g., a database issue), log the error
            // and return Result.failure(). WorkManager will then retry the task later
            // according to its backoff policy.
            Log.e(TAG, "An error occurred during the auto-delete work.", e);
            return Result.failure();
        }
    }
}
