package com.tcm.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUtils {
    
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun createDataUrl(base64String: String, mimeType: String = "image/jpeg"): String {
        return "data:$mimeType;base64,$base64String"
    }

    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val scaleFactor = if (width > height) {
            maxWidth.toFloat() / width
        } else {
            maxHeight.toFloat() / height
        }
        
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun getRotationDegrees(context: Context, uri: Uri): Float {
        // Simplified - in production, use ExifInterface to get actual rotation
        return 0f
    }

    fun preprocessImage(context: Context, uri: Uri): String? {
        val bitmap = uriToBitmap(context, uri) ?: return null
        val resizedBitmap = resizeBitmap(bitmap)
        val base64String = bitmapToBase64(resizedBitmap)
        return createDataUrl(base64String)
    }

    fun preprocessImage(bitmap: Bitmap): String {
        val resizedBitmap = resizeBitmap(bitmap)
        val base64String = bitmapToBase64(resizedBitmap)
        return createDataUrl(base64String)
    }
}
