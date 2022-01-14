KAFKAPROPS=/var/folders/wn/44t__ys13cn9py7cc75st9sr0000gn/T/aiven-secret-538365556/kafka.properties
GROUPID=dp-arena-trakt-v7
TOPICNAVN=teamarenanais.aapen-arena-vedtakendret-v1-q2

kubectl scale deployment -lapp=dp-arena-trakt --replicas=0

kafka-consumer-groups \
  --command-config $KAFKAPROPS \
  --bootstrap-server nav-dev-kafka-nav-dev.aivencloud.com:26484 \
  --group $GROUPID \
  --topic $TOPICNAVN \
  --reset-offsets \
  --to-earliest \
  --execute


