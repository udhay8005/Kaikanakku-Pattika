package in.udhaya.kaikanakku.ui.converter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import in.udhaya.kaikanakku.R;
import in.udhaya.kaikanakku.ui.history.HistoryAdapter;

/**
 * The main user interface for the converter feature. This fragment handles user input,
 * displays results, and observes a ConverterViewModel for state and business logic.
 * It now includes features like auto-saving, recent history display, and streamlined input.
 */
public class ConverterFragment extends Fragment {

    private ConverterViewModel viewModel;
    private TextInputEditText cmInput, kolInput, viralInput, kolCmInput;
    private LinearLayout cmToKolLayout, kolToCmLayout;
    private MaterialTextView resultText;
    private CardView resultCard;
    private SwitchMaterial modeSwitch;
    private Button convertButton;
    private RecyclerView recentHistoryRecyclerView;
    private HistoryAdapter recentHistoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ConverterViewModel.class);

        initializeViews(view);
        setupModeSwitch();
        setupConvertButton();
        setupInputAutoAdvance();
        setupRecentHistory();
        observeViewModel();
        handleArguments();
    }

    private void initializeViews(@NonNull View view) {
        cmInput = view.findViewById(R.id.edit_text_cm);
        kolToCmLayout = view.findViewById(R.id.layout_kol_to_cm);
        kolInput = kolToCmLayout.findViewById(R.id.edit_text_kol);
        viralInput = kolToCmLayout.findViewById(R.id.edit_text_viral);
        kolCmInput = kolToCmLayout.findViewById(R.id.edit_text_cm);
        cmToKolLayout = view.findViewById(R.id.layout_cm_to_kol);
        resultCard = view.findViewById(R.id.card_result);
        resultText = view.findViewById(R.id.text_view_result);
        modeSwitch = view.findViewById(R.id.switch_mode);
        convertButton = view.findViewById(R.id.button_convert);
        recentHistoryRecyclerView = view.findViewById(R.id.recycler_view_recent_history);
    }

    private void setupModeSwitch() {
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { // Kol to CM mode
                cmToKolLayout.setVisibility(View.GONE);
                kolToCmLayout.setVisibility(View.VISIBLE);
                modeSwitch.setText(R.string.mode_kol_to_cm);
            } else { // CM to Kol mode
                cmToKolLayout.setVisibility(View.VISIBLE);
                kolToCmLayout.setVisibility(View.GONE);
                modeSwitch.setText(R.string.mode_cm_to_kol);
            }
            clearInputsAndResult();
        });
    }

    private void setupConvertButton() {
        convertButton.setOnClickListener(v -> {
            try {
                if (modeSwitch.isChecked()) { // Kol to CM
                    int kol = Integer.parseInt(Objects.requireNonNull(kolInput.getText()).toString().isEmpty() ? "0" : kolInput.getText().toString());
                    int viral = Integer.parseInt(Objects.requireNonNull(viralInput.getText()).toString().isEmpty() ? "0" : viralInput.getText().toString());
                    double cm = Double.parseDouble(Objects.requireNonNull(kolCmInput.getText()).toString().isEmpty() ? "0.0" : kolCmInput.getText().toString());
                    viewModel.convertKolToCm(kol, viral, cm);
                } else { // CM to Kol
                    String cmText = Objects.requireNonNull(cmInput.getText()).toString();
                    if(cmText.isEmpty()){
                        Snackbar.make(requireView(), "Please enter a value.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    double cm = Double.parseDouble(cmText);
                    viewModel.convertCmToKol(cm);
                }
            } catch (NumberFormatException e) {
                Snackbar.make(requireView(), R.string.error_invalid_number_format, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void setupInputAutoAdvance() {
        kolInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) viralInput.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        viralInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) kolCmInput.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecentHistory() {
        recentHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Use the main HistoryAdapter. A separate listener can be created if different interactions are needed.
        recentHistoryAdapter = new HistoryAdapter(null); // Passing null as interactions are not needed here
        recentHistoryRecyclerView.setAdapter(recentHistoryAdapter);
    }

    private void observeViewModel() {
        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && !result.isEmpty()) {
                resultCard.setVisibility(View.VISIBLE);
                resultText.setText(result);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
                viewModel.clearError(); // Clear error after showing it
            }
        });

        viewModel.getRecentHistory().observe(getViewLifecycleOwner(), historyEntries -> {
            if (historyEntries != null) {
                recentHistoryAdapter.submitList(historyEntries);
            }
        });
    }

    /**
     * Checks for arguments passed from other fragments (e.g., HistoryFragment's "re-use" button)
     * and pre-fills the UI.
     */
    private void handleArguments() {
        Bundle args = getArguments();
        if (args != null) {
            boolean isKolToCm = args.getBoolean("IS_KOL_TO_CM", false);
            modeSwitch.setChecked(isKolToCm);

            if (isKolToCm) {
                kolInput.setText(String.valueOf(args.getInt("KOL", 0)));
                viralInput.setText(String.valueOf(args.getInt("VIRAL", 0)));
                kolCmInput.setText(String.valueOf(args.getDouble("CM", 0.0)));
            } else {
                cmInput.setText(String.valueOf(args.getDouble("CM_TOTAL", 0.0)));
            }
            // Automatically trigger conversion
            convertButton.performClick();
        }
    }


    private void clearInputsAndResult() {
        cmInput.setText("");
        kolInput.setText("");
        viralInput.setText("");
        kolCmInput.setText("");
        resultCard.setVisibility(View.GONE);
        resultText.setText("");
        viewModel.clearError();
    }
}