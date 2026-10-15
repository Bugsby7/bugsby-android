package com.bugsby.companion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bugsby.companion.character.BugsbyNinjaCharacter
import com.bugsby.companion.character.CompanionMode
import com.bugsby.companion.character.Expression
import com.bugsby.companion.data.AppPreferences
import com.bugsby.companion.overlay.OverlayService

class MainActivity : ComponentActivity() {

    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)

        setContent {
            BugsbyDashboardScreen()
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            201
        )
    }

    private fun startBugsbyService() {
        if (!hasOverlayPermission()) {
            Toast.makeText(this, "Please grant Overlay permission first!", Toast.LENGTH_LONG).show()
            requestOverlayPermission()
            return
        }
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Bugsby is now floating on your screen!", Toast.LENGTH_SHORT).show()
    }

    private fun stopBugsbyService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
        Toast.makeText(this, "Bugsby has retreated to the shadows.", Toast.LENGTH_SHORT).show()
    }

    @Composable
    private fun BugsbyDashboardScreen() {
        var geminiKey by remember { mutableStateOf(prefs.geminiApiKey) }
        var isOverlayGranted by remember { mutableStateOf(hasOverlayPermission()) }
        var isAudioGranted by remember { mutableStateOf(hasAudioPermission()) }
        var isSuperSaiyanPreview by remember { mutableStateOf(prefs.isSuperSaiyan) }

        LaunchedEffect(Unit) {
            isOverlayGranted = hasOverlayPermission()
            isAudioGranted = hasAudioPermission()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF101014)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🥷 Bugsby AI Companion",
                    color = Color.White,
                    fontSize = 24.sp
                )
                Text(
                    text = "Floating 2D Ninja with Super Saiyan Evolve Mode",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Character Live Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF1B1B22), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BugsbyNinjaCharacter(
                        sizeDp = 130.dp,
                        expression = if (isSuperSaiyanPreview) Expression.EXCITED else Expression.IDLE,
                        mode = if (isSuperSaiyanPreview) CompanionMode.SUPER_SAIYAN_3D else CompanionMode.NORMAL_2D,
                        furTint = Color(android.graphics.Color.parseColor(prefs.tintColorHex))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Launch Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { startBugsbyService() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Start Bugsby")
                    }
                    Button(
                        onClick = { stopBugsbyService() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Stop Bugsby")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Permissions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Permissions Setup", color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Overlay permission row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("1. Display over other apps", color = Color.LightGray, fontSize = 14.sp)
                            Button(
                                onClick = { requestOverlayPermission() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOverlayGranted) Color(0xFF2E7D32) else Color(0xFF0288D1)
                                )
                            ) {
                                Text(if (isOverlayGranted) "Granted ✓" else "Grant")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Audio permission row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("2. Microphone for Voice", color = Color.LightGray, fontSize = 14.sp)
                            Button(
                                onClick = { requestAudioPermission() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAudioGranted) Color(0xFF2E7D32) else Color(0xFF0288D1)
                                )
                            ) {
                                Text(if (isAudioGranted) "Granted ✓" else "Grant")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Super Saiyan Toggle Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚡ Super Saiyan Mode", color = Color(0xFFFFD700), fontSize = 16.sp)
                        Text(
                            "Say 'evolve' to transform or 'normal' to return to base ninja form.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Preview Evolve Mode", color = Color.White)
                            Switch(
                                checked = isSuperSaiyanPreview,
                                onCheckedChange = {
                                    isSuperSaiyanPreview = it
                                    prefs.isSuperSaiyan = it
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cloud AI Brain Key
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B22))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Free Cloud AI Brain (Optional)", color = Color.White, fontSize = 16.sp)
                        Text(
                            "Paste free Google Gemini API key for advanced conversational chat.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = geminiKey,
                            onValueChange = {
                                geminiKey = it
                                prefs.geminiApiKey = it
                            },
                            label = { Text("Gemini API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
