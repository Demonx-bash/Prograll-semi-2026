# Proyecto: Indusave - Gestión Logística Inteligente

## Descripción
Indusave es un sistema de gestión logística diseñado para el control de inventarios, despacho de materiales y auditoría de desperdicios (mermas). El sistema garantiza la integridad de los datos mediante una arquitectura de persistencia híbrida (SQLite + CouchDB), permitiendo operaciones offline con sincronización automática.

## Funcionalidades Principales
- **Autenticación Segura:** Control de acceso para administradores.
- **Gestión de Materiales:** Registro detallado con cálculo automático de mermas y alertas de eficiencia.
- **Auditoría de Bodega:** Gestión de despachos con soporte para evidencias multimedia.
- **Persistencia Offline:** Sincronización bidireccional en tiempo real con Apache CouchDB.

## 📋 Guía de Evaluación (Paso a Paso)
Para realizar una prueba exitosa del sistema, siga este flujo:

1. **Acceso al Sistema:**

   - Usuarios: `admin`/ `operario`
   - Contraseña: `12345`

2. **Registro de Materiales:**
   - Navegue al apartado **Registrar Material**.
   - Ingrese un código, descripción, medida original y sobrante.
   - Presione **Registrar** y verifique la sincronización con la nube (Aviso: "Sincronizado con Éxito").

3. **Validación de Eficiencia:**
   - El sistema mostrará un indicador de eficiencia.
   - *Prueba de alerta:* Registre un material con un alto porcentaje de sobrante para visualizar la alerta roja de desperdicio.

4. **Gestión de Bodega:**
   - Acceda a **Ver Bodega** para visualizar pedidos pendientes.
   - Utilice la opción de **Eliminar Pedido** para verificar la eliminación de datos en la base de datos local y remota.

5. **Verificación en Servidor:**
   - Acceda a `http://[IP_PC]:5986/_utils/` desde un navegador para auditar los documentos en `indusave_db`.

## Tecnologías Utilizadas
- **Frontend:** Android SDK (Java).
- **Persistencia Local:** SQLite.
- **Sincronización:** Apache CouchDB / Volley (REST API).
- **Documentación:** Git & GitHub.
