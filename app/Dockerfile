FROM gcr.io/distroless/java21

ENV TZ="Europe/Oslo"

COPY build/install/app/lib/* /app/lib/

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.dagpenger.arena.trakt.AppKt"]