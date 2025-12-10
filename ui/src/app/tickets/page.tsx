'use client'

import { useState } from 'react'
import { Ticket, DollarSign, Package, TrendingUp } from 'lucide-react'
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

const ticketTypes = [
    {
        id: '1',
        eventName: 'Summer Music Festival',
        name: 'VIP Pass',
        price: 299.99,
        quantity: 100,
        availableQuantity: 45,
        active: true,
    },
    {
        id: '2',
        eventName: 'Summer Music Festival',
        name: 'General Admission',
        price: 89.99,
        quantity: 500,
        availableQuantity: 234,
        active: true,
    },
    {
        id: '3',
        eventName: 'Tech Conference 2024',
        name: 'Full Access',
        price: 499.99,
        quantity: 200,
        availableQuantity: 120,
        active: true,
    },
    {
        id: '4',
        eventName: 'Tech Conference 2024',
        name: 'Workshop Only',
        price: 149.99,
        quantity: 50,
        availableQuantity: 0,
        active: false,
    },
    {
        id: '5',
        eventName: 'Art Exhibition',
        name: 'Standard Entry',
        price: 25.00,
        quantity: 1000,
        availableQuantity: 876,
        active: true,
    },
]

export default function TicketsPage() {
    const [tickets] = useState(ticketTypes)

    const totalRevenue = tickets.reduce((acc, t) => acc + (t.quantity - t.availableQuantity) * t.price, 0)
    const totalSold = tickets.reduce((acc, t) => acc + (t.quantity - t.availableQuantity), 0)
    const totalAvailable = tickets.reduce((acc, t) => acc + t.availableQuantity, 0)

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Tickets</h1>
                <p className="text-muted-foreground">
                    Overview of all ticket types across events
                </p>
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
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Event</TableHead>
                                <TableHead>Ticket Type</TableHead>
                                <TableHead className="text-right">Price</TableHead>
                                <TableHead className="text-right">Sold</TableHead>
                                <TableHead className="text-right">Available</TableHead>
                                <TableHead>Status</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {tickets.map((ticket) => (
                                <TableRow key={ticket.id}>
                                    <TableCell className="font-medium">{ticket.eventName}</TableCell>
                                    <TableCell>{ticket.name}</TableCell>
                                    <TableCell className="text-right">
                                        ${ticket.price.toFixed(2)}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        {ticket.quantity - ticket.availableQuantity}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <div className="flex items-center justify-end gap-2">
                                            <span>{ticket.availableQuantity}</span>
                                            <div className="h-2 w-16 rounded-full bg-secondary">
                                                <div
                                                    className="h-2 rounded-full bg-primary"
                                                    style={{
                                                        width: `${(ticket.availableQuantity / ticket.quantity) * 100}%`,
                                                    }}
                                                />
                                            </div>
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={ticket.active ? 'success' : 'secondary'}>
                                            {ticket.active ? 'Active' : 'Inactive'}
                                        </Badge>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    )
}
