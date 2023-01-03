package fr.ubx.poo.ubomb.go;

import fr.ubx.poo.ubomb.go.decor.bonus.*;

// Double dispatch visitor pattern
public interface TakeVisitor {
    // Key
    default void take(Key key) {}

    default void take(BombRangeInc key) {}
    default void take(BombRangeDec key) {}

    default void take(BombNumberInc key) {}
    default void take(BombNumberDec key) {}

    default void take(Heart key) {}
}
