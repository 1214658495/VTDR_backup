package com.byd.vtdr.widget;

//import com.byd.appstore.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.AttributeSet;

import com.byd.vtdr.R;
/**
 * @author byd_tw
 */
public class LightCheckBox extends android.support.v7.widget.AppCompatCheckBox {

	private int color;
	private float radius;

	private Rect bounds;

	public LightCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
		loadAttrs(context, attrs);
	}

	public LightCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		loadAttrs(context, attrs);
	}

	public LightCheckBox(Context context) {
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

			canvas.save();
			canvas.clipRect(bounds, Op.DIFFERENCE);
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			canvas.restore();
		}

		getPaint().setShadowLayer(0, 0, 0, color);
		super.onDraw(canvas);
	}

	private void init() {
		bounds = new Rect();
		setLayerType(LAYER_TYPE_SOFTWARE, null);
	}

	private void loadAttrs(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.LightTextView);

		int color = typedArray.getColor(R.styleable.LightTextView_lightColor,
				com.byd.lighttextview.Constants.COLOR_DEFAULT_BLUE);
		float radius = typedArray.getFloat(R.styleable.LightTextView_radius,
				Constants.DEFAULT_RADIUS);
		setLight(color, radius);

		typedArray.recycle();
	}

	public void setLight(int color, float radius) {
		this.color = color;
		this.radius = radius;
		invalidate();
	}
}
