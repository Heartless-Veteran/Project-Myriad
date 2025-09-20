package com.heartlessveteran.myriad.data.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.services.TextBound
import com.heartlessveteran.myriad.services.TranslatedTextBound
import com.heartlessveteran.myriad.services.TranslationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OCR Service using Google ML Kit
 * 
 * Provides real OCR text recognition and translation capabilities
 * for manga pages and other images.
 */
@Singleton
class OCRService @Inject constructor() {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val languageIdentifier = LanguageIdentification.getClient()
    private var currentTranslator: Translator? = null
    private var currentLanguagePair: Pair<String, String>? = null
    
    /**
     * Extract text from image using OCR
     * 
     * @param imageBase64 Base64 encoded image
     * @return List of detected text with bounding boxes
     */
    suspend fun extractText(imageBase64: String): Result<List<TextBound>> = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeBase64ToBitmap(imageBase64)
            val image = InputImage.fromBitmap(bitmap, 0)
            
            val visionText = suspendCancellableCoroutine { continuation ->
                textRecognizer.process(image)
                    .addOnSuccessListener { result -> continuation.resume(result) }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }
            
            val textBounds = mutableListOf<TextBound>()
            
            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val boundingBox = line.boundingBox
                    if (boundingBox != null) {
                        textBounds.add(
                            TextBound(
                                text = line.text,
                                x = boundingBox.left.toFloat(),
                                y = boundingBox.top.toFloat(),
                                width = boundingBox.width().toFloat(),
                                height = boundingBox.height().toFloat(),
                                confidence = 0.9f // ML Kit doesn't provide confidence, using default
                            )
                        )
                    }
                }
            }
            
            Result.Success(textBounds)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Detect language of text
     * 
     * @param text Text to analyze
     * @return Language code (e.g., "ja", "en")
     */
    suspend fun detectLanguage(text: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val languageCode = suspendCancellableCoroutine { continuation ->
                languageIdentifier.identifyLanguage(text)
                    .addOnSuccessListener { result -> continuation.resume(result) }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }
            if (languageCode == "und") {
                // Undetermined language, default to Japanese for manga
                Result.Success("ja")
            } else {
                Result.Success(languageCode)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Translate text from source to target language
     * 
     * @param text Text to translate
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code  
     * @return Translated text
     */
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sourceLang = mapLanguageCode(sourceLanguage)
            val targetLang = mapLanguageCode(targetLanguage)
            
            if (sourceLang == null || targetLang == null) {
                return@withContext Result.Error(IllegalArgumentException("Unsupported language"))
            }
            
            val translator = getTranslator(sourceLang, targetLang)
            val translatedText = suspendCancellableCoroutine { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { result -> continuation.resume(result) }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }
            
            Result.Success(translatedText)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Perform complete OCR and translation pipeline
     * 
     * @param imageBase64 Base64 encoded image
     * @param targetLanguage Target language for translation
     * @return Complete translation response with original and translated text
     */
    suspend fun performOCRTranslation(
        imageBase64: String,
        targetLanguage: String = "en"
    ): Result<TranslationResponse> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            
            // Extract text from image
            val textExtractionResult = extractText(imageBase64)
            when (textExtractionResult) {
                is Result.Error -> return@withContext Result.Error(textExtractionResult.exception)
                is Result.Loading -> return@withContext Result.Error(IllegalStateException("Unexpected loading state"))
                is Result.Success -> {
                    val originalTextBounds = textExtractionResult.data
                    if (originalTextBounds.isEmpty()) {
                        return@withContext Result.Success(
                            TranslationResponse(
                                originalText = emptyList(),
                                translatedText = emptyList(),
                                confidence = 0.0f,
                                processingTime = System.currentTimeMillis() - startTime,
                                language = "unknown"
                            )
                        )
                    }
                    
                    // Detect language of first text block
                    val firstText = originalTextBounds.first().text
                    val languageResult = detectLanguage(firstText)
                    val detectedLanguage = when (languageResult) {
                        is Result.Success -> languageResult.data
                        is Result.Error -> "ja"
                        is Result.Loading -> "ja"
                    }
                    
                    // Translate each text block
                    val translatedTextBounds = mutableListOf<TranslatedTextBound>()
                    var totalConfidence = 0f
                    
                    for (textBound in originalTextBounds) {
                        val translationResult = translateText(textBound.text, detectedLanguage, targetLanguage)
                        val translatedText = when (translationResult) {
                            is Result.Success -> translationResult.data
                            is Result.Error -> textBound.text
                            is Result.Loading -> textBound.text
                        }
                        
                        translatedTextBounds.add(
                            TranslatedTextBound(
                                originalText = textBound.text,
                                translatedText = translatedText,
                                x = textBound.x,
                                y = textBound.y,
                                width = textBound.width,
                                height = textBound.height,
                                confidence = textBound.confidence
                            )
                        )
                        totalConfidence += textBound.confidence
                    }
                    
                    val averageConfidence = if (originalTextBounds.isNotEmpty()) {
                        totalConfidence / originalTextBounds.size
                    } else {
                        0f
                    }
                    
                    Result.Success(
                        TranslationResponse(
                            originalText = originalTextBounds,
                            translatedText = translatedTextBounds,
                            confidence = averageConfidence,
                            processingTime = System.currentTimeMillis() - startTime,
                            language = detectedLanguage
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Get or create translator for language pair
     */
    private suspend fun getTranslator(sourceLanguage: String, targetLanguage: String): Translator {
        val languagePair = Pair(sourceLanguage, targetLanguage)
        
        if (currentTranslator == null || currentLanguagePair != languagePair) {
            currentTranslator?.close()
            
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
            
            currentTranslator = Translation.getClient(options)
            currentLanguagePair = languagePair
            
            // Download model if needed
            suspendCancellableCoroutine { continuation ->
                currentTranslator!!.downloadModelIfNeeded()
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }
        }
        
        return currentTranslator!!
    }
    
    /**
     * Map language codes to ML Kit format
     */
    private fun mapLanguageCode(languageCode: String): String? {
        return when (languageCode.lowercase()) {
            "japanese", "ja" -> TranslateLanguage.JAPANESE
            "english", "en" -> TranslateLanguage.ENGLISH
            "korean", "ko" -> TranslateLanguage.KOREAN
            "chinese", "zh", "zh-cn" -> TranslateLanguage.CHINESE
            "spanish", "es" -> TranslateLanguage.SPANISH
            "french", "fr" -> TranslateLanguage.FRENCH
            "german", "de" -> TranslateLanguage.GERMAN
            "italian", "it" -> TranslateLanguage.ITALIAN
            "portuguese", "pt" -> TranslateLanguage.PORTUGUESE
            "russian", "ru" -> TranslateLanguage.RUSSIAN
            else -> null
        }
    }
    
    /**
     * Decode base64 string to bitmap
     */
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        currentTranslator?.close()
        currentTranslator = null
        currentLanguagePair = null
    }
}