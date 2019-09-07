package org.caojun.dialog;

class RandomUtils {
    static int getRandom(int min, int max) {
        return (int)(Math.random() * (max + 1 - min) + min);
    }

    static boolean getRandom() {
        return getRandom(0, 1) == 0;
    }
}
