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

@SuppressLint("MissingPermission")
class DeviceListDialogFragment private constructor(
	val callback: DeviceSelectCallback
) : DialogFragment() {

	// Member fields
	private lateinit var mBtAdapter: BluetoothAdapter
	private lateinit var mPairedDevicesArrayAdapter: ArrayAdapter<String>
	private var pairedDevices: Set<BluetoothDevice> = setOf()


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_device_list, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		initList(view)
	}

	private fun initList(view: View) = view.run {
		mPairedDevicesArrayAdapter = ArrayAdapter(context, R.layout.item_device_name)

		findViewById<ListView>(R.id.list_devices).apply {
			adapter = mPairedDevicesArrayAdapter
			onItemClickListener = mDeviceClickListener
		}
		findViewById<TextView>(R.id.btn_cancel).setOnClickListener { dialog?.dismiss() }

		var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
		requireActivity().registerReceiver(mReceiver, filter)

		filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
		requireActivity().registerReceiver(mReceiver, filter)

		mBtAdapter = BluetoothAdapter.getDefaultAdapter()
		pairedDevices = mBtAdapter.bondedDevices

		if (pairedDevices.isNotEmpty()) {
			for (device in pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.name + "\n" + device.address)
			}
		} else {
			val noDevices = "No devices found"
			mPairedDevicesArrayAdapter.add(noDevices)
		}

		doDiscovery()
	}

	// Start device discover with the BluetoothAdapter
	private fun doDiscovery() {
		// Remove all element from the list
		mPairedDevicesArrayAdapter!!.clear()

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.isNotEmpty()) {
			for (device in pairedDevices) {
				mPairedDevicesArrayAdapter.add(
					"""
					${device.name}
					${device.address}
					""".trimIndent()
				)
			}
		} else {
			var strNoFound = "No devices found"
			mPairedDevicesArrayAdapter.add(strNoFound)
		}

		if (mBtAdapter.isDiscovering) {
			mBtAdapter.cancelDiscovery()
		}

		mBtAdapter.startDiscovery()
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			val action = intent.action

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND == action) {
				intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
					?.let { device ->
						if (device.bondState != BluetoothDevice.BOND_BONDED) {
							val strNoFound = "No devices found"
							if (mPairedDevicesArrayAdapter.getItem(0) == strNoFound) {
								mPairedDevicesArrayAdapter.remove(strNoFound)
							}
							if (!device.name.isNullOrBlank()) mPairedDevicesArrayAdapter.add(
								"${device.name}\n${device.address}".trimIndent()
							)
						}
					}

				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
				view?.findViewById<View>(R.id.progress_bar)?.visibility = View.GONE
			}
		}
	}

	// The on-click listener for all devices in the ListViews
	private val mDeviceClickListener =
		OnItemClickListener { av, v, arg2, arg3 ->
			// Cancel discovery because it's costly and we're about to connect
			if (mBtAdapter.isDiscovering) mBtAdapter.cancelDiscovery()
			val strNoFound = "No devices found"
			if ((v as TextView).text.toString() != strNoFound) {
				// Get the device MAC address, which is the last 17 chars in the View
				val info = v.text.toString()
				val address = info.substring(info.length - 17)

				// Create the result Intent and include the MAC address
				val intent = Intent()
				intent.putExtra(BluetoothState.EXTRA_DEVICE_ADDRESS, address)

				callback.onDeviceSelect(intent)
				requireDialog().dismiss()
			}
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

	override fun onDestroy() {
		super.onDestroy()
		// Make sure we're not doing discovery anymore
		mBtAdapter.cancelDiscovery()

		// Unregister broadcast listeners
		requireActivity().unregisterReceiver(mReceiver)
		dialog?.dismiss()
	}

	companion object {
		fun newInstance(
			callback: DeviceSelectCallback
		): DeviceListDialogFragment {
			val fragment = DeviceListDialogFragment(callback)
			return fragment
		}
	}

	interface DeviceSelectCallback {
		fun onDeviceSelect(intent: Intent)
	}
}