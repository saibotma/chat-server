ALTER TABLE "user"
    DROP COLUMN "created_at";
ALTER TABLE "channel"
    DROP COLUMN "created_at";
-- Maybe drop is_managed support for channel.
ALTER TABLE "channel_member"
    DROP COLUMN "added_at";

CREATE TYPE "channel_content_type" AS ENUM (
    'message',
    'message_edit',
    'message_archiving',
    'message_deletion',
    'read_confirmation',
    'channel_name_update',
    'member_addition',
    'member_role_change',
    'member_removal'
    );

CREATE TABLE "channel_content"
(
    id         bigserial,
    channel_id uuid                   NOT NULL,
    type       "channel_content_type" NOT NULL,
    data       jsonb                  NOT NULL,
    created_at timestamptz            NOT NULL DEFAULT now(),
    CONSTRAINT "channel_content_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "channel_content_channel_id_fkey" FOREIGN KEY (channel_id) REFERENCES "channel" ("id")
);

-- TODO(saibotma): Copy  messages to channel_content.
DROP TABLE "message";

-- Change event

CREATE TYPE "change_event_type" AS ENUM (
    'add_member',
    'update_member',
    'remove_member',
    'add_channel_content',
    'start_typing',
    'create_channel',
    'update_channel',
    'update_private_channel_settings'
    );

CREATE TABLE "change_event"
(
    id         bigserial,
    type       "change_event_type" NOT NULL,
    data       jsonb               NOT NULL,
    created_at timestamptz         NOT NULL DEFAULT now(),
    CONSTRAINT "change_event_pkey" PRIMARY KEY ("id")
);

CREATE FUNCTION notify_change_event() RETURNS TRIGGER AS
$$
BEGIN
    -- TODO(saibotma): Maybe put the whole event there.
    PERFORM pg_notify('change_event', NEW."id"::text);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "change_event_insert_notify_change_event"
    AFTER INSERT
    ON "change_event"
    FOR EACH ROW
EXECUTE PROCEDURE notify_change_event();
