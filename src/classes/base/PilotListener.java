/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

/**
 *
 * @author fms
 */
public interface PilotListener {
     public void targetReached(Coordinate co);
     public void starting(Airport ap);
}
