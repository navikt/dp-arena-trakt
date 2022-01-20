# dp-arena-trakt

Håndterer data fra arena databasen. Publiserer vedtakshendelser som konsumeres
av [dp-vedtak](https://github.com/navikt/dp-vedtak).

## Komme i gang

Gradle brukes som byggverktøy og er bundlet inn.

`./gradlew sA b`

## Batch innlesing
Batch innlesing kan skrus på ved å sette variabelen `batch_insert: true` i [dev-vars.yaml](.nais/dev-vars.yaml) og [prod-vars.yaml](.nais/prod-vars.yaml) 

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

Vi markerer data vi trenger som "i bruk" og sletter alt annet etter innsamling

