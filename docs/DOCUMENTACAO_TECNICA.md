# Solidari — Documentação Técnica (Opção B: Aprimoramento da Solução Kotlin)

## 1. Justificativa técnica da escolha

O Solidari já tinha, na entrega anterior, quatro telas em Jetpack Compose (Login,
Home, Localização e Cashback) com identidade visual definida e boa parte da
UI de alta fidelidade pronta. O que faltava não era desenhar mais telas, e sim
ligar as que já existiam: cada uma era montada isoladamente pela `MainActivity`
(que só abria a `CashbackScreen` de forma fixa) e a bottom bar era redesenhada
três vezes, uma em cada tela, sem nenhuma navegação real por trás dos botões.

Por isso a Opção B (aprimorar a base Kotlin existente) fez mais sentido do que
recomeçar em outra stack: o investimento em Compose e Material 3 já estava
feito, o risco técnico de trocar de tecnologia não trazia benefício nenhum
pra esta entrega, e havia um problema concreto e visível pra resolver —
transformar telas bonitas, mas soltas, num app que realmente se navega.

## 2. Alinhamento com os objetivos do projeto

A proposta do Solidari é reduzir o atrito entre a vontade de ajudar e a ação
de doar. Um app onde os botões de "Doar Fundos" ou "Ajudar Agora" não levam a
lugar nenhum é exatamente o tipo de atrito que o projeto se propõe a eliminar.
Fechar o ciclo de navegação (Login → Home → Localização/Cashback/Perfil/Opções)
e dar retorno visual imediato a cada ação de doação são mudanças pequenas em
volume de código, mas que atacam direto o problema que o Solidari existe pra
resolver.

## 3. Mudanças implementadas nesta atividade

- **Navegação real com Navigation Compose.** Criado um `NavHost` único
  (`navigation/SolidariNavGraph.kt`) com rotas para Login, Home, Localização,
  Cashback, Perfil e Opções. A `MainActivity` deixou de abrir uma tela fixa e
  passou a apenas hospedar o grafo de navegação.
- **Bottom bar unificada.** As três implementações divergentes de barra
  inferior (uma em `HomeScreen`, outra em `LocationScreen`, sem nenhuma em
  `CashbackScreen`) foram substituídas por um único componente
  (`navigation/SolidariBottomBar.kt`), que já destaca a aba correspondente à
  rota atual e efetivamente navega ao ser tocado.
- **Login conectado ao app.** O botão de entrar, que já existia como callback
  na `LoginScreen` mas não ia a lugar nenhum, agora navega para a Home e limpa
  a tela de login da pilha de navegação (sem permitir voltar pra ela com o
  botão "voltar" do sistema).
- **Feedback nas ações de doação.** Botões que antes eram apenas decorativos
  (`Doar Fundos`, `Ajudar Agora`, `Entregar` na Home; `Doar Crédito` e
  `Histórico` no Cashback) agora respondem com uma Snackbar de confirmação,
  dando ao usuário a certeza de que a ação foi registrada.
- **Telas de Perfil e Opções.** A bottom bar sempre prometeu essas duas abas
  visualmente; agora elas existem de fato como destinos navegáveis, ainda que
  simples, deixando claro o que fica para a próxima fase.
- **Limpeza de código.** Remoção das duplicidades de bottom bar (`BottomBar`/
  `BottomItem` na Home, `BottomNavigationSection` na Localização) e dos
  imports que ficaram sem uso depois da refatoração.

## 4. Roadmap tecnológico

**Concluído (entregas anteriores)**
- Telas de Login, Home, Localização e Cashback com identidade visual (Material 3,
  paleta verde, cards e chips) e dados de exemplo.
- Carregamento de imagens remotas com Coil na tela de Cashback.

**Em andamento (esta entrega)**
- Navegação real entre todas as telas via Navigation Compose.
- Bottom bar única e reutilizável, com destaque de aba ativa.
- Feedback de interação (Snackbar) nos principais botões de doação.
- Telas de Perfil e Opções como destinos navegáveis.

**Planejado (próximas fases)**
- Tela de detalhes da causa, recebendo o ID da causa como argumento de rota.
- Consumo de uma API real para as "Causas Urgentes" e os pontos do mapa,
  substituindo os dados mockados que hoje vivem dentro de cada tela.
- Estado de usuário compartilhado (ex.: `ViewModel`) no lugar dos dados de
  exemplo hardcoded em `CashbackScreen`.
- Persistência local de sessão e da última localização conhecida.
- Unificação da paleta de cores num único arquivo de tema — hoje `HomeScreen`
  e `LocationScreen` ainda definem verdes ligeiramente diferentes do que está
  em `ui/theme/Color.kt`.
