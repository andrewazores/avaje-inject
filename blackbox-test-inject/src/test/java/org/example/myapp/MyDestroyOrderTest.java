package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyDestroyOrderTest {

  @Test
  void ordering() {
    MyDestroyOrder.ordering().clear();
    try (BeanScope beanScope = BeanScope.builder().build()) {
      beanScope.get(HelloService.class);
    }
    List<String> ordering = MyDestroyOrder.ordering();
    assertThat(ordering).containsExactly("onShutdown-AutoCloseable", "HelloService", "AppConfig", "MyNamed", "MyMetaDataRepo", "ExampleService", "AppHelloData");
  }
}
