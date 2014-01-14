# --- !Ups

alter table bbs_threads
  alter column post_count set default 1;

alter table bbs_posts
  alter column visible set default true;

# --- !Downs

alter table bbs_threads
  alter column last_posted_at drop default;

alter table bbs_posts
  alter column visible drop default;
