# 📍 Reporte Urbano

> Aplicativo Android para registro e visualização de problemas urbanos pelos cidadãos.

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-orange?logo=java)](https://www.java.com/)
[![Supabase](https://img.shields.io/badge/Backend-Supabase-3ECF8E?logo=supabase)](https://supabase.com/)

---

## Sumário

1. [Objetivo](#1-objetivo)
2. [Stack](#2-stack)
3. [Estrutura do Projeto](#3-estrutura-do-projeto)
4. [Funcionalidades](#4-funcionalidades)
5. [Pré-requisitos](#5-pré-requisitos)
6. [Configuração do Supabase](#6-configuração-do-supabase)
7. [Instalação](#7-instalação)
8. [Execução Local](#8-execução-local)
9. [Usuários de Teste](#9-usuários-de-teste)
10. [Testes e Qualidade](#10-testes-e-qualidade)
11. [Deploy / Geração do APK](#11-deploy--geração-do-apk)
12. [Documentação e Evidências](#12-documentação-e-evidências)
13. [Colaboração](#13-colaboração)
14. [Copyright](#14-copyright)

---

## 1. Objetivo

O **Reporte Urbano** é um trabalho acadêmico desenvolvido para a disciplina **Desenvolvimento para Plataformas Móveis (N700)**. O projeto tem como foco o desenvolvimento de uma aplicação nativa Android que permite a cidadãos registrar problemas urbanos — como buracos, iluminação defeituosa, descarte irregular de lixo e outros — com foto capturada na hora e localização GPS precisa.

Os objetivos técnicos centrais do projeto são:

- Praticar desenvolvimento nativo Android com **Java** e **Android SDK**
- Utilizar **sensores reais do dispositivo**: câmera e GPS
- Aplicar os princípios de **Material Design** na interface do usuário
- Integrar um backend completo via **Supabase** (autenticação, banco de dados e armazenamento de arquivos)
- Exibir os reportes em um **mapa interativo** com pinos georreferenciados

---

## 2. Stack

### Front-end (Mobile Nativo)

| Tecnologia | Função |
|---|---|
| Java | Linguagem principal de desenvolvimento |
| Android SDK | APIs nativas de câmera, GPS, ciclo de vida |
| XML (Layouts) | Definição de interfaces de usuário |
| Material Design 3 | Componentes visuais e diretrizes de UX |
| osmdroid | Biblioteca de mapas interativos (OpenStreetMap) |

### Back-end as a Service (BaaS)

| Serviço | Função |
|---|---|
| Supabase Auth | Cadastro, login e gerenciamento de sessões |
| Supabase Database (PostgreSQL) | Armazenamento de reportes e perfis de usuários |
| Supabase Storage | Upload e acesso às fotos dos reportes |
| Supabase Row Level Security (RLS) | Políticas de acesso por perfil (`user` / `admin`) |

---

## 3. Estrutura do Projeto

```
ReporteUrbano/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/reporteurbano/
│   │       │   ├── SupabaseConfig.java           # Credenciais do Supabase (não versionar)
│   │       │   ├── MainActivity.java              # Tela de login (entry point do app)
│   │       │   ├── CadastroActivity.java          # Tela de criação de conta
│   │       │   ├── HomeActivity.java              # Mapa interativo e pinos (usuário logado)
│   │       │   ├── NovoReporteActivity.java       # Registro de novo reporte (foto + GPS)
│   │       │   ├── MeusReportesActivity.java      # Lista dos reportes do usuário
│   │       │   ├── AuthUser.java                  # Modelo de usuário autenticado
│   │       │   ├── Reporte.java                   # Modelo de dados do reporte
│   │       │   ├── ReporteAdapter.java            # Adapter RecyclerView de reportes
│   │       │   ├── SessionManager.java            # Gerenciamento de sessão local
│   │       │   ├── SupabaseAuthService.java       # Serviço de autenticação
│   │       │   ├── SupabaseReporteService.java    # Serviço de CRUD de reportes
│   │       │   ├── SupabaseStorageService.java    # Serviço de upload de fotos
│   │       │   ├── SupabaseProfileService.java    # Serviço de perfis de usuários
│   │       │   ├── SupabaseCallback.java          # Interface de callbacks assíncronos
│   │       │   ├── DatabaseHelper.java            # Auxiliar de banco de dados local
│   │       │   └── LoadingUtils.java              # Utilitário de diálogos de carregamento
│   │       ├── res/
│   │       │   ├── layout/                    # Arquivos XML de interface
│   │       │   ├── drawable/                  # Ícones e recursos visuais
│   │       │   └── values/                    # Cores, strings, temas
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── gradle.properties
└── README.md
```

> **⚠️ Atenção:** O arquivo `SupabaseConfig.java` contém credenciais sensíveis e **não deve ser versionado**. Adicione-o ao `.gitignore`.

---

## 4. Funcionalidades

### Usuário Comum

- **Cadastro e Login** — criação de conta com e-mail e senha via Supabase Auth
- **Captura de Foto** — integração com a câmera nativa do dispositivo no momento do registro
- **Captura de Localização** — utilização da localização atual do dispositivo para auxiliar no preenchimento do endereço
- **Criar Reporte** — envio de título, descrição, endereço, foto e localização para o banco de dados
- **Visualizar no Mapa** — exibição dos próprios reportes como pinos georreferenciados
- **Excluir Reporte** — remoção dos próprios reportes da plataforma

### Painel do Administrador

- **Modo Administrador** — acesso ao perfil com papel (`role`) diferenciado no banco de dados
- **Todos os Reportes** — visualização de todos os reportes cadastrados
- **Mapa Global** — exibição de todos os pinos registrados na cidade

---

## 5. Pré-requisitos

Antes de configurar o projeto, certifique-se de ter instalado:

- **Android Studio** Hedgehog (2025.3.2) ou superior
- **JDK 11** ou superior
- **Android SDK** com API Level mínimo 26 (Android 8.0) e target API 36
- **Conta no Supabase** — [supabase.com](https://supabase.com)
- **Dispositivo físico ou emulador** com suporte a câmera e GPS (recomendado: dispositivo real para testar sensores)

---

## 6. Configuração do Supabase

### 6.1 Estrutura no Banco de Dados

O projeto utiliza os seguintes recursos no Supabase:

- Tabela `profiles`
- Tabela `reportes`
- Bucket `reportes-fotos` no Supabase Storage
- Policies de Row Level Security (RLS) para usuário comum e administrador

**Campos da tabela `profiles`:**

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Referência ao `auth.users` |
| `email` | TEXT | E-mail do usuário |
| `nome` | TEXT | Nome de exibição |
| `role` | TEXT | Papel: `user` ou `admin` |

**Campos da tabela `reportes`:**

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador único |
| `user_id` | UUID | Referência ao autor do reporte |
| `titulo` | TEXT | Título do problema |
| `descricao` | TEXT | Descrição detalhada da ocorrência |
| `endereco` | TEXT | Endereço aproximado do problema |
| `latitude` | DOUBLE PRECISION | Latitude da ocorrência |
| `longitude` | DOUBLE PRECISION | Longitude da ocorrência |
| `foto_url` | TEXT | URL da imagem armazenada no bucket |
| `created_at` | TIMESTAMP | Data de criação do reporte |

### 6.2 Arquivo de Configuração do App

Cada integrante deve configurar manualmente o arquivo abaixo **antes de compilar o projeto**:

```
app/src/main/java/com/example/reporteurbano/SupabaseConfig.java
```

Preencha com as credenciais do seu projeto no Supabase:

```java
public class SupabaseConfig {
    public static final String SUPABASE_URL    = "https://seu-projeto.supabase.co";
    public static final String SUPABASE_ANON_KEY = "sua_publishable_key_aqui";
    public static final String STORAGE_BUCKET  = "reportes-fotos";
}
```

> **⚠️ Importante:**
> - Use **somente** a `anon key` / `publishable key` do seu projeto
> - **Nunca** utilize a `secret key` ou a chave `service_role`
> - **Nunca** publique credenciais reais no repositório, mantenha apenas valores de exemplo no arquivo versionado e configure localmente as credenciais do seu projeto

---

## 7. Instalação

```bash
# 1. Clone o repositório
git clone https://github.com/luanwcs84/ReporteUrbano.git

# 2. Abra o projeto no Android Studio
#    File → Open → selecione a pasta ReporteUrbano/

# 3. Configure o arquivo SupabaseConfig.java com suas credenciais (ver seção 6)

# 4. Sincronize as dependências do Gradle
#    No Android Studio: File → Sync Project with Gradle Files
```

---

## 8. Execução Local

### Via Android Studio

1. Conecte um dispositivo Android via USB com **Depuração USB** habilitada, ou inicie um emulador com câmera e GPS configurados
2. Selecione o dispositivo no seletor de dispositivos do Android Studio
3. Clique em **▶ Run** (Shift + F10)
4. O app será compilado e instalado automaticamente

> **Dica:** Para testar GPS em emulador, utilize o painel de controle de localização virtual do Android Studio (Extended Controls → Location).

---

## 9. Usuários de Teste

Os seguintes usuários já estão cadastrados no ambiente de testes e podem ser utilizados para validação das funcionalidades:

### Administrador

| Campo | Valor |
|---|---|
| **E-mail** | `adminreporteurbano@gmail.com` |
| **Senha** | `urbrepadmin` |
| **Papel** | Administrador (acesso a todos os reportes) |

### Usuários Comuns

| E-mail | Senha |
|---|---|
| `pedro@gmail.com` | `123456` |
| `dianadasilva@gmail.com` | `123456` |
| `murilo@gmail.com` | `123456` |

> **ℹ️ Nota:** As senhas acima são exclusivas para o ambiente de teste acadêmico. Não reutilize essas credenciais em sistemas de produção.

---

## 10. Testes e Qualidade

### Testes Funcionais (Manuais)

Os seguintes fluxos foram validados manualmente no dispositivo físico e no emulador:

| Fluxo | Status |
|---|---|
| Cadastro de novo usuário | ✅ Validado |
| Login com usuário existente | ✅ Validado |
| Captura de foto pela câmera | ✅ Validado |
| Obtenção de coordenadas GPS | ✅ Validado |
| Criação de novo reporte | ✅ Validado |
| Exibição de pinos no mapa | ✅ Validado |
| Exclusão de reporte próprio | ✅ Validado |
| Acesso ao painel de administrador | ✅ Validado |
| Listagem de todos os reportes (admin) | ✅ Validado |

### Boas Práticas Adotadas

- Verificação de permissões em tempo de execução para câmera (`CAMERA`) e localização (`ACCESS_FINE_LOCATION`)
- Tratamento de erros nas chamadas à API do Supabase (callbacks de erro e feedback visual ao usuário)
- Uso de `AsyncTask` / threads em background para operações de rede, evitando bloqueio da UI
- Validação de campos obrigatórios antes do envio de formulários

---

## 11. Deploy / Geração do APK

### APK de Debug (Desenvolvimento)

```bash
./gradlew assembleDebug
```

O arquivo gerado estará em:
```
app/build/outputs/apk/debug/app-debug.apk
```

### APK de Release (Distribuição)

1. No Android Studio, acesse: **Build → Generate Signed Bundle / APK**
2. Selecione **APK**
3. Crie ou selecione um **Keystore** existente
4. Preencha as informações de assinatura
5. Selecione o build variant **release**
6. Clique em **Finish**

O APK assinado estará em:
```
app/build/outputs/apk/release/app-release.apk
```

> **ℹ️ Nota:** Para publicação na Google Play Store, o formato recomendado é o **Android App Bundle (`.aab`)**, gerado via `./gradlew bundleRelease`.

---

## 12. Documentação e Evidências

### Capturas de Tela

| Tela | Descrição |
|---|---|
| Criar Nova Conta | Formulário de cadastro com nome, e-mail e senha |
| Mapa Interativo | Exibição dos pinos de reportes no modo administrador |
| Menu Lateral | Painel de navegação com opções de perfil e acesso a reportes |

> As imagens de evidência do funcionamento do app estão disponíveis na pasta `/docs/screenshots/` do repositório.

### Repositório

🔗 [github.com/luanwcs84/ReporteUrbano](https://github.com/luanwcs84/ReporteUrbano)

### Referência Curricular

- **Disciplina:** Desenvolvimento para Plataformas Móveis
- **Código:** N700
- **Período:** 2026.1

---

## 13. Colaboração

### Equipe — Grupo 16

| Integrante | GitHub |
|---|---|
| Joao Lino | [@linocreates](https://github.com/linocreates) |
| Luan Wesley | [@luanwcs84](https://github.com/luanwcs84) |
| Anderson Guimarães | [@guimaraesander](https://github.com/guimaraesander) |
| Atila Gois | [@atilagois](https://github.com/atilagois) |
| Larissa Fernandes | [@larissa-fernandess](https://github.com/larissa-fernandess) |
| Anderson Ferreira | [@pherreiras](https://github.com/pherreiras) |

### Fluxo de Trabalho

- **Versionamento:** Git com repositório centralizado no GitHub
- **Branches:** Os integrantes trabalharam nas branches de (`develop`), (`mapa`) e (`guimass`) com merge via Pull Request para a branch `main`
- **Commits:** Convenção de commits semânticos (`feat:`, `fix:`, `docs:`, `refactor:`, etc.)
- **Comunicação:** WhatsApp para alinhamentos rápidos e reuniões síncronas semanais para revisão do progresso

---

## 14. Licença

Este projeto é desenvolvido para fins **acadêmicos** no âmbito da disciplina N700 — Desenvolvimento para Plataformas Móveis.

```

Copyright (c) 2026 Grupo 16 — Reporte Urbano

```

---

<div align="center">
  <p>© 2026 Grupo 16 · Todos os direitos reservados</p>
</div>
