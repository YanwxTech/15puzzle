package com.yvan.gamelayout.view;

import java.util.Random;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yvan.gamemethods.MergeSortForReserve;

public class NumLayout extends RelativeLayout implements OnClickListener,
		android.view.View.OnTouchListener {

	public int numColumn = 3;
	public static int numMoves = 0;
	public static int[] minNumMoves = { 0, 0, 0, 0, 0, 0, 0, 0 };
	public static int[] minNumTime = { 60, 90, 150, 300, 500, 720, 1000, 1200 };
	private int mPadding;
	private int mMargin = 3;
	private TextView[] mItems;
	private int mItemWidth;
	private boolean once;
	private int mWidth;
	private boolean isGameSucess;
	private boolean isGameOver;
	private int lastItem_position = 0;
	private int[] numArray;
	float startX;
	float startY;

	public interface NumListener {
		void nextLevel(int nextLevel);

		void timeChanged(int currentTime);

		void gameOver();

		void movesChanged(int currentMoves);

	}

	public NumListener numListener;

	// 设置接口回调
	public void setOnNumListener(NumListener mListener) {
		this.numListener = mListener;
	}

	private int level = 1;
	private static final int MOVES_CHANGED = 101;
	private static final int TIME_CHANGED = 102;
	private static final int NEXT_LEVEL = 103;
	private static final int SET_TIME = 104;
	private static final int COLUMN_CHANGED = 105;
	private static final int PRA_TIME_CHANGED = 106;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case TIME_CHANGED: {
				if (isGameSucess || isGameOver || isPause) {
					return;

				}

				if (numListener != null) {
					numListener.timeChanged(mTime);
					if (mTime == 0 && isTimeEnabled == true) {
						isGameOver = true;
						numListener.gameOver();
						return;
					}
				}
				if (isTimeEnabled == true) {
					mTime--;

					mHandler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);
				}
				break;
			}
			case PRA_TIME_CHANGED:
				if (isGameSucess || isPause) {
					return;
				}

				if (numListener != null) {
					numListener.timeChanged(mTime);
				}
				mTime++;
				mHandler.sendEmptyMessageDelayed(PRA_TIME_CHANGED, 1000);
				break;
			case MOVES_CHANGED:
				numListener.movesChanged(numMoves);
				break;
			case COLUMN_CHANGED:
				break;
			case NEXT_LEVEL: {
				level = level + 1;
				if (numListener != null) {
					numListener.nextLevel(level);
				} else {
					nextLevel();
				}
				break;
			}
			case SET_TIME:
				break;

			}

		};
	};

	public boolean isTimeEnabled = false;
	public int mTime = 0;

	// 设置是否开始时间计算
	public void setTimeEnabled(boolean isTimeEnabled) {
		this.isTimeEnabled = isTimeEnabled;
		mHandler.sendEmptyMessage(SET_TIME);
	}

	public void setNumColumn(int numColumn) {
		this.numColumn = numColumn;
		mHandler.sendEmptyMessage(COLUMN_CHANGED);
	}

	public NumLayout(Context context) {
		this(context, null);
	}

	public NumLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				1, getResources().getDisplayMetrics());
		mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(),
				getPaddingBottom());

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
		if (!once) {
			initNum();
			// 设置TextView(Item)的宽高等属性
			initItem();
			// 判断是否开启时间
			checkTimeEnabled();
			once = true;
		}
		setMeasuredDimension(mWidth, mWidth);

	}

	public void checkTimeEnabled() {

		if (isTimeEnabled) {
			countTimeBaseLevel();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		} else {
			mHandler.sendEmptyMessage(PRA_TIME_CHANGED);
		}

	}

	// 根据游戏numColumn设置游戏时间
	private void countTimeBaseLevel() {
		switch (numColumn) {
		case 3:
			mTime = 60;
			break;
		case 4:
			mTime = 90;
			break;
		case 5:
			mTime = 150;
			break;
		case 6:
			mTime = 300;
			break;
		case 7:
			mTime = 500;
			break;
		case 8:
			mTime = 720;
			break;
		case 9:
			mTime = 1000;
			break;
		case 10:
			mTime = 1200;
			break;
		}

	}

	private void initNum() {
		numMoves = 0;
		mHandler.sendEmptyMessage(MOVES_CHANGED);

		numArray = new int[numColumn * numColumn];
		for (int i = 0; i < numColumn * numColumn - 1; i++) {
			numArray[i] = i + 1;
		}
		numArray[numColumn * numColumn - 1] = 0;

		int odevity = (numColumn - 1) % 2;
		setNoneOrder(numArray);
		setNoneOrder(numArray);
		setNoneOrder(numArray);

		int checkS = checkSolvable(numArray);
		while (checkS % 2 != odevity) {
			setNoneOrder(numArray);
			checkS = checkSolvable(numArray);
		}

	}

	private int checkSolvable(int[] array) {
		int s = 0;
		int dest = 0;
		int array1[] = new int[numColumn * numColumn];
		for (int i = 0; i < numColumn * numColumn; i++) {
			if (numArray[i] == 0) {
				lastItem_position = i;
			}
		}
		dest = Math.abs(lastItem_position / numColumn + 1 - numColumn)
				+ Math.abs(lastItem_position % numColumn + 1 - numColumn);

		int countReserve = MergeSortForReserve.getReserve(numArray, 0,
				numColumn * numColumn - 1, array1);
		s = dest + countReserve;
		return s;
	}

	private void initItem() {
		mItemWidth = (mWidth - mPadding * 2 - mMargin * (numColumn - 1))
				/ numColumn;
		mItems = new TextView[numColumn * numColumn];
		for (int i = 0; i < mItems.length; i++) {
			TextView item = new TextView(getContext());
			item.setOnClickListener(this);
			item.setOnTouchListener(this);
			item.setText("" + numArray[i]);
			int num = numArray[i];
			// 根据当前数值设置背景颜色
			if (num == 0) {
				item.setVisibility(View.INVISIBLE);
			} else if (num > 0 && num <= numColumn) {
				item.setBackgroundColor(Color.RED);
			} else if (num > numColumn && num <= numColumn * 2) {
				item.setBackgroundColor(Color.BLUE);
			} else if (num > 2 * numColumn && num <= numColumn * 3) {
				item.setBackgroundColor(Color.GREEN);
			} else if (num > 3 * numColumn && num <= numColumn * 4) {
				item.setBackgroundColor(Color.parseColor("#da765b"));
			} else if (num > 4 * numColumn && num <= numColumn * 5) {
				item.setBackgroundColor(Color.parseColor("#009ad6"));
			} else if (num > 5 * numColumn && num <= numColumn * 6) {
				item.setBackgroundColor(Color.parseColor("#f47920"));
			} else if (num > 6 * numColumn && num <= numColumn * 7) {
				item.setBackgroundColor(Color.parseColor("#8552a1"));
			} else if (num > 7 * numColumn && num <= numColumn * 8) {
				item.setBackgroundColor(Color.parseColor("#cbc547"));
			} else if (num > 8 * numColumn && num <= numColumn * 9) {
				item.setBackgroundColor(Color.parseColor("#005344"));
			} else {
				item.setBackgroundColor(Color.GRAY);
			}
			TextPaint tp = item.getPaint();
			tp.setFakeBoldText(true);
			item.setTextColor(Color.WHITE);
			item.setGravity(Gravity.CENTER);
			if (numColumn < 7) {
				item.setTextSize(100 - 10 * numColumn);
			} else {
				item.setTextSize(30);
			}

			mItems[i] = item;
			item.setId(i + 1);

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					mItemWidth, mItemWidth);
			// 设置Item间横向间隔，通过RightMargin
			// 不是最后一列
			if ((i + 1) % numColumn != 0) {
				lp.rightMargin = mMargin;
			}
			// 不是第一列，设置rule
			if (i % numColumn != 0) {
				lp.addRule(RelativeLayout.RIGHT_OF, mItems[i - 1].getId());
			}
			// 如果不是第一行,设置TopMargin和rule
			if (i + 1 > numColumn) {
				lp.topMargin = mMargin;
				lp.addRule(RelativeLayout.BELOW, mItems[i - numColumn].getId());
			}
			addView(item, lp);

		}
	}

	private TextView mFirst;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mFirst = (TextView) v;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			startY = event.getY();
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (Math.abs(startX - event.getX()) > 30
					|| (Math.abs(startY - event.getY()) > 30)) {

				for (int i = 0; i < numColumn * numColumn; i++) {
					if (Integer.parseInt(mItems[i].getText().toString()) == 0) {
						lastItem_position = i;
					}
				}
				int mFirstPosition = mFirst.getId() - 1;
				if (mFirstPosition == (lastItem_position + 1)
						|| mFirstPosition == (lastItem_position - 1)
						|| mFirstPosition == (lastItem_position + numColumn)
						|| mFirstPosition == (lastItem_position - numColumn)) {
					if (mFirstPosition % numColumn == 0
							&& mFirstPosition == (lastItem_position + 1)) {
						return false;
					} else if ((mFirstPosition + 1) % numColumn == 0
							&& mFirstPosition == (lastItem_position - 1)) {
						return false;
					} else {
						viewExchange(mFirstPosition);
					}
				} else {
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {

		mFirst = (TextView) v;

		for (int i = 0; i < numColumn * numColumn; i++) {
			if (Integer.parseInt(mItems[i].getText().toString()) == 0) {
				lastItem_position = i;
			}
		}
		int mFirstPosition = mFirst.getId() - 1;
		if (mFirstPosition == (lastItem_position + 1)
				|| mFirstPosition == (lastItem_position - 1)
				|| mFirstPosition == (lastItem_position + numColumn)
				|| mFirstPosition == (lastItem_position - numColumn)) {
			if (mFirstPosition % numColumn == 0
					&& mFirstPosition == (lastItem_position + 1)) {
				return;
			} else if ((mFirstPosition + 1) % numColumn == 0
					&& mFirstPosition == (lastItem_position - 1)) {
				return;
			} else {
				viewExchange(mFirstPosition);
			}
		} else {
			return;
		}

	}

	private void viewExchange(int mFirstPosition) {
		int firstNum = Integer.parseInt(mFirst.getText().toString());
		int secondNum = 0;
		mItems[mFirstPosition].setText(secondNum + "");
		mItems[mFirstPosition].setVisibility(View.INVISIBLE);
		mItems[lastItem_position].setText(firstNum + "");
		ColorDrawable colorDrawable = (ColorDrawable) mItems[mFirstPosition]
				.getBackground();
		int currentColor = colorDrawable.getColor();
		mItems[lastItem_position].setBackgroundColor(currentColor);
		mItems[lastItem_position].setVisibility(View.VISIBLE);
		PropertyValuesHolder p1 = PropertyValuesHolder.ofFloat("scaleX", 1f,
				0.9f, 1f);
		PropertyValuesHolder p2 = PropertyValuesHolder.ofFloat("scaleY", 1f,
				0.9f, 1f);
		PropertyValuesHolder p3 = PropertyValuesHolder.ofFloat("alpha", 1f,
				0.7f, 1f);
		ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(
				mItems[lastItem_position], p1, p2, p3);
		oa.setDuration(200);
		oa.setInterpolator(new LinearInterpolator());
		// oa.setAutoCancel(true);
		oa.start();
		numMoves++;
		mHandler.sendEmptyMessage(MOVES_CHANGED);
		checkSucess();
	}

	// 判断游戏是否成功
	public void checkSucess() {
		boolean isSucess = true;
		for (int i = 0; i < mItems.length - 1; i++) {
			TextView textView = mItems[i];
			int num = Integer.parseInt(mItems[i].getText().toString());
			if ((num - 1) != i) {
				isSucess = false;
			}
		}
		if (isSucess) {
			isGameSucess = true;
			if (isTimeEnabled) {
				if (minNumMoves[numColumn - 3] == 0
						|| minNumTime[numColumn - 3] == 0) {
					minNumMoves[numColumn - 3] = numMoves;
					minNumTime[numColumn - 3] = mTime;
				} else {
					if (numMoves < minNumMoves[numColumn - 3])
						minNumMoves[numColumn - 3] = numMoves;
					if (mTime > minNumTime[numColumn - 3])
						minNumTime[numColumn - 3] = mTime;
				}
			}
			mHandler.removeMessages(TIME_CHANGED);
			mHandler.removeMessages(PRA_TIME_CHANGED);
			mHandler.sendEmptyMessage(NEXT_LEVEL);
		}

	}

	// 重新开始当前局游戏
	public void reStart() {
		this.removeAllViews();
		isGameSucess = false;
		if (isTimeEnabled) {
			countTimeBaseLevel();
		} else {
			mTime = 0;
		}
		initNum();
		initItem();
	}

	private boolean isPause;

	// 暂停游戏
	public void pause() {
		isPause = true;
		if (isTimeEnabled) {
			mHandler.removeMessages(TIME_CHANGED);
		} else {
			mHandler.removeMessages(PRA_TIME_CHANGED);
		}

	}

	// 暂停恢复
	public void resume() {
		if (isPause) {
			isPause = false;
			if (isTimeEnabled) {
				mHandler.sendEmptyMessage(TIME_CHANGED);
			} else {
				mHandler.sendEmptyMessage(PRA_TIME_CHANGED);
			}
		}
	}

	// 下一关
	public void nextLevel() {
		this.removeAllViews();
		if (numColumn < 10) {
			numColumn++;
		}

		isGameSucess = false;
		if (isTimeEnabled) {
			countTimeBaseLevel();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		} else {
			mTime = 0;
			mHandler.sendEmptyMessage(PRA_TIME_CHANGED);
		}
		initNum();
		initItem();

	}

	// 乱序方法
	public static void setNoneOrder(int[] array) {
		Random random = new Random();
		for (int i = 0; i < array.length; i++) {
			int p = random.nextInt(array.length);
			int tmp = array[i];
			array[i] = array[p];
			array[p] = tmp;
		}
		random = null;
	}

	// 不定参数求最小值
	private int min(int... params) {
		// TODO Auto-generated method stub
		int min = params[0];
		for (int param : params) {
			if (param < min) {
				min = param;
			}
		}
		return min;

	}

}
