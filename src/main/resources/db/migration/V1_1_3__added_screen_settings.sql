ALTER TABLE settings 
  ADD COLUMN window_positionx DOUBLE NOT NULL DEFAULT (0.5);

ALTER TABLE settings 
  ADD COLUMN window_positiony DOUBLE NOT NULL DEFAULT (0.5);

ALTER TABLE settings 
  ADD COLUMN screen_hash INT NOT NULL DEFAULT (0);

ALTER TABLE settings 
  ADD COLUMN save_window_position BOOLEAN NOT NULL DEFAULT (false);