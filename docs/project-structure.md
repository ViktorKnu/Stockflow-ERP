# Prosjektstruktur

Koden er delt etter funksjon.

Det betyr at `product` inneholder produktkode, `salesorder` inneholder salgsordrekode, og så videre.

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

## Hva de ulike mappene gjør

`supplier`

Leverandører. En leverandør kan ha flere produkter.

`product`

Produkter som bedriften kjøper, lagrer og selger. Produktet har SKU, pris, lagerbeholdning og minimumslager.

`inventory`

Alle lagerendringer. Denne modulen sørger for at lageret ikke blir negativt og at før/etter-antall lagres.

`purchaseorder`

Innkjøpsordre fra leverandører. Når en ordre mottas, øker lageret og systemet lager ledger- og audit-hendelser.

`salesorder`

Salgsordre til kunder. Ordren kan bekreftes, betales og sendes. Når den sendes, går lageret ned.

`ledger`

En enkel økonomilog for innkjøpskostnader og salgsinntekter.

`audit`

Read-only logg over viktige hendelser i systemet.

`user`

Brukerkontoer med unik e-post, rolle og hashet passord. JWT-innlogging legges til i neste steg.

`exception`

Felles feilhåndtering. Her ligger egne exceptions og global exception handler.

`config`

Konfigurasjon, for eksempel OpenAPI/Swagger.

## Mønsteret i en modul

De fleste moduler følger samme mønster:

```text
Entity
Repository
Service
Controller
DTO-er
Mapper
```

Eksempel fra `salesorder`:

- `SalesOrder` er database-entity
- `SalesOrderRepository` snakker med databasen
- `SalesOrderService` inneholder businesslogikk
- `SalesOrderController` eksponerer HTTP-endepunkter
- `dto` inneholder request- og response-objekter
- `SalesOrderMapper` gjør entity om til response DTO

Controllerne skal være tynne. De tar imot HTTP-kall og sender jobben videre til service-laget.

Service-laget er der reglene bor.

## Database

Databaseskjemaet lages med Flyway:

```text
src/main/resources/db/migration
```

Eksempler:

- `V1__create_suppliers_table.sql`
- `V2__create_products_table.sql`
- `V7__create_sales_orders_tables.sql`
- `V8__add_sales_order_shipped_audit_action.sql`
- `V9__create_users_table.sql`

Hibernate validerer skjemaet, men Flyway lager tabellene.

## Hvor starter man i koden?

For å forstå et endepunkt:

1. Start i controlleren
2. Gå til service-metoden controlleren kaller
3. Se hvilke repositories service-metoden bruker
4. Se DTO-ene for input og output

Eksempel:

```text
POST /api/sales-orders/{id}/ship
```

Start her:

```text
SalesOrderController.ship()
```

Den kaller:

```text
SalesOrderService.ship()
```

Der ser du reglene for shipping, lagerfratrekk og audit log.
