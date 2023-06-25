package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val TAG = "LoadingButton"

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var textSize = resources.getDimension(R.dimen.default_text_size)
    private var textWidth = 0f
    private var circleXOffset = textSize / 2

    private var buttonTitle: String

    private var progressWidth = 0f
    private var progressCircle = 0f

    private var buttonBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private var buttonLoadingBackgroundColor =
        ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private var loadingCircleColor = ContextCompat.getColor(context, R.color.colorAccent)

    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> {
                buttonTitle = resources.getString(R.string.button_clicked)
                invalidate()
            }
            ButtonState.Loading -> {
                buttonTitle = resources.getString(R.string.button_loading)

                valueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat())
                valueAnimator.setDuration(3000)
                valueAnimator.addUpdateListener { animation ->
                    progressWidth = animation.animatedValue as Float
                    progressCircle = (widthSize.toFloat() / 360) * progressWidth
                    invalidate()
                }

                valueAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        progressWidth = 0f
                        if (buttonState == ButtonState.Loading) {
                            buttonState = ButtonState.Loading
                        }
                    }
                })
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                buttonTitle = resources.getString(R.string.button_download)
                progressWidth = 0f
                progressCircle = 0f
                invalidate()
            }
        }
    }


    init {
        buttonTitle = resources.getString(R.string.button_download)
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_buttonBackgroundColor, 0)
            buttonLoadingBackgroundColor =
                getColor(R.styleable.LoadingButton_buttonBackgroundLoadingColor, 0)
            loadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.default_text_size)
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private fun Canvas.drawButtonBackground() {
        paint.color = buttonBackgroundColor
        drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)
    }

    private fun Canvas.drawButtonLoadingBackground() {
        paint.color = buttonLoadingBackgroundColor
        drawRect(0f, 0f, progressWidth, heightSize.toFloat(), paint)
    }

    private fun Canvas.drawCircleLoading() {
        save()
        translate(widthSize / 2 + textWidth / 2 + circleXOffset, heightSize / 2 - textSize / 2)
        paint.color = loadingCircleColor
        drawArc(RectF(0f, 0f, textSize, textSize), 0F, progressCircle * 0.360f, true, paint)
        restore()
    }

    private fun Canvas.drawTitle() {
        paint.color = Color.WHITE
        textWidth = paint.measureText(buttonTitle)
        drawText(
            buttonTitle,
            widthSize / 2 - textWidth / 2,
            heightSize / 2 - (paint.descent() + paint.ascent()) / 2,
            paint
        )
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            drawButtonBackground()
            drawButtonLoadingBackground()
            drawCircleLoading()
            drawTitle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun onChangeButtonState(state: ButtonState) {
        Log.d(TAG, "changeButtonState: $state")
        if (buttonState != state) {
            buttonState = state
            requestLayout()
        }
    }

}