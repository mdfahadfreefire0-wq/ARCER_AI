package com.arcer.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var onResult: ((String) -> Unit)? = null
    private var onStatus: ((String) -> Unit)? = null

    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening()
        else onStatus?.invoke("মাইক্রোফোন অনুমতি দেওয়া হয়নি")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }
        setContent {
            ArcerScreen(
                listen = { result, status ->
                    onResult = result
                    onStatus = status
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) startListening() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                },
                speak = { text -> tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "arcer_reply") }
            )
        }
    }

    private fun startListening() {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            onStatus?.invoke("এই ফোনে Speech Recognition পাওয়া যাচ্ছে না")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { onStatus?.invoke("শুনছি…") }
            override fun onBeginningOfSpeech() { onStatus?.invoke("বলুন…") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { onStatus?.invoke("প্রসেস করছি…") }
            override fun onError(error: Int) { onStatus?.invoke("শোনা যায়নি। আবার চেষ্টা করুন।") }
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                onResult?.invoke(text)
                onStatus?.invoke(if (text.isBlank()) "কিছু শোনা যায়নি" else "শোনা হয়েছে")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer.startListening(intent)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale("bn", "BD")
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun ArcerScreen(
    listen: ((String) -> Unit, (String) -> Unit) -> Unit,
    speak: (String) -> Unit
) {
    var heard by remember { mutableStateOf("এখনো কোনো কথা শোনা হয়নি") }
    var status by remember { mutableStateOf("প্রস্তুত") }
    var reply by remember { mutableStateOf("আমি ARCER। মাইক্রোফোন বাটনে চাপ দিয়ে কথা বলুন।") }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF25A7FF),
            background = Color(0xFF070B14),
            surface = Color(0xFF101A2B)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF070B14)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("ARCER", color = Color(0xFF38B6FF), fontSize = 38.sp)
                Text("PERSONAL VOICE ASSISTANT", color = Color.LightGray, fontSize = 11.sp)
                Spacer(Modifier.height(32.dp))
                Box(
                    modifier = Modifier.size(150.dp).background(
                        Color(0xFF102A46), RoundedCornerShape(100.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", color = Color(0xFF55C2FF), fontSize = 52.sp)
                }
                Spacer(Modifier.height(28.dp))
                Text(status, color = Color(0xFF55C2FF), fontSize = 16.sp)
                Spacer(Modifier.height(14.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101A2B))) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("আপনি বলেছেন", color = Color.Gray)
                        Text(heard, color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        Text("ARCER-এর উত্তর", color = Color.Gray)
                        Text(reply, color = Color.White)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        listen(
                            { text ->
                                heard = text
                                reply = when {
                                    text.isBlank() -> "আবার বলুন।"
                                    text.contains("হ্যালো") || text.contains("হাই") ->
                                        "হ্যালো! আমি ARCER। কীভাবে সাহায্য করব?"
                                    text.contains("সময়") ->
                                        "সময় জানতে ফোনের ঘড়ি দেখুন।"
                                    else ->
                                        "আপনার কথা শুনেছি: $text। আরও কাজের জন্য অনলাইন AI সংযোগ প্রয়োজন।"
                                }
                                speak(reply)
                            },
                            { value -> status = value }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087BC1)),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text("🎙  কথা বলুন", fontSize = 18.sp)
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    "নোট: এটি প্রথম Buildable সংস্করণ। স্ক্রিন বন্ধে সবসময় wake-word শোনা এবং পূর্ণ Offline AI এখনো যুক্ত হয়নি।",
                    color = Color(0xFF9AA9BD),
                    fontSize = 12.sp
                )
            }
        }
    }
}
