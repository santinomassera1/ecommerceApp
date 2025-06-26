package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Cart;
import com.example.vintagevogue.model.CartItem;
import com.example.vintagevogue.model.Order;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.CartService;
import com.example.vintagevogue.service.OrderService;
import com.example.vintagevogue.service.ProductService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/purchase")
public class PurchaseController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;

    // Mostrar la página de compra
    @GetMapping
    public String showPurchasePage(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Obtener el carrito completo y los artículos en el carrito
        Cart cart = cartService.getCartByUser(user);
        List<CartItem> cartItems = cartService.getCartItemsByUser(user);

        // Añadir los items y el total calculado al modelo
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cartService.calculateCartTotal(cart));

        return "purchase"; // Asegúrate de que purchase.html esté configurado
    }

    @PostMapping("/confirm")
    public String confirmPurchase(Authentication authentication) {
        // Redirigir a la selección del método de pago
        return "redirect:/purchase/bank-transfer";
    }

    @GetMapping("/confirmation")
    public String showConfirmationPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Cart cart = cartService.getCartByUser(user);
        List<CartItem> cartItems = cartService.getCartItemsByUser(user);
        BigDecimal total = cartService.calculateCartTotal(cart);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("confirmationMessage", "Thank you for your purchase!");

        // Cambia aquí a la vista unificada
        return "confirmation";
    }

    // Mostrar formulario de datos para transferencia bancaria
    @GetMapping("/bank-transfer")
    public String showBankTransferForm(Model model) {
        model.addAttribute("accountDetails", getBankAccountDetails());
        return "bank-transfer";
    }

    // Procesar los datos de transferencia ingresados por el usuario
    @PostMapping("/process-bank-transfer")
    public String processBankTransfer(@RequestParam("accountNumber") String accountNumber,
                                      @RequestParam("expirationDate") String expirationDate,
                                      @RequestParam("securityCode") String securityCode,
                                      @RequestParam("cardholderName") String cardholderName,
                                      @RequestParam(value = "shippingAddress", required = false) String shippingAddress,
                                      Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Validar datos de la tarjeta
        if (!validateCardDetails(accountNumber, expirationDate, securityCode, cardholderName)) {
            model.addAttribute("error", "Los datos de la tarjeta son inválidos. Por favor, verifica la información.");
            model.addAttribute("accountDetails", getBankAccountDetails());
            return "bank-transfer"; // Volver al formulario con mensaje de error
        }

        // Validar dirección de envío
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            shippingAddress = "Dirección no especificada";
        }

        // Agregar los detalles de la compra al modelo ANTES de procesar la compra
        Cart cart = cartService.getCartByUser(user);
        List<CartItem> cartItems = cartService.getCartItemsByUser(user);
        
        // Verificar que el carrito no esté vacío
        if (cartItems.isEmpty()) {
            return "redirect:/home?error=El+carrito+está+vacío";
        }
        
        BigDecimal total = cartService.calculateCartTotal(cart);

        // CREAR LA ORDEN DE ENTREGA
        Order order = orderService.createOrderFromCart(user, cartItems, shippingAddress);

        // PROCESAR LA COMPRA (esto marca productos como vendidos y limpia el carrito)
        cartService.processPurchase(user);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("order", order);
        model.addAttribute("confirmationMessage", "¡Compra realizada exitosamente! Número de seguimiento: " + order.getTrackingNumber());

        // Redirigir a la vista de confirmación final
        return "order-confirmation";
    }
    
    /**
     * Valida los datos de la tarjeta de crédito
     */
    private boolean validateCardDetails(String accountNumber, String expirationDate, String securityCode, String cardholderName) {
        // Validar número de tarjeta (16 dígitos)
        String cardNumber = accountNumber.replaceAll("\\s", "");
        if (!cardNumber.matches("\\d{16}")) {
            return false;
        }
        
        // Validar fecha de expiración (formato MM/YY)
        if (!expirationDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            return false;
        }
        
        // Verificar que la fecha no esté expirada
        try {
            String[] parts = expirationDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Convertir a año de 4 dígitos
            
            Calendar expiryDate = Calendar.getInstance();
            expiryDate.set(Calendar.YEAR, year);
            expiryDate.set(Calendar.MONTH, month - 1); // Calendar.MONTH es 0-indexed
            expiryDate.set(Calendar.DAY_OF_MONTH, 1);
            
            if (expiryDate.getTime().before(new Date())) {
                return false; // Tarjeta expirada
            }
        } catch (Exception e) {
            return false; // Error al parsear la fecha
        }
        
        // Validar código de seguridad (3 dígitos)
        if (!securityCode.matches("\\d{3}")) {
            return false;
        }
        
        // Validar nombre del titular
        return cardholderName != null && !cardholderName.trim().isEmpty();
    }

    // Datos de la cuenta bancaria (pueden cargarse desde configuración)
    private String getBankAccountDetails() {
        return "Bank: Banco Santander\n" +
                "Account Number: 123-456-789\n" +
                "CBU: 0012345678901234567890\n" +
                "Alias: VINTAGE.VOGUE.BANCO\n";
    }


    @GetMapping("/success")
    public String showSuccessPage(Model model, @RequestParam("method") String paymentMethod, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Obtener el carrito y sus items antes de procesarlo
        Cart cart = cartService.getCartByUser(user);
        List<CartItem> cartItems = new ArrayList<>(cart.getItems()); // Copiamos los items para mostrarlos en la vista
        BigDecimal total = cartService.calculateCartTotal(cart);

        // Pasamos los datos a la vista
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("paymentMethod", paymentMethod);

        // PROCESAR LA COMPRA (esto marca productos como vendidos y limpia el carrito)
        cartService.processPurchase(user);

        return "order-confirmation";
    }

    @GetMapping("/instant-buy/{productId}")
    public String instantBuy(@PathVariable Long productId, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        try {
            // Verificar si el producto pertenece al usuario actual
            if (productService.isProductOwnedByUser(productId, user)) {
                return "redirect:/home?error=No+puedes+comprar+tu+propio+producto";
            }
            
            // Añadir el producto al carrito en lugar de procesarlo instantáneamente
            cartService.addItemToCart(user, productId, 1);
            
            // Redirigir a la página de compra normal
            return "redirect:/purchase";
        } catch (Exception e) {
            // En caso de error, redirigir al home con mensaje de error
            return "redirect:/home?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/instant-success/{productId}")
    public String showInstantPurchaseSuccess(@PathVariable Long productId, Model model, Authentication authentication) {

        // Para simplificar, solo mostramos un mensaje de éxito
        model.addAttribute("confirmationMessage", "¡Compra instantánea realizada exitosamente!");
        model.addAttribute("paymentMethod", "instant");
        
        return "order-confirmation";
    }
}
