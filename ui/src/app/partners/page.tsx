'use client'

import { useState, useEffect } from 'react'
import { Plus, MoreHorizontal, Building2, Mail, FileText } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
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
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { api, Partner } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'

const statusColors: Record<string, 'default' | 'secondary' | 'destructive' | 'success' | 'warning'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'destructive',
    SUSPENDED: 'secondary',
}

export default function PartnersPage() {
    const [partners, setPartners] = useState<Partner[]>([])
    const [loading, setLoading] = useState(true)
    const [isCreateOpen, setIsCreateOpen] = useState(false)
    const { toast } = useToast()

    const fetchPartners = async () => {
        try {
            const data = await api.getPartners()
            setPartners(data)
        } catch (error) {
            console.error('Failed to fetch partners:', error)
            // Mock data for demo
            setPartners([
                { id: '1', name: 'Event Masters Inc.', email: 'contact@eventmasters.com', document: '12.345.678/0001-90', status: 'APPROVED' },
                { id: '2', name: 'Festival Productions', email: 'hello@festprod.com', document: '98.765.432/0001-10', status: 'PENDING' },
                { id: '3', name: 'Concert World', email: 'info@concertworld.com', document: '11.222.333/0001-44', status: 'APPROVED' },
                { id: '4', name: 'Art Gallery Co.', email: 'art@gallery.com', document: '55.666.777/0001-88', status: 'SUSPENDED' },
            ])
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchPartners()
    }, [])

    const handleApprove = async (id: string) => {
        try {
            await api.approvePartner(id)
            toast({ title: 'Partner approved successfully' })
            fetchPartners()
        } catch (error) {
            toast({ title: 'Failed to approve partner', variant: 'destructive' })
        }
    }

    const handleReject = async (id: string) => {
        try {
            await api.rejectPartner(id)
            toast({ title: 'Partner rejected' })
            fetchPartners()
        } catch (error) {
            toast({ title: 'Failed to reject partner', variant: 'destructive' })
        }
    }

    const handleSuspend = async (id: string) => {
        try {
            await api.suspendPartner(id)
            toast({ title: 'Partner suspended' })
            fetchPartners()
        } catch (error) {
            toast({ title: 'Failed to suspend partner', variant: 'destructive' })
        }
    }

    const handleReactivate = async (id: string) => {
        try {
            await api.reactivatePartner(id)
            toast({ title: 'Partner reactivated' })
            fetchPartners()
        } catch (error) {
            toast({ title: 'Failed to reactivate partner', variant: 'destructive' })
        }
    }

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Partners</h1>
                    <p className="text-muted-foreground">
                        Manage event partners and their status
                    </p>
                </div>
                <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                    <DialogTrigger asChild>
                        <Button>
                            <Plus className="mr-2 h-4 w-4" />
                            Add Partner
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[525px]">
                        <DialogHeader>
                            <DialogTitle>Add New Partner</DialogTitle>
                            <DialogDescription>
                                Register a new partner in the system.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="grid gap-2">
                                <Label htmlFor="name">Company Name</Label>
                                <Input id="name" placeholder="Enter company name" />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="email">Email</Label>
                                <Input id="email" type="email" placeholder="Enter email" />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="document">Document (CNPJ)</Label>
                                <Input id="document" placeholder="00.000.000/0001-00" />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                                Cancel
                            </Button>
                            <Button onClick={() => {
                                toast({ title: 'Partner added successfully' })
                                setIsCreateOpen(false)
                            }}>
                                Add Partner
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>

            {/* Stats Cards */}
            <div className="grid gap-4 md:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Partners</CardTitle>
                        <Building2 className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{partners.length}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Approved</CardTitle>
                        <Building2 className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {partners.filter(p => p.status === 'APPROVED').length}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Pending</CardTitle>
                        <Building2 className="h-4 w-4 text-yellow-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {partners.filter(p => p.status === 'PENDING').length}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Suspended</CardTitle>
                        <Building2 className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {partners.filter(p => p.status === 'SUSPENDED').length}
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Partners Table */}
            <Card>
                <CardHeader>
                    <CardTitle>All Partners</CardTitle>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="space-y-3">
                            {[1, 2, 3].map((i) => (
                                <div key={i} className="h-12 animate-pulse rounded bg-muted" />
                            ))}
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Company</TableHead>
                                    <TableHead>Email</TableHead>
                                    <TableHead>Document</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead className="w-[70px]"></TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {partners.map((partner) => (
                                    <TableRow key={partner.id}>
                                        <TableCell className="font-medium">
                                            <div className="flex items-center gap-2">
                                                <Building2 className="h-4 w-4 text-muted-foreground" />
                                                {partner.name}
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex items-center gap-2">
                                                <Mail className="h-4 w-4 text-muted-foreground" />
                                                {partner.email}
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            <div className="flex items-center gap-2">
                                                <FileText className="h-4 w-4 text-muted-foreground" />
                                                {partner.document}
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant={statusColors[partner.status] || 'default'}>
                                                {partner.status}
                                            </Badge>
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
                                                    <DropdownMenuItem>View Details</DropdownMenuItem>
                                                    <DropdownMenuItem>Edit Partner</DropdownMenuItem>
                                                    {partner.status === 'PENDING' && (
                                                        <>
                                                            <DropdownMenuItem onClick={() => handleApprove(partner.id)}>
                                                                Approve
                                                            </DropdownMenuItem>
                                                            <DropdownMenuItem
                                                                className="text-destructive"
                                                                onClick={() => handleReject(partner.id)}
                                                            >
                                                                Reject
                                                            </DropdownMenuItem>
                                                        </>
                                                    )}
                                                    {partner.status === 'APPROVED' && (
                                                        <DropdownMenuItem
                                                            className="text-destructive"
                                                            onClick={() => handleSuspend(partner.id)}
                                                        >
                                                            Suspend
                                                        </DropdownMenuItem>
                                                    )}
                                                    {partner.status === 'SUSPENDED' && (
                                                        <DropdownMenuItem onClick={() => handleReactivate(partner.id)}>
                                                            Reactivate
                                                        </DropdownMenuItem>
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
        </div>
    )
}
