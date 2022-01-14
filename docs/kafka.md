## Kafka - Offsets og observerbarhet for golden gate topics fra arena.

### Prerequisites:

#### Verktøy
-  [nais cli](https://doc.nais.io/cli/install/) - Vi trenger `nais aiven`
-  Kafka: `brew install kafka` - Vi trenger scriptene som ligger i: `ls /usr/local/opt/kafka/bin/`

#### Generer dp-offset-admin (tidsbasert aiven app)
1. Logg inn på gcp:  `gcloud auth login && kubectx dev-gcp && kubens teamdagpenger` - Bytt ut dev med prod om
   nødvendig
2. Kjør `nais aiven create dp-offset-admin teamdagpenger`
3. Kjør kommandoen du fikk fra outputen over <br/>
   f.eks: `nais aiven get teamdagpenger-dp-offset-admin-b6fce022 teamdagpenger`
4. Outputen fra kommandoen over inneholder en path til en mappe (f.eks `/var/folders/../aiven-secret-2875218110`) med konfigurasjonen for å koble til Aiven.
5. Kjør `ls <path/til/konfigurasjonsmappe>`. Ta vare på pathen til kafka.properties, den skal brukes i scriptene under (`--command-config <path-til-kafka-properties>`)
6. For consumer-group-id, se `KAFKA_CONSUMER_GROUP_ID` i [Config.kt](../src/main/kotlin/no/nav/dagpenger/arena/trakt/Config.kt)

For mer utdypende forklaring se nais sin [aiven dokumentasjon](https://doc.nais.io/cli/commands/aiven/)

### Sjekke offsets

```shell
kafka-consumer-groups \
  --command-config <path/til/genererte/kafka.properties> \
  --bootstrap-server nav-dev-kafka-nav-dev.aivencloud.com:26484 \
  --group <consumer-group-id> \
  --describe
```

### Resetting av offsets:
Før man resetter offsets, så er man nødt til å stoppe consumerene. Dette gjør man slik:
`kubectl scale deployment -lapp=dp-arena-trakt --replicas=0`

Deretter kan man kjøre følgende script:

```shell
kafka-consumer-groups \                                 
  --command-config <path/til/genererte/kafka.properties> \
  --bootstrap-server nav-dev-kafka-nav-dev.aivencloud.com:26484 \
  --group <consumer-group-id> \
  --topic <topicnavn> \
  --reset-offsets \
  --to-earliest \
  --execute
```

Hvis man ikke scalerer ned poddene først, for å stoppe consumerne, så vil man få denne feilen:
`Error: Assignments can only be reset if the group 'dp-arena-trakt-v7' is inactive, but the current state is Stable.`

For mer dokumentasjon fra aiven,
se: [viewing and resetting offsets](https://developer.aiven.io/docs/products/kafka/howto/viewing-resetting-offset.html)
