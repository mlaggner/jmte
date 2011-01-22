package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// ${ foreach list item \n}${item.property1}${end}
public class SampleNewlineForeachSeparatorCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleNewlineForeachSeparatorCompiledTemplate(Engine engine) {
		super(engine);
	}

	@Override
	public Set<String> getUsedVariables() {
		Set<String> usedVariables = new HashSet<String>();
		usedVariables.add("address");
		return usedVariables;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();
		
		ForEachToken token1 = new ForEachToken("list", "item", "\n");
		token1.setIterable((Iterable) token1.evaluate(context));

		context.model.enterScope();
		context.push(token1);
		try {
			Iterator<Object> iterator = token1.iterator();
			while (iterator.hasNext()) {
				context.model.put(token1.getVarName(), token1.advance());
				addSpecialVariables(token1, context.model);
				getEngine().notifyListeners(token1,
						ProcessListener.Action.ITERATE_FOREACH);

				buffer.append(new StringToken(Arrays
						.asList(new String[] { "item", "property1" }),
				"item.property1").evaluate(context));

				if (!token1.isLast()) {
					buffer.append(token1.getSeparator());
				}
			}
		} finally {
			context.model.exitScope();
			context.pop();
		}
		return buffer.toString();
	}

}
