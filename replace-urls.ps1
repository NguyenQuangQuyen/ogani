# Script to replace all localhost URLs with production domain
# Run this in PowerShell from the project root directory

$FE_DOMAIN = "https://hgr0a62zxby.sn.mynetname.net:1411"
$BE_DOMAIN = "https://hgr0a62zxby.sn.mynetname.net:2003"

# Frontend files - Replace localhost:8080 with BE domain
Get-ChildItem -Path ".\FrontEnd\ogani\src" -Recurse -Include *.ts,*.html | ForEach-Object {
    (Get-Content $_.FullName) -replace 'http://localhost:8080', $BE_DOMAIN | Set-Content $_.FullName
}

# Frontend files - Replace localhost:4200 with FE domain  
Get-ChildItem -Path ".\FrontEnd\ogani\src" -Recurse -Include *.ts,*.html | ForEach-Object {
    (Get-Content $_.FullName) -replace 'http://localhost:4200', $FE_DOMAIN | Set-Content $_.FullName
}

# Backend files - Replace localhost:4200 with FE domain
Get-ChildItem -Path ".\BackEnd\ogani\src" -Recurse -Include *.java | ForEach-Object {
    (Get-Content $_.FullName) -replace 'http://localhost:4200', $FE_DOMAIN | Set-Content $_.FullName
}

# Backend files - Replace localhost:8080 with BE domain (except database URL)
Get-ChildItem -Path ".\BackEnd\ogani\src" -Recurse -Include *.java | ForEach-Object {
    $content = Get-Content $_.FullName
    $newContent = $content -replace 'http://localhost:8080', $BE_DOMAIN
    # Don't replace if it's a CORS origin for ImageController (should use BE domain)
    Set-Content $_.FullName -Value $newContent
}

Write-Host "✅ Đã thay thế tất cả localhost URLs!" -ForegroundColor Green
Write-Host "Frontend: $FE_DOMAIN" -ForegroundColor Cyan
Write-Host "Backend: $BE_DOMAIN" -ForegroundColor Cyan
