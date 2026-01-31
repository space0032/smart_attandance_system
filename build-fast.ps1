# Fast Docker Build Script with BuildKit
# Run this script to build with maximum speed optimizations

Write-Host "🚀 Building with Docker BuildKit optimizations..." -ForegroundColor Cyan

# Enable BuildKit
$env:DOCKER_BUILDKIT=1
$env:COMPOSE_DOCKER_CLI_BUILD=1
$env:BUILDKIT_PROGRESS="plain"

Write-Host "✅ BuildKit enabled" -ForegroundColor Green

# Build with parallel execution
Write-Host "📦 Building images in parallel..." -ForegroundColor Cyan
docker-compose build --parallel

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build completed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "To start the services, run:" -ForegroundColor Yellow
    Write-Host "  docker-compose up -d" -ForegroundColor White
} else {
    Write-Host "❌ Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}
