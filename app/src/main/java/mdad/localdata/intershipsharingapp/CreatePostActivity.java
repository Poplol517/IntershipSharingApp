package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CreatePostActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FragmentStateAdapter pagerAdapter;

    private String[] titles = new String[]{"Internship", "Question"};
    private static final int NUM_PAGES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Get references to views
        viewPager = findViewById(R.id.mypager);
        tabLayout = findViewById(R.id.tab_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up the ViewPager2 Adapter
        pagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Use TabLayoutMediator to link TabLayout with ViewPager2
        TabLayoutMediator tlm = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(titles[position]));
        tlm.attach();
    }

    private class MyPagerAdapter extends FragmentStateAdapter {
        public MyPagerAdapter(@NonNull AppCompatActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return CreateInternshipFragment.newInstance("fragment 1", null);
                case 1:
                    return CreateQuestionFragment.newInstance("fragment 2", null);
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button press
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // This will call the back stack and finish the activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Ensure the default back press behavior occurs
        // This will navigate back to the previous activity in the stack
    }
}
