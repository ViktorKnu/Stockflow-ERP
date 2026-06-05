# StockFlow ERP API

StockFlow ERP er en produksjonsnær inventory-, ordre- og finansiell ledger-API for bedrifter. Prosjektet bygges med Java 21, Spring Boot, PostgreSQL og Docker, og er laget for å vise solid backend-arbeid: ren arkitektur, domenelogikk, transaksjoner, database-migrasjoner, testing, dokumentasjon og CI/CD.

Siste push på `main` inneholder prosjektgrunnmuren, leverandør-API-et, produkt-API-et og lagerbevegelser: Spring Boot, Docker, PostgreSQL, Flyway, OpenAPI, Actuator, global feilhåndtering, Supplier CRUD, Product CRUD og Inventory Movement workflow.

## Start her

Alle kommandoer under kjøres fra prosjektmappen. I mitt tilfelle:

```powershell
cd C:\Users\vikto\Downloads\Github\Stockflow-ERP
```

Start hele systemet med Docker:

```powershell
docker compose up --build
```

Når appen er startet, åpne disse i nettleseren:

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui.html
```

Health-endepunktet skal svare omtrent slik:

```json
{"status":"UP"}
```

Swagger UI er der du kan se og teste API-endepunkter uten å skrive egne HTTP-kall.

Stopp appen med:

```powershell
Ctrl + C
```

Rydd containere, nettverk og databasevolum:

```powershell
docker compose down -v
```

Bruk `-v` når du vil starte med helt tom database. Ikke bruk `-v` hvis du vil beholde data mellom kjøringer.

## Teste hver push rent

Vi bygger prosjektet med mange små commits. Hvis du har lokale filer som ikke er pushet ennå, kan Docker bygge med de filene og gi et annet resultat enn siste push på GitHub. For å teste nøyaktig det som er commitet, bruk en ren Git worktree.

Lag en ren testkopi av siste commit:

```powershell
git -C C:\Users\vikto\Downloads\Github\Stockflow-ERP worktree add C:\Users\vikto\Downloads\Github\Stockflow-ERP-foundation-test HEAD
```

Gå inn i testkopien:

```powershell
cd C:\Users\vikto\Downloads\Github\Stockflow-ERP-foundation-test
```

Start appen:

```powershell
docker compose up --build
```

Test:

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui.html
```

Når du er ferdig:

```powershell
Ctrl + C
docker compose down -v
cd C:\Users\vikto\Downloads\Github\Stockflow-ERP
git worktree remove C:\Users\vikto\Downloads\Github\Stockflow-ERP-foundation-test
```

Hvis worktree-mappen allerede finnes, slett den eller bruk et annet navn, for eksempel:

```powershell
git -C C:\Users\vikto\Downloads\Github\Stockflow-ERP worktree add C:\Users\vikto\Downloads\Github\Stockflow-ERP-test-2 HEAD
```

## Docker-sjekk

Sjekk at Docker Desktop kjører:

```powershell
docker version
```

Du skal se både `Client` og `Server`. Hvis du bare ser `Client`, åpne Docker Desktop og vent til den er ferdig startet.

Vanlige feil:

- `failed to connect to the docker API`: Docker Desktop kjører ikke.
- `port is already allocated`: En annen prosess bruker porten. Stopp gamle containere med `docker compose down`.
- Appen starter ikke etter databaseendringer: Rydd databasevolumet med `docker compose down -v`.

Se kjørende containere:

```powershell
docker ps
```

Se alle containere, også stoppede:

```powershell
docker ps -a
```

Se logger for appen:

```powershell
docker logs stockflow-api
```

Se logger for databasen:

```powershell
docker logs stockflow-db
```

## Nyttige lokale URL-er

App:

```text
http://localhost:8080
```

Health:

```text
http://localhost:8080/actuator/health
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

PostgreSQL fra maskinen din:

```text
localhost:5432
database: stockflow
username: stockflow
password: stockflow
```

pgAdmin, hvis startet med tools-profil:

```text
http://localhost:5050
```

Start med pgAdmin:

```powershell
docker compose --profile tools up --build
```

## Teste leverandør-API

Den enkleste måten å teste API-et på er Swagger:

1. Start appen med `docker compose up --build`
2. Åpne `http://localhost:8080/swagger-ui.html`
3. Velg et endepunkt
4. Trykk `Try it out`
5. Fyll inn request body hvis endepunktet krever det
6. Trykk `Execute`

Tilgjengelige leverandør-endepunkter:

```text
GET    /api/suppliers
GET    /api/suppliers/{id}
POST   /api/suppliers
PUT    /api/suppliers/{id}
DELETE /api/suppliers/{id}
GET    /api/suppliers/{id}/products
```

Opprett en leverandør med `POST /api/suppliers`:

```json
{
  "name": "Nordic Supplies AS",
  "email": "orders@nordic.example",
  "phone": "+47 22 00 00 00",
  "address": "Oslo"
}
```

Du kan også teste med PowerShell:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/actuator/health
```

Eksempel på `POST` etter Supplier-commiten:

```powershell
$body = @{
  name = "Nordic Supplies AS"
  email = "orders@nordic.example"
  phone = "+47 22 00 00 00"
  address = "Oslo"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/suppliers -ContentType "application/json" -Body $body
```

Hent alle leverandører:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/suppliers
```

Oppdater leverandør med id `1`:

```powershell
$body = @{
  name = "Nordic Supplies Norge AS"
  email = "sales@nordic.example"
  phone = "+47 22 00 00 01"
  address = "Bergen"
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri http://localhost:8080/api/suppliers/1 -ContentType "application/json" -Body $body
```

Slett leverandør med id `1`:

```powershell
Invoke-RestMethod -Method Delete -Uri http://localhost:8080/api/suppliers/1
```

## Teste produkt-API

Produkter kan opprettes alene eller kobles til en leverandør med `supplierId`. Start med å opprette en leverandør, og bruk deretter `id` fra responsen når du lager produktet.

Tilgjengelige produkt-endepunkter:

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

Opprett et produkt med `POST /api/products`:

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

Lag et low-stock-produkt:

```json
{
  "name": "Receipt Paper",
  "sku": "PAPER-001",
  "description": "Thermal receipt paper rolls",
  "category": "Office",
  "quantity": 2,
  "minimumStock": 5,
  "price": 29.00,
  "supplierId": 1
}
```

Test med PowerShell:

```powershell
$body = @{
  name = "Barcode Scanner"
  sku = "SCAN-001"
  description = "USB barcode scanner for warehouse use"
  category = "Hardware"
  quantity = 12
  minimumStock = 3
  price = 799.00
  supplierId = 1
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/products -ContentType "application/json" -Body $body
```

Hent alle produkter:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/products
```

Søk etter produktnavn:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/products/search?name=barcode"
```

Hent low-stock-produkter:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/products/low-stock
```

Hent produkter for leverandør med id `1`:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/suppliers/1/products
```

## Teste lagerbevegelser

Lagerbevegelser er den første ordentlige business-workflowen. Når du oppretter en lagerbevegelse, oppdateres produktets `quantity` og systemet lagrer `previousQuantity` og `newQuantity` for sporbarhet.

Tilgjengelige lager-endepunkter:

```text
POST /api/inventory/movements
GET  /api/inventory/movements
GET  /api/inventory/movements/{id}
GET  /api/inventory/movements/product/{productId}
```

Bevegelsestyper:

```text
IN          øker lagerbeholdning
OUT         senker lagerbeholdning
ADJUSTMENT  setter lagerbeholdning til oppgitt quantity
```

Eksempel: legg 5 varer inn på lager for produkt med id `1`:

```json
{
  "productId": 1,
  "type": "IN",
  "quantity": 5,
  "reason": "Supplier delivery"
}
```

Eksempel: ta 3 varer ut av lager:

```json
{
  "productId": 1,
  "type": "OUT",
  "quantity": 3,
  "reason": "Customer order shipped"
}
```

Eksempel: manuell lagerjustering til 20:

```json
{
  "productId": 1,
  "type": "ADJUSTMENT",
  "quantity": 20,
  "reason": "Manual stock count"
}
```

PowerShell-eksempel:

```powershell
$body = @{
  productId = 1
  type = "OUT"
  quantity = 3
  reason = "Customer order shipped"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/inventory/movements -ContentType "application/json" -Body $body
```

Hent bevegelser for produkt med id `1`:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/inventory/movements/product/1
```

Systemet stopper `OUT` hvis bevegelsen ville gitt negativ lagerbeholdning.

## Status nå

- Maven-basert Spring Boot-prosjekt med Java 21
- Feature-basert pakkestruktur under `com.stockflow`
- PostgreSQL-konfigurasjon med Flyway-migrasjon
- Dockerfile og Docker Compose med app, database og valgfri pgAdmin-profil
- Swagger/OpenAPI på `/swagger-ui.html`
- Actuator health endpoint på `/actuator/health`
- Global exception handler med valideringsfeil
- GitHub Actions workflow for test og build
- Supplier CRUD
- Product CRUD
- Produktsøk, kategorifilter og low-stock-endepunkt
- Kobling mellom Product og Supplier
- Endepunkt for produkter per leverandør
- Lagerbevegelser med IN, OUT og ADJUSTMENT
- Transaksjonell oppdatering av produktbeholdning
- Sporbarhet med previousQuantity og newQuantity
- DTO-er for all API input/output

## Kommer videre

- Innkjøpsordre med mottak av varer
- Ledger-postering for innkjøp
- Audit log for viktige workflows

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
- Swagger/OpenAPI via springdoc
- JUnit 5, Mockito og Testcontainers
- Lombok
- GitHub Actions

## Arkitektur

Prosjektet bruker feature-basert struktur:

```text
src/main/java/com/stockflow
  config
  exception
  product
    dto
  supplier
    dto
```

Hver modul eier egne entity-klasser, repositories, services, controllers, DTO-er og mapper-logikk. Controllere eksponerer aldri JPA entities direkte.

## Domenemodell nå

`Supplier` representerer leverandører som kan levere produkter til bedriften. Modulen har egen entity, repository, service, controller, request DTO-er og response DTO.

`Product` representerer varer bedriften kjøper, lagrer og selger. SKU er unik, lagerbeholdning og minimumslager kan ikke være negative, pris må være positiv, og `Product` har `@Version` for optimistisk låsing før inventory-fasen.

`InventoryMovement` representerer alle lagerendringer. Hver bevegelse peker på et produkt, har type `IN`, `OUT` eller `ADJUSTMENT`, og lagrer både tidligere og ny lagerbeholdning.

## API-oversikt nå

Leverandører:

- `GET /api/suppliers`
- `GET /api/suppliers/{id}`
- `POST /api/suppliers`
- `PUT /api/suppliers/{id}`
- `DELETE /api/suppliers/{id}`
- `GET /api/suppliers/{id}/products`

Produkter:

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/products/search?name=`
- `GET /api/products/low-stock`
- `GET /api/products/category/{category}`

Lagerbevegelser:

- `POST /api/inventory/movements`
- `GET /api/inventory/movements`
- `GET /api/inventory/movements/{id}`
- `GET /api/inventory/movements/product/{productId}`

## Kjøre lokalt

Kopier miljøfilen:

```bash
cp .env.example .env
```

Start PostgreSQL og app:

```bash
docker compose up --build
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

Valgfri pgAdmin:

```bash
docker compose --profile tools up --build
```

## Miljøvariabler

Se `.env.example` for standardverdier:

- `APP_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT`
- `PGADMIN_EMAIL`
- `PGADMIN_PASSWORD`
- `PGADMIN_PORT`

## Testing

Kjør unit-tester:

```bash
mvn test
```

Bygg applikasjonen:

```bash
mvn -DskipTests package
```

GitHub Actions kjører tester og build på push og pull requests.

## Roadmap

- Fase 3: InventoryMovement med IN, OUT og ADJUSTMENT
- Fase 4: Purchase orders med receiving-workflow, ledger expense og audit log
- Fase 5: Sales orders med shipping-workflow, ledger revenue og audit log
- Fase 6: Ledger reporting og månedlig oppsummering
- Fase 7: Spring Security, JWT, brukere og roller
- Fase 8: Flere integrasjonstester, eksempeldata og Postman collection

## Screenshots

Plassholder for Swagger UI, Docker Compose og fremtidige workflow-bilder.

## Fremtidige forbedringer

- Testcontainers-baserte repository- og API-integrasjonstester
- AuditLog-modul for viktige systemhendelser
- Database-seeding for demo-data
- Rate limiting og request correlation IDs
- Bedre observability med metrics og structured logging
