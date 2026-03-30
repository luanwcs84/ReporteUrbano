# REPORTE URBANO
## Relatório Técnico – Atividade Parcial

*UNIVERSIDADE DE FORTALEZA – UNIFOR*  
Educação a Distância – EAD  
N700 – Desenvolvimento para Plataformas Móveis

---

*Grupo 16*

| Integrante | GitHub |
|---|---|
| Joao Lino | [@linocreates](https://github.com/linocreates) |
| Luan Wesley | [@luanwcs84](https://github.com/luanwcs84) |
| Anderson Guimarães | [@guimaraesander](https://github.com/guimaraesander) |
| Atila Gois | [@atilagois](https://github.com/atilagois) |
| Larissa Fernandes | [@larissa-fernandess](https://github.com/larissa-fernandess) |
| Anderson Ferreira | [@pherreiras](https://github.com/pherreiras) |

*Fortaleza – CE, 2026*

---

## 1. Identificação do Problema

A infraestrutura urbana das cidades brasileiras enfrenta um desafio sistêmico: a ausência de canais ágeis e acessíveis para que cidadãos reportem problemas cotidianos, como buracos em vias, calçadas danificadas, falta de iluminação pública e outros incidentes que comprometem a qualidade de vida e a segurança da população.

A comunicação entre o cidadão e os órgãos responsáveis pela manutenção urbana é frequentemente lenta e burocrática, o que resulta em atrasos na resolução de ocorrências e na perpetuação de condições precárias. A falta de um registro visual georreferenciado dificulta a priorização e o acompanhamento das demandas por parte dos gestores públicos.

Diante desse contexto, identificou-se a oportunidade de desenvolver uma solução mobile que democratize o processo de reporte urbano, permitindo que qualquer cidadão com um smartphone Android registre problemas de infraestrutura com foto, localização precisa e descrição detalhada, contribuindo para cidades mais inteligentes e responsivas.

---

## 2. Solução Proposta

O Reporte Urbano é um aplicativo Android nativo que permite o registro colaborativo de problemas de infraestrutura urbana. Por meio da aplicação, o cidadão pode capturar uma fotografia do problema, obter automaticamente as coordenadas geográficas via GPS, informar o endereço ou ponto de referência e submeter o relato a uma plataforma centralizada na nuvem.

A solução adota uma arquitetura cliente-servidor, com frontend nativo Android integrado ao Supabase como Backend-as-a-Service (BaaS). Essa escolha arquitetural permite o gerenciamento centralizado de autenticação, banco de dados relacional, armazenamento de mídia e controle de acesso, reduzindo a complexidade de infraestrutura e concentrando o esforço de engenharia na experiência do usuário.

O sistema contempla dois perfis de usuário: o cidadão comum, que registra e visualiza seus próprios reportes em um mapa interativo, e o administrador, que possui acesso irrestrito a todos os registros e dispõe de uma interface diferenciada para gerenciamento global das ocorrências.

---

## 3. Levantamento de Requisitos

### 3.1 Requisitos Funcionais

| *RF* | *Descrição* |
|---|---|
| RF01 | O sistema deve permitir o cadastro de novos usuários com nome, e-mail e senha. |
| RF02 | O sistema deve autenticar usuários cadastrados via e-mail e senha. |
| RF03 | O sistema deve permitir o registro de um reporte com título, descrição, fotografia e localização. |
| RF04 | O sistema deve capturar automaticamente as coordenadas GPS do dispositivo. |
| RF05 | O sistema deve exibir os reportes registrados em um mapa interativo (OpenStreetMap). |
| RF06 | O sistema deve permitir que o usuário visualize a lista de seus próprios reportes. |
| RF07 | O sistema deve permitir a exclusão de um reporte pelo seu autor ou pelo administrador. |
| RF08 | O administrador deve ter acesso à listagem completa de todos os reportes de todos os usuários. |
| RF09 | O sistema deve realizar o upload da foto para armazenamento em nuvem (Supabase Storage). |
| RF10 | O sistema deve manter a sessão do usuário ativa entre acessos ao aplicativo. |

### 3.2 Requisitos Não Funcionais

| *RNF* | *Descrição* |
|---|---|
| RNF01 | A interface deve seguir as diretrizes do Material Design 3. |
| RNF02 | As operações de rede devem ser executadas em threads secundárias para não bloquear a interface. |
| RNF03 | O acesso aos dados deve ser protegido por tokens JWT e Row Level Security (RLS) no Supabase. |
| RNF04 | O aplicativo deve ser compatível com Android API 26 ou superior (Android 8.0+). |
| RNF05 | As imagens devem ser armazenadas em bucket de objetos, e não diretamente no banco de dados. |
| RNF06 | O sistema deve solicitar apenas as permissões estritamente necessárias ao funcionamento. |

---

## 4. Arquitetura do Sistema

### 4.1 Visão Geral e Pilha Tecnológica

O aplicativo foi estruturado no pacote com.example.reporteurbano, seguindo princípios de modularização que desacoplam a camada de persistência da interface de usuário. A pilha tecnológica adotada é descrita a seguir:

| *Componente* | *Tecnologia / Biblioteca* |
|---|---|
| Linguagem | Java (Android nativo) |
| Interface | Material Design 3 (Theme.Material3.DayNight.NoActionBar) |
| Comunicação HTTP | OkHttp3 – requisições REST assíncronas |
| Mapas | OSMDroid com provedor TileSourceFactory.MAPNIK (OpenStreetMap) |
| Geolocalização | FusedLocationProviderClient (Google Play Services) |
| Carregamento de Imagens | Glide |
| Persistência Local | SQLite via DatabaseHelper |
| Backend / Auth / Storage | Supabase (BaaS) |
| Zoom de Imagem | PhotoView (chrisbanes) |

### 4.2 Estrutura de Activities

A navegação do aplicativo é organizada por Activities especializadas, cada uma responsável por um contexto funcional distinto:

| *Activity* | *Responsabilidade* |
|---|---|
| MainActivity | Tela de login. Ponto de entrada da aplicação, com validação de sessão ativa. |
| CadastroActivity | Formulário de criação de conta com validação de campos em tempo real. |
| HomeActivity | Tela principal. Exibe o mapa com marcadores dos reportes registrados e o FAB de novo reporte. |
| NovoReporteActivity | Formulário de criação de reporte. Integra câmera, GPS e geocodificação. |
| MeusReportesActivity | Lista de reportes do usuário (ou todos os reportes, no perfil administrador). |

### 4.3 Camada de Serviços e Persistência

A lógica de comunicação com o backend é encapsulada em classes de serviço independentes das Activities, garantindo separação de responsabilidades e facilitando a manutenção evolutiva:

- *SupabaseAuthService:* Gerencia os fluxos de signUp e signIn via API REST do Supabase, incluindo a chamada RPC email_exists_for_login para diferenciação precisa de erros de autenticação.
- *SupabaseReporteService:* Responsável pelas operações CRUD de reportes (GET, POST, DELETE), com filtragem por user_id para usuários comuns e acesso irrestrito para administradores.
- *SupabaseStorageService:* Gerencia o upload e a exclusão de imagens no bucket reportes-fotos do Supabase Storage.
- *SupabaseProfileService:* Busca e cria perfis de usuário na tabela profiles, incluindo a recuperação do campo role para o controle de acesso baseado em perfis (RBAC).
- *SessionManager:* Provedor centralizado de estado de autenticação, utilizando SharedPreferences em modo MODE_PRIVATE para persistência segura dos tokens JWT, UUID do usuário, e-mail, nome e perfil de acesso.
- *DatabaseHelper:* Camada de persistência local via SQLite, utilizada para armazenamento e consulta de reportes em cenários de referência offline.

### 4.4 Padrões de Projeto Adotados

A arquitetura do sistema faz uso consistente de padrões de projeto consolidados:

- *Observer/Callback Pattern:* A interface genérica SupabaseCallback<T>, com métodos onSuccess(T result) e onError(String errorMessage), padroniza o tratamento assíncrono de respostas de rede em todas as operações de serviço.
- *Service Abstraction:* As classes de serviço encapsulam toda a lógica de comunicação HTTP, isolando-a das Activities e permitindo substituição ou extensão sem impacto na camada de interface.
- *Role-Based Access Control (RBAC):* O campo user_role, armazenado na sessão via SessionManager, determina dinamicamente o escopo de dados e a identidade visual exibida ao usuário.
- *Polling Manual via Ciclo de Vida:* Os métodos onResume() das Activities HomeActivity e MeusReportesActivity disparam a atualização de dados sempre que a tela recupera o foco, garantindo a exibição de informações atualizadas sem necessidade de WebSockets.

---

## 5. Uso de Sensores Nativos do Dispositivo

### 5.1 Sensor de Localização (GPS)

O aplicativo utiliza o FusedLocationProviderClient, configurado com PRIORITY_HIGH_ACCURACY, para obter as coordenadas geográficas precisas do dispositivo no momento do registro. As permissões ACCESS_FINE_LOCATION e ACCESS_COARSE_LOCATION são declaradas no AndroidManifest.xml e solicitadas em tempo de execução na NovoReporteActivity.

O sistema implementa geocodificação bidirecional por meio da classe Geocoder: a geocodificação reversa converte as coordenadas GPS em endereço legível para preenchimento automático do campo de localização, enquanto a geocodificação direta converte o endereço digitado manualmente pelo usuário em coordenadas para persistência no banco de dados.

### 5.2 Câmera

A captura de imagens é realizada por meio de uma Intent com a ação MediaStore.ACTION_IMAGE_CAPTURE, utilizando a API moderna ActivityResultLauncher com o contrato ActivityResultContracts.StartActivityForResult. Esse padrão substitui o método depreciado onActivityResult, oferecendo um tratamento de retorno mais seguro e tipado.

Após a captura, o Bitmap é armazenado em memória e exibido ao usuário. Antes do upload, o bitmap é convertido em arquivo JPG temporário no diretório de cache privado do aplicativo (getCacheDir()), seguindo as boas práticas de manipulação de mídia no Android. A biblioteca Glide é utilizada para o carregamento eficiente das imagens armazenadas na nuvem na listagem de reportes.

---

## 6. Design de Interface e Usabilidade

### 6.1 Identidade Visual e Material Design

A interface do Reporte Urbano segue rigorosamente as diretrizes do Material Design 3, com aplicação do tema Theme.Material3.DayNight.NoActionBar. A paleta de cores adota tons de violeta e roxo como cores primárias, com suporte a modo claro e escuro definidos nos arquivos themes.xml e themes.xml (night).

| *Token de Cor* | *Valor Hex* | *Aplicação* |
|---|---|---|
| violet_brand | #8155BA | Cor primária – modo claro |
| purple_haze | #BEAFC2 | Cor primária – modo escuro |
| blue_highlight | #2196F3 | Botão de envio e FAB |
| ebony_dark | #211C2D | Background – modo escuro |
| Admin Primary | #0F766E | Toolbar e header – perfil administrador |
| Admin Accent | #F59E0B | FAB e avatar – perfil administrador |

### 6.2 Componentes de Interface Utilizados

- *MaterialToolbar* com NavigationIcon hamburger e avatar do usuário no canto direito.
- *DrawerLayout* com NavigationView para menu lateral, exibindo nome, e-mail e nível de acesso do usuário.
- *FloatingActionButton (FAB)* para acesso rápido ao formulário de novo reporte.
- *TextInputLayout* com estilo OutlinedBox e validação de erros inline para todos os formulários.
- *RecyclerView* com CardView para listagem de reportes, suportando carregamento de imagens via Glide.
- *AlertDialog* para confirmações de exclusão e exibição do diálogo 'Sobre nós'.
- *MapView (OSMDroid)* com suporte a multitoque e marcadores interativos na tela principal.

### 6.3 Diferenciação Visual por Perfil de Acesso

O sistema implementa uma identidade visual dinâmica baseada no perfil do usuário autenticado. Quando o método sessionManager.isAdmin() retorna verdadeiro, a interface assume a cor administrativa (#0F766E) na Toolbar, no cabeçalho do drawer e na StatusBar, e o subtítulo 'Modo administrador' é exibido. O item de menu 'Meus Reportes' é substituído por 'Todos os Reportes', e o escopo de consulta ao Supabase é expandido para incluir todos os registros, independentemente do user_id.

---

## 7. Fluxo de Dados e Persistência

### 7.1 Ciclo de Vida de um Reporte

O percurso de um reporte desde a captura até a exibição no mapa compreende as seguintes etapas:

1. *Captura:* O usuário fotografa o problema (câmera) e obtém a localização (GPS). O Bitmap fica em memória e as coordenadas são exibidas no campo de endereço.
2. *Preparação:* O Bitmap é comprimido em arquivo JPG temporário no cache privado do dispositivo.
3. *Upload de Mídia:* O SupabaseStorageService envia o arquivo ao bucket reportes-fotos via requisição multipart, retornando a URL pública do objeto.
4. *Persistência Relacional:* O SupabaseReporteService envia um payload JSON com título, descrição, endereço, latitude, longitude, user_id e foto_url para a tabela reportes do Supabase.
5. *Renderização:* Na HomeActivity, o método atualizarMapaComReportes() consulta a API, instancia objetos Marker com GeoPoint e os adiciona ao MapView do OSMDroid.

### 7.2 Segurança e Controle de Acesso

Todas as requisições à API do Supabase incluem o token JWT no cabeçalho Authorization: Bearer, obtido via SessionManager. O controle de acesso no servidor é reforçado por Row Level Security (RLS), garantindo que usuários comuns acessem exclusivamente seus próprios registros. A chave anon do Supabase é centralizada na classe SupabaseConfig e utilizada como identificador público da aplicação nas requisições HTTP.

A exclusão de um reporte segue o padrão de limpeza em cascata: o registro é removido do banco de dados e, somente após a confirmação de sucesso, a imagem correspondente é excluída do bucket de armazenamento, prevenindo a existência de arquivos órfãos e garantindo a consistência entre as camadas de dados.

---

## 8. Testes e Validação

Os testes de funcionamento do aplicativo foram realizados no Android Virtual Device (AVD) Google Pixel 6 Pro, configurado com as seguintes especificações:

| *Propriedade* | *Valor* |
|---|---|
| Device | Google Pixel 6 Pro |
| API Level | Android 36 (API 36) |
| Resolução (px) | 1440 × 3120 |
| Resolução (dp) | 412 × 892 |
| Density | 560 dpi |
| RAM | 2048 MB |
| Sistema | system-images/android-36/google_apis/x86_64 |

Os fluxos validados incluem: criação e autenticação de contas, registro completo de reporte com captura de foto e localização GPS, visualização de reportes no mapa com exibição de marcadores e snippets informativos, listagem e exclusão de reportes, e alternância entre os modos de usuário comum e administrador.

---

## 9. Links do Protótipo

- *Dark mode:* https://www.figma.com/proto/vRAGldS4pkB6zNrdxCiRFR/telas-reportes-urbanos?node-id=0-3&t=o6RHyQORcveEqmKY-1
- *Light mode:* https://www.figma.com/proto/vRAGldS4pkB6zNrdxCiRFR/telas-reportes-urbanos?node-id=32-22&t=o6RHyQORcveEqmKY-1

---

## 10. Versionamento e Organização do Projeto

O código-fonte do projeto foi versionado utilizando Git, com repositório hospedado no GitHub. O projeto segue a estrutura padrão de um módulo Android com Gradle Kotlin DSL (build.gradle.kts), organizando as dependências no catálogo libs.versions.toml. As principais dependências externas declaradas incluem:


androidx.appcompat:appcompat:1.7.1
com.google.android.material:material:1.13.0
com.google.android.gms:play-services-location:21.0.1
org.osmdroid:osmdroid-android:6.1.18
com.squareup.okhttp3:okhttp:4.12.0
com.github.bumptech.glide:glide:4.16.0
com.github.chrisbanes:PhotoView:2.3.0


---

## 11. Considerações Finais

O Reporte Urbano demonstra, na prática, a aplicação integrada dos conceitos fundamentais do desenvolvimento mobile Android: ciclo de vida de Activities, uso de sensores nativos (GPS e câmera), persistência local e em nuvem, comunicação assíncrona via REST, controle de acesso baseado em perfis e conformidade com as diretrizes do Material Design 3.

A arquitetura adotada, baseada na separação de responsabilidades entre Activities e classes de serviço, aliada ao uso do Supabase como BaaS, resultou em uma solução escalável, segura e de relevância social comprovada. O sistema está apto a ser estendido com funcionalidades como notificações push, acompanhamento de status de resolução de ocorrências e integração com sistemas de gestão municipal.

O projeto foi desenvolvido pelo Grupo 16 no primeiro semestre de 2026, no âmbito da disciplina N700 – Desenvolvimento para Plataformas Móveis, EAD Unifor.