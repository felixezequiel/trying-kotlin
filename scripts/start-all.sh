#!/bin/bash
# Script para iniciar todos os serviços em desenvolvimento
# Uso: ./scripts/start-all.sh

echo "========================================"
echo "  Ticket System - Starting All Services"
echo "========================================"
echo ""

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# Função para cleanup
cleanup() {
    echo ""
    echo "Stopping all services..."
    kill $(jobs -p) 2>/dev/null
    echo "All services stopped."
    exit 0
}

trap cleanup SIGINT SIGTERM

echo "Starting Backend Services..."
echo ""

# Iniciar serviços em background
echo "Starting Users Service on port 8081..."
(cd "$ROOT_DIR/services/users" && gradle run) &

echo "Starting Events Service on port 8082..."
(cd "$ROOT_DIR/services/events" && gradle run) &

echo "Starting Partners Service on port 8083..."
(cd "$ROOT_DIR/services/partners" && gradle run) &

echo "Starting Tickets Service on port 8084..."
(cd "$ROOT_DIR/services/tickets" && gradle run) &

echo "Starting Orders Service on port 8085..."
(cd "$ROOT_DIR/services/orders" && gradle run) &

echo "Starting Reservations Service on port 8086..."
(cd "$ROOT_DIR/services/reservations" && gradle run) &

echo ""
echo "Waiting for services to start (15 seconds)..."
sleep 15

echo "Starting BFF on port 8080..."
(cd "$ROOT_DIR/bff" && gradle run) &

echo ""
echo "Waiting for BFF to start (10 seconds)..."
sleep 10

echo "Starting UI on port 3000..."
(cd "$ROOT_DIR/ui" && npm install && npm run dev) &

echo ""
echo "========================================"
echo "  All Services Started!"
echo "========================================"
echo ""
echo "Services running:"
echo "  - Users Service:        http://localhost:8081"
echo "  - Events Service:       http://localhost:8082"
echo "  - Partners Service:     http://localhost:8083"
echo "  - Tickets Service:      http://localhost:8084"
echo "  - Orders Service:       http://localhost:8085"
echo "  - Reservations Service: http://localhost:8086"
echo "  - BFF:                  http://localhost:8080"
echo "  - UI:                   http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop all services..."

# Aguardar
wait
