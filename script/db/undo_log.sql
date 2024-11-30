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

CREATE TABLE IF NOT EXISTS undo_log
(
    id            SERIAL PRIMARY KEY,
    branch_id     BIGINT NOT NULL,
    xid           VARCHAR(128) NOT NULL,
    context       VARCHAR(128) NOT NULL,
    rollback_info BYTEA NOT NULL,
    log_status    INTEGER NOT NULL,
    log_created   TIMESTAMP(6) NOT NULL,
    log_modified  TIMESTAMP(6) NOT NULL,
    CONSTRAINT ux_undo_log UNIQUE (xid, branch_id)
);

CREATE INDEX ix_log_created ON undo_log (log_created);

COMMENT ON TABLE undo_log IS 'AT transaction mode undo table';
COMMENT ON COLUMN undo_log.id IS 'increment id';
COMMENT ON COLUMN undo_log.branch_id IS 'branch transaction id';
COMMENT ON COLUMN undo_log.xid IS 'global transaction id';
COMMENT ON COLUMN undo_log.context IS 'undo_log context,such as serialization';
COMMENT ON COLUMN undo_log.rollback_info IS 'rollback info';
COMMENT ON COLUMN undo_log.log_status IS '0:normal status,1:defense status';
COMMENT ON COLUMN undo_log.log_created IS 'create datetime';
COMMENT ON COLUMN undo_log.log_modified IS 'modify datetime';
