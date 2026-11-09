package dev.estudos.boot.deploy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Ping {
    @GetMapping("/ping") String ping() { return "pong"; }
}
