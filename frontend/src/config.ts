export const config = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  noclipFrontendUrl: (window as any).__APP_CONFIG__?.noclipFrontendUrl ?? 'http://localhost:8000',
};
