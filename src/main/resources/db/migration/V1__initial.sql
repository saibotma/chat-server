CREATE TYPE "chat_room_member_role" AS ENUM ('admin', 'user');

CREATE TABLE "user"
(
    "id" varchar,
    CONSTRAINT "user_id_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "chat_room"
(
    "id"         uuid,
    "name"       varchar(128) NULL,
    "is_managed" bool         NOT NULL,
    "created_at" timestamptz  NOT NULL,
    CONSTRAINT "chat_room_id_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "chat_room_member"
(
    "chat_room_id" uuid,
    "user_id"      varchar,
    "role"         "chat_room_member_role" NOT NULL,
    CONSTRAINT "chat_room_member_chat_room_id_user_id_pkey"
        PRIMARY KEY ("chat_room_id", "user_id"),
    CONSTRAINT "chat_room_member_chat_room_id"
        FOREIGN KEY ("chat_room_id") REFERENCES "chat_room" ("id"),
    CONSTRAINT "chat_room_member_user_id"
        FOREIGN KEY ("user_id") REFERENCES "user" ("id")
);

CREATE TABLE "chat_message"
(
    "id"                   uuid,
    "text"                 varchar(1048) NOT NULL,
    "responded_message_id" uuid,
    "extended_message_id"  uuid,
    creator_user_id        varchar,
    "chat_room_id"         uuid          NOT NULL,
    "created_at"           timestamptz   NOT NULL,
    CONSTRAINT "chat_message_id_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "chat_message_responded_message_id_fkey"
        FOREIGN KEY ("responded_message_id") REFERENCES "chat_message" ("id") ON DELETE SET NULL,
    CONSTRAINT "chat_message_extended_message_id_fkey"
        FOREIGN KEY ("extended_message_id") REFERENCES "chat_message" ("id") ON DELETE SET NULL,
    CONSTRAINT "chat_message_creator_user_id_fkey"
        FOREIGN KEY (creator_user_id) REFERENCES "user" ("id") ON DELETE SET NULL,
    CONSTRAINT "chat_message_chat_room_id_fkey"
        FOREIGN KEY ("chat_room_id") REFERENCES "chat_room" ("id") ON DELETE CASCADE
);
