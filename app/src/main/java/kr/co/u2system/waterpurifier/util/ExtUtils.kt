package kr.co.u2system.waterpurifier.util

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*

fun Disposable.addTo(compositeDisposable: CompositeDisposable) = compositeDisposable.add(this)

val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)
fun Long.toFormattedTime(): String = format.format(Date(this))

fun TextView.highLightTargetText(targetText: String, colorCode: String) {
	val original: Spannable = SpannableString(this.text.toString())
	original.setSpan(
		ForegroundColorSpan(Color.parseColor(colorCode)),
		original.indexOf(targetText),
		original.indexOf(targetText) + targetText.length,
		Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
	)

	this.text = original
}