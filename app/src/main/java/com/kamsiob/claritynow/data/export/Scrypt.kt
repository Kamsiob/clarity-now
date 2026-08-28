package com.kamsiob.claritynow.data.export

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * scrypt, RFC 7914, and PBKDF2-HMAC-SHA256, RFC 2898, which scrypt is defined in
 * terms of.
 *
 * **Why this is written out here rather than taken from a library.**
 * MASTER_BUILD_PROMPT 14b.7 asks for a modern key derivation function at its
 * current recommended parameters, and 3.3 says to check what those are rather than
 * to trust a document. Checked on August 27, 2026 against the OWASP Password
 * Storage Cheat Sheet, the order of preference is Argon2id, then scrypt, then
 * PBKDF2, and PBKDF2 is listed for FIPS compliance rather than for strength.
 * Android's platform providers offer PBKDF2 and offer neither of the two above it:
 * there is no scrypt and no Argon2 in any `SecretKeyFactory` Android documents.
 * So the choice was between the third best function, a dependency, or the one
 * piece of the second best function the platform does not already have.
 *
 * This file is that one piece. Everything under it comes from the platform:
 * HMAC-SHA256 is `javax.crypto.Mac`, and the cipher in [BackupCrypto] is the
 * platform's AES-GCM. What is written here is the memory hard mixing that makes
 * scrypt scrypt, about a hundred lines of integer arithmetic with no choices in
 * it, and every intermediate step of it is asserted against the vectors published
 * in RFC 7914 sections 8 through 12. A KDF whose intermediate steps cannot be
 * checked against the vectors published for them is a KDF nobody can check, which
 * is why [salsa20Core8], [blockMix] and [roMix] are public rather than private.
 *
 * **The other half of the reason is the desktop app.** `docs/EVENT_FORMAT.md` is a
 * contract with a Linux companion that does not exist yet, and a backup file is
 * part of that contract the moment somebody tries to open one there. scrypt is in
 * the standard library or the default crypto package of every runtime that app
 * could be written in: Python's `hashlib.scrypt`, Node's `crypto.scryptSync`,
 * Go's `x/crypto/scrypt`, OpenSSL's `EVP_PBE_scrypt`. Argon2 needs a third party
 * package in most of them. The file format is easier to read elsewhere for having
 * chosen the function whose name every runtime already knows.
 *
 * **The password is the HMAC key, so it may not be empty.** RFC 7914's first test
 * vector uses an empty password and is deliberately not asserted anywhere:
 * `SecretKeySpec` refuses a zero length key, and [BackupCrypto] refuses an empty
 * password before it reaches here, so the case cannot arise in this app. The other
 * three scrypt vectors and both PBKDF2 vectors are asserted in full.
 *
 * Nothing here reads a clock, a random number or an Android API, which is what
 * lets the round trip test run on a plain JVM and test the bytes that ship.
 */
object Scrypt {

    /** One Salsa20 block: 16 little endian 32 bit words, which is 64 bytes. */
    const val BLOCK_WORDS: Int = 16

    /**
     * scrypt itself, RFC 7914 section 6.
     *
     * Working memory is `128 * r * n` bytes, allocated once and reused across the
     * [p] passes, so the peak stays at that figure however large [p] is. The
     * parameters are the caller's rather than constants here, because a file
     * carries the parameters it was written with and a later build must be able to
     * raise them without making every earlier backup unreadable. [BackupCrypto]
     * holds the figures this build writes.
     */
    fun derive(
        password: ByteArray,
        salt: ByteArray,
        n: Int,
        r: Int,
        p: Int,
        keyBytes: Int,
    ): ByteArray {
        require(n > 1 && (n and (n - 1)) == 0) { "N has to be a power of two above one" }
        require(r >= 1) { "r has to be at least one" }
        require(p >= 1) { "p has to be at least one" }
        require(keyBytes >= 1) { "the derived key has to be at least one byte" }

        val blockBytes = 128 * r
        val blockWords = blockBytes / 4
        val expanded = pbkdf2HmacSha256(password, salt, iterations = 1, keyBytes = p * blockBytes)

        val x = IntArray(blockWords)
        val v = IntArray(blockWords * n)
        for (i in 0 until p) {
            readWords(expanded, i * blockBytes, x)
            roMix(x, v, n, r)
            writeWords(x, expanded, i * blockBytes)
        }

        val key = pbkdf2HmacSha256(password, expanded, iterations = 1, keyBytes = keyBytes)
        expanded.fill(0)
        return key
    }

    /**
     * PBKDF2-HMAC-SHA256, RFC 2898 section 5.2.
     *
     * Written over `Mac` rather than over `SecretKeyFactory` and `PBEKeySpec`
     * deliberately. A `PBEKeySpec` takes a `char[]` and leaves the conversion to
     * bytes to the provider, and providers do not agree about it: the historical
     * Android implementations kept only the low eight bits of each character.
     * scrypt is defined over octets, and this file format has to be readable by a
     * second implementation, so the encoding of the password is decided in one
     * place, [BackupCrypto.passwordBytes], and what arrives here is already bytes.
     */
    fun pbkdf2HmacSha256(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        keyBytes: Int,
    ): ByteArray {
        require(password.isNotEmpty()) { "the password may not be empty" }
        require(iterations >= 1) { "iterations has to be at least one" }
        require(keyBytes >= 1) { "the derived key has to be at least one byte" }

        val mac = Mac.getInstance(HMAC)
        mac.init(SecretKeySpec(password, HMAC))
        val hashBytes = mac.macLength
        val blocks = (keyBytes + hashBytes - 1) / hashBytes
        val out = ByteArray(blocks * hashBytes)
        val u = ByteArray(hashBytes)
        val t = ByteArray(hashBytes)

        for (block in 1..blocks) {
            mac.update(salt)
            mac.update(
                byteArrayOf(
                    (block ushr 24).toByte(),
                    (block ushr 16).toByte(),
                    (block ushr 8).toByte(),
                    block.toByte(),
                ),
            )
            mac.doFinal(u, 0)
            u.copyInto(t)
            repeat(iterations - 1) {
                mac.update(u)
                mac.doFinal(u, 0)
                for (i in 0 until hashBytes) {
                    t[i] = (t[i].toInt() xor u[i].toInt()).toByte()
                }
            }
            t.copyInto(out, (block - 1) * hashBytes)
        }

        val key = out.copyOf(keyBytes)
        out.fill(0)
        u.fill(0)
        t.fill(0)
        return key
    }

    /**
     * scryptROMix, RFC 7914 section 5. [x] is `32 * r` words in and out, and [v] is
     * the `32 * r * n` word scratch the whole memory hardness comes from.
     */
    fun roMix(x: IntArray, v: IntArray, n: Int, r: Int) {
        val words = 32 * r
        val y = IntArray(words)
        val t = IntArray(words)
        val scratch = IntArray(BLOCK_WORDS)

        for (i in 0 until n) {
            x.copyInto(v, i * words, 0, words)
            blockMix(x, y, scratch, r)
            y.copyInto(x, 0, 0, words)
        }
        repeat(n) {
            val j = integerify(x, r) and (n - 1)
            for (k in 0 until words) t[k] = x[k] xor v[j * words + k]
            blockMix(t, y, scratch, r)
            y.copyInto(x, 0, 0, words)
        }
    }

    /**
     * scryptBlockMix, RFC 7914 section 4. Reads `2 * r` blocks from [input] and
     * writes the shuffled `2 * r` blocks to [output], which must not be [input].
     */
    fun blockMix(input: IntArray, output: IntArray, scratch: IntArray, r: Int) {
        input.copyInto(scratch, 0, (2 * r - 1) * BLOCK_WORDS, 2 * r * BLOCK_WORDS)
        for (i in 0 until 2 * r) {
            val offset = i * BLOCK_WORDS
            for (k in 0 until BLOCK_WORDS) scratch[k] = scratch[k] xor input[offset + k]
            salsa20Core8(scratch)
            val target = if (i % 2 == 0) i / 2 else r + i / 2
            scratch.copyInto(output, target * BLOCK_WORDS)
        }
    }

    /** The allocating form, so the RFC 7914 section 9 vector can be asserted directly. */
    fun blockMix(input: IntArray, r: Int): IntArray {
        val output = IntArray(input.size)
        blockMix(input, output, IntArray(BLOCK_WORDS), r)
        return output
    }

    /**
     * The Salsa20/8 Core, in place, feed forward included. RFC 7914 section 3.
     *
     * Eight rounds, which is four double rounds of a column round and a row round.
     * This is Salsa20's core with a reduced round count and is not a cipher on its
     * own; scrypt uses it as a fast, memory friendly mixing function.
     */
    fun salsa20Core8(x: IntArray) {
        require(x.size >= BLOCK_WORDS) { "the Salsa20 core takes 16 words" }
        val start = x.copyOf(BLOCK_WORDS)
        var round = 0
        while (round < 8) {
            x[4] = x[4] xor rotateLeft(x[0] + x[12], 7)
            x[8] = x[8] xor rotateLeft(x[4] + x[0], 9)
            x[12] = x[12] xor rotateLeft(x[8] + x[4], 13)
            x[0] = x[0] xor rotateLeft(x[12] + x[8], 18)
            x[9] = x[9] xor rotateLeft(x[5] + x[1], 7)
            x[13] = x[13] xor rotateLeft(x[9] + x[5], 9)
            x[1] = x[1] xor rotateLeft(x[13] + x[9], 13)
            x[5] = x[5] xor rotateLeft(x[1] + x[13], 18)
            x[14] = x[14] xor rotateLeft(x[10] + x[6], 7)
            x[2] = x[2] xor rotateLeft(x[14] + x[10], 9)
            x[6] = x[6] xor rotateLeft(x[2] + x[14], 13)
            x[10] = x[10] xor rotateLeft(x[6] + x[2], 18)
            x[3] = x[3] xor rotateLeft(x[15] + x[11], 7)
            x[7] = x[7] xor rotateLeft(x[3] + x[15], 9)
            x[11] = x[11] xor rotateLeft(x[7] + x[3], 13)
            x[15] = x[15] xor rotateLeft(x[11] + x[7], 18)

            x[1] = x[1] xor rotateLeft(x[0] + x[3], 7)
            x[2] = x[2] xor rotateLeft(x[1] + x[0], 9)
            x[3] = x[3] xor rotateLeft(x[2] + x[1], 13)
            x[0] = x[0] xor rotateLeft(x[3] + x[2], 18)
            x[6] = x[6] xor rotateLeft(x[5] + x[4], 7)
            x[7] = x[7] xor rotateLeft(x[6] + x[5], 9)
            x[4] = x[4] xor rotateLeft(x[7] + x[6], 13)
            x[5] = x[5] xor rotateLeft(x[4] + x[7], 18)
            x[11] = x[11] xor rotateLeft(x[10] + x[9], 7)
            x[8] = x[8] xor rotateLeft(x[11] + x[10], 9)
            x[9] = x[9] xor rotateLeft(x[8] + x[11], 13)
            x[10] = x[10] xor rotateLeft(x[9] + x[8], 18)
            x[12] = x[12] xor rotateLeft(x[15] + x[14], 7)
            x[13] = x[13] xor rotateLeft(x[12] + x[15], 9)
            x[14] = x[14] xor rotateLeft(x[13] + x[12], 13)
            x[15] = x[15] xor rotateLeft(x[14] + x[13], 18)
            round += 2
        }
        for (k in 0 until BLOCK_WORDS) x[k] = x[k] + start[k]
    }

    /**
     * Integerify, RFC 7914 section 5: the last 64 byte block read as a little
     * endian integer. Only the low word is needed, because the caller reduces the
     * result modulo a power of two no larger than 2^30.
     */
    fun integerify(x: IntArray, r: Int): Int = x[(2 * r - 1) * BLOCK_WORDS]

    /** Little endian, which is the byte order every block in RFC 7914 is read in. */
    fun readWords(src: ByteArray, offset: Int, dest: IntArray) {
        for (i in dest.indices) {
            val at = offset + i * 4
            dest[i] = (src[at].toInt() and 0xFF) or
                ((src[at + 1].toInt() and 0xFF) shl 8) or
                ((src[at + 2].toInt() and 0xFF) shl 16) or
                ((src[at + 3].toInt() and 0xFF) shl 24)
        }
    }

    fun writeWords(src: IntArray, dest: ByteArray, offset: Int) {
        for (i in src.indices) {
            val at = offset + i * 4
            val word = src[i]
            dest[at] = word.toByte()
            dest[at + 1] = (word ushr 8).toByte()
            dest[at + 2] = (word ushr 16).toByte()
            dest[at + 3] = (word ushr 24).toByte()
        }
    }

    private fun rotateLeft(value: Int, bits: Int): Int = (value shl bits) or (value ushr (32 - bits))

    private const val HMAC = "HmacSHA256"
}
