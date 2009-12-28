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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * With Andoku 1.1.0 class Main has been renamed in MainActivity. Existing desktop icons no longer
 * work after the upgrade because they still point to class Main.
 * <p>
 * This is an attempt to make these icons work again.
 */
public class Main extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(getIntent());
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
