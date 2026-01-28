/*
    NewPlayerPerks applies specific perks to new players.
    Copyright (C) 2024 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.newPlayerPerks.data;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Contains the player's join time.
 */
@ConfigSerializable
public class PlayerData {
    private final @NotNull UUID playerId;
    private long perkTime = 0;
    private long joinTime = 0;
    private boolean perksPaused = false;

    /**
     * Constructor
     * @deprecated Use {@link PlayerData#PlayerData(UUID)} or {@link PlayerData#PlayerData(UUID, long, long, boolean)} instead.
     * @throws RuntimeException if used.
     */
    @Deprecated(since = "1.3.0.0")
    public PlayerData() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param playerId The {@link UUID} the player data belongs to.
     */
    public PlayerData(@NotNull UUID playerId) {
        this.playerId = playerId;
    }

    /**
     * Constructor
     * @param playerId The {@link UUID} the player data belongs to.
     * @param perkTime The player's perk time.
     * @param joinTime The player's join time.
     * @param perksPaused Are perks paused?
     */
    public PlayerData(@NotNull UUID playerId, long perkTime, long joinTime, boolean perksPaused) {
        this.playerId = playerId;
        setPerkTime(perkTime);
        setJoinTime(joinTime);
        setPerksPaused(perksPaused);
    }

    /**
     * Get the {@link UUID} this data belongs to.
     * @return The {@link UUID}.
     */
    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    /**
     * Set the player's perk time.
     * @param perkTime The perk time to set in seconds.
     */
    public void setPerkTime(long perkTime) {
        this.perkTime = Math.max(0, perkTime);
    }

    /**
     * Remove the time from the player's perk time.
     * @param timeToRemove The time in seconds to remove.
     */
    public void removePerkTime(long timeToRemove) {
        if(this.perkTime <= 0) return;
        if(timeToRemove <= 0) return;

        this.perkTime = Math.max(0, perkTime - timeToRemove);
    }

    /**
     * Get the player's perk time in seconds.
     * @return The player's remaining time perks should apply for.
     */
    public long getPerkTime() {
        return perkTime;
    }

    /**
     * Set the player's join time.
     * @param joinTime The join time to set.
     */
    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    /**
     * Get the player's join time.
     * @return The player's join time.
     */
    public long getJoinTime() {
        return joinTime;
    }

    /**
     * Set whether perk time is paused or not.
     * @param perksPaused Should perk time paused or not?
     */
    public void setPerksPaused(boolean perksPaused) {
        this.perksPaused = perksPaused;
    }

    /**
     * Is perk time paused?
     * @return true if paused, false if not.
     */
    public boolean isPerksPaused() {
        return perksPaused;
    }
}
