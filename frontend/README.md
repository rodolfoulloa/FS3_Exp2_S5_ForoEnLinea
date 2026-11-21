# Ágora · Frontend (Angular 22.3)

Aplicación web del Sistema de Foros en Línea. Funciona **por separado del backend**, como pide la pauta: toda la información se maneja con **arreglos de Angular** (`src/app/data/datos-iniciales.ts`), los mismos datos de prueba que cargan los microservicios en Oracle.

```powershell
npm install
npm start                  # http://localhost:4200
npx ng test --watch=false  # 34 pruebas Vitest
npm run build              # dist/frontend/browser
```

Usuarios de prueba: `camila.admin` / `Admin#2025` (ADMIN) y `vale.soto` / `Foro#2025` (USUARIO). En el panel de administración, "Restablecer datos de demostración" vuelve a los arreglos iniciales.

La documentación completa (pantallas, patrones, responsive y validaciones) está en el [README del repositorio](../README.md).
