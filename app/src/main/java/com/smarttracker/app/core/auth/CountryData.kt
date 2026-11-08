package com.smarttracker.app.core.auth

data class Country(
    val code: String,       // e.g. "IN", "US", "GB"
    val name: String,       // e.g. "India", "United States"
    val dialCode: String,   // e.g. "+91", "+1"
    val flagEmoji: String   // Unicode Flag
)

object CountryData {
    val COUNTRIES = listOf(
        Country("IN", "India", "+91", "\uD83C\uDDEE\uD83C\uDDF3"),
        Country("US", "United States", "+1", "\uD83C\uDDFA\uD83C\uDDF8"),
        Country("GB", "United Kingdom", "+44", "\uD83C\uDDEC\uD83C\uDDE7"),
        Country("CA", "Canada", "+1", "\uD83C\uDDE8\uD83C\uDDE6"),
        Country("AE", "United Arab Emirates", "+971", "\uD83C\uDDE6\uD83C\uDDEA"),
        Country("SA", "Saudi Arabia", "+966", "\uD83C\uDDF8\uD83C\uDDE6"),
        Country("SG", "Singapore", "+65", "\uD83C\uDDF8\uD83C\uDDEC"),
        Country("AU", "Australia", "+61", "\uD83C\uDDE6\uD83C\uDDFA"),
        Country("DE", "Germany", "+49", "\uD83C\uDDE9\uD83C\uDDEA"),
        Country("FR", "France", "+33", "\uD83C\uDDEB\uD83C\uDDF7"),
        Country("JP", "Japan", "+81", "\uD83C\uDDEF\uD83C\uDDF5"),
        Country("MY", "Malaysia", "+60", "\uD83C\uDDF2\uD83C\uDDF8"),
        Country("PH", "Philippines", "+63", "\uD83C\uDDF5\uD83C\uDDED"),
        Country("ZA", "South Africa", "+27", "\uD83C\uDDFF\uD83C\uDDE6"),
        Country("BR", "Brazil", "+55", "\uD83C\uDDE7\uD83C\uDDF7"),
        Country("NZ", "New Zealand", "+64", "\uD83C\uDDF3\uD83C\uDDFF"),
        Country("BD", "Bangladesh", "+880", "\uD83C\uDDE7\uD83C\uDDE9"),
        Country("LK", "Sri Lanka", "+94", "\uD83C\uDDF1\uD83C\uDDF0"),
        Country("NP", "Nepal", "+977", "\uD83C\uDDF3\uD83C\uDDF5"),
        Country("QA", "Qatar", "+974", "\uD83C\uDDF6\uD83C\uDDE6"),
        Country("KW", "Kuwait", "+965", "\uD83C\uDDF0\uD83C\uDDFC"),
        Country("OM", "Oman", "+968", "\uD83C\uDDF4\uD83C\uDDF2"),
        Country("ID", "Indonesia", "+62", "\uD83C\uDDE8\uD83C\uDDE9"),
        Country("NG", "Nigeria", "+234", "\uD83C\uDDF3\uD83C\uDDEC"),
        Country("KE", "Kenya", "+254", "\uD83C\uDDF0\uD83C\uDDEA"),
        Country("IT", "Italy", "+39", "\uD83C\uDDE8\uD83C\uDDF9"),
        Country("ES", "Spain", "+34", "\uD83C\uDDEA\uD83C\uDDF8"),
        Country("NL", "Netherlands", "+31", "\uD83C\uDDF3\uD83C\uDDF1"),
        Country("SE", "Sweden", "+46", "\uD83C\uDDF8\uD83C\uDDEA"),
        Country("CH", "Switzerland", "+41", "\uD83C\uDDE8\uD83C\uDDED")
    )
}
