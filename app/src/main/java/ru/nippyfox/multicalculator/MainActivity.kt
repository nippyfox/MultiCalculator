package ru.nippyfox.multicalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import ru.nippyfox.multicalculator.databinding.ActivityMainBinding
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var generalFragment: GeneralFragment
    private lateinit var settingsFragment: SettingsFragment

    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getSharedPreferences("nf_multi_calculator", Context.MODE_PRIVATE)
        val userLanguage = sharedPrefs.getString("user_language", null)
        val wasCreatedWithoutSettings = sharedPrefs.getBoolean("start_without_recreate", true)

        if (userLanguage != null) { // если пользователь уже выбирал язык, то применяем его
            val locale = Locale(userLanguage)
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        generalFragment = GeneralFragment()
        settingsFragment = SettingsFragment()

        setContentView(binding.root)

        if (!wasCreatedWithoutSettings) { // если пользователь изменил настройки и произошёл activity.recreate
            makeGeneralVisible()
            makeSettingsVisible()
            val editor = sharedPrefs.edit()
            editor.putBoolean("start_without_recreate", true)
            editor.apply()
        }

        // смена фрагмента в зависимости от выбранного раздела меню
        binding.btnBottomGeneral.setOnClickListener {
            if (!isGeneralFragmentVisible()) makeGeneralVisible()
        }

        binding.dotGeneral.setOnClickListener {
            if (!isGeneralFragmentVisible()) makeGeneralVisible()
        }

        binding.btnBottomSettings.setOnClickListener {
            if (!isSettingsFragmentVisible()) makeSettingsVisible()
        }

        binding.btnBottomSettings.setOnClickListener {
            if (!isSettingsFragmentVisible()) makeSettingsVisible()
        }
    }

    private fun isGeneralFragmentVisible(): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(binding.mainFragment.id)
        return currentFragment is GeneralFragment
    }

    private fun isSettingsFragmentVisible(): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(binding.mainFragment.id)
        return currentFragment is SettingsFragment
    }

    private fun makeGeneralVisible() {
        val fragmentManager = supportFragmentManager
        val settingsFragment = fragmentManager.findFragmentById(binding.mainFragment.id)
        if (settingsFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(settingsFragment)
                .commit()
        }

        for (i in 0 until binding.customBottomBar.childCount) { // возврат всех полей в меню в нормальное состояние по жирности
            val child = binding.customBottomBar.getChildAt(i)
            if (child is TextView) child.setTypeface(null, Typeface.NORMAL)
        }
        for (i in 0 until binding.customBottomBarDots.childCount) { // обнуление текста c точками во всех пунктах
            val child = binding.customBottomBarDots.getChildAt(i)
            if (child is TextView) child.text = ""
        }

        binding.btnBottomGeneral.setTypeface(null, Typeface.BOLD) // сделать шрифт жирным у выбранного раздела меню
        binding.dotGeneral.text = getString(R.string.barDot) // поставить точку у выбранного раздела меню
    }

    private fun makeSettingsVisible() {
        fragmentManager.beginTransaction()
            .add(binding.mainFragment.id, settingsFragment)
            .commit()

        for (i in 0 until binding.customBottomBar.childCount) { // возврат всех полей в меню в нормальное состояние по жирности
            val child = binding.customBottomBar.getChildAt(i)
            if (child is TextView) child.setTypeface(null, Typeface.NORMAL)
        }
        for (i in 0 until binding.customBottomBarDots.childCount) { // обнуление текста c точками во всех пунктах
            val child = binding.customBottomBarDots.getChildAt(i)
            if (child is TextView) child.text = ""
        }

        binding.btnBottomSettings.setTypeface(null, Typeface.BOLD) // сделать шрифт жирным у выбранного раздела меню
        binding.dotSettings.text = getString(R.string.barDot) // поставить точку у выбранного раздела меню
    }
}
