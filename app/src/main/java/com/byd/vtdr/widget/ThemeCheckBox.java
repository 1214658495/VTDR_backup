package com.byd.vtdr.widget;

//import com.byd.appstore.R;

import android.content.Context;
import android.util.AttributeSet;

import com.byd.vtdr.R;

public class ThemeCheckBox extends android.support.v7.widget.AppCompatCheckBox implements ITheme{
	private Theme mTheme;
	public ThemeCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTheme = new Theme();
		mTheme.set(context, attrs);
		ThemeManager.getInstance().add(this);
	}
	@Override
	public void setTheme(Theme theme) {
		// TODO Auto-generated method stub
		mTheme.set(theme);
		onThemeChanged(ThemeManager.getInstance().getTheme());
	}
	@Override
	public void onThemeChanged(int theme) {
		// TODO Auto-generated method stub
	/*	int res = mTheme.get(theme);
		Drawable selector = getResources().getDrawable(R.drawable.selector_file_sort);
		Drawable drawable = DrawableCompat.wrap(selector);
		if(res > 0) {
			setTextColor(getResources().getColor(res));
			DrawableCompat.setTint(drawable, getResources().getColor(res));
			setButtonDrawable(drawable);
		}*/
		switch (theme) {
		case Theme.SPORT:
//			setButtonDrawable(getResources().getDrawable(R.drawable.selector_file_sort));
			setTextColor(getResources().getColor(R.color.sport_color));
			break;
		default:
//			setButtonDrawable(getResources().getDrawable(R.drawable.pic_check_box_up));
			setTextColor(getResources().getColor(R.color.light));
			break;
		}
	}

}
