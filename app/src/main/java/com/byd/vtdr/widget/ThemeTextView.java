package com.byd.vtdr.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.AttributeSet;

import com.byd.vtdr.R;

public class ThemeTextView extends LightTextView implements ITheme {
	private Theme mTheme;
	
	private int color;
	private float radius;

	private Rect bounds;
	private Context myContext;

	
	public ThemeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		mTheme = new Theme();
		mTheme.set(context, attrs);
		ThemeManager.getInstance().add(this);
		myContext = context;
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		if(isPressed()){
			getPaint().setShadowLayer(radius * 0.2f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.4f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.6f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.8f, 0, 0, color);
			super.onDraw(canvas);

			bounds.left = getCompoundPaddingLeft();
			bounds.right = getWidth() - getCompoundPaddingRight();
			bounds.top = getCompoundPaddingTop();
			bounds.bottom = getHeight() - getCompoundPaddingBottom();

			canvas.save();
			/** Op.DIFFERENCE:A-B/INTERSECT:A和B交集/REVERSE_DIFFERENCE:B-A/XOR:异或/UNION并集*/
			canvas.clipRect(bounds, Op.DIFFERENCE);
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			canvas.restore();
			
			getPaint().setShadowLayer(radius, 0, 0, color);
			super.onDraw(canvas);
//			
//			getPaint().setMaskFilter(new BlurMaskFilter(5, Blur.SOLID));
//			super.onDraw(canvas);
			

//		}
		
		getPaint().setShadowLayer(radius, 0, 0, color);
		super.onDraw(canvas);
	}

	private void init() {
		bounds = new Rect();
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		setLight(Constants.COLOR_DEFAULT_BLUE, Constants.DEFAULT_RADIUS);
	}

	@Override
	public void setLight(int color, float radius) {
		this.color = color;
		this.radius = radius;
		invalidate();
	}

	@Override
	public void setTheme(Theme theme) {
		mTheme.set(theme);
		onThemeChanged(ThemeManager.getInstance().getTheme());
	}

	@SuppressLint("NewApi")
	@Override
	public void onThemeChanged(int theme) {
		ColorStateList colorStateList;
		if(theme == Theme.NORMAL){
			setLight(getResources().getColor(R.color.normal_color), 20);//设置发光的颜色以及半径
			colorStateList = getResources().getColorStateList(R.color.press_selector_text);//点击时候与未点击时候的颜色
		}else{
			setLight(getResources().getColor(R.color.sport_color), 20);
			colorStateList = getResources().getColorStateList(R.color.press_selector_text_sport);
		}		 	
		setTextColor(colorStateList);
	}
}

