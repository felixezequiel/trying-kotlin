'use client'

import { useState, useEffect } from 'react'
import { ShoppingCart, DollarSign, CreditCard, RefreshCw, Eye, MoreHorizontal, Ticket } from 'lucide-react'
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
    DialogFooter,
    DialogHeader,
    DialogTitle,
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
import { api, Order, IssuedTicket, User } from '@/lib/api'
import { useToast } from '@/components/ui/use-toast'

const statusColors: Record<string, 'default' | 'secondary' | 'destructive' | 'success' | 'warning'> = {
    PENDING: 'warning',
    PAID: 'success',
    CANCELLED: 'destructive',
    REFUNDED: 'secondary',
}

export default function OrdersPage() {
    const [orders, setOrders] = useState<Order[]>([])
    const [users, setUsers] = useState<User[]>([])
    const [loading, setLoading] = useState(true)
    const [selectedOrder, setSelectedOrder] = useState<Order | null>(null)
    const [orderTickets, setOrderTickets] = useState<IssuedTicket[]>([])
    const [isTicketsDialogOpen, setIsTicketsDialogOpen] = useState(false)
    const [isPaymentDialogOpen, setIsPaymentDialogOpen] = useState(false)
    const [paymentMethod, setPaymentMethod] = useState('credit_card')
    const { toast } = useToast()

    const fetchOrders = async () => {
        try {
            // For admin view, we'd need an endpoint to list all orders
            // For now, we'll fetch orders for each user
            const usersData = await api.getUsers()
            setUsers(usersData)

            const allOrders: Order[] = []
            for (const user of usersData) {
                try {
                    const userOrders = await api.getMyOrders(user.id)
                    allOrders.push(...userOrders)
                } catch {
                    // User might not have orders
                }
            }
            setOrders(allOrders)
        } catch (error) {
            console.error('Failed to fetch orders:', error)
            toast({ title: 'Failed to load orders', variant: 'destructive' })
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchOrders()
    }, [])

    const handleViewTickets = async (order: Order) => {
        try {
            const tickets = await api.getOrderTickets(order.id)
            setOrderTickets(tickets)
            setSelectedOrder(order)
            setIsTicketsDialogOpen(true)
        } catch (error) {
            toast({ title: 'Failed to load tickets', variant: 'destructive' })
        }
    }

    const handleOpenPayment = (order: Order) => {
        setSelectedOrder(order)
        setIsPaymentDialogOpen(true)
    }

    const handleProcessPayment = async () => {
        if (!selectedOrder) return
        try {
            await api.processPayment(selectedOrder.id, {
                paymentMethod,
                paymentDetails: {},
            })
            toast({ title: 'Payment processed successfully' })
            setIsPaymentDialogOpen(false)
            fetchOrders()
        } catch (error) {
            toast({ title: 'Failed to process payment', variant: 'destructive' })
        }
    }

    const handleRefund = async (orderId: string) => {
        try {
            await api.refundOrder(orderId)
            toast({ title: 'Order refunded successfully' })
            fetchOrders()
        } catch (error) {
            toast({ title: 'Failed to refund order', variant: 'destructive' })
        }
    }

    const handleValidateTicket = async (code: string) => {
        try {
            await api.validateTicket(code)
            toast({ title: 'Ticket validated successfully' })
            if (selectedOrder) {
                const tickets = await api.getOrderTickets(selectedOrder.id)
                setOrderTickets(tickets)
            }
        } catch (error) {
            toast({ title: 'Failed to validate ticket', variant: 'destructive' })
        }
    }

    const getUserName = (customerId: string) => {
        const user = users.find(u => u.id === customerId)
        return user?.name || customerId
    }

    const totalRevenue = orders
        .filter(o => o.status === 'PAID')
        .reduce((acc, o) => acc + o.totalAmount, 0)

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Orders</h1>
                    <p className="text-muted-foreground">
                        Manage customer orders and payments
                    </p>
                </div>
                <Button variant="outline" onClick={fetchOrders}>
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Refresh
                </Button>
            </div>

            {/* Stats Cards */}
            <div className="grid gap-4 md:grid-cols-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Orders</CardTitle>
                        <ShoppingCart className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{orders.length}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Revenue</CardTitle>
                        <DollarSign className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            ${totalRevenue.toFixed(2)}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Pending</CardTitle>
                        <CreditCard className="h-4 w-4 text-yellow-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {orders.filter(o => o.status === 'PENDING').length}
                        </div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Paid</CardTitle>
                        <ShoppingCart className="h-4 w-4 text-green-500" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">
                            {orders.filter(o => o.status === 'PAID').length}
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Orders Table */}
            <Card>
                <CardHeader>
                    <CardTitle>All Orders</CardTitle>
                    <CardDescription>View and manage all customer orders</CardDescription>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="space-y-3">
                            {[1, 2, 3].map((i) => (
                                <div key={i} className="h-12 animate-pulse rounded bg-muted" />
                            ))}
                        </div>
                    ) : orders.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-8 text-center">
                            <ShoppingCart className="h-12 w-12 text-muted-foreground" />
                            <h3 className="mt-4 text-lg font-semibold">No orders yet</h3>
                            <p className="text-sm text-muted-foreground">
                                Orders will appear here when customers make purchases
                            </p>
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Order ID</TableHead>
                                    <TableHead>Customer</TableHead>
                                    <TableHead className="text-right">Amount</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead>Created</TableHead>
                                    <TableHead className="w-[70px]"></TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {orders.map((order) => (
                                    <TableRow key={order.id}>
                                        <TableCell className="font-mono text-sm">
                                            {order.id.slice(0, 8)}...
                                        </TableCell>
                                        <TableCell>{getUserName(order.customerId)}</TableCell>
                                        <TableCell className="text-right font-medium">
                                            ${order.totalAmount.toFixed(2)}
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant={statusColors[order.status]}>
                                                {order.status}
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="text-sm text-muted-foreground">
                                            {new Date(order.createdAt).toLocaleDateString()}
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
                                                    <DropdownMenuItem onClick={() => handleViewTickets(order)}>
                                                        <Ticket className="mr-2 h-4 w-4" />
                                                        View Tickets
                                                    </DropdownMenuItem>
                                                    {order.status === 'PENDING' && (
                                                        <DropdownMenuItem onClick={() => handleOpenPayment(order)}>
                                                            <CreditCard className="mr-2 h-4 w-4" />
                                                            Process Payment
                                                        </DropdownMenuItem>
                                                    )}
                                                    {order.status === 'PAID' && (
                                                        <DropdownMenuItem
                                                            className="text-destructive"
                                                            onClick={() => handleRefund(order.id)}
                                                        >
                                                            Refund Order
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

            {/* View Tickets Dialog */}
            <Dialog open={isTicketsDialogOpen} onOpenChange={setIsTicketsDialogOpen}>
                <DialogContent className="sm:max-w-[600px]">
                    <DialogHeader>
                        <DialogTitle>Order Tickets</DialogTitle>
                        <DialogDescription>
                            Tickets for order {selectedOrder?.id.slice(0, 8)}...
                        </DialogDescription>
                    </DialogHeader>
                    <div className="py-4">
                        {orderTickets.length === 0 ? (
                            <p className="text-center text-muted-foreground">No tickets found</p>
                        ) : (
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Code</TableHead>
                                        <TableHead>Status</TableHead>
                                        <TableHead>Used At</TableHead>
                                        <TableHead></TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {orderTickets.map((ticket) => (
                                        <TableRow key={ticket.id}>
                                            <TableCell className="font-mono">{ticket.code}</TableCell>
                                            <TableCell>
                                                <Badge variant={ticket.status === 'VALID' ? 'success' : 'secondary'}>
                                                    {ticket.status}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                {ticket.usedAt
                                                    ? new Date(ticket.usedAt).toLocaleString()
                                                    : '-'}
                                            </TableCell>
                                            <TableCell>
                                                {ticket.status === 'VALID' && (
                                                    <Button
                                                        size="sm"
                                                        variant="outline"
                                                        onClick={() => handleValidateTicket(ticket.code)}
                                                    >
                                                        Validate
                                                    </Button>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        )}
                    </div>
                </DialogContent>
            </Dialog>

            {/* Process Payment Dialog */}
            <Dialog open={isPaymentDialogOpen} onOpenChange={setIsPaymentDialogOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Process Payment</DialogTitle>
                        <DialogDescription>
                            Process payment for order {selectedOrder?.id.slice(0, 8)}...
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                            <Label>Amount</Label>
                            <Input
                                disabled
                                value={`$${selectedOrder?.totalAmount.toFixed(2) || '0.00'}`}
                            />
                        </div>
                        <div className="grid gap-2">
                            <Label>Payment Method</Label>
                            <Select value={paymentMethod} onValueChange={setPaymentMethod}>
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="credit_card">Credit Card</SelectItem>
                                    <SelectItem value="debit_card">Debit Card</SelectItem>
                                    <SelectItem value="pix">PIX</SelectItem>
                                    <SelectItem value="boleto">Boleto</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsPaymentDialogOpen(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleProcessPayment}>
                            Confirm Payment
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    )
}
