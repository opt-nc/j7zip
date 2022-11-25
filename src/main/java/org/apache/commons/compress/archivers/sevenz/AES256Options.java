package org.apache.commons.compress.archivers.sevenz;

import java.util.Arrays;
import java.util.Random;

public class AES256Options {
    byte[] password;
    byte[] salt = new byte[0];
    byte[] iv = new byte[16];
    int numCyclesPower = 19;

    public AES256Options(byte[] password) {
        this.password = password;
        new Random(Arrays.hashCode(password)).nextBytes(salt);
        new Random(Arrays.hashCode(password)).nextBytes(iv);
    }
}
