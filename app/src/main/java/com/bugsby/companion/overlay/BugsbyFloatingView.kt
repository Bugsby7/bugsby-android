package com.bugsby.companion.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bugsby.companion.brain.BrainRouter
import com.bugsby.companion.character.BugsbyNinjaCharacter
import com.bugsby.companion.character.CompanionMode
import com.bugsby.companion.character.Expression
import com.bugsby.companion.data.AppPreferences
import com.bugsby.companion.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("ViewConstructor")
class BugsbyFloatingView(
    context: Context,
    private val windowManager: WindowManager,
    private val onOpenSettings: () -> Unit
) : FrameLayout(context) {

    private val prefs = AppPreferences(context)
    private val brainRouter = BrainRouter(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 50
        y = 350
    }

    private var initialX = 0
    private var initialY = 0
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isDragging = false

    private val idleHandler = Handler(Looper.getMainLooper())
    private var isIdle = false

    // State observable by Compose
    private val expressionState = mutableStateOf(Expression.IDLE)
    private val modeState = mutableStateOf(
        if (prefs.isSuperSaiyan) CompanionMode.SUPER_SAIYAN_3D else CompanionMode.NORMAL_2D
    )
    private val showActionMenu = mutableStateOf(false)
    private val speechBubbleText = mutableStateOf<String?>(null)
    private val viewAlpha = mutableStateOf(1f)

    private var voiceManager: VoiceManager? = null

    private val idleRunnable = Runnable {
        // Hide in shadows: fade to semi-transparent & tuck to edge
        isIdle = true
        viewAlpha.value = 0.35f
        expressionState.value = Expression.SLEEPY
        snapToNearestEdge(tuck = true)
    }

    init {
        val composeView = ComposeView(context).apply {
            setContent {
                FloatingOverlayContent()
            }
        }
        addView(composeView)
        windowManager.addView(this, layoutParams)

        voiceManager = VoiceManager(context) { recognizedText ->
            handleUserVoiceInput(recognizedText)
        }

        resetIdleTimer()
    }

    @Composable
    private fun FloatingOverlayContent() {
        val expression by expressionState
        val mode by modeState
        val showMenu by showActionMenu
        val speechText by speechBubbleText
        val alpha by viewAlpha

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            // Speech Dialog Bubble
            AnimatedVisibility(
                visible = speechText != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1E24).copy(alpha = 0.95f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .widthIn(max = 220.dp)
                ) {
                    Text(
                        text = speechText ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Quick Action Menu (Expand on tap)
            AnimatedVisibility(
                visible = showMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    // Voice Mic
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B2B36))
                            .clickable {
                                speechBubbleText.value = "Listening..."
                                voiceManager?.startListening()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice", tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    // Super Saiyan Evolve / Normal Toggle Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (mode == CompanionMode.SUPER_SAIYAN_3D) Color(0xFFFFD700) else Color(0xFF3B3B48)
                            )
                            .clickable {
                                toggleSuperSaiyan()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Evolve",
                            tint = if (mode == CompanionMode.SUPER_SAIYAN_3D) Color.Black else Color(0xFFFFD700),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Settings
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B2B36))
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Fluffy Ninja Character (Bugsby)
            BugsbyNinjaCharacter(
                sizeDp = prefs.characterSize.dp,
                expression = expression,
                mode = mode,
                furTint = Color(android.graphics.Color.parseColor(prefs.tintColorHex))
            )
        }
    }

    private fun toggleSuperSaiyan() {
        val willBeSsj = modeState.value != CompanionMode.SUPER_SAIYAN_3D
        modeState.value = if (willBeSsj) CompanionMode.SUPER_SAIYAN_3D else CompanionMode.NORMAL_2D
        prefs.isSuperSaiyan = willBeSsj
        expressionState.value = if (willBeSsj) Expression.EXCITED else Expression.IDLE

        val reply = if (willBeSsj) "Super Saiyan mode activated! ⚡" else "Back to stealth mode. 🥷"
        speechBubbleText.value = reply
        voiceManager?.speak(reply, willBeSsj)
    }

    private fun handleUserVoiceInput(text: String) {
        speechBubbleText.value = "\""$text\"""
        expressionState.value = Expression.THINKING

        coroutineScope.launch {
            val decision = brainRouter.processInput(text)
            decision.modeChange?.let { newMode ->
                modeState.value = newMode
                expressionState.value = if (newMode == CompanionMode.SUPER_SAIYAN_3D) Expression.EXCITED else Expression.IDLE
            }
            speechBubbleText.value = decision.spokenReply
            voiceManager?.speak(decision.spokenReply, modeState.value == CompanionMode.SUPER_SAIYAN_3D)
        }
    }

    private fun wakeUpFromShadows() {
        isIdle = false
        viewAlpha.value = 1f
        if (expressionState.value == Expression.SLEEPY) {
            expressionState.value = Expression.IDLE
        }
        resetIdleTimer()
    }

    private fun resetIdleTimer() {
        idleHandler.removeCallbacks(idleRunnable)
        idleHandler.postDelayed(idleRunnable, prefs.idleTimeoutSeconds * 1000L)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        wakeUpFromShadows()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                touchStartX = event.rawX
                touchStartY = event.rawY
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - touchStartX).toInt()
                val dy = (event.rawY - touchStartY).toInt()

                if (abs(dx) > 10 || abs(dy) > 10) {
                    isDragging = true
                    layoutParams.x = initialX + dx
                    layoutParams.y = initialY + dy
                    try {
                        windowManager.updateViewLayout(this, layoutParams)
                    } catch (_: Exception) {}
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    // Tap event: toggle action menu
                    showActionMenu.value = !showActionMenu.value
                    if (speechBubbleText.value != null) {
                        speechBubbleText.value = null
                    }
                } else {
                    snapToNearestEdge(tuck = false)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun snapToNearestEdge(tuck: Boolean) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val middle = screenWidth / 2

        val targetX = if (layoutParams.x < middle) {
            if (tuck) -30 else 20
        } else {
            if (tuck) screenWidth - width + 30 else screenWidth - width - 20
        }

        val animator = ValueAnimator.ofInt(layoutParams.x, targetX)
        animator.duration = 250
        animator.addUpdateListener { anim ->
            layoutParams.x = anim.animatedValue as Int
            try {
                windowManager.updateViewLayout(this@BugsbyFloatingView, layoutParams)
            } catch (_: Exception) {}
        }
        animator.start()
    }

    fun destroy() {
        idleHandler.removeCallbacks(idleRunnable)
        voiceManager?.destroy()
        try {
            windowManager.removeView(this)
        } catch (_: Exception) {}
    }
}
