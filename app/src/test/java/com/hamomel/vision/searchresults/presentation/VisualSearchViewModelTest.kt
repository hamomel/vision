package com.hamomel.vision.searchresults.presentation

import android.graphics.Bitmap
import com.hamomel.vision.TestCoroutineRule
import com.hamomel.vision.searchresults.data.VisualSearchNetworkDataSource
import com.hamomel.vision.searchresults.data.model.VisualSearchItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisualSearchViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val bitmap = mockk<Bitmap>(relaxed = true)
    private val dataSource = mockk<VisualSearchNetworkDataSource>()
    private val testSearchItem = VisualSearchItem(
        name = "name",
        contentUrl = "https://content.com",
        hostPageUrl = "https://hostpage.com"
    )

    private lateinit var viewModel: VisualSearchViewModel

    @Before
    fun setUp() {
        coEvery { dataSource.search(any()) } returns listOf(testSearchItem)

        viewModel = VisualSearchViewModel(
            image = bitmap,
            dataSource = dataSource
        )
    }

    @Test
    fun `should request data on creation`() = runTest {
        runCurrent()
        coVerify(exactly = 1) { dataSource.search(bitmap) }
    }

    @Test
    fun `should request data when onRetry called`() = runTest {
        viewModel.onRetryClick()

        runCurrent()
        coVerify(exactly = 2) { dataSource.search(bitmap) }
    }

    @Test
    fun `should set Error state if error occured while requesting data`() = runTest {
        coEvery { dataSource.search(any()) } throws IOException()

        viewModel.onRetryClick()
        runCurrent()

        val state = viewModel.viewState.first()

        assertEquals(Error, state)
    }

    @Test
    fun `should not send request and set Error state if passed image is null`() = runTest {
        val dataSource = mockk<VisualSearchNetworkDataSource>()
        val underTest = VisualSearchViewModel(
            image = null,
            dataSource = dataSource
        )
        runCurrent()

        coVerify(exactly = 0) { dataSource.search(any()) }

        val state = underTest.viewState.first()

        assertEquals(Error, state)
    }

    @Test
    fun `should sent OpenResultScreen with items url`() = runTest {

        val eventDeferred = async {
            viewModel.viewEvents.first()
        }
        runCurrent()

        viewModel.onItemClick(testSearchItem)
        runCurrent()

        val event = eventDeferred.await()

        assertTrue(event is OpenResultScreen)
        assertEquals(testSearchItem.hostPageUrl, (event as OpenResultScreen).pageUrl)
    }
}