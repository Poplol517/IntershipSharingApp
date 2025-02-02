package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ViewStatFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public ViewStatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_stat, container, false);

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        if (requireActivity() instanceof StaffMainActivity) { // Ensure it's attached to an activity
            StaffMainActivity activity = (StaffMainActivity) requireActivity();
            activity.setSupportActionBar(toolbar); // Set the toolbar as ActionBar
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }


        // Setup ViewPager2 with a FragmentStateAdapter
        viewPager.setAdapter(new TabAdapter(this));

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("User Statistics");
                    break;
                case 1:
                    tab.setText("Community Statistics");
                    break;
            }
        }).attach();

        return view;
    }

    private class TabAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {

        public TabAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ViewUserStatFragment(); // Fragment displaying Pie Chart
                case 1:
                    return new ViewCommunityStatFragment(); // Fragment displaying Bar Chart
                default:
                    return new Fragment(); // Default case (optional)
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Number of tabs
        }
    }
}
