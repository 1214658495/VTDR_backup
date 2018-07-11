package com.byd.vtdr.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author byd_tw
 */
public class ThemeManager {
	private static ThemeManager sInstance;
	private ArrayList<WeakReference<ITheme>> mThemes;
	private int mTheme = Theme.NORMAL;

	public static ThemeManager getInstance() {
		if (sInstance == null) {
			sInstance = new ThemeManager();
		}
		return sInstance;
	}

	private ThemeManager() {
		mThemes = new ArrayList<WeakReference<ITheme>>();
	}

	public void add(ITheme theme) {
		mThemes.add(new WeakReference<ITheme>(theme));
		theme.onThemeChanged(mTheme);
	}

	public void updateTheme(int theme) {
		if(mTheme == theme) {
			return;
		}
		mTheme = theme;
		synchronized (mThemes) {
			int size = mThemes.size();
			WeakReference<ITheme> weak;
			ITheme iTheme;
			for (int i = size - 1; i >= 0; i--) {
				weak = mThemes.get(i);
				if (weak != null && (iTheme = weak.get()) != null) {
					iTheme.onThemeChanged(theme);
				} else {
					mThemes.remove(i);
				}
			}
		}
	}
	
	public int getTheme() {
		return mTheme;
	}
}
