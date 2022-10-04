package kr.co.u2system.waterpurifier

import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kr.co.u2system.waterpurifier.dialog.PurifierResetDialog
import kr.co.u2system.waterpurifier.util.*


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

	private val filterChangeNoticeText: TextView by lazy { findViewById(R.id.tv_filter_change_notice) }
	private val firstFilterChangeSticker: View by lazy { findViewById(R.id.iv_filter_replace_sticker_1) }
	private val secondFilterChangeSticker: View by lazy { findViewById(R.id.iv_filter_replace_sticker_2) }
	private val thirdFilterChangeSticker: View by lazy { findViewById(R.id.iv_filter_replace_sticker_3) }

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
				recentReceivedCount = count

				updateTotalCount(count)

				val firstPurifierPercent = calculateActualCount(1, count) / first_max.toFloat()
				val secondPurifierPercent = calculateActualCount(2, count) / second_max.toFloat()
				val thirdPurifierPercent = calculateActualCount(3, count) / third_max.toFloat()
				displayLevel(1, firstPurifierPercent)
				displayLevel(2, secondPurifierPercent)
				displayLevel(3, thirdPurifierPercent)
				showNoticeFilterChangeIfNeeded(
					firstPurifierPercent,
					secondPurifierPercent,
					thirdPurifierPercent
				)
			}
			.addTo(compositeDisposable)
	}

	private fun initClearBtns() {
		findViewById<View>(R.id.btn_filter_replace_1).setOnClickListener { sendClearCommand(1) }
		findViewById<View>(R.id.btn_filter_replace_2).setOnClickListener { sendClearCommand(2) }
		findViewById<View>(R.id.btn_filter_replace_3).setOnClickListener { sendClearCommand(3) }
	}

	private fun sendClearCommand(purifierIdx: Int) {
		PurifierResetDialog.newInstance(object: PurifierResetDialog.ResetCallback{
			override fun onReset() {
				(1..3).forEach { Preferences.writeToPurifier(it, recentReceivedCount, (it == purifierIdx)) }
				Preferences.totalPurifierCache = Preferences.totalPurifierCache + recentReceivedCount
				BluetoothManager.write("clear")
			}
		}).show(supportFragmentManager, "tag")
	}

	private fun updateTotalCount(count: Long) {
		findViewById<TextView>(R.id.tv_accumulated_usage).text =
			"${Preferences.totalPurifierCache + count} ml"
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

	private fun showNoticeFilterChangeIfNeeded(
		firstPurifierPercent: Float,
		secondPurifierPercent: Float,
		thirdPurifierPercent: Float
	) {
		fun showFilterNotice(notifyTargetIndex: Int) {
			val highLightText =
				resources.getStringArray(R.array.filter_index_text)[notifyTargetIndex - 1]
			filterChangeNoticeText.text = String.format(
				getString(R.string.filter_change_noti_format),
				highLightText
			)
			filterChangeNoticeText.highLightTargetText(
				highLightText,
				HighlightColor.indexOf(notifyTargetIndex).colorCode
			)
			filterChangeNoticeText.visibility = View.VISIBLE

			firstFilterChangeSticker.visibility =
				if (notifyTargetIndex == 1) View.VISIBLE else View.GONE
			secondFilterChangeSticker.visibility =
				if (notifyTargetIndex == 2) View.VISIBLE else View.GONE
			thirdFilterChangeSticker.visibility =
				if (notifyTargetIndex == 3) View.VISIBLE else View.GONE
		}

		fun hideFilterNotice() {
			filterChangeNoticeText.visibility = View.GONE
			firstFilterChangeSticker.visibility = View.GONE
			secondFilterChangeSticker.visibility = View.GONE
			thirdFilterChangeSticker.visibility = View.GONE
		}
		when {
			thirdPurifierPercent >= 0.9 -> showFilterNotice(3)
			secondPurifierPercent >= 0.9 -> showFilterNotice(2)
			firstPurifierPercent >= 0.9 -> showFilterNotice(1)
			else -> hideFilterNotice()
		}
	}

	private fun calculateActualCount(purifierIdx: Int, receivedCount: Long): Long {
		return when (purifierIdx) {
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
		const val first_max = 1000 * 9
		const val second_max = 1000 * 3
		const val third_max = 1000 * 1
	}

	enum class HighlightColor(
		val colorCode: String
	) {
		FIRST("#6486ff"),
		SECOND("#29a4ff"),
		THIRD("#41b9df");

		companion object {
			fun indexOf(index: Int): HighlightColor = when (index) {
				1 -> FIRST
				2 -> SECOND
				3 -> THIRD
				else -> THIRD
			}
		}
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


