<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Página no encontrada | 404</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <style>
        body {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            text-align: center;
            background-color: var(--pj-bg-body);
            padding: 1.5rem;
        }
        .error-card {
            background-color: #ffffff;
            border: 1px solid var(--pj-card-border);
            border-radius: var(--pj-radius-md);
            padding: 3rem 2.5rem;
            max-width: 480px;
            box-shadow: var(--pj-shadow-md);
        }
        .error-code {
            font-size: 4rem;
            font-weight: 800;
            color: var(--pj-burgundy);
            line-height: 1;
            margin-bottom: 0.5rem;
        }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="eyebrow">Error del Sistema</div>
        <div class="error-code">404</div>
        <h2 style="font-size: 1.45rem; font-weight: 600; margin-bottom: 0.5rem;">Página no encontrada</h2>
        <p style="font-size: 0.92rem; color: var(--pj-text-secondary); margin-bottom: 1.75rem;">
            El recurso o módulo institucional solicitado no existe o ha sido reubicado.
        </p>
        <a href="${pageContext.request.contextPath}/inicio" class="btn btn-primary">
            Volver al inicio
        </a>
    </div>
</body>
</html>

