package com.hui.lovebezier;

import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LoveLayout extends RelativeLayout {
	private Drawable mRed, mYellow, mBlue;
	private Drawable[] mDrawables;
	private Interpolator[] mInterpolators;
	private int mDrawableHeight, mDrawableWidth;
	private int mWidth, mHeight;
	private LayoutParams params;
	private Random mRandom = new Random();

	public LoveLayout(Context context) {
		this(context, null);
	}

	public LoveLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LoveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		initDrawable();
		initInterpolator();
		// ��ʼ��params
		params = new LayoutParams(mDrawableWidth, mDrawableHeight);
		// ������ˮƽ����
		params.addRule(CENTER_HORIZONTAL, TRUE);
		// �������ĵײ�
		params.addRule(ALIGN_PARENT_BOTTOM, TRUE);
	}

	/**
	 * ��ʼ�����ֲ岹��
	 */
	private void initInterpolator() {
		mInterpolators = new Interpolator[4];
		mInterpolators[0] = new LinearInterpolator();// ����
		mInterpolators[1] = new AccelerateDecelerateInterpolator();// �ȼ��ٺ����
		mInterpolators[2] = new AccelerateInterpolator();// ����
		mInterpolators[3] = new DecelerateInterpolator();// ����
	}

	private void initDrawable() {
		mRed = getResources().getDrawable(R.drawable.pl_red);
		mYellow = getResources().getDrawable(R.drawable.pl_yellow);
		mBlue = getResources().getDrawable(R.drawable.pl_blue);

		mDrawables = new Drawable[3];
		mDrawables[0] = mRed;
		mDrawables[1] = mYellow;
		mDrawables[2] = mBlue;

		// �õ�ͼƬ��ʵ�ʿ��
		mDrawableWidth = mRed.getIntrinsicWidth();
		mDrawableHeight = mRed.getIntrinsicHeight();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
	}

	public void addLove() {
		final ImageView loveIv = new ImageView(getContext());
		loveIv.setImageDrawable(mDrawables[mRandom.nextInt(mDrawables.length)]);
		loveIv.setLayoutParams(params);
		addView(loveIv);

		// ���Զ�����������
		AnimatorSet finalSet = getAnimatorSet(loveIv);
		finalSet.setInterpolator(mInterpolators[mRandom
				.nextInt(mInterpolators.length)]);
		// ����һ������
		finalSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				removeView(loveIv);
			}
		});
		finalSet.start();
	}

	/**
	 * �����������Զ���
	 */
	private AnimatorSet getAnimatorSet(ImageView loveIv) {
		// 1.alpha����
		ObjectAnimator alpha = ObjectAnimator
				.ofFloat(loveIv, "alpha", 0.3f, 1f);

		// 2.���Ŷ���
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(loveIv, "scaleX", 0.2f,
				1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(loveIv, "scaleY", 0.2f,
				1f);
		// �ոս��붯������
		AnimatorSet enter = new AnimatorSet();
		enter.setDuration(500);
		enter.playTogether(alpha, scaleX, scaleY);
		enter.setTarget(loveIv);

		// 3.���������߶���( ���ģ����ϵ��޸�ImageView������ponintF(x,y))
		ValueAnimator bezierValueAnimator = getBezierValueAnimator(loveIv);
		AnimatorSet bezierSet = new AnimatorSet();
		// ����ִ��
		bezierSet.playSequentially(enter, bezierValueAnimator);
		bezierSet.setTarget(loveIv);

		return bezierSet;
	}

	/**
	 * ���������߶���( ���ģ����ϵ��޸�ImageView������ponintF(x,y) )
	 */
	private ValueAnimator getBezierValueAnimator(final ImageView loveIv) {
		PointF pointF2 = getPonitF(2);
		PointF pointF1 = getPonitF(1);
		PointF pointF0 = new PointF((mWidth - mDrawableWidth) / 2, mHeight
				- mDrawableHeight);
		// ������λ��
		PointF pointF3 = new PointF(mRandom.nextInt(mWidth), 0);
		// ��ֵ��Evaluator,������view����ʻ·�������ϵ��޸�point.x,point.y��
		BezierEvaluator evaluator = new BezierEvaluator(pointF1, pointF2);
		// ���Զ����������ı�View�����ԣ������Ըı��Զ��������
		ValueAnimator animator = ValueAnimator.ofObject(evaluator, pointF0,
				pointF3);
		animator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				PointF pointF = (PointF) animation.getAnimatedValue();
				loveIv.setX(pointF.x);
				loveIv.setY(pointF.y);
				loveIv.setAlpha(1 - animation.getAnimatedFraction() + 0.1f);// �õ��ٷֱ�
			}
		});
		animator.setTarget(loveIv);
		animator.setDuration(3000);
		return animator;
	}

	private PointF getPonitF(int i) {
		PointF pointF = new PointF();
		// 0~layout width
		pointF.x = mRandom.nextInt(mWidth);
		// Ϊ��Ч����������֤point2.y<point1.y
		pointF.y = mRandom.nextInt(mHeight / 2) + mHeight / 2
				* (Math.abs(1 - i));
		return pointF;
	}
}
