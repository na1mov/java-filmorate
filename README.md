## Это репозиторий проекта "Filmorate" 
#### Схема базы данных
![fixedErDiagram](https://user-images.githubusercontent.com/73291118/219650103-a4327570-d0cc-49ee-b71c-98625ce2a26f.jpg)

База данных соответствует третьей нормальной форме(3NF):
1. UNF - порядок строк не имеет значения, так же как не имеет значения порядок столбцов
2. 1NF:
   - в таблице не должно быть дублирующих строк
   - в каждой ячейке таблицы хранится атомарное значение (одно не составное значение)
   - в столбце хранятся данные одного типа
   - отсутствуют массивы и списки в любом виде
3. 2NF:
   - таблица должна находиться в первой нормальной форме
   - таблица должна иметь ключ
   - все неключевые столбцы таблицы должны зависеть от полного ключа (в случае если он составной)
4. 3NF:
   - в таблицах должна отсутствовать транзитивная зависимость(таблица должна содержать правильные неключевые столбцы)

Примеры запросов к БД:
1. SELECT * FROM user; - получение всех пользователей
2. SELECT * FROM film; - получение всех фильмов
3. получение топ N(?) наиболее популярных фильмов:
   SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_mpa_id, fl.like_count
   FROM film AS f
   LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count
   FROM film_like GROUP BY film_id) AS fl ON f.film_id = fl.film_id
   ORDER BY fl.like_count DESC LIMIT ?;
4. получение списка общих друзей с другим пользователем:
   SELECT * FROM users WHERE user_id IN(
   SELECT friend_id FROM friendship WHERE user_id = 1) AND user_id IN(
   SELECT friend_id FROM friendship WHERE user_id = 2);
------
Приложение написано на Java с использованием Maven, Spring Boot, JDBC, H2 DB, протестировано через Postman, jUnit
