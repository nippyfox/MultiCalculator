package ru.nippyfox.multicalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.nippyfox.multicalculator.databinding.ActivityMainBinding
import java.math.RoundingMode
import kotlin.math.abs

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector

    private var firstNumber = ""
    private var currentNumber = ""
    private var currentOperator = ""
    private var result = ""

    private var prevCurrentNumber = ""

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
        const val MIN_DISTANCE = 100
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
                if (firstNumber.isNotEmpty()) {
                    firstNumber = "${firstNumber.toDouble()}" // для того, чтобы форматировать число в приемлимый для пользователя вид
                    if (firstNumber.endsWith(".0")) firstNumber = firstNumber.dropLast(2)
                    binding.tvResult.text = firstNumber
                }
            } else {
                if (prevCurrentNumber.isNotEmpty()) { // последовательное нажатие на "="
                    currentNumber = "${prevCurrentNumber.toDouble()}"
                    if (currentNumber.endsWith(".0")) currentNumber = currentNumber.dropLast(2)
                    formula.addValue("\n=$result$currentOperator$currentNumber")
                    binding.tvFormula.text = formula.getValue()
                    result = calculate(firstNumber, currentNumber, currentOperator)
                    firstNumber = result
                    prevCurrentNumber = currentNumber
                    currentNumber = ""
                    binding.tvResult.text = "=$result"
                } else { // обычное нажатие на "=" после ввода операции
                    if (currentNumber.isEmpty()) {
                        currentNumber = "0"
                    } else {
                        currentNumber = "${currentNumber.toDouble()}"
                        if (currentNumber.endsWith(".0")) currentNumber = currentNumber.dropLast(2)
                    }
                    formula.addValue(currentNumber)
                    binding.tvFormula.text = formula.getValue()
                    result = calculate(firstNumber, currentNumber, currentOperator)
                    firstNumber = result
                    prevCurrentNumber = currentNumber
                    currentNumber = ""
                    binding.tvResult.text = "=$result"
                }
            }
        }

        binding.btnDot.setOnClickListener() {
            prevCurrentNumber = ""
            if (currentOperator.isEmpty()) {
                if (!firstNumber.contains(".")) {
                    firstNumber += if (firstNumber.isEmpty()) "0." else "."
                    binding.tvResult.text = firstNumber
                }
            } else if (binding.tvResult.text.startsWith("=")) {
                firstNumber += "."
                currentOperator = ""
                result = ""
                binding.tvResult.text = firstNumber
                formula.setValue("")
                binding.tvFormula.text = formula.getValue()
            } else if (!currentNumber.contains(".")) {
                currentNumber += if (currentNumber.isEmpty()) "0."
                else "."
                binding.tvResult.text = currentNumber
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
                prevCurrentNumber = ""
                val buttonText = button.text.toString()
                if (currentOperator.isEmpty()) {
                    if (firstNumber.length < 9) firstNumber += buttonText
                    if (nonsenses.any { firstNumber == it }) {
                        firstNumber = firstNumber.drop(1)
                    }
                    binding.tvResult.text = firstNumber
                } else {
                    if (currentNumber.length < 9) currentNumber += buttonText
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
                prevCurrentNumber = ""
                firstNumber =
                    firstNumber.takeUnless { it.endsWith(".") } ?: firstNumber.dropLast(1)
                currentNumber =
                    currentNumber.takeUnless { it.endsWith(".") } ?: currentNumber.dropLast(1)
                if (firstNumber.isNotEmpty()) {
                    firstNumber = "${firstNumber.toDouble()}"
                    if (firstNumber.endsWith(".0")) firstNumber = firstNumber.dropLast(2)
                }
                if (currentNumber.isNotEmpty()) {
                    currentNumber = "${currentNumber.toDouble()}"
                    if (currentNumber.endsWith(".0")) currentNumber = currentNumber.dropLast(2)
                }
                val buttonText = operatorButton.text.toString()
                if (firstNumber.isEmpty() && currentOperator.isEmpty() && currentNumber.isEmpty()) { // если пользователь сразу нажимает на оператор
                    firstNumber = "0"
                    currentOperator = buttonText
                    formula.setValue("$firstNumber$currentOperator")
                    binding.tvFormula.text = formula.getValue()
                    binding.tvResult.text = "0"
                } else if (currentNumber.isEmpty() && result.isEmpty()) { // после ввода первого числа
                    currentOperator = buttonText
                    formula.setValue("$firstNumber$currentOperator")
                    binding.tvFormula.text = formula.getValue()
                    binding.tvResult.text = "0"
                } else if (result.isEmpty()) { // последовательные операции без нажатия на равно
                    result = calculate(firstNumber, currentNumber, currentOperator)
                    firstNumber = result
                    binding.tvResult.text = "0"
                    currentOperator = buttonText
                    formula.addValue("$currentNumber\n=$result$currentOperator")
                    binding.tvFormula.text = formula.getValue()
                    currentNumber = ""
                    result = ""
                } else { // следующее вычисление после нажатия на равно
                    currentOperator = buttonText
                    formula.addValue("\n=$firstNumber$currentOperator")
                    binding.tvFormula.text = formula.getValue()
                    currentNumber = ""
                    binding.tvResult.text = "0"
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
        prevCurrentNumber = ""
        formula.resetFormula()
        binding.tvResult.text = "0"
        binding.tvFormula.text = ""
    }

    private fun deleteLastSymbol() {
        prevCurrentNumber = ""
        if (currentOperator.isEmpty() && currentNumber.isEmpty() && firstNumber.isNotEmpty()) {
            firstNumber = firstNumber.dropLast(1)
            if (firstNumber.isEmpty()) {
                binding.tvResult.text = "0"
            } else {
                binding.tvResult.text = firstNumber
            }
        } else if (currentNumber.isNotEmpty()) {
            currentNumber = currentNumber.dropLast(1)
            if (currentNumber.isEmpty()) {
                binding.tvResult.text = "0"
            } else {
                binding.tvResult.text = currentNumber
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event!!)
        when (event.action) {
            0 -> {
                x1 = event.x
                y1 = event.y
            }
            1 -> {
                x2 = event.x
                y2 = event.y

                val valueX = x2 - x1

                if (abs(valueX) > MIN_DISTANCE) {
                    if (x2 < x1) deleteLastSymbol()
                }
            }
        }
        return super.onTouchEvent(event)
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