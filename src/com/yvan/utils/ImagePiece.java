package com.yvan.utils;

import android.graphics.Bitmap;

public class ImagePiece {
	private int index;
	private Bitmap bitmap;

	public ImagePiece() {
		this.index = index;
		this.bitmap = bitmap;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	@Override
	public String toString() {
		return "ImagePiece [index=" + index + ", bitmap=" + bitmap + "]";
	}

}
