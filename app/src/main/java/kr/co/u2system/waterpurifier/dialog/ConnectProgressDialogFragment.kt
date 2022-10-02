package kr.co.u2system.waterpurifier.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.renderscript.RenderScript
import android.view.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import kr.co.u2system.waterpurifier.R

class ConnectProgressDialogFragment: DialogFragment() {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {

		return inflater.inflate(R.layout.fragment_connect_progress, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

	}

	override fun onResume() {
		super.onResume()
		val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
}