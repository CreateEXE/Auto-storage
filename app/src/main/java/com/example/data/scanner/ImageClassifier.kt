package com.example.data.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val modelPath = "image_classifier.tflite"
    private val labels = listOf("LANDSCAPE", "DOCUMENT", "PORTRAIT", "OTHER")
    private val inputImageSize = 224

    init {
        try {
            val model = loadModelFile(context, modelPath)
            interpreter = Interpreter(model)
            Log.d("ImageClassifier", "TFLite core model (standalone) loaded successfully.")
        } catch (e: Exception) {
            Log.w("ImageClassifier", "TFLite model not found or failed to load. Heuristic fallback will be used.")
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Classifies an image into subtypes: LANDSCAPE, DOCUMENT, PORTRAIT.
     * Uses TFLite if model is available, otherwise falls back to aspect ratio heuristics.
     */
    fun classifyImage(uriString: String): String {
        val uri = Uri.parse(uriString)
        
        return try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it)
            } ?: return "OTHER"

            if (interpreter != null) {
                runTfLiteInference(bitmap)
            } else {
                runHeuristicInference(bitmap)
            }
        } catch (e: Exception) {
            Log.e("ImageClassifier", "Classification error: ${e.message}")
            "OTHER"
        }
    }

    private fun runTfLiteInference(bitmap: Bitmap): String {
        val interpreter = this.interpreter ?: return "OTHER"
        
        // Resize bitmap to model input size
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)
        
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputImageSize * inputImageSize * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        
        val intValues = IntArray(inputImageSize * inputImageSize)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)
        
        for (pixelValue in intValues) {
            inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        val outputBuffer = ByteBuffer.allocateDirect(1 * labels.size * 4).order(ByteOrder.nativeOrder())
        
        try {
            interpreter.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            Log.e("ImageClassifier", "Inference failed: ${e.message}")
            return runHeuristicInference(bitmap)
        }
        
        outputBuffer.rewind()
        val results = FloatArray(labels.size)
        outputBuffer.asFloatBuffer().get(results)

        val maxIndex = results.indices.maxByOrNull { results[it] } ?: -1
        return if (maxIndex != -1 && results[maxIndex] > 0.5f) {
            labels[maxIndex]
        } else {
            runHeuristicInference(bitmap)
        }
    }

    private fun runHeuristicInference(bitmap: Bitmap): String {
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()

        return when {
            // Very tall/narrow usually implies documents or lists
            ratio < 0.6f -> "DOCUMENT"
            // Portrait aspect ratio
            ratio < 0.9f -> "PORTRAIT"
            // Landscape aspect ratio
            ratio > 1.2f -> "LANDSCAPE"
            else -> "OTHER"
        }
    }
}
