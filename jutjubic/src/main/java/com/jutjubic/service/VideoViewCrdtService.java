package com.jutjubic.service;

import com.jutjubic.domain.VideoViewCrdt;
import com.jutjubic.repository.VideoViewCrdtRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service za upravljanje CRDT brojačima pregleda videa.
 *
 * Ovaj servis pruža API za rad sa G-counter strukturom.
 * Implementacija G-counter logike i sinhronizacije između replika
 * će biti dodata u sledećoj fazi razvoja.
 *
 * Trenutno servis podržava:
 * - Inicijalizaciju brojača za novu repliku
 * - Inkrementiranje lokalnog brojača
 * - Čitanje ukupnog broja pregleda
 */
@Service
public class VideoViewCrdtService {

    private final VideoViewCrdtRepository repository;

    /**
     * ID ove replike (npr. "replica_1", "replica_2").
     * Učitava se iz application-replicaX.properties fajla.
     */
    @Value("${replica.id}")
    private String replicaId;

    public VideoViewCrdtService(VideoViewCrdtRepository repository) {
        this.repository = repository;
    }


    @Transactional
    public void incrementViewForReplica(Long videoId) {
        // Proveri da li postoji unos za ovaj video i ovu repliku
        if (!repository.existsByVideoIdAndReplicaId(videoId, replicaId)) {
            // Ako ne postoji, kreiraj novi unos
            VideoViewCrdt newEntry = new VideoViewCrdt(videoId, replicaId);
            newEntry.setViewCount(1L);
            newEntry.setLastUpdated(System.currentTimeMillis());
            repository.save(newEntry);
        } else {
            // Ako postoji, povećaj brojač
            repository.incrementViewCount(videoId, replicaId, System.currentTimeMillis());
        }

    }

    /**
     * Vraća ukupan broj pregleda za dati video sabiranjem svih brojača.
     * Ovo je implementacija G-counter READ operacije.
     *
     * @param videoId ID videa
     * @return Ukupan broj pregleda (zbir svih replika)
     */
    @Transactional(readOnly = true)
    public Long getTotalViewCount(Long videoId) {
        return repository.getTotalViewCount(videoId);
    }

    /**
     * Vraća sve brojače za dati video (za potrebe debugginga i monitoringa).
     *
     * @param videoId ID videa
     * @return Lista brojača za sve replike
     */
    @Transactional(readOnly = true)
    public List<VideoViewCrdt> getAllCountersForVideo(Long videoId) {
        return repository.findAllByVideoId(videoId);
    }

    /**
     * Merge funkcija koja prima stanje brojača od druge replike.
     * Implementira G-counter merge logiku: uzima MAX od lokalnog i primljenog stanja.
     *
     * TODO: Implementirati G-counter merge logiku
     * - Primiti poruku od druge replike sa (videoId, replicaId, count)
     * - Uzeti MAX(lokalniCount, primljeniCount)
     * - Ažurirati lokalnu tabelu
     *
     * @param videoId ID videa
     * @param sourceReplicaId ID replike koja šalje svoj brojač
     * @param receivedCount Vrednost brojača primljena od druge replike
     */
    @Transactional
    public void mergeFromOtherReplica(Long videoId, String sourceReplicaId, Long receivedCount) {
        // TODO: Implementirati merge logiku
        //
        // Pseudo-kod:
        // 1. Pronađi lokalni brojač za (videoId, sourceReplicaId)
        // 2. Ako ne postoji, kreiraj novi sa primljenom vrednošću
        // 3. Ako postoji, uzmi MAX(lokalniCount, receivedCount)
        // 4. Ažuriraj tabelu sa maksimalnom vrednošću
        //
        // Primer:
        // Long localCount = repository.findByVideoIdAndReplicaId(videoId, sourceReplicaId)
        //                             .map(VideoViewCrdt::getViewCount)
        //                             .orElse(0L);
        // Long mergedCount = Math.max(localCount, receivedCount);
        // repository.updateViewCount(videoId, sourceReplicaId, mergedCount, System.currentTimeMillis());

        System.out.println("[CRDT Service] Merge called from replica: " + sourceReplicaId +
                          " for video: " + videoId + " with count: " + receivedCount);
    }

    /**
     * Metoda koja će biti korišćena za slanje stanja brojača drugim replikama.
     *
     * TODO: Implementirati komunikaciju sa drugim replikama
     * - Koristiti message queue (RabbitMQ, Kafka, ili in-memory)
     * - Periodično slati stanje svih brojača
     * - Ili slati nakon svake izmene (event-driven)
     *
     * @param videoId ID videa čiji brojač treba poslati
     */
    private void broadcastToOtherReplicas(Long videoId) {
        // TODO: Implementirati broadcast logiku
        //
        // Pseudo-kod:
        // 1. Učitaj svoj lokalni brojač za videoId
        // 2. Kreiraj poruku: { videoId, replicaId: this.replicaId, count: localCount }
        // 3. Pošalji poruku u message queue
        // 4. Druge replike će primiti poruku i pozvati mergeFromOtherReplica()

        System.out.println("[CRDT Service] Broadcast to other replicas for video: " + videoId);
    }

    /**
     * Vraća ID trenutne replike.
     *
     * @return ID replike (npr. "replica_1")
     */
    public String getReplicaId() {
        return replicaId;
    }
}
