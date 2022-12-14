package nc.opt.uil.j7zip;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import nc.opt.util.J7zip;
import picocli.CommandLine;

public class Tests {

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() throws IOException {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    public void restoreStreams() throws IOException {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testCompressDecompress() throws IOException {
        // compress
        int exitCode = new CommandLine(new J7zip()).execute(new String[] { "a", "target/archive.7z", "src/test/resources/poem.txt" });
        restoreStreams();

        String err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // checks (compare file size original > compressed)
        assertTrue(Paths.get("target/archive.7z").toFile().exists());
        assertTrue(Paths.get("target/archive.7z").toFile().length() < Paths.get("src/test/resources/poem.txt").toFile().length());

        // decompress
        setUpStreams();
        exitCode = new CommandLine(new J7zip()).execute(new String[] { "e", "target/archive.7z", "target" });
        restoreStreams();

        err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // check (compare decompressed file with initial)
        assertTrue(Paths.get("target/poem.txt").toFile().exists());
        assertArrayEquals(Files.readAllBytes(Paths.get("src/test/resources/poem.txt")), Files.readAllBytes(Paths.get("target/poem.txt")));

        Paths.get("target/poem.txt").toFile().delete();
        Paths.get("target/archive.7z").toFile().delete();
    }

    @Test
    public void testListArrchiveFiles() throws IOException {
        // compress
        int exitCode = new CommandLine(new J7zip()).execute(new String[] { "a", "target/archive.7z", "src/test/resources" });
        restoreStreams();

        String err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // list contents of file
        setUpStreams();
        exitCode = new CommandLine(new J7zip()).execute(new String[] { "l", "target/archive.7z" });
        restoreStreams();

        err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        String[] lines = new String(this.out.toByteArray()).split("\n");
        assertEquals(2, lines.length);
        assertTrue(Stream.of(lines).anyMatch(f -> f.equals("src/test/resources/poem.txt")));
        assertTrue(Stream.of(lines).anyMatch(f -> f.equals("src/test/resources/poem-with-password.7z")));

        Paths.get("target/archive.7z").toFile().delete();
    }

    @Test
    public void testCompressDirectory() throws IOException {
        // compress
        int exitCode = new CommandLine(new J7zip()).execute(new String[] { "a", "target/archive.7z", "src" });
        restoreStreams();

        String err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // list contents of file
        setUpStreams();
        exitCode = new CommandLine(new J7zip()).execute(new String[] { "l", "target/archive.7z" });
        restoreStreams();

        err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        String[] lines = new String(this.out.toByteArray()).split("\n");
        assertNotEquals(0, lines.length);
        for (String entry : lines) {
            assertTrue(entry.startsWith("src/"), "entry not starts with src/ : " + entry);
        }

        Paths.get("target/archive.7z").toFile().delete();
    }

    /**
     * Test with a archive file compressed by 7z tool
     */
    @Test
    public void testDecompressWithPassword() throws IOException {
        // decompress
        int exitCode = new CommandLine(new J7zip())
            .execute(new String[] { "x", "-p", "poem", "src/test/resources/poem-with-password.7z", "target" });
        restoreStreams();

        String err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // check (compare decompressed file with initial)
        assertTrue(Paths.get("target/src/test/resources/poem.txt").toFile().exists());
        assertArrayEquals(
            Files.readAllBytes(Paths.get("src/test/resources/poem.txt")),
            Files.readAllBytes(Paths.get("target/src/test/resources/poem.txt"))
        );

        Paths.get("target/src/test/resources/poem.txt").toFile().delete();
    }

    @Test
    public void testCompressDecompressWithPassowrd() throws IOException {
        // compress
        int exitCode = new CommandLine(new J7zip())
            .execute(new String[] { "a", "-p", "frog", "target/archive.7z", "src/test/resources/poem.txt" });
        restoreStreams();

        String err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // checks (compare file size original > compressed)
        assertTrue(Paths.get("target/archive.7z").toFile().exists());
        assertTrue(Paths.get("target/archive.7z").toFile().length() < Paths.get("src/test/resources/poem.txt").toFile().length());

        // decompress without password : should fail
        setUpStreams();
        exitCode = new CommandLine(new J7zip()).execute(new String[] { "e", "target/archive.7z", "target" });
        restoreStreams();

        assertNotEquals(0, exitCode);

        // decompress
        setUpStreams();
        exitCode = new CommandLine(new J7zip()).execute(new String[] { "e", "-p", "frog", "target/archive.7z", "target" });
        restoreStreams();

        err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // check (compare decompressed file with initial)
        assertTrue(Paths.get("target/poem.txt").toFile().exists());
        assertArrayEquals(Files.readAllBytes(Paths.get("src/test/resources/poem.txt")), Files.readAllBytes(Paths.get("target/poem.txt")));

        Paths.get("target/poem.txt").toFile().delete();
        Paths.get("target/archive.7z").toFile().delete();
    }

    @Test
    public void testDecompressWithoutTarget() throws IOException {
        // decompress
        int exitCode = new CommandLine(new J7zip()).execute(new String[] { "e", "-p", "poem", "src/test/resources/poem-with-password.7z" });
        restoreStreams();

        String err = new String(this.err.toByteArray());
        if (!err.isEmpty()) {
            System.err.println(err);
            throw new AssertionFailedError(err);
        }
        assertEquals(0, exitCode);

        // check (compare decompressed file with initial)
        assertTrue(Paths.get("poem.txt").toFile().exists());
        assertArrayEquals(Files.readAllBytes(Paths.get("src/test/resources/poem.txt")), Files.readAllBytes(Paths.get("poem.txt")));

        Paths.get("poem.txt").toFile().delete();
    }
}
