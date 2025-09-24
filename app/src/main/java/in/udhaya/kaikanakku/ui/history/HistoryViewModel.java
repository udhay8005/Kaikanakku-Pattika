package in.udhaya.kaikanakku.ui.history;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveDataReactiveStreams;

import java.util.List;
import in.udhaya.kaikanakku.data.db.HistoryEntry;
import in.udhaya.kaikanakku.data.repository.HistoryRepository;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * ViewModel for the HistoryFragment. It is responsible for preparing and managing the data
 * for the UI. It handles all data operations, such as fetching, filtering, sorting, and
 * deleting history entries, by communicating with the HistoryRepository.
 */
public class HistoryViewModel extends AndroidViewModel {

    private final HistoryRepository historyRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // LiveData objects to hold the current state of UI filters.
    private final MutableLiveData<HistoryRepository.SortOrder> sortOrder = new MutableLiveData<>(HistoryRepository.SortOrder.BY_DATE);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> showFavoritesOnly = new MutableLiveData<>(false);

    // MediatorLiveData observes the filter LiveData objects and updates the final history list accordingly.
    private final MediatorLiveData<List<HistoryEntry>> filteredHistory = new MediatorLiveData<>();
    private LiveData<List<HistoryEntry>> currentSource;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        historyRepository = HistoryRepository.getInstance(application);
        currentSource = new MutableLiveData<>(); // Initialize with a dummy source

        // Add observers to the filter LiveData. Whenever a filter changes, updateDataSource() is called.
        filteredHistory.addSource(sortOrder, order -> updateDataSource());
        filteredHistory.addSource(searchQuery, query -> updateDataSource());
        filteredHistory.addSource(showFavoritesOnly, showFavs -> updateDataSource());
    }

    /**
     * This is the core logic for filtering and sorting. It removes the old data source,
     * determines the new data source from the repository based on the current filter values,
     * and then begins observing it.
     */
    private void updateDataSource() {
        // Stop observing the old LiveData source to prevent multiple streams from updating the UI.
        filteredHistory.removeSource(currentSource);

        String query = searchQuery.getValue();
        Boolean favsOnly = showFavoritesOnly.getValue();
        HistoryRepository.SortOrder order = sortOrder.getValue();

        // Determine the correct data stream from the repository based on active filters.
        if (query != null && !query.isEmpty()) {
            // Search query takes precedence over all other filters.
            currentSource = LiveDataReactiveStreams.fromPublisher(historyRepository.searchHistory(query));
        } else if (favsOnly != null && favsOnly) {
            // If not searching, check if the favorites filter is active.
            currentSource = LiveDataReactiveStreams.fromPublisher(historyRepository.getFavoriteEntries());
        } else {
            // If no other filters are active, just sort the full list.
            currentSource = LiveDataReactiveStreams.fromPublisher(historyRepository.getHistory(order != null ? order : HistoryRepository.SortOrder.BY_DATE));
        }

        // Start observing the new data source. When it emits data, update the UI's LiveData.
        filteredHistory.addSource(currentSource, filteredHistory::setValue);
    }

    // --- Public methods for the Fragment to interact with the ViewModel ---

    public LiveData<List<HistoryEntry>> getFilteredHistory() {
        return filteredHistory;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSortOrder(HistoryRepository.SortOrder order) {
        sortOrder.setValue(order);
    }

    public void toggleFavoritesFilter(boolean showOnlyFavorites) {
        showFavoritesOnly.setValue(showOnlyFavorites);
    }

    public void delete(HistoryEntry entry) {
        historyRepository.delete(entry);
    }

    public void insert(HistoryEntry entry) {
        historyRepository.insert(entry);
    }

    public void update(HistoryEntry entry) {
        historyRepository.update(entry);
    }

    public void clearAllHistory() {
        historyRepository.deleteAll();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear(); // Clean up any RxJava subscriptions.
    }
}
