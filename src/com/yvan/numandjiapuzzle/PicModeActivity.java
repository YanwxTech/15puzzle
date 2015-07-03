package com.yvan.numandjiapuzzle;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yvan.gamelayout.view.PicLayout;
import com.yvan.gamelayout.view.PicLayout.PicListener;
import com.yvan.gamemethods.MyDialog;

public class PicModeActivity extends Activity implements
		android.view.View.OnClickListener {
	private PicLayout picLayout;
	private ImageView current_imageView;
	private TextView mMoves;
	private TextView mTime;
	private TextView mTimeLeft;
	private Button pic_help;
	private Button btn_restart;
	private Button pause_resume;
	private Button btn_userDefined;
	private boolean isPausing = false;
	private final String IMAGE_TYPE = "image/*";
	private final int IMAGE_CODE = 0;
	private Button showCurrentImg;
	String path = null;
	String bmpath = null;
	Bitmap bm = null;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;
	private MyApplication myApplication;
	public static int[] columnTime = { 120, 240, 420, 650, 900, 1200 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_layout);
		myApplication = (MyApplication) getApplication();
		int num = myApplication.numModeColumn;
		boolean isGameMode = myApplication.isGameMode;

		mSharedPreferences = getSharedPreferences("gameRank", MODE_APPEND);
		mEditor = mSharedPreferences.edit();

		initView();

		if (isGameMode) {
			mTimeLeft.setText("剩余时间");
		} else {
			mTimeLeft.setText("已用时间");
		}
		pic_help.setOnClickListener(this);
		pause_resume.setOnClickListener(this);
		btn_userDefined.setOnClickListener(this);
		btn_restart.setOnClickListener(this);
		showCurrentImg.setOnClickListener(this);
		picLayout.setTimeEnabled(isGameMode);
		picLayout.setPicColumn(num);
		picLayout.setOnPicListener(new PicListener() {

			@Override
			public void timeChanged(int currentTime) {
				mTime.setText("" + currentTime);

			}

			@Override
			public void nextLevel(final int nextLevel) {
				int piclocation = picLayout.picColumn - 3;
				int minPicTime = PicLayout.minPicTime[piclocation];
				int minPicMoves = PicLayout.minPicMoves[piclocation];
				if (mSharedPreferences.getInt("picmoves" + piclocation, 0) == 0) {
					mEditor.putInt("picmoves" + piclocation, minPicMoves);
					mEditor.putInt("pictime" + piclocation,
							(columnTime[piclocation] - minPicTime));
					mEditor.commit();
				} else {
					if (minPicMoves < mSharedPreferences.getInt("picmoves"
							+ piclocation, 0)) {
						mEditor.putInt("picmoves" + piclocation, minPicMoves);
						mEditor.commit();
					}
					if ((columnTime[piclocation] - minPicTime) < mSharedPreferences
							.getInt("pictime" + piclocation, 0)) {
						mEditor.putInt("pictime" + piclocation,
								(columnTime[piclocation] - minPicTime));
						mEditor.commit();
					}
				}
				nextLevelShowDialog();

			}

			@Override
			public void gameOver() {
				gameOverDialog();

			}

			@Override
			public void movesChanged(int currentMoves) {
				mMoves.setText("" + currentMoves);
			}
		});

	}

	private void initView() {
		mMoves = (TextView) findViewById(R.id.current_moves);
		mTime = (TextView) findViewById(R.id.current_time);
		mTimeLeft = (TextView) findViewById(R.id.tmie_tip);
		pause_resume = (Button) findViewById(R.id.pause_resume);
		btn_userDefined = (Button) findViewById(R.id.btn_userDefined);
		btn_restart = (Button) findViewById(R.id.btn_restart);
		pic_help = (Button) findViewById(R.id.pic_help);
		picLayout = (PicLayout) findViewById(R.id.picLayout);
		showCurrentImg = (Button) findViewById(R.id.btn_showImg);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!isPausing) {
			picLayout.pause();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isPausing) {
			picLayout.resume();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pause_resume: {
			picLayout.pause();
			isPausing = true;
			AlertDialog.Builder pauselDialogBuider = new AlertDialog.Builder(
					PicModeActivity.this);
			pauselDialogBuider.setTitle("游戏暂停").setMessage("点击继续游戏或者选择新关卡");
			pauselDialogBuider.setPositiveButton("继续游戏", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					picLayout.resume();
					isPausing = false;

				}
			});
			pauselDialogBuider.setNegativeButton("阶数选择", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					picshowDialog(PicModeActivity.this,
							myApplication.isGameMode, R.layout.piclevel_list,
							R.id.picradiogroup, R.id.picradiogroup1);
				}
			});
			final Dialog dialog = pauselDialogBuider.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			dialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					picLayout.resume();
					isPausing = false;
				}
			});
			break;

		}
		case R.id.btn_restart:
			picLayout.reStart();
			break;
		case R.id.btn_userDefined:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType(IMAGE_TYPE);
			startActivityForResult(intent, IMAGE_CODE);
			break;
		case R.id.btn_showImg:
			LayoutInflater inflater = LayoutInflater.from(PicModeActivity.this);
			View view = inflater.inflate(R.layout.current_img, null);
			current_imageView = (ImageView) view
					.findViewById(R.id.current_imageView);
			current_imageView.setImageBitmap(PicLayout.showBitmap);
			MyDialog builder = new MyDialog(PicModeActivity.this, 0, 0, view,
					R.style.MyDialog);
			builder.show();
			break;
		case R.id.pic_help:
			Toast.makeText(this,
					"图片根据其宽高最小值进行裁剪，建议您先选择易于辨别的并裁剪好的图片再使用;PS：查看图片时，时间不停止哦！！！",
					Toast.LENGTH_LONG).show();
			break;

		}

	}

	public void nextLevelShowDialog() {
		AlertDialog.Builder nextLevelDialogBuider = new AlertDialog.Builder(
				PicModeActivity.this);
		nextLevelDialogBuider.setTitle("成功过关").setMessage("准备好新的挑战了吗？");
		nextLevelDialogBuider.setPositiveButton("继续挑战", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				picLayout.nextLevel();

			}
		});
		nextLevelDialogBuider.setNegativeButton("返回主界面", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();

			}
		});
		nextLevelDialogBuider.setCancelable(false);
		nextLevelDialogBuider.show();
	}

	public void gameOverDialog() {
		AlertDialog.Builder gameOverDialogBuider = new AlertDialog.Builder(
				PicModeActivity.this);

		gameOverDialogBuider.setTitle("游戏失败");
		gameOverDialogBuider.setMessage("不要气馁，再来一次！");
		gameOverDialogBuider.setPositiveButton("重新游戏", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				picLayout.reStart();
				picLayout.resume();

			}
		});
		gameOverDialogBuider.setNegativeButton("返回主界面", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();

			}
		});
		gameOverDialogBuider.setCancelable(false);
		gameOverDialogBuider.show();
	}

	public void picshowDialog(final Context context, final boolean isgameMode,
			int listId, int radiogroupId, int radiogroupId1) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(listId, null);
		// AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// builder.setView(view);
		// final Dialog dialog = builder.create();
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
				case R.id.picthreeOrder:
					checkedId_position = 3;
					break;
				case R.id.picfourOrder:
					checkedId_position = 4;
					break;
				case R.id.picfiveOrder:
					checkedId_position = 5;
					break;
				}
				picLayout
						.setPicColumn(myApplication.numModeColumn = checkedId_position);

				picLayout.reStart();
				picLayout.resume();
				radioGroup.setSelected(false);
				builder.cancel();
			}
		});
		radioGroup1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int checkedId_position = 0;
				switch (checkedId) {
				case R.id.picsixOrder:
					checkedId_position = 6;
					break;
				case R.id.picsevenOrder:
					checkedId_position = 7;
					break;
				case R.id.piceightOrder:
					checkedId_position = 8;
					break;
				}
				picLayout
						.setPicColumn(myApplication.numModeColumn = checkedId_position);
				picLayout.reStart();
				picLayout.resume();

				radioGroup1.setSelected(false);
				builder.cancel();
			}
		});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		ContentResolver resolver = getContentResolver();
		if (requestCode == IMAGE_CODE) {
			Uri originalUri = data.getData();
			try {
				bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(originalUri, proj, null, null,
						null);
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				path = cursor.getColumnName(column_index);
				bmpath = cursor.getString(column_index);
				if (bmpath != null) {
					picLayout.setUserDefined(true, bmpath);
				}
				Log.i("path", path);
				Log.i("bmpath", bmpath);
				Log.i("originalUri", originalUri + "");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
