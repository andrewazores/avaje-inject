package org.example.myapp.generic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class GenericFactoryTest {

  @Inject Generic<Integer> intymcintface;

  @Test
  void test() {
    assertThat(intymcintface).isNotNull();
  }
}
