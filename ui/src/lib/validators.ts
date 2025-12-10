/**
 * Validadores para documentos brasileiros (CPF e CNPJ)
 */

/**
 * Remove caracteres não numéricos de uma string
 */
export const unmask = (value: string): string => value.replace(/\D/g, '')

/**
 * Valida um CPF brasileiro
 * @param cpf - CPF com ou sem máscara
 * @returns true se o CPF é válido
 */
export function isValidCpf(cpf: string): boolean {
    const cleanCpf = unmask(cpf)

    // Deve ter 11 dígitos
    if (cleanCpf.length !== 11) return false

    // Não pode ser sequência de números iguais
    if (/^(\d)\1+$/.test(cleanCpf)) return false

    const digits = cleanCpf.split('').map(Number)

    // Primeiro dígito verificador
    const sum1 = digits.slice(0, 9).reduce((acc, digit, i) => acc + digit * (10 - i), 0)
    const digit1 = sum1 % 11 < 2 ? 0 : 11 - (sum1 % 11)
    if (digits[9] !== digit1) return false

    // Segundo dígito verificador
    const sum2 = digits.slice(0, 10).reduce((acc, digit, i) => acc + digit * (11 - i), 0)
    const digit2 = sum2 % 11 < 2 ? 0 : 11 - (sum2 % 11)
    return digits[10] === digit2
}

/**
 * Valida um CNPJ brasileiro
 * @param cnpj - CNPJ com ou sem máscara
 * @returns true se o CNPJ é válido
 */
export function isValidCnpj(cnpj: string): boolean {
    const cleanCnpj = unmask(cnpj)

    // Deve ter 14 dígitos
    if (cleanCnpj.length !== 14) return false

    // Não pode ser sequência de números iguais
    if (/^(\d)\1+$/.test(cleanCnpj)) return false

    const digits = cleanCnpj.split('').map(Number)
    const weights1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
    const weights2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]

    // Primeiro dígito verificador
    const sum1 = weights1.reduce((acc, weight, i) => acc + digits[i] * weight, 0)
    const digit1 = sum1 % 11 < 2 ? 0 : 11 - (sum1 % 11)
    if (digits[12] !== digit1) return false

    // Segundo dígito verificador
    const sum2 = weights2.reduce((acc, weight, i) => acc + digits[i] * weight, 0)
    const digit2 = sum2 % 11 < 2 ? 0 : 11 - (sum2 % 11)
    return digits[13] === digit2
}

/**
 * Valida um documento (CPF ou CNPJ) baseado no tipo
 * @param document - Documento com ou sem máscara
 * @param type - Tipo do documento ('CPF' ou 'CNPJ')
 * @returns objeto com resultado da validação e mensagem de erro
 */
export function validateDocument(
    document: string,
    type: 'CPF' | 'CNPJ'
): { valid: boolean; error?: string } {
    const cleanDoc = unmask(document)

    if (type === 'CPF') {
        if (cleanDoc.length !== 11) {
            return { valid: false, error: 'CPF deve conter 11 dígitos' }
        }
        if (!isValidCpf(cleanDoc)) {
            return { valid: false, error: 'CPF inválido' }
        }
    } else {
        if (cleanDoc.length !== 14) {
            return { valid: false, error: 'CNPJ deve conter 14 dígitos' }
        }
        if (!isValidCnpj(cleanDoc)) {
            return { valid: false, error: 'CNPJ inválido' }
        }
    }

    return { valid: true }
}
