package com.heartlessveteran.myriad.data.services

import android.content.Context
import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File

/**
 * Unit tests for FileManagerServiceImpl
 */
class FileManagerServiceImplTest {
    @Test
    fun `isSupportedMangaFile returns true for cbz files`() {
        val service = FileManagerServiceImpl(mock(Context::class.java))

        assertTrue(service.isSupportedMangaFile("test.cbz"))
        assertTrue(service.isSupportedMangaFile("test.CBZ"))
        assertTrue(service.isSupportedMangaFile("/path/to/manga.cbz"))
    }

    @Test
    fun `isSupportedMangaFile returns true for cbr files`() {
        val service = FileManagerServiceImpl(mock(Context::class.java))

        assertTrue(service.isSupportedMangaFile("test.cbr"))
        assertTrue(service.isSupportedMangaFile("test.CBR"))
        assertTrue(service.isSupportedMangaFile("/path/to/manga.cbr"))
    }

    @Test
    fun `isSupportedMangaFile returns true for zip files`() {
        val service = FileManagerServiceImpl(mock(Context::class.java))

        assertTrue(service.isSupportedMangaFile("test.zip"))
        assertTrue(service.isSupportedMangaFile("test.ZIP"))
    }

    @Test
    fun `isSupportedMangaFile returns false for unsupported files`() {
        val service = FileManagerServiceImpl(mock(Context::class.java))

        assertFalse(service.isSupportedMangaFile("test.pdf"))
        assertFalse(service.isSupportedMangaFile("test.txt"))
        assertFalse(service.isSupportedMangaFile("test.jpg"))
        assertFalse(service.isSupportedMangaFile("test"))
    }

    @Test
    fun `importMangaFromFile returns error for non-existent file`() =
        runBlocking {
            val context = mock(Context::class.java)
            val cacheDir = mock(File::class.java)
            `when`(context.cacheDir).thenReturn(cacheDir)
            `when`(cacheDir.exists()).thenReturn(true)

            val service = FileManagerServiceImpl(context)
            val result = service.importMangaFromFile("/non/existent/file.cbz")

            assertTrue(result is Result.Error)
            val error = result as Result.Error
            assertTrue(error.exception is IllegalArgumentException)
        }

    @Test
    fun `importMangaFromFile returns error for unsupported format`() =
        runBlocking {
            val context = mock(Context::class.java)
            val cacheDir = mock(File::class.java)
            `when`(context.cacheDir).thenReturn(cacheDir)
            `when`(cacheDir.exists()).thenReturn(true)

            val service = FileManagerServiceImpl(context)
            val result = service.importMangaFromFile("/path/to/file.pdf")

            assertTrue(result is Result.Error)
            val error = result as Result.Error
            assertTrue(error.exception is IllegalArgumentException)
        }
}
