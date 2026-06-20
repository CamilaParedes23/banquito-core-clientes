-- Alinea instalaciones heredadas de AUDITORIA_CUSTOMER_EVENTO con el modelo V2.
-- No modifica migraciones ya aplicadas y es segura para bases nuevas o preexistentes.

DELIMITER $$

DROP PROCEDURE IF EXISTS align_customer_audit_schema$$
CREATE PROCEDURE align_customer_audit_schema()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE constraint_to_drop VARCHAR(128);
    DECLARE check_cursor CURSOR FOR
        SELECT tc.CONSTRAINT_NAME
          FROM information_schema.TABLE_CONSTRAINTS tc
          JOIN information_schema.CHECK_CONSTRAINTS cc
            ON cc.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
           AND cc.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
         WHERE tc.CONSTRAINT_SCHEMA = DATABASE()
           AND tc.TABLE_NAME = 'AUDITORIA_CUSTOMER_EVENTO'
           AND tc.CONSTRAINT_TYPE = 'CHECK'
           AND UPPER(cc.CHECK_CLAUSE) LIKE '%RESULTADO%';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    IF EXISTS (
        SELECT 1
          FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = 'AUDITORIA_CUSTOMER_EVENTO'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
              FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'AUDITORIA_CUSTOMER_EVENTO'
               AND COLUMN_NAME = 'UUID_USUARIO'
        ) THEN
            ALTER TABLE AUDITORIA_CUSTOMER_EVENTO
                ADD COLUMN UUID_USUARIO CHAR(36) NULL AFTER UUID_CORRELACION;
        END IF;

        ALTER TABLE AUDITORIA_CUSTOMER_EVENTO
            MODIFY COLUMN RESULTADO VARCHAR(15) NOT NULL,
            MODIFY COLUMN CANAL_ORIGEN VARCHAR(30) NULL;

        OPEN check_cursor;
        drop_loop: LOOP
            FETCH check_cursor INTO constraint_to_drop;
            IF done = 1 THEN
                LEAVE drop_loop;
            END IF;
            SET @drop_check_sql = CONCAT(
                'ALTER TABLE AUDITORIA_CUSTOMER_EVENTO DROP CHECK `',
                REPLACE(constraint_to_drop, '`', '``'),
                '`'
            );
            PREPARE drop_check_stmt FROM @drop_check_sql;
            EXECUTE drop_check_stmt;
            DEALLOCATE PREPARE drop_check_stmt;
        END LOOP;
        CLOSE check_cursor;

        -- Migra valores heredados antes de crear la restricción V2.
        -- El CHECK anterior ya fue retirado para permitir la transformación.
        UPDATE AUDITORIA_CUSTOMER_EVENTO
           SET RESULTADO = CASE UPPER(TRIM(RESULTADO))
               WHEN 'EXITOSO' THEN 'OK'
               WHEN 'FALLIDO' THEN 'ERROR'
               WHEN 'RECHAZADO' THEN 'DENEGADO'
               WHEN 'OK' THEN 'OK'
               WHEN 'ERROR' THEN 'ERROR'
               WHEN 'DENEGADO' THEN 'DENEGADO'
               ELSE 'ERROR'
           END;

        ALTER TABLE AUDITORIA_CUSTOMER_EVENTO
            ADD CONSTRAINT CK_AUDITORIA_CUSTOMER_RESULTADO
            CHECK (RESULTADO IN ('OK','ERROR','DENEGADO'));
    END IF;
END$$

CALL align_customer_audit_schema()$$
DROP PROCEDURE align_customer_audit_schema$$

DELIMITER ;
