package com.example.deviceadmin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.deviceadmin/info"
    private val DEVICE_ADMIN_CHANNEL = "com.example.deviceadmin/device_admin"
    private val DEVICE_ADMIN_REQUEST_CODE = 1

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Info Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "getSerialNumber" -> result.success(getDeviceSerialNumber())
                    "getMacAddress" -> result.success(getMacAddress())
                    "isDeviceOwner" -> result.success(checkDeviceOwner())
                    "isDeviceAdmin" -> result.success(checkDeviceAdminStatus())
                    else -> result.notImplemented()
                }
            }

        // Device Admin Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, DEVICE_ADMIN_CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "requestDeviceAdmin" -> {
                        val isRequested = requestDeviceAdminPermission()
                        result.success(isRequested)
                    }
                    else -> result.notImplemented()
                }
            }
    }

    private fun getDeviceSerialNumber(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // ดึง Serial Number ด้วย Build.getSerial()
                Build.getSerial() ?: "Unknown"
            } else {
                // ดึง Serial Number ด้วย Build.SERIAL
                Build.SERIAL ?: "Unknown"
            }
        } catch (e: SecurityException) {
            "Permission denied for retrieving Serial Number"
        } catch (e: Exception) {
            "Error retrieving Serial Number"
        }
    }

    private fun getMacAddress(): String {
        return try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
    
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && checkDeviceAdminStatus()) {
                // ใช้ DevicePolicyManager เพื่อดึง MAC Address
                dpm.getWifiMacAddress(adminComponent)?.uppercase() ?: "Unknown"
            } else {
                // ใช้ WifiManager เป็นตัวเลือกสำรอง
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiManager.connectionInfo.macAddress?.uppercase() ?: "Unknown"
            }
        } catch (e: SecurityException) {
            "Permission denied for retrieving MAC address"
        } catch (e: Exception) {
            "Error retrieving MAC address"
        }
    }

    private fun checkDeviceOwner(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.isDeviceOwnerApp(packageName)
        } else {
            false
        }
    }

    private fun requestDeviceAdminPermission(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        return if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "This app needs device admin permissions to retrieve device information"
            )
            startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE)
            true
        } else {
            true
        }
    }

    private fun checkDeviceAdminStatus(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DEVICE_ADMIN_REQUEST_CODE) {
            val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

            val isAdminActive = devicePolicyManager.isAdminActive(adminComponent)
            Toast.makeText(
                this,
                "Device Admin Status: ${if (isAdminActive) "Enabled" else "Disabled"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}