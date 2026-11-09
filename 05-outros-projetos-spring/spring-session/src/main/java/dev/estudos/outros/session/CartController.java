package dev.estudos.outros.session;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {
    @GetMapping("/cart/add")
    String add(HttpSession s) {
        int n = (int) Optional.ofNullable(s.getAttribute("n")).orElse(0) + 1;
        s.setAttribute("n", n);
        return "n=" + n;
    }
}
