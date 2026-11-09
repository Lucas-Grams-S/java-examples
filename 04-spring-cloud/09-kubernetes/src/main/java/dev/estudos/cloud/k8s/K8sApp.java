package dev.estudos.cloud.k8s;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CL09-08/09: orders lê ConfigMap/Secret e descobre serviços pela API do Kubernetes.
 * Dentro do cluster (imagem + k8s/deployment.yaml) ou, nos testes, fora dele apontando
 * para a API do k3s (propriedades {@code spring.cloud.kubernetes.client.*}).
 */
@SpringBootApplication
public class K8sApp {
    public static void main(String[] args) { SpringApplication.run(K8sApp.class, args); }
}
