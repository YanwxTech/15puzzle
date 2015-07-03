package com.yvan.numandjiapuzzle;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Window;

public class RankActivity extends FragmentActivity implements
		OnPageChangeListener {

	private ViewPager rankViewPager;
	private List<String> titlelist;
	private PagerTabStrip tab;
	private List<Fragment> fragmentList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank_layout);

		rankViewPager = (ViewPager) findViewById(R.id.rank_viewPager);

		fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new NumRankFragment());
		fragmentList.add(new PicRankFragment());

		titlelist = new ArrayList<String>();
		titlelist.add("数字模式");
		titlelist.add("图片模式");

		tab = (PagerTabStrip) findViewById(R.id.rank_tab);
		tab.setBackgroundColor(Color.WHITE);
		tab.setDrawFullUnderline(false);
		tab.setTextColor(Color.GRAY);
		tab.setTabIndicatorColor(Color.GREEN);
		// tab.setVisibility(View.GONE);

		MyFragmentPagerAdapter fragmentAdapter = new MyFragmentPagerAdapter(
				getSupportFragmentManager(), fragmentList, titlelist);
		rankViewPager.setAdapter(fragmentAdapter);
		rankViewPager.setOnPageChangeListener(this);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
