package com.coalasw;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class RhinoShell {

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		
		Context cx = Context.enter();
		ScriptableObject globalScope = cx.initStandardObjects(null, true);
		// add scripts into global, here
        ScriptableObject.putProperty(globalScope, "out", Context.javaToJS(System.out, globalScope));
        processSource(cx, globalScope, "test1.js");
		
        globalScope.sealObject();
		
		Map<String, Scriptable> userScopes = new HashMap<String, Scriptable>();
		
		for( String cmd = scanner.nextLine(); cmd.equals("exit") == false; cmd = scanner.nextLine() ) {
			String user = cmd.split(":")[0].trim();
			String filename = cmd.split(":")[1].trim();
			
			Scriptable scope = userScopes.get(user);
			if ( scope == null ) {
				scope = cx.newObject(globalScope);
				scope.setPrototype(globalScope);
				scope.setParentScope(null);
				
				userScopes.put(user,  scope);
			}
			
			processSource(cx, scope, filename);
		}
		
		Context.exit();
		
		scanner.close();
	}
	
	private static void processSource(Context cx, Scriptable scope, String filename) {
		FileReader in = null;
		try {
			in = new FileReader(filename);
		}
		catch (FileNotFoundException ex) {
			System.err.println("Couldn't open file \"" + filename + "\".");
			return;
		}
		
		Object result = null;
		try {
            result = cx.evaluateReader(scope, in, filename, /* lineno */ 1, null);
        }
		catch (EcmaError ee) {
            System.err.println(ee.getErrorMessage());
		}
        catch (WrappedException we) {
            System.err.println(we.getWrappedException().toString());
            we.printStackTrace();
        }
        catch (EvaluatorException ee) {
            System.err.println(ee.getMessage());
        }
        catch (JavaScriptException jse) {
            System.err.println(jse.getMessage());
        }
        catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
        finally {
            try {
                in.close();
                if(result != null && !(result instanceof org.mozilla.javascript.Undefined))
                	System.out.println(Context.toString(result));
            }
            catch (IOException ioe) {
                System.err.println(ioe.toString());
            }
        }
	}
}
