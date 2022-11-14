CREATE TYPE channel_event_type AS ENUM
CREATE TYPE channel_member_event_type AS ENUM ('create', 'update', 'delete');
CREATE TYPE channel_message_event_type AS ENUM ('create', 'update', 'delete');

ALTER TABLE "channel"
    ADD COLUMN "description" text NULL;

ALTER TABLE "channel"
    ADD COLUMN "creator_user_id" varchar NULL;
ALTER TABLE "channel"
    ADD CONSTRAINT "channel_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL;

CREATE TABLE "channel_event"
(
    id         bigserial,
    channel_id uuid,
    created_at timestamptz NOT NULL,
    CONSTRAINT "channel_event_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "channel_event_channel_id_fkey"
        FOREIGN KEY ("channel_id") REFERENCES "channel" ("id") ON DELETE CASCADE
);

CREATE TABLE "channel_meta_event"
(
    "channel_event_id" bigint,
    "name"             varchar(512) NULL,
    "description"      varchar(4096) NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "channel_meta_event_channel_event_id_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "channel_meta_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "channel_meta_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "channel_member_event"
(
    "channel_event_id" bigint,
    "type"             "channel_member_event_type" NOT NULL,
    "user_id"          varchar NULL,
    "role"             channel_member_role NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "channel_member_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "channel_member_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "channel_member_event_user_id_fkey"
        FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE SET NULL,
    CONSTRAINT "channel_member_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "channel_message_event"
(
    "channel_event_id" bigint,
    "type"             "channel_message_event_type" NOT NULL,
    "message_id"       uuid                         NOT NULL,
    CONSTRAINT "channel_message_event_pkey" PRIMARY KEY ("channel_event_id")
);

/*
-- Migrate all channel members to new event structure.
INSERT INTO "create_member_event" values ()

-- Migrate all messages to new event structure.
INSERT INTO "create_message_event" ("")
*/
