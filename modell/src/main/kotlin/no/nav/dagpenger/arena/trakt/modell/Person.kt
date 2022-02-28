package no.nav.dagpenger.arena.trakt.modell

class Person private constructor(
    private val saker: MutableList<Sak>,
) {
    private val observers: MutableList<PersonObserver> = mutableListOf()

    constructor() : this(mutableListOf())

    fun håndter(sak: Sak) {
        saker.filter { it.håndter(sak) }.ifEmpty {
            saker.add(sak)
        }
    }

    fun håndter(vedtak: Vedtak) {
        val nyeVedtak: List<Vedtak> = saker
            .flatMap { it.håndter(vedtak) }
            .ifEmpty {
                Sak(vedtak.sakId).also {
                    saker.add(it)
                }.run {
                    håndter(vedtak)
                }.toList()
            }

        if (nyeVedtak.isNotEmpty()) {
            nyeVedtak.forEach {
                emitNyttVedtak(it)
            }
        }
    }

    private fun emitNyttVedtak(vedtak: Vedtak) {
        observers.forEach { it.nyttVedtak(vedtak) }
    }

    fun leggTilObserver(observer: PersonObserver) = observers.add(observer)
}

interface PersonObserver {
    fun nyttVedtak(vedtak: Vedtak)
}
