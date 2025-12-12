'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Ticket, DollarSign, Package, TrendingUp, RefreshCw, Eye, ShoppingCart } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import { api, TicketType, Event } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'

interface TicketTypeWithEvent extends TicketType {
    eventName: string
}

export default function TicketsPage() {
    const router = useRouter()
    const [tickets, setTickets] = useState<TicketTypeWithEvent[]>([])
    const [loading, setLoading] = useState(true)
    const [reservingTicket, setReservingTicket] = useState<string | null>(null)
    const { toast } = useToast()

    const handleReserveTicket = async (ticketId: string, quantity: number) => {
        if (quantity <= 0) {
            toast({ title: 'Please enter a valid quantity', variant: 'destructive' })
            return
        }

        setReservingTicket(ticketId)
        try {
            await api.reserveTickets({ ticketTypeId: ticketId, quantity })
            toast({ title: 'Tickets reserved successfully' })
            fetchTickets() // Refresh to update available quantities
        } catch (error) {
            toast({ title: 'Failed to reserve tickets', variant: 'destructive' })
        } finally {
            setReservingTicket(null)
        }
    }

    const fetchTickets = async () => {
        try {
            const events = await api.getEvents()
            const allTickets: TicketTypeWithEvent[] = []

            for (const event of events) {
                try {
                    const eventTickets = await api.getTicketTypesByEvent(event.id)
                    allTickets.push(
                        ...eventTickets.map(t => ({
                            ...t,
                            eventName: event.name,
                        }))
                    )
                } catch {
                    // Event might not have ticket types
                }
            }

            setTickets(allTickets)
        } catch (error) {
            console.error('Failed to fetch tickets:', error)
            toast({ title: 'Failed to load tickets', variant: 'destructive' })
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchTickets()
    }, [])

    const totalRevenue = tickets.reduce((acc, t) => acc + (t.totalQuantity - t.availableQuantity) * parseFloat(t.price), 0)
    const totalSold = tickets.reduce((acc, t) => acc + (t.totalQuantity - t.availableQuantity), 0)
    const totalAvailable = tickets.reduce((acc, t) => acc + t.availableQuantity, 0)

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Tickets</h1>
                    <p className="text-muted-foreground">
                        Overview of all ticket types across events
                    </p>
                </div>
                <Button variant="outline" onClick={fetchTickets}>
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Refresh
                </Button>
            </div>

            {/* Stats Cards */}
            <div className="grid gap-4 md:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
                        <DollarSign className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            ${totalRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </div>
                        <p className="text-xs text-muted-foreground">
                            From ticket sales
                        </p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Tickets Sold</CardTitle>
                        <Ticket className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{totalSold}</div>
                        <p className="text-xs text-muted-foreground">
                            Across all events
                        </p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Available</CardTitle>
                        <Package className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{totalAvailable}</div>
                        <p className="text-xs text-muted-foreground">
                            Tickets remaining
                        </p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Sell Rate</CardTitle>
                        <TrendingUp className="h-4 w-4 text-purple-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {Math.round((totalSold / (totalSold + totalAvailable)) * 100)}%
                        </div>
                        <p className="text-xs text-muted-foreground">
                            Overall conversion
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* Tickets Table */}
            <Card>
                <CardHeader>
                    <CardTitle>All Ticket Types</CardTitle>
                    <CardDescription>
                        Manage ticket types for all events
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="space-y-3">
                            {[1, 2, 3].map((i) => (
                                <div key={i} className="h-12 animate-pulse rounded bg-muted" />
                            ))}
                        </div>
                    ) : tickets.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-8 text-center">
                            <Ticket className="h-12 w-12 text-muted-foreground" />
                            <h3 className="mt-4 text-lg font-semibold">No ticket types yet</h3>
                            <p className="text-sm text-muted-foreground">
                                Create ticket types in the event details page
                            </p>
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Event</TableHead>
                                    <TableHead>Ticket Type</TableHead>
                                    <TableHead className="text-right">Price</TableHead>
                                    <TableHead className="text-right">Sold</TableHead>
                                    <TableHead className="text-right">Available</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead className="w-[70px]"></TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {tickets.map((ticket) => (
                                    <TableRow key={ticket.id}>
                                        <TableCell className="font-medium">{ticket.eventName}</TableCell>
                                        <TableCell>{ticket.name}</TableCell>
                                        <TableCell className="text-right">
                                            ${parseFloat(ticket.price).toFixed(2)}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            {ticket.totalQuantity - ticket.availableQuantity}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                <span>{ticket.availableQuantity}</span>
                                                <div className="h-2 w-16 rounded-full bg-secondary">
                                                    <div
                                                        className="h-2 rounded-full bg-primary"
                                                        style={{
                                                            width: `${ticket.totalQuantity > 0 ? (ticket.availableQuantity / ticket.totalQuantity) * 100 : 0}%`,
                                                        }}
                                                    />
                                                </div>
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant={ticket.status === "ACTIVE" ? 'success' : 'secondary'}>
                                                {ticket.status === "ACTIVE" ? 'Active' : 'Inactive'}
                                            </Badge>
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex gap-1">
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => router.push(`/events/${ticket.eventId}`)}
                                                >
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="default"
                                                    size="sm"
                                                    onClick={() => handleReserveTicket(ticket.id, 1)}
                                                    disabled={reservingTicket === ticket.id || ticket.status !== "ACTIVE" || ticket.availableQuantity === 0}
                                                >
                                                    {reservingTicket === ticket.id ? (
                                                        <RefreshCw className="h-4 w-4 animate-spin" />
                                                    ) : (
                                                        <ShoppingCart className="h-4 w-4" />
                                                    )}
                                                    Reserve
                                                </Button>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>
        </div>
    )
}
