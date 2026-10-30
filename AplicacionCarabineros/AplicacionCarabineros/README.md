# Aplicación Carabineros

App Android (Kotlin + Jetpack Compose + Material 3 + Room) para gestionar alumnos, grupos y documentos PDF/Word.
Basada en el proyecto original, con la arquitectura por capas de GestorDocumentos.
La base de datos simulada (BaseDatosSimulada) se reemplazó por Room/SQLite.

## Puntos solicitados

| Punto | Pantalla |
|---|---|
| Pantalla de bienvenida | MainActivity |
| Registro de usuarios | RegistroActivity |
| Editar datos de usuario | EditarUsuarioActivity (menú ☰ o Perfil) |
| Iniciar sesión | InicioSesionActivity |
| Vista de bienvenida | PrincipalActivity |
| Cargar documentos | CargarDocumentoActivity |
| Ver documentos | ListaDocumentosActivity, DetalleDocumentoActivity, VisorPdfActivity |
| Eliminar documento | DetalleDocumentoActivity |
| Cerrar sesión | Alerta en la barra superior + CerrarSesionActivity |

Del proyecto original: ListaAlumnos, AgregarAlumno, DetalleAlumno, ListaGrupos, AgregarGrupo, barras (Barras.kt).

## Estructura

```
app/src/main/java/com/example/aplicacioncarabineros/
├── model/          Usuario, Alumno, Grupo, Documento, Relaciones (tablas Room)
├── data/           DAOs, AppDatabase, repositorios, SesionManager
├── util/           Validaciones (incluye RUT), Seguridad, Formato, ArchivoUtils
├── ui/theme/       Tema Material 3 verde institucional
├── ui/componentes/ Barras, CampoTexto, Navegacion, Tarjetas
└── *Activity.kt    Pantallas
```
