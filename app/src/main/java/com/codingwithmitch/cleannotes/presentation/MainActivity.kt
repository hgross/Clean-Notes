package com.codingwithmitch.cleannotes.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.codingwithmitch.cleannotes.R
import com.codingwithmitch.cleannotes.NotesFeature
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG: String = "AppDebug"

    private val splitInstallManager by lazy{
        SplitInstallManagerFactory.create(application)
    }
    var notesModule: NotesFeature? = null
    private var sessionId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        splitInstallManager.registerListener(listener)

        getNoteListFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        splitInstallManager.unregisterListener(listener)
    }

    private val listener = SplitInstallStateUpdatedListener { state ->
        when (state.status()) {
            SplitInstallSessionStatus.FAILED -> {
                Log.d(TAG, "Module install failed with ${state.errorCode()}")
                Toast.makeText(application, "Module install failed with ${state.errorCode()}", Toast.LENGTH_SHORT).show()
            }
            SplitInstallSessionStatus.INSTALLED -> {
                Toast.makeText(application, "Notes module installed successfully", Toast.LENGTH_SHORT).show()
                getNoteListFragment()
            }
            else -> Log.d(TAG, "Status: ${state.status()}")
        }
    }

    fun getNoteListFragment() {
        if (notesModule == null) {
            if (isNotesInstalled()) {
                initializeNotesFeature()
            } else {
                requestNotesInstall()
            }
        }
        if (notesModule != null) {
            val fragment = (notesModule as NotesFeature).provideNoteListFragment()
            Log.d(TAG, "got fragment: ${fragment}")
        }
        else{
            Log.d(TAG, "NotesModule is NULL")
        }
    }

    fun initializeNotesFeature() {
        // try to obtain the storageFeature from the Dagger component:
        notesModule = (application as BaseApplication).appComponent.notesFeature()
        if (notesModule != null) {
            Log.d(TAG, "Loaded notes feature through dagger")
        }
    }

    private fun isNotesInstalled(): Boolean{
        return splitInstallManager
            .installedModules
            .contains(getString(R.string.module_notes_name))
    }

    private fun requestNotesInstall() {
        Toast.makeText(application, "Requesting notes module install", Toast.LENGTH_SHORT).show()
        val request =
            SplitInstallRequest
                .newBuilder()
                .addModule(getString(R.string.module_notes_name))
                .build()

        splitInstallManager
            .startInstall(request)
            .addOnSuccessListener { id -> sessionId = id }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error installing module: ", exception)
                Toast.makeText(application, "Error requesting module install", Toast.LENGTH_SHORT).show()
            }
    }

}

























