#!/bin/bash
psql --username "postgres" <<-EOSQL
	CREATE DATABASE product_shop;
EOSQL

psql -v ON_ERROR_STOP=1 --username "postgres" <<-EOSQL
  \c product_shop

	CREATE TABLE IF NOT EXISTS product (
      id bigint GENERATED ALWAYS AS IDENTITY,
      name varchar(200)
  );

  CREATE TABLE IF NOT EXISTS product_count (
      product_id bigint UNIQUE,
      count integer
  );

  CREATE TABLE IF NOT EXISTS producer (
      id bigint GENERATED ALWAYS AS IDENTITY,
      name varchar(200) UNIQUE
  );

  CREATE TABLE IF NOT EXISTS product_producer (
      product_id bigint UNIQUE,
      producer_id bigint
  );
EOSQL