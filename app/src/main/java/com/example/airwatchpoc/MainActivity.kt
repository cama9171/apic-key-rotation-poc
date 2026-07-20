package com.example.airwatchpoc

//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.airwatch.sdk.SDKManager
//import org.json.JSONObject
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var statusText: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        statusText = findViewById(R.id.statusText)
//        findViewById<Button>(R.id.checkSdkButton).setOnClickListener {
//            statusText.text = runSdkSmokeTest()
//        }
//    }
//
//    private fun runSdkSmokeTest(): String {
//        val lines = mutableListOf<String>()
//
//        lines += "AirWatch SDK smoke test"
//        lines += "App package: $packageName"
//        lines += ""
//
//        lines += check("AAR class available") {
//            SDKManager::class.java.name
//        }
//
//        lines += check("AAR manifest metadata") {
//            val appInfo = packageManager.getApplicationInfo(packageName, metadataFlags())
//            appInfo.metaData?.getString("WorkspaceOneSdkVersion") ?: "not found"
//        }
//
//        lines += check("Binding package") {
//            SDKManager.getBindingPackageName(this).ifBlank { "not found" }
//        }
//
//        lines += check("Current enrolled anchor app") {
//            SDKManager.getCurrentEnrolledAnchorAppPackage(this) ?: "none"
//        }
//
//        lines += check("SDKManager.init") {
//            val manager = SDKManager.init(applicationContext)
//            "success, serviceConnected=${SDKManager.isServiceConnected()}, apiVersion=${manager.getAPIVersion()}"
//        }
//
//        lines += check("SDK Custom Settings (Raw)") {
//            val manager = SDKManager.init(applicationContext)
//            val customSettings = manager.customSettings
//            if (customSettings.isNullOrBlank()) "None found or service not bound yet" else customSettings
//        }
//
//        lines += check("Parsed Apigee Credential") {
//            val manager = SDKManager.init(applicationContext)
//            val customSettings = manager.customSettings
//
//            if (!customSettings.isNullOrBlank()) {
//                try {
//                    // Parse the raw string from the UEM console into a JSON Object
//                    val jsonObject = JSONObject(customSettings)
//                    // Extract the specific key
//                    jsonObject.optString("apigee_cred", "Key 'apigee_cred' not found in JSON")
//                } catch (e: Exception) {
//                    "Invalid JSON format: ${e.message}"
//                }
//            } else {
//                "No custom settings to parse"
//            }
//        }
//
//        lines += "================="
//        lines += "Expected result:"
//        lines += "- On a managed/enrolled device with Workspace ONE Hub available, SDKManager.init should succeed."
//        lines += "- On an unmanaged device, binding/init may fail. That still confirms the AAR is packaged and callable."
//
//        return lines.joinToString(separator = "\n")
//    }
//
//    private fun check(label: String, block: () -> String): String {
//        return try {
//            "PASS - $label: ${block()}"
//        } catch (throwable: Throwable) {
//            "FAIL - $label: ${throwable.javaClass.simpleName}: ${throwable.message.orEmpty()}"
//        }
//    }
//
//    private fun metadataFlags(): Int {
//        @Suppress("DEPRECATION")
//        return android.content.pm.PackageManager.GET_META_DATA
//    }
//}

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.RestrictionsManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var restrictionsReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)

        // 1. Read the initial configuration when the app starts
        loadManagedConfigurations()

        // 2. Set up a listener for real-time push updates from the UEM console
        restrictionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED) {
                    // This fires the exact moment Workspace ONE pushes the update!
                    loadManagedConfigurations()
                }
            }
        }

        registerReceiver(
            restrictionsReceiver,
            IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        )
    }

    private fun loadManagedConfigurations() {
        val restrictionsManager = getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        val appRestrictions = restrictionsManager.applicationRestrictions

        // Look for the specific key we defined in the XML
        if (appRestrictions.containsKey("apigee_cred")) {
            val apigeeCred = appRestrictions.getString("apigee_cred")
            statusText.text = "Current Apigee Credential: $apigeeCred"
        } else {
            statusText.text = "No Apigee credential pushed yet."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(restrictionsReceiver)
    }
}