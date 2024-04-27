/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TCAS;

import TFM.utils.Vector2D;

/**
 *
 * @author Gabriel
 */
public interface ResolutionAdvisorySolver {
    public int solve(Vector2D S_o, double s_oz, Vector2D V_o, double v_oz, Vector2D S_i, double s_iz, Vector2D V_i, double v_iz, double v, double a);  
}
