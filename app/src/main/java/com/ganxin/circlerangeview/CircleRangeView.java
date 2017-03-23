package com.ganxin.circlerangeview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Description : 自定义圆形仪表盘View，适合根据数值显示不同等级范围的场景  <br/>
 * author : WangGanxin <br/>
 * date : 2017/3/23 <br/>
 * email : mail@wangganxin.me <br/>
 */
public class CircleRangeView extends View {

    private int mRadius; // 画布边缘半径（去除padding后的半径）
    private int mStartAngle = 150; // 起始角度
    private int mSweepAngle = 240; // 绘制角度
    private int mSparkleWidth; // 指示标宽度

    private float mLength1; // 刻度顶部相对边缘的长度
    private int mCalibrationWidth; // 刻度圆弧宽度
    private float mLength2; // 刻度读数顶部相对边缘的长度

    private int mPadding;
    private float mCenterX, mCenterY; // 圆心坐标

    private Paint mPaint;
    private RectF mRectFProgressArc;
    private RectF mRectFCalibrationFArc;
    private RectF mRectFTextArc;
    private Rect mRectText;

    private int mBackgroundColor;

    private CharSequence[] rangeColorArray;
    private CharSequence[] rangeValueArray;
    private CharSequence[] rangeTextArray;

    private int borderColor = ContextCompat.getColor(getContext(), R.color.wdiget_circlerange_border_color);
    private int cursorColor = ContextCompat.getColor(getContext(), R.color.wdiget_circlerange_cursor_color);
    private int extraTextColor = ContextCompat.getColor(getContext(), R.color.widget_circlerange_extra_color);

    private int rangeTextSize = sp2px(34); //中间文本大小
    private int extraTextSize = sp2px(14); //附加信息文本大小
    private int borderSize = dp2px(5); //进度圆弧宽度

    private int mSection = 0; // 等分份数
    private String currentValue;
    private List<String> extraList = new ArrayList<>();

    private boolean isAnimFinish = true;
    private float mAngleWhenAnim;

    public CircleRangeView(Context context) {
        this(context, null);
    }

    public CircleRangeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleRangeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleRangeView);

        rangeColorArray = typedArray.getTextArray(R.styleable.CircleRangeView_rangeColorArray);
        rangeValueArray = typedArray.getTextArray(R.styleable.CircleRangeView_rangeValueArray);
        rangeTextArray = typedArray.getTextArray(R.styleable.CircleRangeView_rangeTextArray);

        borderColor = typedArray.getColor(R.styleable.CircleRangeView_borderColor, borderColor);
        cursorColor = typedArray.getColor(R.styleable.CircleRangeView_cursorColor, cursorColor);
        extraTextColor = typedArray.getColor(R.styleable.CircleRangeView_extraTextColor, extraTextColor);

        rangeTextSize = typedArray.getDimensionPixelSize(R.styleable.CircleRangeView_rangeTextSize, rangeTextSize);
        extraTextSize = typedArray.getDimensionPixelSize(R.styleable.CircleRangeView_extraTextSize, extraTextSize);

        typedArray.recycle();

        if (rangeColorArray == null || rangeValueArray == null || rangeTextArray == null) {
            throw new IllegalArgumentException("CircleRangeView : rangeColorArray、 rangeValueArray、rangeTextArray  must be not null ");
        }
        if (rangeColorArray.length != rangeValueArray.length
                || rangeColorArray.length != rangeTextArray.length
                || rangeValueArray.length != rangeTextArray.length) {
            throw new IllegalArgumentException("arrays must be equal length");
        }

        this.mSection = rangeColorArray.length;

        mSparkleWidth = dp2px(15);
        mCalibrationWidth = dp2px(10);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFProgressArc = new RectF();
        mRectFCalibrationFArc = new RectF();
        mRectFTextArc = new RectF();
        mRectText = new Rect();

        mBackgroundColor = android.R.color.transparent;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mPadding = Math.max(
                Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom())
        );
        setPadding(mPadding, mPadding, mPadding, mPadding);

        mLength1 = mPadding + mSparkleWidth / 2f + dp2px(12);
        mLength2 = mLength1 + mCalibrationWidth + dp2px(1) + dp2px(5);

        int width = resolveSize(dp2px(220), widthMeasureSpec);
        mRadius = (width - mPadding * 2) / 2;

        setMeasuredDimension(width, width - dp2px(30));

        mCenterX = mCenterY = getMeasuredWidth() / 2f;
        mRectFProgressArc.set(
                mPadding + mSparkleWidth / 2f,
                mPadding + mSparkleWidth / 2f,
                getMeasuredWidth() - mPadding - mSparkleWidth / 2f,
                getMeasuredWidth() - mPadding - mSparkleWidth / 2f
        );

        mRectFCalibrationFArc.set(
                mLength1 + mCalibrationWidth / 2f,
                mLength1 + mCalibrationWidth / 2f,
                getMeasuredWidth() - mLength1 - mCalibrationWidth / 2f,
                getMeasuredWidth() - mLength1 - mCalibrationWidth / 2f
        );

        mPaint.setTextSize(sp2px(10));
        mPaint.getTextBounds("0", 0, "0".length(), mRectText);
        mRectFTextArc.set(
                mLength2 + mRectText.height(),
                mLength2 + mRectText.height(),
                getMeasuredWidth() - mLength2 - mRectText.height(),
                getMeasuredWidth() - mLength2 - mRectText.height()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.drawColor(ContextCompat.getColor(getContext(), mBackgroundColor));

        /**
         * 画圆弧背景
         */
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(borderSize);
        mPaint.setAlpha(80);
        mPaint.setColor(borderColor);
        canvas.drawArc(mRectFProgressArc, mStartAngle + 1, mSweepAngle - 2, false, mPaint);

        mPaint.setAlpha(255);

        /**
         * 画指示标
         */
        if (isAnimFinish) {

            float[] point = getCoordinatePoint(mRadius - mSparkleWidth / 2f, mStartAngle + calculateAngleWithValue(currentValue));
            mPaint.setColor(cursorColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(point[0], point[1], mSparkleWidth / 2f, mPaint);

        } else {

            float[] point = getCoordinatePoint(mRadius - mSparkleWidth / 2f, mAngleWhenAnim);
            mPaint.setColor(cursorColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(point[0], point[1], mSparkleWidth / 2f, mPaint);
        }

        /**
         * 画等级圆弧
         */
        mPaint.setShader(null);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setAlpha(255);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setStrokeWidth(mCalibrationWidth);

        if (rangeColorArray != null) {
            for (int i = 0; i < rangeColorArray.length; i++) {
                mPaint.setColor(Color.parseColor(rangeColorArray[i].toString()));
                float mSpaces = mSweepAngle / mSection;
                if (i == 0) {
                    canvas.drawArc(mRectFCalibrationFArc, mStartAngle + 3, mSpaces, false, mPaint);
                } else if (i == rangeColorArray.length - 1) {
                    canvas.drawArc(mRectFCalibrationFArc, mStartAngle + (mSpaces * i), mSpaces, false, mPaint);
                } else {
                    canvas.drawArc(mRectFCalibrationFArc, mStartAngle + (mSpaces * i) + 3, mSpaces, false, mPaint);
                }
            }
        }

        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(null);
        mPaint.setAlpha(255);

        /**
         * 画等级对应值的文本（居中显示）
         */
        if (rangeColorArray != null && rangeValueArray != null && rangeTextArray != null) {

            if (!TextUtils.isEmpty(currentValue)) {
                int pos = 0;

                for (int i = 0; i < rangeValueArray.length; i++) {
                    if (rangeValueArray[i].equals(currentValue)) {
                        pos = i;
                        break;
                    }
                }

                mPaint.setColor(Color.parseColor(rangeColorArray[pos].toString()));
                mPaint.setTextAlign(Paint.Align.CENTER);

                String txt=rangeTextArray[pos].toString();

                if (txt.length() <= 4) {
                    mPaint.setTextSize(rangeTextSize);
                    canvas.drawText(txt, mCenterX, mCenterY + dp2px(10), mPaint);
                } else {
                    mPaint.setTextSize(rangeTextSize - 10);
                    String top = txt.substring(0, 4);
                    String bottom = txt.substring(4, txt.length());

                    canvas.drawText(top, mCenterX, mCenterY, mPaint);
                    canvas.drawText(bottom, mCenterX, mCenterY + dp2px(30), mPaint);
                }
            }
        }

        /**
         * 画附加信息
         */
        if (extraList != null && extraList.size() > 0) {
            mPaint.setAlpha(160);
            mPaint.setColor(extraTextColor);
            mPaint.setTextSize(extraTextSize);
            for (int i = 0; i < extraList.size(); i++) {
                canvas.drawText(extraList.get(i), mCenterX, mCenterY + dp2px(50) + i * dp2px(20), mPaint);
            }
        }

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

    private float[] getCoordinatePoint(float radius, float angle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    /**
     * 根据起始角度计算对应值应显示的角度大小
     */
    private float calculateAngleWithValue(String level) {

        int pos = -1;

        for (int j = 0; j < rangeValueArray.length; j++) {
            if (rangeValueArray[j].equals(level)) {
                pos = j;
                break;
            }
        }

        float degreePerSection = 1f * mSweepAngle / mSection;

        if (pos == -1) {
            return 0;
        } else if (pos == 0) {
            return degreePerSection / 2;
        } else {
            return pos * degreePerSection + degreePerSection / 2;
        }
    }

    /**
     * 设置值并播放动画
     *
     * @param value 值
     */
    public void setValueWithAnim(String value) {
        setValueWithAnim(value,null);
    }

    /**
     * 设置值并播放动画
     *
     * @param value  值
     * @param extras 底部附加信息
     */
    public void setValueWithAnim(String value, List<String> extras) {
        if (!isAnimFinish) {
            return;
        }

        this.currentValue = value;
        this.extraList=extras;

        // 计算最终值对应的角度，以扫过的角度的线性变化来播放动画
        float degree = calculateAngleWithValue(value);

        ValueAnimator degreeValueAnimator = ValueAnimator.ofFloat(mStartAngle, mStartAngle + degree);
        degreeValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAngleWhenAnim = (float) animation.getAnimatedValue();
            }
        });

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(0, (int) degree);
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                postInvalidate();
            }
        });

        long delay = 1500;

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .setDuration(delay)
                .playTogether(creditValueAnimator, degreeValueAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimFinish = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimFinish = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isAnimFinish = true;
            }
        });
        animatorSet.start();
    }
}
