package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.floreysoft.jmte.ProcessListener.Action;

/**
 * <p>
 * The template engine.
 * </p>
 * 
 * <p>
 * Usually this is the only class you need calling
 * {@link #transform(String, Map)}. Like this
 * </p>
 * 
 * <pre>
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * </pre>
 * 
 * <p>
 * You have to provide the template input written in the template language and a
 * model from String to Object. Maybe like this
 * </p>
 * 
 * <pre>
 * String input = &quot;${name}&quot;;
 * Map&lt;String, Object&gt; model = new HashMap&lt;String, Object&gt;();
 * model.put(&quot;name&quot;, &quot;Minimal Template Engine&quot;);
 * Engine engine = new Engine();
 * String transformed = engine.transform(input, model);
 * assert (transformed.equals(&quot;Minimal Template Engine&quot;));
 * </pre>
 * 
 * where <code>input</code> contains the template and <code>model</code> the
 * model. <br>
 * 
 * @see Lexer
 * @see ErrorHandler
 * @see Tool
 */
public final class Engine {
	public static final String ODD_PREFIX = "odd_";
	public static final String EVEN_PREFIX = "even_";
	public static final String LAST_PREFIX = "last_";
	public static final String FIRST_PREFIX = "first_";

	/**
	 * Pairs of begin/end.
	 * 
	 */
	static class StartEndPair {
		public final int start;
		public final int end;

		public StartEndPair(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return "" + start + "-" + end;
		}
	}

	/**
	 * Replacement for {@link java.lang.String.format}. All arguments will be
	 * put into the model having their index starting from 1 as their name.
	 * 
	 * @param pattern
	 *            the template
	 * @param args
	 *            any number of arguments
	 * @return the expanded template
	 */
	public static String format(String pattern, Object... args) {
		Map<String, Object> model = arrayToModel(null, args);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1, Object value1) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2, String name3,
			Object value3) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2, String name3,
			Object value3, String name4, Object value4) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		model.put(name4, value4);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	public static String formatNamed(String pattern, String name1,
			Object value1, String name2, Object value2, String name3,
			Object value3, String name4, Object value4, String name5,
			Object value5) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		model.put(name2, value2);
		model.put(name3, value3);
		model.put(name4, value4);
		model.put(name5, value4);
		Engine engine = new Engine();
		String output = engine.transform(pattern, model);
		return output;
	}

	/**
	 * Transforms an array to a model using the index of the elements (starting
	 * from 1) in the array and a prefix to form their names.
	 * 
	 * @param prefix
	 *            the prefix to add to the index or <code>null</code> if none
	 *            shall be applied
	 * @param args
	 *            the array to be transformed into the model
	 * @return the model containing the arguments
	 */
	public static Map<String, Object> arrayToModel(String prefix,
			Object... args) {
		Map<String, Object> model = new HashMap<String, Object>();
		if (prefix == null) {
			prefix = "";
		}
		for (int i = 0; i < args.length; i++) {
			Object value = args[i];
			String name = prefix + (i + 1);
			model.put(name, value);
		}
		return model;
	}

	public static Map<String, Object> toModel(String name1, Object value1) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(name1, value1);
		return model;
	}

	public static Map<String, Object> toModel(String name1, Object value1,
			String name2, Object value2) {
		Map<String, Object> model = toModel(name1, value1);
		model.put(name2, value2);
		return model;
	}

	public static Map<String, Object> toModel(String name1, Object value1,
			String name2, Object value2, String name3, Object value3) {
		Map<String, Object> model = toModel(name1, value1, name2, value2);
		model.put(name3, value3);
		return model;
	}

	/**
	 * Merges any number of named lists into a single one contained their
	 * combined values. Can be very handy in case of a servlet request which
	 * might contain several lists of parameters that you want to iterate over
	 * in a combined way.
	 * 
	 * @param names
	 *            the names of the variables in the following lists
	 * @param lists
	 *            the lists containing the values for the named variables
	 * @return a merge list containing the combined values of the lists
	 */
	public static List<Map<String, Object>> mergeLists(String[] names,
			List<Object>... lists) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (lists.length != 0) {

			// first check if all looks good
			int expectedSize = lists[0].size();
			for (int i = 1; i < lists.length; i++) {
				List<Object> list = lists[i];
				if (list.size() != expectedSize) {
					throw new IllegalArgumentException(
							"All lists and array of names must have the same size!");
				}
			}

			// yes, things are ok
			List<Object> masterList = lists[0];
			for (int i = 0; i < masterList.size(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int j = 0; j < lists.length; j++) {
					String name = names[j];
					List<Object> list = lists[j];
					Object value = list.get(i);
					map.put(name, value);
				}
				resultList.add(map);
			}
		}
		return resultList;
	}

	private String exprStartToken = "${";
	private String exprEndToken = "}";
	private double expansionSizeFactor = 1.2;
	private ErrorHandler errorHandler = new DefaultErrorHandler();
	private Locale locale = new Locale("en");
	String sourceName = null;

	private final Map<Class<?>, Renderer<?>> renderers = new HashMap<Class<?>, Renderer<?>>();
	private final Map<Class<?>, Renderer<?>> resolvedRendererCache = new HashMap<Class<?>, Renderer<?>>();

	private final Map<String, NamedRenderer> namedRenderers = new HashMap<String, NamedRenderer>();
	private final Map<Class<?>, Set<NamedRenderer>> namedRenderersForClass = new HashMap<Class<?>, Set<NamedRenderer>>();

	final List<ProcessListener> listeners = new ArrayList<ProcessListener>();

	/**
	 * Creates a new engine having <code>${</code> and <code>}</code> as start
	 * and end strings for expressions.
	 */
	public Engine() {
		init();
	}

	private void init() {
		registerRenderer(Object.class, new DefaultObjectRenderer());
		registerRenderer(Map.class, new DefaultMapRenderer());
		registerRenderer(Collection.class, new DefaultCollectionRenderer());
		registerRenderer(Iterable.class, new DefaultIterableRenderer());
	}

	public Engine setSourceName(String sourceName) {
		this.sourceName = sourceName;
		return this;
	}

	/**
	 * Transforms a template into an expanded output using the given model.
	 * 
	 * @param template
	 *            the template to expand
	 * @param model
	 *            the model used to evaluate expressions inside the template
	 * @return the expanded output
	 */
	public String transform(String template, Map<String, Object> model) {
		InterpretedTemplate templateImpl = new InterpretedTemplate(template, this);
		String output = templateImpl.transform(model);
		return output;
	}

	/**
	 * Sets the error handler to be used in this engine
	 * 
	 * @param errorHandler
	 *            the new error handler
	 */
	public Engine setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		this.errorHandler.setLocale(locale);
		return this;
	}

	public Engine registerNamedRenderer(NamedRenderer renderer) {
		namedRenderers.put(renderer.getName(), renderer);
		Set<Class<?>> supportedClasses = Util.asSet(renderer
				.getSupportedClasses());
		for (Class<?> clazz : supportedClasses) {
			Class<?> classInHierarchy = clazz;
			while (classInHierarchy != null) {
				addSupportedRenderer(classInHierarchy, renderer);
				classInHierarchy = classInHierarchy.getSuperclass();
			}
		}
		return this;
	}

	public Engine deregisterNamedRenderer(NamedRenderer renderer) {
		namedRenderers.remove(renderer.getName());
		Set<Class<?>> supportedClasses = Util.asSet(renderer
				.getSupportedClasses());
		for (Class<?> clazz : supportedClasses) {
			Class<?> classInHierarchy = clazz;
			while (classInHierarchy != null) {
				Set<NamedRenderer> renderers = namedRenderersForClass
						.get(classInHierarchy);
				renderers.remove(renderer);
				classInHierarchy = classInHierarchy.getSuperclass();
			}
		}
		return this;
	}

	private void addSupportedRenderer(Class<?> clazz, NamedRenderer renderer) {
		Collection<NamedRenderer> compatibleRenderers = getCompatibleRenderers(clazz);
		compatibleRenderers.add(renderer);
	}

	public Collection<NamedRenderer> getCompatibleRenderers(Class<?> inputType) {
		Set<NamedRenderer> renderers = namedRenderersForClass.get(inputType);
		if (renderers == null) {
			renderers = new HashSet<NamedRenderer>();
			namedRenderersForClass.put(inputType, renderers);
		}
		return renderers;
	}

	public Collection<NamedRenderer> getAllNamedRenderers() {
		Collection<NamedRenderer> values = namedRenderers.values();
		return values;
	}

	public NamedRenderer resolveNamedRenderer(String rendererName) {
		return namedRenderers.get(rendererName);
	}

	public <C> Engine registerRenderer(Class<C> clazz, Renderer<C> renderer) {
		renderers.put(clazz, renderer);
		resolvedRendererCache.clear();
		return this;
	}

	public Engine deregisterRenderer(Class<?> clazz) {
		renderers.remove(clazz);
		resolvedRendererCache.clear();
		return this;
	}

	@SuppressWarnings("unchecked")
	public Renderer<Object> resolveRendererForClass(Class<?> clazz) {
		Renderer resolvedRenderer = resolvedRendererCache.get(clazz);
		if (resolvedRenderer != null) {
			return resolvedRenderer;
		}

		resolvedRenderer = renderers.get(clazz);
		if (resolvedRenderer == null) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> interfaze : interfaces) {
				resolvedRenderer = resolveRendererForClass(interfaze);
				if (resolvedRenderer != null) {
					break;
				}
			}
		}
		if (resolvedRenderer == null) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				resolvedRenderer = resolveRendererForClass(superclass);
			}
		}
		if (resolvedRenderer != null) {
			resolvedRendererCache.put(clazz, resolvedRenderer);
		}
		return resolvedRenderer;
	}

	public Engine addProcessListener(ProcessListener listener) {
		listeners.add(listener);
		return this;
	}

	public Engine removeProcessListener(ProcessListener listener) {
		listeners.remove(listener);
		return this;
	}

	void notifyListeners(Token token, Action action) {
		for (ProcessListener processListener : listeners) {
			processListener.log(token, action);
		}
	}

	/**
	 * Gets the currently used error handler
	 * 
	 * @return the error handler
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public String getExprStartToken() {
		return exprStartToken;
	}

	public String getExprEndToken() {
		return exprEndToken;
	}

	public Engine setExprStartToken(String exprStartToken) {
		this.exprStartToken = exprStartToken;
		return this;
	}

	public Engine setExprEndToken(String exprEndToken) {
		this.exprEndToken = exprEndToken;
		return this;
	}

	public Engine setExpansionSizeFactor(double expansionSizeFactor) {
		this.expansionSizeFactor = expansionSizeFactor;
		return this;
	}

	public double getExpansionSizeFactor() {
		return expansionSizeFactor;
	}

	public Engine setLocale(Locale locale) {
		this.locale = locale;
		if (this.errorHandler != null) {
			this.errorHandler.setLocale(locale);
		}
		return this;
	}

	public Locale getLocale() {
		return locale;
	}

	public Set<String> getUsedVariables(String template) {
		Template templateImpl = getTemplate(template);
		return templateImpl.getUsedVariables();
	}

	private Template getTemplate(String template) {
		return new InterpretedTemplate(template, this);
	}
}
