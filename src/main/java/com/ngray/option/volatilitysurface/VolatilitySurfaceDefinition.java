package com.ngray.option.volatilitysurface;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

import com.ngray.option.financialinstrument.EuropeanOption;

/**
 * Class representing a volatility surface definition
 * @author nigelgray
 *
 */
public class VolatilitySurfaceDefinition {
	
	public enum Field {
		NAME("Name"),
		VALID_FROM("ValidFrom"),
		VALID_TO("ValidTo"),
		OPTION_EPIC_FORM("OptionEpicForm"),
		UNDERLYINGS("Underlyings"),
		MONEYNESS("Moneyness"),
		CALL_OR_PUT("CallOrPut");
		
		private String field;
		 
		private Field(String field) {
			this.field = field;
		}
		
		@Override
		public String toString() {
			return field;
		}
	};
	
	/**
	 * The name for this definition
	 */
	private final String name;
	
	/**
	 * This definition is valid inclusive-from the given date
	 */
	private final LocalDate validFrom;
	
	/**
	 * This definition is valid inclusive-to the given date
	 */
	private final LocalDate validTo;
	
	/**
	 * The form used to construct  option epics eg. OP.FTSE2.{STRIKE}{C|P}.IP for the given underlying
	 * map of underlying epic to option epic form
	 */
	private final Map<String, String> optionEpicForm;
	
	/**
	 * The list of underlying epics in the definition
	 */
	private final List<String> underlyingEpics;
	
	/**
	 * List of option strikes expressed as ratio to ATM strike (1.0 is ATM) 
	 */
	private final List<Double> moneyness;
	
	/**
	 * Build with calls or puts
	 */
	private final EuropeanOption.Type callOrPut;
	
	/**
	 * Constructor
	 */
	public VolatilitySurfaceDefinition(
			String name,
			LocalDate validFrom,
			LocalDate validTo,
			Map<String, String> optionEpicForm,
			List<String> underlyings,
			List<Double> moneyness,
			EuropeanOption.Type callOrPut
			) {
		
		this.name = name;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.optionEpicForm = new HashMap<>(optionEpicForm);
		this.underlyingEpics = new ArrayList<>(underlyings);
		this.moneyness = new ArrayList<>(moneyness);
		this.callOrPut = callOrPut;
	}

	// Happy that underlyings will always be a list of strings
	// A cast exception is acceptable if we end up passing another type by accident
	@SuppressWarnings("unchecked")
	public static VolatilitySurfaceDefinition fromBsonDocument(Document document) {
		String name = null;
		if (document.containsKey(Field.NAME.toString())) {
			name = (String)document.get(Field.NAME.toString());
		}
		LocalDate validFrom = null;
		if (document.containsKey(Field.VALID_FROM.toString())) {
			validFrom = LocalDate.parse((String)document.get(Field.VALID_FROM.toString()));
		}
		
		// ValidTo can be null if this is the currently valid definition
		LocalDate validTo = null;
		if (document.containsKey(Field.VALID_TO.toString()) && document.get(Field.VALID_TO.toString()) != null) {
			validTo = LocalDate.parse((String)document.get(Field.VALID_TO.toString()));
		}
		Map<String, String> optionEpicForm = null;
		if (document.containsKey(Field.OPTION_EPIC_FORM.toString())) {
			optionEpicForm = (Map<String, String>)document.get(Field.OPTION_EPIC_FORM.toString());
		}
		List<String> underlyings = null;
		if (document.containsKey(Field.UNDERLYINGS.toString())) {
				underlyings = (List<String>)document.get(Field.UNDERLYINGS.toString());
		}
		List<Double> moneyness = null;
		if (document.containsKey(Field.MONEYNESS.toString())) {
				moneyness = (List<Double>)document.get(Field.MONEYNESS.toString());
		}
		EuropeanOption.Type callOrPut = null;
		if (document.containsKey(Field.CALL_OR_PUT.toString())) {
			callOrPut = document.get(Field.CALL_OR_PUT.toString()).equals("PUT")? EuropeanOption.Type.PUT : EuropeanOption.Type.CALL;
		}
		
		return new VolatilitySurfaceDefinition(
				name,
				validFrom,
				validTo,
				optionEpicForm,
				underlyings,
				moneyness,
				callOrPut
				);
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public String getOptionEpicForm(String underlyingEpic) {
		return optionEpicForm.get(underlyingEpic);
	}

	public List<String> getUnderlyingEpics() {
		return Collections.unmodifiableList(underlyingEpics);
	}

	public List<Double> getMoneyness() {
		return Collections.unmodifiableList(moneyness);
	}

	public EuropeanOption.Type getCallOrPut() {
		return callOrPut;
	}

	public String getName() {
		return name;
	}
}
