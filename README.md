# StockFlow ERP API

StockFlow ERP er et Java/Spring Boot API for lager, innkjøp, salg, en enkel ledger og audit log.

Tanken er enkel: en liten bedrift kjøper varer fra leverandører, legger dem på lager, selger dem til kunder og trenger spor på hva som skjedde underveis.

Dette repoet er bygget som et backend-prosjekt med ekte flyter, ikke bare en samling CRUD-endepunkter.

## Første gang her?

Du trenger:

- Docker Desktop
- Git
- En terminal, for eksempel PowerShell

Kjør prosjektet:

```powershell
git clone https://github.com/ViktorKnu/Stockflow-ERP.git
cd Stockflow-ERP
docker compose up --build
```

Har du allerede klonet repoet:

```powershell
cd Stockflow-ERP
git pull
docker compose up --build
```

Åpne API-et i nettleseren:

```text
http://localhost:8080/swagger-ui.html
```

Sjekk at appen lever:

```text
http://localhost:8080/actuator/health
```

Forventet svar:

```json
{"status":"UP"}
```

Stopp appen med `Ctrl + C`.

Start helt på nytt med tom database:

```powershell
docker compose down -v
docker compose up --build
```

## Hva gjør prosjektet?

Prosjektet har to hovedflyter.

Innkjøp:

```text
Leverandør -> Produkt -> Innkjøpsordre -> Mottak -> Lager øker -> Ledger EXPENSE -> Audit log
```

Salg:

```text
Produkt på lager -> Salgsordre -> Betalt -> Sendt -> Lager går ned -> Ledger REVENUE -> Audit log
```

Når lageret endres, lagres det alltid som en lagerbevegelse. Det gjør at man kan se hva beholdningen var før og etter en operasjon.

Når en viktig handling skjer, for eksempel at en innkjøpsordre mottas eller en salgsordre sendes, opprettes en audit log. Den er der for å forklare hva systemet gjorde og når.

## Hva finnes nå?

- Leverandører
- Produkter
- Lav-lager-søk
- Lagerbevegelser med `IN`, `OUT` og `ADJUSTMENT`
- Innkjøpsordre med mottak
- Salgsordre med betaling og shipping
- Ledger for innkjøpskostnader og salgsinntekter
- Audit log
- Brukere med unik e-post og BCrypt-hashet passord
- Swagger/OpenAPI
- PostgreSQL med Flyway-migrasjoner
- Docker Compose
- Unit-tester
- GitHub Actions

## Hvor starter jeg?

Hvis du bare vil prøve API-et, bruk Swagger:

```text
http://localhost:8080/swagger-ui.html
```

Anbefalt rekkefølge:

1. Lag en leverandør
2. Lag et produkt med lagerbeholdning
3. Lag en innkjøpsordre og motta den
4. Se at lageret øker
5. Lag en salgsordre
6. Legg til produktet på salgsordren
7. Sett salgsordren til `CONFIRMED`
8. Sett salgsordren til `PAID`
9. Send salgsordren med `/ship`
10. Se at lageret går ned
11. Se at ledger får en `REVENUE`

Mer detaljert testguide ligger her:

[docs/api-testing-guide.md](docs/api-testing-guide.md)

## Dokumentasjon

- [Forretningsflyt](docs/business-flow.md)
- [API-testguide](docs/api-testing-guide.md)
- [Prosjektstruktur](docs/project-structure.md)
- [Roadmap](docs/roadmap.md)

## Viktige lokale URL-er

```text
Swagger UI:   http://localhost:8080/swagger-ui.html
Health:       http://localhost:8080/actuator/health
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

PostgreSQL kjører lokalt på:

```text
host:     localhost
port:     5432
database: stockflow
username: stockflow
password: stockflow
```

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

Lager:

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
GET /api/ledger/summary/monthly?year=2026
```

Audit logs:

```text
GET /api/audit-logs
GET /api/audit-logs/{id}
GET /api/audit-logs/entity/{entityType}/{entityId}
```

Brukere:

```text
GET  /api/users
GET  /api/users/{id}
POST /api/users
```

JWT-innlogging er neste steg. Brukerendepunktene er derfor ikke tilgangsbeskyttet ennå.

## Prosjektstruktur

Koden er delt etter funksjon, ikke etter teknisk lag:

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
  user
```

Eksempel: alt som hører til produkter ligger i `product`, og alt som hører til salgsordre ligger i `salesorder`.

Se mer her:

[docs/project-structure.md](docs/project-structure.md)

## Teknologi

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Validation
- BCrypt passordhashing
- PostgreSQL
- Flyway
- Docker og Docker Compose
- Maven
- Swagger/OpenAPI
- JUnit 5
- Mockito
- Testcontainers
- GitHub Actions

## Tester

Hvis du har Maven installert:

```powershell
mvn test
```

Hvis ikke, kan Docker bygge prosjektet:

```powershell
docker compose up --build
```

## Vanlige problemer

Docker svarer ikke:

```powershell
docker version
```

Du skal se både `Client` og `Server`. Hvis du bare ser `Client`, start Docker Desktop.

Porten er opptatt:

```powershell
docker compose down
docker compose up --build
```

Database virker rar etter nye migrasjoner:

```powershell
docker compose down -v
docker compose up --build
```

`-v` sletter databasevolumet. Bruk det bare når du vil starte med tom database.
