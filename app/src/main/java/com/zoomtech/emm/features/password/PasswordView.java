package com.zoomtech.emm.features.password;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.zoomtech.emm.R;

/**
 * Created by Administrator on 2017/8/10.
 */

public class PasswordView extends EditText {
    private int textLength;

    private int borderColor;
    private float borderWidth;
    private float borderRadius;

    private int passwordLength;
    private int passwordColor;
    private float passwordWidth;
    private float passwordRadius;

    private Paint passwordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int defaultSplitLineWidth = 1;

    public PasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final Resources res = getResources();

        final int defaultBorderColor = res.getColor(R.color.grey);
        final float defaultBorderWidth = res.getDimension(R.dimen.password_dimen);
        final float defaultBorderRadius = res.getDimension(R.dimen.activity_vertical_margin);

        final int defaultPasswordLength = 6;
        final int defaultPasswordColor = res.getColor(R.color.black);
        final float defaultPasswordWidth = res.getDimension(R.dimen.password_text);
        final float defaultPasswordRadius = res.getDimension(R.dimen.password_text);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PasswordInputView, 0, 0);
        try {
            borderColor = typedArray.getColor(R.styleable.PasswordInputView_borderColor, defaultBorderColor);
            borderWidth = typedArray.getDimension(R.styleable.PasswordInputView_borderWidth, defaultBorderWidth);
            borderRadius = typedArray.getDimension(R.styleable.PasswordInputView_borderRadius, defaultBorderRadius);

            passwordLength = typedArray.getInt(R.styleable.PasswordInputView_passwordLength, defaultPasswordLength);
            passwordColor = typedArray.getColor(R.styleable.PasswordInputView_passwordColor, defaultPasswordColor);

            passwordWidth = typedArray.getDimension(R.styleable.PasswordInputView_passwordWidth, defaultPasswordWidth);
            passwordRadius = typedArray.getDimension(R.styleable.PasswordInputView_passwordRadius, defaultPasswordRadius);
        } finally {
            typedArray.recycle();
        }

        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);
        passwordPaint.setStrokeWidth(passwordWidth);
        passwordPaint.setStyle(Paint.Style.FILL);
        passwordPaint.setColor(passwordColor);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // 分割线
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(defaultSplitLineWidth);

        canvas.drawLine(0, 0, width, 0, borderPaint); //绘制上边界

        for (int i = 0; i <= passwordLength; i++) { //绘制中间线
            float x = width * i / passwordLength;
            canvas.drawLine(x, 0, x, height, borderPaint);
        }

        canvas.drawLine(0, height, width, height, borderPaint); //绘制下边界


        // 密码
        float cx, cy = height/ 2;
        float half = width / passwordLength / 2;
        for(int i = 0; i < textLength; i++) {
            cx = width * i / passwordLength + half; //获得text显示位置
            canvas.drawCircle(cx, cy, passwordWidth, passwordPaint);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.textLength = text.toString().length();
        invalidate();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        borderPaint.setColor(borderColor);
        invalidate();
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
        invalidate();
    }

    public float getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(float borderRadius) {
        this.borderRadius = borderRadius;
        invalidate();
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
        invalidate();
    }

    public int getPasswordColor() {
        return passwordColor;
    }

    public void setPasswordColor(int passwordColor) {
        this.passwordColor = passwordColor;
        passwordPaint.setColor(passwordColor);
        invalidate();
    }

    public float getPasswordWidth() {
        return passwordWidth;
    }

    public void setPasswordWidth(float passwordWidth) {
        this.passwordWidth = passwordWidth;
        passwordPaint.setStrokeWidth(passwordWidth);
        invalidate();
    }

    public float getPasswordRadius() {
        return passwordRadius;
    }

    public void setPasswordRadius(float passwordRadius) {
        this.passwordRadius = passwordRadius;
        invalidate();
    }
}
