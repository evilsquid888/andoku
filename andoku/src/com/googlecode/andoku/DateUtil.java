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

import android.content.res.Resources;

public class DateUtil {
	private DateUtil() {
	}

	public static String formatTime(long time) {
		int seconds = (int) (time / 1000);
		int hh = seconds / 3600;
		seconds -= hh * 3600;
		int mm = seconds / 60;
		seconds -= mm * 60;
		int ss = seconds;

		return hh == 0 ? String.format("%02d:%02d", mm, ss) : String.format("%d:%02d:%02d", hh, mm,
				ss);
	}

	public static final String formatTimeSpan(Resources resources, long now, long then) {
		int minutes = (int) ((now - then) / 60000);
		if (minutes == 0)
			return resources.getString(R.string.age_0_minutes);
		if (minutes == 1)
			return resources.getString(R.string.age_1_minute);
		if (minutes < 60)
			return resources.getString(R.string.age_n_minutes, minutes);
		int hours = minutes / 60;
		if (hours == 1)
			return resources.getString(R.string.age_1_hour);
		if (hours < 24)
			return resources.getString(R.string.age_n_hours, hours);
		int days = hours / 24;
		if (days == 1)
			return resources.getString(R.string.age_1_day);
		if (days == 2)
			return resources.getString(R.string.age_2_days);
		if (days < 7)
			return resources.getString(R.string.age_n_days, days);
		int weeks = days / 7;
		if (weeks == 1)
			return resources.getString(R.string.age_1_week);
		return resources.getString(R.string.age_n_weeks, weeks);
	}
}
