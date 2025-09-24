package in.udhaya.kaikanakku.data.repository;

import android.app.Application;

import in.udhaya.kaikanakku.data.db.AppDatabase;
import in.udhaya.kaikanakku.data.db.HistoryDao;
import in.udhaya.kaikanakku.data.db.HistoryEntry;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Repository for handling all data operations related to the HistoryEntry entity.
 * This class follows the Repository Pattern, abstracting the data source (Room database)
 * from the rest of the application. It provides a clean, reactive API for ViewModels
 * to interact with history data without needing to know the implementation details.
 * It is implemented as a singleton to ensure a single source of truth for history data.
 */
public class HistoryRepository {

    private final HistoryDao historyDao;
    private static volatile HistoryRepository INSTANCE;

    public enum SortOrder {
        BY_DATE,
        BY_SIZE_ASC,
        BY_SIZE_DESC
    }

    /**
     * Private constructor to enforce the singleton pattern.
     * @param application The application context, used to get a database instance.
     */
    private HistoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.historyDao = db.historyDao();
    }

    /**
     * Returns the singleton instance of the HistoryRepository.
     * Uses double-checked locking to ensure thread-safe initialization.
     * @param application The application context.
     * @return The singleton HistoryRepository instance.
     */
    public static HistoryRepository getInstance(final Application application) {
        if (INSTANCE == null) {
            synchronized (HistoryRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HistoryRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    // --- Reactive Read Operations from DAO ---
    // Room and RxJava handle the background threading for these Flowable queries.

    public Flowable<List<HistoryEntry>> getHistory(SortOrder sortOrder) {
        return switch (sortOrder) {
            case BY_SIZE_ASC -> historyDao.getAllEntriesSortedBySizeAsc();
            case BY_SIZE_DESC -> historyDao.getAllEntriesSortedBySizeDesc();
            default -> historyDao.getAllEntriesSortedByDate();
        };
    }

    public Flowable<List<HistoryEntry>> getFavoriteEntries() {
        return historyDao.getFavoriteEntries();
    }

    public Flowable<List<HistoryEntry>> searchHistory(String query) {
        // Add wildcards for the LIKE query in the DAO.
        return historyDao.searchHistory("%" + query + "%");
    }

    /**
     * Retrieves the 5 most recent history entries to display on the main converter screen.
     * @return A Flowable list of the 5 most recent entries.
     */
    public Flowable<List<HistoryEntry>> getRecentHistory() {
        return historyDao.getRecentEntries(5);
    }


    // --- Write Operations (Executed on a background thread) ---
    // These methods use the ExecutorService from the AppDatabase to ensure they
    // do not run on the main thread.

    public void insert(HistoryEntry historyEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (historyDao.entryExists(historyEntry.getInputText(), historyEntry.getOutputText()) == 0) {
                historyDao.insert(historyEntry);
            }
        });
    }

    public void update(HistoryEntry historyEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> historyDao.update(historyEntry));
    }

    public void delete(HistoryEntry historyEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> historyDao.delete(historyEntry));
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(historyDao::deleteAll);
    }

    /**
     * This method is specifically for the AutoDeleteWorker and runs synchronously
     * on the worker's background thread, so it doesn't need its own executor.
     * @param timestamp The cutoff time. Entries older than this will be deleted.
     */
    public void deleteOlderThan(long timestamp) {
        historyDao.deleteOlderThan(timestamp);
    }
}