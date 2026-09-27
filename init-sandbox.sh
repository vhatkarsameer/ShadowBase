#!/bin/bash

echo "1. Registering Debezium Connector..."
curl -s -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d '{
  "name": "shadowbase-postgres-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "mock-prod-db",
    "database.port": "5432",
    "database.user": "prod_user",
    "database.password": "prod_password",
    "database.dbname": "prod_db",
    "topic.prefix": "prod",
    "plugin.name": "pgoutput"
  }
}'

echo -e "\n\n2. Initializing Database Schema..."
docker exec shadowbase-mock-prod-db-1 psql -U prod_user -d prod_db -c "CREATE TABLE IF NOT EXISTS test_cdc (id SERIAL PRIMARY KEY, message VARCHAR(255));"

echo "Sandbox is fully linked and ready for testing!"