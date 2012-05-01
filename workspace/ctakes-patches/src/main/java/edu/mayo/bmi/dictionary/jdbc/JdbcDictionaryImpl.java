/*
 * Copyright: (c) 2009   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package edu.mayo.bmi.dictionary.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.mayo.bmi.dictionary.BaseDictionaryImpl;
import edu.mayo.bmi.dictionary.Dictionary;
import edu.mayo.bmi.dictionary.DictionaryException;
import edu.mayo.bmi.dictionary.GenericMetaDataHitImpl;
import edu.mayo.bmi.dictionary.MetaDataHit;

/**
 * 
 * @author Mayo Clinic
 * @author vijay modified to implement caching
 */
public class JdbcDictionaryImpl extends BaseDictionaryImpl implements
		Dictionary {
	private Connection iv_dbConn;
	private String iv_tableName;
	private String iv_lookupFieldName;
	private PreparedStatement iv_mdPrepStmt;
	private PreparedStatement iv_cntPrepStmt;
	private Cache cache;

	public JdbcDictionaryImpl(Connection conn, String tableName,
			String lookupFieldName) {
		iv_dbConn = conn;
		iv_tableName = tableName;
		iv_lookupFieldName = lookupFieldName;
		cache = CacheManager.getInstance().getCache("jdbcDictionaryCache");
	}

	private PreparedStatement initCountPrepStmt(String text)
			throws SQLException {
		if (iv_cntPrepStmt == null) {
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT COUNT(*) ");

			sb.append(" FROM ");
			sb.append(iv_tableName);

			sb.append(" WHERE ");
			sb.append(iv_lookupFieldName);
			sb.append(" = ?");

			iv_cntPrepStmt = iv_dbConn.prepareStatement(sb.toString());
		}

		iv_cntPrepStmt.clearParameters();
		iv_cntPrepStmt.setString(1, text);

		return iv_cntPrepStmt;
	}

	private PreparedStatement initMetaDataPrepStmt(String text)
			throws SQLException {
		if (iv_mdPrepStmt == null) {
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT ");

			// translate meta data field names into columns
			// to be returned in the result set
			Iterator metaFieldNameItr = getMetaFieldNames();
			while (metaFieldNameItr.hasNext()) {
				String mdFieldName = (String) metaFieldNameItr.next();
				sb.append(mdFieldName);
				sb.append(',');
			}
			// chomp off the last comma
			sb.deleteCharAt(sb.length() - 1);

			sb.append(" FROM ");
			sb.append(iv_tableName);

			sb.append(" WHERE ");
			sb.append(iv_lookupFieldName);
			sb.append(" = ?");

			iv_mdPrepStmt = iv_dbConn.prepareStatement(sb.toString());
		}

		iv_mdPrepStmt.clearParameters();
		iv_mdPrepStmt.setString(1, text);

		return iv_mdPrepStmt;
	}

	public boolean contains(String text) throws DictionaryException {
		try {
			PreparedStatement prepStmt = initCountPrepStmt(text);
			ResultSet rs = prepStmt.executeQuery();

			rs.next();
			int count = rs.getInt(1);
			if (count > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			throw new DictionaryException(e);
		}
	}

	/**
	 * vng changes - use cache, use immutable collections to save memory. use
	 * ImmutableList because there is no implementation of equals on the
	 * MetaDataHit
	 */
	@SuppressWarnings("unchecked")
	public Collection getEntries(String str) throws DictionaryException {
		// Set metaDataHitSet = new HashSet();
		// check cache
		if (cache != null) {
			Element e = cache.get(str);
			if (e != null) {
				return (Collection) e.getObjectValue();
			}
		}
		try {
			ImmutableList.Builder<MetaDataHit> b = new ImmutableList.Builder<MetaDataHit>();
			PreparedStatement prepStmt = initMetaDataPrepStmt(str);
			ResultSet rs = prepStmt.executeQuery();

			while (rs.next()) {
				// Map nameValMap = new HashMap();
				ImmutableMap.Builder<String, String> mb = new ImmutableMap.Builder<String, String>();
				Iterator<String> metaFieldNameItr = getMetaFieldNames();
				while (metaFieldNameItr.hasNext()) {
					String metaFieldName = metaFieldNameItr.next();
					String metaFieldValue = rs.getString(metaFieldName);
					// nameValMap.put(metaFieldName, metaFieldValue);
					mb.put(metaFieldName, metaFieldValue);
				}
				// MetaDataHit mdh = new GenericMetaDataHitImpl(nameValMap);
				// metaDataHitSet.add(mdh);
				b.add(new GenericMetaDataHitImpl(mb.build()));
			}
			List<MetaDataHit> metaDataHitSet = b.build();
			// add to cache
			if (cache != null) {
				cache.put(new Element(str, metaDataHitSet));
			}
			return metaDataHitSet;
		} catch (SQLException e) {
			throw new DictionaryException(e);
		}
	}
}
