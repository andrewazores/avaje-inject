package org.example.myapp.external;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;
import org.other.one.OtherComponent;

import static org.assertj.core.api.Assertions.assertThat;

class HasExternalDependenciesTest {

  @Test
  void doStuff() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      final var other = beanScope.get(OtherComponent.class);
      assertThat(other).isNotNull();

      var hasExternalDependencies = beanScope.get(HasExternalDependencies.class);
      assertThat(hasExternalDependencies.doStuff()).isEqualTo("hello");
      assertThat(hasExternalDependencies.fromExternal).isSameAs(other);
    }

  }
}
