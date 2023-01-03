package org.example.generic.repo;

import javax.inject.Singleton;

@Singleton
public class MapRepo2 extends AbstractRepo<Model2> {

  @Override
  public Model2 get() {
    return new Model2();
  }
}
