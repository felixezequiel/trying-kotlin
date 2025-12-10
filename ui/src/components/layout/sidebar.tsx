'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
    Calendar,
    Users,
    Building2,
    Ticket,
    LayoutDashboard,
    Settings,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'

const navigation = [
    { name: 'Dashboard', href: '/', icon: LayoutDashboard },
    { name: 'Events', href: '/events', icon: Calendar },
    { name: 'Partners', href: '/partners', icon: Building2 },
    { name: 'Users', href: '/users', icon: Users },
    { name: 'Tickets', href: '/tickets', icon: Ticket },
]

const secondaryNavigation = [
    { name: 'Settings', href: '/settings', icon: Settings },
]

export function Sidebar() {
    const pathname = usePathname()

    return (
        <div className="flex h-full w-64 flex-col border-r bg-card">
            {/* Logo */}
            <div className="flex h-16 items-center gap-2 px-6">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
                    <Ticket className="h-5 w-5 text-primary-foreground" />
                </div>
                <span className="text-lg font-semibold">Ticket System</span>
            </div>

            <Separator />

            {/* Navigation */}
            <nav className="flex-1 space-y-1 px-3 py-4">
                <div className="space-y-1">
                    {navigation.map((item) => {
                        const isActive = pathname === item.href
                        return (
                            <Link key={item.name} href={item.href}>
                                <Button
                                    variant={isActive ? 'secondary' : 'ghost'}
                                    className={cn(
                                        'w-full justify-start gap-3',
                                        isActive && 'bg-secondary'
                                    )}
                                >
                                    <item.icon className="h-4 w-4" />
                                    {item.name}
                                </Button>
                            </Link>
                        )
                    })}
                </div>
            </nav>

            <Separator />

            {/* Secondary Navigation */}
            <div className="px-3 py-4">
                {secondaryNavigation.map((item) => {
                    const isActive = pathname === item.href
                    return (
                        <Link key={item.name} href={item.href}>
                            <Button
                                variant={isActive ? 'secondary' : 'ghost'}
                                className={cn(
                                    'w-full justify-start gap-3',
                                    isActive && 'bg-secondary'
                                )}
                            >
                                <item.icon className="h-4 w-4" />
                                {item.name}
                            </Button>
                        </Link>
                    )
                })}
            </div>
        </div>
    )
}
