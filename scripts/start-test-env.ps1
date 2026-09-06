Write-Host "====================================="
Write-Host "Starting ShoeShop Test Environment..."
Write-Host "====================================="

docker compose down
docker compose up -d

Write-Host ""
Write-Host "Waiting for MySQL container to become healthy..."
while ($(docker inspect --format='{{.State.Health.Status}}' shoeshop-mysql 2>$null) -ne "healthy") {
    Start-Sleep -Seconds 2
}

Write-Host "MySQL is Healthy. Auto-seeding database from seed_data.sql..."
Get-Content seed_data.sql | docker exec -i shoeshop-mysql mysql -uroot -ptruonghoaiduoc shoe_shopdb

Write-Host ""
Write-Host "Running containers:"
docker ps

Write-Host ""
Write-Host "Environment started and seed_data.sql applied successfully."