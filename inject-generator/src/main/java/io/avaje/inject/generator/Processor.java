package io.avaje.inject.generator;

import static io.avaje.inject.generator.ProcessingContext.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({
  Constants.INJECTMODULE,
  Constants.FACTORY,
  Constants.SINGLETON,
  Constants.COMPONENT,
  Constants.PROTOTYPE,
  Constants.SCOPE,
  Constants.TESTSCOPE,
  Constants.CONTROLLER,
  ImportPrism.PRISM_TYPE
})
public final class Processor extends AbstractProcessor {

  private Elements elementUtils;
  private ScopeInfo defaultScope;
  private AllScopes allScopes;
  private boolean readModuleInfo;
  private final Set<String> pluginFileProvided = new HashSet<>();
  private final Set<String> moduleFileProvided = new HashSet<>();

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    loadProvidedFiles(processingEnv.getFiler());
    ProcessingContext.init(processingEnv, moduleFileProvided);
    this.elementUtils = processingEnv.getElementUtils();
    this.allScopes = new AllScopes();
    this.defaultScope = allScopes.defaultScope();
    ExternalProvider.registerPluginProvidedTypes(defaultScope);
    pluginFileProvided.forEach(defaultScope::pluginProvided);
  }

  /** Loads provider files generated by avaje-inject-maven-plugin */
  void loadProvidedFiles(Filer filer) {
    pluginFileProvided.addAll(targetProvidesLines(filer, "target/avaje-plugin-provides.txt"));
    moduleFileProvided.addAll(targetProvidesLines(filer, "target/avaje-module-provides.txt"));
  }

  private static List<String> targetProvidesLines(Filer filer, String relativeName) {
    try {
      final var resource = targetProvides(filer, relativeName);
      try (var inputStream = new URL(resource).openStream();
          var reader = new BufferedReader(new InputStreamReader(inputStream))) {
        return reader.lines().collect(Collectors.toList());
      }
    } catch (final IOException e) {
      return Collections.emptyList();
    }
  }

  private static String targetProvides(Filer filer, String relativeName) throws IOException {
    return filer
      .getResource(StandardLocation.CLASS_OUTPUT, "", relativeName)
      .toUri()
      .toString()
      .replace("/target/classes", "");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    readModule(roundEnv);
    readScopes(roundEnv.getElementsAnnotatedWith(element(Constants.SCOPE)));
    readChangedBeans(roundEnv.getElementsAnnotatedWith(element(Constants.FACTORY)), true);
    if (defaultScope.includeSingleton()) {
      readChangedBeans(roundEnv.getElementsAnnotatedWith(element(Constants.SINGLETON)), false);
    }
    readChangedBeans(roundEnv.getElementsAnnotatedWith(element(Constants.COMPONENT)), false);
    readChangedBeans(roundEnv.getElementsAnnotatedWith(element(Constants.PROTOTYPE)), false);
   
    final var importedElements =
        roundEnv.getElementsAnnotatedWith(element(ImportPrism.PRISM_TYPE)).stream()
            .map(ImportPrism::getInstanceOn)
            .flatMap(p -> p.value().stream())
            .map(ProcessingContext::asElement)
            .map(TypeElement.class::cast)
            .collect(Collectors.toSet());

    readChangedBeans(importedElements, false);

    readChangedBeans(
        roundEnv.getElementsAnnotatedWith(element(Constants.PROTOTYPE)), false);
    final var typeElement = elementUtils.getTypeElement(Constants.CONTROLLER);
    if (typeElement != null) {
      readChangedBeans(roundEnv.getElementsAnnotatedWith(typeElement), false);
    }
    readChangedBeans(roundEnv.getElementsAnnotatedWith(element(Constants.PROXY)), false);
    allScopes.readBeans(roundEnv);
    defaultScope.write(roundEnv.processingOver());
    allScopes.write(roundEnv.processingOver());
    return false;
  }

  private void readScopes(Set<? extends Element> scopes) {
    for (final Element element : scopes) {
      if ((element.getKind() == ElementKind.ANNOTATION_TYPE) && (element instanceof TypeElement)) {
        final var type = (TypeElement) element;
        allScopes.addScopeAnnotation(type);
      }
    }
    addTestScope();
  }

  /** Add built-in test scope for <code>@TestScope</code> if available. */
  private void addTestScope() {
    final var testScopeType = elementUtils.getTypeElement(Constants.TESTSCOPE);
    if (testScopeType != null) {
      allScopes.addScopeAnnotation(testScopeType);
    }
  }

  /** Read the beans that have changed. */
  private void readChangedBeans(Set<? extends Element> beans, boolean factory) {
    for (final Element element : beans) {
      // ignore methods (e.g. factory methods with @Prototype on them)
      if (element instanceof TypeElement) {
        final var typeElement = (TypeElement) element;
        final var scope = findScope(typeElement);
        if (!factory) {
          // will be found via custom scope so effectively ignore additional @Singleton
          if (scope == null) {
            defaultScope.read(typeElement, false);
          }
        } else if (scope != null) {
          // logWarn("Adding factory to custom scope "+element+" scope: "+scope);
          scope.read(typeElement, true);
        } else {
          defaultScope.read(typeElement, true);
        }
      }
    }
  }

  /** Find the scope if the Factory has a scope annotation. */
  private ScopeInfo findScope(Element element) {
    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final var scopeInfo = allScopes.get(annotationMirror.getAnnotationType().toString());
      if (scopeInfo != null) {
        return scopeInfo;
      }
    }
    return null;
  }

  /** Read the existing meta data from InjectModule (if found) and the factory bean (if exists). */
  private void readModule(RoundEnvironment roundEnv) {
    if (readModuleInfo) {
      // only read the module meta data once
      return;
    }
    readModuleInfo = true;
    final var factory = loadMetaInfServices();
    if (factory != null) {
      final var moduleType = elementUtils.getTypeElement(factory);
      if (moduleType != null) {
        defaultScope.readModuleMetaData(moduleType);
      }
    }
    allScopes.readModules(loadMetaInfCustom());
    readInjectModule(roundEnv);
  }

  /** Read InjectModule for things like package-info etc (not for custom scopes) */
  private void readInjectModule(RoundEnvironment roundEnv) {
    // read other that are annotated with InjectModule
    for (final Element element : roundEnv.getElementsAnnotatedWith(element(Constants.INJECTMODULE))) {
      final var scope = ScopePrism.getInstanceOn(element);
      if (scope == null) {
        // it it not a custom scope annotation
        final var annotation = InjectModulePrism.getInstanceOn(element);
        if (annotation != null) {
          defaultScope.details(annotation.name(), element);
        }
      }
    }
  }
}
