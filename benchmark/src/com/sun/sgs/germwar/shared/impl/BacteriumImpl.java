/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved
 */

package com.sun.sgs.germwar.shared.impl;

import java.io.Serializable;

import com.sun.sgs.germwar.shared.Bacterium;
import com.sun.sgs.germwar.shared.Coordinate;
import com.sun.sgs.germwar.shared.GermWarConstants;
import com.sun.sgs.germwar.shared.InvalidMoveException;

/**
 * Represents a single bacterium.
 */
public class BacteriumImpl implements Bacterium, Serializable {
    /** Default health level that bacteria start off with. */
    public static final float INITIAL_HEALTH = 100f;

    /** Currently, all bacteria always have 1 movement point per turn. */
    public static final int MAX_MOVEMENT_POINTS = 1;

    /** The version of the serialized form of this class. */
    private static final long serialVersionUID = 1L;

    /**
     * This Bacterium's ID - this is unique for a given player, but not
     * globally.  Thus, a globally unique identifier is the tuple
     * <player-id, id>.
     */
    private int id = -1;

    /** The ID of the player that owns this Bacterium. */
    private long playerId = GermWarConstants.SERVER_PLAYER_ID;

    /** Bacterium's current health level. */
    private float health = 0f;

    /** The last turn on which updateFood() was called. */
    private long lastUpdated = 1;

    /** Coordinates of this bacterium's position. */
    private Coordinate position = null;

    /**
     * Max movement points for this bacterium (movement points are reset to
     * this amount at the beginning of each turn).
     */
    private int maxMovementPoints = MAX_MOVEMENT_POINTS;

    /** Remaining movements points for this turn. */
    private int currentMovementPoints = maxMovementPoints;

    /**
     * Creates a new {@code BacteriumImpl} with default initial health and
     * movement points at turn 1.
     */
    public BacteriumImpl(int id, long playerId, Coordinate pos) {
        this(id, playerId, pos, 1, INITIAL_HEALTH, MAX_MOVEMENT_POINTS);
    }

    /**
     * Creates a new {@code BacteriumImpl} with default initial health and
     * movement points.
     */
    public BacteriumImpl(int id, long playerId, Coordinate pos, long turnNo) {
        this(id, playerId, pos, turnNo, INITIAL_HEALTH, MAX_MOVEMENT_POINTS);
    }

    /**
     * Creates a new {@code BacteriumImpl} with default initial movement points.
     */
    public BacteriumImpl(int id, long playerId, Coordinate pos, long turnNo,
        float health)
    {
        this(id, playerId, pos, turnNo, health, MAX_MOVEMENT_POINTS);
    }

    /**
     * Creates a new {@code BacteriumImpl}.
     */
    public BacteriumImpl(int id, long playerId, Coordinate pos, long turnNo,
        float health, int movementPoints)
    {
        this.id = id;
        this.playerId = playerId;
        this.position = pos;
        this.lastUpdated = turnNo;
        this.health = health;
        this.currentMovementPoints = movementPoints;
    }

    /**
     * Creates and returns a new (spawned) bacterium from a split action.
     */
    protected Bacterium createSpawn(long playerId, Coordinate pos,
        float initialHealth)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bacterium) {
            Bacterium b = (Bacterium)obj;
            return ((b.getId() == id) && (b.getPlayerId() == playerId));
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *<p>
     * XOR of player-id and bacterium-id.
     */
    @Override
    public int hashCode() {
        // same as Long.hashCode():
        int playerIdHash = (int)(playerId^(playerId>>>32));
        return playerIdHash ^ id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BacteriaImpl [id=").append(id);
        sb.append(", playerId=").append(playerId);
        sb.append(", health=").append(health);
        sb.append(", pos=").append(position).append("]");
        return sb.toString();
    }

    // implement Bacterium

    /**
     * {@inheritDoc}
     */
    public void addHealth(float mod) {
        health += mod;
    }

    /**
     * {@inheritDoc}
     */
    public void doMove(Coordinate newPos) throws InvalidMoveException {
        int dist = position.diff(newPos).manhattanLength();

        if (currentMovementPoints < dist)
            throw new InvalidMoveException(this, position, newPos, "Distance (" +
                dist + ") is greater than current movement points (" +
                currentMovementPoints + ").");

        if (health < Bacterium.MOVE_HEALTH_COST)
            throw new InvalidMoveException(this, position, newPos,
                "Movement cost (" + Bacterium.MOVE_HEALTH_COST +
                ") is greater than current health (" + health + ").");

        /** else, movement is ok. */
        health -= Bacterium.MOVE_HEALTH_COST;
        currentMovementPoints -= dist;
        position = newPos;
    }

    /**
     * {@inheritDoc}
     */
    public Coordinate getCoordinate() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    public int getCurrentMovementPoints() {
        return currentMovementPoints;
    }

    /**
     * {@inheritDoc}
     */
    public float getHealth() {
        return health;
    }

    /**
     * {@inheritDoc}
     */
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxMovementPoints() {
        return maxMovementPoints;
    }

    /**
     * {@inheritDoc}
     */
    public long getPlayerId() {
        return playerId;
    }

    /**
     * {@inheritDoc}
     */
    public Bacterium splitUpdate(Coordinate spawnPos) {
        /** First deduct the cost of splitting, then divide health in 2. */
        health -= Bacterium.SPLIT_HEALTH_COST;
        float spawnHealth = health / 2;
        health -= spawnHealth;

        return createSpawn(playerId, spawnPos, spawnHealth);
    }

    /**
     * {@inheritDoc}
     */
    public void turnUpdate(long turnNo) {
        if (turnNo == lastUpdated) return;

        health -= (turnNo - lastUpdated)*Bacterium.TURN_HEALTH_COST;
        lastUpdated = turnNo;

        /** Replenish movement points. */
        currentMovementPoints = maxMovementPoints;
    }
}
