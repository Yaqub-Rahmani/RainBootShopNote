package com.example.rainbootshopnote;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.rainbootshopnote.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

ActivityMainBinding binding;
private Fragment currentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.bottomNavigationView.setBackgroundColor(Color.TRANSPARENT);
        setContentView(binding.getRoot());
        loadFragment(new HomeFragment());
        currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

        //binding.bottomNavigationView.getMenu().setGroupCheckable(0,false,true);
        //binding.bottomNavigationView.setSelectedItemId(-1);
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.ihome) {
                loadFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.iuser) {
                loadFragment(new UserFragment());
            } else if (item.getItemId() == R.id.ishare) {
                loadFragment(new SaleFragment());
            } else if (item.getItemId() == R.id.isetting) {
                loadFragment(new CashCoinsFragment());
            } else {
                loadFragment(new HomeFragment());
            }
            return true;
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SheetDialogFragment sheetDialogFragment = new SheetDialogFragment();
                sheetDialogFragment.show(getSupportFragmentManager(), sheetDialogFragment.getTag());
            }
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void refreshMainActivity() {
        recreate();
    }

    // Şu an açık olan fragment'i yeniden yükleme metodu
    public void updateCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);

        if (currentFragment != null && currentFragment instanceof FragmentUpdater) {
            ((FragmentUpdater) currentFragment).onFragmentUpdated();
        }
    }

    // Arayüz ekledik, fragment'lerin bu metodu implemente edebilmesi için
    public interface FragmentUpdater {
        void onFragmentUpdated();
    }
}
