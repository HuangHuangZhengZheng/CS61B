package creatures;

import huglife.*;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class Clorus extends Creature {

    /** red color. */
    private int r;
    /** green color. */
    private int g;
    /** blue color. */
    private int b;


    public Clorus(double e) {
        super("clorus");
        r = 0;
        g = 0;
        b = 0;
        energy = e;
    }

    public Clorus() {
        this(1);
    }

    public Color color() {
        r = 34;
        b = 231;
        g = 0;
        return color(r, g, b);
    }

    public void attack(Creature c) {
        energy += c.energy();
    }


    public void move() {
        energy = energy - 0.03;
    }

    public void stay() {
        energy = energy - 0.01;
    }

    public Clorus replicate() {
        energy = energy * 0.5;
        return new Clorus(energy);
    }


    public Action chooseAction(Map<Direction, Occupant> neighbors) {
        java.util.List<Direction> empties = getNeighborsOfType(neighbors, "empty");
        List<Direction> plips = getNeighborsOfType(neighbors, "plip");
        if (empties.isEmpty()) {
            return new Action(Action.ActionType.STAY);
        } else if (!plips.isEmpty()) {
            if (plips.size() == 1) {
                Direction attackDir = plips.get(0);
                return new Action(Action.ActionType.ATTACK, attackDir);
            }
            Direction attackDir = HugLifeUtils.randomEntry(plips);
            return new Action(Action.ActionType.ATTACK, attackDir);
        } else if (energy>=1.0) {
            if (empties.size() == 1) {
                Direction replicateDir = empties.get(0);
                return new Action(Action.ActionType.REPLICATE, replicateDir);
            }
            Direction replicateDir = HugLifeUtils.randomEntry(empties);
            return new Action(Action.ActionType.REPLICATE, replicateDir);
        }else {
            if (empties.size() == 1) {
                Direction moveDir = empties.get(0);
                return new Action(Action.ActionType.MOVE, moveDir);
            }
            Direction moveDir = HugLifeUtils.randomEntry(empties);
            return new Action(Action.ActionType.MOVE, moveDir);
        }
    }


}
