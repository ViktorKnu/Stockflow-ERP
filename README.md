# StockFlow ERP API

StockFlow ERP er en produksjonsnær inventory-, ordre- og finansiell ledger-API for bedrifter. Prosjektet bygges med Java 21, Spring Boot, PostgreSQL og Docker, og er laget for å vise realistisk backend-arbeid med domenelogikk, transaksjoner, Flyway-migrasjoner, testing, dokumentasjon og CI/CD.

## Rask start

Kjør alt fra prosjektmappen:

```powershell
cd C:\Users\vikto\Downloads\Github\Stockflow-ERP
docker compose up --build
```

Når appen er startet, åpne:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/actuator/health
```

`/actuator/health` skal svare:

```json
{"status":"UP"}
```

Stopp appen med `Ctrl + C`.

Rydd containere og databasevolum når du vil starte helt rent:

```powershell
docker compose down -v
```

Ikke bruk `-v` hvis du vil beholde data mellom kjøringer.

## Hva finnes nå

Prosjektet har nå:

- Spring Boot API med Java 21
- PostgreSQL via Docker Compose
- Flyway-migrasjoner for tabeller
- Swagger/OpenAPI
- Actuator health endpoint
- Global feilhåndtering
- GitHub Actions workflow
- Leverandører
- Produkter
- Lagerbevegelser
- Innkjøpsordre med mottak
- Salgsordre med ordrelinjer, statusflyt og shipping
- Ledger for innkjøpskostnader
- Audit log for viktige hendelser

Siste innkjøpsflyt er:

```text
Leverandør -> Produkt -> Innkjøpsordre -> Mottak -> Lager øker -> Ledger EXPENSE -> Audit log
```

Salgsordre finnes nå med shipping:

```text
Kunde -> Salgsordre -> Ordrelinjer -> CONFIRMED -> PAID -> SHIPPED -> Lager går ned
```

Ledger `REVENUE` for salg kommer i en egen commit etter shipping.

## Test i Swagger

Den enkleste måten å teste på er Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Anbefalt testrekkefølge:

1. Opprett leverandør med `POST /api/suppliers`
2. Opprett produkt med `POST /api/products`
3. Test lagerbevegelse med `POST /api/inventory/movements`
4. Opprett innkjøpsordre med `POST /api/purchase-orders`
5. Legg til ordrelinje med `POST /api/purchase-orders/{id}/items`
6. Motta innkjøpsordre med `POST /api/purchase-orders/{id}/receive`
7. Opprett salgsordre med `POST /api/sales-orders`
8. Legg til salgsordrelinje med `POST /api/sales-orders/{id}/items`
9. Bekreft salgsordre med `PUT /api/sales-orders/{id}/status`
10. Marker salgsordre som betalt med `PUT /api/sales-orders/{id}/status`
11. Send salgsordre med `POST /api/sales-orders/{id}/ship`
12. Sjekk lagerbevegelser med `GET /api/inventory/movements/product/{productId}`
13. Sjekk regnskap med `GET /api/ledger/summary`
14. Sjekk hendelser med `GET /api/audit-logs`

## Eksempeldata

Opprett leverandør:

```json
{
  "name": "Nordic Supplies AS",
  "email": "orders@nordic.example",
  "phone": "+47 22 00 00 00",
  "address": "Oslo"
}
```

Opprett produkt:

```json
{
  "name": "Barcode Scanner",
  "sku": "SCAN-001",
  "description": "USB barcode scanner for warehouse use",
  "category": "Hardware",
  "quantity": 12,
  "minimumStock": 3,
  "price": 799.00,
  "supplierId": 1
}
```

Lag lagerbevegelse ut:

```json
{
  "productId": 1,
  "type": "OUT",
  "quantity": 3,
  "reason": "Customer order shipped"
}
```

Opprett innkjøpsordre:

```json
{
  "supplierId": 1
}
```

Legg til ordrelinje:

```json
{
  "productId": 1,
  "quantity": 5,
  "unitPrice": 699.00
}
```

Motta ordren:

```text
POST /api/purchase-orders/1/receive
```

Når ordren mottas, skjer dette i én transaksjon:

- Ordren får status `RECEIVED`
- Produktets lagerbeholdning øker
- Det opprettes `InventoryMovement` av type `IN`
- Det opprettes `LedgerTransaction` av type `EXPENSE`
- Det opprettes audit log
- Samme ordre kan ikke mottas to ganger

Opprett salgsordre:

```json
{
  "customerName": "Ada Lovelace",
  "customerEmail": "ada@example.com"
}
```

Legg til salgsordrelinje:

```json
{
  "productId": 1,
  "quantity": 2,
  "unitPrice": 899.00
}
```

Bekreft salgsordren:

```json
{
  "status": "CONFIRMED"
}
```

Marker salgsordren som betalt:

```json
{
  "status": "PAID"
}
```

Send salgsordren:

```text
POST /api/sales-orders/1/ship
```

Ved bekreftelse og shipping sjekker systemet at produktene har nok lager. Shipping trekker lager med `InventoryMovement` av type `OUT`, setter ordren til `SHIPPED` og lager audit log. Salgsordrer kan ikke settes til `SHIPPED` via vanlig status-endepunkt.

## PowerShell-eksempler

Health check:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/actuator/health
```

Hent alle produkter:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/products
```

Hent low-stock-produkter:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/products/low-stock
```

Hent ledger summary:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/ledger/summary
```

Hent audit logs:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/audit-logs
```

## Lokale URL-er

```text
App:          http://localhost:8080
Swagger UI:   http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
Health:       http://localhost:8080/actuator/health
```

PostgreSQL fra maskinen:

```text
host:     localhost
port:     5432
database: stockflow
username: stockflow
password: stockflow
```

Valgfri pgAdmin:

```powershell
docker compose --profile tools up --build
```

```text
http://localhost:5050
```

## Miljøvariabler

Standardverdier ligger i `.env.example`:

```text
APP_PORT=8080
POSTGRES_DB=stockflow
POSTGRES_USER=stockflow
POSTGRES_PASSWORD=stockflow
POSTGRES_PORT=5432
PGADMIN_EMAIL=admin@stockflow.local
PGADMIN_PASSWORD=admin
PGADMIN_PORT=5050
```

Docker Compose bruker disse verdiene hvis de finnes i miljøet ditt. For vanlig lokal kjøring holder standardverdiene i repoet.

## API-oversikt

Leverandører:

```text
GET    /api/suppliers
GET    /api/suppliers/{id}
POST   /api/suppliers
PUT    /api/suppliers/{id}
DELETE /api/suppliers/{id}
GET    /api/suppliers/{id}/products
```

Produkter:

```text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
GET    /api/products/search?name=
GET    /api/products/low-stock
GET    /api/products/category/{category}
```

Lagerbevegelser:

```text
POST /api/inventory/movements
GET  /api/inventory/movements
GET  /api/inventory/movements/{id}
GET  /api/inventory/movements/product/{productId}
```

Innkjøpsordre:

```text
GET    /api/purchase-orders
GET    /api/purchase-orders/{id}
POST   /api/purchase-orders
POST   /api/purchase-orders/{id}/items
PUT    /api/purchase-orders/{id}/status
POST   /api/purchase-orders/{id}/receive
DELETE /api/purchase-orders/{id}
```

Salgsordre:

```text
GET    /api/sales-orders
GET    /api/sales-orders/{id}
POST   /api/sales-orders
POST   /api/sales-orders/{id}/items
PUT    /api/sales-orders/{id}/status
POST   /api/sales-orders/{id}/ship
DELETE /api/sales-orders/{id}
```

Ledger:

```text
GET /api/ledger/transactions
GET /api/ledger/transactions/{id}
GET /api/ledger/summary
GET /api/ledger/summary/monthly
```

Audit logs:

```text
GET /api/audit-logs
GET /api/audit-logs/{id}
GET /api/audit-logs/entity/{entityType}/{entityId}
```

## Docker og feilsøking

Sjekk at Docker Desktop kjører:

```powershell
docker version
```

Du skal se både `Client` og `Server`.

Vanlige problemer:

- `failed to connect to the docker API`: Docker Desktop kjører ikke.
- `port is already allocated`: Stopp gamle containere med `docker compose down`.
- Appen starter ikke etter databaseendringer: Kjør `docker compose down -v`.

Nyttige kommandoer:

```powershell
docker ps
docker ps -a
docker logs stockflow-api
docker logs stockflow-db
```

## Teste en ren commit

Hvis du vil teste akkurat det som er commitet, bruk en egen worktree:

```powershell
git -C C:\Users\vikto\Downloads\Github\Stockflow-ERP worktree add C:\Users\vikto\Downloads\Github\Stockflow-ERP-test HEAD
cd C:\Users\vikto\Downloads\Github\Stockflow-ERP-test
docker compose up --build
```

Når du er ferdig:

```powershell
Ctrl + C
docker compose down -v
cd C:\Users\vikto\Downloads\Github\Stockflow-ERP
git worktree remove C:\Users\vikto\Downloads\Github\Stockflow-ERP-test
```

## Arkitektur

Prosjektet bruker feature-basert struktur:

```text
src/main/java/com/stockflow
  audit
  config
  exception
  inventory
  ledger
  product
  purchaseorder
  salesorder
  supplier
```

Hver modul eier egne entities, repositories, services, controllers og DTO-er. Controllerne er tynne, businesslogikk ligger i service-laget, og API-et eksponerer DTO-er i stedet for JPA entities.

Viktige designvalg:

- `BigDecimal` brukes for penger
- `@Transactional` brukes rundt viktige workflows
- Flyway eier databaseskjemaet
- Produkter har optimistisk låsing med `@Version`
- Audit logs opprettes av business workflows

## Teknologistack

- Java 21
- Spring Boot 4
- Spring WebMVC
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Flyway
- Docker og Docker Compose
- Maven
- Swagger/OpenAPI
- JUnit 5
- Mockito
- Testcontainers
- Lombok
- GitHub Actions

## Testing

Kjør tester lokalt hvis Maven er installert:

```powershell
mvn test
```

Bygg applikasjonen:

```powershell
mvn -DskipTests package
```

Hvis Maven ikke er installert lokalt, bygger Docker prosjektet når du kjører:

```powershell
docker compose up --build
```

GitHub Actions kjører tester og build på push.

## Roadmap

Neste naturlige commits:

1. `før inntekt i regnskap`
2. `legg til månedlig rapport`
3. `legg til innlogging`
4. `legg til roller og tilgang`

## Screenshots

Plassholder for Swagger UI, Docker Compose og fremtidige workflow-bilder.

## Fremtidige forbedringer

- Flere integrasjonstester med Testcontainers
- Demo-data via Flyway eller seed-runner
- Postman collection
- Request correlation IDs
- Bedre observability med metrics og strukturert logging
