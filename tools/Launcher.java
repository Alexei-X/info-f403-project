import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Launcher {
    // Configure ici si besoin
    private static final String FLEX_SPEC = "src/lexical_analyzer.flex";
    private static final String JFLEX_JAR = "tools/jflex.jar"; // fallback si 'jflex' n'est pas dans le PATH
    private static final String SRC_DIR   = "src";
    private static final String CLASSNAME = "LexicalAnalyzer"; // sans package
    private static final String INPUT     = "testfiles/test.ycc";
    private static final String OUTPUT    = "testfiles/output.txt";

    public static void main(String[] args) throws Exception {
        Path projectRoot = Paths.get("").toAbsolutePath();
        System.out.println("[Launcher] Project: " + projectRoot);

        // 1) Lancer JFlex
        List<String> jflexCmd = buildJFlexCommand();
        System.out.println("[Launcher] JFlex: " + String.join(" ", jflexCmd));
        run(jflexCmd, projectRoot, null, System.out, System.err);

        // 2) Exécuter le lexer: java -cp src LexicalAnalyzer testfiles/test.ycc
        List<String> runCmd = new ArrayList<>();
        runCmd.add(findJavaExe());
        runCmd.add("-cp");
        runCmd.add(SRC_DIR);
        runCmd.add(CLASSNAME);
        runCmd.add(INPUT);

        System.out.println("[Launcher] Run: " + String.join(" ", runCmd));
        try (FileOutputStream fos = new FileOutputStream(OUTPUT);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            int code = run(runCmd, projectRoot, null, bos, System.err);
            if (code != 0) {
                System.err.println("[Launcher] Erreur d'exécution (code " + code + ")");
                System.exit(code);
            }
        }
        System.out.println("[Launcher] OK -> " + OUTPUT);
    }

    private static List<String> buildJFlexCommand() {
        // Essayer 'jflex' du PATH
        if (commandExists("jflex")) {
            return Arrays.asList("jflex", FLEX_SPEC);
        }
        // Sinon fallback sur le JAR local
        if (Files.exists(Paths.get(JFLEX_JAR))) {
            return Arrays.asList(findJavaExe(), "-jar", JFLEX_JAR, FLEX_SPEC);
        }
        System.err.println("[Launcher] JFlex introuvable. Place le JAR dans " + JFLEX_JAR +
                " ou installe JFlex (commande 'jflex' dans le PATH).");
        System.exit(1);
        return List.of(); // unreachable
    }

    private static boolean commandExists(String cmd) {
        try {
            Process p = new ProcessBuilder(isWindows() ? new String[]{"cmd", "/c", "where " + cmd}
                                                       : new String[]{"bash", "-lc", "command -v " + cmd})
                    .redirectErrorStream(true).start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = br.readLine();
                p.waitFor();
                return line != null && !line.isBlank();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static String findJavaExe() {
        // Utilise simplement 'java' du PATH
        return "java";
    }

    private static int run(List<String> cmd, Path cwd, Map<String,String> env,
                           OutputStream stdout, OutputStream stderr) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(cwd.toFile());
        pb.redirectErrorStream(false);
        if (env != null) pb.environment().putAll(env);
        Process p = pb.start();

        // Pipe stdout/stderr
        Thread tOut = pipe(p.getInputStream(), stdout);
        Thread tErr = pipe(p.getErrorStream(), stderr);

        int code = p.waitFor();
        tOut.join();
        tErr.join();
        return code;
    }

    private static Thread pipe(InputStream in, OutputStream out) {
        Thread t = new Thread(() -> {
            try (in; out) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
            } catch (IOException ignored) {}
        });
        t.start();
        return t;
    }
}
