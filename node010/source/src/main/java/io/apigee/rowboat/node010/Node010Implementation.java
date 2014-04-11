/*
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

package io.apigee.rowboat.node010;

import io.apigee.rowboat.NodeModule;
import io.apigee.rowboat.spi.NodeImplementation;

import java.util.Collection;
import java.util.Collections;

public class Node010Implementation
    implements NodeImplementation
{
    private static final String B = "/node010";
    private static final String R = B + "/rowboat/";

    @Override
    public String getVersion()
    {
        return "0.10.25";
    }

    @Override
    public String getMainScript()
    {
        return R + "trireme.js";
    }

    @Override
    public String[][] getBuiltInModules()
    {
        return new String[0][];
    }

    @Override
    public String[][] getInternalModules()
    {
        return new String[][] {
            { "process", R + "process.js" }
        };
    }

    @Override
    public Collection<Class<? extends NodeModule>> getJavaModules()
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<Class<? extends NodeModule>> getInternalJavaModules()
    {
        return Collections.emptyList();
    }
}
