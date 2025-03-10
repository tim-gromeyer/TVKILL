/**
 * Copyright (C) 2018 Jonas Lochmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.redirectapps.tvkill

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import java.util.*
import kotlin.collections.HashSet

// Singleton used to bind show_mute, depth, and widget_ids preferences
class Settings private constructor(context: Context) {
    companion object {
        private var settings: Settings? = null

        @Synchronized
        fun with(context: Context): Settings {
            if (settings == null) {
                settings = Settings(context.applicationContext)
            }

            return settings!!
        }

        private const val PREF_MUTE = "show_mute"
        private const val PREF_ADDITIONAL_PATTERNS = "depth"
        private const val PREF_WIDGET_IDS = "widget_ids"
    }

    private val showMuteInternal = MutableLiveData<Boolean>()
    private val additionalPatternsInternal = MutableLiveData<Boolean>()

    val showMute: LiveData<Boolean> = showMuteInternal
    val additionalPatterns: LiveData<Boolean> = additionalPatternsInternal
    private var appWidgetIds: Set<Int>
    private val lock = Object()
    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {

        showMuteInternal.value = preferences.getBoolean(PREF_MUTE, false)
        additionalPatternsInternal.value = preferences.getBoolean(PREF_ADDITIONAL_PATTERNS, false)

        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            when {
                PREF_MUTE == key -> showMuteInternal.setValue(preferences.getBoolean(PREF_MUTE, false))
                PREF_ADDITIONAL_PATTERNS == key -> additionalPatternsInternal.setValue(preferences.getBoolean(PREF_ADDITIONAL_PATTERNS, false))
                else -> {
                    // ignore
                }
            }
        }

        appWidgetIds = Collections.unmodifiableSet(
                HashSet<Int>(
                        preferences.getString(PREF_WIDGET_IDS, "")
                                ?.split(",")
                                ?.filter { it.isNotEmpty() }
                                ?.mapNotNull { it.toIntOrNull() }
                                .orEmpty()

                )
        )
    }

    fun getAppWidgetIds(): Set<Int> {
        return appWidgetIds
    }

    fun addAppWidgetIds(newIds: Collection<Int>) {
        synchronized(lock) {
            val newIdList = HashSet<Int>(appWidgetIds)
            newIdList.addAll(newIds)

            preferences.edit()
                    .putString(PREF_WIDGET_IDS, TextUtils.join(",", newIdList.map { it.toString() }))
                    .apply()

            this.appWidgetIds = Collections.unmodifiableSet(newIdList)
        }
    }

    fun removeAppWidgetIds(newIds: Collection<Int>) {
        synchronized(lock) {
            val newIdList = HashSet<Int>(appWidgetIds)
            newIdList.removeAll(newIds)

            preferences.edit()
                    .putString(PREF_WIDGET_IDS, TextUtils.join(",", newIdList.map { it.toString() }))
                    .apply()

            this.appWidgetIds = Collections.unmodifiableSet(newIdList)
        }
    }
}
