/*
 *  NamespaceHandler.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 22/Jan/2008
 *
 *  $Id: NamespaceHandler.java,v 1.1 2011/01/13 16:50:45 textmine Exp $
 */

package gate.util.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for the http://gate.ac.uk/ns/spring
 * namespace.
 */
public class NamespaceHandler extends NamespaceHandlerSupport {

  public void init() {
    registerBeanDefinitionParser("init", new InitBeanDefinitionParser());
    registerBeanDefinitionParser("url", new UrlBeanDefinitionParser());
    registerBeanDefinitionParser("feature-map",
            new FeatureMapBeanDefinitionParser());
    registerBeanDefinitionParser("resource", new ResourceBeanDefinitionParser());
    registerBeanDefinitionParser("saved-application",
            new SavedApplicationBeanDefinitionParser());
    registerBeanDefinitionParser("duplicate", new DuplicateBeanDefinitionParser());
    registerBeanDefinitionParser("set-parameter",
            new SetParameterBeanDefinitionParser());
    registerBeanDefinitionParser("add-pr", new AddPRBeanDefinitionParser());
    registerBeanDefinitionDecorator("pooled-proxy",
            new PooledProxyBeanDefinitionDecorator());
  }

}
