package com.byd.vtdr.widget;

//import com.byd.appstore.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.util.AttributeSet;

import com.byd.vtdr.R;
/**
 * @author byd_tw
 */
public class LightRadioButton extends android.support.v7.widget.AppCompatRadioButton {

	private int color;
	private float radius;
	private RectF bounds;
//	Paint paint;
//	Canvas canvas1;
	/*
	 * 使用自定义属性
	 */
	public LightRadioButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
		loadAttrs(context, attrs);
	}
	public LightRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		loadAttrs(context, attrs);
	}

	public LightRadioButton(Context context) {
		super(context);
		init();
	}

	
	@Override
	protected void onDraw(Canvas canvas) {

		if (isChecked()) {

			getPaint().setShadowLayer(radius * 0.2f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.4f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.6f, 0, 0,color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.8f, 0, 0, color);
		    super.onDraw(canvas);

			getPaint().setShadowLayer(radius, 0, 0, color);
			super.onDraw(canvas);
			
//			getPaint().setMaskFilter(new BlurMaskFilter(20, Blur.SOLID));会一直发光

			bounds.left = getCompoundPaddingLeft();
			bounds.right = getWidth() - getCompoundPaddingRight();
			bounds.top = getCompoundPaddingTop();
			bounds.bottom = getHeight() - getCompoundPaddingBottom();
			
			canvas.save();			
			//canvas.drawOval(bounds, getPaint());
			canvas.clipRect(bounds, Op.DIFFERENCE);
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);//控制图标的背景颜色,也可以Mode.DST_IN,第一个参数外部的颜色
			canvas.restore();
			
			
		}		
        //没有阴影效果时，必须的
		getPaint().setShadowLayer(0, 0, 0, color);
		super.onDraw(canvas);
	}

	private void init() {
		bounds = new RectF();
		setLayerType(LAYER_TYPE_SOFTWARE, null);
	}

	private void loadAttrs(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.LightTextView);
		color = typedArray.getColor(R.styleable.LightTextView_lightColor,
				Constants.COLOR_DEFAULT_BLUE);
		radius = typedArray.getFloat(R.styleable.LightTextView_radius,
				Constants.DEFAULT_RADIUS);
		typedArray.recycle();
		
		setLight(color, radius);
		//setTextColor(getResources().getColor(R.color.check_selector_text));
	}

	public void setLight(int color, float radius) {
		this.color = color;
		this.radius = radius;
		invalidate();
	}

}
