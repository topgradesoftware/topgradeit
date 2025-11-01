package topgrade.parent.com.parentseeks.Teacher.Activity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TaskPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> listFragment = new ArrayList<>();
    private final List<String> tab_title = new ArrayList<>();

    public TaskPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return listFragment.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tab_title.get(position);
    }

    @Override
    public int getCount() {
        return listFragment.size();
    }

    public void AddFragment(Fragment fragmenttt,String titleee){
        listFragment.add(fragmenttt);
        tab_title.add(titleee);
    }
}