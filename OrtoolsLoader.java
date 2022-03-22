import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Objects;

public class OrtoolsLoader {
  private static final String RESOURCE_PATH = "/ortools-" + Platform.RESOURCE_PREFIX + "/";

  private static boolean loaded = false;

  public static synchronized void loadNativeLibraries() {
    if (!loaded) {
      loadNativeLibrariesFromJar();
      loaded = true;
    }
  }

  private static void loadNativeLibrariesFromJar() {
    try {
      Path tempDirectory = Files.createTempDirectory("ortools-java");
      tempDirectory.toFile().deleteOnExit();

      // copy ortools
      copyNativeLibrary(tempDirectory, System.mapLibraryName("ortools"));

      // copy jniortools
      String libPath = copyNativeLibrary(tempDirectory, System.mapLibraryName("jniortools"));

      // load jniortools
      System.load(libPath);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String copyNativeLibrary(Path tempPath, String libName) throws IOException {
    Path resourcePath = Paths.get(RESOURCE_PATH).resolve(libName);

    File temp = new File(tempPath.toFile(), libName);
    try (InputStream is = OrtoolsLoader.class.getResourceAsStream(resourcePath.toString())) {
      Objects.requireNonNull(is, String.format("Resource %s was not found", RESOURCE_PATH));
      Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } finally {
      temp.deleteOnExit();
    }
    return temp.getAbsolutePath();
  }
}
