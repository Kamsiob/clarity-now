package com.kamsiob.claritynow.domain.engine.catalog

import java.io.File

/**
 * The committed corpus files, and the catalog built from them.
 *
 * **These tests run against the real corpus, not a fixture.** That is the point of them.
 * A parser tested only against a synthetic corpus proves the parser and nothing about the
 * three files the app actually reads, and the failure this whole package guards against is
 * a real file drifting away from the engine.
 *
 * Unit tests run from the app module directory, which is what `GoldenFixture` has relied on
 * since phase 1 and what `DomainPurityTest` asserts before it scans anything. The same
 * assumption is checked here before any path is opened, because a wrong working directory
 * would make every test below fail with a missing file rather than with anything useful.
 */
internal object CorpusFixture {

    private const val REPOSITORY_ROOT = ".."

    /** The engine specification, read by the tests that check a table in it against the code. */
    const val LOGIC_ENGINE = "CLARITY_LOGIC_ENGINE.md"

    val pulseText: String by lazy { read(CorpusVolume.PULSE.fileName) }
    val reportText: String by lazy { read(CorpusVolume.REPORT.fileName) }
    val momentumText: String by lazy { read(CorpusVolume.MOMENTUM.fileName) }
    val logicEngineText: String by lazy { read(LOGIC_ENGINE) }

    val pulse: ParsedCorpus by lazy { CorpusParser.parse(CorpusVolume.PULSE, pulseText) }
    val report: ParsedCorpus by lazy { CorpusParser.parse(CorpusVolume.REPORT, reportText) }
    val momentum: ParsedCorpus by lazy { CorpusParser.parse(CorpusVolume.MOMENTUM, momentumText) }

    val catalog: ClarityCatalog by lazy { ClarityCatalog.build(pulseText, reportText, momentumText) }

    val volumes: List<ParsedCorpus> by lazy { listOf(pulse, report, momentum) }

    /**
     * The keyed lines in [fileName], counted the way each volume's own totals table counts
     * them: a line inside a fenced block that begins with a dotted key.
     *
     * The three lines in `CORPUS_2_REPORT.md` that carry no key are excluded, which is why
     * this counts 737 there and not the 740 non blank fenced lines the file holds. Two of
     * them are the accept and decline labels in 4.5 and the third is the fixed generated
     * line in 5.1.
     */
    fun keyedLineCount(fileName: String): Int {
        var inFence = false
        var count = 0
        for (line in read(fileName).split('\n')) {
            if (line.trimStart().startsWith("```")) {
                inFence = !inFence
                continue
            }
            if (inFence && KEYED.containsMatchIn(line)) count++
        }
        return count
    }

    private val KEYED = Regex("""^[a-z][A-Za-z0-9]*(?:\.[A-Za-z0-9]+)+\s""")

    fun read(fileName: String): String {
        val file = File(REPOSITORY_ROOT, fileName)
        check(File("build.gradle.kts").isFile) {
            "unit tests are expected to run from the app module directory, and this run is in " +
                File("").absolutePath
        }
        check(file.isFile) { "missing ${file.path}" }
        return file.readText()
    }
}
