package ru.nippyfox.multicalculator

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.button.MaterialButton
import ru.nippyfox.multicalculator.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector

    private var firstNumber = ""
    private var currentNumber = ""
    private var currentOperator = ""
    private var result = ""

    private lateinit var formula: FormulaString
    private var formulaSize: Int? = null

    private var x1 = 0.0f
    private var y1 = 0.0f
    private var x2 = 0.0f
    private var y2 = 0.0f

    private val nonsenses = listOf(
        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09"
    )

    companion object {
        const val MIN_DISTANCE = 150
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.tvFormula.post {
            formulaSize = binding.tvFormula.height / binding.tvFormula.lineHeight // сколько строк сможет уместиться в tvFormula
            Log.d("formulaSize", formulaSize.toString())
            formula = FormulaString(formulaSize!!, "")
        }

        setContentView(binding.root)
        gestureDetector = GestureDetector(this, this)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.tvFormula.setOnLongClickListener {
            resetAll()
            true
        }

        binding.tvResult.setOnLongClickListener {
            resetAll()
            true
        }

        binding.btnResult.setOnClickListener() {
            if (currentNumber.isEmpty() && currentOperator.isEmpty()) {
                firstNumber =
                    "${firstNumber.toDouble()}" // для того, чтобы форматировать число в приемлимый для пользователя вид
                if (firstNumber.takeLast(2) == ".0") {
                    firstNumber = firstNumber.dropLast(2)
                }
                binding.tvResult.text = firstNumber
            } else {
                if (currentNumber.isEmpty()) {
                    currentNumber = "0"
                } else {
                    currentNumber = "${currentNumber.toDouble()}"
                    if (currentNumber.takeLast(2) == ".0") {
                        currentNumber = currentNumber.dropLast(2)
                    }
                }
                if (formula.getValue().isEmpty()) { // если история пуста
                    formula.setValue("$firstNumber$currentOperator$currentNumber")
                } else if (checkOperatorsInLastFormulaLine(formula.getValue())) { // если в последней строке истории уже содержится оператор
                    formula.addValue("\n=$result$currentOperator$currentNumber")
                } else {
                    formula.addValue("$currentOperator$currentNumber")
                }
                binding.tvFormula.text = formula.getValue()
                result = calculate(firstNumber, currentNumber, currentOperator)
                firstNumber = result
                binding.tvResult.text = result
            }
        }

        binding.btnDot.setOnClickListener() {
            if (currentOperator.isEmpty()) {
                if (!firstNumber.contains(".")) {
                    firstNumber += if (firstNumber.isEmpty()) "0."
                    else "."
                    binding.tvResult.text = firstNumber
                }
            } else {
                if (result.isNotEmpty()) {
                    resetAll()
                    firstNumber = "0."
                    binding.tvResult.text = "0."
                } else if (!currentNumber.contains(".")) {
                    currentNumber += if (currentNumber.isEmpty()) "0."
                    else "."
                    binding.tvResult.text = currentNumber
                }
            }
        }

        val buttons = listOf(
            binding.btn0,
            binding.btn1,
            binding.btn2,
            binding.btn3,
            binding.btn4,
            binding.btn5,
            binding.btn6,
            binding.btn7,
            binding.btn8,
            binding.btn9
        )
        buttons.forEach { button ->
            button.setOnClickListener {
                val buttonText = button.text.toString()
                if (result.isNotEmpty()) {
                    formula.addValue("\n=$result")
                    binding.tvFormula.text = formula.getValue()
                    result = ""
                }
                if (currentOperator.isEmpty()) {
                    firstNumber += buttonText
                    if (nonsenses.any { firstNumber == it }) {
                        firstNumber = firstNumber.drop(1)
                    }
                    binding.tvResult.text = firstNumber
                } else {
                    currentNumber += buttonText
                    if (nonsenses.any { currentNumber == it }) {
                        currentNumber = currentNumber.drop(1)
                    }
                    binding.tvResult.text = currentNumber
                }
            }
        }

        val operatorButtons =
            listOf(binding.btnAdd, binding.btnSubtract, binding.btnMultiply, binding.btnDivide)
        operatorButtons.forEach { operatorButton ->
            operatorButton.setOnClickListener {
                firstNumber =
                    firstNumber.takeUnless { it.takeLast(1) == "." } ?: firstNumber.dropLast(1)
                currentNumber =
                    currentNumber.takeUnless { it.takeLast(1) == "." } ?: currentNumber.dropLast(1)
                firstNumber = "${firstNumber.toDouble()}"
                if (firstNumber.takeLast(2) == ".0") {
                    firstNumber = firstNumber.dropLast(2)
                }
                if (currentNumber.isNotEmpty()) {
                    currentNumber = "${currentNumber.toDouble()}"
                    if (currentNumber.takeLast(2) == ".0") {
                        currentNumber = currentNumber.dropLast(2)
                    }
                }
                binding.tvResult.text = firstNumber
                val buttonText = operatorButton.text.toString()
                if (firstNumber.isEmpty() && currentOperator.isEmpty() && currentNumber.isEmpty()) {
                    firstNumber = "0"
                    formula.setValue(firstNumber)
                    binding.tvFormula.text = formula.getValue()
                    currentOperator = buttonText
                    binding.tvResult.text = "0"
                } else if (currentOperator.isEmpty() && currentNumber.isEmpty() && result.isEmpty()) {
                    formula.setValue(firstNumber)
                    binding.tvFormula.text = formula.getValue()
                    currentOperator = buttonText
                    binding.tvResult.text = "0"
                } else if (result.isEmpty()) {
                    formula.addValue("$currentOperator$currentNumber")
                    binding.tvFormula.text = formula.getValue()
                    result = calculate(firstNumber, currentNumber, currentOperator)
                    firstNumber = result
                    binding.tvResult.text = result
                    currentOperator = buttonText
                    currentNumber = ""
                } else {
                    currentNumber = ""
                    if (binding.tvResult.text.toString().isNotEmpty()) {
                        currentOperator = buttonText
                        binding.tvResult.text = "0"
                    }
                }
            }
        }
    }

    private fun calculate(firstNumber: String, secondNumber: String, operator: String): String {
        val num1 = firstNumber.toDouble()
        val num2 = secondNumber.toDouble()
        if (num2 == 0.0 && operator == "÷") {
            Toast.makeText(this, "Can't divide by 0", Toast.LENGTH_SHORT).show()
            formula.resetFormula()
            binding.tvFormula.text = formula.getValue()
            return "0"
        }
        return when (operator) {
            "+" -> cleanClearResult(
                (num1 + num2).toBigDecimal().setScale(8, RoundingMode.HALF_UP).toDouble().toString()
            )

            "-" -> cleanClearResult(
                (num1 - num2).toBigDecimal().setScale(8, RoundingMode.HALF_UP).toDouble().toString()
            )

            "×" -> cleanClearResult(
                (num1 * num2).toBigDecimal().setScale(8, RoundingMode.HALF_UP).toDouble().toString()
            )

            "÷" -> cleanClearResult(
                (num1 / num2).toBigDecimal().setScale(8, RoundingMode.HALF_UP).toDouble().toString()
            )

            else -> "0"
        }
    }

    private fun cleanClearResult(result: String): String {
        return if (result.takeLast(2) == ".0") {
            result.dropLast(2)
        } else {
            result
        }
    }

    private fun resetAll() {
        firstNumber = ""
        currentNumber = ""
        currentOperator = ""
        result = ""
        formula.resetFormula()
        binding.tvResult.text = "0"
        binding.tvFormula.text = ""
    }

    private fun checkOperatorsInLastFormulaLine(initialString: String): Boolean {
        val mathSymbols = setOf('+', '-', '×', '÷')
        return initialString.substringAfterLast('\n').any { char -> char in mathSymbols }
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return false
    }

    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return false
    }
}