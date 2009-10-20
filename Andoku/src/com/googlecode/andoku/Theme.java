package com.googlecode.andoku;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;

interface Theme {
	char getSymbol(int value);

	Drawable getBackground();

	int getTitleTextColor();
	int getDifficultyTextColor();
	int getSourceTextColor();
	int getTimerTextColor();

	Paint getGridPaint();
	Paint getRegionBorderPaint();
	Paint getExtraRegionPaint();
	Paint getValuePaint();
	Paint getDigitPaint();
	Paint getCluePaint(boolean preview);
	Paint getErrorPaint();
	Paint getMarkedCellPaint();
	Paint getMarkedCluePaint();

	boolean isDrawAreaColors();
	int getAreaColor(int colorNumber);

	Drawable getCongratsDrawable();
	Drawable getPausedDrawable();

	Drawable getPuzzleBackground();
	int getPuzzlePadding();

	/**
	 * Notify theme of text size change.
	 */
	void onNewTextSize(float fontSize);
}
