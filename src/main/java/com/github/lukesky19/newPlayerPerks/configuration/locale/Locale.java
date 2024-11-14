package com.github.lukesky19.newPlayerPerks.configuration.locale;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record Locale(
        String prefix,
        String reload,
        String noPermission,
        String unknownArgument,
        String invalidPlayer,
        String addedPerks,
        String removedPerks,
        List<String> newPlayerMessages,
        List<String> perksExpireMessages) {}
