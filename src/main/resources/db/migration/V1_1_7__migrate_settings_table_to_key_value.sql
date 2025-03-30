CREATE TABLE new_settings(
     setting        VARCHAR NOT NULL,
     setting_value  VARCHAR NOT NULL,
     PRIMARY KEY (setting)
);

-- Migrate color settings
INSERT INTO new_settings (setting, setting_value)
SELECT 'hover_background_color', CAST(hover_background_color AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'hover_font_color', CAST(hover_font_color AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'default_background_color', CAST(default_background_color AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'default_font_color', CAST(default_font_color AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'task_bar_color', CAST(task_bar_color AS VARCHAR(255)) FROM settings;

-- Migrate boolean settings
INSERT INTO new_settings (setting, setting_value)
SELECT 'use_hotkey', CAST(use_hotkey AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'display_projects_right', CAST(display_projects_right AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'hide_projects_on_mouse_exit', CAST(hide_projects_on_mouse_exit AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'save_window_position', CAST(save_window_position AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'remind_if_notes_are_empty', CAST(remind_if_notes_are_empty AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'remind_if_notes_are_empty_only_for_work_entry', CAST(remind_if_notes_are_empty_only_for_work_entry AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'confirm_close', CAST(confirm_close AS VARCHAR(255)) FROM settings;

-- Migrate numerical settings
INSERT INTO new_settings (setting, setting_value)
SELECT 'windowxproportion', CAST(windowxproportion AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'windowyproportion', CAST(windowyproportion AS VARCHAR(255)) FROM settings
UNION ALL
SELECT 'window_screenhash', CAST(window_screenhash AS VARCHAR(255)) FROM settings;

DROP TABLE settings;
ALTER TABLE new_settings RENAME TO settings;