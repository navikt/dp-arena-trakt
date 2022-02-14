# dp-arena-trakt

Håndterer data fra arena databasen. Publiserer vedtakshendelser som konsumeres
av [dp-vedtak](https://github.com/navikt/dp-vedtak).

## Komme i gang

Gradle brukes som byggverktøy og er bundlet inn.

`./gradlew sA b`

## Batch innlesing
Gjøres via github actions, se: [Batchlasting av data](https://github.com/navikt/dp-arena-trakt/actions/workflows/run-job.yaml)

## Databasen

For å observere diverse metrikker i databasen, så kan man gå inn her:
[dp-arena-trakt i dev](https://console.cloud.google.com/sql/instances/dp-arena-trakt/overview?authuser=1&project=teamdagpenger-dev-885f)

NB: Dette krever at man er innlogget med `gcloud auth login`

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* André Roaldseth, andre.roaldseth@nav.no
* Eller en annen måte for omverden å kontakte teamet på

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-dagpenger-dev.

## Etterlevelse

### Dataminimering

Vi vurderer og sletter data fortløpende ettersom de kommer inn. Siden vi ikke alltid kan fastsette om vi skal slette 
eller beholde data, så har vi også en sletterutine som kjører kontinuerlig for å se om den finner data som kan slettes.

