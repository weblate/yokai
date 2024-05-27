package eu.kanade.tachiyomi.data.updater

import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.network.NetworkHelper
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppUpdateCheckerTest {
    private lateinit var appUpdateChecker: AppUpdateChecker

    @BeforeEach
    fun setup() {
        val json = mockk<Json>()
        val network = mockk<NetworkHelper>()
        val preferences = mockk<PreferencesHelper>()

        appUpdateChecker = AppUpdateChecker(json, network, preferences)
    }

    @Test
    fun `Check new nightly version (Tachi format)`() {
        assertTrue(isNewVersion("1.2.3-r2", "1.2.3-r1", true))  // tachi format
        assertTrue(isNewVersion("1.2.4-r1", "1.2.3", true))  // Unlikely to happened, but we should try anyway
    }

    @Test
    fun `Check new nightly version (Yokai format)`() {
        assertTrue(isNewVersion("r2", "1.2.3-r1", true))  // yokai format
    }

    @Test
    fun `Nightly shouldn't get Prod build`() {
        assertFalse(isNewVersion("1.2.3", "1.2.3-r2", true))
        assertFalse(isNewVersion("1.2.4", "1.2.3-r2", true))
        assertFalse(isNewVersion("1.2.4", "1.2.3", true))
        assertFalse(isNewVersion("1.2.4.1", "1.2.4-r2", true))
    }

    @Test
    fun `Beta should get Prod build`() {
        assertTrue(isNewVersion("1.2.4", "1.2.3-r2", false))
        assertTrue(isNewVersion("1.2.3", "1.2.3-r2", false))
    }

    @Test
    fun `Prod should get latest Prod build`() {
        assertTrue(isNewVersion("1.2.4", "1.2.3", false))
        assertTrue(isNewVersion("1.2.4.1", "1.2.4", false))
    }

    @Test
    fun `Prod should get latest Prod build (Check for Betas)`() {
        assertTrue(isNewVersion("1.2.4-r1", "1.2.3", false))
    }

    @Test
    fun `Prod shouldn't get nightly build (Check for Betas)`() {
        assertFalse(isNewVersion("r1", "1.2.3", false))
    }

    @Test
    fun `Latest version check`() {
        assertFalse(isNewVersion("1.2.3", "1.2.3", false))
        assertFalse(isNewVersion("1.2.3-r1", "1.2.3-r1", false))

        assertFalse(isNewVersion("1.2.3-r1", "1.2.3-r1", true))
        assertFalse(isNewVersion("r1", "1.2.4-r1", true))
    }

    private fun isNewVersion(newVersion: String, currentVersion: String, isNightly: Boolean): Boolean {
        return appUpdateChecker.isNewVersion(newVersion, currentVersion, isNightly)
    }
}
