package com.yvan.gamelayout.view;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yvan.gamemethods.MergeSortForReserve;
import com.yvan.numandjiapuzzle.R;
import com.yvan.utils.CompressImg;
import com.yvan.utils.ImagePiece;
import com.yvan.utils.ImageSplitterUtil;

public class PicLayout extends RelativeLayout implements OnClickListener,
		android.view.View.OnTouchListener {

	public int picColumn = 3;
	private int mPadding;
	private int mMargin = 1;
	private ImageView[] mItems;
	public static int[] minPicMoves = { 0, 0, 0, 0, 0, 0, };
	public static int[] minPicTime = { 120, 240, 420, 650, 900, 1200 };
	private int mItemWidth;
	public Bitmap mBitmap;
	public static Bitmap showBitmap;
	private List<ImagePiece> mItemBitmaps;
	private boolean once;
	private int mWidth;
	private boolean isGameSucess;
	private boolean isGameOver;
	private int lastItem_index;
	private int lastItem_position = 0;
	public static int mMoves = 0;
	private Bitmap secondBitmap;
	private Bitmap lastItem_bitmap;
	private Resources res;
	float startX;
	float startY;
	private String bitmapPath = null;

	public interface PicListener {
		void nextLevel(int nextLevel);

		void timeChanged(int currentTime);

		void gameOver();

		void movesChanged(int currentMoves);

	}

	public PicListener mListener;

	// 设置接口回调
	public void setOnPicListener(PicListener mListener) {
		this.mListener = mListener;
	}

	private int level = 1;
	private static final int MOVES_CHANGED = 101;
	private static final int TIME_CHANGED = 102;
	private static final int NEXT_LEVEL = 103;
	private static final int SET_TIME = 104;
	private static final int COLUMN_CHANGED = 105;
	private static final int PRA_TIME_CHANGED = 106;
	private static final int USER_DEFINED = 107;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case TIME_CHANGED: {
				if (isGameSucess || isGameOver || isPause) {
					return;

				}

				if (mListener != null) {
					mListener.timeChanged(mTime);
					if (mTime == 0 && isTimeEnabled == true) {
						isGameOver = true;
						mListener.gameOver();
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

				if (mListener != null) {
					mListener.timeChanged(mTime);
				}
				mTime++;
				mHandler.sendEmptyMessageDelayed(PRA_TIME_CHANGED, 1000);
				break;
			case MOVES_CHANGED:
				mListener.movesChanged(mMoves);
				break;
			case COLUMN_CHANGED:
				break;
			case NEXT_LEVEL: {
				level = level + 1;
				if (mListener != null) {
					mListener.nextLevel(level);
				} else {
					nextLevel();
				}
				break;
			}
			case SET_TIME:
				break;
			case USER_DEFINED:
				reStart();
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

	public void setPicColumn(int picColumn) {
		this.picColumn = picColumn;
		mHandler.sendEmptyMessage(COLUMN_CHANGED);
	}

	public PicLayout(Context context) {
		this(context, null);
	}

	public PicLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PicLayout(Context context, AttributeSet attrs, int defStyle) {
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
			// 进行切图以及乱序
			initBitmap();
			// 设置ImageView(Item)的宽高等属性
			initItem();
			// 判断是否开启时间
			checkTimeEnabled();
			once = true;
		}
		setMeasuredDimension(mWidth, mWidth);

	}

	private void checkTimeEnabled() {

		if (isTimeEnabled) {
			countTimeBaseLevel();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		} else {
			mHandler.sendEmptyMessage(PRA_TIME_CHANGED);
		}

	}

	// 根据游戏picColumn设置游戏时间
	private void countTimeBaseLevel() {
		switch (picColumn) {
		case 3:
			mTime = 120;
			break;
		case 4:
			mTime = 240;
			break;
		case 5:
			mTime = 420;
			break;
		case 6:
			mTime = 650;
			break;
		case 7:
			mTime = 900;
			break;
		case 8:
			mTime = 1200;
		}

	}

	private boolean isUserDefined = false;
	private Bitmap lastItem_orbitmap;

	public void setUserDefined(boolean isUserDefined, String path) {
		this.isUserDefined = isUserDefined;
		this.bitmapPath = path;
		mHandler.sendEmptyMessage(USER_DEFINED);
	}

	@SuppressWarnings("null")
	private void initBitmap() {
		mMoves = 0;
		mHandler.sendEmptyMessage(MOVES_CHANGED);
		if (mBitmap == null && isUserDefined == false) {
			mBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.avril_lavigne);
		}
		if (isUserDefined) {
			mBitmap = getSDBitmap(bitmapPath);
		}

		int width = mBitmap.getWidth();
		int height = mBitmap.getHeight();
		int minSize = Math.min(width, height);
		showBitmap = Bitmap.createBitmap(mBitmap, 0, 0, minSize, minSize);
		mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, picColumn);

		lastItem_orbitmap = mItemBitmaps.get(picColumn * picColumn - 1)
				.getBitmap();
		lastItem_index = mItemBitmaps.get(picColumn * picColumn - 1).getIndex();
		res = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.blank);
		mItemBitmaps.get(picColumn * picColumn - 1).setBitmap(bmp);
		lastItem_bitmap = mItemBitmaps.get(picColumn * picColumn - 1)
				.getBitmap();
		;
		// 乱序三次
		outOfOrder(mItemBitmaps);
		outOfOrder(mItemBitmaps);
		outOfOrder(mItemBitmaps);

		int[] arr_index = new int[picColumn * picColumn];
		int odevity = (picColumn - 1) % 2;
		int checkS = checkSolvable(arr_index);
		while (checkS % 2 != odevity) {
			outOfOrder(mItemBitmaps);
			checkS = checkSolvable(arr_index);
		}
	}

	private int checkSolvable(int[] arr_index) {

		int s = 0;
		int[] array1 = new int[picColumn * picColumn];
		int dest = 0;
		for (int i = 0; i < picColumn * picColumn; i++) {
			arr_index[i] = mItemBitmaps.get(i).getIndex() + 1;
			if (lastItem_index == mItemBitmaps.get(i).getIndex()) {
				lastItem_position = i;
				arr_index[i] = 0;
			}
		}
		dest = Math.abs(lastItem_position / picColumn + 1 - picColumn)
				+ Math.abs(lastItem_position % picColumn + 1 - picColumn);
		MergeSortForReserve.mergeSort(arr_index, 0, picColumn * picColumn - 1,
				array1);
		int countReserve = MergeSortForReserve.getReserve(arr_index, 0,
				picColumn * picColumn - 1, array1);
		s = dest + countReserve;
		return s;
	}

	private Bitmap getSDBitmap(String path) {
		File file = new File(path);
		Bitmap sdBitmap = null;
		if (file.exists()) {
			// 压缩图片
			sdBitmap = CompressImg.getimage(path);
			return sdBitmap;
		} else {
			return sdBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.mona_lisa);
		}

	}

	private void initItem() {
		mItemWidth = (mWidth - mPadding * 2 - mMargin * (picColumn - 1))
				/ picColumn;
		mItems = new ImageView[picColumn * picColumn];
		for (int i = 0; i < mItems.length; i++) {
			ImageView item = new ImageView(getContext());
			item.setOnClickListener(this);
			item.setOnTouchListener(this);
			Bitmap bitmap_item = mItemBitmaps.get(i).getBitmap();
			item.setImageBitmap(bitmap_item);

			mItems[i] = item;
			item.setId(i + 1);
			// 在Item的Tag中存储了index

			item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					mItemWidth, mItemWidth);
			// 设置Item间横向间隔，通过RightMargin
			// 不是最后一列
			if ((i + 1) % picColumn != 0) {
				lp.rightMargin = mMargin;
			}
			// 不是第一列，设置rule
			if (i % picColumn != 0) {
				lp.addRule(RelativeLayout.RIGHT_OF, mItems[i - 1].getId());
			}
			// 如果不是第一行,设置TopMargin和rule
			if (i + 1 > picColumn) {
				lp.topMargin = mMargin;
				lp.addRule(RelativeLayout.BELOW, mItems[i - picColumn].getId());
			}
			addView(item, lp);

		}
	}

	private ImageView mFirst;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mFirst = (ImageView) v;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			startY = event.getY();
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (Math.abs(startX - event.getX()) > 30
					|| (Math.abs(startY - event.getY()) > 30)) {

				for (int i = 0; i < picColumn * picColumn; i++) {
					if (getImageIndexByTag((String) mItems[i].getTag()) == lastItem_index) {
						lastItem_position = i;
					}
				}
				int mFirstPosition = mFirst.getId() - 1;
				if (mFirstPosition == (lastItem_position + 1)
						|| mFirstPosition == (lastItem_position - 1)
						|| mFirstPosition == (lastItem_position + picColumn)
						|| mFirstPosition == (lastItem_position - picColumn)) {
					if (mFirstPosition % picColumn == 0
							&& mFirstPosition == (lastItem_position + 1)) {
						return false;
					} else if ((mFirstPosition + 1) % picColumn == 0
							&& mFirstPosition == (lastItem_position - 1)) {
						return false;
					} else {
						viewExchange();
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

		mFirst = (ImageView) v;

		for (int i = 0; i < picColumn * picColumn; i++) {
			if (getImageIndexByTag((String) mItems[i].getTag()) == lastItem_index) {
				lastItem_position = i;
			}
		}
		int mFirstPosition = mFirst.getId() - 1;
		if (mFirstPosition == (lastItem_position + 1)
				|| mFirstPosition == (lastItem_position - 1)
				|| mFirstPosition == (lastItem_position + picColumn)
				|| mFirstPosition == (lastItem_position - picColumn)) {
			if (mFirstPosition % picColumn == 0
					&& mFirstPosition == (lastItem_position + 1)) {
				return;
			} else if ((mFirstPosition + 1) % picColumn == 0
					&& mFirstPosition == (lastItem_position - 1)) {
				return;
			} else {
				viewExchange();
			}
		} else {
			return;
		}

	}

	// 交换图片
	public void viewExchange() {
		Bitmap firstBitmap = mItemBitmaps.get(
				getImageIdByTag((String) mFirst.getTag())).getBitmap();
		secondBitmap = lastItem_bitmap;
		String firstTag = (String) mFirst.getTag();
		String secondTag = (String) mItems[lastItem_position].getTag();
		mFirst.setImageBitmap(secondBitmap);
		mItems[lastItem_position].setImageBitmap(firstBitmap);

		PropertyValuesHolder p1 = PropertyValuesHolder.ofFloat("scaleX", 1f,
				0.9f, 1f);
		PropertyValuesHolder p2 = PropertyValuesHolder.ofFloat("scaleY", 1f,
				0.9f, 1f);
		PropertyValuesHolder p3 = PropertyValuesHolder.ofFloat("alpha", 1f,
				0.8f, 1f);
		ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(
				mItems[lastItem_position], p1, p2, p3);
		oa.setDuration(200);
		oa.setInterpolator(new LinearInterpolator());
		// oa.setAutoCancel(true);
		oa.start();
		mFirst.setTag(secondTag);
		mItems[lastItem_position].setTag(firstTag);
		mMoves++;
		mHandler.sendEmptyMessage(MOVES_CHANGED);
		checkSucess();
	}

	// 判断游戏是否成功
	public void checkSucess() {
		boolean isSucess = true;
		for (int i = 0; i < mItems.length; i++) {
			ImageView imageView = mItems[i];
			if (getImageIndexByTag((String) imageView.getTag()) != i) {
				isSucess = false;
			}
		}
		if (isSucess) {
			isGameSucess = true;
			if (isTimeEnabled) {
				if (minPicMoves[picColumn - 3] == 0
						|| minPicTime[picColumn - 3] == 0) {
					minPicMoves[picColumn - 3] = mMoves;
					minPicTime[picColumn - 3] = mTime;
				} else {
					if (mMoves < minPicMoves[picColumn - 3])
						minPicMoves[picColumn - 3] = mMoves;
					if (mTime > minPicTime[picColumn - 3])
						minPicTime[picColumn - 3] = mTime;
				}
			}
			mItems[picColumn * picColumn - 1].setImageBitmap(lastItem_orbitmap);
			mHandler.removeMessages(TIME_CHANGED);
			mHandler.removeMessages(PRA_TIME_CHANGED);
			mHandler.sendEmptyMessage(NEXT_LEVEL);
		}

	}

	public int getImageIdByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[0]);
	}

	public int getImageIndexByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[1]);
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
		initBitmap();
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
		if (picColumn < 8) {
			picColumn++;
		}
		isGameSucess = false;
		if (isTimeEnabled) {
			countTimeBaseLevel();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		} else {
			mTime = 0;
			mHandler.sendEmptyMessage(PRA_TIME_CHANGED);
		}
		initBitmap();
		initItem();

	}

	// 乱序方法
	public <T> void outOfOrder(List<T> list) {
		// 利用sort进行乱序
		Collections.sort(list, new Comparator<T>() {

			@Override
			public int compare(T a, T b) {

				return Math.random() > 0.5 ? 1 : -1;
			}
		});

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
