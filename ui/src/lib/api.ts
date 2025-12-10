const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export interface User {
    id: string
    name: string
    email: string
}

export interface Event {
    id: string
    name: string
    description: string
    partnerId: string
    status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'FINISHED'
    startDate: string
    endDate: string
    location: string
}

export interface Partner {
    id: string
    name: string
    email: string
    document: string
    status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED'
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
            throw new Error(error.error || `HTTP error! status: ${response.status}`)
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

    async createEvent(data: Partial<Event>): Promise<Event> {
        return this.request<Event>('/api/events', {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    async updateEvent(id: string, data: Partial<Event>): Promise<Event> {
        return this.request<Event>(`/api/events/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        })
    }

    async publishEvent(id: string): Promise<Event> {
        return this.request<Event>(`/api/events/${id}/publish`, { method: 'POST' })
    }

    async cancelEvent(id: string): Promise<Event> {
        return this.request<Event>(`/api/events/${id}/cancel`, { method: 'POST' })
    }

    async finishEvent(id: string): Promise<Event> {
        return this.request<Event>(`/api/events/${id}/finish`, { method: 'POST' })
    }

    // Partners
    async getPartners(): Promise<Partner[]> {
        return this.request<Partner[]>('/api/partners')
    }

    async getPartner(id: string): Promise<Partner> {
        return this.request<Partner>(`/api/partners/${id}`)
    }

    async createPartner(data: Partial<Partner>): Promise<Partner> {
        return this.request<Partner>('/api/partners', {
            method: 'POST',
            body: JSON.stringify(data),
        })
    }

    async updatePartner(id: string, data: Partial<Partner>): Promise<Partner> {
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
}

export const api = new ApiClient(API_BASE_URL)
