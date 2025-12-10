'use client'

import { useState, useEffect } from 'react'
import { Plus, MoreHorizontal, Calendar, MapPin } from 'lucide-react'
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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { api, Event } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'

const statusColors: Record<string, 'default' | 'secondary' | 'destructive' | 'success' | 'warning'> = {
    DRAFT: 'secondary',
    PUBLISHED: 'success',
    CANCELLED: 'destructive',
    FINISHED: 'default',
}

export default function EventsPage() {
    const [events, setEvents] = useState<Event[]>([])
    const [loading, setLoading] = useState(true)
    const [isCreateOpen, setIsCreateOpen] = useState(false)
    const { toast } = useToast()

    const fetchEvents = async () => {
        try {
            const data = await api.getEvents()
            setEvents(data)
        } catch (error) {
            console.error('Failed to fetch events:', error)
            // Mock data for demo
            setEvents([
                {
                    id: '1',
                    name: 'Summer Music Festival',
                    description: 'A great music festival',
                    partnerId: 'partner-1',
                    status: 'PUBLISHED',
                    startDate: '2024-06-15',
                    endDate: '2024-06-17',
                    location: 'Central Park, NY',
                },
                {
                    id: '2',
                    name: 'Tech Conference 2024',
                    description: 'Annual tech conference',
                    partnerId: 'partner-2',
                    status: 'DRAFT',
                    startDate: '2024-07-20',
                    endDate: '2024-07-22',
                    location: 'Convention Center, SF',
                },
                {
                    id: '3',
                    name: 'Art Exhibition',
                    description: 'Modern art showcase',
                    partnerId: 'partner-1',
                    status: 'PUBLISHED',
                    startDate: '2024-08-01',
                    endDate: '2024-08-15',
                    location: 'Art Gallery, LA',
                },
            ])
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchEvents()
    }, [])

    const handlePublish = async (id: string) => {
        try {
            await api.publishEvent(id)
            toast({ title: 'Event published successfully' })
            fetchEvents()
        } catch (error) {
            toast({ title: 'Failed to publish event', variant: 'destructive' })
        }
    }

    const handleCancel = async (id: string) => {
        try {
            await api.cancelEvent(id)
            toast({ title: 'Event cancelled' })
            fetchEvents()
        } catch (error) {
            toast({ title: 'Failed to cancel event', variant: 'destructive' })
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
                                <Label htmlFor="name">Event Name</Label>
                                <Input id="name" placeholder="Enter event name" />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="description">Description</Label>
                                <Input id="description" placeholder="Enter description" />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="grid gap-2">
                                    <Label htmlFor="startDate">Start Date</Label>
                                    <Input id="startDate" type="date" />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="endDate">End Date</Label>
                                    <Input id="endDate" type="date" />
                                </div>
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="location">Location</Label>
                                <Input id="location" placeholder="Enter location" />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                                Cancel
                            </Button>
                            <Button onClick={() => {
                                toast({ title: 'Event created successfully' })
                                setIsCreateOpen(false)
                            }}>
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
                                            <DropdownMenuItem>View Details</DropdownMenuItem>
                                            <DropdownMenuItem>Edit Event</DropdownMenuItem>
                                            {event.status === 'DRAFT' && (
                                                <DropdownMenuItem onClick={() => handlePublish(event.id)}>
                                                    Publish
                                                </DropdownMenuItem>
                                            )}
                                            {event.status === 'PUBLISHED' && (
                                                <DropdownMenuItem
                                                    className="text-destructive"
                                                    onClick={() => handleCancel(event.id)}
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
