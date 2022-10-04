package kr.co.u2system.waterpurifier.dialog

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.DialogFragment
import app.akexorcist.bluetotohspp.library.BluetoothState
import kr.co.u2system.waterpurifier.R

class PurifierResetDialog private constructor(
	private val callback: ResetCallback
) : DialogFragment() {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_purifier_reset, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) = view.run {
		super.onViewCreated(view, savedInstanceState)


		findViewById<View>(R.id.btn_confirm).setOnClickListener {
			callback.onReset()
			dialog?.dismiss()
		}
		findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog?.dismiss() }
	}

	override fun onResume() {
		super.onResume()
		val windowManager =
			requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			requireActivity().display
		} else {
			windowManager.defaultDisplay
		}
		val size = Point()
		display?.getSize(size)

		val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
		val deviceWidth = size.x
		params?.width = (deviceWidth * 0.9).toInt()
		dialog?.window?.attributes = params as WindowManager.LayoutParams
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

	}
	companion object {
		fun newInstance(
			callback: ResetCallback
		): PurifierResetDialog {
			val fragment = PurifierResetDialog(callback)
			return fragment
		}
	}

	interface ResetCallback {
		fun onReset()
	}
}