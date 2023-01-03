package fr.ubx.poo.ubomb.go.decor.bonus;

import fr.ubx.poo.ubomb.game.Position;
import fr.ubx.poo.ubomb.view.Sprite;

public class Bomb extends Bonus
{
    private final long birth;
    private long now;
    private final long death;

    public Sprite sprite;

    public Bomb(Position position, long now)
    {
        super(position);
        birth = now;
        this.now = birth;
        death = now + 4_000_000_000L; //now + 4 seconds
    }

    public long birth()
    {
        return birth;
    }

    public long death()
    {
        return death;
    }
}
