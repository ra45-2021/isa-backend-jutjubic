package com.jutjubic.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

        Identifier result = delegate.toPhysicalTableName(logicalName, context);
        String tableName = result.getText();

        if ("video_view_crdt".equals(tableName)) {
            String newName = tableName + "_" + tableSuffix;
            return Identifier.toIdentifier(newName);
        }

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
