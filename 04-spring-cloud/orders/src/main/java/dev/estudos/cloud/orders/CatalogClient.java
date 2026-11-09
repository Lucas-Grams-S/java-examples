package dev.estudos.cloud.orders;

import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

/** CL05-02: a interface em vez do codigo do cliente. */
interface CatalogClient {
    @GetExchange("/products/{id}")
    Map<String, Object> product(@PathVariable long id);
}
