package com.smile.karaoke.ui_states

import com.smile.karaoke.models.FileDescription

sealed interface OpenFileUiState {
    companion object {
        const val EXCESS_MAX = 1
    }
    object Initial: OpenFileUiState
    object StartLoading: OpenFileUiState
    data class FinishLoading(val list: List<FileDescription>): OpenFileUiState
    data class ShowToast(val event: Int): OpenFileUiState
}