package com.github.lukesky19.newPlayerPerks.configuration.player;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record PlayerSettings(long joinTime) {}
