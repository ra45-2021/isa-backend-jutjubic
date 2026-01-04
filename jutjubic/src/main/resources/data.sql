-- USERS
INSERT INTO users (username, password_hash, display_name, bio, role)
VALUES
    ('ana', '{noop}pass', 'Ana Zarić', 'Studentkinja softverskog inženjerstva. Volim putovanja i planinarenje.', 'USER'),
    ('marko',  '{noop}pass', 'Marko Marković', 'Backend dev. Spring Boot, PostgreSQL, i dobra kafa.', 'USER'),
    ('jana',    '{noop}pass', 'Jana Petrović', 'Video editor + content. Montaža, rezovi i titlovi.', 'USER');

-- POSTS
INSERT INTO posts (author_id, title, description, video_url, created_at)
VALUES
    (1, 'Vlog sa Fruške gore', 'Kratak vlog sa šetnje i pogleda sa vrha.', 'https://videos.jutjubic.com/fruska-gora.mp4', NOW() - INTERVAL '2 days'),
    (2, 'Kako radi JWT u Spring-u', 'Mini objašnjenje tokena i filtera na primeru.', 'https://videos.jutjubic.com/jwt-spring.mp4', NOW() - INTERVAL '18 hours'),
    (3, 'Montaža za početnike', '3 trika da video izgleda čistije i profesionalnije.', 'https://videos.jutjubic.com/editing-basics.mp4', NOW() - INTERVAL '3 hours'),
    (1, 'Budimpešta: moj mini vodič', 'Top mesta + saveti za prevoz i budžet.', 'https://videos.jutjubic.com/budapest-guide.mp4', NOW() - INTERVAL '25 minutes');

-- COMMENTS
INSERT INTO comments (post_id, author_id, text, created_at)
VALUES
    (1, 2, 'Prelepi kadrovi! Koju si kameru koristila?', NOW() - INTERVAL '1 day'),
    (1, 1, 'Hvala 😄 Snimano telefonom, samo dobra svetlost!', NOW() - INTERVAL '23 hours'),
    (2, 1, 'Ovo mi treba za projekat, super objašnjeno!', NOW() - INTERVAL '12 hours'),
    (2, 3, 'Da, i obavezno refresh token kasnije dodajte 🙂', NOW() - INTERVAL '11 hours'),
    (3, 2, 'Odlični saveti, posebno za audio!', NOW() - INTERVAL '2 hours'),
    (4, 3, 'Budimpešta je top! Dodaj i deo za hostele 😄', NOW() - INTERVAL '10 minutes'),
    (4, 1, 'Važi! Ubaciću i to u sledeći video.', NOW() - INTERVAL '5 minutes');
