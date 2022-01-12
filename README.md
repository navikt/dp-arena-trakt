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

Prerequisites:
- Installer [nais cli](https://doc.nais.io/cli/install/) - Vi trenger `nais aiven`
- Installer Kafka: `brew install kafka` - Vi trenger scriptene som ligger i: `ls /usr/local/opt/kafka/bin/`
- Kjør følgende kommando: `gcloud auth login && kubectx dev-gcp && kubens teamdagpenger` - Bytt ut dev med prod om nødvendig

Når dette er installert, så bruker vi `nais aiven create` kommandoen til å generere en tidsbasert aiven applikasjon for å sjekke/endre offsets i Aiven clusteret. 
Mer utdypende forklaring her: [nais aiven](https://doc.nais.io/cli/commands/aiven/)

1. Kjør deretter: `nais aiven create dp-offset-admin teamdagpenger`
2. Kjør kommandoen du fikk fra outputen over, f.eks: `nais aiven get teamdagpenger-dp-offset-admin-b6fce022 teamdagpenger`
3. Outputen fra kommandoen over inneholder en path en mappe med konfigurasjonen vi trenger for å koble til Aiven. 
   1. F.eks: `/var/folders/../aiven-secret-2875218110`
   2. Hvis du kjører en `ls /var/folders/../aiven-secret-2875218110` så vil du blant annet se `kafka.properties` Det er denne vi skal bruke.
4. Ta hele pathen til kafka.properties vi fant over, og endre `--command-config pathen i scriptet under`

```sh
kafka-consumer-groups \
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

