package arsi.dev.kriptofoni;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import arsi.dev.kriptofoni.Fragments.AlertsFragment;
import arsi.dev.kriptofoni.Fragments.MainFragment;
import arsi.dev.kriptofoni.Fragments.MoreFragment;
import arsi.dev.kriptofoni.Fragments.NewsFragment;
import arsi.dev.kriptofoni.Fragments.PortfolioFragment;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
        bottomNavigationView.getMenu().getItem(0).setEnabled(false);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    int selectedId = 0;

                    switch (menuItem.getItemId()) {
                        case R.id.nav_main:
                            selectedFragment = new MainFragment();
                            selectedId = 0;
                            break;
                        case R.id.nav_portfolio:
                            selectedFragment = new PortfolioFragment();
                            selectedId = 1;
                            break;
                        case R.id.nav_alerts:
                            selectedFragment = new AlertsFragment();
                            selectedId = 2;
                            break;
                        case R.id.nav_news:
                            selectedFragment = new NewsFragment();
                            selectedId = 3;
                            break;
                        case R.id.nav_more:
                            selectedFragment = new MoreFragment();
                            selectedId = 4;
                            break;
                    }

                    for (int i = 0; i < 5; i++) {
                        if (i != selectedId) {
                            bottomNavigationView.getMenu().getItem(i).setEnabled(true);
                        }
                    }
                    bottomNavigationView.getMenu().getItem(selectedId).setEnabled(false);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                    return true;
                }
            };
}
