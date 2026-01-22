package com.jutjubic.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Custom naming strategija koja dodaje sufiks SAMO na video_view_crdt tabelu.
 * Sve ostale tabele ostaju neizmenjene ali se konvertuju u snake_case (Hibernate default).
 *
 * Primer:
 * - video_view_crdt -> video_view_crdt_replica1 (za repliku 1)
 * - video_view_crdt -> video_view_crdt_replica2 (za repliku 2)
 * - users -> users (ostaje isto)
 * - emailAdress -> email_adress (camelCase -> snake_case)
 * - posts -> posts (ostaje isto)
 */
@Component
public class ReplicaPhysicalNamingStrategy implements PhysicalNamingStrategy {

    private final String tableSuffix;
    private final CamelCaseToUnderscoresNamingStrategy delegate = new CamelCaseToUnderscoresNamingStrategy();

    public ReplicaPhysicalNamingStrategy(@Value("${replica.table.suffix:default}") String tableSuffix) {
        this.tableSuffix = tableSuffix;
    }

    @Override
    public Identifier toPhysicalCatalogName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return delegate.toPhysicalCatalogName(logicalName, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return delegate.toPhysicalSchemaName(logicalName, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment context) {
        if (logicalName == null) {
            return null;
        }

        // Prvo delegiraj Spring-u da konvertuje u snake_case
        Identifier result = delegate.toPhysicalTableName(logicalName, context);
        String tableName = result.getText();

        // Dodaj sufiks SAMO za video_view_crdt tabelu
        if ("video_view_crdt".equals(tableName)) {
            String newName = tableName + "_" + tableSuffix;
            return Identifier.toIdentifier(newName);
        }

        // Sve ostale tabele vrati sa Spring konverzijom (snake_case)
        return result;
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return delegate.toPhysicalSequenceName(logicalName, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return delegate.toPhysicalColumnName(logicalName, jdbcEnvironment);
    }
}
