package edu.uchicago.gerber.mvc.controller;

import edu.uchicago.gerber.mvc.model.Movable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 The GameOp (short for Game Operation) simply associates a movable with an action. Once
 the GameOpsQueue is processed it will add/remove movables from their appropriate list
 in the CommandCenter depending on the movable's team.
 */

//the lombok @Data gives us automatic getters and setters
@Data
//the lombok @AllArgsConstructor gives us an All-Args-Constructor :)
@AllArgsConstructor
public class GameOp {
    //this could also be a boolean, but we want to be explicit about what we're doing
    public enum Action {
        ADD, REMOVE
    }
    //members
    private Movable movable;
    private Action action;

}
