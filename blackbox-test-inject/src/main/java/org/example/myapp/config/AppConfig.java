package org.example.myapp.config;

import io.avaje.inject.*;
import javax.inject.Inject;
import javax.inject.Named;
import org.example.myapp.HelloData;

import java.util.Optional;

@Factory
public class AppConfig {

  @Bean
  HelloData data() {
    return new AppHelloData();
  }

  private static class AppHelloData implements HelloData {

    @Override
    public String helloData() {
      return "AppHelloData";
    }
  }

  @Prototype
  @Bean
  public Builder newBuilder() {
    return new Builder();
  }

  @Bean
  Generated newGenerated() {
    return new Generated();
  }

  @Secondary
  @Bean
  public MySecType generalSecondary() {
    return new MySecType();
  }

  @Secondary
  @Bean
  public Optional<MySecOptType> optionalSecondary() {
    return Optional.of(new MySecOptType());
  }

  @Primary
  @Bean
  public MyPrim primaryBean() {
    return new MyPrim("prime");
  }

  @Named("notPrimary")
  @Bean
  public MyPrim notPrimaryBean() {
    return new MyPrim("notPrimary");
  }

  @Bean
  public Provider provider() {
    return new Provider();
  }

  @Bean
  public MyGen<String> myGen() {
    return new MyGen<>();
  }

  @Bean
  MyInterface myInterface2() {
    return new MyInterface() {};
  }

  public static class Builder {
  }

  public static class Generated {
  }

  public static class MySecType {
  }

  public static class MySecOptType {
  }

  public static class Provider {

  }

  public static class MyGen<T> {

  }

  public interface MyInterface {

  }

  public static class MyPrim {
    public final String val;
    public MyPrim(String val) {
      this.val = val;
    }
  }

  @Component
  public static class BuilderUser {

    final javax.inject.Provider<Builder> builderProvider;

    @Inject
    public BuilderUser(javax.inject.Provider<Builder> builderProvider) {
      this.builderProvider = builderProvider;
    }

    public Builder createBuilder() {
      return builderProvider.get();
    }
  }
}
