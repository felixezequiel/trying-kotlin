'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { ArrowLeft, Calendar, MapPin, Plus, Pencil, Power, Trash2, Ticket } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { api, Event, TicketType, CreateTicketTypeRequest } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'

const statusColors: Record<string, 'default' | 'secondary' | 'destructive' | 'success' | 'warning'> = {
    DRAFT: 'secondary',
    PUBLISHED: 'success',
    CANCELLED: 'destructive',
    FINISHED: 'default',
}

export default function EventDetailPage() {
    const params = useParams()
    const router = useRouter()
    const { toast } = useToast()
    const eventId = params.id as string

    const [event, setEvent] = useState<Event | null>(null)
    const [ticketTypes, setTicketTypes] = useState<TicketType[]>([])
    const [loading, setLoading] = useState(true)
    const [isEditOpen, setIsEditOpen] = useState(false)
    const [isTicketDialogOpen, setIsTicketDialogOpen] = useState(false)
    const [editingTicket, setEditingTicket] = useState<TicketType | null>(null)

    const [editForm, setEditForm] = useState({
        name: '',
        description: '',
        startDate: '',
        endDate: '',
        location: '',
    })

    const [ticketForm, setTicketForm] = useState<CreateTicketTypeRequest>({
        eventId: eventId,
        name: '',
        description: '',
        price: 0,
        totalQuantity: 0,
        maxPerCustomer: 10,
    })

    const fetchEvent = async () => {
        try {
            const data = await api.getEvent(eventId)
            setEvent(data)
            setEditForm({
                name: data.name,
                description: data.description,
                startDate: data.startDate,
                endDate: data.endDate,
                location: data.location,
            })
        } catch (error) {
            console.error('Failed to fetch event:', error)
            toast({ title: 'Failed to load event', variant: 'destructive' })
        }
    }

    const fetchTicketTypes = async () => {
        try {
            const data = await api.getTicketTypesByEvent(eventId)
            setTicketTypes(data)
        } catch (error) {
            console.error('Failed to fetch ticket types:', error)
        }
    }

    useEffect(() => {
        const loadData = async () => {
            setLoading(true)
            await Promise.all([fetchEvent(), fetchTicketTypes()])
            setLoading(false)
        }
        loadData()
    }, [eventId])

    const handleUpdateEvent = async () => {
        try {
            await api.updateEvent(eventId, editForm)
            toast({ title: 'Event updated successfully' })
            setIsEditOpen(false)
            fetchEvent()
        } catch (error) {
            toast({ title: 'Failed to update event', variant: 'destructive' })
        }
    }

    const handlePublish = async () => {
        try {
            await api.publishEvent(eventId)
            toast({ title: 'Event published successfully' })
            fetchEvent()
        } catch (error) {
            toast({ title: 'Failed to publish event', variant: 'destructive' })
        }
    }

    const handleCancel = async () => {
        try {
            await api.cancelEvent(eventId)
            toast({ title: 'Event cancelled' })
            fetchEvent()
        } catch (error) {
            toast({ title: 'Failed to cancel event', variant: 'destructive' })
        }
    }

    const handleFinish = async () => {
        try {
            await api.finishEvent(eventId)
            toast({ title: 'Event finished' })
            fetchEvent()
        } catch (error) {
            toast({ title: 'Failed to finish event', variant: 'destructive' })
        }
    }

    const handleCreateTicketType = async () => {
        try {
            await api.createTicketType(event.partnerId, {
                ...ticketForm,
                eventId,
                price: ticketForm.price.toString(),
                totalQuantity: ticketForm.totalQuantity,
                maxPerCustomer: ticketForm.maxPerCustomer,
                salesStartDate: ticketForm.salesStartDate ? `${ticketForm.salesStartDate}:00Z` : undefined,
                salesEndDate: ticketForm.salesEndDate ? `${ticketForm.salesEndDate}:00Z` : undefined
            })
            toast({ title: 'Ticket type created successfully' })
            setIsTicketDialogOpen(false)
            setTicketForm({ eventId, name: '', description: '', price: 0, totalQuantity: 0, maxPerCustomer: 10, salesStartDate: undefined, salesEndDate: undefined })
            fetchTicketTypes()
        } catch (error) {
            toast({ title: 'Failed to create ticket type', variant: 'destructive' })
        }
    }

    const handleUpdateTicketType = async () => {
        if (!editingTicket) return
        try {
            await api.updateTicketType(event.partnerId, editingTicket.id, {
                name: ticketForm.name,
                description: ticketForm.description,
                price: ticketForm.price.toString(),
                totalQuantity: ticketForm.totalQuantity,
                salesStartDate: ticketForm.salesStartDate ? `${ticketForm.salesStartDate}:00Z` : undefined,
                salesEndDate: ticketForm.salesEndDate ? `${ticketForm.salesEndDate}:00Z` : undefined
            })
            toast({ title: 'Ticket type updated successfully' })
            setIsTicketDialogOpen(false)
            setEditingTicket(null)
            setTicketForm({ eventId, name: '', description: '', price: 0, totalQuantity: 0, salesStartDate: undefined, salesEndDate: undefined })
            fetchTicketTypes()
        } catch (error) {
            toast({ title: 'Failed to update ticket type', variant: 'destructive' })
        }
    }

    const handleDeactivateTicketType = async (id: string) => {
        try {
            await api.deactivateTicketType(event.partnerId, id)
            toast({ title: 'Ticket type deactivated' })
            fetchTicketTypes()
        } catch (error) {
            toast({ title: 'Failed to deactivate ticket type', variant: 'destructive' })
        }
    }

    const handleActivateTicketType = async (id: string) => {
        try {
            await api.activateTicketType(event.partnerId, id)
            toast({ title: 'Ticket type activated' })
            fetchTicketTypes()
        } catch (error) {
            toast({ title: 'Failed to activate ticket type', variant: 'destructive' })
        }
    }

    const openEditTicket = (ticket: TicketType) => {
        setEditingTicket(ticket)
        setTicketForm({
            eventId,
            name: ticket.name,
            description: ticket.description,
            price: ticket.price,
            totalQuantity: ticket.totalQuantity,
            salesStartDate: ticket.salesStartDate?.slice(0, -1), // Remove Z for datetime-local input
            salesEndDate: ticket.salesEndDate?.slice(0, -1) // Remove Z for datetime-local input
        })
        setIsTicketDialogOpen(true)
    }

    const openCreateTicket = () => {
        setEditingTicket(null)
        setTicketForm({ eventId, name: '', description: '', price: 0, totalQuantity: 0, maxPerCustomer: 10, salesStartDate: undefined, salesEndDate: undefined })
        setIsTicketDialogOpen(true)
    }

    if (loading) {
        return (
            <div className="space-y-6">
                <div className="h-8 w-48 animate-pulse rounded bg-muted" />
                <div className="h-64 animate-pulse rounded bg-muted" />
            </div>
        )
    }

    if (!event) {
        return (
            <div className="flex flex-col items-center justify-center py-12">
                <h2 className="text-xl font-semibold">Event not found</h2>
                <Button variant="link" onClick={() => router.push('/events')}>
                    Back to events
                </Button>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={() => router.push('/events')}>
                    <ArrowLeft className="h-4 w-4" />
                </Button>
                <div className="flex-1">
                    <div className="flex items-center gap-3">
                        <h1 className="text-3xl font-bold tracking-tight">{event.name}</h1>
                        <Badge variant={statusColors[event.status]}>{event.status}</Badge>
                    </div>
                    <p className="text-muted-foreground">{event.description}</p>
                </div>
                <div className="flex gap-2">
                    {event.status === 'DRAFT' && (
                        <>
                            <Button variant="outline" onClick={() => setIsEditOpen(true)}>
                                <Pencil className="mr-2 h-4 w-4" />
                                Edit
                            </Button>
                            <Button onClick={handlePublish}>Publish</Button>
                        </>
                    )}
                    {event.status === 'PUBLISHED' && (
                        <>
                            <Button variant="outline" onClick={handleFinish}>
                                Finish Event
                            </Button>
                            <Button variant="destructive" onClick={handleCancel}>
                                Cancel Event
                            </Button>
                        </>
                    )}
                </div>
            </div>

            {/* Event Details */}
            <div className="grid gap-6 md:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle>Event Details</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="flex items-center gap-2 text-sm">
                            <Calendar className="h-4 w-4 text-muted-foreground" />
                            <span>
                                {event.startDate} - {event.endDate}
                            </span>
                        </div>
                        <div className="flex items-center gap-2 text-sm">
                            <MapPin className="h-4 w-4 text-muted-foreground" />
                            <span>{event.location}</span>
                        </div>
                        <div className="text-sm text-muted-foreground">
                            Partner ID: {event.partnerId}
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Statistics</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <p className="text-sm text-muted-foreground">Ticket Types</p>
                                <p className="text-2xl font-bold">{ticketTypes.length}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Total Capacity</p>
                                <p className="text-2xl font-bold">
                                    {ticketTypes.reduce((acc, t) => acc + t.quantity, 0)}
                                </p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Available</p>
                                <p className="text-2xl font-bold">
                                    {ticketTypes.reduce((acc, t) => acc + t.availableQuantity, 0)}
                                </p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Sold</p>
                                <p className="text-2xl font-bold">
                                    {ticketTypes.reduce((acc, t) => acc + (t.quantity - t.availableQuantity), 0)}
                                </p>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Ticket Types */}
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <div>
                        <CardTitle>Ticket Types</CardTitle>
                        <CardDescription>Manage ticket types for this event</CardDescription>
                    </div>
                    {(event.status === 'DRAFT' || event.status === 'PUBLISHED') && (
                        <Button onClick={openCreateTicket}>
                            <Plus className="mr-2 h-4 w-4" />
                            Add Ticket Type
                        </Button>
                    )}
                </CardHeader>
                <CardContent>
                    {ticketTypes.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-8 text-center">
                            <Ticket className="h-12 w-12 text-muted-foreground" />
                            <h3 className="mt-4 text-lg font-semibold">No ticket types</h3>
                            <p className="text-sm text-muted-foreground">
                                Create ticket types to start selling tickets
                            </p>
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Name</TableHead>
                                    <TableHead>Description</TableHead>
                                    <TableHead className="text-right">Price</TableHead>
                                    <TableHead className="text-right">Quantity</TableHead>
                                    <TableHead className="text-right">Available</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead className="w-[100px]"></TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {ticketTypes.map((ticket) => (
                                    <TableRow key={ticket.id}>
                                        <TableCell className="font-medium">{ticket.name}</TableCell>
                                        <TableCell className="max-w-[200px] truncate">
                                            {ticket.description}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            ${parseFloat(ticket.price).toFixed(2)}
                                        </TableCell>
                                        <TableCell className="text-right">{ticket.totalQuantity}</TableCell>
                                        <TableCell className="text-right">
                                            {ticket.availableQuantity}
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
                                                    onClick={() => openEditTicket(ticket)}
                                                >
                                                    <Pencil className="h-4 w-4" />
                                                </Button>
                                                {ticket.status === "ACTIVE" && (
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        onClick={() => handleDeactivateTicketType(ticket.id)}
                                                    >
                                                        <Trash2 className="h-4 w-4 text-destructive" />
                                                    </Button>
                                                )}
                                                {ticket.status !== "ACTIVE" && (
                                                    <Button
                                                        variant="ghost"
                                                        size="icon"
                                                        onClick={() => handleActivateTicketType(ticket.id)}
                                                    >
                                                        <Power className="h-4 w-4 text-green-600" />
                                                    </Button>
                                                )}
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>

            {/* Edit Event Dialog */}
            <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
                <DialogContent className="sm:max-w-[525px]">
                    <DialogHeader>
                        <DialogTitle>Edit Event</DialogTitle>
                        <DialogDescription>Update event details</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                            <Label htmlFor="edit-name">Event Name</Label>
                            <Input
                                id="edit-name"
                                value={editForm.name}
                                onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="edit-description">Description</Label>
                            <Textarea
                                id="edit-description"
                                value={editForm.description}
                                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setEditForm({ ...editForm, description: e.target.value })}
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="grid gap-2">
                                <Label htmlFor="edit-startDate">Start Date</Label>
                                <Input
                                    id="edit-startDate"
                                    type="date"
                                    value={editForm.startDate}
                                    onChange={(e) => setEditForm({ ...editForm, startDate: e.target.value })}
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="edit-endDate">End Date</Label>
                                <Input
                                    id="edit-endDate"
                                    type="date"
                                    value={editForm.endDate}
                                    onChange={(e) => setEditForm({ ...editForm, endDate: e.target.value })}
                                />
                            </div>
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="edit-location">Location</Label>
                            <Input
                                id="edit-location"
                                value={editForm.location}
                                onChange={(e) => setEditForm({ ...editForm, location: e.target.value })}
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsEditOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleUpdateEvent}>Save Changes</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Ticket Type Dialog */}
            <Dialog open={isTicketDialogOpen} onOpenChange={setIsTicketDialogOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>
                            {editingTicket ? 'Edit Ticket Type' : 'Create Ticket Type'}
                        </DialogTitle>
                        <DialogDescription>
                            {editingTicket
                                ? 'Update ticket type details'
                                : 'Add a new ticket type for this event'}
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                            <Label htmlFor="ticket-name">Name</Label>
                            <Input
                                id="ticket-name"
                                placeholder="e.g., VIP Pass"
                                value={ticketForm.name}
                                onChange={(e) => setTicketForm({ ...ticketForm, name: e.target.value })}
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="ticket-description">Description</Label>
                            <Textarea
                                id="ticket-description"
                                placeholder="Describe what's included"
                                value={ticketForm.description}
                                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setTicketForm({ ...ticketForm, description: e.target.value })}
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="grid gap-2">
                                <Label htmlFor="ticket-price">Price ($)</Label>
                                <Input
                                    id="ticket-price"
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={ticketForm.price}
                                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                                        setTicketForm({ ...ticketForm, price: parseFloat(e.target.value) || 0 })
                                    }
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="ticket-quantity">Quantity</Label>
                                <Input
                                    id="ticket-quantity"
                                    type="number"
                                    min="1"
                                    value={ticketForm.totalQuantity}
                                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                                        setTicketForm({ ...ticketForm, totalQuantity: parseInt(e.target.value) || 0 })
                                    }
                                />
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="grid gap-2">
                                <Label htmlFor="ticket-sales-start">Sales Start Date</Label>
                                <Input
                                    id="ticket-sales-start"
                                    type="datetime-local"
                                    value={ticketForm.salesStartDate || ''}
                                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                                        setTicketForm({ ...ticketForm, salesStartDate: e.target.value || undefined })
                                    }
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="ticket-sales-end">Sales End Date</Label>
                                <Input
                                    id="ticket-sales-end"
                                    type="datetime-local"
                                    value={ticketForm.salesEndDate || ''}
                                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                                        setTicketForm({ ...ticketForm, salesEndDate: e.target.value || undefined })
                                    }
                                />
                            </div>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsTicketDialogOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={editingTicket ? handleUpdateTicketType : handleCreateTicketType}>
                            {editingTicket ? 'Save Changes' : 'Create'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    )
}
