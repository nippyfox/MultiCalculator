package ru.nippyfox.multicalculator

class FormulaString {
    private var value: String? = null
    private var height: Int? = null

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
        val nowValue = removeRedundantLines(this.getValue())
        if (countNewLines(nowValue) >= getHeight()) {
            setValue(nowValue.substringAfter('\n'))
        } else {
            setValue(nowValue)
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

    private fun removeRedundantLines(input: String): String {
        val arrayOfSym: Array<Char> = arrayOf('+', '-', '×', '÷', '^', ' ')
        val lines = input.lines()
        val lastLine = lines.last()

        val filteredLines = lines.dropLast(1).filter { line ->
            !arrayOfSym.any { symbol ->
                line.lastOrNull() == symbol
            }
        }

        return (filteredLines + lastLine).joinToString("\n")
    }
}