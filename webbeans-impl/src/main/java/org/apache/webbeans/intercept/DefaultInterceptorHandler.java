/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.intercept;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;

import org.apache.webbeans.proxy.InterceptorHandler;
import org.apache.webbeans.util.ExceptionUtil;

public class DefaultInterceptorHandler<T> implements InterceptorHandler
{

    private T target;
    private List<Method> methods;
    private Map<Method, List<Interceptor<T>>> interceptors;
    private Map<Interceptor<T>, T> instances;

    public DefaultInterceptorHandler(T target,
                                     List<Method> methods,
                                     Map<Method, List<Interceptor<T>>> interceptors,
                                     Map<Interceptor<T>, T> instances)
    {
        this.target = target;
        this.methods = methods;
        this.instances = instances;
        this.interceptors = interceptors;
    }

    public Object invoke(int index, Object[] parameters)
    {
        try
        {
            Method method = methods.get(index);
            List<Interceptor<T>> interceptors = this.interceptors.get(method);
            InterceptorInvocationContext<T> ctx
                = new InterceptorInvocationContext<T>(target, InterceptionType.AROUND_INVOKE, interceptors, instances, method, parameters);
            return ctx.proceed();
        }
        catch (Exception e)
        {
            return ExceptionUtil.throwAsRuntimeException(e);
        }
    }
}
