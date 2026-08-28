package com.kamsiob.claritynow.data.export

import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.text.Normalizer
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * How a password becomes a key, and how the body of a backup is sealed with it.
 * MASTER_BUILD_PROMPT 14b.7, Addendum 01 4h.
 *
 * **The parameters, and how they were arrived at.** MASTER_BUILD_PROMPT 3.3 says to
 * verify a KDF's recommended parameters at build time rather than to trust a number
 * written in a document, so they were checked on August 27, 2026 against the OWASP
 * Password Storage Cheat Sheet, which is the reference that moves when the hardware
 * moves. What it says today:
 *
 * - Argon2id first, at m=19456 KiB, t=2, p=1 as the smallest acceptable setting
 * - scrypt when Argon2id is not available, at N=2^17, r=8, p=1, with N=2^16 r=8 p=2,
 *   N=2^15 r=8 p=3, N=2^14 r=8 p=5 and N=2^13 r=8 p=10 listed as equivalents
 * - PBKDF2 for FIPS compliance, at 600,000 iterations of HMAC-SHA256
 *
 * Argon2id is not reachable: no Android provider offers it and no provider offers
 * scrypt either, so the first two both mean a dependency or an implementation.
 * [Scrypt] records why the second one was written out here instead of taking the
 * third, and why a dependency was not the answer.
 *
 * **This build uses the third row of the scrypt table, N=2^15, r=8, p=3, and that
 * is a deliberate move away from the first.** The first row wants 128 MiB of
 * working memory in a single allocation. This app runs inside an Android process
 * with a heap the system decides the size of, on phones that are still supported at
 * 2 GB of RAM, and a backup that cannot be opened on the phone that holds the only
 * copy of a person's history is worse than a backup with the third strongest of
 * five settings the same reference calls equivalent. The three parallel passes cost
 * about the same processor time as one pass at 2^17 while the peak allocation stays
 * at 32 MiB, because [Scrypt.derive] reuses one buffer across them.
 *
 * **Every parameter travels in the file.** A later build can raise them without
 * making one earlier backup unreadable, which is the property that makes a number
 * chosen today safe to have chosen. [BackupCodec] reads them out of the header and
 * hands them back here.
 *
 * **What the sealed body is bound to.** The additional authenticated data is the
 * format version and the KDF and cipher parameters, so a ciphertext cannot be moved
 * onto a header describing a different derivation. It deliberately does not include
 * the counts or the dates in the header: those are covered by the checksum, which
 * spans the header and the body together, and a header edit that broke decryption
 * would be reported as a wrong password when it is nothing of the kind.
 */
object BackupCrypto {

    const val SCRYPT: String = "scrypt"
    const val CIPHER_TRANSFORMATION: String = "AES/GCM/NoPadding"

    /** The value of the header's `encryption` field when a password was set. */
    const val ENCRYPTION_SCRYPT_AES_GCM: String = "scrypt-aes-256-gcm"

    /** The value of the header's `encryption` field when one was not. */
    const val ENCRYPTION_NONE: String = "none"

    /** OWASP's N=2^15, r=8, p=3 row. See the note above for why not the first row. */
    const val SCRYPT_N: Int = 32768
    const val SCRYPT_R: Int = 8
    const val SCRYPT_P: Int = 3

    /** 256 bits, which is what makes the cipher AES-256-GCM. */
    const val KEY_BYTES: Int = 32

    const val SALT_BYTES: Int = 16

    /** 96 bits, the size AES-GCM is specified for and the only one it is fast at. */
    const val NONCE_BYTES: Int = 12

    const val TAG_BITS: Int = 128

    /**
     * The shortest password the export screen accepts.
     *
     * A number rather than a strength meter, and a low one. The file is not on a
     * server being attacked at scale; it is on a phone or a drive, and the person
     * setting the password is the person who will have to remember it in two years
     * to get their own history back. A forgotten password on the only copy of
     * something is the failure this whole feature exists to prevent, so the guidance
     * on the screen is worth more than a rule here, and the rule is only there to
     * stop a password of one character.
     */
    const val MINIMUM_PASSWORD_LENGTH: Int = 8

    /** The parameters a key was derived with, written into the file beside the body. */
    @Serializable
    data class KdfSpec(
        val name: String = SCRYPT,
        val n: Int = SCRYPT_N,
        val r: Int = SCRYPT_R,
        val p: Int = SCRYPT_P,
        val keyBytes: Int = KEY_BYTES,
        val salt: String,
    )

    /** The cipher a body was sealed with, written into the file beside the body. */
    @Serializable
    data class CipherSpec(
        val name: String = CIPHER_TRANSFORMATION,
        val nonce: String,
        val tagBits: Int = TAG_BITS,
    )

    /** A sealed body and everything needed to unseal it except the password. */
    class Sealed(val cipherText: ByteArray, val kdf: KdfSpec, val cipher: CipherSpec)

    /**
     * The one place a password becomes bytes.
     *
     * Normalized to NFC first. Two keyboards can produce the same visible password
     * as different sequences of code points, and a person who typed the same
     * characters on a different device would otherwise be told their password is
     * wrong. It also has to be one rule rather than a provider's guess, because a
     * second implementation of this format has to arrive at the same key.
     */
    fun passwordBytes(password: CharArray): ByteArray {
        require(password.isNotEmpty()) { "a password of no characters is not a password" }
        return Normalizer.normalize(String(password), Normalizer.Form.NFC)
            .toByteArray(Charsets.UTF_8)
    }

    fun seal(plain: ByteArray, password: CharArray, random: SecureRandom, formatVersion: Int): Sealed {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val kdf = KdfSpec(salt = encode(salt))
        val cipherSpec = CipherSpec(nonce = encode(nonce))

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, kdf), GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(associatedData(formatVersion, kdf, cipherSpec))
        return Sealed(cipher.doFinal(plain), kdf, cipherSpec)
    }

    /**
     * Throws `javax.crypto.AEADBadTagException` when the password is wrong, which
     * is the same thing as the body having been altered and is the only signal
     * there is. [BackupCodec] turns it into one sentence and writes nothing.
     */
    fun unseal(
        cipherText: ByteArray,
        password: CharArray,
        kdf: KdfSpec,
        cipherSpec: CipherSpec,
        formatVersion: Int,
    ): ByteArray {
        val cipher = Cipher.getInstance(cipherSpec.name)
        cipher.init(
            Cipher.DECRYPT_MODE,
            deriveKey(password, kdf),
            GCMParameterSpec(cipherSpec.tagBits, decode(cipherSpec.nonce)),
        )
        cipher.updateAAD(associatedData(formatVersion, kdf, cipherSpec))
        return cipher.doFinal(cipherText)
    }

    /**
     * True when this build can derive a key with these parameters at all.
     *
     * A file naming a function this build does not have is a file from a later
     * version, and saying so is more use than failing inside the cipher.
     */
    fun canDerive(kdf: KdfSpec, cipherSpec: CipherSpec): Boolean =
        kdf.name == SCRYPT &&
            cipherSpec.name == CIPHER_TRANSFORMATION &&
            kdf.n > 1 && (kdf.n and (kdf.n - 1)) == 0 &&
            kdf.r >= 1 && kdf.p >= 1 &&
            kdf.keyBytes in setOf(16, 24, 32) &&
            cipherSpec.tagBits in setOf(96, 104, 112, 120, 128)

    fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)

    /**
     * The password's bytes and the derived key's bytes are both zeroed here, which
     * is as far as this goes honestly. `SecretKeySpec` copies the key into a field
     * with no supported way to clear it, so the copy it keeps lives until it is
     * collected. Pretending otherwise with a `destroy()` the platform does not
     * implement would read as more care than is actually being taken.
     */
    private fun deriveKey(password: CharArray, kdf: KdfSpec): SecretKeySpec {
        val bytes = passwordBytes(password)
        val derived = try {
            Scrypt.derive(
                password = bytes,
                salt = decode(kdf.salt),
                n = kdf.n,
                r = kdf.r,
                p = kdf.p,
                keyBytes = kdf.keyBytes,
            )
        } finally {
            bytes.fill(0)
        }
        val key = SecretKeySpec(derived, "AES")
        derived.fill(0)
        return key
    }

    /**
     * Rebuilt identically on both sides from the parameters the file carries, so
     * that a ciphertext cannot be lifted onto a header that describes a different
     * derivation. ASCII, and every field spelled out rather than reordered.
     */
    private fun associatedData(formatVersion: Int, kdf: KdfSpec, cipherSpec: CipherSpec): ByteArray =
        listOf(
            BackupFormat.FORMAT,
            formatVersion.toString(),
            kdf.name,
            kdf.n.toString(),
            kdf.r.toString(),
            kdf.p.toString(),
            kdf.keyBytes.toString(),
            kdf.salt,
            cipherSpec.name,
            cipherSpec.nonce,
            cipherSpec.tagBits.toString(),
        ).joinToString("|").toByteArray(Charsets.US_ASCII)
}
