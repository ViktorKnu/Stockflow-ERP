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

## Neste commits

1. `legg til månedlig rapport`

   Ledger skal kunne vise inntekter, kostnader og resultat per måned.

2. `legg til innlogging`

   Legg til brukere, passordhashing og login.

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
