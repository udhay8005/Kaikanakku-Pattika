package in.udhaya.kaikanakku.ui.help;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import in.udhaya.kaikanakku.R;

/**
 * A simple Fragment to display help and instructional information to the user.
 * The content is static and is loaded from string resources to facilitate easy
 * updates and support for multiple languages (localization).
 */
public class HelpFragment extends Fragment {

    /**
     * A required empty public constructor for fragment instantiation by the Android framework.
     */
    public HelpFragment() {
        // Required empty public constructor
    }

    /**
     * Called by the system to have the fragment instantiate its user interface view.
     * This is where the layout for the fragment is inflated.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the static layout associated with this fragment.
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state
     * has been restored into the view. This is the appropriate place to set up the initial
     * state of the fragment's view.
     *
     * @param view The View returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView helpTextView = view.findViewById(R.id.text_view_help_content);

        // The actual help text is set in the layout file (fragment_help.xml) using
        // the android:text="@string/help_content_placeholder" attribute.
        // This is the best practice for localization.

        // Set the MovementMethod to LinkMovementMethod. This allows any URLs or
        // other links embedded in the string resource (using HTML <a> tags) to be clickable.
        helpTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
