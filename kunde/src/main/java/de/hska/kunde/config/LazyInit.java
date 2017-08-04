package de.hska.kunde.config;

import org.springframework.context.annotation.Profile;

import static de.hska.kunde.config.Settings.DEV_PROFILE;

@Profile(DEV_PROFILE)
interface LazyInit {
}
