package com.kamsiob.claritynow.di

import android.content.Context
import com.kamsiob.claritynow.data.db.ClarityDatabase
import com.kamsiob.claritynow.data.prefs.ClarityPreferences
import com.kamsiob.claritynow.data.repo.ClarityRepository
import com.kamsiob.claritynow.domain.ClarityClock
import com.kamsiob.claritynow.domain.SystemClarityClock
import com.kamsiob.claritynow.ui.theme.AndroidClarityHaptics
import com.kamsiob.claritynow.ui.theme.ClarityHaptics

/**
 * The dependency container, written by hand on purpose.
 *
 * MASTER_BUILD_PROMPT section 3: for a single module app, an annotation processor
 * based injector adds build time and failure modes that are hard to diagnose
 * without reading source, which is exactly the position the builder is in.
 *
 * Everything here is created lazily and lives for the process. Nothing in this
 * object holds an Activity or any other short lived Context.
 */
object ClarityGraph {

    private lateinit var appContext: Context

    fun install(context: Context) {
        appContext = context.applicationContext
    }

    val clock: ClarityClock by lazy { SystemClarityClock() }

    val database: ClarityDatabase by lazy { ClarityDatabase.build(appContext) }

    val preferences: ClarityPreferences by lazy { ClarityPreferences(appContext) }

    /** The only writer in the app. MASTER_BUILD_PROMPT 5.5. */
    val repository: ClarityRepository by lazy {
        ClarityRepository(db = database, prefs = preferences, clock = clock)
    }

    val haptics: ClarityHaptics by lazy { AndroidClarityHaptics(appContext) }

    /** True once [install] has run. Guards against use from a ContentProvider that
     *  initializes before Application.onCreate. */
    val isInstalled: Boolean
        get() = ::appContext.isInitialized
}
