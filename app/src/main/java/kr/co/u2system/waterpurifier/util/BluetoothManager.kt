package kr.co.u2system.waterpurifier.util

import android.content.Intent
import android.util.Log
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import io.reactivex.subjects.PublishSubject

object BluetoothManager {
	private val spp: BluetoothSPP by lazy { BluetoothSPP(App.appContext) }
	val bluetoothState: PublishSubject<Int> = PublishSubject.create()
	var lastUpdateTime: PublishSubject<Long> = PublishSubject.create()
	var lastCount: PublishSubject<Long> = PublishSubject.create()

	fun initManager() {
		if (!spp.isBluetoothEnabled) {
			spp.enable()
		}
		if (!spp.isServiceAvailable) {
			spp.setupService()
			spp.startService(BluetoothState.DEVICE_OTHER)
		}
		initBluetoothListener()
	}

	fun connect(data: Intent?) = spp.connect(data)

	private fun initBluetoothListener() {
		spp.setOnDataReceivedListener { _, message ->
			lastUpdateTime.onNext(System.currentTimeMillis())
			val cnt = message.replace(" ", "").toLong() / 6
			lastCount.onNext(cnt)
			println("lastCount $cnt message $message ")
		}

		spp.setBluetoothStateListener { state ->
			bluetoothState.onNext(state)
			when (state) {
				BluetoothState.STATE_CONNECTED -> {
					Log.e("asdf", "${spp.connectedDeviceName} 기기와 연결되었습니다")
				}
				BluetoothState.STATE_CONNECTING -> {
					Log.e("asdf", "기기와 연결 중입니다.")
				}
				BluetoothState.STATE_LISTEN -> {
					Log.e("asdf", "기기 연결을 기다리는 중입니다.")
				}
				BluetoothState.STATE_NONE -> {
					Log.e("asdf", "기기에 연결해주세요.")
				}
			}
		}
	}
}