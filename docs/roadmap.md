# Roadmap

Dette er planen videre. Målet er små commits som kan testes én og én.

## Ferdig nå

- Prosjektgrunnmur
- Docker Compose
- PostgreSQL
- Flyway
- Swagger
- Leverandører
- Produkter
- Lagerbevegelser
- Innkjøpsordre
- Mottak av innkjøpsordre
- Ledger `EXPENSE` for innkjøp
- Audit log
- Salgsordre
- Shipping av salgsordre
- Ledger `REVENUE` for salg
- Månedsrapport med valgfritt årsfilter
- Brukere med unik e-post, rolle og hashet passord
- JWT-innlogging
- Rollebasert tilgang for `ADMIN`, `MANAGER` og `EMPLOYEE`
- Bootstrap av første administrator fra miljøvariabler

## Neste commits

1. `legg til integrasjonstester`

   Test API og database sammen med Testcontainers.

## Senere forbedringer

- Demo-data
- Postman collection
- Bedre feilkoder i enkelte workflows
- Request correlation ID
- Mer komplett ledger for salg, refusjoner og justeringer
