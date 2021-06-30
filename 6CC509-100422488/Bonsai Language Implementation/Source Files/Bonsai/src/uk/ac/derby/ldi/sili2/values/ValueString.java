package uk.ac.derby.ldi.sili2.values;

import uk.ac.derby.ldi.sili2.interpreter.ExceptionSemantic;

public class ValueString extends ValueAbstract {
	
	private String internalValue;
	
	/** Return a ValueString given a quote-delimited source string. */
	public static ValueString stripDelimited(String b) {
		return new ValueString(b.substring(1, b.length() - 1));
	}
	
	public ValueString(String b) {
		internalValue = b;
	}
	
	public String getName() {
		return "string";
	}
	
	/** Convert this to a String. */
	public String stringValue() {
		return internalValue;		
	}

	public int compare(Value v) {
		return internalValue.compareTo(v.stringValue());
	}
	
	/** Add performs string concatenation. */
	public Value add(Value v) {
		return new ValueString(internalValue + v.stringValue());
	}
	
	public String toString() {
		return internalValue;
	}
	
	private Value invalid() {
		throw new ExceptionSemantic("Cannot perform arithmetic on boolean values.");		
	}
	
	@Override
	public Value sqrt() {
		return invalid();
	}

	@Override
	public Value ceil() {
		return invalid();
	}

	@Override
	public Value floor() {
		return invalid();
	}

	@Override
	public Value abs() {
		return invalid();
	}

	@Override
	public Value factorial() {
		return invalid();
	}

	@Override
	public Value power(Value v) {
		return invalid();
	}

	@Override
	public Value sine() {
		return invalid();
	}

	@Override
	public Value cosine() {
		return invalid();
	}

	@Override
	public Value tangent() {
		return invalid();
	}

	@Override
	public Value degrees() {
		return invalid();
	}

	@Override
	public Value radians() {
		return invalid();
	}

	@Override
	public Value pi() {
		return invalid();
	}
}
