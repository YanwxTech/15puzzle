package com.yvan.numandjiapuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.yvan.gamelayout.view.NumLayout;
import com.yvan.gamelayout.view.NumLayout.NumListener;
import com.yvan.gamemethods.MyDialog;

public class NumModeActivity extends Activity implements
		android.view.View.OnClickListener {
	private NumLayout numLayout;
	private TextView numMoves;
	private TextView numTime;
	private TextView timeLeft;
	private Button btn_numrestart;
	private Button btn_numpause;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;
	private boolean isPausing = false;
	String path = null;
	String bmpath = null;
	Bitmap bm = null;
	private MyApplication myApplication;
	public static int[] columnTime = { 60, 90, 150, 300, 500, 720, 1000, 1200 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.num_layout);
		myApplication = (MyApplication) getApplication();
		int num = myApplication.numModeColumn;
		boolean isgameMode = myApplication.isGameMode;

		mSharedPreferences = getSharedPreferences("gameRank", MODE_APPEND);
		mEditor = mSharedPreferences.edit();

		numMoves = (TextView) findViewById(R.id.numcurrent_moves);
		numTime = (TextView) findViewById(R.id.numcurrent_time);
		timeLeft = (TextView) findViewById(R.id.numtmie_tip);
		btn_numpause = (Button) findViewById(R.id.btn_numpause);
		btn_numrestart = (Button) findViewById(R.id.btn_numrestart);
		numLayout = (NumLayout) findViewById(R.id.numLayout);
		btn_numpause.setOnClickListener(this);
		btn_numrestart.setOnClickListener(this);

		if (isgameMode) {
			timeLeft.setText("剩余时间");
		} else {
			timeLeft.setText("已用时间");
		}
		numLayout.setNumColumn(num);
		numLayout.setTimeEnabled(isgameMode);
		numLayout.setOnNumListener(new NumListener() {

			@Override
			public void timeChanged(int currentTime) {
				numTime.setText("" + currentTime);

			}

			@Override
			public void nextLevel(final int nextLevel) {
				int numlocation = numLayout.numColumn - 3;
				int minNumTime = NumLayout.minNumTime[numlocation];
				int minNumMoves = NumLayout.minNumMoves[numlocation];
				if (mSharedPreferences.getInt("nummoves" + numlocation, 0) == 0) {
					mEditor.putInt("nummoves" + numlocation, minNumMoves);
					mEditor.putInt("numtime" + numlocation,
							(columnTime[numlocation] - minNumTime));
					mEditor.commit();
				} else {
					if (minNumMoves < mSharedPreferences.getInt("nummoves"
							+ numlocation, 0)) {
						mEditor.putInt("nummoves" + numlocation, minNumMoves);
						mEditor.commit();
					}
					if ((columnTime[numlocation] - minNumTime) < mSharedPreferences
							.getInt("numtime" + numlocation, 0)) {
						mEditor.putInt("numtime" + numlocation,
								(columnTime[numlocation] - minNumTime));
						mEditor.commit();
					}
				}
				AlertDialog.Builder nextLevelDialogBuider = new AlertDialog.Builder(
						NumModeActivity.this);
				nextLevelDialogBuider.setTitle("成功过关").setMessage("准备好新的挑战了吗？");
				nextLevelDialogBuider.setPositiveButton("继续挑战",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								numLayout.nextLevel();

							}
						});
				nextLevelDialogBuider.setNegativeButton("返回主界面",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();

							}
						});
				nextLevelDialogBuider.setCancelable(false);
				nextLevelDialogBuider.show();

			}

			@Override
			public void gameOver() {
				AlertDialog.Builder gameOverDialogBuider = new AlertDialog.Builder(
						NumModeActivity.this);

				gameOverDialogBuider.setTitle("游戏失败");
				gameOverDialogBuider.setMessage("不要气馁，再来一次！");
				gameOverDialogBuider.setPositiveButton("重新游戏",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								numLayout.reStart();
								numLayout.resume();
							}
						});
				gameOverDialogBuider.setNegativeButton("返回主界面",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();

							}
						});
				gameOverDialogBuider.setCancelable(false);
				gameOverDialogBuider.show();

			}

			@Override
			public void movesChanged(int currentMoves) {
				numMoves.setText("" + currentMoves);
			}
		});

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!isPausing) {
			numLayout.pause();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isPausing) {
			numLayout.resume();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_numpause: {
			numLayout.pause();
			isPausing = true;
			AlertDialog.Builder pauselDialogBuider = new AlertDialog.Builder(
					NumModeActivity.this);
			pauselDialogBuider.setTitle("游戏暂停").setMessage("点击继续游戏或者选择新关卡");
			pauselDialogBuider.setPositiveButton("继续游戏", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					numLayout.resume();
					isPausing = false;

				}
			});
			pauselDialogBuider.setNegativeButton("阶数选择", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					numshowDialog(NumModeActivity.this,
							myApplication.isGameMode, R.layout.numlevel_list,
							R.id.radiogroup, R.id.radiogroup1);

				}
			});
			final Dialog dialog = pauselDialogBuider.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			dialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					numLayout.resume();
					isPausing = false;
				}
			});
			break;

		}
		case R.id.btn_numrestart:
			numLayout.reStart();
			break;

		}

	}

	public void numshowDialog(final Context context, final boolean isgameMode,
			int listId, int radiogroupId, int radiogroupId1) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(listId, null);
		final MyDialog builder = new MyDialog(context, 0, 0, view,
				R.style.MyDialog);
		builder.setCancelable(false);
		final RadioGroup radioGroup;
		radioGroup = (RadioGroup) view.findViewById(radiogroupId);
		final RadioGroup radioGroup1 = (RadioGroup) view
				.findViewById(radiogroupId1);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int checkedId_position = 0;
				switch (checkedId) {
				case R.id.threeOrder:
					checkedId_position = 3;
					break;
				case R.id.fourOrder:
					checkedId_position = 4;
					break;
				case R.id.fiveOrder:
					checkedId_position = 5;
					break;
				case R.id.sixOrder:
					checkedId_position = 6;
					break;
				}
				numLayout
						.setNumColumn(myApplication.numModeColumn = checkedId_position);

				numLayout.reStart();
				numLayout.resume();
				radioGroup.setSelected(false);
				builder.cancel();
			}
		});
		radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int checkedId_position = 0;
				switch (checkedId) {
				case R.id.sevenOrder:
					checkedId_position = 7;
					break;
				case R.id.eightOrder:
					checkedId_position = 8;
					break;
				case R.id.nineOrder:
					checkedId_position = 9;
					break;
				case R.id.tenOrder:
					checkedId_position = 10;
					break;
				}
				numLayout
						.setNumColumn(myApplication.numModeColumn = checkedId_position);
				numLayout.reStart();
				numLayout.resume();

				radioGroup1.setSelected(false);
				builder.cancel();
			}
		});
		builder.show();
	}

}
