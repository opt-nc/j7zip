package nc.opt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.compress.PasswordRequiredException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "j7zip",
    mixinStandardHelpOptions = true,
    description = "Compress / Decompress files using LZMA algortihm",
    versionProvider = J7zip.VersionProvider.class
)
public class J7zip implements Callable<Integer> {

    @Option(names = { "-p", "--password" })
    private String password;

    @Parameters(description = "${COMPLETION-CANDIDATES}")
    private Command command;

    @Parameters(description = { "archive file to for compression or decompression" })
    private String archive;

    @Parameters(
        index = "2..*",
        arity = "0..*",
        paramLabel = "<files|destination>",
        description = { "list of <files> to compress", "<destination> path for decompression (optional)" }
    )
    private String[] names = new String[0];

    public static void main(String... args) {
        System.exit(new CommandLine(new J7zip()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        if (command == Command.x || command == Command.e) {
            try {
                if (names.length == 0) {
                    J7zip.decompress(archive, ".", password, command == Command.x);
                } else {
                    J7zip.decompress(archive, names[0], password, command == Command.x);
                }
            } catch (PasswordRequiredException e) {
                System.err.println("password required");
                return 1;
            }
        } else if (command == Command.a) {
            if (names.length == 0) {
                System.err.println("Source dir(s)/file(s) missing");
                return 1;
            }

            J7zip.compress(archive, password, names);
        }
        return 0;
    }

    public static void compress(String name, String password, String... files) throws IOException {
        try (SevenZOutputFile out = new SevenZOutputFile(new File(name), password != null ? password.toCharArray() : null)) {
            for (String filename : files) {
                File file = new File(filename);
                addToArchiveCompression(out, file, file.getParent());
            }
        }
    }

    private static void addToArchiveCompression(SevenZOutputFile out, File file, String dir) throws IOException {
        String name = dir + File.separator + file.getName();
        if (file.isFile()) {
            SevenZArchiveEntry entry = out.createArchiveEntry(file, name);
            out.putArchiveEntry(entry);

            try (FileInputStream in = new FileInputStream(file)) {
                byte[] b = new byte[8092];
                int count;
                while ((count = in.read(b)) > 0) {
                    out.write(b, 0, count);
                }
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToArchiveCompression(out, child, name);
                }
            }
        } else {
            System.err.println(file.getName() + " is not supported");
        }
    }

    public static void decompress(String in, String destination, String password, boolean arbo) throws IOException {
        destination = Paths.get(destination).toAbsolutePath().normalize().toString(); // absolute destination path for security check
        try (SevenZFile sevenZFile = new SevenZFile(new File(in), password != null ? password.toCharArray() : null)) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name;
                if (arbo) {
                    Paths.get(destination, entry.getName()).toFile().getParentFile().mkdirs();
                    name = entry.getName();
                } else {
                    name = Paths.get(entry.getName()).toFile().getName();
                }

                File file = new File(destination, name);
                if (!file.toPath().normalize().startsWith(Paths.get(destination))) { // security check: prevent "Zip Slip" vulnerability
                    throw new IllegalStateException("Bad zip entry");
                }
                try (OutputStream out = new FileOutputStream(file)) {
                    byte[] buffer = new byte[8092];
                    int read;
                    while ((read = sevenZFile.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }
        }
    }

    static class VersionProvider implements IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            return new String[] { J7zip.class.getPackage().getImplementationVersion() };
        }
    }

    enum Command {
        x("eXtract files with full paths"),
        e("Extract files from archive (without using directory names)"),
        a("Add files to archive");

        private String description;

        private Command(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return name() + " (" + description + ")";
        }
    }
}
