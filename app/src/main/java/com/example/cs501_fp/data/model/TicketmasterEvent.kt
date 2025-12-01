package com.example.cs501_fp.data.model

data class TicketmasterResponse(
    val _embedded: EmbeddedEvents? = null
)

data class EmbeddedEvents(
    val events: List<TicketmasterEvent>? = null
)

data class TicketmasterEvent(
    val id: String? = null,
    val name: String? = null,
    val url: String? = null,
    val info: String? = null,
    val images: List<TicketmasterImage>? = null,
    val dates: TicketmasterDates? = null,
    val _embedded: EmbeddedVenue? = null
)

data class TicketmasterImage(
    val url: String? = null
)

data class TicketmasterDates(
    val start: TicketmasterStartDate? = null
)

data class TicketmasterStartDate(
    val localDate: String? = null,
    val localTime: String? = null
)

data class EmbeddedVenue(
    val venues: List<TicketmasterVenue>? = null
)

data class TicketmasterVenue(
    val name: String? = null,
    val city: TicketmasterCity? = null,
    val country: TicketmasterCountry? = null
)

data class TicketmasterCity(
    val name: String? = null
)

data class TicketmasterCountry(
    val name: String? = null
)