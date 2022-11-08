package nc.opt.util;

import java.io.BufferedOutputStream;
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

    @Parameters
    private String[] names;

    @Option(names = { "-c", "--stdout", "--to-stdout" }, description = "output data to stdout")
    private boolean stdout;

    @Option(names = { "-d", "--decompress", "--uncompress" }, description = "decompress file")
    private boolean decompress;

    @Option(names = { "-p", "--password" })
    private String password;

    public static void main(String... args) {
        System.exit(new CommandLine(new J7zip()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        if (decompress) {
            if (names.length == 0) {
                System.err.println("Archive file name missing");
                return 1;
            }

            if (stdout) {
                J7zip.decompress(names[0], null, password);
            } else {
                if (names.length < 2) {
                    System.err.println("Destination path missing");
                    return 1;
                }
                J7zip.decompress(names[0], names[1], password);
            }
        } else {
            if (stdout){
                System.err.println("stdout only applicable on decompression");
                return 1;
            }
            if (password != null){
                System.err.println("password protection is only applicable on decompression");
                return 1;
            }
            if (names.length < 2) {
                System.err.println("Destination archive file name or source dir(s)/file(s) missing");
                return 1;
            }

            J7zip.compress(names[0], Stream.of(names).skip(1).map(File::new).toArray(File[]::new));
        }
        return 0;
    }

    public static void compress(String name, File... files) throws IOException {
        try (SevenZOutputFile out = new SevenZOutputFile(new File(name))) {
            for (File file : files) {
                addToArchiveCompression(out, file, ".");
            }
        }
    }

    public static void decompress(String in, String destination, String password) throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(new File(in), password != null ? password.toCharArray() : null)) {
            SevenZArchiveEntry entry;
            int count = 0;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (destination != null) {
                    Paths.get(destination, entry.getName()).toFile().getParentFile().mkdirs();
                } else if (count > 0) {
                    System.err.println("❌ More than one file exists in archive, the stdout is not suitable for that");
                    System.exit(1);
                    return;
                }
                try (
                    OutputStream out = destination != null
                        ? new FileOutputStream(new File(destination, entry.getName()))
                        : new BufferedOutputStream(System.out)
                ) {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
                }
                count++;
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

    static class VersionProvider implements IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            return new String[] { J7zip.class.getPackage().getImplementationVersion() };
        }
    }
}
