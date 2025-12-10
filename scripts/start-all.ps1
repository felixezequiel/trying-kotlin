# Script para iniciar todos os serviços em desenvolvimento
# Uso: .\scripts\start-all.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Ticket System - Starting All Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$rootDir = Split-Path -Parent $PSScriptRoot

# Função para iniciar um serviço Gradle em background
function Start-GradleService {
    param (
        [string]$ServiceName,
        [string]$ServicePath,
        [int]$Port
    )
    
    Write-Host "Starting $ServiceName on port $Port..." -ForegroundColor Yellow
    
    $job = Start-Job -ScriptBlock {
        param($path)
        Set-Location $path
        & gradle run
    } -ArgumentList "$rootDir\$ServicePath"
    
    return $job
}

# Array para armazenar os jobs
$jobs = @()

# Iniciar serviços backend
Write-Host ""
Write-Host "Starting Backend Services..." -ForegroundColor Green
Write-Host ""

# Users Service (8081)
$jobs += Start-GradleService -ServiceName "Users Service" -ServicePath "services\users" -Port 8081

# Events Service (8082)
$jobs += Start-GradleService -ServiceName "Events Service" -ServicePath "services\events" -Port 8082

# Partners Service (8083)
$jobs += Start-GradleService -ServiceName "Partners Service" -ServicePath "services\partners" -Port 8083

# Tickets Service (8084)
$jobs += Start-GradleService -ServiceName "Tickets Service" -ServicePath "services\tickets" -Port 8084

# Aguardar serviços iniciarem
Write-Host ""
Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# BFF (8080)
$jobs += Start-GradleService -ServiceName "BFF" -ServicePath "bff" -Port 8080

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  All Backend Services Started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Services running:" -ForegroundColor Cyan
Write-Host "  - Users Service:    http://localhost:8081" -ForegroundColor White
Write-Host "  - Events Service:   http://localhost:8082" -ForegroundColor White
Write-Host "  - Partners Service: http://localhost:8083" -ForegroundColor White
Write-Host "  - Tickets Service:  http://localhost:8084" -ForegroundColor White
Write-Host "  - BFF:              http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "To start the UI, run in a new terminal:" -ForegroundColor Yellow
Write-Host "  cd ui && npm install && npm run dev" -ForegroundColor White
Write-Host ""
Write-Host "UI will be available at: http://localhost:3000" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop all services..." -ForegroundColor Red

# Aguardar interrupção
try {
    Wait-Job -Job $jobs
} finally {
    Write-Host ""
    Write-Host "Stopping all services..." -ForegroundColor Yellow
    $jobs | Stop-Job
    $jobs | Remove-Job
    Write-Host "All services stopped." -ForegroundColor Green
}
