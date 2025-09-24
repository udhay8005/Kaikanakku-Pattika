package in.udhaya.kaikanakku.ui.about;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import in.udhaya.kaikanakku.R;

/**
 * A simple Fragment to display information about the application,
 * such as the version number and developer details. This screen is static
 * except for the version name, which is fetched dynamically from the package manager.
 */
public class AboutFragment extends Fragment {

    private static final String TAG = "AboutFragment";

    /**
     * A required empty public constructor for fragment instantiation.
     */
    public AboutFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment from the XML resource.
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * This is where we initialize the UI components.
     * @param view The View returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView versionTextView = view.findViewById(R.id.text_view_version);
        setAppVersion(versionTextView);
    }

    /**
     * Retrieves the application's version name from the PackageManager and sets it
     * on the provided TextView. This ensures the version is always up-to-date with
     * what is defined in the build.gradle file.
     *
     * @param textView The TextView where the version information will be displayed.
     */
    private void setAppVersion(TextView textView) {
        // It's good practice to get the context and check for nullability,
        // although in a fragment's lifecycle after onViewCreated, it's rarely null.
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context was null, cannot get package info.");
            textView.setText(getString(R.string.version_unknown));
            return;
        }

        try {
            // Get the PackageInfo for the current application.
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;

            // Format the string using a placeholder for better localization support.
            String versionText = getString(R.string.about_version_format, version);
            textView.setText(versionText);

        } catch (PackageManager.NameNotFoundException e) {
            // This exception is unlikely to happen for the app's own package,
            // but robust error handling is essential for production code.
            Log.e(TAG, "Could not get package info to display app version.", e);
            textView.setText(getString(R.string.version_unknown));
        }
    }
}
