# Forretningsflyt

Denne siden forklarer hva systemet prøver å modellere.

StockFlow handler om tre ting:

1. Varer kommer inn på lager
2. Varer går ut av lager
3. Viktige hendelser blir sporbare

## Innkjøp

Når bedriften kjøper varer, starter det med en leverandør og et produkt.

Flyten er:

```text
Leverandør -> Produkt -> Innkjøpsordre -> Mottak
```

Når en innkjøpsordre mottas, gjør systemet flere ting i samme transaksjon:

- Setter innkjøpsordren til `RECEIVED`
- Øker lagerbeholdningen på produktene
- Lager `InventoryMovement` med type `IN`
- Lager en ledger-transaksjon med type `EXPENSE`
- Lager audit log

Hvis noe av dette feiler, skal ikke systemet stå igjen med halvferdige data.

## Salg

Når bedriften selger varer, starter det med en salgsordre.

Flyten er:

```text
Produkt på lager -> Salgsordre -> CONFIRMED -> PAID -> SHIPPED
```

Når salgsordren sendes, gjør systemet dette:

- Sjekker at ordren er `PAID`
- Sjekker at produktene fortsatt finnes på lager
- Trekker lager med `InventoryMovement` type `OUT`
- Lager en ledger-transaksjon med type `REVENUE`
- Setter salgsordren til `SHIPPED`
- Lager audit log

Det er ikke lov å sende samme ordre to ganger.

En sendt ordre kan refunderes én gang med `POST /api/sales-orders/{id}/refund`. Da skjer alt i
samme databasetransaksjon:

- Varene føres tilbake med `InventoryMovement` type `IN`
- Ledger får en `REFUND` på hele ordrebeløpet
- Ordren settes til `REFUNDED`
- Audit log får `SALES_ORDER_REFUNDED`

Det er ikke mulig å refundere en ordre som ikke er sendt, eller refundere samme ordre to ganger.

## Lagerbevegelser

En lagerbevegelse er historikken bak lagerbeholdningen.

Eksempel:

```text
Produkt: Barcode Scanner
Type: OUT
Antall: 2
Før: 12
Etter: 10
Årsak: Sales order shipped: 3
```

Dette gjør at man ikke bare ser hva lageret er nå, men også hvorfor det endret seg.

## Ledger

Ledger er en enkel økonomilogg.

Systemet lager:

- `EXPENSE` når en innkjøpsordre mottas
- `REVENUE` når en salgsordre sendes
- `REFUND` når en sendt salgsordre refunderes
- `ADJUSTMENT` for manuelle, signerte korrigeringer

Positive justeringer øker resultatet, mens negative justeringer reduserer det.
`GET /api/ledger/summary` viser inntekter, kostnader, refusjoner, justeringer og nettoresultat.

`GET /api/ledger/summary/monthly` deler de samme tallene opp per måned. Legg til for eksempel
`?year=2026` for å begrense rapporten til ett år.

## Audit log

Audit log er en lesbar historikk over viktige handlinger.

Eksempler:

- `PRODUCT_CREATED`
- `INVENTORY_MOVEMENT_CREATED`
- `PURCHASE_ORDER_RECEIVED`
- `SALES_ORDER_SHIPPED`
- `SALES_ORDER_REFUNDED`
- `LEDGER_TRANSACTION_CREATED`

Audit log er read-only fra API-et. Den opprettes av systemet når noe viktig skjer.
