package com.appnest.safe.credentials.vault.ui.viewmodel.add

sealed class AddCredentialEvent {
    object SaveSuccess : AddCredentialEvent()
    data class SaveError(val message: String) : AddCredentialEvent()
}