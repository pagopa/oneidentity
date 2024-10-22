package producers;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;

@Singleton
public class XMLObjectProviderProducer {

  @Singleton
  @Produces
  public MarshallerFactory produceMarshallerFactory() {
    return XMLObjectProviderRegistrySupport.getMarshallerFactory();
  }
}