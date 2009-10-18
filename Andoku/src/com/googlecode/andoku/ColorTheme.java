package com.googlecode.andoku;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

class ColorTheme implements Theme {
	private final float borderStrokeWidth;

	private final Drawable background;

	private final int titleTextColor;
	private final int timerTextColor;

	private final Paint gridPaint;
	private final Paint regionBorderPaint;
	private final Paint extraRegionPaint;
	private final Paint valuePaint;
	private final Paint digitPaint;
	private final Paint cluePaint;
	private final Paint previewCluePaint;
	private final Paint errorPaint;
	private final Paint markedCellPaint;
	private final Paint markedCluePaint;

	private final boolean drawAreaColors;
	private final int[] areaColors;

	private final Drawable congratsDrawable;
	private final Drawable pausedDrawable;

	private final Drawable puzzleBackground;

	public static final class Builder {
		private final Resources resources;

		public int backgroudColor = 0xffeeeeee;
		public int puzzleBackgroundColor = 0xffffffff;
		public int titleTextColor = 0xff222222;
		public int timerTextColor = 0xff222222;
		public int gridColor = 0x40000000;
		public int borderColor = 0xff000000;
		public int extraRegionColor = 0x40002dff;
		public int valueColor = 0xff006000;
		public int clueColor = 0xff000000;
		public int errorColor = 0xffff0000;
		public int markedCellColor = 0x7000ff00;
		public int markedClueColor = 0x70ff0000;

		public Builder(Resources resources) {
			this.resources = resources;
		}

		public Theme build() {
			return new ColorTheme(this);
		}
	}

	private ColorTheme(Builder builder) {
		Resources resources = builder.resources;

		float displayDensity = resources.getDisplayMetrics().density;
		float gridWidth = Math.max(1, displayDensity);

		borderStrokeWidth = Math.max(2, 3 * displayDensity);

		background = new ColorDrawable(builder.backgroudColor);

		titleTextColor = builder.titleTextColor;
		timerTextColor = builder.timerTextColor;

		gridPaint = new Paint();
		gridPaint.setStrokeWidth(gridWidth);
		gridPaint.setAntiAlias(false);
		gridPaint.setColor(builder.gridColor);
		gridPaint.setStrokeCap(Cap.BUTT);
		// gridPaint.setShadowLayer(1, 1, 1, 0xff000000);

		regionBorderPaint = new Paint();
		regionBorderPaint.setStrokeWidth(borderStrokeWidth);
		regionBorderPaint.setAntiAlias(false);
		regionBorderPaint.setColor(builder.borderColor);
		regionBorderPaint.setStrokeCap(Cap.ROUND);

		extraRegionPaint = new Paint();
		extraRegionPaint.setAntiAlias(false);
		extraRegionPaint.setColor(builder.extraRegionColor);

		Typeface typeface = Typeface.SANS_SERIF;
		valuePaint = new Paint();
		valuePaint.setAntiAlias(true);
		valuePaint.setColor(builder.valueColor);
		valuePaint.setTextAlign(Align.CENTER);
		valuePaint.setTypeface(typeface);

		Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
		digitPaint = new Paint();
		digitPaint.setAntiAlias(true);
		digitPaint.setColor(builder.valueColor);
		digitPaint.setTextAlign(Align.CENTER);
		digitPaint.setTypeface(boldTypeface);

		cluePaint = new Paint();
		cluePaint.setAntiAlias(true);
		cluePaint.setColor(builder.clueColor);
		cluePaint.setTextAlign(Align.CENTER);
		cluePaint.setTypeface(boldTypeface);

		previewCluePaint = new Paint(cluePaint);
		previewCluePaint.setAlpha(128);

		errorPaint = new Paint();
		errorPaint.setStrokeWidth(borderStrokeWidth);
		errorPaint.setAntiAlias(true);
		errorPaint.setColor(builder.errorColor);
		errorPaint.setStyle(Style.STROKE);
		errorPaint.setStrokeCap(Cap.BUTT);

		markedCellPaint = new Paint();
		markedCellPaint.setAntiAlias(false);
		markedCellPaint.setColor(builder.markedCellColor);

		markedCluePaint = new Paint();
		markedCluePaint.setAntiAlias(false);
		markedCluePaint.setColor(builder.markedClueColor);

		drawAreaColors = false;
		areaColors = new int[] { 0x0cff0000, 0x0c00ff00, 0x0c0000ff, 0x0cffff00, 0x0cff00ff,
				0x0c00ffff, 0x0c800000, 0x0c008000, 0x0c000080 };

		congratsDrawable = resources.getDrawable(R.drawable.congrats);
		congratsDrawable.setAlpha(144);

		pausedDrawable = resources.getDrawable(R.drawable.paused);
		pausedDrawable.setAlpha(144);

		GradientDrawable bg = new GradientDrawable();
		bg.setColor(builder.puzzleBackgroundColor);
		bg.setStroke(Math.round(borderStrokeWidth), builder.borderColor);
		bg.setCornerRadius(6 * displayDensity);
		puzzleBackground = bg;
	}

	public char getSymbol(int value) {
		return "123456789".charAt(value);
	}

	public Drawable getBackground() {
		return background;
	}

	public int getTitleTextColor() {
		return titleTextColor;
	}

	public int getTimerTextColor() {
		return timerTextColor;
	}

	public Paint getGridPaint() {
		return gridPaint;
	}

	public Paint getRegionBorderPaint() {
		return regionBorderPaint;
	}

	public Paint getExtraRegionPaint() {
		return extraRegionPaint;
	}

	public Paint getValuePaint() {
		return valuePaint;
	}

	public Paint getDigitPaint() {
		return digitPaint;
	}

	public Paint getCluePaint(boolean preview) {
		return preview ? previewCluePaint : cluePaint;
	}

	public Paint getErrorPaint() {
		return errorPaint;
	}

	public Paint getMarkedCellPaint() {
		return markedCellPaint;
	}

	public Paint getMarkedCluePaint() {
		return markedCluePaint;
	}

	public boolean isDrawAreaColors() {
		return drawAreaColors;
	}

	public int getAreaColor(int colorNumber) {
		return areaColors[colorNumber];
	}

	public Drawable getCongratsDrawable() {
		return congratsDrawable;
	}

	public Drawable getPausedDrawable() {
		return pausedDrawable;
	}

	public Drawable getPuzzleBackground() {
		return puzzleBackground;
	}

	public int getPuzzlePadding() {
		return Math.round(borderStrokeWidth);
	}

	public void onNewTextSize(float fontSize) {
		cluePaint.setTextSize(fontSize);
		previewCluePaint.setTextSize(fontSize);
		valuePaint.setTextSize(fontSize);
	}
}