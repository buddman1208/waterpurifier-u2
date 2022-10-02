package kr.co.u2system.waterpurifier.util

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*

fun Disposable.addTo(compositeDisposable: CompositeDisposable) = compositeDisposable.add(this)

val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)
fun Long.toFormattedTime(): String = format.format(Date(this))