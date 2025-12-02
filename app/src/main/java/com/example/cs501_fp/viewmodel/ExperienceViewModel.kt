package com.example.cs501_fp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs501_fp.data.local.AppDatabase
import com.example.cs501_fp.data.local.entity.Experience
import com.example.cs501_fp.data.repository.LocalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExperienceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)

    private val repo = LocalRepository(
        database.userEventDao(),
        database.experienceDao()
    )

    val experiences = repo.getAllExperiences()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}