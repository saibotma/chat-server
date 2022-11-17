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
    data       jsonb                NULL,
    created_at timestamptz          NOT NULL,
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
        CALL insert_channel_event(
                "channel_id" => NEW."id",
                "type" => 'set_channel_name',
                "data" => jsonb_build_object('version', '1.0', 'name', NEW."name")
            );
    END IF;
    IF (is_update AND OLD."description" IS DISTINCT FROM NEW."description") THEN
        CALL insert_channel_event(
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
BEGIN
    is_insert = tg_op = 'INSERT';
    is_update = tg_op = 'UPDATE';
    is_delete = tg_op = 'DELETE';

    IF (is_insert) THEN
        CALL insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'add_member',
                "data" => jsonb_build_object('version', '1.0', 'user_id', NEW."user_id", 'role', NEW."role")
            );
    END IF;
    IF (is_update AND OLD."role" IS DISTINCT FROM NEW."role") THEN
        CALL insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'update_member_role',
                "data" => jsonb_build_object('version', '1.0', 'user_id', NEW."user_id", 'role', NEW."role")
            );
    END IF;
    IF (is_delete) THEN
        CALL insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'remove_member',
                "data" => jsonb_build_object('version', '1.0', 'user_id', NEW."user_id")
            );
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION create_message_channel_event() RETURNS TRIGGER AS
$$
DECLARE
    is_insert bool;
    is_update bool;
    is_delete bool;
BEGIN
    is_insert = tg_op = 'INSERT';
    is_update = tg_op = 'UPDATE';
    is_delete = tg_op = 'DELETE';

    IF (is_insert) THEN
        CALL insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'send_message',
                "data" => jsonb_build_object(
                        'version', '1.0',
                        'id', NEW."id",
                        'text', NEW."text",
                        'replied_message_id', NEW."replied_message_id",
                        'creator_user_id', NEW."creator_user_id"
                    )
            );
    END IF;
    IF (is_update AND OLD."text" IS DISTINCT FROM NEW."text") THEN
        CALL insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'update_message_text',
                "data" => jsonb_build_object('version', '1.0', 'id', NEW."id", 'text', NEW."text")
            );
    END IF;
    IF (is_update AND OLD."replied_message_id" != NEW."replied_message_id") THEN
        CALL insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'update_message_replied_message_id',
                "data" => jsonb_build_object(
                        'version', '1.0',
                        'id', NEW."id",
                        'replied_message_id', NEW."replied_message_id"
                    )
            );
    END IF;
    IF (is_delete) THEN
        CALL insert_channel_event(
                "channel_id" => NEW."channel_id",
                "type" => 'delete_message',
                "data" => jsonb_build_object('version', '1.0', 'id', NEW."id")
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

CREATE TYPE "user_event_type" AS ENUM ('update_name');

CREATE TABLE "user_event"
(
    id         bigserial,
    user_id    varchar           NOT NULL,
    type       "user_event_type" NOT NULL,
    data       jsonb             NULL,
    created_at timestamptz       NOT NULL,
    CONSTRAINT "user_event_pkey" PRIMARY KEY ("id")
);

CREATE FUNCTION create_user_event() RETURNS TRIGGER AS
$$
BEGIN
    IF (tg_op = 'UPDATE') THEN
        IF (OLD."name" IS DISTINCT FROM NEW."name") THEN
            INSERT INTO "user_event" ("user_id", "type", "data", "created_at")
            VALUES (NEW."id", 'update_name', jsonb_build_object('version', '1.0.0', 'name', NEW."name"), now());
        END IF;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "user_update_create_user_event"
    AFTER UPDATE
    ON "user"
    FOR EACH ROW
EXECUTE PROCEDURE create_user_event();

CREATE FUNCTION notify_channel_event() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM pg_notify('channel_event', NEW."id");
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "channel_event_insert_notify_channel_event"
    AFTER INSERT
    ON "channel_event"
    FOR EACH ROW
EXECUTE PROCEDURE notify_channel_event();

CREATE FUNCTION notify_user_event() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM pg_notify('user_event', NEW."id");
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "channel_event_insert_notify_channel_event"
    AFTER INSERT
    ON "user_event"
    FOR EACH ROW
EXECUTE PROCEDURE notify_user_event();
