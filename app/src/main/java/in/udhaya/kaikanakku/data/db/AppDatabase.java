package in.udhaya.kaikanakku.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main database class for the application, built using AndroidX Room.
 * This class is a singleton to ensure only one instance of the database is ever
 * created and used throughout the application's lifecycle, which is a critical
 * performance and stability best practice.
 *
 * It defines the database configuration and serves as the main access point to the
 * persisted data.
 */
@Database(entities = {HistoryEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Abstract method to get the Data Access Object (DAO) for the HistoryEntry table.
    // Room will generate the implementation for this method at compile time.
    public abstract HistoryDao historyDao();

    // A volatile instance of the database ensures that changes to the INSTANCE variable
    // are immediately visible to all threads. This is crucial for the singleton pattern.
    private static volatile AppDatabase INSTANCE;

    // A fixed-size thread pool for running database write operations asynchronously.
    // This prevents database operations (which can be slow) from blocking the main UI thread,
    // which would cause the app to freeze or crash.
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Returns the singleton instance of the AppDatabase.
     * If the instance does not exist, it is created in a thread-safe manner using a
     * synchronized block (double-checked locking).
     *
     * @param context The application context, used to build the database.
     * @return The singleton AppDatabase instance.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            // Use a synchronized block to prevent race conditions during initialization.
            synchronized (AppDatabase.class) {
                // Double-check if the instance is still null after acquiring the lock.
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "kaikanakku_database")
                            // In a production app with schema changes (e.g., adding a new column
                            // in version 2), a migration strategy would be added here using .addMigrations().
                            // For version 1, this is sufficient.
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

