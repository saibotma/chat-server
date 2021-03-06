CREATE TYPE channel_member_role AS ENUM ('admin', 'user');

CREATE TABLE "user"
(
    "id"         varchar,
    "name"       varchar(500),
    "created_at" timestamptz NOT NULL,
    CONSTRAINT "user_id_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "channel"
(
    "id"         uuid,
    "name"       varchar(128) NULL,
    "is_managed" bool         NOT NULL,
    "created_at" timestamptz  NOT NULL,
    CONSTRAINT "channel_id_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "channel_member"
(
    "channel_id" uuid,
    "user_id"    varchar,
    "role"       channel_member_role NOT NULL,
    "added_at"   timestamptz         NOT NULL,
    CONSTRAINT "channel_member_channel_id_user_id_pkey"
        PRIMARY KEY ("channel_id", "user_id"),
    CONSTRAINT "channel_member_channel_id_fkey"
        FOREIGN KEY ("channel_id") REFERENCES "channel" ("id") ON DELETE CASCADE,
    CONSTRAINT "channel_member_user_id_fkey"
        FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE CASCADE
);

CREATE TABLE "message"
(
    "id"                 uuid,
    "text"               varchar(1048),
    "replied_message_id" uuid,
    "creator_user_id"    varchar,
    "channel_id"         uuid        NOT NULL,
    "created_at"         timestamptz NOT NULL,
    CONSTRAINT "message_id_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "message_replied_message_id_fkey"
        FOREIGN KEY ("replied_message_id") REFERENCES "message" ("id") ON DELETE SET NULL,
    CONSTRAINT "message_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL,
    CONSTRAINT "message_channel_id_fkey"
        FOREIGN KEY ("channel_id") REFERENCES "channel" ("id") ON DELETE CASCADE
);
