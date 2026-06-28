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

I Swagger åpner du et endepunkt, trykker `Try it out`, fyller inn JSON og trykker `Execute`.

Guiden antar at du starter med tom database. Hvis du allerede har testdata, kan `id`-ene være annerledes. Bruk da `id`-en du får tilbake i responsen fra API-et.

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

## Vanlige ting å se etter

- Du kan ikke sende en salgsordre før den er `PAID`
- Du kan ikke sende samme salgsordre to ganger
- Du kan ikke trekke mer fra lager enn produktet har
- Mottak av innkjøpsordre øker lager
- Shipping av salgsordre senker lager
- Mottak av innkjøpsordre lager `EXPENSE`
- Shipping av salgsordre lager `REVENUE`
