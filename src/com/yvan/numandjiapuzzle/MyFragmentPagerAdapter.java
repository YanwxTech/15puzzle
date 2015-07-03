package com.yvan.numandjiapuzzle;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
	private List<Fragment> fragmentList;
	private List<String> titlelist;

	public MyFragmentPagerAdapter(FragmentManager fm,
			List<Fragment> fragmentList, List<String> titlelist) {
		super(fm);
		this.fragmentList = fragmentList;
		this.titlelist = titlelist;
	}

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		return fragmentList.get(arg0);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fragmentList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return titlelist.get(position);
	}
}