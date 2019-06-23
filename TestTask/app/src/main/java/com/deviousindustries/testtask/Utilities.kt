package com.deviousindustries.testtask

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.deviousindustries.testtask.constants.*
import java.util.*

class Utilities(){
    companion object{
        private var instantiated = false;
        lateinit var preferences : SharedPreferences
        fun instantiate(context: Context){
            if(!instantiated) {
                preferences = PreferenceManager.getDefaultSharedPreferences(context)
                DatabaseAccess.getInstance(context)
            };
        }

        fun getCurrentCalendar(): Calendar {
            val currentCalendar = Calendar.getInstance()
            if (preferences.getBoolean("enable_debug", false)) {
                //calNow.set(mPrefs.getString())
                val strDatePref = preferences.getString("DatePref", "")
                val strTimePref = preferences.getString("TimePref", "")
                if (strDatePref != "") {
                    val datePieces = strDatePref!!.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    currentCalendar.set(Calendar.YEAR, Integer.parseInt(datePieces[0]))
                    currentCalendar.set(Calendar.MONTH, Integer.parseInt(datePieces[1]) - 1)
                    currentCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(datePieces[2]))

                    if (preferences.getBoolean("enable_time", false) && strTimePref != "") {
                        val timePieces = strTimePref!!.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                        currentCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePieces[0]))
                        currentCalendar.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]))
                    } else {
                        currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        currentCalendar.set(Calendar.MINUTE, 0)
                    }
                }
                currentCalendar.set(Calendar.SECOND, 0)
                currentCalendar.set(Calendar.MILLISECOND, 0)
            }
            return currentCalendar
        }

        fun getEndCurrentDay(): Calendar {
            return getCurrentCalendar().apply {
                set(Calendar.HOUR_OF_DAY, END_HOUR)
                set(Calendar.MINUTE, END_MINUTE)
                set(Calendar.SECOND, END_SECOND)
                set(Calendar.MILLISECOND, END_MILLI)
            }
        }

        fun getBeginningCurentDay(): Calendar {
            return getCurrentCalendar().apply {
                set(Calendar.HOUR_OF_DAY, BEGIN_HOUR)
                set(Calendar.MINUTE, BEGIN_MINUTE)
                set(Calendar.SECOND, BEGIN_SECOND)
                set(Calendar.MILLISECOND, BEGIN_MILLI)
            }
        }

        fun getCalendar(milliDate: Long,
                        asBeginning: Boolean = false,
                        asEnd: Boolean = false): Calendar{
            return getCurrentCalendar().apply{
                timeInMillis = milliDate
                if (asBeginning) {
                    set(Calendar.HOUR_OF_DAY, BEGIN_HOUR)
                    set(Calendar.MINUTE, BEGIN_MINUTE)
                    set(Calendar.SECOND, BEGIN_SECOND)
                    set(Calendar.MILLISECOND, BEGIN_MILLI)
                }
                if (asEnd) {
                    set(Calendar.HOUR_OF_DAY, END_HOUR)
                    set(Calendar.MINUTE, END_MINUTE)
                    set(Calendar.SECOND, END_SECOND)
                    set(Calendar.MILLISECOND, END_MILLI)
                }
            }
        }


    }
}