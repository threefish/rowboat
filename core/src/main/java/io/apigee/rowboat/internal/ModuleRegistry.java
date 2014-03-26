/**
 * Copyright 2014 Apigee Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.apigee.rowboat.internal;

import io.apigee.rowboat.NodeException;
import io.apigee.rowboat.NodeModule;
import io.apigee.rowboat.spi.NodeImplementation;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ServiceLoader;

/**
 * <p>
 *     This tracks all the built-in modules that are available to us. There are three types of modules:
 * </p>
 * <ol>
 *     <li>Native modules are built in Java. They are loaded using the ServiceLoader, which means that
 *     these modules must implement NodeModule and must be listed in META-INF/services/c.a.n.r.NodeModule.</li>
 *     <li>Compiled native modules are built in JavaScript. A Java stub that uses ServiceLoader and implements
 *     NodeScriptModule is used to pull in the JavaScript source, which is then compiled and loaded.</li>
 *     <li>Internal native modules are also built in Java and are also loaded using the ServiceLoader.
 *     However they are loaded using process.binding, not "require" and are intended for internal use only.
 *     This way they aren't in the namespace for ordinary users. They implement InternalNodeModule.</li>
 *     <li>Internal script modules are written in JavaScript -- their source lives in src/main/javascript and
 *     is pre-compiled using Rhino (we have a plugin for this). They are then loaded directly by this
 *     class. These modules may only reside within this Maven module.</li>
 * </ol>
 * <p>
 *     The constructor for this class manually defines all compiled script modules. All the rest
 *     are loaded using the ServiceLoader, which means that new modules can add new scripts.
 * </p>
 * <p>
 *     This registry is loaded based on a NodeImplementation, which means that there can be many if there
 *     are a lot of versions of Node available.
 * </p>
 */
public class ModuleRegistry
{
    /**
     * Add this prefix to internal module code before compiling -- it makes them behave as they expect and as
     * internal modules from "normal" Node behave. This same code must also be included by the Rhino
     * compiler, so it is repeated in pom.xml and must be changed there if it's changed here. This makes these
     * modules be loaded exactly as they would be in traditional Node.
     */
    private static final String CODE_PREFIX = "(function (exports, require, module, __filename, __dirname) {";
    private static final String CODE_POSTFIX = "});";

    //private final HashMap<String, NodeModule>         modules         = new HashMap<String, NodeModule>();
    //private final HashMap<String, InternalNodeModule> internalModules = new HashMap<String, InternalNodeModule>();
    private final HashMap<String, Module>              builtInModules = new HashMap<>();
    private final NodeImplementation                   implementation;

    private CompiledScript mainScript;
    private boolean loaded;

    public ModuleRegistry(NodeImplementation impl)
    {
        this.implementation = impl;
    }

    public NodeImplementation getImplementation() {
        return implementation;
    }

    public synchronized void load(ScriptEngine engine)
    {
        if (loaded) {
            return;
        }

        /*
        // Load all native Java modules implemented using the "NodeModule" interface
        ServiceLoader<NodeModule> loader = ServiceLoader.load(NodeModule.class);
        for (NodeModule mod : loader) {
            addNativeModule(mod);
        }
        */

        /*
        // Load all JavaScript modules implemented using "NodeScriptModule"
        ServiceLoader<NodeScriptModule> scriptLoader = ServiceLoader.load(NodeScriptModule.class);
        for (NodeScriptModule mod: scriptLoader) {
            for (String[] src : mod.getScriptSources()) {
                if (src.length != 2) {
                    throw new AssertionError("Script module " + mod.getClass().getName() +
                                             " returned script source arrays that do not have two elements");
                }
                compileAndAdd(cx, mod, src[0], src[1]);
            }
        }
        */

        loadMainScript(implementation.getMainScript(), engine);
        for (String[] builtin : implementation.getBuiltInModules()) {
            builtInModules.put(builtin[0], new ScriptModule(builtin[0], builtin[1]));
        }
        /*
        for (Class<? extends NodeModule> nat : implementation.getNativeModules()) {
            loadModuleByClass(nat);
        }
        */

        loaded = true;
    }

    /*
    private void compileAndAdd(Context cx, Object impl, String name, String path)
    {
        String scriptSource;
        InputStream is = impl.getClass().getResourceAsStream(path);
        if (is == null) {
            throw new AssertionError("Script " + path + " cannot be found for module " + name);
        }
        try {
            scriptSource = Utils.readStream(is);
        } catch (IOException ioe) {
            throw new AssertionError("Error reading script " + path + " for module " + name);
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }

        String finalSource = CODE_PREFIX + scriptSource + CODE_POSTFIX;
        Script compiled = cx.compileString(finalSource, name, 1, null);
        compiledModules.put(name, compiled);
    }

    private void addNativeModule(NodeModule mod)
    {
        if (mod instanceof InternalNodeModule) {
            internalModules.put(mod.getModuleName(), (InternalNodeModule) mod);
        } else {
            modules.put(mod.getModuleName(), mod);
        }
    }
    */

    private void loadMainScript(String name, ScriptEngine engine)
    {
        try {
            try (InputStream in = implementation.getClass().getResourceAsStream(name)) {
                InputStreamReader rdr = new InputStreamReader(in);
                mainScript = ((Compilable)engine).compile(rdr);
            } catch (ScriptException se) {
                throw new NodeException("Can't compile main script: " + se, se);
            }
        } catch (IOException ioe) {
            throw new NodeException("Can't read main script: " + ioe, ioe);
        }
    }

    /*
    private void loadModuleByName(String className)
    {
        try {
            Class<NodeModule> klass = (Class<NodeModule>)Class.forName(className);
            loadModuleByClass(klass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private void loadModuleByClass(Class<? extends NodeModule> klass)
    {
        try {
            NodeModule mod = klass.newInstance();
            addNativeModule(mod);
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private void addCompiledModule(String name, String className)
    {
        try {
            Class<Script> cl = (Class<Script>)Class.forName(className);
            Script script = cl.newInstance();
            compiledModules.put(name, script);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Missing built-in module " + className);
        } catch (InstantiationException e) {
            throw new AssertionError("Error creating Script instance for " + className);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Error creating Script instance for " + className);
        }
    }

    public NodeModule get(String name)
    {
        return modules.get(name);
    }

    public NodeModule getInternal(String name)
    {
        return internalModules.get(name);
    }

    public Script getCompiledModule(String name)
    {
        return compiledModules.get(name);
    }
    */

    public CompiledScript getMainScript()
    {
        return mainScript;
    }

    private static abstract class Module
    {
        final String name;

        Module(String name)
        {
            this.name = name;
        }


    }

    private static class ScriptModule
        extends Module
    {
        final String resourceName;
        CompiledScript script;

        ScriptModule(String name, String resourceName)
        {
            super(name);
            this.resourceName = resourceName;
        }
    }

    private static class JavaModule
        extends Module
    {
        final NodeModule module;

        JavaModule(String name, NodeModule module)
        {
            super(name);
            this.module = module;
        }
    }
}