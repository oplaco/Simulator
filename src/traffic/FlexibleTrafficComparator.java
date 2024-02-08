/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import static traffic.FlexibleTrafficComparator.Order.hexid;
import static traffic.FlexibleTrafficComparator.Order.time;
/**
 *
 * @author jvila
 */
public class FlexibleTrafficComparator implements java.util.Comparator<Traffic> {

  public enum Order {hexid, time}
  private Order sortingBy = hexid;

  @Override
  public int compare(Traffic o1, Traffic o2) {
    switch(sortingBy) {
      case hexid: return o1.getICAO24().compareTo(o2.getICAO24());
      case time: return (int)(o1.getTimestamp() - o2.getTimestamp());
    }
    throw new RuntimeException("Practically unreachable code, can't be thrown");
  }

  public void setSortingBy(Order sortBy) {
    this.sortingBy = sortBy;
    String a;
    System.out.println("Sorting by: "+sortingBy);
  }
    
}
