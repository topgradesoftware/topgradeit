package topgrade.parent.com.parentseeks.Teacher.Activity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import topgrade.parent.com.parentseeks.R;


public class ComplainPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> listFragment = new ArrayList<>();
    private final List<String> tab_titlee = new ArrayList<>();

    public ComplainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return listFragment.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tab_titlee.get(position);
    }

    @Override
    public int getCount() {
        return listFragment.size();
    }
    public void AddFragmentComplain(Fragment fragment,String title){
        listFragment.add(fragment);
        tab_titlee.add(title);
    }
}