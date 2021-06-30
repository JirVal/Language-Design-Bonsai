package uk.ac.derby.ldi.sili2.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.derby.ldi.sili2.parser.ast.*;
import uk.ac.derby.ldi.sili2.values.*;

public class Parser implements SiliVisitor {
	
	// Scope display handler
	private Display scope = new Display();
	private HashMap<String, ArrayList<Value>> arrayStore = new HashMap<String, ArrayList<Value>>();
	
	// Get the ith child of a given node.
	private static SimpleNode getChild(SimpleNode node, int childIndex) {
		return (SimpleNode)node.jjtGetChild(childIndex);
	}
	
	// Get the token value of the ith child of a given node.
	private static String getTokenOfChild(SimpleNode node, int childIndex) {
		return getChild(node, childIndex).tokenValue;
	}
	
	// Execute a given child of the given node
	private Object doChild(SimpleNode node, int childIndex, Object data) {
		return node.jjtGetChild(childIndex).jjtAccept(this, data);
	}
	
	// Execute a given child of a given node, and return its value as a Value.
	// This is used by the expression evaluation nodes.
	Value doChild(SimpleNode node, int childIndex) {
		return (Value)doChild(node, childIndex, null);
	}
	
	// Execute all children of the given node
	Object doChildren(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}
	
	// Called if one of the following methods is missing...
	public Object visit(SimpleNode node, Object data) {
		System.out.println(node + ": acceptor not implemented in subclass?");
		return data;
	}
	
	// Execute a Sili program
	public Object visit(ASTCode node, Object data) {
		return doChildren(node, data);	
	}
	
	// Execute a statement
	public Object visit(ASTStatement node, Object data) {
		return doChildren(node, data);	
	}

	// Execute a block
	public Object visit(ASTBlock node, Object data) {
		return doChildren(node, data);	
	}

	// Function definition
	public Object visit(ASTFnDef node, Object data) {
		// Already defined?
		if (node.optimised != null)
			return data;
		// Child 0 - identifier (fn name)
		String fnname = getTokenOfChild(node, 0);
		if (scope.findFunctionInCurrentLevel(fnname) != null)
			throw new ExceptionSemantic("Function " + fnname + " already exists.");
		FunctionDefinition currentFunctionDefinition = new FunctionDefinition(fnname, scope.getLevel() + 1);
		// Child 1 - function definition parameter list
		doChild(node, 1, currentFunctionDefinition);
		// Add to available functions
		scope.addFunction(currentFunctionDefinition);
		// Child 2 - function body
		currentFunctionDefinition.setFunctionBody(getChild(node, 2));
		// Child 3 - optional return expression
		if (node.fnHasReturn)
			currentFunctionDefinition.setFunctionReturnExpression(getChild(node, 3));
		// Preserve this definition for future reference, and so we don't define
		// it every time this node is processed.
		node.optimised = currentFunctionDefinition;
		return data;
	}
	
	// Function definition parameter list
	public Object visit(ASTParmlist node, Object data) {
		FunctionDefinition currentDefinition = (FunctionDefinition)data;
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			currentDefinition.defineParameter(getTokenOfChild(node, i));
		return data;
	}
	
	// Function body
	public Object visit(ASTFnBody node, Object data) {
		return doChildren(node, data);
	}
	
	// Function return expression
	public Object visit(ASTReturnExpression node, Object data) {
		return doChildren(node, data);
	}
	
	// Function call
	public Object visit(ASTCall node, Object data) {
		FunctionDefinition fndef;
		if (node.optimised == null) { 
			// Child 0 - identifier (fn name)
			String fnname = getTokenOfChild(node, 0);
			fndef = scope.findFunction(fnname);
			if (fndef == null)
				throw new ExceptionSemantic("Function " + fnname + " is undefined.");
			// Save it for next time
			node.optimised = fndef;
		} else
			fndef = (FunctionDefinition)node.optimised;
		FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		// Child 1 - arglist
		doChild(node, 1, newInvocation);
		// Execute
		scope.execute(newInvocation, this);
		return data;
	}
	
	// Function invocation in an expression
	public Object visit(ASTFnInvoke node, Object data) {
		FunctionDefinition fndef;
		if (node.optimised == null) { 
			// Child 0 - identifier (fn name)
			String fnname = getTokenOfChild(node, 0);
			fndef = scope.findFunction(fnname);
			if (fndef == null)
				throw new ExceptionSemantic("Function " + fnname + " is undefined.");
			if (!fndef.hasReturn())
				throw new ExceptionSemantic("Function " + fnname + " is being invoked in an expression but does not have a return value.");
			// Save it for next time
			node.optimised = fndef;
		} else
			fndef = (FunctionDefinition)node.optimised;
		FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		// Child 1 - arglist
		doChild(node, 1, newInvocation);
		// Execute
		return scope.execute(newInvocation, this);
	}

	// Function invocation argument list.
	public Object visit(ASTArgList node, Object data) {
		FunctionInvocation newInvocation = (FunctionInvocation)data;
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			newInvocation.setArgument(doChild(node, i));
		newInvocation.checkArgumentCount();
		return data;
	}
	
	// Execute an IF 
	public Object visit(ASTIfStatement node, Object data) {
		// evaluate boolean expression
		Value hopefullyValueBoolean = doChild(node, 0);
		if (!(hopefullyValueBoolean instanceof ValueBoolean))
			throw new ExceptionSemantic("The test expression of an if statement must be boolean.");
		if (((ValueBoolean)hopefullyValueBoolean).booleanValue())
			doChild(node, 1);							// if(true), therefore do 'if' statement
		else if (node.ifHasElse)						// does it have an else statement?
			doChild(node, 2);							// if(false), therefore do 'else' statement
		return data;
	}
	
	// Execute a FOR loop
	public Object visit(ASTForLoop node, Object data) {
		// loop initialisation
		doChild(node, 0);
		while (true) {
			// evaluate loop test
			Value hopefullyValueBoolean = doChild(node, 1);
			if (!(hopefullyValueBoolean instanceof ValueBoolean))
				throw new ExceptionSemantic("The test expression of a for loop must be boolean.");
			if (!((ValueBoolean)hopefullyValueBoolean).booleanValue())
				break;
			// do loop statement
			doChild(node, 3);
			// assign loop increment
			doChild(node, 2);
		}
		return data;
	}
	
	// Process an identifier
	// This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
	public Object visit(ASTIdentifier node, Object data) {
		return data;
	}
	
	// Dereference a variable or parameter, and return its value.
	public Object visit(ASTDereference node, Object data) {
		Display.Reference reference;
		if (node.optimised == null) {
			String name = node.tokenValue;
			reference = scope.findReference(name);
			if (reference == null)
				throw new ExceptionSemantic("Variable or parameter " + name + " is undefined.");
			node.optimised = reference;
		} else
			reference = (Display.Reference)node.optimised;
		return reference.getValue();
	}
	
	// Execute an assignment statement.
	public Object visit(ASTAssignment node, Object data) {
		Display.Reference reference;
		if (node.optimised == null) {
			String name = getTokenOfChild(node, 0);
			reference = scope.findReference(name);
			if (reference == null)
				reference = scope.defineVariable(name);
			node.optimised = reference;
		} else
			reference = (Display.Reference)node.optimised;
		reference.setValue(doChild(node, 1));
		return data;
	}

	// OR
	public Object visit(ASTOr node, Object data) {
		return doChild(node, 0).or(doChild(node, 1));
	}

	// AND
	public Object visit(ASTAnd node, Object data) {
		return doChild(node, 0).and(doChild(node, 1));
	}

	// ==
	public Object visit(ASTCompEqual node, Object data) {
		return doChild(node, 0).eq(doChild(node, 1));
	}

	// !=
	public Object visit(ASTCompNequal node, Object data) {
		return doChild(node, 0).neq(doChild(node, 1));
	}

	// >=
	public Object visit(ASTCompGTE node, Object data) {
		return doChild(node, 0).gte(doChild(node, 1));
	}

	// <=
	public Object visit(ASTCompLTE node, Object data) {
		return doChild(node, 0).lte(doChild(node, 1));
	}

	// >
	public Object visit(ASTCompGT node, Object data) {
		return doChild(node, 0).gt(doChild(node, 1));
	}

	// <
	public Object visit(ASTCompLT node, Object data) {
		return doChild(node, 0).lt(doChild(node, 1));
	}

	// +
	public Object visit(ASTAdd node, Object data) {
		return doChild(node, 0).add(doChild(node, 1));
	}

	// -
	public Object visit(ASTSubtract node, Object data) {
		return doChild(node, 0).subtract(doChild(node, 1));
	}

	// *
	public Object visit(ASTTimes node, Object data) {
		return doChild(node, 0).mult(doChild(node, 1));
	}

	// /
	public Object visit(ASTDivide node, Object data) {
		return doChild(node, 0).div(doChild(node, 1));
	}

	// NOT
	public Object visit(ASTUnaryNot node, Object data) {
		return doChild(node, 0).not();
	}

	// + (unary)
	public Object visit(ASTUnaryPlus node, Object data) {
		return doChild(node, 0).unary_plus();
	}

	// - (unary)
	public Object visit(ASTUnaryMinus node, Object data) {
		return doChild(node, 0).unary_minus();
	}

	// Return string literal
	public Object visit(ASTCharacter node, Object data) {
		if (node.optimised == null)
			node.optimised = ValueString.stripDelimited(node.tokenValue);
		return node.optimised;
	}

	// Return integer literal
	public Object visit(ASTInteger node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueInteger(Long.parseLong(node.tokenValue));
		return node.optimised;
	}

	// Return floating point literal
	public Object visit(ASTRational node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueRational(Double.parseDouble(node.tokenValue));
		return node.optimised;
	}

	// Return true literal
	public Object visit(ASTTrue node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueBoolean(true);
		return node.optimised;
	}

	// Return false literal
	public Object visit(ASTFalse node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueBoolean(false);
		return node.optimised;
	}

	// While loop
	public Object visit(ASTWhileLoop node, Object data) {
		while (true) {
			Value hopefullyBoolean = doChild(node, 0);
			if (!(hopefullyBoolean instanceof ValueBoolean))
				throw new ExceptionSemantic("While loop expects boolean condition.");
			if (!((ValueBoolean) hopefullyBoolean).booleanValue())
				break;
			doChild(node, 1);
		}
		return data;
	}
		
	// Array definition
	public Object visit(ASTArrayDefine node, Object data) {
		if (node.optimised != null)
			return data;
		String identifier = getTokenOfChild(node, 0);
		if (arrayStore.get(identifier) != null)
			throw new ExceptionSemantic("Array " + identifier + " is already defined.");
		ArrayList<Value> array = new ArrayList<Value>();
		doChild(node, 1, array);
		arrayStore.put(identifier, array);
		return data;
	}

	// Array parameters
	public Object visit(ASTArrayParamList node, Object data) {
		@SuppressWarnings("unchecked")
		ArrayList<Value> arrayList = (ArrayList<Value>) data;	
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			arrayList.add(doChild(node, i));
		}
		return data;
	}
	
	// Array assignment
	public Object visit(ASTArrayAssignment node, Object data) {
		String identifier = getTokenOfChild(node, 0);
		ArrayList<Value> array = arrayStore.get(identifier);
		if (array == null) {
			throw new ExceptionSemantic("Array " + identifier + " is undefined.");
		}
		Value index = doChild(node, 1);
		if (!(index instanceof ValueInteger))
			throw new ExceptionSemantic("Array index must be integer value.");
		int indexValue = (int) index.longValue();
		if (indexValue >= array.size()) {
			throw new ExceptionSemantic("Array out of bounds " + indexValue);
		}
		array.set(indexValue, doChild(node, 2));
		return data;
	}

	// Array invocation
	public Object visit(ASTArrayInvoke node, Object data) {
		String identifier = getTokenOfChild(node, 0);
		ArrayList<Value> array = arrayStore.get(identifier);
		if (array == null) {
			throw new ExceptionSemantic("Array " + identifier + " is undefined.");
		}
		Value index = doChild(node, 1);
		if (!(index instanceof ValueInteger))
			throw new ExceptionSemantic("Array index must be integer value.");
		int indexValue = (int) index.longValue();

		if (indexValue >= array.size()) {
			throw new ExceptionSemantic("Array out of bounds " + indexValue);
		}
		Value element = array.get(indexValue);
		return element;
	}
	
	// Execute the PRINT statement
	public Object visit(ASTPrint node, Object data) {
		String identifier = getTokenOfChild(node, 0);
		ArrayList<Value> array = arrayStore.get(identifier);
		if (array == null) {
			System.out.println(doChild(node, 0));
		} else {
			int arraySize = array.size();
			String listValues = "[ ";
			for (int index = 0; index < arraySize; index++) {     				
				if (index == arraySize - 1) {
					listValues += array.get(index);       
				} else {				
					listValues += array.get(index) + ", ";       
				}
			}  
			System.out.println(listValues + " ]");      
		}
		return data;
	}
		
	// Terminate program
	public Object visit(ASTExit node, Object data) {
		System.out.println("Program has been terminated ...");
		System.exit(0);
		return true;
	}

	// Square Root
	public Object visit(ASTSquareRoot node, Object data) {		
		Value number = doChild(node, 0);
		return number.sqrt();
	}

	// Ceiling - Returns the smallest integer greater than or equal to x.
	public Object visit(ASTCeil node, Object data) {
		Value number = doChild(node, 0);
		return number.ceil();
	}

	// Floor - Returns the largest integer less than or equal to x
	public Object visit(ASTFloor node, Object data) {
		Value number = doChild(node, 0);
		return number.floor();
	}
	
	// Absolute Value
	public Object visit(ASTAbsoluteValue node, Object data) {
		Value number = doChild(node, 0);
		return number.abs();
	}

	// Factorial - Returns the factorial of x
	public Object visit(ASTFactorial node, Object data) {
		Value number = doChild(node, 0);
		return number.factorial();
	}

	// Power - Returns x raised to the power y
	public Object visit(ASTPower node, Object data) {
		Value x = doChild(node, 0);
		Value y = doChild(node, 1);
		return x.power(y);
	}

	// Sine - Returns the sine of x
	public Object visit(ASTSine node, Object data) {
		Value x = doChild(node, 0);
		return x.sine();
	}

	// Cosine - Returns the cosine of x
	public Object visit(ASTCosine node, Object data) {
		Value x = doChild(node, 0);
		return x.cosine();
	}

	// Tangent - Returns the tangent of x
	public Object visit(ASTTangent node, Object data) {
		Value x = doChild(node, 0);
		return x.tangent();
	}

	// Degrees - Converts angle x from radians to degrees
	public Object visit(ASTDegrees node, Object data) {
		Value number = doChild(node, 0);
		return number.degrees();
	}

	// Radians - Converts angle x from degrees to radians
	public Object visit(ASTRadians node, Object data) {
		Value number = doChild(node, 0);
		return number.radians();
	}

	// PI - Mathematical constant, the ratio of circumference of a circle to it's diameter (3.14159...)
	public Object visit(ASTPi node, Object data) {
		Value pi = new ValueRational(0);
		return pi.pi();
	}
}
