package org.example.coffee.provider;

import io.avaje.inject.Component;
import javax.inject.Provider;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BProvProvider implements Provider<BProv<String>> {

  AtomicInteger counter = new AtomicInteger();

  @Override
  public BProv<String> get() {
    return new BProv<>("Hello BProv" + counter.incrementAndGet());
  }
}
