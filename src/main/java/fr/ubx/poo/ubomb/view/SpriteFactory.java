/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ubomb.view;

import fr.ubx.poo.ubomb.go.GameObject;
import fr.ubx.poo.ubomb.go.decor.bonus.*;
import fr.ubx.poo.ubomb.go.decor.*;
import javafx.scene.layout.Pane;

import static fr.ubx.poo.ubomb.view.ImageResource.*;


public final class SpriteFactory {

    public static Sprite create(Pane layer, GameObject gameObject) {
        if (gameObject instanceof Stone)
            return new Sprite(layer, STONE.getImage(), gameObject);

        if (gameObject instanceof Tree)
            return new Sprite(layer, TREE.getImage(), gameObject);


        if (gameObject instanceof Princess)
            return new Sprite(layer, PRINCESS.getImage(), gameObject);

        if (gameObject instanceof Monster)
            //TODO:Store direction in Monster class and use it here to render correct sprite (up, down, left, right)
            return new Sprite(layer, MONSTER_UP.getImage(), gameObject);

        if (gameObject instanceof Box)
            return new Sprite(layer, BOX.getImage(), gameObject);

        //Bonuses
        if (gameObject instanceof Key)
            return new Sprite(layer, KEY.getImage(), gameObject);

        if (gameObject instanceof BombRangeInc)
            return new Sprite(layer, BONUS_BOMB_RANGE_INC.getImage(), gameObject);
        if (gameObject instanceof BombRangeDec)
            return new Sprite(layer, BONUS_BOMB_RANGE_DEC.getImage(), gameObject);

        if (gameObject instanceof BombNumberInc)
            return new Sprite(layer, BONUS_BOMB_NB_INC.getImage(), gameObject);
        if (gameObject instanceof BombNumberDec)
            return new Sprite(layer, BONUS_BOMB_NB_DEC.getImage(), gameObject);

        if (gameObject instanceof Heart)
            return new Sprite(layer, HEART.getImage(), gameObject);

        //Doors
        if (gameObject instanceof DoorNextClosed)
            return new Sprite(layer, DOOR_CLOSED_PLUS.getImage(), gameObject);
        if (gameObject instanceof DoorNextOpened)
            return new Sprite(layer, DOOR_OPENED_PLUS.getImage(), gameObject);
        if (gameObject instanceof DoorPrevOpened)
            return new Sprite(layer, DOOR_OPENED_MINUS.getImage(), gameObject);

        //Bomb
        if (gameObject instanceof Bomb)
        {
            return new Sprite(layer, BOMB_3.getImage(), gameObject);
        }

        throw new RuntimeException("Unsupported sprite for decor " + gameObject);
    }
}
