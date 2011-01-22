package com.floreysoft.jmte;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// ${if !bean.trueCond}${address}${else}NIX${end}
public class SampleIfEmptyFalseExpressionCompiledTemplate extends
		AbstractCompiledTemplate {

	public SampleIfEmptyFalseExpressionCompiledTemplate(Engine engine) {
		super(engine);
	}

	@Override
	public Set<String> getUsedVariables() {
		Set<String> usedVariables = new HashSet<String>();
		usedVariables.add("address");
		return usedVariables;
	}

	@Override
	protected String transformCompiled(TemplateContext context) {
		StringBuilder buffer = new StringBuilder();

		IfToken token1 = new IfToken(Arrays.asList(new String[] { "bean",
				"trueCond" }), "bean.trueCond", true);

		context.push(token1);
		try {
			if ((Boolean) token1.evaluate(context)) {
				buffer.append(new StringToken("address", "address",
						null, null, null, null, null).evaluate(context));
			} else {
				buffer.append("NIX");
			}
		} finally {
			context.pop();
		}
		return buffer.toString();
	}

}
