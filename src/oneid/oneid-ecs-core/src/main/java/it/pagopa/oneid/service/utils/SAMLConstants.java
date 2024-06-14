package it.pagopa.oneid.service.utils;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public abstract class SAMLConstants {

    @ConfigProperty(name = "metadata_url")
    public static String METADATA_URL;

    @ConfigProperty(name = "service_provider_uri")
    public static String SERVICE_PROVIDER_URI;

}
