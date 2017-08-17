package com.ngray.option.mongo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ngray.option.financialinstrument.EuropeanOption;

/**
 * Class representing a volatility surface definition
 * @author nigelgray
 *
 */
public class VolatilitySurfaceDefinition implements MongoObject{
	
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
	 * List of option strikes expressed as offset from ATM strike 
	 */
	private final List<Double> strikeOffsets;
	
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
			List<Double> strikeOffsets,
			EuropeanOption.Type callOrPut
			) {
		
		this.name = name;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.optionEpicForm = new HashMap<>(optionEpicForm);
		this.underlyingEpics = new ArrayList<>(underlyings);
		this.strikeOffsets = new ArrayList<>(strikeOffsets);
		this.callOrPut = callOrPut;
	}
	

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public String getOptionEpicForm(String underlyingEpic) {
		// optionEpic map is keyed by the index of the underlyingEpic in the underlyingEpic list
		// due to mongo restrictions on dots in keys
		return optionEpicForm.get(Integer.toString(underlyingEpics.indexOf(underlyingEpic)));
	}

	public List<String> getUnderlyingEpics() {
		return Collections.unmodifiableList(underlyingEpics);
	}

	public List<Double> getStrikeOffsets() {
		return Collections.unmodifiableList(strikeOffsets);
	}

	public EuropeanOption.Type getCallOrPut() {
		return callOrPut;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getUniqueId();
	}


	@Override
	public String getUniqueId() {
		return name + ":" + validFrom + ":" + validTo;
	}
	
	@Override
	public String getCollectionName() {
		return MongoConstants.VOLATILITY_SURFACE_DEFINITION_COLLECTION;
	}
}
