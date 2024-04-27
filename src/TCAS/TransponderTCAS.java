/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TCAS;

import TFM.utils.Utils;
import TFM.utils.Vector2D;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class TransponderTCAS {
   
  /**
   * Calculates tau, this time is an estimate (BUT NOT THE SAME) of the time of closest point of approach.
   */
   private double calculate_tau(Vector2D S, Vector2D V){
       if(!V.equals(new Vector2D(0, 0))){
            return -(S.dotProduct(V))/(S.dotProduct(S));           
       }else{
           return 0.;
       }
   }
    /**
     * predicts the vertical separation between the aircraft at time tau assuming a target vertical speed v for the ownship.
     * @param S
     * @param V
     * @param s_oz
     * @param v_oz
     * @param s_iz
     * @param v_iz
     * @param v Target vertical speed v
     * @param a The ownship is assumed to y at constant ground speed and constant vertical acceleration a.
     * @param epsilon  The parameter epsilon specifes a possible direction for the vertical ownship maneuver, 
     *  which is upward when epsilon = 1 and downward when epsilon = -1.
     */
   private double sep_at(Vector2D S,Vector2D V,double s_oz, double v_oz, double s_iz,double v_iz,double v, double a,double epsilon){
       double alt_o = own_alt_at(s_oz, v_oz, Math.abs(v), a, epsilon*Utils.sign(v), calculate_tau(S, V));
       double alt_i = s_iz + calculate_tau(S, V)*v_iz;
       return epsilon*(alt_o-alt_i);
   }
    
   /**
    * Computes the vertical altitude of the ownship at any time t given a target vertical speed v and acceleration a.
    */
   private double own_alt_at(double s_oz, double v_oz, double v, double a, double epsilon, double t){
       double s = stop_accel(v_oz, v, a, epsilon, t);
       double q = Math.min(t, s);
       double l = Math.max(0,t-s);
       return epsilon*q*q*a*0.5 + q*v_oz + s_oz + epsilon*l*s;   
   }
   
   /**
    * Computes the time at which the ownship reaches the target vertical speed v.
   */
   private double stop_accel(double v_oz, double v, double a, double epsilon, double t){
       if(t <= 0 || v_oz >=v){
           return 0;
       } else{
           return (epsilon*v - v_oz)/(epsilon*a);
       }
   }
   
   /**
    * Computes wheter an aircraft must go up or down
    * @param S_o
    * @param s_oz
    * @param V_o
    * @param v_oz
    * @param S_i
    * @param s_iz
    * @param V_i
    * @param v_iz
    * @param v
    * @param a
    * @return 
    */
    private int RA_sense_new(Vector2D S_o, double s_oz, Vector2D V_o, double v_oz, Vector2D S_i, double s_iz, Vector2D V_i, double v_iz, double v, double a){
        double ALIM_l = 100000.0;
        double tau_o = calculate_tau(S_o, V_o);
        double tau_i = calculate_tau(S_i, V_i);
        
        double o_up = own_alt_at(s_oz, v_oz, v, a, 1,  tau_o);
        double o_down = own_alt_at(s_oz, v_oz, v, a, -1,  tau_o);     
        double i_up = own_alt_at(s_iz, v_iz, v, a, 1,  tau_i);
        double i_down = own_alt_at(s_iz, v_iz, v, a, -1,  tau_i);
        
        double alt_o = s_oz + tau_o*v_oz;
        double alt_i = s_iz + tau_i*v_iz;
        
        double dist_o_up = o_up - alt_i;
        double dist_o_down = alt_i - o_down;
        double dist_i_up = i_up - alt_o;
        double dist_i_down = alt_o - i_down;
        
        if( Utils.sign(s_oz-s_iz)==1 && Math.min(dist_o_up, dist_i_down)>= ALIM_l){
            return 1;
        }else if(Utils.sign(s_oz-s_iz)==-1 && Math.min(dist_o_down, dist_i_up)>= ALIM_l){
            return -1;
        }else if(Math.min(dist_o_up,dist_i_down)>=Math.min(dist_o_down,dist_o_up)){
            return 1;
        }else{
            return -1;
        }
   }
   
}
