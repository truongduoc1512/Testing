Write-Host "====================================="
Write-Host "Starting ShoeShop Test Environment..."
Write-Host "====================================="

docker compose down

docker compose up -d

Write-Host ""
Write-Host "Running containers:"
docker ps

Write-Host ""
Write-Host "Environment started successfully."