package orders.domain.valueObjects

import java.math.BigDecimal

@JvmInline
value class Price private constructor(val value: BigDecimal) {

    init {
        require(value >= BigDecimal.ZERO) { "Preço deve ser maior ou igual a zero" }
    }

    companion object {
        val ZERO = Price(BigDecimal.ZERO)

        fun of(value: BigDecimal): Price = Price(value)

        fun fromString(value: String): Price {
            val decimal =
                    value.toBigDecimalOrNull()
                            ?: throw IllegalArgumentException("Preço inválido: $value")
            return Price(decimal)
        }
    }

    operator fun plus(other: Price): Price = Price(value + other.value)

    operator fun times(quantity: Int): Price = Price(value * quantity.toBigDecimal())

    operator fun times(quantity: Quantity): Price = Price(value * quantity.value.toBigDecimal())

    override fun toString(): String = value.toString()
}
