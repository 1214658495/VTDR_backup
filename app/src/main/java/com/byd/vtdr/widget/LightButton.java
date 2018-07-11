package com.byd.vtdr.widget;

//import com.byd.appstore.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.AttributeSet;

import com.byd.vtdr.R;

//import android.widget.Button;

//import com.byd.lighttextview.Constants.ModeState;
//import com.byd.utils.Lg;
public class LightButton extends android.support.v7.widget.AppCompatButton {

	private int color;
	private float radius;
//	OnModeChangeListener listener;
	private Rect bounds;

	public LightButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
		loadAttrs(context, attrs);
	}

	public LightButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		loadAttrs(context, attrs);
	}

	public LightButton(Context context) {
		super(context);
		init();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		/** */
		if (isPressed()) {
             /** 第一个参数为模糊的半径，第二个参数为阴影离开文字的x横向距离，第三个参数为阴影离开文字的Y纵向距离，第四个参数为阴影颜色*/
			getPaint().setShadowLayer(radius * 0.2f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.4f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.6f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius * 0.8f, 0, 0, color);
			super.onDraw(canvas);

			getPaint().setShadowLayer(radius, 0, 0, color);
			super.onDraw(canvas);

			bounds.left = getCompoundPaddingLeft();
			bounds.right = getWidth() - getCompoundPaddingRight();
			bounds.top = getCompoundPaddingTop();
			bounds.bottom = getHeight() - getCompoundPaddingBottom();

			canvas.save(); //用来保存canvas的状态；
			/** Op.DIFFERENCE:A-B/INTERSECT:A和B交集/REVERSE_DIFFERENCE:B-A/XOR:异或/UNION并集*/
			canvas.clipRect(bounds, Op.DIFFERENCE);
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			canvas.restore();//和save()一起使用的时候，恢复到canvas.save()保存时的状态，把上一次的save读出来，方便你可以不用再次设置canvas的画笔颜色粗细
		}

		getPaint().setShadowLayer(0, 0, 0, color);
		super.onDraw(canvas);
	}

	private void init() {
		bounds = new Rect();
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		//用这行代码关闭关闭单个view的硬件加速
	}

	private void loadAttrs(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.LightTextView);
		
		 //kangkang 12-6 add for SportChange
		int color = typedArray.getColor(R.styleable.LightTextView_lightColor,
				 Constants.COLOR_DEFAULT_BLUE);
		float radius = typedArray.getFloat(R.styleable.LightTextView_radius,
				Constants.DEFAULT_RADIUS);
		setLight(color, radius);

		typedArray.recycle();
	}
	
	public void setLight(int color, float radius) {
//		Lg.i("kangkang 12-16 add color = " + color);
		this.color = color;
		this.radius = radius;
		invalidate();
	}
	
//	@Override
//	public void ModeinChanged(int modeEvent) {
//		// TODO Auto-generated method stub
////		int mColor = modeEvent == ModeState.EVENT_SPORT ? getResources().getColor(R.color.check_selector_sport) : getResources().getColor(R.color.normal_color);
//		int mColor = modeEvent == com.byd.vtdr2.widget.Constants.ModeState.EVENT_SPORT ? getResources().getColor(R.color.check_selector_sport) : getResources().getColor(R.color.check_selector_normal);
////		Lg.i("kangkang 12-16 add mColor = " + mColor);
//	}
}
