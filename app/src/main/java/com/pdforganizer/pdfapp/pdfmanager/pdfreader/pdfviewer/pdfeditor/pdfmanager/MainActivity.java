package com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.pdforganizer.pdfapp.pdfmanager.pdfreader.pdfviewer.pdfeditor.pdfmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    BottomNavigationView bottomNavigationView;
    NavController navController;
    NavHostFragment navHostFragment;
    DrawerLayout drawerLayout;
    ImageView drawer;
    public ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.gray));

//        ViewCompat.setOnApplyWindowInsetsListener(binding.drawer, (view, windowInsets) -> {
//            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
//            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
//            return WindowInsetsCompat.CONSUMED;
//        });

        bottomNavigationView = findViewById(R.id.btmNav);
        drawerLayout = findViewById(R.id.drawer);
        drawer = findViewById(R.id.drawers);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
        if (navController != null) {
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NavigationView navigationView = findViewById(R.id.navView);
                drawerLayout.openDrawer(navigationView);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}