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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class FingertipView extends View {
	private PointF point;
	private boolean editable;

	private int radiusX = 64;
	private int radiusY = 64;

	private Drawable redFingertip;
	private Drawable greenFingertip;

	public FingertipView(Context context, AttributeSet attrs) {
		super(context, attrs);

		redFingertip = getResources().getDrawable(R.drawable.ftip_red);
		greenFingertip = getResources().getDrawable(R.drawable.ftip_green);
	}

	public void setRadius(int radiusX, int radiusY) {
		this.radiusX = radiusX;
		this.radiusY = radiusY;
	}

	public void highlight(PointF point, boolean editable) {
		if (eq(this.point, point) && this.editable == editable)
			return;

		invalidatePoint(this.point);
		invalidatePoint(point);

		this.point = point;
		this.editable = editable;
	}

	private void invalidatePoint(PointF point) {
		if (point == null)
			return;

		invalidate((int) Math.floor(point.x - radiusX), (int) Math.floor(point.y - radiusY),
				(int) Math.ceil(point.x + radiusX), (int) Math.ceil(point.y + radiusY));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (point == null)
			return;

		Drawable drawable = editable ? greenFingertip : redFingertip;

		final int cx = Math.round(point.x);
		final int cy = Math.round(point.y);
		drawable.setBounds(cx - radiusX, cy - radiusY, cx + radiusX - 1, cy + radiusY - 1);
		drawable.draw(canvas);
	}

	private boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
}
