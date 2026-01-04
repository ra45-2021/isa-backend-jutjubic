-- USERS
INSERT INTO users (username, password_hash, display_name, bio, role)
VALUES
    ('ana.zaric', '{noop}pass', 'Ana Zarić', 'Studentkinja softverskog inženjerstva. Volim putovanja i planinarenje.', 'USER'),
    ('mark0',  '{noop}pass', 'Marko Marković', 'Backend dev. Spring Boot, PostgreSQL, i dobra kafa.', 'USER'),
    ('jana_p',    '{noop}pass', 'Jana Petrović', 'Video editor + content. Montaža, rezovi i titlovi.', 'USER');

-- POSTS
INSERT INTO posts (author_id, title, description, tags_text, video_url, thumbnail_url, created_at)
VALUES
    (1, 'Fruška gora VLOG', 'Kampovanje 5 dana u šumi na Fruškoj gori.', 'vlog, priroda, planinarenje', '/media/videos/fruska-gora.mp4', '/media/thumbs/fruska-gora.jpg', NOW() - INTERVAL '299 days'),
    (2, 'Intro to SpringBoot', 'Coding for beginners - SpringBoot','spring, jwt, backend', '/media/videos/jwt-spring.mp4', '/media/thumbs/jwt-spring.jpg', NOW() - INTERVAL '3 days' ),
    (3, 'Montaža thumbnaila za početnike', '3 trika da video izgleda čistije i profesionalnije.','video-editing, capcut', '/media/videos/editing-basics.mp4', '/media/thumbs/editing-basics.jpg', NOW() - INTERVAL '5 hours'),
    (1, 'TOP 10 Mesta u Budimpešti', 'Top mesta + saveti za prevoz i budžet.', 'tips, guide, budapest','/media/videos/budapest-guide.mp4', '/media/thumbs/budapest-guide.jpg', NOW() - INTERVAL '2 weeks');

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
