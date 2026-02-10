package net.runelite.client.plugins.runeutils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.*;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaScript execution engine for unified logic between browser and RuneLite
 * Uses Mozilla Rhino for ECMAScript execution (Java 11 compatible)
 */
@Slf4j
public class JSEngine
{
	private final Context context;
	private final Scriptable scope;
	private final Gson gson = new Gson();

	public JSEngine()
	{
		this.context = Context.enter();
		this.context.setOptimizationLevel(-1); // Interpreted mode for compatibility
		this.scope = context.initStandardObjects();

		initializeGlobals();
		log.info("[JSEngine] Initialized with Rhino JavaScript engine");
	}

	private void initializeGlobals()
	{
		// Expose utility functions
		scope.put("log", scope, (BaseFunction) new BaseFunction()
		{
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
			{
				if (args.length > 0)
				{
					log.info(String.valueOf(args[0]));
				}
				return Undefined.instance;
			}
		});

		scope.put("logError", scope, (BaseFunction) new BaseFunction()
		{
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
			{
				if (args.length > 0)
				{
					log.error(String.valueOf(args[0]));
				}
				return Undefined.instance;
			}
		});
	}

	/**
	 * Execute JavaScript code and return result
	 */
	public Object eval(String code)
	{
		if (code == null || code.trim().isEmpty())
		{
			return null;
		}

		try
		{
			Object result = context.evaluateString(scope, code, "eval", 1, null);
			return convertToJava(result);
		}
		catch (Exception e)
		{
			log.error("[JSEngine] Evaluation failed: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Execute JavaScript function with arguments
	 */
	public Object call(String functionName, Object... args)
	{
		if (functionName == null || functionName.trim().isEmpty())
		{
			return null;
		}

		try
		{
			Object func = scope.get(functionName, scope);

			if (func == Scriptable.NOT_FOUND || !(func instanceof Function))
			{
				log.error("[JSEngine] Function not found: {}", functionName);
				return null;
			}

			Object result = ((Function) func).call(context, scope, scope, args);
			return convertToJava(result);
		}
		catch (Exception e)
		{
			log.error("[JSEngine] Function call failed: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Define JavaScript function in global scope
	 */
	public void defineFunction(String name, String code)
	{
		if (name == null || code == null)
		{
			return;
		}

		try
		{
			String wrappedCode = code.trim().startsWith("function")
				? code
				: String.format("var %s = %s;", name, code);

			context.evaluateString(scope, wrappedCode, "define", 1, null);
			log.debug("[JSEngine] Defined function: {}", name);
		}
		catch (Exception e)
		{
			log.error("[JSEngine] Failed to define function {}: {}", name, e.getMessage());
		}
	}

	/**
	 * Set global variable
	 */
	public void setGlobal(String name, Object value)
	{
		if (name == null)
		{
			return;
		}

		try
		{
			Object jsValue = Context.javaToJS(value, scope);
			scope.put(name, scope, jsValue);
		}
		catch (Exception e)
		{
			log.error("[JSEngine] Failed to set global {}: {}", name, e.getMessage());
		}
	}

	/**
	 * Get global variable value
	 */
	public Object getGlobal(String name)
	{
		if (name == null)
		{
			return null;
		}

		try
		{
			Object value = scope.get(name, scope);
			return convertToJava(value);
		}
		catch (Exception e)
		{
			log.error("[JSEngine] Failed to get global {}: {}", name, e.getMessage());
			return null;
		}
	}

	/**
	 * Load JavaScript module from string
	 */
	public void loadModule(String moduleName, String code)
	{
		if (moduleName == null || code == null)
		{
			return;
		}

		try
		{
			context.evaluateString(scope, code, moduleName, 1, null);
			log.info("[JSEngine] Loaded module: {}", moduleName);
		}
		catch (Exception e)
		{
			log.error("[JSEngine] Failed to load module {}: {}", moduleName, e.getMessage());
		}
	}

	/**
	 * Convert Rhino object to Java object
	 */
	private Object convertToJava(Object value)
	{
		if (value == null || value == Undefined.instance || value == Scriptable.NOT_FOUND)
		{
			return null;
		}

		if (value instanceof Wrapper)
		{
			return ((Wrapper) value).unwrap();
		}

		if (value instanceof String || value instanceof Number || value instanceof Boolean)
		{
			return value;
		}

		if (value instanceof NativeArray)
		{
			NativeArray array = (NativeArray) value;
			Object[] result = new Object[(int) array.getLength()];
			for (int i = 0; i < result.length; i++)
			{
				result[i] = convertToJava(array.get(i, array));
			}
			return result;
		}

		if (value instanceof NativeObject)
		{
			NativeObject obj = (NativeObject) value;
			Map<String, Object> map = new HashMap<>();
			for (Object key : obj.keySet())
			{
				String keyStr = String.valueOf(key);
				map.put(keyStr, convertToJava(obj.get(keyStr, obj)));
			}
			return map;
		}

		return value.toString();
	}

	/**
	 * Serialize Java object to JSON string for JS consumption
	 */
	public String toJSON(Object obj)
	{
		return gson.toJson(obj);
	}

	/**
	 * Parse JSON string to Java object
	 */
	public Object fromJSON(String json)
	{
		return gson.fromJson(json, Object.class);
	}

	/**
	 * Close engine and release resources
	 */
	public void close()
	{
		if (context != null)
		{
			Context.exit();
			log.info("[JSEngine] Closed");
		}
	}
}
