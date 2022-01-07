# dp-arena-trakt

Håndterer data fra arena databasen.
Publiserer vedtakshendelser som konsumeres av [dp-vedtak](https://github.com/navikt/dp-vedtak).

## Komme i gang

Gradle brukes som byggverktøy og er bundlet inn.

`./gradlew sA b`

## Databasen

For å observere diverse metrikker i databasen, så kan man gå inn her:
[dp-arena-trakt i dev](https://console.cloud.google.com/sql/instances/dp-arena-trakt/overview?authuser=1&project=teamdagpenger-dev-885f)

NB: Dette krever at man er innlogget med `gcloud auth login`

## Kafka - Offsets og observerbarhet for golden gate topics fra arena.


Vi trenger shellscripts for å resette offsets samt hente info. Dette får vi via `brew install kafka`

Scriptene vi trenger kan man se her: `ls /usr/local/opt/kafka/bin/`

De blir tilgjengeliggjort i path via `brew install kafka`, så man skal kunne kalle de slik: `kafka-consumer-groups`

Gitt at man har satt opp alt dette (phew), så skal man kunne kjøre følgende kommando:

NB: /var/folders... til aiven secret i kafka.properties er automatisk generert, så pathen må endres

```sh
./kafka-consumer-groups \
  --command-config /var/folders/16/fyf0fpps4mz1_5tx5f6t7bq40000gn/T/aiven-secret-2896025567/kafka.properties \
  --bootstrap-server nav-dev-kafka-nav-dev.aivencloud.com:26484 \
  --group dp-arena-trakt-v2 \
  --describe
```


For mer dokumentasjon fra aiven, se: [viewing and resetting offsets](https://developer.aiven.io/docs/products/kafka/howto/viewing-resetting-offset.html)

For å sjekke de forskjellige 

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* André Roaldseth, andre.roaldseth@nav.no
* Eller en annen måte for omverden å kontakte teamet på

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-dagpenger-dev.

## Etterlevelse
### Dataminimering
Vi markerer data vi trenger som "i bruk" og sletter alt annet etter innsamling

