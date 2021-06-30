package uk.ac.derby.ldi.sili2.values;

import uk.ac.derby.ldi.sili2.interpreter.ExceptionSemantic;

public class ValueBoolean extends ValueAbstract {

	private boolean internalValue;
	
	public ValueBoolean(boolean b) {
		internalValue = b;
	}
	
	public String getName() {
		return "boolean";
	}
	
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive string. */
	public String stringValue() {
		return (internalValue) ? "true" : "false";
	}
	
	public Value or(Value v) {
		return new ValueBoolean(internalValue || v.booleanValue());
	}

	public Value and(Value v) {
		return new ValueBoolean(internalValue && v.booleanValue());
	}

	public Value not() {
		return new ValueBoolean(!internalValue);
	}

	public int compare(Value v) {
		if (internalValue == v.booleanValue())
			return 0;
		else if (internalValue)
			return 1;
		else
			return -1;
	}
	
	public String toString() {
		return "" + internalValue;
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
