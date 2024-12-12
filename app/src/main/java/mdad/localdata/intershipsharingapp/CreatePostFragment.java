package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CreatePostFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FragmentStateAdapter pagerAdapter;

    private String[] titles = new String[]{"Internship", "Question"};
    private static final int NUM_PAGES = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_create_post, container, false);

        // Get references to views
        viewPager = rootView.findViewById(R.id.mypager);
        tabLayout = rootView.findViewById(R.id.tab_layout);

        // Set up the ViewPager2 Adapter
        pagerAdapter = new MyPagerAdapter(getActivity());
        viewPager.setAdapter(pagerAdapter);

        // Use TabLayoutMediator to link TabLayout with ViewPager2
        TabLayoutMediator tlm = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(titles[position]));
        tlm.attach();

        return rootView;
    }

    private class MyPagerAdapter extends FragmentStateAdapter {
        public MyPagerAdapter(FragmentActivity fa) {
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
}
