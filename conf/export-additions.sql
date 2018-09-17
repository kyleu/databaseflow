/* Export schema additions, v1.0 */

create extension if not exists hstore;

create table if not exists "system_users" (
  "id" uuid primary key,
  "username" character varying(256) unique,
  "provider" character varying(64) not null,
  "key" varchar(2048) not null,
  "prefs" character varying(4096) not null,
  "role" character varying(64) not null,
  "created" timestamp without time zone not null
);

create index if not exists "system_users_username" on "system_users" using btree ("username" asc);
create unique index if not exists "system_users_provider_key" on "system_users" using btree ("provider" asc, "key" asc);
create index if not exists "system_users_provider" on "system_users" using btree ("provider" asc);
create index if not exists "system_users_key" on "system_users" using btree ("key" asc);

create table if not exists "password_info" (
  "provider" varchar(64) not null,
  "key" varchar(2048) not null,
  "hasher" varchar(64) not null,
  "password" varchar(256) not null,
  "salt" varchar(256),
  "created" timestamp without time zone not null,
  constraint "password_info_pkey" primary key ("provider", "key")
);
create index if not exists "password_info_key" on "password_info" using btree ("key" asc);

create table if not exists "oauth2_info" (
  "provider" varchar(64) not null,
  "key" varchar(2048) not null,
  "access_token" varchar(2048) not null,
  "token_type" varchar(128),
  "expires_in" integer,
  "refresh_token" varchar(1024),
  "params" hstore,
  "created" timestamp without time zone not null,
  constraint "oauth2_info_pkey" primary key ("provider", "key")
);

create index if not exists "oauth2_info_key" on "oauth2_info" using btree ("key" asc);

create type setting_key as enum('AllowRegistration', 'DefaultNewUserRole');
/* alter type "setting_key" add value 'NewValue' after 'OldValue'; */

create table if not exists "setting_values" (
  "k" setting_key primary key,
  "v" character varying(4096) not null
);

create extension if not exists hstore;

create table if not exists "audit" (
  "id" uuid not null,
  "act" character varying(32) not null,
  "app" character varying(64) not null,
  "client" character varying(32) not null,
  "server" character varying(32) not null,
  "user_id" uuid not null,
  "tags" hstore not null,
  "msg" text not null,
  "started" timestamp without time zone not null,
  "completed" timestamp without time zone not null,
  primary key ("id")
) with (oids = false);

create index if not exists "audit_act" on "audit" using btree ("act" asc nulls last);
create index if not exists "audit_app" on "audit" using btree ("app" asc nulls last);
create index if not exists "audit_client" on "audit" using btree ("client" asc nulls last);
create index if not exists "audit_server" on "audit" using btree ("server" asc nulls last);
create index if not exists "audit_user_id" on "audit" using btree ("user_id" asc nulls last);
create index if not exists "audit_tags" on "audit" using gin ("tags");

create table if not exists "audit_record" (
  "id" uuid not null,
  "audit_id" uuid not null references "audit",
  "t" character varying(128) not null,
  "pk" character varying(128)[] not null,
  "changes" jsonb not null,
  primary key ("id")
) with (oids = false);

create index if not exists "audit_record_t" on "audit_record" using btree ("t" asc nulls last);
create index if not exists "audit_record_pk" on "audit_record" using btree ("pk" asc nulls last);
create index if not exists "audit_record_changes" on "audit_record" using gin ("changes");

create table if not exists "note" (
  "id" uuid primary key,
  "rel_type" varchar(128),
  "rel_pk" varchar(256),
  "text" text not null,
  "author" uuid not null,
  "created" timestamp without time zone not null,
  foreign key ("author") references "system_users" ("id")
);

create table if not exists "scheduled_task_run" (
  "id" uuid not null primary key,
  "task" varchar(64) not null,
  "arguments" varchar(64)[] not null,
  "status" varchar(64) not null,
  "output" json not null,
  "started" timestamp without time zone not null,
  "completed" timestamp without time zone not null
);

create index if not exists "scheduled_task_run_task" on "scheduled_task_run" using btree ("task" asc);
create index if not exists "scheduled_task_run_status" on "scheduled_task_run" using btree ("status" asc);
create index if not exists "scheduled_task_run_started" on "scheduled_task_run" using btree ("started" asc);

create table if not exists "sync_progress" (
  "key" varchar(128) not null primary key,
  "status" varchar(128) not null,
  "message" text not null,
  "last_time" timestamp not null
);

create index if not exists "sync_progress_status" on "sync_progress" using btree ("status" asc);

create table if not exists "flyway_schema_history" (
  "installed_rank" integer not null,
  "version" character varying(50),
  "description" character varying(200),
  "type" character varying(20),
  "script" character varying(1000),
  "checksum" integer,
  "installed_by" character varying(100),
  "installed_on" timestamp without time zone not null default now(),
  "execution_time" integer not null,
  "success" boolean not null,
  constraint "flyway_schema_history_pk" primary key ("installed_rank")
);

create index if not exists "flyway_schema_history_s_idx" on "flyway_schema_history" using btree(success);
