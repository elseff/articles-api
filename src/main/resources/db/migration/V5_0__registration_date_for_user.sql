ALTER TABLE _user
    ADD COLUMN registration_date TIMESTAMP NOT NULL DEFAULT now();