package eu.scasefp7.eclipse.reqeditor.helpers;

import java.util.concurrent.atomic.AtomicReference;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Reads a JSON string to Java objects.
 * 
 * @author themis
 */
public class JSON2Java {

	/**
	 * The parser used, which relies on Javascript.
	 */
	private static final ScriptEngine jsonParser;

	/**
	 * The java objects used include {@link Object}, {@link HashMap}, {@link Array}, {@link HashMap}, {@link String},
	 * {@link Boolean}, and {@link Integer}.
	 */
	static {
		try {
			//@formatter:off
			String init = "toJava = function(o) {"
					 + "	return o == null ? null : o.toJava();"
					 + "};"
					 + "Object.prototype.toJava = function() {"
					 + "	var m = new java.util.HashMap();"
					 + "	for (var key in this)"
					 + "		if (this.hasOwnProperty(key))"
					 + "			m.put(key, toJava(this[key]));"
					 + "	return m;"
					 + "};"
					 + "Array.prototype.toJava = function() {"
					 + "	var l = this.length;"
					 + "	var a = new java.lang.reflect.Array.newInstance(java.lang.Object, l);"
					 + "	for (var i = 0;i < l;i++)"
					 + "		a[i] = toJava(this[i]);"
					 + "	return a;"
					 + "};"
					 + "String.prototype.toJava = function() {"
					 + "	return new java.lang.String(this);"
					 + "};"
					 + "Boolean.prototype.toJava = function() {"
					 + "	return java.lang.Boolean.valueOf(this);"
					 + "};"
					 + "Number.prototype.toJava = function() {"
					 + "	return java.lang.Integer(this);"
					 + "};";
	         //@formatter:on
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
			engine.eval(init);
			jsonParser = engine;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Parses a JSON string and returns a java object.
	 * 
	 * @param json the JSON string.
	 * @return a java object including the JSON information.
	 */
	public static Object parseJSON(String json) {
		try {
			String eval = "new java.util.concurrent.atomic.AtomicReference(toJava((" + json + ")))";
			@SuppressWarnings("rawtypes")
			AtomicReference ret = (AtomicReference) jsonParser.eval(eval);
			return ret.get();
		} catch (ScriptException e) {
			throw new RuntimeException("Invalid json", e);
		}
	}
}
