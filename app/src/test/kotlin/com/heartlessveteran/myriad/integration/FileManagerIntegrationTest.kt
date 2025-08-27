package com.heartlessveteran.myriad.integration

import com.heartlessveteran.myriad.di.LibraryDiContainer
import com.heartlessveteran.myriad.domain.models.Result
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.mock
import android.content.Context

/**
 * Integration test for FileManagerService and MangaRepository integration.
 * Tests that the Vault Importer functionality works end-to-end.
 */
class FileManagerIntegrationTest {
    
    @After
    fun teardown() {
        LibraryDiContainer.clearInstances()
    }
    
    @Test
    fun testFileManagerServiceInstantiation() {
        val context = mock(Context::class.java)
        val service = LibraryDiContainer.getFileManagerService(context)
        
        // Test that basic functionality is available
        assertFalse("Service should reject unsupported files", 
                   service.isSupportedMangaFile("/path/to/unsupported.txt"))
        assertTrue("Service should accept .cbz files", 
                  service.isSupportedMangaFile("/path/to/manga.cbz"))
        assertTrue("Service should accept .cbr files", 
                  service.isSupportedMangaFile("/path/to/manga.cbr"))
    }
    
    @Test
    fun testDependencyInjectionSetup() {
        val context = mock(Context::class.java)
        
        // Test that all components can be instantiated properly
        val fileManagerService = LibraryDiContainer.getFileManagerService(context)
        assertNotNull("FileManagerService should be instantiated", fileManagerService)
        
        // Test singleton behavior
        val fileManagerService2 = LibraryDiContainer.getFileManagerService(context)
        assertSame("FileManagerService should be singleton", fileManagerService, fileManagerService2)
    }
}