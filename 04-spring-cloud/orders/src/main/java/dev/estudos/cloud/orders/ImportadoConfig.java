package dev.estudos.cloud.orders;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.service.registry.ImportHttpServices;

/** CL05-08: forma atual. O nome do grupo ("catalog") vira o service id; nao precisa de propriedade. */
@Configuration
@Profile("!manual & !armadilha")
@ImportHttpServices(group = "catalog", types = CatalogClient.class)
class ImportadoConfig { }
