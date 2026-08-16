package com.kamsiob.claritynow

import android.app.Application
import com.kamsiob.claritynow.di.ClarityGraph

class ClarityApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ClarityGraph.install(this)
    }
}
