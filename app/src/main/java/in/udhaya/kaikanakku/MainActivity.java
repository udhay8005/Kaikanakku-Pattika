package in.udhaya.kaikanakku;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import in.udhaya.kaikanakku.data.repository.SettingsRepository;
import in.udhaya.kaikanakku.util.LocaleHelper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * This method is crucial for setting the app's language.
     * It's called BEFORE onCreate(), ensuring that the correct locale is applied
     * as the activity is being constructed.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        // We get the saved language preference here.
        // The blockingFirst() call is one of the few acceptable uses of a blocking
        // operation on the main thread because the UI cannot be drawn without this information.
        SettingsRepository settingsRepository = SettingsRepository.getInstance(newBase);
        String language = settingsRepository.getLanguage().blockingFirst("en"); // Default to English
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            throw new IllegalStateException("NavHostFragment not found in the layout. Check activity_main.xml.");
        }

        appBarConfiguration = new AppBarConfiguration.Builder(R.id.converterFragment).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Listen for language changes to recreate the activity.
        SettingsRepository settingsRepository = SettingsRepository.getInstance(this);
        disposables.add(settingsRepository.getLanguage()
                .skip(1) // Important: Skip the initial value on first launch to avoid a pointless reload.
                .distinctUntilChanged() // Important: Only proceed if the language value has actually changed.
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lang -> {
                    // FIX: We removed the "if (savedInstanceState == null)" condition.
                    // Now, recreate() will be called every time the language changes,
                    // forcing the app to reload with the correct (e.g., Malayalam) strings.
                    recreate();
                }, throwable -> {
                    Log.e("MainActivity", "Error observing language changes", throwable);
                }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Let the NavigationUI handle menu item clicks first.
        // This will navigate to the correct fragment if the menu item's ID matches a destination in the nav graph.
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // This ensures the Up button (back arrow in the toolbar) works correctly with the Navigation Component.
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear(); // Always clear disposables to prevent memory leaks.
    }
}