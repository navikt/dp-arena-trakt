package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.modell.Sak
import no.nav.dagpenger.arena.trakt.modell.Vedtak
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding
import no.nav.dagpenger.arena.trakt.meldinger.SakReplikertMelding
import no.nav.dagpenger.arena.trakt.meldinger.VedtakReplikertMelding

// HendelseMediator
// Sender replikeringsmeldinger til modellen
internal class HendelseMediator(
    private val sakRepository: SakRepository,
    private val vedtakRepository: VedtakRepository
) : IHendelseMediator {
    override fun behandle(message: ReplikeringsMelding) {
        message.behandle(this)
    }

    override fun behandle(sakReplikertMelding: SakReplikertMelding, sak: Sak) {
        sakRepository.lagre(sak)
    }

    override fun behandle(vedtakReplikertMelding: VedtakReplikertMelding, vedtak: Vedtak) {
        vedtakRepository.lagre(vedtak)
    }
}

internal interface IHendelseMediator {
    fun behandle(message: ReplikeringsMelding)
    fun behandle(sakReplikertMelding: SakReplikertMelding, sak: Sak)
    fun behandle(vedtakReplikertMelding: VedtakReplikertMelding, vedtak: Vedtak)
}
