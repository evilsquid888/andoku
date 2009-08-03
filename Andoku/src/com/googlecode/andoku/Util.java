package com.googlecode.andoku;

import android.app.Activity;
import android.view.WindowManager;

public class Util {
	private Util() {
	}

	public static void setFullscreenWorkaround(Activity activity) {
		// Workaround for issue #1
		// FLAG_LAYOUT_NO_LIMITS: allow window to extend outside of the screen.
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	}
}
