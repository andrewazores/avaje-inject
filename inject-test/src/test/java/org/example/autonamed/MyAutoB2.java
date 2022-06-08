package org.example.autonamed;

import javax.inject.Singleton;

@Singleton
public class MyAutoB2 {

  final AutoB2 one;
  final AutoB2 two;

  public MyAutoB2(AutoB2 one, AutoB2 two) {
    this.one = one;
    this.two = two;
  }

  public String one() {
    return one.who();
  }

  public String two() {
    return two.who();
  }
}
