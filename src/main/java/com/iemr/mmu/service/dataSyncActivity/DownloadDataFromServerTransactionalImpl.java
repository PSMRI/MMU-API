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
package com.iemr.mmu.service.dataSyncActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.iemr.mmu.data.syncActivity_syncLayer.Indent;
import com.iemr.mmu.data.syncActivity_syncLayer.IndentIssue;
import com.iemr.mmu.data.syncActivity_syncLayer.IndentOrder;
import com.iemr.mmu.data.syncActivity_syncLayer.ItemStockEntry;
import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;
import com.iemr.mmu.data.syncActivity_syncLayer.T_StockTransfer;
import com.iemr.mmu.repo.login.MasterVanRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentIssueRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentOrderRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.ItemStockEntryRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.StockTransferRepo;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.RestTemplateUtil;
import com.iemr.mmu.utils.mapper.InputMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
@PropertySource("classpath:application.properties")
public class DownloadDataFromServerTransactionalImpl implements DownloadDataFromServerTransactional {

	@Value("${dataSyncTransactionDownloadUrl}")
	private String dataSyncTransactionDownloadUrl;

	@Value("${dataSyncProcessedFlagUpdate}")
	private String dataSyncProcessedFlagUpdate;

	@Autowired
	private MasterVanRepo masterVanRepo;
	@Autowired
	private IndentRepo indentRepo;
	@Autowired
	private IndentOrderRepo indentOrderRepo;
	@Autowired
	private IndentIssueRepo indentIssueRepo;
	@Autowired
	private StockTransferRepo stockTransferRepo;
	@Autowired
	private ItemStockEntryRepo itemStockEntryRepo;
	@Autowired
	private CookieUtil cookieUtil;

	public int downloadTransactionalData(int vanID, String ServerAuthorization, String token) throws Exception {
		JSONObject obj;
		for (int i = 0; i < 5; i++) {
			switch (i) {
			case 0: {
				obj = downloadDataFromCentral("db_iemr", "t_indent", vanID, ServerAuthorization, token);
				List<Long> ids = new ArrayList<Long>();
				Indent[] indentArr = InputMapper.gson(1).fromJson(String.valueOf(obj.get("data")), Indent[].class, 1);
				List<Indent> indentList = Arrays.asList(indentArr);

				if (indentList != null && indentList.size() > 0) {

					for (Indent indent : indentList) {
						ids.add(indent.getIndentID());

						Long id = indentRepo.searchBySyncFacilityIDAndVanSerialNo(indent.getSyncFacilityID(),
								indent.getVanSerialNo());
						if (id != null)
							indent.setIndentID(id);
						else
							indent.setIndentID(null);

						indent.setProcessed("P");

					}
					indentRepo.saveAll(indentList);

					int updateFlag = updateProcessedFlagToCentral("db_iemr", "t_indent", ids, ServerAuthorization);
				}

				break;
			}
			case 1: {
				obj = downloadDataFromCentral("db_iemr", "t_indentorder", vanID, ServerAuthorization, token);
				List<Long> ids = new ArrayList<Long>();
				IndentOrder[] indentOrderArr = InputMapper.gson(1).fromJson(String.valueOf(obj.get("data")),
						IndentOrder[].class, 1);
				List<IndentOrder> indentOrderList = Arrays.asList(indentOrderArr);

				if (indentOrderList != null && indentOrderList.size() > 0) {

					for (IndentOrder indentOrder : indentOrderList) {
						ids.add(indentOrder.getIndentOrderID());

						Long id = indentOrderRepo.searchBySyncFacilityIDAndVanSerialNo(indentOrder.getSyncFacilityID(),
								indentOrder.getVanSerialNo());
						if (id != null)
							indentOrder.setIndentOrderID(id);
						else
							indentOrder.setIndentOrderID(null);

						indentOrder.setProcessed("P");
					}
					indentOrderRepo.saveAll(indentOrderList);
					int updateFlag = updateProcessedFlagToCentral("db_iemr", "t_indentorder", ids, ServerAuthorization);
				}
				break;
			}
			case 2: {
				obj = downloadDataFromCentral("db_iemr", "t_indentissue", vanID, ServerAuthorization, token);
				List<Long> ids = new ArrayList<Long>();
				IndentIssue[] indentIssueArr = InputMapper.gson(1).fromJson(String.valueOf(obj.get("data")),
						IndentIssue[].class, 1);
				List<IndentIssue> indentIssueList = Arrays.asList(indentIssueArr);

				if (indentIssueList != null && indentIssueList.size() > 0) {

					for (IndentIssue indentIssue : indentIssueList) {
						ids.add(indentIssue.getIndentIssueID());

						Long id = indentIssueRepo.searchBySyncFacilityIDAndVanSerialNo(indentIssue.getSyncFacilityID(),
								indentIssue.getVanSerialNo());
						if (id != null)
							indentIssue.setIndentIssueID(id);
						else
							indentIssue.setIndentIssueID(null);

						indentIssue.setProcessed("P");

					}
					indentIssueRepo.saveAll(indentIssueList);

					int updateFlag = updateProcessedFlagToCentral("db_iemr", "t_indentissue", ids, ServerAuthorization);
				}
				break;
			}
			case 3: {
				obj = downloadDataFromCentral("db_iemr", "t_stocktransfer", vanID, ServerAuthorization, token);
				List<Long> ids = new ArrayList<Long>();
				T_StockTransfer[] stockTransferArr = InputMapper.gson(1).fromJson(String.valueOf(obj.get("data")),
						T_StockTransfer[].class, 1);
				List<T_StockTransfer> stockTransferList = Arrays.asList(stockTransferArr);

				if (stockTransferList != null && stockTransferList.size() > 0) {

					for (T_StockTransfer stockTransfer : stockTransferList) {
						ids.add(stockTransfer.getStockTransferID());

						Long id = stockTransferRepo.searchBySyncFacilityIDAndVanSerialNo(
								stockTransfer.getSyncFacilityID(), stockTransfer.getVanSerialNo());
						if (id != null)
							stockTransfer.setStockTransferID(id);
						else
							stockTransfer.setStockTransferID(null);

						stockTransfer.setProcessed("P");
					}
					stockTransferRepo.saveAll(stockTransferList);

					int updateFlag = updateProcessedFlagToCentral("db_iemr", "t_stocktransfer", ids,
							ServerAuthorization);
				}
				break;
			}
			case 4: {
				obj = downloadDataFromCentral("db_iemr", "t_itemstockentry", vanID, ServerAuthorization, token);
				List<Long> ids = new ArrayList<Long>();
				ItemStockEntry[] itemStockEntryArr = InputMapper.gson(1).fromJson(String.valueOf(obj.get("data")),
						ItemStockEntry[].class, 1);
				List<ItemStockEntry> itemStockEntryList = Arrays.asList(itemStockEntryArr);

				if (itemStockEntryList != null && itemStockEntryList.size() > 0) {

					for (ItemStockEntry itemStockEntry : itemStockEntryList) {
						ids.add(itemStockEntry.getItemStockEntryID());

						Long id = itemStockEntryRepo.searchBySyncFacilityIDAndVanSerialNo(
								itemStockEntry.getSyncFacilityID(), itemStockEntry.getVanSerialNo());
						if (id != null)
							itemStockEntry.setItemStockEntryID(id);
						else
							itemStockEntry.setItemStockEntryID(null);

						itemStockEntry.setProcessed("P");
					}
					itemStockEntryRepo.saveAll(itemStockEntryList);

					int updateFlag = updateProcessedFlagToCentral("db_iemr", "t_itemstockentry", ids,
							ServerAuthorization);
				}
				break;
			}
			default:

			}

			i++;
		}
		return 1;

	}

	private JSONObject downloadDataFromCentral(String schemaName, String tableName, int vanID,
			String ServerAuthorization, String token) throws Exception {

		Integer facilityID = masterVanRepo.getFacilityID(vanID);
		if (facilityID != null) {

			RestTemplate restTemplate = new RestTemplate();
			SyncUploadDataDigester syncUploadDataDigester = new SyncUploadDataDigester(schemaName, tableName,
					facilityID);

			HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(syncUploadDataDigester, ServerAuthorization, token);
			// Call rest-template to call API to download master data for given table
			ResponseEntity<String> response = restTemplate.exchange(dataSyncTransactionDownloadUrl, HttpMethod.POST,
					request, String.class);
			// System.out.println("hello.....");

			JSONObject obj = new JSONObject(response.getBody());

			return obj;
//			Indent[] indent = InputMapper.gson(1).fromJson(String.valueOf(obj.get("data")), Indent[].class, 1);
//
//			return new Gson().toJson(Arrays.asList(indent));
		} else
			throw new Exception("Facility mapping for this van is either missing/wrong...");

	}

	private int updateProcessedFlagToCentral(String schemaName, String tableName, List<Long> ids,
			String ServerAuthorization, String token) throws Exception {

		int result = 0;
		RestTemplate restTemplate = new RestTemplate();
		SyncUploadDataDigester syncUploadDataDigester = new SyncUploadDataDigester(schemaName, tableName, ids);

		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(syncUploadDataDigester, ServerAuthorization, token);
		// Call rest-template to call API to download master data for given table
		ResponseEntity<String> response = restTemplate.exchange(dataSyncProcessedFlagUpdate, HttpMethod.POST, request,
				String.class);
		if (response != null && response.hasBody()) {
			JSONObject obj = new JSONObject(response.getBody());
			if (obj != null && obj.has("data") && obj.has("statusCode") && obj.getInt("statusCode") == 200)
				result = 1;

		}

		return result;

	}
}
