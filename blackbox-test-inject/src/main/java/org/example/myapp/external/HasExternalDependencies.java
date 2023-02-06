package org.example.myapp.external;

import io.avaje.inject.Component;
import org.example.external.aspect.MyExternalAspect;
import org.other.one.OtherComponent;

@Component
public class HasExternalDependencies {

  // component from other module
  public final OtherComponent fromExternal;

  public HasExternalDependencies(OtherComponent otherComponent) {
    this.fromExternal = otherComponent;
  }

  @MyExternalAspect
  public String doStuff() {
    return "hello";
  }
}
