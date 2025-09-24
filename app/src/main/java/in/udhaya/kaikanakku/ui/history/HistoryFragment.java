package in.udhaya.kaikanakku.ui.history;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import in.udhaya.kaikanakku.R;
import in.udhaya.kaikanakku.data.db.HistoryEntry;
import in.udhaya.kaikanakku.data.repository.HistoryRepository;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnHistoryItemInteractionListener {

    private HistoryViewModel historyViewModel;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        setHasOptionsMenu(true); // This fragment has its own options menu
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupRecyclerView(view);
        setupObservers();
        setupItemTouchHelper();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_history);
        emptyView = view.findViewById(R.id.layout_empty_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        adapter = new HistoryAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        historyViewModel.getFilteredHistory().observe(getViewLifecycleOwner(), entries -> {
            adapter.submitList(entries);
            if (entries == null || entries.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });
    }

    private void setupItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                HistoryEntry entryToDelete = adapter.getCurrentList().get(position);
                historyViewModel.delete(entryToDelete);

                Snackbar.make(requireView(), R.string.entry_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> historyViewModel.insert(entryToDelete))
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                historyViewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_sort_date) {
            historyViewModel.setSortOrder(HistoryRepository.SortOrder.BY_DATE);
            return true;
        } else if (itemId == R.id.action_sort_size_asc) {
            historyViewModel.setSortOrder(HistoryRepository.SortOrder.BY_SIZE_ASC);
            return true;
        } else if (itemId == R.id.action_sort_size_desc) {
            historyViewModel.setSortOrder(HistoryRepository.SortOrder.BY_SIZE_DESC);
            return true;
        } else if (itemId == R.id.action_clear_all) {
            showClearAllConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFavoriteClicked(HistoryEntry entry) {
        // Create a new entry object to satisfy DiffUtil, as the original object is mutated.
        entry.setFavorite(!entry.isFavorite());
        historyViewModel.update(entry);
    }

    @Override
    public void onItemCopied(HistoryEntry entry, int position) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.clipboard_label), entry.getOutputText());
        clipboard.setPrimaryClip(clip);
        Snackbar.make(requireView(), R.string.result_copied, Snackbar.LENGTH_SHORT).show();

        // Trigger visual feedback
        adapter.showCopyHighlight(position);
        new Handler(Looper.getMainLooper()).postDelayed(() -> adapter.clearCopyHighlight(), 500); // 500ms highlight
    }

    @Override
    public void onItemReused(HistoryEntry entry) {
        Bundle args = new Bundle();
        String inputText = entry.getInputText();

        if (inputText.contains("cm") && !inputText.contains("kol")) {
            // This was a CM -> Kol conversion
            args.putBoolean("IS_KOL_TO_CM", false);
            args.putDouble("CM_TOTAL", entry.getTotalCm());
        } else {
            // This was a Kol -> CM conversion or a calculation
            args.putBoolean("IS_KOL_TO_CM", true);
            // Use regex to parse the Kol, Viral, and CM values from the input string
            Pattern p = Pattern.compile("(\\d+)\\s*kol|(\\d+)\\s*viral|(\\d*\\.?\\d+)\\s*cm");
            Matcher m = p.matcher(inputText);
            int kol = 0;
            int viral = 0;
            double cm = 0.0;
            while(m.find()){
                if(m.group(1) != null) kol = Integer.parseInt(Objects.requireNonNull(m.group(1)));
                if(m.group(2) != null) viral = Integer.parseInt(Objects.requireNonNull(m.group(2)));
                if(m.group(3) != null) cm = Double.parseDouble(Objects.requireNonNull(m.group(3)));
            }
            args.putInt("KOL", kol);
            args.putInt("VIRAL", viral);
            args.putDouble("CM", cm);
        }

        // Navigate back to the converter fragment with the pre-filled values
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.converterFragment, args);
    }

    private void showClearAllConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_clear_history_title)
                .setMessage(R.string.dialog_clear_history_message)
                .setPositiveButton(R.string.action_clear_all, (dialog, which) -> historyViewModel.clearAllHistory())
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(R.drawable.ic_delete_forever)
                .show();
    }
}