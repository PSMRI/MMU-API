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
package com.iemr.mmu.service.fileSync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.iemr.mmu.data.fileSync.ServerCredential;
import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.http.HttpUtils;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.response.OutputResponse;

@Service
@PropertySource("classpath:application.properties")
public class FileSyncServiceImpl implements FileSyncService{
	
	private final static Logger logger = LoggerFactory.getLogger(FileSyncServiceImpl.class);
	
	private static HttpUtils httpUtils = new HttpUtils();
	
	@Value("${serverIP}")
	private String serverIP;
	
	@Value("${serverDomain}")
	private String serverDomain;
	
	@Value("${serverUserName}")
	private String serverUserName;
	
	@Value("${serverPassword}")
	private String serverPassword;
	
	@Value("${getServerCredentialURL}")
	private String getServerCredentialURL;
	
	@Value("${localFolderToSync}")
	private String localFolderToSync;
	
	@Value("${serverFolder}")
	private String serverFolder;
	
	private static String fileSynclogspath = "./fileSynclogs";
	
	static
	{
		File f = new File(fileSynclogspath);
		System.out.println("Log Folder Path - "+ f.getAbsolutePath());
		if (!f.exists()) {
			f.mkdir();
			logger.info("Log Folder created for File Sync activity");
		}
	}

	@Override
	public String getServerCredential() {

		Map<String,String> map = new HashMap<String,String>();
		
		map.put("serverIP", serverIP);
		map.put("serverDomain", serverDomain);
		map.put("serverUserName", serverUserName);
		map.put("serverPassword", serverPassword);
		
		String jsonResult= new Gson().toJson(map);
		
		return jsonResult;
	}

	
	@Override
	public String syncFiles(String ServerAuthorization) throws IEMRException, IOException {
		logger.info("File Sync activity Started");
		HashMap<String, Object> header = new HashMap<>();
		if (ServerAuthorization != null)
		{
			header.put("Authorization", ServerAuthorization);
		}

		String result = httpUtils.get(getServerCredentialURL,header);
		
		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE)
		{
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		String data=  identityResponse.getData();
		ServerCredential serverCredential = InputMapper.gson().fromJson(data, ServerCredential.class);
		
		String IP=serverCredential.getServerIP();
		String Domain=serverCredential.getServerDomain();
		String UserName=serverCredential.getServerUserName();
		String Password=serverCredential.getServerPassword();
		
		
		String logFileName= "/fileSync.log." +System.currentTimeMillis();
		
		String createAccessCommand= "net use \\\\"+IP+"\\IPC$ /u:"+Domain+"\\"+UserName+" "+Password;
		String createCopyCommand= "robocopy "+localFolderToSync+" \\\\"+IP+"\\"+serverFolder+"  /E  /R:3 /log+:" + fileSynclogspath + logFileName;
		String deleteAccessCommand= "net use \\\\"+IP+"\\IPC$ /delete";
		
		logger.info("fileSync createAccessCommand - >"+ createAccessCommand);
		logger.info("fileSync createCopyCommand - >"  + createCopyCommand);
		logger.info("fileSync deleteAccessCommand - >"+ deleteAccessCommand);
		
		Runtime rt = Runtime.getRuntime();
		FileInputStream fstream = null;
		 
		try 
		{
			Process pr1= rt.exec(createAccessCommand);
			Process pr2= rt.exec(createCopyCommand);
			Process pr3= rt.exec(deleteAccessCommand);
			
			logger.info(System.getProperty("user.dir"));
			
			try {
				 logger.info("File Sync activity sleeping");
				Thread.sleep(35000);
				 logger.info("File Sync activity wakeup");
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				 Thread.currentThread().interrupt();
			}
			
			   fstream = new FileInputStream(fileSynclogspath.substring(2) + logFileName);
		
			    try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
			   
			   String strLine;
			   /* read log line by line */
			   while ((strLine = br.readLine()) != null)   {
			     /* parse strLine to obtain what you want to read*/
			     if(strLine.contains("ERROR"))
			     {
			    	 pr1.destroy();
			    	 pr2.destroy();
			    	 pr3.destroy();
			    	 
			    	 logger.info("File Sync activity Failed. Log File Name - "+logFileName.substring(1));
			    	 return "File Sync activity Failed. Log File Name - "+logFileName.substring(1);
			     }
			    
			   }
			  
			 }
		    	 pr1.destroy();
		    	 pr2.destroy();
		    	 pr3.destroy();
		} 
		catch (IOException e) {
			logger.error(e.getMessage());
			logger.info("File Sync activity Failed. Log File Name - "+logFileName.substring(1));
			return "File Sync activity Failed. Log File Name - "+logFileName.substring(1);
		}finally {
			if (fstream != null)
				 fstream.close();
		
		}
		
		logger.info("File Sync activity Completed");
		return "File Sync activity Completed";
	}
	
	private void sleepProcess(Process bol)
	{
		//File f = new File(fileSynclogspath.substring(2) + logFileName);
		//System.out.println("Log Folder Path - "+ f.getAbsolutePath());
		while (bol.isAlive()) {
			
			try {
				 logger.info("File Sync activity sleeping");
				Thread.sleep(1000);
				 logger.info("File Sync activity wakeup");
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				 Thread.currentThread().interrupt();
			}
		}
	}
}
