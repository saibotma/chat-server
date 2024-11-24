CREATE FUNCTION "update_updated_at"() RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

ALTER TABLE "message"
    ALTER COLUMN "created_at" SET DEFAULT now();

ALTER TABLE "message"
    ADD COLUMN "updated_at" timestamptz NULL;

CREATE TRIGGER "message_update_updated_at"
    BEFORE UPDATE
    ON "message"
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at();

ALTER TABLE "user"
    ALTER COLUMN "created_at" SET DEFAULT now();

ALTER TABLE "user"
    ADD COLUMN "updated_at" timestamptz NULL;

CREATE TRIGGER "user_update_updated_at"
    BEFORE UPDATE
    ON "user"
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at();

ALTER TABLE "channel_member"
    ALTER COLUMN "added_at" SET DEFAULT now();

ALTER TABLE "channel_member"
    ADD COLUMN "updated_at" timestamptz NULL;

CREATE TRIGGER "channel_member_update_updated_at"
    BEFORE UPDATE
    ON "channel_member"
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at();

ALTER TABLE "channel"
    ADD COLUMN "description" text NULL;

ALTER TABLE "channel"
    ALTER COLUMN "created_at" SET DEFAULT now();

ALTER TABLE "channel"
    ADD COLUMN "updated_at" timestamptz NULL;

CREATE TRIGGER "channel_update_updated_at"
    BEFORE UPDATE
    ON "channel"
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at();

ALTER TABLE "channel"
    ADD COLUMN "creator_user_id" varchar NULL;
ALTER TABLE "channel"
    ADD CONSTRAINT "channel_creator_user_id_fkey"
        FOREIGN KEY ("creator_user_id") REFERENCES "user" ("id") ON DELETE SET NULL;

CREATE TYPE "channel_event_type" AS ENUM (
    'set_channel_name',
    'set_channel_description',
    'add_member',
    'update_member_role',
    'remove_member',
    'send_message',
    'update_message_text',
    'update_message_replied_message_id',
    'delete_message'
    );

CREATE TABLE "channel_event"
(
    id         bigserial,
    channel_id uuid,
    type       "channel_event_type" NOT NULL,
    data       jsonb                NOT NULL,
    created_at timestamptz          NOT NULL DEFAULT now(),
    CONSTRAINT "channel_event_pkey" PRIMARY KEY ("id"),
    CONSTRAINT "channel_event_channel_id_fkey"
        FOREIGN KEY ("channel_id") REFERENCES "channel" ("id") ON DELETE CASCADE
);

CREATE FUNCTION insert_channel_event("channel_id" uuid, "type" "channel_event_type", "data" jsonb) RETURNS VOID AS
$$
BEGIN
    INSERT INTO "channel_event" ("channel_id", "type", "data", "created_at")
    VALUES ("channel_id", "type", "data", now());
END;
$$ LANGUAGE plpgsql;


CREATE FUNCTION create_channel_channel_event() RETURNS TRIGGER AS
$$
DECLARE
    is_insert bool;
    is_update bool;
    is_delete bool;
BEGIN
    is_insert = tg_op = 'INSERT';
    is_update = tg_op = 'UPDATE';
    is_delete = tg_op = 'DELETE';
    IF (is_update AND OLD."name" IS DISTINCT FROM NEW."name") THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."id",
                "type" => 'set_channel_name',
                "data" => jsonb_build_object('version', '1.0', 'name', NEW."name")
            );
    END IF;
    IF (is_update AND OLD."description" IS DISTINCT FROM NEW."description") THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."id",
                "type" => 'set_channel_description',
                "data" => jsonb_build_object('version', '1.0', 'description', NEW."description")
            );
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION create_channel_member_channel_event() RETURNS TRIGGER AS
$$
DECLARE
    is_insert bool;
    is_update bool;
    is_delete bool;
    user_id   varchar;
    reference jsonb;
BEGIN
    is_insert = tg_op = 'INSERT';
    is_update = tg_op = 'UPDATE';
    is_delete = tg_op = 'DELETE';
    -- Need this, because OLD/NEW are null depending on tg_op.
    user_id = coalesce(OLD."user_id", NEW."user_id");
    reference = jsonb_build_object('user_id', user_id);

    IF (is_insert) THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'add_member',
                "data" => reference || jsonb_build_object('version', '1.0', 'role', NEW."role")
            );
    END IF;
    IF (is_update AND OLD."role" IS DISTINCT FROM NEW."role") THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'update_member_role',
                "data" => reference || jsonb_build_object('version', '1.0', 'role', NEW."role")
            );
    END IF;
    IF (is_delete) THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'remove_member',
                "data" => reference || jsonb_build_object('version', '1.0')
            );
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION create_message_channel_event() RETURNS TRIGGER AS
$$
DECLARE
    is_insert  bool;
    is_update  bool;
    is_delete  bool;
    message_id uuid;
    reference  jsonb;
BEGIN
    is_insert = tg_op = 'INSERT';
    is_update = tg_op = 'UPDATE';
    is_delete = tg_op = 'DELETE';
    -- Need this, because OLD/NEW are null depending on tg_op.
    message_id = coalesce(OLD."id", NEW."id");
    reference = jsonb_build_object('message_id', message_id);

    IF (is_insert) THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'send_message',
                "data" => reference || jsonb_build_object(
                        'version', '1.0',
                        'text', NEW."text",
                        'replied_message_id', NEW."replied_message_id",
                        'creator_user_id', NEW."creator_user_id"
                    )
            );
    END IF;
    IF (is_update AND OLD."text" IS DISTINCT FROM NEW."text") THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'update_message_text',
                "data" => reference || jsonb_build_object('version', '1.0', 'text', NEW."text")
            );
    END IF;
    IF (is_update AND OLD."replied_message_id" != NEW."replied_message_id") THEN
        PERFORM insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'update_message_replied_message_id',
                "data" => reference || jsonb_build_object(
                        'version', '1.0',
                        'replied_message_id', NEW."replied_message_id"
                    )
            );
    END IF;
    IF (is_delete) THEN
        -- Don't need the message history and to improve data privacy
        -- can remove all previous events of this message.
        DELETE
        FROM "channel_event"
        where "data" -> 'message_id' = message_id;

        PERFORM insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'delete_message',
                "data" => reference || jsonb_build_object('version', '1.0')
            );
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "channel_update_create_channel_channel_event"
    AFTER UPDATE
    ON "channel"
    FOR EACH ROW
EXECUTE PROCEDURE create_channel_channel_event();

CREATE TRIGGER "channel_member_insert_create_channel_member_channel_event"
    AFTER INSERT
    ON "channel_member"
    FOR EACH ROW
EXECUTE PROCEDURE create_channel_member_channel_event();

CREATE TRIGGER "channel_member_update_create_channel_member_channel_event"
    AFTER UPDATE
    ON "channel_member"
    FOR EACH ROW
EXECUTE PROCEDURE create_channel_member_channel_event();

CREATE TRIGGER "channel_member_delete_create_channel_member_channel_event"
    AFTER DELETE
    ON "channel_member"
    FOR EACH ROW
EXECUTE PROCEDURE create_channel_member_channel_event();

CREATE TRIGGER "message_insert_create_message_channel_event"
    AFTER INSERT
    ON "message"
    FOR EACH ROW
EXECUTE PROCEDURE create_message_channel_event();

CREATE TRIGGER "message_update_create_message_channel_event"
    AFTER UPDATE
    ON "message"
    FOR EACH ROW
EXECUTE PROCEDURE create_message_channel_event();

CREATE TRIGGER "message_delete_create_message_channel_event"
    AFTER DELETE
    ON "message"
    FOR EACH ROW
EXECUTE PROCEDURE create_message_channel_event();

CREATE FUNCTION notify_channel_event() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM pg_notify('channel_event', NEW."id"::text);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "channel_event_insert_notify_channel_event"
    AFTER INSERT
    ON "channel_event"
    FOR EACH ROW
EXECUTE PROCEDURE notify_channel_event();

CREATE TABLE "contact"
(
    user_id_1  varchar     NOT NULL,
    user_id_2  varchar     NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NULL,
    CONSTRAINT "contact_pkey" PRIMARY KEY ("user_id_1", "user_id_2"),
    CONSTRAINT "contact_user_id_1_fkey" FOREIGN KEY ("user_id_1") REFERENCES "user" ("id"),
    CONSTRAINT "contact_user_id_2_fkey" FOREIGN KEY ("user_id_2") REFERENCES "user" ("id")
);

CREATE TRIGGER "contact_update_updated_at"
    BEFORE UPDATE
    ON "contact"
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at();
