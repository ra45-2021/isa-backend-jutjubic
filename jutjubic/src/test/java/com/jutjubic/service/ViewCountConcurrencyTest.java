package com.jutjubic.service;

import com.jutjubic.domain.Post;
import com.jutjubic.domain.User;
import com.jutjubic.repository.PostRepository;
import com.jutjubic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test koji demonstrira konzistentnost brojača pregleda pri istovremenom pristupu istom videu.
 *
 * Simulira scenario gde više korisnika istovremeno pristupa istom videu i validira
 * da se broj pregleda pravilno inkrementira bez gubitka podataka.
 */
@SpringBootTest
@ActiveProfiles("test")
class ViewCountConcurrencyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testPostId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Očisti bazu pre svakog testa
        postRepository.deleteAll();
        userRepository.deleteAll();

        // Kreiraj test korisnika
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmailAdress("test@example.com");
        testUser.setPassword("password");
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser = userRepository.save(testUser);

        // Kreiraj test video post
        Post testPost = new Post();
        testPost.setAuthor(testUser);
        testPost.setTitle("Test Video");
        testPost.setVideoUrl("test-video.mp4");
        testPost.setThumbnailUrl("test-thumbnail.jpg");
        testPost.setViewCount(0L);
        testPost = postRepository.save(testPost);

        testPostId = testPost.getId();
    }

    @Test
    void testConcurrentViewIncrement_shouldBeConsistent() throws Exception {
        // Broj istovremenih "gledaoca"
        int numberOfViewers = 50;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfViewers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfViewers);

        AtomicInteger successfulIncrements = new AtomicInteger(0);
        AtomicInteger failedIncrements = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Simuliraj istovremene pozive od više korisnika
        for (int i = 0; i < numberOfViewers; i++) {
            executorService.submit(() -> {
                try {
                    // Čekaj da svi threadovi budu spremni
                    startLatch.await();

                    // Pozovi metodu za inkrementaciju
                    postService.incrementViewCount(testPostId);
                    successfulIncrements.incrementAndGet();

                } catch (Exception e) {
                    failedIncrements.incrementAndGet();
                    System.err.println("Failed to increment: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Pokreni sve threadove istovremeno
        startLatch.countDown();

        // Čekaj da se svi završe (max 10 sekundi)
        boolean finished = endLatch.await(10, TimeUnit.SECONDS);
        assertTrue(finished, "Test nije završen u predviđenom vremenu");

        // Daj malo vremena async operacijama da se završe
        Thread.sleep(2000);

        long duration = System.currentTimeMillis() - startTime;

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Učitaj post iz baze i proveri viewCount
        Post updatedPost = postRepository.findById(testPostId)
                .orElseThrow(() -> new AssertionError("Post not found"));

        System.out.println("=== Test Results ===");
        System.out.println("Number of concurrent viewers: " + numberOfViewers);
        System.out.println("Successful increments: " + successfulIncrements.get());
        System.out.println("Failed increments: " + failedIncrements.get());
        System.out.println("Final view count in DB: " + updatedPost.getViewCount());
        System.out.println("Test duration: " + duration + "ms");
        System.out.println("==================");

        // Validacije
        assertEquals(0, failedIncrements.get(),
                "Sve inkrementacije treba da uspeju");

        assertEquals(numberOfViewers, updatedPost.getViewCount(),
                "Broj pregleda treba da bude tačan bez gubitka inkrementacija");

        assertTrue(duration < 10000,
                "Test treba da se završi brzo (< 10s) što pokazuje da nema blocking-a");
    }

    @Test
    void testSequentialViewIncrement_baseline() throws Exception {
        int numberOfViews = 10;

        long startTime = System.currentTimeMillis();

        // Sekvencijalni pozivi (baseline za poređenje)
        for (int i = 0; i < numberOfViews; i++) {
            postService.incrementViewCount(testPostId);
        }

        // Čekaj async operacije
        Thread.sleep(1000);

        long duration = System.currentTimeMillis() - startTime;

        Post updatedPost = postRepository.findById(testPostId)
                .orElseThrow(() -> new AssertionError("Post not found"));

        System.out.println("=== Sequential Baseline ===");
        System.out.println("Number of views: " + numberOfViews);
        System.out.println("Final view count: " + updatedPost.getViewCount());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("=========================");

        assertEquals(numberOfViews, updatedPost.getViewCount(),
                "Sequential increments should work correctly");
    }

    @Test
    void testHighLoadConcurrency_stressTest() throws Exception {
        // Stress test sa mnogo više konkurentnih zahteva
        // Ograničeno na 100 jer AsyncConfig ima: 5 max threads + 100 queue capacity = 105 total
        int numberOfViewers = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfViewers);

        AtomicInteger completed = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfViewers; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    postService.incrementViewCount(testPostId);
                    completed.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = endLatch.await(20, TimeUnit.SECONDS);
        assertTrue(finished, "Stress test timeout");

        // Daj dovoljno vremena za sve async operacije
        Thread.sleep(5000);

        long duration = System.currentTimeMillis() - startTime;

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        Post updatedPost = postRepository.findById(testPostId)
                .orElseThrow(() -> new AssertionError("Post not found"));

        System.out.println("=== Stress Test Results ===");
        System.out.println("Target viewers: " + numberOfViewers);
        System.out.println("Completed calls: " + completed.get());
        System.out.println("Final view count: " + updatedPost.getViewCount());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("==========================");

        assertEquals(numberOfViewers, updatedPost.getViewCount(),
                "Sve inkrementacije moraju biti evidentirane čak i pod visokim opterećenjem");
    }
}
