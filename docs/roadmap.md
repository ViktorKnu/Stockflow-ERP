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
- Integrasjonstest av autentisert innkjøpsflyt mot PostgreSQL med Testcontainers
- Valgfri demo-data gjennom Spring-profilen `demo`
- Postman-samling med automatisert ERP-flyt
- Maskinlesbare feilkoder for validering og ERP-workflows

## Neste commits

Ingen planlagte commits akkurat nå.

## Senere forbedringer

- Request correlation ID
- Mer komplett ledger for salg, refusjoner og justeringer
