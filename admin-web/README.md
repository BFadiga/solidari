# Solidari Admin

Dashboard administrativo em Angular do sistema Solidari, consumindo a mesma
API REST (`backend/`) usada pelo aplicativo mobile.

## Telas

- `/home`: página inicial de boas-vindas.
- `/admin`: login de administrador, listagem de parceiros e cadastro de
  novos parceiros.

## Como rodar

Com o backend rodando em `http://localhost:8080`:

```bash
npm install
npm start
```

A aplicação sobe em `http://localhost:4200`.

Login de administrador padrão (criado pelo `DataSeeder` do backend):

- E-mail: `admin@solidari.com`
- Senha: `admin123`
