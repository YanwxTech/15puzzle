package com.yvan.numandjiapuzzle;

import java.io.IOException;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yvan.gamemethods.MyDialog;

public class MainActivity extends Activity implements OnClickListener {
	private Button num_game;
	private Button num_pra;
	private Button pic_game;
	private Button pic_pra;
	private ImageView settings;
	private static MyApplication myApplication;
	public static MediaPlayer mediaPlayer;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;
	private static final int BGM_STOP = 101;
	private static final int BGM_START = 102;

	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BGM_STOP:
				mediaPlayer.stop();
				break;
			case BGM_START: {
				playBGM();
				break;
			}
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		myApplication = (MyApplication) getApplication();
		int num = myApplication.numModeColumn;

		initView();
		mSharedPreferences = getSharedPreferences("gameRank", MODE_APPEND);
		boolean firstTime = mSharedPreferences.getBoolean("firstTime", true);
		Log.i("firstTime", "" + firstTime);
		mEditor = mSharedPreferences.edit();
		if (firstTime) {
			initGameData();
			mEditor.putBoolean("firstTime", false);
			mEditor.putBoolean("isSoundsOn", true);
			mEditor.commit();
		}

		num_game.setOnClickListener(this);
		num_pra.setOnClickListener(this);
		pic_game.setOnClickListener(this);
		pic_pra.setOnClickListener(this);
		settings.setOnClickListener(this);

		playBGM();

	}

	public void initGameData() {

		for (int i = 0; i < 8; i++) {
			mEditor.putInt("numtime" + i, 0);
			mEditor.putInt("nummoves" + i, 0);
			mEditor.putInt("pictime" + i, 0);
			mEditor.putInt("picmoves" + i, 0);
			mEditor.commit();
		}

	}

	public void playBGM() {
		mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.game_bgm);
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mSharedPreferences.getBoolean("isSoundsOn", true)) {
			mediaPlayer.start();
		}
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.start();
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				try {
					mp.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}

	private void initView() {

		settings = (ImageView) findViewById(R.id.settings);
		num_game = (Button) findViewById(R.id.num_game);
		num_pra = (Button) findViewById(R.id.num_pra);
		pic_game = (Button) findViewById(R.id.pic_game);
		pic_pra = (Button) findViewById(R.id.pic_pra);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setMessage("真的要退出游戏吗？");
			builder.setPositiveButton("退出",
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					});
			builder.setNegativeButton("取消",
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
			builder.show();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		Intent numintent = new Intent(MainActivity.this, NumModeActivity.class);
		Intent picintent = new Intent(MainActivity.this, PicModeActivity.class);
		switch (v.getId()) {
		case R.id.num_game: {

			showDialog(MainActivity.this, true, R.layout.numlevel_list,
					R.id.radiogroup, R.id.radiogroup1, numintent);
			break;
		}
		case R.id.num_pra: {
			showDialog(MainActivity.this, false, R.layout.numlevel_list,
					R.id.radiogroup, R.id.radiogroup1, numintent);
			break;
		}
		case R.id.pic_game: {
			showDialog(MainActivity.this, true, R.layout.piclevel_list,
					R.id.picradiogroup, R.id.picradiogroup1, picintent);
			break;
		}
		case R.id.pic_pra: {
			showDialog(MainActivity.this, false, R.layout.piclevel_list,
					R.id.picradiogroup, R.id.picradiogroup1, picintent);
			break;
		}
		case R.id.settings: {
			showSettingDialog(MainActivity.this);
			break;
		}

		}
	}

	public void showSettingDialog(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.setting_layout, null);
		final MyDialog builder = new MyDialog(context, 400, 420, view,
				R.style.settingDialog);
		builder.show();
		final Button soundsOnOff;
		final TextView game_rank;
		final TextView about_me;
		final TextView game_help;
		game_help = (TextView) view.findViewById(R.id.game_help);
		about_me = (TextView) view.findViewById(R.id.about_me);
		game_rank = (TextView) view.findViewById(R.id.game_rank);
		soundsOnOff = (Button) view.findViewById(R.id.soundsOnOff);
		if (mSharedPreferences.getBoolean("isSoundsOn", true)) {
			soundsOnOff.setText("关闭声音");
		} else {
			soundsOnOff.setText("开启声音");
		}
		soundsOnOff.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mSharedPreferences.getBoolean("isSoundsOn", true)) {
					mEditor.putBoolean("isSoundsOn", true);
					mEditor.commit();
					myHandler.sendEmptyMessage(BGM_START);
				} else {
					mEditor.putBoolean("isSoundsOn", false);
					mEditor.commit();
					myHandler.sendEmptyMessage(BGM_STOP);
				}
				builder.cancel();
			}
		});
		game_rank.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						RankActivity.class);
				startActivity(intent);
				Toast.makeText(MainActivity.this, "单击查看记录，长按可以重置",
						Toast.LENGTH_LONG).show();
				builder.cancel();
			}
		});
		about_me.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						AboutMeActivity.class);
				startActivity(intent);
			}
		});
		game_help.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showHelp();

			}
		});
	}

	private void showHelp() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View view = inflater.inflate(R.layout.game_help, null);
		MyDialog builder = new MyDialog(MainActivity.this, 0, 0, view,
				R.style.MyDialog);
		final TextView tv = (TextView) view
				.findViewById(R.id.game_success_info);
		final ImageView iv1 = (ImageView) view.findViewById(R.id.eight1_img);
		final ImageView iv = (ImageView) view.findViewById(R.id.eight_img);
		ImageView thumb_slide = (ImageView) view.findViewById(R.id.thumb_img);

		ObjectAnimator oa = ObjectAnimator.ofFloat(thumb_slide, "translationX",
				0f, -30f);
		oa.setDuration(2000);
		oa.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				iv.setVisibility(View.INVISIBLE);
				iv1.setVisibility(View.VISIBLE);
				tv.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});
		oa.start();
		builder.show();
	}

	public void showDialog(final Context context, final boolean isgameMode,
			int listId, int radiogroupId, int radiogroupId1, final Intent intent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(listId, null);
		// AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// builder.setView(view);
		// final Dialog dialog = builder.create();
		final MyDialog builder = new MyDialog(context, 0, 0, view,
				R.style.MyDialog);
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

				myApplication.numModeColumn = checkedId_position;
				myApplication.isGameMode = isgameMode;
				startActivity(intent);
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
				myApplication.numModeColumn = checkedId_position;
				myApplication.isGameMode = isgameMode;
				startActivity(intent);
				radioGroup1.setSelected(false);
				builder.cancel();
			}
		});
		builder.show();
	}

}
