'use client'

import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { useToast } from '@/components/ui/use-toast'
import { Moon, Sun, Laptop, Palette } from 'lucide-react'
import { useTheme } from 'next-themes'

export default function SettingsPage() {
    const { theme, setTheme } = useTheme()
    const { toast } = useToast()
    const [apiUrl, setApiUrl] = useState('http://localhost:8080')

    const themes = [
        { name: 'light', label: 'Light', icon: Sun },
        { name: 'dark', label: 'Dark', icon: Moon },
        { name: 'system', label: 'System', icon: Laptop },
    ]

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
                <p className="text-muted-foreground">
                    Manage your application preferences
                </p>
            </div>

            {/* Theme Settings */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Palette className="h-5 w-5" />
                        Appearance
                    </CardTitle>
                    <CardDescription>
                        Customize the look and feel of the application
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                    <div className="space-y-2">
                        <Label>Theme</Label>
                        <p className="text-sm text-muted-foreground">
                            Select your preferred theme for the dashboard
                        </p>
                        <div className="grid grid-cols-3 gap-4 pt-2">
                            {themes.map((t) => (
                                <button
                                    key={t.name}
                                    onClick={() => setTheme(t.name)}
                                    className={`flex flex-col items-center gap-2 rounded-lg border-2 p-4 transition-colors hover:bg-accent ${theme === t.name
                                            ? 'border-primary bg-accent'
                                            : 'border-transparent'
                                        }`}
                                >
                                    <t.icon className="h-6 w-6" />
                                    <span className="text-sm font-medium">{t.label}</span>
                                </button>
                            ))}
                        </div>
                    </div>

                    <Separator />

                    <div className="space-y-2">
                        <Label>Color Scheme</Label>
                        <p className="text-sm text-muted-foreground">
                            Choose your accent color (coming soon)
                        </p>
                        <div className="flex gap-2 pt-2">
                            {['violet', 'blue', 'green', 'orange', 'red'].map((color) => (
                                <button
                                    key={color}
                                    className={`h-8 w-8 rounded-full transition-transform hover:scale-110 ${color === 'violet' ? 'ring-2 ring-offset-2 ring-primary' : ''
                                        }`}
                                    style={{
                                        backgroundColor:
                                            color === 'violet'
                                                ? 'hsl(262.1, 83.3%, 57.8%)'
                                                : color === 'blue'
                                                    ? 'hsl(217.2, 91.2%, 59.8%)'
                                                    : color === 'green'
                                                        ? 'hsl(142.1, 76.2%, 36.3%)'
                                                        : color === 'orange'
                                                            ? 'hsl(24.6, 95%, 53.1%)'
                                                            : 'hsl(0, 84.2%, 60.2%)',
                                    }}
                                />
                            ))}
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* API Settings */}
            <Card>
                <CardHeader>
                    <CardTitle>API Configuration</CardTitle>
                    <CardDescription>
                        Configure the backend API connection
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="grid gap-2">
                        <Label htmlFor="apiUrl">API Base URL</Label>
                        <Input
                            id="apiUrl"
                            value={apiUrl}
                            onChange={(e) => setApiUrl(e.target.value)}
                            placeholder="http://localhost:8080"
                        />
                        <p className="text-sm text-muted-foreground">
                            The base URL for the BFF API
                        </p>
                    </div>
                    <Button
                        onClick={() => {
                            toast({ title: 'Settings saved successfully' })
                        }}
                    >
                        Save Changes
                    </Button>
                </CardContent>
            </Card>

            {/* About */}
            <Card>
                <CardHeader>
                    <CardTitle>About</CardTitle>
                    <CardDescription>
                        Application information
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-2">
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">Version</span>
                        <span className="font-medium">1.0.0</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">Framework</span>
                        <span className="font-medium">Next.js 14</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-muted-foreground">UI Library</span>
                        <span className="font-medium">shadcn/ui</span>
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}
