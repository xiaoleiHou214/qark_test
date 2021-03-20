package com.example.qark_test;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.qark_test.customintent.CreateCustomIntentActivity;
import com.example.qark_test.exportedcomponent.ExportedComponentsFragment;
import com.example.qark_test.fileborwser.FileBrowserFragment;
import com.example.qark_test.intentsniffer.BroadcastIntentSnifferFragment;
import com.example.qark_test.tapjacking.TapJackingExploitFragment;
import com.example.qark_test.webviewtests.WebViewTestsActivityFragment;
import com.google.android.material.navigation.NavigationView;

public class TopLevelActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;

    public static String PACKAGE_NAME = "com.secbro.qark";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_level);
        PACKAGE_NAME = getApplicationContext().getPackageName();

        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.setDrawerListener(mDrawerToggle);

        // Find our drawer view
        mNavigationView = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(mNavigationView);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch(menuItem.getItemId()) {
            case R.id.nav_broadcast_intent_sniffer:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, BroadcastIntentSnifferFragment.newInstance())
                        .commit();
                break;
            case R.id.nav_exported_components:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new ExportedComponentsFragment())
                        .commit();
                break;
            case R.id.nav_tap_jacking:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new TapJackingExploitFragment())
                        .commit();
                break;
            case R.id.nav_web_view_tests:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new WebViewTestsActivityFragment())
                        .commit();
                break;
            case R.id.nav_file_browser:
                Bundle args = new Bundle();
                args.putString(FileBrowserFragment.INTENT_ACTION_SELECT_FILE, FileBrowserFragment.INTENT_ACTION_SELECT_FILE);

                Fragment instance = FileBrowserFragment.newInstance();
                instance.setArguments(args);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, instance)
                        .commit();
                break;
            case R.id.nav_custom_intent:
                Intent createNewIntent = new Intent(this, CreateCustomIntentActivity.class);
                startActivity(createNewIntent);
            default:
                //TODO:
        }

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
