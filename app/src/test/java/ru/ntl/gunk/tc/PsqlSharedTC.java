package ru.ntl.gunk.tc;

import org.testcontainers.containers.PostgreSQLContainer;

public class PsqlSharedTC extends PostgreSQLContainer<PsqlSharedTC>{

    private static final String IMAGE_VERSION = "postgres:11.1";
    private static PsqlSharedTC container;

    public PsqlSharedTC() {
        super(IMAGE_VERSION);
    }

    public static PsqlSharedTC getInstance() {
        if (container == null) {
            container = new PsqlSharedTC();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}