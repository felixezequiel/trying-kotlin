'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Plus, MoreHorizontal, Calendar, MapPin, Eye, Pencil, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
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
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '@/components/ui/dialog'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { api, Event, Partner, CreateEventRequest } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'

const statusColors: Record<string, 'default' | 'secondary' | 'destructive' | 'success' | 'warning'> = {
    DRAFT: 'secondary',
    PUBLISHED: 'success',
    CANCELLED: 'destructive',
    FINISHED: 'default',
}

export default function EventsPage() {
    const router = useRouter()
    const [events, setEvents] = useState<Event[]>([])
    const [partners, setPartners] = useState<Partner[]>([])
    const [loading, setLoading] = useState(true)
    const [isCreateOpen, setIsCreateOpen] = useState(false)
    const { toast } = useToast()

    const [newEvent, setNewEvent] = useState<CreateEventRequest>({
        partnerId: '',
        name: '',
        description: '',
        startDate: '',
        endDate: '',
        location: '',
    })

    const fetchEvents = async () => {
        try {
            const data = await api.getEvents()
            setEvents(data)
        } catch (error) {
            console.error('Failed to fetch events:', error)
            toast({ title: 'Failed to load events', variant: 'destructive' })
        } finally {
            setLoading(false)
        }
    }

    const fetchPartners = async () => {
        try {
            const data = await api.getPartners()
            setPartners(data.filter(p => p.status === 'APPROVED'))
        } catch (error) {
            console.error('Failed to fetch partners:', error)
        }
    }

    useEffect(() => {
        fetchEvents()
        fetchPartners()
    }, [])

    const handlePublish = async (event: Event) => {
        try {
            await api.publishEvent(event.partnerId, event.id)
            toast({ title: 'Event published successfully' })
            fetchEvents()
        } catch (error) {
            toast({ title: 'Failed to publish event', variant: 'destructive' })
        }
    }

    const handleCancel = async (event: Event) => {
        try {
            await api.cancelEvent(event.partnerId, event.id)
            toast({ title: 'Event cancelled' })
            fetchEvents()
        } catch (error) {
            toast({ title: 'Failed to cancel event', variant: 'destructive' })
        }
    }

    const handleCreateEvent = async () => {
        if (!newEvent.partnerId || !newEvent.name || !newEvent.startDate || !newEvent.endDate) {
            toast({ title: 'Please fill all required fields', variant: 'destructive' })
            return
        }
        try {
            // Backend expects a structured Venue, but frontend only has location string
            // Constructing a venue object to satisfy the contract
            const venue = {
                name: newEvent.location || 'Unknown Venue',
                address: newEvent.location || 'Unknown Address',
                city: 'Unknown',
                state: 'UN',
                zipCode: '00000',
                capacity: 100
            }

            const eventData = {
                name: newEvent.name,
                description: newEvent.description,
                startDate: new Date(newEvent.startDate).toISOString(),
                endDate: new Date(newEvent.endDate).toISOString(),
                venue: venue,
                imageUrl: newEvent.imageUrl
            }

            const created = await api.createEvent(newEvent.partnerId, eventData)
            toast({ title: 'Event created successfully' })
            setIsCreateOpen(false)
            setNewEvent({
                partnerId: '',
                name: '',
                description: '',
                startDate: '',
                endDate: '',
                location: '',
            })
            fetchEvents()
            router.push(`/events/${created.id}`)
        } catch (error) {
            console.error('Create event failed:', error)
            toast({ title: 'Failed to create event', variant: 'destructive' })
        }
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Events</h1>
                    <p className="text-muted-foreground">
                        Manage your events and ticket types
                    </p>
                </div>
                <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                    <DialogTrigger asChild>
                        <Button>
                            <Plus className="mr-2 h-4 w-4" />
                            Create Event
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[525px]">
                        <DialogHeader>
                            <DialogTitle>Create New Event</DialogTitle>
                            <DialogDescription>
                                Fill in the details to create a new event.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="grid gap-2">
                                <Label htmlFor="partner">Partner *</Label>
                                <Select
                                    value={newEvent.partnerId}
                                    onValueChange={(value: string) => setNewEvent({ ...newEvent, partnerId: value })}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="Select a partner" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {partners.map((partner) => (
                                            <SelectItem key={partner.id} value={partner.id}>
                                                {partner.companyName}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="name">Event Name *</Label>
                                <Input
                                    id="name"
                                    placeholder="Enter event name"
                                    value={newEvent.name}
                                    onChange={(e) => setNewEvent({ ...newEvent, name: e.target.value })}
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="description">Description</Label>
                                <Textarea
                                    id="description"
                                    placeholder="Enter description"
                                    value={newEvent.description}
                                    onChange={(e) => setNewEvent({ ...newEvent, description: e.target.value })}
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="grid gap-2">
                                    <Label htmlFor="startDate">Start Date *</Label>
                                    <Input
                                        id="startDate"
                                        type="date"
                                        value={newEvent.startDate}
                                        onChange={(e) => setNewEvent({ ...newEvent, startDate: e.target.value })}
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="endDate">End Date *</Label>
                                    <Input
                                        id="endDate"
                                        type="date"
                                        value={newEvent.endDate}
                                        onChange={(e) => setNewEvent({ ...newEvent, endDate: e.target.value })}
                                    />
                                </div>
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="location">Location</Label>
                                <Input
                                    id="location"
                                    placeholder="Enter location"
                                    value={newEvent.location}
                                    onChange={(e) => setNewEvent({ ...newEvent, location: e.target.value })}
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                                Cancel
                            </Button>
                            <Button onClick={handleCreateEvent}>
                                Create Event
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>

            {/* Events Grid */}
            {loading ? (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {[1, 2, 3].map((i) => (
                        <Card key={i} className="animate-pulse">
                            <CardHeader className="space-y-2">
                                <div className="h-4 w-3/4 rounded bg-muted" />
                                <div className="h-3 w-1/2 rounded bg-muted" />
                            </CardHeader>
                            <CardContent>
                                <div className="h-20 rounded bg-muted" />
                            </CardContent>
                        </Card>
                    ))}
                </div>
            ) : (
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {events.map((event) => (
                        <Card key={event.id} className="overflow-hidden">
                            <CardHeader className="pb-3">
                                <div className="flex items-start justify-between">
                                    <div className="space-y-1">
                                        <CardTitle className="text-lg">{event.name}</CardTitle>
                                        <CardDescription className="line-clamp-2">
                                            {event.description}
                                        </CardDescription>
                                    </div>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild>
                                            <Button variant="ghost" size="icon" className="h-8 w-8">
                                                <MoreHorizontal className="h-4 w-4" />
                                            </Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align="end">
                                            <DropdownMenuLabel>Actions</DropdownMenuLabel>
                                            <DropdownMenuSeparator />
                                            <DropdownMenuItem onClick={() => router.push(`/events/${event.id}`)}>
                                                <Eye className="mr-2 h-4 w-4" />
                                                View Details
                                            </DropdownMenuItem>
                                            {event.status === 'DRAFT' && (
                                                <>
                                                    <DropdownMenuItem onClick={() => router.push(`/events/${event.id}`)}>
                                                        <Pencil className="mr-2 h-4 w-4" />
                                                        Edit Event
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem onClick={() => handlePublish(event)}>
                                                        Publish
                                                    </DropdownMenuItem>
                                                </>
                                            )}
                                            {event.status === 'PUBLISHED' && (
                                                <DropdownMenuItem
                                                    className="text-destructive"
                                                    onClick={() => handleCancel(event)}
                                                >
                                                    Cancel Event
                                                </DropdownMenuItem>
                                            )}
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </div>
                            </CardHeader>
                            <CardContent className="space-y-3">
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <Calendar className="h-4 w-4" />
                                    <span>{event.startDate} - {event.endDate}</span>
                                </div>
                                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                    <MapPin className="h-4 w-4" />
                                    <span>{event.location}</span>
                                </div>
                                <div className="pt-2">
                                    <Badge variant={statusColors[event.status] || 'default'}>
                                        {event.status}
                                    </Badge>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    )
}
