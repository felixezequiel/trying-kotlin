# Script para instalar Node.js LTS via NVM
# Execute apos instalar o NVM e reabrir o terminal

$ErrorActionPreference = "Stop"

Write-Host "=== Instalando Node.js LTS via NVM ===" -ForegroundColor Cyan

# Verificar se NVM esta disponivel
$nvmCommand = Get-Command nvm -ErrorAction SilentlyContinue
if (-not $nvmCommand) {
    Write-Host "ERRO: NVM nao encontrado. Certifique-se de:" -ForegroundColor Red
    Write-Host "  1. Ter executado install-nvm-node.ps1" -ForegroundColor Yellow
    Write-Host "  2. Ter fechado e reaberto o terminal" -ForegroundColor Yellow
    exit 1
}

Write-Host "Instalando Node.js LTS..." -ForegroundColor Green
nvm install lts

Write-Host "Ativando Node.js LTS..." -ForegroundColor Green
nvm use lts

Write-Host ""
Write-Host "=== Verificando instalacao ===" -ForegroundColor Cyan
Write-Host "NVM version:" -ForegroundColor White
nvm version

Write-Host "Node version:" -ForegroundColor White
node --version

Write-Host "NPM version:" -ForegroundColor White
npm --version

Write-Host ""
Write-Host "Node.js LTS instalado com sucesso!" -ForegroundColor Green
