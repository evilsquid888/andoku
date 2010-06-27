/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2010  Markus Wiederkehr
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

public class FingertipPopup {
	private final View parent;
	private final ContentView view;
	private final PopupWindow popup;
	private boolean visible;

	public FingertipPopup(View parent) {
		this.parent = parent;

		float displayDensity = parent.getResources().getDisplayMetrics().density;
		int size = Math.round(150 * displayDensity);

		view = new ContentView(parent.getContext(), size / 2);

		popup = new PopupWindow(view, size, size);
		popup.setTouchable(false);
		popup.setClippingEnabled(false);
	}

	public void show(PointF center, boolean editable) {
		if (view.editable != editable) {
			view.editable = editable;
			view.invalidate();
		}

		int x = (int) (center.x - view.getWidth() / 2f);
		int y = (int) (center.y - view.getHeight() / 2f);

		if (visible) {
			popup.update(x, y, -1, -1);
		}
		else {
			popup.showAtLocation(parent, Gravity.NO_GRAVITY, x, y);
			visible = true;
		}
	}

	public void hide() {
		if (visible) {
			popup.dismiss();
			visible = false;
		}
	}

	private static final class ContentView extends View {
		private final GradientDrawable redFingertip;
		private final GradientDrawable greenFingertip;

		public boolean editable;

		public ContentView(Context context, int radius) {
			super(context);

			redFingertip = createRadialGradient(0xff0000, radius);
			greenFingertip = createRadialGradient(0x00cc00, radius);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			GradientDrawable gradient = editable ? greenFingertip : redFingertip;

			gradient.setBounds(0, 0, getWidth(), getHeight());
			gradient.draw(canvas);
		}

		private GradientDrawable createRadialGradient(int color, int radius) {
			int[] colors = new int[] { 0xff000000 | color, 0x00ffffff & color };
			GradientDrawable gradient = new GradientDrawable(Orientation.TL_BR, colors);
			gradient.setGradientType(GradientDrawable.RADIAL_GRADIENT);
			gradient.setGradientRadius(radius);
			gradient.setGradientCenter(0.5f, 0.5f);
			return gradient;
		}
	}
}
