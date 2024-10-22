package it.pagopa.oneid.common.utils.config;

import io.quarkus.logging.Log;
import it.pagopa.oneid.common.model.exception.SAMLUtilsException;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;

@Singleton
public class ConfigXMLObjectProvider {

  @Inject
  BasicParserPool basicParserPool;

  @Singleton
  @Produces
  public MarshallerFactory produceMarshallerFactory() {
    try {
      initialize();
    } catch (SAMLUtilsException e) {
      Log.error("Unable to initialize MarshallerFactory: " + e.getMessage());
      throw new RuntimeException(e);
    }
    return XMLObjectProviderRegistrySupport.getMarshallerFactory();
  }

  private void initialize() throws SAMLUtilsException {
    try {
      XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
      ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
      registry.setParserPool(basicParserPool);
      InitializationService.initialize();
    } catch (InitializationException e) {
      throw new SAMLUtilsException(e);
    }
  }
}