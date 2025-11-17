# Configuração do JavaFX e Resolução de Problemas

Este documento detalha o processo de configuração do JavaFX no projeto e os desafios comuns encontrados, servindo como um guia para futuras instalações ou depurações.

---

## 1. O Desafio da Configuração do JavaFX

A configuração do JavaFX em projetos Java modernos (JDK 11+) pode ser complexa devido a três fatores principais:

1.  **JavaFX Fora do JDK:** A partir do Java 11, o JavaFX foi removido do JDK padrão e se tornou uma biblioteca separada (OpenJFX). Isso significa que ele não é mais "embutido" e precisa ser adicionado manualmente.
2.  **Sistema de Módulos (JPMS):** O Java Platform Module System, introduzido no Java 9, é rigoroso. Ele exige que as bibliotecas sejam carregadas como módulos nomeados, o que contrasta com o antigo e permissivo "Classpath".
3.  **Incompatibilidade de Versões:** A versão do JavaFX SDK deve ser compatível com a versão do JDK em uso (ex: JavaFX 21 para JDK 21, JavaFX 25 para JDK 25).

---

## 2. Erros Comuns Encontrados

Durante o processo de configuração, os seguintes erros foram observados:

-   `JavaFX runtime components are missing`: Indica que a JVM não conseguiu encontrar ou carregar os módulos do JavaFX.
-   `Error initializing QuantumRenderer: no suitable pipeline found`: Problema na inicialização do subsistema gráfico do JavaFX, geralmente relacionado a drivers ou aceleração de hardware.
-   `java.lang.module.FindException: Module <nome_do_modulo> not found`: O Java não conseguiu encontrar um módulo específico (ex: `com.google.gson`, `Notas`).
-   `java.lang.module.InvalidModuleDescriptorException: Aluno.class found in top-level directory (unnamed package not allowed in module)`: O projeto estava sendo executado como um módulo Java, mas havia classes no pacote padrão (sem `package` declarado), o que não é permitido.
-   `java: cannot access javafx.event.EventTarget class file for javafx.event.EventTarget not found`: Módulo `javafx.graphics` ausente ou não carregado.
-   `java: <campo> has private access in <classe>`: Erros de acesso a campos privados de DTOs, resolvidos com o uso de getters/setters.
-   `java: incompatible types: java.util.List<Turma> cannot be converted to java.util.List<model.Turma>`: Inconsistência na resolução de tipos devido a problemas de cache ou compilação.

---

## 3. A Solução Final para Execução da GUI

A configuração que permitiu a execução bem-sucedida da `AppGUI` envolveu o uso de **VM Options** específicas na configuração de execução do IntelliJ IDEA, combinada com a estrutura de pacotes correta e a remoção do `module-info.java` (para operar no modo de classpath, que é mais flexível para o IntelliJ).

### 3.1. Estrutura de Pacotes

Todas as classes do projeto foram organizadas em pacotes nomeados (`app`, `dao`, `dto`, `gui`, `model`, `service`, `tools`). Não há classes no pacote padrão.

### 3.2. Configuração das VM Options (IntelliJ IDEA)

Para executar a `AppGUI`, a seguinte linha deve ser configurada no campo **`VM options`** da sua configuração de execução (`Run` > `Edit Configurations...` > `AppGUI`):

```
-Dprism.order=sw --enable-native-access=ALL-UNNAMED --module-path "CAMINHO_EXATO_PARA_SEU_JAVAFX_SDK_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base
```

**Explicação dos Argumentos:**

-   `-Dprism.order=sw`: Força o JavaFX a usar o renderizador de software (`sw`) em vez de tentar a aceleração de hardware. Isso resolve o erro `Error initializing QuantumRenderer: no suitable pipeline found`.
-   `--enable-native-access=ALL-UNNAMED`: Concede permissão para que o JavaFX acesse componentes nativos do sistema, resolvendo avisos e erros relacionados a acesso restrito.
-   `--module-path "CAMINHO_EXATO_PARA_SEU_JAVAFX_SDK_LIB"`: Indica à JVM onde encontrar os arquivos JAR dos módulos do JavaFX. **Substitua `"CAMINHO_EXATO_PARA_SEU_JAVAFX_SDK_LIB"` pelo caminho completo da pasta `lib` do seu JavaFX SDK (ex: `D:\Local\Dev\Java\javafx-sdk-25.0.1\lib`).**
-   `--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base`: Informa à JVM quais módulos específicos do JavaFX a aplicação precisa carregar.

### 3.3. Configuração do Projeto no IntelliJ IDEA

-   **JDK Compatível:** Certifique-se de que a versão do JavaFX SDK (ex: 25.0.1) é compatível com a versão do JDK do projeto (ex: JDK 25).
-   **Bibliotecas Adicionadas:** O `javafx-sdk-<versao>/lib` deve ser adicionado como uma biblioteca ao projeto (`File` > `Project Structure...` > `Libraries`).
-   **Remoção de `module-info.java`:** O arquivo `module-info.java` foi removido para que o projeto opere no modo de classpath tradicional, que é mais flexível para o IntelliJ gerenciar com as VM Options.

---

## 4. Passos Essenciais para Resolver Problemas de Compilação/Execução

Sempre que encontrar erros persistentes de compilação ou execução, especialmente após grandes mudanças de estrutura ou dependências, siga estes passos:

1.  **Verificar Estrutura de Pastas:** Confirme que todos os arquivos `.java` estão nas pastas corretas, correspondendo às suas declarações `package`.
2.  **Verificar Declarações `package` e `import`:** Garanta que todos os arquivos `.java` tenham a declaração `package` correta no topo e que todos os `import`s estejam apontando para os pacotes certos.
3.  **Reconstruir o Projeto:** No IntelliJ IDEA, vá em `Build` > `Rebuild Project`.
4.  **Invalidar Caches e Reiniciar:** No IntelliJ IDEA, vá em `File` > `Invalidate Caches / Restart...` e selecione "Invalidate and Restart".

Seguir estes passos garante que o IDE e o compilador estejam sempre com o estado mais atualizado e consistente do projeto.
