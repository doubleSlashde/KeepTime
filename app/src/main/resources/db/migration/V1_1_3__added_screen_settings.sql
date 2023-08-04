ALTER TABLE settings 
  ADD COLUMN windowxproportion DOUBLE NOT NULL DEFAULT (0.5);

ALTER TABLE settings 
  ADD COLUMN windowyproportion DOUBLE NOT NULL DEFAULT (0.5);

ALTER TABLE settings 
  ADD COLUMN window_screenhash INT NOT NULL DEFAULT (0);

ALTER TABLE settings 
  ADD COLUMN save_window_position BOOLEAN NOT NULL DEFAULT (false);