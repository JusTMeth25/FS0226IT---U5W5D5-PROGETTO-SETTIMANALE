-- Eseguito a ogni avvio dopo Hibernate (ddl-auto=update non aggiorna i CHECK degli enum).
-- Allinea il vincolo sugli stati del documento ai valori di StatoDocumento.
ALTER TABLE documenti DROP CONSTRAINT IF EXISTS documenti_stato_check;
ALTER TABLE documenti ADD CONSTRAINT documenti_stato_check
    CHECK (stato IN ('IN_ATTESA', 'IN_ELABORAZIONE', 'DA_REVISIONARE', 'COMPLETATO', 'ERRORE'));
