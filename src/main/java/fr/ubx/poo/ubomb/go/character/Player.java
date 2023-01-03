/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ubomb.go.character;

import fr.ubx.poo.ubomb.game.Direction;
import fr.ubx.poo.ubomb.game.Game;
import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.go.GameObject;
import fr.ubx.poo.ubomb.go.Movable;
import fr.ubx.poo.ubomb.go.TakeVisitor;
import fr.ubx.poo.ubomb.go.decor.*;
import fr.ubx.poo.ubomb.go.decor.bonus.*;
import javafx.scene.paint.Color;

public class Player extends GameObject implements Movable, TakeVisitor {

    private Direction direction;
    private boolean moveRequested = false;
    private boolean keyUseRequested = false;
    public boolean bombRequested = false;

    private boolean won = false;

    private final int maxLives;
    private int lives;

    private int bombs;

    private int range;

    private int keys;

    public Player(Game game, Position position) {
        super(game, position);
        this.direction = Direction.DOWN;

        this.maxLives = game.configuration().playerLives();
        this.lives = maxLives;

        this.bombs = game.configuration().bombBagCapacity();

        this.range = 1;

        this.keys = 0;
    }

    public int getMaxLives() { return maxLives;}
    public int getLives() { return lives;}
    public void setLives(int value) { lives = value;}

    public int getBombs() { return bombs;}
    public void setBombs(int value) { bombs = value;}

    public int getBombRange() { return range;}

    public int getKeys() { return keys;}

    public Direction getDirection() {
        return direction;
    }

    public void doMove(Direction direction) {
        // This method is called only if the move is possible, do not check again
        Position nextPos = direction.nextPosition(getPosition());
        GameObject nextGo = game.grid().get(nextPos);

        if (nextGo instanceof Bonus bonus)
        {
            bonus.takenBy(this);
        }

        if (nextGo instanceof Monster)
        {
            if (lives > 0) { --lives;}
        }

        if (nextGo instanceof Box box)
        {
            box.remove();

            Position newBoxPosition = direction.nextPosition(nextPos);

            Box newBox = new Box(newBoxPosition);
            game.grid().set(newBoxPosition, newBox);
            newBox.setModified(true);

            game.addNewRenderTarget(newBox);

        }

        if (nextGo instanceof DoorNextOpened)
        {
            game.oldLevel = game.activeLevel;
            game.activeLevel++;

            game.levelChangeRequested = true;
        }

        if (nextGo instanceof DoorPrevOpened)
        {
            game.oldLevel = game.activeLevel;
            game.activeLevel--;

            game.levelChangeRequested = true;
        }

        if (nextGo instanceof Princess)
        {
            won = true;
        }

        setPosition(nextPos);
    }

    public void useKey(Direction direction)
    {
        Position nextPos = direction.nextPosition(getPosition());
        GameObject nextGo = game.grid().get(nextPos);

        //nextGo.remove();

        DoorNextOpened newDoor = new DoorNextOpened(nextPos);
        game.grid().set(nextPos, newDoor);

        game.addNewRenderTarget(newDoor);
    }

    public void requestMove(Direction direction)
    {
        if (direction != this.direction) {
            this.direction = direction;
            setModified(true);
        }
        moveRequested = true;
    }

    public void requestKeyUse(Direction direction)
    {
        if (direction != this.direction) {
            this.direction = direction;
            setModified(true);
        }

        keyUseRequested = true;
    }

    public void requestBomb(Direction direction)
    {
        if (direction != this.direction) {
            this.direction = direction;
            setModified(true);
        }
        bombRequested = true;
    }

    public final boolean canMove(Direction direction)
    {
        //Check if player goes out of bounds
        //Check if player can move on next position (not tree and not stone)
        Position nextPos = direction.nextPosition(game.player().getPosition());
        Decor nextPosDecor = game.grid().get(nextPos);

        if (nextPosDecor == null)
        {
            return game.grid().inside(nextPos);
        }

        if (nextPosDecor instanceof Box)
        {
            Position nextNextPos = direction.nextPosition(nextPos);
            Decor nextNextPosDecor = game.grid().get(nextNextPos);

            return (nextNextPosDecor == null) && game.grid().inside(nextNextPos);
        }

        return nextPosDecor.walkableBy(game.player()) && game.grid().inside(nextPos);
    }

    public final boolean canUseKey(Direction direction)
    {
        Position nextPos = direction.nextPosition(game.player().getPosition());
        Decor nextPosDecor = game.grid().get(nextPos);

        if (nextPosDecor instanceof DoorNextClosed)
        {
            return keys > 0;
        }

        return false;
    }

    public final boolean canBomb()
    {
        return bombs > 0;
    }

    public void update(long now)
    {
        if (moveRequested)
        {
            if (canMove(direction))
            {
                doMove(direction);
            }
        }
        moveRequested = false;

        if (keyUseRequested)
        {
            if (canUseKey(direction))
            {
                useKey(direction);
            }
        }
    }

    @Override
    public void explode() {
        // TODO
    }

    @Override
    public void take(Key key) { ++keys; key.remove(); }

    @Override
    public void take(BombRangeInc bombRange) { range++; bombRange.remove();}
    @Override
    public void take(BombRangeDec bombRange) { if (range > 1) {range--; bombRange.remove();} }

    @Override
    public void take(BombNumberInc bombNumber) { bombs++; bombNumber.remove();}
    @Override
    public void take(BombNumberDec bombNumber) { if (bombs > 1) { bombs--; bombNumber.remove();}; }

    @Override
    public void take(Heart heart) { if (lives < maxLives) { lives++; heart.remove();} }

    public boolean hasWon()
    {
        return won;
    }
}
