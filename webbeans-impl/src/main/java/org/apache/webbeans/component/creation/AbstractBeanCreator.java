/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.webbeans.component.creation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.DefinitionUtil;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Abstract implementation.
 * 
 * @version $Rev$ $Date$
 *
 * @param <T> bean class info
 */
public class AbstractBeanCreator<T> implements BeanCreator<T>
{
    /**Bean instance*/
    private final AbstractOwbBean<T> bean;    
    
    /**Default metadata provider*/
    private MetaDataProvider metadataProvider = MetaDataProvider.DEFAULT;
    
    /**Third party producer or null if not set*/
    private Producer<T> producer;
    
    /**Bean annotations*/
    private final Annotation[] beanAnnotations;
    
    /**If annotated type is set by ProcessAnnotatedType event, used this annotated type
     * to define bean instance intead of using class artifacts.
     */
    private AnnotatedType<T> annotatedType;
    
    /**Producer set or not*/
    private boolean producerSet = false;
    
    /**
     * Creates a bean instance.
     * 
     * @param bean bean instance
     * @param beanAnnotations annotations
     */
    public AbstractBeanCreator(AbstractOwbBean<T> bean, Annotation[] beanAnnotations)
    {
        this.bean = bean;
        this.beanAnnotations = beanAnnotations;           
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProducerSet()
    {
        return this.producerSet;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void checkCreateConditions()
    {
        //Sub-class can override this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defineApiType()
    {
        if(isDefaultMetaDataProvider())
        {
            DefinitionUtil.defineApiTypes(this.bean, this.bean.getReturnType());
        }
        else
        {
            Set<Type> types = this.annotatedType.getTypeClosure();
            this.bean.getTypes().addAll(types);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defineQualifier()
    {
        if(isDefaultMetaDataProvider())
        {
            DefinitionUtil.defineQualifiers(this.bean, this.beanAnnotations);
        }
        else
        {
            DefinitionUtil.defineQualifiers(this.bean, AnnotationUtil.getAnnotationsFromSet(this.annotatedType.getAnnotations()));
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void defineName(String defaultName)
    {
        if(isDefaultMetaDataProvider())
        {
            DefinitionUtil.defineName(this.bean, this.beanAnnotations, defaultName);
        }
        else
        {
            DefinitionUtil.defineName(this.bean, AnnotationUtil.getAnnotationsFromSet(this.annotatedType.getAnnotations()), 
                    WebBeansUtil.getManagedBeanDefaultName(annotatedType.getJavaClass().getSimpleName()));
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defineScopeType(String errorMessage)
    {
        if(isDefaultMetaDataProvider())
        {
            DefinitionUtil.defineScopeType(this.bean, this.beanAnnotations, errorMessage);
            WebBeansUtil.checkPassivationScope(getBean(), getBean().getScope());
        }
        else
        {
            DefinitionUtil.defineScopeType(this.bean, AnnotationUtil.getAnnotationsFromSet(this.annotatedType.getAnnotations()), errorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defineSerializable()
    {
        DefinitionUtil.defineSerializable(this.bean);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defineStereoTypes()
    {
        if(isDefaultMetaDataProvider())
        {
            DefinitionUtil.defineStereoTypes(this.bean, this.beanAnnotations);
        }
        else
        {
            DefinitionUtil.defineStereoTypes(this.bean, AnnotationUtil.getAnnotationsFromSet(this.annotatedType.getAnnotations()));
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public MetaDataProvider getMetaDataProvider()
    {
        return this.metadataProvider;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setMetaDataProvider(MetaDataProvider metadataProvider)
    {
        this.metadataProvider = metadataProvider;
    }
    
    /**
     * Returns true if metadata provider is default,
     * false otherwise
     * 
     * @return true if metadata provider is default
     */
    protected boolean isDefaultMetaDataProvider()
    {
        if(this.metadataProvider.equals(MetaDataProvider.DEFAULT))
        {
            return true;
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Producer<T> getProducer()
    {
        return this.producer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProducer(Producer<T> producer)
    {
        this.producer = producer;
        this.producerSet = true;
    }
    
    /**
     * {@inheritDoc}
     */
    public AbstractOwbBean<T> getBean()
    {
        return this.bean;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(T instance)
    {
        producer.dispose(instance);
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return producer.getInjectionPoints();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T produce(CreationalContext<T> creationalContext)
    {
        return producer.produce(creationalContext);
    }
    
    protected AnnotatedType<T> getAnnotatedType()
    {
        return this.annotatedType;
    }
    
    public void setAnnotatedType(AnnotatedType<T> annotatedType)
    {
        this.annotatedType = annotatedType;
    }
}