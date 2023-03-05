# java-filmorate
Template repository for Filmorate project.

Получение всех фильмов:
SELECT f.id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       r.name,
       COUNT(user_id)
FROM films AS f
LEFT OUTER JOIN ratings AS r ON r.id = f.rating_id
LEFT OUTER JOIN likes AS l ON f.id = l.film_id
GROUP BY f.id,
         r.name;
		 

SELECT f.id,
       g.name
FROM films AS f
LEFT OUTER JOIN film_genre AS fg ON fg.film_id = f.id
LEFT OUTER JOIN genres AS g ON g.id = fg.genre_id;

(Могу предположить, что на запрос о фильме достаточно вернуть количество лайков у фильма, а не id лайкнувших его пользователей. 
Кроме того предположила, что проще заполнять dto фильма в два запроса к бд, а не в один, 
но как будет в действительности лучше станет ясно после изучения дальнейшей теории и финального тз, я думаю.
Так что это просто один из вариантов возможных запросов.)

Получение фильма по id:
SELECT f.id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       r.name,
       COUNT(user_id)
FROM films AS f
LEFT OUTER JOIN ratings AS r ON r.id = f.rating_id
LEFT OUTER JOIN likes AS l ON f.id = l.film_id
WHERE f.id = id
GROUP BY f.id;


SELECT g.name
FROM films AS f
LEFT OUTER JOIN film_genre AS fg ON fg.film_id = f.id
LEFT OUTER JOIN genres AS g ON g.id = fg.genre_id
WHERE f.id = id;

Получение топ n фильмов:
SELECT f.id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       r.name,
       COUNT(user_id)
FROM films AS f
LEFT OUTER JOIN ratings AS r ON r.id = f.rating_id
LEFT OUTER JOIN likes AS l ON f.id = l.film_id
WHERE f.id IN
    (SELECT f.id
     FROM films AS f
     LEFT OUTER JOIN likes AS l ON f.id = l.film_id
     GROUP BY f.id
     ORDER BY COUNT(user_id) DESC
     LIMIT n)
GROUP BY f.id,
         r.name
ORDER BY COUNT(user_id) DESC;


Получение всех пользователей:
SELECT u.id,
       u.email,
       u.login,
       u.name,
       u.birthday
FROM users AS u;


SELECT u.id,
       f.friend_2_id,
       s.name
FROM users AS u
LEFT OUTER JOIN friends AS f ON u.id = f.friend_1_id
LEFT OUTER JOIN statuses AS s ON f.status_id = s.id;

Получение пользователя по id:
SELECT u.id,
       u.email,
       u.login,
       u.name,
       u.birthday
FROM users AS u
WHERE u.id = id;


SELECT f.friend_2_id,
       s.name
FROM users AS u
LEFT OUTER JOIN friends AS f ON u.id = f.friend_1_id
LEFT OUTER JOIN statuses AS s ON f.status_id = s.id
WHERE u.id = id;

Получение списка друзей пользователя с id = id:
SELECT u.id,
       u.email,
       u.login,
       u.name,
       u.birthday
FROM users AS u
WHERE u.id IN
    (SELECT f.friend_2_id
     FROM users AS u
     LEFT OUTER JOIN friends AS f ON u.id = f.friend_1_id
     LEFT OUTER JOIN statuses AS s ON f.status_id = s.id
     WHERE u.id = id
       AND s.name = 'confirmed');


SELECT f.friend_1_id,
       f.friend_2_id,
       s.name
FROM users AS u
LEFT OUTER JOIN friends AS f ON u.id = f.friend_1_id
LEFT OUTER JOIN statuses AS s ON f.status_id = s.id
WHERE u.id IN
    (SELECT f.friend_2_id
     FROM users AS u
     LEFT OUTER JOIN friends AS f ON u.id = f.friend_1_id
     LEFT OUTER JOIN statuses AS s ON f.status_id = s.id
     WHERE u.id = id
       AND s.name = 'confirmed');

Получение списка общих друзей пользователей с id_1 и id_2:

SELECT u.id,
       u.email,
       u.login,
       u.name,
       u.birthday
FROM users AS u
WHERE u.id IN
    (SELECT p1_f.friend_2_id
     FROM
       (SELECT *
        FROM friends
        WHERE friend_1_id = id_1) AS p1_f
     INNER JOIN
       (SELECT *
        FROM friends
        WHERE friend_1_id = id_2) AS p2_f ON p1_f.friend_2_id = p2_f.friend_2_id);
