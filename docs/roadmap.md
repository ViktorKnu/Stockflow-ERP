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

## Neste commits

1. `legg til brukere`

   Opprett brukermodellen og lagring av brukere som grunnlag for innlogging.

2. `legg til innlogging`

   Legg til passordhashing, registrering og login med JWT.

3. `legg til roller og tilgang`

   Innfør `ADMIN`, `MANAGER` og `EMPLOYEE`.

4. `legg til integrasjonstester`

   Test API og database sammen med Testcontainers.

## Senere forbedringer

- Demo-data
- Postman collection
- Bedre feilkoder i enkelte workflows
- Request correlation ID
- Mer komplett ledger for salg, refusjoner og justeringer
