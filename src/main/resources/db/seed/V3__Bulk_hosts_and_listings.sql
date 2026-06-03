WITH
    host_data AS (
        SELECT
            gen_random_uuid() AS user_id,
            'bulk_host_' || i || '@example.com' AS email,
            '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK4.6mWqKWNG7h5cH5IE8Zas6' AS password,
            'HOST' AS role,
            'host_runner_' || i AS username,
            i AS rn
        FROM generate_series(1, 1000) AS i
    ),
    inserted_hosts AS (
        INSERT INTO users (user_id, email, password, role, username)
            SELECT user_id, email, password, role, username FROM host_data
            RETURNING user_id
    ),

    location_data AS (
        SELECT
            i || ' Random St.' AS address,
            (ARRAY['Miami', 'London', 'Paris', 'Tokyo', 'New York', 'Karaganda', 'Astana', 'Almaty'])[mod(i, 8) + 1] AS city,
            (ARRAY['USA', 'UK', 'France', 'Japan', 'USA', 'Kazakhstan', 'Kazakhstan', 'Kazakhstan'])[mod(i, 8) + 1] AS country,
            i AS rn
        FROM generate_series(1, 1000) AS i
    ),
    inserted_locations AS (
        INSERT INTO location (address, city, country)
            SELECT address, city, country FROM location_data
            RETURNING location_id, address
    )

INSERT INTO listing (created_at, description, price_per_night, average_rating, reviews_count, title, updated_at, location_id, user_id)
SELECT
    NOW() - (random() * interval '365 days') AS created_at,
    'Generated elite property description for index benchmarking #' || s.i AS description,
    (floor(random() * (500 - 30 + 1)) + 30)::numeric(10,2) AS price_per_night,
    0.00 AS average_rating,
    0 AS reviews_count,
    'Apartment Premium Class #' || s.i AS title,
    NOW() AS updated_at,
    il.location_id,
    h.user_id
FROM generate_series(1, 3000) AS s(i)
         JOIN host_data h ON h.rn = (mod(s.i, 1000) + 1)
         JOIN inserted_locations il ON il.address = (mod(s.i, 1000) + 1) || ' Random St.';