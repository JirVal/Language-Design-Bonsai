package uk.ac.derby.ldi.sili2.values;

import uk.ac.derby.ldi.sili2.interpreter.ExceptionSemantic;

public class ValueInteger extends ValueAbstract {

	private long internalValue;
	
	public ValueInteger(long b) {
		internalValue = b;
	}
	
	public String getName() {
		return "integer";
	}
	
	/** Convert this to a primitive long. */
	public long longValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		return (double)internalValue;
	}
	
	/** Convert this to a primitive String. */
	public String stringValue() {
		return "" + internalValue;
	}

	public int compare(Value v) {
		if (internalValue == v.longValue())
			return 0;
		else if (internalValue > v.longValue())
			return 1;
		else
			return -1;
	}
	
	public Value add(Value v) {
		return new ValueInteger(internalValue + v.longValue());
	}

	public Value subtract(Value v) {
		return new ValueInteger(internalValue - v.longValue());
	}

	public Value mult(Value v) {
		return new ValueInteger(internalValue * v.longValue());
	}

	public Value div(Value v) {
		return new ValueInteger(internalValue / v.longValue());
	}

	public Value unary_plus() {
		return new ValueInteger(internalValue);
	}

	public Value unary_minus() {
		return new ValueInteger(-internalValue);
	}
	
	public String toString() {
		return "" + internalValue;
	}
	
	@Override
	public Value sqrt() {
		return new ValueRational(Math.sqrt(internalValue));
	}

	@Override
	public Value ceil() {
		return new ValueRational(Math.ceil(internalValue));
	}

	@Override
	public Value floor() {
		return new ValueRational(Math.floor(internalValue));
	}

	@Override
	public Value abs() {
		return new ValueRational(Math.abs(internalValue));
	}

	@Override
	public Value factorial() {
		if (internalValue > 10) {
			throw new ExceptionSemantic("Factorial number is too high. Only 0 - 5 numbers are supported.");
		}
		int res = 1, i;
        for (i=2; i<=internalValue; i++)
            res *= i;
		return new ValueRational(res);
	}

	@Override
	public Value power(Value v) {
		return new ValueRational(Math.pow(internalValue, v.longValue()));
	}

	@Override
	public Value sine() {
		return new ValueRational(Math.sin(internalValue));
	}

	@Override
	public Value cosine() {
		return new ValueRational(Math.cos(internalValue));
	}

	@Override
	public Value tangent() {
		return new ValueRational(Math.tan(internalValue));
	}

	@Override
	public Value degrees() {
		return new ValueRational(Math.toDegrees(internalValue));
	}

	@Override
	public Value radians() {
		return new ValueRational(Math.toRadians(internalValue));
	}

	@Override
	public Value pi() {
		return new ValueRational(Math.PI);
	}
}
