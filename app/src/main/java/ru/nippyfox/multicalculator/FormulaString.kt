package ru.nippyfox.multicalculator

class FormulaString {
    private var value: String? = null
    private var height: Int? = null

    constructor() {
        this.value = ""
        this.height = 0
    }

    constructor(height: Int) {
        this.value = ""
        this.height = height
    }

    constructor(height: Int, value: String) {
        this.value = ""
        this.height = height
    }

    fun setValue(value: String) {
        this.value = value
    }

    fun addValue(newLine: String) {
        this.value += newLine
        val nowValue = this.getValue()
        if (countNewLines(nowValue) >= getHeight()) {
            setValue(nowValue.substringAfter('\n'))
        }
    }

    fun getValue(): String {
        return value!!
    }

    fun getHeight(): Int {
        return height!!
    }

    fun resetFormula() {
        setValue("")
    }

    private fun countNewLines(s: String): Int {
        return s.count { it == '\n' }
    }
}