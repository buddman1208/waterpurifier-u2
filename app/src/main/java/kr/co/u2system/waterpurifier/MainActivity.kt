package kr.co.u2system.waterpurifier

import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kr.co.u2system.waterpurifier.util.BluetoothManager
import kr.co.u2system.waterpurifier.util.addTo
import kr.co.u2system.waterpurifier.util.toFormattedTime


class MainActivity : AppCompatActivity() {

	val compositeDisposable = CompositeDisposable()
	val a: ClipDrawable by lazy {
		findViewById<ImageView>(R.id.iv_purifier_clip_1)
		a.drawable as ClipDrawable
	}
	val b: ClipDrawable by lazy {
		findViewById<ImageView>(R.id.iv_purifier_clip_2)
		b.drawable as ClipDrawable
	}
	val c: ClipDrawable by lazy {
		findViewById<ImageView>(R.id.iv_purifier_clip_3)
		c.drawable as ClipDrawable
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		subscribeObservables()
	}

	fun updateRecentTime(timeInMillis: Long) {
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
	}

	override fun onDestroy() {
		super.onDestroy()
		compositeDisposable.clear()
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


