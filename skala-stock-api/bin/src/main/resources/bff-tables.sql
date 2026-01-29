
DROP TABLE IF EXISTS bff_user_group_mst;
CREATE TABLE bff_user_group_mst
  (
    user_group_id VARCHAR(64) NOT NULL,
    user_group_name VARCHAR(128) NOT NULL,
    id_insert VARCHAR(64)  NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,
    PRIMARY KEY (user_group_id)
);


DROP TABLE IF EXISTS bff_user_mst;
CREATE TABLE bff_user_mst
  (
    user_id VARCHAR(64) NOT NULL,
    user_group_id VARCHAR(64) NOT NULL,
    user_name VARCHAR(128) NOT NULL,
    user_password VARCHAR(128) NOT NULL,
    user_email VARCHAR(128) NULL,
    user_phone VARCHAR(128) NULL,
    dt_updated_password DATETIME NULL,
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,
    PRIMARY KEY (user_id)
);

DROP TABLE IF EXISTS bff_menu_mst;
CREATE TABLE bff_menu_mst
  (
    system_id VARCHAR(64) NOT NULL,
    system_name VARCHAR(128) NULL,
    menu_id VARCHAR(128) NOT NULL,
    menu_name VARCHAR(128) NULL,
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,
    PRIMARY KEY (system_id, menu_id)
);

DROP TABLE IF EXISTS bff_user_group_menu;
CREATE TABLE bff_user_group_menu
  (
    user_group_id VARCHAR(64) NOT NULL,
    user_group_name VARCHAR(128) NOT NULL,
    system_id VARCHAR(128) NOT NULL,
    system_name VARCHAR(128) NULL,
    menu_id VARCHAR(128) NOT NULL,
    menu_name VARCHAR(128) NULL,
    auth_level INTEGER NULL,
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,
    PRIMARY KEY (user_group_id, system_id, menu_id)
);


DROP TABLE IF EXISTS bff_saga_mst;
CREATE TABLE bff_saga_mst
  (
    saga_id VARCHAR(64) NOT NULL,
    saga_name VARCHAR(128) NOT NULL,
    backend_apis TEXT NULL,
    
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,
    PRIMARY KEY (saga_id)
);

DROP TABLE IF EXISTS bff_saga_log;
CREATE TABLE bff_saga_log
  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    saga_id VARCHAR(64) NOT NULL,
    saga_name VARCHAR(128) NOT NULL,
    saga_time DATETIME NULL,
    api_name VARCHAR(128) NOT NULL,
    api_url VARCHAR(256) NOT NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    result VARCHAR(8) NULL,
    request_body TEXT NULL,
    response_body TEXT NULL,
    
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL
);

DROP TABLE IF EXISTS bff_code_group_mst;
CREATE TABLE bff_code_group_mst
  (
    code_group_id VARCHAR(64) NOT NULL,
    code_group_name VARCHAR(128) NOT NULL,
    codes TEXT NULL,
    
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL,
    PRIMARY KEY (code_group_id)
);

DROP TABLE IF EXISTS bff_api_mst;
CREATE TABLE bff_api_mst
  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_path VARCHAR(256) NOT NULL,
    api_name VARCHAR(128) NOT NULL,
    
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL
);

DROP TABLE IF EXISTS bff_user_group_api;
CREATE TABLE bff_user_group_api
  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_group_id VARCHAR(64) NOT NULL,
    user_group_name VARCHAR(128) NOT NULL,
    allowed_apis TEXT NULL,
    denied_apis TEXT NULL,
    
    id_insert VARCHAR(64) NULL,
    dt_insert DATETIME NULL,
    id_update VARCHAR(64) NULL,
    dt_update DATETIME NULL
);

DROP TABLE IF EXISTS bff_proxy_log;
CREATE TABLE bff_proxy_log
  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    time_stamp BIGINT NOT NULL,
    proxy_tid VARCHAR(256) NULL,
    request_time DATETIME NOT NULL,
    client_address VARCHAR(128) NULL,
    bff_user_id VARCHAR(128) NULL,
    api_method VARCHAR(16) NULL,
    api_path VARCHAR(256) NULL,
    api_result VARCHAR(16) NULL,
    request_params TEXT NULL,
    request_body TEXT NULL,
    response_code VARCHAR(128) NULL,
    response_body TEXT NULL,
    elapsed_time BIGINT NULL
);
