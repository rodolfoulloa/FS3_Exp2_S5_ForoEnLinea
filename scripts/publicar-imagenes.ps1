<#
  Publica las imágenes de Ágora en Docker Hub (Container Registry).

  Uso (PowerShell, desde la raíz del repositorio):
    docker login
    .\scripts\publicar-imagenes.ps1 -Usuario tu_usuario_dockerhub -Version 1.0.0

  Construye las 4 imágenes con docker compose y las sube con dos etiquetas:
  la versión indicada y "latest".
#>
param(
    [Parameter(Mandatory = $true)][string]$Usuario,
    [string]$Version = "1.0.0"
)
$ErrorActionPreference = "Stop"
$env:DOCKERHUB_USER = $Usuario
$env:VERSION = $Version

Write-Host "Construyendo imágenes $Usuario/agora-*:$Version ..." -ForegroundColor Cyan
docker compose build
if ($LASTEXITCODE -ne 0) { throw "Falló la construcción de las imágenes" }

foreach ($servicio in "ms-usuarios", "ms-foro", "ms-moderacion", "frontend") {
    $imagen = "$Usuario/agora-${servicio}"
    docker tag "${imagen}:$Version" "${imagen}:latest"
    Write-Host "Publicando ${imagen}:$Version" -ForegroundColor Cyan
    docker push "${imagen}:$Version"
    if ($LASTEXITCODE -ne 0) { throw "Falló la publicación de ${imagen}:$Version (¿ejecutaste docker login?)" }
    docker push "${imagen}:latest"
}
Write-Host "Listo: imágenes publicadas en https://hub.docker.com/u/$Usuario" -ForegroundColor Green
