const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
console.log('API_BASE_URL configured as:', API_BASE_URL)

// ============ Types ============

export interface User {
    id: string
    name: string
    email: string
}

export interface Venue {
    name: string
    address: string
    city: string
    state: string
    zipCode: string
    capacity?: number
}

export interface Event {
    id: string
    name: string
    description: string
    partnerId: string
    venue: Venue
    status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'FINISHED'
    startDate: string
    endDate: string
    imageUrl?: string
    createdAt: string
    publishedAt?: string
}

export interface Partner {
    id: string
    userId: number
    companyName: string
    tradeName?: string
    document: string
    documentType: string
    email: string
    phone: string
    status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED'
    rejectionReason?: string
    createdAt: string
    approvedAt?: string
}

export interface TicketType {
    id: string
    eventId: string
    name: string
    description: string
    price: number
    quantity: number
    availableQuantity: number
    active: boolean
}

export interface Order {
    id: string
    customerId: string
    reservationId: string
    status: 'PENDING' | 'PAID' | 'CANCELLED' | 'REFUNDED'
    totalAmount: number
    paymentId?: string
    createdAt: string
    paidAt?: string
}

export interface IssuedTicket {
    id: string
    orderId: string
    ticketTypeId: string
    code: string
    status: 'VALID' | 'USED' | 'CANCELLED'
    usedAt?: string
}

export interface Reservation {
    id: string
    customerId: string
    eventId: string
    status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'CONVERTED'
    items: ReservationItem[]
    totalAmount: number
    expiresAt: string
    createdAt: string
}

export interface ReservationItem {
    ticketTypeId: string
    quantity: number
    unitPrice: number
    subtotal: number
}

// ============ Request Types ============

export interface VenueRequest {
    name: string
    address: string
    city: string
    state: string
    zipCode: string
    capacity?: number
}

export interface CreateEventRequest {
    name: string
    description: string
    venue: VenueRequest
    startDate: string
    endDate: string
    imageUrl?: string
}

export interface UpdateEventRequest {
    name?: string
    description?: string
    venue?: VenueRequest
    startDate?: string
    endDate?: string
    imageUrl?: string
}

export interface CreatePartnerRequest {
    companyName: string
    tradeName?: string
    document: string
    documentType?: string
    email: string
    phone: string
}

export interface UpdatePartnerRequest {
    companyName?: string
    tradeName?: string
    email?: string
    phone?: string
}

export interface CreateTicketTypeRequest {
    eventId: string
    name: string
    description: string
    price: number
    quantity: number
}

export interface UpdateTicketTypeRequest {
    name?: string
    description?: string
    price?: number
    quantity?: number
}

export interface CreateReservationRequest {
    customerId: string
    eventId: string
    items: { ticketTypeId: string; quantity: number }[]
}

export interface CreateOrderRequest {
    customerId: string
    reservationId: string
}

export interface ProcessPaymentRequest {
    paymentMethod: string
    paymentDetails?: Record<string, string>
}

class ApiClient {
    private baseUrl: string

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl
    }

    private async request<T>(
        endpoint: string,
        options: RequestInit = {}
    ): Promise<T> {
        const url = `${this.baseUrl}${endpoint}`
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
        })

        if (!response.ok) {
            const error = await response.json().catch(() => ({ error: 'Unknown error' }))
            throw new Error(error.error || error.message || `HTTP error! status: ${response.status}`)
        }

        return response.json()
    }

    // Users
    async getUsers(): Promise<User[]> {
        return this.request<User[]>('/api/users/all')
    }

    async getUserByEmail(email: string): Promise<User | null> {
        try {
            return await this.request<User>(`/api/users?email=${encodeURIComponent(email)}`)
        } catch {
            return null
        }
    }

    async createUser(data: { name: string; email: string }): Promise<User> {
        return this.request<User>('/api/users', {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    // Events
    async getEvents(): Promise<Event[]> {
        return this.request<Event[]>('/api/events')
    }

    async getEvent(id: string): Promise<Event> {
        return this.request<Event>(`/api/events/${id}`)
    }

    async createEvent(partnerId: string, data: CreateEventRequest): Promise<Event> {
        return this.request<Event>('/api/events', {
            method: 'POST',
            headers: { 'X-Partner-Id': partnerId },
            body: JSON.stringify(data),
        })
    }

    async updateEvent(partnerId: string, id: string, data: UpdateEventRequest): Promise<Event> {
        return this.request<Event>(`/api/events/${id}`, {
            method: 'PUT',
            headers: { 'X-Partner-Id': partnerId },
            body: JSON.stringify(data),
        })
    }

    async publishEvent(partnerId: string, id: string): Promise<Event> {
        return this.request<Event>(`/api/events/${id}/publish`, {
            method: 'POST',
            headers: { 'X-Partner-Id': partnerId },
        })
    }

    async cancelEvent(partnerId: string, id: string): Promise<Event> {
        return this.request<Event>(`/api/events/${id}/cancel`, {
            method: 'POST',
            headers: { 'X-Partner-Id': partnerId },
        })
    }

    async finishEvent(partnerId: string, id: string): Promise<Event> {
        return this.request<Event>(`/api/events/${id}/finish`, {
            method: 'POST',
            headers: { 'X-Partner-Id': partnerId },
        })
    }

    // Partners
    async getPartners(): Promise<Partner[]> {
        return this.request<Partner[]>('/api/partners')
    }

    async getPartner(id: string): Promise<Partner> {
        return this.request<Partner>(`/api/partners/${id}`)
    }

    async createPartner(data: CreatePartnerRequest): Promise<Partner> {
        return this.request<Partner>('/api/partners', {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    async updatePartner(id: string, data: UpdatePartnerRequest): Promise<Partner> {
        return this.request<Partner>(`/api/partners/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        })
    }

    async approvePartner(id: string): Promise<Partner> {
        return this.request<Partner>(`/api/partners/${id}/approve`, { method: 'POST' })
    }

    async rejectPartner(id: string): Promise<Partner> {
        return this.request<Partner>(`/api/partners/${id}/reject`, { method: 'POST' })
    }

    async suspendPartner(id: string): Promise<Partner> {
        return this.request<Partner>(`/api/partners/${id}/suspend`, { method: 'POST' })
    }

    async reactivatePartner(id: string): Promise<Partner> {
        return this.request<Partner>(`/api/partners/${id}/reactivate`, { method: 'POST' })
    }

    // Ticket Types
    async getTicketTypesByEvent(eventId: string): Promise<TicketType[]> {
        return this.request<TicketType[]>(`/api/ticket-types/event/${eventId}`)
    }

    async getTicketType(id: string): Promise<TicketType> {
        return this.request<TicketType>(`/api/ticket-types/${id}`)
    }

    async createTicketType(data: Partial<TicketType>): Promise<TicketType> {
        return this.request<TicketType>('/api/ticket-types', {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    async updateTicketType(id: string, data: Partial<TicketType>): Promise<TicketType> {
        return this.request<TicketType>(`/api/ticket-types/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        })
    }

    async deactivateTicketType(id: string): Promise<void> {
        await this.request(`/api/ticket-types/${id}`, { method: 'DELETE' })
    }

    // Orders
    async createOrder(data: CreateOrderRequest): Promise<Order> {
        return this.request<Order>('/api/orders', {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    async getOrder(id: string): Promise<Order> {
        return this.request<Order>(`/api/orders/${id}`)
    }

    async getMyOrders(customerId: string): Promise<Order[]> {
        return this.request<Order[]>('/api/orders/me', {
            headers: { 'X-Customer-Id': customerId },
        })
    }

    async processPayment(orderId: string, data: ProcessPaymentRequest): Promise<Order> {
        return this.request<Order>(`/api/orders/${orderId}/pay`, {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    async refundOrder(orderId: string): Promise<Order> {
        return this.request<Order>(`/api/orders/${orderId}/refund`, { method: 'POST' })
    }

    async getOrderTickets(orderId: string): Promise<IssuedTicket[]> {
        return this.request<IssuedTicket[]>(`/api/orders/${orderId}/tickets`)
    }

    // Issued Tickets
    async getTicketByCode(code: string): Promise<IssuedTicket> {
        return this.request<IssuedTicket>(`/api/tickets/${code}`)
    }

    async validateTicket(code: string): Promise<IssuedTicket> {
        return this.request<IssuedTicket>(`/api/tickets/${code}/validate`, { method: 'POST' })
    }

    // Reservations
    async createReservation(data: CreateReservationRequest): Promise<Reservation> {
        return this.request<Reservation>('/api/reservations', {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    async getReservation(id: string): Promise<Reservation> {
        return this.request<Reservation>(`/api/reservations/${id}`)
    }

    async getMyReservations(customerId: string): Promise<Reservation[]> {
        return this.request<Reservation[]>('/api/reservations/me', {
            headers: { 'X-Customer-Id': customerId },
        })
    }

    async getEventReservations(eventId: string): Promise<Reservation[]> {
        return this.request<Reservation[]>(`/api/reservations/event/${eventId}`)
    }

    async cancelReservation(id: string): Promise<Reservation> {
        return this.request<Reservation>(`/api/reservations/${id}/cancel`, { method: 'POST' })
    }

    async convertReservation(id: string): Promise<Reservation> {
        return this.request<Reservation>(`/api/reservations/${id}/convert`, { method: 'POST' })
    }
}

export const api = new ApiClient(API_BASE_URL)
