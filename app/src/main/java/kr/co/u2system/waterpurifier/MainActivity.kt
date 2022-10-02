package kr.co.u2system.waterpurifier

import android.annotation.SuppressLint
import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kr.co.u2system.waterpurifier.util.BluetoothManager
import kr.co.u2system.waterpurifier.util.Preferences
import kr.co.u2system.waterpurifier.util.addTo
import kr.co.u2system.waterpurifier.util.toFormattedTime
import kotlin.math.absoluteValue


class MainActivity : AppCompatActivity() {

	val compositeDisposable = CompositeDisposable()

	private val firstPurifierProgressImage: ClipDrawable by lazy {
		findViewById<ImageView>(R.id.iv_purifier_clip_1).drawable as ClipDrawable
	}
	private val secondPurifierProgressImage: ClipDrawable by lazy {
		findViewById<ImageView>(R.id.iv_purifier_clip_2).drawable as ClipDrawable
	}
	private val thirdPurifierProgressImage: ClipDrawable by lazy {
		findViewById<ImageView>(R.id.iv_purifier_clip_3).drawable as ClipDrawable
	}
	private val firstPurifierProgressText: TextView by lazy { findViewById(R.id.tv_percent_1) }
	private val secondPurifierProgressText: TextView by lazy { findViewById(R.id.tv_percent_2) }
	private val thirdPurifierProgressText: TextView by lazy { findViewById(R.id.tv_percent_3) }

	private var recentReceivedCount: Long = 0L

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		subscribeObservables()
		initClearBtns()
	}

	private fun updateRecentTime(timeInMillis: Long) {
		findViewById<TextView>(R.id.tv_last_connected_time).text =
			String.format(
				getString(R.string.last_update_time_format),
				timeInMillis.toFormattedTime()
			)
	}


	private fun subscribeObservables() {
		BluetoothManager.lastUpdateTime
			.subscribe(::updateRecentTime)
			.addTo(compositeDisposable)

		BluetoothManager.lastCount
			.subscribe { count ->
				Log.e("asdf", "count $count cache ${Preferences.firstPurifierCache} ml a ${calculateActualCount(1, count) / first_max.toFloat()* 10000}")
				Log.e("asdf", "count $count cache ${Preferences.secondPurifierCache} ml b ${calculateActualCount(2, count) /second_max.toFloat()* 10000}")
				Log.e("asdf", "count $count cache ${Preferences.thirdPurifierCache} ml c ${calculateActualCount(3, count) / third_max.toFloat()* 10000}")
				recentReceivedCount = count

				updateTotalCount(count)
				displayLevel(1, calculateActualCount(1, count) / first_max.toFloat())
				displayLevel(2, calculateActualCount(2, count) / second_max.toFloat())
				displayLevel(3, calculateActualCount(3, count) / third_max.toFloat())
			}
			.addTo(compositeDisposable)
	}

	private fun initClearBtns() {
		findViewById<View>(R.id.btn_filter_replace_1).setOnClickListener { sendClearCommand(1) }
		findViewById<View>(R.id.btn_filter_replace_2).setOnClickListener { sendClearCommand(2) }
		findViewById<View>(R.id.btn_filter_replace_3).setOnClickListener { sendClearCommand(3) }
	}

	private fun sendClearCommand(purifierIdx: Int) {
		(1..3).forEach { Preferences.writeToPurifier(it, recentReceivedCount, (it == purifierIdx)) }
		Preferences.totalPurifierCache = Preferences.totalPurifierCache + recentReceivedCount
		BluetoothManager.write("clear")
	}

	private fun updateTotalCount(count: Long) {
		findViewById<TextView>(R.id.tv_accumulated_usage).text = "${Preferences.totalPurifierCache + count} ml"
	}

	private fun displayLevel(purifierIdx: Int, percentValue: Float) {
		when (purifierIdx) {
			1 -> firstPurifierProgressImage
			2 -> secondPurifierProgressImage
			3 -> thirdPurifierProgressImage
			else -> null
		}?.level = (percentValue * 10000).toInt()
		when (purifierIdx) {
			1 -> firstPurifierProgressText
			2 -> secondPurifierProgressText
			3 -> thirdPurifierProgressText
			else -> null
		}?.text = (percentValue * 100).toInt().toString()
	}

	private fun calculateActualCount(purifierIdx: Int, receivedCount: Long): Long {
		return when(purifierIdx) {
			1 -> Preferences.firstPurifierCache
			2 -> Preferences.secondPurifierCache
			3 -> Preferences.thirdPurifierCache
			else -> 0
		} + receivedCount
	}

	override fun onDestroy() {
		super.onDestroy()
		compositeDisposable.clear()

	}

	companion object {
		const val first_max = 1000 * 90
		const val second_max = 1000 * 30
		const val third_max = 1000 * 10
	}
}

// 6ps = 1ml
/*
90L 1/9
30L 1/3
10L 1/1

0000 0006 0000

90L - 0 0
30L 1/3 60000
10L 1/1 60000

0000 0000 0000
 */


