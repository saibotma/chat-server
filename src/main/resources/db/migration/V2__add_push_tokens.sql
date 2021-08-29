CREATE TABLE "firebase_push_token"
(
    "user_id"    varchar      NOT NULL,
    "device_id"  varchar(256) NOT NULL,
    "push_token" varchar(256) NOT NULL,
    CONSTRAINT "firebase_push_token_device_id_pkey" PRIMARY KEY ("device_id"),
    CONSTRAINT "firebase_push_token_user_id_fkey"
        FOREIGN KEY ("user_id")
            REFERENCES "user" ("id") ON DELETE CASCADE
);
