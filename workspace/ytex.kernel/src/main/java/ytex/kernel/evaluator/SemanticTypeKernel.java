package ytex.kernel.evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * before comparing semantic distance, use this kernel to filter by semantic type
 * 
 * Modes: <li>MAINSUI (default): concept's main semantic types must overlap <li>TUI:
 * concept's TUIs must overlap
 * 
 * @author vijay
 * 
 */
public class SemanticTypeKernel implements Kernel {
	private static final Log log = LogFactory.getLog(SemanticTypeKernel.class);
	private static final String MAINSUI = "MAINSUI";
	private static final String TUI = "TUI";

	private SimpleJdbcTemplate simpleJdbcTemplate;
	private Map<String, Set<String>> cuiTuiMap;
	private Map<String, Set<Integer>> cuiMainSuiMap;
	private PlatformTransactionManager transactionManager;
	private DataSource dataSource;
	private String mode = "MAINSUI";

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public static int getMainSem(int sui) {
		switch (sui) {
		case 52:
		case 53:
		case 56:
		case 51:
		case 64:
		case 55:
		case 66:
		case 57:
		case 54:
			return 0;
		case 17:
		case 29:
		case 23:
		case 30:
		case 31:
		case 22:
		case 25:
		case 26:
		case 18:
		case 21:
		case 24:
			return 1;
		case 116:
		case 195:
		case 123:
		case 122:
		case 118:
		case 103:
		case 120:
		case 104:
		case 200:
		case 111:
		case 196:
		case 126:
		case 131:
		case 125:
		case 129:
		case 130:
		case 197:
		case 119:
		case 124:
		case 114:
		case 109:
		case 115:
		case 121:
		case 192:
		case 110:
		case 127:
			return 2;
		case 185:
		case 77:
		case 169:
		case 102:
		case 78:
		case 170:
		case 171:
		case 80:
		case 81:
		case 89:
		case 82:
		case 79:
			return 3;
		case 203:
		case 74:
		case 75:
			return 4;
		case 20:
		case 190:
		case 49:
		case 19:
		case 47:
		case 50:
		case 33:
		case 37:
		case 48:
		case 191:
		case 46:
		case 184:
			return 5;
		case 87:
		case 88:
		case 28:
		case 85:
		case 86:
			return 6;
		case 83:
			return 7;
		case 100:
		case 3:
		case 11:
		case 8:
		case 194:
		case 7:
		case 12:
		case 99:
		case 13:
		case 4:
		case 96:
		case 16:
		case 9:
		case 15:
		case 1:
		case 101:
		case 2:
		case 98:
		case 97:
		case 14:
		case 6:
		case 10:
		case 204: // vng missing sui
		case 5:
			return 8;
		case 71:
		case 168:
		case 73:
		case 72:
		case 167:
			return 9;
		case 91:
		case 90:
			return 10;
		case 93:
		case 92:
		case 94:
		case 95:
			return 11;
		case 38:
		case 69:
		case 68:
		case 34:
		case 70:
		case 67:
			return 12;
		case 43:
		case 201:
		case 45:
		case 41:
		case 44:
		case 42:
		case 32:
		case 40:
		case 39:
			return 13;
		case 60:
		case 65:
		case 58:
		case 59:
		case 63:
		case 62:
		case 61:
			return 14;
		default:
			break;
		}
		return -1;
	}

	public Set<Integer> tuiToMainSui(Set<String> tuis) {
		Set<Integer> mainSui = new HashSet<Integer>(tuis.size());
		for (String tui : tuis) {
			mainSui.add(getMainSem(Integer.parseInt(tui.substring(1))));
		}
		return mainSui;
	}

	public void initCuiTuiMap() {
		String query = "select m.cui, m.tui from umls.MRSTY m inner join (select distinct cui from suj_concept)s  on s.cui = m.cui";
		List<Map<String, Object>> results = simpleJdbcTemplate
				.queryForList(query);
		this.cuiTuiMap = new HashMap<String, Set<String>>();
		for (Map<String, Object> result : results) {
			String cui = (String) result.get("cui");
			String tui = (String) result.get("tui");
			Set<String> tuis = cuiTuiMap.get(cui);
			if (tuis == null) {
				tuis = new HashSet<String>();
				cuiTuiMap.put(cui, tuis);
			}
			tuis.add(tui);
		}
		this.cuiMainSuiMap = new HashMap<String, Set<Integer>>(cuiTuiMap.size());
		for (Map.Entry<String, Set<String>> cuiTui : cuiTuiMap.entrySet()) {
			cuiMainSuiMap.put(cuiTui.getKey(), tuiToMainSui(cuiTui.getValue()));
		}
	}

	public double evaluate(Object o1, Object o2) {
		if (this.getMode() == null || this.getMode().length() == 0
				|| MAINSUI.equals(this.getMode()))
			return mainSuiCheck(o1, o2);
		else if (TUI.equals(this.getMode()))
			return tuiCheck(o1, o2);
		else {
			log.error("invalid mode");
			throw new RuntimeException("invalid mode");
		}
	}

	private double tuiCheck(Object o1, Object o2) {
		Set<String> tuis1 = this.cuiTuiMap.get((String) o1);
		Set<String> tuis2 = this.cuiTuiMap.get((String) o2);
		if (tuis1 != null && tuis2 != null
				&& !Collections.disjoint(tuis1, tuis2)) {
			return 1;
		} else {
			return 0;
		}
	}

	private double mainSuiCheck(Object o1, Object o2) {
		Set<Integer> tuis1 = cuiMainSuiMap.get((String) o1);
		Set<Integer> tuis2 = cuiMainSuiMap.get((String) o2);
		// only compare the two if they have a common semantic type
		if (tuis1 != null && tuis2 != null
				&& !Collections.disjoint(tuis1, tuis2)) {
			return 1;
		} else {
			return 0;
		}
	}

	public void init() {
		TransactionTemplate t = new TransactionTemplate(this.transactionManager);
		t.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
		t.execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus arg0) {
				initCuiTuiMap();
				return null;
			}
		});
	}
}
