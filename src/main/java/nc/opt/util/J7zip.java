package nc.opt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

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

    @Parameters(description = "${COMPLETION-CANDIDATES}")
    private Command command;

    @Option(names = { "-p", "--password" })
    private String password;

    @Parameters(description = "<archive> <files>..")
    private String[] names;

    public static void main(String... args) {
        System.exit(new CommandLine(new J7zip()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        if (command == Command.x || command == Command.e) {
            if (names.length == 0) {
                System.err.println("Archive file name missing");
                return 1;
            }
            if (names.length < 2) {
                System.err.println("Destination path missing");
                return 1;
            }

            J7zip.decompress(names[1], names[2], password, command == Command.x);
        } else if (command == Command.a) {
            if (names.length < 2) {
                System.err.println("Destination archive file name or source dir(s)/file(s) missing");
                return 1;
            }

            J7zip.compress(names[1], password, Stream.of(names).skip(2).map(File::new).toArray(File[]::new));
        }
        return 0;
    }

    public static void compress(String name, String password, File... files) throws IOException {
        try (SevenZOutputFile out = new SevenZOutputFile(new File(name), password != null ? password.toCharArray() : null)) {
            for (File file : files) {
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
                byte[] b = new byte[1024];
                int count = 0;
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
                try (OutputStream out = new FileOutputStream(new File(destination, name))) {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
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
