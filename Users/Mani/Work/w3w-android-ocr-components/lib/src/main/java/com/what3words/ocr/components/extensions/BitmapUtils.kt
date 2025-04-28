package com.what3words.ocr.components.extensions

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.InputStream

class BitmapUtils {
    companion object {
        /**
         * Efficiently loads a bitmap from a Uri with automatic downsampling
         * to prevent OOM errors with large images
         * 
         * @param contentResolver ContentResolver to access the Uri
         * @param uri Uri pointing to the image
         * @return Downsampled bitmap or null if loading failed
         */
        fun loadDownsampledBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
            return try {
                // Get image dimensions first before loading full bitmap
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                
                // Read original dimensions
                val (originalWidth, originalHeight) = getImageDimensions(contentResolver, uri)
                
                // Calculate scale based on original dimensions
                // Target maximum dimension - based on how large the original is
                val maxDimension = when {
                    originalWidth > 3000 || originalHeight > 3000 -> 1280 // ~25% for very large images
                    originalWidth > 1500 || originalHeight > 1500 -> 800  // ~50% for medium images
                    else -> 600  // Keep smaller images at reasonable size
                }
                
                // Calculate target dimensions maintaining aspect ratio
                val (targetWidth, targetHeight) = if (originalWidth > originalHeight) {
                    // Landscape
                    Pair(maxDimension, (originalHeight * maxDimension / originalWidth.toFloat()).toInt())
                } else {
                    // Portrait
                    Pair((originalWidth * maxDimension / originalHeight.toFloat()).toInt(), maxDimension)
                }
                
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                        val source = ImageDecoder.createSource(contentResolver, uri)
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            
                            // Set target size while maintaining aspect ratio
                            decoder.setTargetSize(targetWidth, targetHeight)
                        }
                    }
                    else -> {
                        // For older Android versions
                        @Suppress("DEPRECATION")
                        val sampleSize = calculateSampleSize(originalWidth, originalHeight, targetWidth, targetHeight)
                        
                        val loadOptions = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
                        }
                        
                        @Suppress("DEPRECATION")
                        val inputStream = contentResolver.openInputStream(uri)
                        val result = BitmapFactory.decodeStream(inputStream, null, loadOptions)
                        inputStream?.close()
                        result
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Gets the dimensions of an image without loading the full bitmap into memory
         * 
         * @param contentResolver ContentResolver to access the Uri
         * @param uri Uri pointing to the image
         * @return Pair of width and height
         */
        private fun getImageDimensions(contentResolver: ContentResolver, uri: Uri): Pair<Int, Int> {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                var inputStream: InputStream? = null
                try {
                    inputStream = contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream, null, options)
                } finally {
                    inputStream?.close()
                }
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                // For API 28+, we need to use ImageDecoder to get dimensions
                try {
                    ImageDecoder.decodeDrawable(source) { decoder, info, _ ->
                        options.outWidth = info.size.width
                        options.outHeight = info.size.height
                        // Cancel the decode after getting dimensions
                        decoder.setOnPartialImageListener { _: ImageDecoder.DecodeException -> true }
                    }
                } catch (_: Exception) {
                    // Expected, we canceled the decode
                }
            }
            
            return Pair(options.outWidth, options.outHeight)
        }
        
        /**
         * Calculates the appropriate sample size for downsampling based on target dimensions
         * 
         * @param width Original width
         * @param height Original height
         * @param targetWidth Target width
         * @param targetHeight Target height
         * @return Sample size (power of 2)
         */
        private fun calculateSampleSize(width: Int, height: Int, targetWidth: Int, targetHeight: Int): Int {
            var sampleSize = 1
            
            if (width > targetWidth || height > targetHeight) {
                val halfWidth = width / 2
                val halfHeight = height / 2
                
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width
                while ((halfWidth / sampleSize) >= targetWidth && (halfHeight / sampleSize) >= targetHeight) {
                    sampleSize *= 2
                }
            }
            
            return sampleSize
        }
    }
}