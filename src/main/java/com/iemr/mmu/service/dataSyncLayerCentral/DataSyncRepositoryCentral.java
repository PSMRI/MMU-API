package com.iemr.mmu.service.dataSyncLayerCentral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.sql.Statement; // Import Statement for batchUpdate result interpretation
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
        System.out.println("Checking record existence query: " + query + " with params: " + Arrays.toString(queryParams));

        try {
            List<Map<String, Object>> resultSet = jdbcTemplate.queryForList(query, queryParams);
            if (resultSet != null && !resultSet.isEmpty()) {
                System.out.println("Record found for table " + tableName + ": VanSerialNo=" + vanSerialNo + ", VanID=" + vanID);
                logger.debug("Record found for table {}: VanSerialNo={}, VanID={}", tableName, vanSerialNo, vanID);
                return 1;
            } else {
                System.out.println("No record found for table " + tableName + ": VanSerialNo=" + vanSerialNo + ", VanID=" + vanID);
                logger.debug("No record found for table {}: VanSerialNo={}, VanID={}", tableName, vanSerialNo, vanID);
                return 0;
            }
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            System.out.println("No record found (EmptyResultDataAccessException) for table " + tableName + ": VanSerialNo=" + vanSerialNo + ", VanID=" + vanID);
            logger.debug("No record found (EmptyResultDataAccessException) for table {}: VanSerialNo={}, VanID={}", tableName, vanSerialNo, vanID);
            return 0;
        } catch (Exception e) {
            System.out.println("Database error during checkRecordIsAlreadyPresentOrNot for table " + tableName + ": VanSerialNo=" + vanSerialNo + ", VanID=" + vanID);
            logger.error("Database error during checkRecordIsAlreadyPresentOrNot for table {}: VanSerialNo={}, VanID={}. Error: {}", tableName, vanSerialNo, vanID, e.getMessage(), e);
            throw new RuntimeException("Failed to check record existence: " + e.getMessage(), e); // Re-throw or handle as appropriate
        }
    }

    // Method for synchronization of data to central DB
    public int[] syncDataToCentralDB(String schema, String tableName, String serverColumns, String query,
                                     List<Object[]> syncDataList) {
        jdbcTemplate = getJdbcTemplate();
        logger.info("Executing batch operation for table: {}. Query type: {}. Number of records: {}", tableName, query.startsWith("INSERT") ? "INSERT" : "UPDATE", syncDataList.size());
        logger.debug("Query: {}", query);
System.out.println("Executing batch operation for table: " + tableName + ". Query type: " + (query.startsWith("INSERT") ? "INSERT" : "UPDATE") + ". Number of records: " + syncDataList.size());
        try {
            // Start batch insert/update
            int[] i = jdbcTemplate.batchUpdate(query, syncDataList);
            System.out.println("Batch operation completed for table " + tableName + ". Results: " + Arrays.toString(i));
            logger.info("Batch operation completed for table {}. Results: {}", tableName, Arrays.toString(i));
            return i;
        } catch (Exception e) {
            logger.error("Exception during batch update for table {}: {}", tableName, e.getMessage(), e);
            System.out.println("Exception during batch update for table " + tableName + ": " + e.getMessage());
            // Log the error with detailed information
            // Re-throw the exception to be handled by the service layer, so specific errors can be captured.
            throw new RuntimeException("Batch sync failed for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    // End of Data Upload Repository

    public List<Map<String, Object>> getMasterDataFromTable(String schema, String table, String columnNames,
                                                            String masterType, Timestamp lastDownloadDate, Integer vanID, Integer psmID) throws Exception {
        jdbcTemplate = getJdbcTemplate();
        List<Map<String, Object>> resultSetList = new ArrayList<>();
        StringBuilder baseQueryBuilder = new StringBuilder(" SELECT ").append(columnNames).append(" FROM ").append(schema).append(".").append(table);
        List<Object> params = new ArrayList<>();

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
        logger.info("Select query central: {}", finalQuery);
        logger.info("Last Downloaded Date: {}", lastDownloadDate);
        logger.info("Query Params: {}", params);
        System.out.println("Select query central: " + finalQuery);
        System.out.println("Last Downloaded Date: " + lastDownloadDate);
        System.out.println("Query Params: " + params);

        try {
            if (params.isEmpty()) {
                resultSetList = jdbcTemplate.queryForList(finalQuery);
            } else {
                resultSetList = jdbcTemplate.queryForList(finalQuery, params.toArray());
            }
        } catch (Exception e) {
            System.out.println("Error fetching master data from table " + table + ": " + e.getMessage());
            logger.error("Error fetching master data from table {}: {}", table, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch master data: " + e.getMessage(), e);
        }
System.out.println("Result set Details size: " + resultSetList.size());
        logger.info("Result set Details size: {}", resultSetList.size());
        return resultSetList;
    }

    public List<Map<String, Object>> getBatchForBenDetails(String schema, String table, String columnNames,
                                                            String whereClause, int limit, int offset) {
        jdbcTemplate = getJdbcTemplate();
        String query = "SELECT " + columnNames + " FROM " + schema + "." + table + whereClause + " LIMIT ? OFFSET ?";
        System.out.println("Fetching batch for beneficiary details. Query: " + query + ", Limit: " + limit + ", Offset: " + offset);
        logger.debug("Fetching batch for beneficiary details. Query: {}, Limit: {}, Offset: {}", query, limit, offset);
        try {
            return jdbcTemplate.queryForList(query, limit, offset);
        } catch (Exception e) {
            logger.error("Error fetching batch for beneficiary details from table {}: {}", table, e.getMessage(), e);
            System.out.println("Error fetching batch for beneficiary details from table " + table + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch batch data: " + e.getMessage(), e);
        }
    }

    // End of Data Download Repository
}