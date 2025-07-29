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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DataSyncRepositoryCentral {
    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    // Lazily initialize jdbcTemplate to ensure DataSource is available
    private JdbcTemplate getJdbcTemplate() {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return this.jdbcTemplate;
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    
    // Data Upload Repository
    public int checkRecordIsAlreadyPresentOrNot(String schemaName, String tableName, String vanSerialNo, String vanID,
                                                 String vanAutoIncColumnName, int syncFacilityID) {
        jdbcTemplate = getJdbcTemplate();

        List<Object> params = new ArrayList<>();

       if (!isValidDatabaseIdentifier(schemaName) || !isValidDatabaseIdentifier(tableName) || !isValidDatabaseIdentifier(vanAutoIncColumnName)) {
            logger.error("Invalid database identifier detected: schemaName={}, tableName={}, vanAutoIncColumnName={}", schemaName, tableName, vanAutoIncColumnName);
            throw new IllegalArgumentException("Invalid database identifier provided.");
        }


        StringBuilder queryBuilder = new StringBuilder("SELECT ");
        queryBuilder.append(vanAutoIncColumnName);
        queryBuilder.append(" FROM ");
        queryBuilder.append(schemaName).append(".").append(tableName);

        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" WHERE ");
        whereClause.append("VanSerialNo = ?");
        params.add(vanSerialNo);

        if (Arrays.asList("t_patientissue", "t_physicalstockentry", "t_stockadjustment", "t_saitemmapping",
                "t_stocktransfer", "t_patientreturn", "t_facilityconsumption", "t_indent",
                "t_indentorder", "t_indentissue", "t_itemstockentry", "t_itemstockexit")
                .contains(tableName.toLowerCase()) && syncFacilityID > 0) {

            whereClause.append(" AND ");
            whereClause.append("SyncFacilityID = ?");
            params.add(syncFacilityID);

        } else {
            whereClause.append(" AND ");
            whereClause.append("VanID = ?");
            params.add(vanID);
        }

        queryBuilder.append(whereClause);
        String query = queryBuilder.toString();
        Object[] queryParams = params.toArray();

        logger.debug("Checking record existence query: {} with params: {}", query, Arrays.toString(queryParams));

        try {
            List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(query, queryParams);
            if (resultSet != null && !resultSet.isEmpty()) {
                logger.debug("Record found for table {}: VanSerialNo={}, VanID={}", tableName, vanSerialNo, vanID);
                return 1;
            } else {
                logger.debug("No record found for table {}: VanSerialNo={}, VanID={}", tableName, vanSerialNo, vanID);
                return 0;
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            logger.debug("No record found (EmptyResultDataAccessException) for table {}: VanSerialNo={}, VanID={}", tableName, vanSerialNo, vanID);
            return 0;
        } catch (Exception e) {
            logger.error("Database error during checkRecordIsAlreadyPresentOrNot for table {}: VanSerialNo={}, VanID={}. Error: {}", tableName, vanSerialNo, vanID, e.getMessage(), e);
            throw new RuntimeException("Failed to check record existence: " + e.getMessage(), e); // Re-throw or handle as appropriate
        }
    }
    

    // Helper method to validate database identifiers
    private boolean isValidDatabaseIdentifier(String identifier) {
        return identifier != null && identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    // Method for synchronization of data to central DB
    public int[] syncDataToCentralDB(String schema, String tableName, String serverColumns, String query,
                                     List<Object[]> syncDataList) {
        jdbcTemplate = getJdbcTemplate();
        try {
            int[] i = jdbcTemplate.batchUpdate(query, syncDataList);
            logger.info("Batch operation completed for table {}. Results: {}", tableName, Arrays.toString(i));
            return i;
        } catch (Exception e) {
            logger.error("Exception during batch update for table {}: {}", tableName, e.getMessage(), e);
            throw new RuntimeException("Batch sync failed for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    // End of Data Upload Repository

    public List<Map<String, Object>> getMasterDataFromTable(String schema, String table, String columnNames,
                                                            String masterType, Timestamp lastDownloadDate, Integer vanID, Integer psmID) throws Exception {
        jdbcTemplate = getJdbcTemplate();
        List<Map<String, Object>> resultSetList = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (!isValidDatabaseIdentifier(schema) || !isValidDatabaseIdentifier(table)) {
            throw new IllegalArgumentException("Invalid database identifier provided.");
        }

        StringBuilder baseQueryBuilder = new StringBuilder(" SELECT ");
        baseQueryBuilder.append(columnNames); 
        baseQueryBuilder.append(" FROM ");
        baseQueryBuilder.append(schema).append(".").append(table);

        if (masterType != null) {
            if (lastDownloadDate != null) {
                baseQueryBuilder.append(" WHERE LastModDate >= ? ");
                params.add(lastDownloadDate);

                if (masterType.equalsIgnoreCase("V")) {
                    baseQueryBuilder.append(" AND VanID = ? ");
                    params.add(vanID);
                } else if (masterType.equalsIgnoreCase("P")) {
                    baseQueryBuilder.append(" AND ProviderServiceMapID = ? ");
                    params.add(psmID);
                }
            } else {
                if (masterType.equalsIgnoreCase("V")) {
                    baseQueryBuilder.append(" WHERE VanID = ? ");
                    params.add(vanID);
                } else if (masterType.equalsIgnoreCase("P")) {
                    baseQueryBuilder.append(" WHERE ProviderServiceMapID = ? ");
                    params.add(psmID);
                }
            }
        }

        String finalQuery = baseQueryBuilder.toString();
        try {
            if (params.isEmpty()) {
                resultSetList = jdbcTemplate.queryForList(finalQuery);
            } else {
                resultSetList = jdbcTemplate.queryForList(finalQuery, params.toArray());
            }
        } catch (Exception e) {
            logger.error("Error fetching master data from table {}: {}", table, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch master data: " + e.getMessage(), e);
        }
        logger.info("Result set Details size: {}", resultSetList.size());
        return resultSetList;
    }

    public List<Map<String, Object>> getBatchForBenDetails(String schema, String table, String columnNames,
                                                            String whereClause, int limit, int offset) {
        jdbcTemplate = getJdbcTemplate();

        if (!isValidDatabaseIdentifier(schema) || !isValidDatabaseIdentifier(table)) {
            logger.error("Invalid database identifier detected in getBatchForBenDetails: schema={}, table={}", schema, table);
            throw new IllegalArgumentException("Invalid database identifier provided.");
        }
        
        String query = "SELECT " + columnNames + " FROM " + schema + "." + table + whereClause + " LIMIT ? OFFSET ?";
        logger.debug("Fetching batch for beneficiary details. Query: {}, Limit: {}, Offset: {}", query, limit, offset);
        try {
            return jdbcTemplate.queryForList(query, limit, offset);
        } catch (Exception e) {
            logger.error("Error fetching batch for beneficiary details from table {}: {}", table, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch batch data: " + e.getMessage(), e);
        }
    }

}