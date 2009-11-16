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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.CompoundButton;

public class KeypadButton extends CompoundButton {
	private static final int[] HIGHLIGHTED_STATE_SET = { R.attr.state_highlighted };

	private boolean highlighted;

	public KeypadButton(Context context) {
		this(context, null);
	}

	public KeypadButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.buttonStyleKeypad);
	}

	public KeypadButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeypadButton, defStyle, 0);
		highlighted = a.getBoolean(R.styleable.KeypadButton_highlighted, false);

		a.recycle();
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) {
			this.highlighted = highlighted;
			refreshDrawableState();
		}
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (highlighted) {
			mergeDrawableStates(drawableState, HIGHLIGHTED_STATE_SET);
		}
		return drawableState;
	}
}
