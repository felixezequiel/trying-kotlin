# BFF DTO Mismatches - Integration Fixes

## Overview
This document documents the critical DTO mismatches found between the BFF and backend services that were blocking the ticket creation workflow.

## Issues Found and Fixed

### 1. PartnersClient - DocumentType Enum Mismatch
**Problem**: BFF was sending `documentType: String` but Partners service expected `DocumentType` enum
**Fix**: Added shared `DocumentType` enum to BFF's PartnersClient.kt
```kotlin
@Serializable
enum class DocumentType {
    CPF,
    CNPJ
}
```

### 2. TicketsClient - Multiple Field Mismatches
**Problem**: BFF DTOs didn't match Tickets service structure
**Fixes**:
- Changed `price: Double` to `price: String` (BigDecimal serialization)
- Changed `quantity: Int` to `totalQuantity: Int`
- Added `maxPerCustomer: Int` field
- Added optional `salesStartDate` and `salesEndDate` fields
- Added `availableQuantity: Int` to response DTO
- Changed `status: String` to match service enum format

### 3. OrdersClient - Extra Field in Request
**Problem**: BFF was sending `customerId` in CreateOrderRequest but service didn't expect it
**Fix**: Removed `customerId` field from CreateOrderTypeRequest

### 4. ReservationsClient - Extra Field in Request  
**Problem**: BFF was sending `customerId` in CreateReservationRequest but service didn't expect it
**Fix**: Removed `customerId` field from CreateReservationRequest

### 5. TicketsRoutes - Missing Header Forwarding
**Problem**: BFF wasn't forwarding `X-Partner-Id` header to Tickets service
**Fix**: Updated route to extract partnerId and pass to TicketsClient:
```kotlin
val partnerId = call.request.headers["X-Partner-Id"]
val ticketType = ticketsClient.createTicketType(partnerId, request)
```

### 6. Character Encoding Issue
**Problem**: Special characters like "ã" in "São Paulo" caused JSON deserialization failures
**Fix**: Use ASCII-only text or proper UTF-8 encoding in JSON requests

## Files Modified
- `bff/clients/TicketsClient.kt` - Updated all DTOs and header forwarding
- `bff/clients/PartnersClient.kt` - Added DocumentType enum
- `bff/clients/OrdersClient.kt` - Fixed CreateOrderRequest
- `bff/clients/ReservationsClient.kt` - Fixed CreateReservationRequest  
- `bff/routes/TicketsRoutes.kt` - Added partnerId header forwarding
- `bff/Application.kt` - Fixed CORS headers (previous fix)

## Verification
- Partner creation: ✅ Working
- Event creation: ✅ Working  
- Event publishing: ✅ Working
- **Ticket type creation: ✅ Working (201 Created)**
- End-to-end workflow: ✅ Complete

## Notes
- All BFF clients now properly match their corresponding service DTOs
- Header forwarding is implemented for authentication/authorization
- Error responses now include actual service error messages
- Debug logging removed from production code
