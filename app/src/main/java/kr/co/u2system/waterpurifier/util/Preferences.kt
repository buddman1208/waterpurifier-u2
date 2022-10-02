package kr.co.u2system.waterpurifier.util

import android.content.SharedPreferences
import kotlin.math.max

object Preferences {
	val preference: SharedPreferences by lazy {
		App.appContext.getSharedPreferences("water_purifier", 0)
	}
	val editor: SharedPreferences.Editor by lazy { preference.edit() }

	var firstPurifierCache: Long
		get() = preference.getLong(PreferencesKey.FIRST_PURIFIER_KEY.key, 0L)
		set(value) = editor.putLong(PreferencesKey.FIRST_PURIFIER_KEY.key, value).apply()

	var secondPurifierCache: Long
		get() = preference.getLong(PreferencesKey.SECOND_PURIFIER_KEY.key, 0L)
		set(value) = editor.putLong(PreferencesKey.SECOND_PURIFIER_KEY.key, value).apply()

	var thirdPurifierCache: Long
		get() = preference.getLong(PreferencesKey.THIRD_PURIFIER_KEY.key, 0L)
		set(value) = editor.putLong(PreferencesKey.THIRD_PURIFIER_KEY.key, value).apply()

	var totalPurifierCache: Long
		get() = preference.getLong(PreferencesKey.TOTAL_PURIFY_COUNT_KEY.key, 0L)
		set(value) = editor.putLong(PreferencesKey.TOTAL_PURIFY_COUNT_KEY.key, value).apply()


	fun writeToPurifier(index: Int, value: Long, isClear: Boolean) {
		when (index) {
			1 -> PreferencesKey.FIRST_PURIFIER_KEY
			2 -> PreferencesKey.SECOND_PURIFIER_KEY
			3 -> PreferencesKey.THIRD_PURIFIER_KEY
			else -> null
		}?.let {
			val originValue = preference.getLong(it.key, 0L)
			editor.putLong(
				it.key,
				if (isClear) 0L else originValue + value
			).apply()
		}
	}

	enum class PreferencesKey(val key: String) {
		FIRST_PURIFIER_KEY("first_purifier_key"),
		SECOND_PURIFIER_KEY("second_purifier_key"),
		THIRD_PURIFIER_KEY("third_purifier_key"),
		TOTAL_PURIFY_COUNT_KEY("total_purify_count_key")
	}

}