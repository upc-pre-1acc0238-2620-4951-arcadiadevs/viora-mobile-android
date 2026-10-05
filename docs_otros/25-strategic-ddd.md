## Strategic-Level Domain-Driven Design

En esta sección se introduce y fundamenta el proceso metodológico desarrollado para la toma de decisiones arquitectónicas de nivel estratégico aplicando Domain-Driven Design (DDD). Se expone el procedimiento integral para descomponer la complejidad del dominio agronómico del olivo en subconjuntos cohesivos con límites naturales o Bounded Contexts, asegurando una demarcación clara de responsabilidades, la definición rigurosa del lenguaje ubicuo y la articulación del ecosistema Viora mediante herramientas analíticas como EventStorming, Bounded Context Canvas, Context Mapping y el modelado arquitectónico C4.

### EventStorming

Para la construcción del EventStorming, se coordinó la obtención de una primera versión integral del modelo de dominio de Viora a través de un proceso estructurado en 9 etapas consecutivas, transitando desde la exploración divergente hasta la delimitación formal de las fronteras transaccionales.

\noindent \textbf{Paso 1: Exploración y descubrimiento de eventos de dominio}

Exploración abierta y divergente de todos los hechos inmutables significativos ocurridos en el ciclo agronómico, productivo y comercial del olivo en Tacna (ver \autoref{fig:es-p1-events}).

\begin{figure}[H]
\caption{EventStorming - Paso 1: Exploración de Domain Events (Brainstorming).} \label{fig:es-p1-events}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/1-domain-events.jpg}
\caption*{\textit{Nota.} Distribución de los 54 eventos de dominio capturados en post-its naranjas a lo largo de los diferentes ámbitos del ciclo de cultivo y gestión. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 2: Secuenciación y estructuración de líneas de tiempo}

Ordenamiento cronológico continuo de los eventos de izquierda a derecha, estructurado en ocho líneas de tiempo concurrentes según las fases fenológicas y operativas del cultivo (ver \autoref{fig:es-p2-timelines}).

\begin{figure}[H]
\caption{EventStorming - Paso 2: Estructuración de Timelines del Dominio.} \label{fig:es-p2-timelines}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/2-timeline.jpg}
\caption*{\textit{Nota.} Secuenciación temporal de los 54 eventos de dominio organizados en 8 líneas de tiempo concurrentes e interconectadas mediante dependencias causales. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 3: Identificación de puntos de dolor}

Señalización sistemática de las fricciones operativas, cuellos de botella, riesgos fisiológicos e incertidumbres críticas que enfrentan productores y gestores técnicos (ver \autoref{fig:es-p3-pp-all}, \autoref{fig:es-p3-pp-1}, \autoref{fig:es-p3-pp-2} y \autoref{fig:es-p3-pp-3}).

\begin{figure}[H]
\caption{EventStorming - Paso 3: Identificación de Pain Points (Vista general).} \label{fig:es-p3-pp-all}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/3-pain-points.jpg}
\caption*{\textit{Nota.} Distribución panorámica de los 24 pain points identificados en post-its rojos a lo largo de las timelines del sistema. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 3: Identificación de Pain Points (Primera división).} \label{fig:es-p3-pp-1}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/3-pain-points-1.jpg}
\caption*{\textit{Nota.} Detalle del tablero de pain points: primera división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 3: Identificación de Pain Points (Segunda división).} \label{fig:es-p3-pp-2}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/3-pain-points-2.jpg}
\caption*{\textit{Nota.} Detalle del tablero de pain points: segunda división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 3: Identificación de Pain Points (Tercera división).} \label{fig:es-p3-pp-3}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/3-pain-points-3.jpg}
\caption*{\textit{Nota.} Detalle del tablero de pain points: tercera división. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 4: Identificación de eventos clave de negocio}

Determinación de los hitos agronómicos y de negocio de no retorno que marcan la culminación de una etapa y desbloquean la transición hacia la siguiente fase operativa (ver \autoref{fig:es-p4-pivotal}).

\begin{figure}[H]
\caption{EventStorming - Paso 4: Identificación de Pivotal Events.} \label{fig:es-p4-pivotal}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/4-pivotal-events.jpg}
\caption*{\textit{Nota.} Delimitación de los 11 pivotal events que actúan como umbrales divisores de fase en el ciclo productivo y de gestión. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 5: Identificación de comandos y actores}

Especificación de las intenciones de acción invocadas por usuarios clave (productores y asesores técnicos) que provocan cambios de estado en el sistema (ver \autoref{fig:es-p5-cmd-all}, \autoref{fig:es-p5-cmd-1}, \autoref{fig:es-p5-cmd-2} y \autoref{fig:es-p5-cmd-3}).

\begin{figure}[H]
\caption{EventStorming - Paso 5: Comandos y Actores (Vista general).} \label{fig:es-p5-cmd-all}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/5-commands-actors.jpg}
\caption*{\textit{Nota.} Incorporación de los 35 comandos (post-its azules) y etiquetas de actores (post-its amarillos pequeños) sobre el flujo de eventos. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 5: Comandos y Actores (Primera división).} \label{fig:es-p5-cmd-1}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/5-commands-actors-1.jpg}
\caption*{\textit{Nota.} Detalle del tablero de comandos y actores: primera división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 5: Comandos y Actores (Segunda división).} \label{fig:es-p5-cmd-2}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/5-commands-actors-2.jpg}
\caption*{\textit{Nota.} Detalle del tablero de comandos y actores: segunda división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 5: Comandos y Actores (Tercera división).} \label{fig:es-p5-cmd-3}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/5-commands-actors-3.jpg}
\caption*{\textit{Nota.} Detalle del tablero de comandos y actores: tercera división. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 6: Definición de políticas reactivas}

Modelado de las reglas reactivas de negocio que ejecutan comandos de forma autónoma ante la ocurrencia de eventos de dominio bajo condiciones específicas (ver \autoref{fig:es-p6-pol-all}, \autoref{fig:es-p6-pol-1}, \autoref{fig:es-p6-pol-2} y \autoref{fig:es-p6-pol-3}).

\begin{figure}[H]
\caption{EventStorming - Paso 6: Políticas Reactivas (Vista general).} \label{fig:es-p6-pol-all}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/6-policies.jpg}
\caption*{\textit{Nota.} Mapeo panorámico de las 19 políticas reactivas (post-its lila) que orquestan los procesos automáticos y la colaboración reactiva. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 6: Políticas Reactivas (Primera división).} \label{fig:es-p6-pol-1}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/6-policies-1.jpg}
\caption*{\textit{Nota.} Detalle del tablero de políticas reactivas: primera división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 6: Políticas Reactivas (Segunda división).} \label{fig:es-p6-pol-2}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/6-policies-2.jpg}
\caption*{\textit{Nota.} Detalle del tablero de políticas reactivas: segunda división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 6: Políticas Reactivas (Tercera división).} \label{fig:es-p6-pol-3}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/6-policies-3.jpg}
\caption*{\textit{Nota.} Detalle del tablero de políticas reactivas: tercera división. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 7: Identificación de modelos de lectura}

Definición de las proyecciones de información y pantallas consolidadas que los usuarios consultan para evaluar el estado del lote y tomar decisiones operativas (ver \autoref{fig:es-p7-rm-all}, \autoref{fig:es-p7-rm-1}, \autoref{fig:es-p7-rm-2} y \autoref{fig:es-p7-rm-3}).

\begin{figure}[H]
\caption{EventStorming - Paso 7: Read Models (Vista general).} \label{fig:es-p7-rm-all}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/7-read-models.jpg}
\caption*{\textit{Nota.} Ubicación de los 15 read models (post-its verdes) que proporcionan el contexto informativo previo a la ejecución de comandos. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 7: Read Models (Primera división).} \label{fig:es-p7-rm-1}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/7-read-models-1.jpg}
\caption*{\textit{Nota.} Detalle del tablero de read models: primera división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 7: Read Models (Segunda división).} \label{fig:es-p7-rm-2}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/7-read-models-2.jpg}
\caption*{\textit{Nota.} Detalle del tablero de read models: segunda división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 7: Read Models (Tercera división).} \label{fig:es-p7-rm-3}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/7-read-models-3.jpg}
\caption*{\textit{Nota.} Detalle del tablero de read models: tercera división. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 8: Identificación de sistemas externos}

Delimitación de las cuatro plataformas de terceros y servicios auxiliares fuera de la frontera de Viora con los que el ecosistema intercambia datos o delega transacciones (ver \autoref{fig:es-p8-ext-all}, \autoref{fig:es-p8-ext-1}, \autoref{fig:es-p8-ext-2} y \autoref{fig:es-p8-ext-3}): la pasarela de pagos digitales (Mercado Pago Checkout Pro), el proveedor cartográfico satelital (Mapbox), el servicio agrometeorológico de series horarias y pronóstico (identificado funcionalmente como Agroclimatic Weather API y materializado en la arquitectura con Open-Meteo), y el servicio de despacho de correos transaccionales con tokens de recuperación de acceso (Transactional Mail Service, adoptando a Brevo en el diseño arquitectónico).

\begin{figure}[H]
\caption{EventStorming - Paso 8: Sistemas Externos (Vista general).} \label{fig:es-p8-ext-all}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/8-external-systems.jpg}
\caption*{\textit{Nota.} Integración de los 4 sistemas externos (post-its rosa): Mercado Pago Checkout Pro, Mapbox GIS, servicio meteorológico (Open-Meteo / Weather API) y servicio de correo transaccional (Brevo / Mail Service). Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 8: Sistemas Externos (Primera división).} \label{fig:es-p8-ext-1}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/8-external-systems-1.jpg}
\caption*{\textit{Nota.} Detalle del tablero de sistemas externos: primera división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 8: Sistemas Externos (Segunda división).} \label{fig:es-p8-ext-2}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/8-external-systems-2.jpg}
\caption*{\textit{Nota.} Detalle del tablero de sistemas externos: segunda división. Elaboración propia.}
\end{figure}

\begin{figure}[H]
\caption{EventStorming - Paso 8: Sistemas Externos (Tercera división).} \label{fig:es-p8-ext-3}
\centering
\includegraphics[width=0.85\textwidth,height=0.28\textheight,keepaspectratio]{report/assets/event-storming/8-external-systems-3.jpg}
\caption*{\textit{Nota.} Detalle del tablero de sistemas externos: tercera división. Elaboración propia.}
\end{figure}

\noindent \textbf{Paso 9: Definición de agregados de dominio}

Encapsulamiento de entidades y objetos de valor en unidades transaccionales atómicas responsables de salvaguardar las invariantes del negocio en cada subdominio (ver \autoref{fig:es-p9-aggregates}).

\begin{figure}[H]
\caption{EventStorming - Paso 9: Agregados de Dominio.} \label{fig:es-p9-aggregates}
\vspace{0.25cm}
\centering
\includegraphics[width=0.82\textwidth,height=0.32\textheight,keepaspectratio]{report/assets/event-storming/9-aggregates.jpg}
\caption*{\textit{Nota.} Delimitación de los 12 agregados de dominio (post-its amarillos grandes) como guardianes de consistencia entre comandos y eventos. Elaboración propia.}
\end{figure}

\clearpage 

#### Candidate Context Discovery
&nbsp;

A partir del modelo de EventStorming realizado en Miro, se llevó a cabo una sesión de \textit{Candidate Context Discovery} para identificar y formalizar los bounded contexts de la solución Viora. Se utilizó principalmente la técnica estratégica \textit{look-for-pivotal-events} durante la sesión.

Primero, se buscaron eventos clave que indiquen cambios de estado e hitos irreversibles entre diferentes partes del proceso del negocio (ver \autoref{fig:ccd-p1-pivot}):

\begin{figure}[H]
\caption{Candidate Context Discovery - Paso 1: Búsqueda de Pivotal Events.} \label{fig:ccd-p1-pivot}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.25\textheight,keepaspectratio]{report/assets/event-storming/ccd/1-pivot.jpg}
\caption*{\textit{Nota.} Identificación de los pivotal events que marcan puntos de inflexión y cambios de fase cualitativos en el ciclo de vida del olivar y la plataforma. Elaboración propia.}
\end{figure}

Luego, se agruparon los eventos de acuerdo a los principales cambios de contexto y cohesión funcional (ver \autoref{fig:ccd-p2-groups}):

\begin{figure}[H]
\caption{Candidate Context Discovery - Paso 2: Agrupación de eventos por afinidad funcional.} \label{fig:ccd-p2-groups}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.25\textheight,keepaspectratio]{report/assets/event-storming/ccd/2-groups.jpg}
\caption*{\textit{Nota.} Formación de clústeres preliminares de eventos alrededor de los eventos pivote según la naturaleza de sus responsabilidades de negocio. Elaboración propia.}
\end{figure}

Se trazaron fronteras alrededor de los grupos identificados, estableciendo los límites iniciales de los bounded contexts (ver \autoref{fig:ccd-p3-boundaries}):

\begin{figure}[H]
\caption{Candidate Context Discovery - Paso 3: Trazado de fronteras de contextos iniciales.} \label{fig:ccd-p3-boundaries}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.25\textheight,keepaspectratio]{report/assets/event-storming/ccd/3-boundaries.jpg}
\caption*{\textit{Nota.} Delimitación de las fronteras semánticas iniciales que aíslan los modelos conceptuales y la consistencia transaccional. Elaboración propia.}
\end{figure}

Finalmente, se seleccionaron nombres ubicuos para los bounded contexts, dando como resultado la definición formal de 9 bounded contexts y la versión final del EventStorming (ver \autoref{fig:ccd-p4-bc}):

\begin{figure}[H]
\caption{Candidate Context Discovery - Paso 4: Definición final de los 9 Bounded Contexts.} \label{fig:ccd-p4-bc}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.25\textheight,keepaspectratio]{report/assets/event-storming/ccd/4-bc.jpg}
\caption*{\textit{Nota.} Mural general del EventStorming con los 9 bounded contexts delimitados y nombrados según el lenguaje ubicuo del dominio olivarero. Elaboración propia.}
\end{figure}

\clearpage

A continuación, se explica en qué consiste cada uno de los 9 bounded contexts identificados para el ecosistema Viora:

\noindent \textbf{Identity and Access Management (IAM):} También llamado "IAM", este bounded context genérico contiene el proceso de autenticación segura, inicio de sesión persistente, renovación de sesiones, recuperación de credenciales mediante tokens efímeros y asignación de privilegios de acceso según el rol (ver \autoref{fig:bc-iam}).

\begin{figure}[H]
\caption{Bounded Context: Identity and Access Management (IAM).} \label{fig:bc-iam}
\vspace{0.25cm}
\centering
\includegraphics[width=0.70\textwidth,height=0.17\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/1-iam.jpg}
\caption*{\textit{Nota.} Elementos de dominio, agregados y flujos pertenecientes al bounded context de IAM. Elaboración propia.}
\end{figure}

\noindent \textbf{User Profiles:} También llamado "Profiles", este bounded context de soporte contiene el proceso de registro y formalización de la identidad personal del agricultor, administración de nombres completos, país de residencia y validación estricta de números telefónicos de contacto bajo la norma internacional E.164 (ver \autoref{fig:bc-profiles}).

\begin{figure}[H]
\caption{Bounded Context: User Profiles (Profiles).} \label{fig:bc-profiles}
\vspace{0.25cm}
\centering
\includegraphics[width=0.70\textwidth,height=0.17\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/2-up.jpg}
\caption*{\textit{Nota.} Elementos de dominio y agregado Profile para la gestión de identidad humana y canales de contacto. Elaboración propia.}
\end{figure}

\clearpage

\noindent \textbf{Subscription and Cooperative Membership:} También llamado "Subscription", este bounded context genérico contiene el proceso de suscripción comercial al Plan Productor mediante pasarela de pagos digital (Mercado Pago Checkout Pro) en Soles (PEN), canje de códigos corporativos patrocinados por cooperativas y fiscalización del cupo de hectáreas catastradas autorizadas (ver \autoref{fig:bc-subscription}).

\begin{figure}[H]
\caption{Bounded Context: Subscription and Cooperative Membership (Subscription).} \label{fig:bc-subscription}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.21\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/3-scm.jpg}
\caption*{\textit{Nota.} Elementos de dominio para la monetización SaaS, pasarela de pagos y gestión de membresías. Elaboración propia.}
\end{figure}

\noindent \textbf{Olive Orchard and Plot Management:} También llamado "Orchard", este bounded context de soporte contiene el proceso de delimitación georreferenciada de predios y cuarteles olivareros mediante polígonos cerrados GeoJSON, cálculo de cabida neta en hectáreas, tipificación de variedades cultivadas (Criolla o Sevillana) y cómputo de la densidad arbórea por hectárea (ver \autoref{fig:bc-orchard}).

\begin{figure}[H]
\caption{Bounded Context: Olive Orchard and Plot Management (Orchard).} \label{fig:bc-orchard}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.21\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/4-oopm.jpg}
\caption*{\textit{Nota.} Elementos de dominio para el catastro predial y la estructuración dendrométrica. Elaboración propia.}
\end{figure}

\clearpage

\noindent \textbf{Agroclimatic Telemetry and Sensor Monitoring:} También llamado "Telemetry", este bounded context de soporte contiene el proceso de vinculación y calibración de dispositivos sensores edáficos virtuales (sondas a 30 y 60 cm), ingesta continua de lecturas horarias de humedad radicular y microclima, integración de pronósticos a 7 días y activación automática de alertas ante estrés hídrico y choques térmicos en floración (ver \autoref{fig:bc-telemetry}).

\begin{figure}[H]
\caption{Bounded Context: Agroclimatic Telemetry and Sensor Monitoring (Telemetry).} \label{fig:bc-telemetry}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.21\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/5-atsm.jpg}
\caption*{\textit{Nota.} Elementos de dominio para la sensometría edáfica, series temporales y vigilancia climática. Elaboración propia.}
\end{figure}

\noindent \textbf{Phenology and Historical Bearing Analytics:} También llamado "Phenology", este bounded context del core agronómico contiene el proceso de registro de memorias plurianuales de cosecha, cálculo oficial del Índice de Vecería ($BBI$ de Hoblyn et al., 1936) y cómputo de Porciones de Frío invernales (Modelo de Erez) para detectar anomalías térmicas por efecto ENOS y reajustar el potencial de floración (ver \autoref{fig:bc-phenology}).

\begin{figure}[H]
\caption{Bounded Context: Phenology and Historical Bearing Analytics (Phenology).} \label{fig:bc-phenology}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.21\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/6-phba.jpg}
\caption*{\textit{Nota.} Elementos de dominio para la memoria productiva de vecería y el cómputo bioclimático de frío de Erez. Elaboración propia.}
\end{figure}

\clearpage

\noindent \textbf{Crop Load Regulation and Thinning Advisory:} También llamado "Thinning", este bounded context representa el núcleo de valor primario (\textit{Core Domain}) de Viora y contiene el proceso de muestreo a pie de árbol de frutos cuajados, evaluación de representatividad estadística, determinación de carga admisible sostenible, emisión de prescripciones de aclareo frutal, monitoreo de la ventana límite previa al endurecimiento del carozo y auditoría de la labor ejecutada (ver \autoref{fig:bc-thinning}).

\begin{figure}[H]
\caption{Bounded Context: Crop Load Regulation and Thinning Advisory (Thinning).} \label{fig:bc-thinning}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.21\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/7-clrta.jpg}
\caption*{\textit{Nota.} Elementos del Core Domain primario para la regulación de carga frutal y mitigación de la alternancia. Elaboración propia.}
\end{figure}

\noindent \textbf{Harvest Settlement and Performance Reporting:} También llamado "Harvest", este bounded context contiene el proceso de asentamiento definitivo de la cosecha anual (kilos de aceituna verde y negra recolectados), evaluación de la curva interanual de estabilización productiva y generación del expediente técnico agronómico oficial certificado en PDF (ver \autoref{fig:bc-harvest}).

\begin{figure}[H]
\caption{Bounded Context: Harvest Settlement and Performance Reporting (Harvest).} \label{fig:bc-harvest}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.21\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/8-hspr.jpg}
\caption*{\textit{Nota.} Elementos de dominio para el cierre de campaña, curvas de estabilización y generación de expedientes técnicos. Elaboración propia.}
\end{figure}

\clearpage

\noindent \textbf{Cooperative Operations and Territorial Intelligence:} También llamado "Territory", este bounded context de soporte contiene el proceso de administración gremial del padrón de socios productores olivareros, emisión y control de lotes de códigos corporativos, supervisión del semáforo territorial de riesgo fenológico y cálculo de proyecciones tempranas de volumen de acopio colectivo (ver \autoref{fig:bc-territory}).

\begin{figure}[H]
\caption{Bounded Context: Cooperative Operations and Territorial Intelligence (Territory).} \label{fig:bc-territory}
\vspace{0.25cm}
\centering
\includegraphics[width=0.75\textwidth,height=0.21\textheight,keepaspectratio]{report/assets/event-storming/ccd/bc/9-coti.jpg}
\caption*{\textit{Nota.} Elementos de dominio para la inteligencia territorial, supervisión sectorial y estimaciones de acopio gremial. Elaboración propia.}
\end{figure}

\clearpage 

#### Domain Message Flows Modeling
&nbsp;

Los Domain Message Flows de Viora describen el intercambio de mensajes entre los actores, la aplicación móvil, los sistemas externos y los bounded contexts definidos durante el diseño estratégico. Mediante una representación de Domain Storytelling, cada diagrama presenta un escenario concreto e identifica el emisor, el receptor, el orden de los mensajes y la información significativa que transportan.

Los ocho escenarios seleccionados abarcan la regulación de carga frutal, la evaluación de la alternancia, el seguimiento agroclimático, la ejecución del raleo, el cierre de cosecha, la generación de dosieres y los procesos de suscripción y recuperación de acceso. Los comandos se representan mediante post-its celestes, los eventos mediante post-its naranjas y sus contenidos mediante notas amarillas. Cuando la aplicación móvil aparece en distintas posiciones, se representa el mismo sistema en diferentes momentos o sesiones del flujo.

\newpage

A continuación, se presentan los escenarios que muestran colaboración entre bounded contexts:

\noindent \textbf{Escenario 1: Sincronizar el muestreo, prescribir el raleo y actualizar el riesgo y el acopio cooperativo.}

En este flujo se muestra la interacción entre los bounded contexts Crop Load Regulation and Thinning Advisory y Cooperative Operations and Territorial Intelligence cuando el productor sincroniza un lote de muestras mediante la aplicación móvil. El escenario considera un muestreo suficiente y representativo, una parcela cooperativa y una situación de sobrecarga dentro de la ventana de raleo. A partir de la información recibida, Viora emite la prescripción y actualiza la proyección de acopio y la matriz territorial de riesgo, comunicando los resultados al productor y al gestor técnico (ver \autoref{fig:dmf-1}).

\begin{figure}[H]
\caption{Domain Message Flow 1: Sincronización de muestreo, prescripción de raleo y actualización cooperativa.} \label{fig:dmf-1}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-7.png}
\caption*{\textit{Nota.} Colaboración entre Thinning y Territory a partir de una única solicitud de sincronización del productor. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Escenario 2: Registrar el histórico y ajustar la evaluación de carga y el raleo.}

En este flujo se muestra la colaboración entre los bounded contexts Phenology and Historical Bearing Analytics, Crop Load Regulation and Thinning Advisory y Cooperative Operations and Territorial Intelligence. El productor registra un histórico suficiente para evaluar el índice de vecería; este resultado permite reevaluar la carga sostenible utilizando el muestreo de la campaña actual, previamente disponible. Para el caso de sobrecarga y ventana de raleo vigente, Viora emite una prescripción y actualiza la matriz de riesgo cooperativo. La aplicación comunica al productor el índice y la recomendación, y al gestor técnico la evaluación territorial (ver \autoref{fig:dmf-2}).

\begin{figure}[H]
\caption{Domain Message Flow 2: Registro histórico, evaluación de alternancia y ajuste del raleo.} \label{fig:dmf-2}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-6.png}
\caption*{\textit{Nota.} Interacción entre Phenology, Thinning y Territory con histórico suficiente y muestreo actual previamente registrado. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Escenario 3: Incorporar telemetría y reajustar el potencial floral ante una anomalía invernal.}

En este flujo se muestra la interacción entre los bounded contexts Agroclimatic Telemetry and Sensor Monitoring y Phenology and Historical Bearing Analytics. El proceso comienza automáticamente con la incorporación de lecturas de un nodo virtual previamente vinculado y calibrado. Cuando se dispone de la serie necesaria, se solicita el cálculo de la acumulación diaria de frío. El escenario representa la detección de una anomalía térmica invernal y el posterior reajuste del potencial floral, cuyos resultados se comunican a la aplicación móvil para su seguimiento (ver \autoref{fig:dmf-3}).

\begin{figure}[H]
\caption{Domain Message Flow 3: Ingesta de telemetría y reajuste del potencial floral.} \label{fig:dmf-3}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-5.png}
\caption*{\textit{Nota.} Flujo automático entre Telemetry y Phenology para el caso de anomalía térmica invernal. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Escenario 4: Confirmar el raleo y actualizar la proyección de acopio cooperativo.}

En este flujo se muestra la colaboración entre los bounded contexts Crop Load Regulation and Thinning Advisory y Cooperative Operations and Territorial Intelligence. El productor confirma la ejecución de un raleo asociado a una prescripción existente, realizado dentro del plazo y en una parcela vinculada a una cooperativa. Tras registrar la ejecución, la política ReprojectCooperativeIntakeOnThinningExecution del contexto Thinning solicita actualizar la proyección mediante el comando existente ProjectCooperativeIntakeVolume. La aplicación comunica la confirmación al productor y la proyección actualizada al gestor técnico (ver \autoref{fig:dmf-4}).

\begin{figure}[H]
\caption{Domain Message Flow 4: Confirmación del raleo y reajuste del acopio cooperativo.} \label{fig:dmf-4}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-4.png}
\caption*{\textit{Nota.} La colaboración se define mediante la política de recálculo de acopio posterior a la confirmación del raleo, conservando los comandos y eventos existentes. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Escenario 5: Cerrar la cosecha, evaluar la estabilización y actualizar el acopio.}

En este flujo se muestra la interacción entre los bounded contexts Harvest Settlement and Performance Reporting y Cooperative Operations and Territorial Intelligence. El productor registra el resultado final de una campaña cosechada, correspondiente a una parcela cooperativa y con histórico suficiente para comparar su evolución. El cierre permite evaluar la curva de estabilización productiva y aporta información a la proyección de acopio. La aplicación comunica al productor el cierre y la evaluación, mientras el gestor técnico recibe la proyección cooperativa actualizada (ver \autoref{fig:dmf-5}).

\begin{figure}[H]
\caption{Domain Message Flow 5: Cierre de cosecha, evaluación de estabilización y actualización del acopio.} \label{fig:dmf-5}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-3.png}
\caption*{\textit{Nota.} La evaluación de estabilización y la actualización cooperativa se derivan del cierre de campaña sin una segunda solicitud del productor. Elaboración propia.}
\end{figure}

\newpage

Adicionalmente, se presentan escenarios que muestran la colaboración de un bounded context con la aplicación móvil o con sistemas externos:

\noindent \textbf{Escenario 6: El productor o gestor solicita y obtiene el dosier agronómico.}

En este flujo se representa el proceso de generación de un dosier dentro del bounded context Harvest Settlement and Performance Reporting. El productor o gestor técnico solicita el documento para una parcela y un conjunto de campañas mediante la aplicación móvil. El contexto procesa la solicitud con la información disponible y comunica la generación del dosier, incluyendo una referencia al documento. El diagrama muestra la solicitud y su resultado; no detalla consultas a otros bounded contexts (ver \autoref{fig:dmf-6}).

\begin{figure}[H]
\caption{Domain Message Flow 6: Solicitud y generación del dosier agronómico.} \label{fig:dmf-6}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-2.png}
\caption*{\textit{Nota.} Interacción entre el productor o gestor técnico, la aplicación móvil y Harvest para solicitar y obtener el dosier. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Escenario 7: Procesar el pago aprobado y activar la suscripción.}

En este flujo se muestra la interacción del sistema externo Payment Gateway Service con el bounded context Subscription and Cooperative Membership. El escenario comienza cuando la pasarela comunica la confirmación de un pago correspondiente a una suscripción identificada. Una vez registrada la aprobación, la política Auto-Activation On Payment Approved activa la suscripción y la aplicación recibe los resultados para mostrar su estado. El diagrama representa el caso exitoso de confirmación y activación, sin incluir el proceso previo de compra (ver \autoref{fig:dmf-7}).

\begin{figure}[H]
\caption{Domain Message Flow 7: Confirmación de pago y activación de la suscripción.} \label{fig:dmf-7}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-1.png}
\caption*{\textit{Nota.} Flujo iniciado por la pasarela de pagos y procesado por Subscription para el caso de pago aprobado. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Escenario 8: El usuario solicita recuperar su contraseña y Viora comunica la solicitud al servicio de correo.}

En este flujo se muestra la interacción entre el usuario, la aplicación móvil, el bounded context Identity and Access Management y el sistema externo Transactional Mail Service. Para el caso de una cuenta existente, el usuario solicita recuperar su contraseña utilizando su correo electrónico. IAM registra la solicitud, comunica al servicio de correo la información necesaria para preparar el mensaje de recuperación y devuelve a la aplicación la confirmación de la solicitud. El escenario no incluye la confirmación de entrega del correo ni el posterior cambio de contraseña (ver \autoref{fig:dmf-8}).

\begin{figure}[H]
\caption{Domain Message Flow 8: Solicitud de recuperación de contraseña y comunicación al servicio de correo.} \label{fig:dmf-8}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/event-storming/dmf/scenery-8.png}
\caption*{\textit{Nota.} El mismo hecho de solicitud de recuperación se comunica al servicio de correo y a la aplicación con el contenido correspondiente a cada destinatario. Elaboración propia.}
\end{figure}

\clearpage 

#### Bounded Context Canvases
&nbsp;

Para mejorar la organización del dominio y facilitar una comunicación consistente, se elaboraron los Bounded Context Canvases para cada subdominio de Viora. Estos canvases delimitan claramente las responsabilidades, establecen el lenguaje ubicuo y los modelos clave, y describen los puntos de integración y los flujos de mensajes entre contextos delimitados, ordenados de acuerdo a su importancia estratégica para el negocio (iniciando por los subdominios Core diferenciadores y concluyendo con los genéricos de plataforma). Los diagramas que siguen consolidan estas decisiones y sirven como guía para alinear la arquitectura, las interfaces y la evolución técnica del sistema (ver \autoref{fig:bcc-legend} a \autoref{fig:bcc-iam}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Leyenda de Notación y Convenciones Visuales.} \label{fig:bcc-legend}
\vspace{0.25cm}
\centering
\includegraphics[width=0.70\textwidth]{report/assets/bounded-context-canvases/00-legend.png}
\caption*{\textit{Nota.} Convención de colores para mensajes (comandos en azul, eventos en naranja, políticas en lila) y colaboradores (nubes para contextos internos, engranajes para sistemas externos). Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Crop Load Regulation and Thinning Advisory (Thinning):} Este bounded context evalúa la representatividad del muestreo a pie de árbol y emite prescripciones de porcentaje de aclareo frutal antes del endurecimiento del carozo. Su propósito es intervenir oportunamente para romper el ciclo biológico de la alternancia productiva (ver \autoref{fig:bcc-thinning}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Crop Load Regulation and Thinning Advisory (Thinning).} \label{fig:bcc-thinning}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/01-thining.png}
\caption*{\textit{Nota.} Canvas de diseño para el Core Domain primario de regulación de carga frutal y aclareo. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Phenology and Historical Bearing Analytics (Phenology):} Este bounded context gestiona la memoria histórica de cosechas, computa el índice $BBI$ de vecería y acumula porciones dinámicas de frío invernal. Su propósito es prever patrones de alternancia (años ON/OFF) y anticipar brotaciones heterogéneas (ver \autoref{fig:bcc-phenology}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Phenology and Historical Bearing Analytics (Phenology).} \label{fig:bcc-phenology}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/02-phenology.png}
\caption*{\textit{Nota.} Canvas de diseño para el Core Domain de inteligencia bioclimática, vecería e índice BBI. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Harvest Settlement and Performance Reporting (Harvest):} Este bounded context asienta la liquidación de kilos cosechados, computa la curva interanual de estabilización y compila el expediente técnico oficial en PDF. Su propósito es certificar el rendimiento anual y validar la mitigación lograda (ver \autoref{fig:bcc-harvest}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Harvest Settlement and Performance Reporting (Harvest).} \label{fig:bcc-harvest}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/03-harvest.png}
\caption*{\textit{Nota.} Canvas de diseño para el Core Domain de cierre de campaña y dossier agronómico certificado. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Olive Orchard and Plot Management (Orchard):} Este bounded context gestiona el catastro georreferenciado de cuarteles mediante polígonos GeoJSON, tipifica variedades de olivo y calcula la densidad arbórea. Su propósito es establecer la base espacial y agronómica del olivar (ver \autoref{fig:bcc-orchard}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Olive Orchard and Plot Management (Orchard).} \label{fig:bcc-orchard}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/04-orchard.png}
\caption*{\textit{Nota.} Canvas de diseño para el subdominio de soporte de catastro predial y dendrometría. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Agroclimatic Telemetry and Sensor Monitoring (Telemetry):} Este bounded context ingesta series horarias de humedad edáfica a 30 y 60 cm y pronósticos a 7 días, emitiendo alertas ante estrés hídrico o choques térmicos. Su propósito es vigilar continuamente las condiciones edafoclimáticas del suelo (ver \autoref{fig:bcc-telemetry}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Agroclimatic Telemetry and Sensor Monitoring (Telemetry).} \label{fig:bcc-telemetry}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/05-agroclimatic.png}
\caption*{\textit{Nota.} Canvas de diseño para el subdominio de soporte de telemetría y sensores edafoclimáticos. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Cooperative Operations and Territorial Intelligence (Territory):} Este bounded context administra el padrón de socios, autoriza qué gestores técnicos pueden solicitar la emisión de lotes de códigos corporativos, centraliza el semáforo de riesgo por valles y proyecta el volumen de acopio colectivo. La custodia del contrato corporativo y el ciclo de vida de los códigos residen en \textit{Subscription and Cooperative Membership}. Su propósito es brindar inteligencia sectorial a la cooperativa (ver \autoref{fig:bcc-territory}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Cooperative Operations and Territorial Intelligence (Territory).} \label{fig:bcc-territory}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/06-cooperative.png}
\caption*{\textit{Nota.} Canvas de diseño para el subdominio de soporte de inteligencia territorial y gremial. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{User Profiles (Profiles):} Este bounded context administra la identidad civil y canales de contacto del productor, normalizando números telefónicos bajo la norma internacional E.164. Su propósito es centralizar la representación de la persona física y facilitar la asistencia técnica (ver \autoref{fig:bcc-profiles}).

\begin{figure}[H]
\caption{Bounded Context Canvas: User Profiles (Profiles).} \label{fig:bcc-profiles}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/07-profiles.png}
\caption*{\textit{Nota.} Canvas de diseño para el subdominio de soporte de perfiles de usuario y contacto E.164. Elaboración propia.}
\end{figure}

\newpage

\noindent \textbf{Subscription and Cooperative Membership (Subscription):} Este bounded context gestiona pagos al Plan Productor y el canje de códigos de patrocinio, fiscalizando la cuota de hectáreas catastrables. Su propósito es asegurar la monetización del servicio SaaS y controlar el acceso predial (ver \autoref{fig:bcc-subscription}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Subscription and Cooperative Membership (Subscription).} \label{fig:bcc-subscription}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/08-subscription.png}
\caption*{\textit{Nota.} Canvas de diseño para el subdominio genérico de suscripción SaaS y membresías. Elaboración propia.}
\end{figure}

\clearpage

\noindent \textbf{Identity and Access Management (IAM):} Este bounded context gestiona la autenticación de usuarios, asignación de roles, emisión de tokens de sesión y recuperación segura de credenciales. Su propósito es garantizar acceso controlado y seguridad perimetral en la plataforma (ver \autoref{fig:bcc-iam}).

\begin{figure}[H]
\caption{Bounded Context Canvas: Identity and Access Management (IAM).} \label{fig:bcc-iam}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/bounded-context-canvases/09-iam.png}
\caption*{\textit{Nota.} Canvas de diseño para el subdominio genérico de seguridad, credenciales y tokens. Elaboración propia.}
\end{figure}

\clearpage 

### Context Mapping
&nbsp;

Una vez delimitadas las fronteras y responsabilidades en los Bounded Context Canvases, el diseño estratégico de Domain-Driven Design exige formalizar el \textit{Context Map} (Mapa de Contextos). Este artefacto modela la topología global del ecosistema Viora, definiendo la naturaleza técnica y organizativa de las relaciones de integración, los flujos de dependencia (\textit{Upstream / Downstream}) y los patrones de frontera aplicados para salvaguardar la integridad de los modelos de dominio.

La arquitectura estratégica consolidada articula los nueve bounded contexts a través de dieciséis relaciones internas (estructuradas en dieciocho flujos dirigidos) y cuatro integraciones perimetrales con sistemas externos. Como criterio rector de diseño, la topología organiza el ecosistema situando los tres subdominios \textit{Core} en el centro de gravedad del valor agronómico, respaldados por los subdominios de \textit{Soporte} como proveedores de datos territoriales y de sensometría, y por los subdominios \textit{Genéricos} en la periferia como infraestructura estándar de seguridad y monetización (ver \autoref{fig:context-map}).

\begin{figure}[H]
\caption{Context Map de Viora: mapa de relaciones estructurales adoptado.} \label{fig:context-map}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/context-map/01-context-map-elegido.png}
\caption*{\textit{Nota.} Topología estratégica compuesta por 9 bounded contexts, 16 relaciones internas y 4 integraciones externas. La dirección de las flechas indica la dependencia desde el contexto proveedor (\textit{Upstream}) hacia el contexto consumidor (\textit{Downstream}). Elaboración propia.}
\end{figure}

\noindent \textbf{Patrones de relación y estrategias de integración:} Para gobernar el acoplamiento y preservar la pureza del lenguaje ubicuo en cada frontera, se seleccionaron los siguientes patrones canónicos de DDD:

\begin{itemize}
    \item \textbf{Partnership (P, colaboración mutua):} Se implementó exclusivamente entre los subdominios \textit{Core}, donde existe una interdependencia biológica y productiva simétrica:
    \begin{itemize}
        \item \textit{Phenology $\longleftrightarrow$ Crop Load Regulation (Thinning):} Ambos contextos cooperan dentro del mismo ciclo fenológico. \textit{Phenology} aporta la acumulación de frío invernal y recalibra el potencial floral ante anomalías térmicas; a su vez, \textit{Thinning} retroalimenta el factor de ejecución de aclareo para ajustar las curvas fisiológicas del árbol. Ninguno subordina al otro y el éxito de la mitigación depende del alineamiento estrecho de sus contratos.
        \item \textit{Phenology $\longleftrightarrow$ Harvest Settlement:} La liquidación final de kilos recolectados en \textit{Harvest} retroalimenta la serie histórica plurianual en \textit{Phenology}; a su vez, \textit{Phenology} provee el cálculo del índice $BBI$ de Hoblyn et al. (1936) necesario para que \textit{Harvest} determine la curva de estabilización interanual y certifique el expediente agronómico.
    \end{itemize}
    
    \item \textbf{Customer / Supplier (C/S, cliente / proveedor):} Rige la mayoría de las interacciones internas. En este esquema, el contexto proveedor (\textit{Upstream}) atiende las necesidades del contexto consumidor (\textit{Downstream}), garantizando que el consumidor preserve su propio lenguaje ubicuo y traduzca las señales recibidas sin acoplarse a los agregados internos del emisor. Destacan las emisiones desde \textit{Olive Orchard and Plot Management} hacia los contextos analíticos y de sensores, de \textit{Agroclimatic Telemetry} hacia \textit{Phenology} y \textit{Cooperative Operations}, y de \textit{Crop Load Regulation} hacia el seguimiento territorial y cierre de campaña. En sentido inverso, \textit{Cooperative Operations and Territorial Intelligence} actúa como proveedor de la autorización institucional: es el único contexto que conoce qué gestor técnico obra en nombre de qué cooperativa, dato que no reside en \textit{IAM} porque su rol de plataforma no distingue organización. Sobre esa potestad, \textit{Subscription} valida quién puede solicitar la emisión de un lote de códigos corporativos y \textit{Olive Orchard and Plot Management} resuelve el alcance del listado predial cooperativo; ambos consumidores traducen el contrato a un puerto propio y angosto sin absorber el vocabulario territorial, de modo que la relación se clasifica como \textit{Customer / Supplier} en los dos casos. Que \textit{Subscription} sea a la vez proveedor de la afiliación asíncrona y consumidor de la autorización síncrona no configura una \textit{Partnership}: se trata de dos capacidades distintas, con ciclos de vida independientes, que no exigen planificación conjunta.
    
    \item \textbf{Conformist (CF, conformista):} Aplica en la integración entre \textit{Identity and Access Management (IAM)} y \textit{User Profiles}. Dado que \textit{IAM} opera como proveedor genérico de seguridad sin estado (\textit{stateless}), \textit{User Profiles} adopta directamente el identificador de usuario (\textit{UserId}) como clave foránea lógica sin necesidad de capas de traducción intermedias, aceptando la convención de identidad establecida por la autoridad de seguridad.
    
    \item \textbf{Open Host Service / Published Language (OHS / PL):} Los contextos de mayor consumo transversal exponen protocolos de integración unificados. \textit{IAM} actúa como OHS emitiendo tokens de acceso JWT bajo un protocolo universal (PL); \textit{Subscription} ofrece un servicio abierto para la validación de cuotas prediales; \textit{Olive Orchard and Plot Management} publica un contrato unificado de parcela y geometría que abastece por igual a la telemetría, a la analítica fenológica y al motor de aclareo, adoptando GeoJSON como Published Language de industria tanto en la proyección de sus polígonos como en el consumo del proveedor cartográfico externo; y \textit{Crop Load Regulation} expone eventos estandarizados de riesgo de sobrecarga para múltiples consumidores.
    
    \item \textbf{Anticorruption Layer (ACL, capa anticorrupción):} Se interpone de manera obligatoria en los puntos de contacto con sistemas externos: la pasarela de pagos (\textit{Mercado Pago Checkout Pro}), el servicio agrometeorológico (\textit{Open-Meteo Weather API}), el proveedor cartográfico (\textit{Mapbox GIS}) y el servicio de correo (\textit{Brevo Transactional Mail}). La ACL aísla al dominio de Viora frente a cambios de esquemas, códigos de respuesta propietarios o estructuras de datos ajenas, traduciendo cada interacción a comandos y eventos nativos de la plataforma.
\end{itemize}

\newpage

\begin{figure}[H]
\caption{Context Map de Viora: alternativas de diseño evaluadas y descartadas.} \label{fig:context-map-alternatives}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/context-map/context-map-alternatives.png}
\caption*{\textit{Nota.} Alternativas A y B, correspondientes al nivel de exploración que reorganiza la agrupación de capacidades entre contextos. Las alternativas C y D operan sobre relaciones individuales y no admiten representación topológica comparada. Elaboración propia.}
\end{figure}

\noindent \textbf{Evaluación y justificación de diseños alternativos:} La exploración de la topología operó en dos niveles. En el primero se contrastaron hipótesis que reorganizaban la estructura completa del mapa, preguntando qué ocurriría si una capacidad compartida se alojara en un núcleo común o si un contexto de soporte renunciara a su propio modelo; ambas se recogen como alternativas A y B (ver \autoref{fig:context-map-alternatives}). En el segundo nivel se sometieron a examen relaciones individuales cuya existencia parecía evidente pero cuyo sustento no resistió la verificación contra el modelo táctico: allí la pregunta no fue cómo agrupar los contextos, sino si una dependencia concreta debía formalizarse, invertirse o retirarse. De ese segundo nivel proceden las alternativas C y D, cuyo descarte quedó asentado en la bitácora de cambios del propio Context Map:

\begin{itemize}
    \item \textbf{Alternativa A (implementación de un Shared Kernel entre Phenology y Harvest Settlement, descartada):} Se evaluó la posibilidad de unificar la memoria histórica de cosechas, el registro de cosechas previas y el cálculo del $BBI$ dentro de un núcleo compartido (\textit{Shared Kernel}) co-administrado por ambos contextos. Esta opción se descartó debido a que el \textit{Shared Kernel} constituye el patrón de mayor acoplamiento en DDD, exigiendo sincronización binaria y dependencia directa en el código y base de datos de ambos módulos ante cualquier cambio o recalibración de los algoritmos de vecería. La solución adoptada asigna la custodia exclusiva de la memoria bioclimática y del índice $BBI$ al contexto de \textit{Phenology}, mientras que \textit{Harvest Settlement} se restringe a la liquidación transaccional de la campaña actual, resolviendo la colaboración mediante un patrón \textit{Partnership} que brinda alta cohesión con mínimo acoplamiento estructural.
    
    \item \textbf{Alternativa B (subordinación conformista de Cooperative Operations hacia sus proveedores, descartada):} Se analizó la alternativa de que \textit{Cooperative Operations and Territorial Intelligence} operara como un consumidor conformista (\textit{Conformist}) frente a sus cuatro contextos proveedores (\textit{Subscription}, \textit{User Profiles}, \textit{Thinning} y \textit{Telemetry}), asumiendo que al ser un subdominio de soporte podría incorporar pasivamente los modelos de dichos contextos. Esta opción se desestimó porque el subdominio territorial posee un lenguaje ubicuo especializado y un modelo conceptual propio y diferenciado (\textit{Cooperative}, \textit{CooperativeMember}, \textit{TerritorialRiskMatrix}, \textit{EarlyIntakeProjection}, \textit{SamplingCoverageRate}, \textit{SectorZone}). Un contexto conformista importa estructuras foráneas sin transformación; en contraste, \textit{Cooperative Operations} actúa como un integrador territorial que traduce eventos heterogéneos provenientes de parcelas individuales hacia métricas agregadas de cuenca o valle, justificando plenamente su relación como cliente soberano bajo el patrón \textit{Customer / Supplier}.

    \item \textbf{Alternativa C (contrato de consulta desde Olive Orchard hacia Crop Load Regulation para la baja de parcela, descartada por inversión):} Se evaluó abrir una relación dirigida desde \textit{Crop Load Regulation} hacia \textit{Olive Orchard and Plot Management}, de modo que el catastro consultara de forma síncrona la elegibilidad de baja antes de retirar una parcela con prescripción de aclareo vigente. El examen mostró que el bloqueo no protegía aquello que aparentaba proteger: con la prescripción ya replicada en el dispositivo móvil, la cuadrilla sale a campo de igual manera, por lo que la consulta resguardaba la confirmación en el servidor y no la labor agronómica. Se constató además que la restricción abarcaba dos agregados alojados en contextos distintos, y las invariantes solo se sostienen dentro de un único límite transaccional. En lugar de formalizar la dependencia se optó por invertirla: el catastro da de baja la parcela sin consultar a nadie y publica el evento correspondiente, y \textit{Crop Load Regulation} reacciona anulando las prescripciones pendientes mediante una política reactiva. La restricción se reclasificó de invariante de agregado a política, y ninguna arista de consulta cruza de vuelta.
    
    \item \textbf{Alternativa D (consumo de la liquidación de campaña por parte de Cooperative Operations, descartada por retiro):} Se analizó formalizar una relación desde \textit{Harvest Settlement and Performance Reporting} hacia \textit{Cooperative Operations and Territorial Intelligence}, bajo el supuesto de que el volumen efectivamente liquidado al cierre de campaña permitiría calibrar la proyección de acopio asociativa. La verificación del extremo consumidor desmintió el supuesto: el agregado territorial no posee atributo alguno de margen de error, su proyección retiene un único valor vigente sin serie histórica contra la cual contrastar, y su indicador de confiabilidad se deriva de la cobertura muestral del padrón y no de la precisión alcanzada por el modelo. Formalizar la arista habría elevado a contrato una dependencia cuyo consumidor no podía hacer nada con el dato recibido, de modo que se retiró el consumo en lugar de incorporarlo. La capacidad queda registrada como identificada y no adoptada: incorporarla constituiría alcance nuevo, con serie histórica de proyecciones, representación del margen y evento propio.
\end{itemize}

Las cuatro evaluaciones responden a las preguntas de exploración que orientan esta etapa. Las alternativas A y B interrogan la agrupación de capacidades, examinando qué ocurre al alojar una capacidad compartida en un núcleo común y al trasladar la traducción de vocabularios fuera del contexto que la ejerce. Las alternativas C y D interrogan la ruptura de dependencias, mostrando que una relación puede eliminarse invirtiendo el sentido del control o retirando un consumo que el modelo receptor no sostiene. Conviene precisar que estas cuatro son las únicas hipótesis que se sometieron a deliberación: las revisiones intermedias del diagrama no constituyen diseños candidatos, sino estados sucesivos de un mismo mapa cuyas omisiones documentales se fueron subsanando conforme la capa táctica precisaba los contratos entre contextos.

La topología definitiva resultante garantiza una clara separación de responsabilidades, optimiza la trazabilidad del flujo de mensajes y asegura que los algoritmos predictivos y de regulación de carga permanezcan completamente protegidos en el núcleo del sistema.

\clearpage 

### Software Architecture

Para representar la arquitectura de software de Viora se aplicó el modelo C4, describiendo el sistema en cuatro niveles de abstracción progresiva: Contexto, Contenedores, Componentes (documentado por separado para cada aplicación cliente y para el backend) y Despliegue. La notación mantiene el mismo código de colores en las cuatro vistas: los actores humanos se representan en verde, los elementos propios de Viora (contenedores y componentes) en azul, y los sistemas de software externos en rojo, unidos mediante relaciones dirigidas que documentan el protocolo y el propósito de cada interacción.

#### Software Architecture Context Level Diagrams
&nbsp;

El diagrama de contexto sitúa a Viora frente a sus tres actores humanos y sus cuatro integraciones externas. El Visitante explora la propuesta de valor y los planes de descarga sin necesidad de autenticarse; el Productor Olivícola registra muestras y cosechas, y revisa el asesoramiento y confirma la ejecución del aclareo; el Gestor Técnico Cooperativo administra la membresía y revisa el riesgo de las parcelas afiliadas junto con la proyección de acopio. Del lado de los sistemas externos, Mapbox provee la visualización y delimitación de parcelas, Open-Meteo aporta el histórico y pronóstico horario de variables agroclimáticas que alimentan el cálculo de acumulación de frío, Mercado Pago procesa los pagos de suscripción individual, Brevo despacha los correos de recuperación de contraseña.

\begin{figure}[H]
\caption{C4 Model - Nivel 1: Diagrama de Contexto de Viora.}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/c4-model/viora-system-context.png}
\caption*{\textit{Nota.} Tres actores humanos (Visitante, Productor Olivícola, Gestor Técnico Cooperativo) y cuatro sistemas externos (Mapbox, Open-Meteo, Mercado Pago, Brevo) interactuando con el sistema Viora. Elaboración propia.}
\end{figure}



\begin{figure}[H]
\caption{C4 Model - Leyenda de notación del Diagrama de Contexto.}
\centering
\includegraphics[width=0.7\textwidth]{report/assets/c4-model/viora-system-context-key.png}
\caption*{\textit{Nota.} Clave de notación: personas en verde, sistema de software en azul y sistemas externos en rojo. Elaboración propia.}
\end{figure}

#### Software Architecture Container Level Diagrams
&nbsp;

Al descomponer a Viora en contenedores, el sistema queda dividido en dos aplicaciones móviles nativas por plataforma tecnológica (Aplicación Android en Kotlin y Aplicación Cross-platform en Flutter/Dart), cada una con su propia base de datos local (Android Local Database sobre Room/SQLite y Cross-platform Local Database sobre sqflite/SQLite) que cachea los datos autorizados de parcela y encola las muestras pendientes de sincronización en cada instalación. Ambas aplicaciones exponen exactamente las mismas funcionalidades orientadas a rol, captura de campo, asesoría de aclareo, seguimiento de cosecha y vistas cooperativas, y soportan muestreo sin conexión; los actores Productor Olivícola y Gestor Técnico Cooperativo del nivel de contexto interactúan indistintamente con cualquiera de las dos, mientras que el Visitante se limita a una Landing Page (HTML5/CSS3/JavaScript) que presenta la propuesta de valor, los planes y los enlaces de descarga. El Backend API (Java, Spring Boot; REST/OpenAPI) concentra la lógica de dominio modular, la autenticación, la sincronización idempotente, los cálculos de frío y aclareo, los reportes de cosecha y las proyecciones de acopio, persistiendo en una única Viora Database (PostgreSQL) compartida por ambos clientes. Un Telemetry Simulator (Java, aplicación programada) genera lecturas sintéticas etiquetadas de clima y suelo para los nodos virtuales de parcela registrados, sustituyendo al hardware IoT real fuera del alcance del curso. Las cuatro integraciones externas del nivel de contexto se mantienen en este nivel, cada una consumida por el contenedor que efectivamente la requiere: Mapbox y Mercado Pago desde ambas aplicaciones móviles, y Open-Meteo, Brevo y Mercado Pago desde el Backend API.

\begin{figure}[H]
\caption{C4 Model - Nivel 2: Diagrama de Contenedores de Viora.}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/c4-model/viora-container.png}
\caption*{\textit{Nota.} Ocho contenedores propios (Landing Page, Aplicación Android, Aplicación Cross-platform, Telemetry Simulator, Backend API y tres almacenes de datos) y cuatro sistemas externos. Elaboración propia.}
\end{figure}



\begin{figure}[H]
\caption{C4 Model - Leyenda de notación del Diagrama de Contenedores.}
\centering
\includegraphics[width=0.7\textwidth]{report/assets/c4-model/viora-container-key.png}
\caption*{\textit{Nota.} Clave de notación de contenedores, bases de datos y sistemas externos. Elaboración propia.}
\end{figure}

#### Software Architecture Component Level Diagrams
&nbsp;

Dado que Viora despliega dos aplicaciones cliente sobre pilas tecnológicas distintas y un backend compartido, el nivel de componentes se documentó por separado para cada uno de los tres contenedores con lógica propia: la Aplicación Android, la Aplicación Cross-platform y el Backend API.

##### Android Component Diagram
&nbsp;

La Aplicación Android organiza su lógica interna en quince componentes bajo el patrón MVVM con Jetpack Compose. El App Navigation selecciona los destinos según sesión y rol, gestiona los enlaces validados de la aplicación y el back stack; seis componentes de interfaz (Account and Profile UI, Plot Management UI, Field Sampling UI, Agronomy and Harvest UI, Cooperative Operations UI y Subscription UI) exponen el estado de pantalla mediante ViewModel y delegan la persistencia en Feature Repositories, que centraliza los contratos de datos de cuenta, parcela, agronomía y cooperativa, y refresca las cachés autorizadas. Local Data Access encapsula los DAOs de Room, las transacciones y la caché con alcance de cuenta sobre la Android Local Database; Sampling Repository conserva localmente las muestras con IDs de operación estables y confirmaciones del servidor por registro; y Sampling Sync programa el trabajo único en segundo plano, drena los lotes pendientes y reintenta las fallas transitorias al reconectar. Backend API Client, sobre Retrofit/OkHttp, encapsula las llamadas REST, DTOs, transferencia de archivos y el refresco tipado de tokens hacia el Backend API; Session Manager gestiona el estado de sesión y persiste las credenciales protegidas mediante cifrado respaldado por Android Keystore. Plot Map Adapter encapsula el Mapbox Maps SDK para el renderizado y edición de polígonos de parcela, devolviendo GeoJSON a la pantalla de parcelas, y Hosted Checkout Coordinator obtiene la URL de checkout creada por el backend, la abre mediante Custom Tabs y revalida el estado de pago al retornar.

\begin{figure}[H]
\caption{C4 Model - Nivel 3: Diagrama de Componentes de la Aplicación Android.}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/c4-model/viora-android-components.png}
\caption*{\textit{Nota.} Quince componentes internos de la Aplicación Android (Kotlin, Jetpack Compose) y sus dependencias hacia Android Local Database, Backend API, Mapbox y Mercado Pago. Elaboración propia.}
\end{figure}



\begin{figure}[H]
\caption{C4 Model - Leyenda de notación del Diagrama de Componentes de Android.}
\centering
\includegraphics[width=0.7\textwidth]{report/assets/c4-model/viora-android-components-key.png}
\caption*{\textit{Nota.} Clave de notación de componentes internos, contenedores relacionados y sistemas externos. Elaboración propia.}
\end{figure}

##### Backend Component Diagram
&nbsp;

El Backend API se descompone en doce componentes de dominio construidos sobre Spring; cada uno materializa, en el nivel táctico, uno de los bounded contexts delimitados previamente en el mapa de contexto estratégico. Mobile REST API concentra los controladores Spring MVC que validan las solicitudes entrantes y despachan comandos, consultas y lotes offline hacia las interfaces del resto de módulos. Identity and Access emite y valida los JWT, aplica los roles y gestiona la recuperación de contraseña mediante un adaptador de correo hacia Brevo. Orchard and Plot Management valida la geometría GeoJSON, la variedad, la densidad arbórea y la titularidad, autorizando el acceso a la parcela. Agroclimatic Telemetry ingiere las lecturas sintéticas programadas del Telemetry Simulator, importa el histórico horario de clima desde Open-Meteo y registra la fuente y calidad del dato. Phenology and Bearing Analytics es propietario del historial de cosechas y del Biennial Bearing Index, calculando la acumulación de frío invernal y actualizando el potencial floral y el estado de la ventana biológica. Sustainable Load Calculator realiza el cálculo puro de la carga admisible y el objetivo de remoción a partir de los insumos agronómicos validados, y Thinning Advisory coordina la evaluación de carga, emite las prescripciones de aclareo y audita su ejecución contra la ventana biológica. Field Sampling and Sync valida los lotes de campo, deduplica los reintentos y evalúa la representatividad del muestreo. Cooperative Operations administra los miembros, consolida el riesgo de parcela y proyecta el acopio con indicadores de cobertura de muestreo. Subscription and Membership gestiona los planes, las cuotas por hectárea y los códigos cooperativos, verificando y deduplicando los webhooks de pago de Mercado Pago. Harvest Settlement and Reporting cierra los pesos de campaña y compila los expedientes agronómicos reproducibles a partir del BBI que expone Phenology. Profile Management mantiene los datos personales, el país y los canales de contacto del usuario. Todos los componentes leen y escriben sus propios registros en la Viora Database mediante JPA/JDBC sobre TLS, y se comunican entre sí principalmente mediante eventos internos y llamadas Java en proceso.

\begin{figure}[H]
\caption{C4 Model - Nivel 3: Diagrama de Componentes del Backend API.}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/c4-model/viora-backend-components.png}
\caption*{\textit{Nota.} Doce componentes de dominio del Backend API (Spring, Java) y sus dependencias hacia Viora Database, Telemetry Simulator, las dos aplicaciones cliente y los sistemas externos. Elaboración propia.}
\end{figure}



\begin{figure}[H]
\caption{C4 Model - Leyenda de notación del Diagrama de Componentes del Backend.}
\centering
\includegraphics[width=0.7\textwidth]{report/assets/c4-model/viora-backend-components-key.png}
\caption*{\textit{Nota.} Clave de notación de componentes de dominio, contenedores relacionados y sistemas externos. Elaboración propia.}
\end{figure}

##### Cross-Platform Component Diagram
&nbsp;

La Aplicación Cross-platform replica la misma organización interna que la Aplicación Android, los mismos quince componentes con idéntica responsabilidad, materializada sobre el stack Flutter/Dart. App Navigation, basado en go\_router, selecciona los destinos por sesión y rol y dispara deep links; los seis componentes de interfaz (Account and Profile UI, Plot Management UI, Field Sampling UI, Agronomy and Harvest UI, Cooperative Operations UI y Subscription UI) se implementan como widgets Flutter con ChangeNotifier ViewModel y delegan en Feature Repositories. Local Data Access encapsula las transacciones sqflite sobre la Cross-platform Local Database con invalidación explícita de caché; Sampling Repository conserva las muestras con IDs de operación estables, y Sampling Sync coordina la sincronización en primer y segundo plano mediante workmanager y el ciclo de vida de la app, solicitando el reintento tras cada confirmación o reintento del usuario. Backend API Client, sobre Dio, encapsula las llamadas REST, DTOs y transferencias de archivo hacia el Backend API, mientras que Session Manager gestiona el estado de sesión y persiste los tokens protegidos mediante flutter\_secure\_storage. Plot Map Adapter encapsula mapbox\_maps\_flutter para el renderizado y edición de polígonos, devolviendo GeoJSON a la pantalla de parcelas, y Hosted Checkout Coordinator obtiene la URL de checkout alojado por el backend, la abre mediante url\_launcher y revalida el estado de pago al retorno. La equivalencia funcional exacta entre ambos árboles de componentes evidencia que las dos aplicaciones cliente ofrecen paridad de capacidades orientadas a rol, difiriendo únicamente en la tecnología de implementación.

\begin{figure}[H]
\caption{C4 Model - Nivel 3: Diagrama de Componentes de la Aplicación Cross-platform.}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/c4-model/viora-cross-platform-components.png}
\caption*{\textit{Nota.} Quince componentes internos de la Aplicación Cross-platform (Flutter, Dart) y sus dependencias hacia Cross-platform Local Database, Backend API, Mapbox y Mercado Pago. Elaboración propia.}
\end{figure}



\begin{figure}[H]
\caption{C4 Model - Leyenda de notación del Diagrama de Componentes Cross-platform.}
\centering
\includegraphics[width=0.7\textwidth]{report/assets/c4-model/viora-cross-platform-components-key.png}
\caption*{\textit{Nota.} Clave de notación de componentes internos, contenedores relacionados y sistemas externos. Elaboración propia.}
\end{figure}

#### Software Architecture Deployment Diagrams
&nbsp;


El diagrama de despliegue mapea los ocho contenedores lógicos sobre la infraestructura física de prueba. El dispositivo del Visitante ejecuta un navegador web compatible con HTTPS que solicita y renderiza los recursos de la Landing Page, alojada como sitio estático en Vercel. Firebase App Distribution entrega los APK firmados de Kotlin y de Flutter para su instalación y actualización, manteniendo registros de aplicación separados por plataforma: un dispositivo de prueba Android ejecuta la Aplicación Android dentro de su propio sandbox nativo junto con la Android Local Database, y un segundo dispositivo de prueba Android ejecuta la Aplicación Cross-platform sobre el motor Flutter junto con la Cross-platform Local Database. El Backend API se despliega en Render como servicio web Dockerizado sobre runtime Java/Spring Boot, junto con el Telemetry Simulator como cron job independiente en la misma plataforma. La persistencia recae en un servicio PostgreSQL alojado en Filess.io. Las cuatro integraciones externas se despliegan fuera de la infraestructura propia: Mapbox, Brevo y Open-Meteo como nubes SaaS, y Mercado Pago en su modo de pruebas ("test mode"), notificando los cambios de estado de pago al Backend API mediante un webhook firmado.

\begin{figure}[H]
\caption{C4 Model - Nivel 4: Diagrama de Despliegue de Viora.}
\vspace{0.25cm}
\centering
\includegraphics[width=0.95\textwidth]{report/assets/c4-model/viora-deployment.png}
\caption*{\textit{Nota.} Distribución del sistema sobre Vercel, Firebase App Distribution, dos dispositivos de prueba Android, Render y Filess.io, junto con los nodos SaaS externos y de pago. Elaboración propia.}
\end{figure}



\begin{figure}[H]
\caption{C4 Model - Leyenda de notación del Diagrama de Despliegue.}
\centering
\includegraphics[width=0.7\textwidth]{report/assets/c4-model/viora-deployment-key.png}
\caption*{\textit{Nota.} Clave de notación de nodos de despliegue, contenedores desplegados y sistemas externos. Elaboración propia.}
\end{figure}

\newpage
