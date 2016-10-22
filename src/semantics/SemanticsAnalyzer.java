package semantics;

import java.util.ArrayList;
import java.util.List;

public class SemanticsAnalyzer {

	private StringBuilder genCode = new StringBuilder();
	private StringBuilder funcParamBuilder = new StringBuilder();
	private StringBuilder variablesBuilder = new StringBuilder();
	private StringBuilder constantBuilder = new StringBuilder();
	private TokenStack tokenStack = new TokenStack();
	private TokenStack tempTokenStack = new TokenStack();
	private List<Variable> variableList = new ArrayList<>();
	private List<String> constantList = new ArrayList<>();
	private String currentContext = "gbl";
	private List<String> contextStack = new ArrayList<>();
	private List<String> functionNameStack = new ArrayList<>();
	private List<String> flowStack = new ArrayList<>();
	private List<String> subCallStack = new ArrayList<>();
	private List<Integer> paramStackExp = new ArrayList<>();
	private List<TokenStack> tempParamTokenStack = new ArrayList<>();

	private AssignmentStack assignmentStack = new AssignmentStack();

	private List<Integer> counterStack = new ArrayList<>();
	
	private int counter = 0;
	private int counterOperators = 0;
	private int paramCounter = 0;
	private int expressionCounter = 0;

	public SemanticsAnalyzer() {
		constantList.add("const_0");
		writeToConstFormatted("0");
	}

	public void performOperation(TransitionInfo info) {
		// if (semanticOperation.equals("subVardecl")) {
		// createVariable();
		// }
		//
		// if(semanticOperation.equals("begin")) {
		// writeVariables();
		// }

		// Declaracao de nova variavel
		// if(info.goingToMachine.equals("subProgram") &&
		// info.goingToState.equals("7") &&
		// info.comingFromMachine.equals("subVardecl")) {

		// Declaracao de nova variavel
		if (info.goingToMachine.equals("subVardecl") && info.goingToState.equals("5")
				&& info.comingFromMachine.equals("subVardecl")) {
			System.out.println("subVarDecl");
			createVariable();

			// Inicio da rotina principal
		} else if (info.goingToMachine.equals("subProgram") && info.goingToState.equals("11")) {
			System.out.println("subProgram");
			//writeGlobalVariables();
			// writeCode();

			currentContext = "bgn";

			// Declaracao de funcao
		} else if (info.goingToMachine.equals("subFuncdecl") && info.goingToState.equals("11")
				&& info.comingFromMachine.equals("subFuncdecl")) {
			System.out.println("subFuncDecl");

			createFuncDecl();
			/*
			 * } else if(info.comingFromMachine.equals("subExpression") &&
			 * info.comingFromState.equals("12") &&
			 * !info.goingToMachine.equals("subExpression")) {
			 * solveExpression();
			 */

			// Retorno de funcao
		} else if (info.comingFromMachine.equals("subFuncBlock") && info.comingFromState.equals("7")) {

			returnFunction();

			// Saida de funcao
		} else if (info.comingFromMachine.equals("subFuncBlock") && info.comingFromState.equals("9")) {
			exitFunction();

			// Expressao inicio
		} else if (info.comingFromMachine.equals("subExpression") && info.comingFromState.equals("4")) {
			System.out.println("subExp start");
			treatExpressionStart();

			// Expressao fim
		} else if (info.comingFromMachine.equals("subExpression") && info.comingFromState.equals("12")
				&& !info.goingToMachine.equals("subExpression")) {
			System.out.println("subExp end");
			treatExpressionFinal();

			/*
			 * } else if(info.comingFromMachine.equals("subCommand") &&
			 * info.comingFromState.equals("2")) {
			 * 
			 * //assignmentFunction();
			 * 
			 * }
			 */

		// Inicio assignment
		} else if (info.comingFromMachine.equals("subAssignment") && info.goingToState.equals("3")) {
			assignmentFunctionStart();
			System.out.println("subAssignment Start");

		// Fim assignment
		} else if (info.comingFromMachine.equals("subAssignment") && info.comingFromState.equals("9")) {
			assignmentFunctionEnd();
			System.out.println("subAssignment End");

		// Condicional THEN
		} else if (info.comingFromMachine.equals("subConditional") && info.comingFromState.equals("4")) {
			startIF();

		// Condicional ELSE
		} else if (info.comingFromMachine.equals("subConditional") && info.comingFromState.equals("11")
				&& info.goingToState.equals("11")) {
			escreveElse();

		// END IF
		} else if (info.comingFromMachine.equals("subConditional") && info.comingFromState.equals("12")) {
			finalizaIF();
			
		// Operador de comparacao
		} else if (info.comingFromMachine.equals("subRelation") && info.comingFromState.equals("7")) {
			colocaOperador();
			
			//Inicio While
		} else if(info.comingFromMachine.equals("subCommand") && info.comingFromState.equals("21")) {
			startWhile();
			
			//Meio While
		} else if(info.comingFromMachine.equals("subLoop") && info.comingFromState.equals("4")) {
			meioWhile();
			
			//Finaliza While
		} else if(info.comingFromMachine.equals("subLoop") && info.comingFromState.equals("8")) {
			finalizaWhile();
			
			//Chamada de funcao inicio
		} else if(info.comingFromMachine.equals("subSubcall") && info.comingFromState.equals("2")) {
			colocaSubRotinaNoStack();
			
			
			//Chamada de funcao, multiplos parametros
		} else if(info.comingFromMachine.equals("subSubcall") && info.comingFromState.equals("7")) {
			multiplosParametros();
			
			
		//Chamada de funcao fim	
		} else if(info.comingFromMachine.equals("subSubcall") && info.comingFromState.equals("9")) {
			chamaSubRotina();
		}

		//System.out.println(info.toString());

	}

	private void multiplosParametros() {
		System.out.println("\n\n====MULTIPLOS PARAMETROS");
		tokenStack.printStack();
		System.out.println("==================\n\n");
		
		String stackTop = tokenStack.pop();
		tokenStack.pop();
		tokenStack.clear();
		tokenStack.push(stackTop);		
		paramStackExp.add(0);
		paramCounter++;
	}

	private void colocaSubRotinaNoStack() {
		System.out.println("=============Subrotina start");
		tokenStack.printStack();
		System.out.println("==================");
		
		String stackTop = tokenStack.pop();
		
		tokenStack.pop();
		subCallStack.add("sub_" + tokenStack.pop());
		
		tokenStack.clear();
		tokenStack.push(stackTop);
		
		if(!stackTop.equals("("))
			paramCounter++;
		
		paramStackExp.add(0);
		
	}

	private void chamaSubRotina() {
		System.out.println("=============Subrotina end");
		tokenStack.printStack();
		System.out.println("==================");
		
		if(paramCounter  > 1) {
			writeToGenFormatted("", "", "", "");
			for(int i = paramCounter; i > 0; i--) {
				writeToGenFormatted("", "SC", "POP", "Retira da pilha original");
				writeToGenFormatted("", "SC", "PARAM_PUSH", "Coloca na pilha de parametros");
			}
			
			writeToGenFormatted("", "", "", "");
			
			paramCounter = 0;
		}
		
		String funcName = subCallStack.remove(subCallStack.size() - 1);
		writeToGenFormatted("", "SC", funcName, "Chama funcao " + funcName);
		
		String stackTop = tokenStack.pop();
		tokenStack.clear();
		tokenStack.push(stackTop);
		
		paramStackExp.clear();
		tempParamTokenStack.clear();
		
	}

	private void startWhile() {
		writeToGenFormatted("start_while" + ++counter, "+", "const_0", "Inicia While");
		counterStack.add(counter);
	}

	private void finalizaWhile() {
		System.out.println("Finaliza While");
		int count = counterStack.remove(counterStack.size() - 1);
		writeToGenFormatted("", "JP", "start_while" + count, "Volta para o loop");
		writeToGenFormatted("end_while_" + count, "+", "const_0", "Termina While");
		
		String stackTop = tokenStack.pop();
		tokenStack.clear();
		tokenStack.push(stackTop);

		
	}

	private void meioWhile() {
		String stackTop = tokenStack.pop();
		tokenStack.clear();
		tokenStack.push(stackTop);
		
		String operator = flowStack.remove(flowStack.size() - 1);
		escreveOperador(operator);
		
		int count = counterStack.get(counterStack.size() - 1);
		
		
		writeToGenFormatted("", "SC", "POP", "Busca resultado da comparacao");
		writeToGenFormatted("", "JZ", "end_while_" + count, "Vai para final do while");
		
	}

	private void colocaOperador() {
		String topOfStack = tokenStack.pop();
		
		flowStack.add(tokenStack.pop());
		
		
		tokenStack.clear();
		tokenStack.push(topOfStack);
		
	}

	private void finalizaIF() {
		System.out.println("Finaliza IF");
		int index = flowStack.size() - 1;
		
		int count = counterStack.remove(counterStack.size() - 1);

		if (index < 0) {
			writeToGenFormatted("else_if_" + count, "+", "const_0", "Vai para else se codicao falsa");
		} else {
			flowStack.remove(index);
		}
		
		writeToGenFormatted("end_if_" + count, "+", "const_0", "Termina IF");
		
		String stackTop = tokenStack.pop();
		tokenStack.clear();
		tokenStack.push(stackTop);

	}

	private void escreveOperador(String operator) {
		if(operator.equals("<")) {
			writeToGenFormatted("", "SC", "POP", "Pega primeiro parametro");
			writeToGenFormatted("", "MM", "ARG", "Salva primeiro parametro");
			writeToGenFormatted("", "SC", "POP", "Pega segundo parametro");
			writeToGenFormatted("", "-", "ARG", "Realiza subtracao");
			writeToGenFormatted("", "JN", "value_" + ++counterOperators, "Se op1 < op2, verdadeiro");
			writeToGenFormatted("", "LV", "=0000", "Carrega valor zero");
			writeToGenFormatted("", "JP", "value_" + ++counterOperators, "Pula para finalizar");
			counterOperators -= 2;
			writeToGenFormatted("value_" + ++counterOperators, "LV", "=0001", "Condicao verdadeira");
			writeToGenFormatted("value_" + ++counterOperators, "SC", "PUSH", "Coloca na pilha");
			
		} else if(operator.equals(">")) {
			writeToGenFormatted("", "SC", "POP", "Pega primeiro parametro");
			writeToGenFormatted("", "MM", "ARG", "Salva primeiro parametro");
			writeToGenFormatted("", "SC", "POP", "Pega segundo parametro");
			writeToGenFormatted("", "-", "ARG", "Realiza subtracao");
			writeToGenFormatted("", "JN", "value_" + ++counterOperators, "Se op1 < op2, falso");
			writeToGenFormatted("", "JZ", "value_" + counterOperators, "Se op1 = op2, falso");
			writeToGenFormatted("", "LV", "=0001", "Carrega valor um, op1 > op2");
			writeToGenFormatted("", "JP", "value_" + ++counterOperators, "Pula para finalizar");
			counterOperators -= 2;
			writeToGenFormatted("value_" + ++counterOperators, "LV", "=0000", "Condicao Falsa");
			writeToGenFormatted("value_" + ++counterOperators, "SC", "PUSH", "Coloca na pilha");
		} else if(operator.equals(">=")) {
			writeToGenFormatted("", "SC", "POP", "Pega primeiro parametro");
			writeToGenFormatted("", "MM", "ARG", "Salva primeiro parametro");
			writeToGenFormatted("", "SC", "POP", "Pega segundo parametro");
			writeToGenFormatted("", "-", "ARG", "Realiza subtracao");
			writeToGenFormatted("", "JN", "value_" + ++counterOperators, "Se op1 < op2, falso");
			writeToGenFormatted("", "LV", "=0001", "Carrega valor um, op1 >= op2, verdadeiro");
			writeToGenFormatted("", "JP", "value_" + ++counterOperators, "Pula para finalizar");
			counterOperators -= 2;
			writeToGenFormatted("value_" + ++counterOperators, "LV", "=0000", "Condicao Falsa");
			writeToGenFormatted("value_" + ++counterOperators, "SC", "PUSH", "Coloca na pilha");
		} else if(operator.equals("<=")) {
			writeToGenFormatted("", "SC", "POP", "Pega primeiro parametro");
			writeToGenFormatted("", "MM", "ARG", "Salva primeiro parametro");
			writeToGenFormatted("", "SC", "POP", "Pega segundo parametro");
			writeToGenFormatted("", "-", "ARG", "Realiza subtracao");
			writeToGenFormatted("", "JN", "value_" + ++counterOperators, "Se op1 < op2, verdadeiro");
			writeToGenFormatted("", "JZ", "value_" + counterOperators, "Se op1 == op2, verdadeiro");
			writeToGenFormatted("", "LV", "=0000", "Carrega valor zero");
			writeToGenFormatted("", "JP", "value_" + ++counterOperators, "Pula para finalizar");
			counterOperators -= 2;
			writeToGenFormatted("value_" + ++counterOperators, "LV", "=0001", "Condicao verdadeira");
			writeToGenFormatted("value_" + ++counterOperators, "SC", "PUSH", "Coloca na pilha");
		} else if(operator.equals("==")) {
			writeToGenFormatted("", "SC", "POP", "Pega primeiro parametro");
			writeToGenFormatted("", "MM", "ARG", "Salva primeiro parametro");
			writeToGenFormatted("", "SC", "POP", "Pega segundo parametro");
			writeToGenFormatted("", "-", "ARG", "Realiza subtracao");
			writeToGenFormatted("", "JZ", "value_" + ++counterOperators, "Se op1 == op2, verdadeiro");
			writeToGenFormatted("", "LV", "=0000", "Carrega valor zero");
			writeToGenFormatted("", "JP", "value_" + ++counterOperators, "Pula para finalizar");
			counterOperators -= 2;
			writeToGenFormatted("value_" + ++counterOperators, "LV", "=0001", "Condicao verdadeira");
			writeToGenFormatted("value_" + ++counterOperators, "SC", "PUSH", "Coloca na pilha");
		} else if(operator.equals("!=")) {
			writeToGenFormatted("", "SC", "POP", "Pega primeiro parametro");
			writeToGenFormatted("", "MM", "ARG", "Salva primeiro parametro");
			writeToGenFormatted("", "SC", "POP", "Pega segundo parametro");
			writeToGenFormatted("", "-", "ARG", "Realiza subtracao");
			writeToGenFormatted("", "JZ", "value_" + ++counterOperators, "Se op1 == op2, false");
			writeToGenFormatted("", "LV", "=0001", "Carrega valor zero");
			writeToGenFormatted("", "JP", "value_" + ++counterOperators, "Pula para finalizar");
			counterOperators -= 2;
			writeToGenFormatted("value_" + ++counterOperators, "LV", "=0000", "Condicao verdadeira");
			writeToGenFormatted("value_" + ++counterOperators, "SC", "PUSH", "Coloca na pilha");
		}
		
	}

	private void escreveElse() {
		System.out.println("ELSE Starts here");
		
		int count = counterStack.get(counterStack.size() - 1);
		
		writeToGenFormatted("", "JP", "end_if_" + count, "Vai para o final do IF");
		writeToGenFormatted("else_if_" + count, "+", "const_0", "Vai para else se codicao falsa");
		flowStack.add("else");

	}

	private void startIF() {

		String stackTop = tokenStack.pop();
		tokenStack.clear();
		tokenStack.push(stackTop);

		counter++;
		
		String operator = flowStack.remove(flowStack.size() - 1);
		escreveOperador(operator);
		
		
		writeToGenFormatted("", "SC", "POP", "Busca resultado da comparacao");
		writeToGenFormatted("", "JZ", "else_if_" + counter, "Vai para else se codicao falsa");
		
		counterStack.add(counter);

	}

	private void exitFunction() {
		String functionName = functionNameStack.remove(functionNameStack.size() - 1);

		writeToGenFormatted("", "RS", functionName, "Retorno de sub Rotina");
		genCode.append(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n\n");
	}

	private void returnFunction() {
		String stackTop = tokenStack.pop();
		tokenStack.pop();
		String variableToReturn = tokenStack.pop();
		writeToGenFormatted("", "LD", currentContext + "_" + variableToReturn, "Carrega valor de retorno");
		writeToGenFormatted("", "SC", "PUSH", "Coloca retorno na pilha");
		tokenStack.clear();
		tokenStack.push(stackTop);

	}

	private void assignmentFunctionEnd() {
		String var = assignmentStack.pop();

		writeToGenFormatted("", "SC", "POP", "Retira valor da pilha");
		writeToGenFormatted("", "MM", var, "Salva atribuicao na variavel");
	}

	private void treatExpressionFinal() {
		
		if(paramStackExp.size() > 0) {
			
			int value = paramStackExp.remove(paramStackExp.size() - 1) - 1;
			
			if(value == 0) {
				String topOfStack = tokenStack.pop();
				treatExpression(tokenStack.getFullStack());
				tokenStack = tempParamTokenStack.remove(tempParamTokenStack.size() - 1);
				tokenStack.push(topOfStack);
			} else  {
				paramStackExp.add(value);
			}
			
		} else {
			if (--expressionCounter == 0) {
				String topOfStack = tokenStack.pop();
				treatExpression(tokenStack.getFullStack());
				tokenStack = tempTokenStack;
				tempTokenStack = new TokenStack();
				tokenStack.push(topOfStack);
			}
			
		}

	}

	private void treatExpressionStart() {
		
		if(paramStackExp.size() != 0) {
			int expCount = paramStackExp.remove(paramStackExp.size() - 1);
			if (expCount == 0) {
				TokenStack tTokenStack = new TokenStack();
				tTokenStack.addStack(tokenStack);
				tokenStack = new TokenStack();

				tokenStack.push(tTokenStack.getHead());

				tTokenStack.pop();
				tempParamTokenStack.add(tTokenStack);
			}
			
			paramStackExp.add(++expCount);
			
		} else {
			if (expressionCounter == 0) {
				tempTokenStack.addStack(tokenStack);
				tokenStack = new TokenStack();

				tokenStack.push(tempTokenStack.getHead());

				tempTokenStack.pop();
			}

			expressionCounter++;
		}

	}

	private void treatExpression(List<String> fullStack) {
		RPNConverter rpnConverter = new RPNConverter();
		List<String> rpnList = rpnConverter.convert(fullStack);

		// System.out.println("\n\nRPN");
		// for(String each : rpnList) {
		// System.out.println(each);
		// }
		
		System.out.println("=============Treat expression");
		tokenStack.printStack();
		System.out.println("==================");

		System.out.println();
		writeExpressionASM(rpnList);
		
		

	}

	private void writeExpressionASM(List<String> rpnList) {		

		while (rpnList.size() > 0) {
			String token = rpnList.remove(0);

			if (isNumero(token)) {
				saveConstant(token);
				writeToGenFormatted("", "LD", "const_" + token, "Carrega constante");
				writeToGenFormatted("", "SC", "PUSH", "Enviar para pilha");
			} else if (isOperator(token)) {
				writeToGenFormatted("", "SC", "POP", "Retira argumento");
				writeToGenFormatted("", "MM", "ARG", "Salva argumento");
				writeToGenFormatted("", "SC", "POP", "Retira argumento");
				writeToGenFormatted("", token, "ARG", "Efetua operacao");
				writeToGenFormatted("", "SC", "PUSH", "Guarda resultado na pilha");
			} else if (isVariable(token)) {
				String fullName = existsInVariable(token);
				if (fullName != null) {
					writeToGenFormatted("", "LD", fullName, "Carrega variavel");
					writeToGenFormatted("", "SC", "PUSH", "Envia variavel para pilha");
				} else {
					throw new AssertionError("Variavel nao existe");
				}
			}

		}

		tokenStack.clear();
		

	}

	private String existsInVariable(String token) {
		token = token.substring(1);
		
		
		for (Variable each : variableList) {
			if (each.getInitialName().equals(token)) {
				if (each.getContext().equals(currentContext))
					return each.getFullName();
			}
		}

		for (Variable each : variableList) {
			if (each.getInitialName().equals(token)) {
				if (each.getContext().equals("gbl"))
					return each.getFullName();
			}
		}

		return null;
	}

	private boolean isVariable(String token) {
		if (token.startsWith("_"))
			return true;
		return false;
	}

	private boolean isOperator(String token) {
		switch (token) {
		case "+":
		case "-":
		case "/":
		case "*":
			return true;
		}

		return false;
	}

	private boolean isNumero(String token) {
		switch (token) {
		case "+":
		case "-":
		case "/":
		case "*":
		case "(":
		case ")":
			return false;
		}

		try {
			Integer.parseInt(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private void saveConstant(String value) {
		if (constantList.contains("const_" + value))
			return;

		
		writeToConstFormatted(value);
		constantList.add("const_" + value);

	}

	private void writeCode() {
		// System.out.println(funcParamBuilder.toString());
		// System.out.println(genCode.toString());

	}

	public void push(String token) {
		tokenStack.push(token);
	}

	public void flush() {
		tokenStack.clear();
	}

	public void variableDeclaration(List<Variable> variables) {
		StringBuilder builder = new StringBuilder();

		for (Variable each : variables) {
			if (each.getType().equals("int")) {

				String decString = "=" + each.getValue();
				writeToVariableFormatted(each.getFullName(), decString, false);
			} else if (each.getType().contains("vector")) {

				int size = getVectorSize(each.getType());
				writeToVariableFormatted(each.getFullName(), size + "", true);
			}

		}
		// System.out.println(builder.toString());

	}

	private int getVectorSize(String type) {

		String sub = type.substring(7);
		String[] array = sub.split(":");
		array[1] = array[1].replaceFirst("]", "");
		int int1 = Integer.parseInt(array[0]);
		int int2 = Integer.parseInt(array[1]);

		return (int1 + 1) * (int2 + 1);
	}

	private void createVariable() {
		String stackTop = tokenStack.pop();
		String t = tokenStack.pop();

		String varType;
		String varName;
		String num1;
		String num2;

		if (!t.equals(";"))
			return;

		t = tokenStack.pop();

		if (t.equals("INT") || t.equals("STRING") || t.equals("BOOLEAN")) {
			varType = t;
		} else if (t.equals("]")) {
			num2 = tokenStack.pop();
			tokenStack.pop();
			num1 = tokenStack.pop();
			tokenStack.pop();
			varType = tokenStack.pop() + "[" + num1 + ":" + num2 + "]";
		} else {
			return;
		}

		tokenStack.pop(); // pops :

		varName = tokenStack.pop();

		tokenStack.pop();

		Variable v = new Variable(varName, "0", varType.toLowerCase());
		v.setFullName(currentContext + "_" + v.getInitialName());
		v.setContext(currentContext);

		if (variableList.contains(v))
			throw new AssertionError("Ja contem variavel" + v.getFullName());

		variableList.add(v);
		writeToVariableFormatted(v.getFullName(), "=0", false);

		// tokenStack.push(stackTop);
		tokenStack.clear();
		tokenStack.push(stackTop);
		// SymbolTable currentSymbolTable = symbolStack.pop();

	}

	/*private void writeGlobalVariables() {
		List<Variable> vars = new ArrayList<>();

		for (Variable each : variableList) {
			if (each.getContext().equals("gbl")) {
				vars.add(each);
			}
		}

		variableDeclaration(vars);

	}*/

	public String getGenCode() {
		return genCode.toString();
	}

	public void createFuncDecl() {
		// String previousContext = currentContext;

		String stackTop = tokenStack.pop();
		// tokenStack.printStack();

		tokenStack.next();

		String subName = tokenStack.next();

		currentContext = subName;
		
		genCode.append("\n;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n");
		writeToGenFormatted("sub_" + subName, "JP", "=0000", "Endereco de retorno");

		functionNameStack.add("sub_" + subName);

		tokenStack.next();
		String t = tokenStack.next();

		while (!t.equals(")")) {
			String paramName;
			String varType;

			if (t.equals(","))
				t = tokenStack.next();

			if (t.toUpperCase().equals("VECTOR")) {
				tokenStack.next();
				String num1 = tokenStack.next();
				tokenStack.next();
				String num2 = tokenStack.next();
				tokenStack.next();
				varType = "vector[" + num1 + ":" + num2 + "]";
			} else {
				varType = t.toLowerCase();
			}

			String initialName = tokenStack.next();
			paramName = currentContext + "_" + initialName;
			writeToGenFormatted("", "SC", "PARAM_POP", "Retira parametro da pilha");
			writeToGenFormatted("", "MM", paramName, "Salva na variavel");

			t = tokenStack.next();

			Variable each = new Variable(initialName, "0", varType.toLowerCase());
			each.setFullName(paramName);
			each.setContext(currentContext);
			buildFuncDataParam(each);

		}

		// System.out.println("\n\n");
		// System.out.println(tempBuilder.toString());
		// System.out.println(funcParamBuilder.toString());

		tokenStack.clear();
		tokenStack.push(stackTop);

	}

	public void buildFuncDataParam(Variable each) {
		StringBuilder builder = new StringBuilder();

		if (each.getType().equals("int")) {
			writeToFuncParamFormatted(each.getFullName(), "0", false);
		} else if (each.getType().contains("vector")) {

			int size = getVectorSize(each.getType());
			writeToFuncParamFormatted(each.getFullName(), size + "", true);
		}

		//funcParamBuilder.append(builder.toString());

		if (variableList.contains(each))
			throw new AssertionError("Ja contem variavel" + each.getFullName());

		variableList.add(each);

		// System.out.println(builder.toString());

	}

	public void printCode() {

		System.out.println("\n\n");
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;Area de dados;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n\n");
		
		System.out.println(";;;;;;;; Variaveis\n");
		System.out.println(variablesBuilder.toString());
		
		System.out.println("\n;;;;;;;; Constantes\n");
		System.out.println(constantBuilder.toString());
		
		System.out.println("\n;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;Parametros de Funcoes;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n\n");
		
		System.out.println(funcParamBuilder.toString());
		
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;Programa;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n\n");
		System.out.println(genCode.toString());

	}

	private void solveExpression() {
		System.out.println("\n\nEXPRESSION!!!");
		tokenStack.printStack();
		tokenStack.clear();

	}

	private void assignmentFunctionStart() {
		String stackTop = tokenStack.pop();
		// tokenStack.printStack();

		tokenStack.pop();

		String var = existsInVariable("_" + tokenStack.pop());

		if (var == null)
			throw new AssertionError("Variavel nao existe");

		assignmentStack.push(var);

		tokenStack.clear();

		tokenStack.push(stackTop);

	}

	/*
	 * public String getHexString(String value) { String initial =
	 * Integer.toHexString(Integer.parseInt(value));
	 * 
	 * while(initial.length() < 4) { initial = "0" + initial; }
	 * 
	 * return ("/" + initial).toUpperCase(); }
	 */
	
	public void writeToGenFormatted(String label, String mneumonic, String operand, String comment) {		
		String formatted = String.format("%-25s%2s%2s%5s%-15s%2s%-40s\n", label, "", mneumonic, "", operand, "", "; " + comment);
		genCode.append(formatted);
		
	}
	
	
	public void writeToConstFormatted(String value) {		
		String formatted = String.format("%-25s%2s%2s%5s%-15s%2s%-40s\n", "const_" + value, "","K", "", "=" + value, "", "; Declara constante " + value);
		constantBuilder.append(formatted);
		
	}
	
	public void writeToVariableFormatted(String label, String value, boolean vector) {		
		String formatted;
		
		if(vector) {
			 formatted = String.format("%-25s%2s%2s%5s%-15s%2s%-40s\n", label, "","$", "", "=" + value, "", "; Declara vetor " + label);
		} else {
			formatted = String.format("%-25s%2s%2s%5s%-15s%2s%-40s\n", label, "","K", "", value, "", "; Declara variavel " + label);
		}
		
		
		variablesBuilder.append(formatted);
		
	}
	
	public void writeToFuncParamFormatted(String label, String value, boolean vector) {		
		String formatted;
		
		if(vector) {
			 formatted = String.format("%-25s%2s%2s%5s%-15s%2s%-40s\n", label, "","$", "", "=" + value, "", "; Declara vetor " + label);
		} else {
			formatted = String.format("%-25s%2s%2s%5s%-15s%2s%-40s\n", label, "","K", "", "=" + value, "", "; Declara variavel " + label);
		}
		
		
		funcParamBuilder.append(formatted);
		
	}
}
