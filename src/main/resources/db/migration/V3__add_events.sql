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

CREATE TABLE "update_channel_meta_event"
(
    "channel_event_id" bigint,
    "name"             varchar(512) NULL,
    "description"      varchar(4096) NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "update_channel_meta_event_channel_event_id_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "update_channel_meta_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "update_channel_meta_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "create_member_event"
(
    "channel_event_id" bigint,
    "user_id"          varchar NULL,
    "role"             channel_member_role NOT NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "create_member_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "create_member_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "create_member_event_user_id_fkey"
        FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE SET NULL,
    CONSTRAINT "create_member_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "update_member_event"
(
    "channel_event_id" bigint,
    "source_id"        bigint              NOT NULL,
    "role"             channel_member_role NOT NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "update_member_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "update_member_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "update_member_event_source_id_fkey"
        FOREIGN KEY ("source_id") REFERENCES "create_member_event" ("channel_event_id") ON DELETE CASCADE,
    CONSTRAINT "update_member_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "delete_member_event"
(
    "channel_event_id" bigint,
    "source_id"        bigint NOT NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "delete_member_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "delete_member_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "delete_member_event_source_id_fkey"
        FOREIGN KEY ("source_id") REFERENCES "create_member_event" ("channel_event_id") ON DELETE CASCADE,
    CONSTRAINT "delete_member_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "create_message_event"
(
    "channel_event_id" bigint,
    "text"             text NULL,
    "replied_event_id" bigint NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "create_message_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "create_message_event_replied_event_id_fkey"
        FOREIGN KEY ("replied_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "create_message_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "update_message_event"
(
    "channel_event_id" bigint,
    "source_id"        bigint NOT NULL,
    "text"             text NULL,
    "replied_event_id" bigint NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "update_message_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "update_message_event_replied_event_id_fkey"
        FOREIGN KEY ("replied_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "update_message_event_source_id_fkey"
        FOREIGN KEY ("source_id") REFERENCES "create_message_event" ("channel_event_id") ON DELETE CASCADE,
    CONSTRAINT "update_message_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "delete_message_event"
(
    "channel_event_id" bigint,
    "source_id"        bigint NOT NULL,
    "creator_user_id"  varchar NULL,
    CONSTRAINT "delete_message_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "delete_message_event_source_id_fkey"
        FOREIGN KEY ("source_id") REFERENCES "create_message_event" ("channel_event_id") ON DELETE CASCADE,
    CONSTRAINT "update_message_event_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

/*
-- Migrate all channel members to new event structure.
INSERT INTO "create_member_event" values ()

-- Migrate all messages to new event structure.
INSERT INTO "create_message_event" ("")
*/
