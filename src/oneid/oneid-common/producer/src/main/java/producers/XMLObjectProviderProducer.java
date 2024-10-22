package producers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;

public class XMLObjectProviderProducer {

  @ApplicationScoped
  @Produces
  public MarshallerFactory produceMarshallerFactory() {
    return XMLObjectProviderRegistrySupport.getMarshallerFactory();
  }
}