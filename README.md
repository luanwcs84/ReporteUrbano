# ReporteUrbano

Aplicativo Android para registrar problemas urbanos com foto, descricao e localizacao, permitindo acompanhar as ocorrencias no mapa, em lista e em um modo administrativo.

## Visao Geral
O ReporteUrbano foi desenvolvido para facilitar o registro de problemas encontrados na cidade, como lixo acumulado, falhas na iluminacao publica, buracos e outras ocorrencias urbanas. O aplicativo permite que usuarios criem uma conta, enviem reportes com imagem e endereco, e acompanhem os registros diretamente no celular.

## Destaques
- Cadastro e login com Supabase Auth
- Reportes com foto, descricao e localizacao
- Armazenamento remoto de dados e imagens
- Visualizacao em lista e no mapa
- Exclusao de reportes e arquivos enviados
- Diferenciacao entre usuario comum e administrador

## Funcionalidades
### Usuario comum
- Criar conta e fazer login
- Registrar um problema com titulo, descricao, foto e endereco
- Visualizar os proprios reportes
- Excluir os proprios reportes

### Administrador
- Fazer login com conta marcada como `admin`
- Visualizar todos os reportes cadastrados
- Identificar o autor de cada reporte
- Usar interface visual diferenciada no app

## Tecnologias Utilizadas
- Android Studio
- Java
- Supabase Auth
- Supabase Database (PostgreSQL)
- Supabase Storage
- OSMDroid
- Gradle
- Git e GitHub


## Estrutura no Supabase
O projeto utiliza os seguintes recursos no Supabase:
- Tabela `profiles`
- Tabela `reportes`
- Bucket `reportes-fotos`
- Policies de acesso para usuario comum e administrador

### Campos principais da tabela `profiles`
- `id`
- `email`
- `nome`
- `role`

### Campos principais da tabela `reportes`
- `id`
- `user_id`
- `titulo`
- `descricao`
- `endereco`
- `latitude`
- `longitude`
- `foto_url`
- `created_at`

## Configuracao do Supabase
Cada integrante precisa configurar manualmente o arquivo abaixo antes de rodar o projeto:

`app/src/main/java/com/example/reporteurbano/SupabaseConfig.java`

Preencha com:
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY` (publishable key)
- `STORAGE_BUCKET`

Exemplo:

```java
public static final String SUPABASE_URL = "https://seu-projeto.supabase.co";
public static final String SUPABASE_ANON_KEY = "sua_publishable_key";
public static final String STORAGE_BUCKET = "reportes-fotos";
```

Importante:
- Nao usar `secret key`
- Nao usar `service_role`
- Usar somente a chave publica do projeto

## Como Executar
### 1. Atualizar o projeto
```powershell
git checkout develop
git pull origin develop
```

### 2. Abrir no Android Studio
- Abrir a pasta do projeto
- Esperar o Gradle sincronizar
- Configurar o `SupabaseConfig.java`

### 3. Compilar
```powershell
.\gradlew.bat assembleDebug
```

### 4. Instalar em dispositivo
```powershell
.\gradlew.bat installDebug
```

## Testes Realizados
Os seguintes fluxos foram testados com sucesso em dispositivo Android real:
- Cadastro de usuario
- Cadastro com e-mail ja existente
- Cadastro com senhas diferentes
- Login com e-mail inexistente
- Login com senha incorreta
- Login como administrador
- Criacao de reporte com foto e localizacao
- Visualizacao em lista
- Visualizacao no mapa
- Exclusao de reporte
- Exclusao da imagem no storage

## Modo Administrador
A conta de administrador consegue:
- Visualizar todos os reportes
- Ver o autor de cada registro
- Navegar com interface visual diferenciada

A definicao de administrador e feita no Supabase, na tabela `profiles`, usando o campo `role` com valor `admin`.

## Observacoes Para a Equipe
- Testar preferencialmente em celular fisico
- Configurar o Supabase antes de executar o app
- Se algum autor nao aparecer corretamente, conferir a tabela `profiles`
- Se houver erro de login, verificar se a conta existe no Supabase Auth e se o perfil correspondente esta criado

## Integrantes
- Preencher com os nomes e matriculas da equipe

## Repositorio
- Atualizar com o link oficial do GitHub do projeto
