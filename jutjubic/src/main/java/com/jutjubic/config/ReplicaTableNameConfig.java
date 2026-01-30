package com.jutjubic.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.mapping.PersistentClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ReplicaTableNameConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final String tableSuffix;
    private final EntityManagerFactory entityManagerFactory;

    public ReplicaTableNameConfig(
            @Value("${replica.table.suffix:default}") String tableSuffix,
            EntityManagerFactory entityManagerFactory) {
        this.tableSuffix = tableSuffix;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("=================================");
        System.out.println("Replica Table Config:");
        System.out.println("  Table suffix: " + tableSuffix);
        System.out.println("  VideoViewCrdt table: video_view_crdt_" + tableSuffix);
        System.out.println("=================================");
    }
}
