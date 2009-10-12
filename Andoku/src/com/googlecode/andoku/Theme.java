package com.googlecode.andoku;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

class Theme {
	private final float borderStrokeWidth;

	private final Paint gridPaint;
	private final Paint regionBorderPaint;
	private final Paint extraRegionPaint;
	private final Paint valuePaint;
	private final Paint digitPaint;
	private final Paint cluePaint;
	private final Paint previewCluePaint;
	private final Paint errorPaint;
	private final Paint markedCellPaint;
	private final Paint markedCellCluePaint;

	private final boolean drawAreaColors;
	private final int[] areaColors;

	private final Drawable congratsDrawable;
	private final Drawable pausedDrawable;

	private final Drawable background;

	public Theme(Resources resources) {
		float displayDensity = resources.getDisplayMetrics().density;
		float gridWidth = Math.max(1, displayDensity);

		borderStrokeWidth = Math.max(2, 3 * displayDensity);

		gridPaint = new Paint();
		gridPaint.setStrokeWidth(gridWidth);
		gridPaint.setAntiAlias(false);
		gridPaint.setColor(0x40000000);
		gridPaint.setStrokeCap(Cap.BUTT);
		// gridPaint.setShadowLayer(1, 1, 1, 0xff000000);

		regionBorderPaint = new Paint();
		regionBorderPaint.setStrokeWidth(borderStrokeWidth);
		regionBorderPaint.setAntiAlias(false);
		regionBorderPaint.setColor(0xff000000);
		regionBorderPaint.setStrokeCap(Cap.ROUND);

		extraRegionPaint = new Paint();
		extraRegionPaint.setAntiAlias(false);
		extraRegionPaint.setColor(0x40002dff);

		valuePaint = new Paint();
		valuePaint.setAntiAlias(true);
		valuePaint.setColor(0xff006000);
		valuePaint.setTextAlign(Align.CENTER);

		Typeface typeface = Typeface.create(valuePaint.getTypeface(), Typeface.BOLD);
		digitPaint = new Paint();
		digitPaint.setAntiAlias(true);
		digitPaint.setColor(0xff006000);
		digitPaint.setTextAlign(Align.CENTER);
		digitPaint.setTypeface(typeface);

		cluePaint = new Paint();
		cluePaint.setAntiAlias(true);
		cluePaint.setColor(0xff000000);
		cluePaint.setTextAlign(Align.CENTER);
		cluePaint.setTypeface(Typeface.create(valuePaint.getTypeface(), Typeface.BOLD));

		previewCluePaint = new Paint(cluePaint);
		previewCluePaint.setAlpha(128);

		errorPaint = new Paint();
		errorPaint.setStrokeWidth(borderStrokeWidth);
		errorPaint.setAntiAlias(true);
		errorPaint.setColor(0xffff0000);
		errorPaint.setStyle(Style.STROKE);
		errorPaint.setStrokeCap(Cap.BUTT);

		markedCellPaint = new Paint();
		markedCellPaint.setAntiAlias(false);
		markedCellPaint.setColor(0x7000ff00);

		markedCellCluePaint = new Paint();
		markedCellCluePaint.setAntiAlias(false);
		markedCellCluePaint.setColor(0x70ff0000);

		drawAreaColors = false;
		areaColors = new int[] { 0x0cff0000, 0x0c00ff00, 0x0c0000ff, 0x0cffff00, 0x0cff00ff,
				0x0c00ffff, 0x0c800000, 0x0c008000, 0x0c000080 };

		congratsDrawable = resources.getDrawable(R.drawable.congrats);
		congratsDrawable.setAlpha(144);

		pausedDrawable = resources.getDrawable(R.drawable.paused);
		pausedDrawable.setAlpha(144);

		GradientDrawable bg = new GradientDrawable();
		bg.setColor(0xffffffff);
		bg.setStroke(Math.round(borderStrokeWidth), 0xff000000);
		bg.setCornerRadius(6);
		background = bg;
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

	public Paint getMarkedCellCluePaint() {
		return markedCellCluePaint;
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

	public Drawable getBackground() {
		return background;
	}

	public int getPadding() {
		return Math.round(borderStrokeWidth);
	}

	public void setTextSize(float fontSize) {
		cluePaint.setTextSize(fontSize);
		previewCluePaint.setTextSize(fontSize);
		valuePaint.setTextSize(fontSize);
	}
}
