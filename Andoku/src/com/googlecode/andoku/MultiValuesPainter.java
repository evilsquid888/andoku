/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
 *
 * This file is part of Andoku.
 *
 * Andoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Andoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andokusquid.R;

public class MultiValuesPainter {
	private Theme theme;

	private float textOffset;
	private float baselineDist;
	private float xOffset;

	private float cellWidth;
	private float cellHeight;

	public MultiValuesPainter() {
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public void setPuzzleSize(int puzzleSize) {
		// TODO
	}

	public void setCellSize(float cellSizeX, float cellSizeY) {
		this.cellWidth = cellSizeX;
		this.cellHeight = cellSizeY;

		float fontSize = cellHeight * 0.3f;
		setFontSize(fontSize);
	}

	public void paintValues(Canvas canvas, ValueSet values) {
		for (int value = values.nextValue(0); value != -1; value = values.nextValue(value + 1)) {
			int vrow = value / 3;
			int vcol = value % 3;
			String dv = String.valueOf(theme.getSymbol(value));
			float py = textOffset + vrow * baselineDist;
			float px = vcol == 0 ? xOffset : (vcol == 1 ? cellWidth / 2f : cellWidth - xOffset);
			Paint paint = theme.getDigitPaint();
			canvas.drawText(dv, px, py, paint);
		}
	}

	private void setFontSize(float fontSize) {
		Paint digitPaint = theme.getDigitPaint();
		digitPaint.setTextSize(fontSize);

		FontMetrics fontMetrics = digitPaint.getFontMetrics();
		float fontHeight = -fontMetrics.ascent - fontMetrics.descent;
		int rows = 3;
		float spacing = (cellHeight - rows * fontHeight) / (rows + 1);
		baselineDist = fontHeight + spacing;
		textOffset = cellHeight - spacing - (rows - 1) * baselineDist + 0.5f;
		xOffset = spacing + digitPaint.measureText("5") / 2;
	}
}
