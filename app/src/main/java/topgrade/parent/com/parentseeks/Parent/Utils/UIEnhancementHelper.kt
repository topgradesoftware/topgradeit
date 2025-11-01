@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.card.MaterialCardView

/**
 * Modern UI/UX Enhancement Helper
 * Provides smooth animations, transitions, and micro-interactions
 * 
 * @author Topgradeit Team
 * @version 1.0
 * @since 2025-10-15
 */
object UIEnhancementHelper {
    
    private const val DEFAULT_DURATION = 300L
    private const val FAST_DURATION = 200L
    private const val SLOW_DURATION = 500L
    
    /**
     * Animate view with smooth fade in
     */
    fun fadeIn(view: View, duration: Long = DEFAULT_DURATION, onComplete: (() -> Unit)? = null) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete?.invoke()
                }
            })
            .start()
    }
    
    /**
     * Animate view with smooth fade out
     */
    fun fadeOut(view: View, duration: Long = FAST_DURATION, hideOnComplete: Boolean = true) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (hideOnComplete) {
                        view.visibility = View.GONE
                    }
                }
            })
            .start()
    }
    
    /**
     * Slide view up from bottom with fade
     */
    fun slideUpFadeIn(view: View, duration: Long = DEFAULT_DURATION) {
        view.alpha = 0f
        view.translationY = 100f
        view.visibility = View.VISIBLE
        
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
    
    /**
     * Slide view down and fade out
     */
    fun slideDownFadeOut(view: View, duration: Long = FAST_DURATION) {
        view.animate()
            .alpha(0f)
            .translationY(100f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    view.translationY = 0f
                }
            })
            .start()
    }
    
    /**
     * Scale animation for cards (bounce effect)
     */
    fun scaleIn(view: View, duration: Long = DEFAULT_DURATION) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f
        view.visibility = View.VISIBLE
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator())
            .start()
    }
    
    /**
     * Press effect for cards (scale down on press)
     */
    fun addPressEffect(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                    v.performClick()
                }
            }
            true
        }
    }
    
    /**
     * Staggered animation for multiple views
     */
    fun staggeredFadeIn(views: List<View>, delayBetween: Long = 50L) {
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.visibility = View.VISIBLE
            
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * delayBetween)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
    
    /**
     * Rotate animation (useful for refresh icons)
     */
    fun rotate(view: View, degrees: Float = 360f, duration: Long = DEFAULT_DURATION) {
        view.animate()
            .rotation(degrees)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }
    
    /**
     * Shake animation (for error states)
     */
    fun shake(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = 500
        animator.start()
    }
    
    /**
     * Pulse animation (for attention)
     */
    fun pulse(view: View, repeat: Boolean = false) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
        
        scaleX.duration = 1000
        scaleY.duration = 1000
        
        if (repeat) {
            scaleX.repeatCount = ValueAnimator.INFINITE
            scaleY.repeatCount = ValueAnimator.INFINITE
        }
        
        scaleX.start()
        scaleY.start()
    }
    
    /**
     * Add elevation animation to card (Material Design)
     */
    fun elevateCard(card: MaterialCardView, elevationDp: Float = 12f) {
        val context = card.context
        val elevationPx = dpToPx(context, elevationDp)
        
        card.animate()
            .translationZ(elevationPx)
            .setDuration(FAST_DURATION)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
    
    /**
     * Reset card elevation
     */
    fun resetCardElevation(card: MaterialCardView) {
        card.animate()
            .translationZ(0f)
            .setDuration(FAST_DURATION)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
    
    /**
     * Reveal animation (circular reveal for API 21+)
     */
    fun circularReveal(view: View, centerX: Int, centerY: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val finalRadius = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
            
            val anim = android.view.ViewAnimationUtils.createCircularReveal(
                view, centerX, centerY, 0f, finalRadius
            )
            
            view.visibility = View.VISIBLE
            anim.duration = SLOW_DURATION
            anim.start()
        } else {
            // Fallback for older devices
            fadeIn(view)
        }
    }
    
    /**
     * Utility: Convert dp to pixels
     */
    private fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
    
    /**
     * Set up modern ripple effect for view
     */
    fun setupRippleEffect(view: View, @androidx.annotation.ColorInt color: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val ripple = android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(color),
                view.background,
                null
            )
            view.background = ripple
        }
    }
    
    /**
     * Animate progress bar smoothly
     */
    fun animateProgress(progressBar: android.widget.ProgressBar, toProgress: Int, duration: Long = DEFAULT_DURATION) {
        val animator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, toProgress)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
    
    /**
     * Show view with spring animation
     */
    fun springIn(view: View) {
        view.scaleX = 0.3f
        view.scaleY = 0.3f
        view.alpha = 0f
        view.visibility = View.VISIBLE
        
        ViewCompat.animate(view)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(SLOW_DURATION)
            .setInterpolator(OvershootInterpolator(2f))
            .start()
    }
    
    /**
     * Flip animation
     */
    fun flip(view: View, duration: Long = DEFAULT_DURATION) {
        view.animate()
            .rotationY(180f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.rotationY = 0f
            }
            .start()
    }
    
    /**
     * Translate horizontally
     */
    fun slideHorizontally(view: View, fromX: Float, toX: Float, duration: Long = DEFAULT_DURATION) {
        view.translationX = fromX
        view.animate()
            .translationX(toX)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
    
    /**
     * Cross-fade between two views
     */
    fun crossFade(fadeOut: View, fadeIn: View, duration: Long = DEFAULT_DURATION) {
        fadeIn.alpha = 0f
        fadeIn.visibility = View.VISIBLE
        
        fadeOut.animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fadeOut.visibility = View.GONE
                }
            })
        
        fadeIn.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null)
    }
}

