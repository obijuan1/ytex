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
package ytex.umls.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import ytex.umls.model.DownloadEntry;

import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;

public class DownloadDAOSimpleDBImpl implements DownloadDAO {

	private static Map<String, String> properties = new HashMap<String, String>();
	// static {
	// properties.put("lobBucketName", S3StorageManager.getKey().toLowerCase() +
	// "-travellog-lob" + StageUtils.getResourceSuffixForCurrentStage());
	// }

	private static EntityManagerFactoryImpl factory = new EntityManagerFactoryImpl(
			"ytex-umlsdownload", properties);
	
	@Override
	public void saveDownloadEntry(String username, String version, String platform) {
		DownloadEntry de = new DownloadEntry();
		de.setUsername(username);
		de.setPlatform(platform);
		de.setVersion(version);
		de.setDate(new Date());
		this.saveDownloadEntry(de);
	}

	@Override
	public void saveDownloadEntry(DownloadEntry downloadEntry) {
		EntityManager em = null;
		// Storage fails if id is an empty string, so nullify it
		if (downloadEntry.getId() != null && downloadEntry.getId().equals("")) {
			downloadEntry.setId(null);
		}
		try {
			em = factory.createEntityManager();
			em.persist(downloadEntry);

		} finally {
			if (em != null) {
				em.close();
			}
		}
	}
}
