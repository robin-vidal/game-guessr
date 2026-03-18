/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly NOCLIP_FRONTEND_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
