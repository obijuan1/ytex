package ytex.umls.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.kernel.KernelContextHolder;
import ytex.umls.model.UmlsAuiFirstWord;

/**
 * setup umls_aui_fword table
 * @author vijay
 *
 */
public class SetupAuiFirstWord {
	private static final Pattern nonWord = Pattern.compile("\\W");

	/**
	 * get the first word from the string. done to recreate first words as in
	 * arc's umls_ms_2009 table. it doesn't really make sense for strings that
	 * start with non-word characters, but whatever.
	 * 
	 * @param str
	 *            full concept string
	 * @return first word
	 */
	public String getFirstWord(String str) {
		String firstWord = str;
		Matcher firstNonWordMatcher = nonWord.matcher(str);
		if (firstNonWordMatcher.find()) {
			int firstNonWord = firstNonWordMatcher.start();
			if (firstNonWord == 0) {
				// non-word token at beginning of string - this is the first
				// word
				firstWord = str.substring(0, 1);
			} else {
				firstWord = str.substring(0, firstNonWord);
			}
		}
		return firstWord.toLowerCase(Locale.ENGLISH);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SetupAuiFirstWord setupFword = new SetupAuiFirstWord();
		setupFword.setupAuiFirstWord();
	}
	
	public void setupAuiFirstWord() {
		UMLSDao umlsDao = KernelContextHolder.getApplicationContext().getBean(
				UMLSDao.class);
		TransactionTemplate t = new TransactionTemplate(KernelContextHolder
				.getApplicationContext().getBean(
						PlatformTransactionManager.class));
		t.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
		// delete all records
		umlsDao.deleteAuiFirstWord();
		// get all auis and their strings
		String lastAui = null;
		List<Object[]> listAuiStr = null;
		do {
			//get the next 10k auis
			listAuiStr = umlsDao.getAllAuiStr(lastAui);
			//put the aui - fword pairs in a list
			List<UmlsAuiFirstWord> listFword = new ArrayList<UmlsAuiFirstWord>(1000);
			for(Object[] auiStr : listAuiStr) {
				lastAui = (String)auiStr[0];
				listFword.add(new UmlsAuiFirstWord((String)auiStr[0], this.getFirstWord((String)auiStr[1])));
			}
			// batch insert
			if(listFword.size() > 0) {
				umlsDao.insertAuiFirstWord(listFword);
			}
		} while(listAuiStr.size() > 0);
	}
}
