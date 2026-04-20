CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE location
(
    location_id uuid         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    address     varchar(255) NOT NULL,
    city        varchar(255) NOT NULL,
    country     varchar(255) NOT NULL,
    CONSTRAINT uniqueaddress UNIQUE (country, city, address)
);

CREATE TABLE users
(
    user_id  uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    email    varchar(255),
    password varchar(255),
    role     varchar(255),
    username varchar(255),

    CONSTRAINT users_role_check
        CHECK (role::text = ANY ((ARRAY['USER','HOST','ADMIN'])::text[]))
);

CREATE TABLE listing
(
    listing_id      uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      timestamp(6),
    description     varchar(255),
    price_per_night numeric(38, 2),
    title           varchar(255),
    updated_at      timestamp(6),

    location_id     uuid,
    user_id         uuid,

    CONSTRAINT fk_listing_location
        FOREIGN KEY (location_id) REFERENCES location(location_id),

    CONSTRAINT fk_listing_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE booking
(
    booking_id     uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    check_in_date  timestamp(6),
    check_out_date timestamp(6),
    created_at     timestamp(6),
    status         varchar(255),
    total_price    numeric(38, 2),
    updated_at     timestamp(6),

    listing_id     uuid,
    user_id        uuid,

    CONSTRAINT booking_status_check
        CHECK (status::text = ANY ((ARRAY['PENDING','CONFIRMED','CANCELLED'])::text[])),

    CONSTRAINT fk_booking_listing
        FOREIGN KEY (listing_id) REFERENCES listing(listing_id),

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE listing_image
(
    listing_image_id uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    url              varchar(255),
    listing_id       uuid,

    CONSTRAINT fk_listing_image_listing
        FOREIGN KEY (listing_id) REFERENCES listing(listing_id)
);

CREATE TABLE reviews
(
    review_id          uuid NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    comment            varchar(255),
    created_at         timestamp(6),
    rating             numeric(38, 2),

    listing_listing_id uuid,
    user_user_id       uuid,

    CONSTRAINT fk_reviews_listing
        FOREIGN KEY (listing_listing_id) REFERENCES listing(listing_id),

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_listing_location_id ON listing (location_id);
CREATE INDEX idx_listing_user_id     ON listing (user_id);

CREATE INDEX idx_booking_listing_id  ON booking (listing_id);
CREATE INDEX idx_booking_user_id     ON booking (user_id);

CREATE INDEX idx_listing_image_listing_id ON listing_image (listing_id);

CREATE INDEX idx_reviews_listing_id  ON reviews (listing_listing_id);
CREATE INDEX idx_reviews_user_id     ON reviews (user_user_id);

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password SET NOT NULL,
    ALTER COLUMN username SET NOT NULL,
    ALTER COLUMN role SET NOT NULL;