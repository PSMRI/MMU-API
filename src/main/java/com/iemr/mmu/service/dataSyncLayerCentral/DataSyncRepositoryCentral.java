/*
* AMRIT – Accessible Medical Records via Integrated Technology
* Integrated EHR (Electronic Health Records) Solution
*
* Copyright (C) "Piramal Swasthya Management and Research Institute"
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mmu.service.dataSyncLayerCentral;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;

@Service
public class DataSyncRepositoryCentral {
    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate getJdbcTemplate() {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return this.jdbcTemplate;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private static final Set<String> VALID_SCHEMAS = Set.of("public", "db_iemr");

    private static final Set<String> VALID_TABLES = Set.of(
            "m_beneficiaryregidmapping", "i_beneficiaryaccount", "i_beneficiaryaddress", "i_beneficiarycontacts",
            "i_beneficiarydetails", "i_beneficiaryfamilymapping", "i_beneficiaryidentity", "i_beneficiarymapping",
            "t_benvisitdetail", "t_phy_anthropometry", "t_phy_vitals", "t_benadherence", "t_anccare", "t_pnccare",
            "t_ncdscreening", "t_ncdcare", "i_ben_flow_outreach", "t_covid19", "t_idrsdetails", "t_physicalactivity",
            "t_phy_generalexam", "t_phy_headtotoe", "t_sys_obstetric", "t_sys_gastrointestinal", "t_sys_cardiovascular",
            "t_sys_respiratory", "t_sys_centralnervous", "t_sys_musculoskeletalsystem", "t_sys_genitourinarysystem",
            "t_ancdiagnosis", "t_ncddiagnosis", "t_pncdiagnosis", "t_benchefcomplaint", "t_benclinicalobservation",
            "t_prescription", "t_prescribeddrug", "t_lab_testorder", "t_benreferdetails",
            "t_lab_testresult", "t_physicalstockentry", "t_patientissue", "t_facilityconsumption", "t_itemstockentry",
            "t_itemstockexit", "t_benmedhistory", "t_femaleobstetrichistory", "t_benmenstrualdetails",
            "t_benpersonalhabit", "t_childvaccinedetail1", "t_childvaccinedetail2", "t_childoptionalvaccinedetail",
            "t_ancwomenvaccinedetail", "t_childfeedinghistory", "t_benallergyhistory", "t_bencomorbiditycondition",
            "t_benmedicationhistory", "t_benfamilyhistory", "t_perinatalhistory", "t_developmenthistory",
            "t_cancerfamilyhistory", "t_cancerpersonalhistory", "t_cancerdiethistory", "t_cancerobstetrichistory",
            "t_cancervitals", "t_cancersignandsymptoms", "t_cancerlymphnode", "t_canceroralexamination",
            "t_cancerbreastexamination", "t_cancerabdominalexamination", "t_cancergynecologicalexamination",
            "t_cancerdiagnosis", "t_cancerimageannotation", "i_beneficiaryimage", "t_stockadjustment",
            "t_stocktransfer", "t_patientreturn", "t_indent", "t_indentissue", "t_indentorder", "t_saitemmapping"
    );

    private boolean isValidDatabaseIdentifierCharacter(String identifier) {
        return identifier != null && identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    private boolean isValidSchemaName(String schemaName) {
        return VALID_SCHEMAS.contains(schemaName.toLowerCase());
    }

    private boolean isValidTableName(String tableName) {
        return VALID_TABLES.contains(tableName.toLowerCase());
    }

    private boolean isValidColumnNamesList(String columnNames) {
        if (columnNames == null || columnNames.trim().isEmpty()) {
            return false;
        }
        for (String col : columnNames.split(",")) {
            if (!isValidDatabaseIdentifierCharacter(col.trim())) {
                return false;
            }
        }
        return true;
    }

    public int checkRecordIsAlreadyPresentOrNot(String schemaName, String tableName, String vanSerialNo, String vanID,
                                                 String vanAutoIncColumnName, int syncFacilityID) {
        jdbcTemplate = getJdbcTemplate();
        List<Object> params = new ArrayList<>();

        if (!isValidSchemaName(schemaName) || !isValidTableName(tableName) ||
                !isValidDatabaseIdentifierCharacter(vanAutoIncColumnName)) {
            logger.error("Invalid identifiers: schema={}, table={}, column={}", schemaName, tableName, vanAutoIncColumnName);
            throw new IllegalArgumentException("Invalid identifiers provided.");
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT ")
                .append(vanAutoIncColumnName).append(" FROM ")
                .append(schemaName).append(".").append(tableName).append(" WHERE VanSerialNo = ?");

        params.add(vanSerialNo);

        if (List.of("t_patientissue", "t_physicalstockentry", "t_stockadjustment", "t_saitemmapping",
                "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
                "t_indentorder", "t_indentissue", "t_itemstockentry", "t_itemstockexit").contains(tableName.toLowerCase()) && syncFacilityID > 0) {
            queryBuilder.append(" AND SyncFacilityID = ?");
            params.add(syncFacilityID);
        } else {
            queryBuilder.append(" AND VanID = ?");
            params.add(vanID);
        }

        try {
            List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(queryBuilder.toString(), params.toArray());
            return (resultSet != null && !resultSet.isEmpty()) ? 1 : 0;
        } catch (Exception e) {
            logger.error("Error checking record presence: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check record existence: " + e.getMessage(), e);
        }
    }

    public int[] syncDataToCentralDB(String schema, String tableName, String serverColumns, String query,
                                     List<Object[]> syncDataList) {
        jdbcTemplate = getJdbcTemplate();
        try {
            return jdbcTemplate.batchUpdate(query, syncDataList);
        } catch (Exception e) {
            logger.error("Batch sync failed for table {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Batch sync failed: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getMasterDataFromTable(String schema, String table, String columnNames,
                                                            String masterType, Timestamp lastDownloadDate, Integer vanID, Integer psmID) {
        jdbcTemplate = getJdbcTemplate();
        List<Object> params = new ArrayList<>();

        if (!isValidSchemaName(schema) || !isValidTableName(table) || !isValidColumnNamesList(columnNames)) {
            throw new IllegalArgumentException("Invalid schema, table, or column names.");
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT ").append(columnNames)
                .append(" FROM ").append(schema).append(".").append(table);

        if (masterType != null) {
            if (lastDownloadDate != null) {
                queryBuilder.append(" WHERE LastModDate >= ?");
                params.add(lastDownloadDate);

                if ("V".equalsIgnoreCase(masterType)) {
                    queryBuilder.append(" AND VanID = ?");
                    params.add(vanID);
                } else if ("P".equalsIgnoreCase(masterType)) {
                    queryBuilder.append(" AND ProviderServiceMapID = ?");
                    params.add(psmID);
                }
            } else {
                queryBuilder.append(" WHERE ");
                if ("V".equalsIgnoreCase(masterType)) {
                    queryBuilder.append("VanID = ?");
                    params.add(vanID);
                } else if ("P".equalsIgnoreCase(masterType)) {
                    queryBuilder.append("ProviderServiceMapID = ?");
                    params.add(psmID);
                }
            }
        }

        try {
            // Safe dynamic SQL: All dynamic parts (table names, columns, etc.) are validated or hardcoded.
            // Parameter values are bound safely using prepared statement placeholders (?).
            return jdbcTemplate.queryForList(queryBuilder.toString(), params.toArray());
        } catch (Exception e) {
            logger.error("Error fetching master data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch master data: " + e.getMessage(), e);
        }
    }

        public List<Map<String, Object>> getBatchForBenDetails(SyncUploadDataDigester digester,
                                                            String whereClause, int limit, int offset) {
            jdbcTemplate = getJdbcTemplate();

String schema = digester.getSchemaName();
    String table = digester.getTableName();
    String columnNames = digester.getServerColumns();


            if (!isValidSchemaName(schema) || !isValidTableName(table) || !isValidColumnNamesList(columnNames)) {
                throw new IllegalArgumentException("Invalid schema, table, or column names.");
            }
            // Safe dynamic SQL: Schema, table, and column names are validated against predefined whitelists.
            // Only trusted values are used in the query string.
            // limit and offset are passed as parameters to prevent SQL injection.
            String query = String.format("SELECT %s FROM %s.%s %s LIMIT ? OFFSET ?", columnNames, schema, table, whereClause); //NOSONAR

            try {
                return jdbcTemplate.queryForList(query, limit, offset);
            } catch (Exception e) {
                logger.error("Error fetching batch details: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to fetch batch data: " + e.getMessage(), e);
            }
        }
}
