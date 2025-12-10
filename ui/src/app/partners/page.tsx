'use client'

import { useState, useEffect } from 'react'
import { Plus, MoreHorizontal, Building2, Mail, FileText, Pencil, Eye } from 'lucide-react'
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
import { api, Partner, CreatePartnerRequest } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'
import { validateDocument } from '@/lib/validators'

const statusColors: Record<string, 'default' | 'secondary' | 'destructive' | 'success' | 'warning'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'destructive',
    SUSPENDED: 'secondary',
}

// Funções de máscara
const maskCNPJ = (value: string): string => {
    const numbers = value.replace(/\D/g, '').slice(0, 14)
    return numbers
        .replace(/^(\d{2})(\d)/, '$1.$2')
        .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
        .replace(/\.(\d{3})(\d)/, '.$1/$2')
        .replace(/(\d{4})(\d)/, '$1-$2')
}

const maskPhone = (value: string): string => {
    const numbers = value.replace(/\D/g, '').slice(0, 11)
    if (numbers.length <= 10) {
        return numbers
            .replace(/^(\d{2})(\d)/, '($1) $2')
            .replace(/(\d{4})(\d)/, '$1-$2')
    }
    return numbers
        .replace(/^(\d{2})(\d)/, '($1) $2')
        .replace(/(\d{5})(\d)/, '$1-$2')
}

const unmask = (value: string): string => value.replace(/\D/g, '')

export default function PartnersPage() {
    const [partners, setPartners] = useState<Partner[]>([])
    const [loading, setLoading] = useState(true)
    const [isCreateOpen, setIsCreateOpen] = useState(false)
    const [isEditOpen, setIsEditOpen] = useState(false)
    const [editingPartner, setEditingPartner] = useState<Partner | null>(null)
    const { toast } = useToast()

    const [newPartner, setNewPartner] = useState<CreatePartnerRequest>({
        companyName: '',
        tradeName: '',
        document: '',
        documentType: 'CNPJ',
        email: '',
        phone: '',
    })

    const [editForm, setEditForm] = useState({
        companyName: '',
        tradeName: '',
        email: '',
        phone: '',
    })

    const fetchPartners = async () => {
        try {
            const data = await api.getPartners()
            setPartners(data)
        } catch (error) {
            console.error('Failed to fetch partners:', error)
            toast({ title: 'Failed to load partners', variant: 'destructive' })
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

    const handleCreatePartner = async () => {
        if (!newPartner.companyName || !newPartner.email || !newPartner.document || !newPartner.phone) {
            toast({ title: 'Please fill all required fields', variant: 'destructive' })
            return
        }

        // Validação de CNPJ (ADR-011)
        const documentValidation = validateDocument(newPartner.document, 'CNPJ')
        if (!documentValidation.valid) {
            toast({ title: documentValidation.error || 'Invalid CNPJ', variant: 'destructive' })
            return
        }

        try {
            // Enviar documento sem máscara
            const partnerData = {
                ...newPartner,
                document: unmask(newPartner.document),
                phone: unmask(newPartner.phone),
            }
            await api.createPartner(partnerData)
            toast({ title: 'Partner created successfully' })
            setIsCreateOpen(false)
            setNewPartner({ companyName: '', tradeName: '', document: '', documentType: 'CNPJ', email: '', phone: '' })
            fetchPartners()
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Failed to create partner'
            toast({ title: message, variant: 'destructive' })
        }
    }

    const handleUpdatePartner = async () => {
        if (!editingPartner) return
        try {
            const updateData = {
                ...editForm,
                phone: unmask(editForm.phone),
            }
            await api.updatePartner(editingPartner.id, updateData)
            toast({ title: 'Partner updated successfully' })
            setIsEditOpen(false)
            setEditingPartner(null)
            fetchPartners()
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Failed to update partner'
            toast({ title: message, variant: 'destructive' })
        }
    }

    const openEditDialog = (partner: Partner) => {
        setEditingPartner(partner)
        setEditForm({
            companyName: partner.companyName,
            tradeName: partner.tradeName || '',
            email: partner.email,
            phone: maskPhone(partner.phone)
        })
        setIsEditOpen(true)
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
                                <Label htmlFor="name">Company Name *</Label>
                                <Input
                                    id="name"
                                    placeholder="Enter company name"
                                    value={newPartner.companyName}
                                    onChange={(e) => setNewPartner({ ...newPartner, companyName: e.target.value })}
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="email">Email *</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="Enter email"
                                    value={newPartner.email}
                                    onChange={(e) => setNewPartner({ ...newPartner, email: e.target.value })}
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="document">Document (CNPJ) *</Label>
                                <Input
                                    id="document"
                                    placeholder="00.000.000/0001-00"
                                    value={newPartner.document}
                                    onChange={(e) => setNewPartner({ ...newPartner, document: maskCNPJ(e.target.value) })}
                                />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="phone">Phone *</Label>
                                <Input
                                    id="phone"
                                    placeholder="(11) 99999-9999"
                                    value={newPartner.phone}
                                    onChange={(e) => setNewPartner({ ...newPartner, phone: maskPhone(e.target.value) })}
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                                Cancel
                            </Button>
                            <Button onClick={handleCreatePartner}>
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
                                                {partner.companyName}
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
                                                    <DropdownMenuItem onClick={() => openEditDialog(partner)}>
                                                        <Pencil className="mr-2 h-4 w-4" />
                                                        Edit Partner
                                                    </DropdownMenuItem>
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

            {/* Edit Partner Dialog */}
            <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Edit Partner</DialogTitle>
                        <DialogDescription>
                            Update partner information.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                            <Label htmlFor="edit-companyName">Company Name</Label>
                            <Input
                                id="edit-companyName"
                                value={editForm.companyName}
                                onChange={(e) => setEditForm({ ...editForm, companyName: e.target.value })}
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="edit-tradeName">Trade Name</Label>
                            <Input
                                id="edit-tradeName"
                                value={editForm.tradeName}
                                onChange={(e) => setEditForm({ ...editForm, tradeName: e.target.value })}
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="edit-email">Email</Label>
                            <Input
                                id="edit-email"
                                type="email"
                                value={editForm.email}
                                onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label htmlFor="edit-phone">Phone</Label>
                            <Input
                                id="edit-phone"
                                value={editForm.phone}
                                onChange={(e) => setEditForm({ ...editForm, phone: maskPhone(e.target.value) })}
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label>Document (CNPJ)</Label>
                            <Input
                                disabled
                                value={maskCNPJ(editingPartner?.document || '')}
                            />
                            <p className="text-xs text-muted-foreground">Document cannot be changed</p>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsEditOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleUpdatePartner}>
                            Save Changes
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    )
}
