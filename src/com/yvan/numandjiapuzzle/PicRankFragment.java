package com.yvan.numandjiapuzzle;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yvan.gamemethods.MyDialog;

public class PicRankFragment extends Fragment {
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pic_rank, container, false);
		initListView(view);
		mSharedPreferences = getActivity().getSharedPreferences("gameRank",
				getActivity().MODE_APPEND);
		mEditor = mSharedPreferences.edit();
		return view;
	}

	private void initListView(View view) {
		ListView pic_rank = (ListView) view.findViewById(R.id.pic_rank);
		String[] orders = new String[6];
		for (int i = 3; i < orders.length + 3; i++) {
			orders[i - 3] = i + "x" + i;
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this
				.getActivity().getApplicationContext(),
				R.layout.listview_text_style, orders);
		pic_rank.setAdapter(arrayAdapter);
		pic_rank.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showRankDialog(position);
			}
		});
		pic_rank.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				mEditor.putInt("pictime" + position, 0);
				mEditor.putInt("picmoves" + position, 0);
				mEditor.commit();
				Toast.makeText(getActivity(),
						"" + (position + 3) + "x" + (position + 3) + "的记录已重置",
						Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}

	public void showRankDialog(int position) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View dialogView = inflater.inflate(R.layout.rank_content, null);
		MyDialog builder = new MyDialog(getActivity(), 0, 0, dialogView,
				R.style.MyDialog);
		TextView minMoves = (TextView) dialogView
				.findViewById(R.id.num_minMoves);
		TextView minTime = (TextView) dialogView.findViewById(R.id.num_minTime);
		TextView setTime = (TextView) dialogView.findViewById(R.id.num_setTime);
		setTime.setText("" + PicModeActivity.columnTime[position]);
		minMoves.setText(""
				+ mSharedPreferences.getInt("picmoves" + position, 0));
		minTime.setText("" + mSharedPreferences.getInt("pictime" + position, 0));
		builder.show();
	}

}
