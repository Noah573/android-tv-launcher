package com.tvlauncher.ui.screensaver

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tvlauncher.R
import com.tvlauncher.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreensaverFragment : Fragment() {

    private lateinit var clockText: TextView
    private lateinit var dateText: TextView
    private lateinit var backgroundView: View

    private val handler = Handler(Looper.getMainLooper())
    private var timeAnimator: ValueAnimator? = null
    private var colorAnimator: ValueAnimator? = null

    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_screensaver, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clockText = view.findViewById(R.id.clockText)
        dateText = view.findViewById(R.id.dateText)
        backgroundView = view.findViewById(R.id.screensaverBackground)

        setupBackground()
        updateClock()
        updateDate()

        view.setOnTouchListener { _, _ ->
            dismiss()
            true
        }

        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setOnKeyListener { _, keyCode, _ ->
            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                dismiss()
                true
            } else {
                false
            }
        }

        view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        handler.post(timeUpdateRunnable)
        startColorAnimation()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeUpdateRunnable)
        colorAnimator?.cancel()
    }

    private fun setupBackground() {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.parseColor("#0D0221"),
                Color.parseColor("#150734"),
                Color.parseColor("#0A1628")
            )
        )
        backgroundView.background = gradient
    }

    private fun startColorAnimation() {
        val auroraColors = listOf(
            intArrayOf(Color.parseColor("#0D0221"), Color.parseColor("#150734"), Color.parseColor("#0A1628")),
            intArrayOf(Color.parseColor("#0A1628"), Color.parseColor("#1B0A3C"), Color.parseColor("#0D1F3C")),
            intArrayOf(Color.parseColor("#0D1F3C"), Color.parseColor("#142850"), Color.parseColor("#0D0221")),
            intArrayOf(Color.parseColor("#150734"), Color.parseColor("#0D0221"), Color.parseColor("#1B0A3C"))
        )

        var currentIndex = 0

        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 20000 // 20 seconds per cycle
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                if (fraction < 0.001f) {
                    currentIndex = (currentIndex + 1) % auroraColors.size
                    val nextIndex = (currentIndex + 1) % auroraColors.size
                    val colors = IntArray(3) { i ->
                        blendColor(
                            auroraColors[currentIndex][i],
                            auroraColors[nextIndex][i],
                            fraction
                        )
                    }
                    val gradient = GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        colors
                    )
                    backgroundView.background = gradient
                }
            }
            start()
        }
    }

    private fun blendColor(color1: Int, color2: Int, fraction: Float): Int {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        val r = (r1 + (r2 - r1) * fraction).toInt()
        val g = (g1 + (g2 - g1) * fraction).toInt()
        val b = (b1 + (b2 - b1) * fraction).toInt()

        return Color.rgb(r, g, b)
    }

    private fun updateClock() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        clockText.text = sdf.format(Date())
    }

    private fun updateDate() {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault())
        dateText.text = sdf.format(Date())
    }

    private fun dismiss() {
        handler.removeCallbacks(timeUpdateRunnable)
        colorAnimator?.cancel()

        view?.animate()
            ?.alpha(0f)
            ?.setDuration(500)
            ?.withEndAction {
                (activity as? MainActivity)?.closeOverlay()
            }
            ?.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timeUpdateRunnable)
        colorAnimator?.cancel()
    }

    companion object {
        fun newInstance(): ScreensaverFragment = ScreensaverFragment()
    }
}
