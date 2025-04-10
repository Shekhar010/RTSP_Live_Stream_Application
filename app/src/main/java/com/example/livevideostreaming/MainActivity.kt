package com.example.livevideostreaming

import android.app.PictureInPictureParams
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private var streamStatus = false
    private lateinit var player: ExoPlayer
    private lateinit var streamButton: MaterialButton
    private lateinit var urlInput: EditText
//    private var recording = false // Flag to track recording status

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        streamButton = findViewById(R.id.materialButton)
        urlInput = findViewById(R.id.url_input)

        // Initialize player only once
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
            .build()

        val playerView = findViewById<PlayerView>(R.id.player_view)
        playerView.player = player


        streamButton.setOnClickListener {
            if (!streamStatus) {
                try {
                val url = urlInput.text.toString().trim()

                if (url.isEmpty()) {
                    Toast.makeText(this, "Please enter RTSP URL", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                    val mediaItem = MediaItem.Builder()
                        .setUri(url)
                        .setMimeType(MimeTypes.APPLICATION_RTSP)
                        .build()

                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()

                    updateStreamUI(started = true)
                    streamStatus = true

                } catch (e: Exception) {
                    Toast.makeText(this, "Streaming failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    // change the ui
                    updateStreamUI(started = false)
                }

            } else {
                // Stop stream
                player.stop()
                updateStreamUI(started = false)
                streamStatus = false
            }
        }

        val popOutButton = findViewById<MaterialButton>(R.id.popOutButton)
        popOutButton.setOnClickListener {
            enterPipMode()
        }


        /* record the screen feature */
        // button to start recording
//        val recordButton = findViewById<MaterialButton>(R.id.recordScreen)
//        recordButton.setOnClickListener {
//            if (recording == false) {
//                // check if streaming or not if not then don't record
//                if (streamStatus == true) {
//                    // then start recording
//
//
//                    // change the ui of button
//                    recordButton.setIconResource(R.drawable.stopstream)
//                    recordButton.text = "Stop"
//                } else {
//                    // show toast
//                    Toast.makeText(this, "start the stream first", Toast.LENGTH_SHORT).show()
//                }
//
//                recording = true
//            } else {
//                // check if the recoding is on and if on then stop
//
//
//                // revert the changes
//                recordButton.setIconResource(R.drawable.startrecording)
//                recordButton.text = "Start"
//                recording = false
//            }
//        }
    }

    // Update UI based on stream status
    private fun updateStreamUI(started: Boolean) {
        if (started) {
            streamButton.setIconResource(R.drawable.stopstream)
            streamButton.setStrokeColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.red
                    )
                )
            )
            streamButton.text = "Stop Streaming"
            streamButton.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            streamButton.setIconResource(R.drawable.icons8_stream)
            streamButton.setStrokeColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
            )
            streamButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            streamButton.text = "Start Streaming"
        }
    }

    // Picture-in-Picture mode
    @RequiresApi(Build.VERSION_CODES.O)
    private fun enterPipMode() {
        val aspectRatio = Rational(16, 9)
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .build()

        try {
            enterPictureInPictureMode(params)
        } catch (e: Exception) {
            Log.e("PipMode", "Failed to enter PiP: ${e.message}", e)
        }
    }

    // Hide/show UI in PiP mode
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        val visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
        findViewById<MaterialButton>(R.id.popOutButton).visibility = visibility
        findViewById<MaterialButton>(R.id.materialButton).visibility = visibility
        findViewById<TextInputLayout>(R.id.url_input_layout).visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
