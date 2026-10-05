## Requirements specification

Esta sección permite que el equipo realice, en base al análisis de la información obtenida en las investigaciones de campo, el Needfinding y el taller de Big Picture Event Storming, la especificación formal de los requisitos de los productos digitales que integran el ecosistema Viora. La especificación articula las necesidades de los productores olivareros independientes y de los gestores técnicos de organizaciones olivareras con los servicios de backend y la presencia digital del negocio.

### User Stories

En esta sección se definen los requisitos del ecosistema mediante Épicas e Historias de Usuario, estructuradas a partir de la línea de tiempo agronómica y los focos de mayor incertidumbre identificados en el Big Picture Event Storming (especialmente "Unmeasured Crop Load" y "Lack of History"). Los requisitos abarcan las funcionalidades del dominio transaccional de las aplicaciones móviles (Android nativa y multiplataforma), el sitio web estático (*Landing Page*) orientado a la conversión y transparencia de tarifas, las Technical Stories desde la perspectiva del desarrollador cliente para la integración de contratos RESTful, y las *Spike Stories* destinadas a reducir riesgos técnicos mediante aprendizaje autónomo.

Todos los criterios de aceptación son comprobables y siguen estrictamente la estructura BDD (*Behavior-Driven Development*) con sintaxis Gherkin (*Given-When-Then*) en tiempo presente y tercera persona, sin hacer referencia a detalles efímeros de interfaz gráfica y modelando escenarios representativos de éxito, validación y control de excepciones.

A continuación, se presentan las 15 Épicas definidas para el ecosistema Viora, estructuradas bajo el formato estándar establecido:

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP01} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP01} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Gestión de Identidad, Acceso y Perfil de Usuario} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} registrarme, autenticarme con credenciales seguras y gestionar mis datos de contacto, \textbf{para} acceder a la plataforma con las vistas, privilegios y seguridad correspondientes a mi rol.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP02} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP02} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Gestión de Suscripciones SaaS y Membresía Cooperativa} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} gestionar mi modalidad de suscripción (mediante pago digital en línea para planes individuales o canje de código de activación de cooperativa) y administrar cupos corporativos, \textbf{para} habilitar y mantener activo el acceso a los servicios de la plataforma.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP03} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP03} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Delimitación Georreferenciada y Gestión de Parcelas} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} delimitar espacialmente mis parcelas con el sensor GPS interno y mapas satelitales, registrando la caracterización agronómica de variedad de olivo cultivada, marco de plantación y densidad de árboles por hectárea, así como consultar la cartera georreferenciada de predios socios, \textbf{para} estructurar la base territorial y dendrométrica del olivar y optimizar las rutas de asistencia técnica en campo.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP04} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Monitoreo Agroclimático y Dispositivos de Parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} vincular nodos sensores a mis lotes, consultar las series periódicas de temperatura, humedad ambiental y suelo, y revisar los pronósticos meteorológicos de la zona, \textbf{para} vigilar el microclima de mi unidad productiva y anticipar condiciones climáticas desfavorables.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP05} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP05} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Diagnóstico Histórico de Vecería y Cómputo de Frío Invernal} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} registrar las cosechas de campañas previas, calcular automáticamente mi índice BBI de alternancia y monitorear la acumulación de frío invernal con alerta por picos térmicos de efecto ENOS, para conocer la severidad histórica de la alternancia en mi fundo y prever el potencial floral de la temporada.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP06} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP06} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Regulación de Carga Frutal y Prescripción de Aclareo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} realizar muestreos guiados de cuajado en campo operando sin conexión a internet mediante persistencia local en el dispositivo y sincronización automática al recuperar cobertura, calcular la carga frutal objetivo sostenible y recibir prescripciones in-app de intensidad y ventana de aclareo, \textbf{para} remover el exceso de fruta a tiempo antes del endurecimiento del carozo y mitigar la vecería prolongada en los años de menor rendimiento.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP07} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP07} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Cierre de Campaña, Balance Productivo y Reportes Técnicos} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} asentar los kilogramos cosechados al término del ciclo agrícola, visualizar la curva comparativa de estabilización interanual frente al año base y generar la ficha técnica en PDF con la trazabilidad agronómica de mi predio, \textbf{para} certificar el rendimiento del lote, sustentar financiamiento agrícola y demostrar ante compradores la consistencia productiva de mi olivar.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP08} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Gestor Técnico de Cooperativa} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP08} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Inteligencia Territorial Cooperativa y Proyecciones de Acopio} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Gestor Técnico de Cooperativa, \textbf{quiero} consultar un semáforo de riesgo fenológico que priorice parcelas socias con sobrecarga frutal o déficit térmico, y generar proyecciones tempranas del volumen global de acopio discriminadas por aceituna verde y negra, \textbf{para} orientar las visitas de campo a los predios más vulnerables, optimizar la capacidad de salmueras en planta y respaldar compromisos comerciales de exportación.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP09} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Presencia Web, Propuesta de Valor y Conversión de Usuarios} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} visitante del sitio web de Viora, \textbf{quiero} conocer la propuesta de valor para mitigar la vecería prolongada y sostener la productividad del olivar en los años de menor cosecha, consultar las tarifas transparentes en Soles (PEN), conocer al equipo fundador, acceder a los enlaces de descarga móvil y consultar en el pie de página los Términos y Condiciones junto con las Políticas de Privacidad conforme a la Ley N° 29733, \textbf{para} evaluar la adopción de la solución digital con total respaldo legal e instalar la aplicación en mi dispositivo.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP10} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Integración de Servicios de Identidad, Acceso y Suscripciones (IAM \& Billing)} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} consumir los endpoints de autenticación JWT, registro por roles, generación de preferencias de cobro en pasarela de pagos digitales (Sandbox) y canje de códigos de activación, \textbf{para} implementar las interfaces de control de acceso, monetización directa y membresías corporativas en las aplicaciones móviles y web.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP11} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Integración de Servicios Geoespaciales, Nodos de Campo y Clima} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} consumir los endpoints de gestión poligonal de parcelas, ciclo de vida de nodos sensores, consulta de telemetría y pronósticos meteorológicos externos, \textbf{para} implementar las vistas cartográficas satelitales, vinculación de dispositivos y paneles de monitoreo climático en la interfaz de usuario.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP12} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Integración de Servicios del Motor de Vecería, Regulación de Carga y Acopio} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} consumir los endpoints de registro de cosechas, métricas de BBI y frío, muestreos de cuajado, prescripciones de aclareo, generación de PDF y proyección agregada de acopio, para implementar los flujos agronómicos centrales y los paneles predictivos de la cooperativa en la experiencia móvil.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP13} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Ingeniero de Plataforma Core} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP13} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Fundamentos de Arquitectura Core y Estandarización de Backend} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Ingeniero de Plataforma Core, \textbf{quiero} implementar los componentes transversales de la arquitectura en capas DDD (manejo centralizado de excepciones RFC 7807, estrategias de nomenclatura ORM y contratos OpenAPI/Swagger), \textbf{para} proveer una infraestructura backend robusta, uniforme, observable y desacoplada que facilite el consumo de servicios por los desarrolladores frontend.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP14} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Equipo de Desarrollo de Viora} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP14} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Spikes de Viabilidad Técnica y Aprendizaje Autónomo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} equipo de desarrollo de Viora, \textbf{queremos} prototipar e investigar la viabilidad matemática del modelo de Erez, los mecanismos de persistencia offline-first con SQLite local y el flujo de checkout en sandbox de Mercado Pago, \textbf{para} mitigar la incertidumbre técnica y asegurar la correcta integración de tecnologías clave en la solución final.} \\ \hline
\end{tabular}
\end{table}

\begin{table}[H]
\centering
\begin{tabular}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{EP15} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Usuario del Ecosistema Viora} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP15} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Internacionalización y Localización del Ecosistema (i18n / l10n)} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario del ecosistema digital de Viora (Visitante, Productor Olivarero o Gestor Técnico), \textbf{quiero} acceder a la plataforma web y móvil en mi idioma de preferencia (Español o Inglés) con adaptación de contenidos y formatos regionales, \textbf{para} interactuar con las herramientas agronómicas y comerciales en un entorno comprensible que facilite la adopción y la expansión internacional de la plataforma.} \\ \hline
\end{tabular}
\end{table}

\clearpage

A continuación, se presentan las Historias de Usuario desarrolladas para el ecosistema Viora, formuladas con sus respectivos criterios de aceptación BDD:

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US01} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP01} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Registro de cuenta de acceso y credenciales seguras con asignación de rol} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario nuevo de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} registrar una cuenta en la plataforma ingresando mi correo electrónico, una contraseña segura y seleccionando mi rol de trabajo, \textbf{para} darme de alta en el sistema y disponer de una identidad de acceso que me permita autenticarme.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Creación exitosa de cuenta de acceso con asignación de rol}\newline
\textbf{Given} un usuario que no posee una cuenta registrada en la plataforma.\newline
\textbf{When} solicita su registro proporcionando un correo electrónico válido, una contraseña segura y su rol de trabajo (Productor Olivarero o Gestor Técnico).\newline
\textbf{Then} el sistema crea la cuenta de acceso en estado activo con el rol correspondiente asignado.\newline
\textbf{And} el usuario puede autenticarse satisfactoriamente con esas credenciales.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por correo electrónico ya registrado}\newline
\textbf{Given} un usuario que intenta registrarse en el sistema.\newline
\textbf{When} ingresa una dirección de correo electrónico que ya se encuentra asociada a una cuenta existente.\newline
\textbf{Then} el sistema rechaza la solicitud impidiendo duplicar identidades de acceso.\newline
\textbf{And} notifica que el correo electrónico ya se encuentra registrado en la plataforma.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por contraseña que incumple estándar de seguridad}\newline
\textbf{Given} un usuario que solicita el registro de una nueva cuenta de acceso.\newline
\textbf{When} ingresa una contraseña que posee menos de 8 caracteres o carece de combinación alfanumérica.\newline
\textbf{Then} el sistema rechaza la operación sin registrar la cuenta.\newline
\textbf{And} notifica el incumplimiento de las políticas de complejidad requeridas para la contraseña.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US02} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP01} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Inicio de sesión y autenticación persistente mediante tokens} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario registrado de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} autenticarme con mi correo electrónico y contraseña para mantener mi sesión activa en el dispositivo móvil mediante tokens seguros, \textbf{para} operar de forma continua y protegida en la gestión de mis predios o cartera cooperativa sin tener que reingresar credenciales continuamente durante mis labores agrícolas en campo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Autenticación exitosa con emisión de tokens de acceso y actualización}\newline
\textbf{Given} un usuario que posee una cuenta activa registrada en la plataforma.\newline
\textbf{When} solicita el inicio de sesión proporcionando su correo electrónico y contraseña correcta.\newline
\textbf{Then} el sistema valida satisfactoriamente la identidad del usuario.\newline
\textbf{And} emite un token de acceso seguro (JWT) con los privilegios de su rol ("ROLE\_PRODUCTOR" o "ROLE\_GESTOR") junto con un token de actualización.\newline
\textbf{And} establece la sesión persistente en el dispositivo autorizando las operaciones subsiguientes.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Denegación de acceso por credenciales inválidas}\newline
\textbf{Given} un usuario que solicita el inicio de sesión en el sistema.\newline
\textbf{When} proporciona una contraseña incorrecta o un correo electrónico no registrado.\newline
\textbf{Then} el sistema rechaza la autenticación sin generar tokens de sesión.\newline
\textbf{And} notifica que las credenciales son inválidas sin detallar cuál de los datos es el erróneo por motivos de seguridad.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Renovación transparente de sesión mediante token de actualización}\newline
\textbf{Given} un usuario con sesión iniciada en el dispositivo móvil cuyo token de acceso ha expirado.\newline
\textbf{When} la aplicación realiza una solicitud a los servicios presentando un token de actualización válido y vigente.\newline
\textbf{Then} el sistema valida el token de actualización y emite un nuevo token de acceso.\newline
\textbf{And} ejecuta la operación solicitada sin interrumpir las labores del usuario en campo ni requerir el reingreso manual de credenciales.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US03} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP01} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta y actualización de datos de perfil y contacto} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario autenticado de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} consultar y modificar mis datos personales, país y número telefónico en mi perfil, \textbf{para} mantener actualizada mi información de contacto y facilitar las coordinaciones operativas y de asistencia técnica entre productores y la administración cooperativa.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Actualización exitosa de datos de contacto}\newline
\textbf{Given} un usuario autenticado que accede a la gestión de su perfil.\newline
\textbf{When} actualiza su nombre completo o número celular proporcionando un valor conforme a la norma E.164 según su país de residencia.\newline
\textbf{Then} el sistema persiste los cambios en los datos del usuario.\newline
\textbf{And} confirma la actualización manteniendo la información de contacto vigente para las coordinaciones operativas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por número telefónico con formato incompatible}\newline
\textbf{Given} un usuario autenticado editando sus datos de contacto.\newline
\textbf{When} ingresa un número telefónico que no cumple con el estándar E.164 o cuya longitud no corresponde al país seleccionado.\newline
\textbf{Then} el sistema deniega la actualización sin modificar el registro previo.\newline
\textbf{And} notifica la inconsistencia especificando el formato telefónico esperado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por envío de atributos obligatorios vacíos}\newline
\textbf{Given} un usuario autenticado editando su información personal.\newline
\textbf{When} envía la solicitud de actualización omitiendo el nombre completo o dejándolo en blanco.\newline
\textbf{Then} el sistema rechaza la operación preservando los valores originales en la base de datos.\newline
\textbf{And} notifica que el nombre completo es un campo obligatorio para la identificación en la plataforma.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US04} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP01} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Cambio seguro de contraseña de acceso} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario autenticado de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} actualizar mi contraseña de acceso verificando mi clave actual e ingresando una nueva clave robusta, \textbf{para} proteger el acceso a mis registros agrícolas, históricos de cosecha y datos comerciales ante sospechas de vulneración y salvaguardar la privacidad de mis parcelas o cartera gremial.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Actualización exitosa de contraseña}\newline
\textbf{Given} un usuario autenticado que accede a la configuración de seguridad de su cuenta.\newline
\textbf{When} proporciona su contraseña actual correcta y define una nueva contraseña que satisface los requisitos de seguridad y es distinta a la anterior.\newline
\textbf{Then} el sistema actualiza las credenciales de acceso del usuario en la plataforma.\newline
\textbf{And} el usuario puede autenticarse satisfactoriamente utilizando únicamente la nueva contraseña definida.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por contraseña actual incorrecta}\newline
\textbf{Given} un usuario autenticado que solicita el cambio de su clave de acceso.\newline
\textbf{When} ingresa una contraseña actual que no coincide con la registrada en el sistema.\newline
\textbf{Then} el sistema rechaza la solicitud sin modificar las credenciales persistidas.\newline
\textbf{And} notifica que la contraseña actual es errónea.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por nueva contraseña que incumple políticas de complejidad}\newline
\textbf{Given} un usuario autenticado que solicita la actualización de su contraseña.\newline
\textbf{When} valida satisfactoriamente su clave actual pero ingresa una nueva contraseña con menos de 8 caracteres o sin combinación alfanumérica.\newline
\textbf{Then} el sistema bloquea el cambio sin persistir modificaciones.\newline
\textbf{And} notifica los criterios de complejidad requeridos para la nueva contraseña.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 4: Rechazo por nueva contraseña idéntica a la actual}\newline
\textbf{Given} un usuario autenticado que intenta modificar su clave de acceso.\newline
\textbf{When} ingresa como nueva contraseña exactamente la misma clave que tiene en uso.\newline
\textbf{Then} el sistema deniega la operación impidiendo la reutilización inmediata de la misma clave.\newline
\textbf{And} notifica al usuario que la nueva contraseña debe diferir de la contraseña actual.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US05} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP01} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Recuperación de contraseña olvidada mediante enlace por correo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario registrado de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} solicitar el restablecimiento de mi clave ingresando mi correo electrónico para recibir un enlace de un solo uso, \textbf{para} recuperar el acceso a mis registros agrícolas o cartera gremial de forma autónoma sin depender de soporte técnico ni perder la trazabilidad histórica de mis parcelas.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Solicitud exitosa de enlace de recuperación}\newline
\textbf{Given} un usuario que no recuerda su contraseña y posee una cuenta activa en la plataforma.\newline
\textbf{When} solicita la recuperación de clave proporcionando su dirección de correo electrónico registrada.\newline
\textbf{Then} el sistema genera un token de seguridad temporal de un solo uso con vigencia de 15 minutos y envía el correo con el enlace de restablecimiento.\newline
\textbf{And} notifica que la solicitud ha sido procesada.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Restablecimiento exitoso de contraseña con token válido}\newline
\textbf{Given} un usuario que accede mediante un token de recuperación válido y vigente.\newline
\textbf{When} define una nueva contraseña que satisface los requisitos de complejidad y confirma su envío.\newline
\textbf{Then} el sistema actualiza la contraseña del usuario e invalida el token de recuperación utilizado.\newline
\textbf{And} el usuario puede autenticarse exitosamente utilizando su nueva contraseña.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por token de recuperación expirado o ya utilizado}\newline
\textbf{Given} un usuario que intenta restablecer su clave utilizando un enlace cuyo token ya caducó o fue consumido previamente.\newline
\textbf{When} envía la solicitud con la nueva contraseña.\newline
\textbf{Then} el sistema rechaza la operación sin modificar las credenciales del usuario.\newline
\textbf{And} notifica que el enlace de recuperación ha expirado, requiriendo generar una nueva solicitud.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 4: Manejo seguro ante solicitud con correo electrónico no registrado}\newline
\textbf{Given} un usuario que solicita la recuperación de acceso.\newline
\textbf{When} ingresa una dirección de correo electrónico que no existe en el sistema.\newline
\textbf{Then} el sistema procesa la petición sin generar tokens ni enviar correos.\newline
\textbf{And} emite la misma notificación genérica de confirmación sin revelar si el correo está o no registrado para evitar ataques de enumeración.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US06} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP02} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Suscripción individual al Plan Productor mediante pasarela de pago digital} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero independiente, \textbf{quiero} suscribirme al Plan Productor seleccionando la tarifa correspondiente a la extensión de mis parcelas y realizando el pago en línea mediante una pasarela digital segura, \textbf{para} habilitar de inmediato las herramientas de diagnóstico histórico, monitoreo climático y prescripción agronómica de Viora sin depender de intermediarios ni membresías corporativas.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Suscripción y confirmación exitosa de pago en línea}\newline
\textbf{Given} un productor olivarero independiente con cuenta activa que no posee una suscripción vigente.\newline
\textbf{When} selecciona el Plan Productor según la extensión de su predio y completa el pago a través de la pasarela digital con un medio de pago válido.\newline
\textbf{Then} el sistema registra la suscripción en estado activo.\newline
\textbf{And} el usuario puede acceder inmediatamente a las funcionalidades de diagnóstico de vecería y prescripción de aclareo de su plan.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Pago rechazado o fondos insuficientes en la pasarela}\newline
\textbf{Given} un productor iniciando el proceso de suscripción al Plan Productor.\newline
\textbf{When} la pasarela digital rechaza la transacción por fondos insuficientes o medio de pago declinado.\newline
\textbf{Then} el sistema mantiene la cuenta en estado no suscrito sin realizar cargos.\newline
\textbf{And} notifica que la transacción no pudo completarse, permitiendo reintentar la operación con otro medio de pago.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Activación asíncrona mediante confirmación de pago}\newline
\textbf{Given} una transacción de pago procesada a través de la pasarela digital.\newline
\textbf{When} el sistema recibe la confirmación electrónica del pago exitoso.\newline
\textbf{Then} el sistema valida la confirmación de la pasarela y asocia el identificador de pago a la cuenta del productor.\newline
\textbf{And} actualiza la vigencia de la suscripción anual en el sistema.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US07} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP02} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Activación de cuenta de socio mediante canje de código de cooperativa} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero socio de una cooperativa agraria, \textbf{quiero} canjear un código de activación proporcionado por mi organización, \textbf{para} habilitar el acceso completo a los servicios de Viora bajo la membresía corporativa de la cooperativa sin asumir costos individuales de suscripción.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Canje exitoso de código de activación de cooperativa}\newline
\textbf{Given} un productor olivarero autenticado que no cuenta con una membresía activa.\newline
\textbf{When} ingresa un código de invitación válido emitido por su cooperativa agraria.\newline
\textbf{Then} el sistema vincula al productor con la cooperativa correspondiente y marca el código como utilizado.\newline
\textbf{And} el usuario accede a las herramientas del sistema con estado de membresía corporativa activa.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por código de invitación inexistente o inválido}\newline
\textbf{Given} un productor ingresando un código de activación.\newline
\textbf{When} proporciona un código que no existe en el registro de invitaciones del sistema.\newline
\textbf{Then} el sistema deniega la vinculación sin alterar el estado de la cuenta.\newline
\textbf{And} notifica que el código ingresado no es válido.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por código de invitación expirado o ya canjeado}\newline
\textbf{Given} un productor intentando vincularse a una cooperativa.\newline
\textbf{When} ingresa un código cuya fecha de vigencia caducó o que ya fue consumido por otro usuario.\newline
\textbf{Then} el sistema rechaza el canje impidiendo la activación de la membresía.\newline
\textbf{And} notifica que el código ha expirado o ya no cuenta con cupos disponibles.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US08} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Gestor Técnico de Cooperativa} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP02} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Administración de la cartera de socios productores y consulta de cuota corporativa} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Gestor Técnico de Cooperativa, \textbf{quiero} consultar la nómina de socios productores agremiados con su superficie declarada y verificar el cupo contratado de la membresía colectiva, \textbf{para} supervisar la base territorial de la cooperativa y coordinar la entrega de códigos de activación a los agricultores elegibles.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta de padrón gremial}\newline
\textbf{Given} un gestor técnico autenticado con rol \texttt{GESTOR}.\newline
\textbf{When} accede al directorio de miembros de su cooperativa.\newline
\textbf{Then} el sistema retorna la lista consolidada de socios agremiados detallando nombre completo, teléfono E.164, correo, hectáreas declaradas y estado de vinculación.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Consulta de estado de licencias y códigos}\newline
\textbf{Given} un gestor técnico consultando la capacidad contratada.\newline
\textbf{When} solicita el balance de la licencia institucional.\newline
\textbf{Then} el sistema presenta las plazas y hectáreas totales contratadas frente a las comprometidas y disponibles, junto con el estado de los lotes de códigos emitidos.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Restricción de acceso}\newline
\textbf{Given} una solicitud emitida por un usuario sin rol de gestor técnico en la organización.\newline
\textbf{When} el sistema evalúa los privilegios institucionales.\newline
\textbf{Then} deniega el acceso con código \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US09} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP03} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Delimitación georreferenciada de parcela con GPS y caracterización agronómica inicial} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} delimitar el contorno de mi parcela capturando los vértices mediante el sensor GPS del dispositivo móvil o fijándolos sobre la cartografía satelital, registrando la variedad de olivo cultivada y el marco de plantación, \textbf{para} establecer la base territorial y dendrométrica de mi lote necesaria para dimensionar el potencial productivo y regular la carga frutal.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Creación exitosa de parcela con polígono cerrado y cálculo de densidad}\newline
\textbf{Given} un productor autenticado que inicia el alta de una nueva parcela en la plataforma.\newline
\textbf{When} delimita un polígono cerrado de al menos tres vértices, asigna una denominación al lote, selecciona la variedad de olivo correspondiente y define el marco de plantación.\newline
\textbf{Then} el sistema calcula la superficie en hectáreas y la densidad de árboles resultante.\newline
\textbf{And} registra la parcela en estado activo asociada a la cuenta del productor permitiendo su visualización cartográfica.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por polígono abierto o vértices insuficientes}\newline
\textbf{Given} un productor trazando los límites de su predio.\newline
\textbf{When} intenta registrar el predio con menos de tres coordenadas georreferenciadas o con un trazado perimétrico que no cierra geométricamente.\newline
\textbf{Then} el sistema deniega el registro impidiendo la creación del lote.\newline
\textbf{And} notifica que se requiere un polígono cerrado de al menos tres vértices válidos.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Trazado manual sobre mapa satelital ante ausencia de señal GPS}\newline
\textbf{Given} un productor delimitando un lote en campo sin recepción de señal satelital en el sensor GPS del dispositivo.\newline
\textbf{When} no se obtiene fijación de coordenadas satelitales directas.\newline
\textbf{Then} el sistema permite fijar manualmente los puntos perimétricos sobre la vista satelital de la zona.\newline
\textbf{And} calcula el área delimitada conservando la validez geométrica del lote.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US10} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP03} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta y modificación de linderos y datos dendrométricos de parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} consultar y actualizar los linderos perimétricos, el nombre o el marco de plantación de una parcela existente, \textbf{para} corregir mediciones topográficas tras labores de replante y mantener al día la caracterización dendrométrica del olivar.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Actualización exitosa de linderos y recálculo de área}\newline
\textbf{Given} un productor que consulta una de sus parcelas registradas.\newline
\textbf{When} ajusta la posición de uno de los vértices del polígono perimétrico y confirma los cambios.\newline
\textbf{Then} el sistema recalcula la superficie total en hectáreas.\newline
\textbf{And} actualiza la geometría del predio conservando el historial agronómico previo.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Modificación del marco de plantación y actualización de densidad}\newline
\textbf{Given} un productor editando los parámetros agronómicos de su lote.\newline
\textbf{When} modifica el marco de plantación (ej. de 10x10 a 8x8 metros) tras una renovación de árboles.\newline
\textbf{Then} el sistema recalcula automáticamente la densidad de árboles por hectárea.\newline
\textbf{And} actualiza la población vegetal estimada para los modelos de regulación de carga.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por marco de plantación incompatible con la agronomía del olivo}\newline
\textbf{Given} un productor modificando las dimensiones del marco de plantación.\newline
\textbf{When} ingresa espaciamientos negativos o valores que arrojan densidades biológicamente incompatibles con el olivar (superiores a 500 árboles/ha en sistema tradicional).\newline
\textbf{Then} el sistema bloquea la actualización sin alterar la configuración previa.\newline
\textbf{And} notifica los rangos agronómicos admisibles para la plantación.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US11} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Baja} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP03} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Baja y remoción de parcela del inventario productivo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} dar de baja o eliminar una parcela registrada por error o que ya no forma parte de mi explotación agrícola, \textbf{para} mantener ordenado mi inventario de unidades productivas y evitar asignación innecesaria de recursos o cobros.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Eliminación exitosa de una parcela sin registros históricos asociados}\newline
\textbf{Given} un productor olivarero que gestiona una parcela recientemente creada sin cosechas ni muestreos vinculados.\newline
\textbf{When} confirma la eliminación definitiva del predio.\newline
\textbf{Then} el sistema remueve la parcela del inventario de unidades productivas del usuario.\newline
\textbf{And} libera el área asociada permitiendo su reutilización en el límite del plan de suscripción.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Solicitud de confirmación ante eliminación de parcela con datos históricos}\newline
\textbf{Given} un productor que solicita dar de baja una parcela que cuenta con registros de cosechas e historial telemétrico previo.\newline
\textbf{When} inicia la solicitud de eliminación del predio.\newline
\textbf{Then} el sistema advierte sobre la pérdida permanente de la trazabilidad agronómica asociada al lote.\newline
\textbf{And} requiere una confirmación explícita para procesar la baja definitiva.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US12} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Gestor Técnico de Cooperativa} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP03} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de la matriz de riesgo territorial y semáforo sectorial con geolocalización GPS} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Gestor Técnico de Cooperativa, \textbf{quiero} consultar un tablero con la matriz de riesgo territorial del valle olivarero detectando mi posición GPS en tiempo real, \textbf{para} identificar en qué sector me encuentro y priorizar visitas de asistencia agronómica en las parcelas que presentan sobrecarga crítica o alerta de helada en dicha zona.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Detección de sector mediante GPS interno}\newline
\textbf{Given} un gestor técnico en campo con el sensor GPS activado en su dispositivo móvil.\newline
\textbf{When} abre la matriz de riesgo territorial de la cooperativa.\newline
\textbf{Then} el sistema captura las coordenadas de latitud y longitud del asesor.\newline
\textbf{And} sitúa automáticamente su posición dentro del sector geográfico correspondiente (e.g., Sector La Yarada Baja), destacando visualmente el semáforo y las alertas activas de esa zona.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Visualización consolidada de sectores del valle}\newline
\textbf{Given} un gestor técnico consultando el estado general de su organización.\newline
\textbf{When} examina el tablero macro territorial.\newline
\textbf{Then} el sistema despliega el semáforo consolidado de todos los sectores agrupando el número de predios en sobrecarga crítica (> 30\%) y el conteo de alertas climáticas activas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: GPS fuera de cobertura o sin señal}\newline
\textbf{Given} el gestor en una zona sin fijación satelital GPS o con permisos denegados.\newline
\textbf{When} accede a la matriz territorial.\newline
\textbf{Then} el sistema presenta la vista global por defecto y permite seleccionar manualmente el sector de interés.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US13} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Vinculación y alta de nodo sensor virtual a una parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} dar de alta un nodo sensor virtual (estación microclimática o sonda de humedad de suelo) en una de mis parcelas asignándole una denominación y tipo, \textbf{para} habilitar la ingesta y recepción de series telemétricas en el lote sin requerir el despliegue de hardware físico en campo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Vinculación exitosa de nodo sensor virtual}\newline
\textbf{Given} un productor olivarero autenticado que gestiona una parcela registrada.\newline
\textbf{When} registra un nodo sensor virtual indicando un nombre descriptivo y seleccionando el tipo de sensor (microclima o sonda de suelo).\newline
\textbf{Then} el sistema asocia el nodo sensor virtual a la parcela registrándolo en estado activo.\newline
\textbf{And} habilita la ingesta periódica de telemetría simulada para dicha unidad productiva.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por campos obligatorios incompletos o tipo no soportado}\newline
\textbf{Given} un productor intentando dar de alta un nodo sensor virtual.\newline
\textbf{When} omite el nombre descriptivo o selecciona un tipo de dispositivo no admitido por el sistema.\newline
\textbf{Then} el sistema deniega el registro sin modificar la configuración del lote.\newline
\textbf{And} notifica los campos obligatorios y tipos de sensores virtuales válidos.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por nombre duplicado de nodo virtual en la misma parcela}\newline
\textbf{Given} un productor registrando un nodo virtual en su parcela.\newline
\textbf{When} ingresa una denominación idéntica a la de otro nodo virtual ya existente en la misma parcela.\newline
\textbf{Then} el sistema bloquea el alta impidiendo nombres duplicados en el predio.\newline
\textbf{And} notifica que la denominación del sensor virtual debe ser única en el lote.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US14} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de inventario y estado operativo de nodos sensores virtuales en parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} consultar el inventario de nodos sensores virtuales vinculados a mi parcela y su estado de transmisión simulada, \textbf{para} comprobar qué puntos de monitoreo se encuentran activos alimentando los modelos agroclimáticos del olivar.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Listado consolidado de nodos virtuales con estado operativo activo}\newline
\textbf{Given} una parcela que cuenta con múltiples nodos sensores virtuales vinculados.\newline
\textbf{When} el productor accede al inventario de dispositivos del predio.\newline
\textbf{Then} el sistema presenta la relación de nodos virtuales detallando tipo de dispositivo, estado operativo (activo o pausado) y fecha de última telemetría generada.\newline
\textbf{And} resalta en estado activo aquellos nodos que alimentan la simulación climática actual.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Visualización de nodo virtual en estado de transmisión pausado}\newline
\textbf{Given} un nodo sensor virtual cuyo estado de transmisión ha sido pausado por el usuario o por mantenimiento de datos.\newline
\textbf{When} el productor consulta la lista de dispositivos de la parcela.\newline
\textbf{Then} el sistema clasifica el nodo en estado inactivo o en pausa.\newline
\textbf{And} notifica que las series temporales de dicho punto se encuentran temporalmente suspendidas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Consulta en parcela sin dispositivos sensores asignados}\newline
\textbf{Given} un productor que consulta una parcela que aún no posee nodos virtuales vinculados.\newline
\textbf{When} accede al módulo de sensores.\newline
\textbf{Then} el sistema informa que no existen nodos sensores virtuales asociados al lote.\newline
\textbf{And} presenta la opción de dar de alta un nuevo sensor virtual.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US15} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Baja} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Configuración y calibración de nodo sensor virtual en parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} personalizar la denominación del nodo sensor virtual y definir la profundidad de monitoreo de la sonda de suelo (30 cm o 60 cm), \textbf{para} asegurar que las lecturas telemétricas se computen en el estrato radicular correspondiente a las raíces absorbentes del olivo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Configuración exitosa de profundidad y etiqueta de sonda virtual}\newline
\textbf{Given} un productor que gestiona un sensor virtual de humedad de suelo vinculado a su parcela.\newline
\textbf{When} asigna un nombre descriptivo (ej. "Sonda Sector Norte") y selecciona la profundidad de monitoreo (30 cm o 60 cm).\newline
\textbf{Then} el sistema persiste la configuración técnica del nodo virtual.\newline
\textbf{And} asocia las series de humedad posteriores al estrato radicular seleccionado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por profundidad de sonda no soportada}\newline
\textbf{Given} un productor configurando los parámetros de una sonda virtual de suelo.\newline
\textbf{When} ingresa un valor de profundidad fuera de las opciones estándar de calibración (30 cm o 60 cm).\newline
\textbf{Then} el sistema rechaza la actualización sin alterar la configuración previa.\newline
\textbf{And} notifica los valores de profundidad admitidos para el monitoreo radicular del olivo.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US16} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Desvinculación y baja de nodo sensor virtual de una parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} dar de baja o desvincular un nodo sensor virtual de mi parcela cuando ya no requiera monitorear ese punto, \textbf{para} mantener limpio el inventario del lote preservando intacto el historial previo de telemetría registrada.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Desvinculación exitosa conservando la serie histórica de datos}\newline
\textbf{Given} un productor olivarero que gestiona un nodo sensor virtual activo en una parcela.\newline
\textbf{When} solicita y confirma la desvinculación del nodo virtual del predio.\newline
\textbf{Then} el sistema remueve el nodo sensor virtual del inventario activo de la parcela.\newline
\textbf{And} preserva intactas todas las series de telemetría y lecturas históricas registradas previamente en el lote.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Consulta de métricas históricas de parcela tras desvinculación de nodo}\newline
\textbf{Given} un productor que consulta las métricas históricas de una parcela tras la baja de un sensor virtual.\newline
\textbf{When} accede a los reportes de temporadas anteriores.\newline
\textbf{Then} el sistema presenta las series históricas generadas durante la vigencia del sensor.\newline
\textbf{And} confirma que la desvinculación no afectó los datos acumulados de campañas pasadas.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US17} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Monitoreo agroclimático y consulta de series temporales de suelo y microclima} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} consultar las lecturas periódicas de temperatura ambiental, humedad relativa y humedad del suelo registradas en mi parcela, \textbf{para} supervisar el confort hídrico del olivar y detectar oportunamente riesgos de estrés térmico en floración o déficit de humedad en cuajado.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta de telemetría para un rango temporal definido}\newline
\textbf{Given} un productor olivarero con sensores activos en su parcela.\newline
\textbf{When} consulta el historial agroclimático seleccionando un rango de fechas (últimas 24 horas, 7 días o 30 días).\newline
\textbf{Then} el sistema presenta las curvas temporales de temperatura, humedad relativa y humedad del suelo correspondientes al intervalo.\newline
\textbf{And} destaca el último valor medido con su respectiva marca de tiempo.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Visualización de promedios diurnos y nocturnos de temperatura}\newline
\textbf{Given} una parcela con lecturas horarias consolidadas durante la semana.\newline
\textbf{When} el productor consulta el resumen térmico semanal.\newline
\textbf{Then} el sistema discrimina las temperaturas promedio diurnas y nocturnas registradas en el campo.\newline
\textbf{And} calcula la oscilación térmica diaria para evaluar el estímulo fisiológico del cultivo.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US18} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Alertas automáticas de estrés hídrico y umbral térmico crítico en parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} recibir alertas automáticas en el sistema cuando la humedad del suelo caiga a niveles de estrés o la temperatura ambiental supere umbrales fisiológicos críticos, \textbf{para} adelantar turnos de riego y proteger la viabilidad del polen durante la etapa crítica de floración.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Emisión de alerta por caída de humedad de suelo a punto de recarga}\newline
\textbf{Given} una parcela con monitoreo continuo de humedad de suelo.\newline
\textbf{When} la lectura de la sonda en el estrato radicular desciende por debajo del punto de recarga configurado (ej. < 18\% de humedad volumétrica).\newline
\textbf{Then} el sistema emite una alerta de estrés hídrico de prioridad alta asociada a la parcela.\newline
\textbf{And} sugiere la programación urgente de un turno de riego en el sector afectado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Emisión de alerta por temperatura extrema durante floración}\newline
\textbf{Given} una parcela en fase de floración con telemetría de microclima activa.\newline
\textbf{When} la temperatura ambiental supera los 32°C con humedad relativa inferior al 20\% durante más de tres horas consecutivas.\newline
\textbf{Then} el sistema registra una advertencia de riesgo de desecación estigmática y aborto floral.\newline
\textbf{And} notifica al productor el peligro de reducción en la tasa de cuajado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Normalización de alerta tras restablecimiento de variables dentro de rango}\newline
\textbf{Given} una parcela con alerta activa por estrés hídrico.\newline
\textbf{When} una nueva lectura de la sonda registra una recuperación de humedad de suelo por encima del umbral seguro tras un evento de riego.\newline
\textbf{Then} el sistema actualiza el estado de la alerta a normalizada.\newline
\textbf{And} registra el tiempo total que el olivar permaneció bajo estrés hídrico.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US19} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP04} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de pronóstico meteorológico geolocalizado a 7 días} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} consultar el pronóstico del tiempo a 7 días geolocalizado para las coordenadas de mi predio, \textbf{para} anticipar condiciones climáticas desfavorables (vientos desecantes o bajadas térmicas) y programar con antelación los riegos y labores de aclareo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta exitosa de proyección meteorológica a 7 días}\newline
\textbf{Given} un productor autenticado que selecciona una de sus parcelas georreferenciadas.\newline
\textbf{When} consulta el pronóstico meteorológico del predio.\newline
\textbf{Then} el sistema presenta la proyección a 7 días con temperaturas máximas, mínimas, velocidad del viento y probabilidad de lluvia calculadas para las coordenadas del lote.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Presentación de datos en caché ante indisponibilidad del servicio externo}\newline
\textbf{Given} un productor solicitando la previsión meteorológica.\newline
\textbf{When} el servicio meteorológico externo presenta demoras o falla de conexión.\newline
\textbf{Then} el sistema muestra la última previsión almacenada en memoria caché.\newline
\textbf{And} notifica la fecha y hora de la última sincronización disponible.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US20} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP05} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Registro retrospectivo de campañas históricas de cosecha y cálculo del Índice de Vecería (BBI)} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} registrar los volúmenes totales cosechados en campañas agrícolas de años anteriores en mi parcela, \textbf{para} que el sistema calcule el Índice de Vecería de Hoblyn ($BBI$) y determine la intensidad histórica de alternancia de mi cuartel.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Cálculo exitoso de BBI con al menos tres campañas históricas}\newline
\textbf{Given} una parcela con 3 o más campañas previas registradas.\newline
\textbf{When} el productor guarda el pesaje de una campaña histórica adicional.\newline
\textbf{Then} el sistema evalúa la fórmula matemática de Hoblyn y clasifica la vecería en baja ($< 0.20$), moderada ($0.20 - 0.40$) o severa ($> 0.40$).} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Datos insuficientes para evaluación matemática}\newline
\textbf{Given} una parcela con menos de 3 campañas históricas registradas.\newline
\textbf{When} el usuario solicita la métrica de vecería.\newline
\textbf{Then} el sistema notifica que la serie histórica es insuficiente y solicita registrar cosechas anteriores sin bloquear la operatividad predial.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por volumen de cosecha negativo o año futuro}\newline
\textbf{Given} un productor ingresando datos de cosecha.\newline
\textbf{When} proporciona un pesaje negativo o un año de campaña futuro que aún no ha tenido lugar.\newline
\textbf{Then} el sistema bloquea el registro impidiendo almacenar datos inconsistentes.\newline
\textbf{And} notifica los rangos válidos para el año agrícola y los kilogramos cosechados.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US21} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Baja} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP05} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Modificación y rectificación de registros históricos de cosecha} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} corregir o actualizar las cifras de kilogramos cosechados en una campaña anterior, \textbf{para} subsanar errores de digitación de boletas de pesaje en almazara y recalcular con precisión el índice BBI histórico de la parcela.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Rectificación exitosa de pesaje y recálculo automático del BBI}\newline
\textbf{Given} una parcela con registros de cosechas plurianuales e índice BBI previamente calculado.\newline
\textbf{When} el productor modifica el pesaje en kilogramos de una campaña previa y confirma la rectificación.\newline
\textbf{Then} el sistema actualiza el registro histórico del año corregido.\newline
\textbf{And} recalcula automáticamente la serie de índices BBI de alternancia para todos los intervalos interanuales afectados.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Eliminación de un registro erróneo de cosecha}\newline
\textbf{Given} un productor que consulta el historial de cosechas de una parcela.\newline
\textbf{When} elimina un registro de campaña duplicado o erróneo.\newline
\textbf{Then} el sistema suprime el registro del historial del lote.\newline
\textbf{And} actualiza la línea base de alternancia según las campañas válidas remanentes.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US22} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP05} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Monitoreo dinámico de porciones de frío invernal acumuladas mediante el modelo de Erez} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} consultar el avance de acumulación de porciones de frío calculadas mediante el modelo dinámico de Erez durante el reposo invernal en mi parcela, \textbf{para} conocer si el olivo alcanzará el estímulo fisiológico indispensable para inducir una floración uniforme en el olivar.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta del avance periódico de porciones de frío de Erez}\newline
\textbf{Given} una parcela con registros horarios de temperatura durante los meses de invierno (mayo a agosto).\newline
\textbf{When} el productor consulta el panel de descanso invernal del lote.\newline
\textbf{Then} el sistema calcula y muestra las porciones de frío acumuladas a la fecha aplicando el modelo dinámico de Erez.\newline
\textbf{And} contrasta la cifra acumulada frente al umbral fisiológico requerido por la variedad registrada en la parcela (ej. 25 a 30 porciones para Sevillana/Criolla).} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Notificación de cumplimiento de acumulación de frío}\newline
\textbf{Given} una parcela en seguimiento de reposo invernal.\newline
\textbf{When} las porciones de frío acumuladas alcanzan el umbral óptimo de la variedad.\newline
\textbf{Then} el sistema actualiza el estado fisiológico de la parcela a estímulo térmico completado.\newline
\textbf{And} notifica al productor que el olivar cuenta con el estímulo térmico necesario para una brotación uniforme.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Consulta fuera del período invernal de acumulación}\newline
\textbf{Given} un productor accediendo al panel térmico fuera de la temporada de reposo (meses de verano u otoño).\newline
\textbf{When} solicita la lectura de acumulación de frío en curso.\newline
\textbf{Then} el sistema informa que el ciclo de acumulación de frío se encuentra inactivo.\newline
\textbf{And} expone el consolidado histórico final de la campaña invernal previa.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US23} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP05} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Detección de anomalías térmicas invernales y advertencia de riesgo floral por efecto ENOS} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} recibir advertencias tempranas en el sistema cuando se registren picos de calor anómalos durante el invierno asociados al fenómeno de El Niño, \textbf{para} anticipar una baja inducción floral y reajustar oportunamente las proyecciones de rendimiento y las metas de aclareo de la campaña.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Detección de temperaturas diurnas perjudiciales en invierno}\newline
\textbf{Given} una parcela durante la etapa de acumulación de frío invernal.\newline
\textbf{When} la temperatura ambiental diurna supera sostenidamente los 24°C durante más de tres días consecutivos destruyendo los intermediarios del frío de Erez.\newline
\textbf{Then} el sistema registra una advertencia de anomalía térmica invernal asociada al lote.\newline
\textbf{And} notifica al productor el riesgo de reversión floral y brotación exclusivamente vegetativa.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Reajuste predictivo de floración y carga frutal potencial}\newline
\textbf{Given} una parcela con advertencia activa por invierno cálido.\newline
\textbf{When} el productor consulta el detalle de la advertencia térmica.\newline
\textbf{Then} el sistema presenta el resumen del estrés térmico acumulado y actualiza la proyección de diferenciación floral a nivel crítico.\newline
\textbf{And} reajusta la estimación de carga potencial de la parcela para considerar la baja floración en los modelos de regulación de carga.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US24} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP06} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Muestreo guiado de cuajado en campo a pie de árbol con persistencia local offline} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} registrar los conteos de brotes y frutos de muestra a pie de árbol sin requerir conexión a internet, \textbf{para} asentar la densidad real de cuajado directamente en el olivar y sincronizar automáticamente las observaciones al restablecer la conectividad celular o de red.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Registro offline de conteo de frutos a pie de árbol}\newline
\textbf{Given} un productor olivarero ubicado en campo sin cobertura celular ni acceso a internet.\newline
\textbf{When} registra el número de árbol muestreado, total de brotes observados y frutos cuajados en la muestra y confirma el guardado.\newline
\textbf{Then} la aplicación móvil almacena el registro en la base de datos local del dispositivo.\newline
\textbf{And} clasifica el muestreo como pendiente de sincronización permitiendo continuar con la evaluación de los siguientes árboles.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Sincronización automática de muestreos al recuperar conexión}\newline
\textbf{Given} un dispositivo con muestreos de cuajado pendientes de sincronización en su almacenamiento local.\newline
\textbf{When} el dispositivo restablece la conectividad a internet.\newline
\textbf{Then} el sistema transmite de manera automática el lote de registros al servidor de Viora.\newline
\textbf{And} actualiza el estado de los muestreos a sincronizados sin requerir intervención manual del usuario.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por conteos fuera de rango biológico}\newline
\textbf{Given} un productor registrando datos en el protocolo de muestreo.\newline
\textbf{When} ingresa valores negativos o una cantidad de frutos cuajados que supera físicamente la capacidad biológica del brote evaluado.\newline
\textbf{Then} el sistema rechaza el ingreso impidiendo registrar mediciones inverosímiles.\newline
\textbf{And} notifica los rangos biológicos admisibles para el conteo de frutos por brote.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US25} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP06} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de representatividad estadística e historial de árboles muestreados en campo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} consultar el avance de la ronda de muestreo y revisar la lista de árboles evaluados en mi predio, \textbf{para} saber si alcancé la representatividad mínima requerida ($\ge 5$ árboles) e identificar qué árboles ya fueron evaluados a pie de campo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta de representatividad y listado de árboles evaluados}\newline
\textbf{Given} una ronda de muestreo en progreso en la parcela activa.\newline
\textbf{When} el usuario consulta el estado del muestreo en su dispositivo.\newline
\textbf{Then} el sistema presenta el indicador de suficiencia estadística (\texttt{isSampleSufficient}), la media de frutos por brote y el listado de árboles evaluados con su etiqueta física, brotes y frutos registrados.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Alcanzar el umbral de representatividad}\newline
\textbf{Given} una ronda donde se alcanza el árbol mínimo requerido ($\ge 5$ árboles válidos).\newline
\textbf{When} se sincroniza el último registro.\newline
\textbf{Then} el sistema transiciona la ronda a completada y habilita la generación de la prescripción de aclareo.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US26} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP06} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Cálculo de carga frutal objetivo sostenible y rendimiento potencial de campaña} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} que el sistema procese los muestreos de cuajado, la densidad de plantación y el área de mi predio para calcular la carga frutal máxima sostenible en frutos por árbol y kilogramos por hectárea, \textbf{para} conocer el límite productivo que el olivo puede soportar sin agotar sus reservas y evitar el colapso vegetativo de la siguiente campaña.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Cálculo exitoso de carga admisible con muestra representativa}\newline
\textbf{Given} una parcela con al menos 5 árboles representativos muestreados en campo.\newline
\textbf{When} el productor solicita el balance de carga frutal de la temporada.\newline
\textbf{Then} el sistema procesa el promedio de frutos cuajados y proyecta la carga total estimada en frutos por árbol.\newline
\textbf{And} determina la carga frutal objetivo sostenible y el rendimiento en kilogramos por hectárea según el marco de plantación del lote.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Detección de sobrecarga frutal con riesgo de vecería severa}\newline
\textbf{Given} una parcela cuyo cálculo de carga estimada supera en más del 30\% la capacidad de carga biológica calibrada para la variedad registrada en el predio.\newline
\textbf{When} se genera el cálculo de rendimiento potencial.\newline
\textbf{Then} el sistema clasifica el lote en estado de sobrecarga severa.\newline
\textbf{And} notifica al productor que el exceso de fruta inducirá una vecería prolongada si no se regula la carga a tiempo.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Bloqueo de cálculo por cantidad insuficiente de árboles evaluados}\newline
\textbf{Given} una parcela con menos de 5 árboles muestreados.\newline
\textbf{When} el productor solicita el dimensionamiento productivo.\newline
\textbf{Then} el sistema deniega el cálculo automático por falta de representatividad muestral.\newline
\textbf{And} notifica la cantidad de árboles adicionales requeridos para completar el diagnóstico.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US27} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP06} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Prescripción técnica in-app de porcentaje y ventana fenológica de aclareo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} recibir una prescripción agronómica con el porcentaje exacto de frutos a remover y la ventana de fechas límite de ejecución, \textbf{para} remover el exceso de fruta a tiempo antes del endurecimiento del carozo y asegurar un buen calibre comercial sin inducir vecería en el siguiente año.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Prescripción de aclareo ante sobrecarga frutal}\newline
\textbf{Given} una parcela con diagnóstico de sobrecarga frutal procesado durante la fase previa al endurecimiento del carozo.\newline
\textbf{When} el productor consulta la recomendación de regulación de carga.\newline
\textbf{Then} el sistema prescribe el porcentaje óptimo de remoción de fruta (ej. 30\% de aclareo).\newline
\textbf{And} define la ventana temporal recomendada con fecha de inicio y fecha límite de ejecución antes de la lignificación del carozo.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Parcela con carga frutal equilibrada que no requiere aclareo}\newline
\textbf{Given} una parcela cuya carga estimada se encuentra dentro del rango fisiológico óptimo.\newline
\textbf{When} el productor consulta la prescripción de regulación.\newline
\textbf{Then} el sistema determina un porcentaje de aclareo del 0\%.\newline
\textbf{And} notifica que la carga frutal es óptima y no requiere intervención para sostener la productividad interanual.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Advertencia por consulta posterior al endurecimiento del carozo}\newline
\textbf{Given} una parcela donde la fecha de consulta supera la ventana fenológica de aclareo con el carozo ya endurecido (lignificado).\newline
\textbf{When} el productor solicita la prescripción de aclareo.\newline
\textbf{Then} el sistema advierte que la ventana óptima de aclareo ha concluido.\newline
\textbf{And} notifica que la remoción tardía de fruto ya no evitará la inhibición hormonal de la floración de la siguiente campaña.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US28} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP06} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Registro y confirmación de ejecución de aclareo en campo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} registrar la fecha y el porcentaje real de frutos removidos durante las labores de aclareo en mi parcela, \textbf{para} asentar la ejecución de la práctica de manejo en la bitácora del lote y permitir al sistema actualizar la estimación de calibre y cosecha final.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Registro exitoso de intervención de aclareo ejecutada}\newline
\textbf{Given} un productor con una prescripción de aclareo activa para su predio.\newline
\textbf{When} registra la fecha de intervención en campo y confirma el porcentaje de fruta removido según la recomendación.\newline
\textbf{Then} el sistema actualiza la bitácora agronómica del lote marcando la labor como ejecutada.\newline
\textbf{And} recalcula la proyección de calibre comercial de aceituna esperado para la cosecha.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Notificación por ejecución registrada fuera de la ventana fenológica}\newline
\textbf{Given} un productor registrando la ejecución de aclareo.\newline
\textbf{When} la fecha ingresada es posterior a la fecha límite prescrita por endurecimiento del carozo.\newline
\textbf{Then} el sistema guarda el registro de la labor en la bitácora.\newline
\textbf{And} notifica una advertencia indicando que la eficacia para mitigar la vecería será reducida debido a la lignificación del carozo.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US29} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP07} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Asentamiento formal de cosecha de fin de campaña y balance de estabilización productiva} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} asentar formalmente el pesaje real de cosecha al término de la temporada discriminando kilos de aceituna verde y negra, \textbf{para} formalizar la liquidación de entrega y auditar la curva interanual de atenuación de vecería.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Asentamiento exitoso de liquidación de cosecha}\newline
\textbf{Given} la culminación de las faenas de cosecha de la campaña en curso.\newline
\textbf{When} el productor o la almazara asienta los kilogramos recolectados (\texttt{greenOlivesKg} y \texttt{blackOlivesKg}).\newline
\textbf{Then} el sistema emite el comprobante inmutable de liquidación, actualiza la curva de estabilización productiva y bloquea el año contra modificaciones no autorizadas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Campaña previamente liquidada}\newline
\textbf{Given} un intento de registrar un pesaje sobre una campaña ya asentada.\newline
\textbf{When} el sistema valida la unicidad anual.\newline
\textbf{Then} responde con código \texttt{409 Conflict} preservando la inmutabilidad de auditoría.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US30} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP07} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Emisión, certificación criptográfica y exportación del expediente agronómico en PDF} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Productor Olivarero, \textbf{quiero} generar el expediente agronómico oficial de mi parcela con certificación criptográfica, \textbf{para} descargar un documento PDF auditable con el historial técnico, telemetría y labores de aclareo para trámites bancarios o cooperativos.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Descarga del expediente binario en formato PDF}\newline
\textbf{Given} una solicitud con cabecera \texttt{Accept: application/pdf}.\newline
\textbf{When} el motor compila el historial productivo, clima y regulaciones de carga.\newline
\textbf{Then} responde con \texttt{Content-Type: application/pdf}, cabecera \texttt{Content-Disposition} para descarga y el sello criptográfico en el pie de página.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Consulta de metadatos y verificación de integridad}\newline
\textbf{Given} una solicitud con cabecera \texttt{Accept: application/json}.\newline
\textbf{When} se consulta el expediente técnico.\newline
\textbf{Then} responde con los datos resumidos del predio y el hash SHA-256 de autenticidad documental.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US31} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Gestor Técnico de Cooperativa} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP08} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Semáforo fenológico reactivo y priorización técnica ante sobrecarga crítica} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Gestor Técnico de Cooperativa, \textbf{quiero} que el semáforo territorial se actualice reactivamente cuando se detecte sobrecarga frutal (> 30\%) o riesgo de helada en las parcelas socias, \textbf{para} focalizar de inmediato la emisión de alertas agronómicas en los sectores más vulnerables.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Activación reactiva de semáforo rojo por sobrecarga frutal}\newline
\textbf{Given} un socio cuya prescripción técnica arroja una sobrecarga superior al 30\%.\newline
\textbf{When} el evento de dominio impacta en el contexto de territorio.\newline
\textbf{Then} el semáforo del sector correspondiente transiciona automáticamente a nivel crítico (Rojo) e incrementa el contador de parcelas en riesgo.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Alerta meteorológica preventiva}\newline
\textbf{Given} una predicción de temperatura crítica ($\le 1.5^\circ\text{C}$) a 48 horas en un sector.\newline
\textbf{When} el evento climático es recibido desde telemetría.\newline
\textbf{Then} el sector activa la advertencia de helada radiativa en el semáforo gremial.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US32} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Gestor Técnico de Cooperativa} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP08} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Proyección agregada temprana de volumen de acopio de aceituna verde y negra para la cooperativa} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Gestor Técnico de Cooperativa, \textbf{quiero} consultar la estimación agregada del tonelaje total de aceituna verde y negra que entregarán los socios en la campaña, \textbf{para} planificar con meses de anticipación la logística de salmueras en almazara, gestionar turnos de recepción y asegurar contratos comerciales de exportación sin riesgo de penalidades.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Proyección consolidada de cosecha cooperativa por aptitud comercial}\newline
\textbf{Given} una cooperativa agraria con predios socios que han registrado sus muestreos de cuajado y regulación de carga.\newline
\textbf{When} el gestor técnico solicita la proyección agregada de cosecha para la campaña en curso.\newline
\textbf{Then} el sistema consolida los modelos productivos individuales y calcula el tonelaje total proyectado para la cooperativa.\newline
\textbf{And} discrimina el volumen estimado por aptitud comercial en aceituna verde para mesa y aceituna negra para aceite y maduración.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Advertencia por baja cobertura de muestreos en la cartera de socios}\newline
\textbf{Given} una cartera cooperativa donde menos del 50\% de las parcelas socias ha completado el protocolo de muestreo de cuajado.\newline
\textbf{When} el gestor técnico consulta la estimación de acopio.\newline
\textbf{Then} el sistema presenta la proyección preliminar indicando el porcentaje de predios contabilizados.\newline
\textbf{And} notifica que la estimación posee un margen de incertidumbre elevado hasta incrementar la cobertura de fundos evaluados.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US33} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Presentación de la propuesta de valor central para la mitigación de la vecería prolongada en el olivar} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante, \textbf{quiero} que el sistema exponga con claridad cómo la integración de datos de microclima, frío invernal y regulación de carga frutal atenúa la severidad de la alternancia productiva y mitiga la vecería prolongada, \textbf{para} comprender de inmediato la solución tecnológica que ofrece Viora frente a la incertidumbre agronómica del cultivo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Comprensión del valor del ecosistema ante la incertidumbre climática}\newline
\textbf{Given} un visitante que accede al sitio web informativo de la plataforma.\newline
\textbf{When} consulta la presentación inicial de la propuesta de valor.\newline
\textbf{Then} el sistema expone la relación entre acumulación de frío, riesgo fenológico y decisiones preventivas de aclareo basadas en datos.\newline
\textbf{And} resalta el objetivo agronómico de atenuar la severidad de la vecería y sostener un piso productivo viable en los años de menor cosecha sin comprometer la longevidad del olivar.\newline
\textbf{And} la interfaz responde con diseño adaptativo fluido (\textit{mobile-first}), reordenando los bloques visuales y garantizando legibilidad en pantallas móviles ($\le 480$px), tablets y escritorio sin desbordamiento horizontal.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Adaptabilidad a diversas variedades de olivar y aptitudes comerciales}\newline
\textbf{Given} un visitante evaluando la pertinencia técnica de la plataforma para su predio.\newline
\textbf{When} explora los fundamentos del modelo de estabilización productiva.\newline
\textbf{Then} el sistema detalla cómo los algoritmos adaptan dinámicamente los parámetros de frío y regulación de carga según la variedad de olivo registrada (mesa o aceite).\newline
\textbf{And} comunica cómo el soporte técnico continuo asiste la toma de decisiones del agricultor.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US34} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante Productor} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Exploración de beneficios y capacidades operativas para el productor olivarero} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante Productor, \textbf{quiero} consultar las herramientas tecnológicas orientadas al monitoreo y manejo agronómico de parcelas, \textbf{para} evaluar cómo la plataforma me ayuda a registrar conteos sin conexión a internet, anticipar estrés hídrico y recibir prescripciones precisas de aclareo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Descubrimiento de herramientas para el trabajo a pie de árbol}\newline
\textbf{Given} un visitante con perfil de agricultor explorando el sitio informativo.\newline
\textbf{When} solicita la información de beneficios orientada al productor individual.\newline
\textbf{Then} el sistema presenta las capacidades de recolección offline de datos en campo, sincronización diferida y cálculo automático del índice de vecería BBI.\newline
\textbf{And} expone las alertas tempranas de estrés hídrico y térmico junto con la prescripción técnica de porcentaje de fruta a remover.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Consulta del impacto en la estabilidad de ingresos del predio}\newline
\textbf{Given} un productor olivarero evaluando el retorno productivo de la adopción.\newline
\textbf{When} revisa la justificación técnica de la regulación de carga frutal.\newline
\textbf{Then} el sistema expone la proyección de calibres comerciales uniformes y la reducción del riesgo de colapso productivo en la siguiente campaña.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US35} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante Gestor de Cooperativa} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Exploración de beneficios y herramientas de gestión territorial para cooperativas agrarias} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante Gestor de Cooperativa, \textbf{quiero} consultar las capacidades de supervisión cartográfica y proyección agregada de cosecha, \textbf{para} determinar si la plataforma facilita la asistencia técnica a los socios agremiados y mejora la planificación logística del acopio en almazara.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Descubrimiento de capacidades de supervisión territorial y semáforo de riesgo}\newline
\textbf{Given} un representante de cooperativa agraria consultando las soluciones institucionales.\newline
\textbf{When} accede a la información de valor para organizaciones de productores.\newline
\textbf{Then} el sistema expone el mapa satelital de parcelas socias con posición GPS y el tablero semafórico de vulnerabilidad fenológica.\newline
\textbf{And} detalla la optimización de rutas de asistencia técnica según la severidad de sobrecarga frutal de los predios.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Visualización de capacidades de estimación temprana de acopio}\newline
\textbf{Given} un gestor técnico evaluando el impacto de la solución en la recepción de materia prima.\newline
\textbf{When} revisa los módulos de previsión de volumen.\newline
\textbf{Then} el sistema presenta la estimación agregada temprana de tonelaje discriminada por aptitud comercial (aceituna de mesa y para almazara) para organizar turnos de procesamiento y salmueras.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US36} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Visualización de planes de suscripción y tarifas transparentes en moneda nacional (PEN)} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante, \textbf{quiero} consultar las tarifas de suscripción en Soles (PEN) por superficie o membresía institucional junto con el detalle de servicios incluidos, \textbf{para} evaluar la opción comercial más conveniente y transparente para mi escala productiva antes de contratar.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta de alternativas comerciales diferenciadas por segmento}\newline
\textbf{Given} un visitante interesado en contratar el servicio de la plataforma.\newline
\textbf{When} consulta las opciones de suscripción y tarifas vigentes.\newline
\textbf{Then} el sistema presenta los costos expresados en Soles (PEN), diferenciando el plan de productor individual por hectárea y la membresía corporativa para cooperativas.\newline
\textbf{And} detalla las prestaciones analíticas, límites de parcelas y soporte agronómico comprendidos en cada alternativa.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Transparencia en condiciones de facturación y renovación}\newline
\textbf{Given} un agricultor evaluando la periodicidad de pago del servicio.\newline
\textbf{When} revisa las condiciones comerciales del plan.\newline
\textbf{Then} el sistema expone con claridad los ciclos de cobro, los medios locales de pago admitidos y la ausencia de penalidades ocultas por cancelación.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US37} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante Interesado} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Reproducción del video promocional y demostrativo del producto ("About the Product")} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante Interesado, \textbf{quiero} reproducir un video demostrativo breve sobre el funcionamiento de Viora, \textbf{para} apreciar la aplicación práctica de los modelos agronómicos en campo y validar su eficacia en la mitigación de la vecería prolongada antes de adoptar la plataforma.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Reproducción del video demostrativo del producto}\newline
\textbf{Given} un visitante interesado en conocer la operatividad práctica de la plataforma.\newline
\textbf{When} solicita reproducir el contenido audiovisual sobre el producto ("About the Product").\newline
\textbf{Then} el sistema inicia la reproducción del video demostrando el flujo de muestreo a pie de árbol, la sincronización offline y la generación de prescripciones agronómicas.\newline
\textbf{And} presenta casos de uso orientados tanto a productores individuales como a organizaciones cooperativas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Control de reproducción adaptativa}\newline
\textbf{Given} un usuario reproduciendo el video del producto sobre una conexión de datos móvil.\newline
\textbf{When} el contenido audiovisual se reproduce en el navegador.\newline
\textbf{Then} el sistema ofrece controles de reproducción, pausa y ajuste dinámico de calidad según el ancho de banda disponible.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US38} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante Cauteloso} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Baja} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Reproducción del video institucional sobre el equipo y proceso de ingeniería ("About the Team")} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante Cauteloso, \textbf{quiero} reproducir un video sobre el equipo y el proceso de trabajo detrás del desarrollo de Viora, \textbf{para} corroborar el respaldo profesional, rigor agronómico e institucional del software antes de incorporarlo en mi actividad agrícola.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Reproducción del video de trayectoria y metodología del equipo}\newline
\textbf{Given} un visitante que busca comprobar la seriedad y el respaldo técnico del proyecto.\newline
\textbf{When} solicita reproducir el video institucional del equipo ("About the Team").\newline
\textbf{Then} el sistema reproduce el material audiovisual documentando el trabajo de campo con agricultores, diseño centrado en el usuario y pruebas de software.\newline
\textbf{And} expone las intervenciones de los integrantes describiendo las competencias agronómicas y tecnológicas aplicadas en la solución.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Consulta de perfiles y roles de los miembros del equipo}\newline
\textbf{Given} un visitante examinando la información institucional del proyecto.\newline
\textbf{When} consulta el detalle complementario del equipo.\newline
\textbf{Then} el sistema presenta la identidad, especialidad y rol técnico de cada integrante del equipo desarrollador.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US39} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de términos de servicio y política de privacidad y protección de datos (Ley N° 29733)} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante, \textbf{quiero} consultar los Términos y Condiciones y la Política de Privacidad formulada conforme a la Ley N° 29733 (Ley de Protección de Datos Personales del Perú), \textbf{para} tener plena certidumbre legal sobre la confidencialidad de mis registros de cultivo y los derechos sobre mis datos agronómicos.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta formal de la política de privacidad de datos}\newline
\textbf{Given} un visitante interesado en las garantías de protección de la información.\newline
\textbf{When} accede al documento de política de privacidad de la plataforma.\newline
\textbf{Then} el sistema expone los lineamientos de tratamiento de datos personales en estricta conformidad con la Ley N° 29733 y su reglamento.\newline
\textbf{And} especifica los fines exclusivamente agronómicos de custodia y los canales formales para ejercer los derechos de acceso, rectificación, cancelación y oposición (derechos ARCO).} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Consulta de términos y condiciones de la plataforma SaaS}\newline
\textbf{Given} un usuario evaluando el marco contractual del servicio digital.\newline
\textbf{When} consulta las condiciones de uso de la plataforma.\newline
\textbf{Then} el sistema expone los términos comerciales estipulando la propiedad inalienable de los datos de cosecha por parte del agricultor y los compromisos de disponibilidad del servicio.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US40} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP09} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Redirección y acceso a la descarga oficial de la aplicación móvil} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante, \textbf{quiero} disponer de accesos directos hacia los repositorios oficiales de distribución móvil, \textbf{para} descargar e instalar la aplicación en mi dispositivo e iniciar mi experiencia en la plataforma.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Acceso guiado a la descarga según la plataforma del dispositivo}\newline
\textbf{Given} un visitante que decide adoptar la solución móvil de Viora.\newline
\textbf{When} solicita el acceso a la descarga de la aplicación.\newline
\textbf{Then} el sistema provee el enlace directo y verificado hacia la tienda oficial de distribución de aplicaciones correspondiente al sistema operativo del usuario.\newline
\textbf{And} confirma los requisitos mínimos de compatibilidad del sistema operativo para una instalación exitosa.\newline
\textbf{And} los botones y badges de descarga se adaptan al ancho de pantalla manteniendo un área táctil mínima de 48 por 48 píxeles en dispositivos móviles.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Orientación de primeros pasos al completar la instalación}\newline
\textbf{Given} un visitante que finaliza la instalación de la aplicación móvil.\newline
\textbf{When} abre la aplicación por primera vez en su dispositivo.\newline
\textbf{Then} el sistema ofrece la alternativa de iniciar sesión con credenciales previas o activar una nueva cuenta de productor olivarero o cooperativa.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US41} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Visitante} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP15} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Selección de idioma y localización de contenidos en la Landing Page} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} Visitante, \textbf{quiero} alternar el idioma de los contenidos entre Español e Inglés mediante un selector visible en la cabecera, \textbf{para} consultar la propuesta de valor, los beneficios agronómicos y las tarifas en mi idioma preferido.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Detección automática del idioma del navegador}\newline
\textbf{Given} un visitante que accede al sitio web público de Viora.\newline
\textbf{When} la página carga en el navegador del dispositivo.\newline
\textbf{Then} el sistema detecta la configuración regional del navegador y presenta los contenidos en idioma inglés si el navegador utiliza dicho idioma, o en español de forma predeterminada.\newline
\textbf{And} el selector de cabecera refleja visualmente la opción de idioma activa.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Cambio manual interactivo y persistencia local}\newline
\textbf{Given} un visitante explorando cualquier sección de la landing page.\newline
\textbf{When} selecciona un idioma distinto a través del componente selector en la barra superior.\newline
\textbf{Then} la interfaz traduce instantáneamente todos los textos, menús y tarifas sin requerir la recarga completa del sitio web.\newline
\textbf{And} persiste la preferencia en el almacenamiento local (\texttt{localStorage}) para conservar la configuración en visitas sucesivas.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US42} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP15} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Configuración y cambio de idioma de la interfaz en la aplicación móvil} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario autenticado de la aplicación móvil de Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} seleccionar mi idioma de preferencia (Español o Inglés) desde el panel de ajustes de la aplicación, \textbf{para} visualizar todos los menús, diagnósticos y alertas en el idioma con el que tenga mayor familiaridad.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Cambio de idioma en caliente sin reinicio de sesión}\newline
\textbf{Given} un usuario autenticado navegando dentro de la aplicación móvil.\newline
\textbf{When} ingresa a la configuración de preferencias y selecciona una nueva opción de idioma (Español o Inglés).\newline
\textbf{Then} la aplicación actualiza en caliente todas las etiquetas, títulos, botones y mensajes de alerta al idioma seleccionado sin cerrar la sesión activa del usuario.\newline
\textbf{And} adapta los separadores de miles y fechas según la convención regional correspondiente.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Persistencia local de la preferencia de idioma en el dispositivo}\newline
\textbf{Given} un usuario que configuró previamente su preferencia de idioma en la app.\newline
\textbf{When} cierra la aplicación y vuelve a iniciarla o reinicia el dispositivo móvil.\newline
\textbf{Then} la aplicación móvil recupera el ajuste persistido desde el almacenamiento local seguro y levanta directamente en el idioma seleccionado.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{US43} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Productor Olivarero / Gestor Técnico} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP01} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Completado de perfil de usuario y contacto validado bajo estándar E.164} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} usuario nuevo autenticado en Viora (Productor Olivarero o Gestor Técnico), \textbf{quiero} completar mi perfil de usuario ingresando mi nombre completo, país de residencia y número celular de contacto, \textbf{para} personalizar mi cuenta y habilitar los canales de notificación agronómica y operativa del sistema.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Registro exitoso de perfil y contacto con normalización telefónica}\newline
\textbf{Given} un usuario autenticado con una cuenta activa que aún no ha completado su perfil inicial.\newline
\textbf{When} ingresa su nombre completo, país de residencia y un número celular válido para dicho país.\newline
\textbf{Then} el sistema persiste el perfil de usuario asociándolo a su cuenta y normaliza el número telefónico bajo el estándar internacional E.164.\newline
\textbf{And} habilita el acceso completo a las funciones operativas de la plataforma según su rol asignado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rechazo por número telefónico incompatible con el país seleccionado}\newline
\textbf{Given} un usuario autenticado completando su información de perfil.\newline
\textbf{When} proporciona un número telefónico que no cumple con el formato E.164 o cuya longitud no corresponde al estándar del país seleccionado.\newline
\textbf{Then} el sistema rechaza el guardado del perfil sin alterar la cuenta de acceso existente.\newline
\textbf{And} notifica la inconsistencia indicando el formato telefónico y prefijo requerido para dicho país.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Rechazo por campos obligatorios incompletos o nombre vacío}\newline
\textbf{Given} un usuario autenticado completando su perfil de usuario.\newline
\textbf{When} envía el formulario omitiendo su nombre completo o país de residencia.\newline
\textbf{Then} el sistema deniega el registro del perfil.\newline
\textbf{And} resalta los campos requeridos solicitando su debido diligenciamiento.} \\ \hline
\end{longtable}
\endgroup

\clearpage

A continuación, se presentan las Historias Técnicas (\textit{Technical Stories}) orientadas al equipo de desarrollo de backend, correspondientes a los servicios de integración RESTful y arquitectura de soporte (\textbf{EP10}, \textbf{EP11}, \textbf{EP12}, \textbf{EP13} y \textbf{EP15}), así como los Spikes técnicos de investigación (\textbf{EP14}). Conforme a las buenas prácticas de diseño de software y directrices de desarrollo, cada historia técnica comprende exactamente un único endpoint HTTP con sus respectivos escenarios BDD basados en los códigos de respuesta RESTful:

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS01} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Registro de credenciales de cuenta de usuario y asignación de rol en IAM} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar las credenciales de registro (correo, contraseña y rol) al servicio de autenticación, \textbf{para} crear la cuenta de usuario con contraseña cifrada y rol asignado en el contexto de identidad y acceso.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Creación exitosa de cuenta de usuario}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/sign-up} es recibida con un cuerpo JSON que contiene: \texttt{email}, \texttt{password} y \texttt{role}.\newline
\textbf{When} la API valida la sintaxis, verifica que el correo no esté registrado y genera el hash seguro de la contraseña mediante BCrypt.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{UserAccountResource} con id, email, role y status activo.\newline
\textbf{And} persiste la cuenta de usuario en el almacén de identidades.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Correo electrónico duplicado en el sistema}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/sign-up} con un correo ya existente en el sistema.\newline
\textbf{When} la API detecta conflicto de unicidad en la base de datos de identidades.\newline
\textbf{Then} la API responde \texttt{409 Conflict} bajo el estándar RFC 7807 indicando que la dirección de correo ya se encuentra en uso.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Contraseña no cumple con las políticas de complejidad}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/sign-up} con una contraseña que no satisface las reglas de longitud mínima o complejidad.\newline
\textbf{When} el validador de payload procesa los atributos del registro.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} detallando la infracción de la política de contraseñas.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS02} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Autenticación de usuarios y emisión de tokens JWT con claims de rol} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar las credenciales de acceso a la API, \textbf{para} autenticar al usuario y recibir un token de acceso JWT con sus respectivos claims de autorización.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Autenticación exitosa}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/sign-in} es recibida con email y password válidos.\newline
\textbf{When} la API verifica el hash criptográfico de la contraseña.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{AuthResource} conteniendo accessToken (JWT con vigencia de 15 minutos y claims de rol), refreshToken con rotación y tokenType Bearer.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Credenciales incorrectas}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/sign-in} con contraseña incorrecta o correo no registrado.\newline
\textbf{When} la API valida las credenciales.\newline
\textbf{Then} la API responde \texttt{401 Unauthorized} con mensaje genérico de error de autenticación.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS03} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Renovación periódica de tokens de sesión mediante Refresh Token} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar el refresh token a la API, \textbf{para} renovar el token de acceso JWT expirado sin requerir que el usuario vuelva a ingresar sus credenciales.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Renovación exitosa de token}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/refresh-tokens} es recibida con un refreshToken vigente y no revocado.\newline
\textbf{When} la API valida la firma y el estado de la sesión en el almacén de tokens.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{AuthResource} con un nuevo accessToken y un nuevo refreshToken rotado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Refresh token expirado o revocado}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/refresh-tokens} con un token revocado o caducado.\newline
\textbf{When} la API valida el token.\newline
\textbf{Then} la API responde \texttt{401 Unauthorized} exigiendo nueva autenticación interactiva.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS04} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de información de perfil del usuario autenticado} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} consumir el endpoint GET del perfil de usuario, \textbf{para} obtener los datos personales, de membresía y contacto del usuario autenticado.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta exitosa de perfil propio}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/profiles/{userId}} con cabecera Authorization Bearer.\newline
\textbf{When} la API valida que el \texttt{userId} solicitado coincide con el claim del token JWT o el solicitante es administrador.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{ProfileResource} con id, userId, fullName, country, phoneNumber y createdAt.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Intento de consulta de perfil de otro usuario}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/profiles/{userId}} con un identificador ajeno al usuario autenticado.\newline
\textbf{When} la API evalúa la correspondencia de propiedad de la cuenta.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Usuario inexistente}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/profiles/{userId}} con un identificador no registrado.\newline
\textbf{When} la API consulta la persistencia.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS05} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Actualización parcial de datos de perfil con validación telefónica E.164} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar actualizaciones parciales del perfil a la API, \textbf{para} modificar el nombre de contacto o el número de teléfono operativo validado bajo el estándar E.164.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Actualización exitosa}\newline
\textbf{Given} una solicitud PATCH a \url{/api/v1/profiles/{userId}} con cuerpo JSON que incluye fullName y/o phoneNumber y país.\newline
\textbf{When} la API valida la propiedad de la cuenta y verifica el nuevo teléfono mediante la biblioteca \texttt{libphonenumber}.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{ProfileResource} con los datos actualizados y persistidos.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Teléfono inválido en actualización}\newline
\textbf{Given} una solicitud PATCH a \url{/api/v1/profiles/{userId}} con un teléfono que no cumple la norma E.164 según \texttt{libphonenumber}.\newline
\textbf{When} la API valida los campos provistos.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} sin alterar la información previa.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Permiso denegado sobre cuenta ajena}\newline
\textbf{Given} una solicitud PATCH a \url{/api/v1/profiles/{userId}} dirigida a un identificador distinto al token autenticado.\newline
\textbf{When} la API evalúa la correspondencia.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS06} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Generación de preferencia de checkout para suscripción de productor independiente} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar a la API la creación de una orden de suscripción SaaS, \textbf{para} obtener el identificador de preferencia y la URL de redirección a la pasarela digital de pagos.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Creación exitosa de preferencia de suscripción}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/subscriptions} con cuerpo JSON: planType: PRODUCER y hectares: number.\newline
\textbf{When} la API calcula el monto en Soles (PEN) según la superficie y genera la orden en la pasarela de pagos configurada.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{SubscriptionPreferenceResource} con preferenceId, checkoutUrl y externalReference.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Datos de suscripción inválidos}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/subscriptions} con hectares menor o igual a cero o plan inexistente.\newline
\textbf{When} la API valida la solicitud de cobro.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} con la especificación del error.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS07} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Recepción y procesamiento de webhooks de notificación de pagos} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de plataforma backend, \textbf{quiero} exponer un endpoint webhook para la pasarela de pagos, \textbf{para} procesar asíncronamente las confirmaciones de transacción y activar la suscripción del productor de manera inmediata.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Procesamiento exitoso de pago confirmado}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/payment-notifications/mercado-pago} recibida desde la pasarela con cabecera \texttt{x-signature} conteniendo la firma criptográfica HMAC-SHA256 válida y estado approved.\newline
\textbf{When} la API valida la firma de autenticidad, recupera la orden y actualiza el estado de la suscripción del usuario.\newline
\textbf{Then} la API responde \texttt{200 OK} y transiciona el estado de la suscripción a ACTIVE, asignando la fecha de vigencia correspondiente.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Firma de webhook inválida}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/payment-notifications/mercado-pago} con cabecera \texttt{x-signature} ausente o alterada.\newline
\textbf{When} el validador criptográfico de webhooks detecta discrepancia en la firma HMAC.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} bajo RFC 7807 y descarta el procesamiento.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS08} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Generación de lote de códigos de activación para socios cooperativos} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar la creación de códigos de activación institucionales a la API, \textbf{para} que el gestor técnico pueda distribuirlos a los socios de la cooperativa.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Generación exitosa de códigos de activación}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/cooperatives/{id}/invitation-code-batches} con cuerpo JSON conteniendo: \texttt{quantity} y \texttt{validDays}.\newline
\textbf{When} la API valida que el usuario tiene rol GESTOR en dicha cooperativa y que la cantidad solicitada no supera el límite contratado en la licencia.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{InvitationCodeBatchResource} con el identificador del lote, arreglo de códigos alfanuméricos únicos generados y fecha de expiración.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Cupo de membresías excedido}\newline
\textbf{Given} una solicitud POST con una cantidad que sobrepasa el cupo de la membresía cooperativa.\newline
\textbf{When} la API evalúa la disponibilidad de cupos en la licencia.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} indicando el límite de licencias permitidas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Acceso no autorizado para no gestores}\newline
\textbf{Given} una solicitud POST emitida por un usuario sin rol GESTOR en la cooperativa especificada.\newline
\textbf{When} la API valida los permisos institucionales.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS09} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta y auditoría de códigos de activación de cooperativa} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el listado de códigos de activación de una cooperativa a la API, \textbf{para} mostrar al gestor los códigos disponibles, canjeados y los socios vinculados.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Listado de códigos disponibles y canjeados}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/cooperatives/{id}/invitation-code-batches} con token de gestor técnico y parámetros opcionales de filtro por estado.\newline
\textbf{When} la API valida la pertenencia institucional y consulta los registros del agregado.\newline
\textbf{Then} la API responde \texttt{200 OK} con un arreglo de objetos \texttt{InvitationCodeBatchResource} que detallan lotes, códigos, estados (AVAILABLE, REDEEMED, EXPIRED) y fechas de expiración.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Acceso no autorizado}\newline
\textbf{Given} una solicitud GET emitida por un usuario que no es gestor de la cooperativa.\newline
\textbf{When} la API verifica el rol institucional.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS10} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Canje de código de activación de socio para vinculación cooperativa} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar el código de activación provisto por el socio a la API, \textbf{para} afiliar al productor a la licencia colectiva de la cooperativa sin cobro individual.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Canje exitoso y afiliación}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/cooperative-code-redemptions} con cuerpo JSON: \texttt{invitationCode}.\newline
\textbf{When} la API valida que el código existe, está en estado AVAILABLE y pertenece al socio autenticado.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{CooperativeMembershipResource} confirmando la vinculación con la cooperativa y el cambio de estado del código a REDEEMED.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Código inválido o agotado}\newline
\textbf{Given} una solicitud POST con un \texttt{invitationCode} inexistente o ya canjeado previamente.\newline
\textbf{When} la API consulta la validez del código.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} indicando que el código no es válido o expiró.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Socio ya vinculado activamente}\newline
\textbf{Given} una solicitud POST emitida por un usuario que ya cuenta con membresía activa en la cooperativa.\newline
\textbf{When} la API comprueba el estado actual de membresías.\newline
\textbf{Then} la API responde \texttt{409 Conflict}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS11} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Creación y delimitación poligonal de parcelas georreferenciadas} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar los vértices poligonales en formato WGS84 a la API, \textbf{para} registrar una nueva parcela y persistir sus propiedades agronómicas.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Creación exitosa de parcela}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots} con cuerpo JSON conteniendo: name, polygonCoordinates, variety, plantDensity y plantationYear.\newline
\textbf{When} la API verifica que el polígono esté cerrado, calcula la superficie en hectáreas y valida la densidad biológica.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{PlotResource} con el identificador asignado y el área calculada.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Geometría poligonal inválida}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots} con menos de 3 vértices o con un polígono que no cierra.\newline
\textbf{When} la API valida la geometría espacial.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} indicando la inconsistencia en las coordenadas.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS12} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Listado y sincronización incremental delta de parcelas} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} consultar el inventario de parcelas con soporte de marcas temporales, \textbf{para} actualizar la base de datos local SQLite mediante sincronización delta eficiente.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta y sincronización incremental}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots} con parámetro opcional \texttt{?updatedSince=\{timestamp\}}.\newline
\textbf{When} la API filtra las parcelas del usuario modificadas posteriormente a dicha marca temporal.\newline
\textbf{Then} la API responde \texttt{200 OK} con un arreglo de objetos \texttt{PlotResource} actualizados.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Acceso no autorizado a predios ajenos}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots} con parámetro \texttt{?userId=\{id\}} perteneciente a otro agricultor sin ser gestor técnico.\newline
\textbf{When} la API valida los permisos de acceso.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS13} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta detallada de información agronómica y espacial de parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el detalle de una parcela mediante su ID, \textbf{para} visualizar la ficha agronómica completa del predio en la interfaz de usuario.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Obtención de detalle de parcela}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}} con token autorizado.\newline
\textbf{When} la API verifica la titularidad y recupera la parcela.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{PlotDetailResource} con geometría, variedad, densidad, año de siembra y sensores vinculados.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Parcela inexistente}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}} con un identificador no existente.\newline
\textbf{When} la API busca en la base de datos.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS14} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Actualización y rectificación integral de parcela con bloqueo optimista} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar los datos actualizados de la parcela junto con la cabecera de control de versión a la API, \textbf{para} rectificar linderos poligonales, densidad de árboles o nombre del lote previniendo colisiones de concurrencia.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Actualización exitosa con control de concurrencia}\newline
\textbf{Given} una solicitud PUT a \url{/api/v1/plots/{plotId}} con cabecera \texttt{If-Match} conteniendo la versión actual y cuerpo JSON con los datos modificados.\newline
\textbf{When} la API valida la titularidad, verifica que la versión coincida, recalcula la superficie si variaron las coordenadas y persiste los cambios.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{PlotResource} actualizado con nueva cabecera ETag.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Conflicto de versión por modificación concurrente}\newline
\textbf{Given} una solicitud PUT a \url{/api/v1/plots/{plotId}} con una cabecera \texttt{If-Match} que difiere de la versión persistida en el almacén de datos.\newline
\textbf{When} el interceptor de concurrencia optimista detecta conflicto de modificación simultánea.\newline
\textbf{Then} la API responde \texttt{412 Precondition Failed} bajo el estándar RFC 7807 impidiendo sobreescrituras concurrentes.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Acceso no autorizado sobre parcela ajena}\newline
\textbf{Given} una solicitud PUT enviada por un usuario no propietario del predio.\newline
\textbf{When} la API evalúa la titularidad en el contexto de inventario parcelario.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS15} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Baja} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Eliminación y baja lógica de parcela del inventario} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar a la API la remoción de una parcela, \textbf{para} dar de baja predios registrados por error o desafectados de la producción.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Eliminación exitosa}\newline
\textbf{Given} una solicitud DELETE a \url{/api/v1/plots/{plotId}} emitida por el propietario del lote.\newline
\textbf{When} la API valida la propiedad y ejecuta la baja lógica del predio.\newline
\textbf{Then} la API responde \texttt{204 No Content}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Parcela ajena}\newline
\textbf{Given} una solicitud DELETE a un lote perteneciente a otro usuario.\newline
\textbf{When} la API verifica permisos.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS16} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Alta y vinculación de nodo sensor virtual a parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} registrar un nodo sensor virtual (microclima o sonda de suelo a 30/60 cm) en la API, \textbf{para} activar la simulación de telemetría agroclimática en la parcela.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Alta exitosa de nodo sensor virtual}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots/{plotId}/iot-devices} con cuerpo JSON conteniendo name, type (MICROCLIMATE o SOIL\_PROBE) y depthCm.\newline
\textbf{When} la API valida que el tipo sea válido y la profundidad corresponda a 30 o 60 cm para sondas.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{IotDeviceResource} con id asignado y estado ACTIVE.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Nombre duplicado de sensor en la misma parcela}\newline
\textbf{Given} una solicitud POST con un nombre de sensor ya existente en dicho lote.\newline
\textbf{When} la API comprueba unicidad dentro del predio.\newline
\textbf{Then} la API responde \texttt{409 Conflict}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Parámetros de nodo inválidos}\newline
\textbf{Given} una solicitud POST con tipo desconocido o profundidad distinta a 30 o 60 cm.\newline
\textbf{When} la API evalúa la configuración técnica.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS17} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de inventario de nodos virtuales vinculados a parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el listado de nodos virtuales de una parcela a la API, \textbf{para} desplegar su estado operativo y última lectura simulada en la interfaz.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Listado de dispositivos vinculados}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/iot-devices} con token de usuario autorizado.\newline
\textbf{When} la API recupera los dispositivos asociados a la parcela.\newline
\textbf{Then} la API responde \texttt{200 OK} con un arreglo de objetos \texttt{IotDeviceResource} detallando id, name, type, depthCm, status y lastReadingTimestamp.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Parcela inexistente}\newline
\textbf{Given} una solicitud GET con un plotId inexistente.\newline
\textbf{When} la API consulta la persistencia.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS18} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Baja} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Desvinculación de nodo virtual preservando trazabilidad histórica} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar la desvinculación de un nodo virtual a la API, \textbf{para} retirar sensores obsoletos preservando las lecturas históricas asociadas al lote.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Desvinculación exitosa}\newline
\textbf{Given} una solicitud DELETE a \url{/api/v1/plots/{plotId}/iot-devices/{deviceId}} emitida por el titular de la parcela.\newline
\textbf{When} la API verifica la pertenencia y ejecuta la baja lógica del nodo.\newline
\textbf{Then} la API responde \texttt{204 No Content} manteniendo la integridad de las series cronológicas previas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Dispositivo no encontrado}\newline
\textbf{Given} una solicitud DELETE con identificador de dispositivo inexistente.\newline
\textbf{When} la API busca el registro.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS19} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de series temporales de telemetría ambiental y de suelo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar las lecturas horarias de microclima y humedad de suelo a la API, \textbf{para} graficar las curvas térmicas e hídricas en los paneles de control de la parcela.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta de series históricas}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/telemetries} con parámetros \texttt{?startDate=\{ISO\}\&endDate=\{ISO\}}.\newline
\textbf{When} la API valida el rango temporal y recupera las series horarias continuas.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{TelemetrySeriesResource} con arreglos de temperatura, humedad relativa y humedad volumétrica a 30 y 60 cm.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Rango temporal ilógico}\newline
\textbf{Given} una solicitud GET con una fecha inicial posterior a la fecha final.\newline
\textbf{When} la API valida la coherencia de las fechas.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS20} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de pronóstico meteorológico geolocalizado a 7 días} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el pronóstico meteorológico para la coordenada centroide de la parcela, \textbf{para} advertir al productor sobre olas de calor, heladas o vientos desecantes.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Entrega de pronóstico geolocalizado}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/forecasts} con token autorizado.\newline
\textbf{When} la API resuelve el centroide del lote y obtiene el pronóstico a 7 días desde el servicio climático externo con almacenamiento en caché local por 3 horas.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{ForecastResource} con temperaturas máximas y mínimas, probabilidad de precipitación, velocidad de viento y timestamp de actualización.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Parcela sin geometría definida}\newline
\textbf{Given} una solicitud GET dirigida a una parcela sin coordenadas válidas.\newline
\textbf{When} la API intenta calcular el centroide geográfico.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} indicando la ausencia de georreferenciación.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS21} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Asentamiento de cosecha anual por campaña para auditoría productiva} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar los kilogramos cosechados al cierre de la temporada a la API, \textbf{para} registrar la producción anual del lote y alimentar el cálculo del índice BBI.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Asentamiento exitoso de cosecha}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots/{plotId}/harvest-records} con cuerpo JSON: campaignYear, totalYieldKg, greenKg y blackKg.\newline
\textbf{When} la API valida la propiedad del lote, verifica que la suma de calidades coincida con el total y persiste el registro.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{HarvestRecordResource} con el registro auditado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Campaña ya registrada previamente}\newline
\textbf{Given} una solicitud POST para una campaña agrícola ya asentada en dicho lote.\newline
\textbf{When} la API comprueba la existencia del año agrícola.\newline
\textbf{Then} la API responde \texttt{409 Conflict}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Valores de cosecha inconsistentes}\newline
\textbf{Given} una solicitud POST con rendimientos negativos o año futuro.\newline
\textbf{When} la API valida los campos numéricos.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS22} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta del historial plurianual de cosechas de la parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el historial de cosechas de una parcela a la API, \textbf{para} renderizar la curva interanual de rendimiento productivo en la interfaz.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Listado cronológico de cosechas}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/harvest-records} con token de usuario autorizado.\newline
\textbf{When} la API recupera los registros productivos históricos del lote.\newline
\textbf{Then} la API responde \texttt{200 OK} con un arreglo de objetos \texttt{HarvestRecordResource} ordenados cronológicamente por año agrícola.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Parcela no encontrada}\newline
\textbf{Given} una solicitud GET con un plotId inexistente.\newline
\textbf{When} la API consulta la base de datos.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS23} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Cálculo y entrega de métricas de vecería BBI y frío dinámico de Erez} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar los indicadores matemáticos de vecería y frío invernal a la API, \textbf{para} desplegar el índice BBI y las porciones de frío acumuladas con alertas térmicas ENOS.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Cálculo exitoso de índice BBI o frío de Erez}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/metrics} con parámetro \texttt{?name=BBI} o \texttt{?name=CHILLING}.\newline
\textbf{When} la API computa la fórmula de Hoblyn ($\ge 3$ campañas) o ejecuta el modelo dinámico de Erez sobre las temperaturas horarias.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{MetricResource} con el valor numérico, categoría de severidad y el flag \texttt{enosAnomalyDetected: boolean}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Datos insuficientes para el cálculo}\newline
\textbf{Given} una solicitud GET para BBI en un lote con menos de 3 campañas registradas.\newline
\textbf{When} la API valida los requisitos estadísticos.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} indicando que se requieren al menos 3 campañas agrícolas.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS24} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Registro y sincronización de muestreos guiados de cuajado en campo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar los registros de conteo de frutos y brotes tomados a pie de árbol a la API, \textbf{para} sincronizar los muestreos offline y calcular la carga frutal del predio.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Sincronización exitosa de lote de muestreos}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots/{plotId}/samplings} con cuerpo JSON conteniendo la lista de árboles evaluados con número de brotes y frutos observados.\newline
\textbf{When} la API valida los conteos, calcula el promedio de frutos por brote y la carga estimada del predio.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{SamplingBatchResource} con el resumen del lote de muestreos.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Datos de muestreo inconsistentes}\newline
\textbf{Given} una solicitud POST con conteos negativos o árbol duplicado en el mismo lote de muestreo.\newline
\textbf{When} la API valida la consistencia agronómica.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS25} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de representatividad estadística y estado de muestreo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el estado de representatividad de muestreos a la API, \textbf{para} notificar al usuario si ha evaluado suficientes árboles para generar prescripciones confiables.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta de cobertura de muestreos}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/samplings} con token autorizado.\newline
\textbf{When} la API consolida los árboles evaluados en la campaña activa.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{SamplingSummaryResource} con total de árboles evaluados, representatividad porcentual y el indicador \texttt{isSampleSufficient: boolean} ($\ge 5$ árboles).} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Parcela no encontrada}\newline
\textbf{Given} una solicitud GET con plotId inválido o inexistente.\newline
\textbf{When} la API busca en persistencia.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS26} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de prescripción técnica de aclareo y ventana fenológica} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar la prescripción agronómica de regulación de carga a la API, \textbf{para} desplegar el porcentaje de remoción recomendado y la fecha límite antes del endurecimiento del carozo.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Prescripción generada satisfactoriamente}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/thinning-prescriptions} con muestreos suficientes en el lote.\newline
\textbf{When} la API compara la carga real frente a la capacidad fisiológica del árbol y calcula la fecha límite fenológica.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{ThinningPrescriptionResource} con el porcentaje de remoción frutal sugerido, fecha inicio, fecha límite antes de lignificación del carozo y diagnóstico de sobrecarga (> 30\%).\newline
\textbf{And} entrega las instrucciones operativas de aclareo manual o mecánico.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Muestreo insuficiente para emitir prescripción}\newline
\textbf{Given} una solicitud GET en un lote con menos de 5 árboles evaluados.\newline
\textbf{When} la API evalúa la representatividad.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} indicando que no es posible formular prescripciones sin alcanzar la muestra mínima.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS27} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Confirmación y registro de ejecución de labor de aclareo en campo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar la confirmación de la labor de aclareo ejecutada a la API, \textbf{para} registrar la fecha de intervención y recalcular la proyección de calibre comercial.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Registro exitoso de ejecución de aclareo}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/thinning-prescriptions/{id}/execution-confirmations} con cuerpo JSON: \texttt{executionDate}, \texttt{actualRemovalPercentage} y \texttt{notes}.\newline
\textbf{When} la API valida que el porcentaje esté entre 0\% y 100\% y persiste la intervención agronómica.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{ExecutionConfirmationResource} con el nuevo balance de carga y calibre proyectado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Porcentaje de remoción fuera de rango}\newline
\textbf{Given} una solicitud POST con un porcentaje mayor al 100\% o negativo.\newline
\textbf{When} la API valida la entrada.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Prescripción inexistente o ya confirmada}\newline
\textbf{Given} una solicitud POST dirigida a una prescripción inexistente o que ya fue confirmada previamente.\newline
\textbf{When} el servicio de aplicación evalúa el estado del agregado.\newline
\textbf{Then} la API responde \texttt{404 Not Found} o \texttt{409 Conflict} según corresponda.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS28} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Generación y descarga de reporte agronómico auditable en formato PDF} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el archivo binario del reporte agronómico a la API, \textbf{para} descargar la ficha técnica en PDF con la trazabilidad completa del predio para trámites bancarios o cooperativos.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Generación exitosa de PDF mediante negociación de contenidos}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/agronomic-reports} con cabecera \texttt{Accept: application/pdf} y token autorizado.\newline
\textbf{When} la API compila los registros de cosecha, índice BBI, frío acumulado y labores de aclareo en el motor de renderizado de documentos.\newline
\textbf{Then} la API responde \texttt{200 OK} con tipo de contenido \texttt{application/pdf} y cabecera \texttt{Content-Disposition} para descarga directa.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Consulta en formato estructurado JSON}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/plots/{plotId}/agronomic-reports} con cabecera \texttt{Accept: application/json}.\newline
\textbf{When} la API recupera la estructura agregada del expediente agronómico.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{AgronomicReportResource} con los metadatos y hash de certificación.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Parcela inexistente}\newline
\textbf{Given} una solicitud GET con un identificador de parcela no existente.\newline
\textbf{When} la API busca los datos del reporte.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS29} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Consulta de la matriz de riesgo territorial y semáforo sectorial para el gestor técnico} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar el estado consolidado de riesgo de los predios socios geolocalizados a la API, \textbf{para} desplegar el semáforo fenológico y priorizar visitas técnicas a parcelas con sobrecarga crítica (> 30\%) o estrés hídrico.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Consulta exitosa de semáforo de riesgo sectorial}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/cooperatives/{cooperativeId}/territorial-risk} con parámetros \texttt{?latitude=\{lat\}\&longitude=\{lon\}} y token de gestor técnico.\newline
\textbf{When} la API evalúa los indicadores de frío y sobrecarga de todas las parcelas socias registradas en el radio territorial.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{TerritorialRiskResource} agrupando los predios en verde (óptimo), amarillo (moderado) y rojo (sobrecarga > 30\% o frío insuficiente).} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Permisos insuficientes para no gestores}\newline
\textbf{Given} una solicitud GET emitida por un usuario sin rol GESTOR en la cooperativa.\newline
\textbf{When} la API valida las credenciales.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS30} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Proyección agregada temprana de volumen de acopio cooperativo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} solicitar la proyección temprana consolidada de acopio a la API, \textbf{para} mostrar el tonelaje total previsto discriminado por aptitud de aceituna verde para mesa y negra para aceite.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Proyección de acopio agregada}\newline
\textbf{Given} una solicitud GET a \url{/api/v1/cooperatives/{cooperativeId}/intake-forecasts} con parámetro \texttt{?campaignYear=\{year\}}.\newline
\textbf{When} la API agrega las cargas estimadas de los muestreos de los socios y computa el tonelaje esperado.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{IntakeForecastResource} con total de toneladas estimadas, desglose mesa/aceite y el porcentaje de superficie muestreada.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Acceso no autorizado}\newline
\textbf{Given} una solicitud GET emitida por un usuario sin rol de gestor.\newline
\textbf{When} la API evalúa la pertenencia institucional.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS31} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Ingeniero de Plataforma Core} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP13} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Manejo centralizado de excepciones y errores bajo estándar RFC 7807} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} ingeniero de plataforma core, \textbf{quiero} implementar un interceptor global de excepciones en el backend, \textbf{para} garantizar que todas las respuestas de error sigan el estándar RFC 7807 (Problem Details) con códigos HTTP semánticos y sin exponer trazas internas.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Intercepción de excepciones de validación y dominio}\newline
\textbf{Given} una solicitud a cualquier endpoint que dispara una excepción de validación o regla de negocio.\newline
\textbf{When} el interceptor centralizado captura la excepción.\newline
\textbf{Then} responde con el código HTTP correspondiente (\texttt{400}, \texttt{404} o \texttt{409}) y cuerpo \texttt{application/problem+json} conteniendo \texttt{type}, \texttt{title}, \texttt{status}, \texttt{detail}, \texttt{instance} y \texttt{timestamp}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Protección ante fallas no controladas}\newline
\textbf{Given} un error interno no previsto en el servidor.\newline
\textbf{When} el interceptor procesa el fallo.\newline
\textbf{Then} responde \texttt{500 Internal Server Error} con un mensaje seguro sin divulgar stacktraces de la base de datos o sistema operativo.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS32} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Ingeniero de Plataforma Core} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP13} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Convenciones de persistencia relacional, nomenclatura ORM y tipado espacial} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} ingeniero de plataforma core, \textbf{quiero} configurar la estrategia de mapeo objeto-relacional en el ORM, \textbf{para} normalizar la conversión automática de propiedades camelCase a snake\_case y persistir tipos geométricos espaciales WGS84 de forma consistente.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Mapeo automático de entidades y convenciones}\newline
\textbf{Given} la capa de persistencia interactuando con el motor relacional.\newline
\textbf{When} se ejecutan las migraciones y consultas del ORM.\newline
\textbf{Then} las tablas son nombradas en plural en minúsculas, las columnas se persisten en formato \texttt{snake\_case} y las claves foráneas mantienen integridad referencial ACID.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Conversión bidireccional de geometrías espaciales}\newline
\textbf{Given} entidades con polígonos o coordenadas de geolocalización.\newline
\textbf{When} se persisten o recuperan desde la base de datos.\newline
\textbf{Then} el ORM serializa y deserializa transparentemente entre tipos espaciales nativos y GeoJSON conforme al elipsoide WGS84.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS33} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Ingeniero de Plataforma Core} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP13} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Generación dinámica y documentación interactiva de contratos de API con OpenAPI 3.0} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} ingeniero de plataforma core, \textbf{quiero} integrar el generador de contratos OpenAPI 3.0 en el backend, \textbf{para} exponer una interfaz Swagger UI interactiva y esquemas JSON que documenten exhaustivamente todos los endpoints del sistema.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Exposición de consola Swagger UI}\newline
\textbf{Given} el backend en ejecución.\newline
\textbf{When} un desarrollador accede a \url{/swagger-ui.html}.\newline
\textbf{Then} el sistema presenta la documentación viva interactiva con la totalidad de controladores, modelos de petición/respuesta y autenticación Bearer JWT configurada.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Generación del esquema OpenAPI en formato JSON}\newline
\textbf{Given} una solicitud GET a \url{/v3/api-docs}.\newline
\textbf{When} se consulta el endpoint de especificación.\newline
\textbf{Then} la API responde \texttt{200 OK} con el documento OpenAPI 3.0 completo en formato JSON para pruebas automatizadas y generación de SDKs.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS34} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Ingeniero de Plataforma Core} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP15} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Resolución de localización y mensajes internacionalizados mediante cabecera Accept-Language} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} ingeniero de plataforma core, \textbf{quiero} configurar el resolvedor de localización y los catálogos de recursos MessageSource en el backend, \textbf{para} interceptar el encabezado HTTP Accept-Language y entregar mensajes de validación y errores RFC 7807 traducidos en Español o Inglés.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Respuesta localizada en base a la cabecera HTTP}\newline
\textbf{Given} una solicitud a cualquier endpoint de la API con el encabezado \texttt{Accept-Language: en} que dispara una excepción de validación o dominio.\newline
\textbf{When} el interceptor centralizado captura la excepción e invoca el resolvedor de mensajes \texttt{MessageSource}.\newline
\textbf{Then} responde con el código HTTP correspondiente y cuerpo RFC 7807 conteniendo el detalle y descripción traducidos en idioma inglés.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Aplicación del idioma predeterminado ante omisión o valor no soportado}\newline
\textbf{Given} una solicitud HTTP recibida sin encabezado \texttt{Accept-Language} o con un código de idioma no configurado en el backend.\newline
\textbf{When} el interceptor procesa el fallo.\newline
\textbf{Then} el sistema aplica Español (\texttt{es}) como localización por defecto y entrega los mensajes en idioma español.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS35} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Creación y completado inicial de perfil de usuario con validación telefónica E.164} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar los datos de perfil y contacto (nombre completo, país y número celular) a la API de perfiles junto con el token JWT de autenticación, \textbf{para} crear el registro de perfil vinculado al usuario y validar el teléfono con la biblioteca especializada.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Creación exitosa de perfil de usuario}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/profiles} es recibida con el encabezado \texttt{Authorization: Bearer <JWT>} y un cuerpo JSON que contiene: \texttt{fullName}, \texttt{country} y \texttt{phoneNumber}.\newline
\textbf{When} la API extrae el identificador del usuario autenticado (\texttt{userId}), procesa el número telefónico mediante la biblioteca \texttt{libphonenumber} verificando su validez para el país provisto y normalizándolo al formato E.164.\newline
\textbf{Then} la API responde \texttt{201 Created} y retorna \texttt{ProfileResource} con id, userId, fullName, country, phoneNumber normalizado y createdAt.\newline
\textbf{And} persiste el registro de perfil en la base de datos vinculado al usuario.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Número telefónico inválido según la biblioteca de validación}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/profiles} con un número telefónico que no cumple con el formato o longitud requerida según \texttt{libphonenumber} para el país seleccionado.\newline
\textbf{When} la API somete el teléfono a validación internacional.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} bajo el estándar RFC 7807 indicando la invalidez del número telefónico.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Solicitud no autenticada o token inválido}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/profiles} enviada sin la cabecera \texttt{Authorization} o con un token expirado o corrupto.\newline
\textbf{When} el filtro de seguridad intercepta la petición.\newline
\textbf{Then} la API responde \texttt{401 Unauthorized} impidiendo la persistencia del perfil.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS36} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Actualización de contraseña para sesión de usuario autenticado} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar la contraseña actual y la nueva contraseña confirmada a la API de autenticación, \textbf{para} que el usuario con sesión activa modifique sus credenciales de acceso de forma segura.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Actualización exitosa de contraseña}\newline
\textbf{Given} una solicitud PATCH a \url{/api/v1/auth/passwords} con cabecera \texttt{Authorization: Bearer <JWT>} y cuerpo JSON con \texttt{currentPassword}, \texttt{newPassword} y \texttt{confirmPassword}.\newline
\textbf{When} la API valida la identidad del token, verifica que el hash de \texttt{currentPassword} coincida con el almacenado y que \texttt{newPassword} cumpla las políticas de complejidad.\newline
\textbf{Then} la API responde \texttt{204 No Content} y persiste el nuevo hash criptográfico BCrypt.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Contraseña actual incorrecta}\newline
\textbf{Given} una solicitud PATCH a \url{/api/v1/auth/passwords} con una contraseña actual errónea.\newline
\textbf{When} el servicio de autenticación compara los hashes criptográficos.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} bajo el estándar RFC 7807 indicando credencial previa no coincidente.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Nueva contraseña no cumple políticas de complejidad}\newline
\textbf{Given} una solicitud PATCH a \url{/api/v1/auth/passwords} con una nueva clave que no satisface longitud mínima o variedad de caracteres.\newline
\textbf{When} el validador de seguridad procesa la solicitud.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} detallando la regla de complejidad incumplida.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS37} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Solicitud de código de restablecimiento de contraseña olvidada vía correo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar la dirección de correo del usuario al servicio de recuperación de contraseñas, \textbf{para} que el sistema despache de manera asíncrona un token temporal seguro sin revelar la existencia previa del correo en la base de datos.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Solicitud exitosa de restablecimiento}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/password-reset-tokens} con cuerpo JSON conteniendo: \texttt{email}.\newline
\textbf{When} la API valida la sintaxis del correo, genera un token criptográfico unívoco de 6 dígitos con expiración a 15 minutos y encola el evento para despacho seguro de correo.\newline
\textbf{Then} la API responde \texttt{202 Accepted} bajo el patrón de procesamiento asíncrono.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Dirección de correo con formato inválido}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/auth/password-reset-tokens} con una cadena que no conforma un correo electrónico válido.\newline
\textbf{When} el validador sintáctico analiza el cuerpo de la petición.\newline
\textbf{Then} la API responde \texttt{400 Bad Request} bajo el estándar RFC 7807.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS38} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP10} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Restablecimiento de contraseña mediante token temporal de un solo uso} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar el token temporal recibido por correo y la nueva contraseña a la API, \textbf{para} restablecer el acceso a la cuenta del usuario y revocar el token consumido.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Restablecimiento exitoso de contraseña}\newline
\textbf{Given} una solicitud PUT a \url{/api/v1/auth/password-reset-tokens/{token}} con cuerpo JSON conteniendo \texttt{newPassword} y \texttt{confirmPassword}.\newline
\textbf{When} la API valida que el token exista, no haya expirado, no haya sido consumido previamente y aplica el nuevo hash criptográfico BCrypt.\newline
\textbf{Then} la API responde \texttt{204 No Content}, marca el token como CONSUMED y revoca todas las sesiones activas previas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Token expirado, inexistente o consumido previamente}\newline
\textbf{Given} una solicitud PUT a \url{/api/v1/auth/password-reset-tokens/{token}} con un token inválido o vencido.\newline
\textbf{When} la API consulta el almacén de tokens temporales.\newline
\textbf{Then} la API responde \texttt{404 Not Found} bajo el estándar RFC 7807 exigiendo reiniciar el flujo de recuperación.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Nueva contraseña idéntica a la anterior o sin complejidad}\newline
\textbf{Given} una solicitud PUT a \url{/api/v1/auth/password-reset-tokens/{token}} con una clave que coincide con el hash previo o incumple las reglas de seguridad.\newline
\textbf{When} la API valida la política de credenciales.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS39} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Asentamiento formal y balance de liquidación de cosecha de fin de campaña} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar el pesaje formal definitivo de cosecha (aceituna verde y negra) al cierre de campaña a la API, \textbf{para} asentar la liquidación oficial de la parcela, computar el balance contra la prescripción y congelar el expediente de campaña.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Asentamiento exitoso de liquidación de cosecha}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots/{plotId}/harvest-settlements} con cuerpo JSON: \texttt{campaignYear}, \texttt{greenYieldKg}, \texttt{blackYieldKg} y \texttt{settlementNotes}.\newline
\textbf{When} la API valida la titularidad del predio, comprueba que el año no haya sido liquidado previamente, computa el tonelaje total y registra la liquidación oficial.\newline
\textbf{Then} la API responde \texttt{201 Created}, retorna \texttt{HarvestSettlementResource} con el balance de campaña y emite \texttt{CampaignHarvestSettledEvent}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Campaña ya liquidada previamente para la parcela}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots/{plotId}/harvest-settlements} para un año agrícola que ya cuenta con liquidación formal asentada.\newline
\textbf{When} el servicio de aplicación detecta conflicto de duplicidad de cierre.\newline
\textbf{Then} la API responde \texttt{409 Conflict} impidiendo la sobreescritura del balance definitivo.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Pesajes inconsistentes o valores negativos}\newline
\textbf{Given} una solicitud POST con cifras de kilogramos negativas o incongruentes.\newline
\textbf{When} la API valida las restricciones de dominio agronómico.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS40} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Certificación criptográfica colegiada del expediente agronómico inmutable} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar la rúbrica y número de colegiatura del profesional agronómico a la API, \textbf{para} certificar oficialmente el expediente de la parcela y generar el hash criptográfico SHA-256 inmutable de auditoría.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Certificación exitosa y generación de hash criptográfico}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/plots/{plotId}/agronomic-reports/certifications} con cuerpo JSON: \texttt{campaignYear}, \texttt{certifiedBy}, \texttt{cipNumber} y \texttt{certificationNotes}.\newline
\textbf{When} la API valida la existencia de la liquidación de cosecha, compila el estado integral del expediente y computa la huella digital criptográfica SHA-256.\newline
\textbf{Then} la API responde \texttt{201 Created}, retorna \texttt{DossierCertificationResource} con el hash de verificación y emite \texttt{AgronomicDossierGeneratedEvent}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Intento de doble certificación sobre expediente auditado}\newline
\textbf{Given} una solicitud POST sobre una campaña que ya cuenta con certificación colegiada emitida.\newline
\textbf{When} la API evalúa la inmutabilidad del expediente.\newline
\textbf{Then} la API responde \texttt{409 Conflict} garantizando la no repudiación del documento.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Parcela sin liquidación de campaña previa}\newline
\textbf{Given} una solicitud POST sobre una parcela cuya campaña aún no ha sido liquidada formalmente.\newline
\textbf{When} el servicio de dominio verifica las precondiciones de auditoría.\newline
\textbf{Then} la API responde \texttt{422 Unprocessable Entity}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS41} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP11} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Revocación anticipada y ajuste de vigencia de código de activación cooperativo} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar la solicitud de expiración inmediata de un código no canjeado a la API, \textbf{para} revocar la invitación emitida por la cooperativa y restituir el cupo disponible a la licencia colectiva.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Expiración anticipada y restitución de cupo}\newline
\textbf{Given} una solicitud POST a \url{/api/v1/cooperatives/{id}/invitation-codes/{codeId}/expiry-adjustments} emitida por un gestor técnico institucional.\newline
\textbf{When} la API valida que el código se encuentre en estado AVAILABLE, adelanta su fecha de expiración a la fecha actual y transiciona su estado a EXPIRED.\newline
\textbf{Then} la API responde \texttt{200 OK}, retorna \texttt{InvitationCodeResource} actualizado, emite \texttt{InvitationCodeExpiredEvent} y restituye automáticamente el cupo en la licencia cooperativa.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Código ya canjeado o inactivo}\newline
\textbf{Given} una solicitud POST sobre un código que ya se encuentra en estado REDEEMED o EXPIRED.\newline
\textbf{When} la API evalúa el ciclo de vida del código.\newline
\textbf{Then} la API responde \texttt{409 Conflict} impidiendo la alteración de códigos consumidos.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Acceso no autorizado para no gestores}\newline
\textbf{Given} una solicitud POST emitida por un usuario sin rol GESTOR en la cooperativa.\newline
\textbf{When} la API valida los permisos institucionales.\newline
\textbf{Then} la API responde \texttt{403 Forbidden}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS42} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP15} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Calibración y ajuste de offset edafoclimático para nodo sensor IoT en parcela} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar los factores de calibración y offset ambiental al nodo sensor registrado en la API, \textbf{para} ajustar las lecturas telemétricas según las condiciones de suelo y microclima del lote.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Calibración exitosa de nodo sensor virtual}\newline
\textbf{Given} una solicitud PUT a \url{/api/v1/plots/{plotId}/iot-devices/{deviceId}} con cuerpo JSON conteniendo \texttt{temperatureOffset}, \texttt{humidityOffset}, \texttt{soilCorrectionFactor} y \texttt{calibrationNotes}.\newline
\textbf{When} la API valida la titularidad del lote, comprueba el estado activo de la sonda y aplica los factores de corrección edafoclimática.\newline
\textbf{Then} la API responde \texttt{200 OK}, retorna \texttt{DeviceResource} actualizado y persiste los coeficientes de calibración.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Dispositivo inexistente o desvinculado de la parcela}\newline
\textbf{Given} una solicitud PUT hacia un identificador de sonda no vinculado a la parcela especificada.\newline
\textbf{When} la API consulta el inventario telemétrico.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Parámetros de calibración fuera del rango físico admisible}\newline
\textbf{Given} una solicitud PUT con valores de offset que exceden los límites físicos tolerados.\newline
\textbf{When} el validador de dominio procesa los atributos.\newline
\textbf{Then} la API responde \texttt{400 Bad Request}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{TS43} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Desarrollador de Aplicaciones Cliente} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Media} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP12} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Rectificación de pesaje y baja de registro erróneo de cosecha en histórico fenológico} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} desarrollador de aplicaciones cliente, \textbf{quiero} enviar solicitudes de rectificación o eliminación de pesajes de cosecha pasados a la API, \textbf{para} subsanar errores de digitación en campañas históricas y recomputar el Índice de Vecería (BBI) del olivar.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Rectificación exitosa de pesaje histórico de campaña}\newline
\textbf{Given} una solicitud PUT a \url{/api/v1/plots/{plotId}/harvest-records/{recordId}} con cuerpo JSON: \texttt{rectifiedYieldKg} y \texttt{rectificationReason}.\newline
\textbf{When} la API valida la titularidad, verifica que el valor sea positivo, actualiza el pesaje en la serie histórica y dispara el recálculo reactivo del BBI.\newline
\textbf{Then} la API responde \texttt{200 OK} y retorna \texttt{HarvestRecordResource} actualizado.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Baja física de registro erróneo de cosecha}\newline
\textbf{Given} una solicitud DELETE a \url{/api/v1/plots/{plotId}/harvest-records/{recordId}} emitida por el titular de la parcela.\newline
\textbf{When} la API valida la propiedad y elimina el registro erróneo del histórico productivo.\newline
\textbf{Then} la API responde \texttt{204 No Content} y actualiza el balance plurianual.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Registro de cosecha inexistente}\newline
\textbf{Given} una solicitud PUT o DELETE sobre un \texttt{recordId} no registrado.\newline
\textbf{When} la API consulta la persistencia.\newline
\textbf{Then} la API responde \texttt{404 Not Found}.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{SPK01} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Equipo de Desarrollo de Backend} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP14} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Investigación y modelado dinámico de Erez para cálculo de frío en backend} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} equipo de desarrollo de backend, \textbf{queremos} implementar un prototipo computacional del modelo dinámico de Erez en nuestro backend Spring Boot Java para la Plataforma Viora, \textbf{para} validar la viabilidad matemática de procesar series horarias de temperatura y calibrar el umbral invernal del olivo antes de su integración definitiva en los servicios RESTful.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Ejecución del algoritmo dinámico de dos etapas}\newline
\textbf{Given} series sintéticas de temperatura horaria correspondientes a los meses de reposo invernal.\newline
\textbf{When} el algoritmo procesa las fluctuaciones térmicas acumulando intermediarios termolábiles y porciones de frío fijadas.\newline
\textbf{Then} el prototipo computa con exactitud las porciones de frío de Erez contrastándolas contra el umbral agronómico de 25 a 30 porciones.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Informe de viabilidad y código reproducible}\newline
\textbf{Given} la conclusión de los ensayos de cálculo.\newline
\textbf{When} se evalúa el rendimiento computacional del algoritmo en el backend.\newline
\textbf{Then} el equipo emite un informe técnico de viabilidad y consolida la función matemática en el módulo de dominio del backend.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{SPK02} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Equipo de Desarrollo Móvil} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP14} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Investigación de persistencia local SQLite y protocolo offline-first} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} equipo de desarrollo móvil, \textbf{queremos} construir un prototipo de persistencia local en SQLite (Room / sqflite) para nuestras aplicaciones móviles Android (Kotlin) y Cross-Platform (Flutter) de la Plataforma Viora, \textbf{para} verificar la operatividad offline del muestreo a pie de árbol y comprobar la sincronización bidireccional idempotente con el backend.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Registro y almacenamiento local en modo desconectado}\newline
\textbf{Given} el prototipo móvil funcionando en un entorno simulado sin conexión a internet.\newline
\textbf{When} el usuario registra conteos de muestreo de frutos y brotes a pie de árbol.\newline
\textbf{Then} los datos se persisten de manera inmediata en la base de datos local SQLite y se encolan para su despacho.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Sincronización automática idempotente al recuperar red}\newline
\textbf{Given} un lote de registros pendientes en la cola local de SQLite.\newline
\textbf{When} se restablece la conectividad celular o Wi-Fi.\newline
\textbf{Then} el prototipo despacha los registros al backend y actualiza los identificadores remotos sin duplicar información.} \\ \hline
\end{longtable}
\endgroup

\vspace{1em}

\begingroup
\renewcommand{\arraystretch}{1.15}
\linespread{1.0}\selectfont
\begin{longtable}{|m{0.18\textwidth}|m{0.32\textwidth}|m{0.18\textwidth}|m{0.22\textwidth}|}
\hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Story ID}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{\textbf{User}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Priority}} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{\textbf{Epic}} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{SPK03} & \multicolumn{1}{>{\centering\arraybackslash}m{0.32\textwidth}|}{Equipo de Desarrollo (Móvil y Backend)} & \multicolumn{1}{>{\centering\arraybackslash}m{0.18\textwidth}|}{Alta} & \multicolumn{1}{>{\centering\arraybackslash}m{0.22\textwidth}|}{EP14} \\ \hline
\multicolumn{1}{|>{\centering\arraybackslash}m{0.18\textwidth}|}{\textbf{Title}} & \multicolumn{3}{m{\dimexpr 0.72\textwidth + 4\tabcolsep\relax}|}{Investigación e integración de Checkout Pro en Mercado Pago Sandbox y webhooks} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Description}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Como} equipo de desarrollo (móvil y backend),\newline \textbf{queremos} investigar y prototipar la integración de Mercado Pago Checkout Pro (Sandbox) y webhooks en nuestras aplicaciones móviles Android (Kotlin) / Flutter y backend Spring Boot Java para la Plataforma Viora,\newline \textbf{para} que podamos entender las implicaciones técnicas, riesgos potenciales de transacción y esfuerzo requerido para la implementación completa en los componentes móvil y backend.} \\ \hline
\multicolumn{4}{|>{\centering\arraybackslash}m{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\textbf{Acceptance Criteria}} \\ \hline
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 1: Compatibilidad del backend y generación de preferencia Checkout Pro}\newline
\textbf{Given} el backend Spring Boot Java configurado con credenciales de prueba de Mercado Pago Sandbox.\newline
\textbf{When} el backend procesa una solicitud de suscripción en Soles (PEN) invocando el SDK oficial.\newline
\textbf{Then} genera la preferencia con éxito, retornando el enlace \texttt{init\_point} de Checkout Pro sin capturar datos sensibles de tarjetas.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 2: Integración del flujo de pago en la aplicación móvil}\newline
\textbf{Given} la aplicación móvil (Android Kotlin / Flutter) interactuando con el backend de Viora.\newline
\textbf{When} el usuario inicia el pago de su suscripción y la app móvil recibe el identificador \texttt{init\_point} desde el backend.\newline
\textbf{Then} la aplicación móvil abre de manera segura la pasarela de Checkout Pro mediante Custom Tabs o Deep Linking, permitiendo el abono en Soles (PEN) y retornando el control a la app tras la transacción.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 3: Integración y verificación asíncrona de webhooks en el backend}\newline
\textbf{Given} una notificación asíncrona de pago enviada por el simulador de Mercado Pago Sandbox al endpoint \url{/api/v1/webhooks/payment}.\newline
\textbf{When} el backend Spring Boot verifica el encabezado criptográfico \texttt{x-signature} y valida el estado aprobado del pago.\newline
\textbf{Then} confirma la validez del evento y simula la activación de la suscripción SaaS en la base de datos PostgreSQL de forma idempotente.} \\
\multicolumn{4}{|p{\dimexpr 0.90\textwidth + 6\tabcolsep\relax}|}{\raggedright\noindent \textbf{Escenario 4: Prototipo funcional integrado (PoC) y Definition of Done}\newline
\textbf{Given} la integración de los componentes móvil y backend en el entorno Sandbox.\newline
\textbf{When} el equipo valida el flujo end-to-end de pago y documenta los hallazgos técnicos y el esfuerzo requerido.\newline
\textbf{Then} el PoC funcional queda integrado y versionado en una rama del repositorio, y el spike se completa dentro del \textit{timebox} establecido (8 a 16 horas).} \\ \hline
\end{longtable}
\endgroup

\clearpage

### Impact Mapping

En esta sección se presenta el Impact Mapping del ecosistema Viora, el cual articula los objetivos estratégicos de negocio con las funcionalidades y requisitos de software a desarrollar. Mediante este mapa se conectan las metas de validación comercial, tracción del modelo SaaS y fidelización de convenios institucionales con los User Personas definidos (Teodoro Mamani como productor olivarero y Rubén Ticona como gestor técnico de cooperativa), identificando los impactos conductuales y los entregables de software necesarios para mitigar la vecería prolongada y brindar soporte agronómico y logístico en campo (ver \autoref{fig:im-1-2}, \autoref{fig:im-3-4} y \autoref{fig:im-5-6}).

\begin{figure}[H]
\caption{Impact Mapping de Viora - Partes 1 y 2.} \label{fig:im-1-2}
\centering
\includegraphics[width=0.48\textwidth,height=0.37\textheight,keepaspectratio]{report/assets/impact-mapping/im-1.png}\hfill
\includegraphics[width=0.48\textwidth,height=0.37\textheight,keepaspectratio]{report/assets/impact-mapping/im-2.png}
\caption*{\textit{Nota.} Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{Impact Mapping de Viora - Partes 3 y 4.} \label{fig:im-3-4}
\centering
\includegraphics[width=0.48\textwidth,height=0.37\textheight,keepaspectratio]{report/assets/impact-mapping/im-3.png}\hfill
\includegraphics[width=0.48\textwidth,height=0.37\textheight,keepaspectratio]{report/assets/impact-mapping/im-4.png}
\caption*{\textit{Nota.} Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{Impact Mapping de Viora - Partes 5 y 6.} \label{fig:im-5-6}
\centering
\includegraphics[width=0.48\textwidth,height=0.37\textheight,keepaspectratio]{report/assets/impact-mapping/im-5.png}\hfill
\includegraphics[width=0.48\textwidth,height=0.37\textheight,keepaspectratio]{report/assets/impact-mapping/im-6.png}
\caption*{\textit{Nota.} Elaboración propia.}
\end{figure}

\clearpage

### Product Backlog

El Product Backlog de Viora consolida y prioriza los 89 ítems de trabajo del sistema (43 Historias de Usuario, 43 Technical Stories y 3 Spike Stories) estructurados rigurosamente bajo el criterio de valor para el negocio y mitigación temprana de riesgo técnico. La iteración inicial (Sprint 1) concentra el despliegue íntegro de la Landing Page para asegurar la captación comercial y la credibilidad agronómica, la resolución de spikes críticos (modelo dinámico de Erez, persistencia local offline y pasarela de cobro), la entrega de las pantallas centrales del productor olivarero (delimitación parcelaria, monitoreo de frío, cálculo del índice BBI y muestreo sin conectividad) y el 70% del backend fundacional. Siguiendo las buenas prácticas ágiles, todas las historias de usuario y técnicas de gestión de identidad y accesos (IAM) se posponen al Sprint 3, dado que no aportan valor agronómico directo al usuario final; durante las primeras iteraciones, el backend operará mediante perfiles de entorno automáticos que inyectan el contexto del usuario en la API REST sin requerir credenciales manuales. El Sprint 2 despliega los algoritmos de carga frutal sostenible, prescripción fenológica de aclareo, pasarela de pagos y herramientas territoriales de la cooperativa. Finalmente, el Sprint 3 culmina con la implementación integral de la seguridad e IAM, el cierre productivo de campaña, la proyección logística de acopio diferenciado entre aceituna verde y negra, y la exportación de reportes técnicos auditables en formato PDF.

A continuación, en la \autoref{tab:product-backlog} se presenta la matriz integral del Product Backlog del sistema Viora, detallando el orden de ejecución, identificador, denominación técnica, estimación de esfuerzo en puntos de historia (*Story Points*) y la asignación a su respectivo ciclo de iteración (*Sprint*).

\setcounter{table}{8}

\begin{center}
\small
\renewcommand{\arraystretch}{1.15}
\begin{longtable}{|>{\centering\arraybackslash}p{0.07\textwidth}|>{\centering\arraybackslash}p{0.11\textwidth}|p{0.50\textwidth}|>{\centering\arraybackslash}p{0.11\textwidth}|>{\centering\arraybackslash}p{0.13\textwidth}|}
\caption{Product Backlog priorizado del sistema Viora} \label{tab:product-backlog} \\
\hline
\textbf{\# Orden} & \textbf{Story ID} & \textbf{Título} & \textbf{Story Points} & \textbf{Sprint} \\ \hline
\endfirsthead
\hline
\multicolumn{5}{l}{\textit{Tabla \ref{tab:product-backlog} (Continuación)}} \\ \hline
\textbf{\# Orden} & \textbf{Story ID} & \textbf{Título} & \textbf{Story Points} & \textbf{Sprint} \\ \hline
\endhead
\hline
\endfoot
\hline
\multicolumn{5}{l}{\parbox{16cm}{\vspace{0.1cm} \textit{Nota.} Elaboración propia.}} \\
\endlastfoot
1 & US33 & Presentación de la propuesta de valor central para la mitigación de la vecería prolongada en el olivar & 2 & Sprint 1 \\ \hline
2 & US34 & Exploración de beneficios y capacidades operativas para el productor olivarero & 2 & Sprint 1 \\ \hline
3 & US35 & Exploración de beneficios y herramientas de gestión territorial para cooperativas agrarias & 2 & Sprint 1 \\ \hline
4 & US36 & Visualización de planes de suscripción y tarifas transparentes en moneda nacional (PEN) & 2 & Sprint 1 \\ \hline
5 & US37 & Reproducción del video promocional y demostrativo del producto (''About the Product'') & 1 & Sprint 1 \\ \hline
6 & US38 & Reproducción del video institucional sobre el equipo y proceso de ingeniería (''About the Team'') & 1 & Sprint 1 \\ \hline
7 & US39 & Consulta de términos de servicio y política de privacidad y protección de datos (Ley N° 29733) & 1 & Sprint 1 \\ \hline
8 & US40 & Redirección y acceso a la descarga oficial de la aplicación móvil & 1 & Sprint 1 \\ \hline
9 & US41 & Selección de idioma y localización de contenidos en la Landing Page & 2 & Sprint 1 \\ \hline
10 & SPK01 & Investigación y modelado dinámico de Erez para cálculo de frío en backend & 3 & Sprint 1 \\ \hline
11 & SPK02 & Investigación de persistencia local SQLite y protocolo offline-first & 3 & Sprint 1 \\ \hline
12 & SPK03 & Investigación e integración de Checkout Pro en Mercado Pago Sandbox y webhooks & 3 & Sprint 1 \\ \hline
13 & US09 & Delimitación georreferenciada de parcela con GPS y caracterización agronómica inicial & 5 & Sprint 1 \\ \hline
14 & US10 & Consulta y modificación de linderos y datos dendrométricos de parcela & 3 & Sprint 1 \\ \hline
15 & US17 & Monitoreo agroclimático y consulta de series temporales de suelo y microclima & 5 & Sprint 1 \\ \hline
16 & US20 & Registro retrospectivo de campañas históricas de cosecha y cálculo del Índice de Vecería (BBI) & 5 & Sprint 1 \\ \hline
17 & US21 & Modificación y rectificación de registros históricos de cosecha & 2 & Sprint 1 \\ \hline
18 & US24 & Muestreo guiado de cuajado en campo a pie de árbol con persistencia local offline & 5 & Sprint 1 \\ \hline
19 & US25 & Consulta de representatividad estadística e historial de árboles muestreados en campo & 3 & Sprint 1 \\ \hline
20 & US13 & Vinculación y alta de nodo sensor virtual a una parcela & 3 & Sprint 1 \\ \hline
21 & US14 & Consulta de inventario y estado operativo de nodos sensores virtuales en parcela & 2 & Sprint 1 \\ \hline
22 & TS31 & Manejo centralizado de excepciones y errores bajo estándar RFC 7807 & 1 & Sprint 1 \\ \hline
23 & TS32 & Convenciones de persistencia relacional, nomenclatura ORM y tipado espacial & 1 & Sprint 1 \\ \hline
24 & TS33 & Generación dinámica y documentación interactiva de contratos de API con OpenAPI 3.0 & 1 & Sprint 1 \\ \hline
25 & TS34 & Resolución de localización y mensajes internacionalizados mediante cabecera Accept-Language & 1 & Sprint 1 \\ \hline
26 & TS11 & Creación y delimitación poligonal de parcelas georreferenciadas & 5 & Sprint 1 \\ \hline
27 & TS12 & Listado y sincronización incremental delta de parcelas & 3 & Sprint 1 \\ \hline
28 & TS13 & Consulta detallada de información agronómica y espacial de parcela & 2 & Sprint 1 \\ \hline
29 & TS14 & Actualización y rectificación integral de parcela con bloqueo optimista & 3 & Sprint 1 \\ \hline
30 & TS15 & Eliminación y baja lógica de parcela del inventario & 2 & Sprint 1 \\ \hline
31 & TS16 & Alta y vinculación de nodo sensor virtual a parcela & 3 & Sprint 1 \\ \hline
32 & TS17 & Consulta de inventario de nodos virtuales vinculados a parcela & 2 & Sprint 1 \\ \hline
33 & TS18 & Desvinculación de nodo virtual preservando trazabilidad histórica & 2 & Sprint 1 \\ \hline
34 & TS19 & Consulta de series temporales de telemetría ambiental y de suelo & 3 & Sprint 1 \\ \hline
35 & TS20 & Consulta de pronóstico meteorológico geolocalizado a 7 días & 3 & Sprint 1 \\ \hline
36 & TS21 & Asentamiento de cosecha anual por campaña para auditoría productiva & 3 & Sprint 1 \\ \hline
37 & TS22 & Consulta del historial plurianual de cosechas de la parcela & 2 & Sprint 1 \\ \hline
38 & TS23 & Cálculo y entrega de métricas de vecería BBI y frío dinámico de Erez & 5 & Sprint 1 \\ \hline
39 & TS24 & Registro y sincronización de muestreos guiados de cuajado en campo & 5 & Sprint 1 \\ \hline
40 & TS25 & Consulta de representatividad estadística y estado de muestreo & 3 & Sprint 1 \\ \hline
41 & TS26 & Consulta de prescripción técnica de aclareo y ventana fenológica & 3 & Sprint 1 \\ \hline
42 & TS27 & Confirmación y registro de ejecución de labor de aclareo en campo & 3 & Sprint 1 \\ \hline
43 & TS08 & Generación de lote de códigos de activación para socios cooperativos & 3 & Sprint 1 \\ \hline
44 & TS09 & Consulta y auditoría de códigos de activación de cooperativa & 2 & Sprint 1 \\ \hline
45 & TS10 & Canje de código de activación de socio para vinculación cooperativa & 3 & Sprint 1 \\ \hline
46 & US22 & Monitoreo dinámico de porciones de frío invernal acumuladas mediante el modelo de Erez & 5 & Sprint 2 \\ \hline
47 & US23 & Detección de anomalías térmicas invernales y advertencia de riesgo floral por efecto ENOS & 3 & Sprint 2 \\ \hline
48 & US26 & Cálculo de carga frutal objetivo sostenible y rendimiento potencial de campaña & 5 & Sprint 2 \\ \hline
49 & US27 & Prescripción técnica in-app de porcentaje y ventana fenológica de aclareo & 5 & Sprint 2 \\ \hline
50 & US28 & Registro y confirmación de ejecución de aclareo en campo & 3 & Sprint 2 \\ \hline
51 & US06 & Suscripción individual al Plan Productor mediante pasarela de pago digital & 5 & Sprint 2 \\ \hline
52 & US07 & Activación de cuenta de socio mediante canje de código de cooperativa & 3 & Sprint 2 \\ \hline
53 & US08 & Administración de la cartera de socios productores y consulta de cuota corporativa & 3 & Sprint 2 \\ \hline
54 & US12 & Consulta de la matriz de riesgo territorial y semáforo sectorial con geolocalización GPS & 5 & Sprint 2 \\ \hline
55 & TS06 & Generación de preferencia de checkout para suscripción de productor independiente & 3 & Sprint 2 \\ \hline
56 & TS07 & Recepción y procesamiento de webhooks de notificación de pagos & 5 & Sprint 2 \\ \hline
57 & TS28 & Generación y descarga de reporte agronómico auditable en formato PDF & 5 & Sprint 2 \\ \hline
58 & TS29 & Consulta de la matriz de riesgo territorial y semáforo sectorial para el gestor técnico & 3 & Sprint 2 \\ \hline
59 & TS30 & Proyección agregada temprana de volumen de acopio cooperativo & 5 & Sprint 2 \\ \hline
60 & US29 & Asentamiento formal de cosecha de fin de campaña y balance de estabilización productiva & 3 & Sprint 3 \\ \hline
61 & US30 & Emisión, certificación criptográfica y exportación del expediente agronómico en PDF & 5 & Sprint 3 \\ \hline
62 & US31 & Semáforo fenológico reactivo y priorización técnica ante sobrecarga crítica & 5 & Sprint 3 \\ \hline
63 & US32 & Proyección agregada temprana de volumen de acopio de aceituna verde y negra para la cooperativa & 5 & Sprint 3 \\ \hline
64 & US18 & Alertas automáticas de estrés hídrico y umbral térmico crítico en parcela & 3 & Sprint 3 \\ \hline
65 & US19 & Consulta de pronóstico meteorológico geolocalizado a 7 días & 3 & Sprint 3 \\ \hline
66 & US15 & Configuración y calibración de nodo sensor virtual en parcela & 2 & Sprint 3 \\ \hline
67 & US16 & Desvinculación y baja de nodo sensor virtual de una parcela & 2 & Sprint 3 \\ \hline
68 & US11 & Baja y remoción de parcela del inventario productivo & 2 & Sprint 3 \\ \hline
69 & US42 & Configuración y cambio de idioma de la interfaz en la aplicación móvil & 2 & Sprint 3 \\ \hline
70 & US01 & Registro de cuenta de acceso y credenciales seguras con asignación de rol & 3 & Sprint 3 \\ \hline
71 & US02 & Inicio de sesión y autenticación persistente mediante tokens & 3 & Sprint 3 \\ \hline
72 & US03 & Consulta y actualización de datos de perfil y contacto & 2 & Sprint 3 \\ \hline
73 & US04 & Cambio seguro de contraseña de acceso & 2 & Sprint 3 \\ \hline
74 & US05 & Recuperación de contraseña olvidada mediante enlace por correo & 3 & Sprint 3 \\ \hline
75 & TS01 & Registro de credenciales de cuenta de usuario y asignación de rol en IAM & 3 & Sprint 3 \\ \hline
76 & TS02 & Autenticación de usuarios y emisión de tokens JWT con claims de rol & 3 & Sprint 3 \\ \hline
77 & TS03 & Renovación periódica de tokens de sesión mediante Refresh Token & 2 & Sprint 3 \\ \hline
78 & TS04 & Consulta de información de perfil del usuario autenticado & 2 & Sprint 3 \\ \hline
79 & TS05 & Actualización parcial de datos de perfil con validación telefónica E.164 & 2 & Sprint 3 \\ \hline
80 & US43 & Completado de perfil de usuario y contacto validado bajo estándar E.164 & 3 & Sprint 3 \\ \hline
81 & TS35 & Creación y completado inicial de perfil de usuario con validación telefónica E.164 & 3 & Sprint 3 \\ \hline
82 & TS36 & Actualización de contraseña para sesión de usuario autenticado & 2 & Sprint 3 \\ \hline
83 & TS37 & Solicitud de código de restablecimiento de contraseña olvidada vía correo & 3 & Sprint 3 \\ \hline
84 & TS38 & Restablecimiento de contraseña mediante token temporal de un solo uso & 2 & Sprint 3 \\ \hline
85 & TS39 & Asentamiento formal y balance de liquidación de cosecha de fin de campaña & 3 & Sprint 3 \\ \hline
86 & TS40 & Certificación criptográfica colegiada del expediente agronómico inmutable & 3 & Sprint 3 \\ \hline
87 & TS41 & Revocación anticipada y ajuste de vigencia de código de activación cooperativo & 2 & Sprint 3 \\ \hline
88 & TS42 & Calibración y ajuste de offset edafoclimático para nodo sensor IoT en parcela & 2 & Sprint 3 \\ \hline
89 & TS43 & Rectificación de pesaje y baja de registro erróneo de cosecha en histórico fenológico & 2 & Sprint 3 \\ \hline
\end{longtable}
\end{center}

Para garantizar la visibilidad compartida, la trazabilidad ágil y la gestión continua del flujo de trabajo, los 89 ítems del Product Backlog han sido registrados y priorizados en la herramienta colaborativa Trello. La \autoref{fig:trello-product-backlog} ilustra el tablero del proyecto, donde cada tarjeta consolida su código de historia, denominación estandarizada, etiquetas visuales de esfuerzo y componente, así como la narrativa ágil de usuario.

\begin{figure}[H]
\caption{Product Backlog del proyecto Viora en Trello.} \label{fig:trello-product-backlog}
\centering
\includegraphics[width=0.95\textwidth,height=0.45\textheight,keepaspectratio]{report/assets/trello/product-backlog.png}
\caption*{\textit{Nota.} Elaboración propia a partir de la herramienta Trello. Disponible en: \url{https://tinyurl.com/1acc0238-product-backlog}.}
\end{figure}

\clearpage  