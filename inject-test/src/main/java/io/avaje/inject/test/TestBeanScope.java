package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

/**
 * Provides access to the global "test scope" and helper methods to use it.
 */
@NonNullApi
public abstract class TestBeanScope {

  /**
   * Build and return a new BeanScope that will have the global "test scope" as its parent.
   * <p>
   * That is, beans created using {@code @TestScope} are all in the global "test scope"
   * which is a parent to this scope.
   *
   * @return A new test BeanScope with the global "test scope" as its parent.
   */
  public static BeanScopeBuilder builder() {
    BeanScope globalTestScope = init(true);
    return BeanScope.builder().parent(globalTestScope, false);
  }

  /**
   * Return the BeanScope for {@code @TestScope} beans ONLY building once.
   * <p>
   * If the BeanScope has already been created then that shared scope is returned.
   *
   * @return The test scope BeanScope (nullable).
   */
  @Nullable
  public static BeanScope initialise() {
    return init(true);
  }

  /**
   * Build and return a new BeanScope for {@code @TestScope} beans. This builds the beans
   * in the test scope every time it is called.
   * <p>
   * Generally, we DO NOT want to use this method but use {@link #initialise()} instead.
   *
   * @param shutdownHook Set whether a shutdown hook should be created to close the BeanScope.
   * @return The test scope BeanScope (nullable).
   */
  @Nullable
  public static BeanScope create(boolean shutdownHook) {
    return TSBuild.create(shutdownHook);
  }

  @Nullable
  static BeanScope init(boolean shutdownHook) {
    return TSBuild.initialise(shutdownHook);
  }

}
