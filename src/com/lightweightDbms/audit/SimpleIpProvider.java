package com.lightweightDbms.audit;

import java.util.Random;

/**
 * Simple pseudo IP provider; not network-aware.
 */
public final class SimpleIpProvider implements IpProvider {
    private final Random random = new Random();

    @Override
    public String getIp() {
        // 10.x.y.z private range
        int a = 10;
        int b = random.nextInt(256);
        int c = random.nextInt(256);
        int d = random.nextInt(256);
        return a + "." + b + "." + c + "." + d;
    }
}