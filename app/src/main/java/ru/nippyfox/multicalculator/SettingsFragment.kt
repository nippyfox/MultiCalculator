package ru.nippyfox.multicalculator

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import ru.nippyfox.multicalculator.databinding.FragmentSettingsBinding
import java.util.Locale
import kotlin.properties.Delegates

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val sharedPreferences = requireActivity()
            .getSharedPreferences("nf_multi_calculator", Context.MODE_PRIVATE)

        when (sharedPreferences.getInt("user_theme", -1)) {
            -1 -> binding.tvSettingsThemeChosen.text = getString(R.string.theme_system)
            Configuration.UI_MODE_NIGHT_NO -> binding.tvSettingsThemeChosen.text = getString(R.string.theme_light)
            Configuration.UI_MODE_NIGHT_YES -> binding.tvSettingsThemeChosen.text = getString(R.string.theme_dark)
        }

        // Открытие контекстного меню
        registerForContextMenu(binding.llLanguageSettings)
        registerForContextMenu(binding.llThemeSettings)

        binding.llLanguageSettings.setOnClickListener {
            activity?.openContextMenu(it)
        }
        binding.llThemeSettings.setOnClickListener {
            activity?.openContextMenu(it)
        }

        // Изначальное положение операторов
        if (sharedPreferences.getBoolean("use_addition", true)) binding.cbAddition.isChecked = true
        if (sharedPreferences.getBoolean("use_subtract", true)) binding.cbSubtraction.isChecked = true
        if (sharedPreferences.getBoolean("use_multiply", true)) binding.cbMultiplication.isChecked = true
        if (sharedPreferences.getBoolean("use_divide", true)) binding.cbDivision.isChecked = true
        if (sharedPreferences.getBoolean("use_power_of", false)) binding.cbPowerOf.isChecked = true
        if (sharedPreferences.getBoolean("use_mod", false)) binding.cbMod.isChecked = true

        // Включение и выключение операторов
        binding.llAdditionSettings.setOnClickListener {
            changeAddition(sharedPreferences)
            activity?.recreate()
        }
        binding.tvAdditionSettings.setOnClickListener {
            changeAddition(sharedPreferences)
            activity?.recreate()
        }
        binding.cbAddition.setOnClickListener {
            binding.cbAddition.isChecked = !binding.cbAddition.isChecked
            changeAddition(sharedPreferences)
            activity?.recreate()
        }
        binding.llSubtractionSettings.setOnClickListener {
            changeSubtraction(sharedPreferences)
            activity?.recreate()
        }
        binding.tvSubtractionSettings.setOnClickListener {
            changeSubtraction(sharedPreferences)
            activity?.recreate()
        }
        binding.cbSubtraction.setOnClickListener {
            binding.cbSubtraction.isChecked = !binding.cbSubtraction.isChecked
            changeSubtraction(sharedPreferences)
            activity?.recreate()
        }
        binding.llMultiplicationSettings.setOnClickListener {
            changeMultiplication(sharedPreferences)
            activity?.recreate()
        }
        binding.tvMultiplicationSettings.setOnClickListener {
            changeMultiplication(sharedPreferences)
            activity?.recreate()
        }
        binding.cbMultiplication.setOnClickListener {
            binding.cbMultiplication.isChecked = !binding.cbMultiplication.isChecked
            changeMultiplication(sharedPreferences)
            activity?.recreate()
        }
        binding.llDivisionSettings.setOnClickListener {
            changeDivision(sharedPreferences)
            activity?.recreate()
        }
        binding.tvDivisionSettings.setOnClickListener {
            changeDivision(sharedPreferences)
            activity?.recreate()
        }
        binding.cbDivision.setOnClickListener {
            binding.cbDivision.isChecked = !binding.cbDivision.isChecked
            changeDivision(sharedPreferences)
            activity?.recreate()
        }
        binding.llPowerOfSettings.setOnClickListener {
            changePowerOf(sharedPreferences)
            activity?.recreate()
        }
        binding.tvPowerOfSettings.setOnClickListener {
            changePowerOf(sharedPreferences)
            activity?.recreate()
        }
        binding.cbPowerOf.setOnClickListener {
            binding.cbPowerOf.isChecked = !binding.cbPowerOf.isChecked
            changePowerOf(sharedPreferences)
            activity?.recreate()
        }
        binding.llModSettings.setOnClickListener {
            changeMod(sharedPreferences)
            activity?.recreate()
        }
        binding.tvModSettings.setOnClickListener {
            changeMod(sharedPreferences)
            activity?.recreate()
        }
        binding.cbMod.setOnClickListener {
            binding.cbMod.isChecked = !binding.cbMod.isChecked
            changeMod(sharedPreferences)
            activity?.recreate()
        }

        return binding.root
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            binding.llLanguageSettings.id -> {
                requireActivity().menuInflater.inflate(R.menu.language_context_menu, menu)
            }
            binding.llThemeSettings.id -> {
                requireActivity().menuInflater.inflate(R.menu.theme_context_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_lang_en -> changeLanguage(this, "en")
            R.id.item_lang_de -> changeLanguage(this, "de")
            R.id.item_lang_ru -> changeLanguage(this, "ru")
            R.id.item_theme_system -> changeTheme(this, -1)
            R.id.item_theme_dark -> changeTheme(this, Configuration.UI_MODE_NIGHT_YES)
            R.id.item_theme_light -> changeTheme(this, Configuration.UI_MODE_NIGHT_NO)
        }
        return super.onContextItemSelected(item)
    }

    private fun changeLanguage(fragment: Fragment, lang: String) {
        val context = fragment.requireContext()
        val locale = Locale(lang)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)

        val sharedPreferences = requireActivity()
            .getSharedPreferences("nf_multi_calculator", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_language", lang)
        editor.putBoolean("start_without_recreate", false)
        editor.apply()

        fragment.activity?.let {
            it.createConfigurationContext(config)
            it.resources.updateConfiguration(config, it.resources.displayMetrics)
            it.recreate()
        }
    }

    private fun changeTheme(fragment: Fragment, themeMode: Int) {
        val context = fragment.requireContext()
        val sharedPreferences = requireActivity()
            .getSharedPreferences("nf_multi_calculator", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        when (themeMode) {
            Configuration.UI_MODE_NIGHT_NO -> { // Светлая тема
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Configuration.UI_MODE_NIGHT_YES -> { // Тёмная тема
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> { // Системная тема
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
        editor.putInt("user_theme", themeMode)
        editor.putBoolean("start_without_recreate", false)
        editor.apply()
        fragment.activity?.recreate()
    }

    private fun checkOperatorsAmount(): Int {
        var count = 0
        if (binding.cbAddition.isChecked) count += 1
        if (binding.cbSubtraction.isChecked) count += 1
        if (binding.cbMultiplication.isChecked) count += 1
        if (binding.cbDivision.isChecked) count += 1
        if (binding.cbPowerOf.isChecked) count += 1
        if (binding.cbMod.isChecked) count += 1
        return count
    }

    private fun changeAddition(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val newValue = !binding.cbAddition.isChecked
        if (checkOperatorsAmount() > 4 || newValue) {
            binding.cbAddition.isChecked = newValue
            editor.putBoolean("use_addition", newValue)
            editor.putBoolean("start_without_recreate", false)
        } else {
            Toast.makeText(context, getString(R.string.attention_more_than_4_operators), Toast.LENGTH_LONG).show()
        }
        editor.apply()
    }

    private fun changeSubtraction(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val newValue = !binding.cbSubtraction.isChecked
        if (checkOperatorsAmount() > 4 || newValue) {
            binding.cbSubtraction.isChecked = newValue
            editor.putBoolean("use_subtract", newValue)
            editor.putBoolean("start_without_recreate", false)
        } else {
            Toast.makeText(context, getString(R.string.attention_more_than_4_operators), Toast.LENGTH_LONG).show()
        }
        editor.apply()
    }

    private fun changeMultiplication(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val newValue = !binding.cbMultiplication.isChecked
        if (checkOperatorsAmount() > 4 || newValue) {
            binding.cbMultiplication.isChecked = newValue
            editor.putBoolean("use_multiply", newValue)
            editor.putBoolean("start_without_recreate", false)
        } else {
            Toast.makeText(context, getString(R.string.attention_more_than_4_operators), Toast.LENGTH_LONG).show()
        }
        editor.apply()
    }

    private fun changeDivision(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val newValue = !binding.cbDivision.isChecked
        if (checkOperatorsAmount() > 4 || newValue) {
            binding.cbDivision.isChecked = newValue
            editor.putBoolean("use_divide", newValue)
            editor.putBoolean("start_without_recreate", false)
        } else {
            Toast.makeText(context, getString(R.string.attention_more_than_4_operators), Toast.LENGTH_LONG).show()
        }
        editor.apply()
    }

    private fun changePowerOf(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val newValue = !binding.cbPowerOf.isChecked
        if (checkOperatorsAmount() > 4 || newValue) {
            binding.cbPowerOf.isChecked = newValue
            editor.putBoolean("use_power_of", newValue)
            editor.putBoolean("start_without_recreate", false)
        } else {
            Toast.makeText(context, getString(R.string.attention_more_than_4_operators), Toast.LENGTH_LONG).show()
        }
        editor.apply()
    }

    private fun changeMod(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val newValue = !binding.cbMod.isChecked
        if (checkOperatorsAmount() > 4 || newValue) {
            binding.cbMod.isChecked = newValue
            editor.putBoolean("use_mod", newValue)
            editor.putBoolean("start_without_recreate", false)
        } else {
            Toast.makeText(context, getString(R.string.attention_more_than_4_operators), Toast.LENGTH_LONG).show()
        }
        editor.apply()
    }
}