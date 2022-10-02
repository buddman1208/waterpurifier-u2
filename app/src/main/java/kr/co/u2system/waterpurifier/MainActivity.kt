package kr.co.u2system.waterpurifier

import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)


		val a = findViewById<ImageView>(R.id.iv_purifier_clip_1)
		(a.drawable as ClipDrawable).level = 6000
		val b = findViewById<ImageView>(R.id.iv_purifier_clip_2)
		(b.drawable as ClipDrawable).level = 4000
		val c = findViewById<ImageView>(R.id.iv_purifier_clip_3)
		(c.drawable as ClipDrawable).level = 7500


	}

	override fun onDestroy() {
		super.onDestroy()

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


