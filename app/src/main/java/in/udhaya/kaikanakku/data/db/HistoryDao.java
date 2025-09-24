package in.udhaya.kaikanakku.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Data Access Object (DAO) for the history_table.
 * This interface defines all the database operations (queries, inserts, updates, deletes)
 * that can be performed on the HistoryEntry table. Room will generate the necessary
 * implementation code at compile time. It uses RxJava's Flowable to provide reactive
 * streams of data that automatically update the UI when the underlying data changes.
 */
@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(HistoryEntry historyEntry);

    @Update
    void update(HistoryEntry historyEntry);

    @Delete
    void delete(HistoryEntry historyEntry);

    @Query("DELETE FROM history_table")
    void deleteAll();

    @Query("DELETE FROM history_table WHERE timestamp < :timestamp")
    void deleteOlderThan(long timestamp);

    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    Flowable<List<HistoryEntry>> getAllEntriesSortedByDate();

    @Query("SELECT * FROM history_table ORDER BY totalCm ASC")
    Flowable<List<HistoryEntry>> getAllEntriesSortedBySizeAsc();

    @Query("SELECT * FROM history_table ORDER BY totalCm DESC")
    Flowable<List<HistoryEntry>> getAllEntriesSortedBySizeDesc();

    @Query("SELECT * FROM history_table WHERE isFavorite = 1 ORDER BY timestamp DESC")
    Flowable<List<HistoryEntry>> getFavoriteEntries();

    @Query("SELECT * FROM history_table WHERE inputText LIKE :query OR outputText LIKE :query ORDER BY timestamp DESC")
    Flowable<List<HistoryEntry>> searchHistory(String query);

    /**
     * Retrieves the most recent history entries, limited by the given count.
     * This is a new method to support showing recent conversions on the main screen.
     * @param limit The maximum number of recent entries to retrieve.
     * @return A Flowable list of the most recent HistoryEntry objects.
     */
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC LIMIT :limit")
    Flowable<List<HistoryEntry>> getRecentEntries(int limit);
}