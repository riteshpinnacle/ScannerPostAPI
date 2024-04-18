package com.example.piracycheckpostapi_1

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import com.example.piracycheckpostapi_1.databinding.ActivityBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        iniBc()
    }

    private fun iniBc() {
        try {
            barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.CODE_128)
                .build()
            cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error initializing barcode scanner", Toast.LENGTH_SHORT).show()
            return
        }

        binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(binding.surfaceView.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Error starting camera", Toast.LENGTH_SHORT).show()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                try {
                    cameraSource.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Error stopping camera", Toast.LENGTH_SHORT).show()
                }
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "barcode scanner has been stopped", Toast.LENGTH_LONG).show()
            }

            // Inside the `receiveDetections` method
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    val barcode = barcodes.valueAt(0)
                    val barcodeValue = barcode.displayValue
                    runOnUiThread {
                        binding.txtBarcodeValue.text = barcodeValue // Update UI with barcode value
                    }
                    // Send barcodeValue to API
                    sendBarcodeToAPI(barcodeValue)
                }
            }
        })
    }

    private fun sendBarcodeToAPI(barcodeValue: String) {
        // Assuming you have an API endpoint to send the barcode value
        val apiUrl = "https://nodei.ssccglpinnacle.com/searchBarr1"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                // Set your request body if needed
                // val requestBody = "{\"barcode\": \"$barcodeValue\"}"
                // connection.outputStream.write(requestBody.toByteArray())
                val responseCode = connection.responseCode
                // Inside the `sendBarcodeToAPI` method
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    // Update UI with API response
                    runOnUiThread {
                        binding.txtApiResponse.text = responseBody
                    }
                } else {
                    // Show toast message on API call failure
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to call API. Response code: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMessage = "Error sending barcode to API: ${e.message}"
                println(errorMessage)
                // Show toast message on error
                runOnUiThread {
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()

                }
        }
    }
}}