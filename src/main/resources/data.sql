-- Seed data for the dev (H2) profile. Runs automatically on startup
-- because of spring.jpa.defer-datasource-initialization=true below.
--
-- Game titles are placeholders: the proposal describes the studio's games
-- generically as "Coming Soon" with no names/lore committed yet.
 
INSERT INTO genres (name) VALUES ('Action'), ('Adventure'), ('Puzzle'), ('Platformer');
INSERT INTO platforms (name) VALUES ('PC'), ('PlayStation'), ('Xbox'), ('Switch');
 
INSERT INTO games (title, slug, tagline, description, release_status, esrb_rating)
VALUES
  ('Untitled Project One', 'untitled-project-one', 'Coming soon.',
   'Details to be revealed as development progresses.', 'ANNOUNCED', 'E10+'),
  ('Untitled Project Two', 'untitled-project-two', 'Coming soon.',
   'Details to be revealed as development progresses.', 'ANNOUNCED', 'E');
  ('Untitled Project Three', 'untitled-project-three', 'Coming soon.',
    'Details to be revealed as development progresses.', 'ANNOUNCED', 'E');
  ('Untitled Project Four', 'untitled-project-four', 'Coming soon.',
     'Details to be revealed as development progresses.', 'ANNOUNCED', 'E');
 
INSERT INTO team_members (name, role_title, bio, sort_order) VALUES
  ('Alex Rivera', 'Founder & Creative Director', 'Started Loose Cannon Games to make original, all-ages worlds.', 1),
  ('Sam Park', 'Lead Engineer', 'Builds the systems that hold everything together.', 2);
 
INSERT INTO job_postings (title, department, location, description, requirements, is_open)
VALUES ('Gameplay Engineer', 'Engineering', 'Remote', 'Build and ship gameplay systems.', '2+ years of game development experience.', true);
 