package com.byd.vtdr.widget;

//import com.byd.appstore.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import com.byd.vtdr.R;


public class ThemeLightRadioButton extends LightRadioButton implements ITheme {
	private Theme mTheme;
	private Context myContext;

	public ThemeLightRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTheme = new Theme();
		mTheme.set(context, attrs);
		ThemeManager.getInstance().add(this);
		myContext = context;
	}
	@Override
	public void setTheme(Theme theme) {
		mTheme.set(theme);
		onThemeChanged(ThemeManager.getInstance().getTheme());
	}
	@Override
	public void onThemeChanged(int theme) {
//	方法一，此法需要在xml文件中调用其属性	
//		int res = mTheme.get(theme);
//		if(res > 0) {	
////			setLight(getResources().getColor(res+4), 15);
//			setLight(getResources().getColor(R.color.lightone), 25);
//		
//			ColorStateList colorStateList = getResources().getColorStateList(res);
//			setTextColor(colorStateList);
//		}
	
//方法二
		ColorStateList colorStateList;
		if(theme == Theme.NORMAL){
			setLight(getResources().getColor(R.color.lightone), 30);
			colorStateList = getResources().getColorStateList(R.color.check_selector_normal);
		}else{
			setLight(getResources().getColor(R.color.sport_color), 12);
			colorStateList = getResources().getColorStateList(R.color.check_selector_sport);
		}
		setTextColor(colorStateList);
	}
}
