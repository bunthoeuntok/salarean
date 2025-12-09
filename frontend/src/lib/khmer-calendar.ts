/**
 * Khmer Calendar Utilities
 * Provides formatting functions for displaying dates in Khmer language
 * with Buddhist Era (BE) year system
 */

/**
 * Khmer month names (Gregorian calendar months in Khmer)
 */
export const khmerMonths = [
  'មករា',      // January
  'កុម្ភៈ',     // February
  'មីនា',      // March
  'មេសា',      // April
  'ឧសភា',      // May
  'មិថុនា',     // June
  'កក្កដា',     // July
  'សីហា',      // August
  'កញ្ញា',     // September
  'តុលា',      // October
  'វិច្ឆិកា',   // November
  'ធ្នូ',       // December
]

/**
 * Short Khmer month names
 */
export const khmerMonthsShort = [
  'មក.',   // Jan
  'កុ.',   // Feb
  'មី.',   // Mar
  'មេ.',   // Apr
  'ឧស.',  // May
  'មិ.',   // Jun
  'កក.',  // Jul
  'សី.',   // Aug
  'កញ.',  // Sep
  'តុ.',   // Oct
  'វិ.',   // Nov
  'ធ្នូ',  // Dec
]

/**
 * Khmer day names (short version for calendar headers)
 */
export const khmerDaysShort = [
  'អា',    // Sunday
  'ច',     // Monday
  'អ',     // Tuesday
  'ព',     // Wednesday
  'ព្រ',   // Thursday
  'សុ',    // Friday
  'ស',     // Saturday
]

/**
 * Full Khmer day names
 */
export const khmerDaysFull = [
  'អាទិត្យ',        // Sunday
  'ចន្ទ',           // Monday
  'អង្គារ',         // Tuesday
  'ពុធ',            // Wednesday
  'ព្រហស្បតិ៍',     // Thursday
  'សុក្រ',          // Friday
  'សៅរ៍',          // Saturday
]

/**
 * Converts a number to Khmer numerals
 * Example: 123 -> ១២៣
 */
export const toKhmerNumeral = (num: number): string => {
  const khmerDigits = ['០', '១', '២', '៣', '៤', '៥', '៦', '៧', '៨', '៩']
  return num.toString().split('').map(d => khmerDigits[parseInt(d)]).join('')
}

/**
 * Converts Gregorian year to Buddhist Era (BE) year
 * Buddhist Era = Gregorian Year + 543
 * Example: 2024 -> 2567
 */
export const toBuddhistYear = (gregorianYear: number): number => {
  return gregorianYear + 543
}

/**
 * Formats a date in Khmer format with Buddhist Era
 * Example: "២៥ មករា ២៥៦៧" (25 January 2567 BE)
 */
export const formatKhmerDate = (date: Date, useKhmerNumerals = true): string => {
  const day = useKhmerNumerals ? toKhmerNumeral(date.getDate()) : date.getDate()
  const month = khmerMonths[date.getMonth()]
  const year = useKhmerNumerals
    ? toKhmerNumeral(toBuddhistYear(date.getFullYear()))
    : toBuddhistYear(date.getFullYear())

  return `${day} ${month} ${year}`
}

/**
 * Formats a date in short Khmer format
 * Example: "២៥/០១/២៥៦៧" (25/01/2567 BE)
 */
export const formatKhmerDateShort = (date: Date, useKhmerNumerals = true): string => {
  const day = date.getDate().toString().padStart(2, '0')
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const year = toBuddhistYear(date.getFullYear()).toString()

  const formatted = `${day}/${month}/${year}`

  return useKhmerNumerals ? toKhmerNumeral(parseInt(formatted.replace(/\//g, '')))
    .split('')
    .reduce((acc, char, i) => {
      if (i === 2 || i === 4) return acc + '/' + char
      return acc + char
    }, '') : formatted
}

/**
 * Formats month and year for calendar caption
 * Example: "មករា ២៥៦៧" (January 2567 BE)
 */
export const formatKhmerMonthYear = (date: Date, useKhmerNumerals = true): string => {
  const month = khmerMonths[date.getMonth()]
  const year = useKhmerNumerals
    ? toKhmerNumeral(toBuddhistYear(date.getFullYear()))
    : toBuddhistYear(date.getFullYear())

  return `${month} ${year}`
}

/**
 * Formats month and year for dropdown
 * Example: "មក. ២៥៦៧"
 */
export const formatKhmerMonthYearShort = (date: Date, useKhmerNumerals = true): string => {
  const month = khmerMonthsShort[date.getMonth()]
  const year = useKhmerNumerals
    ? toKhmerNumeral(toBuddhistYear(date.getFullYear()))
    : toBuddhistYear(date.getFullYear())

  return `${month} ${year}`
}

/**
 * Gets the Khmer day name for a given date
 */
export const getKhmerDayName = (date: Date, short = true): string => {
  return short ? khmerDaysShort[date.getDay()] : khmerDaysFull[date.getDay()]
}

/**
 * Parses a Khmer numeral string to a JavaScript number
 * Example: "១២៣" -> 123
 */
export const fromKhmerNumeral = (khmerNum: string): number => {
  const arabicDigits = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9']
  const khmerDigits = ['០', '១', '២', '៣', '៤', '៥', '៦', '៧', '៨', '៩']

  const arabicString = khmerNum.split('').map(char => {
    const index = khmerDigits.indexOf(char)
    return index !== -1 ? arabicDigits[index] : char
  }).join('')

  return parseInt(arabicString)
}

/**
 * Formats a date for display in input fields
 * Format: dd-MMMM-yyyy (e.g., "25 January 2024" or "២៥ មករា ២០២៤")
 *
 * @param date - The date to format
 * @param language - Language code ('en' or 'km')
 * @param useKhmerNumerals - Whether to use Khmer numerals for numbers (default: true for Khmer)
 */
export const formatDateDisplay = (
  date: Date,
  language: 'en' | 'km',
  useKhmerNumerals?: boolean
): string => {
  if (language === 'km') {
    const day = useKhmerNumerals !== false ? toKhmerNumeral(date.getDate()) : date.getDate()
    const month = khmerMonths[date.getMonth()]
    const year = useKhmerNumerals !== false ? toKhmerNumeral(date.getFullYear()) : date.getFullYear()
    return `${day} ${month} ${year}`
  }

  // English format
  const day = date.getDate()
  const month = date.toLocaleDateString('en-US', { month: 'long' })
  const year = date.getFullYear()
  return `${day} ${month} ${year}`
}
