package io.avaje.inject.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScopeUtilTest {

  @Test
  void name() {
    assertEquals("Example", ScopeUtil.name("org.example"));
    assertEquals("Example", ScopeUtil.name("org.Example"));
    assertEquals("Example", ScopeUtil.name("example"));
    assertEquals("Example", ScopeUtil.name("Example"));
  }

  @Test
  void initName_inject() {
    // resulting module can't be InjectModule as that clashes with @InjectModule
    assertEquals("DInject", ScopeUtil.initName("org.example.inject"));
    assertEquals("Foo", ScopeUtil.initName("org.example.foo"));
  }

  @Test
  void name_withSpace() {
    assertEquals("ExAmple", ScopeUtil.name("org.ex ample"));
    assertEquals("ExAmple", ScopeUtil.name("org.ex_ample"));
    assertEquals("ExAmple", ScopeUtil.name("ex-ample"));
    assertEquals("Example", ScopeUtil.name("ex$ample"));
  }

  @Test
  void name_withDigits() {
    assertEquals("Example1", ScopeUtil.name("org.example1"));
    assertEquals("Ex42ample", ScopeUtil.name("org.ex42ample"));
  }

  @Test
  void name_withSuffix() {
    assertEquals("MyCustom", ScopeUtil.name("MyCustomScope"));
    assertEquals("MyCustom", ScopeUtil.name("MyCustomModule"));
  }
}
