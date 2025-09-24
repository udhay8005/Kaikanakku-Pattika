package in.udhaya.kaikanakku.ui.calculator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import in.udhaya.kaikanakku.R;
import in.udhaya.kaikanakku.util.CalculatorUtils;

/**
 * A fragment that provides a UI for users to perform addition, subtraction, and multiplication on
 * measurements in the Kol/Viral/cm system. It handles user input, validation,
 * and delegates calculation logic to the CalculatorViewModel.
 */
public class CalculatorFragment extends Fragment {

    private CalculatorViewModel viewModel;
    private EditText kolInputA, viralInputA, cmInputA;
    private EditText kolInputB, viralInputB, cmInputB;
    private EditText kolInputMultiply, viralInputMultiply, cmInputMultiply;
    private EditText multiplierInput;
    private Button addButton, subtractButton, multiplyButton;
    private CardView resultCard;
    private TextView resultTextView;
    private TabLayout tabLayout;
    private LinearLayout addSubtractLayout, multiplyLayout;

    public CalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);

        initializeViews(view);
        setupClickListeners();
        setupObservers();
        setupInputAutoAdvance();
        setupTabLayout();
        setupInputValidation();
    }

    /**
     * Initializes all UI components from the layout file.
     * @param view The root view of the fragment.
     */
    private void initializeViews(@NonNull View view) {
        View inputsA = view.findViewById(R.id.inputs_a);
        kolInputA = inputsA.findViewById(R.id.edit_text_kol);
        viralInputA = inputsA.findViewById(R.id.edit_text_viral);
        cmInputA = inputsA.findViewById(R.id.edit_text_cm);

        View inputsB = view.findViewById(R.id.inputs_b);
        kolInputB = inputsB.findViewById(R.id.edit_text_kol);
        viralInputB = inputsB.findViewById(R.id.edit_text_viral);
        cmInputB = inputsB.findViewById(R.id.edit_text_cm);

        View inputsMultiply = view.findViewById(R.id.inputs_multiply);
        kolInputMultiply = inputsMultiply.findViewById(R.id.edit_text_kol);
        viralInputMultiply = inputsMultiply.findViewById(R.id.edit_text_viral);
        cmInputMultiply = inputsMultiply.findViewById(R.id.edit_text_cm);
        multiplierInput = view.findViewById(R.id.edit_text_multiplier);

        addButton = view.findViewById(R.id.button_add);
        subtractButton = view.findViewById(R.id.button_subtract);
        multiplyButton = view.findViewById(R.id.button_multiply);
        resultCard = view.findViewById(R.id.card_result);
        resultTextView = view.findViewById(R.id.text_view_calc_result);
        tabLayout = view.findViewById(R.id.tab_layout);
        addSubtractLayout = view.findViewById(R.id.add_subtract_layout);
        multiplyLayout = view.findViewById(R.id.multiply_layout);
    }

    /**
     * Sets up the click listeners for the Add, Subtract and Multiply buttons.
     */
    private void setupClickListeners() {
        addButton.setOnClickListener(v -> performCalculation(CalculatorViewModel.Operation.ADD));
        subtractButton.setOnClickListener(v -> performCalculation(CalculatorViewModel.Operation.SUBTRACT));
        multiplyButton.setOnClickListener(v -> performCalculation(CalculatorViewModel.Operation.MULTIPLY));
    }

    /**
     * Sets up observers on the ViewModel's LiveData to update the UI
     * in response to data changes (new results, errors, or save statuses).
     */
    private void setupObservers() {
        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            resultCard.setVisibility(View.VISIBLE);
            resultTextView.setText(result);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                resultCard.setVisibility(View.GONE);
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
                viewModel.clearError(); // Clear error after showing it
            }
        });

        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                // A subtle confirmation that the result was saved.
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Implements the auto-focus feature to move the cursor automatically
     * to the next field once the current field's max length is reached.
     */
    private void setupInputAutoAdvance() {
        // For Value A
        kolInputA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) { // Max length for Kol
                    viralInputA.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        viralInputA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) { // Max length for Viral
                    cmInputA.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // For Value B
        kolInputB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) { // Max length for Kol
                    viralInputB.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        viralInputB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) { // Max length for Viral
                    cmInputB.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // For Multiply
        kolInputMultiply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) { // Max length for Kol
                    viralInputMultiply.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        viralInputMultiply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) { // Max length for Viral
                    cmInputMultiply.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    addSubtractLayout.setVisibility(View.VISIBLE);
                    multiplyLayout.setVisibility(View.GONE);
                } else {
                    addSubtractLayout.setVisibility(View.GONE);
                    multiplyLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * Gathers, parses, and validates user input before triggering the calculation in the ViewModel.
     * @param operation The calculation operation to perform.
     */
    private void performCalculation(CalculatorViewModel.Operation operation) {
        resultCard.setVisibility(View.GONE);

        if (operation == CalculatorViewModel.Operation.MULTIPLY) {
            CalculatorUtils.Measurement measurement = readAndValidateMeasurement(kolInputMultiply, viralInputMultiply, cmInputMultiply);
            if (measurement == null) return; // Validation failed

            double multiplier;
            try {
                String multiplierStr = multiplierInput.getText().toString();
                if (TextUtils.isEmpty(multiplierStr)) {
                    viewModel.calculate(operation, measurement, 0);
                    return;
                }
                multiplier = Double.parseDouble(multiplierStr);
            } catch (NumberFormatException e) {
                Snackbar.make(requireView(), R.string.error_invalid_multiplier, Snackbar.LENGTH_LONG).show();
                return;
            }

            viewModel.calculate(operation, measurement, multiplier);

        } else {
            CalculatorUtils.Measurement measurementA = readAndValidateMeasurement(kolInputA, viralInputA, cmInputA);
            if (measurementA == null) return; // Validation failed

            CalculatorUtils.Measurement measurementB = readAndValidateMeasurement(kolInputB, viralInputB, cmInputB);
            if (measurementB == null) return; // Validation failed

            viewModel.calculate(operation, measurementA, measurementB);
        }
    }

    /**
     * Reads the input from a set of EditText fields, parses them into a Measurement object,
     * and validates the Viral and cm values.
     * @return A valid Measurement object, or null if parsing or validation fails.
     */
    private CalculatorUtils.Measurement readAndValidateMeasurement(EditText kolEditText, EditText viralEditText, EditText cmEditText) {
        int kol = 0;
        int viral = 0;
        double cm = 0.0;

        try {
            String kolStr = kolEditText.getText().toString();
            if (!TextUtils.isEmpty(kolStr)) {
                kol = Integer.parseInt(kolStr);
            }

            String viralStr = viralEditText.getText().toString();
            if (!TextUtils.isEmpty(viralStr)) {
                viral = Integer.parseInt(viralStr);
            }

            String cmStr = cmEditText.getText().toString();
            if (!TextUtils.isEmpty(cmStr)) {
                cm = Double.parseDouble(cmStr);
            }

            // --- Input Validation ---
            if (viral > 23) {
                Snackbar.make(requireView(), R.string.error_invalid_viral_input, Snackbar.LENGTH_LONG).show();
                viralEditText.requestFocus();
                viralEditText.setError(getString(R.string.error_invalid_viral_input));
                return null;
            }

            if (cm >= 3.0) {
                Snackbar.make(requireView(), R.string.error_invalid_kol_cm_input, Snackbar.LENGTH_LONG).show();
                cmEditText.requestFocus();
                cmEditText.setError(getString(R.string.error_invalid_kol_cm_input));
                return null;
            }

        } catch (NumberFormatException e) {
            Snackbar.make(requireView(), R.string.error_invalid_number_format, Snackbar.LENGTH_LONG).show();
            return null;
        }

        return new CalculatorUtils.Measurement(kol, viral, cm);
    }

    private void setupInputValidation() {
        addDecimalValidation(cmInputA);
        addDecimalValidation(cmInputB);
        addDecimalValidation(cmInputMultiply);
        addDecimalValidation(multiplierInput);
    }

    private void addDecimalValidation(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.indexOf('.') != text.lastIndexOf('.')) {
                    editText.setText(text.substring(0, text.length() - 1));
                    editText.setSelection(editText.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
