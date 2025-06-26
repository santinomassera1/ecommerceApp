/**
 * Script para manejar el comportamiento del navbar
 */
$(document).ready(function() {
    // Detectar scroll para aplicar efectos al navbar
    $(window).scroll(function() {
        if ($(this).scrollTop() > 50) {
            $('.navbar.fixed-top').addClass('scrolled');
        } else {
            $('.navbar.fixed-top').removeClass('scrolled');
        }
        
        // Mostrar/ocultar botón de volver arriba
        if ($(this).scrollTop() > 300) {
            $('#backToTop').addClass('show');
        } else {
            $('#backToTop').removeClass('show');
        }
    });
    
    // Inicializar el estado del navbar al cargar la página
    if ($(window).scrollTop() > 50) {
        $('.navbar.fixed-top').addClass('scrolled');
    }
    
    // Manejar el clic en enlaces del navbar para dispositivos móviles
    $('.navbar-nav .nav-link').on('click', function() {
        if ($('.navbar-toggler').is(':visible')) {
            $('.navbar-collapse').collapse('hide');
        }
    });
    
    // Manejar el clic en el botón de volver arriba
    $('#backToTop').on('click', function() {
        $('html, body').animate({
            scrollTop: 0
        }, 800);
        return false;
    });
}); 