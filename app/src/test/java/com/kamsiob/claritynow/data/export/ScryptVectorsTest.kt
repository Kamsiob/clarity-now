package com.kamsiob.claritynow.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The key derivation function, checked against the vectors published for it rather
 * than against itself. RFC 7914 sections 8 through 12, and RFC 2898 by way of
 * section 11.
 *
 * **This is the test that makes writing scrypt out in [Scrypt] a defensible thing
 * to have done.** A KDF is deterministic and has published intermediate values at
 * every stage, so an implementation of one is either exactly right or visibly
 * wrong, and there is no third state for a bug to hide in. Each step is asserted
 * separately, so a failure names the stage rather than saying only that the answer
 * came out different.
 *
 * RFC 7914's first scrypt vector, with an empty password, is deliberately absent.
 * `SecretKeySpec` refuses a zero length HMAC key and [BackupCrypto] refuses an
 * empty password, so the case cannot occur in this app and asserting it would mean
 * widening the code to make a test pass.
 */
class ScryptVectorsTest {

    @Test
    fun `the Salsa20 8 core matches RFC 7914 section 8`() {
        val block = words(SALSA_INPUT)
        Scrypt.salsa20Core8(block)
        assertEquals(SALSA_OUTPUT, hex(block))
    }

    @Test
    fun `scryptBlockMix matches RFC 7914 section 9`() {
        val mixed = Scrypt.blockMix(words(BLOCK_MIX_INPUT), r = 1)
        assertEquals(BLOCK_MIX_OUTPUT, hex(mixed))
    }

    @Test
    fun `scryptROMix matches RFC 7914 section 10`() {
        val x = words(BLOCK_MIX_INPUT)
        Scrypt.roMix(x, IntArray(32 * 16), n = 16, r = 1)
        assertEquals(RO_MIX_OUTPUT, hex(x))
    }

    @Test
    fun `PBKDF2 with HMAC SHA256 matches RFC 7914 section 11`() {
        assertEquals(
            PBKDF2_ONE,
            hex(
                Scrypt.pbkdf2HmacSha256(
                    password = "passwd".toByteArray(Charsets.US_ASCII),
                    salt = "salt".toByteArray(Charsets.US_ASCII),
                    iterations = 1,
                    keyBytes = 64,
                ),
            ),
        )
        assertEquals(
            PBKDF2_TWO,
            hex(
                Scrypt.pbkdf2HmacSha256(
                    password = "Password".toByteArray(Charsets.US_ASCII),
                    salt = "NaCl".toByteArray(Charsets.US_ASCII),
                    iterations = 80_000,
                    keyBytes = 64,
                ),
            ),
        )
    }

    @Test
    fun `scrypt matches RFC 7914 section 12`() {
        assertEquals(
            SCRYPT_TWO,
            hex(
                Scrypt.derive(
                    password = "password".toByteArray(Charsets.US_ASCII),
                    salt = "NaCl".toByteArray(Charsets.US_ASCII),
                    n = 1024,
                    r = 8,
                    p = 16,
                    keyBytes = 64,
                ),
            ),
        )
        assertEquals(
            SCRYPT_THREE,
            hex(
                Scrypt.derive(
                    password = "pleaseletmein".toByteArray(Charsets.US_ASCII),
                    salt = "SodiumChloride".toByteArray(Charsets.US_ASCII),
                    n = 16384,
                    r = 8,
                    p = 1,
                    keyBytes = 64,
                ),
            ),
        )
    }

    /**
     * The parameters this build writes, held to what was verified.
     *
     * MASTER_BUILD_PROMPT 3.3 says a KDF's recommended parameters move on their own
     * schedule and do not announce that they have moved. This cannot check the
     * internet, so it checks the next best thing: that the shipped figures are still
     * one of the rows the reference listed when they were last looked up, and that
     * nobody has quietly lowered one. A row that is no longer recommended is a
     * conversation, and this is what makes somebody have it.
     */
    @Test
    fun `the shipped parameters are one of the verified rows`() {
        val rows = listOf(
            Triple(131072, 8, 1),
            Triple(65536, 8, 2),
            Triple(32768, 8, 3),
            Triple(16384, 8, 5),
            Triple(8192, 8, 10),
        )
        val shipped = Triple(BackupCrypto.SCRYPT_N, BackupCrypto.SCRYPT_R, BackupCrypto.SCRYPT_P)
        assertTrue(
            "the shipped scrypt parameters $shipped are not one of the rows verified on " +
                "August 27, 2026 against the OWASP Password Storage Cheat Sheet. If the " +
                "reference has moved, move this list with it and say so in BackupCrypto",
            shipped in rows,
        )
        assertEquals("AES-256 or it is not what the header says it is", 32, BackupCrypto.KEY_BYTES)
        assertEquals("96 bit nonce, which is what AES-GCM is specified for", 12, BackupCrypto.NONCE_BYTES)
        assertEquals("a full length tag", 128, BackupCrypto.TAG_BITS)
        assertTrue("a salt shorter than 16 bytes is not a salt", BackupCrypto.SALT_BYTES >= 16)
    }

    private fun words(hex: String): IntArray {
        val bytes = bytes(hex)
        val out = IntArray(bytes.size / 4)
        Scrypt.readWords(bytes, 0, out)
        return out
    }

    private fun hex(words: IntArray): String {
        val bytes = ByteArray(words.size * 4)
        Scrypt.writeWords(words, bytes, 0)
        return hex(bytes)
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") {
        val value = it.toInt() and 0xFF
        "0123456789abcdef"[value ushr 4].toString() + "0123456789abcdef"[value and 0x0F]
    }

    private fun bytes(hex: String): ByteArray {
        val clean = hex.filterNot { it.isWhitespace() }
        return ByteArray(clean.length / 2) {
            clean.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }

    private companion object {

        const val SALSA_INPUT =
            "7e879a214f3ec9867ca940e641718f26" +
                "baee555b8c61c1b50df846116dcd3b1d" +
                "ee24f319df9b3d8514121e4b5ac5aa32" +
                "76021d2909c74829edebc68db8b8c25e"

        const val SALSA_OUTPUT =
            "a41f859c6608cc993b81cacb020cef05" +
                "044b2181a2fd337dfd7b1c6396682f29" +
                "b4393168e3c9e6bcfe6bc5b7a06d96ba" +
                "e424cc102c91745c24ad673dc7618f81"

        const val BLOCK_MIX_INPUT =
            "f7ce0b653d2d72a4108cf5abe912ffdd" +
                "777616dbbb27a70e8204f3ae2d0f6fad" +
                "89f68f4811d1e87bcc3bd7400a9ffd29" +
                "094f0184639574f39ae5a1315217bcd7" +
                "894991447213bb226c25b54da86370fb" +
                "cd984380374666bb8ffcb5bf40c254b0" +
                "67d27c51ce4ad5fed829c90b505a571b" +
                "7f4d1cad6a523cda770e67bceaaf7e89"

        const val BLOCK_MIX_OUTPUT =
            "a41f859c6608cc993b81cacb020cef05" +
                "044b2181a2fd337dfd7b1c6396682f29" +
                "b4393168e3c9e6bcfe6bc5b7a06d96ba" +
                "e424cc102c91745c24ad673dc7618f81" +
                "20edc975323881a80540f64c162dcd3c" +
                "21077cfe5f8d5fe2b1a4168f953678b7" +
                "7d3b3d803b60e4ab920996e59b4d53b6" +
                "5d2a225877d5edf5842cb9f14eefe425"

        const val RO_MIX_OUTPUT =
            "79ccc193629debca047f0b70604bf6b6" +
                "2ce3dd4a9626e355fafc6198e6ea2b46" +
                "d58413673b99b029d665c357601fb426" +
                "a0b2f4bba200ee9f0a43d19b571a9c71" +
                "ef1142e65d5a266fddca832ce59faa7c" +
                "ac0b9cf1be2bffca300d01ee387619c4" +
                "ae12fd4438f203a0e4e1c47ec314861f" +
                "4e9087cb33396a6873e8f9d2539a4b8e"

        const val PBKDF2_ONE =
            "55ac046e56e3089fec1691c22544b605" +
                "f94185216dde0465e68b9d57c20dacbc" +
                "49ca9cccf179b645991664b39d77ef31" +
                "7c71b845b1e30bd509112041d3a19783"

        const val PBKDF2_TWO =
            "4ddcd8f60b98be21830cee5ef22701f9" +
                "641a4418d04c0414aeff08876b34ab56" +
                "a1d425a1225833549adb841b51c9b317" +
                "6a272bdebba1d078478f62b397f33c8d"

        const val SCRYPT_TWO =
            "fdbabe1c9d3472007856e7190d01e9fe" +
                "7c6ad7cbc8237830e77376634b373162" +
                "2eaf30d92e22a3886ff109279d9830da" +
                "c727afb94a83ee6d8360cbdfa2cc0640"

        const val SCRYPT_THREE =
            "7023bdcb3afd7348461c06cd81fd38eb" +
                "fda8fbba904f8e3ea9b543f6545da1f2" +
                "d5432955613f0fcf62d49705242a9af9" +
                "e61e85dc0d651e40dfcf017b45575887"
    }
}
