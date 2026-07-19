package com.example.airwatchpoc

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airwatch.sdk.SDKManager

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        statusText = findViewById(R.id.statusText)
        findViewById<Button>(R.id.checkSdkButton).setOnClickListener {
            statusText.text = runSdkSmokeTest()
        }
    }

    private fun runSdkSmokeTest(): String {
        val lines = mutableListOf<String>()

        lines += "AirWatch SDK smoke test"
        lines += "App package: $packageName"
        lines += ""

        lines += check("AAR class available") {
            SDKManager::class.java.name
        }

        lines += check("AAR manifest metadata") {
            val appInfo = packageManager.getApplicationInfo(packageName, metadataFlags())
            appInfo.metaData?.getString("WorkspaceOneSdkVersion") ?: "not found"
        }

        lines += check("Binding package") {
            SDKManager.getBindingPackageName(this).ifBlank { "not found" }
        }

        lines += check("Current enrolled anchor app") {
            SDKManager.getCurrentEnrolledAnchorAppPackage(this) ?: "none"
        }

        lines += check("SDKManager.init") {
            val manager = SDKManager.init(applicationContext)
            "success, serviceConnected=${SDKManager.isServiceConnected()}, apiVersion=${manager.getAPIVersion()}"
        }

        lines += ""
        lines += "Expected result:"
        lines += "- On a managed/enrolled device with Workspace ONE Hub available, SDKManager.init should succeed."
        lines += "- On an unmanaged device, binding/init may fail. That still confirms the AAR is packaged and callable."

        return lines.joinToString(separator = "\n")
    }

    private fun check(label: String, block: () -> String): String {
        return try {
            "PASS - $label: ${block()}"
        } catch (throwable: Throwable) {
            "FAIL - $label: ${throwable.javaClass.simpleName}: ${throwable.message.orEmpty()}"
        }
    }

    private fun metadataFlags(): Int {
        @Suppress("DEPRECATION")
        return android.content.pm.PackageManager.GET_META_DATA
    }
}
