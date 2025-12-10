'use client'

import { useState, useEffect } from 'react'
import { Clock, ShoppingCart, XCircle, RefreshCw, MoreHorizontal, ArrowRight } from 'lucide-react'
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
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import { api, Reservation, Event, User } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'

const statusColors: Record<string, 'default' | 'secondary' | 'destructive' | 'success' | 'warning'> = {
    PENDING: 'warning',
    CONFIRMED: 'success',
    CANCELLED: 'destructive',
    EXPIRED: 'secondary',
    CONVERTED: 'default',
}

export default function ReservationsPage() {
    const [reservations, setReservations] = useState<Reservation[]>([])
    const [events, setEvents] = useState<Event[]>([])
    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState(true)
    const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null)
    const [isDetailsOpen, setIsDetailsOpen] = useState(false)
    const { toast } = useToast()

    const fetchData = async () => {
        try {
            const [eventsData, usersData] = await Promise.all([
                api.getEvents(),
                api.getUsers(),
            ])
            setEvents(eventsData)
            setUsers(usersData)

            // Fetch reservations for all events
            const allReservations: Reservation[] = []
            for (const event of eventsData) {
                try {
                    const eventReservations = await api.getEventReservations(event.id)
                    allReservations.push(...eventReservations)
                } catch {
                    // Event might not have reservations
                }
            }
            setReservations(allReservations)
        } catch (error) {
            console.error('Failed to fetch data:', error)
            toast({ title: 'Failed to load reservations', variant: 'destructive' })
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchData()
    }, [])

    const handleCancel = async (id: string) => {
        try {
            await api.cancelReservation(id)
            toast({ title: 'Reservation cancelled' })
            fetchData()
        } catch (error) {
            toast({ title: 'Failed to cancel reservation', variant: 'destructive' })
        }
    }

    const handleConvert = async (id: string) => {
        try {
            await api.convertReservation(id)
            toast({ title: 'Reservation converted to order' })
            fetchData()
        } catch (error) {
            toast({ title: 'Failed to convert reservation', variant: 'destructive' })
        }
    }

    const getEventName = (eventId: string) => {
        const event = events.find(e => e.id === eventId)
        return event?.name || eventId
    }

    const getUserName = (customerId: string) => {
        const user = users.find(u => u.id === customerId)
        return user?.name || customerId
    }

    const openDetails = (reservation: Reservation) => {
        setSelectedReservation(reservation)
        setIsDetailsOpen(true)
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Reservations</h1>
                    <p className="text-muted-foreground">
                        Manage ticket reservations across all events
                    </p>
                </div>
                <Button variant="outline" onClick={fetchData}>
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Refresh
                </Button>
            </div>

            {/* Stats Cards */}
            <div className="grid gap-4 md:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Reservations</CardTitle>
                        <Clock className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{reservations.length}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Pending</CardTitle>
                        <Clock className="h-4 w-4 text-yellow-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {reservations.filter(r => r.status === 'PENDING').length}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Converted</CardTitle>
                        <ShoppingCart className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {reservations.filter(r => r.status === 'CONVERTED').length}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Cancelled/Expired</CardTitle>
                        <XCircle className="h-4 w-4 text-destructive" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {reservations.filter(r => r.status === 'CANCELLED' || r.status === 'EXPIRED').length}
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Reservations Table */}
            <Card>
                <CardHeader>
                    <CardTitle>All Reservations</CardTitle>
                    <CardDescription>View and manage all ticket reservations</CardDescription>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="space-y-3">
                            {[1, 2, 3].map((i) => (
                                <div key={i} className="h-12 animate-pulse rounded bg-muted" />
                            ))}
                        </div>
                    ) : reservations.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-8 text-center">
                            <Clock className="h-12 w-12 text-muted-foreground" />
                            <h3 className="mt-4 text-lg font-semibold">No reservations yet</h3>
                            <p className="text-sm text-muted-foreground">
                                Reservations will appear here when customers reserve tickets
                            </p>
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Event</TableHead>
                                    <TableHead>Customer</TableHead>
                                    <TableHead className="text-right">Amount</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead>Expires</TableHead>
                                    <TableHead className="w-[70px]"></TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {reservations.map((reservation) => (
                                    <TableRow key={reservation.id}>
                                        <TableCell className="font-medium">
                                            {getEventName(reservation.eventId)}
                                        </TableCell>
                                        <TableCell>{getUserName(reservation.customerId)}</TableCell>
                                        <TableCell className="text-right">
                                            ${reservation.totalAmount.toFixed(2)}
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant={statusColors[reservation.status]}>
                                                {reservation.status}
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="text-sm text-muted-foreground">
                                            {new Date(reservation.expiresAt).toLocaleString()}
                                        </TableCell>
                                        <TableCell>
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" size="icon" className="h-8 w-8">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuLabel>Actions</DropdownMenuLabel>
                                                    <DropdownMenuSeparator />
                                                    <DropdownMenuItem onClick={() => openDetails(reservation)}>
                                                        View Details
                                                    </DropdownMenuItem>
                                                    {reservation.status === 'PENDING' && (
                                                        <>
                                                            <DropdownMenuItem onClick={() => handleConvert(reservation.id)}>
                                                                <ArrowRight className="mr-2 h-4 w-4" />
                                                                Convert to Order
                                                            </DropdownMenuItem>
                                                            <DropdownMenuItem
                                                                className="text-destructive"
                                                                onClick={() => handleCancel(reservation.id)}
                                                            >
                                                                Cancel
                                                            </DropdownMenuItem>
                                                        </>
                                                    )}
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>

            {/* Reservation Details Dialog */}
            <Dialog open={isDetailsOpen} onOpenChange={setIsDetailsOpen}>
                <DialogContent className="sm:max-w-[500px]">
                    <DialogHeader>
                        <DialogTitle>Reservation Details</DialogTitle>
                        <DialogDescription>
                            Details for reservation {selectedReservation?.id.slice(0, 8)}...
                        </DialogDescription>
                    </DialogHeader>
                    {selectedReservation && (
                        <div className="space-y-4 py-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-sm text-muted-foreground">Event</p>
                                    <p className="font-medium">{getEventName(selectedReservation.eventId)}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Customer</p>
                                    <p className="font-medium">{getUserName(selectedReservation.customerId)}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Status</p>
                                    <Badge variant={statusColors[selectedReservation.status]}>
                                        {selectedReservation.status}
                                    </Badge>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Total Amount</p>
                                    <p className="font-medium">${selectedReservation.totalAmount.toFixed(2)}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Created</p>
                                    <p className="text-sm">{new Date(selectedReservation.createdAt).toLocaleString()}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Expires</p>
                                    <p className="text-sm">{new Date(selectedReservation.expiresAt).toLocaleString()}</p>
                                </div>
                            </div>

                            <div>
                                <p className="text-sm font-medium mb-2">Items</p>
                                <Table>
                                    <TableHeader>
                                        <TableRow>
                                            <TableHead>Ticket Type</TableHead>
                                            <TableHead className="text-right">Qty</TableHead>
                                            <TableHead className="text-right">Price</TableHead>
                                            <TableHead className="text-right">Subtotal</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {selectedReservation.items.map((item, index) => (
                                            <TableRow key={index}>
                                                <TableCell className="font-mono text-sm">
                                                    {item.ticketTypeId.slice(0, 8)}...
                                                </TableCell>
                                                <TableCell className="text-right">{item.quantity}</TableCell>
                                                <TableCell className="text-right">
                                                    ${item.unitPrice.toFixed(2)}
                                                </TableCell>
                                                <TableCell className="text-right">
                                                    ${item.subtotal.toFixed(2)}
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </div>
                        </div>
                    )}
                </DialogContent>
            </Dialog>
        </div>
    )
}
