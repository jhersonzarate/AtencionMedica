// Botón "Mostrar/Ocultar" de los campos de contraseña.
document.querySelectorAll('[data-mostrar]').forEach(function (boton) {
    boton.addEventListener('click', function () {
        var campo = document.getElementById(boton.getAttribute('data-mostrar'));
        var ver = campo.type === 'password';
        campo.type = ver ? 'text' : 'password';
        boton.textContent = ver ? 'Ocultar' : 'Mostrar';
    });
});
