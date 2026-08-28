package com.kamsiob.claritynow.tile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The quick settings tile's manifest declaration. MASTER_BUILD_PROMPT 13.5, issue #41.
 *
 * **Read from the manifest because a tile that is declared wrong is a tile that never
 * appears**, and nothing else in the build would say so: the app compiles, installs and
 * runs, and the only symptom is an empty space in a list of tiles the person is
 * scrolling through in the shade. A unit test cannot bind a tile service, so what it
 * checks is the three attributes without which System UI never tries.
 *
 * It also holds the permission line. Issue #41 requires the tile to add no permission,
 * and `android:permission` on a service is a permission required **of** the caller, so
 * the one place that could go wrong is somebody adding a `uses-permission` beside it
 * out of a reasonable misreading. The merged manifest is checked for network
 * permissions by `verifyNoInternetPermission`; this counts the rest.
 */
class FocusTileContractTest {

    private val manifest = File("src/main/AndroidManifest.xml")

    /**
     * Every permission the source manifest asks for, MASTER_BUILD_PROMPT 18 and
     * `docs/BUILD_STATE.md`. WorkManager contributes three more at merge time, which is
     * recorded rather than checked here, because they arrive from a library manifest
     * this file cannot see.
     */
    private val expectedPermissions = listOf(
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.POST_PROMOTED_NOTIFICATIONS",
        "android.permission.VIBRATE",
    )

    @Test
    fun `the manifest is where this test looks`() {
        assertTrue(
            "unit tests are expected to run from the app module directory, and this run " +
                "is in ${File("").absolutePath}",
            File("build.gradle.kts").isFile,
        )
        assertTrue("missing ${manifest.path}", manifest.isFile)
    }

    @Test
    fun `the tile is declared the one way the system can bind it`() {
        val service = services().single { it.getAttribute("android:name") == SERVICE }
        assertEquals(
            "a tile service is bound by System UI and has to be exported to be bound",
            "true",
            service.getAttribute("android:exported"),
        )
        assertEquals(
            "BIND_QUICK_SETTINGS_TILE is what makes System UI the only thing that can " +
                "bind it, and the platform refuses to show a tile without it",
            "android.permission.BIND_QUICK_SETTINGS_TILE",
            service.getAttribute("android:permission"),
        )
        val actions = service.getElementsByTagName("action")
        assertEquals(
            "the tile answers one action and nothing else",
            listOf("android.service.quicksettings.action.QS_TILE"),
            (0 until actions.length).map {
                (actions.item(it) as Element).getAttribute("android:name")
            },
        )
    }

    @Test
    fun `the tile added no permission`() {
        val nodes = document().getElementsByTagName("uses-permission")
        val declared = (0 until nodes.length)
            .map { (nodes.item(it) as Element) }
            // ACCESS_NETWORK_STATE appears here only to be removed from the merge, and
            // a removal is the opposite of a request.
            .filter { it.getAttribute("tools:node") != "remove" }
            .map { it.getAttribute("android:name") }
        assertEquals(
            "MASTER_BUILD_PROMPT 18 allows notifications permissions, design-v3.md 9 " +
                "requires VIBRATE, and phase 12 was to add nothing. A new name here is " +
                "either a mistake or a decision the owner has to make",
            expectedPermissions,
            declared,
        )
    }

    private fun services(): List<Element> {
        val nodes = document().getElementsByTagName("service")
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private fun document() =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifest)

    private companion object {
        const val SERVICE = ".tile.FocusTileService"
    }
}
