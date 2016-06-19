package dcnh35.com.checkdialoglibrary;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Custom view that will draw a customized (to some extent) checkmark that can be anywhere in your
 * interface. It was designed to be placed as an overlay of other views, such as an ImageView or
 * VideoView. A feedback to the user clicking something.
 * <p/>
 * Author: lambor
 * adapted from Author: juan.cortes@devtopia.coop
 */
public class CheckmarkView extends View {
    public final static int OK = 1;
    public final static int ERROR = 0;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ERROR, OK})
    public @interface WaitResult {}

    @WaitResult
    private int result = OK;
    //Static strings and configurable variables (not from xml)
    private final static String FIRST_LEG_LENGTH = "first_leg_length";
    private final static String SECOND_LEG_LENGTH = "second_leg_length";
    protected float first_leg_length = 50;
    protected float second_leg_length = 100;

    private final static Double OK_FIRST_PORTION = 0.4;
    private final static Double OK_SECOND_PORTION = 0.6;
    private final static Double ERROR_TIME_PORTION = 0.5;

    private final static float reverse_root_2 = 0.707f;
    private final static Float ZERO = 0f;

    protected DisplayMetrics mMetrics = new DisplayMetrics();
    private float scale = 1.0f;
    //view's center position
    private float centerY;
    private float centerX;

    protected int stroke_width = 15;
    //Editable Properties
    protected int total_duration = 1500;
    protected int stroke_color = Color.BLACK;

    //these value base on the view's width equals 250dp
    protected final float mOKPlaceHolderFirstLegLength = 50;
    protected final float mOKPlaceHolderSecondLegLength = 90;
    protected final float mERRORPlaceHoldLegLength = 160.0f;
    //the leg real px length
    protected float mPlaceHoldFirstLegLength;
    protected float mPlaceHoldSecondLegLength;

    protected Paint mPaint;

    protected ObjectAnimator mFirstLegAnimator, mSecondLegAnimator;
    protected OnAnimationCompleteListener completeListener;

    //Flags
    private boolean shouldAnimateFirst = false;
    private boolean shouldAnimateSecond = false;

    public CheckmarkView(Context context) {
        super(context);
    }

    public CheckmarkView(Context context, AttributeSet set) {
        super(context, set);

        TypedArray a = context.obtainStyledAttributes(set, R.styleable.check_mark_view);
        Integer totalDuration = a.getInt(R.styleable.check_mark_view_total_duration, this.total_duration);
        Integer strokeColor = a.getColor(R.styleable.check_mark_view_stroke_color, this.stroke_color);

        if (strokeColor != null) {
            stroke_color = strokeColor;
        }

        if (totalDuration != null) {
            total_duration = totalDuration;
        }

        a.recycle();

        init(context);
    }

    private void init(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mMetrics);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(stroke_width);
        mPaint.setColor(stroke_color);
    }

    public CheckmarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        int widht = layoutParams.width;
        int height = getLayoutParams().height;
        int size = Math.min(widht, height);
        scale = size / (250.0f * mMetrics.density);
        centerX = ((float)widht)/2;
        centerY = ((float)height)/2;

        stroke_width = (int) (stroke_width * mMetrics.density * scale);
        mPaint.setStrokeWidth(stroke_width);
//    setBackGround();
    }

    private void resetAnimation() {
        first_leg_length = ZERO;
        second_leg_length = ZERO;

        shouldAnimateFirst = true;
        shouldAnimateSecond = false;


        if (result == OK) {
            mPlaceHoldFirstLegLength = mOKPlaceHolderFirstLegLength * mMetrics.density * scale;
            mPlaceHoldSecondLegLength = mOKPlaceHolderSecondLegLength * mMetrics.density * scale;

            mFirstLegAnimator = ObjectAnimator.ofFloat(this, FIRST_LEG_LENGTH, ZERO, mPlaceHoldFirstLegLength);
            mFirstLegAnimator.setDuration((long) (total_duration * OK_FIRST_PORTION));
            mFirstLegAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            mSecondLegAnimator = ObjectAnimator.ofFloat(this, SECOND_LEG_LENGTH, ZERO, mPlaceHoldSecondLegLength);
            mSecondLegAnimator.setDuration((long) (total_duration * OK_SECOND_PORTION));
//            mSecondLegAnimator.setInterpolator(new OvershootInterpolator());
            mSecondLegAnimator.setInterpolator(new AnticipateOvershootInterpolator());
        } else {
            mPlaceHoldFirstLegLength = mERRORPlaceHoldLegLength * mMetrics.density * scale;
            mPlaceHoldSecondLegLength = mERRORPlaceHoldLegLength * mMetrics.density * scale;

            mFirstLegAnimator = ObjectAnimator.ofFloat(this, FIRST_LEG_LENGTH, ZERO, mPlaceHoldFirstLegLength);
            mFirstLegAnimator.setDuration((long) (total_duration * ERROR_TIME_PORTION));
            mFirstLegAnimator.setInterpolator(new AccelerateInterpolator());

            mSecondLegAnimator = ObjectAnimator.ofFloat(this, SECOND_LEG_LENGTH, ZERO, mPlaceHoldSecondLegLength);
            mSecondLegAnimator.setDuration((long) (total_duration * ERROR_TIME_PORTION));
            mSecondLegAnimator.setInterpolator(new OvershootInterpolator());
        }
        mSecondLegAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(completeListener!=null) completeListener.onComplete();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mFirstLegAnimator.start();
    }

    public void start(@WaitResult int result) {
        this.result = result;
        resetAnimation();
    }

    //set view border background
    public void setBackGround() {
        GradientDrawable circle = (GradientDrawable) getBackground();
        circle.setStroke(stroke_width, stroke_color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.setBackground(circle);
        } else {
            this.setBackgroundDrawable(circle);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (shouldAnimateFirst) {
            Path p = new Path();
            float offsetY,offsetX;
            if(result == OK){
                offsetY = 120 * mMetrics.density * scale;
                offsetX = 50 * mMetrics.density * scale;

                p.moveTo(offsetX, offsetY);
                p.lineTo(offsetX + first_leg_length, offsetY + first_leg_length);

                if (shouldAnimateSecond) {
                    p.moveTo(offsetX + first_leg_length, offsetY + first_leg_length - (stroke_width / 3 * 2.1f));
                    p.lineTo(offsetX + first_leg_length + second_leg_length, offsetY + first_leg_length - second_leg_length - (stroke_width / 3 * 2.1f));
                }
            }else{
                //mPlaceHoldFirstLegLength == mPlaceHoldSecondLegLength
                final float half_step = mPlaceHoldFirstLegLength * reverse_root_2 * 0.5f;

                offsetY = centerY-half_step;
                offsetX = centerX+half_step;
                p.moveTo(offsetX, offsetY);
                p.lineTo(offsetX- first_leg_length *reverse_root_2, offsetY + first_leg_length *reverse_root_2);

                if(shouldAnimateSecond){
                    offsetY = centerY-half_step;
                    offsetX = centerX-half_step;
                    p.moveTo(offsetX, offsetY);
                    p.lineTo(offsetX+ second_leg_length *reverse_root_2,offsetY+ second_leg_length *reverse_root_2);
                }
            }
            canvas.drawPath(p, mPaint);
        }
    }

    /**
     * Getter and setter for the animated length of the first leg
     *
     * @return
     */
    public float getFirst_leg_length() {
        return first_leg_length;
    }

    public void setFirst_leg_length(float first_leg_length) {
        this.first_leg_length = first_leg_length;
        if (first_leg_length == mPlaceHoldFirstLegLength) {
            mSecondLegAnimator.start();
            shouldAnimateSecond = true;
        }
        this.invalidate();
    }

    /**
     * Getter and setter for the animated length of the second leg
     *
     * @return
     */
    public float getSecond_leg_length() {
        return second_leg_length;
    }

    public void setSecond_leg_length(float second_leg_length) {
        this.second_leg_length = second_leg_length;
        this.invalidate();
    }

    public void setCompleteListener(OnAnimationCompleteListener listener){
        this.completeListener = listener;
    }

    public interface OnAnimationCompleteListener{
        void onComplete();
    }

}