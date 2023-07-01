package io.avaje.inject.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class InjectProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws IOException {
    try {
      Files.walk(Paths.get("io").toAbsolutePath())
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
      Paths.get("io.avaje.inject.spi.Module").toAbsolutePath().toFile().delete();
    } catch (final Exception e) {
    }
  }

  @Test
  void testGeneration() throws Exception {
    final String source =
        Paths.get("src/test/java/io/avaje/inject/generator/models/valid")
            .toAbsolutePath()
            .toString();

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);

    manager.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);

    final Iterable<JavaFileObject> files =
        manager.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);

    final CompilationTask task =
        compiler.getTask(
            new PrintWriter(System.out), null, null, Arrays.asList("--release=17"), null, files);
    task.setProcessors(Arrays.asList(new Processor()));

    assertThat(task.call()).isTrue();
  }
}
