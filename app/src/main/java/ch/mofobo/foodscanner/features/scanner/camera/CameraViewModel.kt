package ch.mofobo.foodscanner.features.scanner.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.mofobo.foodscanner.features.scanner.camera.CameraViewModel.Action.SearchBarcode
import ch.mofobo.foodscanner.utils.SingleLiveEvent

class CameraViewModel : ViewModel() {

    val actions = SingleLiveEvent<Action>()

    var isScanned = false

    private val _text = MutableLiveData<String>().apply {
        value = "This is fight Fragment"
    }
    val text: LiveData<String> = _text

    fun onBarcodeScanned(barcode: String?) {
        if (!isScanned && !barcode.isNullOrBlank()) {
            isScanned = true
            actions.postValue(SearchBarcode(barcode))
        }
    }

    sealed class Action {
        data class SearchBarcode(val barcode: String) : Action()
    }
}