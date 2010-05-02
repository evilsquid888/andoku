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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class Application extends android.app.Application {
	private static final String TAG = Application.class.getName();

	@Override
	public void onCreate() {
		super.onCreate();

		provideDefaultValueForFullscreenMode();

		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
	}

	private void provideDefaultValueForFullscreenMode() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.contains(Settings.KEY_FULLSCREEN_MODE)) {
			Log.d(TAG, "Fullscreen mode has already been set");
		}
		else {
			Log.d(TAG, "Fullscreen mode undecided");
			Editor editor = preferences.edit();
			editor.putBoolean(Settings.KEY_FULLSCREEN_MODE, isDefaultFullscreenMode());
			editor.commit();
		}
	}

	private boolean isDefaultFullscreenMode() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		float aspect = (float) metrics.widthPixels / metrics.heightPixels;
		boolean fullscreen = aspect >= 320f / 480;

		Log.d(TAG, "Width: " + metrics.widthPixels + ", height: " + metrics.heightPixels
				+ ", aspect: " + aspect + ", fullscreen: " + fullscreen);

		return fullscreen;
	}
}
