package com.floreysoft.jmte;

import java.util.Map;

public class EndToken extends AbstractToken {
	public static final String END = "end";

	public EndToken() {
	}

	public EndToken(EndToken endToken) {
		super(endToken);
	}

	@Override
	public String getText() {
		if (text == null) {
			text = END;
		}
		return text;
	}


	@Override
	public Token dup() {
		return new EndToken(this);
	}

	@Override
	public Object evaluate(Engine engine, Map<String, Object> model, ErrorHandler errorHandler) {
		return "";
	}
}
