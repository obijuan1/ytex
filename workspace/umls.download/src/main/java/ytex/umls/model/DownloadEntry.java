package ytex.umls.model;


/*
 * Copyright 2010-2011 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.directwebremoting.annotations.RemoteProperty;
import org.directwebremoting.annotations.RemoteProxy;

/**
 * The entry class maps to a single journal entry. 
 */
@Entity
@RemoteProxy
public class DownloadEntry {
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	@RemoteProperty
	private String id;

	@RemoteProperty
	private String username;

	@RemoteProperty
	private String version;
	
	@RemoteProperty
	private String platform;
	
	private Date date;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}



	@RemoteProperty
	private String formattedDate;

	private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
		this.formattedDate = formatter.format(date);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@Transient
	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	

	@Transient
	public SimpleDateFormat getFormatter() {
		return formatter;
	}

}
