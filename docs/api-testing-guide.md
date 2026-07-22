# API-testguide

Denne guiden er laget for første gjennomgang i Swagger.

Start appen:

```powershell
docker compose up --build
```

Åpne:

```text
http://localhost:8080/swagger-ui.html
```

Før du tester de beskyttede endepunktene, logg inn med bootstrap-administratoren fra `.env`:

```text
POST /api/auth/login
```

```json
{
  "email": "admin@stockflow.local",
  "password": "passordet-fra-env-filen"
}
```

Kopier `accessToken` fra responsen. Trykk **Authorize** øverst i Swagger og lim inn tokenet.

I Swagger åpner du et endepunkt, trykker `Try it out`, fyller inn JSON og trykker `Execute`.

Guiden antar at du starter med tom database. Hvis du allerede har testdata, kan `id`-ene være annerledes. Bruk da `id`-en du får tilbake i responsen fra API-et.

## Feilresponser

Alle feilresponser har et stabilt, maskinlesbart `code`-felt i tillegg til HTTP-status og melding:

```json
{
  "timestamp": "2026-07-20T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "PURCHASE_ORDER_ALREADY_RECEIVED",
  "message": "Purchase order has already been received",
  "path": "/api/purchase-orders/42/receive",
  "validationErrors": {}
}
```

Klienter bør bruke `code` for programlogikk og `message` for visning. Eksempler på workflow-koder er
`PURCHASE_ORDER_NOT_ORDERED`, `SALES_ORDER_NOT_PAID`, `SALES_ORDER_ITEMS_REQUIRED` og
`INSUFFICIENT_STOCK`.

## 1. Lag en leverandør

Bruk:

```text
POST /api/suppliers
```

Body:

```json
{
  "name": "Nordic Supplies AS",
  "email": "orders@nordic.example",
  "phone": "+47 22 00 00 00",
  "address": "Oslo"
}
```

Noter `id` fra responsen. I eksemplene under brukes `1`.

## 2. Lag et produkt

Bruk:

```text
POST /api/products
```

Body:

```json
{
  "name": "Barcode Scanner",
  "sku": "SCAN-001",
  "description": "USB scanner for warehouse use",
  "category": "Hardware",
  "quantity": 12,
  "minimumStock": 3,
  "price": 799.00,
  "supplierId": 1
}
```

Produktet starter med `quantity` 12.

Noter `id` fra responsen. I eksemplene under brukes `1`.

## 3. Test lager ut

Bruk:

```text
POST /api/inventory/movements
```

Body:

```json
{
  "productId": 1,
  "type": "OUT",
  "quantity": 2,
  "reason": "Manual test"
}
```

Etterpå kan du hente produktet:

```text
GET /api/products/1
```

Lageret skal ha gått fra 12 til 10.

## 4. Lag og motta en innkjøpsordre

Lag ordre:

```text
POST /api/purchase-orders
```

```json
{
  "supplierId": 1
}
```

Noter `id` fra responsen. I eksemplene under brukes `1`.

Legg til vare:

```text
POST /api/purchase-orders/1/items
```

```json
{
  "productId": 1,
  "quantity": 5,
  "unitPrice": 699.00
}
```

Sett status til `ORDERED`:

```text
PUT /api/purchase-orders/1/status
```

```json
{
  "status": "ORDERED"
}
```

Motta ordren:

```text
POST /api/purchase-orders/1/receive
```

Nå skal lageret øke, og ledger skal få en `EXPENSE`.

Sjekk:

```text
GET /api/ledger/summary
GET /api/audit-logs
```

## 5. Lag og send en salgsordre

Lag salgsordre:

```text
POST /api/sales-orders
```

```json
{
  "customerName": "Ada Lovelace",
  "customerEmail": "ada@example.com"
}
```

Noter `id` fra responsen. I eksemplene under brukes `1`.

Legg til vare:

```text
POST /api/sales-orders/1/items
```

```json
{
  "productId": 1,
  "quantity": 2,
  "unitPrice": 899.00
}
```

Bekreft ordren:

```text
PUT /api/sales-orders/1/status
```

```json
{
  "status": "CONFIRMED"
}
```

Marker som betalt:

```text
PUT /api/sales-orders/1/status
```

```json
{
  "status": "PAID"
}
```

Send ordren:

```text
POST /api/sales-orders/1/ship
```

Nå skal lageret gå ned, ledger skal få en `REVENUE`, og audit log skal få `SALES_ORDER_SHIPPED`.

Sjekk:

```text
GET /api/inventory/movements/product/1
GET /api/ledger/transactions
GET /api/ledger/summary
GET /api/audit-logs
```

## 6. Se månedsrapporten

Finn `GET /api/ledger/summary/monthly` under `ledger-controller` i Swagger.

Trykk **Try it out**. La `year` stå tomt for å se alle måneder, eller skriv inn for eksempel
`2026` for å se bare det året. Trykk deretter **Execute**.

Det samme kallet kan åpnes direkte i nettleseren:

```text
http://localhost:8080/api/ledger/summary/monthly?year=2026
```

Hver måned viser summen av `REVENUE`, summen av `EXPENSE`, resultatet og antall transaksjoner.
Måneder uten transaksjoner vises ikke.

## 7. Opprett en bruker

Finn `POST /api/users` under `user-controller` i Swagger og bruk:

```json
{
  "name": "Kari Nordmann",
  "email": "kari@example.com",
  "password": "hemmelig123"
}
```

Responsen skal ha rollen `EMPLOYEE` når `role` utelates. En administrator kan også sende
`"role": "MANAGER"` eller `"role": "ADMIN"`. Passordet og den lagrede passordhashen skal ikke være med i responsen.

Sjekk brukeren med:

```text
GET /api/users
GET /api/users/1
```

Prøver du samme e-post en gang til, skal API-et svare med HTTP `409 Conflict`.
Brukerendepunktene krever rollen `ADMIN`.

## Vanlige ting å se etter

- Du kan ikke sende en salgsordre før den er `PAID`
- Du kan ikke sende samme salgsordre to ganger
- Du kan ikke trekke mer fra lager enn produktet har
- Mottak av innkjøpsordre øker lager
- Shipping av salgsordre senker lager
- Mottak av innkjøpsordre lager `EXPENSE`
- Shipping av salgsordre lager `REVENUE`
- Nye brukere får rollen `EMPLOYEE`
- Samme e-post kan ikke registreres to ganger
