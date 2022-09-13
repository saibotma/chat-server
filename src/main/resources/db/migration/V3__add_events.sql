CREATE TABLE "set_channel_meta_event"
(
    "channel_event_id" bigserial,
    "name"             varchar(512)  NULL,
    "description"      varchar(4096) NULL,
    CONSTRAINT "set_channel_meta_event_channel_event_id_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "set_channel_meta_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE
);

CREATE TABLE "add_member_event"
(
    "channel_event_id" bigserial,
    "user_id"          varchar             NULL,
    "role"             channel_member_role NOT NULL,
    CONSTRAINT "add_member_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "add_member_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "add_member_event_user_id_fkey"
        FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "update_member_event"
(
    "channel_event_id" bigserial,
    "source_id"        bigserial           NOT NULL,
    "role"             channel_member_role NOT NULL,
    CONSTRAINT "add_member_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "add_member_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "add_member_event_source_id_fkey"
        FOREIGN KEY ("source_id") REFERENCES "add_member_event" ("channel_event_id") ON DELETE CASCADE
);

CREATE TABLE "delete_member_event"
(
    "channel_event_id" bigserial,
    "source_id"        bigserial NOT NULL,
    CONSTRAINT "delete_member_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "delete_member_event_channel_event_id_fkey"
        FOREIGN KEY ("channel_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "delete_member_event_source_id_fkey"
        FOREIGN KEY ("source_id") REFERENCES "add_member_event" ("channel_event_id") ON DELETE CASCADE
);

CREATE TABLE "create_message_event"
(
    "channel_event_id" bigserial,
    "text"             varchar(4096)       NULL,
    "replied_event_id" bigserial           NULL,
    "creator_user_id"  varchar             NULL,
    "role"             channel_member_role NOT NULL,
    CONSTRAINT "create_message_event_pkey" PRIMARY KEY ("channel_event_id"),
    CONSTRAINT "create_message_event_replied_event_id_fkey"
        FOREIGN KEY ("replied_event_id") REFERENCES "channel_event" ("id") ON DELETE CASCADE,
    CONSTRAINT "add_member_event_user_id_fkey"
        FOREIGN KEY ("user_id") REFERENCES "user" ("id") ON DELETE SET NULL
);

CREATE TABLE "channel_event"
(
    id         bigserial,
    source_id  bigserial   NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT "channel_event_source_id_fkey"
        FOREIGN KEY ("source_id") REFERENCES "channel_event" ("source_id") ON DELETE CASCADE
);
