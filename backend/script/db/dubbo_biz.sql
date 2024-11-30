--
-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

DROP TABLE IF EXISTS stock_tbl;
CREATE TABLE stock_tbl
(
    id             SERIAL PRIMARY KEY,
    commodity_code VARCHAR(255),
    count          INTEGER DEFAULT 0,
    CONSTRAINT uk_stock_commodity_code UNIQUE (commodity_code)
);

DROP TABLE IF EXISTS order_tbl;
CREATE TABLE order_tbl
(
    id             SERIAL PRIMARY KEY,
    user_id        VARCHAR(255),
    commodity_code VARCHAR(255),
    count          INTEGER DEFAULT 0,
    money          INTEGER DEFAULT 0
);

DROP TABLE IF EXISTS account_tbl;
CREATE TABLE account_tbl
(
    id      SERIAL PRIMARY KEY,
    user_id VARCHAR(255),
    money   INTEGER DEFAULT 0
);

INSERT INTO account_tbl(user_id, money) VALUES ('ACC_001', 1000);
INSERT INTO stock_tbl(commodity_code, count) VALUES ('STOCK_001', 100);
