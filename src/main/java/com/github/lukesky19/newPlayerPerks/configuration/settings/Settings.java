package com.github.lukesky19.newPlayerPerks.configuration.settings;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record Settings(String locale, boolean invulnerable, boolean fly, boolean keepInventory, boolean keepExp, boolean voidTeleport, String period) {}
