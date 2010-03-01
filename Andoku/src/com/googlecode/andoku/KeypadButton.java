/*
 * Andoku - a sudoku puzzle game for Android.
 * Copyright (C) 2009, 2010  Markus Wiederkehr
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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;

public class KeypadButton extends Button {
	private static final int[] CHECKED_STATE_SET = { R.attr.state_checked };
	private static final int[] HIGHLIGHTED_STATE_SET = { R.attr.state_highlighted };

	private boolean checked;
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
		checked = a.getBoolean(R.styleable.KeypadButton_checked, false);
		highlighted = a.getBoolean(R.styleable.KeypadButton_highlighted, false);

		a.recycle();
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		if (this.checked != checked) {
			this.checked = checked;
			refreshDrawableState();
		}
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
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
		if (checked)
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		if (highlighted)
			mergeDrawableStates(drawableState, HIGHLIGHTED_STATE_SET);
		return drawableState;
	}
}
