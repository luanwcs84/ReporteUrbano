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
9. [Testes e Qualidade](#9-testes-e-qualidade)
10. [Deploy / Geração do APK](#10-deploy--geração-do-apk)
11. [Documentação e Evidências](#11-documentação-e-evidências)
12. [Equipe e Repositório](#12-equipe-e-repositório)
13. [Licença](#13-licença)

---

## 1. Objetivo

O **Reporte Urbano** é um trabalho acadêmico desenvolvido para a disciplina **Desenvolvimento para Plataformas Móveis (N700)**. O projeto tem como foco o desenvolvimento de uma aplicação nativa Android que permite a cidadãos registrar problemas urbanos, como buracos, iluminação defeituosa, descarte irregular de lixo e outros, com foto e localização do dispositivo.

Os objetivos técnicos centrais do projeto são:

- praticar desenvolvimento nativo Android com **Java** e **Android SDK**;
- utilizar recursos reais do dispositivo, como **câmera** e **localização**;
- aplicar princípios de **Material Design** na interface;
- integrar autenticação, banco de dados e armazenamento de arquivos com **Supabase**;
- exibir os reportes em um **mapa interativo** com pinos georreferenciados.

---

## 2. Stack

### Front-end (Mobile Nativo)

| Tecnologia | Função |
|---|---|
| Java | Linguagem principal de desenvolvimento |
| Android SDK | APIs nativas de câmera, localização e ciclo de vida |
| XML (Layouts) | Definição das interfaces de usuário |
| Material Design | Componentes visuais e diretrizes de UX |
| osmdroid | Biblioteca de mapas interativos baseada em OpenStreetMap |

### Back-end as a Service (BaaS)

| Serviço | Função |
|---|---|
| Supabase Auth | Cadastro, login e gerenciamento de sessões |
| Supabase Database (PostgreSQL) | Armazenamento de reportes e perfis de usuários |
| Supabase Storage | Upload e acesso às fotos dos reportes |
| Supabase Row Level Security (RLS) | Políticas de acesso por perfil (`user` / `admin`) |

---

## 3. Estrutura do Projeto

```text
ReporteUrbano/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/reporteurbano/
│   │       │   ├── SupabaseConfig.java
│   │       │   ├── MainActivity.java
│   │       │   ├── CadastroActivity.java
│   │       │   ├── HomeActivity.java
│   │       │   ├── NovoReporteActivity.java
│   │       │   ├── MeusReportesActivity.java
│   │       │   ├── AuthUser.java
│   │       │   ├── Reporte.java
│   │       │   ├── ReporteAdapter.java
│   │       │   ├── SessionManager.java
│   │       │   ├── SupabaseAuthService.java
│   │       │   ├── SupabaseReporteService.java
│   │       │   ├── SupabaseStorageService.java
│   │       │   ├── SupabaseProfileService.java
│   │       │   ├── SupabaseCallback.java
│   │       │   ├── DatabaseHelper.java
│   │       │   └── LoadingUtils.java
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── drawable/
│   │       │   └── values/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── gradle.properties
└── README.md
```

> **Atenção:** as credenciais reais do Supabase não ficam no código-fonte versionado. O projeto lê esses dados do arquivo `local.properties`, que é local e ignorado pelo Git.

---

## 4. Funcionalidades

### Usuário Comum

- **Cadastro e Login** — criação de conta com e-mail e senha via Supabase Auth
- **Captura de Foto** — integração com a câmera nativa do dispositivo no momento do registro
- **Uso da Localização** — utilização da localização atual do dispositivo para auxiliar no preenchimento do endereço
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

- **Android Studio** compatível com o projeto;
- **JDK 11** ou superior;
- **Android SDK** com API mínima 26;
- **Conta no Supabase** — [supabase.com](https://supabase.com/);
- **Dispositivo físico ou emulador** para execução do app.

---

## 6. Configuração do Supabase

### 6.1 Estrutura no Banco de Dados

O projeto utiliza os seguintes recursos no Supabase:

- tabela `profiles`;
- tabela `reportes`;
- bucket `reportes-fotos` no Supabase Storage;
- policies de Row Level Security (RLS) para usuário comum e administrador.

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

### 6.2 Configuração Local do App

Para executar o projeto, é necessário configurar manualmente o arquivo `local.properties`, na raiz do projeto, **antes de compilar e executar a aplicação**.

Além do caminho do Android SDK, o arquivo deve conter:

```properties
sdk.dir=C\:\\Users\\SEU_USUARIO\\AppData\\Local\\Android\\Sdk
supabase.url=https://seu-projeto.supabase.co
supabase.anon.key=sua_publishable_key_aqui
supabase.storage.bucket=reportes-fotos
```

O aplicativo lê esses valores localmente durante o build, sem expor credenciais reais no código versionado.

Se o arquivo ainda não existir, crie um `local.properties` na raiz do projeto e inclua também o caminho do Android SDK.

> **Importante:**
> - Use somente a `anon key` / `publishable key` do projeto
> - Nunca utilize a `secret key` ou a chave `service_role`
> - O arquivo `local.properties` não sobe para o Git
> - As credenciais do projeto devem ser configuradas localmente antes da execução

---

## 7. Instalação

```bash
# 1. Clone o repositório
git clone https://github.com/luanwcs84/ReporteUrbano.git

# 2. Abra o projeto no Android Studio
#    File -> Open -> selecione a pasta ReporteUrbano/

# 3. Configure o arquivo local.properties com suas credenciais (ver seção 6)

# 4. Sincronize as dependências do Gradle
#    No Android Studio: File -> Sync Project with Gradle Files
```

---

## 8. Execução Local

### Via Android Studio

1. Conecte um dispositivo Android via USB com **Depuração USB** habilitada, ou inicie um emulador.
2. Selecione o dispositivo no seletor de dispositivos do Android Studio.
3. Clique em **Run**.
4. O app será compilado e instalado automaticamente.

> **Dica:** para testar localização em emulador, utilize o painel de localização virtual do Android Studio.

---

## 9. Testes e Qualidade

### Testes Funcionais (Manuais)

Os seguintes fluxos foram validados manualmente:

| Fluxo | Status |
|---|---|
| Cadastro de novo usuário | ✅ Validado |
| Login com usuário existente | ✅ Validado |
| Captura de foto pela câmera | ✅ Validado |
| Obtenção da localização | ✅ Validado |
| Criação de novo reporte | ✅ Validado |
| Exibição de pinos no mapa | ✅ Validado |
| Exclusão de reporte próprio | ✅ Validado |
| Acesso ao painel de administrador | ✅ Validado |
| Listagem de todos os reportes (admin) | ✅ Validado |

### Boas Práticas Adotadas

- verificação de permissões em tempo de execução para localização (`ACCESS_FINE_LOCATION`);
- tratamento de erros nas chamadas à API do Supabase;
- uso de operações assíncronas e threads em background para chamadas de rede;
- validação de campos obrigatórios antes do envio de formulários.

---

## 10. Deploy / Geração do APK

### APK de Debug

```bash
./gradlew assembleDebug
```

O arquivo gerado estará em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### APK de Release

1. No Android Studio, acesse **Build -> Generate Signed Bundle / APK**.
2. Selecione **APK**.
3. Crie ou selecione um **Keystore**.
4. Preencha as informações de assinatura.
5. Selecione o build variant **release**.
6. Clique em **Finish**.

O APK assinado estará em:

```text
app/build/outputs/apk/release/app-release.apk
```

> Para publicação na Google Play Store, o formato recomendado é o **Android App Bundle (`.aab`)**.

---

## 11. Documentação e Evidências

### Capturas de Tela

| Tela | Descrição |
|---|---|
| Criar Nova Conta | Formulário de cadastro com nome, e-mail e senha |
| Mapa Interativo | Exibição dos pinos de reportes no modo administrador |
| Menu Lateral | Painel de navegação com opções de perfil e acesso a reportes |

As imagens de evidência do funcionamento do app estão disponíveis na pasta `/docs/images/` do repositório.

### Repositório

🔗 [github.com/luanwcs84/ReporteUrbano](https://github.com/luanwcs84/ReporteUrbano)

### Referência Curricular

- **Disciplina:** Desenvolvimento para Plataformas Móveis
- **Código:** N700
- **Período:** 2026.1

---

## 12. Equipe e Repositório

### Equipe — Grupo 16

| Integrante | GitHub |
|---|---|
| Joao Lino | [@linocreates](https://github.com/linocreates) |
| Luan Wesley | [@luanwcs84](https://github.com/luanwcs84) |
| Anderson Guimarães | [@guimaraesander](https://github.com/guimaraesander) |
| Atila Gois | [@atilagois](https://github.com/atilagois) |
| Larissa Fernandes | [@larissa-fernandess](https://github.com/larissa-fernandess) |
| Anderson Ferreira | [@pherreiras](https://github.com/pherreiras) |

### Repositório

- **GitHub:** [luanwcs84/ReporteUrbano](https://github.com/luanwcs84/ReporteUrbano)
- **Versionamento:** Git
- **Fluxo de integração:** Pull Requests

---

## 13. Licença

Este projeto é desenvolvido para fins **acadêmicos** no âmbito da disciplina N700 — Desenvolvimento para Plataformas Móveis.

---

<div align="center">
  <p>© 2026 Grupo 16 · Todos os direitos reservados</p>
</div>
