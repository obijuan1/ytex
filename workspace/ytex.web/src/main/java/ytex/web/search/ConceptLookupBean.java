package ytex.web.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * JSF Backing Bean for semanticSearch.jspx. Search for documents based on
 * concept ids, negation status, patient, and date. Based on the IceFaces
 * autocomplete example.
 * 
 * @author vijay
 * 
 */
public class ConceptLookupBean {
	private static final Log log = LogFactory.getLog(ConceptLookupBean.class);

	private UMLSFirstWordService umlsFirstWordService;

	public UMLSFirstWordService getUmlsFirstWordService() {
		return umlsFirstWordService;
	}

	public void setUmlsFirstWordService(
			UMLSFirstWordService umlsFirstWordService) {
		this.umlsFirstWordService = umlsFirstWordService;
	}

	public UMLSFirstWord getSearchCUI() {
		return searchCUI;
	}

	public void setSearchCUI(UMLSFirstWord searchCUI) {
		this.searchCUI = searchCUI;
	}

	// default city, no value.
	private UMLSFirstWord currentCUI = new UMLSFirstWord();

	private UMLSFirstWord searchCUI = new UMLSFirstWord();

	// list of possible matches.
	private List<SelectItem> matchesList = new ArrayList<SelectItem>();

	public void resetListen(ActionEvent event) {
		this.matchesList.clear();
		this.searchCUI = null;
	}

	/**
	 * Called when a user has modifed the SelectInputText value. This method
	 * call causes the match list to be updated.
	 * 
	 * @param event
	 */
	public void updateList(ValueChangeEvent event) {

		// get a new list of matches.
		setMatches(event);

		// Get the auto complete component from the event and assing
		if (event.getComponent() instanceof SelectInputText) {
			SelectInputText autoComplete = (SelectInputText) event
					.getComponent();
			// if no selected item then return the previously selected item.
			if (autoComplete.getSelectedItem() != null) {
				currentCUI = (UMLSFirstWord) autoComplete.getSelectedItem()
						.getValue();
			}
			// otherwise if there is a selected item get the value from the
			// match list
			else {
				UMLSFirstWord tempCUI = getMatch(autoComplete.getValue()
						.toString());
				if (tempCUI != null) {
					currentCUI = tempCUI;
				}
			}
		}
	}

	/**
	 * Gets the currently selected city.
	 * 
	 * @return selected city.
	 */
	public UMLSFirstWord getCurrentCUI() {
		return currentCUI;
	}

	/**
	 * The list of possible matches for the given SelectInputText value
	 * 
	 * @return list of possible matches.
	 */
	public List<SelectItem> getList() {
		return matchesList;
	}

	public static String formatUMLSFirstWord(UMLSFirstWord fword) {
		return fword.getText() + " [" + fword.getCui() + ']';
	}

	public static UMLSFirstWord extractUMLSFirstWord(String fword) {
		String tokens[] = fword.split("[|]");
		UMLSFirstWord umlsFWord = new UMLSFirstWord();
		// last token is cui
		if (tokens.length > 1) {
			String cui = tokens[tokens.length - 1];
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < tokens.length - 1; i++) {
				builder.append(tokens[i]);
			}
			String text = builder.toString();
			umlsFWord.setCui(cui);
			umlsFWord.setText(text);
		}
		return umlsFWord;
	}

	public static Comparator<Object> umlsFirstWordComparator = new Comparator<Object>() {

		// compare method for city entries.
		public int compare(Object o1, Object o2) {
			String s1;
			String s2;

			if (o1 instanceof UMLSFirstWord) {
				s1 = formatUMLSFirstWord((UMLSFirstWord) o1);
			} else {
				s1 = o1.toString();
			}

			if (o2 instanceof UMLSFirstWord) {
				s2 = formatUMLSFirstWord((UMLSFirstWord) o2);
			} else {
				s2 = o2.toString();
			}
			return s1.compareTo(s2);
		}
	};

	private UMLSFirstWord getMatch(String value) {
		UMLSFirstWord result = null;
		if (matchesList != null) {
			SelectItem si;
			Iterator<SelectItem> iter = matchesList.iterator();
			while (iter.hasNext()) {
				si = iter.next();
				if (value.equals(si.getLabel())) {
					return (UMLSFirstWord) si.getValue();
				}
			}
		}
		return result;
	}

	/**
	 * Utility method for building the match list given the current value of the
	 * SelectInputText component.
	 * 
	 * @param event
	 */
	private void setMatches(ValueChangeEvent event) {

		Object searchWord = event.getNewValue();
		String searchString;
		if (searchWord instanceof SelectItem) {
			searchString = ((SelectItem) searchWord).getLabel();
		} else {
			searchString = searchWord.toString();
		}
		if (searchString != null && searchString.length() > 2) {
			List<UMLSFirstWord> cuis = this.umlsFirstWordService
					.getUMLSbyFirstWord(searchString);
			this.matchesList = new ArrayList<SelectItem>(cuis.size());
			for (UMLSFirstWord cui : cuis) {
				this.matchesList.add(new SelectItem(cui,
						formatUMLSFirstWord(cui)));
			}
		}
	}

}
