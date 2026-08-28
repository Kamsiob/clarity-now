package com.kamsiob.claritynow.data.export

/**
 * Why a file was refused, and nothing else. MASTER_BUILD_PROMPT 14b.7.
 *
 * **These six sentences are fixed strings and not corpus lines, and the reason is
 * worth stating rather than leaving to be rediscovered.** CLAUDE.md rule 8 sends
 * every sentence about a person's own data through a corpus and the engine layers.
 * A sentence about a file is not one: it describes something that arrived from
 * outside, it has no facts behind it to validate, and there is nothing here for an
 * observation to be right or wrong about. 14b.7 says so explicitly and calls it one
 * of the few places the rule does not reach.
 *
 * **The screen owns the strings; this enum owns the reasons.** The exact wording
 * for each one is written below and belongs in `strings.xml` under the name given,
 * because that is where a fixed label lives. Nothing here formats a sentence, so
 * there is no second path to the screen and no sentence that skipped a validator.
 *
 * **Six and not twelve.** Every internal distinction that does not change what a
 * person can do about it collapses into one of these. A truncated file, a flipped
 * bit and a checksum that was edited are all [DAMAGED], because the answer to all
 * three is the same: this copy is no good, find another. The distinctions that do
 * survive are the ones that change the next action: try a different file, try a
 * different password, update the app, or nothing at all.
 *
 * [Refusal.diagnostic] carries the precise cause for a bug report and for the tests
 * to assert on, and is never shown to anyone.
 */
enum class ImportRefusal {

    /**
     * `import_refused_not_a_backup`
     *
     * > This is not a Clarity Now backup file.
     */
    NOT_A_BACKUP,

    /**
     * `import_refused_newer_version`
     *
     * > This backup was written by a newer version of Clarity Now.
     *
     * Covers a later envelope version, a later event schema version, and a record
     * of a kind this build has never heard of. Importing part of a file and
     * dropping the rest is the failure MASTER_BUILD_PROMPT 6.4 describes: nothing
     * looks wrong afterwards, the numbers are just smaller, forever.
     */
    NEWER_VERSION,

    /**
     * `import_refused_damaged`
     *
     * > This backup is damaged or incomplete.
     */
    DAMAGED,

    /**
     * `import_refused_password_required`
     *
     * > This backup is protected by a password.
     */
    PASSWORD_REQUIRED,

    /**
     * `import_refused_wrong_password`
     *
     * > That password did not open this backup.
     *
     * Nothing has been touched when this is returned, and nothing can have been:
     * [BackupCodec.read] has no way to write. The screen is free to say so, and
     * that reassurance is a sentence about the app rather than about the file.
     */
    WRONG_PASSWORD,

    /**
     * `import_refused_inconsistent`
     *
     * > This backup does not hold the history it says it holds.
     *
     * The file parsed and opened, and then disagreed with itself: a record count
     * that is not the number of records, the same record twice, or a record with no
     * identity. Distinct from [DAMAGED] because a damaged file is a transport
     * accident and this is a file that was built wrong.
     */
    INCONSISTENT,
}
