ALTER TABLE "user"
    ADD COLUMN "updated_at" timestamptz NULL;

ALTER TABLE "channel_member"
    ADD COLUMN "updated_at" timestamptz NULL;

ALTER TABLE "channel"
    ADD COLUMN "description" text NULL;

ALTER TABLE "channel"
    ADD COLUMN "updated_at" timestamptz NULL;

ALTER TABLE "channel"
    ADD COLUMN "creator_user_id" varchar NULL;
ALTER TABLE "channel"
    ADD CONSTRAINT "channel_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL;

CREATE TYPE "channel_event_type" AS ENUM (
    'create',
    'set_channel_name',
    'set_channel_description',
    'add_member',
    'update_member_role',
    'remove_member',
    'send_message',
    'update_message_text',
    'update_message_replied_message_id',
    'delete_message',
    'delete'
);

CREATE TABLE "channel_event"
(
    id         bigserial,
    channel_id uuid,
    type       "channel_event_type" NOT NULL,
    data       jsonb NULL,
    created_at timestamptz          NOT NULL,
    CONSTRAINT "channel_event_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "channel_event_channel_id_fkey"
        FOREIGN KEY ("channel_id") REFERENCES "channel" ("id") ON DELETE CASCADE
);

CREATE TYPE "user_event_type" AS ENUM (
    'update_name',
);

CREATE TABLE "user_event"
(
    id         bigserial,
    user_id    varchar           NOT NULL,
    type       "user_event_type" NOT NULL,
    data       jsonb NULL,
    created_at timestamptz       NOT NULL,
    CONSTRAINT "user_event_pkey" PRIMARY KEY ("id")
);
