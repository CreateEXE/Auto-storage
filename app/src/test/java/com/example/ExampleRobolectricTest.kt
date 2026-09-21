package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.GeminiFileAnalyzer
import com.example.data.local.AppDatabase
import com.example.data.model.FileCategory
import com.example.data.model.FileMetadata
import com.example.data.scanner.FileScannerEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("File Organizer", appName)
  }

  @Test
  fun `test file analysis creates clean suggested name and tags`() {
    val analyzer = GeminiFileAnalyzer()
    val result = analyzer.analyzeWithLocalHeuristics(
      originalName = "Scan_10492_invoice_final_v2.pdf",
      extension = "pdf",
      category = "DOCUMENTS",
      sizeBytes = 184000L,
      createdAt = 1710500000000L,
      contentSnippet = "AWS Invoice payment"
    )

    assertNotNull(result.suggestedName)
    assertTrue(result.suggestedName.endsWith(".pdf"))
    assertTrue(result.tags.isNotEmpty())
    assertTrue(result.tags.contains("Invoice") || result.tags.contains("Finance"))
  }

  @Test
  fun `test room database storage and duplicate queries`() = runBlocking {
    val dao = database.fileDao()

    val file1 = FileMetadata(
      uri = "file:///dummy/receipt1.pdf",
      originalName = "receipt_001.pdf",
      suggestedName = "2024_Receipt_Store.pdf",
      currentName = "receipt_001.pdf",
      extension = "pdf",
      mimeType = "application/pdf",
      category = FileCategory.DOCUMENTS.name,
      sizeBytes = 102400L,
      createdAt = 1000L,
      modifiedAt = 1000L,
      fileHash = "hash123",
      isDuplicate = false,
      tags = "Receipt, Expense",
      extractedSummary = "Store purchase"
    )

    val file2 = FileMetadata(
      uri = "file:///dummy/receipt1_copy.pdf",
      originalName = "receipt_001_copy.pdf",
      suggestedName = "2024_Receipt_Store.pdf",
      currentName = "receipt_001_copy.pdf",
      extension = "pdf",
      mimeType = "application/pdf",
      category = FileCategory.DOCUMENTS.name,
      sizeBytes = 102400L,
      createdAt = 2000L,
      modifiedAt = 2000L,
      fileHash = "hash123",
      isDuplicate = true,
      duplicateGroupId = "hash123_102400",
      tags = "Receipt, Expense",
      extractedSummary = "Store purchase"
    )

    dao.insertFiles(listOf(file1, file2))

    val allFiles = dao.getAllFiles().first()
    assertEquals(2, allFiles.size)

    val duplicates = dao.getDuplicates().first()
    assertEquals(1, duplicates.size)
    assertEquals("receipt_001_copy.pdf", duplicates[0].originalName)

    // Test search by tag
    val searchResult = dao.searchFiles("Expense").first()
    assertEquals(2, searchResult.size)
  }

  @Test
  fun `test file scanner engine creates realistic sample files and finds duplicates`() = runBlocking {
    val engine = FileScannerEngine(context)
    val files = engine.seedSampleFilesIfEmpty()

    assertTrue(files.isNotEmpty())
    // Ensure duplicates were detected
    val duplicates = files.filter { it.isDuplicate }
    assertTrue("Engine should identify duplicates in sample files", duplicates.isNotEmpty())
  }

  @Test
  fun `test custom renaming rule evaluation with tokens and patterns`() {
    val rule = com.example.data.model.RenamingRuleEntity(
      ruleName = "Music Track Number and Title",
      targetCategory = "AUDIO",
      outputFormat = "{artist} - {album} - {track} - {title}",
      replaceSpacesWith = "_"
    )

    val audioFile = FileMetadata(
      uri = "file:///dummy/song.mp3",
      originalName = "track01_final.mp3",
      suggestedName = "track01_final.mp3",
      currentName = "track01_final.mp3",
      extension = "mp3",
      mimeType = "audio/mpeg",
      category = "AUDIO",
      sizeBytes = 8000000L,
      createdAt = 1710500000000L,
      modifiedAt = 1710500000000L,
      fileHash = "audio_hash_01",
      artist = "Linkin Park",
      album = "Meteora",
      trackTitle = "Numb",
      trackNumber = 13,
      genre = "Alternative Rock"
    )

    assertTrue(rule.matches(audioFile))
    val renamed = rule.applyTo(audioFile)
    assertEquals("Linkin_Park_-_Meteora_-_13_-_Numb.mp3", renamed)
  }

  @Test
  fun `test image subtype classification`() {
    val analyzer = GeminiFileAnalyzer()
    val screenshotResult = analyzer.analyzeWithLocalHeuristics(
      originalName = "Screenshot_20240315_142201.png",
      extension = "png",
      category = "IMAGES",
      sizeBytes = 2400000L,
      createdAt = 1710500000000L,
      contentSnippet = null
    )
    assertEquals("SCREENSHOT", screenshotResult.imageSubtype)

    val designResult = analyzer.analyzeWithLocalHeuristics(
      originalName = "mockup_product_dashboard_ui.png",
      extension = "png",
      category = "IMAGES",
      sizeBytes = 1500000L,
      createdAt = 1710500000000L,
      contentSnippet = null
    )
    assertEquals("PRODUCT_DESIGN", designResult.imageSubtype)
  }
}

