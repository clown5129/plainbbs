# --- !Ups

create table bbs_threads (
	  id             serial       primary key,
	  title          varchar(255) not null,
	  post_count     integer      not null,
	  created_at     timestamp(3) not null,
	  last_posted_at timestamp(3) not null,
	  deleted_at     timestamp(3)
	);

create table bbs_posts (
  id        serial       primary key,
  subject   varchar(255),
  posted_at timestamp(3) not null,
  content   text         not null,
  visible   boolean      not null,
  thread_id integer      not null,
  constraint thread_fk foreign key (thread_id) references bbs_threads (id)
);

# --- !Downs

drop table bbs_posts;

drop table bbs_threads;
