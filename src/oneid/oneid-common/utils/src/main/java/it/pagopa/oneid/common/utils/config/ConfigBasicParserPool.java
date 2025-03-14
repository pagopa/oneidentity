package it.pagopa.oneid.common.utils.config;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import java.util.HashMap;
import java.util.Map;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

@Singleton
public class ConfigBasicParserPool {

  @Singleton
  @Produces
  BasicParserPool basicParserPool() throws ComponentInitializationException {
    BasicParserPool parserPool = new BasicParserPool();
    parserPool.setMaxPoolSize(100);
    parserPool.setCoalescing(true);
    parserPool.setIgnoreComments(true);
    parserPool.setIgnoreElementContentWhitespace(true);
    parserPool.setNamespaceAware(true);
    parserPool.setExpandEntityReferences(false);
    parserPool.setXincludeAware(false);

    final Map<String, Boolean> features = new HashMap<String, Boolean>();
    features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
    features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
    features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
    features.put("http://apache.org/xml/features/validation/schema/normalized-value",
        Boolean.FALSE);
    features.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);

    parserPool.setBuilderFeatures(features);
    parserPool.setBuilderAttributes(new HashMap<String, Object>());
    parserPool.initialize();
    return parserPool;
  }

}
