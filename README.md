## Это репозиторий проекта "Filmorate" 
#### Схема базы данных
![Схема базы данных](https://user-images.githubusercontent.com/73291118/217991010-220fd135-9a54-48e6-b327-dfa990595647.png)

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
2. SELECT * FROM user; - получение всех фильмов
3. получение топ N наиболее популярных фильмов:
   SELECT name 
   FROM film
   WHERE film_id IN(SELECT film_id
                    FROM film_like
                    GROUP BY film_id
                    ORDER BY COUNT(*) DESC
                    LIMIT *N*);
4. получение списка общих друзей с другим пользователем:
   SELECT name
   FROM (SELECT name
         FROM user
         WHERE user_id IN(SELECT friend_id
                          FROM friendship
                          WHERE user_id = 1 AND status = 'подтвержден')) AS first_user_friends
   WHERE name IN(SELECT name
                 FROM user
                 WHERE user_id IN(SELECT friend_id
                                   FROM friendship
                                   WHERE user_id = 2 AND status = 'подтвержден'));
------
Приложение написано на Java с использованием Maven, Spring Boot, PostgresQL, протестировано через Postman
