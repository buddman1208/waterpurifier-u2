package kr.co.u2system.waterpurifier

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.akexorcist.bluetotohspp.library.BluetoothState
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import io.reactivex.disposables.CompositeDisposable
import kr.co.u2system.waterpurifier.dialog.ConnectProgressDialogFragment
import kr.co.u2system.waterpurifier.dialog.DeviceListDialogFragment
import kr.co.u2system.waterpurifier.util.BluetoothManager


class BluetoothConnectActivity : AppCompatActivity() {

	private val compositeDisposable: CompositeDisposable = CompositeDisposable()
	private val bluetoothDeviceCallback = object: DeviceListDialogFragment.DeviceSelectCallback{
		override fun onDeviceSelect(intent: Intent) {
			BluetoothManager.connect(intent)
		}
	}

	private lateinit var connectingDialog: ConnectProgressDialogFragment

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_bluetooth_connect)
		checkPermissions()

		findViewById<Button>(R.id.btn_add_purifier).setOnClickListener {
			DeviceListDialogFragment.newInstance(bluetoothDeviceCallback).show(supportFragmentManager, "")
		}

	}

	private fun checkPermissions() {
		val permissions = if (Build.VERSION.SDK_INT > 30) {
			arrayOf(
				Manifest.permission.BLUETOOTH_CONNECT,
				Manifest.permission.BLUETOOTH_SCAN
			)
		} else {
			arrayOf(
				Manifest.permission.BLUETOOTH,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION
			)
		}
		TedPermission.create()
			.setPermissionListener(object : PermissionListener {
				override fun onPermissionGranted() {
					initBluetoothStateListener()
					BluetoothManager.initManager()
				}

				override fun onPermissionDenied(deniedPermissions: List<String>) {
					checkPermissions()
				}
			})
			.setDeniedMessage("????????? ????????? ????????? ????????? ???????????? ?????? ???????????? ????????? ??? ????????????.\n\n[??????] > [??????]?????? ????????? ????????? ????????? ?????????.")
			.setPermissions(*permissions)
			.check()

	}

	private fun initBluetoothStateListener() {
		compositeDisposable.add(
			BluetoothManager.bluetoothState.subscribe {
				when (it) {
					BluetoothState.STATE_CONNECTED -> startMainActivity()
					BluetoothState.STATE_CONNECTING -> {
						connectingDialog = ConnectProgressDialogFragment().apply {
							show(supportFragmentManager, "connectingDialog")
						}
					}
				}
			}
		)
	}

	private fun startMainActivity() {
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}

	override fun onDestroy() {
		super.onDestroy()
		compositeDisposable.clear()
	}
}

// 6ps = 1ml
/*
900L
300L
100L
 */
