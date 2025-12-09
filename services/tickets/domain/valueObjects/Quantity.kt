package tickets.domain.valueObjects

@JvmInline
value class Quantity private constructor(val value: Int) {

    init {
        require(value >= 0) { "Quantidade não pode ser negativa" }
    }

    companion object {
        val ZERO = Quantity(0)

        fun of(value: Int): Quantity {
            require(value >= 0) { "Quantidade não pode ser negativa" }
            return Quantity(value)
        }

        fun positive(value: Int): Quantity {
            require(value > 0) { "Quantidade deve ser maior que zero" }
            return Quantity(value)
        }

        fun atLeast(value: Int, min: Int): Quantity {
            require(value >= min) { "Quantidade deve ser pelo menos $min" }
            return Quantity(value)
        }
    }

    operator fun plus(other: Quantity): Quantity = Quantity(value + other.value)

    operator fun minus(other: Quantity): Quantity {
        val result = value - other.value
        require(result >= 0) { "Resultado não pode ser negativo" }
        return Quantity(result)
    }

    operator fun compareTo(other: Quantity): Int = value.compareTo(other.value)

    fun isZero(): Boolean = value == 0

    override fun toString(): String = value.toString()
}
