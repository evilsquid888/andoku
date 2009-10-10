/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009  Markus Wiederkehr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.andoku;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;

import com.googlecode.andoku.model.ValueSet;
import com.googlecode.andoku.symbols.PuzzleSymbols;

public class MultiValuesPainter {
	private final Paint digitPaint;
	private PuzzleSymbols puzzleSymbols;

	private float textOffset;
	private float baselineDist;
	private float xOffset;

	private float cellWidth;
	private float cellHeight;

	public MultiValuesPainter(Typeface typeface) {
		digitPaint = new Paint();
		digitPaint.setAntiAlias(true);
		digitPaint.setARGB(255, 0, 96, 0);
		digitPaint.setTextAlign(Align.CENTER);
		digitPaint.setTypeface(typeface);
	}

	public void setPuzzle(int puzzleSize, PuzzleSymbols puzzleSymbols) {
		setPuzzleSize(puzzleSize);
		this.puzzleSymbols = puzzleSymbols;
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
			String dv = String.valueOf(puzzleSymbols.getSymbol(value));
			float py = textOffset + vrow * baselineDist;
			float px = vcol == 0 ? xOffset : (vcol == 1 ? cellWidth / 2f : cellWidth - xOffset);
			Paint paint = digitPaint;
			canvas.drawText(dv, px, py, paint);
		}
	}

	private void setPuzzleSize(int puzzleSize) {
		// TODO
	}

	private void setFontSize(float fontSize) {
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
