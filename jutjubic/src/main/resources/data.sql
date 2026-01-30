-- USERS
INSERT INTO users (username, password, email_adress, name, surname, adress, bio, profile_image_url, active, phone_number)
VALUES
    ('ana.zaric', '{noop}pass', 'ana.zaric@mail.com', 'Ana', 'Zarić', 'Bulevar oslobođenja 45, Novi Sad', 'Volim putovanja i planinarenje.', '/uploads/ana-profile.png', true, ''),
    ('mark0',  '{noop}pass', 'mark0@mail.com', 'Marko', 'Marković', 'Nemanjina 12, Beograd', 'Backend dev. Spring Boot, PostgreSQL, i dobra kafa.', '/uploads/marko-profile.png', true, ''),
    ('jana_p',    '{noop}pass', 'jana_p@mail.com', 'Jana', 'Petrović', 'Cara Dušana 8, Niš', 'Video editor + content. Montaža, rezovi i titlovi.','/uploads/jana-profile.png', true, '');

-- POSTS
INSERT INTO posts (author_id, title, description, tags_text, video_url, thumbnail_url, created_at, location_lat, location_lon, view_count)
VALUES
    (1, 'Fruška gora VLOG', 'Kampovanje 5 dana u šumi na Fruškoj gori.', 'vlog, priroda, planinarenje', '/media/videos/fruska-gora.mp4', '/media/thumbs/fruska-gora.jpg', NOW() - INTERVAL '299 days', 45.1566, 19.8066,12),
    (2, 'Intro to SpringBoot', 'Coding for beginners - SpringBoot','spring, jwt, backend', '/media/videos/jwt-spring.mp4', '/media/thumbs/jwt-spring.jpg', NOW() - INTERVAL '3 days', 44.8125, 20.4612,15),
    (3, 'Montaža thumbnaila za početnike', '3 trika da video izgleda čistije i profesionalnije.','video-editing, capcut', '/media/videos/editing-basics.mp4', '/media/thumbs/editing-basics.jpg', NOW() - INTERVAL '5 hours', 43.3209, 21.8958,100),
    (1, 'TOP 10 Mesta u Budimpešti', 'Top mesta + saveti za prevoz i budžet.', 'tips, guide, budapest','/media/videos/budapest-guide.mp4', '/media/thumbs/budapest-guide.jpg', NOW() - INTERVAL '2 weeks', 47.4979, 19.0402,150),
    (2, 'Osnove snimanja zvuka', 'Kako dobiti čist zvuk bez profesionalnog mikrofona.', 'audio, recording, tips', '/media/videos/audio-basics.mp4', '/media/thumbs/audio-basics.jpg', NOW() - INTERVAL '1 day', 44.8125, 20.4612,5235),
    (3, 'ORGANISATION TIPS - kako planiram projekte', 'Kako planiram obaveze, ispite i projekte.', 'productivity, student, organization', '/media/videos/project-organization.mp4', '/media/thumbs/project-organization.jpg', NOW() - INTERVAL '6 days', 43.3209, 21.8958, 36);

-- COMMENTS
INSERT INTO comments (post_id, author_id, text, created_at)
VALUES
    (1, 2, 'Prelepi kadrovi! Koju si kameru koristila?', NOW() - INTERVAL '1 day'),
    (1, 1, 'Hvala 😄 Snimano telefonom, samo dobra svetlost!', NOW() - INTERVAL '23 hours'),
    (2, 1, 'Ovo mi treba za projekat, super objašnjeno!', NOW() - INTERVAL '12 hours'),
    (2, 3, 'Da, i obavezno refresh token kasnije dodajte 🙂', NOW() - INTERVAL '11 hours'),
    (3, 2, 'Odlični saveti, posebno za audio!', NOW() - INTERVAL '2 hours'),
    (4, 3, 'Budimpešta je top! Dodaj i deo za hostele 😄', NOW() - INTERVAL '10 minutes'),
    (4, 1, 'Važi! Ubaciću i to u sledeći video.', NOW() - INTERVAL '5 minutes'),
    (5, 1, 'Ovo mi je baš pomoglo, zvuk mi je uvek bio problem.', NOW() - INTERVAL '20 hours'),
    (5, 3, 'Jednostavno i jasno objašnjeno, svaka čast!', NOW() - INTERVAL '18 hours'),
    (6, 2, 'Ovakva organizacija mi stvarno treba za faks.', NOW() - INTERVAL '40 days'),
    (6, 3, 'Drago mi je ako znači! Bez plana nema mira 😄', NOW() - INTERVAL '3 days'),
    (2, 1, 'Meni je ovde najviše pomoglo kako je SecurityConfig postavljen.', NOW() - INTERVAL '1 day'),
    (2, 2, 'Možda bi mogao primer sa roles i authorities?', NOW() - INTERVAL '22 hours'),
    (2, 3, 'Odlično! Konačno neko da objasni Spring Security bez komplikovanja.', NOW() - INTERVAL '3 days'),
    (2, 1, 'JWT je uvek konfuzan dok se ne vidi ceo flow, ovo je top.', NOW() - INTERVAL '5 hours'),
    (2, 3, 'Mali tip: HttpOnly cookie za token je sigurnija opcija.', NOW() - INTERVAL '6 hours'),
    (2, 1, 'Da li planiraš nastavak sa refresh tokenima?', NOW() - INTERVAL '30 minutes'),
    (2, 1, 'Backend deo je jasan, frontend primer bi bio šlag na tortu.', NOW() - INTERVAL '222 days'),
    (2, 2, 'Ovakav sadržaj treba više na našem jeziku 👏', NOW() - INTERVAL '444 days');


-- POST_LIKES
INSERT INTO post_likes (post_id, user_id)
VALUES
    (1, 2), -- Marko lajkuje Anin VLOG
    (1, 3), -- Jana lajkuje Anin VLOG
    (2, 1), -- Ana lajkuje Markov SpringBoot tutorial
    (5, 3); -- Jana lajkuje Markov video o zvuku


-- CRDT VIDEO VIEWS
-- NAPOMENA: CRDT podatke ne popunjavamo ovde jer se ime tabele dinamički menja
-- (video_view_crdt_replica1, video_view_crdt_replica2, itd.)
-- CRDT zapisi će biti automatski kreirani kada korisnik prvi put pogleda video
-- kroz VideoViewCrdtService.incrementViewForReplica() metodu