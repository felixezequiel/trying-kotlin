'use client'

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Calendar, Users, Building2, Ticket, TrendingUp, DollarSign } from 'lucide-react'

const stats = [
    {
        title: 'Total Events',
        value: '24',
        description: '+3 from last month',
        icon: Calendar,
        trend: '+12%',
    },
    {
        title: 'Active Partners',
        value: '12',
        description: '+2 new this week',
        icon: Building2,
        trend: '+8%',
    },
    {
        title: 'Registered Users',
        value: '1,234',
        description: '+89 new users',
        icon: Users,
        trend: '+15%',
    },
    {
        title: 'Tickets Sold',
        value: '5,678',
        description: 'This month',
        icon: Ticket,
        trend: '+23%',
    },
]

const recentEvents = [
    { name: 'Summer Music Festival', date: '2024-01-15', status: 'PUBLISHED', tickets: 450 },
    { name: 'Tech Conference 2024', date: '2024-01-20', status: 'PUBLISHED', tickets: 320 },
    { name: 'Art Exhibition', date: '2024-02-01', status: 'DRAFT', tickets: 0 },
    { name: 'Food & Wine Festival', date: '2024-02-10', status: 'PUBLISHED', tickets: 180 },
]

export default function DashboardPage() {
    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
                <p className="text-muted-foreground">
                    Welcome back! Here&apos;s an overview of your ticket system.
                </p>
            </div>

            {/* Stats Grid */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                {stats.map((stat) => (
                    <Card key={stat.title}>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">{stat.title}</CardTitle>
                            <stat.icon className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">{stat.value}</div>
                            <div className="flex items-center text-xs text-muted-foreground">
                                <TrendingUp className="mr-1 h-3 w-3 text-green-500" />
                                <span className="text-green-500">{stat.trend}</span>
                                <span className="ml-1">{stat.description}</span>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>

            {/* Recent Events & Revenue */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
                <Card className="col-span-4">
                    <CardHeader>
                        <CardTitle>Recent Events</CardTitle>
                        <CardDescription>
                            Latest events in the system
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            {recentEvents.map((event, index) => (
                                <div
                                    key={index}
                                    className="flex items-center justify-between rounded-lg border p-4"
                                >
                                    <div className="space-y-1">
                                        <p className="font-medium">{event.name}</p>
                                        <p className="text-sm text-muted-foreground">{event.date}</p>
                                    </div>
                                    <div className="flex items-center gap-4">
                                        <div className="text-right">
                                            <p className="text-sm font-medium">{event.tickets} tickets</p>
                                            <span
                                                className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${event.status === 'PUBLISHED'
                                                        ? 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300'
                                                        : 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-300'
                                                    }`}
                                            >
                                                {event.status}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </CardContent>
                </Card>

                <Card className="col-span-3">
                    <CardHeader>
                        <CardTitle>Revenue Overview</CardTitle>
                        <CardDescription>Monthly revenue breakdown</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-2">
                                    <DollarSign className="h-4 w-4 text-muted-foreground" />
                                    <span className="text-sm font-medium">Total Revenue</span>
                                </div>
                                <span className="text-2xl font-bold">$45,231</span>
                            </div>
                            <div className="space-y-2">
                                <div className="flex items-center justify-between text-sm">
                                    <span className="text-muted-foreground">Ticket Sales</span>
                                    <span className="font-medium">$38,500</span>
                                </div>
                                <div className="h-2 rounded-full bg-secondary">
                                    <div className="h-2 w-[85%] rounded-full bg-primary" />
                                </div>
                            </div>
                            <div className="space-y-2">
                                <div className="flex items-center justify-between text-sm">
                                    <span className="text-muted-foreground">Service Fees</span>
                                    <span className="font-medium">$6,731</span>
                                </div>
                                <div className="h-2 rounded-full bg-secondary">
                                    <div className="h-2 w-[15%] rounded-full bg-primary" />
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    )
}
