package com.uou.alarmit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uou.alarmit.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {
    lateinit var binding: ActivityWebBinding
    var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)

        if (intent.hasExtra("url")) {
            url = intent.getStringExtra("url")!!
        }

        val safeUrl = UrlSecurity.normalizeSafeUrl(url)
        if (safeUrl == null) {
            Log.w("WebActivity", "Blocked unsafe URL")
            Toast.makeText(this, "안전하지 않은 링크는 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        url = safeUrl
        Log.d("WebActivity", "Opening validated URL")

        binding.webWebviewWv.loadUrl(url)

        binding.webBackBtnIv.setOnClickListener {
            finish()
        }

        binding.webLinkBtnIv.setOnClickListener {
            val externalUrl = UrlSecurity.normalizeSafeUrl(url)
            if (externalUrl == null) {
                Toast.makeText(this, "안전하지 않은 링크는 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl))
            startActivity(browserIntent)
        }

        setContentView(binding.root)
    }
}
