package no.nav.dagpenger.arena.trakt.modell

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PersonTest {
    private val person = Person()
    private lateinit var observer: TestObserver

    @BeforeEach
    fun setup() {
        observer = TestObserver().also { person.leggTilObserver(it) }
    }

    @Test
    fun `Sak først, så vedtak`() {
        person.håndter(Sak(1))
        person.håndter(Sak(2))
        person.håndter(Sak(3))
        person.håndter(vedtak(1, 1))

        assertSame(1, observer.vedtak())
    }

    @Test
    fun `Vedtak først, så sak`() {
        person.håndter(vedtak(1, 1))
        person.håndter(Sak(1))
        person.håndter(Sak(2))
        person.håndter(Sak(3))

        assertSame(1, observer.vedtak())
    }

    class TestObserver : PersonObserver {
        private val vedtak = mutableListOf<Vedtak>()

        override fun nyttVedtak(nyttVedtak: Vedtak) {
            vedtak.add(nyttVedtak)
        }

        fun vedtak() = vedtak.size
    }
}

private fun vedtak(vedtakId: Int = 1, sakId: Int = 1) = Vedtak(
    sakId = sakId,
    vedtakId = vedtakId,
    personId = 1,
    vedtaktypekode = "O",
    utfallkode = "JA",
    rettighetkode = "DAGO",
    vedtakstatuskode = "IVERK",
)
