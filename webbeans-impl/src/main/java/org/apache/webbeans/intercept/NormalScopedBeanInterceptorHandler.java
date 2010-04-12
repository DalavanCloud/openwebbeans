/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.intercept;

import java.lang.reflect.Method;
import java.util.List;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.context.AbstractContext;
import org.apache.webbeans.context.creational.CreationalContextFactory;
import org.apache.webbeans.context.creational.CreationalContextImpl;


/**
 * Normal scoped beans interceptor handler.
 * @version $Rev$ $Date$
 *
 */
@SuppressWarnings("unchecked")
public class NormalScopedBeanInterceptorHandler extends InterceptorHandler 
{
    /**Serial id*/
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new bean instance
     * @param bean bean 
     * @param creationalContext creational context
     */
    public NormalScopedBeanInterceptorHandler(OwbBean<?> bean, CreationalContext<?> creationalContext)
    {
        super(bean);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object instance, Method method, Method proceed, Object[] arguments) throws Exception
    {
        CreationalContextImpl<?> creationalContext = null;
        
        //Context of the bean
        Context webbeansContext = getBeanManager().getContext(bean.getScope());

        if (webbeansContext instanceof AbstractContext)
        {
            creationalContext = (CreationalContextImpl<?>) ((AbstractContext)webbeansContext).getCreationalContext(bean);
        }
        if (creationalContext == null)
        {
            // if there was no CreationalContext set from external, we create a new one
            creationalContext = (CreationalContextImpl<?>) CreationalContextFactory.getInstance().getCreationalContext(bean);
        }
        
        //Get instance from context
        Object webbeansInstance = getContextualInstance((OwbBean<Object>) this.bean, creationalContext);
        
        //Call super
        return super.invoke(webbeansInstance, method, proceed, arguments, creationalContext);
    }
        
    /**
     * {@inheritDoc}
     */
    protected Object callAroundInvokes(Method proceed, Object[] arguments, List<InterceptorData> stack, CreationalContextImpl<?> creationalContext)
    throws Exception
    {
        InvocationContextImpl impl = new InvocationContextImpl(this.bean, 
                                                               getContextualInstance((OwbBean<Object>) this.bean, creationalContext),
                                                               proceed, arguments, stack, InterceptorType.AROUND_INVOKE);
        impl.setCreationalContext(creationalContext);

        return impl.proceed();

    }
    
    
    /**
     * Gets instance from context.
     * @param bean bean instance
     * @param creationalContext
     * @return the underlying contextual instance, either cached or resolved from the context 
     */
    protected Object getContextualInstance(OwbBean<Object> bean, CreationalContextImpl<?> creationalContext)
    {
        Object webbeansInstance = null;
        
        //Context of the bean
        Context webbeansContext = getBeanManager().getContext(bean.getScope());
        
        //Already saved in context?
        webbeansInstance=webbeansContext.get(bean);
        if (webbeansInstance != null)
        {
            // voila, we are finished if we found an existing contextual instance
            return webbeansInstance;
        }
        
        // finally, we create a new contextual instance
        webbeansInstance = webbeansContext.get((Contextual<Object>)this.bean, (CreationalContext<Object>) creationalContext);
        
        return webbeansInstance;
    }
}
